package net.vpc.app.nuts.core.filters.dependency;

import net.vpc.app.nuts.*;

import java.util.Collection;
import java.util.EnumSet;

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
}
