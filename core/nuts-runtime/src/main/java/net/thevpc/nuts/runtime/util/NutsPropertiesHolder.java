/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author thevpc
 */
public class NutsPropertiesHolder {

    private Map<String, Object> properties = new LinkedHashMap<>();

    public NutsPropertiesHolder copy() {
        NutsPropertiesHolder h = new NutsPropertiesHolder();
        h.properties = this.properties == null ? null : new LinkedHashMap<>(this.properties);
        return h;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }

    public void setProperty(String key, Object value) {
        if (properties == null) {
            if (value != null) {
                properties = new LinkedHashMap<>();
                properties.put(key, value);
            }
        } else {
            if (value != null) {
                properties.put(key, value);
            } else {
                properties.remove(key);
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(properties);
    }

    public int size() {
        return properties.size();
    }
}
