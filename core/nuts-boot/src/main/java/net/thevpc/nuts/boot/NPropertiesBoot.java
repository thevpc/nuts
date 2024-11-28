package net.thevpc.nuts.boot;

import java.util.*;

public class NPropertiesBoot {
    private Map<String, Map<NEnvConditionBoot, NDescriptorPropertyBoot>> properties = new LinkedHashMap<>();

    public NPropertiesBoot() {
    }

    public NPropertiesBoot remove(String name) {
        if (name != null) {
            properties.remove(name);
        }
        return this;
    }

    public NPropertiesBoot remove(NDescriptorPropertyBoot p) {
        if (p != null) {
            String n = p.getName();
            Map<NEnvConditionBoot, NDescriptorPropertyBoot> m = properties.get(n);
            if (m != null) {
                NEnvConditionBoot c = p.getCondition();
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

    public List<NDescriptorPropertyBoot> toList() {
        List<NDescriptorPropertyBoot> all = new ArrayList<>();
        for (Map<NEnvConditionBoot, NDescriptorPropertyBoot> value : properties.values()) {
            all.addAll(value.values());
        }
        return all;
    }

    public NDescriptorPropertyBoot[] toArray() {
        List<NDescriptorPropertyBoot> all = new ArrayList<>();
        for (Map<NEnvConditionBoot, NDescriptorPropertyBoot> value : properties.values()) {
            all.addAll(value.values());
        }
        return all.toArray(new NDescriptorPropertyBoot[0]);
    }

    public NDescriptorPropertyBoot get(String name, NEnvConditionBoot cond) {
        if (cond != null && cond.isBlank()) {
            cond = null;
        }
        Map<NEnvConditionBoot, NDescriptorPropertyBoot> m = properties.get(name);
        if (m != null) {
            return m.get(cond);
        }
        return null;
    }

    public NDescriptorPropertyBoot[] getAll(String name) {
        Map<NEnvConditionBoot, NDescriptorPropertyBoot> m = properties.get(name);
        if (m != null) {
            return m.values().toArray(new NDescriptorPropertyBoot[0]);
        }
        return new NDescriptorPropertyBoot[0];
    }

    public NPropertiesBoot addAll(List<NDescriptorPropertyBoot> arr) {
        if (arr != null) {
            for (NDescriptorPropertyBoot p : arr) {
                add(p);
            }
        }
        return this;
    }

    public NPropertiesBoot add(NDescriptorPropertyBoot p) {
        if (p != null) {
            String n = p.getName();
            Map<NEnvConditionBoot, NDescriptorPropertyBoot> m = properties.get(n);
            if (m == null) {
                m = new LinkedHashMap<>();
                properties.put(n, m);
            }
            NEnvConditionBoot c = p.getCondition();
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
