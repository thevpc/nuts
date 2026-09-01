package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.internal.rpi.NDependencyFilterRPI;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.util.NFilterOp;

import java.util.*;
import java.util.stream.Collectors;


public class NDependencyOsFilter extends NDependencyFilterBase {

    private Set<NOsFamily> os = EnumSet.noneOf(NOsFamily.class);

    public NDependencyOsFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyOsFilter(Collection<NOsFamily> os) {
        super(NFilterOp.CUSTOM);
        this.os = EnumSet.copyOf(os);
    }

    public NDependencyOsFilter(String os) {
        super(NFilterOp.CUSTOM);
        this.os = EnumSet.noneOf(NOsFamily.class);
        for (String e : StringTokenizerUtils.splitDefault(os)) {
            if (!e.isEmpty()) {
                this.os.add(NOsFamily.parse(e).orNull());
            }
        }
    }

    public NDependencyOsFilter add(Collection<NOsFamily> oses) {
        if(oses==null) {
            return this;
        }
        EnumSet<NOsFamily> s2 = EnumSet.copyOf(this.os);
        NCollections.addAllNonNull(s2, oses);
        return new NDependencyOsFilter(s2);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        List<String> current = dependency.condition().os();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (os.contains(NOsFamily.parse(e).orNull())) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrEqOrIn("os",
                        os.stream().map(x -> x.id()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return os.isEmpty() ? NDependencyFilterRPI.of().always() : this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDependencyOsFilter that = (NDependencyOsFilter) o;
        return Objects.equals(os, that.os);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), os);
    }
}
