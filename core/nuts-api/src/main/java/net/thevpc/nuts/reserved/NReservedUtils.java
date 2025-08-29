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
package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/15/17.
 *
 * @app.category Internal
 * @since 0.5.4
 */
public final class NReservedUtils {


    public static String[] parseComplexStrings(String complex) {
        if (complex == null) {
            complex = "";
        }
        final int EXPECT_REAL = 0;
        final int EXPECT_IMAG = 1;
        int STATUS = EXPECT_REAL;
        int i = 0;
        StringBuilder real = new StringBuilder();
        StringBuilder imag = new StringBuilder();
        char[] chars = complex.toCharArray();
        for (i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '+':
                case '-': {
                    if (STATUS == EXPECT_REAL) {
                        if (real.length() == 0 || real.toString().toLowerCase().endsWith("e")) {
                            real.append(chars[i]);
                        } else {
                            imag.append(chars[i]);
                            STATUS = EXPECT_IMAG;
                        }
                        break;
                    } else {
                        imag.append(chars[i]);
                    }
                    break;
                }
                case 'i':
                case 'î': {
                    if (STATUS == EXPECT_REAL) {
                        imag.append(real);
                        real.delete(0, real.length());
                        if (imag.length() == 0) {
                            imag.append("1");
                        } else if (imag.toString().equals("+") || imag.toString().equals("-")) {
                            imag.append("1");
                        }
                        STATUS = EXPECT_IMAG;
                    } else {
                        if (imag.length() == 0) {
                            imag.append("1");
                        } else if (imag.toString().equals("+") || imag.toString().equals("-")) {
                            imag.append("1");
                        }
                        //
                    }
                    break;
                }
                case '*': {
                    if (chars[i + 1] == 'i' || chars[i + 1] == 'î') {
                        if (STATUS == EXPECT_REAL) {
                            imag.append(real);
                            real.delete(0, real.length());
                            STATUS = EXPECT_IMAG;
                        } else {
                            //
                        }
                    } else {
                        //
                    }
                    break;
                }
                default: {
                    if ((chars[i] >= '0' && chars[i] <= '9') || chars[i] == '.' || chars[i] == 'E' || chars[i] == 'e') {
                        if (STATUS == EXPECT_REAL) {
                            real.append(chars[i]);
                        } else {
                            imag.append(chars[i]);
                        }
                    } else {

                        if (STATUS == EXPECT_REAL) {
                            real.append(chars[i]);
                        } else {
                            imag.append(chars[i]);
                        }
//                        real.delete(0,real.length());
//                        imag.delete(0,imag.length());
//                        real.append("NaN");
//                        imag.append("NaN");
                    }
                    break;
                }
            }
        }
        if (real.length() == 0) {
            real.append("0");
        }
        if (imag.length() == 0) {
            imag.append("0");
        }
        return new String[]{
                (real.toString()),
                (imag.toString())
        };
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


    public static String getIdShortName(String groupId, String artifactId, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (NBlankable.isBlank(classifier)) {
            if (!NBlankable.isBlank(groupId)) {
                sb.append(groupId).append(":");
            }
            sb.append(NStringUtils.trim(artifactId));
        } else {
            if (!NBlankable.isBlank(groupId)) {
                sb.append(groupId);
            }
            sb.append(":");
            sb.append(NStringUtils.trim(artifactId));
            sb.append(":");
            sb.append(classifier);
        }
        return sb.toString();
    }

    public static String getIdLongName(String groupId, String artifactId, NVersion version, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (NBlankable.isBlank(classifier)) {
            if (!NBlankable.isBlank(groupId)) {
                sb.append(groupId).append(":");
            }
            sb.append(NStringUtils.trim(artifactId));
        } else {
            if (!NBlankable.isBlank(groupId)) {
                sb.append(groupId);
            }
            sb.append(":");
            sb.append(NStringUtils.trim(artifactId));
            sb.append(":");
            sb.append(classifier);
        }
        if (!NBlankable.isBlank(version)) {
            sb.append("#");
            sb.append(version);
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
                NId ee = NId.get(e).get();
                if (ee.getShortName().equalsIgnoreCase(eos.id())) {
                    if (acceptVersion(ee.getVersion(), NVersion.of(System.getProperty("os.version")))) {
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
        NVersion a = NVersion.of(one.getLowerBound());
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
        a = NVersion.of(one.getUpperBound());
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
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("expected single value version: %s", other));
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
        nutsId = NStringUtils.trim(nutsId);
        if (NBlankable.isBlank(nutsId)) {
            return NOptional.of(NId.BLANK);
        }
        Matcher m = NId.PATTERN.matcher(nutsId);
        if (m.find()) {
            String group = m.group("group");
            String artifact = m.group("artifact");
            String classifier = m.group("classifier");
            String version = m.group("version");
            String query = m.group("query");
            if (artifact == null) {
                artifact = group;
                group = null;
            }
            LinkedHashSet<String> condArch = new LinkedHashSet<>();
            LinkedHashSet<String> condOs = new LinkedHashSet<>();
            LinkedHashSet<String> condDist = new LinkedHashSet<>();
            LinkedHashSet<String> condPlatform = new LinkedHashSet<>();
            LinkedHashSet<String> condDE = new LinkedHashSet<>();
            List<String> condProfiles = new ArrayList<>();
            Map<String, String> queryMap = NStringMapFormat.DEFAULT.parse(query).get();

            Map<String, String> idProperties = new LinkedHashMap<>();
            Map<String, String> condProperties = new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> e = iterator.next();
                String key = e.getKey();
                String value = e.getValue();
                switch (NStringUtils.trim(key)) {
                    case NConstants.IdProperties.CLASSIFIER: {
                        classifier = value;
                        break;
                    }
                    case NConstants.IdProperties.PROFILE: {
                        condProfiles.addAll(NReservedLangUtils.splitDefault(value));
                        break;
                    }
                    case NConstants.IdProperties.PLATFORM: {
                        condPlatform.addAll(NStringUtils.parsePropertyIdList(value).get());
                        break;
                    }
                    case NConstants.IdProperties.OS_DIST: {
                        condDist.addAll(NStringUtils.parsePropertyIdList(value).get());
                        break;
                    }
                    case NConstants.IdProperties.ARCH: {
                        condArch.addAll(NStringUtils.parsePropertyIdList(value).get());
                        break;
                    }
                    case NConstants.IdProperties.OS: {
                        condOs.addAll(NStringUtils.parsePropertyIdList(value).get());
                        break;
                    }
                    case NConstants.IdProperties.DESKTOP: {
                        condDE.addAll(NStringUtils.parsePropertyIdList(value).get());
                        break;
                    }
                    case NConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                        condProperties.putAll(NStringMapFormat.COMMA_FORMAT.parse(value).get());
                        break;
                    }
                    default: {
                        idProperties.put(key, value);
                    }
                }
            }
            return NOptional.of(new DefaultNId(
                    group, artifact, classifier, NVersion.of(version),
                    idProperties, new DefaultNEnvCondition(
                    new ArrayList<>(condArch), new ArrayList<>(condOs), new ArrayList<>(condDist), new ArrayList<>(condPlatform), new ArrayList<>(condDE),
                    new ArrayList<>(condProfiles), condProperties
            )
            ));
        }
        return NOptional.ofError(NMsg.ofC("invalid id format : %s", nutsId));
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
            bLog.log(NMsg.ofPlain("unable to update update " + filePath).asWarningAlert(ex));
            return false;
        }
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
                NOptional<NId> y = NId.get(x).ifBlankEmpty();
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
        if (condition.getProfiles() != null) {
            s = condition.getProfiles().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
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

}
