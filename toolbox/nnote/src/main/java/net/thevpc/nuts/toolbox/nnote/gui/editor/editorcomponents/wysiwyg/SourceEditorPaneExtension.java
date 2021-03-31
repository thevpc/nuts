/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.wysiwyg;

import net.thevpc.echo.Application;
import net.thevpc.jeep.editor.JEditorPaneBuilder;

/**
 *
 * @author vpc
 */
public interface SourceEditorPaneExtension {

    public void uninstall(JEditorPaneBuilder editorBuilder, Application app);
    
    public void prepareEditor(JEditorPaneBuilder editorBuilder, Application app);

}
