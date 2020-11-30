/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple Implementation of Nuts BootClassLoader
 * %category SPI Base
 */
class NutsBootClassLoader extends URLClassLoader {

    /**
     * default constructor
     * @param urls urls
     * @param parent parent class loader
     */
    NutsBootClassLoader(NutsBootDependencyNode[] urls, ClassLoader parent) {
        super(new URL[0], parent);
        LinkedHashSet<URL> all=new LinkedHashSet<>();
        for (NutsBootDependencyNode url : urls) {
            addURL(url,all);
        }
        for (URL url : all) {
            super.addURL(url);
        }
    }

    protected void addURL(NutsBootDependencyNode ids, Set<URL> urls) {
        urls.add(ids.getURL());
        for (NutsBootDependencyNode dependency : ids.getDependencies()) {
            addURL(dependency,urls);
        }
    }

    @Override
    protected void addURL(URL url) {
        for (URL u : getURLs()) {
            if(u.equals(url)){
                return;
            }
        }
        super.addURL(url);
    }

    static class IdInfoBuilder{
        private String id;
        private URL url;
        private List<NutsBootDependencyNode> dependencies=new ArrayList<>();

        public String getId() {
            return id;
        }

        public IdInfoBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public URL getUrl() {
            return url;
        }

        public IdInfoBuilder setUrl(URL url) {
            this.url = url;
            return this;
        }

        public List<NutsBootDependencyNode> getDependencies() {
            return dependencies;
        }

        public IdInfoBuilder addDependency(NutsBootDependencyNode other) {
            this.dependencies.add(other);
            return this;
        }
        public IdInfoBuilder setDependencies(List<NutsBootDependencyNode> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public NutsBootDependencyNode build(){
            return new NutsBootDependencyNode(
                    id, url,dependencies.toArray(new NutsBootDependencyNode[0])
            );
        }
    }
}
