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
package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.NutsException;
import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.bundles.io.InputStreamVisitor;
import net.thevpc.nuts.runtime.bundles.io.ZipUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author vpc
 */
public final class CoreServiceUtils {

    private CoreServiceUtils() {
    }

    public static Set<String> loadZipServiceClassNames(URL url, Class service,NutsSession session) {
        LinkedHashSet<String> found = new LinkedHashSet<>();
        try (final InputStream jarStream = url.openStream()) {
            if (jarStream != null) {
                ZipUtils.visitZipStream(jarStream, s -> s.equals("META-INF/services/" + service.getName()), new InputStreamVisitor() {
                    @Override
                    public boolean visit(String path, InputStream inputStream) throws IOException {
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
                });
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        return found;
    }

    public static List<String> loadServiceClasseNames(URL u, Class<?> service,NutsSession session) {
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
            throw new NutsIOException(session,ex);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex2) {
                throw new NutsIOException(session, ex2);
            }
        }
        return names;
    }

    public static List<Class> loadServiceClasses(Class service, ClassLoader classLoader, NutsSession session) {
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
            throw new NutsIOException(session,ex);
        }
        while (configs.hasMoreElements()) {
            names.addAll(loadServiceClasseNames(configs.nextElement(), service,session));
        }
        List<Class> classes = new ArrayList<>();
        for (String n : names) {
            Class<?> c = null;
            try {
                c = Class.forName(n, false, classLoader);
            } catch (ClassNotFoundException x) {
                throw new NutsException(session, NutsMessage.cstyle("unable to load service class %s", n), x);
            }
            if (!service.isAssignableFrom(c)) {
                throw new NutsException(session,
                        NutsMessage.cstyle("not a valid type %s <> %s", c, service));
            }
            classes.add(c);
        }
        return classes;
    }

}
