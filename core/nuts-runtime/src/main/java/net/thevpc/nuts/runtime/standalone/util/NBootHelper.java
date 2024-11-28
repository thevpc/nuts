package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NDependency;
import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.boot.NDependencyBoot;
import net.thevpc.nuts.boot.NDescriptorBoot;
import net.thevpc.nuts.boot.NIdBoot;
import net.thevpc.nuts.runtime.standalone.DefaultNDependencyBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class NBootHelper {
    public static NId toId(NIdBoot x) {
        return x == null ? null : NId.of(x.toString()).get();
    }

    public static NDependency toDependency(NDependencyBoot x) {
        return x == null ? null : new DefaultNDependencyBuilder().setAll(x).build();
    }

    public static List<NDependency> toDependencyList(List<NDependencyBoot> x) {
        return x == null ? null : x.stream().map(y -> toDependency(y)).collect(Collectors.toList());
    }

    public static NDescriptor toDescriptor(NDescriptorBoot x) {
        return x == null ? null : new DefaultNDescriptorBuilder().setAll(x).build();
    }

    public static List<NDescriptor> toDescriptorList(List<NDescriptorBoot> x) {
        return x == null ? null : x.stream().map(y -> toDescriptor(y)).collect(Collectors.toList());
    }
}
