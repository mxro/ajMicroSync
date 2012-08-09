/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.microsync.internal.ui;

import aj.apps.microsync.internal.ui.AjLogin;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import one.core.domain.OneClient;
import one.core.dsl.callbacks.WhenLoaded;
import one.core.dsl.callbacks.results.WithLoadResult;
import one.core.dsl.callbacks.results.WithUserRegisteredResult;

/**
 *
 * @author mroh004
 */
public class MainFrame extends javax.swing.JFrame {

    SyncPanel syncPanel;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("appjangle MicroSync");

        contentPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                final MainFrame mf = new MainFrame();
                mf.setSize(500, 400);
                mf.setLocation(600, 400);
                mf.setVisible(true);

                final AjLogin loginForm = new AjLogin(new AjLogin.WhenLoggedIn() {

                    public void thenDo(final OneClient client, final Component p_loginForm, final WithUserRegisteredResult wurr) {
                        mf.contentPanel.remove(p_loginForm);
                        mf.contentPanel.validate();
                        mf.contentPanel.revalidate();
                        
                        client.one().load(wurr.userNodeUri()).withSecret(wurr.userNodeSecret()).in(client).and(new WhenLoaded() {

                            @Override
                            public void thenDo(WithLoadResult<Object> wlr) {
                               

                                mf.contentPanel.add(new SyncPanel(client, wurr), BorderLayout.CENTER);

                                mf.contentPanel.validate();
                                mf.contentPanel.revalidate();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                super.onFailure(t);
                            }
                        });



                    }
                });

                mf.contentPanel.add(loginForm);

                loginForm.setVisible(true);

            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    // End of variables declaration//GEN-END:variables
}
