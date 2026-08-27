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

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.runtime.standalone.atrifact.DefaultNClasspathEntry;
import net.thevpc.nuts.runtime.standalone.io.NCoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Manifest;

/**
 * Base mutable classloader shared by {@link DefaultNMutableClassLoader} and
 * legacy {@link DefaultImmutableNClassLoader}.
 * <p>
 * Internally behaves as a mutable composite of NClassLoader children
 * (typically VM-shared {@link DefaultNLeafClassLoader} instances obtained
 * via {@link NIdClassLoaderRegistry}). The class still extends
 * {@link URLClassLoader} for backwards compatibility (so callers that cast
 * to URLClassLoader and collect {@link #getURLs()} still work); however,
 * URLs are never actually added to the underlying URLClassLoader — they
 * are owned by the children. {@link #getURLs()} is overridden to aggregate
 * from children.
 *
 * @app.category Internal
 */
public abstract class NClassLoaderBase extends URLClassLoader implements NClassLoaderPeer {

    private final String name;
    protected final List<NClasspathEntry> baseEntries = new ArrayList<>();
    protected final List<NClasspathEntry> pendingEntries = new ArrayList<>();
    protected final List<NClasspathEntry> lastResolved = new ArrayList<>();
    protected final Map<String, NDefinition> lastDefinitions = new LinkedHashMap<>();
    protected NRepositoryFilter repositoryFilter;
    protected NDependencyFilter dependencyFilter;

    /**
     * Ordered list of child classloaders used for delegation. Populated by
     * {@link #build()} from {@link #pendingEntries} using the registry.
     */
    protected final List<NClassLoader> children = new ArrayList<>();

    /**
     * Tracks which (shortName → version) have already been loaded in this
     * mutable composite. Used to detect version conflicts and warn the user
     * when a different version for the same shortName is requested (we keep
     * the first-loaded version to respect JVM class identity guarantees).
     */
    protected final Map<String, NVersion> loadedShortNameVersions = new LinkedHashMap<>();

    protected NClassLoaderBase(String name, ClassLoader parent, NClasspathEntry[] entries,
                               NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        super(new URL[0], parent == null ? ClassLoader.getSystemClassLoader() : parent);
        this.name = NStringUtils.firstNonBlank(name, "nclassloader");
        this.repositoryFilter = repositoryFilter;
        this.dependencyFilter = dependencyFilter == null
                ? NDependencyFilter.ofRunnable(true).and(NDependencyFilter.ofOptional(false))
                : dependencyFilter;
        if (entries != null) {
            boolean didAdd = false;
            for (NClasspathEntry e : entries) {
                if (add0(e)) {
                    didAdd = true;
                }
            }
            if (didAdd) {
                buildInternal();
            }
        }
    }

    public List<NClasspathEntry> baseEntries() {
        return baseEntries;
    }

    public List<NDefinition> loadedDefinitions() {
        return new ArrayList<>(lastDefinitions.values());
    }

    public boolean contains(NId node) {
        return search(node).isPresent();
    }

