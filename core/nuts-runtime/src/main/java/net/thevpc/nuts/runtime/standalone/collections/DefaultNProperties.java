package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.artifact.NDescriptorProperty;
import net.thevpc.nuts.artifact.NEnvCondition;
import net.thevpc.nuts.util.NProperties;

import java.util.*;

public class DefaultNProperties implements NProperties {
    private Map<String, Map<NEnvCondition, NDescriptorProperty>> properties = new LinkedHashMap<>();

    public DefaultNProperties() {
    }

    @Override
    public DefaultNProperties remove(String name) {
        if (name != null) {
            properties.remove(name);
        }
        return this;
    }

    @Override
    public DefaultNProperties remove(NDescriptorProperty p) {
        if (p != null) {
            String n = p.name();
            Map<NEnvCondition, NDescriptorProperty> m = properties.get(n);
            if (m != null) {
                NEnvCondition c = p.condition();
                if (c != null && c.isBlank()) {
                    c = null;
                }
                m.remove(c);
            }
        }
        return this;
    }

    @Override
    public Set<String> keySet() {
        return properties.keySet();
    }

    @Override
    public List<NDescriptorProperty> toList() {
        List<NDescriptorProperty> all = new ArrayList<>();
        for (Map<NEnvCondition, NDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all;
    }

    @Override
    public NDescriptorProperty[] toArray() {
        List<NDescriptorProperty> all = new ArrayList<>();
        for (Map<NEnvCondition, NDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all.toArray(new NDescriptorProperty[0]);
    }

    @Override
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

    @Override
    public NDescriptorProperty[] getAll(String name) {
        Map<NEnvCondition, NDescriptorProperty> m = properties.get(name);
        if (m != null) {
            return m.values().toArray(new NDescriptorProperty[0]);
        }
        return new NDescriptorProperty[0];
    }

    @Override
    public DefaultNProperties addAll(List<NDescriptorProperty> arr) {
        if (arr != null) {
            for (NDescriptorProperty p : arr) {
                add(p);
            }
        }
        return this;
    }

    @Override
    public DefaultNProperties add(NDescriptorProperty p) {
        if (p != null) {
            String n = p.name();
            Map<NEnvCondition, NDescriptorProperty> m = properties.get(n);
            if (m == null) {
                m = new LinkedHashMap<>();
                properties.put(n, m);
            }
            NEnvCondition c = p.condition();
            if (c != null && c.isBlank()) {
                c = null;
            }
            m.put(c, p);
        }
        return this;
    }

    @Override
    public void clear() {
        properties.clear();
    }
}
