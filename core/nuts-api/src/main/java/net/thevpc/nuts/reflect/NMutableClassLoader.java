package net.thevpc.nuts.reflect;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.util.NCopiable;

import java.util.List;

public interface NMutableClassLoader extends NClassLoader, NCopiable {
    static NMutableClassLoader of() {
        return NReflectRPI.of().createMutableClassLoader(null, null, new NDefinition[0], null, null);
    }

    static NMutableClassLoader of(NDefinition... entries) {
        return NReflectRPI.of().createMutableClassLoader(null, null, entries, null, null);
    }

    static NMutableClassLoader of(String name, ClassLoader parent, NDefinition... entries) {
        return NReflectRPI.of().createMutableClassLoader(name, parent, entries, null, null);
    }

    static NMutableClassLoader of(String name, ClassLoader parent, NDefinition[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return NReflectRPI.of().createMutableClassLoader(name, parent, entries, repositoryFilter, dependencyFilter);
    }

    static NMutableClassLoader of(String name, ClassLoader parent, NClasspathEntry[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        return NReflectRPI.of().createMutableClassLoader(name, parent, entries, repositoryFilter, dependencyFilter);
    }

    List<NDefinition> loadedDefinitions();

    NDefinition[] add(NClasspathEntry... entries);

    NDefinition[] add(NDependency... dependencies);

    NDefinition[] add(NId... ids);

    NDefinition[] add(NDefinition... definitions);

    boolean isLoaded(NId id);

    NMutableClassLoader copy();
}
