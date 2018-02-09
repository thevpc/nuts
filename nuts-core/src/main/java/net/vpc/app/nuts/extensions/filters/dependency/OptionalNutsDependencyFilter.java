package net.vpc.app.nuts.extensions.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.extensions.util.Simplifiable;

public class OptionalNutsDependencyFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private boolean optional;

    public OptionalNutsDependencyFilter(boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean accept(NutsDependency value) {
        return optional == value.isOptional();
    }

    @Override
    public NutsDependencyFilter simplify() {
        return this;
    }
}
