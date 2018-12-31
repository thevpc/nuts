package net.vpc.app.nuts.extensions.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Simplifiable;
import net.vpc.common.strings.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NutsDependencyFilterAnd implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private final NutsDependencyFilter[] all;

    public NutsDependencyFilterAnd(NutsDependencyFilter... all) {
        this.all = all;
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency) {
        for (NutsDependencyFilter nutsDependencyFilter : all) {
            if (nutsDependencyFilter != null && !nutsDependencyFilter.accept(from, dependency)) {
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

    @Override
    public String toString() {
        return StringUtils.join(" And ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Arrays.deepHashCode(this.all);
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
        final NutsDependencyFilterAnd other = (NutsDependencyFilterAnd) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }
    
    

}
