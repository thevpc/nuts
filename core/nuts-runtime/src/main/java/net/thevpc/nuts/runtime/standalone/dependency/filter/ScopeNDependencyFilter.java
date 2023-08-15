package net.thevpc.nuts.runtime.standalone.dependency.filter;

import java.util.EnumSet;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Objects;

public class ScopeNDependencyFilter extends AbstractDependencyFilter{

    private EnumSet<NDependencyScope> scopes = EnumSet.noneOf(NDependencyScope.class);

    public ScopeNDependencyFilter(NSession session, NDependencyScopePattern... scopes) {
        super(session, NFilterOp.CUSTOM);
        for (NDependencyScopePattern scope : scopes) {
            if(scope!=null) {
                this.scopes.addAll(scope.toScopes());
            }
        }
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency, NSession session) {

        NDependencyScope d = NDependencyScope.parse(dependency.getScope()).orElse(NDependencyScope.API);
        return d != null && scopes.contains(d);
    }

    @Override
    public NDependencyFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "(" + scopes + ")";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.scopes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ScopeNDependencyFilter other = (ScopeNDependencyFilter) obj;
        if (!Objects.equals(this.scopes, other.scopes)) {
            return false;
        }
        return true;
    }

}
