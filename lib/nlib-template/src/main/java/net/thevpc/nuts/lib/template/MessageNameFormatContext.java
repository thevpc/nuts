/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.template;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public final class MessageNameFormatContext {
    
    private Map<String, MessageNameFormat.Function> functions = new HashMap<>();
    private boolean editable;

    public MessageNameFormatContext() {
        this(false,true);
    }
    
    public MessageNameFormatContext(boolean addDefaults, boolean editable) {
        this.editable = true;
        if (addDefaults) {
            addDefaults();
        }
        this.editable = editable;
    }

    public MessageNameFormatContext freeze() {
        this.editable=false;
        return this;
    }
    
    public MessageNameFormatContext toEditable() {
        if (isEditable()) {
            return this;
        }
        return copy(true);
    }

    public MessageNameFormatContext copy(boolean editable) {
        MessageNameFormatContext r = new MessageNameFormatContext(false, editable);
        r.functions.putAll(functions);
        return r;
    }

    public MessageNameFormatContext addDefaults() {
        register("switch", MessageNameFormatFactory.FCT_SWITCH);
        register("integer", MessageNameFormatFactory.FCT_INTEGER);
        register("double", MessageNameFormatFactory.FCT_DOUBLE);
        register("float", MessageNameFormatFactory.FCT_DOUBLE);
        register("date", MessageNameFormatFactory.FCT_DATE);
        register("parseDate", MessageNameFormatFactory.FCT_DATE_PARSE);
        return this;
    }

    public MessageNameFormatContext register(String name, MessageNameFormat.Function function) {
        if (!editable) {
            throw new IllegalArgumentException("Read only");
        }
        if (function == null) {
            functions.remove(name.toLowerCase());
        } else {
            functions.put(name.toLowerCase(), function);
        }
        return this;
    }

    public boolean isEditable() {
        return editable;
    }

    public MessageNameFormat.Function getFunction(String name) {
        return functions.get(name.toLowerCase());
    }
    
}
