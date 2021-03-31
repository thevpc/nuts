/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import javax.swing.JComponent;

/**
 *
 * @author vpc
 */
public class SimpleEditorKitHeader implements EditorKitHeader {
    
    JComponent component;
    String contentType;

    public SimpleEditorKitHeader(JComponent component, String contentType) {
        this.component = component;
        this.contentType = contentType;
    }

    @Override
    public JComponent component() {
        return component;
    }

    @Override
    public boolean acceptContentType(String contentType) {
        return this.contentType==null || this.contentType.equals(contentType);
    }
    
}
