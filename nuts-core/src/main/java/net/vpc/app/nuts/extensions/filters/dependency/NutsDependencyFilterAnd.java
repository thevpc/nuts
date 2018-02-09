package net.vpc.app.nuts.extensions.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Simplifiable;

public class NutsDependencyFilterAnd implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {
    private final NutsDependencyFilter[] all;

    public NutsDependencyFilterAnd(NutsDependencyFilter... all) {
        this.all = all;
    }

    @Override
    public boolean accept(NutsDependency value) {
        for (NutsDependencyFilter nutsDependencyFilter : all) {
            if (nutsDependencyFilter != null && !nutsDependencyFilter.accept(value)) {
                return false;
            }
        }
        return true;
    }

    public NutsDependencyFilter simplify() {
        NutsDependencyFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsDependencyFilter.class, all);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsDependencyFilterAnd(newValues);
        }
        return this;
    }
}
