package net.vpc.app.nuts.core.filters.dependency;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.Simplifiable;

public class OptionalNutsDependencyFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private boolean optional;

    public OptionalNutsDependencyFilter(boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency, NutsWorkspace ws, NutsSession session) {
        return optional == dependency.isOptional();
    }

    @Override
    public NutsDependencyFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "OptionalNutsDependencyFilter{" + optional + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.optional ? 1 : 0);
        return hash;
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
        final OptionalNutsDependencyFilter other = (OptionalNutsDependencyFilter) obj;
        if (this.optional != other.optional) {
            return false;
        }
        return true;
    }
    
    
}
