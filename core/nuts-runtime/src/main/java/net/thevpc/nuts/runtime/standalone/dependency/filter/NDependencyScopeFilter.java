package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.util.NFilterOp;

public class NDependencyScopeFilter extends AbstractDependencyFilter {

    private EnumSet<NDependencyScope> scope=EnumSet.noneOf(NDependencyScope.class);

    public NDependencyScopeFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyScopeFilter(Collection<NDependencyScope> scope) {
        super(NFilterOp.CUSTOM);
        this.scope = EnumSet.copyOf(scope);
    }

    public NDependencyScopeFilter add(Collection<NDependencyScope> scopes) {
        if(scopes==null){
            return this;
        }
        EnumSet<NDependencyScope> newScopes = EnumSet.copyOf(this.scope);
        NCoreCollectionUtils.addAllNonNull(newScopes, scopes);
        return new NDependencyScopeFilter(newScopes);
    }

    public NDependencyScopeFilter addScopePatterns(Collection<NDependencyScopePattern> scopes) {
        if(scopes==null){
            return this;
        }
        EnumSet<NDependencyScope> s2 = EnumSet.copyOf(this.scope);
        for (NDependencyScopePattern ss : scopes) {
            if(ss!=null) {
                s2.addAll(ss.toScopes());
            }
        }
        return new NDependencyScopeFilter(s2);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        return scope.isEmpty() || scope.contains(NDependencyScope.parse(dependency.getScope()).orElse(NDependencyScope.API));
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrEqOrIn("scope",
                        scope.stream().map(x-> x.id()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return scope.isEmpty()? NDependencyFilters.of().always() : this;
    }
}
