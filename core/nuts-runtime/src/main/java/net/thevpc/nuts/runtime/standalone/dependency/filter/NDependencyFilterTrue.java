package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.util.NFilterOp;

public final class NDependencyFilterTrue extends NDependencyFilterBase {

    public NDependencyFilterTrue() {
        super(NFilterOp.TRUE);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        return true;
    }

    @Override
    public NDependencyFilter simplify() {
        return null;
    }

    @Override
    public String toString() {
        return "true";
    }

}
