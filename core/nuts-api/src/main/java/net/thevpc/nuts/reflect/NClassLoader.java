package net.thevpc.nuts.reflect;

import net.thevpc.nuts.artifact.NClasspathEntry;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.util.NOptional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Nuts ClassLoader contract Interface.
 * Instances of this class must extend Java's Classloader.
 * <code>asClassLoader</code> is the way this instance is cast to ClassLoader
 */
public interface NClassLoader {


    /**
     * search for existing immutable classloader with current name, parent, and entries (ignoring the name),
     * when found return it, if not create a new one and cache it
     *
     * @param name   name
     * @param parent parent or null
     * @param entries  entries
     * @return immutable classloader
     */
    static NClassLoader of(String name, ClassLoader parent, NDefinition... entries) {
        return NReflectRPI.of().createImmutableClassLoader(name, parent, entries,null,null);
    }

    /**
     * search for existing immutable classloader with current parent and entries (ignoring the name),
     * when found return it (even with other name)
     *
     * @param preferredName preferred name
     * @param parent        parent or null
     * @param entries         entries
     * @return immutable classloader
     */
    static NClassLoader ofPreferred(String preferredName, ClassLoader parent, NDefinition... entries) {
        return NReflectRPI.of().createPreferredImmutableClassLoader(preferredName, parent, entries,null,null);
    }

    /**
     * search for existing immutable classloader with current name, parent, and entries (ignoring the name),
     * when found return it, if not create a new one and cache it
     *
     * @param name   name
     * @param parent parent or null
     * @param entries  entries
     * @return immutable classloader
     */
    static NClassLoader of(String name, ClassLoader parent, NClasspathEntry... entries) {
        return NReflectRPI.of().createImmutableClassLoader(name, parent, entries,null,null);
    }

    /**
     * search for existing immutable classloader with current parent and entries (ignoring the name),
     * when found return it (even with other name)
     *
     * @param preferredName preferred name
     * @param parent        parent or null
     * @param entries         entries
     * @return immutable classloader
     */
    static NClassLoader ofPreferred(String preferredName, ClassLoader parent, NClasspathEntry... entries) {
        return NReflectRPI.of().createPreferredImmutableClassLoader(preferredName, parent, entries,null,null);
    }

    /**
     * search for existing immutable classloader with current name, parent, and entries (ignoring the name),
     * when found return it, if not create a new one and cache it
     *
     * @param name   name
     * @param parent parent or null
     * @param entries  entries
     * @param repositoryFilter  repositoryFilter
     * @param dependencyFilter  dependencyFilter, defaults to runnable non-optional
     * @return immutable classloader
     */
    static NClassLoader of(String name, ClassLoader parent, NDefinition[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return NReflectRPI.of().createImmutableClassLoader(name, parent, entries,repositoryFilter, dependencyFilter);
    }

    /**
     * search for existing immutable classloader with current parent and entries (ignoring the name),
     * when found return it (even with other name)
     *
     * @param preferredName preferred name
     * @param parent        parent or null
     * @param entries         entries
     * @param repositoryFilter  repositoryFilter
     * @param dependencyFilter  dependencyFilter, defaults to runnable non-optional
     * @return immutable classloader
     */
    static NClassLoader ofPreferred(String preferredName, ClassLoader parent, NDefinition[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return NReflectRPI.of().createPreferredImmutableClassLoader(preferredName, parent, entries,repositoryFilter, dependencyFilter);
    }

    /**
     * search for existing immutable classloader with current name, parent, and entries (ignoring the name),
     * when found return it, if not create a new one and cache it
     *
     * @param name   name
     * @param parent parent or null
     * @param entries  entries
     * @param repositoryFilter  repositoryFilter
     * @param dependencyFilter  dependencyFilter, defaults to runnable non-optional
     * @return immutable classloader
     */
    static NClassLoader of(String name, ClassLoader parent, NClasspathEntry[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return NReflectRPI.of().createImmutableClassLoader(name, parent, entries,repositoryFilter, dependencyFilter);
    }

    /**
     * search for existing immutable classloader with current parent and entries (ignoring the name),
     * when found return it (even with other name)
     *
     * @param preferredName preferred name
     * @param parent        parent or null
     * @param entries         entries
     * @param repositoryFilter  repositoryFilter
     * @param dependencyFilter  dependencyFilter, defaults to runnable non-optional
     * @return immutable classloader
     */
    static NClassLoader ofPreferred(String preferredName, ClassLoader parent, NClasspathEntry[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return NReflectRPI.of().createPreferredImmutableClassLoader(preferredName, parent, entries,repositoryFilter, dependencyFilter);
    }

    /**
     * As class loader.
     *
     * @return as class loader result
     */
    default ClassLoader asClassLoader() {
        return (ClassLoader) this;
    }

    /**
     * Contains.
     *
     * @param node node
     * @return contains result
     */
    boolean contains(NId node);

    /**
     * Finds the search.
     *
     * @param node node
     * @return search result
     */
    NOptional<NId> search(NId node);

    /**
     * Load class.
     *
     * @param name name
     * @return load class result
     * @throws ClassNotFoundException if execution fails
     */
    Class<?> loadClass(String name) throws ClassNotFoundException;

    /**
     * Returns the resource.
     *
     * @param name name
     * @return get resource result
     */
    URL getResource(String name);

    /**
     * Returns the resources.
     *
     * @param name name
     * @return get resources result
     * @throws IOException if execution fails
     */
    Enumeration<URL> getResources(String name) throws IOException;

    /**
     * Returns the resource as stream.
     *
     * @param name name
     * @return get resource as stream result
     */
    InputStream getResourceAsStream(String name);

    /**
     * Returns the parent.
     *
     * @return get parent result
     */
    ClassLoader getParent();


    /**
     * classloader name.
     * @return name
     */
    String name();

    /**
     * classloader name.
     * defined to match java's ClassLoader getName signature.
     * @return name
     */
    String getName();

    /**
     * return immutable version of this classloader or self if already immutable
     *
     * @return immutable version of this classloader or self if already immutable
     */
    NClassLoader immutable();

    /**
     * return mutable version of this classloader or self if already immutable
     *
     * @return mutable version of this classloader or self if already immutable
     */
    NMutableClassLoader mutable();
}
