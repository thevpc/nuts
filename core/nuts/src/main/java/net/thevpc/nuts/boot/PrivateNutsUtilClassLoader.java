package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class PrivateNutsUtilClassLoader {
    private static void fillBootDependencyNodes(NutsClassLoaderNode node, Set<URL> urls) {
        urls.add(node.getURL());
        for (NutsClassLoaderNode dependency : node.getDependencies()) {
            fillBootDependencyNodes(dependency, urls);
        }
    }

    static URL[] resolveClassWorldURLs(NutsClassLoaderNode[] nodes, ClassLoader contextClassLoader, PrivateNutsLog LOG) {
        LinkedHashSet<URL> urls0 = new LinkedHashSet<>();
        for (NutsClassLoaderNode info : nodes) {
            fillBootDependencyNodes(info, urls0);
        }
        List<URL> urls = new ArrayList<>();
        for (URL url0 : urls0) {
            if (url0 != null) {
                if (isLoadedClassPath(url0, contextClassLoader, LOG)) {
                    LOG.log(Level.WARNING, NutsLogVerb.CACHE, NutsMessage.jstyle("url will not be loaded (already in classloader) : {0}", url0));
                } else {
                    urls.add(url0);
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static URL findClassLoaderJar(NutsBootId id, URL[] urls) {
        for (URL url : urls) {
            NutsBootId[] nutsBootIds = PrivateNutsUtilMaven.resolveJarIds(url);
            for (NutsBootId i : nutsBootIds) {
                if (NutsBlankable.isBlank(id.getGroupId()) || i.getGroupId().equals(id.getGroupId())) {
                    if (NutsBlankable.isBlank(id.getArtifactId()) || i.getArtifactId().equals(id.getArtifactId())) {
                        if (NutsBlankable.isBlank(id.getVersionString()) || i.getVersion().toString().equals(id.getVersionString())) {
                            return url;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static URL[] resolveClasspathURLs(ClassLoader contextClassLoader) {
        List<URL> all = new ArrayList<>();
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

    private static boolean isLoadedClassPath(URL url, ClassLoader contextClassLoader, PrivateNutsLog LOG) {
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
                            if (PrivateNutsUtils.isInfiniteLoopThread(NutsBootWorkspace.class.getName(), "isLoadedClassPath")) {
                                return false;
                            }
                            URL incp = contextClassLoader.getResource(zname);
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            if (incp != null) {
                                LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, NutsMessage.jstyle("url {0} is already in classpath. checked class {1} successfully",
                                        url, clz));
                                return true;
                            } else {
                                LOG.log(Level.FINEST, NutsLogVerb.INFO, NutsMessage.jstyle("url {0} is not in classpath. failed to check class {1}",
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
        LOG.log(Level.FINEST, NutsLogVerb.FAIL, NutsMessage.jstyle("url {0} is not in classpath. no class found to check", url));
        return false;
    }
}
