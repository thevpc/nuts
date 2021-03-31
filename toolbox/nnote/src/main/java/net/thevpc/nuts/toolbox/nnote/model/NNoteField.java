/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import java.util.Objects;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class NNoteField {

    private String name;
    private String value;

    public NNoteField() {
    }

    public NNoteField(String name, String value) {
        this.name = name;
        this.value = value;
    }

    
    public String getName() {
        return name;
    }

    public NNoteField setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public NNoteField setValue(String value) {
        this.value = value;
        return this;
    }

    public NNoteField copy() {
        return new NNoteField().setName(name).setValue(value);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.value);
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
        final NNoteField other = (NNoteField) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return OtherUtils.toEscapedName(name)
                + "=" + OtherUtils.toEscapedValue(value);
    }

}
