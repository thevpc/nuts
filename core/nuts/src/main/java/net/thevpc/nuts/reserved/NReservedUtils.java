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
package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.boot.NBootOptions;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.nio.file.*;
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
public final class NReservedUtils {


    public static boolean isValidWorkspaceName(String workspace) {
        if (NBlankable.isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (NBlankable.isBlank(workspace)) {
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
        String exe = NOsFamily.getCurrent().equals(NOsFamily.WINDOWS) ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (NBlankable.isBlank(javaHome) || "null".equals(javaHome)) {
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
        if (NBlankable.isBlank(javaHome) || "null".equals(javaHome)) {
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
                (s instanceof Enum) ? NNameFormat.CONST_NAME.format(((Enum<?>) s).name())
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
        String a = NReservedUtils.desc(unresolved);
        String b = NReservedUtils.desc(resolved);
        if (a.equals(b)) {
            return a;
        } else {
            return a + " => " + b;
        }
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return
                NLiteral.of(System.getProperty("nuts." + property)).asBoolean().ifEmpty(defaultValue).orElse(false)
                        || NLiteral.of(System.getProperty("nuts.export." + property)).asBoolean().ifEmpty(defaultValue).orElse(false)
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

    public static String getHome(NStoreType storeFolder, NBootOptions bOptions) {
        return (bOptions.getSystem().orElse(false) ?
                NPlatformHome.ofSystem(bOptions.getStoreLayout().orNull()) :
                NPlatformHome.of(bOptions.getStoreLayout().orNull()))
                .getWorkspaceLocation(
                        storeFolder,
                        bOptions.getHomeLocations().orNull(),
                        bOptions.getName().orNull()
                );
    }


    public static String getIdShortName(String groupId, String artifactId) {
        if (NBlankable.isBlank(groupId)) {
            return NStringUtils.trim(artifactId);
        }
        return NStringUtils.trim(groupId) + ":" + NStringUtils.trim(artifactId);
    }

    public static String getIdLongName(String groupId, String artifactId, NVersion version, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (!NBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NStringUtils.trim(artifactId));
        if (version != null && !version.isBlank()) {
            sb.append("#");
            sb.append(version);
        }
        if (!NBlankable.isBlank(classifier)) {
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
    }

    public static boolean isAcceptCondition(NEnvCondition cond) {
        List<String> oss = NReservedLangUtils.uniqueNonBlankList(cond.getOs());
        List<String> archs = NReservedLangUtils.uniqueNonBlankList(cond.getArch());
        if (!oss.isEmpty()) {
            NOsFamily eos = NOsFamily.getCurrent();
            boolean osOk = false;
            for (String e : oss) {
                NId ee = NId.of(e).get();
                if (ee.getShortName().equalsIgnoreCase(eos.id())) {
                    if (acceptVersion(ee.getVersion(), NVersion.of(System.getProperty("os.version")).get())) {
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

    public static boolean isAcceptDependency(NDependency s, NBootOptions bOptions) {
        boolean bootOptionals = isBootOptional(bOptions);
        //by default ignore optionals
        String o = s.getOptional();
        if (NBlankable.isBlank(o) || Boolean.parseBoolean(o)) {
            if (!bootOptionals && !isBootOptional(s.getArtifactId(), bOptions)) {
                return false;
            }
        }
        return isAcceptCondition(s.getCondition());
    }

    public static String toDependencyExclusionListString(List<NId> exclusions) {
        TreeSet<String> ex = new TreeSet<>();
        for (NId exclusion : exclusions) {
            ex.add(exclusion.getShortName());
        }
        return String.join(",", ex);
    }

    public static boolean isDependencyDefaultScope(String s1) {
        return NDependencyScope.parse(s1).orElse(NDependencyScope.API) == NDependencyScope.API;
    }

    public static boolean acceptVersion(NVersionInterval one, NVersion other) {
        NVersion a = NVersion.of(one.getLowerBound()).get();
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
        a = NVersion.of(one.getUpperBound()).get();
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

    public static boolean acceptVersion(NVersion one, NVersion other) {
        if (!other.isSingleValue()) {
            throw new NBootException(NMsg.ofC("expected single value version: %s", other));
        }
        List<NVersionInterval> ii = one.intervals().get();
        if (ii.isEmpty()) {
            return true;
        }
        for (NVersionInterval i : ii) {
            if (acceptVersion(i, other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * examples : script://groupId:artifactId/version?face
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @param nutsId nutsId
     * @return nutsId
     */
    public static NOptional<NId> parseId(String nutsId) {
        if (NBlankable.isBlank(nutsId)) {
            return NOptional.of(NId.BLANK);
        }
        Matcher m = NId.PATTERN.matcher(nutsId);
        if (m.find()) {
            NIdBuilder idBuilder = NIdBuilder.of();
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

            Map<String, String> queryMap = NStringMapFormat.DEFAULT.parse(m.group("query")).get();
            NEnvConditionBuilder conditionBuilder = new DefaultNEnvConditionBuilder();

            Map<String, String> idProperties = new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> e = iterator.next();
                String key = e.getKey();
                String value = e.getValue();
                setIdProperty(key, value, idBuilder, conditionBuilder, idProperties);
            }

            return NOptional.of(idBuilder.setCondition(conditionBuilder)
                    .setProperties(idProperties).build());
        }
        return NOptional.ofError(session -> NMsg.ofC("invalid id format : %s", nutsId));
    }

    private static void setIdProperty(String key, String value, NIdBuilder builder, NEnvConditionBuilder sb, Map<String, String> props) {
        if (key == null) {
            return;
        }
        switch (key) {
            case NConstants.IdProperties.CLASSIFIER: {
                builder.setClassifier(value);
                break;
            }
            case NConstants.IdProperties.PROFILE: {
                sb.setProfile(NReservedLangUtils.splitDefault(value));
                break;
            }
            case NConstants.IdProperties.PLATFORM: {
                sb.setPlatform(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.OS_DIST: {
                sb.setOsDist(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.ARCH: {
                sb.setArch(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.OS: {
                sb.setOs(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.DESKTOP: {
                sb.setDesktopEnvironment(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                Map<String, String> mm = NStringMapFormat.COMMA_FORMAT.parse(value).get();
                sb.setProperties(mm);
                break;
            }
            default: {
                props.put(key, value);
            }
        }
    }

    private static boolean ndiAddFileLine(Path filePath, String commentLine, String goodLine, boolean force,
                                          String ensureHeader, String headerReplace, NLog bLog) {
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

    static boolean ndiRemoveFileCommented2Lines(Path filePath, String commentLine, boolean force, NLog bLog) {
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
            bLog.with().level(Level.WARNING).verb(NLogVerb.WARNING).error(ex).log(NMsg.ofPlain("unable to update update " + filePath));
            return false;
        }
    }

    public static void ndiUndo(NLog bLog) {
        //need to unset settings configuration.
        //what is the safest way to do so?
        NOsFamily os = NOsFamily.getCurrent();
        //windows is ignored because it does not define a global nuts environment
        if (os == NOsFamily.LINUX || os == NOsFamily.MACOS) {
            String bashrc = os == NOsFamily.LINUX ? ".bashrc" : ".bash_profile";
            Path sysrcFile = Paths.get(System.getProperty("user.home")).resolve(bashrc);
            if (Files.exists(sysrcFile)) {

                //these two lines will remove older versions of nuts ( before 0.8.0)
                ndiRemoveFileCommented2Lines(sysrcFile, "net.vpc.app.nuts.toolbox.ndi configuration", true, bLog);
                ndiRemoveFileCommented2Lines(sysrcFile, "net.vpc.app.nuts configuration", true, bLog);

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
                            .map(x -> sysrcFile.getFileName().toString()).min((o1, o2) -> NVersion.of(o2).get().compareTo(NVersion.of(o1).get()))
                            .orElse(null);
                }
                if (latestDefaultVersion != null) {
                    ndiAddFileLine(sysrcFile, "net.thevpc.nuts configuration",
                            "source " + nbase.resolve(latestDefaultVersion).resolve(".nuts-bashrc"),
                            true, "#!.*", "#!/bin/sh", bLog);
                }
            } catch (Exception e) {
                //ignore
                bLog.with().level(Level.FINEST).verb(NLogVerb.FAIL).log(NMsg.ofC("unable to undo NDI : %s", e.toString()));
            }
        }
    }

    public static String formatIdList(List<NId> s) {
        return s.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatIdArray(NId[] s) {
        return Arrays.stream(s).map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatStringIdList(List<String> s) {
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        if (s != null) {
            for (String s1 : s) {
                s1 = NStringUtils.trim(s1);
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
                s1 = NStringUtils.trim(s1);
                if (s1.length() > 0) {
                    allIds.add(s1);
                }
            }
        }
        return String.join(",", allIds);
    }

    public static NOptional<List<String>> parseStringIdList(String s) {
        if (s == null) {
            return NOptional.of(Collections.emptyList());
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
        return NOptional.of(new ArrayList<>(allIds));
    }

    public static NOptional<List<NId>> parseIdList(String s) {
        List<NId> list = new ArrayList<>();
        NOptional<List<String>> o = parseStringIdList(s);
        if (o.isPresent()) {
            for (String x : o.get()) {
                NOptional<NId> y = NId.of(x).ifBlankEmpty();
                if (y.isError()) {
                    return NOptional.ofError(y.getMessage());
                }
                if (y.isPresent()) {
                    list.add(y.get());
                }
            }
            return NOptional.of(list);
        }
        if (o.isError()) {
            return NOptional.ofError(o.getMessage());
        }
        return NOptional.ofEmpty(o.getMessage());
    }

    public static Map<String, String> toMap(NEnvCondition condition) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s;
        if (condition.getArch() != null) {
            s = condition.getArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBlankable.isBlank(s)) {
                m.put(NConstants.IdProperties.ARCH, s);
            }
        }
        if (condition.getOs() != null) {
            s = condition.getOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBlankable.isBlank(s)) {
                m.put(NConstants.IdProperties.OS, s);
            }
        }
        if (condition.getOsDist() != null) {
            s = condition.getOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBlankable.isBlank(s)) {
                m.put(NConstants.IdProperties.OS_DIST, s);
            }
        }
        if (condition.getPlatform() != null) {
            s = formatStringIdList(condition.getPlatform());
            if (!NBlankable.isBlank(s)) {
                m.put(NConstants.IdProperties.PLATFORM, s);
            }
        }
        if (condition.getDesktopEnvironment() != null) {
            s = condition.getDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBlankable.isBlank(s)) {
                m.put(NConstants.IdProperties.DESKTOP, s);
            }
        }
        if (condition.getProfile() != null) {
            s = condition.getProfile().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBlankable.isBlank(s)) {
                m.put(NConstants.IdProperties.PROFILE, s);
            }
        }
        if (condition.getProperties() != null) {
            Map<String, String> properties = condition.getProperties();
            if (!properties.isEmpty()) {
                m.put(NConstants.IdProperties.CONDITIONAL_PROPERTIES, NStringMapFormat.DEFAULT.format(properties));
            }
        }
        return m;
    }

    static boolean isBootOptional(String name, NBootOptions bOptions) {
        for (String property : bOptions.getCustomOptions().orElseGet(Collections::emptyList)) {
            NArg a = NArg.of(property);
            if (a.getKey().asString().orElse("").equals("boot-" + name)) {
                return true;
            }
        }
        return false;
    }

    static boolean isBootOptional(NBootOptions bOptions) {
        for (String property : bOptions.getCustomOptions().orElseGet(Collections::emptyList)) {
            NArg a = NArg.of(property);
            if (a.getKey().asString().orElse("").equals("boot-optional")) {
                return a.getValue().asBoolean().orElse(true);
            }
        }
        return true;
    }
}
