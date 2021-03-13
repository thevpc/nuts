package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;

public class NutsDependencyArchFilter extends AbstractNutsFilter implements NutsDependencyFilter {

    private Set<NutsArchFamily> archs = EnumSet.noneOf(NutsArchFamily.class);

    public NutsDependencyArchFilter(NutsWorkspace ws) {
        super(ws, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyArchFilter(NutsWorkspace ws, Collection<NutsArchFamily> os) {
        super(ws, NutsFilterOp.CUSTOM);
        this.archs = EnumSet.copyOf(os);
    }

    public NutsDependencyArchFilter(NutsWorkspace ws, String os) {
        super(ws, NutsFilterOp.CUSTOM);
        this.archs = EnumSet.noneOf(NutsArchFamily.class);
        for (String e : os.split("[,; ]")) {
            if (!e.isEmpty()) {
                this.archs.add(NutsArchFamily.parseLenient(e));
            }
        }
    }

    public NutsDependencyArchFilter add(Collection<NutsArchFamily> os) {
        EnumSet<NutsArchFamily> s2 = EnumSet.copyOf(this.archs);
        s2.addAll(os);
        return new NutsDependencyArchFilter(getWorkspace(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        String current = dependency.getArch();
        boolean empty = true;
        if (current != null) {
            for (String e : current.split("[,; ]")) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (archs.contains(NutsArchFamily.parseLenient(e))) {
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
    public NutsFilter simplify() {
        return archs.isEmpty() ? getWorkspace().filters().dependency().always() : this;
    }
}
