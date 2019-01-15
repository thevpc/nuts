package net.vpc.app.nuts.core.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.core.util.Simplifiable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ScopeNutsDependencyFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private Set<String> scopes = new HashSet<>();

    public ScopeNutsDependencyFilter(String scope) {
        for (String s : scope.split("[ ,]")) {
            s = s.trim();
            if (!s.isEmpty()) {
                scopes.add(s.toLowerCase());
            }
        }
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency) {
        String scope = dependency.getScope();
        if (scope == null) {
            scope = "";
        }
        scope = scope.toLowerCase();
        if (scope.isEmpty()) {
            scope = "compile";
        }
        if (!(scopes.contains(scope))) {
            return false;
        }
        return true;
    }

    @Override
    public NutsDependencyFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "ScopeNutsDependencyFilter" + scopes;
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
