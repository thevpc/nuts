package net.thevpc.nuts.runtime.standalone.dependency.filter;

import java.util.Objects;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.util.NutsDependencyUtils;


public class NutsDependencyTypeFilter extends AbstractDependencyFilter {

    private String type = null;

    public NutsDependencyTypeFilter(NutsSession session, String type) {
        super(session, NutsFilterOp.CUSTOM);
        this.type = type;
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        String curr = NutsDependencyUtils.normalizeDependencyType(dependency.getType());
        String toCheck = NutsDependencyUtils.normalizeDependencyType(type);
        return Objects.equals(curr, toCheck);
    }

    @Override
    public String toString() {
        return (type == null || type.isEmpty()) ? "empty-type" : "type=" + type;
    }

    @Override
    public NutsDependencyFilter simplify() {
        return this;
    }
}
