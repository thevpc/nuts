package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NClasspathEntry;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.core.NClassLoaderNode;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;

public class DefaultImmutableNClassLoader extends NClassLoaderBase implements NClassLoader {
    protected DefaultImmutableNClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        super(name, parent, nodes, repositoryFilter, dependencyFilter);
    }

    @Override
    public NClassLoader immutable() {
        return this;
    }

    @Override
    public NMutableClassLoader mutable() {
        return new DefaultNMutableClassLoader(getName(), getParent(), baseEntries().toArray(new NClasspathEntry[0]), repositoryFilter, dependencyFilter);
    }
}
