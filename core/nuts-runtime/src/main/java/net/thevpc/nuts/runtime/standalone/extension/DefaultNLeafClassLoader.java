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
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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

    static final List<NClassLoaderResolution.NResolutionTier<Class<?>>> CLASS_TIERS =
            Collections.unmodifiableList(createClassTiers());
    static final List<NClassLoaderResolution.NResolutionTier<URL>> SINGLE_RESOURCE_TIERS =
            Collections.unmodifiableList(createSingleResourceTiers());
    static final List<NClassLoaderResolution.NResolutionTier<List<URL>>> MULTI_RESOURCE_TIERS =
            Collections.unmodifiableList(createMultiResourceTiers());

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

    private static List<NClassLoaderResolution.NResolutionTier<Class<?>>> createClassTiers() {
        return Arrays.asList(
                new NClassLoaderResolution.NResolutionTier<Class<?>>() {
                    @Override
                    public String name() {
                        return "own";
                    }

                    @Override
                    public Class<?> tryResolve(DefaultNLeafClassLoader requester, String key) {
                        try {
                            return requester.loadClassFromParentAndOwn(key);
                        } catch (ClassNotFoundException ignored) {
                            return null;
                        }
                    }
                },
                new NClassLoaderResolution.NResolutionTier<Class<?>>() {
                    @Override
                    public String name() {
                        return "tccl";
                    }

                    @Override
                    public Class<?> tryResolve(DefaultNLeafClassLoader requester, String key) {
                        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                        if (!NClassLoaderContext.isSiblingLookup()
                                && tccl != null && tccl != requester && tccl != requester.getParent()
                                && !(tccl instanceof NClassLoaderPeer)) {
                            try {
                                return tccl.loadClass(key);
                            } catch (ClassNotFoundException ignored) {
                                return null;
                            }
                        }
                        return null;
                    }
                },
                new NClassLoaderResolution.NResolutionTier<Class<?>>() {
                    @Override
                    public String name() {
                        return "peer";
                    }

                    @Override
                    public Class<?> tryResolve(DefaultNLeafClassLoader requester, String key) {
                        NClassLoaderPeer peer = NClassLoaderContext.current();
                        if (peer != null && !NClassLoaderContext.isSiblingLookup()) {
                            try {
                                return peer.loadClassFromChildren(requester, key);
                            } catch (ClassNotFoundException ignored) {
                                return null;
                            }
                        }
                        return null;
                    }
                },
                new NClassLoaderResolution.NResolutionTier<Class<?>>() {
                    @Override
                    public String name() {
                        return "registry";
                    }

                    @Override
                    public Class<?> tryResolve(DefaultNLeafClassLoader requester, String key) {
                        if (NClassLoaderContext.current() == null) {
                            NLog.of(DefaultNLeafClassLoader.class).debug(
                                    NMsg.ofC("Composite-scoped resolution was unavailable (no active NClassLoaderContext); falling back to VM-wide registry for class %s", key));
                        }
                        try {
                            return NIdClassLoaderRegistry.findInRegisteredLeaves(requester, key);
                        } catch (ClassNotFoundException ignored) {
                            return null;
                        }
                    }
                }
        );
    }

    private static List<NClassLoaderResolution.NResolutionTier<URL>> createSingleResourceTiers() {
        return Arrays.asList(
                new NClassLoaderResolution.NResolutionTier<URL>() {
                    @Override
                    public String name() {
                        return "own";
                    }

                    @Override
                    public URL tryResolve(DefaultNLeafClassLoader requester, String key) {
                        return requester.findOwnResource(key);
                    }
                },
                new NClassLoaderResolution.NResolutionTier<URL>() {
                    @Override
                    public String name() {
                        return "tccl";
                    }

                    @Override
                    public URL tryResolve(DefaultNLeafClassLoader requester, String key) {
                        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                        if (!NClassLoaderContext.isSiblingLookup()
                                && tccl != null && tccl != requester && tccl != requester.getParent()
                                && !(tccl instanceof NClassLoaderPeer)) {
                            return tccl.getResource(key);
                        }
                        return null;
                    }
                },
                new NClassLoaderResolution.NResolutionTier<URL>() {
                    @Override
                    public String name() {
                        return "peer";
                    }

                    @Override
                    public URL tryResolve(DefaultNLeafClassLoader requester, String key) {
                        NClassLoaderPeer peer = NClassLoaderContext.current();
                        if (peer != null && !NClassLoaderContext.isSiblingLookup()) {
                            try {
                                List<URL> urls = peer.loadResourcesFromChildren(requester, key);
                                if (urls != null && !urls.isEmpty()) {
                                    return urls.get(0);
                                }
                            } catch (IOException ignored) {
                                return null;
                            }
                        }
                        return null;
                    }
                },
                new NClassLoaderResolution.NResolutionTier<URL>() {
                    @Override
                    public String name() {
                        return "registry";
                    }

                    @Override
                    public URL tryResolve(DefaultNLeafClassLoader requester, String key) {
                        if (NClassLoaderContext.current() == null) {
                            NLog.of(DefaultNLeafClassLoader.class).debug(
                                    NMsg.ofC("Composite-scoped resolution was unavailable (no active NClassLoaderContext); falling back to VM-wide registry for resource %s", key));
                        }
                        List<URL> urls = NIdClassLoaderRegistry.findResourcesInRegisteredLeaves(requester, key);
                        if (urls != null && !urls.isEmpty()) {
                            return urls.get(0);
                        }
                        return null;
                    }
                }
        );
    }

    private static List<NClassLoaderResolution.NResolutionTier<List<URL>>> createMultiResourceTiers() {
        return Arrays.asList(
                new NClassLoaderResolution.NResolutionTier<List<URL>>() {
                    @Override
                    public String name() {
                        return "own";
                    }

                    @Override
                    public List<URL> tryResolve(DefaultNLeafClassLoader requester, String key) {
                        try {
                            Enumeration<URL> e = requester.findOwnResources(key);
                            List<URL> list = new ArrayList<>();
                            if (e != null) {
                                while (e.hasMoreElements()) {
                                    URL u = e.nextElement();
                                    if (u != null) {
                                        list.add(u);
                                    }
                                }
                            }
                            return list;
                        } catch (IOException ignored) {
                            return Collections.emptyList();
                        }
                    }
                },
                new NClassLoaderResolution.NResolutionTier<List<URL>>() {
                    @Override
                    public String name() {
                        return "tccl";
                    }

                    @Override
                    public List<URL> tryResolve(DefaultNLeafClassLoader requester, String key) {
                        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                        if (!NClassLoaderContext.isSiblingLookup()
                                && tccl != null && tccl != requester && tccl != requester.getParent()
                                && !(tccl instanceof NClassLoaderPeer)) {
                            try {
                                Enumeration<URL> e = tccl.getResources(key);
                                List<URL> list = new ArrayList<>();
                                if (e != null) {
                                    while (e.hasMoreElements()) {
                                        URL u = e.nextElement();
                                        if (u != null) {
                                            list.add(u);
                                        }
                                    }
                                }
                                return list;
                            } catch (IOException ignored) {
                                return Collections.emptyList();
                            }
                        }
                        return Collections.emptyList();
                    }
                },
                new NClassLoaderResolution.NResolutionTier<List<URL>>() {
                    @Override
                    public String name() {
                        return "peer";
                    }

                    @Override
                    public List<URL> tryResolve(DefaultNLeafClassLoader requester, String key) {
                        NClassLoaderPeer peer = NClassLoaderContext.current();
                        if (peer != null && !NClassLoaderContext.isSiblingLookup()) {
                            try {
                                return peer.loadResourcesFromChildren(requester, key);
                            } catch (IOException ignored) {
                                return Collections.emptyList();
                            }
                        }
                        return Collections.emptyList();
                    }
                },
                new NClassLoaderResolution.NResolutionTier<List<URL>>() {
                    @Override
                    public String name() {
                        return "registry";
                    }

                    @Override
                    public List<URL> tryResolve(DefaultNLeafClassLoader requester, String key) {
                        if (NClassLoaderContext.current() == null) {
                            NLog.of(DefaultNLeafClassLoader.class).debug(
                                    NMsg.ofC("Composite-scoped resolution was unavailable (no active NClassLoaderContext); falling back to VM-wide registry for resources %s", key));
                        }
                        return NIdClassLoaderRegistry.findResourcesInRegisteredLeaves(requester, key);
                    }
                }
        );
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            Class<?> c = NClassLoaderResolution.NResolutionChain.resolveFirst(this, name, CLASS_TIERS);
            if (c != null) {
                return c;
            }
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }
        throw new ClassNotFoundException(name);
    }

    Class<?> findOwnClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    /** Parent-first lookup used by the registry fallback, without sibling delegation. */
    Class<?> loadClassFromParentAndOwn(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    URL findOwnResource(String name) {
        return super.getResource(name);
    }

    Enumeration<URL> findOwnResources(String name) throws IOException {
        return super.getResources(name);
    }

    @Override
    public URL getResource(String name) {
        try {
            return NClassLoaderResolution.NResolutionChain.resolveFirst(this, name, SINGLE_RESOURCE_TIERS);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        try {
            List<URL> all = NClassLoaderResolution.NResolutionChain.resolveAll(this, name, MULTI_RESOURCE_TIERS);
            return Collections.enumeration(all);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL u = getResource(name);
        if (u == null) {
            return null;
        }
        try {
            return u.openStream();
        } catch (IOException e) {
            return null;
        }
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
