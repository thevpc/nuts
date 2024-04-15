package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.base.AbstractVersionFilter;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;
import net.thevpc.nuts.util.NFilterOp;

public class NVersionFilterTrue extends AbstractVersionFilter implements NExprIdFilter {

    public NVersionFilterTrue(NSession session) {
        super(session, NFilterOp.TRUE);
    }

    @Override
    public boolean acceptVersion(NVersion id, NSession session) {
        return true;
    }

    @Override
    public NVersionFilter simplify() {
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
        final NVersionFilterTrue other = (NVersionFilterTrue) obj;
        return true;
    }

    @Override
    public String toString() {
        return "true";
    }

}
