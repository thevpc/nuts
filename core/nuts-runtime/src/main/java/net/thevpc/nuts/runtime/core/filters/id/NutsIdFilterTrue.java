package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.core.util.Simplifiable;

public class NutsIdFilterTrue extends AbstractIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsScriptAwareIdFilter {

    public NutsIdFilterTrue(NutsSession session) {
        super(session, NutsFilterOp.TRUE);
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
