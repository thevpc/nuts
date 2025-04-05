package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

public final class NDependencyFilterTrue extends AbstractDependencyFilter{

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
        return true;
    }

}
