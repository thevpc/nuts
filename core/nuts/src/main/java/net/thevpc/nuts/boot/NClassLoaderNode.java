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

import net.thevpc.nuts.reserved.NReservedLangUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Loaded Package Information used to Boot
 *
 * @author thevpc
 * @app.category Internal
 */
public class NClassLoaderNode {

    private final String id;
    private final boolean includedInClasspath;
    private final URL url;
    private final boolean enabled;
    private final List<NClassLoaderNode> dependencies;

    public NClassLoaderNode(String id, URL url, boolean enabled, boolean includedInClasspath, NClassLoaderNode... dependencies) {
        this.id = id;
        this.url = url;
        this.enabled = enabled;
        this.includedInClasspath = includedInClasspath;
        this.dependencies = NReservedLangUtils.nonNullList(Arrays.asList(dependencies));
    }

    public boolean isIncludedInClasspath() {
        return includedInClasspath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getId() {
        return id;
    }

    public URL getURL() {
        return url;
    }

    public List<NClassLoaderNode> getDependencies() {
        return dependencies;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, includedInClasspath, url, enabled,dependencies);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NClassLoaderNode that = (NClassLoaderNode) o;
        return includedInClasspath == that.includedInClasspath && enabled == that.enabled && Objects.equals(id, that.id) && Objects.equals(url, that.url) && Objects.equals(dependencies, that.dependencies);
    }

    @Override
    public String toString() {
        return "NutsClassLoaderNode{" +
                "id='" + id + '\'' +
                ", loaded=" + includedInClasspath +
                ", url=" + url +
                ", enabled=" + enabled +
                ", dependencies=" + dependencies +
                '}';
    }
}
