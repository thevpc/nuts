package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

public final class NDependencyFilterFalse extends AbstractDependencyFilter{

    public NDependencyFilterFalse(NWorkspace workspace) {
        super(workspace, NFilterOp.FALSE);
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency) {
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
