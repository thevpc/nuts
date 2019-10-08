package net.vpc.app.nuts.runtime.filters.dependency;

import net.vpc.app.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import net.vpc.app.nuts.runtime.util.NutsDependencyScopes;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;

public class NutsDependencyScopeFilter implements NutsDependencyFilter {

    private EnumSet<NutsDependencyScope> scope=EnumSet.noneOf(NutsDependencyScope.class);

    public NutsDependencyScopeFilter() {

    }

    private NutsDependencyScopeFilter(Collection<NutsDependencyScope> scope) {
        this.scope = EnumSet.copyOf(scope);
    }

    public NutsDependencyScopeFilter addScopes(Collection<NutsDependencyScope> scope) {
        EnumSet<NutsDependencyScope> s2 = EnumSet.copyOf(this.scope);
        s2.addAll(scope);
        return new NutsDependencyScopeFilter(s2);
    }

    public NutsDependencyScopeFilter addScopePatterns(Collection<NutsDependencyScopePattern> scope) {
        EnumSet<NutsDependencyScope> s2 = EnumSet.copyOf(this.scope);
        for (NutsDependencyScopePattern ss : scope) {
            s2.addAll(NutsDependencyScopes.expand(ss));
        }
        return new NutsDependencyScopeFilter(s2);
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency, NutsSession session) {
        return scope.isEmpty() || scope.contains(NutsDependencyScopes.parseDependencyScope(dependency.getScope()));
    }

    @Override
    public String toString() {
        return "scope in (" + scope.stream().map(CoreCommonUtils::getEnumString).collect(Collectors.joining(", ")) + ')';
    }

}
