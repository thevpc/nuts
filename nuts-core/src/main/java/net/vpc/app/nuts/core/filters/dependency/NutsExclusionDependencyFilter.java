package net.vpc.app.nuts.core.filters.dependency;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.Simplifiable;

import java.util.Arrays;
import java.util.Objects;

public class NutsExclusionDependencyFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private NutsDependencyFilter base;
    private NutsId[] exclusions;

    public NutsExclusionDependencyFilter(NutsDependencyFilter base, NutsId[] exclusions) {
        this.base = base;
        this.exclusions = exclusions;
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency, NutsWorkspace ws, NutsSession session) {
        if (base != null) {
            if (!base.accept(from, dependency, ws, session)) {
                return false;
            }
        }
        for (NutsId exclusion : exclusions) {
            NutsId nutsId = dependency.getId();
            if (nutsId.groupToken().like(exclusion.getGroup())
                    && nutsId.nameToken().like(exclusion.getName())
                    && exclusion.getVersion().toFilter().accept(nutsId.getVersion(), ws, session)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsDependencyFilter simplify() {
        if (exclusions.length == 0) {
            return base;
        }
        NutsDependencyFilter base2 = CoreNutsUtils.simplify(base);
        if (base2 != base) {
            return new NutsExclusionDependencyFilter(base2, exclusions);
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.base);
        hash = 67 * hash + Arrays.deepHashCode(this.exclusions);
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
        final NutsExclusionDependencyFilter other = (NutsExclusionDependencyFilter) obj;
        if (!Objects.equals(this.base, other.base)) {
            return false;
        }
        if (!Arrays.deepEquals(this.exclusions, other.exclusions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsExclusionDependencyFilter{" + "base=" + base + ", exclusions=" + (exclusions==null?"":Arrays.toString(exclusions)) + '}';
    }

}
