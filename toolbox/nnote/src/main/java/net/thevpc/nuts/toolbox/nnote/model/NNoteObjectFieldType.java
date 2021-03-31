/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

/**
 *
 * @author vpc
 */
public enum NNoteObjectFieldType {
    TEXT,
    URL,
    PASSWORD,
    COMBOBOX,
    CHECKBOX,
    TEXTAREA;

    public boolean isSelectMulti() {
        return this == NNoteObjectFieldType.CHECKBOX;
    }

    public boolean isSelectOne() {
        return this == NNoteObjectFieldType.COMBOBOX;
    }

    public boolean isFreeTextTypeAcceptingNewLine() {
        return this == TEXTAREA;
    }

    public boolean isFreeTextType() {
        switch (this) {
            case TEXT:
            case TEXTAREA: 
            case PASSWORD: 
            case URL: 
            {
                return true;
            }
        }
        return false;
    }

    public boolean isSelectType() {
        switch (this) {
            case COMBOBOX:
            case CHECKBOX: {
                return true;
            }
        }
        return false;
    }

}
