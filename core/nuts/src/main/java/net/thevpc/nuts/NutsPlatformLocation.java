/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
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

import java.util.Objects;

/**
 * SDK location
 *
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NutsPlatformLocation extends NutsConfigItem {

    public static final long serialVersionUID = 3;
    private final NutsId id;
    private final NutsPlatformFamily platformType;
    private final String name;
    private final String packaging;
    private final String product;
    private final String path;
    private final String version;
    private final int priority;

    /**
     * default constructor
     *
     * @param id        id
     * @param product   SDK product. In java this is Oracle JDK or OpenJDK.
     * @param name      SDK name
     * @param path      SDK path
     * @param version   SDK version
     * @param packaging SDK packaging. for Java SDK this is room to set JRE or JDK.
     * @param priority  SDK priority
     */
    public NutsPlatformLocation(NutsId id, String product, String name, String path, String version, String packaging, int priority) {
        this.id = id;
        this.platformType = (id == null || NutsBlankable.isBlank(id.getArtifactId())) ? NutsPlatformFamily.JAVA :
                NutsPlatformFamily.parse(id.getArtifactId()).orElse(NutsPlatformFamily.UNKNOWN);
        this.product = product;
        this.name = name;
        this.path = path;
        this.version = version;
        this.packaging = packaging;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public NutsPlatformLocation setPriority(int priority) {
        return new NutsPlatformLocation(id, product, name, path, version, packaging, priority);
    }

    public NutsId getId() {
        return id;
    }

    public NutsPlatformFamily getPlatformType() {
        return platformType;
    }

    /**
     * SDK product. In java this is
     * Oracle JDK or OpenJDK.
     *
     * @return product name
     */
    public String getProduct() {
        return product;
    }

    /**
     * SDK version
     *
     * @return SDK version
     */
    public String getVersion() {
        return version;
    }

    /**
     * sdk name
     *
     * @return sdk name
     */
    public String getName() {
        return name;
    }

    /**
     * sdk path
     *
     * @return sdk path
     */
    public String getPath() {
        return path;
    }

    /**
     * sdk packaging. for Java SDK this
     * is room to set JRE or JDK.
     *
     * @return packaging name
     */
    public String getPackaging() {
        return packaging;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, packaging, product, path, version, priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsPlatformLocation that = (NutsPlatformLocation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(packaging, that.packaging) &&
                Objects.equals(product, that.product) &&
                Objects.equals(path, that.path) &&
                Objects.equals(version, that.version) &&
                Objects.equals(priority, that.priority)
                ;
    }

    @Override
    public String toString() {
        return "NutsPlatformLocation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", packaging='" + packaging + '\'' +
                ", product='" + product + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", priority=" + priority +
                '}';
    }
}
