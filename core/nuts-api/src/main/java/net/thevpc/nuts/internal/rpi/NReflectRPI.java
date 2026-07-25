package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NClassLoaderNode;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.ext.NServiceLoader;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

public interface NReflectRPI extends NComponent {
    static NReflectRPI of() {
        return get().get();
    }

    static NOptional<NReflectRPI> get() {
        return NExtensions.get(NReflectRPI.class);
    }

    NClassLoader createImmutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    NClassLoader createPreferredImmutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    NMutableClassLoader createMutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    NClassLoader createImmutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    NClassLoader createPreferredImmutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    NMutableClassLoader createMutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    NClasspathEntry createClasspathEntry(NId id);

    NClasspathEntry createClasspathEntry(NDependency dependency);

    NClasspathEntry createClasspathEntry(NDefinition definition);

    NClasspathEntry createClasspathEntry(NPath path);


    <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType);

    <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader);
}
