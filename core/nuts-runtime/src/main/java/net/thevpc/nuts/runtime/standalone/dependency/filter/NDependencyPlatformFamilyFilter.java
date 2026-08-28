package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.internal.rpi.NDependencyFilterRPI;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.platform.NRuntimeDistributionFamily;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.util.NFilterOp;

import java.util.*;
import java.util.stream.Collectors;


public class NDependencyPlatformFamilyFilter extends NDependencyFilterBase {

    private Set<NRuntimeDistributionFamily> accepted = EnumSet.noneOf(NRuntimeDistributionFamily.class);

    public NDependencyPlatformFamilyFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyPlatformFamilyFilter(Collection<NRuntimeDistributionFamily> accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = EnumSet.copyOf(accepted);
    }

    public NDependencyPlatformFamilyFilter(String accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = EnumSet.noneOf(NRuntimeDistributionFamily.class);
        for (NId e : NId.getList(accepted).get()) {
            if (!e.isBlank()) {
                this.accepted.add(NRuntimeDistributionFamily.parse(e.artifactId()).orNull());
            }
        }
    }

    public NDependencyPlatformFamilyFilter add(Collection<NRuntimeDistributionFamily> oses) {
        EnumSet<NRuntimeDistributionFamily> s2 = EnumSet.copyOf(this.accepted);
        NCollections.addAllNonNull(s2, oses);
        return new NDependencyPlatformFamilyFilter(s2);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        List<String> current = dependency.condition().platform();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (accepted.contains(NRuntimeDistributionFamily.parse(e).orNull())) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrEqOrIn("platform",
                        accepted.stream().map(x -> x.id()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return accepted.isEmpty() ? NDependencyFilterRPI.of().always() : this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDependencyPlatformFamilyFilter that = (NDependencyPlatformFamilyFilter) o;
        return Objects.equals(accepted, that.accepted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accepted);
    }
}
