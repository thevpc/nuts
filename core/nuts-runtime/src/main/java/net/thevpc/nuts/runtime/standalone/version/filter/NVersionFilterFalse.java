package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;

public class NVersionFilterFalse extends AbstractVersionFilter implements NExprIdFilter {

    public NVersionFilterFalse(NSession session) {
        super(session, NFilterOp.FALSE);
    }

    @Override
    public boolean acceptVersion(NVersion id, NSession session) {
        return false;
    }

    @Override
    public NVersionFilter simplify() {
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
        final NVersionFilterFalse other = (NVersionFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
