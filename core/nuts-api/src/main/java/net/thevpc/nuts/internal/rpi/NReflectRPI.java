package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NClassLoaderNode;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.ext.NServiceLoader;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

/**
 * NReflectRPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NReflectRPI extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NReflectRPI of() {
        /**
         * Returns the get.
         *
         * @param ).get( ).get(
         * @return get result
         */
        return get().get();
    }

    /**
     * Returns the get.
     *
     * @return get result
     */
    static NOptional<NReflectRPI> get() {
        return NExtensions.get(NReflectRPI.class);
    }

    /**
     * Returns the default reflect repository.
     *
     * @return get default reflect repository result
     */
    NReflectRepository getDefaultReflectRepository();

    /**
     * Creates a new instance of create immutable class loader.
     *
     * @param name name
     * @param parent parent
     * @param nodes nodes
     * @param repositoryFilter repository filter
     * @param dependencyFilter dependency filter
     * @return create immutable class loader result
     */
    NClassLoader createImmutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    /**
     * Creates a new instance of create preferred immutable class loader.
     *
     * @param name name
     * @param parent parent
     * @param nodes nodes
     * @param repositoryFilter repository filter
     * @param dependencyFilter dependency filter
     * @return create preferred immutable class loader result
     */
    NClassLoader createPreferredImmutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    /**
     * Creates a new instance of create mutable class loader.
     *
     * @param name name
     * @param parent parent
     * @param nodes nodes
     * @param repositoryFilter repository filter
     * @param dependencyFilter dependency filter
     * @return create mutable class loader result
     */
    NMutableClassLoader createMutableClassLoader(String name, ClassLoader parent, NDefinition[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    /**
     * Creates a new instance of create immutable class loader.
     *
     * @param name name
     * @param parent parent
     * @param nodes nodes
     * @param repositoryFilter repository filter
     * @param dependencyFilter dependency filter
     * @return create immutable class loader result
     */
    NClassLoader createImmutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    /**
     * Creates a new instance of create preferred immutable class loader.
     *
     * @param name name
     * @param parent parent
     * @param nodes nodes
     * @param repositoryFilter repository filter
     * @param dependencyFilter dependency filter
     * @return create preferred immutable class loader result
     */
    NClassLoader createPreferredImmutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    /**
     * Creates a new instance of create mutable class loader.
     *
     * @param name name
     * @param parent parent
     * @param nodes nodes
     * @param repositoryFilter repository filter
     * @param dependencyFilter dependency filter
     * @return create mutable class loader result
     */
    NMutableClassLoader createMutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter);

    /**
     * Creates a new instance of create classpath entry.
     *
     * @param id id
     * @return create classpath entry result
     */
    NClasspathEntry createClasspathEntry(NId id);

    /**
     * Creates a new instance of create classpath entry.
     *
     * @param dependency dependency
     * @return create classpath entry result
     */
    NClasspathEntry createClasspathEntry(NDependency dependency);

    /**
     * Creates a new instance of create classpath entry.
     *
     * @param definition definition
     * @return create classpath entry result
     */
    NClasspathEntry createClasspathEntry(NDefinition definition);

    /**
     * Creates a new instance of create classpath entry.
     *
     * @param path path
     * @return create classpath entry result
     */
    NClasspathEntry createClasspathEntry(NPath path);


    /**
     * Creates a new instance of create service loader.
     *
     * @param serviceType service type
     * @param criteriaType criteria type
     * @return create service loader result
     */
    <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType);

    /**
     * Creates a new instance of create service loader.
     *
     * @param serviceType service type
     * @param criteriaType criteria type
     * @param classLoader class loader
     * @return create service loader result
     */
    <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader);
}
