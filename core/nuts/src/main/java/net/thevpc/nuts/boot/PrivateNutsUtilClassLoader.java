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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class PrivateNutsUtilClassLoader {
    private static void fillBootDependencyNodes(NutsClassLoaderNode node, Set<URL> urls, Set<String> visitedIds,
                                                PrivateNutsBootLog bLog) {
        String shortName = NutsId.of(node.getId()).get().getShortName();
        if (!visitedIds.contains(shortName)) {
            visitedIds.add(shortName);
            if (!node.isIncludedInClasspath()) {
                urls.add(node.getURL());
            } else {
                bLog.log(Level.WARNING, NutsLoggerVerb.CACHE, NutsMessage.jstyle("url will not be loaded (already in classloader) : {0}", node.getURL()));
            }
            for (NutsClassLoaderNode dependency : node.getDependencies()) {
                fillBootDependencyNodes(dependency, urls, visitedIds, bLog);
            }
        }
    }

    static URL[] resolveClassWorldURLs(NutsClassLoaderNode[] nodes, ClassLoader contextClassLoader,
                                       PrivateNutsBootLog bLog) {
        LinkedHashSet<URL> urls = new LinkedHashSet<>();
        Set<String> visitedIds = new HashSet<>();
        for (NutsClassLoaderNode info : nodes) {
            fillBootDependencyNodes(info, urls, visitedIds, bLog);
        }
        return urls.toArray(new URL[0]);
    }

    public static URL findClassLoaderJar(NutsId id, URL[] urls) {
        for (URL url : urls) {
            NutsId[] nutsBootIds = PrivateNutsUtilMavenRepos.resolveJarIds(url);
            for (NutsId i : nutsBootIds) {
                if (NutsBlankable.isBlank(id.getGroupId()) || i.getGroupId().equals(id.getGroupId())) {
                    if (NutsBlankable.isBlank(id.getArtifactId()) || i.getArtifactId().equals(id.getArtifactId())) {
                        if (NutsBlankable.isBlank(id.getVersion()) || i.getVersion().equals(id.getVersion())) {
                            return url;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static URL[] resolveClasspathURLs(ClassLoader contextClassLoader, boolean includeClassPath) {
        LinkedHashSet<URL> all = new LinkedHashSet<>();
        if (includeClassPath) {
            String classPath = System.getProperty("java.class.path");
            if (classPath != null) {
                for (String s : classPath.split(System.getProperty("path.separator"))) {
                    s = s.trim();
                    if (s.length() > 0) {
                        try {
                            Path pp = Paths.get(s);
                            if (Files.exists(pp)) {
                                all.add(pp.toUri().toURL());
                            }
                        } catch (MalformedURLException e) {
                            //e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (contextClassLoader != null) {
            if (contextClassLoader instanceof URLClassLoader) {
                all.addAll(Arrays.asList(((URLClassLoader) contextClassLoader).getURLs()));
            } else {
                //open jdk 9+ uses module and AppClassLoader no longer extends URLClassLoader
                try {
                    Enumeration<URL> r = contextClassLoader.getResources("META-INF/MANIFEST.MF");
                    while (r.hasMoreElements()) {
                        URL u = r.nextElement();
                        if ("jrt".equals(u.getProtocol())) {
                            //ignore java runtime until we find a way to retrieve their content
                            // In anyways we do not think this is useful for nuts.jar file!
                        } else if ("jar".equals(u.getProtocol())) {
                            if (u.getFile().endsWith("!/META-INF/MANIFEST.MF")) {
                                String jar = u.getFile().substring(0, u.getFile().length() - "!/META-INF/MANIFEST.MF".length());
                                all.add(new URL(jar));
                            }
                        } else {
                            //ignore any other loading url format!
                        }
                    }
                } catch (IOException ex) {
                    //ignore...
                }
            }
        }
        //Thread.currentThread().getContextClassLoader()
        return all.toArray(new URL[0]);
    }

    public static boolean isLoadedClassPath(URL url, ClassLoader contextClassLoader,
                                            PrivateNutsBootLog bLog) {
        try {
            if (url != null) {
                if (contextClassLoader == null) {
                    return false;
                }
                File file = PrivateNutsUtilIO.toFile(url);
                if (file == null) {
                    throw new NutsBootException(NutsMessage.cstyle("unsupported classpath item; expected a file path: %s", url));
                }
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (!zname.endsWith("/") && zname.endsWith(".class") && !zname.contains("$")) {
                            if (PrivateNutsUtils.isInfiniteLoopThread(PrivateNutsUtilClassLoader.class.getName(), "isLoadedClassPath")) {
                                return false;
                            }
                            URL incp = contextClassLoader.getResource(zname);
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            if (incp != null) {
                                bLog.log(Level.FINEST, NutsLoggerVerb.SUCCESS, NutsMessage.jstyle("url {0} is already in classpath. checked class {1} successfully",
                                        url, clz));
                                return true;
                            } else {
                                bLog.log(Level.FINEST, NutsLoggerVerb.INFO, NutsMessage.jstyle("url {0} is not in classpath. failed to check class {1}",
                                        url, clz));
                                return false;
                            }
                        }
                    }
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            //ignore return false;
                        }
                    }
                }

            }
        } catch (IOException e) {
            //
        }
        bLog.log(Level.FINEST, NutsLoggerVerb.FAIL, NutsMessage.jstyle("url {0} is not in classpath. no class found to check", url));
        return false;
    }
}
