/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.echo.Application;

/**
 *
 * @author vpc
 */
public class PasswordComponent extends JPanel implements FormComponent {

    private JPasswordField pf = new JPasswordField();
    private JCheckBox showPassword;
    private AnyDocumentListener listener;

    public PasswordComponent() {
        showPassword = new JCheckBox("Message.showPassword");
        showPassword.addActionListener((e)
                -> pf.setEchoChar(showPassword.isSelected() ? '\0' : '*')
        );
        new GridBagLayoutSupport("[pwd-===][check] ; insets(2)")
                .bind("pwd", pf)
                .bind("check", showPassword)
                .apply(this);
    }

    public void install(Application app) {
        showPassword.setText(app.i18n().getString("Message.showPassword"));
    }

    public JPasswordField getPasswordField() {
        return pf;
    }

    @Override
    public void uninstall() {
        if (listener != null) {
            pf.getDocument().removeDocumentListener(listener);
            listener = null;
        }
    }

    @Override
    public String getContentString() {
        return new String(pf.getPassword());
    }

    @Override
    public void setContentString(String s) {
        pf.setText(s);
    }

    @Override
    public void setFormChangeListener(Runnable callback) {
        if (listener == null) {
            listener = new AnyDocumentListener() {
                @Override
                public void anyChange(DocumentEvent e) {
                    callback.run();
                }
            };
            pf.getDocument().addDocumentListener(listener);
        }
    }

    public void setEditable(boolean b){
        pf.setEditable(b);
    }
    
    public boolean isEditable(){
        return pf.isEditable();
    }
    
}
