package net.vpc.app.nuts.extensions.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsId;

import java.util.Collection;
import java.util.EnumSet;

public class NutsDependencyScopeFilter implements NutsDependencyFilter {
    private EnumSet<NutsDependencyScope> scope;

    public NutsDependencyScopeFilter(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.expand(scope);
        if(this.scope.isEmpty()){
            this.scope = NutsDependencyScope.expand(EnumSet.of(NutsDependencyScope.ALL));
        }
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency) {
        return scope.contains(NutsDependencyScope.lenientParse(dependency.getScope()));
    }
}
