/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.objectlist;

import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDocument;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObject;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDescriptor;

/**
 *
 * @author vpc
 */
public class NNoteObjectExt {
    private NNoteObject object;
    private NNoteObjectDescriptor descriptor;
    private NNoteObjectDocument document;

    public NNoteObjectExt() {
    }

    public NNoteObjectExt(NNoteObject object, NNoteObjectDescriptor descriptor, NNoteObjectDocument document) {
        this.object = object;
        this.descriptor = descriptor;
        this.document = document;
    }

    public NNoteObject getObject() {
        return object;
    }

    public NNoteObjectDescriptor getDescriptor() {
        return descriptor;
    }

    public NNoteObjectDocument getDocument() {
        return document;
    }
    
}
