/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.common.collections;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author thevpc
 */
public class NPropertiesHolder {

    private Map<String, Object> properties = new LinkedHashMap<>();

    public NPropertiesHolder copy() {
        NPropertiesHolder h = new NPropertiesHolder();
        synchronized (this) {
            h.properties = this.properties == null ? null : new LinkedHashMap<>(this.properties);
        }
        return h;
    }

    public Map<String, Object> toMap() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        synchronized (this) {
            this.properties = properties;
        }
    }

    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }

    public <T> NOptional<T> getOptional(String key) {
        if (properties != null) {
            Object a = properties.get(key);
            if (a != null) {
                return NOptional.of((T) a);
            }
        }
        return NOptional.ofNamedEmpty(key);
    }

    public <T> T getOrComputeProperty(String key, NSession session, Function<NSession, T> supplier) {
        if (properties != null) {
            Object v = properties.get(key);
            if (v != null) {
                return (T) v;
            }
        }
        synchronized (this) {
            T o = properties != null ? ((T) properties.get(key)) : null;
            if (o != null) {
                return o;
            }
            setProperty(key, o = supplier.apply(session));
            return o;
        }
    }

    public Object setProperty(String key, Object value) {
        synchronized (this) {
            if (properties == null) {
                if (value != null) {
                    properties = new LinkedHashMap<>();
                    return properties.put(key, value);
                }
            } else {
                if (value != null) {
                    return properties.put(key, value);
                } else {
                    return properties.remove(key);
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
