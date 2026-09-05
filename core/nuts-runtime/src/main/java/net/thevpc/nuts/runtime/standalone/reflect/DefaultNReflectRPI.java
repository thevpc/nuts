package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.app.NApplicationHandler;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.ext.NServiceLoader;
import net.thevpc.nuts.runtime.standalone.app.NReservedApplication;
import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.runtime.standalone.atrifact.DefaultNClasspathEntry;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNMutableClassLoader;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NAssert;

import java.util.Arrays;
import java.util.Objects;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNReflectRPI implements NReflectRPI {
    @Override
    public NReflectRepository getDefaultReflectRepository() {
        return NWorkspaceExt.of().getModel().getDefaultReflectRepository();
    }

    public NClassLoader createImmutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return ((DefaultNExtensions) NExtensions.of()).getModel().createImmutableClassLoader(name,parent,
                nodes==null?null:Arrays.stream(nodes).map(x->x==null?null:new DefaultNClasspathEntry(x)).filter(Objects::nonNull).toArray(NClasspathEntry[]::new)
                ,false,repositoryFilter, dependencyFilter);
    }

    @Override
    public NClassLoader createPreferredImmutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return ((DefaultNExtensions) NExtensions.of()).getModel().createImmutableClassLoader(name,parent,
                nodes==null?null:Arrays.stream(nodes).map(x->x==null?null:new DefaultNClasspathEntry(x)).filter(Objects::nonNull).toArray(NClasspathEntry[]::new)
                ,true,repositoryFilter, dependencyFilter);
    }

    @Override
    public NMutableClassLoader createMutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return new DefaultNMutableClassLoader(name,
                parent,
                nodes==null?null:Arrays.stream(nodes).map(x->x==null?null:new DefaultNClasspathEntry(x)).filter(Objects::nonNull).toArray(NClasspathEntry[]::new)
                ,repositoryFilter, dependencyFilter
                );
    }
    public NClassLoader createImmutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return ((DefaultNExtensions) NExtensions.of()).getModel().createImmutableClassLoader(name,parent,nodes,false,repositoryFilter, dependencyFilter);
    }

    @Override
    public NClassLoader createPreferredImmutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return ((DefaultNExtensions) NExtensions.of()).getModel().createImmutableClassLoader(name,parent,nodes,true,repositoryFilter, dependencyFilter);
    }

    @Override
    public NMutableClassLoader createMutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return new DefaultNMutableClassLoader(name,parent,nodes,repositoryFilter, dependencyFilter);
    }

    @Override
    public <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType) {
        return NWorkspaceExt.of().getModel().extensionModel.createServiceLoader(serviceType, criteriaType);
    }

    @Override
    public <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        return NWorkspaceExt.of().getModel().extensionModel.createServiceLoader(serviceType, criteriaType, classLoader);
    }

    @Override
    public NClasspathEntry createClasspathEntry(NId id) {
        NAssert.requireNamedNonBlank(id,"id");
        return new DefaultNClasspathEntry(id);
    }

    @Override
    public NClasspathEntry createClasspathEntry(NDependency dependency) {
        NAssert.requireNamedNonBlank(dependency,"dependency");
        return new DefaultNClasspathEntry(dependency);
    }

    @Override
    public NClasspathEntry createClasspathEntry(NDefinition definition) {
        NAssert.requireNamedNonBlank(definition,"definition");
        return new DefaultNClasspathEntry(definition);
    }

    @Override
    public NClasspathEntry createClasspathEntry(NPath path) {
        NAssert.requireNamedNonBlank(path,"definition");
        return new DefaultNClasspathEntry(path);
    }

    @Override
    public boolean isAnnotatedApplicationClass(Class appClass) {
        return NReservedApplication.isAnnotatedApplicationClass(appClass);
    }

    @Override
    public NApplicationHandler createApplicationInstanceFromAnnotatedInstance(Object appInstance) {
        return NReservedApplication.createApplicationInstanceFromAnnotatedInstance(appInstance);
    }
}
