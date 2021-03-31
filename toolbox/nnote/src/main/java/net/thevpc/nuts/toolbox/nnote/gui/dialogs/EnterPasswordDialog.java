/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.dialogs;

import net.thevpc.nuts.toolbox.nnote.gui.util.OkCancelAppDialog;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.PasswordComponent;

/**
 *
 * @author vpc
 */
public class EnterPasswordDialog extends OkCancelAppDialog {

    private JLabel valueLabel;

    private PasswordComponent typePasswordValue;

    private boolean ok = false;

    public EnterPasswordDialog(NNoteGuiApp sapp) throws HeadlessException {
        super(sapp,"Message.password");

        this.valueLabel = new JLabel(sapp.app().i18n().getString("Message.password.label"));
        typePasswordValue = new PasswordComponent();
        typePasswordValue.install(sapp.app());
        typePasswordValue.setMinimumSize(new Dimension(50,30));
        GridBagLayoutSupport gbs = GridBagLayoutSupport.load(EnterPasswordDialog.class.getResource(
                "/net/thevpc/nuts/toolbox/nnote/forms/EnterPassword.gbl-form"
        ));
        gbs.bind("label", new JLabel(sapp.app().i18n().getString("Message.enter-password")));
        gbs.bind("pwd", typePasswordValue);
        
        build(gbs.apply(new JPanel()), this::ok,this::cancel);
    }

    protected void install() {
        typePasswordValue.install(sapp.app());
    }

    protected void uninstall() {
        typePasswordValue.uninstall();
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

    public String showDialog(Consumer<Exception> exHandler) {
        while (true) {
            install();
            this.ok = false;
            pack();
            setLocationRelativeTo((JFrame) sapp.app().mainWindow().get().component());
            setVisible(true);
            try {
                return get();
            } catch (Exception ex) {
                exHandler.accept(ex);
            }
        }
    }

    public String get() {
        if (ok) {
            return typePasswordValue.getContentString();
        }
        return null;
    }

}
