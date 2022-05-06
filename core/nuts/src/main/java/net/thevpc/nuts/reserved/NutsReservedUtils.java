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
package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNutsWorkspaceBootOptionsBuilder;
import net.thevpc.nuts.util.NutsApiUtils;
import net.thevpc.nuts.boot.NutsWorkspaceBootOptions;
import net.thevpc.nuts.boot.NutsWorkspaceBootOptionsBuilder;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsArrayElementBuilder;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
public final class NutsReservedUtils {


    private static final String DELETE_FOLDERS_HEADER = "ATTENTION ! You are about to delete nuts workspace files.";

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

    public static String idToPath(NutsId id) {
        return id.getGroupId().replace('.', '/') + "/"
                + id.getArtifactId() + "/" + id.getVersion();
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
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
                =
                (s instanceof Enum) ?NutsNameFormat.CONST_NAME.formatName(((Enum<?>) s).name())
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
        String a = NutsReservedUtils.desc(unresolved);
        String b = NutsReservedUtils.desc(resolved);
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
        File f = NutsReservedIOUtils.toFile(url);
        if (f != null) {
            return f.getPath();
        }
        return url.toString();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return
                NutsValue.of(System.getProperty("nuts." + property)).asBoolean().ifEmpty(defaultValue).orElse(false)
                        || NutsValue.of(System.getProperty("nuts.export." + property)).asBoolean().ifEmpty(defaultValue).orElse(false)
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

    public static String getHome(NutsStoreLocation storeFolder, NutsWorkspaceBootOptions bOptions) {
        return NutsPlatformUtils.getPlatformHomeFolder(
                bOptions.getStoreLocationLayout().orNull(),
                storeFolder,
                bOptions.getHomeLocations().orNull(),
                bOptions.getGlobal().orElse(false),
                bOptions.getName().orNull()
        );
    }


    public static String getStoreLocationPath(NutsWorkspaceBootOptions bOptions, NutsStoreLocation value) {
        Map<NutsStoreLocation, String> storeLocations = bOptions.getStoreLocations().orNull();
        if (storeLocations != null) {
            return storeLocations.get(value);
        }
        return null;
    }

    /**
     * @param includeRoot true if include root
     * @param locations   of type NutsStoreLocation, Path of File
     */
    public static long deleteStoreLocations(NutsWorkspaceBootOptions lastBootOptions, NutsWorkspaceBootOptions o, boolean includeRoot,
                                            NutsReservedBootLog bLog, Object[] locations) {
        if (lastBootOptions == null) {
            return 0;
        }
        NutsConfirmationMode confirm = o.getConfirm().orElse(NutsConfirmationMode.ASK);
        if (confirm == NutsConfirmationMode.ASK
                && o.getOutputFormat().orElse(NutsContentType.PLAIN) != NutsContentType.PLAIN) {
            throw new NutsBootException(
                    NutsMessage.ofPlain("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), 243);
        }
        bLog.log(Level.FINE, NutsLoggerVerb.WARNING, NutsMessage.ofJstyle("delete workspace location(s) at : {0}", lastBootOptions.getWorkspace()));
        boolean force = false;
        switch (confirm) {
            case ASK: {
                break;
            }
            case YES: {
                force = true;
                break;
            }
            case NO:
            case ERROR: {
                bLog.log(Level.WARNING, NutsLoggerVerb.WARNING, NutsMessage.ofPlain("reset cancelled (applied '--no' argument)"));
                throw new NutsNoSessionCancelException(NutsMessage.ofPlain("cancel delete folder"));
            }
        }
        NutsWorkspaceConfigManager conf = null;
        List<Path> folders = new ArrayList<>();
        if (includeRoot) {
            folders.add(Paths.get(lastBootOptions.getWorkspace().get()));
        }
        for (Object ovalue : locations) {
            if (ovalue != null) {
                if (ovalue instanceof NutsStoreLocation) {
                    NutsStoreLocation value = (NutsStoreLocation) ovalue;
                    String p = getStoreLocationPath(lastBootOptions, value);
                    if (p != null) {
                        folders.add(Paths.get(p));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue));
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue).toPath());
                } else {
                    throw new NutsBootException(NutsMessage.ofCstyle("unsupported path type : %s", ovalue));
                }
            }
        }
        NutsWorkspaceBootOptionsBuilder optionsCopy = o.builder();
        if (optionsCopy.getBot().orElse(false) || !NutsReservedGuiUtils.isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return deleteAndConfirmAll(folders.toArray(new Path[0]), force, DELETE_FOLDERS_HEADER, null, bLog, optionsCopy);
    }

    public static long deleteAndConfirmAll(Path[] folders, boolean force, String header, NutsSession session,
                                           NutsReservedBootLog bLog, NutsWorkspaceBootOptions bOptions) {
        return deleteAndConfirmAll(folders, force, new NutsReservedDeleteFilesContextImpl(), header, session, bLog, bOptions);
    }

    private static long deleteAndConfirmAll(Path[] folders, boolean force, NutsReservedDeleteFilesContext refForceAll,
                                            String header, NutsSession session, NutsReservedBootLog bLog, NutsWorkspaceBootOptions bOptions) {
        long count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            for (Path child : folders) {
                if (Files.exists(child)) {
                    if (!headerWritten) {
                        headerWritten = true;
                        if (!force && !refForceAll.isForce()) {
                            if (header != null) {
                                if (!bOptions.getBot().orElse(false)) {
                                    if (session != null) {
                                        session.err().println(header);
                                    } else {
                                        bLog.log(Level.WARNING, NutsLoggerVerb.WARNING, NutsMessage.ofJstyle("{0}", header));
                                    }
                                }
                            }
                        }
                    }
                    count += deleteAndConfirm(child, force, refForceAll, session, bLog, bOptions);
                }
            }
        }
        return count;
    }

