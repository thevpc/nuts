/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor;

import javax.swing.JComponent;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;

/**
 *
 * @author vpc
 */
public interface NNoteEditorTypeComponent {

    JComponent component();

    void uninstall();

    public void setNote(VNNote note, NNoteGuiApp sapp);

    public void setEditable(boolean b);

    public boolean isEditable();
}
