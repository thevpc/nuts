/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author vpc
 */
public class NNoteObject {

    private List<NNoteField> fields;

    public List<NNoteField> getFields() {
        return fields;
    }

    public NNoteObject addField(NNoteField field) {
        if (this.fields == null) {
            this.fields = new java.util.ArrayList<>();
        }
        this.fields.add(field);
        return this;
    }

    public NNoteObject setFields(List<NNoteField> fields) {
        this.fields = fields;
        return this;
    }

    public NNoteObject copy() {
        NNoteObject d = new NNoteObject();
        d.setFields(new ArrayList<>());
        if (fields != null) {
            for (NNoteField field : fields) {
                d.getFields().add(field.copy());
            }
        }
        return d;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.fields);
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
        final NNoteObject other = (NNoteObject) obj;
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        
        return "{" + 
                (fields ==null?"":fields.stream().map(x->String.valueOf(x)).collect(Collectors.joining(", ")))
                + '}';
    }

}
