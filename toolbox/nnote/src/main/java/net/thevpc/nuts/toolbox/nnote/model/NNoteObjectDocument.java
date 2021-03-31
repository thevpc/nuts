/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class NNoteObjectDocument {

    private NNoteObjectDescriptor descriptor;
    private List<NNoteObject> values = new ArrayList<>();

    public NNoteObjectDescriptor getDescriptor() {
        return descriptor;
    }

    public NNoteObjectDocument setDescriptor(NNoteObjectDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public List<NNoteObject> getValues() {
        return values;
    }

    public NNoteObjectDocument addValue(NNoteObject value) {
        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(value);
        return this;
    }

    public NNoteObjectDocument setValues(List<NNoteObject> values) {
        this.values = values;
        return this;
    }

    public boolean addField(NNoteFieldDescriptor descr) {
        if (descriptor == null) {
            descriptor = new NNoteObjectDescriptor();
        }
        if (descriptor.getFields() == null) {
            descriptor.setFields(new ArrayList<>());
        }
        if (descriptor.findField(descr.getName()) != null) {
            return false;
        }
        descriptor.addField(descr);
        if (values != null) {
            for (NNoteObject o : values) {
                if (o.getFields()==null || !o.getFields().stream().anyMatch(x -> Objects.equals(x.getName(), descr.getName()))) {
                    o.addField(new NNoteField().setName(descr.getName()).setValue(descr.getDefaultValue()));
                }
            }
        }
        return true;
    }

    public void removeField(String fieldName) {
        if (descriptor != null && descriptor.getFields() != null) {
            for (Iterator<NNoteFieldDescriptor> it = descriptor.getFields().iterator(); it.hasNext();) {
                NNoteFieldDescriptor field = it.next();
                if (Objects.equals(field.getName(), fieldName)) {
                    it.remove();
                }
            }
        }
        if (values != null) {
            for (NNoteObject o : values) {
                for (Iterator<NNoteField> it = o.getFields().iterator(); it.hasNext();) {
                    NNoteField field = it.next();
                    if (Objects.equals(field.getName(), fieldName)) {
                        it.remove();
                    }
                }
            }
        }
    }

    public void renameField(String from, String to) {
        if (descriptor != null && descriptor.getFields() != null) {
            for (NNoteFieldDescriptor field : descriptor.getFields()) {
                if (Objects.equals(field.getName(), from)) {
                    field.setName(to);
                }
            }
        }
        if (values != null) {
            for (NNoteObject o : values) {
                for (NNoteField field : o.getFields()) {
                    if (Objects.equals(field.getName(), from)) {
                        field.setName(to);
                    }
                }
            }
        }
    }

    public void changeType(String fieldName, NNoteObjectFieldType newFieldType) {
        NNoteFieldDescriptor theField = null;
        if (descriptor != null) {
            theField = descriptor.findField(fieldName);
        }
        if (theField != null) {
            NNoteObjectFieldType fromType = theField.getType();
            theField.setType(newFieldType);
            if (fromType == null) {
                return;
            }
            if (fromType != newFieldType) {
                if (fromType.isFreeTextType()) {
                    if (newFieldType.isSelectOne()) {
                        LinkedHashSet<String> allValues = new LinkedHashSet<>();
                        if (theField.getValues() != null) {
                            allValues.addAll(theField.getValues());
                        }
                        if (values != null) {
                            for (NNoteObject o : values) {
                                for (NNoteField field : o.getFields()) {
                                    if (Objects.equals(field.getName(), fieldName)) {
                                        String s = field.getValue();
                                        if (s == null) {
                                            s = "";
                                            field.setValue(s);
                                        }
                                        allValues.add(s);
                                    }
                                }
                            }
                        }
                        theField.setValues(new ArrayList<>(allValues));
                    } else if (newFieldType.isSelectMulti()) {
                        LinkedHashSet<String> allValues = new LinkedHashSet<>();
                        if (theField.getValues() != null) {
                            allValues.addAll(theField.getValues());
                        }
                        if (values != null) {
                            for (NNoteObject o : values) {
                                for (NNoteField field : o.getFields()) {
                                    if (Objects.equals(field.getName(), fieldName)) {
                                        String s = field.getValue();
                                        if (s == null) {
                                            s = "";
                                            field.setValue(s);
                                        }
                                        Set<String> selected = new LinkedHashSet<>();
                                        for (String nv : s.split("[\n,;]")) {
                                            nv = nv.trim();
                                            selected.add(nv);
                                        }
                                        field.setValue(String.join("\n", selected));
                                        allValues.addAll(selected);
                                    }
                                }
                            }
                        }
                        theField.setValues(new ArrayList<>(allValues));
                    }
                } else if (fromType.isSelectOne()) {
                    //do nothing, can be transofrmed to any other...
                } else if (fromType.isSelectMulti()) {
                    if (newFieldType.isFreeTextType()) {
                        if (newFieldType.isFreeTextTypeAcceptingNewLine()) {
                            //do nothing
                        } else {
                            //replace newline with ';'
                            if (values != null) {
                                for (NNoteObject o : values) {
                                    for (NNoteField field : o.getFields()) {
                                        if (Objects.equals(field.getName(), fieldName)) {
                                            String s = field.getValue();
                                            if (s == null) {
                                                s = "";
                                            }
                                            Set<String> selected = new LinkedHashSet<>();
                                            for (String nv : s.split("\n")) {
                                                nv = nv.trim();
                                                selected.add(nv);
                                            }
                                            field.setValue(String.join(";", selected));
                                        }
                                    }
                                }
                            }
                        }
                    } else if (newFieldType.isSelectOne()) {
                        //expand multiple selections and select only the first one... (non bijective)!!
                        LinkedHashSet<String> allValues = new LinkedHashSet<>();
                        if (theField.getValues() != null) {
                            allValues.addAll(theField.getValues());
                        }
                        if (values != null) {
                            for (NNoteObject o : values) {
                                for (NNoteField field : o.getFields()) {
                                    if (Objects.equals(field.getName(), fieldName)) {
                                        String s = field.getValue();
                                        if (s == null) {
                                            s = "";
                                        }
                                        Set<String> selected = new LinkedHashSet<>();
                                        for (String nv : s.split("\n")) {
                                            nv = nv.trim();
                                            selected.add(nv);
                                        }
                                        //only first is selected!
                                        if (selected.size() > 0) {
                                            field.setValue((String) selected.toArray()[0]);
                                        } else {
                                            field.setValue("");
                                        }
                                        allValues.addAll(selected);
                                    }
                                }
                            }
                        }
                        theField.setValues(new ArrayList<>(allValues));
                    }
                }
            }
        }
    }

    public void updateFieldValues(String name, String[] split) {
        Set<String> all = new TreeSet<String>();
        if (split != null) {
            for (String s : split) {
                if (s == null) {
                    s = "";
                }
                s = s.trim();
                all.add(s);
            }
        }
        if (descriptor != null) {
            NNoteFieldDescriptor d = descriptor.findField(name);
            if (d != null) {
                if (d.getType() == NNoteObjectFieldType.COMBOBOX) {
                    if (values != null) {
                        for (NNoteObject value : values) {
                            for (NNoteField field : value.getFields()) {
                                if (Objects.equals(field.getName(), name)) {
                                    String v = field.getValue();
                                    if (v == null) {
                                        v = "";
                                    }
                                    v = v.trim();
                                    field.setValue(v);
                                    all.add(v);
                                }
                            }
                        }
                    }
                }
                d.setValues(new ArrayList<>(all));
            }

        }
    }

    public void moveFieldDown(String name) {
        int from = descriptor.indexOfField(name);
        OtherUtils.switchListValues(descriptor.getFields(), from, from + 1);
    }

    public void moveFieldUp(String name) {
        int from = descriptor.indexOfField(name);
        OtherUtils.switchListValues(descriptor.getFields(), from, from - 1);
    }
}
