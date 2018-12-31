package net.vpc.app.nuts.extensions.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.Simplifiable;

public class NutsDependencyOptionFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {
    public static final NutsDependencyFilter OPTIONAL = new NutsDependencyOptionFilter(true);
    public static final NutsDependencyFilter NON_OPTIONAL = new NutsDependencyOptionFilter(false);

    public static final NutsDependencyFilter valueOf(boolean b) {
        return b ? OPTIONAL : NON_OPTIONAL;
    }

    private Boolean optional;

    public NutsDependencyOptionFilter(Boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency) {
        if (optional == null) {
            return false;
        }
        String o = dependency.getOptional();
        if (o == null) {
            o = "";
        }
        o = o.trim().toLowerCase();
        return optional == (o.isEmpty() || o.equals("true"));
    }

    @Override
    public NutsDependencyFilter simplify() {
        if (optional == null) {
            return null;
        }
        return this;
    }
}
