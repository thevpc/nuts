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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.cmdline.NBootCmdLine;
import net.thevpc.nuts.boot.reserved.cmdline.NBootWorkspaceCmdLineFormatter;
import net.thevpc.nuts.boot.reserved.cmdline.NBootWorkspaceCmdLineParser;
import net.thevpc.nuts.boot.reserved.cmdline.NBootWorkspaceOptionsConfig;
import net.thevpc.nuts.boot.reserved.util.*;
import net.thevpc.nuts.boot.reserved.util.NReservedErrorInfo;
import net.thevpc.nuts.boot.reserved.util.NBootErrorInfoList;
import net.thevpc.nuts.boot.reserved.util.NBootIOUtilsBoot;
import net.thevpc.nuts.boot.reserved.maven.NReservedMavenUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NBootJsonParser;
import net.thevpc.nuts.boot.reserved.util.NBootPath;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * NutsBootWorkspace is responsible of loading initial nuts-runtime.jar and its
 * dependencies and for creating workspaces using the method
 * {@link #openWorkspace()} . NutsBootWorkspace is also responsible of managing
 * local jar cache folder located at ~/.cache/nuts/default-workspace/boot
 * <br>
 * Default Bootstrap implementation. This class is responsible of loading
 * initial nuts-runtime.jar and its dependencies and for creating workspaces
 * using the method {@link #openWorkspace()}.
 * <br>
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public final class NBootWorkspace {
    private static final String version = "0.8.5";

    public static String getVersion() {
        return version;
    }

    private final Instant creationTime = Instant.now();
    private final NBootOptionsBoot userOptions;
    private final NBootLog bLog;
    private final NBootOptionsBoot computedOptions = new NBootOptionsBoot();
    private final NBootRepositoryDB repositoryDB = new NBootRepositoryDB();
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "workspace":
                    return computedOptions.getWorkspace();
                case "user.home":
                    return System.getProperty("user.home");
                case "home.apps":
                case "home.config":
                case "home.lib":
                case "home.temp":
                case "home.var":
                case "home.cache":
                case "home.run":
                case "home.log":
                    return NBootUtils.getHome(from.substring("home.".length()).toUpperCase(), computedOptions);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<String, String> s = NBootUtils.firstNonNull(computedOptions.getStoreLocations(), Collections.emptyMap());
                    String v = s.get(from);
                    if (v == null) {
                        return "${" + from + "}";
                    }
                    return v;
                }
            }
            return "${" + from + "}";
        }
    };
    private int newInstanceRequirements = 0;
    private NBootOptionsBoot lastWorkspaceOptions;
    //private Set<NRepositoryLocationBoot> parsedBootRuntimeDependenciesRepositories;
    private Set<NBootRepositoryLocation> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private Scanner scanner;
    private NBootCache cache = new NBootCache();
    Boolean runtimeLoaded;
    NBootId runtimeLoadedId;

    public NBootWorkspace(NBootArguments userOptionsUnparsed) {
        if (userOptionsUnparsed == null) {
            userOptionsUnparsed = new NBootArguments();
        }
        NBootOptionsBoot userOptions = new NBootOptionsBoot();
        userOptions.setStdin(userOptionsUnparsed.getIn());
        userOptions.setStdout(userOptionsUnparsed.getOut());
        userOptions.setStderr(userOptionsUnparsed.getErr());
        userOptions.setCreationTime(userOptionsUnparsed.getStartTime());
        InputStream in = userOptions.getStdin();
        scanner = new Scanner(in == null ? System.in : in);
        this.bLog = new NBootLog(userOptions);
        String[] args = userOptionsUnparsed.getArgs();
        NBootWorkspaceCmdLineParser.parseNutsArguments(args == null ? new String[0] : args, userOptions);
        if (NBootUtils.firstNonNull(userOptions.getSkipErrors(), false)) {
            StringBuilder errorMessage = new StringBuilder();
            if (userOptions.getErrors() != null) {
                for (String s : userOptions.getErrors()) {
                    errorMessage.append(s).append("\n");
                }
            }
            errorMessage.append("Try 'nuts --help' for more information.");
            bLog.log(Level.WARNING, "WARNING", NBootMsg.ofC("Error : %s", errorMessage));
        }
        this.userOptions = userOptions.copy();
        this.postInit();
    }

    public NBootWorkspace(NBootOptionsBoot userOptions) {
        if (userOptions == null) {
            userOptions = new NBootOptionsBoot();
        }
        this.bLog = new NBootLog(userOptions);
        this.userOptions = userOptions;
        this.postInit();
    }

    private void postInit() {
        this.computedOptions.setAll(userOptions);
        this.computedOptions.setUserOptions(this.userOptions);
//        this.computedOptions.setIsolationLevel(this.computedOptions.getIsolationLevel().orElse(NIsolationLevel.SYSTEM));
//        this.computedOptions.setExecutionType(this.computedOptions.getExecutionType().orElse(NExecutionType.SPAWN));
//        this.computedOptions.setConfirm(this.computedOptions.getConfirm().orElse(NConfirmationMode.ASK));
//        this.computedOptions.setFetchStrategy(this.computedOptions.getFetchStrategy().orElse(NFetchStrategy.ONLINE));
//        this.computedOptions.setOpenMode(this.computedOptions.getOpenMode().orElse(NOpenMode.OPEN_OR_CREATE));
//        this.computedOptions.setRunAs(this.computedOptions.getRunAs().orElse(NRunAs.CURRENT_USER));
//        this.computedOptions.setLogConfig(this.computedOptions.getLogConfig().orElseGet(NLogConfig::new));
//        this.computedOptions.setStdin(this.computedOptions.getStdin().orElse(System.in));
//        this.computedOptions.setStdout(this.computedOptions.getStdout().orElse(System.out));
//        this.computedOptions.setStderr(this.computedOptions.getStderr().orElse(System.err));
//        this.computedOptions.setLocale(this.computedOptions.getLocale().orElse(Locale.getDefault().toString()));
//        this.computedOptions.setOutputFormat(this.computedOptions.getOutputFormat().orElse(NContentType.PLAIN));
//        this.computedOptions.setCreationTime(this.computedOptions.getCreationTime().orElse(creationTime));
        if (this.computedOptions.getApplicationArguments().isEmpty()) {
            this.computedOptions.setApplicationArguments(new ArrayList<>());
        }
        this.bLog.setOptions(this.computedOptions);
    }

    private static void revalidateLocations(NBootOptionsBoot bootOptions, String workspaceName, boolean immediateLocation, String sandboxMode) {
        if (NBootStringUtils.isBlank(bootOptions.getName())) {
            bootOptions.setName(workspaceName);
        }
        boolean system = NBootUtils.firstNonNull(bootOptions.getSystem(), false);
        if (NBootUtils.sameEnum(sandboxMode, "SANDBOX")) {
            bootOptions.setStoreStrategy("STANDALONE");
            bootOptions.setRepositoryStoreStrategy("EXPLODED");
            system = false;
        } else {
            if (bootOptions.getStoreStrategy() == null) {
                bootOptions.setStoreStrategy(immediateLocation ? "EXPLODED" : "STANDALONE");
            }
            if (bootOptions.getRepositoryStoreStrategy() == null) {
                bootOptions.setRepositoryStoreStrategy("EXPLODED");
            }
        }
        Map<String, String> storeLocations =
                NBootPlatformHome.of(bootOptions.getStoreLayout(), system)
                        .buildLocations(bootOptions.getStoreStrategy(), bootOptions.getStoreLocations(), bootOptions.getHomeLocations(), bootOptions.getWorkspace() //no session!
                        );
        if (new HashSet<>(storeLocations.values()).size() != storeLocations.size()) {
            Map<String, List<String>> conflicts = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : storeLocations.entrySet()) {
                conflicts.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
            }
            StringBuilder error = new StringBuilder();
            error.append("invalid store locations. Two or more stores point to the same location:");
            List<Object> errorParams = new ArrayList<>();
            for (Map.Entry<String, List<String>> e : conflicts.entrySet()) {
                List<String> ev = e.getValue();
                if (ev.size() > 1) {
                    String ek = e.getKey();
                    error.append("\n");
                    error.append("all of (").append(ev.stream().map(x -> "%s").collect(Collectors.joining(","))).append(") point to %s");
                    errorParams.addAll(ev);
                    errorParams.add(ek);
                }
            }
            throw new NBootException(NBootMsg.ofC(error.toString(), errorParams.toArray()));
        }
        bootOptions.setStoreLocations(storeLocations);
    }

    private static final class ApiDigestHolder {
        static final String apiDigest = NBootUtils.resolveNutsIdDigest();
    }

    /**
     * current nuts version, loaded from pom file
     *
     * @return current nuts version
     */
    private static String getApiDigestOrInternal() {
        return ApiDigestHolder.apiDigest == null ? "<internal>" : ApiDigestHolder.apiDigest;
    }

    public boolean hasUnsatisfiedRequirements() {
        prepareWorkspace();
        return newInstanceRequirements != 0;
    }

    public void runNewProcess() {
        String[] processCmdLine = createProcessCmdLine();
        int result;
        try {
//            if (nLog != null) {
//                nLog.with().level(Level.FINE).verbStart().log(NMsgBoot.ofC("start new process : %s", NCmdLine.of(processCmdLine)));
//            } else {
            bLog.log(Level.FINE, "START", NBootMsg.ofC("start new process : %s", new NBootCmdLine(processCmdLine)));
//            }
            result = new ProcessBuilder(processCmdLine).inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ex) {
            throw new NBootException(NBootMsg.ofPlain("failed to run new nuts process"), ex);
        }
        if (result != 0) {
            throw new NBootException(NBootMsg.ofC("failed to exec new process. returned %s", result));
        }
    }

    /**
     * repositories used to locale nuts-runtime artifact or its dependencies
     *
     * @param dependencies when true search for runtime dependencies, when
     *                     false, search for runtime
     * @return repositories
     */
    public Set<NBootRepositoryLocation> resolveBootRuntimeRepositories(boolean dependencies) {
//        if (dependencies) {
//            if (parsedBootRuntimeDependenciesRepositories != null) {
//                return parsedBootRuntimeDependenciesRepositories;
//            }
//            bLog.log(Level.FINE, "START", NMsgBoot.ofC("resolve boot repositories to load nuts-runtime dependencies from options : %s and config: %s", computedOptions.getRepositories().orElseGet(Collections::emptyList).toString(), computedOptions.getBootRepositories().ifBlankEmpty().orElse("[]")));
//        } else {
        if (parsedBootRuntimeRepositories != null) {
            return parsedBootRuntimeRepositories;
        }
        bLog.log(Level.FINE, "START", NBootMsg.ofC("resolve boot repositories to load nuts-runtime from options : %s and config: %s",
                computedOptions.getRepositories() == null ? "[]" : computedOptions.getRepositories().toString(),
                computedOptions.getBootRepositories() == null ? "[]" : computedOptions.getBootRepositories().toString()));
//        }
        NBootRepositorySelectorList bootRepositoriesSelector = NBootRepositorySelectorList.of(computedOptions.getRepositories(), repositoryDB);
        NBootRepositorySelector[] old = NBootRepositorySelectorList.of(Arrays.asList(computedOptions.getBootRepositories()), repositoryDB).toArray();
        NBootRepositoryLocation[] result;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositoriesSelector.resolve(new NBootRepositoryLocation[]{
                    Boolean.getBoolean("nomaven") ? null : new NBootRepositoryLocation("maven", "maven", "maven")
            }, repositoryDB);
        } else {
            result = bootRepositoriesSelector.resolve(Arrays.stream(old).map(x -> NBootRepositoryLocation.of(x.getName(), x.getUrl())).toArray(NBootRepositoryLocation[]::new), repositoryDB);
        }
        result = Arrays.stream(result).map(
                r -> {
                    if (NBootStringUtils.isBlank(r.getLocationType()) || NBootStringUtils.isBlank(r.getName())) {
                        boolean fileExists = false;
                        if (r.getPath() != null) {
                            NBootPath r1 = new NBootPath(r.getPath()).toAbsolute();
                            if (!r.getPath().equals(r1.getPath())) {
                                r = r.setPath(r1.getPath());
                            }
                            NBootPath r2 = r1.resolve(".nuts-repository");
                            NBootJsonParser parser = null;
                            try {
                                byte[] bytes = r2.readAllBytes(bLog);
                                if (bytes != null) {
                                    fileExists = true;
                                    parser = new NBootJsonParser(new InputStreamReader(new ByteArrayInputStream(bytes)));
                                    Map<String, Object> jsonObject = parser.parseObject();
                                    if (NBootStringUtils.isBlank(r.getLocationType())) {
                                        Object o = jsonObject.get("repositoryType");
                                        if (o instanceof String && !NBootStringUtils.isBlank((String) o)) {
                                            r = r.setLocationType(String.valueOf(o));
                                        }
                                    }
                                    if (NBootStringUtils.isBlank(r.getName())) {
                                        Object o = jsonObject.get("repositoryName");
                                        if (o instanceof String && !NBootStringUtils.isBlank((String) o)) {
                                            r = r.setName(String.valueOf(o));
                                        }
                                    }
                                    if (NBootStringUtils.isBlank(r.getName())) {
                                        r = r.setName(r.getName());
                                    }
                                }
                            } catch (Exception e) {
                                bLog.log(Level.CONFIG, "WARNING", NBootMsg.ofC("unable to load %s", r2));
                            }
                        }
                        if (fileExists) {
                            if (NBootStringUtils.isBlank(r.getLocationType())) {
                                r = r.setLocationType(NBootConstants.RepoTypes.NUTS);
                            }
                        }
                    }
                    return r;
                }
        ).toArray(NBootRepositoryLocation[]::new);
        Set<NBootRepositoryLocation> rr = Arrays.stream(result).collect(Collectors.toCollection(LinkedHashSet::new));
