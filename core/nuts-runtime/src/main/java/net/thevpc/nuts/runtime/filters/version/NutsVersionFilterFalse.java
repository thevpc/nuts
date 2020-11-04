package net.thevpc.nuts.runtime.filters.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.filters.id.NutsScriptAwareIdFilter;
import net.thevpc.nuts.runtime.util.common.Simplifiable;

public class NutsVersionFilterFalse extends AbstractNutsFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsScriptAwareIdFilter {

    public NutsVersionFilterFalse(NutsWorkspace ws) {
        super(ws, NutsFilterOp.FALSE);
    }

    @Override
    public boolean acceptVersion(NutsVersion id, NutsSession session) {
        return false;
    }

    @Override
    public NutsVersionFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
            return "false";
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
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
        final NutsVersionFilterFalse other = (NutsVersionFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
