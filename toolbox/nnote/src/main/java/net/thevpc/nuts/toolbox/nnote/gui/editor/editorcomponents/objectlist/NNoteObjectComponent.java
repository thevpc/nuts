/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.objectlist;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.model.NNoteField;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObject;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNoteFieldDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectFieldType;

/**
 *
 * @author vpc
 */
public class NNoteObjectComponent extends JPanel {

    private NNoteObjectExt currentValue;
    private List<NNoteFieldDescriptorPanel> components = new ArrayList<>();
    private NNoteObjectTracker objectTracker;
    private NNoteGuiApp sapp;
    private NNoteObjectTracker dynamicObjectTrackerAdapter = new NNoteObjectTracker() {
        @Override
        public void onStructureChanged() {
            if (objectTracker != null) {
                objectTracker.onStructureChanged();
            }
        }

        @Override
        public void onValueChanged() {
            objectTracker.onValueChanged();
        }
    };

    public NNoteObjectComponent(NNoteObjectTracker objectTracker, NNoteGuiApp sapp) {
        super(new GridBagLayout());
        this.objectTracker = objectTracker;
        this.sapp = sapp;
    }

    public NNoteObject getObject() {
        NNoteObject o = new NNoteObject();
        for (NNoteFieldDescriptorPanel c : components) {
            o.addField(c.getValue());
        }
        return o;
    }

    public void setStructure(NNoteObjectDescriptor descriptor) {
        List<NNoteFieldDescriptorPanel> newComponents = new ArrayList<>();

        List<NNoteFieldDescriptor> fields = descriptor.getFields();
        if (fields != null) {
            for (NNoteFieldDescriptor field : fields) {
                if (field != null) {
                    int old = indexOfDescriptor(field);
                    if (old != -1) {
                        NNoteFieldDescriptorPanel r = components.remove(old);
                        if (r.getDescr().equals(field)) {
                            newComponents.add(r);//no change!
                        } else if (r.supportsUpdateDescriptor(field)) {
                            r.updateDescriptor(field);
                            newComponents.add(r);
                        } else {
                            newComponents.add(new NNoteFieldDescriptorPanel(sapp, field, dynamicObjectTrackerAdapter));
                        }
                    } else {
                        newComponents.add(new NNoteFieldDescriptorPanel(sapp, field, dynamicObjectTrackerAdapter));
                    }
                }
            }
        }
        while (!components.isEmpty()) {
            NNoteFieldDescriptorPanel a = components.remove(0);
            a.uninstall();
        }
        components.addAll(newComponents);

        if (components.isEmpty()) {
            NNoteFieldDescriptor v = new NNoteFieldDescriptor();
            v.setName(sapp.app().i18n().getString("Message.title"));
            v.setType(NNoteObjectFieldType.TEXT);
            this.currentValue.getDocument().addField(v);
            components.add(new NNoteFieldDescriptorPanel(sapp, v, dynamicObjectTrackerAdapter));
        }
        relayoutObject();
    }

    public void relayoutObject() {
        removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = 1;
//        c.weighty = 0;
        c.insets = new Insets(3, 3, 3, 3);
        int row = 0;
        for (int i = 0; i < components.size(); i++) {
            NNoteFieldDescriptorPanel cad = components.get(i);
            if (cad.getDescr().getType() == NNoteObjectFieldType.TEXTAREA) {
                c.gridy = row;
                c.gridheight = 1;
                c.gridwidth = 2;
                c.gridx = 0;
                c.weighty = 0;
                add(cad.getLabel(), c.clone());
                row++;
                c.gridy = row;
                c.gridx = 0;
                c.gridwidth = 3;
                c.gridheight = 2;
                c.weighty = 4;
                add(cad.getComponent(), c.clone());
                row += 2;
            } else {
                c.weighty = 0;
                c.gridheight = 1;
                c.gridy = row;
                c.weightx = 0;
                c.gridwidth = 1;
                c.gridx = 0;
                add(cad.getLabel(), c.clone());
                c.weightx = 3;
                c.gridwidth = 2;
                c.gridx = 1;
                add(cad.getComponent(), c.clone());
                row++;
            }
        }
    }

    public void setObject(NNoteObjectExt value) {
        this.currentValue = value;
        setStructure(value.getDescriptor());
        Map<String, NNoteField> map = new HashMap<String, NNoteField>();
        if (value.getObject().getFields() != null) {
            for (NNoteField field : value.getObject().getFields()) {
                map.put(field.getName(), field);
            }
        }
        for (NNoteFieldDescriptorPanel component : components) {
            NNoteField v = map.get(component.getDescr().getName());
            if (v == null) {
                v = new NNoteField(component.getDescr().getName(), "");
                value.getObject().addField(v);
            }
            if (v.getValue() == null) {
                v.setValue("");
            }
            component.setValue(v, currentValue.getDocument());
        }
    }

    public int indexOfDescriptor(NNoteFieldDescriptor d) {
        for (int i = 0; i < components.size(); i++) {
            NNoteFieldDescriptorPanel component = components.get(i);
            if (component.getDescr().equals(d)) {
                return i;
            }
        }
        return -1;
    }

    public void uninstall() {
        while (!components.isEmpty()) {
            NNoteFieldDescriptorPanel a = components.remove(0);
            a.uninstall();
        }
    }

}
