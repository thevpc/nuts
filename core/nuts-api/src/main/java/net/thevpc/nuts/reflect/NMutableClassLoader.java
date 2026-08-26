package net.thevpc.nuts.reflect;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.util.NCopiable;

import java.util.List;

/**
 * NMutableClassLoader interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NMutableClassLoader extends NClassLoader, NCopiable {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NMutableClassLoader of() {
        return NReflectRPI.of().createMutableClassLoader(null, null, new NDefinition[0], null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param entries entries
     * @return of result
     */
    static NMutableClassLoader of(NDefinition... entries) {
        return NReflectRPI.of().createMutableClassLoader(null, null, entries, null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param parent parent
     * @param entries entries
     * @return of result
     */
    static NMutableClassLoader of(String name, ClassLoader parent, NDefinition... entries) {
        return NReflectRPI.of().createMutableClassLoader(name, parent, entries, null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param parent parent
     * @param entries entries
     * @param repositoryFilter repository filter
     * @param dependencyFilter dependency filter
     * @return of result
     */
    static NMutableClassLoader of(String name, ClassLoader parent, NDefinition[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return NReflectRPI.of().createMutableClassLoader(name, parent, entries, repositoryFilter, dependencyFilter);
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param parent parent
     * @param entries entries
     * @param repositoryFilter repository filter
     * @param dependencyFilter dependency filter
     * @return of result
     */
    static NMutableClassLoader of(String name, ClassLoader parent, NClasspathEntry[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return NReflectRPI.of().createMutableClassLoader(name, parent, entries, repositoryFilter, dependencyFilter);
    }

    /**
     * Loaded definitions.
     *
     * @return loaded definitions result
     */
    List<NDefinition> loadedDefinitions();

    /**
     * Adds add.
     *
     * @param entries entries
     * @return add result
     */
    NDefinition[] add(NClasspathEntry... entries);

    /**
     * Adds add.
     *
     * @param dependencies dependencies
     * @return add result
     */
    NDefinition[] add(NDependency... dependencies);

    /**
     * Adds add.
     *
     * @param ids ids
     * @return add result
     */
    NDefinition[] add(NId... ids);

    /**
     * Adds add.
     *
     * @param definitions definitions
     * @return add result
     */
    NDefinition[] add(NDefinition... definitions);

    /**
     * Checks if is loaded.
     *
     * @param id id
     * @return is loaded result
     */
    boolean isLoaded(NId id);

    /**
     * Copy.
     *
     * @return copy result
     */
    NMutableClassLoader copy();
}
