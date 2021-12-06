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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsBootId;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/15/17.
 *
 * @app.category Internal
 * @since 0.5.4
 */
final class PrivateNutsUtils {

    private static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([a-zA-Z]+))[}]");

    public static String compressString(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\0': {
                    sb.append("\\0");
                    break;
                }
                case '\n': {
                    sb.append("\\n");
                    break;
                }
                case '\r': {
                    sb.append("\\r");
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    break;
                }
                case '\f': {
                    sb.append("\\f");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String leftAlign(String s, int size) {
        int len = s.length();
        StringBuilder sb = new StringBuilder(Math.max(len, size));
        sb.append(s);
        for (int i = len; i < size; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static boolean isValidWorkspaceName(String workspace) {
        if (NutsBlankable.isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (NutsBlankable.isBlank(workspace)) {
            return NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        String workspaceName = workspace.trim();
        if (workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..")) {
            return workspaceName;
        } else {
            String p = null;
            try {
                p = new File(workspaceName).getCanonicalFile().getName();
            } catch (IOException ex) {
                p = new File(workspaceName).getAbsoluteFile().getName();
            }
            if (p.isEmpty() || p.equals(".") || p.equals("..")) {
                return "unknown";
            }
            return p;
        }
    }

    public static String idToPath(NutsBootId id) {
        return id.getGroupId().replace('.', '/') + "/"
                + id.getArtifactId() + "/" + id.getVersion();
    }

    public static List<String> split(String str, String separators) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

    public static String replaceDollarString(String path, Function<String, String> m) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String x = m.apply(matcher.group("name"));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String formatPeriodMilli(long period) {
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        int h = (int) (period / (1000L * 60L * 60L));
        int mn = (int) ((period % (1000L * 60L * 60L)) / 60000L);
        int s = (int) ((period % 60000L) / 1000L);
        int ms = (int) (period % 1000L);
        if (h > 0) {
            sb.append(formatRight(String.valueOf(h), 2)).append("h ");
            started = true;
        }
        if (mn > 0 || started) {
            sb.append(formatRight(String.valueOf(mn), 2)).append("mn ");
            started = true;
        }
        if (s > 0 || started) {
            sb.append(formatRight(String.valueOf(s), 2)).append("s ");
            //started=true;
        }
        sb.append(formatRight(String.valueOf(ms), 3)).append("ms");
        return sb.toString();
    }

    public static String formatRight(String str, int size) {
        StringBuilder sb = new StringBuilder(size);
        sb.append(str);
        while (sb.length() < size) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }

    public static String resolveJavaCommand(String javaHome) {
        String exe = NutsOsFamily.getCurrent().equals(NutsOsFamily.WINDOWS) ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (NutsBlankable.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    public static boolean isActualJavaOptions(String options) {
        //FIX ME
        return true;
    }

    public static boolean isActualJavaCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return true;
        }
        String javaHome = System.getProperty("java.home");
        if (NutsBlankable.isBlank(javaHome) || "null".equals(javaHome)) {
            return cmd.equals("java") || cmd.equals("java.exe") || cmd.equals("javaw.exe") || cmd.equals("javaw");
        }
        String jh = javaHome.replace("\\", "/");
        cmd = cmd.replace("\\", "/");
        if (cmd.equals(jh + "/bin/java")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/java.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/javaw")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/javaw.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/jre/bin/java")) {
            return true;
        }
        return cmd.equals(jh + "/jre/bin/java.exe");
    }

    public static String desc(Object s) {
        if (s == null) {
            return "<EMPTY>";
        }
        String ss
                = (s instanceof Enum) ? ((Enum) s).name().toLowerCase().replace('_', '-')
                : s.toString().trim();
        return ss.isEmpty() ? "<EMPTY>" : ss;
    }

    public static String coalesce(Object... all) {
        for (Object object : all) {
            if (object != null) {
                return desc(object);
            }
        }
        return desc(null);
    }

    public static String formatLogValue(Object unresolved, Object resolved) {
        String a = PrivateNutsUtils.desc(unresolved);
        String b = PrivateNutsUtils.desc(resolved);
        if (a.equals(b)) {
            return a;
        } else {
            return a + " => " + b;
        }
    }

    public static String formatURL(URL url) {
        if (url == null) {
            return "<EMPTY>";
        }
        File f = PrivateNutsUtilIO.toFile(url);
        if (f != null) {
            return f.getPath();
        }
        return url.toString();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return
                NutsUtilStrings.parseBoolean(System.getProperty("nuts." + property), defaultValue, false)
                        || NutsUtilStrings.parseBoolean(System.getProperty("nuts.export." + property), defaultValue, false)
                ;
    }


    public static Set<NutsBootId> parseDependencies(String s) {
        return s == null ? Collections.emptySet() :
                Arrays.stream(s.split(";"))
                        .map(String::trim)
                        .filter(x -> x.length() > 0)
                        .map(NutsBootId::parse)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean isFileAccessible(Path path, Instant expireTime, PrivateNutsBootLog bLog) {
        boolean proceed = Files.isRegularFile(path);
        if (proceed) {
            try {
                if (expireTime != null) {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(path);
                    if (lastModifiedTime.toInstant().compareTo(expireTime) < 0) {
                        return false;
                    }
                }
            } catch (Exception ex0) {
                bLog.log(Level.FINEST, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to get LastModifiedTime for file : {0}", path.toString(), ex0.toString()));
            }
        }
        return proceed;
    }

    public static int firstIndexOf(String string, char[] chars) {
        char[] value = string.toCharArray();
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < chars.length; j++) {
                if (value[i] == chars[j]) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String[] stacktraceToArray(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            List<String> s = new ArrayList<>();
            String line = null;
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
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            return sw.toString();
        } catch (Exception ex) {
            // ignore
        }
        return "";
    }

    public static boolean isInfiniteLoopThread(String className, String methodName) {
        Thread thread = Thread.currentThread();
        StackTraceElement[] elements = thread.getStackTrace();

        if (elements == null || elements.length == 0) {
            return false;
        }

        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[elements.length - (i + 1)];
            if (className.equals(element.getClassName())) {
                if (methodName.equals(element.getMethodName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getHome(NutsStoreLocation storeFolder, NutsBootOptions bOptions) {
        return NutsUtilPlatforms.getPlatformHomeFolder(
                bOptions.getStoreLocationLayout(),
                storeFolder,
                bOptions.getHomeLocations(),
                bOptions.isGlobal(),
                bOptions.getName()
        );
    }

    public static <K, V> LinkedHashMap<K, V> copy(Map<K, V> o) {
        if (o == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(o);
    }

    public static <K> LinkedHashSet<K> copy(Set<K> o) {
        if (o == null) {
            return new LinkedHashSet<>(o);
        }
        return new LinkedHashSet<>();
    }

    public static Level parseLenientLogLevel(String value, Level emptyValue, Level errorValue) {
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            return emptyValue;
        }
        switch (value.trim().toLowerCase()) {
            case "off": {
                return Level.OFF;
            }
            case "verbose":
            case "finest": {
                return Level.FINEST;
            }
            case "finer": {
                return Level.FINER;
            }
            case "fine": {
                return Level.FINE;
            }
            case "info": {
                return Level.INFO;
            }
            case "all": {
                return Level.ALL;
            }
            case "warning": {
                return Level.WARNING;
            }
            case "severe": {
                return Level.SEVERE;
            }
            case "config": {
                return Level.CONFIG;
            }
        }
        Integer i = NutsApiUtils.parseInt(value, null, null);
        if (i != null) {
            switch (i) {
                case Integer.MAX_VALUE:
                    return Level.OFF;
                case 1000:
                    return Level.SEVERE;
                case 900:
                    return Level.WARNING;
                case 800:
                    return Level.INFO;
                case 700:
                    return Level.CONFIG;
                case 500:
                    return Level.FINE;
                case 400:
                    return Level.FINER;
                case 300:
                    return Level.FINEST;
                case Integer.MIN_VALUE:
                    return Level.ALL;
            }
            return new CustomLogLevel("LEVEL" + i, i);
        }
        return errorValue;
    }

    public static Integer parseInt(String value, Integer emptyValue, Integer errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    public static Integer parseInt16(String value, Integer emptyValue, Integer errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        value = value.trim();
        try {
            return Integer.parseInt(value, 16);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    public static Integer parseFileSizeInBytes(String value, Integer defaultMultiplier, Integer emptyValue, Integer errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        value = value.trim();
        Integer i = parseInt(value, null, null);
        if (i != null) {
            if (defaultMultiplier != null) {
                return i * defaultMultiplier;
            } else {
                return i;
            }
        }
        for (String s : new String[]{"kb", "mb", "gb", "k", "m", "g"}) {
            if (value.toLowerCase().endsWith(s)) {
                String v = value.substring(0, value.length() - s.length()).trim();
                i = parseInt(v, null, null);
                if (i != null) {
                    switch (s) {
                        case "k":
                        case "kb":
                            return i * 1024;
                        case "m":
                        case "mb":
                            return i * 1024 * 1024;
                        case "g":
                        case "gb":
                            return i * 1024 * 1024 * 1024;
                    }
                }
            }
        }
        return errorValue;
    }

    public static Path getBootConfFile(NutsBootId id, NutsBootOptions computedOptions, PrivateNutsBootLog bLog) {
        String cFileName = id.getArtifactId() + "-" + id.getVersionString() + ".nuts-boot-props";
        return Paths.get(computedOptions.getStoreLocation(NutsStoreLocation.LIB) + File.separator + NutsConstants.Folders.ID)
                .resolve(PrivateNutsUtils.idToPath(id)).resolve(
                        cFileName
                );
    }

    /**
     * @app.category Internal
     */
    public static class Deps {

        LinkedHashSet<NutsBootId> deps = new LinkedHashSet<>();
        LinkedHashSet<String> repos = new LinkedHashSet<>();
    }

    private static class CustomLogLevel extends Level {
        public CustomLogLevel(String name, int value) {
            super(name, value);
        }
    }
}
