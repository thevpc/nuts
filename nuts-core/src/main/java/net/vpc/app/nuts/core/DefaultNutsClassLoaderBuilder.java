package net.vpc.app.nuts.core;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import net.vpc.app.nuts.*;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DefaultNutsClassLoaderBuilder implements NutsClassLoaderBuilder {

    private NutsFindCommand query;
    private NutsWorkspace ws;
    private ClassLoader parentClassLoader;

    public DefaultNutsClassLoaderBuilder(NutsWorkspace workspace) {
        this.ws = workspace;
        query = workspace.find();
    }

    @Override
    public NutsClassLoaderBuilder addId(String id) {
        query.addId(id);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder addIds(String... ids) {
        query.addIds(ids);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder addIds(NutsId... ids) {
        query.addIds(ids);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder addScope(NutsDependencyScope... scope) {
        query.addScope(scope);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder setScope(Set<NutsDependencyScope> scope) {
        query.setScope(scope);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder addScope(Collection<NutsDependencyScope> scope) {
        query.addScope(scope);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder addScope(NutsDependencyScope scope) {
        query.addScope(scope);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder removeScope(Collection<NutsDependencyScope> scope) {
        query.removeScope(scope);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder removeScope(NutsDependencyScope scope) {
        query.removeScope(scope);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder setSession(NutsSession session) {
        query.setSession(session);
        return this;
    }

    @Override
    public NutsClassLoaderBuilder setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return this;
    }

    @Override
    public ClassLoader build() {
        List<NutsDefinition> nutsDefinitions = query.mainAndDependencies().getResultDefinitions().list();
        URL[] all = new URL[nutsDefinitions.size()];
        for (int i = 0; i < all.length; i++) {
            try {
                all[i] = nutsDefinitions.get(i).getPath().toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return new NutsURLClassLoader(ws,all, parentClassLoader);
    }
}
