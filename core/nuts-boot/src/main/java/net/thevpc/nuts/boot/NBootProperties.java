package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.util.*;

public class NBootProperties {
    private Map<String, Map<String, NBootDescriptorProperty>> properties = new LinkedHashMap<>();

    public NBootProperties() {
    }

    public NBootProperties remove(String name) {
        if (name != null) {
            properties.remove(name);
        }
        return this;
    }
    private String condKey(NBootDescriptorProperty d){
        NBootDependency d2=new NBootDependency();
        d2.setConditionOs(d.getConditionOs());
        d2.setConditionOsDist(d.getConditionOsDist());
        d2.setConditionArch(d.getConditionArch());
        d2.setConditionDesktopEnvironment(d.getConditionDesktopEnvironment());
        d2.setConditionProfile(d.getConditionProfiles());
        d2.setConditionPlatform(d.getConditionPlatform());
        d2.setConditionProperties(d.getConditionProperties());
        d2.setGroupId("g");
        d2.setArtifactId("a");
        String s = d2.toString();
        int i=s.indexOf("?");
        if(i>0){
            return s.substring(i+1);
        }
        return "";
    }

    public NBootProperties remove(NBootDescriptorProperty p) {
        if (p != null) {
            String n = p.getName();
            Map<String, NBootDescriptorProperty> m = properties.get(n);
            if (m != null) {
                String c = condKey(p);
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
        for (Map<String, NBootDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all;
    }

    public NBootDescriptorProperty[] toArray() {
        List<NBootDescriptorProperty> all = new ArrayList<>();
        for (Map<String, NBootDescriptorProperty> value : properties.values()) {
            all.addAll(value.values());
        }
        return all.toArray(new NBootDescriptorProperty[0]);
    }

//    public NBootDescriptorProperty get(String name, NBootEnvCondition cond) {
//        if (cond != null && cond.isBlank()) {
//            cond = null;
//        }
//        Map<String, NBootDescriptorProperty> m = properties.get(name);
//        if (m != null) {
//            return m.get(cond);
//        }
//        return null;
//    }

    public NBootDescriptorProperty[] getAll(String name) {
        Map<String, NBootDescriptorProperty> m = properties.get(name);
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
            Map<String, NBootDescriptorProperty> m = properties.get(n);
            if (m == null) {
                m = new LinkedHashMap<>();
                properties.put(n, m);
            }
            String c = condKey(p);
            m.put(c, p);
        }
        return this;
    }

    public void clear() {
        properties.clear();
    }
}
