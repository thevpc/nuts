/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
//package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;
//
//import java.net.*;
//import java.nio.file.*;
//import java.util.*;
//
///**
// * @author thevpc
// */
//public class URLParts {
//
//    URLPart[] values;
//
//    public URLParts(URL r) {
//        this(r.toString());
//    }
//
//    public URLParts(String r) {
//        if (r.startsWith("file:")) {
//            try {
//                values = new URLPart[]{new URLPart(URLPart.Type.URL_FILE, Paths.get(new URI(r)).toFile().getPath())};
//            } catch (URISyntaxException e) {
//                throw new RuntimeException(e);
//            }
//        } else if (r.startsWith("http:") || r.startsWith("https:") || r.startsWith("ftp:")) {
//            values = new URLPart[]{new URLPart(URLPart.Type.WEB, r)};
//        } else if (r.startsWith("jar:nested:")) {
//            final String r2 = r.substring("jar:nested:".length());
//            int x = r2.indexOf('!');
//            List<URLPart> rr = new ArrayList<>();
//            String j = r2.substring(0, x);
//            if (j.endsWith("/")) {
//                j = j.substring(0, j.length() - 1);
//            }
//            rr.add(new URLPart(URLPart.Type.JAR, j));
//            j = r2.substring(x + 1);
//            if (j.startsWith("BOOT-INF/classes/!/")) {
//                rr.add(new URLPart(URLPart.Type.FS_FILE, "BOOT-INF/classes"));
//                j = "BOOT-INF/classes/" + j.substring("BOOT-INF/classes/!/".length());
//                rr.add(new URLPart(URLPart.Type.FS_FILE, j));
//            }else {
//                rr.add(new URLPart(URLPart.Type.FS_FILE, j));
//            }
//            values = rr.toArray(new URLPart[0]);
//        } else if (r.startsWith("jar:")) {
//            final String r2 = r.substring(4);
//            int x = r2.indexOf('!');
//            List<URLPart> rr = new ArrayList<>();
//            rr.add(new URLPart(URLPart.Type.JAR, r2.substring(0, x)));
//            for (URLPart pathItem : new URLParts(r2.substring(x + 1)).values) {
//                rr.add(pathItem);
//            }
//            values = rr.toArray(new URLPart[0]);
//        } else if (
//                r.startsWith("/")
//                        || (r.length() >= 2 && Character.isAlphabetic(r.charAt(0)) && r.charAt(1) == ':')
//        ) {
//            values = new URLPart[]{new URLPart(URLPart.Type.FS_FILE, r)};
//        } else {
//            throw new UnsupportedOperationException("unsupported protocol " + r);
//        }
//    }
//
//    public URLParts(URLPart... values) {
//        this.values = values;
//    }
//
//    public String getName() {
//        return getLastPart().getName();
//    }
//
//    public URLPart getLastPart() {
//        if (values.length == 0) {
//            return null;
//        }
//        return values[values.length - 1];
//    }
//
//    public URLPart[] getParts() {
//        return values;
//    }
//
//    public URLParts getParent() {
//        URLPart[] values2 = new URLPart[values.length - 1];
//        System.arraycopy(values, 0, values2, 0, values2.length);
//        return new URLParts(values2);
//    }
//
//    public URLParts append(URLParts a) {
//        List<URLPart> all = new ArrayList<>(Arrays.asList(this.values));
//        all.addAll(Arrays.asList(a.values));
//        return new URLParts(all.toArray(new URLPart[0]));
//    }
//
//    public URLParts append(String path) {
//        if (values.length == 0) {
//            return append(new URLParts(path));
//        }
//        URLPart last = values[values.length - 1];
//        List<URLPart> old = new ArrayList<>(Arrays.asList(values));
//        switch (last.getType()) {
//            case JAR: {
//                old.add(new URLPart(URLPart.Type.FS_FILE, path));
//                return new URLParts(old.toArray(new URLPart[0]));
//            }
//            case URL: {
//                old.remove(old.size() - 1);
//                old.add(new URLPart(URLPart.Type.URL, last.getPath() + "/" + path));
//                return new URLParts(old.toArray(new URLPart[0]));
//            }
//            case URL_FILE: {
//                old.remove(old.size() - 1);
//                old.add(new URLPart(URLPart.Type.URL_FILE, last.getPath() + "/" + path));
//                return new URLParts(old.toArray(new URLPart[0]));
//            }
//            case FS_FILE: {
//                old.remove(old.size() - 1);
//                old.add(new URLPart(URLPart.Type.FS_FILE, last.getPath() + "/" + path));
//                return new URLParts(old.toArray(new URLPart[0]));
//            }
//            default: {
//                throw new IllegalArgumentException("not supported");
//            }
//        }
//    }
//
//    public URLParts append(URLPart a) {
//        List<URLPart> all = new ArrayList<>(Arrays.asList(this.values));
//        all.add(a);
//        return new URLParts(all.toArray(new URLPart[0]));
//    }
//
//    public URLParts appendHead(URLPart a) {
//        List<URLPart> all = new ArrayList<>();
//        all.add(a);
//        all.addAll(Arrays.asList(this.values));
//        return new URLParts(all.toArray(new URLPart[0]));
//    }
//
//
//    @Override
//    public String toString() {
//        return "URLParts{" +
//                "values=" + Arrays.toString(values) +
//                '}';
//    }
//}
