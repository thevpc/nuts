package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NBootException;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.boot.NClassLoaderNode;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.io.NReservedIOUtils;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class NReservedLangUtils {

    public static String getErrorMessage(Throwable ex) {
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        return m;
    }

    public static <T> NOptional<T> findThrowable(Throwable th, Class<T> type, Predicate<Throwable> filter) {
        Set<Throwable> visited = new HashSet<>();
        Stack<Throwable> stack = new Stack<>();
        if (th != null) {
            stack.push(th);
        }
        while (!stack.isEmpty()) {
            Throwable a = stack.pop();
            if (visited.add(a)) {
                if (type.isAssignableFrom(th.getClass())) {
                    if (filter == null || filter.test(th)) {
                        return NOptional.of((T) th);
                    }
                }
                Throwable c = th.getCause();
                if (c != null) {
                    stack.add(c);
                }
                if (th instanceof InvocationTargetException) {
                    c = ((InvocationTargetException) th).getTargetException();
                    if (c != null) {
                        stack.add(c);
                    }
                }
            }
        }
        return NOptional.ofEmpty(x -> NMsg.ofC("error with type %s not found", type.getSimpleName()));
    }

    public static String[] stacktraceToArray(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try ( PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            List<String> s = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                s.add(line);
            }
            return s.toArray(new String[0]);
        } catch (Exception ex) {
            // ignore
        }
        return new String[0];
    }

    public static String stacktrace(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try ( PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            return sw.toString();
        } catch (Exception ex) {
            // ignore
        }
        return "";
    }

    public static List<String> splitDefault(String str) {
        return NStringUtils.split(str, " ;,\n\r\t|", true, true);
    }

    public static List<String> parseAndTrimToDistinctList(String s) {
        if (s == null) {
            return new ArrayList<>();
        }
        return splitDefault(s).stream().map(String::trim)
                .filter(x -> x.length() > 0)
                .distinct().collect(Collectors.toList());
    }

    public static String joinAndTrimToNull(List<String> args) {
        return NStringUtils.trimToNull(
                String.join(",", args)
        );
    }

    public static NOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty size"));
        }
        value = value.trim();
        Integer i = NLiteral.of(value).asInt().orNull();
        if (i != null) {
            if (defaultMultiplier != null) {
                return NOptional.of(i * defaultMultiplier);
            } else {
                return NOptional.of(i);
            }
        }
        for (String s : new String[]{"kb", "mb", "gb", "k", "m", "g"}) {
            if (value.toLowerCase().endsWith(s)) {
                String v = value.substring(0, value.length() - s.length()).trim();
                i = NLiteral.of(v).asInt().orNull();
                if (i != null) {
                    switch (s) {
                        case "k":
                        case "kb":
                            return NOptional.of(i * 1024);
                        case "m":
                        case "mb":
                            return NOptional.of(i * 1024 * 1024);
                        case "g":
                        case "gb":
                            return NOptional.of(i * 1024 * 1024 * 1024);
                    }
                }
            }
        }
        String finalValue = value;
        return NOptional.ofError(session -> NMsg.ofC("invalid size :%s", finalValue));
    }

    public static int firstIndexOf(String string, char[] chars) {
        char[] value = string.toCharArray();
        for (int i = 0; i < value.length; i++) {
            for (char aChar : chars) {
                if (value[i] == aChar) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static <T, V> Map<T, V> nonNullMap(Map<T, V> other) {
        if (other == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(other);
    }

    public static <T> List<T> nonNullListFromArray(T[] other) {
        return nonNullList(Arrays.asList(other));
    }

    public static <T> List<T> unmodifiableOrNullList(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableList(new ArrayList<>(other));
    }

    public static <T> Set<T> unmodifiableOrNullSet(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(other));
    }

    public static <T, V> Map<T, V> unmodifiableOrNullMap(Map<T, V> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(other));
    }

    public static <T> List<T> copyOrNullList(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return new ArrayList<>(other);
    }

    public static <T> List<T> nonNullList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(other);
    }

    public static <T> Set<T> nonNullSet(Collection<T> other) {
        if (other == null) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(other);
    }

    public static List<String> addUniqueNonBlankList(List<String> list, String... values) {
        LinkedHashSet<String> newList = new LinkedHashSet<>();
        if (list != null) {
            newList.addAll(list);
        }
        boolean someUpdates = false;
        if (values != null) {
            for (String value : values) {
                if (!NBlankable.isBlank(value)) {
                    if (newList.add(NStringUtils.trim(value))) {
                        someUpdates = true;
                    }
                }
            }
        }
        if (someUpdates) {
            list = new ArrayList<>(newList);
        }
        return list;
    }

    public static <T> List<T> uniqueNonBlankList(Collection<T> other) {
        return uniqueList(other).stream().filter(x -> !NBlankable.isBlank(x)).collect(Collectors.toList());
    }

    public static <T> List<T> addUniqueNonBlankList(List<T> list, Collection<T> other) {
        if (other != null) {
            for (T t : other) {
                if (!NBlankable.isBlank(t)) {
                    if (!list.contains(t)) {
                        list.add(t);
                    }
                }
            }
        }
        return list;
    }

    public static <T> Set<T> nonBlankSet(Collection<T> other) {
        return set(other).stream().filter(x -> !NBlankable.isBlank(x)).collect(Collectors.toSet());
    }

    public static <T> List<T> uniqueList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(other));
    }

    public static <T> Set<T> set(Collection<T> other) {
        if (other == null) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(other);
    }

    public static <T> List<T> unmodifiableList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(other));
    }

    public static <T, V> Map<T, V> unmodifiableMap(Map<T, V> other) {
        return other == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(other));
    }

    public static <T> List<T> unmodifiableUniqueList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(uniqueList(other));
    }

    public static boolean isGraphicalDesktopEnvironment() {
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                return false;
            }
            try {
                java.awt.GraphicsDevice[] screenDevices = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                if (screenDevices == null || screenDevices.length == 0) {
                    return false;
                }
            } catch (java.awt.HeadlessException e) {
                return false;
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            return false;
        } catch (Throwable e) {
            //exception may occur if the sdk is built without awt package for instance!
            return false;
        }
    }

    public static String inputString(String message, String title, Supplier<String> in, NLog bLog) {
        try {
            if (title == null) {
                title = "Nuts Package Manager - " + Nuts.getVersion();
            }
            String line = javax.swing.JOptionPane.showInputDialog(
                    null,
                    message, title, javax.swing.JOptionPane.QUESTION_MESSAGE
            );
            if (line == null) {
                line = "";
            }
            return line;
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            bLog.with().level(Level.OFF).verb(NLogVerb.WARNING).log( NMsg.ofC("[Graphical Environment Unsupported] %s", title));
            if (in == null) {
                return new Scanner(System.in).nextLine();
            }
            return in.get();
        }
    }

    public static void showMessage(String message, String title, NLog bLog) {
        if (title == null) {
            title = "Nuts Package Manager";
        }
        try {
            javax.swing.JOptionPane.showMessageDialog(null, message);
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            bLog.with().level(Level.OFF).verb(NLogVerb.WARNING).log( NMsg.ofC("[Graphical Environment Unsupported] %s", title));
        }
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
                    throw new NBootException(NMsg.ofC("unsupported classpath item; expected a file path: %s", url));
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
}