//        if (dependencies) {
//            parsedBootRuntimeDependenciesRepositories = rr;
//        } else {
        parsedBootRuntimeRepositories = rr;
//        }
        return rr;
    }

    public String[] createProcessCmdLine() {
        prepareWorkspace();
        bLog.log(Level.FINE, "START", NBootMsg.ofC("running version %s.  %s", computedOptions.getApiVersion(), getRequirementsHelpString(true)));
        String defaultWorkspaceLibFolder = computedOptions.getStoreType("LIB") + "/" + NBootConstants.Folders.ID;
        List<NBootRepositoryLocation> repos = new ArrayList<>();
        repos.add(NBootRepositoryLocation.of("nuts@" + defaultWorkspaceLibFolder));
        Collection<NBootRepositoryLocation> bootRepositories = resolveBootRuntimeRepositories(true);
        repos.addAll(bootRepositories);
        NBootErrorInfoList errorList = new NBootErrorInfoList();
        File file = NReservedMavenUtilsBoot.resolveOrDownloadJar(NBootId.ofApi(computedOptions.getApiVersion()), repos.toArray(new NBootRepositoryLocation[0]),
                NBootRepositoryLocation.of("nuts@" + computedOptions.getStoreType("LIB") + File.separator + NBootConstants.Folders.ID), bLog, false, computedOptions.getExpireTime(), errorList);
        if (file == null) {
            errorList.insert(0, new NReservedErrorInfo(null, null, null, "unable to load nuts " + computedOptions.getApiVersion(), null));
            logError(null, errorList);
            throw new NBootException(NBootMsg.ofC("unable to load %s#%s", NBootConstants.Ids.NUTS_API, computedOptions.getApiVersion()));
        }

        List<String> cmd = new ArrayList<>();
        String jc = computedOptions.getJavaCommand();
        if (jc == null || jc.trim().isEmpty()) {
            jc = NBootUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : NBootCmdLine.parseDefaultList(computedOptions.getJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        if (computedOptions.getJavaOptions() == null) {
            Collections.addAll(cmd, NBootCmdLine.parseDefaultList(computedOptions.getJavaOptions()));
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(asCmdLine(computedOptions, new NBootWorkspaceOptionsConfig().setCompact(true).setApiVersion(computedOptions.getApiVersion())).toStringList());
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            bLog.log(Level.FINE, "START", NBootMsg.ofC("[exec] %s", sb));
        }
        return cmd.toArray(new String[0]);
    }

    private NBootCmdLine asCmdLine(NBootOptionsBoot cc, NBootWorkspaceOptionsConfig conf) {
        if (conf == null) {
            conf = new NBootWorkspaceOptionsConfig();
        }
        NBootWorkspaceCmdLineFormatter a = new NBootWorkspaceCmdLineFormatter(
                conf
                ,
                cc
        );
        return a.toCmdLine();
    }

    public NBootOptionsBoot getOptions() {
        return computedOptions;
    }


    private NBootIdCache getFallbackCache(NBootId baseId, boolean lastWorkspace, boolean copyTemp) {
        NBootIdCache old = cache.fallbackIdMap.get(baseId);
        if (old != null) {
            return old;
        }

        NBootIdCache fid = new NBootIdCache();
        fid.baseId = baseId;
        cache.fallbackIdMap.put(fid.baseId, fid);
        String s = (lastWorkspace ? lastWorkspaceOptions : computedOptions).getStoreLocations().get("LIB") + "/id/"
                + NBootUtils.resolveIdPath(baseId.getShortId());
        //
        Path ss = Paths.get(s);
        NBootId bestId = null;
        NBootVersion bestVersion = null;
        Path bestPath = null;

        if (Files.isDirectory(ss)) {
            try (Stream<Path> stream = Files.list(ss)) {
                for (Path path : stream.collect(Collectors.toList())) {
                    NBootVersion version = NBootVersion.of(path.getFileName().toString());
                    if (version != null) {
                        if (Files.isDirectory(path)) {
                            NBootId rId = baseId.copy().setVersion(version.getValue());
                            Path jar = ss.resolve(version.toString()).resolve(NBootUtils.resolveFileName(
                                    rId,
                                    "jar"
                            ));
                            if (Files.isRegularFile(jar)) {
                                if (bestVersion == null || bestVersion.compareTo(version) < 0) {
                                    bestVersion = version;
                                    bestPath = jar;
                                    bestId = rId;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore any error
            }
        }
        if (bestVersion != null) {
            Path descNutsPath = bestPath.resolveSibling(NBootUtils.resolveFileName(bestId, "nuts"));
            Set<NBootId> dependencies = NReservedMavenUtilsBoot.loadDependenciesFromNutsUrl(descNutsPath.toString(), bLog);
            if (dependencies != null) {
                fid.deps = dependencies.stream()
                        .filter(x -> NBootUtils.isAcceptDependency(x.toDependency(), computedOptions))
                        .collect(Collectors.toSet());
                fid.depsData = fid.deps.stream()
                        .map(x -> {
                            try {
                                return getFallbackCache(x, lastWorkspace, copyTemp);
                            } catch (RuntimeException e) {
                                if (!x.toDependency().isOptional()) {
                                    throw e;
                                }
                                //
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            fid.id = bestId;
            if (copyTemp) {
                Path temp = null;
                try {
                    temp = Files.createTempFile("old-", bestPath.getFileName().toString());
                    Files.copy(bestPath, temp, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new NBootException(NBootMsg.ofPlain("error storing nuts-runtime.jar"), e);
                }
                fid.jar = temp.toString();
                fid.expected = bestPath.toString();
                fid.temp = true;

            } else {
                fid.jar = bestPath.toString();
            }
        }

        return fid;
    }

    private boolean isRuntimeLoaded() {
        if (runtimeLoaded == null) {
            runtimeLoaded = false;
            try {
                Class<?> c = Class.forName("net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace");
                runtimeLoadedId = (NBootId) c.getField("RUNTIME_ID").get(null);
                runtimeLoaded = true;
            } catch (Exception ex) {
                //
            }
        }
        return runtimeLoaded;
    }

    @SuppressWarnings("unchecked")
    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {

            preparedWorkspace = true;
            String isolationLevel = NBootStringUtils.firstNonBlank(computedOptions.getIsolationLevel(), "SYSTEM");
            if (bLog.isLoggable(Level.CONFIG)) {
                bLog.log(Level.CONFIG, "START", NBootMsg.ofC("bootstrap Nuts version %s %s digest %s...", getVersion(),
                        NBootUtils.sameEnum(isolationLevel, "SYSTEM") ? "" :
                                NBootUtils.sameEnum(isolationLevel, "USER") ? " (user mode)" :
                                        NBootUtils.sameEnum(isolationLevel, "CONFINED") ? " (confined mode)" :
                                                NBootUtils.sameEnum(isolationLevel, "SANDBOX") ? " (sandbox mode)" : " (unsupported mode " + isolationLevel + ")",
                        getApiDigestOrInternal()));
                bLog.log(Level.CONFIG, "START", NBootMsg.ofPlain("boot-class-path:"));
                for (String s : NBootStringUtils.split(System.getProperty("java.class.path"), File.pathSeparator, true, true)) {
                    bLog.log(Level.CONFIG, "START", NBootMsg.ofC("                  %s", s));
                }
                ClassLoader thisClassClassLoader = getClass().getClassLoader();
                bLog.log(Level.CONFIG, "START", NBootMsg.ofC("class-loader: %s", thisClassClassLoader));
                for (URL url : NBootUtils.resolveClasspathURLs(thisClassClassLoader, false)) {
                    bLog.log(Level.CONFIG, "START", NBootMsg.ofC("                 %s", url));
                }
                ClassLoader tctxloader = Thread.currentThread().getContextClassLoader();
                if (tctxloader != null && tctxloader != thisClassClassLoader) {
                    bLog.log(Level.CONFIG, "START", NBootMsg.ofC("thread-class-loader: %s", tctxloader));
                    for (URL url : NBootUtils.resolveClasspathURLs(tctxloader, false)) {
                        bLog.log(Level.CONFIG, "START", NBootMsg.ofC("                 %s", url));
                    }
                }
                ClassLoader contextClassLoader = getContextClassLoader();
                bLog.log(Level.CONFIG, "START", NBootMsg.ofC("ctx-class-loader: %s", contextClassLoader));
                if (contextClassLoader != null && contextClassLoader != thisClassClassLoader) {
                    for (URL url : NBootUtils.resolveClasspathURLs(contextClassLoader, false)) {
                        bLog.log(Level.CONFIG, "START", NBootMsg.ofC("                 %s", url));
                    }
                }
                bLog.log(Level.CONFIG, "START", NBootMsg.ofPlain("system-properties:"));
                Map<String, String> m = (Map) System.getProperties();
                int max = m.keySet().stream().mapToInt(String::length).max().getAsInt();
                for (String k : new TreeSet<String>(m.keySet())) {
                    bLog.log(Level.CONFIG, "START", NBootMsg.ofC("    %s = %s", NBootStringUtils.formatAlign(k, max, NBootPositionTypeBoot.FIRST), NBootStringUtils.formatStringLiteral(m.get(k))));
                }
            }
            String workspaceName = null;
            NBootOptionsBoot lastConfigLoaded = null;
            String lastNutsWorkspaceJsonConfigPath = null;
            boolean immediateLocation = false;
            boolean resetFlag = NBootUtils.firstNonNull(computedOptions.getReset(), false);
            boolean dryFlag = NBootUtils.firstNonNull(computedOptions.getDry(), false);
            String _ws = computedOptions.getWorkspace();
            if (NBootUtils.sameEnum(isolationLevel, "SANDBOX")) {
                Path t = null;
                try {
                    t = Files.createTempDirectory("nuts-sandbox-" + Instant.now().toString().replace(':', '-'));
                } catch (IOException e) {
                    throw new NBootException(NBootMsg.ofPlain("unable to create temporary/sandbox folder"), e);
                }
                lastNutsWorkspaceJsonConfigPath = t.toString();
                immediateLocation = true;
                workspaceName = t.getFileName().toString();
                resetFlag = false; //no need for reset
                if (NBootUtils.firstNonNull(computedOptions.getSystem(), false)) {
                    throw new NBootException(NBootMsg.ofPlain("you cannot specify option '--global' in sandbox mode"));
                }
                if (!NBootStringUtils.isBlank(computedOptions.getWorkspace())) {
                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--workspace' in sandbox mode"));
                }
                if (!NBootUtils.sameEnum(computedOptions.getStoreStrategy(), "STANDALONE")) {
                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--exploded' in sandbox mode"));
                }
                if (NBootUtils.firstNonNull(computedOptions.getSystem(), false)) {
                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--global' in sandbox mode"));
                }
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            } else {
                if (!NBootUtils.sameEnum(isolationLevel, "SYSTEM") && NBootUtils.firstNonNull(userOptions.getSystem(), false)) {
                    if (NBootUtils.firstNonNull(userOptions.getReset(), false)) {
                        throw new NBootException(NBootMsg.ofC("invalid option 'global' in %s mode", isolationLevel));
                    }
                }
                if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                    //this is a protocol based workspace
                    //String protocol=ws.substring(0,ws.indexOf("://"));
                    workspaceName = "remote-bootstrap";
                    lastNutsWorkspaceJsonConfigPath = NBootPlatformHome.of(null, NBootUtils.firstNonNull(computedOptions.getSystem(), false)).getWorkspaceLocation(NBootUtils.resolveValidWorkspaceName(workspaceName));
                    lastConfigLoaded = NBootBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                    immediateLocation = true;

                } else {
                    immediateLocation = NBootUtils.isValidWorkspaceName(_ws);
                    int maxDepth = 36;
                    for (int i = 0; i < maxDepth; i++) {
                        lastNutsWorkspaceJsonConfigPath = NBootUtils.isValidWorkspaceName(_ws) ? NBootPlatformHome.of(null, NBootUtils.firstNonNull(computedOptions.getSystem(), false)).getWorkspaceLocation(NBootUtils.resolveValidWorkspaceName(_ws)) : NBootIOUtilsBoot.getAbsolutePath(_ws);

                        NBootOptionsBoot configLoaded = NBootBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                        if (configLoaded == null) {
                            //not loaded
                            break;
                        }
                        if (NBootStringUtils.isBlank(configLoaded.getWorkspace())) {
                            lastConfigLoaded = configLoaded;
                            break;
                        }
                        _ws = configLoaded.getWorkspace();
                        if (i >= maxDepth - 1) {
                            throw new NBootException(NBootMsg.ofPlain("cyclic workspace resolution"));
                        }
                    }
                    workspaceName = NBootUtils.resolveValidWorkspaceName(computedOptions.getWorkspace());
                }
            }
            computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            if (lastConfigLoaded != null) {
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                computedOptions.setName(lastConfigLoaded.getName());
                computedOptions.setUuid(lastConfigLoaded.getUuid());
                NBootOptionsBoot curr;
                if (!resetFlag) {
                    curr = computedOptions;
                } else {
                    lastWorkspaceOptions = new NBootOptionsBoot();
                    curr = lastWorkspaceOptions;
                    curr.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                    curr.setName(lastConfigLoaded.getName());
                    curr.setUuid(lastConfigLoaded.getUuid());
                }
                curr.setBootRepositories(lastConfigLoaded.getBootRepositories());
                curr.setJavaCommand(lastConfigLoaded.getJavaCommand());
                curr.setJavaOptions(lastConfigLoaded.getJavaOptions());
                curr.setExtensionsSet(NBootUtils.nonNullSet(lastConfigLoaded.getExtensionsSet()));
                curr.setStoreStrategy(lastConfigLoaded.getStoreStrategy());
                curr.setRepositoryStoreStrategy(lastConfigLoaded.getRepositoryStoreStrategy());
                curr.setStoreLayout(lastConfigLoaded.getStoreLayout());
                curr.setStoreLocations(NBootUtils.nonNullMap(lastConfigLoaded.getStoreLocations()));
                curr.setHomeLocations(NBootUtils.nonNullMap(lastConfigLoaded.getHomeLocations()));
            }
            revalidateLocations(computedOptions, workspaceName, immediateLocation, isolationLevel);
            long countDeleted = 0;
            //now that config is prepared proceed to any cleanup
            if (resetFlag) {
                //force loading version early, it will be used later-on
                if (lastWorkspaceOptions != null) {
                    revalidateLocations(lastWorkspaceOptions, workspaceName, immediateLocation, isolationLevel);
                    if (dryFlag) {
                        bLog.log(Level.INFO, "DEBUG", NBootMsg.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, "WARNING", NBootMsg.ofPlain("reset workspace"));
                        getFallbackCache(NBootId.RUNTIME_ID, true, true);
                        countDeleted = NBootIOUtilsBoot.deleteStoreLocations(lastWorkspaceOptions, getOptions(), true, bLog, NBootPlatformHome.storeTypes(), () -> scanner.nextLine());
                        NBootUtils.ndiUndo(bLog);
                    }
                } else {
                    if (dryFlag) {
                        bLog.log(Level.INFO, "DEBUG", NBootMsg.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, "WARNING", NBootMsg.ofPlain("reset workspace"));
                        getFallbackCache(NBootId.RUNTIME_ID, false, true);
                        countDeleted = NBootIOUtilsBoot.deleteStoreLocations(computedOptions, getOptions(), true, bLog, NBootPlatformHome.storeTypes(), () -> scanner.nextLine());
                        NBootUtils.ndiUndo(bLog);
                    }
                }
            } else if (NBootUtils.firstNonNull(computedOptions.getRecover(), false)) {
                if (dryFlag) {
                    bLog.log(Level.INFO, "DEBUG", NBootMsg.ofPlain("[dry] [recover] delete CACHE/TEMP workspace folders"));
                } else {
                    bLog.log(Level.CONFIG, "WARNING", NBootMsg.ofPlain("recover workspace."));
                    List<Object> folders = new ArrayList<>();
                    folders.add("CACHE");
                    folders.add("TEMP");
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = NBootIOUtilsBoot.getStoreLocationPath(computedOptions, "LIB");
                    if (p != null) {
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts"));
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = NBootIOUtilsBoot.deleteStoreLocations(computedOptions, getOptions(), false, bLog, folders.toArray(), () -> scanner.nextLine());
                }
            }
            if (computedOptions.getExtensionsSet() == null) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setExtensionsSet(NBootUtils.firstNonNull(lastWorkspaceOptions.getExtensionsSet(), Collections.emptySet()));
                } else {
                    computedOptions.setExtensionsSet(Collections.emptySet());
                }
            }
            if (computedOptions.getHomeLocations() == null) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setHomeLocations(NBootUtils.firstNonNull(lastWorkspaceOptions.getHomeLocations(), Collections.emptyMap()));
                } else {
                    computedOptions.setHomeLocations(Collections.emptyMap());
                }
            }
            if (computedOptions.getStoreLayout() == null) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setStoreLayout(NBootUtils.firstNonNull(lastWorkspaceOptions.getStoreLayout(), NBootPlatformHome.currentOsFamily()));
                } else {
                    computedOptions.setHomeLocations(Collections.emptyMap());
                }
            }

            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (computedOptions.getApplicationArguments().size() == 0 && NBootUtils.firstNonNull(computedOptions.getSkipBoot(), false) && (NBootUtils.firstNonNull(computedOptions.getRecover(), false) || resetFlag)) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        bLog.log(Level.WARNING, "WARNING", NBootMsg.ofC("workspace erased : %s", computedOptions.getWorkspace()));
                    } else {
                        bLog.log(Level.WARNING, "WARNING", NBootMsg.ofC("workspace is not erased because it does not exist : %s", computedOptions.getWorkspace()));
                    }
                }
                throw new NBootException(NBootMsg.ofPlain(""), 0);
            }
            //after eventual clean up
            if (NBootUtils.firstNonNull(computedOptions.getInherited(), false)) {
                //when Inherited, always use the current Api version!
                computedOptions.setApiVersion(getVersion().toString());
            } else {
                NBootVersion nutsVersion = NBootVersion.of(computedOptions.getApiVersion());
                if (nutsVersion.isLatestVersion() || nutsVersion.isReleaseVersion()) {
                    NBootId s = NReservedMavenUtilsBoot.resolveLatestMavenId(NBootId.ofApi(""), null, bLog, resolveBootRuntimeRepositories(true), computedOptions);
                    if (s == null) {
                        throw new NBootException(NBootMsg.ofPlain("unable to load latest nuts version"));
                    }
                    computedOptions.setApiVersion(s.getVersion());
                }
                if (nutsVersion.isBlank()) {
                    computedOptions.setApiVersion(getVersion().toString());
                }
            }

            NBootId bootApiId = NBootId.ofApi(computedOptions.getApiVersion());
            Path nutsApiConfigBootPath = Paths.get(computedOptions.getStoreType("CONF") + File.separator + NBootConstants.Folders.ID).resolve(NBootUtils.resolveIdPath(bootApiId)).resolve(NBootConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && NBootIOUtilsBoot.isFileAccessible(nutsApiConfigBootPath, computedOptions.getExpireTime(), bLog)) {
                try {
                    Map<String, Object> obj = NBootJsonParser.parse(nutsApiConfigBootPath);
                    if (!obj.isEmpty()) {
                        bLog.log(Level.CONFIG, "READ", NBootMsg.ofC("loaded %s file : %s", nutsApiConfigBootPath.getFileName(), nutsApiConfigBootPath.toString()));
                        loadedApiConfig = true;
                        if (computedOptions.getRuntimeId() == null) {
                            String runtimeId = (String) obj.get("runtimeId");
                            if (NBootStringUtils.isBlank(runtimeId)) {
                                bLog.log(Level.CONFIG, "FAIL", NBootMsg.ofC("%s does not contain runtime-id", nutsApiConfigBootPath));
                            }
                            computedOptions.setRuntimeId(runtimeId);
                        }
                        if (computedOptions.getJavaCommand() == null) {
                            computedOptions.setJavaCommand((String) obj.get("javaCommand"));
                        }
                        if (computedOptions.getJavaOptions() == null) {
                            computedOptions.setJavaOptions((String) obj.get("javaOptions"));
                        }
                    }
                } catch (UncheckedIOException e) {
                    bLog.log(Level.CONFIG, "READ", NBootMsg.ofC("unable to read %s : %s", nutsApiConfigBootPath, e));
                }
            }
            if (!loadedApiConfig || computedOptions.getRuntimeId() == null || computedOptions.getRuntimeBootDescriptor() == null || computedOptions.getExtensionBootDescriptors() == null || computedOptions.getBootRepositories() == null) {

                NBootVersion apiVersion = NBootVersion.of(computedOptions.getApiVersion());
                if (isRuntimeLoaded() && (apiVersion.isBlank() || getVersion().equals(apiVersion))) {
                    if (computedOptions.getRuntimeId() == null) {
                        computedOptions.setRuntimeId(runtimeLoadedId == null ? null : runtimeLoadedId.toString());
                        computedOptions.setRuntimeBootDescriptor(null);
                    }
                }
                //resolve runtime id
                if (computedOptions.getRuntimeId() == null) {
                    //load from local lib folder
                    NBootId runtimeId = null;
                    if (!resetFlag && !NBootUtils.firstNonNull(computedOptions.getRecover(), false)) {
                        runtimeId = NReservedMavenUtilsBoot.resolveLatestMavenId(NBootId.of(NBootConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, Collections.singletonList(NBootRepositoryLocation.of("nuts@" + computedOptions.getStoreType("LIB") + File.separatorChar + NBootConstants.Folders.ID)), computedOptions);
                    }
                    if (runtimeId == null) {
                        runtimeId = NReservedMavenUtilsBoot.resolveLatestMavenId(NBootId.of(NBootConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, resolveBootRuntimeRepositories(true), computedOptions);
                    }
                    if (runtimeId == null) {
                        runtimeId = getFallbackCache(NBootId.RUNTIME_ID, false, false).id;
                    }
                    if (runtimeId == null) {
                        bLog.log(Level.FINEST, "FAIL", NBootMsg.ofPlain("unable to resolve latest runtime-id version (is connection ok?)"));
                    }
                    computedOptions.setRuntimeId(runtimeId == null ? null : runtimeId.toString());
                    computedOptions.setRuntimeBootDescriptor(null);
                }
                if (computedOptions.getRuntimeId() == null) {
                    computedOptions.setRuntimeId((resolveDefaultRuntimeId(computedOptions.getApiVersion())));
                    bLog.log(Level.CONFIG, "READ", NBootMsg.ofC("consider default runtime-id : %s", computedOptions.getRuntimeId()));
                }
                NBootId runtimeIdObject = NBootId.of(computedOptions.getRuntimeId());
                if (NBootStringUtils.isBlank(runtimeIdObject.getVersion())) {
                    computedOptions.setRuntimeId(resolveDefaultRuntimeId(computedOptions.getApiVersion()));
                }

                //resolve runtime libraries
                if (computedOptions.getRuntimeBootDescriptor() == null && !isRuntimeLoaded()) {
                    Set<NBootId> loadedDeps = null;
                    String rid = computedOptions.getRuntimeId();
                    Path nutsRuntimeCacheConfigPath = Paths.get(computedOptions.getStoreType("CONF") + File.separator + NBootConstants.Folders.ID).resolve(NBootUtils.resolveIdPath(bootApiId)).resolve(NBootConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
                    try {
                        boolean cacheLoaded = false;
                        if (!NBootUtils.firstNonNull(computedOptions.getRecover(), false) && !resetFlag && NBootIOUtilsBoot.isFileAccessible(nutsRuntimeCacheConfigPath, computedOptions.getExpireTime(), bLog)) {
                            try {
                                Map<String, Object> obj = NBootJsonParser.parse(nutsRuntimeCacheConfigPath);
                                bLog.log(Level.CONFIG, "READ", NBootMsg.ofC("loaded %s file : %s", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString()));
                                loadedDeps = NBootId.ofSet((String) obj.get("dependencies"));
                                if (loadedDeps == null) {
                                    loadedDeps = new LinkedHashSet<>();
                                }
                            } catch (Exception ex) {
                                bLog.log(Level.FINEST, "FAIL", NBootMsg.ofC("unable to load %s file : %s : %s", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString(), ex.toString()));
                                //ignore...
                            }
                            cacheLoaded = true;
                        }

                        if (!cacheLoaded || loadedDeps == null) {
                            loadedDeps = NReservedMavenUtilsBoot.loadDependenciesFromId(NBootId.of(computedOptions.getRuntimeId()), bLog, resolveBootRuntimeRepositories(false), cache);
                            bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofC("detect runtime dependencies : %s", loadedDeps));
                        }
                    } catch (Exception ex) {
                        bLog.log(Level.FINEST, "FAIL", NBootMsg.ofC("unable to load %s file : %s", nutsRuntimeCacheConfigPath.getFileName(), ex.toString()));
                        //
                    }
                    if (loadedDeps == null) {
                        NBootIdCache r = getFallbackCache(NBootId.RUNTIME_ID, false, false);
                        loadedDeps = r.deps;
                    }

                    if (loadedDeps == null) {
                        throw new NBootException(NBootMsg.ofC("unable to load dependencies for %s", rid));
                    }
                    computedOptions.setRuntimeBootDescriptor(new NBootDescriptor().setId(computedOptions.getRuntimeId()).setDependencies(loadedDeps.stream().map(NBootId::toDependency).collect(Collectors.toList())));
                    Set<NBootRepositoryLocation> bootRepositories = resolveBootRuntimeRepositories(false);
                    if (bLog.isLoggable(Level.CONFIG)) {
                        if (bootRepositories.size() == 0) {
                            bLog.log(Level.CONFIG, "FAIL", NBootMsg.ofPlain("workspace bootRepositories could not be resolved"));
                        } else if (bootRepositories.size() == 1) {
                            bLog.log(Level.CONFIG, "INFO", NBootMsg.ofC("workspace bootRepositories resolved to : %s", bootRepositories.toArray()[0]));
                        } else {
                            bLog.log(Level.CONFIG, "INFO", NBootMsg.ofPlain("workspace bootRepositories resolved to : "));
                            for (NBootRepositoryLocation repository : bootRepositories) {
                                bLog.log(Level.CONFIG, "INFO", NBootMsg.ofC("    %s", repository));
                            }
                        }
                    }
                    computedOptions.setBootRepositories(bootRepositories.stream().map(NBootRepositoryLocation::toString).collect(Collectors.joining(";")));
                }

                //resolve extension libraries
                if (computedOptions.getExtensionBootDescriptors() == null) {
                    LinkedHashSet<String> excludedExtensions = new LinkedHashSet<>();
                    if (computedOptions.getExcludedExtensions() != null) {
                        for (String excludedExtensionGroup : computedOptions.getExcludedExtensions()) {
                            for (String excludedExtension : NBootStringUtils.split(excludedExtensionGroup, ";,", true, true)) {
                                excludedExtensions.add(NBootId.of(excludedExtension).getShortName());
                            }
                        }
                    }
                    if (computedOptions.getExtensionsSet() != null) {
                        List<NBootDescriptor> all = new ArrayList<>();
                        for (String extension : computedOptions.getExtensionsSet()) {
                            NBootId eid = NBootId.of(extension);
                            if (!excludedExtensions.contains(eid.getShortName()) && !excludedExtensions.contains(eid.getArtifactId())) {
                                Path extensionFile = Paths.get(computedOptions.getStoreType("CONF") + File.separator + NBootConstants.Folders.ID).resolve(NBootUtils.resolveIdPath(bootApiId)).resolve(NBootConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
                                Set<NBootId> loadedDeps = null;
                                if (isLoadFromCache() && NBootIOUtilsBoot.isFileAccessible(extensionFile, computedOptions.getExpireTime(), bLog)) {
                                    try {
                                        Properties obj = NBootIOUtilsBoot.loadURLProperties(extensionFile, bLog);
                                        bLog.log(Level.CONFIG, "READ", NBootMsg.ofC("loaded %s file : %s", extensionFile.getFileName(), extensionFile.toString()));
                                        List<NBootId> loadedDeps0 = NBootId.ofList((String) obj.get("dependencies"));
                                        loadedDeps = loadedDeps0 == null ? new LinkedHashSet<>() : new LinkedHashSet<>(loadedDeps0);
                                    } catch (Exception ex) {
                                        bLog.log(Level.CONFIG, "FAIL", NBootMsg.ofC("unable to load %s file : %s : %s", extensionFile.getFileName(), extensionFile.toString(), ex.toString()));
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    loadedDeps = NReservedMavenUtilsBoot.loadDependenciesFromId(eid, bLog, resolveBootRuntimeRepositories(true), cache);
                                }
                                if (loadedDeps == null) {
                                    throw new NBootException(NBootMsg.ofC("unable to load dependencies for %s", eid));
                                }
                                all.add(new NBootDescriptor().setId(NBootId.of(extension)).setDependencies(loadedDeps.stream().map(NBootId::toDependency).collect(Collectors.toList())));
                            }
                        }
                        computedOptions.setExtensionBootDescriptors(all);
                    } else {
                        computedOptions.setExtensionBootDescriptors(new ArrayList<>());
                    }
                }
            }
            newInstanceRequirements = checkRequirements(true);
            if (newInstanceRequirements == 0) {
                computedOptions.setJavaCommand(null);
                computedOptions.setJavaOptions(null);
            }
            return true;
        }
        return false;
    }

    private boolean isPlainTrace() {
        return NBootUtils.firstNonNull(computedOptions.getTrace(), true)
                && !NBootUtils.firstNonNull(computedOptions.getBot(), false)
                && (NBootUtils.sameEnum(computedOptions.getOutputFormat(), "PLAIN") || NBootStringUtils.isBlank(computedOptions.getOutputFormat()));
    }

    private boolean isLoadFromCache() {
        return !NBootUtils.firstNonNull(computedOptions.getRecover(), false) && !NBootUtils.firstNonNull(computedOptions.getReset(), false);
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     *
     * @return NWorkspace instance as object
     */
    public Object openWorkspace() {
        return openOrRunWorkspace(false);
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     *
     * @return NWorkspace instance as object
     */
    private Object openOrRunWorkspace(boolean run) {
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NBootUnsatisfiedRequirementsException(NBootMsg.ofC("unable to open a distinct version : %s from nuts#%s", getRequirementsHelpString(true), getVersion()));
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (computedOptions.getApplicationArguments().size() == 0 && NBootUtils.firstNonNull(computedOptions.getSkipBoot(), false) && (NBootUtils.firstNonNull(computedOptions.getRecover(), false) || NBootUtils.firstNonNull(computedOptions.getReset(), false))) {
            if (isPlainTrace()) {
                bLog.log(Level.WARNING, "WARNING", NBootMsg.ofC("workspace erased : %s", computedOptions.getWorkspace()));
            }
            throw new NBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        Object wsInstance = null;
        NBootErrorInfoList errorList = new NBootErrorInfoList();
        try {
            Path configFile = Paths.get(computedOptions.getWorkspace()).resolve(NBootConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            if (NBootUtils.sameEnum(computedOptions.getOpenMode(), "OPEN_OR_ERROR")) {
                //add fail fast test!!
                if (!Files.isRegularFile(configFile)) {
                    throw new NBootWorkspaceNotFoundException(computedOptions.getWorkspace());
                }
            } else if (NBootUtils.sameEnum(computedOptions.getOpenMode(), "CREATE_OR_ERROR")) {
                if (Files.exists(configFile)) {
                    throw new NBootWorkspaceAlreadyExistsException(computedOptions.getWorkspace());
                }
            }
            if (NBootStringUtils.isBlank(computedOptions.getApiVersion())
                    || NBootStringUtils.isBlank(computedOptions.getRuntimeId())
                    || (!isRuntimeLoaded() && computedOptions.getRuntimeBootDescriptor() == null)
                    || computedOptions.getExtensionBootDescriptors() == null
//                    || (!runtimeLoaded && (computedOptions.getBootRepositories().isBlank()))
            ) {
                throw new NBootException(NBootMsg.ofPlain("invalid workspace state"));
            }
            boolean recover = NBootUtils.firstNonNull(computedOptions.getRecover(), false) || NBootUtils.firstNonNull(computedOptions.getReset(), false);

            List<NBootClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = computedOptions.getStoreType("LIB") + File.separator + NBootConstants.Folders.ID;

            NBootRepositoryLocation[] repositories = NBootStringUtils.split(computedOptions.getBootRepositories(), "\n;", true, true).stream().map(NBootRepositoryLocation::of).toArray(NBootRepositoryLocation[]::new);

            NBootRepositoryLocation workspaceBootLibFolderRepo = NBootRepositoryLocation.of("nuts@" + workspaceBootLibFolder);
            computedOptions.setRuntimeBootDependencyNode(
                    isRuntimeLoaded() ? null :
                            createClassLoaderNode(computedOptions.getRuntimeBootDescriptor(), repositories, workspaceBootLibFolderRepo, recover, errorList, true)
            );

            if (computedOptions.getExtensionBootDescriptors() != null) {
                for (NBootDescriptor nutsBootDescriptor : computedOptions.getExtensionBootDescriptors()) {
                    deps.add(createClassLoaderNode(nutsBootDescriptor, repositories, workspaceBootLibFolderRepo, recover, errorList, false));
                }
            }
            computedOptions.setExtensionBootDependencyNodes(deps);
            deps.add(0, computedOptions.getRuntimeBootDependencyNode());

            bootClassWorldURLs = NBootUtils.resolveClassWorldURLs(deps.toArray(new NBootClassLoaderNode[0]), getContextClassLoader(), bLog);
            workspaceClassLoader = /*bootClassWorldURLs.length == 0 ? getContextClassLoader() : */ new NBootClassLoader(deps.toArray(new NBootClassLoaderNode[0]), getContextClassLoader());
            computedOptions.setClassWorldLoader(workspaceClassLoader);
            if (bLog.isLoggable(Level.CONFIG)) {
                if (bootClassWorldURLs.length == 0) {
                    bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofPlain("empty nuts class world. All dependencies are already loaded in classpath, most likely"));
                } else if (bootClassWorldURLs.length == 1) {
                    bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofC("resolve nuts class world to : %s %s", NBootIOUtilsBoot.getURLDigest(bootClassWorldURLs[0], bLog), bootClassWorldURLs[0]));
                } else {
                    bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofPlain("resolve nuts class world to : "));
                    for (URL u : bootClassWorldURLs) {
                        bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofC("    %s : %s", NBootIOUtilsBoot.getURLDigest(u, bLog), u));
                    }
                }
            }
            computedOptions.setClassWorldURLs(Arrays.asList(bootClassWorldURLs));
            bLog.log(Level.CONFIG, "INFO", NBootMsg.ofPlain("search for NutsBootWorkspaceFactory service implementations"));
            ServiceLoader<NBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NBootWorkspaceFactory.class, workspaceClassLoader);
            List<NBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new NBootWorkspaceFactoryComparator(computedOptions));
            if (bLog.isLoggable(Level.CONFIG)) {
                switch (factories.size()) {
                    case 0: {
                        bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofPlain("unable to detect NutsBootWorkspaceFactory service implementations"));
                        break;
                    }
                    case 1: {
                        bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofC("detect NutsBootWorkspaceFactory service implementation : %s", factories.get(0).getClass().getName()));
                        break;
                    }
                    default: {
                        bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofPlain("detect NutsBootWorkspaceFactory service implementations are :"));
                        for (NBootWorkspaceFactory u : factories) {
                            bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofC("    %s", u.getClass().getName()));
                        }
                    }
                }
            }
            NBootWorkspaceFactory factoryInstance;
            List<Throwable> exceptions = new ArrayList<>();
            for (NBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    if (bLog.isLoggable(Level.CONFIG)) {
                        bLog.log(Level.CONFIG, "INFO", NBootMsg.ofC("create workspace using %s", factoryInstance.getClass().getName()));
                    }
                    computedOptions.setBootWorkspaceFactory(factoryInstance);
                    if (run) {
                        wsInstance = a.runWorkspace(computedOptions);
                    } else {
                        wsInstance = a.createWorkspace(computedOptions);
                    }
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("unable to create workspace using factory %s", a), ex);
                    // if the creation generates an error
                    // just stop
                    break;
                }
                if (wsInstance != null) {
                    break;
                }
            }
            if (wsInstance == null) {
                //should never happen
                bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("unable to load Workspace \"%s\" from ClassPath :", computedOptions.getName()));
                for (URL url : bootClassWorldURLs) {
                    bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("\t %s", NBootIOUtilsBoot.formatURL(url)));
                }
                for (Throwable exception : exceptions) {
                    bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("%s", exception), exception);
                }
                bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("unable to load Workspace Component from ClassPath : %s", Arrays.asList(bootClassWorldURLs)));
                throw new NBootInvalidWorkspaceException(this.computedOptions.getWorkspace(), NBootMsg.ofC("unable to load Workspace Component from ClassPath : %s%n  caused by:%n\t%s", Arrays.asList(bootClassWorldURLs), exceptions.stream().map(Throwable::toString).collect(Collectors.joining("\n\t"))));
            }
            return wsInstance;
