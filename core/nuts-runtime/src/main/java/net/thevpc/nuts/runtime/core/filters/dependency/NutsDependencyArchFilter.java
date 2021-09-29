package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;


public class NutsDependencyArchFilter extends AbstractDependencyFilter {

    private Set<NutsArchFamily> archs = EnumSet.noneOf(NutsArchFamily.class);

    public NutsDependencyArchFilter(NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyArchFilter(NutsSession session, Collection<NutsArchFamily> os) {
        super(session, NutsFilterOp.CUSTOM);
        this.archs = EnumSet.copyOf(os);
    }

    public NutsDependencyArchFilter(NutsSession session, String os) {
        super(session, NutsFilterOp.CUSTOM);
        this.archs = EnumSet.noneOf(NutsArchFamily.class);
        for (String e : os.split("[,; ]")) {
            if (!e.isEmpty()) {
                this.archs.add(NutsArchFamily.parseLenient(e,NutsArchFamily.UNKNOWN));
            }
        }
    }

    public NutsDependencyArchFilter add(Collection<NutsArchFamily> os) {
        EnumSet<NutsArchFamily> s2 = EnumSet.copyOf(this.archs);
        s2.addAll(os);
        return new NutsDependencyArchFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        String[] current = dependency.getCondition().getArch();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (archs.contains(NutsArchFamily.parseLenient(e,NutsArchFamily.UNKNOWN))) {
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
        return archs.isEmpty() ? getWorkspace().filters().dependency().always() : this;
    }
}
