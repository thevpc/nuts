package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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
        List<String> current = dependency.getCondition().getArch();
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
        return archs.isEmpty() ? NDependencyFilters.of().always() : this;
    }
}
