package net.vpc.app.nuts;

import java.util.Collection;
import java.util.Set;

public interface NutsClassLoaderBuilder {
    NutsClassLoaderBuilder addId(String id);

    NutsClassLoaderBuilder addIds(String... id);

    NutsClassLoaderBuilder addIds(NutsId... ids);

    NutsClassLoaderBuilder addScope(NutsDependencyScope... scope);

    NutsClassLoaderBuilder setScope(Set<NutsDependencyScope> scope);

    NutsClassLoaderBuilder addScope(Collection<NutsDependencyScope> scope);

    NutsClassLoaderBuilder addScope(NutsDependencyScope scope);

    NutsClassLoaderBuilder removeScope(Collection<NutsDependencyScope> scope);

    NutsClassLoaderBuilder removeScope(NutsDependencyScope scope);

    NutsClassLoaderBuilder setSession(NutsSession session);

    NutsClassLoaderBuilder setParentClassLoader(ClassLoader parentClassLoader);

    ClassLoader build();
}
