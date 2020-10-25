package net.vpc.app.nuts.runtime.filters.dependency;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

public final class NutsDependencyFilterTrue extends AbstractNutsFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    public NutsDependencyFilterTrue(NutsWorkspace ws) {
        super(ws, NutsFilterOp.TRUE);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        return true;
    }

    @Override
    public NutsDependencyFilter simplify() {
        return null;
    }

    @Override
    public String toString() {
        return "true";
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

}
