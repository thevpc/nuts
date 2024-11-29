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

import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.boot.reserved.cmdline.NBootArg;
import net.thevpc.nuts.boot.reserved.maven.NReservedMavenUtilsBoot;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
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

    public static String enumTitle(String s) {
        if (NBootStringUtils.isBlank(s)) {
            return "";
        }
        char[] charArray = s.trim().toCharArray();
        charArray[0] = Character.toUpperCase(charArray[0]);
        return new String(charArray);
    }

    public static String enumId(String s) {
        if (NBootStringUtils.isBlank(s)) {
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
        if (NBootStringUtils.isBlank(s)) {
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
            char c2 = charArray1[i];
            if (c1 == '-') {
                c1 = '_';
            } else {
                c1 = Character.toUpperCase(c1);
            }
            if (c2 == '-') {
                c2 = '_';
            } else {
                c2 = Character.toUpperCase(c1);
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
        if (NBootStringUtils.isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (NBootStringUtils.isBlank(workspace)) {
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
            if (NBootStringUtils.isBlank(javaHome) || "null".equals(javaHome)) {
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
        if (NBootStringUtils.isBlank(javaHome) || "null".equals(javaHome)) {
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
                =
                (s instanceof Enum) ? enumName(((Enum<?>) s).name())
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

    public static String getHome(String storeFolder, NBootOptionsBoot bOptions) {
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
        if (NBootStringUtils.isBlank(groupId)) {
            return NBootStringUtils.trim(artifactId);
        }
        return NBootStringUtils.trim(groupId) + ":" + NBootStringUtils.trim(artifactId);
    }

    public static String getIdLongName(String groupId, String artifactId, String version, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (!NBootStringUtils.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NBootStringUtils.trim(artifactId));
        if (version != null && !NBootStringUtils.isBlank(version)) {
            sb.append("#");
            sb.append(version);
        }
        if (!NBootStringUtils.isBlank(classifier)) {
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
        if (NBootStringUtils.isBlank(nutsId)) {
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

    public static void ndiUndo(NBootLog bLog) {
        //need to unset settings configuration.
        //what is the safest way to do so?
        String os = NBootPlatformHome.currentOsFamily();
        //windows is ignored because it does not define a global nuts environment
        if (NBootUtils.sameEnum(os, "LINUX") || NBootUtils.sameEnum(os, "MACOS")) {
            String bashrc = NBootUtils.sameEnum(os, "LINUX") ? ".bashrc" : ".bash_profile";
            Path sysrcFile = Paths.get(System.getProperty("user.home")).resolve(bashrc);
            if (Files.exists(sysrcFile)) {

                //these two lines will remove older versions of nuts ( before 0.8.0)
                ndiRemoveFileCommented2Lines(sysrcFile, "net.thevpc.app.nuts.toolbox.ndi configuration", true, bLog);
                ndiRemoveFileCommented2Lines(sysrcFile, "net.thevpc.app.nuts configuration", true, bLog);

                //this line will remove 0.8.0+ versions of nuts
                ndiRemoveFileCommented2Lines(sysrcFile, "net.thevpc.nuts configuration", true, bLog);
            }

            // if we have deleted a non default workspace, we will fall back to the default one
            // and will consider the latest version of it.
            // this is helpful if we are playing with multiple workspaces. The default workspace will always be
            // accessible when deleting others
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
                s1 = NBootStringUtils.trim(s1);
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
                s1 = NBootStringUtils.trim(s1);
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
            if (!NBootStringUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.ARCH, s);
            }
        }
        if (condition.getOs() != null) {
            s = condition.getOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootStringUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.OS, s);
            }
        }
        if (condition.getOsDist() != null) {
            s = condition.getOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootStringUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.OS_DIST, s);
            }
        }
        if (condition.getPlatform() != null) {
            s = formatStringIdList(condition.getPlatform());
            if (!NBootStringUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.PLATFORM, s);
            }
        }
        if (condition.getDesktopEnvironment() != null) {
            s = condition.getDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootStringUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.DESKTOP, s);
            }
        }
        if (condition.getProfiles() != null) {
            s = condition.getProfiles().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootStringUtils.isBlank(s)) {
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

    public static boolean isBootOptional(String name, NBootOptionsBoot bOptions) {
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

    public static boolean isBootOptional(NBootOptionsBoot bOptions) {
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
        if (NBootStringUtils.isBlank(s)) {
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
        if (NBootStringUtils.isBlank(s)) {
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
        String svalue = String.valueOf(any).trim().toLowerCase();
        if (svalue.isEmpty()) {
            return null;
        }
        if (svalue.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return true;
        }
        if (svalue.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return false;
        }
        return null;
    }

    public static String resolveNutsIdDigest() {
        //TODO COMMIT TO 0.8.4
        return resolveNutsIdDigest(NBootId.ofApi(NBootWorkspace.getVersion()), resolveClasspathURLs(NBootWorkspace.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NBootId id, URL[] urls) {
        return NBootIOUtilsBoot.getURLDigest(findClassLoaderJar(id, urls), null);
    }

    public static boolean isAcceptDependency(NBootDependency s, NBootOptionsBoot bOptions) {
        boolean bootOptionals = isBootOptional(bOptions);
        //by default ignore optionals
        String o = s.getOptional();
        if (NBootStringUtils.isBlank(o) || Boolean.parseBoolean(o)) {
            if (!bootOptionals && !isBootOptional(s.getArtifactId(), bOptions)) {
                return false;
            }
        }
        return isAcceptCondition(s.getCondition());
    }

    public static boolean isDependencyDefaultScope(String s1) {
        if (NBootStringUtils.isBlank(s1)) {
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
        if (!NBootStringUtils.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NBootStringUtils.trim(artifactId));
        if (version != null && !version.isBlank()) {
            sb.append("#");
            sb.append(version);
        }
        if (!NBootStringUtils.isBlank(classifier)) {
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
                sb.setPlatform(NBootStringUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.OS_DIST: {
                sb.setOsDist(NBootStringUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.ARCH: {
                sb.setArch(NBootStringUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.OS: {
                sb.setOs(NBootStringUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.DESKTOP: {
                sb.setDesktopEnvironment(NBootStringUtils.parsePropertyIdList(value));
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
        return NBootStringUtils.split(str, " ;,\n\r\t|", true, true);
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
        return NBootStringUtils.trimToNull(
                String.join(",", args)
        );
    }

    public static Integer parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        if (NBootStringUtils.isBlank(value)) {
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
                if (!NBootStringUtils.isBlank(value)) {
                    if (newList.add(NBootStringUtils.trim(value))) {
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
        return uniqueList(other).stream().filter(x -> x!=null && !blakifier.test(x)).collect(Collectors.toList());
    }

    public static List<String> uniqueNonBlankStringList(Collection<String> other) {
        return uniqueList(other).stream().filter(x -> !NBootStringUtils.isBlank(x)).collect(Collectors.toList());
    }

    public static <T> List<T> addUniqueNonBlankList(List<T> list, Collection<T> other, Predicate<T> blakifier) {
        if (other != null) {
            for (T t : other) {
                if (t!=null && !blakifier.test(t)) {
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
                title = "Nuts Package Manager - " + NBootWorkspace.getVersion();
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
            bLog.with().level(Level.OFF).verbWarning().log( NBootMsg.ofC("[Graphical Environment Unsupported] %s", title));
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
            bLog.with().level(Level.OFF).verbWarning().log( NBootMsg.ofC("[Graphical Environment Unsupported] %s", title));
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
                bLog.with().level(Level.WARNING).verbCache().log( NBootMsg.ofC("url will not be loaded (already in classloader) : %s", node.getURL()));
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
            if(info!=null) {
                fillBootDependencyNodes(info, urls, visitedIds, bLog);
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static URL findClassLoaderJar(NBootId id, URL[] urls) {
        for (URL url : urls) {
            NBootId[] nutsBootIds = NReservedMavenUtilsBoot.resolveJarIds(url);
            for (NBootId i : nutsBootIds) {
                if (NBootStringUtils.isBlank(id.getGroupId()) || i.getGroupId().equals(id.getGroupId())) {
                    if (NBootStringUtils.isBlank(id.getArtifactId()) || i.getArtifactId().equals(id.getArtifactId())) {
                        if (NBootStringUtils.isBlank(id.getVersion()) || i.getVersion().equals(id.getVersion())) {
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
                                            NBootLog bLog) {
        try {
            if (url != null) {
                if (contextClassLoader == null) {
                    return false;
                }
                File file = NBootIOUtilsBoot.toFile(url);
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
                                bLog.with().level(Level.FINEST).verbSuccess().log( NBootMsg.ofC("url %s is already in classpath. checked class %s successfully",
                                        url, clz));
                                return true;
                            } else {
                                bLog.with().level(Level.FINEST).verbInfo().log( NBootMsg.ofC("url %s is not in classpath. failed to check class %s",
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
        bLog.with().level(Level.FINEST).verbFail().log( NBootMsg.ofC("url %s is not in classpath. no class found to check", url));
        return false;
    }

    public static String resolveGroupIdPath(String groupId) {
        return groupId.replace('.', '/');
    }

    public static String resolveIdPath(NBootId id) {
        StringBuilder sb = new StringBuilder();
        sb.append(resolveGroupIdPath(id.getGroupId()));
        if (!NBootStringUtils.isBlank(id.getArtifactId())) {
            sb.append("/");
            sb.append(id.getArtifactId());
            if (!NBootStringUtils.isBlank(id.getVersion())) {
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
        if (!NBootStringUtils.isBlank(id.getVersion())) {
            sb.append("-").append(id.getVersion());
        }
        if (!NBootStringUtils.isBlank(extension)) {
            sb.append(".").append(extension);
        }
        return sb.toString();
    }

    public static String resolveFilePath(NBootId id, String extension) {
        String fileName = resolveFileName(id, extension);
        return resolveIdPath(id) + '/' + fileName;
    }
}
