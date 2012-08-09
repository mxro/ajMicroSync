/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.filesync.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import one.client.jre.OneJre;
import one.core.domain.OneClient;
import one.core.dsl.CoreDsl;
import one.core.dsl.callbacks.WhenShutdown;
import one.core.dsl.callbacks.WhenUserLoggedIn;
import one.core.dsl.callbacks.results.WithChallengedContext;
import one.core.dsl.callbacks.results.WithUserRegisteredResult;
import one.core.dsl.grammars.LoginWithUserDetailsParameters;
import org.openide.util.NbPreferences;

/**
 *
 * @author mroh004
 */
public class AjLogin extends javax.swing.JPanel {

    private final WhenLoggedIn callback;

    public interface WhenLoggedIn {

        public void thenDo(OneClient client, Component loginForm, WithUserRegisteredResult wurr);
    }

    /**
     * Creates new form AjLogin
     */
    public AjLogin(WhenLoggedIn callback) {
        this.callback = callback;
        initComponents();
        
        final String email = NbPreferences.forModule(this.getClass()).node("login").get("email", null);
        final String password = NbPreferences.forModule(this.getClass()).node("login").get("password", null);
        
        if (email != null) {
            emailField.setText(email);
        }
        
        if (password != null) {
            passwordFiled.setText(password);
        }
        
        if (email != null && password != null) {
            loginButtonActionPerformed(null);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        detailsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        passwordFiled = new javax.swing.JPasswordField();
        jButton2 = new javax.swing.JButton();
        loginButton = new javax.swing.JButton();
        saveLoginData = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        jLabel3.setText("<html>You will need an appjangle account to use appjangle File Sync. <br/>If you do not have one yet, you can get a free account at <a href=\"http://appjangle.com/\">appjangle.com</a>!</html>");

        jLabel1.setText("E-Mail:");

        jLabel2.setText("Password:");

        jButton2.setText("Close");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        loginButton.setText("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        saveLoginData.setSelected(true);
        saveLoginData.setText("Save Login Data");

        javax.swing.GroupLayout detailsPanelLayout = new javax.swing.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(detailsPanelLayout.createSequentialGroup()
                                .addComponent(saveLoginData)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(emailField)
                            .addComponent(passwordFiled)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, detailsPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(loginButton)))
                .addContainerGap())
        );
        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(passwordFiled, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveLoginData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(loginButton))
                .addContainerGap(134, Short.MAX_VALUE))
        );

        add(detailsPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        JOptionPane.showMessageDialog(null, "You cannot use this application without signing in to appjangle.\nThis application will now close.");

        System.exit(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void showDetailsPanel() {
        this.add(this.detailsPanel, BorderLayout.CENTER);
       this.detailsPanel.setVisible(true);
        this.validate();
        this.revalidate();
                
    }
    
    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        
        if (saveLoginData.isSelected()) {
            NbPreferences.forModule(this.getClass()).node("login").put("email", emailField.getText());
            NbPreferences.forModule(this.getClass()).node("login").put("password", String.valueOf(passwordFiled.getPassword()));
        }
        this.remove(this.detailsPanel);

       final LogginInPanel lp = new LogginInPanel();
        this.add(lp, BorderLayout.CENTER);
        lp.setVisible(true);

        lp.validate();
        lp.revalidate();

        this.validate();
        this.revalidate();

        
        final CoreDsl dsl = OneJre.init();
        
        final OneClient c = dsl.createClient();
        
        dsl.loginUser(new LoginWithUserDetailsParameters() {

            public String getEmail() {
                return emailField.getText();
            }

            public String getPassword() {
                return String.valueOf(passwordFiled.getPassword());
            }

            public String getApplicationNodeUri() {
                return "https://u1.linnk.it/0fs7dr/Apps1/appjangle";
            }

            public String getApplicationNodeSecret() {
                return "";
            }

            public OneClient getClient() {
               return c;
            }

            public WhenUserLoggedIn getCallback() {
               return new WhenUserLoggedIn() {

                    public void thenDo(WithUserRegisteredResult wurr) {
                       remove(lp);
                        showDetailsPanel();
                        
                        callback.thenDo(c, AjLogin.this, wurr);
                    }

                    public void onChallenge(WithChallengedContext wcc) {
                        JOptionPane.showMessageDialog(null, "Unexpected challenge received.");
                        remove(lp);
                        showDetailsPanel();
                    }

                    public void onInvalidDetails() {
                        JOptionPane.showMessageDialog(null, "Invalid username and/or password.");
                        remove(lp);
                        showDetailsPanel();
                    }

                    public void onNotRegisteredForApplication() {
                       JOptionPane.showMessageDialog(null, "User is not registered for application.");
                        remove(lp);
                        showDetailsPanel();
                    }

                    public void onFailure(Throwable thrwbl) {
                       JOptionPane.showMessageDialog(null, "Unexpected error: "+thrwbl);
                        remove(lp);
                        showDetailsPanel();
                    }
                };
            }
        });


    }//GEN-LAST:event_loginButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JTextField emailField;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton loginButton;
    private javax.swing.JPasswordField passwordFiled;
    private javax.swing.JCheckBox saveLoginData;
    // End of variables declaration//GEN-END:variables
}
