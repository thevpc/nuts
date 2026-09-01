/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NClasspathEntry;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple Implementation of Nuts Mutable ClassLoader for nuts extensions.
 * Internally delegates through {@link NClassLoaderBase} which now behaves as
 * a mutable composite of VM-shared leaves (obtained via
 * {@link NIdClassLoaderRegistry}).
 *
 * @author thevpc
 * @app.category Boot
 */
public class DefaultNMutableClassLoader extends NClassLoaderBase implements NMutableClassLoader {

    public DefaultNMutableClassLoader(String name, ClassLoader parent, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        this(name, parent, new NClasspathEntry[0], repositoryFilter, dependencyFilter);
    }

    public DefaultNMutableClassLoader(String name, ClassLoader parent, NClasspathEntry[] nodes, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        super(name, parent, nodes, repositoryFilter, dependencyFilter);
    }

    @Override
    public NClassLoader immutable() {
        // Create a snapshot composite that shares the same child leaves with us.
        // The composite keeps the same parent for workspace-scoped extensions visibility.
        List<NClassLoader> snapshot = new ArrayList<>(children);
        return new DefaultNCompositeClassLoader(getName(), getParent(), snapshot);
    }

    @Override
    public NMutableClassLoader mutable() {
        return this;
    }

    @Override
    public NMutableClassLoader copy() {
        NClasspathEntry[] entries = baseEntries.toArray(new NClasspathEntry[0]);
        return new DefaultNMutableClassLoader(getName(), getParent(), entries, repositoryFilter, dependencyFilter);
    }
}
