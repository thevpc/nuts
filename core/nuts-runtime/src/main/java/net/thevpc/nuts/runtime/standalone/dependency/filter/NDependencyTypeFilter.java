package net.thevpc.nuts.runtime.standalone.dependency.filter;

import java.util.Objects;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependencyUtils;
import net.thevpc.nuts.util.NFilterOp;


public class NDependencyTypeFilter extends NDependencyFilterBase {

    private final String type;

    public NDependencyTypeFilter(String type) {
        super(NFilterOp.CUSTOM);
        this.type = type;
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        String curr = NDependencyUtils.normalizeDependencyType(dependency.type());
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDependencyTypeFilter that = (NDependencyTypeFilter) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}
