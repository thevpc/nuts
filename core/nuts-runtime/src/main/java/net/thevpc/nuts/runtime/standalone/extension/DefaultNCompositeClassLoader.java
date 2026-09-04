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
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.reflect.NMutableClassLoader;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Immutable composite classloader that delegates to an ordered list of
 * children. Children are tried in iteration order for class loading and
 * single-resource lookup; multiple-resource lookup aggregates results from
 * the parent and every child.
 * <p>
 * The composite itself never defines classes: all {@code defineClass}
 * calls happen inside children (typically {@link DefaultNLeafClassLoader}s
 * which are shared VM-wide).
 *
 * @app.category Internal
 */
class DefaultNCompositeClassLoader extends ClassLoader implements NClassLoader, NClassLoaderPeer {

    private final String name;
    private final List<NClassLoader> children;

    private final java.util.concurrent.ConcurrentHashMap<String, Class<?>> classCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final Set<String> negativeCache = java.util.concurrent.ConcurrentHashMap.newKeySet();

    DefaultNCompositeClassLoader(String name, ClassLoader parent, List<NClassLoader> children) {
        super(parent == null ? ClassLoader.getSystemClassLoader() : parent);
        this.name = NStringUtils.firstNonBlank(name, "n-composite-cl");
        List<NClassLoader> copy = new ArrayList<>();
        if (children != null) {
            for (NClassLoader c : children) {
                if (c != null && c != this) {
                    copy.add(c);
                }
            }
        }
        this.children = Collections.unmodifiableList(copy);
    }

    public List<NClassLoader> children() {
        return children;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 1. Fast cache check
        Class<?> c = classCache.get(name);
        if (c != null) {
            if (resolve) resolveClass(c);
            return c;
        }
        if (negativeCache.contains(name)) {
            throw new ClassNotFoundException(name);
        }

        // 2. Already defined by this loader? (unlikely because composite never defineClass; but be safe)
        c = findLoadedClass(name);
        if (c != null) {
            classCache.put(name, c);
            if (resolve) resolveClass(c);
            return c;
        }

        // 3. Parent first (standard classloader delegation).
        //    The parent is workspace-scoped (workspaceExtensionsClassLoader),
        //    which lets extension classes be visible before delegating to leaves.
        try {
            ClassLoader p = getParent();
            if (p != null) {
                c = p.loadClass(name);
                if (c != null) {
                    classCache.put(name, c);
                    if (resolve) resolveClass(c);
                    return c;
                }
            }
        } catch (ClassNotFoundException ignore) {
            // not in parent; continue to children
        }

        // 4. Children in order — first successful child wins.
        //    The Class object's defining loader is the child that found it.
        for (NClassLoader child : children) {
            try {
                NClassLoaderContext.enter(this);
                c = child.loadClass(name);
                if (c != null) {
                    classCache.put(name, c);
                    if (resolve) resolveClass(c);
                    return c;
                }
            } catch (ClassNotFoundException ignore) {
                // continue to next child
            } finally {
                NClassLoaderContext.exit(this);
            }
        }

        negativeCache.add(name);
        throw new ClassNotFoundException(name);
    }

    @Override
    public Class<?> loadClassFromChildren(ClassLoader requester, String name)
            throws ClassNotFoundException {
        NClassLoaderContext.beginSiblingLookup();
        try {
        for (NClassLoader child : children) {
            if (child.asClassLoader() == requester) {
                continue;
            }
            try {
                NClassLoaderContext.enter(this);
                return child.loadClass(name);
            } catch (ClassNotFoundException ignore) {
                // continue to the next sibling
            } finally {
                NClassLoaderContext.exit(this);
            }
        }
        throw new ClassNotFoundException(name);
        } finally {
            NClassLoaderContext.endSiblingLookup();
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    public URL getResource(String name) {
        // Parent first
        URL u = getParent().getResource(name);
        if (u != null) return u;
        for (NClassLoader child : children) {
            u = child.getResource(name);
            if (u != null) return u;
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> all = new ArrayList<>();
        Enumeration<URL> p = getParent().getResources(name);
        while (p.hasMoreElements()) all.add(p.nextElement());
        for (NClassLoader child : children) {
            Enumeration<URL> c = child.getResources(name);
            while (c.hasMoreElements()) {
                URL next = c.nextElement();
                if (next != null) {
                    all.add(next);
                }
            }
        }
        // Dedupe by URL
        LinkedHashSet<URL> set = new LinkedHashSet<>(all);
        return Collections.enumeration(new ArrayList<>(set));
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL u = getResource(name);
        if (u == null) return null;
        try {
            return u.openStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean contains(NId node) {
        return search(node).isPresent();
    }

    @Override
    public NOptional<NId> search(NId node) {
        if (node == null) return NOptional.ofNamedEmpty("null");
        for (NClassLoader child : children) {
            NOptional<NId> r = child.search(node);
            if (r.isPresent()) {
                return r;
            }
        }
        ClassLoader p = getParent();
        if (p!=null) {
            NOptional<NId> o = NClassLoaderBase.search(node, p, true);
            if(o.isPresent()){
                return o;
            }
        }
        return NClassLoaderBase.search(node, this, false);
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
        for (NClassLoader c : children) {
            if (c instanceof DefaultNLeafClassLoader) {
                DefaultNLeafClassLoader leaf = (DefaultNLeafClassLoader) c;
                if (leaf.id() != null) {
                    m.add(NClasspathEntry.of(leaf.id()));
                } else if (leaf.path() != null) {
                    m.add(NClasspathEntry.of(leaf.path()));
                }
            }
        }
        return m;
    }

    @Override
    public String toString() {
        return "NCompositeClassLoader{" +
                "name='" + name + '\'' +
                ", children=" + children.size() +
                '}';
    }
}
