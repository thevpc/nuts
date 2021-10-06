/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.NutsId;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import net.thevpc.nuts.NutsClassLoaderNode;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

public class DefaultNutsClassLoader extends URLClassLoader {

    private String name;
    private NutsSession ws;
    private LinkedHashMap<String, NutsClassLoaderNode> nodes = new LinkedHashMap<>();
    private LinkedHashMap<String, NutsClassLoaderNode> effective = new LinkedHashMap<>();
    public DefaultNutsClassLoader(String name, NutsSession ws, ClassLoader parent) {
        super(new URL[0], parent);
        this.name = name;
        this.ws = ws;
    }

    public String getName() {
        return name;
    }

    public boolean contains(NutsClassLoaderNode node, boolean deep) {
        return search(node, deep) != null;
    }

    public NutsClassLoaderNode search(NutsClassLoaderNode node, boolean deep) {
        NutsId ii = ws.id().parser().parse(node.getId());
        String sn = ii.getShortName();
        NutsClassLoaderNode o = nodes.get(sn);
        if (o != null) {
            return o;
        }
        if (deep) {
            o = effective.get(sn);
            if (o != null) {
                return o;
            }
        }
        ClassLoader p = getParent();
        if(p instanceof DefaultNutsClassLoader){
            return ((DefaultNutsClassLoader) p).search(node, deep);
        }
        return null;
    }

    public boolean add(NutsClassLoaderNode node) {
        NutsId ii = ws.id().parser().parse(node.getId());
        String sn = ii.getShortName();
        if (!nodes.containsKey(sn)) {
            nodes.put(sn, node);
            return add(node, true);
        }
        return false;
    }

    protected boolean add(NutsClassLoaderNode node, boolean deep) {
        String s = node.getId();
        NutsId ii = ws.id().parser().parse(s);
        String sn = ii.getShortName();
        if (!effective.containsKey(sn)) {
            effective.put(sn, node);
            super.addURL(node.getURL());
            if (deep) {
                for (NutsClassLoaderNode dependency : node.getDependencies()) {
                    add(dependency, true);
                }
            }
            return true;
        } else {
            if (deep) {
                for (NutsClassLoaderNode dependency : node.getDependencies()) {
                    add(dependency, true);
                }
            }
            return false;
        }
    }

    @Override
    public void addURL(URL url) {
        throw new IllegalArgumentException("unsupported addURL");
    }

//    public boolean addId(NutsId id){
//        return ids.add(id);
//    }
//
//    public Set<NutsId> getIds() {
//        return ids;
//    }
//
//    public void addPath(Path url) {
//        try {
//            addURL(url.toUri().toURL());
//        } catch (MalformedURLException e) {
//            throw new NutsIllegalArgumentException(ws, url.toString());
//        }
//    }
//
//    public void addPath(String path) {
//        try {
//            addURL(Paths.get(path).toUri().toURL());
//        } catch (MalformedURLException e) {
//            throw new NutsIllegalArgumentException(ws, path);
//        }
//    }
//
//    public void addFile(File url) {
//        try {
//            addURL(url.toURI().toURL());
//        } catch (MalformedURLException e) {
//            throw new NutsIllegalArgumentException(ws, url.toString());
//        }
//    }
//
//    public void addURL(String url) {
//        try {
//            addURL(CoreIOUtils.toURL(new String[]{url})[0]);
//        } catch (UncheckedIOException| NutsIOException e) {
//            throw new NutsIllegalArgumentException(ws, url.toString());
//        }
//    }
    @Override
    public String toString() {
        return "NutsURLClassLoader{"
                + "name='" + name + '\''
                + '}';
    }
}
