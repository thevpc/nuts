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

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.boot.reserved.cmdline.NArgBoot;
import net.thevpc.nuts.boot.reserved.NLogBoot;
import net.thevpc.nuts.boot.reserved.NMsgBoot;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/15/17.
 *
 * @app.category Internal
 * @since 0.5.4
 */
public final class NUtilsBoot {
    public static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    };
    public static String enumId(String s) {
        if (NStringUtilsBoot.isBlank(s)) {
            return "";
        }
        return NNameFormatBoot.LOWER_KEBAB_CASE.format(s.trim());
    }

    public static boolean sameEnum(String a, String b) {
        return Objects.equals(enumId(a), enumId(b));
    }

    public static String enumName(String s) {
        if (NStringUtilsBoot.isBlank(s)) {
            return "";
        }
        return NNameFormatBoot.CONST_NAME.format(s.trim());
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
        if (NStringUtilsBoot.isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (NStringUtilsBoot.isBlank(workspace)) {
            return NConstants.Names.DEFAULT_WORKSPACE_NAME;
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
        String exe = NNameFormatBoot.equalsIgnoreFormat(NPlatformHomeBoot.currentOsFamily(), "WINDOWS") ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (NStringUtilsBoot.isBlank(javaHome) || "null".equals(javaHome)) {
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
        if (NStringUtilsBoot.isBlank(javaHome) || "null".equals(javaHome)) {
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
                (s instanceof Enum) ? NNameFormatBoot.CONST_NAME.format(((Enum<?>) s).name())
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
        String a = NUtilsBoot.desc(unresolved);
        String b = NUtilsBoot.desc(resolved);
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
                NPlatformHomeBoot.ofSystem(bOptions.getStoreLayout()) :
                NPlatformHomeBoot.of(bOptions.getStoreLayout()))
                .getWorkspaceLocation(
                        storeFolder,
                        bOptions.getHomeLocations(),
                        bOptions.getName()
                );
    }


    public static String getIdShortName(String groupId, String artifactId) {
        if (NStringUtilsBoot.isBlank(groupId)) {
            return NStringUtilsBoot.trim(artifactId);
        }
        return NStringUtilsBoot.trim(groupId) + ":" + NStringUtilsBoot.trim(artifactId);
    }

    public static String getIdLongName(String groupId, String artifactId, String version, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (!NStringUtilsBoot.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NStringUtilsBoot.trim(artifactId));
        if (version != null && !NStringUtilsBoot.isBlank(version)) {
            sb.append("#");
            sb.append(version);
        }
        if (!NStringUtilsBoot.isBlank(classifier)) {
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
    }

    public static boolean isAcceptCondition(NEnvConditionBoot cond) {
        List<String> oss = NReservedLangUtilsBoot.uniqueNonBlankStringList(cond.getOs());
        List<String> archs = NReservedLangUtilsBoot.uniqueNonBlankStringList(cond.getArch());
        if (!oss.isEmpty()) {
            String eos = NPlatformHomeBoot.currentOsFamily();
            boolean osOk = false;
            for (String e : oss) {
                NIdBoot ee = NIdBoot.of(e);
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
    public static NIdBoot parseId(String nutsId) {
        if (NStringUtilsBoot.isBlank(nutsId)) {
            return new NIdBoot(null, null);
        }
        Matcher m = NIdBoot.PATTERN.matcher(nutsId);
        if (m.find()) {
            NIdBoot idBuilder = new NIdBoot();
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

            Map<String, String> queryMap = NStringMapFormatBoot.DEFAULT.parse(m.group("query"));
            NEnvConditionBoot conditionBuilder = new NEnvConditionBoot();

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
        throw new NBootException(NMsgBoot.ofC("invalid id format : %s", nutsId));
    }

    private static void setIdProperty(String key, String value, NIdBoot builder, NEnvConditionBoot sb, Map<String, String> props) {
        if (key == null) {
            return;
        }
        switch (key) {
            case NConstants.IdProperties.CLASSIFIER: {
                builder.setClassifier(value);
                break;
            }
            case NConstants.IdProperties.PROFILE: {
                sb.setProfile(NReservedLangUtilsBoot.splitDefault(value));
                break;
            }
            case NConstants.IdProperties.PLATFORM: {
                sb.setPlatform(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.OS_DIST: {
                sb.setOsDist(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.ARCH: {
                sb.setArch(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.OS: {
                sb.setOs(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.DESKTOP: {
                sb.setDesktopEnvironment(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                Map<String, String> mm = NStringMapFormatBoot.COMMA_FORMAT.parse(value);
                sb.setProperties(mm);
                break;
            }
            default: {
                props.put(key, value);
            }
        }
    }

    private static boolean ndiAddFileLine(Path filePath, String commentLine, String goodLine, boolean force,
                                          String ensureHeader, String headerReplace, NLogBoot bLog) {
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

    static boolean ndiRemoveFileCommented2Lines(Path filePath, String commentLine, boolean force, NLogBoot bLog) {
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
            bLog.with().level(Level.WARNING).verbWarning().error(ex).log(NMsgBoot.ofPlain("unable to update update " + filePath));
            return false;
        }
    }

    public static void ndiUndo(NLogBoot bLog) {
        //need to unset settings configuration.
        //what is the safest way to do so?
        String os = NPlatformHomeBoot.currentOsFamily();
        //windows is ignored because it does not define a global nuts environment
        if (NUtilsBoot.sameEnum(os, "LINUX") || NUtilsBoot.sameEnum(os, "MACOS")) {
            String bashrc = NUtilsBoot.sameEnum(os, "LINUX") ? ".bashrc" : ".bash_profile";
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
                Path nbase = Paths.get(System.getProperty("user.home")).resolve(".local/share/nuts/apps/" + NConstants.Names.DEFAULT_WORKSPACE_NAME + "/id/net/thevpc/nuts/nuts");
                if (Files.isDirectory(nbase)) {
                    latestDefaultVersion = Files.list(nbase).filter(f -> Files.exists(f.resolve(".nuts-bashrc")))
                            .map(x -> sysrcFile.getFileName().toString()).min((o1, o2) -> NVersionBoot.of(o2).compareTo(NVersionBoot.of(o1)))
                            .orElse(null);
                }
                if (latestDefaultVersion != null) {
                    ndiAddFileLine(sysrcFile, "net.thevpc.nuts configuration",
                            "source " + nbase.resolve(latestDefaultVersion).resolve(".nuts-bashrc"),
                            true, "#!.*", "#!/bin/sh", bLog);
                }
            } catch (Exception e) {
                //ignore
                bLog.with().level(Level.FINEST).verbFail().log(NMsgBoot.ofC("unable to undo NDI : %s", e.toString()));
            }
        }
    }

    public static String formatIdList(List<NIdBoot> s) {
        return s.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatIdArray(NIdBoot[] s) {
        return Arrays.stream(s).map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatStringIdList(List<String> s) {
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        if (s != null) {
            for (String s1 : s) {
                s1 = NStringUtilsBoot.trim(s1);
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
                s1 = NStringUtilsBoot.trim(s1);
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

    public static List<NIdBoot> parseIdList(String s) {
        List<NIdBoot> list = new ArrayList<>();
        List<String> o = parseStringIdList(s);
        for (String x : o) {
            NIdBoot y = NIdBoot.of(x);
            if (y == null) {
                return null;
            }
            list.add(y);
        }
        return list;
    }

    public static Map<String, String> toMap(NEnvConditionBoot condition) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s;
        if (condition.getArch() != null) {
            s = condition.getArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NStringUtilsBoot.isBlank(s)) {
                m.put(NConstants.IdProperties.ARCH, s);
            }
        }
        if (condition.getOs() != null) {
            s = condition.getOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NStringUtilsBoot.isBlank(s)) {
                m.put(NConstants.IdProperties.OS, s);
            }
        }
        if (condition.getOsDist() != null) {
            s = condition.getOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NStringUtilsBoot.isBlank(s)) {
                m.put(NConstants.IdProperties.OS_DIST, s);
            }
        }
        if (condition.getPlatform() != null) {
            s = formatStringIdList(condition.getPlatform());
            if (!NStringUtilsBoot.isBlank(s)) {
                m.put(NConstants.IdProperties.PLATFORM, s);
            }
        }
        if (condition.getDesktopEnvironment() != null) {
            s = condition.getDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NStringUtilsBoot.isBlank(s)) {
                m.put(NConstants.IdProperties.DESKTOP, s);
            }
        }
        if (condition.getProfiles() != null) {
            s = condition.getProfiles().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NStringUtilsBoot.isBlank(s)) {
                m.put(NConstants.IdProperties.PROFILE, s);
            }
        }
        if (condition.getProperties() != null) {
            Map<String, String> properties = condition.getProperties();
            if (!properties.isEmpty()) {
                m.put(NConstants.IdProperties.CONDITIONAL_PROPERTIES, NStringMapFormatBoot.DEFAULT.format(properties));
            }
        }
        return m;
    }

    public static boolean isBootOptional(String name, NBootOptionsBoot bOptions) {
        if (bOptions.getCustomOptions() != null) {
            for (String property : bOptions.getCustomOptions()) {
                NArgBoot a = new NArgBoot(property);
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
                NArgBoot a = new NArgBoot(property);
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
        if(NStringUtilsBoot.isBlank(s)){
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
        throw new NBootException(NMsgBoot.ofC("invalid instant %s", s));
    }

    public static Instant parseInstant(String s, Instant emptyValue, Instant errorValue) {
        if(NStringUtilsBoot.isBlank(s)){
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

    public static Boolean parseBoolean(String any,Boolean emptyValue,Boolean errorValue) {
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
        return resolveNutsIdDigest(NIdBoot.ofApi(NBootWorkspace.getVersion()), NReservedLangUtilsBoot.resolveClasspathURLs(NBootWorkspace.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NIdBoot id, URL[] urls) {
        return NReservedIOUtilsBoot.getURLDigest(NReservedLangUtilsBoot.findClassLoaderJar(id, urls), null);
    }

    public static boolean isAcceptDependency(NDependencyBoot s, NBootOptionsBoot bOptions) {
        boolean bootOptionals = isBootOptional(bOptions);
        //by default ignore optionals
        String o = s.getOptional();
        if (NStringUtilsBoot.isBlank(o) || Boolean.parseBoolean(o)) {
            if (!bootOptionals && !isBootOptional(s.getArtifactId(), bOptions)) {
                return false;
            }
        }
        return isAcceptCondition(s.getCondition());
    }

    public static String toDependencyExclusionListString(List<NIdBoot> exclusions) {
        TreeSet<String> ex = new TreeSet<>();
        for (NIdBoot exclusion : exclusions) {
            ex.add(exclusion.getShortName());
        }
        return String.join(",", ex);
    }

    public static boolean isDependencyDefaultScope(String s1) {
        if(NStringUtilsBoot.isBlank(s1)){
            return true;
        }
        return sameEnum(s1,"api");
    }

    public static boolean acceptVersion(String one, String other) {
        return acceptVersion(NVersionBoot.of(one), NVersionBoot.of(other));
    }

    public static boolean acceptVersion(NVersionBoot.NVersionIntervalBoot one, NVersionBoot other) {
        NVersionBoot a = NVersionBoot.of(one.getLowerBound());
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
        a = NVersionBoot.of(one.getUpperBound());
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

    public static boolean acceptVersion(NVersionBoot one, NVersionBoot other) {
        if (!other.isSingleValue()) {
            throw new NBootException(NMsgBoot.ofC("expected single value version: %s", other));
        }
        List<NVersionBoot.NVersionIntervalBoot> ii = one.intervals();
        if (ii.isEmpty()) {
            return true;
        }
        for (NVersionBoot.NVersionIntervalBoot i : ii) {
            if (acceptVersion(i, other)) {
                return true;
            }
        }
        return false;
    }
}
