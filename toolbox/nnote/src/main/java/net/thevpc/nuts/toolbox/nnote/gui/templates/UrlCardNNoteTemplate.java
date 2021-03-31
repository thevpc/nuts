/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.templates;

import java.util.ArrayList;
import java.util.Arrays;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTemplate;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDocument;
import net.thevpc.nuts.toolbox.nnote.model.NNoteFieldDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectFieldType;

/**
 *
 * @author vpc
 */
public class UrlCardNNoteTemplate implements NNoteTemplate {
    
    @Override
    public String getId() {
        return "application/nnote-extra-web-account";
    }

    @Override
    public void prepare(NNote n, NNoteGuiApp sapp) {
        String prefix = "NodeTypeFamily." + getId() + ".";
        NNoteObjectDocument doc = new NNoteObjectDocument().setDescriptor(
                new NNoteObjectDescriptor()
                        .addField(new NNoteFieldDescriptor().setName(sapp.app().i18n().getString(prefix + "title")).setType(NNoteObjectFieldType.TEXT))
                        .addField(new NNoteFieldDescriptor().setName(sapp.app().i18n().getString(prefix + "userName")).setType(NNoteObjectFieldType.TEXT))
                        .addField(new NNoteFieldDescriptor().setName(sapp.app().i18n().getString(prefix + "password")).setType(NNoteObjectFieldType.PASSWORD))
                        .addField(new NNoteFieldDescriptor().setName(sapp.app().i18n().getString(prefix + "url")).setType(NNoteObjectFieldType.URL))
                        .addField(new NNoteFieldDescriptor().setName(sapp.app().i18n().getString(prefix + "notes")).setType(NNoteObjectFieldType.TEXTAREA)));
        n.setContentType(NNoteTypes.OBJECT_LIST);
        n.setContent(sapp.service().stringifyDescriptor(doc.setValues(new ArrayList<>(Arrays.asList(doc.getDescriptor().createObject())))));
    }
    
}
