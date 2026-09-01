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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
     * Universal parent for all leaves: the system classloader. Leaves must
     * NOT depend on any workspace-specific parent so they can safely be
     * shared across workspaces in the same VM. Workspace scoping is
     * provided by the composite's own parent (workspaceExtensionsClassLoader).
     */
    private static ClassLoader LEAF_PARENT /*= ClassLoader.getSystemClassLoader()*/;

    public static boolean LEAF_PARENT(ClassLoader c){
        if(c!=null) {
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
    private static ClassLoader LEAF_PARENT(){
        NAssert.requireNamedNonNull(LEAF_PARENT,"LEAF_PARENT_CLASSLOADER");
        return LEAF_PARENT;
    }


    static DefaultNLeafClassLoader getOrCreate(NId id, NPath resolvedJarPath) {
        if (id == null || resolvedJarPath == null) {
            return null;
        }
        String key = id.longName();
        return BY_ID.computeIfAbsent(key, k ->
                new DefaultNLeafClassLoader(id, resolvedJarPath, LEAF_PARENT()));
    }

    static DefaultNLeafClassLoader getOrCreate(NPath rawPath) {
        if (rawPath == null) {
            return null;
        }
        final NPath abs;
        try {
            abs = rawPath.toAbsolute();
        } catch (Exception ex) {
            return BY_PATH.computeIfAbsent(rawPath.toString(), k ->
                    new DefaultNLeafClassLoader(null, rawPath, LEAF_PARENT()));
        }
        String key = abs.toString();
        return BY_PATH.computeIfAbsent(key, k ->
                new DefaultNLeafClassLoader(null, abs, LEAF_PARENT()));
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
        List<DefaultNLeafClassLoader> leaves = new ArrayList<>(BY_ID.values());
        leaves.addAll(BY_PATH.values());
        for (DefaultNLeafClassLoader leaf : leaves) {
            if (leaf != requester) {
                try {
                    // Keep normal URLClassLoader parent-first semantics. A
                    // direct findClass() here could redefine an API already
                    // owned by the leaf's parent and cause LinkageError.
                    return leaf.loadClassFromParentAndOwn(name);
                } catch (ClassNotFoundException ignored) {
                    // Try the next registered artifact.
                }
            }
        }
        throw new ClassNotFoundException(name);
    }

    static DefaultNLeafClassLoader getIfPresent(NId id) {
        if (id == null) {
            return null;
        }
        return BY_ID.get(id.longName());
    }

    static DefaultNLeafClassLoader getIfPresent(NPath path) {
        if (path == null) {
            return null;
        }
        try {
            return BY_PATH.get(path.toAbsolute().toString());
        } catch (Exception ex) {
            return BY_PATH.get(path.toString());
        }
    }

    static void invalidate(NId id) {
        if (id != null) {
            BY_ID.remove(id.longName());
        }
    }

    static void invalidate(NPath path) {
        if (path != null) {
            try {
                BY_PATH.remove(path.toAbsolute().toString());
            } catch (Exception ex) {
                BY_PATH.remove(path.toString());
            }
        }
    }

    static void invalidateAll() {
        BY_ID.clear();
        BY_PATH.clear();
    }
}
