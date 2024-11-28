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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public final class NReservedUtilsBoot {

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
        String exe = NUtilsBoot.sameEnum(NPlatformHomeBoot.currentOsFamily(),"WINDOWS") ? "java.exe" : "java";
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
        String a = NReservedUtilsBoot.desc(unresolved);
        String b = NReservedUtilsBoot.desc(resolved);
        if (a.equals(b)) {
            return a;
        } else {
            return a + " => " + b;
        }
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return
                NUtilsBoot.firstNonNull(NUtilsBoot.parseBoolean(System.getProperty("nuts." + property)),defaultValue,false)
                || NUtilsBoot.firstNonNull(NUtilsBoot.parseBoolean(System.getProperty("nuts.export." + property)),defaultValue,false)
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
        return (NUtilsBoot.firstNonNull(bOptions.getSystem(),false) ?
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

    public static String getIdLongName(String groupId, String artifactId, NVersionBoot version, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (!NStringUtilsBoot.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NStringUtilsBoot.trim(artifactId));
        if (version != null && !version.isBlank()) {
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

    public static boolean isAcceptDependency(NDependencyBoot s, NBootOptionsBoot bOptions) {
        boolean bootOptionals = isBootOptional(bOptions);
        //by default ignore optionals
        String o = s.getOptional();
        if (NStringUtilsBoot.isBlank(o) || Boolean.parseBoolean(o)) {
            if (!bootOptionals && !isBootOptional(s.getArtifactId(), bOptions)) {
                return false;
            }
        }
        return NEnvConditionBoot.isAcceptCondition(s.getCondition());
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
        return NUtilsBoot.sameEnum(s1,"api");
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
            return NIdBoot.BLANK;
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
            return Collections.emptyList();
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
        return new ArrayList<>(allIds);
    }

    public static List<NIdBoot> parseIdList(String s) {
        List<NIdBoot> list = new ArrayList<>();
        List<String> o = parseStringIdList(s);
        if (o!=null) {
            for (String x : o) {
                NIdBoot y = NIdBoot.of(x);
                if (y!=null) {
                    list.add(y);
                }
            }
            return list;
        }
        return null;
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

    static boolean isBootOptional(String name, NBootOptionsBoot bOptions) {
        if(bOptions.getCustomOptions()!=null) {
            for (String property : bOptions.getCustomOptions()) {
                NArgBoot a = new NArgBoot(property);
                if (("boot-" + name).equals(a.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isBootOptional(NBootOptionsBoot bOptions) {
        if(bOptions.getCustomOptions()!=null) {
            for (String property : bOptions.getCustomOptions()) {
                NArgBoot a = new NArgBoot(property);
                if (("boot-optional").equals(a.getKey())) {
                    return NUtilsBoot.parseBooleanOr(a.getValue(),true);
                }
            }
        }
        return true;
    }
}
