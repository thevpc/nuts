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
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Objects;

/**
 * SDK location
 * @author vpc
 * @since 0.5.4
 */
public class NutsSdkLocation extends NutsConfigItem {

    public static final long serialVersionUID = 2;
    private final NutsId id;
    private final String name;
    private final String packaging;
    private final String product;
    private final String path;
    private final String version;

    /**
     * default constructor
     * @param id id
     * @param product sdk product. In java this is Oracle JDK or OpenJDK.
     * @param name sdk name
     * @param path sdk path
     * @param version sdk version
     * @param packaging sdk packaging. for Java SDK this is room to set JRE or JDK.
     */
    public NutsSdkLocation(NutsId id, String product, String name, String path, String version, String packaging) {
        this.id = id;
        this.product = product;
        this.name = name;
        this.path = path;
        this.version = version;
        this.packaging = packaging;
    }

    public NutsId getId() {
        return id;
    }

    /**
     * sdk product. In java this is
     * Oracle JDK or OpenJDK.
     *
     * @return product name
     */
    public String getProduct() {
        return product;
    }

    /**
     * sdk version
     * @return sdk version
     */
    public String getVersion() {
        return version;
    }

    /**
     * sdk name
     * @return sdk name
     */
    public String getName() {
        return name;
    }

    /**
     * sdk path
     * @return
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsSdkLocation that = (NutsSdkLocation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(packaging, that.packaging) &&
                Objects.equals(product, that.product) &&
                Objects.equals(path, that.path) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, packaging, product, path, version);
    }

    @Override
    public String toString() {
        return "NutsSdkLocation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", packaging='" + packaging + '\'' +
                ", product='" + product + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
