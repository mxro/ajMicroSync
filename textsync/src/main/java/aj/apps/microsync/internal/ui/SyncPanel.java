/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.microsync.internal.ui;

import aj.apps.microsync.AjMicroSync;
import aj.apps.microsync.internal.*;
import aj.apps.microsync.internal.engine.FileCache;
import aj.apps.microsync.internal.engine.ParseTextProcess;
import aj.apps.microsync.internal.engine.ProcessFilesProcess;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.TransferHandler;
import one.async.joiner.CallbackLatch;
import one.core.domain.OneClient;
import one.core.dsl.CoreDsl;
import one.core.dsl.callbacks.WhenLoaded;
import one.core.dsl.callbacks.WhenShutdown;
import one.core.dsl.callbacks.results.WithLoadResult;
import one.core.dsl.callbacks.results.WithUserRegisteredResult;

/**
 *
 * @author mroh004
 */
public class SyncPanel extends javax.swing.JPanel {

    WithUserRegisteredResult registrationInfos;
    DataServiceFactory dataServiceFactory;
    FileCache fileCache = new FileCache();
    LogService logService = new LogService() {

        public void note(String text) {

            if (messages.getText().equals("")) {
                messages.setText(text);
            } else {
                messages.setText(messages.getText() + "\n" + text);
            }

            if (messages.getText().length() > 10000) {
                messages.setText(messages.getText().substring(messages.getText().length() - 9999, messages.getText().length()));
            }

            messages.setCaretPosition(messages.getText().length() - 1);
        }
    };
    private volatile boolean syncing = false;

    public void doSync() {

        if (syncing) {
            return;
        }

        syncing = true;

        forceSyncButton.setEnabled(false);
        final DefaultListModel model = (DefaultListModel) (directories.getModel());

        logService.note("Start Synchronization: " + new Date());

        dataServiceFactory.createDataService(new DataServiceFactory.WhenDataServiceCreated() {

            public void thenDo(final DataService dataService) {
                progressBar.setMaximum((model.getSize() * 2) + 1);
                progressBar.setValue(1);

                final AtomicBoolean active = new AtomicBoolean(true);

                final CallbackLatch latch = new CallbackLatch(model.getSize()) {

                    @Override
                    public void onCompleted() {
                        logService.note("  Winding down data service. " + new Date());
                        dataService.shutdown(new WhenShutdown() {

                            @Override
                            public void thenDo() {
                                progressBar.setValue(0);
                                active.set(false);
                                logService.note("Finished Synchronization: " + new Date());
                                forceSyncButton.setEnabled(true);
                                syncing = false;
                            }
                        });

                    }

                    @Override
                    public void onFailed(Throwable thrwbl) {
                        logService.note("  Exception occured: " + thrwbl.getMessage());
                        progressBar.setValue(0);
                        syncing = false;
                        forceSyncButton.setEnabled(true);
                        throw new RuntimeException(thrwbl);
                    }
                };



                Thread syncThread = new Thread(new Runnable() {

                    public void run() {
                        for (int i = 0; i <= model.getSize() - 1; i++) {

                            final String elem = model.get(i).toString();

                            try {

                                progressBar.setValue(progressBar.getValue() + 1);
                                ProcessFilesProcess.processFile(new File((String) elem), dataService, logService, fileCache, new ProcessFilesProcess.WhenFilesProcessed() {

                                    public void onSuccess() {
                                        progressBar.setValue(progressBar.getValue() + 1);

                                        latch.registerSuccess();
                                    }

                                    public void onFailure(Throwable t) {
                                        latch.registerFail(t);
                                    }
                                });

                            } catch (Exception e) {
                                logService.note(e.getMessage());
                                latch.registerFail(e);
                            }

                        }
                    }
                });

                Thread monitorSyncThread = new Thread(new Runnable() {

                    public void run() {
                        try {
                            Thread.sleep(240 * 1000); // wait for 4 minutes

                            if (active.get()) {
                                logService.note("Error: Synchronization timeout " + new Date());
                                progressBar.setValue(0);
                                forceSyncButton.setEnabled(true);
                                syncing = false;
                            }

                        } catch (InterruptedException ex) {
                            logService.note("  Unexpected exception: " + ex);
                        }
                    }
                });

                syncThread.start();
                monitorSyncThread.start();

            }

            public void onFailure(Throwable t) {
                logService.note(t.getMessage());
                throw new RuntimeException(t);
            }
        });




    }

