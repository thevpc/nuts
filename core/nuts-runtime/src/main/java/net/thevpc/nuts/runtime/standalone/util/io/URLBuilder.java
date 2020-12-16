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
package net.thevpc.nuts.runtime.standalone.util.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author thevpc
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
