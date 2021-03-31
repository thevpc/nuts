/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui;

import net.thevpc.nuts.toolbox.nnote.model.NNote;

/**
 *
 * @author vpc
 */
public interface NNoteTemplate {

    String getId();

    void prepare(NNote n, NNoteGuiApp sapp);

    public default String getLabel(NNoteGuiApp sapp) {
        return null;
    }
    
}
