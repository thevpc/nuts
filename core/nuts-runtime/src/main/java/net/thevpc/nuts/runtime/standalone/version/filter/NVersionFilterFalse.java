package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.spi.base.NVersionFilterBase;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;
import net.thevpc.nuts.util.NFilterOp;

public class NVersionFilterFalse extends NVersionFilterBase implements NExprIdFilter {

    public NVersionFilterFalse() {
        super(NFilterOp.FALSE);
    }

    @Override
    public boolean acceptVersion(NVersion id) {
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
    public String toString() {
        return "false";
    }

}
