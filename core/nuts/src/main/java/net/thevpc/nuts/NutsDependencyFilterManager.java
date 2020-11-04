package net.thevpc.nuts;

import java.util.Collection;

public interface NutsDependencyFilterManager extends NutsTypedFilters<NutsDependencyFilter>{
    NutsDependencyFilter byScope(NutsDependencyScopePattern scope);
    NutsDependencyFilter byScope(NutsDependencyScope scope);

    NutsDependencyFilter byScope(NutsDependencyScope ... scope);

    NutsDependencyFilter byScope(Collection<NutsDependencyScope> scope);

    NutsDependencyFilter byOptional(Boolean optional);

    NutsDependencyFilter byExpression(String expression);

    NutsDependencyFilter byExclude(NutsDependencyFilter filter, String[] exclusions);
}
