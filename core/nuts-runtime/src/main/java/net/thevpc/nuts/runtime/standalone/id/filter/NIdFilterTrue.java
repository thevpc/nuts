package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;
import net.thevpc.nuts.util.NFilterOp;

public class NIdFilterTrue extends AbstractIdFilter implements NIdFilter, Simplifiable<NIdFilter>, NExprIdFilter {

    public NIdFilterTrue(NSession session) {
        super(session, NFilterOp.TRUE);
    }

    @Override
    public boolean acceptId(NId id, NSession session) {
        return true;
    }

    @Override
    public NIdFilter simplify() {
        return this;
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
        final NIdFilterTrue other = (NIdFilterTrue) obj;
        return true;
    }

    @Override
    public String toString() {
        return "true";
    }

}
