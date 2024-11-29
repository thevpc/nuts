package net.thevpc.nuts.boot;

import java.util.*;

public class NBootProperties {
    private Map<String, Map<NBootEnvCondition, NBootDescriptorProperty>> properties = new LinkedHashMap<>();

    public NBootProperties() {
    }

    public NBootProperties remove(String name) {
        if (name != null) {
            properties.remove(name);
        }
        return this;
    }

    public NBootProperties remove(NBootDescriptorProperty p) {
        if (p != null) {
            String n = p.getName();
            Map<NBootEnvCondition, NBootDescriptorProperty> m = properties.get(n);
            if (m != null) {
                NBootEnvCondition c = p.getCondition();
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

    public List<NBootDescriptorProperty> toList() {
        List<NBootDescriptorProperty> all = new ArrayList<>();
        for (Map<NBootEnvCondition, NBootDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all;
    }

    public NBootDescriptorProperty[] toArray() {
        List<NBootDescriptorProperty> all = new ArrayList<>();
        for (Map<NBootEnvCondition, NBootDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all.toArray(new NBootDescriptorProperty[0]);
    }

    public NBootDescriptorProperty get(String name, NBootEnvCondition cond) {
        if (cond != null && cond.isBlank()) {
            cond = null;
        }
        Map<NBootEnvCondition, NBootDescriptorProperty> m = properties.get(name);
        if (m != null) {
            return m.get(cond);
        }
        return null;
    }

    public NBootDescriptorProperty[] getAll(String name) {
        Map<NBootEnvCondition, NBootDescriptorProperty> m = properties.get(name);
        if (m != null) {
            return m.values().toArray(new NBootDescriptorProperty[0]);
        }
        return new NBootDescriptorProperty[0];
    }

    public NBootProperties addAll(List<NBootDescriptorProperty> arr) {
        if (arr != null) {
            for (NBootDescriptorProperty p : arr) {
                add(p);
            }
        }
        return this;
    }

    public NBootProperties add(NBootDescriptorProperty p) {
        if (p != null) {
            String n = p.getName();
            Map<NBootEnvCondition, NBootDescriptorProperty> m = properties.get(n);
            if (m == null) {
                m = new LinkedHashMap<>();
                properties.put(n, m);
            }
            NBootEnvCondition c = p.getCondition();
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
