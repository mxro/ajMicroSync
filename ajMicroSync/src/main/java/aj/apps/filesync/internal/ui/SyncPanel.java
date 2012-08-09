/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.filesync.internal.ui;

import aj.apps.microsync.AjFileSync;
import aj.apps.filesync.internal.AjFileSyncData;
import aj.apps.filesync.internal.DataService;
import aj.apps.filesync.internal.LogService;
import aj.apps.filesync.internal.engine.SyncEngine;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.TransferHandler;
import one.async.joiner.CallbackLatch;
import one.core.domain.OneClient;
import one.core.dsl.callbacks.results.WithUserRegisteredResult;

/**
 *
 * @author mroh004
 */
public class SyncPanel extends javax.swing.JPanel {

    WithUserRegisteredResult registrationInfos;
    DataService dataService;
    LogService logService = new LogService() {

                    public void note(String text) {
                        
                        messages.setText(text+"\n"+messages.getText());
                        
                        if (messages.getText().length() > 1000) {
                            messages.setText(messages.getText().substring(0, 999));
                        }
                         
                        messages.setCaretPosition(0);
                    }
                    
                };
    
    public void doSync() {
        
        forceSyncButton.setEnabled(false);
        DefaultListModel model = (DefaultListModel) (directories.getModel());

        progressBar.setMaximum((model.getSize()*2)+1);
        progressBar.setValue(1);
        final CallbackLatch latch = new CallbackLatch(model.getSize()) {

            @Override
            public void onCompleted() {
               progressBar.setValue(0);
               forceSyncButton.setEnabled(true);
            }

            @Override
            public void onFailed(Throwable thrwbl) {
                logService.note(thrwbl.getMessage());
                progressBar.setValue(0);
            }
        };
        
        for (int i=0; i<= model.getSize()-1; i++) {
            
            final String elem = model.get(i).toString();
  
                try {
                logService.note("Processing entry: "+elem);
                progressBar.setValue(progressBar.getValue()+1);
                SyncEngine.processFile(new File((String) elem), dataService, logService , new SyncEngine.WhenFilesProcessed() {

                    public void onSuccess() {
                        progressBar.setValue(progressBar.getValue()+1);
                        logService.note("Entry processed: "+(String) elem );
                        latch.registerSuccess();
                    }

                    public void onFailure(Throwable t) {
                        latch.registerFail(t);
                    }
                });
                
                } catch (Exception e ) {
                    logService.note(e.getMessage());
                }
                
        }
        
        
    }
    
    /**
     * Creates new form SyncPanel
     */
    public SyncPanel(OneClient client, WithUserRegisteredResult wurr) {
        this.registrationInfos = wurr;
        initComponents();

        restoreSelectedDirsFromPrefs();
        
        dataService = new AjFileSyncData(client, wurr);
        
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
        Preferences prefs = Preferences.userNodeForPackage(AjFileSync.class);
        String dirs = prefs.get("dirs", null);
        if (dirs != null) {
            
            for (String dir: dirs.split(";;;")) {
                if (!dir.equals("")) {
                    ((DefaultListModel) directories.getModel()).addElement(dir);
                }
            }
            
        }
        
    }
    
    private void saveSelectedDirsToPrefs() {
        Preferences prefs = Preferences.userNodeForPackage(AjFileSync.class);

        DefaultListModel model = (DefaultListModel) (directories.getModel());

        String dirs = "";
        for (int i=0; i<= model.getSize()-1; i++) {
            dirs = model.get(i).toString() +";;;";
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

        jLabel1.setText("Monitored Directories:");

        directories.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(directories);

        removeButton.setText("Remove");
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(forceSyncButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(35, 35, 35)
                        .addComponent(removeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeButton)
                    .addComponent(jLabel3))
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
        doSync();
    }//GEN-LAST:event_forceSyncButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
         DefaultListModel model = (DefaultListModel) (directories.getModel());
        model.remove(directories.getSelectedIndex());
       saveSelectedDirsToPrefs();   
    }//GEN-LAST:event_removeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList directories;
    private javax.swing.JButton forceSyncButton;
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
