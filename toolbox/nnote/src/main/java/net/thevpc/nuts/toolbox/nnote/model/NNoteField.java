/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class NNoteField {

    private String name;
    private String value;
    private boolean hidden;

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
        return new NNoteField().setName(name).setValue(value).setHidden(hidden);
    }

    @Override
    public String toString() {
        return OtherUtils.toEscapedName(name)
                + "=" + OtherUtils.toEscapedValue(value);
    }

    public boolean isHidden() {
        return hidden;
    }

    public NNoteField setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

}
