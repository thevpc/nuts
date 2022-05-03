package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsDescriptorProperty;
import net.thevpc.nuts.NutsEnvCondition;

import java.util.*;

public class DefaultNutsProperties {
    private Map<String, Map<NutsEnvCondition, NutsDescriptorProperty>> properties = new LinkedHashMap<>();

    public DefaultNutsProperties() {
    }

    public DefaultNutsProperties remove(String name) {
        if (name != null) {
            properties.remove(name);
        }
        return this;
    }

    public DefaultNutsProperties remove(NutsDescriptorProperty p) {
        if (p != null) {
            String n = p.getName();
            Map<NutsEnvCondition, NutsDescriptorProperty> m = properties.get(n);
            if (m != null) {
                NutsEnvCondition c = p.getCondition();
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

    public List<NutsDescriptorProperty> toList() {
        List<NutsDescriptorProperty> all = new ArrayList<>();
        for (Map<NutsEnvCondition, NutsDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all;
    }

    public NutsDescriptorProperty[] toArray() {
        List<NutsDescriptorProperty> all = new ArrayList<>();
        for (Map<NutsEnvCondition, NutsDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all.toArray(new NutsDescriptorProperty[0]);
    }

    public NutsDescriptorProperty get(String name, NutsEnvCondition cond) {
        if (cond != null && cond.isBlank()) {
            cond = null;
        }
        Map<NutsEnvCondition, NutsDescriptorProperty> m = properties.get(name);
        if (m != null) {
            return m.get(cond);
        }
        return null;
    }

    public NutsDescriptorProperty[] getAll(String name) {
        Map<NutsEnvCondition, NutsDescriptorProperty> m = properties.get(name);
        if (m != null) {
            return m.values().toArray(new NutsDescriptorProperty[0]);
        }
        return new NutsDescriptorProperty[0];
    }

    public DefaultNutsProperties addAll(List<NutsDescriptorProperty> arr) {
        if (arr != null) {
            for (NutsDescriptorProperty p : arr) {
                add(p);
            }
        }
        return this;
    }

    public DefaultNutsProperties add(NutsDescriptorProperty p) {
        if (p != null) {
            String n = p.getName();
            Map<NutsEnvCondition, NutsDescriptorProperty> m = properties.get(n);
            if (m == null) {
                m = new LinkedHashMap<>();
                properties.put(n, m);
            }
            NutsEnvCondition c = p.getCondition();
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
