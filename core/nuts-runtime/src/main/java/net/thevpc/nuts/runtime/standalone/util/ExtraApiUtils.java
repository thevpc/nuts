package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.runtime.standalone.io.NCoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.NReservedMavenUtils;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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

public class ExtraApiUtils {
    public static String resolveNutsVersionFromClassPath(NLog bLog) {
        return NReservedMavenUtils.resolveNutsApiVersionFromClassPath(bLog);
    }

    public static String resolveNutsIdDigestOrError() {
        String d = resolveNutsIdDigest();
        if (d == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL[] urls = resolveClasspathURLs(cl, true);
            throw new NIllegalArgumentException(NMsg.ofPlain("unable to detect nuts digest. Most likely you are missing valid compilation of nuts." + "\n\t 'pom.properties' could not be resolved and hence, we are unable to resolve nuts version." + "\n\t java=" + System.getProperty("java.home") + " as " + System.getProperty("java.version") + "\n\t class-path=" + System.getProperty("java.class.path") + "\n\t urls=" + Arrays.toString(urls) + "\n\t class-loader=" + cl.getClass().getName() + " as " + cl));
        }
        return d;

    }

    public static String resolveNutsIdDigest() {
        //TODO COMMIT TO 0.8.4
        return resolveNutsIdDigest(NId.getApi(Nuts.getVersion()).get(), resolveClasspathURLs(Nuts.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NId id, URL[] urls) {
        return NCoreIOUtils.getURLDigest(findClassLoaderJar(id, urls), null);
    }

    public static boolean asBoolean(Boolean value) {
        return asBooleanOr(value, false);
    }

    public static boolean asBooleanOr(Boolean value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.booleanValue();
    }

//    public static URL findClassLoaderJar(NId id, URL[] urls) {
//        return findClassLoaderJar(id, urls);
//    }

    public static URL findClassLoaderJar(NId id, URL[] urls) {
        for (URL url : urls) {
            NId[] nutsBootIds = NReservedMavenUtils.resolveJarIds(url);
            for (NId i : nutsBootIds) {
                if (NBlankable.isBlank(id.getGroupId()) || i.getGroupId().equals(id.getGroupId())) {
                    if (NBlankable.isBlank(id.getArtifactId()) || i.getArtifactId().equals(id.getArtifactId())) {
                        if (NBlankable.isBlank(id.getVersion()) || i.getVersion().equals(id.getVersion())) {
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
                                all.add(CoreIOUtils.urlOf(jar));
                            }
                        } else {
                            //ignore any other loading url format!
                        }
                    }
                } catch (IOException | UncheckedIOException ex) {
                    //ignore...
                }
            }
        }
        //Thread.currentThread().getContextClassLoader()
        return all.toArray(new URL[0]);
    }

    public static boolean isLoadedClassPath(URL url, ClassLoader contextClassLoader,
                                            NLog bLog) {
        try {
            if (url != null) {
                if (contextClassLoader == null) {
                    return false;
                }
                File file = NCoreIOUtils.toFile(url);
                if (file == null) {
                    throw new NIllegalArgumentException(NMsg.ofC("unsupported classpath item; expected a file path: %s", url));
                }
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (!zname.endsWith("/") && zname.endsWith(".class") && !zname.contains("$")) {
                            if (NReservedUtils.isInfiniteLoopThread(NReservedLangUtils.class.getName(), "isLoadedClassPath")) {
                                return false;
                            }
                            URL incp = contextClassLoader.getResource(zname);
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            if (incp != null) {
                                bLog.log(NMsg.ofC("url %s is already in classpath. checked class %s successfully",
                                        url, clz).asFinest().withIntent(NMsgIntent.SUCCESS));
                                return true;
                            } else {
                                bLog.log(NMsg.ofC("url %s is not in classpath. failed to check class %s",
                                        url, clz).asFinest().withIntent(NMsgIntent.INFO));
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
        bLog.log(NMsg.ofC("url %s is not in classpath. no class found to check", url).asFinestFail());
        return false;
    }

    private static void fillBootDependencyNodes(NClassLoaderNode node, Set<URL> urls, Set<String> visitedIds,
                                                NLog bLog) {
        if (node.getId() == null) {
            if (!node.isIncludedInClasspath()) {
                urls.add(node.getURL());
            } else {
                bLog.log(NMsg.ofC("url will not be loaded (already in classloader) : %s", node.getURL()).asWarning().withIntent(NMsgIntent.CACHE));
            }
            for (NClassLoaderNode dependency : node.getDependencies()) {
                fillBootDependencyNodes(dependency, urls, visitedIds, bLog);
            }
            return;
        } else {
            String shortName = node.getId().getShortName();
            if (!visitedIds.contains(shortName)) {
                visitedIds.add(shortName);
                if (!node.isIncludedInClasspath()) {
                    urls.add(node.getURL());
                } else {
                    bLog.log(NMsg.ofC("url will not be loaded (already in classloader) : %s", node.getURL()).asWarning().withIntent(NMsgIntent.CACHE));
                }
                for (NClassLoaderNode dependency : node.getDependencies()) {
                    fillBootDependencyNodes(dependency, urls, visitedIds, bLog);
                }
            }
        }
    }

    public static URL[] resolveClassWorldURLs(NClassLoaderNode[] nodes, ClassLoader contextClassLoader,
                                              NLog bLog) {
        LinkedHashSet<URL> urls = new LinkedHashSet<>();
        Set<String> visitedIds = new HashSet<>();
        for (NClassLoaderNode info : nodes) {
            if (info != null) {
                fillBootDependencyNodes(info, urls, visitedIds, bLog);
            }
        }
        return urls.toArray(new URL[0]);
    }


}
