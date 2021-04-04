/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.source;

import net.thevpc.jeep.editor.JEditorPaneBuilder;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;

/**
 *
 * @author vpc
 */
public interface SourceEditorPaneExtension {

    public void uninstall(JEditorPaneBuilder editorBuilder, NNoteGuiApp sapp);
    
    public void prepareEditor(JEditorPaneBuilder editorBuilder, boolean compactMode, NNoteGuiApp sapp);

}