    private static long deleteAndConfirm(Path directory, boolean force, NutsReservedDeleteFilesContext refForceAll,
                                         NutsSession session, NutsReservedBootLog bLog, NutsWorkspaceBootOptions bOptions) {
        if (Files.exists(directory)) {
            if (!force && !refForceAll.isForce() && refForceAll.accept(directory)) {
                String line = null;
                if (session != null) {
                    line = session.getTerminal().ask()
                            .resetLine()
                            .forString(
                                    NutsMessage.ofCstyle(
                                            "do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory
                                    )).setSession(session).getValue();
                } else {
                    if (bOptions.getBot().orElse(false)) {
                        if (bOptions.getConfirm().orElse(NutsConfirmationMode.ASK) == NutsConfirmationMode.YES) {
                            line = "y";
                        } else {
                            throw new NutsBootException(NutsMessage.ofPlain("failed to delete files in --bot mode without auto confirmation"));
                        }
                    } else {
                        if (bOptions.getGui().orElse(false)) {
                            line = NutsReservedGuiUtils.inputString(
                                    NutsMessage.ofCstyle("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).toString(),
                                    null, () -> bLog.readLine(), bLog.err()
                            );
                        } else {
                            NutsConfirmationMode cc = bOptions.getConfirm().orElse(NutsConfirmationMode.ASK);
                            switch (cc) {
                                case YES: {
                                    line = "y";
                                    break;
                                }
                                case NO: {
                                    line = "n";
                                    break;
                                }
                                case ERROR: {
                                    throw new NutsBootException(NutsMessage.ofPlain("error response"));
                                }
                                case ASK: {
                                    // Level.OFF is to force logging in all cases
                                    bLog.log(Level.OFF, NutsLoggerVerb.WARNING, NutsMessage.ofJstyle("do you confirm deleting {0} [y/n/c/a] (default 'n') ? : ", directory));
                                    line = bLog.readLine();
                                }
                            }
                        }
                    }
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll.setForce(true);
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NutsCancelException(session);
                } else if (!NutsValue.of(line).asBoolean().orElse(false)) {
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
//                        LOG.log(Level.FINEST, NutsLogVerb.WARNING, "delete file   : {0}", file);
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
                bLog.log(Level.FINEST, NutsLoggerVerb.WARNING, NutsMessage.ofJstyle("delete folder : {0} ({1} files/folders deleted)", directory, count[0]));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return count[0];
        }
        return 0;
    }

    public static String getIdShortName(String groupId, String artifactId) {
        if (NutsBlankable.isBlank(groupId)) {
            return NutsStringUtils.trim(artifactId);
        }
        return NutsStringUtils.trim(groupId) + ":" + NutsStringUtils.trim(artifactId);
    }

    public static String getIdLongName(String groupId, String artifactId, NutsVersion version, String classifier) {
        StringBuilder sb = new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NutsStringUtils.trim(artifactId));
        if (version != null && !version.isBlank()) {
            sb.append("#");
            sb.append(version);
        }
        if (!NutsBlankable.isBlank(classifier)) {
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
    }

    public static boolean isAcceptDependency(NutsDependency s, NutsWorkspaceBootOptions bOptions) {
        boolean bootOptionals = NutsReservedNutsUtilWorkspaceOptions.isBootOptional(bOptions);
        //by default ignore optionals
        String o = s.getOptional();
        if (NutsBlankable.isBlank(o) || Boolean.parseBoolean(o)) {
            if (!bootOptionals && !NutsReservedNutsUtilWorkspaceOptions.isBootOptional(s.getArtifactId(), bOptions)) {
                return false;
            }
        }
        List<String> oss = NutsReservedCollectionUtils.uniqueNonBlankList(s.getCondition().getOs());
        List<String> archs = NutsReservedCollectionUtils.uniqueNonBlankList(s.getCondition().getArch());
        if (oss.isEmpty()) {
            oss.add("");
        }
        if (archs.isEmpty()) {
            archs.add("");
        }
        if (!oss.isEmpty()) {
            NutsOsFamily eos = NutsOsFamily.getCurrent();
            boolean osOk = false;
            for (String e : oss) {
                NutsId ee = NutsId.of(e).get();
                if (ee.getShortName().equalsIgnoreCase(eos.id())) {
                    if (acceptVersion(ee.getVersion(), NutsVersion.of(System.getProperty("os.version")).get())) {
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

    public static String toDependencyExclusionListString(List<NutsId> exclusions) {
        TreeSet<String> ex = new TreeSet<>();
        for (NutsId exclusion : exclusions) {
            ex.add(exclusion.getShortName());
        }
        return String.join(",", ex);
    }

    public static boolean isDependencyDefaultScope(String s1) {
        return NutsDependencyScope.parse(s1).orElse(NutsDependencyScope.API) == NutsDependencyScope.API;
    }

    public static boolean acceptVersion(NutsVersionInterval one, NutsVersion other) {
        NutsVersion a = NutsVersion.of(one.getLowerBound()).get();
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
        a = NutsVersion.of(one.getUpperBound()).get();
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

    public static boolean acceptVersion(NutsVersion one, NutsVersion other) {
        if (!other.isSingleValue()) {
            throw new NutsBootException(NutsMessage.ofCstyle("expected single value version: %s", other));
        }
        List<NutsVersionInterval> ii = one.intervals().get();
        if (ii.isEmpty()) {
            return true;
        }
        for (NutsVersionInterval i : ii) {
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
    public static NutsOptional<NutsId> parseId(String nutsId) {
        if (NutsBlankable.isBlank(nutsId)) {
            return NutsOptional.of(NutsId.BLANK);
        }
        Matcher m = NutsId.PATTERN.matcher(nutsId);
        if (m.find()) {
            NutsIdBuilder idBuilder = NutsIdBuilder.of();
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

            Map<String, String> queryMap = NutsStringUtils.parseDefaultMap(m.group("query")).get();
            NutsEnvConditionBuilder conditionBuilder = new DefaultNutsEnvConditionBuilder();

            Map<String, String> idProperties = new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> e = iterator.next();
                String key = e.getKey();
                String value = e.getValue();
                setIdProperty(key, value, idBuilder, conditionBuilder, idProperties);
            }

            return NutsOptional.of(idBuilder.setCondition(conditionBuilder)
                    .setProperties(idProperties).build());
        }
        return NutsOptional.ofError(session -> NutsMessage.ofCstyle("invalid id format : %s", nutsId));
    }

    private static void setIdProperty(String key, String value, NutsIdBuilder builder, NutsEnvConditionBuilder sb, Map<String, String> props) {
        switch (key) {
            case NutsConstants.IdProperties.CLASSIFIER: {
                builder.setClassifier(value);
                break;
            }
            case NutsConstants.IdProperties.PROFILE: {
                sb.setProfile(NutsReservedStringUtils.splitDefault(value));
                break;
            }
            case NutsConstants.IdProperties.PLATFORM: {
                sb.setPlatform(NutsStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.OS_DIST: {
                sb.setOsDist(NutsStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.ARCH: {
                sb.setArch(NutsStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.OS: {
                sb.setOs(NutsStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.DESKTOP: {
                sb.setDesktopEnvironment(NutsStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                Map<String, String> mm = NutsStringUtils.parseMap(value, "=", ",").get();
                sb.setProperties(mm);
                break;
            }
            default: {
                props.put(key, value);
            }
        }
    }

    private static boolean ndiAddFileLine(Path filePath, String commentLine, String goodLine, boolean force,
                                          String ensureHeader, String headerReplace, NutsReservedBootLog bLog) {
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

    static boolean ndiRemoveFileCommented2Lines(Path filePath, String commentLine, boolean force, NutsReservedBootLog bLog) {
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
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            }
            return updatedFile;
        } catch (IOException ex) {
            bLog.log(Level.WARNING, NutsMessage.ofPlain("unable to update update " + filePath), ex);
            return false;
        }
    }

    public static void ndiUndo(NutsReservedBootLog bLog) {
        //need to unset settings configuration.
        //what is the safest way to do so?
        NutsOsFamily os = NutsOsFamily.getCurrent();
        //windows is ignored because it does not define a global nuts environment
        if (os == NutsOsFamily.LINUX || os == NutsOsFamily.MACOS) {
            String bashrc = os == NutsOsFamily.LINUX ? ".bashrc" : ".bash_profile";
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
                Path nbase = Paths.get(System.getProperty("user.home")).resolve(".local/share/nuts/apps/" + NutsConstants.Names.DEFAULT_WORKSPACE_NAME + "/id/net/thevpc/nuts/nuts");
                if (Files.isDirectory(nbase)) {
                    latestDefaultVersion = Files.list(nbase).filter(f -> Files.exists(f.resolve(".nuts-bashrc")))
                            .map(x -> sysrcFile.getFileName().toString()).min((o1, o2) -> NutsVersion.of(o2).get().compareTo(NutsVersion.of(o1).get()))
                            .orElse(null);
                }
                if (latestDefaultVersion != null) {
                    ndiAddFileLine(sysrcFile, "net.thevpc.nuts configuration",
                            "source " + nbase.resolve(latestDefaultVersion).resolve(".nuts-bashrc"),
                            true, "#!.*", "#!/bin/sh", bLog);
                }
            } catch (Exception e) {
                //ignore
                bLog.log(Level.FINEST, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("unable to undo NDI : {0}", e.toString()));
            }
        }
    }

    public static String formatIdList(List<NutsId> s) {
        return s.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatIdArray(NutsId[] s) {
        return Arrays.stream(s).map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatStringIdList(List<String> s) {
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        if (s != null) {
            for (String s1 : s) {
                s1 = NutsStringUtils.trim(s1);
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
                s1 = NutsStringUtils.trim(s1);
                if (s1.length() > 0) {
                    allIds.add(s1);
                }
            }
        }
        return String.join(",", allIds);
    }

    public static NutsOptional<List<String>> parseStringIdList(String s) {
        if (s == null) {
            return NutsOptional.of(Collections.emptyList());
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
        return NutsOptional.of(new ArrayList<>(allIds));
    }

    public static NutsOptional<List<NutsId>> parseIdList(String s) {
        List<NutsId> list = new ArrayList<>();
        NutsOptional<List<String>> o = parseStringIdList(s);
        if (o.isPresent()) {
            for (String x : o.get()) {
                NutsOptional<NutsId> y = NutsId.of(x).ifBlankNull();
                if (y.isError()) {
                    return NutsOptional.ofError(y.getMessage());
                }
                if (y.isPresent()) {
                    list.add(y.get());
                }
            }
            return NutsOptional.of(list);
        }
        if (o.isError()) {
            return NutsOptional.ofError(o.getMessage());
        }
        return NutsOptional.ofEmpty(o.getMessage());
    }

    /**
     * process throwable and return exit code
     *
     * @param ex  exception
     * @param out out stream
     * @return exit code
     */
    public static int processThrowable(Throwable ex, PrintStream out) {
        if (ex == null) {
            return 0;
        }

        NutsSession session = NutsSessionAwareExceptionBase.resolveSession(ex).orNull();
        NutsWorkspaceBootOptionsBuilder bo = null;
        if (session != null) {
            bo = session.boot().getBootOptions().builder();
            if (bo.getGui().orElse(false)) {
                if (!session.env().isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        } else {
            NutsWorkspaceBootOptionsBuilder options = new DefaultNutsWorkspaceBootOptionsBuilder();
            //load inherited
            String nutsArgs = NutsStringUtils.trim(
                    NutsStringUtils.trim(System.getProperty("nuts.boot.args"))
                            + " " + NutsStringUtils.trim(System.getProperty("nuts.args"))
            );
            try {
                options.setCommandLine(NutsCommandLine.parseDefault(nutsArgs).get().toStringArray(), null);
            } catch (Exception e) {
                //any, ignore...
            }
            bo = options;
            if (bo.getGui().orElse(false)) {
                if (!NutsApiUtils.isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        }

        boolean bot = bo.getBot().orElse(false);
        boolean gui = bo.getGui().orElse(false);
        boolean showTrace = bo.getDebug() != null;
        NutsLogConfig logConfig = bo.getLogConfig().orElseGet(NutsLogConfig::new);
        showTrace |= (logConfig != null
                && logConfig.getLogTermLevel() != null
                && logConfig.getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showTrace) {
            showTrace = getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showTrace = false;
            gui = false;
        }
        return processThrowable(ex, out, true, showTrace, gui);
    }

    public static int processThrowable(Throwable ex, PrintStream out, boolean showMessage, boolean showTrace, boolean showGui) {
        if (ex == null) {
            return 0;
        }
        NutsSession session = NutsSessionAwareExceptionBase.resolveSession(ex).orNull();
        NutsMessage fm = NutsSessionAwareExceptionBase.resolveSessionAwareExceptionBase(ex).map(NutsSessionAwareExceptionBase::getFormattedMessage)
                .orNull();
        int errorCode = NutsExceptionWithExitCodeBase.resolveExitCode(ex).orElse(204);
        if (errorCode == 0) {
            return 0;
        }
        String m = NutsReservedLangUtils.getErrorMessage(ex);
        NutsPrintStream fout = null;
        if (out == null) {
            if (session != null) {
                try {
                    fout = session.config().getSystemTerminal().getErr();
                    if (fm != null) {
                        fm = NutsMessage.ofNtf(NutsTexts.of(session).ofBuilder().append(fm, NutsTextStyle.error()).build());
                    } else {
                        fm = NutsMessage.ofStyled(m, NutsTextStyle.error());
                    }
                } catch (Exception ex2) {
                    NutsLoggerOp.of(NutsApplications.class, session).level(Level.FINE).error(ex2).log(
                            NutsMessage.ofPlain("unable to get system terminal")
                    );
                    //
                }
            } else {
                if (fm != null) {
                    // session is null but the exception is of NutsException type
                    // This is kind of odd, so will ignore message fm
                    fm = null;
                } else {
                    out = System.err;
                }
            }
        } else {
            if (session != null) {
                fout = NutsPrintStream.of(out, NutsTerminalMode.FORMATTED, null, session);
            } else {
                fout = null;
            }
        }
        if (showMessage) {

            if (fout != null) {
                if (session.getOutputFormat() == NutsContentType.PLAIN) {
                    if (fm != null) {
                        fout.println(fm);
                    } else {
                        fout.println(m);
                    }
                    if (showTrace) {
                        ex.printStackTrace(fout.asPrintStream());
                    }
                    fout.flush();
                } else {
                    if (fm != null) {
                        session.eout().add(NutsElements.of(session).ofObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", NutsTexts.of(session).ofText(fm).filteredText())
                                .build()
                        );
                        if (showTrace) {
                            session.eout().add(NutsElements.of(session).ofObject().set("errorTrace",
                                    NutsElements.of(session).ofArray().addAll(NutsReservedLangUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NutsArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            fout.printlnf(e.build());
                            e.clear();
                        }
                        fout.flush();
                    } else {
                        session.eout().add(NutsElements.of(session).ofObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", m)
                                .build());
                        if (showTrace) {
                            session.eout().add(NutsElements.of(session).ofObject().set("errorTrace",
                                    NutsElements.of(session).ofArray().addAll(NutsReservedLangUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NutsArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            fout.printlnf(e.build());
                            e.clear();
                        }
                        fout.flush();
                    }
                    fout.flush();
                }
            } else {
                if (out == null) {
                    out = System.err;
                }
                if (fm != null) {
                    out.println(fm);
                } else {
                    out.println(m);
                }
                if (showTrace) {
                    ex.printStackTrace(out);
                }
                out.flush();
            }
        }
        if (showGui) {
            StringBuilder sb = new StringBuilder();
            if (fm != null) {
                if (session != null) {
                    sb.append(NutsTexts.of(session).ofText(fm).filteredText());
                } else {
                    sb.append(fm);
                }
            } else {
                sb.append(m);
            }
            if (showTrace) {
                if (sb.length() > 0) {
                    sb.append("\n");
                    sb.append(NutsReservedLangUtils.stacktrace(ex));
                }
            }
            if (session != null) {
                //TODO show we delegate to the workspace implementation?
                NutsReservedGuiUtils.showMessage(NutsMessage.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            } else {
                NutsReservedGuiUtils.showMessage(NutsMessage.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            }
        }
        return (errorCode);
    }

    public static Map<String, String> toMap(NutsEnvCondition condition) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s;
        if (condition.getArch() != null) {
            s = condition.getArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.ARCH, s);
            }
        }
        if (condition.getOs() != null) {
            s = condition.getOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.OS, s);
            }
        }
        if (condition.getOsDist() != null) {
            s = condition.getOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.OS_DIST, s);
            }
        }
        if (condition.getPlatform() != null) {
            s = formatStringIdList(condition.getPlatform());
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.PLATFORM, s);
            }
        }
        if (condition.getDesktopEnvironment() != null) {
            s = condition.getDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.DESKTOP, s);
            }
        }
        if (condition.getProfile() != null) {
            s = condition.getProfile().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.PROFILE, s);
            }
        }
        if (condition.getProperties() != null) {
            Map<String, String> properties = condition.getProperties();
            if (!properties.isEmpty()) {
                m.put(NutsConstants.IdProperties.CONDITIONAL_PROPERTIES, NutsStringUtils.formatDefaultMap(properties));
            }
        }
        return m;
    }
}
