package net.vpc.app.nuts.extensions.filters.version;

import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.extensions.filters.id.JsNutsIdFilter;
import net.vpc.app.nuts.extensions.util.Simplifiable;

public class AllNutsVersionFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, JsNutsIdFilter {

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

}