    public static NOptional<NId> search(NId id, ClassLoader classLoader, boolean delegateSearch) {
        if (classLoader instanceof NClassLoader) {
            if (delegateSearch) {
                NOptional<NId> u = ((NClassLoader) classLoader).search(id);
                if (u.isPresent()) {
                    return u;
                }
            }
        }
        URL s = classLoader.getResource("META-INF/maven/" + id.groupId() + "/" + id.artifactId() + "/pom.properties");
        if (s != null) {
            Properties properties = NCoreIOUtils.loadURLProperties(s, null, false, NLog.of(NClassLoaderBase.class));
            NOptional<NId> g = NId.get(properties.getProperty("groupId"), properties.getProperty("artifactId"), properties.getProperty("version"));
            if (g.isPresent()) {
                return g;
            }
        }
        s = classLoader.getResource("META-INF/maven/" + id.groupId() + "/" + id.artifactId() + "/pom.xml");
        if (s != null) {
            try (InputStream is = s.openStream()) {
                NDescriptor d = MavenUtils.of().parsePomXml(is, NFetchMode.LOCAL, id.groupId() + "/" + id.artifactId(), null);
                NId id2 = d.id();
                if (id2 != null) {
                    return NOptional.of(id2);
                }
            } catch (IOException ex) {
                //ignore
            }
        }
        s = classLoader.getResource("META-INF/nuts/" + id.groupId() + "/" + id.artifactId() + "/nuts.nuts");
        if (s != null) {
            try {
                NElement e = NElementReader.ofTson().read(s);
                if (e != null) {
                    if (e.isAnyObject()) {
                        String id2 = e.asObject().get().getStringValue("id").orNull();
                        if (NBlankable.isBlank(id2)) {
                            NOptional<NId> id2o = NId.get(id2);
                            if (id2o.isPresent()) {
                                return id2o;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //ignore
            }
        }
        return NOptional.ofEmpty(NMsg.ofC("not found %s", id));
    }

    public ClassLoader asClassLoader() {
        return this;
    }

    public String name() {
        return name;
    }

    public String getName() {
        return name;
    }


    public NOptional<NId> search(NId id) {
        if (id == null) {
            return NOptional.ofNamedEmpty("null");
        }
        ClassLoader parent = getParent();
        if (parent != null) {
            NOptional<NId> pid = NClassLoaderBase.search(id, parent,true);
            if (pid.isPresent()) {
                return pid;
            }
        }
        String sn = id.shortName();
        for (NClasspathEntry be : baseEntries) {
            switch (be.type()) {
                case DEPENDENCY:
                case DEFINITION: {
                    if (be.id() != null && be.id().equalsShortId(id)) {
                        return NOptional.of(be.id());
                    }
                    break;
                }
            }
        }
        NDefinition o = lastDefinitions.get(sn);
        if (o != null) {
            return NOptional.of(o.id());
        }
        for (NClassLoader child : children) {
            NOptional<NId> r = child.search(id);
            if (r.isPresent()) {
                return r;
            }
        }
        ClassLoader p = getParent();
        if (p instanceof NClassLoader) {
            return ((NClassLoader) p).search(id);
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s", id));
    }

    public NDefinition[] add(NDefinition... defs) {
        if (defs == null) {
            return new NDefinition[0];
        }
        return add(Arrays.stream(defs).filter(Objects::nonNull).map(DefaultNClasspathEntry::new).toArray(NClasspathEntry[]::new));
    }

    public NDefinition[] add(NDependency... defs) {
        if (defs == null) {
            return new NDefinition[0];
        }
        return add(Arrays.stream(defs).filter(Objects::nonNull).map(DefaultNClasspathEntry::new).toArray(NClasspathEntry[]::new));
    }

    public NDefinition[] add(NId... defs) {
        if (defs == null) {
            return new NDefinition[0];
        }
        return add(Arrays.stream(defs).filter(Objects::nonNull).map(DefaultNClasspathEntry::new).toArray(NClasspathEntry[]::new));
    }

    public NDefinition[] add(NClasspathEntry... entries) {
        if (entries == null) {
            return new NDefinition[0];
        }
        boolean b = false;
        for (NClasspathEntry a : entries) {
            if (a == null) {
                continue;
            }
            if (add0(a)) {
                b = true;
            }
        }
        if (b) {
            return buildInternal();
        }
        return new NDefinition[0];
    }

    public boolean add0(NClasspathEntry entry) {
        if (entry == null) {
            return false;
        }
        if (entry.type() == NClasspathEntryType.PATH) {
            pendingEntries.add(entry);
            baseEntries.add(entry);
            return true;
        } else {
            if (entry.id() != null && search(entry.id()).isPresent()) {
                return false;
            }
            pendingEntries.add(entry);
            baseEntries.add(entry);
            return true;
        }
    }

    public boolean add0(NDefinition definition) {
        if (definition == null) {
            return false;
        }
        return add0(new DefaultNClasspathEntry(definition));
    }

    public boolean add0(NId id) {
        if (id == null) {
            return false;
        }
        if (search(id).isPresent()) {
            return false;
        }
        return add0(new DefaultNClasspathEntry(id));
    }

    public boolean add0(NDependency id) {
        if (id == null) {
            return false;
        }
        if (search(id.toId()).isPresent()) {
            return false;
        }
        return add0(new DefaultNClasspathEntry(id));
    }

    @Override
    public void addURL(URL url) {
        if (url != null) {
            add0(new DefaultNClasspathEntry(NPath.of(url)));
        }
    }

    protected void addPath0(NPath path) {
        if (path != null) {
            add0(new DefaultNClasspathEntry(path));
        }
    }

    @Override
    public String toString() {
        return "NutsURLClassLoader{" +
                "name='" + name + '\'' +
                '}';
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
    public URL[] getURLs() {
        // Aggregate from children leaves for backwards compat with callers
        // that collect URLs via URLClassLoader.getURLs().
        List<URL> urls = new ArrayList<>();
        for (NClassLoader c : children) {
            if (c instanceof DefaultNLeafClassLoader) {
                URL[] cu = ((DefaultNLeafClassLoader) c).getURLs();
                if (cu != null) {
                    for (URL u : cu) {
                        if (u != null) {
                            urls.add(u);
                        }
                    }
                }
            } else if (c instanceof URLClassLoader) {
                URL[] cu = ((URLClassLoader) c).getURLs();
                if (cu != null) {
                    for (URL u : cu) {
                        if (u != null) {
                            urls.add(u);
                        }
                    }
                }
            }
        }
        // Also include anything ever added to super.getURLs() (legacy, should be empty).
        URL[] sup = super.getURLs();
        if (sup != null && sup.length > 0) {
            for (URL u : sup) {
                if (u != null) {
                    urls.add(u);
                }
            }
        }
        LinkedHashSet<URL> dedup = new LinkedHashSet<>(urls);
        return dedup.toArray(new URL[0]);
    }

    @Override
    protected Package definePackage(String name, Manifest man, URL url) {
        return super.definePackage(name, man, url);
    }

    @Override
    public URL findResource(String name) {
        // Composite semantics: first child hit wins.
        for (NClassLoader child : children) {
            URL u = child.getResource(name);
            if (u != null) return u;
        }
        return super.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        List<URL> all = new ArrayList<>();
        Enumeration<URL> sup = super.findResources(name);
        if (sup != null) {
            while (sup.hasMoreElements()) all.add(sup.nextElement());
        }
        for (NClassLoader child : children) {
            Enumeration<URL> c = child.getResources(name);
            while (c.hasMoreElements()) {
                URL next = c.nextElement();
                if (next != null) all.add(next);
            }
        }
        LinkedHashSet<URL> set = new LinkedHashSet<>(all);
        return Collections.enumeration(new ArrayList<>(set));
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 1. Already loaded?
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            if (resolve) resolveClass(c);
            return c;
        }

        // 2. Parent (standard delegation).
        try {
            ClassLoader p = getParent();
            if (p != null) {
                c = p.loadClass(name);
                if (c != null) {
                    if (resolve) resolveClass(c);
                    return c;
                }
            }
        } catch (ClassNotFoundException ignore) {
            // not in parent
        }

        // 3. Delegate to children in order; first success wins.
        for (NClassLoader child : children) {
            try {
                NClassLoaderContext.enter(this);
                c = child.loadClass(name);
                if (c != null) {
                    if (resolve) resolveClass(c);
                    return c;
                }
            } catch (ClassNotFoundException ignore) {
                // continue to next child
            } finally {
                NClassLoaderContext.exit(this);
            }
        }

        // 4. Fallback: let URLClassLoader's own findClass() try it against
        //    super.getURLs() (should be empty for new usage; keeps legacy compat).
        try {
            c = findClass(name);
            if (c != null) {
                if (resolve) resolveClass(c);
                return c;
            }
        } catch (ClassNotFoundException ignore) {
            // not there either
        }

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
        // parent first, then children
        URL u = getParent().getResource(name);
        if (u != null) return u;
        for (NClassLoader child : children) {
            u = child.getResource(name);
            if (u != null) return u;
        }
        return super.findResource(name);
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
                if (next != null) all.add(next);
            }
        }
        Enumeration<URL> sup = super.findResources(name);
        while (sup.hasMoreElements()) all.add(sup.nextElement());
        LinkedHashSet<URL> set = new LinkedHashSet<>(all);
        return Collections.enumeration(new ArrayList<>(set));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NClassLoaderBase that = (NClassLoaderBase) o;
        return Objects.equals(name, that.name) && Objects.equals(baseEntries, that.baseEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, baseEntries);
    }

    /**
     * Remove all children that correspond to the given NId from the
     * delegation list. Does NOT remove entries from the VM-static registry
     * (JVM constraints prevent actual unloading).
     */
    public synchronized void remove(NId id) {
        if (id == null) return;
        String longName = id.longName();
        String shortName = id.shortName();
        children.removeIf(c -> {
            if (c instanceof DefaultNLeafClassLoader) {
                NId cid = ((DefaultNLeafClassLoader) c).id();
                return cid != null && cid.longName().equals(longName);
            }
            return false;
        });
        baseEntries.removeIf(e -> e.id() != null && e.id().longName().equals(longName));
        loadedShortNameVersions.remove(shortName);
        lastDefinitions.remove(shortName);
    }

    /**
     * Atomically rebuild the mutable composite so only the desired set of
     * NDefinitions (and their resolved entries) remain in the delegation
     * list. Dropped jars are no longer consulted for future lookups; loaded
     * Class objects remain valid per JVM constraints.
     */
    public synchronized NDefinition[] rebuild(NDefinition[] desired) {
        // Clear state
        pendingEntries.clear();
        lastResolved.clear();
        lastDefinitions.clear();
        loadedShortNameVersions.clear();
        children.clear();
        baseEntries.clear();
        // Repopulate from the desired set
        if (desired != null) {
            for (NDefinition d : desired) {
                if (d != null) {
                    add0(d);
                }
            }
        }
        return buildInternal();
    }

    public synchronized NDefinition[] build() {
        return buildInternal();
    }

    /**
     * Resolve pending entries via {@link NClasspathBuilder}, create or
     * retrieve shared leaves via {@link NIdClassLoaderRegistry}, and add
     * them to this mutable composite's children list.
     * <p>
     * Version conflicts (same shortName, different version already loaded
     * in this composite) are resolved by keeping the first-loaded version
     * and logging a warning for subsequent different versions.
     * <p>
     * This method is final to ensure subclass constructors (e.g.
     * DefaultImmutableNClassLoader) always execute the real resolution at
     * construction time, even if the subclass overrides the public
     * {@link #build()} to freeze mutability post-construction.
     */
    protected final synchronized NDefinition[] buildInternal() {
        if (pendingEntries.isEmpty()) {
            return new NDefinition[0];
        }
        NClasspathBuilder cb = NClasspathBuilder.of()
                .repositoryFilter(repositoryFilter)
                .dependencyFilter(dependencyFilter);
        for (NClasspathEntry value : lastResolved) {
            cb.add(value);
        }
        for (NClasspathEntry be : pendingEntries) {
            cb.add(be);
        }

        Map<String, NDefinition> oldLastDefinitions = new LinkedHashMap<>(lastDefinitions);
        Map<String, NDefinition> newDefinitions = new LinkedHashMap<>();
        lastDefinitions.clear();
        lastResolved.clear();

        // Before resolving, snapshot currently-loaded shortName versions so we
        // can preserve first-loaded semantics across build() calls.
        Map<String, NVersion> alreadyLoadedShort = new LinkedHashMap<>(loadedShortNameVersions);
        // Collect candidate leaves first (per resolved entry), honoring conflict rules.
        List<NClassLoader> newLeavesToAdd = new ArrayList<>();
        Set<String> addedLongNames = new LinkedHashSet<>();
        for (NClassLoader c : children) {
            if (c instanceof DefaultNLeafClassLoader) {
                NId cid = ((DefaultNLeafClassLoader) c).id();
                if (cid != null) {
                    addedLongNames.add(cid.longName());
                }
            }
        }

        if (!cb.isEmpty()) {
            List<NClasspathEntry> resolved = cb.resolve();
            for (NClasspathEntry d : resolved) {
                // Resolve a leaf candidate for this entry
                DefaultNLeafClassLoader leaf = null;
                NDefinition entryDef = null;
                NId entryId = null;
                NPath entryPath = null;

                if (d.type() == NClasspathEntryType.PATH) {
                    entryPath = d.path();
                    if (entryPath != null) {
                        leaf = NIdClassLoaderRegistry.getOrCreate(entryPath);
                    }
                } else {
                    entryId = d.id();
                    entryDef = d.definition();
                    entryPath = d.path();
                    if (entryPath == null && entryDef != null) {
                        entryPath = entryDef.content().orNull();
                    }
                    if (entryId != null && entryPath != null) {
                        leaf = NIdClassLoaderRegistry.getOrCreate(entryId, entryPath);
                    } else if (entryDef != null) {
                        leaf = NIdClassLoaderRegistry.getOrCreate(entryDef);
                    } else if (entryPath != null) {
                        leaf = NIdClassLoaderRegistry.getOrCreate(entryPath);
                    }
                }
                if (leaf == null) {
                    // No resolvable path; skip
                    lastResolved.add(d);
                    continue;
                }

                // If this is an id-backed leaf, perform conflict check by shortName.
                NId leafId = leaf.id();
                if (leafId != null) {
                    String shortName = leafId.shortName();
                    NVersion desiredVersion = leafId.version();
                    NVersion already = alreadyLoadedShort.get(shortName);
                    if (already != null) {
                        // Same version? No conflict. Accept if not already in list.
                        if (!Objects.equals(already, desiredVersion)) {
                            // Version mismatch! Keep old, warn user.
                            NLog.of(NClassLoaderBase.class).warn(
                                    NMsg.ofC(
                                            "Extension %s requested version %s but version %s is already loaded in this mutable classloader. Keeping the already-loaded version to preserve JVM class identity.",
                                            shortName, desiredVersion, already));
                            lastResolved.add(d);
                            continue;
                        }
                    } else {
                        alreadyLoadedShort.put(shortName, desiredVersion);
                    }
                    // Track definitions by shortName for loadedDefinitions() compat
                    if (entryDef != null) {
                        if (!lastDefinitions.containsKey(shortName)) {
                            lastDefinitions.put(shortName, entryDef);
                            if (!oldLastDefinitions.containsKey(shortName)) {
                                newDefinitions.put(shortName, entryDef);
                            }
                        }
                    }
                    // Avoid duplicating same longName in children
                    String longName = leafId.longName();
                    if (addedLongNames.add(longName)) {
                        newLeavesToAdd.add(leaf);
                    }
                } else {
                    // Path-backed: add by leaf identity (dedup same object)
                    if (!newLeavesToAdd.contains(leaf) && !children.contains(leaf)) {
                        newLeavesToAdd.add(leaf);
                    }
                }

                lastResolved.add(d);
            }

            // Commit children and version tracking
            children.addAll(newLeavesToAdd);
            loadedShortNameVersions.clear();
            loadedShortNameVersions.putAll(alreadyLoadedShort);
        }
        pendingEntries.clear();
        return newDefinitions.values().toArray(new NDefinition[0]);
    }
}
