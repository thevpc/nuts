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
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Leaf (single-source) NutsClassLoader backed by exactly one jar URL.
 * <p>
 * Instances for the same NId (GAV) are unique within the VM via
 * {@link NIdClassLoaderRegistry}. Leaves share the system classloader as
 * their parent so they are workspace-agnostic and may be reused across
 * multiple workspaces running in the same VM.
 *
 * @app.category Internal
 */
class DefaultNLeafClassLoader extends URLClassLoader implements NClassLoader {

    private final String name;
    private final NId id;
    private final NPath path;

    DefaultNLeafClassLoader(NId id, NPath path, ClassLoader parent) {
        super(toURLArray(path), parent == null ? ClassLoader.getSystemClassLoader() : parent);
        this.id = id;
        this.path = path;
        this.name = buildName(id, path);
    }

    private static String buildName(NId id, NPath path) {
        if (id != null) {
            return "nut-leaf:" + id.longName();
        }
        if (path != null) {
            try {
                return "nut-leaf-path:" + path.toAbsolute();
            } catch (Exception ex) {
                return "nut-leaf-path:" + path;
            }
        }
        return "nut-leaf:anonymous";
    }

    private static URL[] toURLArray(NPath path) {
        if (path == null) {
            return new URL[0];
        }
        URL u = path.toURL().orNull();
        return u == null ? new URL[0] : new URL[]{u};
    }

    public NId id() {
        return id;
    }

    public NPath path() {
        return path;
    }

    @Override
    public boolean contains(NId node) {
        return search(node).isPresent();
    }

    @Override
    public NOptional<NId> search(NId node) {
        if (node == null) return NOptional.ofNamedEmpty("null");
        if (id != null && id.equalsShortId(node)) {
            return NOptional.of(id);
        }
        return NClassLoaderBase.search(node, this, false);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            // Linking can happen later, after the original composite call has
            // returned (for example while invoking an application method).
            // In that case use the application TCCL as the durable bridge.
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            if (tccl != null && tccl != this && tccl != getParent()) {
                try {
                    return tccl.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                    // Try the active composite below.
                }
            }
            // The leaf is intentionally parented by the system loader and is
            // shared VM-wide. Resolve application dependencies through the
            // workspace composite that is currently using this leaf.
            NClassLoaderPeer peer = NClassLoaderContext.current();
            if (peer != null) {
                try {
                    return peer.loadClassFromChildren(this, name);
                } catch (ClassNotFoundException ignored) {
                    // Preserve the original failure and its useful class name.
                }
            }
            try {
                return NIdClassLoaderRegistry.findInRegisteredLeaves(this, name);
            } catch (ClassNotFoundException ignored) {
                // Preserve the original failure and its useful class name.
            }
            throw e;
        }
    }

    Class<?> findOwnClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NClassLoader immutable() {
        return this;
    }

    @Override
    public NMutableClassLoader mutable() {
        NMutableClassLoader m = NMutableClassLoader.of(name, getParent(), new NDefinition[0], null, null);
        if (id != null) {
            NClasspathEntry ce = NClasspathEntry.of(id);
            m.add(ce);
        } else if (path != null) {
            NClasspathEntry ce = NClasspathEntry.of(path);
            m.add(ce);
        }
        return m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultNLeafClassLoader)) return false;
        DefaultNLeafClassLoader that = (DefaultNLeafClassLoader) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id)
                && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, path);
    }

    @Override
    public String toString() {
        return name;
    }
}
