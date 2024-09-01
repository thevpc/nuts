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
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.io.urlpart.URLPart;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.util.NMsg;


import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public final class CoreServiceUtils {

    private CoreServiceUtils() {
    }

    private static Set<String> loadZipServiceClassNamesFromJarStream(InputStream jarStream, Class service, NSession session) {
        LinkedHashSet<String> found = new LinkedHashSet<>();
        if (jarStream != null) {
            ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
                if (path.equals("META-INF/services/" + service.getName())) {
                    try (Reader reader = new InputStreamReader(inputStream)) {
                        found.addAll(CoreIOUtils.confLines(reader, session).map(String::trim).collect(Collectors.toSet()));
                    } catch (IOException ex) {
                        throw new NIOException(session, ex);
                    }
                    return NVisitResult.TERMINATE;
                }
                return NVisitResult.CONTINUE;
            }, session);
        }
        return found;
    }

    public static Set<String> loadZipServiceClassNamesFromClassLoader(ClassLoader classLoader, Class service, NSession session) {
        LinkedHashSet<String> found = new LinkedHashSet<>();
        try {
            List<URL> found2 = NCollections.list(classLoader.getResources("META-INF/services/" + service.getName()));
            for (URL url : found2) {
                try (Reader reader = new InputStreamReader(url.openStream())) {
                    found.addAll(
                            CoreIOUtils.confLines(reader, session).map(String::trim).collect(Collectors.toSet())
                    );
                } catch (IOException ex) {
                    throw new NIOException(session, ex);
                }
            }
        }catch (IOException ex){
            throw new NIOException(session, ex);
        }
        return found;
    }

    private static Set<String> loadZipServiceClassNamesFromFolder(File file, Class service, NSession session) {
        LinkedHashSet<String> found = new LinkedHashSet<>();
        File dir = new File(file, "META-INF/services/");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().equals(service.getName())) {
                    try (Reader reader = new FileReader(f)) {
                        return CoreIOUtils.confLines(reader, session).map(String::trim).collect(Collectors.toSet());
                    } catch (IOException ex) {
                        throw new NIOException(session, ex);
                    }
                }
            }
        }
        return found;
    }

    public static Set<String> loadZipServiceClassNames(URL url, Class service, NSession session) {
        LinkedHashSet<String> found = new LinkedHashSet<>();
        URLPart lastPart = URLPart.of(url);
        File file = lastPart.getFile().orNull();
        if (file!=null) {
            if (file.isDirectory()) {
                return loadZipServiceClassNamesFromFolder(file, service, session);
            } else if (file.isFile()) {
                try (final InputStream jarStream = new FileInputStream(file)) {
                    return loadZipServiceClassNamesFromJarStream(jarStream, service, session);
                } catch (IOException ex) {
                    throw new NIOException(session, ex);
                }
            }
        } else {
            try (final InputStream jarStream = url.openStream()) {
                return loadZipServiceClassNamesFromJarStream(jarStream, service, session);
            } catch (IOException ex) {
                throw new NIOException(session, ex);
            }
        }
        return found;
    }

    public static List<String> loadServiceClassNames(URL u, Class<?> service, NSession session) {

        try (InputStreamReader ir = new InputStreamReader(CoreIOUtils.openStream(u).get(), StandardCharsets.UTF_8)) {
            return CoreIOUtils.confLines(ir, session).map(String::trim).collect(Collectors.toList());
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

    public static List<Class> loadServiceClasses(Class service, ClassLoader classLoader, NSession session) {
        String fullName = "META-INF/services/" + service.getName();
        Enumeration<URL> configs;
        LinkedHashSet<String> names = new LinkedHashSet<>();
        try {
            if (classLoader == null) {
                configs = ClassLoader.getSystemResources(fullName);
            } else {
                configs = classLoader.getResources(fullName);
            }
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        while (configs.hasMoreElements()) {
            names.addAll(loadServiceClassNames(configs.nextElement(), service, session));
        }
        List<Class> classes = new ArrayList<>();
        for (String n : names) {
            Class<?> c = null;
            try {
                c = Class.forName(n, false, classLoader);
            } catch (ClassNotFoundException x) {
                throw new NException(session, NMsg.ofC("unable to load service class %s", n), x);
            }
            if (!service.isAssignableFrom(c)) {
                throw new NException(session,
                        NMsg.ofC("not a valid type %s <> %s", c, service));
            }
            classes.add(c);
        }
        return classes;
    }

}
