/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.NExceptionBootAware;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.boot.reserved.cmdline.NBootArg;
import net.thevpc.nuts.boot.reserved.cmdline.NBootWorkspaceCmdLineParser;
import net.thevpc.nuts.boot.reserved.maven.NReservedMavenUtilsBoot;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by vpc on 1/15/17.
 *
 * @app.category Internal
 * @since 0.5.4
 */
public final class NBootUtils {
    public static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    };
    private static final char[] BASE16_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final URL urlOf(String any) {
        try {
            return URI.create(any).toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String enumTitle(String s) {
        if (isBlank(s)) {
            return "";
        }
        char[] charArray = s.trim().toCharArray();
        charArray[0] = Character.toUpperCase(charArray[0]);
        return new String(charArray);
    }

    public static String enumId(String s) {
        if (isBlank(s)) {
            return "";
        }
        char[] charArray = s.trim().toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '_') {
                charArray[i] = '-';
            } else {
                charArray[i] = Character.toLowerCase(c);
            }
        }
        return new String(charArray);
    }


    public static String enumName(String s) {
        if (isBlank(s)) {
            return "";
        }
        char[] charArray = s.trim().toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '-') {
                charArray[i] = '_';
            } else {
                charArray[i] = Character.toUpperCase(c);
            }
        }
        return new String(charArray);
    }

    public static boolean sameEnum(String a, String b) {
        char[] charArray1 = a == null ? new char[0] : a.trim().toCharArray();
        char[] charArray2 = b == null ? new char[0] : b.trim().toCharArray();
        if (charArray1.length != charArray2.length) {
            return false;
        }
        for (int i = 0; i < charArray1.length; i++) {
            char c1 = charArray1[i];
            char c2 = charArray2[i];
            if (c1 == '-') {
                c1 = '_';
            } else {
                c1 = Character.toUpperCase(c1);
            }
            if (c2 == '-') {
                c2 = '_';
            } else {
                c2 = Character.toUpperCase(c2);
            }
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }

    public static Integer parseInt(String s) {
        if (s != null) {
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                //
            }
        }
        return null;
    }

    public static <T> T firstNonNull(T... values) {
        if (values != null) {
            for (T value : values) {
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    public static boolean isValidWorkspaceName(String workspace) {
        if (isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (isBlank(workspace)) {
            return NBootConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        String workspaceName = workspace.trim();
        if (workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..")) {
            return workspaceName;
        } else {
            String p = null;
            try {
                p = Paths.get(workspaceName).toRealPath().getFileName().toString();
            } catch (IOException ex) {
                p = new File(workspaceName).getAbsoluteFile().getName();
            }
            if (p.isEmpty() || p.equals(".") || p.equals("..")) {
                return "unknown";
            }
            return p;
        }
    }

    public static String resolveJavaCommand(String javaHome) {
        String exe = sameEnum(NBootPlatformHome.currentOsFamily(), "WINDOWS") ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (isBlank(javaHome) || "null".equals(javaHome)) {
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
        if (isBlank(javaHome) || "null".equals(javaHome)) {
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
            return "<undefined>";
        }
        String ss
                =
                (s instanceof Enum) ? enumName(((Enum<?>) s).name())
                        : s.toString().trim();
        return ss.isEmpty() ? "<undefined>" : ss;
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
        String a = NBootUtils.desc(unresolved);
        String b = NBootUtils.desc(resolved);
        if (a.equals(b)) {
            return a;
        } else {
            return a + " => " + b;
        }
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return
                parseBooleanOr(System.getProperty("nuts." + property), defaultValue)
                        || parseBooleanOr(System.getProperty("nuts.export." + property), defaultValue)
                ;
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

    public static String getHome(String storeFolder, NBootOptionsInfo bOptions) {
        return (firstNonNull(bOptions.getSystem(), false) ?
                NBootPlatformHome.ofSystem(bOptions.getStoreLayout()) :
                NBootPlatformHome.of(bOptions.getStoreLayout()))
                .getWorkspaceLocation(
                        storeFolder,
                        bOptions.getHomeLocations(),
                        bOptions.getName()
                );
    }


    public static String getIdShortName(String groupId, String artifactId) {
        if (isBlank(groupId)) {
            return trim(artifactId);
        }
        return trim(groupId) + ":" + trim(artifactId);
    }

    public static String getIdLongName(String groupId, String artifactId, String version, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(trim(artifactId));
        if (version != null && !isBlank(version)) {
            sb.append("#");
            sb.append(version);
        }
        if (!isBlank(classifier)) {
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
    }

    public static boolean isAcceptCondition(NBootEnvCondition cond) {
        List<String> oss = uniqueNonBlankStringList(cond.getOs());
        List<String> archs = uniqueNonBlankStringList(cond.getArch());
        if (!oss.isEmpty()) {
            String eos = NBootPlatformHome.currentOsFamily();
            boolean osOk = false;
            for (String e : oss) {
                NBootId ee = NBootId.of(e);
                if (ee.getShortName().equalsIgnoreCase(eos)) {
                    if (acceptVersion(ee.getVersion(), System.getProperty("os.version"))) {
                        osOk = true;
                    }
                    break;
                }
            }
            if (!osOk) {
                return false;
            }
        }
        if (!archs.isEmpty()) {
            String earch = System.getProperty("os.arch");
            if (earch != null) {
                boolean archOk = false;
                for (String e : archs) {
                    if (!e.isEmpty()) {
                        if (e.equalsIgnoreCase(earch)) {
                            archOk = true;
                            break;
                        }
                    }
                }
                return archOk;
            }
        }
        return true;
    }

    /**
     * examples : script://groupId:artifactId/version?face
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @param nutsId nutsId
     * @return nutsId
     */
    public static NBootId parseId(String nutsId) {
        if (isBlank(nutsId)) {
            return new NBootId(null, null);
        }
        Matcher m = NBootId.PATTERN.matcher(nutsId);
        if (m.find()) {
            NBootId idBuilder = new NBootId();
            String group = m.group("group");
            String artifact = m.group("artifact");
            idBuilder.setArtifactId(artifact);
            idBuilder.setVersion(m.group("version"));
            if (artifact == null) {
                artifact = group;
                group = null;
            }
            idBuilder.setArtifactId(artifact);
            idBuilder.setGroupId(group);

            Map<String, String> queryMap = NBootStringMapFormat.DEFAULT.parse(m.group("query"));
            NBootEnvCondition conditionBuilder = new NBootEnvCondition();

            Map<String, String> idProperties = new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> e = iterator.next();
                String key = e.getKey();
                String value = e.getValue();
                setIdProperty(key, value, idBuilder, conditionBuilder, idProperties);
            }

            return (idBuilder.setCondition(conditionBuilder)
                    .setProperties(idProperties));
        }
        throw new NBootException(NBootMsg.ofC("invalid id format : %s", nutsId));
    }


    private static boolean ndiAddFileLine(Path filePath, String commentLine, String goodLine, boolean force,
                                          String ensureHeader, String headerReplace, NBootLog bLog) {
        boolean found = false;
        boolean updatedFile = false;
        List<String> lines = new ArrayList<>();
        if (Files.isRegularFile(filePath)) {
            String fileContent = null;
            try {
                fileContent = new String(Files.readAllBytes(filePath));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            String[] fileRows = fileContent.split("\n");
            if (ensureHeader != null) {
                if (fileRows.length == 0 || !fileRows[0].trim().matches(ensureHeader)) {
                    lines.add(headerReplace);
                    updatedFile = true;
                }
            }
            for (int i = 0; i < fileRows.length; i++) {
                String row = fileRows[i];
                if (row.trim().equals("# " + (commentLine))) {
                    lines.add(row);
                    found = true;
                    i++;
                    if (i < fileRows.length) {
                        if (!fileRows[i].trim().equals(goodLine)) {
                            updatedFile = true;
                        }
                    }
                    lines.add(goodLine);
                    i++;
                    for (; i < fileRows.length; i++) {
                        lines.add(fileRows[i]);
                    }
                } else {
                    lines.add(row);
                }
            }
        }
        if (!found) {
            if (ensureHeader != null && headerReplace != null && lines.isEmpty()) {
                lines.add(headerReplace);
            }
            lines.add("# " + (commentLine));
            lines.add(goodLine);
            updatedFile = true;
        }
        if (force || updatedFile) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return updatedFile;
    }

    static boolean ndiRemoveFileCommented2Lines(Path filePath, String commentLine, boolean force, NBootLog bLog) {
        boolean found = false;
        boolean updatedFile = false;
        try {
            List<String> lines = new ArrayList<>();
            if (Files.isRegularFile(filePath)) {
                String fileContent = new String(Files.readAllBytes(filePath));
                String[] fileRows = fileContent.split("\n");
                for (int i = 0; i < fileRows.length; i++) {
                    String row = fileRows[i];
                    if (row.trim().equals("# " + (commentLine))) {
                        found = true;
                        i += 2;
                        for (; i < fileRows.length; i++) {
                            lines.add(fileRows[i]);
                        }
                    } else {
                        lines.add(row);
                    }
                }
            }
            if (found) {
                updatedFile = true;
            }
            if (force || updatedFile) {
                if (!Files.exists(filePath.getParent())) {
                    Files.createDirectories(filePath.getParent());
                }
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            }
            return updatedFile;
        } catch (IOException ex) {
            bLog.with().level(Level.WARNING).verbWarning().error(ex).log(NBootMsg.ofPlain("unable to update update " + filePath));
            return false;
        }
    }

    public static void ndiUndo(NBootLog bLog, String wsName, boolean update) {
        //need to unset settings configuration.
        //what is the safest way to do so?
        String os = NBootPlatformHome.currentOsFamily();
        //windows is ignored because it does not define a global nuts environment
        if (NBootUtils.sameEnum(os, "LINUX") || NBootUtils.sameEnum(os, "MACOS")) {
            String bashrc = NBootUtils.sameEnum(os, "LINUX") ? ".bashrc" : ".bash_profile";
            Path sysrcFile = Paths.get(System.getProperty("user.home")).resolve(bashrc);
            if (Files.exists(sysrcFile)) {

                //these two lines will remove older versions of nuts (before 0.8.0)
                ndiRemoveFileCommented2Lines(sysrcFile, "net.thevpc.app.nuts.toolbox.ndi configuration", true, bLog);
                ndiRemoveFileCommented2Lines(sysrcFile, "net.thevpc.app.nuts configuration", true, bLog);

                //this line will remove 0.8.0+ versions of nuts
                ndiRemoveFileCommented2Lines(sysrcFile, "net.thevpc.nuts configuration", true, bLog);
            }

            // if we have deleted a non default workspace, we will fall back to the default one
            // and will consider the latest version of it.
            // this is helpful if we are playing with multiple workspaces. The default workspace will always be
            // accessible when deleting others
            if (update && !Objects.equals(NBootConstants.Names.DEFAULT_WORKSPACE_NAME, wsName)) {
                String latestDefaultVersion = null;
                try {
                    Path nbase = Paths.get(System.getProperty("user.home")).resolve(".local/share/nuts/apps/" + NBootConstants.Names.DEFAULT_WORKSPACE_NAME + "/id/net/thevpc/nuts/nuts");
                    if (Files.isDirectory(nbase)) {
                        latestDefaultVersion = Files.list(nbase).filter(f -> Files.exists(f.resolve(".nuts-bashrc")))
                                .map(x -> sysrcFile.getFileName().toString()).min((o1, o2) -> NBootVersion.of(o2).compareTo(NBootVersion.of(o1)))
                                .orElse(null);
                    }
                    if (latestDefaultVersion != null) {
                        ndiAddFileLine(sysrcFile, "net.thevpc.nuts configuration",
                                "source " + nbase.resolve(latestDefaultVersion).resolve(".nuts-bashrc"),
                                true, "#!.*", "#!/bin/sh", bLog);
                    }
                } catch (Exception e) {
                    //ignore
                    bLog.with().level(Level.FINEST).verbFail().log(NBootMsg.ofC("unable to undo NDI : %s", e.toString()));
                }
            }
        }
    }

    public static String formatIdList(List<NBootId> s) {
        return s.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatIdArray(NBootId[] s) {
        return Arrays.stream(s).map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatStringIdList(List<String> s) {
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        if (s != null) {
            for (String s1 : s) {
                s1 = trim(s1);
                if (s1.length() > 0) {
                    allIds.add(s1);
                }
            }
        }
        return String.join(",", allIds);
    }

    public static String formatStringIdArray(String[] s) {
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        if (s != null) {
            for (String s1 : s) {
                s1 = trim(s1);
                if (s1.length() > 0) {
                    allIds.add(s1);
                }
            }
        }
        return String.join(",", allIds);
    }

    public static List<String> parseStringIdList(String s) {
        if (s == null) {
            return (Collections.emptyList());
        }
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        StringBuilder q = null;
        boolean inBrackets = false;
        for (char c : s.toCharArray()) {
            if (q == null) {
                q = new StringBuilder();
                if (c == '[' || c == ']') {
                    inBrackets = true;
                    q.append(c);
                } else if (c == ',' || Character.isWhitespace(c) || c == ';') {
                    //ignore
                } else {
                    q.append(c);
                }
            } else {
                if (c == ',' || Character.isWhitespace(c) || c == ';') {
                    if (inBrackets) {
                        q.append(c);
                    } else {
                        if (q.length() > 0) {
                            allIds.add(q.toString());
                        }
                        q = null;
                        inBrackets = false;
                    }
                } else if (c == '[' || c == ']') {
                    if (inBrackets) {
                        inBrackets = false;
                        q.append(c);
                    } else {
                        inBrackets = true;
                        q.append(c);
                    }
                } else {
                    q.append(c);
                }
            }
        }
        if (q != null) {
            if (q.length() > 0) {
                allIds.add(q.toString());
            }
        }
        return (new ArrayList<>(allIds));
    }

    public static List<NBootId> parseIdList(String s) {
        List<NBootId> list = new ArrayList<>();
        List<String> o = parseStringIdList(s);
        for (String x : o) {
            NBootId y = NBootId.of(x);
            if (y == null) {
                return null;
            }
            list.add(y);
        }
        return list;
    }

    public static Map<String, String> toMap(NBootEnvCondition condition) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s;
        if (condition.getArch() != null) {
            s = condition.getArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!isBlank(s)) {
                m.put(NBootConstants.IdProperties.ARCH, s);
            }
        }
        if (condition.getOs() != null) {
            s = condition.getOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!isBlank(s)) {
                m.put(NBootConstants.IdProperties.OS, s);
            }
        }
        if (condition.getOsDist() != null) {
            s = condition.getOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!isBlank(s)) {
                m.put(NBootConstants.IdProperties.OS_DIST, s);
            }
        }
        if (condition.getPlatform() != null) {
            s = formatStringIdList(condition.getPlatform());
            if (!isBlank(s)) {
                m.put(NBootConstants.IdProperties.PLATFORM, s);
            }
        }
        if (condition.getDesktopEnvironment() != null) {
            s = condition.getDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!isBlank(s)) {
                m.put(NBootConstants.IdProperties.DESKTOP, s);
            }
        }
        if (condition.getProfiles() != null) {
            s = condition.getProfiles().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!isBlank(s)) {
                m.put(NBootConstants.IdProperties.PROFILE, s);
            }
        }
        if (condition.getProperties() != null) {
            Map<String, String> properties = condition.getProperties();
            if (!properties.isEmpty()) {
                m.put(NBootConstants.IdProperties.CONDITIONAL_PROPERTIES, NBootStringMapFormat.DEFAULT.format(properties));
            }
        }
        return m;
    }

    public static boolean isBootOptional(String name, NBootOptionsInfo bOptions) {
        if (bOptions.getCustomOptions() != null) {
            for (String property : bOptions.getCustomOptions()) {
                NBootArg a = new NBootArg(property);
                if (("boot-" + name).equals(a.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBootOptional(NBootOptionsInfo bOptions) {
        if (bOptions.getCustomOptions() != null) {
            for (String property : bOptions.getCustomOptions()) {
                NBootArg a = new NBootArg(property);
                if ("boot-optional".equals(a.getKey())) {
                    return parseBooleanOr(a.getValue(), true);
                }
            }
        }
        return true;
    }

    public static boolean parseBooleanOr(String any, boolean b) {
        return firstNonNull(parseBoolean(any), b);
    }

    public static Instant parseInstant(String s) {
        if (isBlank(s)) {
            return null;
        }
        try {
            return DateTimeFormatter.ISO_INSTANT.parse(s, Instant::from);
        } catch (Exception ex) {
            //
        }
        for (String f : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(f).parse(s).toInstant();
            } catch (Exception ex) {
                //
            }
        }
        throw new NBootException(NBootMsg.ofC("invalid instant %s", s));
    }

    public static Instant parseInstant(String s, Instant emptyValue, Instant errorValue) {
        if (isBlank(s)) {
            return emptyValue;
        }
        try {
            return DateTimeFormatter.ISO_INSTANT.parse(s, Instant::from);
        } catch (Exception ex) {
            //
        }
        for (String f : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(f).parse(s).toInstant();
            } catch (Exception ex) {
                //
            }
        }
        return errorValue;
    }

    public static Boolean parseBoolean(String any, Boolean emptyValue, Boolean errorValue) {
        if (any == null) {
            return emptyValue;
        }
        String svalue = String.valueOf(any).trim().toLowerCase();
        if (svalue.isEmpty()) {
            return emptyValue;
        }
        if (svalue.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return true;
        }
        if (svalue.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return false;
        }
        return errorValue;
    }

    public static Boolean parseBoolean(String any) {
        return parseBoolean(any, null, null);
    }

    public static String resolveNutsIdDigest() {
        //TODO COMMIT TO 0.8.4
        return resolveNutsIdDigest(NBootId.ofApi(NBootWorkspace.NUTS_BOOT_VERSION), resolveClasspathURLs(NBootWorkspaceImpl.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NBootId id, URL[] urls) {
        return getURLDigest(findClassLoaderJar(id, urls), null);
    }

    public static boolean isAcceptDependency(NBootDependency s, NBootOptionsInfo bOptions) {
        boolean bootOptionals = isBootOptional(bOptions);
        //by default ignore optionals
        String o = s.getOptional();
        if (isBlank(o) || Boolean.parseBoolean(o)) {
            if (!bootOptionals && !isBootOptional(s.getArtifactId(), bOptions)) {
                return false;
            }
        }
        return isAcceptCondition(s.getCondition());
    }

    public static boolean isDependencyDefaultScope(String s1) {
        if (isBlank(s1)) {
            return true;
        }
        return sameEnum(s1, "api");
    }

    public static boolean acceptVersion(String one, String other) {
        return acceptVersion(NBootVersion.of(one), NBootVersion.of(other));
    }

    public static boolean acceptVersion(NBootVersion.NVersionIntervalBoot one, NBootVersion other) {
        NBootVersion a = NBootVersion.of(one.getLowerBound());
        if (a.isBlank()) {
            //ok
        } else {
            int c = a.compareTo(other);
            if (one.isIncludeLowerBound()) {
                if (c > 0) {
                    return false;
                }
            } else {
                if (c >= 0) {
                    return false;
                }
            }
        }
        a = NBootVersion.of(one.getUpperBound());
        if (a.isBlank()) {
            //ok
        } else {
            int c = a.compareTo(other);
            if (one.isIncludeUpperBound()) {
                return c >= 0;
            } else {
                return c > 0;
            }
        }
        return true;
    }

    public static boolean acceptVersion(NBootVersion one, NBootVersion other) {
        if (!other.isSingleValue()) {
            throw new NBootException(NBootMsg.ofC("expected single value version: %s", other));
        }
        List<NBootVersion.NVersionIntervalBoot> ii = one.intervals();
        if (ii.isEmpty()) {
            return true;
        }
        for (NBootVersion.NVersionIntervalBoot i : ii) {
            if (acceptVersion(i, other)) {
                return true;
            }
        }
        return false;
    }

    public static String getIdLongName(String groupId, String artifactId, NBootVersion version, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(trim(artifactId));
        if (version != null && !version.isBlank()) {
            sb.append("#");
            sb.append(version);
        }
        if (!isBlank(classifier)) {
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
    }

    public static String toDependencyExclusionListString(List<NBootId> exclusions) {
        TreeSet<String> ex = new TreeSet<>();
        for (NBootId exclusion : exclusions) {
            ex.add(exclusion.getShortName());
        }
        return String.join(",", ex);
    }

    private static void setIdProperty(String key, String value, NBootId builder, NBootEnvCondition sb, Map<String, String> props) {
        if (key == null) {
            return;
        }
        switch (key) {
            case NBootConstants.IdProperties.CLASSIFIER: {
                builder.setClassifier(value);
                break;
            }
            case NBootConstants.IdProperties.PROFILE: {
                sb.setProfile(splitDefault(value));
                break;
            }
            case NBootConstants.IdProperties.PLATFORM: {
                sb.setPlatform(parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.OS_DIST: {
                sb.setOsDist(parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.ARCH: {
                sb.setArch(parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.OS: {
                sb.setOs(parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.DESKTOP: {
                sb.setDesktopEnvironment(parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                Map<String, String> mm = NBootStringMapFormat.COMMA_FORMAT.parse(value);
                sb.setProperties(mm);
                break;
            }
            default: {
                props.put(key, value);
            }
        }
    }

    public static String getErrorMessage(Throwable ex) {
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        return m;
    }

    public static <T> T findThrowable(Throwable th, Class<T> type, Predicate<Throwable> filter) {
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
                        return ((T) th);
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
        return null;
    }

    public static String[] stacktraceToArray(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
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
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            return sw.toString();
        } catch (Exception ex) {
            // ignore
        }
        return "";
    }

    public static List<String> splitDefault(String str) {
        return split(str, " ;,\n\r\t|", true, true);
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
        return trimToNull(
                String.join(",", args)
        );
    }

    public static Integer parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        if (isBlank(value)) {
            return null;
        }
        value = value.trim();
        Integer i = parseInt(value);
        if (i != null) {
            if (defaultMultiplier != null) {
                return (i * defaultMultiplier);
            } else {
                return (i);
            }
        }
        for (String s : new String[]{"kb", "mb", "gb", "k", "m", "g"}) {
            if (value.toLowerCase().endsWith(s)) {
                String v = value.substring(0, value.length() - s.length()).trim();
                i = parseInt(v);
                if (i != null) {
                    switch (s) {
                        case "k":
                        case "kb":
                            return (i * 1024);
                        case "m":
                        case "mb":
                            return (i * 1024 * 1024);
                        case "g":
                        case "gb":
                            return (i * 1024 * 1024 * 1024);
                    }
                }
            }
        }
//        String finalValue = value;
        return null;
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
                if (!isBlank(value)) {
                    if (newList.add(trim(value))) {
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

    public static <T> List<T> uniqueNonBlankList(Collection<T> other, Predicate<T> blakifier) {
        return uniqueList(other).stream().filter(x -> x != null && !blakifier.test(x)).collect(Collectors.toList());
    }

    public static List<String> uniqueNonBlankStringList(Collection<String> other) {
        return uniqueList(other).stream().filter(x -> !isBlank(x)).collect(Collectors.toList());
    }

    public static <T> List<T> addUniqueNonBlankList(List<T> list, Collection<T> other, Predicate<T> blakifier) {
        if (other != null) {
            for (T t : other) {
                if (t != null && !blakifier.test(t)) {
                    if (!list.contains(t)) {
                        list.add(t);
                    }
                }
            }
        }
        return list;
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

    public static String inputString(String message, String title, Supplier<String> in, NBootLog bLog) {
        try {
            if (title == null) {
                title = "Nuts Package Manager - " + NBootWorkspace.NUTS_BOOT_VERSION;
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
            bLog.with().level(Level.OFF).verbWarning().log(NBootMsg.ofC("[Graphical Environment Unsupported] %s", title));
            if (in == null) {
                return new Scanner(System.in).nextLine();
            }
            return in.get();
        }
    }

    public static void showMessage(String message, String title, NBootLog bLog) {
        if (title == null) {
            title = "Nuts Package Manager";
        }
        try {
            javax.swing.JOptionPane.showMessageDialog(null, message);
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            bLog.with().level(Level.OFF).verbWarning().log(NBootMsg.ofC("[Graphical Environment Unsupported] %s", title));
        }
    }

    private static void fillBootDependencyNodes(NBootClassLoaderNode node, Set<URL> urls, Set<String> visitedIds,
                                                NBootLog bLog) {
        String shortName = NBootId.of(node.getId()).getShortName();
        if (!visitedIds.contains(shortName)) {
            visitedIds.add(shortName);
            if (!node.isIncludedInClasspath()) {
                urls.add(node.getURL());
            } else {
                bLog.with().level(Level.WARNING).verbCache().log(NBootMsg.ofC("url will not be loaded (already in classloader) : %s", node.getURL()));
            }
            for (NBootClassLoaderNode dependency : node.getDependencies()) {
                fillBootDependencyNodes(dependency, urls, visitedIds, bLog);
            }
        }
    }

    public static URL[] resolveClassWorldURLs(NBootClassLoaderNode[] nodes, ClassLoader contextClassLoader,
                                              NBootLog bLog) {
        LinkedHashSet<URL> urls = new LinkedHashSet<>();
        Set<String> visitedIds = new HashSet<>();
        for (NBootClassLoaderNode info : nodes) {
            if (info != null) {
                fillBootDependencyNodes(info, urls, visitedIds, bLog);
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static URL findClassLoaderJar(NBootId id, URL[] urls) {
        for (URL url : urls) {
            NBootId[] nutsBootIds = NReservedMavenUtilsBoot.resolveJarIds(url);
            for (NBootId i : nutsBootIds) {
                if (isBlank(id.getGroupId()) || i.getGroupId().equals(id.getGroupId())) {
                    if (isBlank(id.getArtifactId()) || i.getArtifactId().equals(id.getArtifactId())) {
                        if (isBlank(id.getVersion()) || i.getVersion().equals(id.getVersion())) {
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
                                all.add(NBootUtils.urlOf(jar));
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
                                            NBootLog bLog) {
        try {
            if (url != null) {
                if (contextClassLoader == null) {
                    return false;
                }
                File file = toFile(url);
                if (file == null) {
                    throw new NBootException(NBootMsg.ofC("unsupported classpath item; expected a file path: %s", url));
                }
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (!zname.endsWith("/") && zname.endsWith(".class") && !zname.contains("$")) {
                            if (isInfiniteLoopThread(NBootUtils.class.getName(), "isLoadedClassPath")) {
                                return false;
                            }
                            URL incp = contextClassLoader.getResource(zname);
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            if (incp != null) {
                                bLog.with().level(Level.FINEST).verbSuccess().log(NBootMsg.ofC("url %s is already in classpath. checked class %s successfully",
                                        url, clz));
                                return true;
                            } else {
                                bLog.with().level(Level.FINEST).verbInfo().log(NBootMsg.ofC("url %s is not in classpath. failed to check class %s",
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
        bLog.with().level(Level.FINEST).verbFail().log(NBootMsg.ofC("url %s is not in classpath. no class found to check", url));
        return false;
    }

    public static String resolveGroupIdPath(String groupId) {
        return groupId.replace('.', '/');
    }

    public static String resolveIdPath(NBootId id) {
        StringBuilder sb = new StringBuilder();
        sb.append(resolveGroupIdPath(id.getGroupId()));
        if (!isBlank(id.getArtifactId())) {
            sb.append("/");
            sb.append(id.getArtifactId());
            if (!isBlank(id.getVersion())) {
                sb.append("/");
                sb.append(id.getVersion());
            }
        }
        return sb.toString();
    }

    public static String resolveJarPath(NBootId id) {
        return resolveFilePath(id, "jar");
    }

    public static String resolveDescPath(NBootId id) {
        return resolveFilePath(id, "nuts");
    }

    public static String resolveNutsDescriptorPath(NBootId id) {
        return resolveFilePath(id, "nuts");
    }

    public static String resolveFileName(NBootId id, String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.getArtifactId());
        if (!isBlank(id.getVersion())) {
            sb.append("-").append(id.getVersion());
        }
        if (!isBlank(extension)) {
            sb.append(".").append(extension);
        }
        return sb.toString();
    }

    public static String resolveFilePath(NBootId id, String extension) {
        String fileName = resolveFileName(id, extension);
        return resolveIdPath(id) + '/' + fileName;
    }

    public static List<String> nonNullStrList(List<String> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public static <T> boolean isEmptyList(List<T> any) {
        return any == null || any.isEmpty();
    }

    public static long parseTimePeriod(String argPart, String optionName) {
        if (argPart == null) {
            throw new IllegalArgumentException("missing value for " + optionName);
        }
        final Object[] i = parseSuffixedLong(argPart);
        if (i[0] == null) {
            throw new IllegalArgumentException("expected time number for " + optionName + " : " + argPart);
        }
        long c = (Long) i[0];
        switch (String.valueOf(i[1]).toLowerCase()) {
            case "":
            case "ms":
                return c;
            case "s":
                return c * 1000;
            case "mn":
                return c * 1000 * 60;
            case "h":
                return c * 1000 * 60 * 60;
        }
        throw new IllegalArgumentException("invalid time unit for " + optionName + " : " + argPart);
    }

    private static Object[] parseSuffixedLong(String argPart) {
        if (argPart == null) {
            argPart = "";
        }
        argPart = argPart.trim();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        final char[] chars = argPart.toCharArray();
        boolean expectInt = true;
        for (int i = 0; i < chars.length; i++) {
            if (expectInt) {
                if (Character.isDigit(chars[i])) {
                    sb1.append(chars[i]);
                } else {
                    sb2.append(chars[i]);
                    expectInt = false;
                }
            } else {
                sb2.append(chars[i]);
            }
        }
        if (sb1.length() > 0) {
            try {
                long ii = Long.parseLong(sb1.toString());
                return new Object[]{ii, sb2.toString()};
            } catch (Exception e) {
                return new Object[]{null, sb1.toString() + sb2.toString()};
            }
        }
        return new Object[]{null, sb2.toString()};
    }

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static String readStringFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static InputStream openStream(URL url, NBootLog bLog) {
        return NBootMonitoredURLInputStream.of(url, bLog);
    }

    public static byte[] loadStream(InputStream stream, NBootLog bLog) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(stream, bos, true, true);
        return bos.toByteArray();
    }

    public static ByteArrayInputStream preloadStream(InputStream stream, NBootLog bLog) throws IOException {
        return new ByteArrayInputStream(loadStream(stream, bLog));
    }

    public static Properties loadURLProperties(Path path, NBootLog bLog) {
        Properties props = new Properties();
        if (Files.isRegularFile(path)) {
            try (InputStream is = Files.newInputStream(path)) {
                props.load(is);
            } catch (IOException ex) {
                return new Properties();
            }
        }
        return props;
    }

    public static Properties loadURLProperties(URL url, File cacheFile, boolean useCache, NBootLog bLog) {
        NBootChronometer chrono = NBootChronometer.startNow();
        Properties props = new Properties();
        InputStream inputStream = null;
        File urlFile = toFile(url);
        try {
            if (useCache) {
                if (cacheFile != null && cacheFile.isFile()) {
                    try {
                        inputStream = new FileInputStream(cacheFile);
                        props.load(inputStream);
                        chrono.stop();
                        NBootDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verbSuccess().log(NBootMsg.ofC("load cached file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), chrono));
                        return props;
                    } catch (IOException ex) {
                        bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("invalid cache. Ignored %s : %s", cacheFile.getPath(), ex.toString()));
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception ex) {
                                if (bLog != null) {
                                    bLog.with().level(Level.FINE).verbFail().error(ex).log(NBootMsg.ofPlain("unable to close stream"));
                                }
                                //
                            }
                        }
                    }
                }
            }
            inputStream = null;
            try {
                if (url != null) {
                    String urlString = url.toString();
                    inputStream = openStream(url, bLog);
                    if (inputStream != null) {
                        props.load(inputStream);
                        if (cacheFile != null) {
                            boolean copy = true;
                            //do not override self!
                            if (urlFile != null) {
                                if (getAbsolutePath(urlFile.getPath()).equals(getAbsolutePath(cacheFile.getPath()))) {
                                    copy = false;
                                }
                            }
                            if (copy) {
                                File pp = cacheFile.getParentFile();
                                if (pp != null) {
                                    pp.mkdirs();
                                }
                                boolean cachedRecovered = cacheFile.isFile();
                                if (urlFile != null) {
                                    copy(urlFile, cacheFile, bLog);
                                } else {
                                    copy(url, cacheFile, bLog);
                                }
                                NBootDuration time = chrono.getDuration();
                                if (cachedRecovered) {
                                    bLog.with().level(Level.CONFIG).verbCache().log(NBootMsg.ofC("recover cached prp file %s (from %s)" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), urlString, time));
                                } else {
                                    bLog.with().level(Level.CONFIG).verbCache().log(NBootMsg.ofC("cache prp file %s (from %s)" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), urlString, time));
                                }
                                return props;
                            }
                        }
                        NBootDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verbSuccess().log(NBootMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), urlString, time));
                    } else {
                        NBootDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), urlString, time));
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            NBootDuration time = chrono.getDuration();
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), String.valueOf(url),
                    time));
        }
        return props;
    }

    public static boolean isURL(String url) {
        if (url != null) {
            try {
                urlOf(url);
                return true;
            } catch (Exception e) {
                //
            }
        }
        return false;
    }

    public static String getNativePath(String s) {
        return s.replace('/', File.separatorChar);
    }

    public static File toFile(String url) {
        if (isBlank(url)) {
            return null;
        }
        URL u = null;
        try {
            u = urlOf(url);
            return toFile(u);
        } catch (Exception e) {
            //
            return new File(url);
        }
    }

    public static URL toURL(String url) {
        if (isBlank(url)) {
            return null;
        }
        try {
            return urlOf(url);
        } catch (Exception e) {
            return null;
        }
    }

    public static File toFile(URL url) {
        if (url == null) {
            return null;
        }
        if ("file".equals(url.getProtocol())) {
            try {
                return Paths.get(url.toURI()).toFile();
            } catch (URISyntaxException e) {
                //
            }
        }
        return null;
    }

    public static long copy(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) throws IOException {
        byte[] bytes = new byte[10240];
        int count;
        long all = 0;
        try {
            try {
                while ((count = from.read(bytes)) > 0) {
                    to.write(bytes, 0, count);
                    all += count;
                }
                return all;
            } finally {
                if (closeInput) {
                    from.close();
                }
            }
        } finally {
            if (closeOutput) {
                to.close();
            }
        }
    }

    public static void copy(File ff, File to, NBootLog bLog) throws IOException {
        if (ff.equals(to)) {
            return;
        }
        if (to.getParentFile() != null) {
            to.getParentFile().mkdirs();
        }
        if (ff == null || !ff.exists()) {
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("not found %s", ff));
            throw new FileNotFoundException(ff == null ? "" : ff.getPath());
        }
        try {
            Files.copy(ff.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("error copying %s to %s : %s", ff, to, ex.toString()));
            throw ex;
        }
    }

    public static void copy(String path, File to, NBootLog bLog) throws IOException {
        if (isBlank(path)) {
            throw new IOException("empty path " + path);
        }
        File file = toFile(path);
        if (file != null) {
            copy(file, to, bLog);
        } else {
            URL u = toURL(path);
            if (u != null) {
                copy(u, to, bLog);
            } else {
                throw new IOException("neither file nor URL : " + path);
            }
        }
    }

    public static void copy(URL url, File to, NBootLog bLog) throws IOException {
        try {
            InputStream in = openStream(url, bLog);
            if (in == null) {
                throw new IOException("empty Stream " + url);
            }
            if (to.getParentFile() != null) {
                if (!to.getParentFile().isDirectory()) {
                    boolean mkdirs = to.getParentFile().mkdirs();
                    if (!mkdirs) {
                        bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("error creating folder %s", url));
                    }
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (FileNotFoundException | UncheckedIOException ex) {
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("not found %s", url));
            throw ex;
        } catch (IOException ex) {
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("error copying %s to %s : %s", url, to, ex.toString()));
            throw ex;
        }
    }

    public static File createFile(String parent, String child) {
        String userHome = System.getProperty("user.home");
        if (child.startsWith("~/") || child.startsWith("~\\")) {
            child = new File(userHome, child.substring(2)).getPath();
        }
        if ((child.startsWith("/") || child.startsWith("\\") || new File(child).isAbsolute())) {
            return new File(child);
        }
        if (parent != null) {
            if (parent.startsWith("~/")) {
                parent = new File(userHome, parent.substring(2)).getPath();
            }
        } else {
            parent = ".";
        }
        return new File(parent, child);
    }

    public static String expandPath(String path, String base, Function<String, String> pathExpansionConverter) {
        path = NBootMsg.ofV(path.trim(), pathExpansionConverter).toString();
        if (isURL(path)) {
            return path;
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        if (base == null) {
            base = System.getProperty("user.dir");
        }
        if (new File(path).isAbsolute()) {
            return path;
        }
        return base + File.separator + path;
    }

    public static boolean isFileAccessible(Path path, Instant expireTime, NBootLog bLog) {
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
                bLog.with().level(Level.FINEST).verbFail().log(NBootMsg.ofC("unable to get LastModifiedTime for file : %s", path.toString(), ex0.toString()));
            }
        }
        return proceed;
    }

    public static String getURLDigest(URL url, NBootLog bLog) {
        if (url != null) {
            File ff = toFile(url);
            if (ff != null) {
                return getFileOrDirectoryDigest(ff.toPath());
            }
            InputStream is = null;
            try {
                is = openStream(url, bLog);
                if (is != null) {
                    return getStreamDigest(is);
                }
            } catch (Exception e) {
                //
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
        }
        return null;
    }

    public static String getFileOrDirectoryDigest(Path p) {
        if (Files.isDirectory(p)) {
            return getDirectoryDigest(p);
        } else if (Files.isRegularFile(p)) {
            try (InputStream is = Files.newInputStream(p)) {
                return getStreamDigest(is);
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    private static String getDirectoryDigest(Path p) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Files.walkFileTree(p, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    incrementalUpdateFileDigest(new ByteArrayInputStream(dir.toString().getBytes()), md);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    incrementalUpdateFileDigest(new ByteArrayInputStream(file.toString().getBytes()), md);
                    incrementalUpdateFileDigest(Files.newInputStream(file), md);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            byte[] digest = md.digest();
            return toHexString(digest);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStreamDigest(InputStream is) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return getStreamDigest(is, md, 2048);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getStreamDigest(InputStream is, MessageDigest md, int byteArraySize) {
        try {
            md.reset();
            byte[] bytes = new byte[byteArraySize];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
            byte[] digest = md.digest();
            return toHexString(digest);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void incrementalUpdateFileDigest(InputStream is, MessageDigest md) {
        try {
            byte[] bytes = new byte[4096];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static InputStream resolveInputStream(String url, NBootLog bLog) {
        InputStream in = null;
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                URL url1 = urlOf(url);
                try {
                    in = openStream(url1, bLog);
                } catch (Exception ex) {
                    //do not need to log error
                    return null;
                }
            } else if (url.startsWith("file:")) {
                URL url1 = urlOf(url);
                File file = toFile(url1);
                if (file == null) {
                    // was not able to resolve to File
                    try {
                        in = openStream(url1, bLog);
                    } catch (Exception ex) {
                        //do not need to log error
                        return null;
                    }
                } else if (file.isFile()) {
                    in = Files.newInputStream(file.toPath());
                } else {
                    return null;
                }
            } else {
                File file = new File(url);
                if (file.isFile()) {
                    in = Files.newInputStream(file.toPath());
                } else {
                    return null;
                }
            }
        } catch (IOException | UncheckedIOException e) {
            bLog.with().level(Level.FINE).verbFail().error(e).log(NBootMsg.ofC("unable to resolveInputStream %s", url));
        }
        return in;
    }

    public static long deleteAndConfirmAll(Path[] folders, boolean force, NBootDeleteFilesContextBoot refForceAll,
                                           String header, NBootLog bLog, NBootOptionsInfo bOptions, Supplier<String> readline) {
        long count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            for (Path child : folders) {
                if (Files.exists(child)) {
                    if (!headerWritten) {
                        headerWritten = true;
                        if (!force && !refForceAll.isForce(true)) {
                            if (header != null) {
                                if (!firstNonNull(bOptions.getBot(), false)) {
                                    bLog.with().level(Level.WARNING).verbWarning().log(NBootMsg.ofC("%s", header));
                                }
                            }
                        }
                    }
                    count += deleteAndConfirm(child, force, refForceAll, bLog, bOptions, readline);
                }
            }
        }
        return count;
    }

    private static long deleteAndConfirm(Path directory, boolean force, NBootDeleteFilesContextBoot refForceAll,
                                         NBootLog bLog, NBootOptionsInfo bOptions, Supplier<String> readline) {
        String confirm = _confirm(bOptions);
        boolean bot = firstNonNull(bOptions.getBot(), false);
        boolean gui = firstNonNull(bOptions.getGui(), false);
        if (Files.exists(directory)) {
            if (!force && !refForceAll.isForce(true) && refForceAll.accept(directory)) {
                String line = null;
                if (bot) {
                    if (sameEnum(confirm, "YES")) {
                        line = "y";
                    } else {
                        throw new NBootException(NBootMsg.ofPlain("failed to delete files in --bot mode without auto confirmation"));
                    }
                } else {
                    switch (confirm) {
                        case "YES": {
                            line = "y";
                            break;
                        }
                        case "NO": {
                            line = "n";
                            break;
                        }
                        case "ERROR": {
                            throw new NBootException(NBootMsg.ofPlain("error response"));
                        }
                        case "ASK": {
                            if (gui) {
                                line = inputString(
                                        NBootMsg.ofC("do you confirm deleting %s [y/n/c] (default 'n') ?", directory).toString(),
                                        null, readline, bLog
                                );
                            } else {
                                // Level.OFF is to force logging in all cases
                                bLog.with().level(Level.OFF).verbWarning().log(NBootMsg.ofC("do you confirm deleting %s [y/n/c] (default 'n') ? : ", directory));
                                line = readline.get();
                            }
                        }
                    }
                }
                if (line != null && line.equals(line.toUpperCase()) && parseBoolean(line) != null) {
                    refForceAll.setForce(parseBoolean(line));
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NBootCancelException();
                } else if (!firstNonNull(parseBoolean(line), false)) {
                    refForceAll.ignore(directory);
                    return 0;
                }
            }
            long[] count = new long[1];
            try {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        count[0]++;
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        count[0]++;
                        boolean deleted = false;
                        for (int i = 0; i < 2; i++) {
                            try {
                                Files.delete(dir);
                                deleted = true;
                                break;
                            } catch (DirectoryNotEmptyException e) {
                                // sometimes, on Windows OS, the Filesystem hasn't yet finished deleting
                                // the children (asynchronous)
                                //try three times and then exit!
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (!deleted) {
                            //do not catch, last time the exception is thrown
                            Files.delete(dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                count[0]++;
                bLog.with().level(Level.FINEST).verbWarning().log(NBootMsg.ofC("delete folder : %s (%s files/folders deleted)", directory, count[0]));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return count[0];
        }
        return 0;
    }

    public static String formatURL(URL url) {
        if (url == null) {
            return "<EMPTY>";
        }
        File f = toFile(url);
        if (f != null) {
            return f.getPath();
        }
        return url.toString();
    }

    public static String getStoreLocationPath(NBootOptionsInfo bOptions, String storeType) {
        Map<String, String> storeLocations = bOptions.getStoreLocations();
        if (storeLocations != null) {
            return storeLocations.get(enumId(storeType));
        }
        return null;
    }

    /**
     * @param includeRoot true if include root
     * @param storeTypesOrPaths   of type NutsStoreLocation, Path of File
     * @param readline
     */
    public static long deleteStoreLocations(NBootOptionsInfo lastBootOptions, NBootOptionsInfo o, boolean includeRoot,
                                            NBootLog bLog, Object[] storeTypesOrPaths, Supplier<String> readline) {
        if (lastBootOptions == null) {
            return 0;
        }
        String confirm = _confirm(o);
        if (sameEnum(confirm, "ASK")
                && !sameEnum(enumId(firstNonNull(o.getOutputFormat(), "PLAIN")), "PLAIN")) {
            throw new NBootException(
                    NBootMsg.ofPlain("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), 255);
        }
        bLog.with().level(Level.FINEST).verbWarning().log(NBootMsg.ofC("delete workspace location(s) at : %s",
                lastBootOptions.getWorkspace()
        ));
        boolean force = false;
        switch (confirm) {
            case "ASK": {
                break;
            }
            case "YES": {
                force = true;
                break;
            }
            case "NO":
            case "ERROR": {
                bLog.with().level(Level.WARNING).verbWarning().log(NBootMsg.ofPlain("reset cancelled (applied '--no' argument)"));
                throw new NBootCancelException();
            }
        }
        List<Path> folders = new ArrayList<>();
        if (includeRoot) {
            folders.add(Paths.get(lastBootOptions.getWorkspace()));
        }
        NBootPlatformHome hh = (firstNonNull(o.getSystem(), false) ?
                NBootPlatformHome.ofSystem(o.getStoreLayout()) :
                NBootPlatformHome.of(o.getStoreLayout()));

        for (Object ovalue : storeTypesOrPaths) {
            if (ovalue != null) {
                if (ovalue instanceof String) {
                    String p = getStoreLocationPath(lastBootOptions, (String) ovalue);
                    if (p != null) {
                        folders.add(Paths.get(p));
                    }else{
                        folders.add(Paths.get(hh.getStore(
                                (String)ovalue
                        )));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue));
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue).toPath());
                } else {
                    throw new NBootException(NBootMsg.ofC("unsupported path type : %s", ovalue));
                }
            }
        }
        NBootOptionsInfo optionsCopy = o.copy();
        if (firstNonNull(optionsCopy.getBot(), false) || !isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return deleteAndConfirmAll(folders.toArray(new Path[0]), force,
                "ATTENTION ! You are about to delete nuts workspace files."
                , bLog, optionsCopy, readline);
    }

    private static String _confirm(NBootOptionsInfo o) {
        return enumName(firstNonNull(o.getConfirm(), "ASK"));
    }

    /**
     * @param readline
     */
    public static long deleteStoreLocationsHard(NBootOptionsInfo lastBootOptions, NBootOptionsInfo bOptions,
                                                NBootLog bLog, Supplier<String> readline) {
        String confirm = _confirm(bOptions);
        if (sameEnum(confirm, "ASK")
                && !sameEnum(enumName(firstNonNull(bOptions.getOutputFormat(), "PLAIN")), "PLAIN")) {
            throw new NBootException(
                    NBootMsg.ofPlain("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), 255);
        }
        bLog.with().level(Level.FINEST).verbWarning().log(NBootMsg.ofC("hard reset nuts to remove all workspaces and all configuration files."));
        boolean force = false;
        switch (confirm) {
            case "ASK": {
                break;
            }
            case "YES": {
                force = true;
                break;
            }
            case "NO":
            case "ERROR": {
                bLog.with().level(Level.WARNING).verbWarning().log(NBootMsg.ofPlain("reset cancelled (applied '--no' argument)"));
                throw new NBootCancelException();
            }
        }
        LinkedHashSet<Path> folders = new LinkedHashSet<>();

        NBootPlatformHome hh = (firstNonNull(bOptions.getSystem(), false) ?
                NBootPlatformHome.ofSystem(bOptions.getStoreLayout()) :
                NBootPlatformHome.of(bOptions.getStoreLayout()));
        folders.add(Paths.get(hh.getHome()).resolve("ws"));


        for (String storeType : NBootPlatformHome.storeTypes()) {
            folders.add(Paths.get(hh.getStore(
                    storeType
            )));
        }

        ///  current WS

        if (lastBootOptions != null) {
            folders.add(Paths.get(lastBootOptions.getWorkspace()));
            for (Object ovalue : NBootPlatformHome.storeTypes()) {
                if (ovalue != null) {
                    if (ovalue instanceof String) {
                        String p = getStoreLocationPath(lastBootOptions, (String) ovalue);
                        if (p != null) {
                            folders.add(Paths.get(p));
                        }
                    } else if (ovalue instanceof Path) {
                        folders.add(((Path) ovalue));
                    } else if (ovalue instanceof File) {
                        folders.add(((File) ovalue).toPath());
                    } else {
                        throw new NBootException(NBootMsg.ofC("unsupported path type : %s", ovalue));
                    }
                }
            }
        }

        String _ws = bOptions.getWorkspace();
        if (!isRemoteWorkspaceLocation(_ws)) {
            Boolean systemWorkspace = NBootUtils.firstNonNull(bOptions.getSystem(), false);
            String lastNutsWorkspaceJsonConfigPath = NBootUtils.isValidWorkspaceName(_ws) ? NBootPlatformHome.of(null, systemWorkspace)
                    .getWorkspaceLocation(NBootUtils.resolveValidWorkspaceName(_ws)) : NBootUtils.getAbsolutePath(_ws);
            folders.add(Paths.get(lastNutsWorkspaceJsonConfigPath));
        }
        for (Object ovalue : NBootPlatformHome.storeTypes()) {
            if (ovalue != null) {
                if (ovalue instanceof String) {
                    String p = getStoreLocationPath(bOptions, (String) ovalue);
                    if (p != null) {
                        folders.add(Paths.get(p));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue));
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue).toPath());
                } else {
                    throw new NBootException(NBootMsg.ofC("unsupported path type : %s", ovalue));
                }
            }
        }

        NBootOptionsInfo optionsCopy = bOptions.copy();
        if (firstNonNull(optionsCopy.getBot(), false) || !isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return deleteAndConfirmAll(folders.stream().sorted().toArray(Path[]::new), force,
                "ATTENTION ! You are about to delete workspaces and all nuts configuration files."
                , bLog, optionsCopy, readline);
    }


    public static long deleteAndConfirmAll(Path[] folders, boolean force, String header,
                                           NBootLog bLog, NBootOptionsInfo bOptions, Supplier<String> readline) {
        return deleteAndConfirmAll(folders, force, new NBootDeleteFilesContextBootImpl(), header, bLog, bOptions, readline);
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static CharSequence trim(CharSequence value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return value.toString().trim();
        }
        int len0 = value.length();
        int len = len0;
        int st = 0;
        while ((st < len) && (value.charAt(st) <= ' ')) {
            st++;
        }
        while ((st < len) && (value.charAt(len - 1) <= ' ')) {
            len--;
        }
        return ((st > 0) || (len < len0)) ? value.subSequence(st, len) : value.toString();
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static CharSequence trimLeft(CharSequence value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value.toString();
        }
        int st = 0;
        while ((st < len) && (value.charAt(st) <= ' ')) {
            st++;
        }
        if (st > 0) {
            return value.subSequence(st, len);
        }
        return value.toString();
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static CharSequence trimRight(CharSequence value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value.toString();
        }
        int st = len;
        while ((st > 0) && (value.charAt(st - 1) <= ' ')) {
            st--;
        }
        if (st < len) {
            return value.subSequence(0, st);
        }
        return value.toString();
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trimToNull(CharSequence value) {
        if (value == null) {
            return null;
        }
        String t = trim(value).toString();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    public static String firstNonNull(String... values) {
        return firstNonNull(values == null ? null : Arrays.asList(values));
    }

    public static String firstNonNull(List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static String firstNonBlank(String a, String b) {
        if (!isBlank(a)) {
            return a;
        }
        if (!isBlank(b)) {
            return b;
        }
        return null;
    }

    public static String firstNonBlank(String... values) {
        return firstNonBlank(values == null ? null : Arrays.asList(values));
    }

    public static String firstNonBlank(List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (!isBlank(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = BASE16_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = BASE16_CHARS[v & 15];
        }
        return new String(hexChars);
    }

    public static String formatAlign(String text, int size, NBootPositionTypeBoot position) {
        if (text == null) {
            text = "";
        }
        int len = text.length();
        if (len >= size) {
            return text;
        }
        switch (position) {
            case FIRST: {
                StringBuilder sb = new StringBuilder(size);
                sb.append(text);
                for (int i = len; i < size; i++) {
                    sb.append(' ');
                }
                return sb.toString();
            }
            case LAST: {
                StringBuilder sb = new StringBuilder(size);
                for (int i = len; i < size; i++) {
                    sb.append(' ');
                }
                sb.append(text);
                return sb.toString();
            }
            case CENTER: {
                StringBuilder sb = new StringBuilder(size);
                int h = size / 2 + size % 2;
                for (int i = len; i < h; i++) {
                    sb.append(' ');
                }
                sb.append(text);
                h = size / 2;
                for (int i = len; i < h; i++) {
                    sb.append(' ');
                }
                return sb.toString();
            }
        }
        throw new UnsupportedOperationException();
    }

    public static String formatStringLiteral(String text) {
        return formatStringLiteral(text, NBootQuoteTypeBoot.DOUBLE);
    }

    public static String formatStringLiteral(String text, NBootQuoteTypeBoot quoteType) {
        return formatStringLiteral(text, quoteType, NBootSupportMode.ALWAYS);
    }

    public static String formatStringLiteral(String text, NBootQuoteTypeBoot quoteType, NBootSupportMode condition) {
        return formatStringLiteral(text, quoteType, condition, "");
    }

    public static String formatStringLiteral(String text, NBootQuoteTypeBoot quoteType, NBootSupportMode condition, String escapeChars) {
        if (text == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        boolean requireQuotes = condition == NBootSupportMode.ALWAYS;
        boolean allowQuotes = condition != NBootSupportMode.NEVER;
        for (char c : text.toCharArray()) {
            switch (c) {
                case ' ': {
                    if (allowQuotes) {
                        sb.append(" ");
                        requireQuotes = true;
                    } else {
                        sb.append("\\ ");
                    }
                    break;
                }
                case '\n': {
                    sb.append("\\n");
                    if (!requireQuotes && allowQuotes) {
                        requireQuotes = true;
                    }
                    break;
                }
                case '\f': {
                    sb.append("\\f");
                    if (!requireQuotes && allowQuotes) {
                        requireQuotes = true;
                    }
                    break;
                }
                case '\r': {
                    sb.append("\\r");
                    if (!requireQuotes && allowQuotes) {
                        requireQuotes = true;
                    }
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    if (!requireQuotes && allowQuotes) {
                        requireQuotes = true;
                    }
                    break;
                }
                case '\"': {
                    if (quoteType == NBootQuoteTypeBoot.DOUBLE) {
                        sb.append("\\").append(c);
                        if (!requireQuotes && allowQuotes) {
                            requireQuotes = true;
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                case '\'': {
                    if (quoteType == NBootQuoteTypeBoot.SIMPLE) {
                        sb.append("\\").append(c);
                        if (!requireQuotes && allowQuotes) {
                            requireQuotes = true;
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                case '`': {
                    sb.append(c);
                    break;
                }
                default: {
                    if (escapeChars != null && escapeChars.indexOf(c) >= 0) {
                        if (allowQuotes) {
                            sb.append(c);
                            requireQuotes = true;
                        } else {
                            sb.append("\\").append(c);
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
            }
        }
        if (sb.length() == 0) {
            requireQuotes = true;
        }
        if (requireQuotes) {
            switch (quoteType) {
                case DOUBLE: {
                    sb.insert(0, '\"');
                    sb.append('\"');
                    break;
                }
                case SIMPLE: {
                    sb.insert(0, '\'');
                    sb.append('\'');
                    break;
                }
            }
        }
        return sb.toString();
    }

    public static List<String> parsePropertyIdList(String s) {
        return parseStringIdList(s);
    }

    public static List<String> parsePropertyStringList(String s) {
        return parseAndTrimToDistinctList(s);
    }

    public static List<String> split(String value, String chars) {
        return split(value, chars, true, false);
    }

    public static String repeat(char c, int count) {
        char[] e = new char[count];
        Arrays.fill(e, c);
        return new String(e);
    }

    public static String repeat(String str, int count) {
        if (count < 0) {
            throw new ArrayIndexOutOfBoundsException(count);
        }
        switch (count) {
            case 0:
                return "";
            case 1:
                return str;
        }
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static String alignLeft(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.append(repeat(' ', x));
            }
        }
        return sb.toString();
    }

    public static String alignRight(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.insert(0, repeat(' ', x));
            }
        }
        return sb.toString();
    }

    public static List<String> split(String value, String chars, boolean trim, boolean ignoreEmpty) {
        if (value == null) {
            value = "";
        }
        StringTokenizer st = new StringTokenizer(value, chars, true);
        List<String> all = new ArrayList<>();
        boolean wasSep = true;
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (chars.indexOf(s.charAt(0)) >= 0) {
                if (wasSep) {
                    s = "";
                    if (!ignoreEmpty) {
                        all.add(s);
                    }
                }
                wasSep = true;
            } else {
                wasSep = false;
                if (trim) {
                    s = s.trim();
                }
                if (!ignoreEmpty || !s.isEmpty()) {
                    all.add(s);
                }
            }
        }
        if (wasSep) {
            if (!ignoreEmpty) {
                all.add("");
            }
        }
        return all;
    }

    public static byte[] fromHexString(String s) {
        int len = s.length();
        if (len == 0) {
            return new byte[0];
        }
        if (s.length() % 2 == 1) {
            s = s + "0";
            len++;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            char c1 = s.charAt(i);
            char c2 = s.charAt(i + 1);
            data[i / 2] = (byte) ((Character.digit(c1, 16) << 4)
                    + Character.digit(c2, 16));
        }
        return data;
    }

    public static String replaceDollarPlaceHolder(String text, Function<String, String> mapper) {
        if (mapper == null) {
            return "";
        }
        return parseDollarPlaceHolder(text)
                .map(t -> {
                    switch (t.ttype) {
                        case NBootToken.TT_DOLLAR:
                        case NBootToken.TT_DOLLAR_BRACE: {
                            String x = mapper.apply(t.sval);
                            if (x == null) {
                                throw new IllegalArgumentException("var not found " + t.sval);
                            }
                            return x;
                        }
                    }
                    return t.sval;
                }).collect(Collectors.joining());
    }

    public static Stream<NBootToken> parseDollarPlaceHolder(String text) {
        final String TT_DEFAULT_STR = NBootToken.typeString(NBootToken.TT_DEFAULT);
        final String TT_DOLLAR_BRACE_STR = NBootToken.typeString(NBootToken.TT_DOLLAR_BRACE);
        final String TT_DOLLAR_STR = NBootToken.typeString(NBootToken.TT_DOLLAR);
        return iterToStream(new Iterator<NBootToken>() {
            final char[] t = (text == null ? new char[0] : text.toCharArray());
            int p = 0;
            final int length = t.length;
            final StringBuilder sb = new StringBuilder(length);
            final StringBuilder n = new StringBuilder(length);
            final StringBuilder ni = new StringBuilder(length);
            final List<NBootToken> buffer = new ArrayList<>(2);

            private boolean ready() {
                return !buffer.isEmpty();
            }

            @Override
            public boolean hasNext() {
                if (ready()) {
                    return true;
                }
                while (p < length) {
                    fillOnce();
                    if (ready()) {
                        return true;
                    }
                }
                if (sb.length() > 0) {
                    buffer.add(NBootToken.of(NBootToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                    sb.setLength(0);
                }
                return ready();
            }

            private void fillOnce() {
                char c = t[p];
                if (c == '$' && p + 1 < length && t[p + 1] == '{') {
                    p += 2;
                    n.setLength(0);
                    ni.setLength(0);
                    ni.append(c).append('{');
                    while (p < length) {
                        c = t[p];
                        if (c != '}') {
                            n.append(c);
                            ni.append(c);
                            p++;
                        } else {
                            ni.append(c);
                            break;
                        }
                    }
                    if (sb.length() > 0) {
                        buffer.add(NBootToken.of(NBootToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                        sb.setLength(0);
                    }
                    buffer.add(NBootToken.of(NBootToken.TT_DOLLAR_BRACE, n.toString(), 0, 0, ni.toString(), TT_DOLLAR_BRACE_STR));
                } else if (c == '$' && p + 1 < length && isValidVarStart(t[p + 1])) {
                    p++;
                    n.setLength(0);
                    ni.setLength(0);
                    ni.append(c);
                    while (p < length) {
                        c = t[p];
                        if (isValidVarPart(c)) {
                            n.append(c);
                            ni.append(c);
                            p++;
                        } else {
                            p--;
                            break;
                        }
                    }
                    if (sb.length() > 0) {
                        buffer.add(NBootToken.of(NBootToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                        sb.setLength(0);
                    }
                    buffer.add(NBootToken.of(NBootToken.TT_DOLLAR, n.toString(), 0, 0, ni.toString(), TT_DOLLAR_STR));
                } else {
                    sb.append(c);
                }
                p++;
            }

            @Override
            public NBootToken next() {
                requireTrue(ready(), "token ready");
                return buffer.remove(0);
            }
        });

    }

    public static boolean isValidVarPart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    public static boolean isValidVarStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static <T> Stream<T> iterToStream(Iterator<T> it) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false);
    }

    public static String toStringOrEmpty(Object any) {
        if (any == null) {
            return "";
        }
        return any.toString();
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static NBootMsg createMessage(Supplier<NBootMsg> msg) {
        requireNonNull(msg, "message supplier");
        NBootMsg m = msg.get();
        requireNonNull(m, "message");
        return m;
    }

    private static String createName(String name) {
        return isBlank(name) ? "value" : name;
    }

    public static <T> T requireNonNull(T object, Supplier<NBootMsg> msg) {
        if (object == null) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }

    public static <T> T requireNonNull(T object, String name) {
        return requireNonNull(object, () -> NBootMsg.ofC("%s should not be null", createName(name)));
    }

    public static <T> T requireNonNull(T object) {
        return requireNonNull(object, "value");
    }

    public static void requireNull(Object object, String name) {
        if (object != null) {
            throw creatIllegalArgumentException(NBootMsg.ofC("%s must be null", createName(name)));
        }
    }

    public static void requireNull(Object object, Supplier<NBootMsg> message) {
        if (object != null) {
            throw creatIllegalArgumentException(createMessage(message));
        }
    }

    public static String requireNonBlank(String object, String name) {
        if (isBlank(object)) {
            throw creatIllegalArgumentException(NBootMsg.ofC("%s should not be blank", createName(name)));
        }
        return object;
    }

    public static String requireNonBlank(String object, Supplier<NBootMsg> msg) {
        if (isBlank(object)) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }

    private static RuntimeException creatIllegalArgumentException(NBootMsg m) {
        throw new IllegalArgumentException(m.toString());
    }

    public static void requireNull(Object object) {
        requireNull(object, (String) null);
    }

    public static boolean requireTrue(boolean value, String name) {
        return requireTrue(value, () -> NBootMsg.ofC("should be %s", createName(name)));
    }

    public static boolean requireTrue(boolean object, Supplier<NBootMsg> msg) {
        if (!object) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }

    public static boolean requireFalse(boolean value, String name) {
        return requireFalse(value, () -> NBootMsg.ofC("should not be %s", createName(name)));
    }

    public static boolean requireFalse(boolean object, Supplier<NBootMsg> msg) {
        if (!object) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }


    public static int processThrowable(Throwable ex, String[] args, NBootOptionsInfo bootOptions, NBootLog bootLog) {
        if (ex == null) {
            return 0;
        } else {
            NExceptionBootAware u = findThrowable(ex, NExceptionBootAware.class, null);
            if (u != null) {
                return u.processThrowable(bootOptions, bootLog);
            } else {
                if (bootOptions == null) {
                    bootOptions = new NBootOptionsInfo();
                    NBootWorkspaceCmdLineParser.parseNutsArguments(args, bootOptions);
                }
                return processThrowable(ex, (NBootLog) bootLog, true, resolveShowStackTrace(bootOptions), resolveGui(bootOptions));
            }
        }
    }


    public static boolean resolveGui(NBootOptionsInfo bo) {
        if (bo == null) {
            return false;
        }
        if (bo.getBot() != null && bo.getBot()) {
            return false;
        } else if (bo.getGui() != null && bo.getGui()) {
            return isGraphicalDesktopEnvironment();
        } else {
            return false;
        }
    }

    public static int processThrowable(Throwable ex, NBootLog out, boolean showMessage, boolean showStackTrace, boolean showGui) {
        if (ex == null) {
            return 0;
        } else {
            String m = getErrorMessage(ex);
            if (out == null) {
                if (showMessage) {
                    System.err.println(NBootMsg.ofPlain(m));
                    if (showStackTrace) {
                        System.err.println(NBootMsg.ofPlain("---------------"));
                        System.err.println(NBootMsg.ofPlain(">  STACKTRACE :"));
                        System.err.println(NBootMsg.ofPlain("---------------"));
                        System.err.println(NBootMsg.ofPlain(stacktrace(ex)));
                    }
                }
                if (showGui) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(m);
                    if (showStackTrace && sb.length() > 0) {
                        sb.append("\n");
                        sb.append(stacktrace(ex));
                    }
                    showMessage(NBootMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
                }
            } else {
                if (showMessage) {
                    out.with().level(Level.OFF).verbFail().log(NBootMsg.ofPlain(m));
                    if (showStackTrace) {
                        out.with().level(Level.OFF).verbFail().log(NBootMsg.ofPlain("---------------"));
                        out.with().level(Level.OFF).verbFail().log(NBootMsg.ofPlain(">  STACKTRACE :"));
                        out.with().level(Level.OFF).verbFail().log(NBootMsg.ofPlain("---------------"));
                        out.with().level(Level.OFF).verbFail().log(NBootMsg.ofPlain(stacktrace(ex)));
                    }
                }
                if (showGui) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(m);
                    if (showStackTrace && sb.length() > 0) {
                        sb.append("\n");
                        sb.append(stacktrace(ex));
                    }
                    showMessage(NBootMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
                }
            }
            return 224;
        }
    }

    public static int exitIfError(Throwable ex, String[] args, NBootOptionsInfo bootOptions, NBootLog bootLog) {
        int code = processThrowable(ex, args, bootOptions, bootLog);
        if (code != 0) {
            System.exit(code);
        }
        return code;
    }

    public static int exitIfError(int code) {
        if (code != 0) {
            System.exit(code);
        }
        return code;
    }


    public static boolean resolveShowStackTrace(NBootOptionsInfo bo) {
        if (bo == null) {
            return true;
        }
        if (bo.getShowStacktrace() != null) {
            return bo.getShowStacktrace();
        } else if (bo.getBot() != null && bo.getBot()) {
            return false;
        } else {
            if (getSysBoolNutsProperty("stacktrace", false)) {
                return true;
            }
            if (bo.getDebug() != null && !isBlank(bo.getDebug())) {
                return true;
            }
            NBootLogConfig nLogConfig = bo.getLogConfig();
            if (nLogConfig != null && nLogConfig.getLogTermLevel() != null
                    && nLogConfig.getLogTermLevel().intValue() < Level.INFO.intValue()) {
                return true;
            }
            return false;
        }
    }


    public static String damerauLevenshteinClosest(double threshold, String str1, String[] dictionary) {
        if (threshold > 1) {
            threshold = 1;
        }
        if (threshold < 0) {
            threshold = 0;
        }
        double bestRelativeDistance = -1;
        String bestResult = null;
        if (str1 == null) {
            str1 = "";
        }
        for (String s : dictionary) {
            if (s == null) {
                s = "";
            }
            double u = computeOptionSimilarity(str1, s);
            if (u >= threshold) {
                if (bestResult == null || u > bestRelativeDistance) {
                    bestResult = s;
                    bestRelativeDistance = u;
                }
            }
        }
        return bestResult;
    }

    // Damerau-Levenshtein distance function
    public static int damerauLevenshtein(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,      // Deletion
                                dp[i][j - 1] + 1),     // Insertion
                        dp[i - 1][j - 1] + cost // Substitution
                );
                if (i > 1 && j > 1 && s1.charAt(i - 1) == s2.charAt(j - 2) && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + cost);
                }
            }
        }
        return dp[len1][len2];
    }


    public static double tokenSimilarity(String[] tokens1, String[] tokens2) {
        if (tokens1.length == 0 || tokens2.length == 0) return 0.0;

        double totalScore = 0;
        int totalComparisons = 0;

        for (String token1 : tokens1) {
            int minDistance = Integer.MAX_VALUE;
            String bestMatch = "";

            for (String token2 : tokens2) {
                int distance = damerauLevenshtein(token1, token2);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestMatch = token2;
                }
            }
            double maxLength = Math.max(token1.length(), bestMatch.length());
            double similarity = maxLength == 0 ? 1.0 : 1.0 - (double) minDistance / maxLength;
            totalScore += similarity;
            totalComparisons++;
        }

        return totalComparisons == 0 ? 0.0 : totalScore / totalComparisons;
    }

    public static double fuzzyJaccardSimilarity(String[] tokens1, String[] tokens2) {
        if (tokens1.length == 0 || tokens2.length == 0) return 0.0;

        Set<String> set1 = new HashSet<>(Arrays.asList(tokens1));
        Set<String> set2 = new HashSet<>(Arrays.asList(tokens2));
        double intersection = 0;
        double union = set1.size() + set2.size();
        for (String token1 : set1) {
            double bestMatchScore = 0.0;
            for (String token2 : set2) {
                int distance = damerauLevenshtein(token1, token2);
                double maxLen = Math.max(token1.length(), token2.length());
                double similarity = maxLen == 0 ? 1.0 : 1.0 - (double) distance / maxLen;

                if (similarity > bestMatchScore) {
                    bestMatchScore = similarity;
                }
            }
            intersection += bestMatchScore;
        }
        return intersection / union;
    }

    public static double computeOptionSimilarity(String word1, String word2) {
        String[] tokens1 = split(word1, " ;,\n\r\t|-_", true, true).toArray(new String[0]);
        String[] tokens2 = split(word2, " ;,\n\r\t|-_", true, true).toArray(new String[0]);
        double tokenEditSimilarity = tokenSimilarity(tokens1, tokens2);
        double fuzzyJaccardSimilarity = fuzzyJaccardSimilarity(tokens1, tokens2);
        return (tokenEditSimilarity * 0.6) + (fuzzyJaccardSimilarity * 0.4);
    }

    public static boolean isRemoteWorkspaceLocation(String _ws) {
        return _ws != null && _ws.matches("[a-z-]+://.*");
    }
}
