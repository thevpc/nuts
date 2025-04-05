package net.thevpc.nuts.runtime.standalone.dependency.filter;

import java.util.Objects;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependencyUtils;
import net.thevpc.nuts.util.NFilterOp;


public class NDependencyTypeFilter extends AbstractDependencyFilter {

    private String type = null;

    public NDependencyTypeFilter(String type) {
        super(NFilterOp.CUSTOM);
        this.type = type;
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        String curr = NDependencyUtils.normalizeDependencyType(dependency.getType());
        String toCheck = NDependencyUtils.normalizeDependencyType(type);
        return Objects.equals(curr, toCheck);
    }

    @Override
    public String toString() {
        return (type == null || type.isEmpty()) ? "empty-type" : "type=" + type;
    }

    @Override
    public NDependencyFilter simplify() {
        return this;
    }
}
