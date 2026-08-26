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
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.internal.rpi.NDependencyFilterRPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.runtime.standalone.atrifact.DefaultNClasspathEntry;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Manifest;

public abstract class NClassLoaderBase extends URLClassLoader {

    private final String name;
    //    protected Map<String, NClasspathEntry> baseIdsByShortName = new LinkedHashMap<>();
    protected List<NClasspathEntry> baseEntries = new ArrayList<>();
    protected List<NClasspathEntry> pendingEntries = new ArrayList<>();
    protected List<NClasspathEntry> lastResolved = new ArrayList<>();
    protected Map<String, NDefinition> lastDefinitions = new LinkedHashMap<>();
    protected NRepositoryFilter repositoryFilter;
    protected NDependencyFilter dependencyFilter;


    protected NClassLoaderBase(String name, ClassLoader parent, NClasspathEntry[] entries, NRepositoryFilter repositoryFilter, NDependencyFilter dependencyFilter) {
        super(new URL[0], parent);
        this.name = NStringUtils.firstNonBlank(name, "nclassloader");
        this.repositoryFilter = repositoryFilter;
        this.dependencyFilter = dependencyFilter == null ? NDependencyFilter.ofRunnable(true).and(NDependencyFilter.ofOptional(false)) : dependencyFilter;
        add(entries);
    }

    public List<NClasspathEntry> baseEntries() {
        return baseEntries;
    }

    public List<NDefinition> loadedDefinitions() {
        return new ArrayList<>(lastDefinitions.values());
    }


    public boolean isLoaded(NId id) {
        if (search(id).isPresent()) return true;
        // try current class loader
        URL s = getResource("META-INF/maven/" + id.groupId() + "/" + id.artifactId() + "/pom.properties");
        if (s != null) {
            return true;
        }
        s = getResource("META-INF/maven/" + id.groupId() + "/" + id.artifactId() + "/pom.xml");
        if (s != null) {
            return true;
        }
        s = getResource("META-INF/nuts/" + id.groupId() + "/" + id.artifactId() + "/nuts.nuts");
        return s != null;
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


    public boolean contains(NId node) {
        return search(node).isPresent();
    }

    public NOptional<NId> search(NId id) {
        String sn = id.shortName();
        for (NClasspathEntry be : baseEntries) {
            switch (be.type()) {
                case DEPENDENCY:
                case DEFINITION: {
                    if (be.id().equalsShortId(id)) {
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
        o = lastDefinitions.get(sn);
        if (o != null) {
            return NOptional.of(o.id());
        }
        ClassLoader p = getParent();
        if (p instanceof NClassLoader) {
            return ((NClassLoader) p).search(id);
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s",id));
    }


    public NDefinition[] add(NDefinition... defs) {
        if(defs==null){
            return new NDefinition[0];
        }
        return add(Arrays.stream(defs).filter(Objects::nonNull).map(DefaultNClasspathEntry::new).toArray(NClasspathEntry[]::new));
    }

    public NDefinition[] add(NDependency... defs) {
        if(defs==null){
            return new NDefinition[0];
        }
        return add(Arrays.stream(defs).filter(Objects::nonNull).map(DefaultNClasspathEntry::new).toArray(NClasspathEntry[]::new));
    }

    public NDefinition[] add(NId... defs) {
        if(defs==null){
            return new NDefinition[0];
        }
        return add(Arrays.stream(defs).filter(Objects::nonNull).map(DefaultNClasspathEntry::new).toArray(NClasspathEntry[]::new));
    }

    public NDefinition[] add(NClasspathEntry... entries) {
        if(entries==null){
            return new NDefinition[0];
        }
        boolean b = false;
        for (NClasspathEntry a : entries) {
            if(a==null){
                continue;
            }
            if(add0(a)){
                b=true;
            }
        }
        if (b) {
            return build();
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
            if (search(entry.id()).isPresent()) {
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
        return "NutsURLClassLoader{"
                + "name='" + name + '\''
                + '}';
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }

    @Override
    public URL[] getURLs() {
        return super.getURLs();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    protected Package definePackage(String name, Manifest man, URL url) {
        return super.definePackage(name, man, url);
    }

    @Override
    public URL findResource(String name) {
        return super.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    public @Nullable URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
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

    public synchronized NDefinition[] build() {
        if (pendingEntries.isEmpty()) {
            return new NDefinition[0];
        }
        NClasspathBuilder cb = NClasspathBuilder.of()
                .repositoryFilter(repositoryFilter)
                .dependencyFilter(dependencyFilter);
        for (NClasspathEntry value : lastResolved) {
            //ensure always resolve already loaded!
            cb.add(value);
        }
        for (NClasspathEntry be : pendingEntries) {
            cb.add(be);
        }

        Map<String, NDefinition> oldLastDefinitions = new LinkedHashMap<>(lastDefinitions);
        Map<String, NDefinition> newDefinitions = new LinkedHashMap<>();
        lastDefinitions.clear();
        lastResolved.clear();
        if (!cb.isEmpty()) {
            List<NClasspathEntry> resolved = cb.resolve();
            for (NClasspathEntry d : resolved) {
                if (d.path() != null) {
                    URL url = d.path().toURL().orNull();
                    if (url != null) {
                        super.addURL(url);
                    }
                }
                if (d.id() != null) {
                    String shortName = d.id().shortName();
                    if (!lastDefinitions.containsKey(shortName)) {
                        if (d.definition() != null) {
                            lastDefinitions.put(shortName, d.definition());
                            if (!oldLastDefinitions.containsKey(shortName)) {
                                newDefinitions.put(shortName, d.definition());
                            }
                        }
                    }
                }
            }
            lastResolved.addAll(resolved);
        }
        pendingEntries.clear();
        return newDefinitions.values().toArray(new NDefinition[0]);
    }


}
