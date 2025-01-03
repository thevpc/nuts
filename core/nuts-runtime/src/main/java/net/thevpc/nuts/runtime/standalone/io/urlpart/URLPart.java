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
 *
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.urlpart;

import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.util.NOptional;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public abstract class URLPart {

    protected URLPart parent;

    protected Type type;
    protected String path;
    protected Object obj;

    public static URLPart of(URL r) {
        return of(r.toString(), r);
    }

    public static URLPart of(File r) {
        return new URLPartFile(null,r.getPath(),r);
    }

    public static URLPart of(String r) {
        return of(r, r);
    }

    public static URLPart of(String r, Object obj) {
        if (r.startsWith("file:")) {
            try {
                File file = Paths.get(new URI(r)).toFile();
                return new URLPartFile(null, file.getPath(), file);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else if (r.startsWith("http:") || r.startsWith("https:") || r.startsWith("ftp:")) {
            return new URLPartWeb(null, r, obj);
        } else if (r.startsWith("jar:nested:")) {
            final String r2 = r.substring("jar:nested:".length());
            int x = r2.indexOf('!');
            String j = r2.substring(0, x);
            if (j.endsWith("/")) {
                j = j.substring(0, j.length() - 1);
            }
            String base0 = j;
            j = r2.substring(x + 1);
            if (j.startsWith("BOOT-INF/classes/!/")) {
                return new URLPartSpringJarNested( j.substring("BOOT-INF/classes/!/".length()),
                        (obj instanceof URL)?(URL) obj:null
                        , of(base0), "BOOT-INF/classes");
            } else {
                return new URLPartJar(j, obj, of(base0));
            }
        } else if (r.startsWith("jar:")) {
            final String r2 = r.substring(4);
            int x = r2.indexOf('!');
            return new URLPartJar(r2.substring(x + 1), obj, of(r2.substring(0, x)));
        } else if (
                r.startsWith("/")
                        || (r.length() >= 2 && Character.isAlphabetic(r.charAt(0)) && r.charAt(1) == ':')
        ) {
            return new URLPartFile(null, r, new File(r));
        } else {
            throw new UnsupportedOperationException("unsupported protocol " + r);
        }
    }

    public URLPart(URLPart parent, Type type, String path, Object obj) {
        this.type = type;
        this.path = path;
        this.parent = parent;
        this.obj = obj;
    }

    public int len() {
        if (parent == null) {
            return 1;
        }
        return 1 + parent.len();
    }

    public URLPart root() {
        if (parent == null) {
            return null;
        }
        if (parent.parent == null) {
            return parent;
        }
        return parent.root();
    }

    public abstract URLPart rootSibling(String path);

    public URLPart parent() {
        return parent;
    }

    public String getName() {
        String n = path;
        final int p = n.replace('\\', '/').lastIndexOf('/');
        if (p > 0) {
            n = path.substring(p + 1);
        }
        return n;
    }

    public Type getType() {
        return type;
    }

    public NOptional<File> getFile() {
        try {
            String path = getPath();
            if (getType() == URLPart.Type.FS_FILE) {
                return NOptional.of(new File(path));
            } else if (getType() == Type.URL_FILE) {
                try {
                    if (path.startsWith("file:")) {
                        return NOptional.of(URLPath._toFile(CoreIOUtils.urlOf(path)));
                    } else {
                        return NOptional.of(new File(path));
                    }
                } catch (Exception e) {
                    return NOptional.ofNamedEmpty("Not a file " + this);
                }
            }
        } catch (Exception ex) {
            //
        }
        return NOptional.ofNamedEmpty("Not a file " + this);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "URLPart{" + "type=" + type + ", path=" + path + '}';
    }

    public enum Type {
        WEB, FS_FILE, URL_FILE, URL, JAR, SUB_PATH
    }

    public abstract URLPart[] getChildren(boolean includeFolders, boolean deep, final Predicate<URLPart> filter) ;

    public abstract InputStream getInputStream();


}
