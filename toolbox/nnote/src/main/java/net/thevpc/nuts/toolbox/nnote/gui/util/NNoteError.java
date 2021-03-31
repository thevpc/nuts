/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

/**
 *
 * @author vpc
 */
public class NNoteError {
    private Exception ex;

    public NNoteError(Exception ex) {
        this.ex = ex;
    }

    public Exception getEx() {
        return ex;
    }
    
    
}
