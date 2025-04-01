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
 *
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

import net.thevpc.nuts.NClassLoaderNode;
import net.thevpc.nuts.NId;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;

public class DefaultNClassLoader extends URLClassLoader {

    private String name;
    private LinkedHashMap<String, NClassLoaderNode> nodes = new LinkedHashMap<>();
    private LinkedHashMap<String, NClassLoaderNode> effective = new LinkedHashMap<>();
    public DefaultNClassLoader(String name, ClassLoader parent) {
        super(new URL[0], parent);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean contains(NClassLoaderNode node, boolean deep) {
        return search(node, deep) != null;
    }

    public NClassLoaderNode search(NClassLoaderNode node, boolean deep) {
        NId ii = node.getId();
        String sn = ii.getShortName();
        NClassLoaderNode o = nodes.get(sn);
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
        if(p instanceof DefaultNClassLoader){
            return ((DefaultNClassLoader) p).search(node, deep);
        }
        return null;
    }

    public boolean add(NClassLoaderNode node) {
        NId ii = node.getId();
        String sn = ii==null?null:ii.getShortName();
        if (!nodes.containsKey(sn)) {
            nodes.put(sn, node);
            return add(node, true);
        }
        return false;
    }

    protected boolean add(NClassLoaderNode node, boolean deep) {
        NId ii = node.getId();
        String sn = ii==null?null:ii.getShortName();
        if (!effective.containsKey(sn)) {
            effective.put(sn, node);
            super.addURL(node.getURL());
            if (deep) {
                for (NClassLoaderNode dependency : node.getDependencies()) {
                    add(dependency, true);
                }
            }
            return true;
        } else {
            if (deep) {
                for (NClassLoaderNode dependency : node.getDependencies()) {
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
