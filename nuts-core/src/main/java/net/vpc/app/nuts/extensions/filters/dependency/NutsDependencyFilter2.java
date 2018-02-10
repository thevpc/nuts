package net.vpc.app.nuts.extensions.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Simplifiable;

public class NutsDependencyFilter2 implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private NutsDependencyFilter base;
    private NutsId[] exclusions;

    public NutsDependencyFilter2(NutsDependencyFilter base, NutsId[] exclusions) {
        this.base = base;
        this.exclusions = exclusions;
    }

    @Override
    public boolean accept(NutsDependency value) {
        if (base != null) {
            if (!base.accept(value)) {
                return false;
            }
        }
        for (NutsId exclusion : exclusions) {
            NutsId nutsId = value.toId();
            if (nutsId.groupLike(exclusion.getGroup())
                    && nutsId.nameLike(exclusion.getName())
                    && exclusion.getVersion().toFilter().accept(nutsId.getVersion())) {
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
            return new NutsDependencyFilter2(base2, exclusions);
        }
        return this;
    }
}
