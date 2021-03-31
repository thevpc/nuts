/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.echo.Application;

/**
 *
 * @author vpc
 */
public class OkCancelFooter extends JPanel {

    JButton okButton;
    JButton cancelButton;

    public OkCancelFooter(Application app, Runnable onCancel, Runnable onOk) {
        okButton = new JButton(app.i18n().getString("Message.ok"));
        okButton.addActionListener((e) -> onOk.run());
        cancelButton = new JButton(app.i18n().getString("Message.cancel"));
        cancelButton.addActionListener((e) -> onCancel.run());
        new GridBagLayoutSupport("[-=glue(h)][ok][cancel] ; insets(5)")
                .bind("ok", okButton)
                .bind("cancel", cancelButton)
                .apply(this);
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    }

    public JButton getOkButton() {
        return okButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

}
