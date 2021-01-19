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
package net.thevpc.nuts.runtime.bundles.mvn;

import java.io.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author thevpc
 */
class URLParts {

    URLPart[] values;

    public URLParts(URL r) {
        this(r.toString());
    }

    public URLParts(String r) {
        if (r.startsWith("file:")) {
            values = new URLPart[]{new URLPart("file", r)};
        } else if (r.startsWith("http:") || r.startsWith("https:") || r.startsWith("ftp:")) {
            values = new URLPart[]{new URLPart("web", r)};
        } else if (r.startsWith("jar:")) {
            final String r2 = r.substring(4);
            int x = r2.indexOf('!');
            List<URLPart> rr = new ArrayList<>();
            rr.add(new URLPart("jar", r2.substring(0, x)));
            for (URLPart pathItem : new URLParts(r2.substring(x + 1)).values) {
                rr.add(pathItem);
            }
            values = rr.toArray(new URLPart[0]);
        } else if (r.startsWith("/")) {
            values = new URLPart[]{new URLPart("/", r.substring(1))};
        } else {
            throw new UnsupportedOperationException("unsupported protocol " + r);
        }
    }

    public URLParts(URLPart... values) {
        this.values = values;
    }

    public String getName() {
        return getLastPart().getName();
    }

    public URLPart getLastPart() {
        if (values.length == 0) {
            return null;
        }
        return values[values.length - 1];
    }

    public URLParts getParent() {
        URLPart[] values2 = new URLPart[values.length - 1];
        System.arraycopy(values, 0, values2, 0, values2.length);
        return new URLParts(values2);
    }

    public URLParts append(URLParts a) {
        List<URLPart> all = new ArrayList<>(Arrays.asList(this.values));
        all.addAll(Arrays.asList(a.values));
        return new URLParts(all.toArray(new URLPart[0]));
    }

    public URLParts append(String path) {
        return append(new URLParts(path));
    }

    public URLParts append(URLPart a) {
        List<URLPart> all = new ArrayList<>(Arrays.asList(this.values));
        all.add(a);
        return new URLParts(all.toArray(new URLPart[0]));
    }

    public URLParts appendHead(URLPart a) {
        List<URLPart> all = new ArrayList<>();
        all.add(a);
        all.addAll(Arrays.asList(this.values));
        return new URLParts(all.toArray(new URLPart[0]));
    }

    public URL[] getChildren(boolean includeFolders, boolean deep, final URLFilter filter) throws IOException {
        Object parent = null;
        for (int i = 0; i < values.length; i++) {
            URLPart value = values[i];
            switch (value.getType()) {
                case "/": {
                    if (parent == null) {
                        parent = new File("/" + value.getPath());
                    } else if (parent instanceof File) {
                        File f2 = new File((File) parent, value.getPath());
                        if (i == values.length - 1) {
                            final File[] listFiles = f2.listFiles();
                            if (listFiles == null) {
                                return new URL[0];
                            }
                            URL[] found = new URL[listFiles.length];
                            for (int j = 0; j < found.length; j++) {
                                found[j] = (listFiles[i]).toURI().toURL();
                            }
                            return found;
                        } else {
                            throw new UnsupportedOperationException("unsupported");
                        }
                    } else if (parent instanceof URL) {
                        final URL uu = (URL) parent;
                        JarURLConnection urlcon = (JarURLConnection) (uu.openConnection());
                        List<URL> ff = new ArrayList<>();
                        try (final JarFile jar = urlcon.getJarFile()) {
                            Enumeration<JarEntry> entries = jar.entries();
                            String pv = value.getPath();
                            if (!pv.endsWith("/")) {
                                pv += "/";
                            }
                            while (entries.hasMoreElements()) {
                                String entry = entries.nextElement().getName();
                                //                                    System.out.println(entry);
                                if (entry.startsWith(pv)) {
                                    String y = entry.substring(pv.length());
                                    if (y.endsWith("/")) {
                                        y = y.substring(0, y.length() - 1);
                                    }
                                    if (deep || y.indexOf('/') < 0) {
                                        if (y.length() > 0) {
                                            if (includeFolders || !entry.endsWith("/")) {
                                                StringBuilder s = new StringBuilder();
                                                s.append(uu.toString());
                                                s.append(entry);
                                                final URL uuu = new URL(s.toString());
                                                if (filter == null || filter.accept(uuu)) {
                                                    ff.add(uuu);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (i == values.length - 1) {
                            return ff.toArray(new URL[0]);
                        }
                        parent = ff.get(0);
                    } else {
                        throw new UnsupportedOperationException( "unsupported");
                    }
                }
                case "file": {
                    if (parent == null) {
                        File f2 = new File((File) parent, value.getPath());
                        if (i == values.length - 1) {
                            final File[] listFiles = f2.listFiles(new FileFilter() {
                                @Override
                                public boolean accept(File pathname) {
                                    try {
                                        return filter == null || filter.accept(pathname.toURI().toURL());
                                    } catch (MalformedURLException ex) {
                                        return false;
                                    }
                                }
                            });
                            if (listFiles == null) {
                                return new URL[0];
                            }
                            URL[] found = new URL[listFiles.length];
                            for (int j = 0; j < found.length; j++) {
                                found[j] = (listFiles[i]).toURI().toURL();
                            }
                            return found;
                        } else {
                            throw new UnsupportedOperationException( "unsupported");
                        }
                    } else {
                        throw new UnsupportedOperationException("unsupported");
                    }
                }
                case "jar": {
                    if (parent == null) {
                        parent = new URL("jar:" + value.getPath() + "!/");
                    } else {
                        throw new IllegalArgumentException("unsupported");
                    }
                }
            }
        }
        throw new UnsupportedOperationException( "unsupported");
    }

    public InputStream getInputStream() throws IOException {
        Object parent = null;
        for (URLPart value : values) {
            switch (value.getType()) {
                case "/": {
                    if (parent == null) {
                        parent = new File(value.getPath());
                    } else {
                        throw new IllegalArgumentException( "unsupported");
                    }
                }
                case "file": {
                    if (parent == null) {
                        parent = new URL(value.getPath());
                    } else {
                        throw new UnsupportedOperationException( "unsupported");
                    }
                }
                case "jar": {
                    if (parent == null) {
                        parent = new URL(value.getPath()).openStream();
                    } else {
                        throw new UnsupportedOperationException( "unsupported");
                    }
                }
            }
        }
        if (parent instanceof File) {
            return new FileInputStream((File) parent);
        }
        if (parent instanceof URL) {
            return ((URL) parent).openStream();
        }
        throw new UnsupportedOperationException( "unsupported");
    }

}
