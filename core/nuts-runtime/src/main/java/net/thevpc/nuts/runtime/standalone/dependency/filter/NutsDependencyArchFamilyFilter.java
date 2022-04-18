package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NutsDependencyArchFamilyFilter extends AbstractDependencyFilter {

    private Set<NutsArchFamily> archs = EnumSet.noneOf(NutsArchFamily.class);

    public NutsDependencyArchFamilyFilter(NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyArchFamilyFilter(NutsSession session, Collection<NutsArchFamily> os) {
        super(session, NutsFilterOp.CUSTOM);
        this.archs = EnumSet.copyOf(os);
    }

    public NutsDependencyArchFamilyFilter(NutsSession session, String os) {
        super(session, NutsFilterOp.CUSTOM);
        this.archs = EnumSet.noneOf(NutsArchFamily.class);
        for (String e : StringTokenizerUtils.splitDefault( os)) {
            if (!e.isEmpty()) {
                this.archs.add(NutsArchFamily.parse(e).orElse(NutsArchFamily.UNKNOWN));
            }
        }
    }

    public NutsDependencyArchFamilyFilter add(Collection<NutsArchFamily> os) {
        EnumSet<NutsArchFamily> s2 = EnumSet.copyOf(this.archs);
        s2.addAll(os);
        return new NutsDependencyArchFamilyFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        List<String> current = dependency.getCondition().getArch();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (archs.contains(NutsArchFamily.parse(e).orElse(NutsArchFamily.UNKNOWN))) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return archs.isEmpty() ? "true" : "arch in (" + archs.stream().map(x -> x.id()).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsDependencyFilter simplify() {
        return archs.isEmpty() ? NutsDependencyFilters.of(getSession()).always() : this;
    }
}
