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
import net.thevpc.nuts.boot.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NApiUtils;
import net.thevpc.nuts.boot.NBootOptions;
import net.thevpc.nuts.boot.NBootOptionsBuilder;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Supplier;
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


    private static final String DELETE_FOLDERS_HEADER = "ATTENTION ! You are about to delete nuts workspace files.";

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

    public static String formatURL(URL url) {
        if (url == null) {
            return "<EMPTY>";
        }
        File f = NReservedIOUtils.toFile(url);
        if (f != null) {
            return f.getPath();
        }
        return url.toString();
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


    public static String getStoreLocationPath(NBootOptions bOptions, NStoreType value) {
        Map<NStoreType, String> storeLocations = bOptions.getStoreLocations().orNull();
        if (storeLocations != null) {
            return storeLocations.get(value);
        }
        return null;
    }

    /**
     * @param includeRoot true if include root
     * @param locations   of type NutsStoreLocation, Path of File
     * @param readline
     */
    public static long deleteStoreLocations(NBootOptions lastBootOptions, NBootOptions o, boolean includeRoot,
                                            NLog bLog, Object[] locations, Supplier<String> readline) {
        if (lastBootOptions == null) {
            return 0;
        }
        NConfirmationMode confirm = o.getConfirm().orElse(NConfirmationMode.ASK);
        if (confirm == NConfirmationMode.ASK
                && o.getOutputFormat().orElse(NContentType.PLAIN) != NContentType.PLAIN) {
            throw new NBootException(
                    NMsg.ofPlain("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), NExecutionException.ERROR_255);
        }
        bLog.with().level(Level.FINEST).verb(NLogVerb.WARNING).log(NMsg.ofC("delete workspace location(s) at : %s",
                lastBootOptions.getWorkspace().orNull()
        ));
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
                bLog.with().level(Level.WARNING).verb(NLogVerb.WARNING).log(NMsg.ofPlain("reset cancelled (applied '--no' argument)"));
                throw new NNoSessionCancelException(NMsg.ofPlain("cancel delete folder"));
            }
        }
        NConfigs conf = null;
        List<Path> folders = new ArrayList<>();
        if (includeRoot) {
            folders.add(Paths.get(lastBootOptions.getWorkspace().get()));
        }
        for (Object ovalue : locations) {
            if (ovalue != null) {
                if (ovalue instanceof NStoreType) {
                    NStoreType value = (NStoreType) ovalue;
                    String p = getStoreLocationPath(lastBootOptions, value);
                    if (p != null) {
                        folders.add(Paths.get(p));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue));
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue).toPath());
                } else {
                    throw new NBootException(NMsg.ofC("unsupported path type : %s", ovalue));
                }
            }
        }
        NBootOptionsBuilder optionsCopy = o.builder();
        if (optionsCopy.getBot().orElse(false) || !NReservedGuiUtils.isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return deleteAndConfirmAll(folders.toArray(new Path[0]), force, DELETE_FOLDERS_HEADER, null, bLog, optionsCopy, readline);
    }

    public static long deleteAndConfirmAll(Path[] folders, boolean force, String header, NSession session,
                                           NLog bLog, NBootOptions bOptions, Supplier<String> readline) {
        return deleteAndConfirmAll(folders, force, new NReservedDeleteFilesContextImpl(), header, session, bLog, bOptions, readline);
    }

    private static long deleteAndConfirmAll(Path[] folders, boolean force, NReservedDeleteFilesContext refForceAll,
                                            String header, NSession session, NLog bLog, NBootOptions bOptions, Supplier<String> readline) {
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
                                        bLog.with().level(Level.WARNING).verb(NLogVerb.WARNING).log(NMsg.ofC("%s", header));
                                    }
                                }
                            }
                        }
                    }
                    count += deleteAndConfirm(child, force, refForceAll, session, bLog, bOptions, readline);
                }
            }
        }
        return count;
    }

    private static long deleteAndConfirm(Path directory, boolean force, NReservedDeleteFilesContext refForceAll,
                                         NSession session, NLog bLog, NBootOptions bOptions, Supplier<String> readline) {
        if (Files.exists(directory)) {
            if (!force && !refForceAll.isForce() && refForceAll.accept(directory)) {
                String line = null;
                if (session != null) {
                    line = session.getTerminal().ask()
                            .resetLine()
                            .forString(
                                    NMsg.ofC(
                                            "do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory
                                    )).setSession(session).getValue();
                } else {
                    if (bOptions.getBot().orElse(false)) {
                        if (bOptions.getConfirm().orElse(NConfirmationMode.ASK) == NConfirmationMode.YES) {
                            line = "y";
                        } else {
                            throw new NBootException(NMsg.ofPlain("failed to delete files in --bot mode without auto confirmation"));
                        }
                    } else {
                        if (bOptions.getGui().orElse(false)) {
                            line = NReservedGuiUtils.inputString(
                                    NMsg.ofC("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).toString(),
                                    null, readline, bLog
                            );
                        } else {
                            NConfirmationMode cc = bOptions.getConfirm().orElse(NConfirmationMode.ASK);
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
                                    throw new NBootException(NMsg.ofPlain("error response"));
                                }
                                case ASK: {
                                    // Level.OFF is to force logging in all cases
                                    bLog.with().level(Level.OFF).verb(NLogVerb.WARNING).log(NMsg.ofC("do you confirm deleting %s [y/n/c/a] (default 'n') ? : ", directory));
                                    line = readline.get();
                                }
                            }
                        }
                    }
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll.setForce(true);
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NCancelException(session);
                } else if (!NLiteral.of(line).asBoolean().orElse(false)) {
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
                bLog.with().level(Level.FINEST).verb(NLogVerb.WARNING).log(NMsg.ofC("delete folder : %s (%s files/folders deleted)", directory, count[0]));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return count[0];
        }
        return 0;
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
        List<String> oss = NReservedCollectionUtils.uniqueNonBlankList(cond.getOs());
        List<String> archs = NReservedCollectionUtils.uniqueNonBlankList(cond.getArch());
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
        boolean bootOptionals = NReservedNUtilWorkspaceOptions.isBootOptional(bOptions);
        //by default ignore optionals
        String o = s.getOptional();
        if (NBlankable.isBlank(o) || Boolean.parseBoolean(o)) {
            if (!bootOptionals && !NReservedNUtilWorkspaceOptions.isBootOptional(s.getArtifactId(), bOptions)) {
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
        switch (key) {
            case NConstants.IdProperties.CLASSIFIER: {
                builder.setClassifier(value);
                break;
            }
            case NConstants.IdProperties.PROFILE: {
                sb.setProfile(NReservedStringUtils.splitDefault(value));
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

    /**
     * process throwable and return exit code
     *
     * @param ex  exception
     * @param out out stream
     * @return exit code
     */
    public static int processThrowable(Throwable ex, NLog out) {
        if (ex == null) {
            return 0;
        }

        NSession session = NSessionAwareExceptionBase.resolveSession(ex).orNull();
        NBootOptionsBuilder bo = null;
        if (session != null) {
            bo = NBootManager.of(session).getBootOptions().builder();
            if (bo.getGui().orElse(false)) {
                if (!NEnvs.of(session).isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        } else {
            NBootOptionsBuilder options = new DefaultNBootOptionsBuilder();
            //load inherited
            String nutsArgs = NStringUtils.trim(
                    NStringUtils.trim(System.getProperty("nuts.boot.args"))
                            + " " + NStringUtils.trim(System.getProperty("nuts.args"))
            );
            try {
                options.setCommandLine(NCmdLine.parseDefault(nutsArgs).get().toStringArray(), null);
            } catch (Exception e) {
                //any, ignore...
            }
            bo = options;
            if (bo.getGui().orElse(false)) {
                if (!NApiUtils.isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        }

        boolean bot = bo.getBot().orElse(false);
        boolean gui = bo.getGui().orElse(false);
        boolean showStackTrace = bo.getDebug() != null;
        NLogConfig logConfig = bo.getLogConfig().orElseGet(NLogConfig::new);
        showStackTrace |= (logConfig != null
                && logConfig.getLogTermLevel() != null
                && logConfig.getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showStackTrace) {
            showStackTrace = getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showStackTrace = false;
            gui = false;
        }
        return processThrowable(ex, out, true, showStackTrace, gui);
    }

    public static int processThrowable(Throwable ex, NLog out, boolean showMessage, boolean showStackTrace, boolean showGui) {
        if (ex == null) {
            return 0;
        }
        int errorCode = NExceptionWithExitCodeBase.resolveExitCode(ex).orElse(204);
        if (errorCode == 0) {
            return 0;
        }
        NSession session = NSessionAwareExceptionBase.resolveSession(ex).orNull();
        NMsg fm = NSessionAwareExceptionBase.resolveSessionAwareExceptionBase(ex).map(NSessionAwareExceptionBase::getFormattedMessage)
                .orNull();
        String m = NReservedLangUtils.getErrorMessage(ex);
        NPrintStream fout = null;
        if (out == null) {
            if (session != null) {
                try {
                    fout = NIO.of(session).getSystemTerminal().getErr();
                    if (fm != null) {
                        fm = NMsg.ofNtf(NTexts.of(session).ofBuilder().append(fm, NTextStyle.error()).build());
                    } else {
                        fm = NMsg.ofStyled(m, NTextStyle.error());
                    }
                } catch (Exception ex2) {
                    NLogOp.of(NApplications.class, session).level(Level.FINE).error(ex2).log(
                            NMsg.ofPlain("unable to get system terminal")
                    );
                    //
                }
            } else {
                if (fm != null) {
                    // session is null but the exception is of NutsException type
                    // This is kind of odd, so will ignore message fm
                    fm = null;
                } else {
                    out = new NReservedBootLog();
                }
            }
        } else {
            if (session != null) {
//                fout = NutsPrintStream.of(out, NutsTerminalMode.FORMATTED, null, session);
                fout = session.err();
            } else {
                fout = null;
            }
        }
        if (showMessage) {

            if (fout != null) {
                if (session.getOutputFormat() == NContentType.PLAIN) {
                    if (fm != null) {
                        fout.println(fm);
                    } else {
                        fout.println(m);
                    }
                    if (showStackTrace) {
                        ex.printStackTrace(fout.asPrintStream());
                    }
                    fout.flush();
                } else {
                    if (fm != null) {
                        session.eout().add(NElements.of(session).ofObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", NTexts.of(session).ofText(fm).filteredText())
                                .build()
                        );
                        if (showStackTrace) {
                            session.eout().add(NElements.of(session).ofObject().set("errorTrace",
                                    NElements.of(session).ofArray().addAll(NReservedLangUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            fout.println(e.build());
                            e.clear();
                        }
                        fout.flush();
                    } else {
                        session.eout().add(NElements.of(session).ofObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", m)
                                .build());
                        if (showStackTrace) {
                            session.eout().add(NElements.of(session).ofObject().set("errorTrace",
                                    NElements.of(session).ofArray().addAll(NReservedLangUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            fout.println(e.build());
                            e.clear();
                        }
                        fout.flush();
                    }
                    fout.flush();
                }
            } else {
                if (out == null) {
                    out = new NReservedBootLog();
                }
                if (fm != null) {
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(fm);
                } else {
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain(m));
                }
                if (showStackTrace) {
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain("---------------"));
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain(">  STACKTRACE :"));
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain("---------------"));
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain(
                            NReservedLangUtils.stacktrace(ex)
                    ));
                }
            }
        }
        if (showGui) {
            StringBuilder sb = new StringBuilder();
            if (fm != null) {
                if (session != null) {
                    sb.append(NTexts.of(session).ofText(fm).filteredText());
                } else {
                    sb.append(fm);
                }
            } else {
                sb.append(m);
            }
            if (showStackTrace) {
                if (sb.length() > 0) {
                    sb.append("\n");
                    sb.append(NReservedLangUtils.stacktrace(ex));
                }
            }
            if (session != null) {
                //TODO show we delegate to the workspace implementation?
                NReservedGuiUtils.showMessage(NMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            } else {
                NReservedGuiUtils.showMessage(NMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            }
        }
        return (errorCode);
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
}
