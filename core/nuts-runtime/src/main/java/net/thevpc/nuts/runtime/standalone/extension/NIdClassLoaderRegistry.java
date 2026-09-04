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

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * VM-static (classloader-global) registry of leaf classloaders keyed by
 * NId.longName (group:artifact:classifier#version) or absolute NPath for
 * raw-path entries.
 * <p>
 * Guarantees that for the same GAV (longName), exactly one
 * {@link DefaultNLeafClassLoader} is ever created in this VM, so Class
 * identity is preserved across workspaces, searches, and mutable extension
 * classloaders.
 * <p>
 * Mirror-de-duplication comes for free: entries with the same longName but
 * different paths/mirrors simply return the already-registered leaf.
 * <p>
 * All methods are thread-safe. Entries are held with strong references to
 * respect JVM class identity semantics (leaves are pinned as long as any
 * Class they defined is reachable, which is effectively forever for most
 * workloads).
 *
 * @app.category Internal
 */
public final class NIdClassLoaderRegistry {

    private NIdClassLoaderRegistry() {
    }

    /**
     * Key: NId.longName() (GAV, includes version and classifier).
     */
    private static final ConcurrentHashMap<String, DefaultNLeafClassLoader> BY_ID =
            new ConcurrentHashMap<>();

    /**
     * Key: absolute, normalized NPath.toString() for raw (non-id) paths.
     */
    private static final ConcurrentHashMap<String, DefaultNLeafClassLoader> BY_PATH =
            new ConcurrentHashMap<>();

    /**
     * Index of class name -> leaf classloader that contains it in its jar/folder.
     */
    private static final ConcurrentHashMap<String, DefaultNLeafClassLoader> CLASS_TO_LEAF =
            new ConcurrentHashMap<>();

    /**
     * Index of package name -> set of leaf classloaders containing classes in that package.
     */
    private static final ConcurrentHashMap<String, Set<DefaultNLeafClassLoader>> PACKAGE_TO_LEAVES =
            new ConcurrentHashMap<>();

    /**
     * Leaves that could not be completely indexed (e.g. non-local or unparseable).
     */
    private static final Set<DefaultNLeafClassLoader> UNINDEXED_LEAVES =
            ConcurrentHashMap.newKeySet();

    /**
     * Metadata index per leaf.
     */
    private static final ConcurrentHashMap<DefaultNLeafClassLoader, LeafIndex> LEAF_INDEXES =
            new ConcurrentHashMap<>();

    /**
     * Positive cache: className -> ClassRecord(Class<?>, DefaultNLeafClassLoader).
     */
    private static final ConcurrentHashMap<String, ClassRecord> RESOLVED_CLASS_CACHE =
            new ConcurrentHashMap<>();

    /**
     * Negative cache: class names confirmed not to exist in any registered leaf.
     */
    private static final Set<String> NEGATIVE_CLASS_CACHE =
            ConcurrentHashMap.newKeySet();

    /**
     * Universal parent for all leaves: the system classloader. Leaves must
     * NOT depend on any workspace-specific parent so they can safely be
     * shared across workspaces in the same VM. Workspace scoping is
     * provided by the composite's own parent (workspaceExtensionsClassLoader).
     */
    private static ClassLoader LEAF_PARENT /*= ClassLoader.getSystemClassLoader()*/;

    public static boolean LEAF_PARENT(ClassLoader c) {
        if (c != null) {
            NAssert.requireNamedNonNull(c, "LEAF_PARENT_CLASSLOADER");
            synchronized (NIdClassLoaderRegistry.class) {
                if (LEAF_PARENT == null) {
                    LEAF_PARENT = c;
                    return true;
                }
            }
        }
        return false;
    }

    private static ClassLoader LEAF_PARENT() {
        NAssert.requireNamedNonNull(LEAF_PARENT, "LEAF_PARENT_CLASSLOADER");
        return LEAF_PARENT;
    }

    static DefaultNLeafClassLoader getOrCreate(NId id, NPath resolvedJarPath) {
        if (id == null || resolvedJarPath == null) {
            return null;
        }
        String key = id.longName();
        DefaultNLeafClassLoader existing = BY_ID.get(key);
        if (existing != null) {
            return existing;
        }
        boolean[] created = new boolean[1];
        DefaultNLeafClassLoader leaf = BY_ID.computeIfAbsent(key, k -> {
            created[0] = true;
            return new DefaultNLeafClassLoader(id, resolvedJarPath, LEAF_PARENT());
        });
        if (created[0]) {
            registerLeaf(leaf);
        }
        return leaf;
    }

    static DefaultNLeafClassLoader getOrCreate(NPath rawPath) {
        if (rawPath == null) {
            return null;
        }
        NPath abs = null;
        String key;
        try {
            abs = rawPath.toAbsolute();
            key = abs.toString();
        } catch (Exception ex) {
            key = rawPath.toString();
        }
        DefaultNLeafClassLoader existing = BY_PATH.get(key);
        if (existing != null) {
            return existing;
        }
        final NPath effectivePath = abs != null ? abs : rawPath;
        boolean[] created = new boolean[1];
        DefaultNLeafClassLoader leaf = BY_PATH.computeIfAbsent(key, k -> {
            created[0] = true;
            return new DefaultNLeafClassLoader(null, effectivePath, LEAF_PARENT());
        });
        if (created[0]) {
            registerLeaf(leaf);
        }
        return leaf;
    }

    static DefaultNLeafClassLoader getOrCreate(NDefinition def) {
        if (def == null) {
            return null;
        }
        NPath p = def.content().orNull();
        if (p == null) {
            return null;
        }
        return getOrCreate(def.id(), p);
    }

    /** Find a class in another registered leaf without re-entering its parent. */
    static Class<?> findInRegisteredLeaves(DefaultNLeafClassLoader requester, String name)
            throws ClassNotFoundException {
        // 1. Check resolved class cache
        ClassRecord rec = RESOLVED_CLASS_CACHE.get(name);
        if (rec != null) {
            if (rec.definingLeaf != requester) {
                return rec.clazz;
            }
            throw new ClassNotFoundException(name);
        }

        // 2. Check negative cache
        if (NEGATIVE_CLASS_CACHE.contains(name)) {
            throw new ClassNotFoundException(name);
        }

        // 3. Check known class owner from jar index
        DefaultNLeafClassLoader owner = CLASS_TO_LEAF.get(name);
        if (owner != null && owner != requester) {
            try {
                Class<?> c = owner.loadClassFromParentAndOwn(name);
                RESOLVED_CLASS_CACHE.put(name, new ClassRecord(c, owner));
                return c;
            } catch (ClassNotFoundException ignored) {
                // In case indexing had an entry that couldn't be loaded, fall through
            }
        }

        // 4. Check leaves sharing the same package (e.g. for dynamic proxies, split packages)
        int lastDot = name.lastIndexOf('.');
        String pkg = lastDot > 0 ? name.substring(0, lastDot) : "";
        Set<DefaultNLeafClassLoader> pkgLeaves = PACKAGE_TO_LEAVES.get(pkg);
        if (pkgLeaves != null) {
            for (DefaultNLeafClassLoader leaf : pkgLeaves) {
                if (leaf != requester && leaf != owner) {
                    try {
                        Class<?> c = leaf.loadClassFromParentAndOwn(name);
                        RESOLVED_CLASS_CACHE.put(name, new ClassRecord(c, leaf));
                        return c;
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        }

        // 5. Check unindexed leaves (fallback)
        for (DefaultNLeafClassLoader leaf : UNINDEXED_LEAVES) {
            if (leaf != requester && leaf != owner) {
                try {
                    Class<?> c = leaf.loadClassFromParentAndOwn(name);
                    RESOLVED_CLASS_CACHE.put(name, new ClassRecord(c, leaf));
                    return c;
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        // 6. Not found in any registered leaf! Record in negative cache.
        NEGATIVE_CLASS_CACHE.add(name);
        throw new ClassNotFoundException(name);
    }

    public static ClassLoader getIfPresent(NId id) {
        if (id == null) {
            return null;
        }
        return BY_ID.get(id.longName());
    }

    public static ClassLoader getIfPresent(NPath path) {
        if (path == null) {
            return null;
        }
        try {
            return BY_PATH.get(path.toAbsolute().toString());
        } catch (Exception ex) {
            return BY_PATH.get(path.toString());
        }
    }

    public static void invalidate(NId id) {
        if (id != null) {
            DefaultNLeafClassLoader removed = BY_ID.remove(id.longName());
            if (removed != null) {
                unregisterLeaf(removed);
            }
        }
    }

    public static void invalidate(NPath path) {
        if (path != null) {
            DefaultNLeafClassLoader removed = null;
            try {
                removed = BY_PATH.remove(path.toAbsolute().toString());
            } catch (Exception ex) {
                removed = BY_PATH.remove(path.toString());
            }
            if (removed != null) {
                unregisterLeaf(removed);
            }
        }
    }

    public static void invalidateAll() {
        BY_ID.clear();
        BY_PATH.clear();
        LEAF_INDEXES.clear();
        CLASS_TO_LEAF.clear();
        PACKAGE_TO_LEAVES.clear();
        UNINDEXED_LEAVES.clear();
        RESOLVED_CLASS_CACHE.clear();
        NEGATIVE_CLASS_CACHE.clear();
    }

    public static void invalidateCache() {
        RESOLVED_CLASS_CACHE.clear();
        NEGATIVE_CLASS_CACHE.clear();
    }

    public static void invalidateClass(String className) {
        if (className != null) {
            RESOLVED_CLASS_CACHE.remove(className);
            NEGATIVE_CLASS_CACHE.remove(className);
        }
    }

    private static void registerLeaf(DefaultNLeafClassLoader leaf) {
        if (leaf == null) return;
        LeafIndex idx = indexLeaf(leaf);
        LEAF_INDEXES.put(leaf, idx);
        if (idx.complete) {
            for (String cname : idx.classes) {
                CLASS_TO_LEAF.putIfAbsent(cname, leaf);
            }
            for (String pkg : idx.packages) {
                PACKAGE_TO_LEAVES.computeIfAbsent(pkg, k -> ConcurrentHashMap.newKeySet()).add(leaf);
            }
        } else {
            UNINDEXED_LEAVES.add(leaf);
        }
        NEGATIVE_CLASS_CACHE.clear();
    }

    private static void unregisterLeaf(DefaultNLeafClassLoader leaf) {
        LeafIndex idx = LEAF_INDEXES.remove(leaf);
        if (idx != null) {
            for (String cname : idx.classes) {
                CLASS_TO_LEAF.remove(cname, leaf);
            }
            for (String pkg : idx.packages) {
                Set<DefaultNLeafClassLoader> set = PACKAGE_TO_LEAVES.get(pkg);
                if (set != null) {
                    set.remove(leaf);
                    if (set.isEmpty()) {
                        PACKAGE_TO_LEAVES.remove(pkg);
                    }
                }
            }
        }
        UNINDEXED_LEAVES.remove(leaf);
        RESOLVED_CLASS_CACHE.values().removeIf(rec -> rec.definingLeaf == leaf);
        NEGATIVE_CLASS_CACHE.clear();
    }

    private static LeafIndex indexLeaf(DefaultNLeafClassLoader leaf) {
        NPath path = leaf.path();
        if (path == null) {
            return LeafIndex.EMPTY;
        }
        File file = path.toFile().orNull();
        if (file == null) {
            java.nio.file.Path p = path.toPath().orNull();
            if (p != null) {
                try {
                    file = p.toFile();
                } catch (Exception ignored) {
                }
            }
        }
        if (file == null || !file.exists()) {
            return LeafIndex.EMPTY;
        }
        Set<String> classes = new HashSet<>();
        Set<String> packages = new HashSet<>();
        try {
            if (file.isFile()) {
                try (JarFile jar = new JarFile(file)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String ename = entry.getName();
                        if (ename.endsWith(".class") && !entry.isDirectory()) {
                            String cname = ename.substring(0, ename.length() - 6).replace('/', '.');
                            classes.add(cname);
                            int lastSlash = ename.lastIndexOf('/');
                            if (lastSlash > 0) {
                                packages.add(ename.substring(0, lastSlash).replace('/', '.'));
                            } else {
                                packages.add("");
                            }
                        }
                    }
                    return new LeafIndex(classes, packages, true);
                }
            } else if (file.isDirectory()) {
                indexDirectory(file, "", classes, packages);
                return new LeafIndex(classes, packages, true);
            }
        } catch (Exception ignored) {
            // fallback
        }
        return LeafIndex.EMPTY;
    }

    private static void indexDirectory(File dir, String pkgPrefix, Set<String> classes, Set<String> packages) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                String subPkg = pkgPrefix.isEmpty() ? f.getName() : pkgPrefix + "." + f.getName();
                packages.add(subPkg);
                indexDirectory(f, subPkg, classes, packages);
            } else if (f.isFile() && f.getName().endsWith(".class")) {
                String cname = f.getName().substring(0, f.getName().length() - 6);
                String fullClass = pkgPrefix.isEmpty() ? cname : pkgPrefix + "." + cname;
                classes.add(fullClass);
                packages.add(pkgPrefix);
            }
        }
    }

    private static class LeafIndex {
        static final LeafIndex EMPTY = new LeafIndex(Collections.emptySet(), Collections.emptySet(), false);
        final Set<String> classes;
        final Set<String> packages;
        final boolean complete;

        LeafIndex(Set<String> classes, Set<String> packages, boolean complete) {
            this.classes = classes;
            this.packages = packages;
            this.complete = complete;
        }
    }

    private static class ClassRecord {
        final Class<?> clazz;
        final DefaultNLeafClassLoader definingLeaf;

        ClassRecord(Class<?> clazz, DefaultNLeafClassLoader definingLeaf) {
            this.clazz = clazz;
            this.definingLeaf = definingLeaf;
        }
    }
}
