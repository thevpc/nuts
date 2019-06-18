package net.vpc.app.nuts.core.filters.dependency;

import net.vpc.app.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

public class NutsDependencyScopeFilter implements NutsDependencyFilter {

    private EnumSet<NutsDependencyScope> scope;

    public NutsDependencyScopeFilter(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.expand(scope);
        if (this.scope.isEmpty()) {
            this.scope = NutsDependencyScope.expand(EnumSet.of(NutsDependencyScope.ALL));
        }
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency, NutsSession session) {
        return scope.contains(NutsDependencyScope.lenientParse(dependency.getScope()));
    }

    @Override
    public String toString() {
        return "scope in (" + scope.stream().map(x -> CoreCommonUtils.getEnumString(x)).collect(Collectors.joining(", ")) + ')';
    }

}
