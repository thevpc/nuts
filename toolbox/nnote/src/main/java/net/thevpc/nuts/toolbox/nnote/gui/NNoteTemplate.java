/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui;

import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.service.NNoteService;

/**
 *
 * @author vpc
 */
public interface NNoteTemplate {

    String getId();
    String getIcon();

    void prepare(NNote n, NNoteService sapp);

    public default String getLabel(NNoteService sapp) {
        return null;
    }
    
}
