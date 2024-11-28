package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.boot.NClassLoaderNode;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.NReservedMavenUtils;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.reserved.io.NReservedIOUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

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
        return resolveNutsIdDigest(NId.ofApi(Nuts.getVersion()).get(), resolveClasspathURLs(Nuts.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NId id, URL[] urls) {
        return NReservedIOUtils.getURLDigest(findClassLoaderJar(id, urls), null);
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
                                            NLog bLog) {
        try {
            if (url != null) {
                if (contextClassLoader == null) {
                    return false;
                }
                File file = NReservedIOUtils.toFile(url);
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
                                bLog.with().level(Level.FINEST).verb(NLogVerb.SUCCESS).log( NMsg.ofC("url %s is already in classpath. checked class %s successfully",
                                        url, clz));
                                return true;
                            } else {
                                bLog.with().level(Level.FINEST).verb(NLogVerb.INFO).log( NMsg.ofC("url %s is not in classpath. failed to check class %s",
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
        bLog.with().level(Level.FINEST).verb(NLogVerb.FAIL).log( NMsg.ofC("url %s is not in classpath. no class found to check", url));
        return false;
    }

    private static void fillBootDependencyNodes(NClassLoaderNode node, Set<URL> urls, Set<String> visitedIds,
                                                NLog bLog) {
        String shortName = NId.of(node.getId()).get().getShortName();
        if (!visitedIds.contains(shortName)) {
            visitedIds.add(shortName);
            if (!node.isIncludedInClasspath()) {
                urls.add(node.getURL());
            } else {
                bLog.with().level(Level.WARNING).verb(NLogVerb.CACHE).log( NMsg.ofC("url will not be loaded (already in classloader) : %s", node.getURL()));
            }
            for (NClassLoaderNode dependency : node.getDependencies()) {
                fillBootDependencyNodes(dependency, urls, visitedIds, bLog);
            }
        }
    }

    public static URL[] resolveClassWorldURLs(NClassLoaderNode[] nodes, ClassLoader contextClassLoader,
                                              NLog bLog) {
        LinkedHashSet<URL> urls = new LinkedHashSet<>();
        Set<String> visitedIds = new HashSet<>();
        for (NClassLoaderNode info : nodes) {
            if(info!=null) {
                fillBootDependencyNodes(info, urls, visitedIds, bLog);
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static String resolveGroupIdPath(String groupId) {
        return groupId.replace('.', '/');
    }

    public static String resolveIdPath(NId id) {
        StringBuilder sb = new StringBuilder();
        sb.append(resolveGroupIdPath(id.getGroupId()));
        if (!NBlankable.isBlank(id.getArtifactId())) {
            sb.append("/");
            sb.append(id.getArtifactId());
            if (!NBlankable.isBlank(id.getVersion())) {
                sb.append("/");
                sb.append(id.getVersion());
            }
        }
        return sb.toString();
    }

    public static String resolveJarPath(NId id) {
        return resolveFilePath(id, "jar");
    }

    public static String resolveDescPath(NId id) {
        return resolveFilePath(id, "nuts");
    }

    public static String resolveNutsDescriptorPath(NId id) {
        return resolveFilePath(id, "nuts");
    }

    public static String resolveFileName(NId id, String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.getArtifactId());
        if (!id.getVersion().isBlank()) {
            sb.append("-").append(id.getVersion());
        }
        if (!NBlankable.isBlank(extension)) {
            sb.append(".").append(extension);
        }
        return sb.toString();
    }

    public static String resolveFilePath(NId id, String extension) {
        String fileName = resolveFileName(id, extension);
        return resolveIdPath(id) + '/' + fileName;
    }
}
