/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import net.thevpc.common.swing.JDialog2;
import net.thevpc.common.swing.SwingUtilities3;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;

/**
 *
 * @author vpc
 */
public class OkCancelAppDialog extends JDialog2{
    private OkCancelFooter footer;
    protected NNoteGuiApp sapp;

    public OkCancelAppDialog(NNoteGuiApp sapp,String titleId) {
        super((JFrame) sapp.app().mainWindow().get().component(),
                sapp.app().i18n().getString(titleId), true
        );
        this.sapp=sapp;
    }

    protected void build(JComponent mainComponent,Runnable onOk,Runnable onCancel){
        getRootPane().setLayout(new BorderLayout());
        footer=new OkCancelFooter(sapp.app(), onCancel, onOk);
        getRootPane().add(mainComponent,BorderLayout.CENTER);
        getRootPane().add(footer,BorderLayout.SOUTH);
        SwingUtilities3.addEscapeBindings(this);
        this.getRootPane().setDefaultButton(footer.getOkButton());
    }
    
    
}
