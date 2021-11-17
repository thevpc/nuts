package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;

public class NutsIdFilterFalse extends AbstractIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsExprIdFilter {

    public NutsIdFilterFalse(NutsSession session) {
        super(session, NutsFilterOp.FALSE);
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        return false;
    }

    @Override
    public NutsIdFilter simplify() {
        return this;
    }

    @Override
    public String toExpr() {
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
        final NutsIdFilterFalse other = (NutsIdFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
