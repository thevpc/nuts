package net.thevpc.nuts.util;

import net.thevpc.nuts.artifact.NDescriptorProperty;
import net.thevpc.nuts.artifact.NEnvCondition;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;
import java.util.Set;

public interface NProperties {
    static NProperties of(){
        return NUtilsRPI.of().createProperties();
    }
    NProperties remove(String name);

    NProperties remove(NDescriptorProperty p);

    Set<String> keySet();

    List<NDescriptorProperty> toList();

    NDescriptorProperty[] toArray();

    NDescriptorProperty get(String name, NEnvCondition cond);

    NDescriptorProperty[] getAll(String name);

    NProperties addAll(List<NDescriptorProperty> arr);

    NProperties add(NDescriptorProperty p);

    void clear();
}
