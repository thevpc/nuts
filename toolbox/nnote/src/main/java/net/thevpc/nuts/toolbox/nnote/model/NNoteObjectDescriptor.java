/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author vpc
 */
public class NNoteObjectDescriptor implements Cloneable {

    private String name;
    private List<NNoteFieldDescriptor> fields;

    public String getName() {
        return name;
    }

    public NNoteObjectDescriptor setName(String name) {
        this.name = name;
        return this;
    }

    public NNoteFieldDescriptor findField(String name) {
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                NNoteFieldDescriptor field = fields.get(i);
                if (field != null && Objects.equals(field.getName(), name)) {
                    return field;
                }
            }
        }
        return null;
    }

    public int indexOfField(String name) {
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                NNoteFieldDescriptor field = fields.get(i);
                if (field != null && Objects.equals(field.getName(), name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public NNoteObjectDescriptor addField(NNoteFieldDescriptor f) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        if (f != null) {
            fields.add(f);
        }
        return this;
    }

    public List<NNoteFieldDescriptor> getFields() {
        return fields;
    }

    public NNoteObjectDescriptor setFields(List<NNoteFieldDescriptor> fields) {
        this.fields = fields;
        return this;
    }

    public NNoteObject createObject() {
        NNoteObject o = new NNoteObject();
        if (this.getFields() != null) {
            for (NNoteFieldDescriptor field : this.getFields()) {
                o.addField(new NNoteField()
                        .setName(field.getName())
                        .setValue(field.getDefaultValue())
                );
            }
        }
        return o;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.fields);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NNoteObjectDescriptor other = (NNoteObjectDescriptor) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        return true;
    }

    public NNoteObjectDescriptor copy() {
        try {
            NNoteObjectDescriptor d = (NNoteObjectDescriptor) super.clone();
            if (d.fields != null) {
                d.fields = new ArrayList<>();
                for (NNoteFieldDescriptor field : fields) {
                    d.fields.add(field.copy());
                }
            }
            return d;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
