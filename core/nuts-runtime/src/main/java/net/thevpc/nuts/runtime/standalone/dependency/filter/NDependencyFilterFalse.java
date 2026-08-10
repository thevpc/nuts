package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.util.NFilterOp;

public final class NDependencyFilterFalse extends AbstractDependencyFilter{

    public NDependencyFilterFalse() {
        super(NFilterOp.FALSE);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        return false;
    }

    @Override
    public NDependencyFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "false";
    }


}
