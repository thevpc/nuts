package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;

public class NDependencyScopeFilter extends AbstractDependencyFilter {

    private EnumSet<NDependencyScope> scope=EnumSet.noneOf(NDependencyScope.class);

    public NDependencyScopeFilter(NSession session) {
        super(session, NFilterOp.CUSTOM);
    }

    private NDependencyScopeFilter(NSession session, Collection<NDependencyScope> scope) {
        super(session, NFilterOp.CUSTOM);
        this.scope = EnumSet.copyOf(scope);
    }

    public NDependencyScopeFilter add(Collection<NDependencyScope> scope) {
        EnumSet<NDependencyScope> s2 = EnumSet.copyOf(this.scope);
        s2.addAll(scope);
        return new NDependencyScopeFilter(getSession(),s2);
    }

    public NDependencyScopeFilter addScopePatterns(Collection<NDependencyScopePattern> scope) {
        EnumSet<NDependencyScope> s2 = EnumSet.copyOf(this.scope);
        for (NDependencyScopePattern ss : scope) {
            if(ss!=null) {
                s2.addAll(ss.toScopes());
            }
        }
        return new NDependencyScopeFilter(getSession(),s2);
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency, NSession session) {
        return scope.isEmpty() || scope.contains(NDependencyScope.parse(dependency.getScope()).orElse(NDependencyScope.API));
    }

    @Override
    public String toString() {
        return scope.isEmpty()?"true": "scope in (" + scope.stream().map(CoreEnumUtils::getEnumString).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NDependencyFilter simplify() {
        return scope.isEmpty()? NDependencyFilters.of(getSession()).always() : this;
    }
}
