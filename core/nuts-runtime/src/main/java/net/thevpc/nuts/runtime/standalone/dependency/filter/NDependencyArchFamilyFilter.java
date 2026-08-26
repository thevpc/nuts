package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.internal.rpi.NDependencyFilterRPI;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.util.NFilterOp;

import java.util.*;
import java.util.stream.Collectors;


public class NDependencyArchFamilyFilter extends AbstractDependencyFilter {

    private Set<NArchFamily> archs = EnumSet.noneOf(NArchFamily.class);

    public NDependencyArchFamilyFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyArchFamilyFilter(Collection<NArchFamily> os) {
        super(NFilterOp.CUSTOM);
        this.archs = EnumSet.copyOf(os);
    }

    public NDependencyArchFamilyFilter(String os) {
        super(NFilterOp.CUSTOM);
        this.archs = EnumSet.noneOf(NArchFamily.class);
        for (String e : StringTokenizerUtils.splitDefault( os)) {
            if (!e.isEmpty()) {
                this.archs.add(NArchFamily.parse(e).orElse(NArchFamily.UNKNOWN));
            }
        }
    }

    public NDependencyArchFamilyFilter add(Collection<NArchFamily> oses) {
        EnumSet<NArchFamily> s2 = EnumSet.copyOf(this.archs);
        NCollections.addAllNonNull(s2, oses);
        return new NDependencyArchFamilyFilter(s2);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        List<String> current = dependency.condition().arch();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (archs.contains(NArchFamily.parse(e).orElse(NArchFamily.UNKNOWN))) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return
                CoreStringUtils.trueOrEqOrIn("arch",
                        archs.stream().map(x -> x.id()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return archs.isEmpty() ? NDependencyFilterRPI.of().always() : this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDependencyArchFamilyFilter that = (NDependencyArchFamilyFilter) o;
        return Objects.equals(archs, that.archs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), archs);
    }
}
