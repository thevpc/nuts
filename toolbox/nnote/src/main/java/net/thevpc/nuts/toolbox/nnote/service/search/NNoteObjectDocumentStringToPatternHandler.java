/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.thevpc.nuts.toolbox.nnote.model.NNoteField;
import net.thevpc.nuts.toolbox.nnote.model.NNoteFieldDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObject;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDocument;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.service.NNoteService;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.StringToPatternPortionImpl;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.DocumentTextNavigator;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.DocumentTextPart;

/**
 *
 * @author vpc
 */
public class NNoteObjectDocumentStringToPatternHandler implements DocumentTextNavigator<VNNote> {

    private NNoteService service;
    private NNoteObjectDocument document;
    private VNNote note;

    public NNoteObjectDocumentStringToPatternHandler(NNoteService service, VNNote note, Object source) {
        this.service = service;
        this.note = note;
        document
                = source == null ? new NNoteObjectDocument()
                        : source instanceof NNoteObjectDocument ? (NNoteObjectDocument) source
                                : service.parseObjectDocument(String.valueOf(source));
    }

    @Override
    public Iterator<DocumentTextPart<VNNote>> iterator() {
        List<DocumentTextPart<VNNote>> all = new ArrayList<>();
        for (NNoteFieldDescriptor value : document.getDescriptor().getFields()) {
            if (!OtherUtils.isBlank(value.getName())) {
                //String key, String text, T object, String stringValue
                all.add(new StringToPatternPortionImpl<VNNote>("fieldDescriptor", value.getName(), note, value, value.getName()));
            }
        }
        for (NNoteObject value : document.getValues()) {
            for (NNoteField field : value.getFields()) {
                String s = field.getValue();
                if (!OtherUtils.isBlank(s)) {
                    all.add(new StringToPatternPortionImpl("fieldValue", s, note, field, s));
                }
            }
        }
        return all.iterator();
    }

}
