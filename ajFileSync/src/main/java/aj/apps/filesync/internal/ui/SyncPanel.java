/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.filesync.internal.ui;

import aj.apps.filesync.internal.AjFileSyncData;
import aj.apps.filesync.internal.DataService;
import aj.apps.filesync.internal.LogService;
import aj.apps.filesync.internal.engine.SyncEngine;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.TransferHandler;
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
                        
                        messages.setText(messages.getText()+"\n"+text);
                         
                    }
                    
                };
    
    public void doSync() {
        DefaultListModel model = (DefaultListModel) directories.getModel();
        
        for (final Object elem: model.toArray()) {
            
            if (elem instanceof String) {
                
                try {
                
                SyncEngine.processFile(new File((String) elem), dataService, logService , new SyncEngine.WhenFilesProcessed() {

                    public void onSuccess() {
                        logService.note("File processed: "+(String) elem );
                    }

                    public void onFailure(Throwable t) {
                        logService.note(t.getMessage());
                    }
                });
                
                } catch (Exception e ) {
                    logService.note(e.getMessage());
                }
               
                
                
            }
            
            
        }
        
        
    }
    
    /**
     * Creates new form SyncPanel
     */
    public SyncPanel(OneClient client, WithUserRegisteredResult wurr) {
        this.registrationInfos = wurr;
        initComponents();

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
                    model.addElement(file.getAbsolutePath());
                }
                return true;
            }
        };
        this.directories.setTransferHandler(handler);

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
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        messages = new javax.swing.JTextArea();
        forceSyncButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        jLabel1.setText("Monitored Directories:");

        jScrollPane1.setViewportView(directories);

        jButton2.setText("Remove");

        jLabel2.setText("Messages");

        messages.setColumns(20);
        messages.setRows(5);
        jScrollPane2.setViewportView(messages);

        forceSyncButton.setText("Force Sync");
        forceSyncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forceSyncButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Add directories via drag and drop to box above!");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(forceSyncButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2))
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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(forceSyncButton)
                            .addComponent(jLabel3))
                        .addGap(26, 26, 26))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void forceSyncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forceSyncButtonActionPerformed
        doSync();
    }//GEN-LAST:event_forceSyncButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList directories;
    private javax.swing.JButton forceSyncButton;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea messages;
    // End of variables declaration//GEN-END:variables
}
