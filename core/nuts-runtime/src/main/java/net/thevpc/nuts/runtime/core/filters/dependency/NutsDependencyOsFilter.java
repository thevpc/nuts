package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;

public class NutsDependencyOsFilter extends AbstractNutsFilter implements NutsDependencyFilter {

    private Set<NutsOsFamily> os = EnumSet.noneOf(NutsOsFamily.class);

    public NutsDependencyOsFilter(NutsWorkspace ws) {
        super(ws, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyOsFilter(NutsWorkspace ws, Collection<NutsOsFamily> os) {
        super(ws, NutsFilterOp.CUSTOM);
        this.os = EnumSet.copyOf(os);
    }

    public NutsDependencyOsFilter(NutsWorkspace ws, String os) {
        super(ws, NutsFilterOp.CUSTOM);
        this.os = EnumSet.noneOf(NutsOsFamily.class);
        for (String e : os.split("[,; ]")) {
            if (!e.isEmpty()) {
                this.os.add(NutsOsFamily.parseLenient(e));
            }
        }
    }

    public NutsDependencyOsFilter add(Collection<NutsOsFamily> os) {
        EnumSet<NutsOsFamily> s2 = EnumSet.copyOf(this.os);
        s2.addAll(os);
        return new NutsDependencyOsFilter(getWorkspace(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        String current = dependency.getOs();
        boolean empty = true;
        if (current != null) {
            for (String e : current.split("[,; ]")) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (os.contains(NutsOsFamily.parseLenient(e))) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return os.isEmpty() ? "true" : "os in (" + os.stream().map(x -> x.id()).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsFilter simplify() {
        return os.isEmpty() ? getWorkspace().filters().dependency().always() : this;
    }
}