//        } catch (NReadOnlyException | NCancelException | NNoSessionCancelException ex) {
//            throw ex;
        } catch (UnsatisfiedLinkError | AbstractMethodError ex) {
            NBootMsg errorMessage = NBootMsg.ofC("unable to boot nuts workspace because the installed binaries are incompatible with the current nuts bootstrap version %s\nusing '-N' command line flag should fix the problem", getVersion());
            errorList.insert(0, new NReservedErrorInfo(null, null, null, errorMessage + ": " + ex, ex));
            logError(bootClassWorldURLs, errorList);
            throw new NBootException(errorMessage, ex);
        } catch (Throwable ex) {
            NBootMsg message = NBootMsg.ofPlain("unable to locate valid nuts-runtime package");
            errorList.insert(0, new NReservedErrorInfo(null, null, null, message + " : " + ex, ex));
            logError(bootClassWorldURLs, errorList);
            if (ex instanceof NBootException) {
                throw (NBootException) ex;
            }
            throw new NBootException(message, ex);
        }
    }

    private ClassLoader getContextClassLoader() {
        Supplier<ClassLoader> classLoaderSupplier = computedOptions.getClassLoaderSupplier();
        if (classLoaderSupplier != null) {
            ClassLoader classLoader = classLoaderSupplier.get();
            if (classLoader != null) {
                return classLoader;
            }
        }
        return Thread.currentThread().getContextClassLoader();
    }

    private void runCommandHelp() {
        String f = NBootUtils.firstNonNull(computedOptions.getOutputFormat(), "PLAIN");
        if (NBootUtils.firstNonNull(computedOptions.getDry(), false)) {
            printDryCommand("help");
        } else {
            String msg = "nuts is an open source package manager mainly for java applications. Type 'nuts help' or visit https://github.com/thevpc/nuts for more help.";
            switch (NBootUtils.enumName(f)) {
                case "JSON": {
                    bLog.outln("{");
                    bLog.outln("  \"help\": \"%s\"", msg);
                    bLog.outln("}");
                    return;
                }
                case "TSON": {
                    bLog.outln("{");
                    bLog.outln("  help: \"%s\"", msg);
                    bLog.outln("}");
                    return;
                }
                case "YAML": {
                    bLog.outln("help: %s", msg);
                    return;
                }
                case "TREE": {
                    bLog.outln("- help: %s", msg);
                    return;
                }
                case "TABLE": {
                    bLog.outln("help  %s", msg);
                    return;
                }
                case "XML": {
                    bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    bLog.outln("<string>");
                    bLog.outln(" %s", msg);
                    bLog.outln("</string>");
                    return;
                }
                case "PROPS": {
                    bLog.outln("help=%s", msg);
                    return;
                }
            }
            bLog.outln("%s", msg);
        }
    }

    private void printDryCommand(String cmd) {
        String f = NBootUtils.firstNonNull(computedOptions.getOutputFormat(), "PLAIN");
        if (NBootUtils.firstNonNull(computedOptions.getDry(), false)) {
            switch (NBootUtils.enumName(f)) {
                case "JSON": {
                    bLog.outln("{");
                    bLog.outln("  \"dryCommand\": \"%s\"", cmd);
                    bLog.outln("}");
                    return;
                }
                case "TSON": {
                    bLog.outln("{");
                    bLog.outln("  dryCommand: \"%s\"", cmd);
                    bLog.outln("}");
                    return;
                }
                case "YAML": {
                    bLog.outln("dryCommand: %s", cmd);
                    return;
                }
                case "TREE": {
                    bLog.outln("- dryCommand: %s", cmd);
                    return;
                }
                case "TABLE": {
                    bLog.outln("dryCommand  %s", cmd);
                    return;
                }
                case "XML": {
                    bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    bLog.outln("<object>");
                    bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "dryCommand", cmd);
                    bLog.outln("</object>");
                    return;
                }
                case "PROPS": {
                    bLog.outln("dryCommand=%s", cmd);
                    return;
                }
            }
            bLog.outln("[Dry] %s", getVersion());
        }
    }

    private void runCommandVersion() {
        String f = NBootUtils.firstNonNull(computedOptions.getOutputFormat(), "PLAIN");
        if (NBootUtils.firstNonNull(computedOptions.getDry(), false)) {
            printDryCommand("version");
            return;
        }
        switch (NBootUtils.enumName(f)) {
            case "JSON": {
                bLog.outln("{");
                bLog.outln("  \"version\": \"%s\",", getVersion());
                bLog.outln("  \"digest\": \"%s\"", getApiDigestOrInternal());
                bLog.outln("}");
                return;
            }
            case "TSON": {
                bLog.outln("{");
                bLog.outln("  version: \"%s\",", getVersion());
                bLog.outln("  digest: \"%s\"", getApiDigestOrInternal());
                bLog.outln("}");
                return;
            }
            case "YAML": {
                bLog.outln("version: %s", getVersion());
                bLog.outln("digest: %s", getApiDigestOrInternal());
                return;
            }
            case "TREE": {
                bLog.outln("- version: %s", getVersion());
                bLog.outln("- digest: %s", getApiDigestOrInternal());
                return;
            }
            case "TABLE": {
                bLog.outln("version      %s", getVersion());
                bLog.outln("digest  %s", getApiDigestOrInternal());
                return;
            }
            case "XML": {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("<object>");
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "version", getVersion());
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "digest", getApiDigestOrInternal());
                bLog.outln("</object>");
                return;
            }
            case "PROPS": {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("version=%s", getVersion());
                bLog.outln("digest=%s", getApiDigestOrInternal());
                bLog.outln("</object>");
                return;
            }
        }
        bLog.outln("%s", getVersion());
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     *
     * @return NWorkspace instance as object
     */
    public Object runWorkspace() {
        if (NBootUtils.firstNonNull(computedOptions.getCommandHelp(), false)) {
            runCommandHelp();
            return null;
        } else if (NBootUtils.firstNonNull(computedOptions.getCommandVersion(), false)) {
            runCommandVersion();
            return null;
        }
        if (hasUnsatisfiedRequirements()) {
            runNewProcess();
            return null;
        }
        return this.openOrRunWorkspace(true);
    }

    private void fallbackInstallActionUnavailable(String message) {
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain(message));
    }

    private void logError(URL[] bootClassWorldURLs, NBootErrorInfoList ths) {
        String workspace = computedOptions.getWorkspace();
        Map<String, String> rbc_locations = computedOptions.getStoreLocations();
        if (rbc_locations == null) {
            rbc_locations = new HashMap<>();
        }
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("unable to bootstrap nuts (digest %s):", getApiDigestOrInternal()));
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("%s", ths.list().get(0)));
        }
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("here after current environment info:"));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-boot-api-version            : %s", NBootUtils.firstNonNull(computedOptions.getApiVersion(), "<?> Not Found!")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-boot-runtime                : %s", NBootUtils.firstNonNull(computedOptions.getRuntimeId(), "<?> Not Found!")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-boot-repositories           : %s", NBootUtils.firstNonNull(computedOptions.getBootRepositories(), "<?> Not Found!")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  workspace-location               : %s", NBootUtils.firstNonNull(workspace, "<default-location>")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-bin                   : %s", rbc_locations.get("BIN")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-conf                  : %s", rbc_locations.get("CONF")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-var                   : %s", rbc_locations.get("VAR")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-log                   : %s", rbc_locations.get("LOG")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-temp                  : %s", rbc_locations.get("TEMP")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-cache                 : %s", rbc_locations.get("CACHE")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-run                   : %s", rbc_locations.get("RUN")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-lib                   : %s", rbc_locations.get("LIB")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-strategy              : %s", NBootUtils.desc(computedOptions.getStoreStrategy())));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-store-layout                : %s", NBootUtils.desc(computedOptions.getStoreLayout())));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-boot-args                   : %s", asCmdLine(this.computedOptions, null)));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-app-args                    : %s", this.computedOptions.getApplicationArguments()));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  option-read-only                 : %s", NBootUtils.firstNonNull(this.computedOptions.getReadOnly(), false)));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  option-trace                     : %s", NBootUtils.firstNonNull(this.computedOptions.getTrace(), false)));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  option-progress                  : %s", NBootUtils.desc(this.computedOptions.getProgressOptions())));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  option-open-mode                 : %s", NBootUtils.desc(NBootUtils.firstNonNull(this.computedOptions.getOpenMode(), "OPEN_OR_CREATE"))));

        NBootClassLoaderNode rtn = this.computedOptions.getRuntimeBootDependencyNode();
        String rtHash = "";
        if (rtn != null) {
            rtHash = NBootIOUtilsBoot.getURLDigest(rtn.getURL(), bLog);
        }
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-runtime-digest                : %s", rtHash));

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-runtime-classpath           : %s", "<none>"));
        } else {
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-runtime-hash                : %s", "<none>"));
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  nuts-runtime-classpath           : %s", NBootIOUtilsBoot.formatURL(bootClassWorldURL)));
                } else {
                    bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("                                     %s", NBootIOUtilsBoot.formatURL(bootClassWorldURL)));
                }
            }
        }
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  java-version                     : %s", System.getProperty("java.version")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  java-executable                  : %s", NBootUtils.resolveJavaCommand(null)));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  java-class-path                  : %s", System.getProperty("java.class.path")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  java-library-path                : %s", System.getProperty("java.library.path")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  os-name                          : %s", System.getProperty("os.name")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  os-arch                          : %s", System.getProperty("os.arch")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  os-version                       : %s", System.getProperty("os.version")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  user-name                        : %s", System.getProperty("user.name")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  user-home                        : %s", System.getProperty("user.home")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC("  user-dir                         : %s", System.getProperty("user.dir")));
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain(""));
        NBootLogConfig logConfig = this.computedOptions.getLogConfig();
        if (logConfig == null || logConfig.getLogTermLevel() == null || (logConfig.getLogFileLevel() != null && logConfig.getLogFileLevel().intValue() > Level.FINEST.intValue())) {
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("If the problem persists you may want to get more debug info by adding '--verbose' arguments."));
        }
        if (!NBootUtils.firstNonNull(this.computedOptions.getReset(), false) && !NBootUtils.firstNonNull(this.computedOptions.getRecover(), false) && this.computedOptions.getExpireTime() == null) {
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments."));
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("Here is the proper command : "));
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("  java -jar nuts.jar --verbose --recover --expire [...]"));
        } else if (!NBootUtils.firstNonNull(this.computedOptions.getReset(), false) && NBootUtils.firstNonNull(this.computedOptions.getRecover(), false) && this.computedOptions.getExpireTime() == null) {
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("You may also enable full reset mode to ignore existing configuration with '--reset' argument."));
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("ATTENTION: this will delete all your nuts configuration. Use it at your own risk."));
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("Here is the proper command : "));
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("  java -jar nuts.jar --verbose --reset [...]"));
        }
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("error stack trace is:"));
            for (NReservedErrorInfo th : ths.list()) {
                StringBuilder msg = new StringBuilder();
                List<Object> msgParams = new ArrayList<>();
                msg.append("[error]");
                if (th.getNutsId() != null) {
                    msg.append(" <id>=%s");
                    msgParams.add(th.getNutsId());
                }
                if (th.getRepository() != null) {
                    msg.append(" <repo>=%s");
                    msgParams.add(th.getRepository());
                }
                if (th.getUrl() != null) {
                    msg.append(" <url>=%s");
                    msgParams.add(th.getUrl());
                }
                if (th.getThrowable() != null) {
                    msg.append(" <error>=%s");
                    msgParams.add(th.getThrowable().toString());
                } else {
                    msg.append(" <error>=%s");
                    msgParams.add("unexpected error");
                }
                bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofC(msg.toString(), msgParams.toArray()));
                bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain(th.toString()), th.getThrowable());
            }
        } else {
            bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("no stack trace is available."));
        }
        bLog.log(Level.SEVERE, "FAIL", NBootMsg.ofPlain("now exiting nuts, Bye!"));
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NBootStringUtils.isBlank(computedOptions.getApiVersion())) {
            if (!unsatisfiedOnly || !computedOptions.getApiVersion().equals(getVersion())) {
                req += 1;
            }
        }
        if (!unsatisfiedOnly || !NBootUtils.isActualJavaCommand(computedOptions.getJavaCommand())) {
            req += 2;
        }
        if (!unsatisfiedOnly || !NBootUtils.isActualJavaOptions(computedOptions.getJavaOptions())) {
            req += 4;
        }
        return req;
    }

    /**
     * return a string representing unsatisfied constraints
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return a string representing unsatisfied constraints
     */
    public String getRequirementsHelpString(boolean unsatisfiedOnly) {
        int req = unsatisfiedOnly ? newInstanceRequirements : checkRequirements(false);
        StringBuilder sb = new StringBuilder();
        if ((req & 1) != 0) {
            sb.append("nuts version ").append(NBootId.ofApi(computedOptions.getApiVersion()));
        }
        if ((req & 2) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java command ").append(computedOptions.getJavaCommand());
        }
        if ((req & 4) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java options ").append(computedOptions.getJavaOptions());
        }
        if (sb.length() > 0) {
            sb.insert(0, "required ");
            return sb.toString();
        }
        return null;
    }

    private NBootClassLoaderNode createClassLoaderNode(NBootDescriptor descr, NBootRepositoryLocation[] repositories, NBootRepositoryLocation workspaceBootLibFolder, boolean recover, NBootErrorInfoList errorList, boolean runtimeDep) throws MalformedURLException {
        NBootId id = descr.getId();
        List<NBootDependency> deps = descr.getDependencies();
        NBootClassLoaderNodeBuilder rt = new NBootClassLoaderNodeBuilder();
        String name = runtimeDep ? "runtime" : ("extension " + id.toString());
        File file = NReservedMavenUtilsBoot.getBootCacheJar(NBootId.of(computedOptions.getRuntimeId()), repositories, workspaceBootLibFolder, !recover, name, computedOptions.getExpireTime(), errorList, computedOptions, pathExpansionConverter, bLog, cache);
        rt.setId(id.toString());
        rt.setUrl(file.toURI().toURL());
        rt.setIncludedInClasspath(NBootUtils.isLoadedClassPath(rt.getURL(), getContextClassLoader(), bLog));

        if (bLog.isLoggable(Level.CONFIG)) {
            String rtHash = "";
            if (computedOptions.getRuntimeId() != null) {
                rtHash = NBootIOUtilsBoot.getFileOrDirectoryDigest(file.toPath());
                if (rtHash == null) {
                    rtHash = "";
                }
            }
            bLog.log(Level.CONFIG, "INFO", NBootMsg.ofC("detect %s version %s - digest %s from %s", name, id.toString(), rtHash, file));
        }

        for (NBootDependency s : deps) {
            NBootClassLoaderNodeBuilder x = new NBootClassLoaderNodeBuilder();
            if (NBootUtils.isAcceptDependency(s, computedOptions)) {
                x.setId(s.toString()).setUrl(NReservedMavenUtilsBoot.getBootCacheJar(s.toId(), repositories, workspaceBootLibFolder, !recover, name + " dependency", computedOptions.getExpireTime(), errorList, computedOptions, pathExpansionConverter, bLog, cache).toURI().toURL());
                x.setIncludedInClasspath(NBootUtils.isLoadedClassPath(x.getURL(), getContextClassLoader(), bLog));
                rt.addDependency(x.build());
            }
        }
        return rt.build();
    }

    private String resolveDefaultRuntimeId(String sApiVersion) {
        // check fo qualifier
        int q = sApiVersion.indexOf('-');
        if (q > 0) {
            return NBootId.ofRuntime((sApiVersion.substring(0, q) + ".0" + sApiVersion.substring(q))).toString();
        }
        return NBootId.ofRuntime(sApiVersion + ".0").toString();
    }
}
