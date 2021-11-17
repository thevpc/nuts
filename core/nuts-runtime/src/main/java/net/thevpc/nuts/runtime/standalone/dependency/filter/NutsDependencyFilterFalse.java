package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;

public final class NutsDependencyFilterFalse extends AbstractDependencyFilter{

    public NutsDependencyFilterFalse(NutsSession session) {
        super(session, NutsFilterOp.FALSE);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        return false;
    }

    @Override
    public NutsDependencyFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "false";
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
