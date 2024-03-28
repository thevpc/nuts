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
package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author thevpc
 */
public class URLParts {

    URLPart[] values;

    public URLParts(URL r) {
        this(r.toString());
    }

    public URLParts(String r) {
        if (r.startsWith("file:")) {
            try {
                values = new URLPart[]{new URLPart(URLPart.Type.URL_FILE, Paths.get(new URI(r)).toFile().getPath())};
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else if (r.startsWith("http:") || r.startsWith("https:") || r.startsWith("ftp:")) {
            values = new URLPart[]{new URLPart(URLPart.Type.WEB, r)};
        } else if (r.startsWith("jar:")) {
            final String r2 = r.substring(4);
            int x = r2.indexOf('!');
            List<URLPart> rr = new ArrayList<>();
            rr.add(new URLPart(URLPart.Type.JAR, r2.substring(0, x)));
            for (URLPart pathItem : new URLParts(r2.substring(x + 1)).values) {
                rr.add(pathItem);
            }
            values = rr.toArray(new URLPart[0]);
        } else if (
                r.startsWith("/")
                        || (r.length() >= 2 && Character.isAlphabetic(r.charAt(0)) && r.charAt(1) == ':')
        ) {
            values = new URLPart[]{new URLPart(URLPart.Type.FS_FILE, r)};
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

    public URLPart[] getParts() {
        return values;
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

    private URL[] searchURL(URL root, URLPart base,boolean includeFolders,boolean deep, final Predicate<URL> filter) throws IOException {
        JarURLConnection urlcon = (JarURLConnection) (root.openConnection());
        List<URL> ff = new ArrayList<>();
        try (final JarFile jar = urlcon.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            String pv = base.getPath();
            if (pv.startsWith("/")) {
                // zip entries does not start with '/'
                pv = pv.substring(1);
            }
            if (!pv.endsWith("/")) {
                pv += "/";
            }
            while (entries.hasMoreElements()) {
                String entry = entries.nextElement().getName();
                if (entry.startsWith(pv)) {
                    String y = entry.substring(pv.length());
                    if (y.endsWith("/")) {
                        y = y.substring(0, y.length() - 1);
                    }
                    if (deep || y.indexOf('/') < 0) {
                        if (y.length() > 0) {
                            if (includeFolders || !entry.endsWith("/")) {
                                StringBuilder s = new StringBuilder();
                                s.append(root.toString());
                                s.append(entry);
                                final URL uuu = new URL(s.toString());
                                if (filter == null || filter.test(uuu)) {
                                    ff.add(uuu);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ff.toArray(new URL[0]);
    }

    private URL[] searchFile(File root, boolean includeFolders, boolean deep, final Predicate<URL> filter) throws IOException {
        if (deep) {
            List<URL> found = new ArrayList<>();
            Files.walkFileTree(
                    root.toPath(),
                    new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if (includeFolders) {
                                URL url = dir.toFile().toURI().toURL();
                                if (filter == null) {
                                    found.add(url);
                                } else {
                                    if (filter.test(url)) {
                                        found.add(url);
                                    }
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            URL url = file.toFile().toURI().toURL();
                            if (filter == null) {
                                found.add(url);
                            } else {
                                if (filter.test(url)) {
                                    found.add(url);
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
            return found.toArray(new URL[0]);
        } else {
            final File[] listFiles = root.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile() || (includeFolders && pathname.isDirectory())) {
                        try {
                            return filter == null || filter.test(pathname.toURI().toURL());
                        } catch (MalformedURLException ex) {
                            return false;
                        }
                    }
                    return false;
                }
            });
            if (listFiles == null) {
                return new URL[0];
            }
            URL[] found = new URL[listFiles.length];
            for (int j = 0; j < found.length; j++) {
                found[j] = (listFiles[j]).toURI().toURL();
            }
            return found;
        }
    }

    public URL[] getChildren(boolean includeFolders, boolean deep, final Predicate<URL> filter) throws IOException {
        Object parent = null;
        for (int i = 0; i < values.length; i++) {
            URLPart value = values[i];
            switch (value.getType()) {
                case FS_FILE: {
                    if (parent == null) {
                        parent = new File(value.getPath());
                        if (i == values.length - 1) {
                            File f2 = ((File) parent);
                            return searchFile(f2, includeFolders,deep, filter);
                        }
                    } else if (parent instanceof File) {
                        File f2 = new File((File) parent, value.getPath());
                        if (i == values.length - 1) {
                            return searchFile(f2, deep, includeFolders,filter);
                        } else {
                            throw new UnsupportedOperationException("unsupported");
                        }
                    } else if (parent instanceof URL) {
                        final URL uu = (URL) parent;
                        URL[] ff=searchURL(uu, value, includeFolders, deep, filter);
                        if (i == values.length - 1) {
                            return ff;
                        }
                        if(ff.length>0) {
                            parent = ff[0];
                        }else {
                            return ff;
                        }
                    } else {
                        throw new UnsupportedOperationException("unsupported");
                    }
                    break;
                }
                case URL_FILE: {
                    if (parent == null) {
                        File f2 = new File((File) parent, value.getPath());
                        if (i == values.length - 1) {
                            return searchFile(f2,includeFolders, deep, filter);
                        } else {
                            throw new UnsupportedOperationException("unsupported");
                        }
                    } else {
                        throw new UnsupportedOperationException("unsupported");
                    }
                }
                case JAR: {
                    if (parent == null) {
                        parent = new URL("jar:" + value.getPath() + "!/");
                    } else {
                        throw new IllegalArgumentException("unsupported");
                    }
                    break;
                }
                case URL:
                case WEB: {
                    if (parent == null) {
                        parent = new URL(value.getPath());
                    } else {
                        throw new IllegalArgumentException("unsupported");
                    }
                }
            }
        }
        throw new UnsupportedOperationException("unsupported");
    }

    public InputStream getInputStream() throws IOException {
        Object parent = null;
        for (URLPart value : values) {
            switch (value.getType()) {
                case FS_FILE: {
                    if (parent == null) {
                        parent = new File(value.getPath());
                    } else {
                        throw new IllegalArgumentException("unsupported");
                    }
                }
                case URL_FILE: {
                    if (parent == null) {
                        parent = new URL(value.getPath());
                    } else {
                        throw new UnsupportedOperationException("unsupported");
                    }
                }
                case JAR: {
                    if (parent == null) {
                        parent = new URL(value.getPath()).openStream();
                    } else {
                        throw new UnsupportedOperationException("unsupported");
                    }
                }
                case URL:
                case WEB: {
                    if (parent == null) {
                        parent = new URL(value.getPath());
                    } else {
                        throw new UnsupportedOperationException("unsupported");
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
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public String toString() {
        return "URLParts{" +
                "values=" + Arrays.toString(values) +
                '}';
    }
}
