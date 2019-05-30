package net.vpc.app.nuts.core.filters.dependency;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.Simplifiable;

import java.util.Arrays;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

public class NutsDependencyFilterOr implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private final NutsDependencyFilter[] all;

    public NutsDependencyFilterOr(NutsDependencyFilter... all) {
        this.all = all;
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency, NutsWorkspace ws, NutsSession session) {
        boolean one = false;
        for (NutsDependencyFilter nutsDependencyFilter : all) {
            if (nutsDependencyFilter != null) {
                one = true;
                if (nutsDependencyFilter.accept(from, dependency, ws, session)) {
                    return true;
                }
            }
        }
        return one ? false : true;
    }

    @Override
    public NutsDependencyFilter simplify() {
        if(all.length==0){
            return null;
        }
        NutsDependencyFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsDependencyFilter.class, all);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsDependencyFilterOr(newValues);
        }
        if (all.length == 0) {
            return null;
        }
        if (all.length == 1) {
            return all[0];
        }
        return this;
    }

        @Override
    public String toString() {
        return CoreStringUtils.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Arrays.deepHashCode(this.all);
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
        final NutsDependencyFilterOr other = (NutsDependencyFilterOr) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }
    

}
