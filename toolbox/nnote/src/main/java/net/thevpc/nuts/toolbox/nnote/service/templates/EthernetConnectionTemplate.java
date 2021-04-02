/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.templates;

import java.util.ArrayList;
import java.util.Arrays;
import net.thevpc.common.i18n.I18n;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTemplate;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDocument;
import net.thevpc.nuts.toolbox.nnote.model.NNoteFieldDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectFieldType;
import net.thevpc.nuts.toolbox.nnote.service.NNoteService;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class EthernetConnectionTemplate implements NNoteTemplate {

    @Override
    public String getId() {
        return "application/nnote-extra-ethernet-connection";
    }

    @Override
    public String getIcon() {
        return "network";
    }

    @Override
    public void prepare(NNote n, NNoteService service) {
        String prefix = "NNoteTypeFamily." + getId() + ".";
        I18n i18n = service.i18n();
        NNoteObjectDocument doc = new NNoteObjectDocument().setDescriptor(new NNoteObjectDescriptor()
                .addField(new NNoteFieldDescriptor().setName(i18n.getString(prefix + "title")).setType(NNoteObjectFieldType.TEXT))
                .addField(new NNoteFieldDescriptor().setName(i18n.getString(prefix + "ip")).setType(NNoteObjectFieldType.TEXT))
                .addField(new NNoteFieldDescriptor().setName(i18n.getString(prefix + "mask")).setType(NNoteObjectFieldType.TEXT))
                .addField(new NNoteFieldDescriptor().setName(i18n.getString(prefix + "dns")).setType(NNoteObjectFieldType.TEXT))
                .addField(new NNoteFieldDescriptor().setName(i18n.getString(prefix + "gateway")).setType(NNoteObjectFieldType.TEXT))
                .addField(new NNoteFieldDescriptor().setName(i18n.getString(prefix + "notes")).setType(NNoteObjectFieldType.TEXTAREA)));
        n.setContentType(NNoteTypes.OBJECT_LIST);
        n.setContent(service.stringifyDescriptor(doc.setValues(new ArrayList<>(Arrays.asList(doc.getDescriptor().createObject())))));
    }

}
