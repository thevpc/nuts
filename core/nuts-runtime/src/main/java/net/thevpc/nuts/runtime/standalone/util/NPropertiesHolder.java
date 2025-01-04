/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NOptional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author thevpc
 */
public class NPropertiesHolder {

    private Map<String, NScopedValue> properties = new LinkedHashMap<>();


    public static class NScopedValue {
        private NScopeType scope;
        private Object value;

        public NScopedValue(NScopeType scope, Object value) {
            this.scope = scope;
            this.value = value;
        }

        public NScopedValue(NScopedValue other) {
            this.scope = other.scope;
            this.value = other.value;
        }

        public NScopeType getScope() {
            return scope;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return scope+"(" +value+')';
        }
    }

    public NPropertiesHolder copy() {
        NPropertiesHolder h = new NPropertiesHolder();
        synchronized (this) {
            h.properties = new LinkedHashMap<>();
            for (Map.Entry<String, NScopedValue> e : properties.entrySet()) {
                h.properties.put(e.getKey(), new NScopedValue(e.getValue()));
            }
        }
        return h;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> lhm=new LinkedHashMap<>();
        for (Map.Entry<String, NScopedValue> e : properties.entrySet()) {
            lhm.put(e.getKey(), e.getValue().value);
        }
        return lhm;
    }

    public Set<String> keySet() {
        return new LinkedHashSet<>(properties.keySet());
    }

//    public Map<String, Object> toMap() {
//        return properties;
//    }

    public void setProperties(Map<String, Object> properties,NScopeType scope) {
        synchronized (this) {
            this.properties.clear();
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                this.properties.put(e.getKey(), new NScopedValue(scope,e.getValue()));
            }
        }
    }

    public Object getProperty(String key) {
        NScopedValue a = properties.get(key);
        return a == null ? null : a.value;
    }

    public NScopedValue getScopedValue(String key) {
        NScopedValue a = properties.get(key);
        return a;
    }

    public <T> NOptional<T> getOptional(String key) {
        NScopedValue a = properties.get(key);
        if (a != null) {
            return NOptional.of((T) a.value);
        }
        return NOptional.ofNamedEmpty(key);
    }

    public <T> T getOrComputeProperty(String key, Supplier<T> supplier, NScopeType scope) {
        NScopedValue v = properties.get(key);
        if (v != null) {
            return (T) v.value;
        }
        synchronized (this) {
            v = properties.get(key);
            if (v != null) {
                return (T) v.value;
            }
            T z = supplier.get();
            properties.put(key, new NScopedValue(scope,z));
            return z;
        }
    }

    public Object setProperty(String key, Object value,NScopeType scope) {
        synchronized (this) {
            if (properties == null) {
                if (value != null) {
                    properties = new LinkedHashMap<>();
                    NScopedValue o = properties.put(key, new NScopedValue(scope, value));
                    return o==null?null:o.value;
                }
            } else {
                if (value != null) {
                    NScopedValue o = properties.put(key, new NScopedValue(scope, value));
                    return o==null?null:o.value;
                } else {
                    NScopedValue o = properties.remove(key);
                    return o==null?null:o.value;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(properties);
    }

    public int size() {
        return properties.size();
    }
}
