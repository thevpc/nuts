package net.thevpc.nuts.runtime.core.filters.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.core.filters.id.NutsScriptAwareIdFilter;
import net.thevpc.nuts.runtime.core.util.Simplifiable;

public class NutsVersionFilterTrue extends AbstractVersionFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsScriptAwareIdFilter {

    public NutsVersionFilterTrue(NutsSession ws) {
        super(ws, NutsFilterOp.TRUE);
    }

    @Override
    public boolean acceptVersion(NutsVersion id, NutsSession session) {
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
        final NutsVersionFilterTrue other = (NutsVersionFilterTrue) obj;
        return true;
    }

    @Override
    public String toString() {
        return "true";
    }

}
