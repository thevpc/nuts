package net.vpc.app.nuts.extensions.filters.version;

import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsJsAwareIdFilter;
import net.vpc.app.nuts.extensions.util.Simplifiable;

public class AllNutsVersionFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsJsAwareIdFilter {

    @Override
    public boolean accept(NutsVersion version) {
        return true;
    }

    @Override
    public NutsVersionFilter simplify() {
        return null;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        return "true";
    }

    @Override
    public String toString() {
        return "AllVersions";
    }

    @Override
    public int hashCode() {
        return 3368;
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
