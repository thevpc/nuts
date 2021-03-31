/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author vpc
 */
public abstract class AnyDocumentListener implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
        anyChange(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        anyChange(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        anyChange(e);
    }

    public abstract void anyChange(DocumentEvent e);
}
