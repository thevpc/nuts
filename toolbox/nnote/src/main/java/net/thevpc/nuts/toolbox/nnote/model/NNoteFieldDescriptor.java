/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class NNoteFieldDescriptor implements Cloneable {

    private String name;
    private String defaultValue;
    private NNoteObjectFieldType type;
    private List<String> values;
    private String pattern;

    public String getName() {
        return name;
    }

    public NNoteFieldDescriptor setName(String name) {
        this.name = name;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public NNoteFieldDescriptor setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public NNoteObjectFieldType getType() {
        return type;
    }

    public NNoteFieldDescriptor setType(NNoteObjectFieldType type) {
        this.type = type;
        return this;
    }

    public List<String> getValues() {
        return values;
    }

    public NNoteFieldDescriptor addValue(String value) {
        if (this.values == null) {
            this.values = new ArrayList<>();
        }
        if (value == null) {
            value = "";
        }
        values.add(value);
        return this;
    }

    public NNoteFieldDescriptor setValues(List<String> values) {
        this.values = values;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public NNoteFieldDescriptor setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public NNoteFieldDescriptor copy() {
        try {
            NNoteFieldDescriptor d = (NNoteFieldDescriptor) super.clone();
            if (d.values != null) {
                d.values = new ArrayList<>(d.values);
            }
            return d;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.defaultValue);
        hash = 29 * hash + Objects.hashCode(this.type);
        hash = 29 * hash + Objects.hashCode(this.values);
        hash = 29 * hash + Objects.hashCode(this.pattern);
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
        final NNoteFieldDescriptor other = (NNoteFieldDescriptor) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.defaultValue, other.defaultValue)) {
            return false;
        }
        if (!Objects.equals(this.pattern, other.pattern)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.values, other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return OtherUtils.toEscapedName(name) + ":" + (type == null ? "<null>" : type.toString().toLowerCase());
    }

}
