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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author thevpc
 */
public final class CoreServiceUtils {

    private CoreServiceUtils() {
    }

    public static Set<String> loadZipServiceClassNames(URL url, Class service, NSession session) {
        LinkedHashSet<String> found = new LinkedHashSet<>();
        try (final InputStream jarStream = url.openStream()) {
            if (jarStream != null) {
                ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
                    if(path.equals("META-INF/services/" + service.getName())) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.length() > 0 && !line.startsWith("#")) {
                                found.add(line);
                            }
                        }
                        return false;
                    }
                    return true;
                },session);
            }
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        return found;
    }

    public static List<String> loadServiceClassNames(URL u, Class<?> service, NSession session) {
        InputStream in = null;
        BufferedReader r = null;
        List<String> names = new ArrayList<>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            int lc = 1;
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    names.add(line);
                }
            }
        } catch (IOException ex) {
            throw new NIOException(session,ex);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex2) {
                throw new NIOException(session, ex2);
            }
        }
        return names;
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
            throw new NIOException(session,ex);
        }
        while (configs.hasMoreElements()) {
            names.addAll(loadServiceClassNames(configs.nextElement(), service,session));
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
