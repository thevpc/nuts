package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.core.util.CoreEnumUtils;

public class NutsDependencyScopeFilter extends AbstractDependencyFilter {

    private EnumSet<NutsDependencyScope> scope=EnumSet.noneOf(NutsDependencyScope.class);

    public NutsDependencyScopeFilter(NutsSession session) {
        super(session,NutsFilterOp.CUSTOM);
    }

    private NutsDependencyScopeFilter(NutsSession session,Collection<NutsDependencyScope> scope) {
        super(session,NutsFilterOp.CUSTOM);
        this.scope = EnumSet.copyOf(scope);
    }

    public NutsDependencyScopeFilter add(Collection<NutsDependencyScope> scope) {
        EnumSet<NutsDependencyScope> s2 = EnumSet.copyOf(this.scope);
        s2.addAll(scope);
        return new NutsDependencyScopeFilter(getSession(),s2);
    }

    public NutsDependencyScopeFilter addScopePatterns(Collection<NutsDependencyScopePattern> scope) {
        EnumSet<NutsDependencyScope> s2 = EnumSet.copyOf(this.scope);
        for (NutsDependencyScopePattern ss : scope) {
            if(ss!=null) {
                s2.addAll(ss.toScopes());
            }
        }
        return new NutsDependencyScopeFilter(getSession(),s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        return scope.isEmpty() || scope.contains(NutsDependencyScope.parseLenient(dependency.getScope(),NutsDependencyScope.API,NutsDependencyScope.API));
    }

    @Override
    public String toString() {
        return scope.isEmpty()?"true": "scope in (" + scope.stream().map(CoreEnumUtils::getEnumString).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsDependencyFilter simplify() {
        return scope.isEmpty()?getSession().filters().dependency().always() : this;
    }
}
