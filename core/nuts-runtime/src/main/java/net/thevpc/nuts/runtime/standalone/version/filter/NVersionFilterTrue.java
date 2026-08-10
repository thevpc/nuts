package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.spi.base.AbstractVersionFilter;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;
import net.thevpc.nuts.util.NFilterOp;

public class NVersionFilterTrue extends AbstractVersionFilter implements NExprIdFilter {

    public NVersionFilterTrue() {
        super(NFilterOp.TRUE);
    }

    @Override
    public boolean acceptVersion(NVersion id) {
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
    public String toString() {
        return "true";
    }

}
