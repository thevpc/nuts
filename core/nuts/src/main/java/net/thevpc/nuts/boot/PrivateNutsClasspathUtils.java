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

class PrivateNutsClasspathUtils {
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
                    LOG.log(Level.WARNING, NutsLogVerb.CACHE, "url will not be loaded (already in classloader) : {0}", new Object[]{url0});
                } else {
                    urls.add(url0);
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static URL findClassLoaderJar(NutsBootId id, URL[] urls) {
        for (URL url : urls) {
            NutsBootId[] nutsBootIds = PrivateNutsMavenUtils.resolveJarIds(url);
            for (NutsBootId i : nutsBootIds) {
                if (NutsUtilStrings.isBlank(id.getGroupId()) || i.getGroupId().equals(id.getGroupId())) {
                    if (NutsUtilStrings.isBlank(id.getArtifactId()) || i.getArtifactId().equals(id.getArtifactId())) {
                        if (NutsUtilStrings.isBlank(id.getVersionString()) || i.getVersion().toString().equals(id.getVersionString())) {
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
            }
        }
        //Thread.currentThread().getContextClassLoader()
        return all.toArray(new URL[0]);
    }

    private static boolean isLoadedClassPath(URL url, ClassLoader contextClassLoader, PrivateNutsLog LOG) {
        try {
            if (url != null) {
                File file = PrivateNutsIOUtils.toFile(url);
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
                        if (!zname.endsWith("/") && zname.endsWith(".class")) {
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            try {
                                if (PrivateNutsUtils.isInfiniteLoopThread(NutsBootWorkspace.class.getName(), "isLoadedClassPath")) {
                                    return false;
                                }
                                if (contextClassLoader == null) {
                                    return false;
                                }
                                Class<?> aClass = contextClassLoader.loadClass(clz);
                                LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "class {0} loaded successfully from {1}", new Object[]{aClass, file});
                                return true;
                            } catch (ClassNotFoundException e) {
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
            return false;
        }
        return false;
    }
}
