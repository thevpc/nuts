/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.dialogs;

import net.thevpc.nuts.toolbox.nnote.gui.util.dialog.OkCancelDialog;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.common.swing.util.CancelException;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.PasswordComponent;
import net.thevpc.nuts.toolbox.nnote.service.security.PasswordHandler;

/**
 *
 * @author vpc
 */
public class EnterNewPasswordDialog extends OkCancelDialog {

    private PasswordComponent passwordComponent1;
    private PasswordComponent passwordComponent2;

    private boolean ok = false;
    private String path;
    private PasswordHandler ph;

    public EnterNewPasswordDialog(NNoteGuiApp sapp, String path, PasswordHandler ph) throws HeadlessException {
        super(sapp, "Message.password");
        this.ph=ph;
        passwordComponent1 = new PasswordComponent();
        passwordComponent1.install(sapp.app());
        passwordComponent1.setMinimumSize(new Dimension(50, 30));
        passwordComponent2 = new PasswordComponent();
        passwordComponent2.install(sapp.app());
        passwordComponent2.setMinimumSize(new Dimension(50, 30));
        GridBagLayoutSupport gbs = GridBagLayoutSupport.load(EnterNewPasswordDialog.class.getResource(
                "/net/thevpc/nuts/toolbox/nnote/forms/EnterNewPassword.gbl-form"
        ));
        gbs.bind("label", new JLabel(sapp.app().i18n().getString("Message.enter-password")));
        gbs.bind("file", new JLabel(path));
        gbs.bind("pwd1", passwordComponent1);
        gbs.bind("pwd2", passwordComponent2);

        build(gbs.apply(new JPanel()), this::ok, this::cancel);
    }

    protected void install() {
        passwordComponent1.install(sapp.app());
        passwordComponent2.install(sapp.app());
    }

    protected void uninstall() {
        passwordComponent1.uninstall();
        passwordComponent2.uninstall();
    }

    protected void ok() {
        uninstall();
        this.ok = true;
        setVisible(false);
    }

    protected void cancel() {
        uninstall();
        this.ok = false;
        setVisible(false);
    }

    public String showDialog() {
        while (true) {
            install();
            this.ok = false;
            pack();
            setLocationRelativeTo((JFrame) sapp.app().mainWindow().get().component());
            setVisible(true);
            try {
                return get();
            } catch (Exception ex) {
                if (!ph.reTypePasswordOnError()) {
                    throw new CancelException();
                }
                //exHandler.accept(ex);
            }
        }
    }

    public String get() {
        if (ok) {
            String s1 = passwordComponent1.getContentString();
            String s2 = passwordComponent2.getContentString();
            if (s1 != null && s1.trim().length() > 0 && s1.equals(s2)) {
                return s1;
            }
            throw new IllegalArgumentException(sapp.app().i18n().getString("Message.passwordsDoNotMatch"));
        }
        return null;
    }

}
