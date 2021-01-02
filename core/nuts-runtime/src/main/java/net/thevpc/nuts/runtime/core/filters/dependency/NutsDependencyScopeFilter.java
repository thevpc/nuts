package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;

public class NutsDependencyScopeFilter extends AbstractNutsFilter implements NutsDependencyFilter {

    private EnumSet<NutsDependencyScope> scope=EnumSet.noneOf(NutsDependencyScope.class);

    public NutsDependencyScopeFilter(NutsWorkspace ws) {
        super(ws,NutsFilterOp.CUSTOM);
    }

    private NutsDependencyScopeFilter(NutsWorkspace ws,Collection<NutsDependencyScope> scope) {
        super(ws,NutsFilterOp.CUSTOM);
        this.scope = EnumSet.copyOf(scope);
    }

    public NutsDependencyScopeFilter addScopes(Collection<NutsDependencyScope> scope) {
        EnumSet<NutsDependencyScope> s2 = EnumSet.copyOf(this.scope);
        s2.addAll(scope);
        return new NutsDependencyScopeFilter(getWorkspace(),s2);
    }

    public NutsDependencyScopeFilter addScopePatterns(Collection<NutsDependencyScopePattern> scope) {
        EnumSet<NutsDependencyScope> s2 = EnumSet.copyOf(this.scope);
        for (NutsDependencyScopePattern ss : scope) {
            s2.addAll(NutsDependencyScopes.expand(ss));
        }
        return new NutsDependencyScopeFilter(getWorkspace(),s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        return scope.isEmpty() || scope.contains(NutsDependencyScopes.parseDependencyScope(dependency.getScope()));
    }

    @Override
    public String toString() {
        return "scope in (" + scope.stream().map(CoreCommonUtils::getEnumString).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsFilter simplify() {
        return this;
    }
}
