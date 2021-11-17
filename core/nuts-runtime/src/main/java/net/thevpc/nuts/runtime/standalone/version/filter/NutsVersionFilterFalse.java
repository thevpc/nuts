package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsExprIdFilter;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;

public class NutsVersionFilterFalse extends AbstractVersionFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsExprIdFilter {

    public NutsVersionFilterFalse(NutsSession session) {
        super(session, NutsFilterOp.FALSE);
    }

    @Override
    public boolean acceptVersion(NutsVersion id, NutsSession session) {
        return false;
    }

    @Override
    public NutsVersionFilter simplify() {
        return this;
    }

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
        final NutsVersionFilterFalse other = (NutsVersionFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
