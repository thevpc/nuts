/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
package net.thevpc.nuts.boot;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Loaded Package Information used to Boot
 *
 * @author thevpc
 * @app.category Internal
 */
public class NBootClassLoaderNodeBuilder {

    private String id;
    private URL url;
    private boolean enabled;
    private boolean includedInClasspath;
    private List<NBootClassLoaderNode> dependencies = new ArrayList<>();

    public NBootClassLoaderNodeBuilder() {
    }

    public NBootClassLoaderNodeBuilder copyFrom(NBootClassLoaderNode o) {
        if (o != null) {
            id = o.getId();
            enabled = o.isEnabled();
            includedInClasspath = o.isIncludedInClasspath();
            url = o.getURL();
            dependencies.clear();
            if (o.getDependencies() != null) {
                for (NBootClassLoaderNode dependency : o.getDependencies()) {
                    addDependency(dependency);
                }
            }
        }
        return this;
    }

    public NBootClassLoaderNodeBuilder copyFrom(NBootClassLoaderNodeBuilder o) {
        if (o != null) {
            id = o.getId();
            enabled = o.isEnabled();
            includedInClasspath = o.isIncludedInClasspath();
            url = o.getURL();
            dependencies.clear();
            if (o.getDependencies() != null) {
                for (NBootClassLoaderNode dependency : o.getDependencies()) {
                    addDependency(dependency);
                }
            }
        }
        return this;
    }

    public boolean isIncludedInClasspath() {
        return includedInClasspath;
    }

    public NBootClassLoaderNodeBuilder setIncludedInClasspath(boolean includedInClasspath) {
        this.includedInClasspath = includedInClasspath;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NBootClassLoaderNodeBuilder setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getId() {
        return id;
    }

    public NBootClassLoaderNodeBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public URL getURL() {
        return url;
    }

    public NBootClassLoaderNodeBuilder setUrl(URL url) {
        this.url = url;
        return this;
    }

    public List<NBootClassLoaderNode> getDependencies() {
        return dependencies;
    }

    public NBootClassLoaderNodeBuilder setDependencies(List<NBootClassLoaderNode> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public NBootClassLoaderNodeBuilder addDependency(NBootClassLoaderNode other) {
        this.dependencies.add(other);
        return this;
    }

    public NBootClassLoaderNode build() {
        return new NBootClassLoaderNode(id, url, enabled, includedInClasspath, dependencies.toArray(new NBootClassLoaderNode[0]));
    }

}
