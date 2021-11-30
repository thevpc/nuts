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
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsClassLoaderNode;
import net.thevpc.nuts.spi.NutsBootId;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;

/**
 * Simple Implementation of Nuts BootClassLoader
 *
 * @author thevpc
 * @app.category Boot
 */
class PrivateNutsBootClassLoader extends URLClassLoader {

    private final LinkedHashMap<String, NutsClassLoaderNode> nodes = new LinkedHashMap<>();
    private final LinkedHashMap<String, NutsClassLoaderNode> effective = new LinkedHashMap<>();

    /**
     * default constructor
     *
     * @param urls   urls
     * @param parent parent class loader
     */
    PrivateNutsBootClassLoader(NutsClassLoaderNode[] urls, ClassLoader parent) {
        super(new URL[0], parent);
        for (NutsClassLoaderNode url : urls) {
            add(url);
        }
    }

    public boolean contains(NutsClassLoaderNode node, boolean deep) {
        return search(node, deep) != null;
    }

    public NutsClassLoaderNode search(NutsClassLoaderNode node, boolean deep) {
        NutsBootId ii = NutsBootId.parse(node.getId());
        String sn = ii.getShortName();
        NutsClassLoaderNode o = nodes.get(sn);
        if (o != null) {
            return o;
        }
        if (deep) {
            return effective.get(sn);
        }
        return null;
    }

    public boolean add(NutsClassLoaderNode node) {
        NutsBootId ii = NutsBootId.parse(node.getId());
        String sn = ii.getShortName();
        if (!nodes.containsKey(sn)) {
            nodes.put(sn, node);
            return add(node, true);
        }
        return false;
    }

    protected boolean add(NutsClassLoaderNode node, boolean deep) {
        String s = node.getId();
        NutsBootId ii = NutsBootId.parse(s);
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
            return false;
        }
    }

    @Override
    public String toString() {
        return "PrivateNutsBootClassLoader{" + nodes.values() + '}';
    }

}