    /**
     * Creates new form SyncPanel
     */
    public SyncPanel(CoreDsl dsl, WithUserRegisteredResult wurr) {
        this.registrationInfos = wurr;
        initComponents();

        restoreSelectedDirsFromPrefs();

        dataServiceFactory = new AjDataServiceFactory(dsl, wurr);

        this.directories.setDragEnabled(true);

        TransferHandler handler = new TransferHandler() {

            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                // we only import FileList
                if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport info) {
                if (!info.isDrop()) {
                    return false;
                }

                // Check for FileList flavor
                if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return false;
                }

                // Get the fileList that is being dropped.
                Transferable t = info.getTransferable();
                List<File> data;
                try {
                    data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (Exception e) {
                    return false;
                }
                DefaultListModel model = (DefaultListModel) directories.getModel();
                for (File file : data) {
                    //System.out.println("File added: "+file);
                    model.addElement(file.getAbsolutePath());
                }
                directories.repaint();

                saveSelectedDirsToPrefs();

                return true;
            }
        };
        this.directories.setTransferHandler(handler);

    }

    private void restoreSelectedDirsFromPrefs() {
        Preferences prefs = Preferences.userNodeForPackage(AjMicroSync.class);
        String dirs = prefs.get("dirs", null);
        if (dirs != null) {

            for (String dir : dirs.split(";;;")) {
                if (!dir.equals("")) {
                    ((DefaultListModel) directories.getModel()).addElement(dir);
                }
            }

        }

    }

    private void saveSelectedDirsToPrefs() {
        Preferences prefs = Preferences.userNodeForPackage(AjMicroSync.class);

        DefaultListModel model = (DefaultListModel) (directories.getModel());

        String dirs = "";
        for (int i = 0; i <= model.getSize() - 1; i++) {
            dirs = dirs + model.get(i).toString() + ";;;";
        }

        prefs.put("dirs", dirs);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        directories = new javax.swing.JList();
        removeButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        forceSyncButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        checkFilesButton = new javax.swing.JButton();

        jLabel1.setText("Monitored Files and Directories:");

        directories.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(directories);

        removeButton.setText("Remove File");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Messages");

        messages.setColumns(20);
        messages.setRows(5);
        jScrollPane2.setViewportView(messages);

        jLabel3.setText("Add files and directories via drag and drop to box above!");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        forceSyncButton.setText("Synchronize Now");
        forceSyncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forceSyncButtonActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Synchronize in Background");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(forceSyncButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(forceSyncButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton1.setText("Logout");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        checkFilesButton.setText("Check Files");
        checkFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFilesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addComponent(jScrollPane1)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(checkFilesButton)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(removeButton)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeButton)
                    .addComponent(jLabel3)
                    .addComponent(checkFilesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void forceSyncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forceSyncButtonActionPerformed
        fileCache = new FileCache();
        doSync();
    }//GEN-LAST:event_forceSyncButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        DefaultListModel model = (DefaultListModel) (directories.getModel());
        model.remove(directories.getSelectedIndex());
        saveSelectedDirsToPrefs();
    }//GEN-LAST:event_removeButtonActionPerformed
    Timer timer;
    TimerTask syncTask;

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed

        if (jCheckBox1.isSelected()) {
            if (timer != null) {
                return;
            }

            timer = new Timer();

            syncTask = new TimerTask() {

                @Override
                public void run() {
                    doSync();
                }
            };

            timer.scheduleAtFixedRate(syncTask, 10, 1000 * 20);



        } else {
            syncTask.cancel();
            timer.purge();

            timer = null;
            syncTask = null;
        }



    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Preferences prefs = Preferences.userNodeForPackage(AjMicroSync.class);
        prefs.remove("sessionId");

        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
        final Container parent = this.getParent();

        parent.remove(this);

        final Container destPanel = parent;
        AjLogin login = new AjLogin(new AjLogin.WhenLoggedIn() {

            public void thenDo(final OneClient client, final Component p_loginForm, final WithUserRegisteredResult wurr) {

                destPanel.remove(p_loginForm);
                destPanel.validate();
                //destPanel.revalidate();

                client.one().load(wurr.userNodeUri()).withSecret(wurr.userNodeSecret()).in(client).and(new WhenLoaded() {

                    @Override
                    public void thenDo(WithLoadResult<Object> wlr) {


                        destPanel.add(new SyncPanel(client.one(), wurr), BorderLayout.CENTER);

                        destPanel.validate();
                        // destPanel.revalidate();

                        client.one().shutdown(client).and(WhenShutdown.DO_NOTHING);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        super.onFailure(t);
                    }
                });
            }
        });
        parent.add(login, BorderLayout.CENTER);
        parent.validate();

    }//GEN-LAST:event_jButton1ActionPerformed

    private void doCheckFiles() {
        logService.note("Checking registered files (" + new Date() + ")");

        final DefaultListModel model = (DefaultListModel) (directories.getModel());
        {
            final List<Integer> toClear = new LinkedList();
            for (int i = 0; i <= model.getSize() - 1; i++) {
                final String filePath = model.get(i).toString();

                if (!new File(filePath).exists()) {
                    toClear.add(i);
                }
            }
            Collections.reverse(toClear);
            for (Integer idxToDelete : toClear) {
                logService.note("  Removed non existing file: " + model.get(idxToDelete).toString());
                model.remove(idxToDelete);

            }
        }

        logService.note("Checking for duplicate files.");

        {
            final Set<String> visited = new HashSet<String>(model.getSize());
            final List<Integer> toClear = new LinkedList();
            for (int i = 0; i <= model.getSize() - 1; i++) {
                final String filePath = model.get(i).toString();
                
                if (visited.contains(filePath)) {
                    toClear.add(i);
                }
                
                visited.add(filePath);
                
                
            }
            Collections.reverse(toClear);
            for (Integer idxToDelete : toClear) {
                logService.note("  Removed duplicate file: " + model.get(idxToDelete).toString());
                model.remove(idxToDelete);

            }
        }

        directories.revalidate();
        saveSelectedDirsToPrefs();
    }

    private void checkFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFilesButtonActionPerformed
        doCheckFiles();
    }//GEN-LAST:event_checkFilesButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checkFilesButton;
    private javax.swing.JList directories;
    private javax.swing.JButton forceSyncButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea messages;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
