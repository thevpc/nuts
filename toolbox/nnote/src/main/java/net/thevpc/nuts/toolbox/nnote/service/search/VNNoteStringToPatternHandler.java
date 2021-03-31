/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search;

import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.TextStringToPatternHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.service.NNoteService;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.DocumentTextNavigator;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.DocumentTextPart;

/**
 *
 * @author vpc
 */
public class VNNoteStringToPatternHandler implements DocumentTextNavigator {

    private NNoteService service;
    private VNNote note;

    public VNNoteStringToPatternHandler(NNoteService service, VNNote source) {
        this.service = service;
        note = source == null ? new VNNote() : source;
    }

    @Override
    public Iterator<DocumentTextPart<VNNote>> iterator() {
        List<Iterator<DocumentTextPart<VNNote>>> parts = new ArrayList<>();
        parts.add(new TextStringToPatternHandler("name", note, "name", note.getName()).iterator());
        parts.add(new TextStringToPatternHandler("tags", note, "tags", String.join(" ", note.getTags())).iterator());
        String ct = note.getContentType();
        if (ct == null) {
            ct = NNoteTypes.PLAIN;
        }
        switch (ct) {
            case NNoteTypes.OBJECT_LIST: {
                parts.add(new NNoteObjectDocumentStringToPatternHandler(service, note, note.getContent()).iterator());
                break;
            }
            default: {
                parts.add(new TextStringToPatternHandler("content", note, "content", note.getContent()).iterator());
            }
        }
        List<DocumentTextPart<VNNote>> li = new ArrayList<>();
        for (Iterator<DocumentTextPart<VNNote>> o : parts) {
            o.forEachRemaining(li::add);
        }
        return li.iterator();
    }

}
