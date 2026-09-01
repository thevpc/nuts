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
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;

import java.net.URL;

/**
 * Immutable implementation of {@link NClassLoader} backed by the shared
 * leaf registry. Fully resolves its classpath entries at construction time
 * via the base class {@link NClassLoaderBase#build()}, after which the
 * instance is frozen and all mutator operations become no-ops.
 * <p>
 * Note: the workspace extension model's
 * {@code createImmutableClassLoader(...)} now prefers to return a
 * {@link DefaultNCompositeClassLoader} directly (which doesn't carry the
 * mutable-classloader fields), but this class is kept for any callers that
 * construct it explicitly or rely on its type.
 *
 * @app.category Internal
 */
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
        NMutableClassLoader m = new DefaultNMutableClassLoader(getName(), getParent(), repositoryFilter, dependencyFilter);
        for (NClasspathEntry e : baseEntries) {
            m.add(e);
        }
        return m;
    }

    // ---- Freeze all mutators ----

    @Override
    public NDefinition[] add(NDefinition... defs) {
        return new NDefinition[0];
    }

    @Override
    public NDefinition[] add(NClasspathEntry... entries) {
        return new NDefinition[0];
    }

    @Override
    public NDefinition[] add(NId... ids) {
        return new NDefinition[0];
    }

    @Override
    public NDefinition[] add(net.thevpc.nuts.artifact.NDependency... dependencies) {
        return new NDefinition[0];
    }

    @Override
    public boolean add0(NClasspathEntry entry) {
        return false;
    }

    @Override
    public boolean add0(NDefinition definition) {
        return false;
    }

    @Override
    public boolean add0(NId id) {
        return false;
    }

    @Override
    public boolean add0(net.thevpc.nuts.artifact.NDependency dependency) {
        return false;
    }

    @Override
    public void addURL(URL url) {
        // no-op
    }

    @Override
    public synchronized NDefinition[] build() {
        return new NDefinition[0];
    }

    @Override
    public synchronized void remove(NId id) {
        // no-op
    }

    @Override
    public synchronized NDefinition[] rebuild(NDefinition[] desired) {
        return new NDefinition[0];
    }
}
