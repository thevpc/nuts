package net.thevpc.nuts.runtime.standalone.dependency.filter;

import java.util.EnumSet;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.internal.rpi.NDependencyFilterRPI;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Objects;

public class ScopeNDependencyFilter extends AbstractDependencyFilter{

    private EnumSet<NDependencyScope> scopes = EnumSet.noneOf(NDependencyScope.class);

    public ScopeNDependencyFilter(NDependencyScopePattern... scopes) {
        super(NFilterOp.CUSTOM);
        for (NDependencyScopePattern scope : scopes) {
            if(scope!=null) {
                this.scopes.addAll(scope.toScopes());
            }
        }
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {

        NDependencyScope d = NDependencyScope.parse(dependency.scope()).orElse(NDependencyScope.API);
        return d != null && scopes.contains(d);
    }

    @Override
    public NDependencyFilter simplify() {
        if(scopes.isEmpty()) {
            return NDependencyFilterRPI.of().always();
        }
        return this;
    }

    @Override
    public String toString() {
        return "(" + scopes + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ScopeNDependencyFilter that = (ScopeNDependencyFilter) o;
        return Objects.equals(scopes, that.scopes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), scopes);
    }
}
