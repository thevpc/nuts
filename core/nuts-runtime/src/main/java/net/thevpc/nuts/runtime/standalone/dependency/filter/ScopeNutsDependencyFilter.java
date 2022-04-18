package net.thevpc.nuts.runtime.standalone.dependency.filter;

import java.util.EnumSet;

import net.thevpc.nuts.*;

import java.util.Objects;

public class ScopeNutsDependencyFilter extends AbstractDependencyFilter{

    private EnumSet<NutsDependencyScope> scopes = EnumSet.noneOf(NutsDependencyScope.class);

    public ScopeNutsDependencyFilter(NutsSession session, NutsDependencyScopePattern... scopes) {
        super(session, NutsFilterOp.CUSTOM);
        for (NutsDependencyScopePattern scope : scopes) {
            if(scope!=null) {
                this.scopes.addAll(scope.toScopes());
            }
        }
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {

        NutsDependencyScope d = NutsDependencyScope.parse(dependency.getScope()).orElse(NutsDependencyScope.API);
        return d != null && scopes.contains(d);
    }

    @Override
    public NutsDependencyFilter simplify() {
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
        final ScopeNutsDependencyFilter other = (ScopeNutsDependencyFilter) obj;
        if (!Objects.equals(this.scopes, other.scopes)) {
            return false;
        }
        return true;
    }

}
