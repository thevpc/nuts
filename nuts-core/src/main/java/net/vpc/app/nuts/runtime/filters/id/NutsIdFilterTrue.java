package net.vpc.app.nuts.runtime.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

public class NutsIdFilterTrue extends AbstractNutsFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsScriptAwareIdFilter {

    public NutsIdFilterTrue(NutsWorkspace ws) {
        super(ws, NutsFilterOp.TRUE);
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        return true;
    }

    @Override
    public NutsIdFilter simplify() {
        return this;
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
        final NutsIdFilterTrue other = (NutsIdFilterTrue) obj;
        return true;
    }

    @Override
    public String toString() {
        return "true";
    }

}
