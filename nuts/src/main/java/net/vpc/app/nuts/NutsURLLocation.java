/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.net.URL;

/**
 * Convenient class to hold information about base path and expanded url
 *
 * @author vpc
 * @since 0.5.4
 */
public class NutsURLLocation {

    private final String path;
    private final URL url;

    /**
     * sole constructor
     *
     * @param path url path
     * @param url url
     */
    public NutsURLLocation(String path, URL url) {
        this.path = path;
        this.url = url;
    }

    /**
     * base path
     *
     * @return base path
     */
    public String getPath() {
        return path;
    }

    /**
     * expanded url
     *
     * @return expanded url
     */
    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "NutsURLLocation{" + "path=" + path + ", url=" + url + '}';
    }

}
