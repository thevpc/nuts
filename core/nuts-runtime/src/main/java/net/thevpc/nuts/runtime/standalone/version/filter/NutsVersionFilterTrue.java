package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsExprIdFilter;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;

public class NutsVersionFilterTrue extends AbstractVersionFilter implements NutsExprIdFilter {

    public NutsVersionFilterTrue(NutsSession session) {
        super(session, NutsFilterOp.TRUE);
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
    public String toExpr() {
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
