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
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author vpc
 */
public class URLBuilder {

    private String prefix;
    private String base;
    private String query;

    private URLBuilder(String prefix, String base, String query) {
        this.prefix = prefix;
        this.base = base;
        this.query = query;
    }

    public URLBuilder(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            int i1 = path.indexOf("://");
            base = path.substring(i1);
            int i2 = base.indexOf('?');
            if (i2 >= 0) {
                query = base.substring(i2 + 1);
                base = base.substring(0, i2);
            }
            prefix = path.substring(0, i1 + 3);
        } else {
            base = path;
        }
    }

    public String getName() {
        return new File(base).getName();
    }

    public URLBuilder getParent() {
        String p = new File(base).getParent();
        if (p == null) {
            p = "/";
        }
        return new URLBuilder(prefix, p, query);
    }

    public URLBuilder resolve(String newName) {
        return new URLBuilder(prefix, base + "/" + newName, query);
    }

    public URLBuilder resolveSibling(String newName) {
        return getParent().resolve(newName);
    }

//    public URLBuilder removeQuery(String newName) {
//        throw new IllegalArgumentException("No Parent Implemented");
//    }
//
//    public URLBuilder addQuery(String newName) {
//        throw new IllegalArgumentException("No Parent Implemented");
//    }
    public URL toURL() {
        try {
            if (prefix != null) {
                return new URL(prefix + base + (query == null ? "" : ("?" + query)));
            } else {
                return new File(base).toURI().toURL();
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
