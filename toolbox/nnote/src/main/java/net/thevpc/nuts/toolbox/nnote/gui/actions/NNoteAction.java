/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.actions;

import javax.swing.AbstractAction;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;

/**
 *
 * @author vpc
 */
public abstract class NNoteAction extends AbstractAction {

    private NNoteGuiApp napp;

    public NNoteAction(String id, NNoteGuiApp napp) {
        this.napp = napp;
    }
}
