package net.thevpc.nuts.util;

import net.thevpc.nuts.NDescriptorProperty;
import net.thevpc.nuts.env.NEnvCondition;

import java.util.*;

public class DefaultNProperties {
    private Map<String, Map<NEnvCondition, NDescriptorProperty>> properties = new LinkedHashMap<>();

    public DefaultNProperties() {
    }

    public DefaultNProperties remove(String name) {
        if (name != null) {
            properties.remove(name);
        }
        return this;
    }

    public DefaultNProperties remove(NDescriptorProperty p) {
        if (p != null) {
            String n = p.getName();
            Map<NEnvCondition, NDescriptorProperty> m = properties.get(n);
            if (m != null) {
                NEnvCondition c = p.getCondition();
                if (c != null && c.isBlank()) {
                    c = null;
                }
                m.remove(c);
            }
        }
        return this;
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    public List<NDescriptorProperty> toList() {
        List<NDescriptorProperty> all = new ArrayList<>();
        for (Map<NEnvCondition, NDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all;
    }

    public NDescriptorProperty[] toArray() {
        List<NDescriptorProperty> all = new ArrayList<>();
        for (Map<NEnvCondition, NDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all.toArray(new NDescriptorProperty[0]);
    }

    public NDescriptorProperty get(String name, NEnvCondition cond) {
        if (cond != null && cond.isBlank()) {
            cond = null;
        }
        Map<NEnvCondition, NDescriptorProperty> m = properties.get(name);
        if (m != null) {
            return m.get(cond);
        }
        return null;
    }

    public NDescriptorProperty[] getAll(String name) {
        Map<NEnvCondition, NDescriptorProperty> m = properties.get(name);
        if (m != null) {
            return m.values().toArray(new NDescriptorProperty[0]);
        }
        return new NDescriptorProperty[0];
    }

    public DefaultNProperties addAll(List<NDescriptorProperty> arr) {
        if (arr != null) {
            for (NDescriptorProperty p : arr) {
                add(p);
            }
        }
        return this;
    }

    public DefaultNProperties add(NDescriptorProperty p) {
        if (p != null) {
            String n = p.getName();
            Map<NEnvCondition, NDescriptorProperty> m = properties.get(n);
            if (m == null) {
                m = new LinkedHashMap<>();
                properties.put(n, m);
            }
            NEnvCondition c = p.getCondition();
            if (c != null && c.isBlank()) {
                c = null;
            }
            m.put(c, p);
        }
        return this;
    }

    public void clear() {
        properties.clear();
    }
}
