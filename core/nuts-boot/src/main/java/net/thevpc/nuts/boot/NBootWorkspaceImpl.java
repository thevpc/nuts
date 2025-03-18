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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NExceptionBootAware;
import net.thevpc.nuts.NExceptionWithExitCodeBase;
import net.thevpc.nuts.NWorkspaceBase;
import net.thevpc.nuts.boot.reserved.cmdline.*;
import net.thevpc.nuts.boot.reserved.util.*;
import net.thevpc.nuts.boot.reserved.util.NReservedErrorInfo;
import net.thevpc.nuts.boot.reserved.util.NBootErrorInfoList;
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
 * {@link #getWorkspace()} . NutsBootWorkspace is also responsible of managing
 * local jar cache folder located at ~/.cache/nuts/default-workspace/boot
 * <br>
 * Default Bootstrap implementation. This class is responsible of loading
 * initial nuts-runtime.jar and its dependencies and for creating workspaces
 * using the method {@link #getWorkspace()}.
 * <br>
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public final class NBootWorkspaceImpl implements NBootWorkspace {
    public static final boolean DEFAULT_PREVIEW = true;
    private final Instant creationTime = Instant.now();
    private NBootOptionsInfo options;
    private final NBootLog bLog;
    private final NBootRepositoryDB repositoryDB = new NBootRepositoryDB();
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "workspace":
                    return options.getWorkspace();
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
                    return NBootUtils.getHome(from.substring("home.".length()).toUpperCase(), options);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<String, String> s = NBootUtils.firstNonNull(options.getStoreLocations(), Collections.emptyMap());
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
    private NBootOptionsInfo lastWorkspaceOptions;
    //private Set<NRepositoryLocationBoot> parsedBootRuntimeDependenciesRepositories;
    private Set<NBootRepositoryLocation> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private Scanner scanner;
    private NBootCache cache = new NBootCache();
    Boolean runtimeLoaded;
    NBootId runtimeLoadedId;
    NBootArguments unparsedOptions;
    NWorkspaceBase loadedWorkspace;
    Runnable exceptionRunnable;

    public NBootWorkspaceImpl(NBootArguments userOptionsUnparsed) {
        if (userOptionsUnparsed == null) {
            userOptionsUnparsed = new NBootArguments();
        }
        NBootOptionsInfo userOptions = new NBootOptionsInfo();
        try {
            this.unparsedOptions = userOptionsUnparsed;
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
                bLog.warn(NBootMsg.ofC("Skipped Error : %s", errorMessage));
            }
            if (userOptionsUnparsed.getAppArgs() != null) {
                userOptions.getApplicationArguments().addAll(Arrays.asList(userOptionsUnparsed.getAppArgs()));
            }
            this.options = userOptions.copy();
            this.postInit();
        } catch (Exception e) {
            NBootErrorInfoList li = new NBootErrorInfoList();
            li.add(new NReservedErrorInfo(null, null, null, NBootUtils.getErrorMessage(e), e));
            NBootOptionsInfo currOptions = options == null ? userOptions : options;
            logError(new URL[0], li, currOptions);
            throw new NBootException(NBootMsg.ofC("unable to initialize boot %s#%s : %s", NBootConstants.Ids.NUTS_API, currOptions.getApiVersion(), e));
        }
    }

    public NBootArguments getBootArguments() {
        return unparsedOptions;
    }

    public NBootOptionsInfo getOptions() {
        return options;
    }

    public NBootWorkspaceImpl(NBootOptionsInfo options) {
        if (options == null) {
            options = new NBootOptionsInfo();
        }
        this.bLog = new NBootLog(options);
        this.options = options;
        this.postInit();
    }

    private void postInit() {
        if (options == null) {
            options = new NBootOptionsInfo();
        }
        if (this.options.getCreationTime() == null) {
            this.options.setCreationTime(creationTime);
        }
        this.bLog.setOptions(this.options);
    }

    private static void revalidateLocations(NBootOptionsInfo bootOptions, String workspaceName, boolean immediateLocation, String sandboxMode) {
        if (NBootUtils.isBlank(bootOptions.getName())) {
            bootOptions.setName(workspaceName);
        }
        boolean system = NBootUtils.firstNonNull(bootOptions.getSystem(), false);
        if (NBootUtils.sameEnum(sandboxMode, "SANDBOX") || NBootUtils.sameEnum(sandboxMode, "MEMORY")) {
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
        if (parsedBootRuntimeRepositories != null) {
            return parsedBootRuntimeRepositories;
        }
        bLog.log(Level.FINE, "START", NBootMsg.ofC("resolve boot repositories to load nuts-runtime from options : %s and config: %s",
                options.getRepositories() == null ? "[]" : options.getRepositories().toString(),
                options.getBootRepositories() == null ? "[]" : options.getBootRepositories()));
        NBootRepositorySelectorList bootRepositoriesSelector = NBootRepositorySelectorList.of(options.getRepositories(), repositoryDB);
        NBootRepositorySelector[] old = NBootRepositorySelectorList.of(Arrays.asList(options.getBootRepositories()), repositoryDB).toArray();
        NBootRepositoryLocation[] result;
        if (old.length == 0) {
            //no previous config, use defaults!
            List<NBootRepositoryLocation> drs = new ArrayList<>();
            List<String> tags = new ArrayList<>();
            tags.add(NBootConstants.RepoTags.MAIN);
            if (options.getPreviewRepo() != null) {
                if (options.getPreviewRepo()) {
                    tags.add(NBootConstants.RepoTags.PREVIEW);
                }
            } else if (DEFAULT_PREVIEW) {
                tags.add(NBootConstants.RepoTags.PREVIEW);
            }
            for (String s : repositoryDB.findByAnyTag(tags.toArray(new String[0]))) {
                if ("maven".equals(s)) {
                    for (String customOption : options.getCustomOptions()) {
                        NBootArg a = new NBootArg(customOption);
                        if ("---m2".equals(a.key())) {
                            if (a.isActive()) {
                                boolean m2 = a.isEnabled()
                                        ? NBootUtils.parseBoolean(a.getValue(), true, true)
                                        : !NBootUtils.parseBoolean(a.getValue(), true, false);
                                if (!m2) {
                                    continue;
                                }
                            }
                            break;
                        }
                    }
                }
                drs.add(NBootRepositoryLocation.of(s, repositoryDB));
            }
            result = drs.toArray(new NBootRepositoryLocation[0]);
        } else {
            result = bootRepositoriesSelector.resolve(Arrays.stream(old).map(x -> NBootRepositoryLocation.of(x.getName(), x.getUrl())).toArray(NBootRepositoryLocation[]::new), repositoryDB);
        }
        result = Arrays.stream(result).map(
                r -> {
                    if (NBootUtils.isBlank(r.getLocationType()) || NBootUtils.isBlank(r.getName())) {
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
                                    if (NBootUtils.isBlank(r.getLocationType())) {
                                        Object o = jsonObject.get("repositoryType");
                                        if (o instanceof String && !NBootUtils.isBlank((String) o)) {
                                            r = r.setLocationType(String.valueOf(o));
                                        }
                                    }
                                    if (NBootUtils.isBlank(r.getName())) {
                                        Object o = jsonObject.get("repositoryName");
                                        if (o instanceof String && !NBootUtils.isBlank((String) o)) {
                                            r = r.setName(String.valueOf(o));
                                        }
                                    }
                                    if (NBootUtils.isBlank(r.getName())) {
                                        r = r.setName(r.getName());
                                    }
                                }
                            } catch (Exception e) {
                                bLog.log(Level.CONFIG, "WARNING", NBootMsg.ofC("unable to load %s", r2));
                            }
                        }
                        if (fileExists) {
                            if (NBootUtils.isBlank(r.getLocationType())) {
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
        bLog.log(Level.FINE, "START", NBootMsg.ofC("resolve boot repositories to load nuts-runtime as %s",
                parsedBootRuntimeRepositories
        ));
        return rr;
    }

    public String[] createProcessCmdLine() {
        prepareWorkspace();
        bLog.log(Level.FINE, "START", NBootMsg.ofC("running version %s.  %s", options.getApiVersion(), getRequirementsHelpString(true)));
        String defaultWorkspaceLibFolder = options.getStoreType("LIB") + "/" + NBootConstants.Folders.ID;
        List<NBootRepositoryLocation> repos = new ArrayList<>();
        repos.add(NBootRepositoryLocation.of("nuts@" + defaultWorkspaceLibFolder));
        Collection<NBootRepositoryLocation> bootRepositories = resolveBootRuntimeRepositories(true);
        repos.addAll(bootRepositories);
        NBootErrorInfoList errorList = new NBootErrorInfoList();
        File file = NReservedMavenUtilsBoot.resolveOrDownloadJar(NBootId.ofApi(options.getApiVersion()), repos.toArray(new NBootRepositoryLocation[0]),
                NBootRepositoryLocation.of("nuts@" + options.getStoreType("LIB") + File.separator + NBootConstants.Folders.ID), bLog, false, options.getExpireTime(), errorList);
        if (file == null) {
            errorList.insert(0, new NReservedErrorInfo(null, null, null, "unable to load nuts " + options.getApiVersion(), null));
            logError(null, errorList, options);
            throw new NBootException(NBootMsg.ofC("unable to load %s#%s", NBootConstants.Ids.NUTS_API, options.getApiVersion()));
        }

        List<String> cmd = new ArrayList<>();
        String jc = options.getJavaCommand();
        if (jc == null || jc.trim().isEmpty()) {
            jc = NBootUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : NBootCmdLine.parseDefaultList(options.getJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        if (options.getJavaOptions() == null) {
            Collections.addAll(cmd, NBootCmdLine.parseDefaultList(options.getJavaOptions()));
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(asCmdLine(options, new NBootWorkspaceOptionsConfig().setCompact(true).setApiVersion(options.getApiVersion())).toStringList());
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

    private NBootCmdLine asCmdLine(NBootOptionsInfo cc, NBootWorkspaceOptionsConfig conf) {
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

    private NBootIdCache getFallbackCache(NBootId baseId, boolean lastWorkspace, boolean copyTemp) {
        NBootIdCache old = cache.fallbackIdMap.get(baseId);
        if (old != null) {
            return old;
        }

        NBootIdCache fid = new NBootIdCache();
        fid.baseId = baseId;
        cache.fallbackIdMap.put(fid.baseId, fid);
        String s = (lastWorkspace ? lastWorkspaceOptions : options).getStoreLocations().get("LIB") + "/id/"
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
                        if (!(baseId.equals(NBootId.RUNTIME_ID) && !version.toString().startsWith(NBootWorkspaceImpl.NUTS_BOOT_VERSION + "."))) {
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
                        .filter(x -> NBootUtils.isAcceptDependency(x.toDependency(), options))
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
            String isolationLevel = NBootUtils.firstNonBlank(options.getIsolationLevel(), "SYSTEM");
            if (bLog.isLoggable(Level.CONFIG)) {
                bLog.log(Level.CONFIG, "START", NBootMsg.ofC("bootstrap Nuts version %s %s digest %s...", NBootWorkspaceImpl.NUTS_BOOT_VERSION,
                        NBootUtils.sameEnum(isolationLevel, "SYSTEM") ? "" :
                                NBootUtils.sameEnum(isolationLevel, "USER") ? " (user mode)" :
                                        NBootUtils.sameEnum(isolationLevel, "CONFINED") ? " (confined mode)" :
                                                NBootUtils.sameEnum(isolationLevel, "SANDBOX") ? " (sandbox mode)" :
                                                        NBootUtils.sameEnum(isolationLevel, "MEMORY") ? " (in-memory mode)"
                                                                : " (unsupported mode " + isolationLevel + ")",
                        getApiDigestOrInternal()));
                bLog.log(Level.CONFIG, "START", NBootMsg.ofPlain("boot-class-path:"));
                for (String s : NBootUtils.split(System.getProperty("java.class.path"), File.pathSeparator, true, true)) {
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
                    bLog.log(Level.CONFIG, "START", NBootMsg.ofC("    %s = %s", NBootUtils.formatAlign(k, max, NBootPositionTypeBoot.FIRST), NBootUtils.formatStringLiteral(m.get(k))));
                }
            }
            String workspaceName = null;
            NBootOptionsInfo lastConfigLoaded = null;
            String lastNutsWorkspaceJsonConfigPath = null;
            boolean immediateLocation = false;
            boolean resetHardFlag = NBootUtils.firstNonNull(options.getResetHard(), false);
            boolean resetFlag = NBootUtils.firstNonNull(options.getReset(), false);
            boolean dryFlag = NBootUtils.firstNonNull(options.getDry(), false);
            String _ws = options.getWorkspace();
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
                if (NBootUtils.firstNonNull(options.getSystem(), false)) {
                    throw new NBootException(NBootMsg.ofPlain("you cannot specify option '--global' in sandbox mode"));
                }
                if (!NBootUtils.isBlank(options.getWorkspace())) {
                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--workspace' in sandbox mode"));
                }
                if (!NBootUtils.isBlank(options.getStoreStrategy()) && !NBootUtils.sameEnum(options.getStoreStrategy(), "STANDALONE")) {
                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--exploded' in sandbox mode"));
                }
                if (NBootUtils.firstNonNull(options.getSystem(), false)) {
                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--global' in sandbox mode"));
                }
                options.setWorkspace(lastNutsWorkspaceJsonConfigPath);
//            }else if (NBootUtils.sameEnum(isolationLevel, "MEMORY")) {
//                Path t = null;
//                try {
//                    t = Files.createTempDirectory("nuts-memory-" + Instant.now().toString().replace(':', '-'));
//                } catch (IOException e) {
//                    throw new NBootException(NBootMsg.ofPlain("unable to create temporary/sandbox folder"), e);
//                }
//                lastNutsWorkspaceJsonConfigPath = t.toString();
//                immediateLocation = true;
//                workspaceName = t.getFileName().toString();
//                resetFlag = false; //no need for reset
//                if (NBootUtils.firstNonNull(options.getSystem(), false)) {
//                    throw new NBootException(NBootMsg.ofPlain("you cannot specify option '--global' in in-memory mode"));
//                }
//                if (!NBootUtils.isBlank(options.getWorkspace())) {
//                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--workspace' in in-memory mode"));
//                }
//                if (!NBootUtils.isBlank(options.getStoreStrategy())  && !NBootUtils.sameEnum(options.getStoreStrategy(), "STANDALONE")) {
//                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--exploded' in in-memory mode"));
//                }
//                if (NBootUtils.firstNonNull(options.getSystem(), false)) {
//                    throw new NBootException(NBootMsg.ofPlain("you cannot specify '--global' in in-memory mode"));
//                }
//                options.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            } else {
                if (!NBootUtils.sameEnum(isolationLevel, "SYSTEM") && NBootUtils.firstNonNull(options.getSystem(), false)) {
                    if (NBootUtils.firstNonNull(options.getReset(), false)) {
                        throw new NBootException(NBootMsg.ofC("invalid option 'global' in %s mode", isolationLevel));
                    }
                }
                if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                    //this is a protocol based workspace
                    //String protocol=ws.substring(0,ws.indexOf("://"));
                    workspaceName = "remote-bootstrap";
                    lastNutsWorkspaceJsonConfigPath = NBootPlatformHome.of(null, NBootUtils.firstNonNull(options.getSystem(), false)).getWorkspaceLocation(NBootUtils.resolveValidWorkspaceName(workspaceName));
                    lastConfigLoaded = NBootBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                    immediateLocation = true;

                } else {
                    immediateLocation = NBootUtils.isValidWorkspaceName(_ws);
                    int maxDepth = 36;
                    for (int i = 0; i < maxDepth; i++) {
                        lastNutsWorkspaceJsonConfigPath = NBootUtils.isValidWorkspaceName(_ws) ? NBootPlatformHome.of(null, NBootUtils.firstNonNull(options.getSystem(), false)).getWorkspaceLocation(NBootUtils.resolveValidWorkspaceName(_ws)) : NBootUtils.getAbsolutePath(_ws);

                        NBootOptionsInfo configLoaded = NBootBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                        if (configLoaded == null) {
                            //not loaded
                            break;
                        }
                        if (NBootUtils.isBlank(configLoaded.getWorkspace())) {
                            lastConfigLoaded = configLoaded;
                            break;
                        }
                        _ws = configLoaded.getWorkspace();
                        if (i >= maxDepth - 1) {
                            throw new NBootException(NBootMsg.ofPlain("cyclic workspace resolution"));
                        }
                    }
                    workspaceName = NBootUtils.resolveValidWorkspaceName(options.getWorkspace());
                }
            }
            options.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            if (lastConfigLoaded != null) {
                options.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                options.setName(lastConfigLoaded.getName());
                options.setUuid(lastConfigLoaded.getUuid());
                NBootOptionsInfo curr;
                if (!resetFlag && !resetHardFlag) {
                    curr = options;
                } else {
                    lastWorkspaceOptions = new NBootOptionsInfo();
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
            revalidateLocations(options, workspaceName, immediateLocation, isolationLevel);
            long countDeleted = 0;
            //now that config is prepared proceed to any cleanup
            if (resetHardFlag) {
                //force loading version early, it will be used later-on
                bLog.log(isAskConfirm(getOptions()) ? Level.OFF : Level.WARNING, "WARNING", NBootMsg.ofPlain("reset hard all workspaces"));
                if (lastWorkspaceOptions != null) {
                    revalidateLocations(lastWorkspaceOptions, workspaceName, immediateLocation, isolationLevel);
                }
                if (dryFlag) {
                    //
                } else {
                    countDeleted = NBootUtils.deleteStoreLocationsHard(lastWorkspaceOptions, getOptions(), bLog, () -> scanner.nextLine());
                    NBootUtils.ndiUndo(bLog, null, false);
                }
            } else if (resetFlag) {
                //force loading version early, it will be used later-on
                bLog.log(isAskConfirm(getOptions()) ? Level.OFF : Level.WARNING, "WARNING", NBootMsg.ofPlain("reset workspace"));
                if (dryFlag) {
                    //
                } else {
                    if (lastWorkspaceOptions != null) {
                        revalidateLocations(lastWorkspaceOptions, workspaceName, immediateLocation, isolationLevel);
                        getFallbackCache(NBootId.RUNTIME_ID, true, true);
                        countDeleted = NBootUtils.deleteStoreLocations(lastWorkspaceOptions, getOptions(), true, bLog, NBootPlatformHome.storeTypes(), () -> scanner.nextLine());
                    } else {
                        getFallbackCache(NBootId.RUNTIME_ID, false, true);
                        countDeleted = NBootUtils.deleteStoreLocations(options, getOptions(), true, bLog, NBootPlatformHome.storeTypes(), () -> scanner.nextLine());
                    }
                    NBootUtils.ndiUndo(bLog, workspaceName, true);
                }
            } else if (NBootUtils.firstNonNull(options.getRecover(), false)) {
                bLog.log(isAskConfirm(getOptions()) ? Level.OFF : Level.WARNING, "WARNING", NBootMsg.ofPlain("recover workspace."));
                if (dryFlag) {
                    //bLog.log(Level.INFO, "DEBUG", NBootMsg.ofPlain("[dry] [recover] delete CACHE/TEMP workspace folders"));
                } else {
                    List<Object> folders = new ArrayList<>();
                    folders.add("CACHE");
                    folders.add("TEMP");
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = NBootUtils.getStoreLocationPath(options, "LIB");
                    if (p != null) {
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts"));
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = NBootUtils.deleteStoreLocations(options, getOptions(), false, bLog, folders.toArray(), () -> scanner.nextLine());
                }
            }
            if (options.getExtensionsSet() == null) {
                if (lastWorkspaceOptions != null && !resetFlag && !resetHardFlag) {
                    options.setExtensionsSet(NBootUtils.firstNonNull(lastWorkspaceOptions.getExtensionsSet(), Collections.emptySet()));
                } else {
                    options.setExtensionsSet(Collections.emptySet());
                }
            }
            if (options.getHomeLocations() == null) {
                if (lastWorkspaceOptions != null && !resetFlag && !resetHardFlag) {
                    options.setHomeLocations(NBootUtils.firstNonNull(lastWorkspaceOptions.getHomeLocations(), Collections.emptyMap()));
                } else {
                    options.setHomeLocations(Collections.emptyMap());
                }
            }
            if (options.getStoreLayout() == null) {
                if (lastWorkspaceOptions != null && !resetFlag && !resetHardFlag) {
                    options.setStoreLayout(NBootUtils.firstNonNull(lastWorkspaceOptions.getStoreLayout(), NBootPlatformHome.currentOsFamily()));
                } else {
                    options.setHomeLocations(Collections.emptyMap());
                }
            }

            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (
                    NBootUtils.isEmptyList(options.getApplicationArguments())
                            && NBootUtils.firstNonNull(options.getSkipBoot(), true)
                            && resetHardFlag
            ) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        bLog.warn(NBootMsg.ofC("nuts hard reset successfully"));
                    } else {
                        bLog.warn(NBootMsg.ofC("nuts hard reset did not require to delete any file. system is clean."));
                    }
                }
                throw new NBootException(NBootMsg.ofPlain(""), 0);
            } else if (
                    NBootUtils.isEmptyList(options.getApplicationArguments())
                            && NBootUtils.firstNonNull(options.getSkipBoot(), false)
                            && (NBootUtils.firstNonNull(options.getRecover(), false) || resetFlag)
            ) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        bLog.warn(NBootMsg.ofC("workspace erased : %s", options.getWorkspace()));
                    } else {
                        bLog.warn(NBootMsg.ofC("workspace is not erased because it does not exist : %s", options.getWorkspace()));
                    }
                }
                throw new NBootException(NBootMsg.ofPlain(""), 0);
            }
            //after eventual clean up
            if (NBootUtils.firstNonNull(options.getInherited(), false)) {
                //when Inherited, always use the current Api version!
                options.setApiVersion(NBootWorkspaceImpl.NUTS_BOOT_VERSION);
            } else {
                NBootVersion nutsVersion = NBootVersion.of(options.getApiVersion());
                if (nutsVersion.isLatestVersion() || nutsVersion.isReleaseVersion()) {
                    NBootId s = NReservedMavenUtilsBoot.resolveLatestMavenId(NBootId.ofApi(""), null, bLog, resolveBootRuntimeRepositories(true), options, cache);
                    if (s == null) {
                        throw new NBootException(NBootMsg.ofPlain("unable to load latest nuts version"));
                    }
                    options.setApiVersion(s.getVersion());
                }
                if (nutsVersion.isBlank()) {
                    options.setApiVersion(NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                }
            }

            NBootId bootApiId = NBootId.ofApi(options.getApiVersion());
            Path nutsApiConfigBootPath = Paths.get(options.getStoreType("CONF") + File.separator + NBootConstants.Folders.ID).resolve(NBootUtils.resolveIdPath(bootApiId)).resolve(NBootConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && NBootUtils.isFileAccessible(nutsApiConfigBootPath, options.getExpireTime(), bLog)) {
                try {
                    Map<String, Object> obj = NBootJsonParser.parse(nutsApiConfigBootPath);
                    if (!obj.isEmpty()) {
                        bLog.log(Level.CONFIG, "READ", NBootMsg.ofC("loaded %s file : %s", nutsApiConfigBootPath.getFileName(), nutsApiConfigBootPath.toString()));
                        loadedApiConfig = true;
                        if (options.getRuntimeId() == null) {
                            String runtimeId = (String) obj.get("runtimeId");
                            if (NBootUtils.isBlank(runtimeId)) {
                                bLog.log(Level.CONFIG, "FAIL", NBootMsg.ofC("%s does not contain runtime-id", nutsApiConfigBootPath));
                            }
                            options.setRuntimeId(runtimeId);
                        }
                        if (options.getJavaCommand() == null) {
                            options.setJavaCommand((String) obj.get("javaCommand"));
                        }
                        if (options.getJavaOptions() == null) {
                            options.setJavaOptions((String) obj.get("javaOptions"));
                        }
                    }
                } catch (UncheckedIOException e) {
                    bLog.log(Level.CONFIG, "READ", NBootMsg.ofC("unable to read %s : %s", nutsApiConfigBootPath, e));
                }
            }
            if (!loadedApiConfig || options.getRuntimeId() == null || options.getRuntimeBootDescriptor() == null || options.getExtensionBootDescriptors() == null || options.getBootRepositories() == null) {

                NBootVersion apiVersion = NBootVersion.of(options.getApiVersion());
                if (isRuntimeLoaded() && (apiVersion.isBlank() || NBootWorkspaceImpl.NUTS_BOOT_VERSION.equals(apiVersion.toString()))) {
                    if (options.getRuntimeId() == null) {
                        options.setRuntimeId(runtimeLoadedId == null ? null : runtimeLoadedId.toString());
                        options.setRuntimeBootDescriptor(null);
                    }
                }
                //resolve runtime id
                if (options.getRuntimeId() == null) {
                    //load from local lib folder
                    NBootId runtimeId = null;
                    if (!resetFlag && !resetHardFlag && !NBootUtils.firstNonNull(options.getRecover(), false)) {
                        runtimeId = NReservedMavenUtilsBoot.resolveLatestMavenId(NBootId.of(NBootConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, Collections.singletonList(NBootRepositoryLocation.of("nuts@" + options.getStoreType("LIB") + File.separatorChar + NBootConstants.Folders.ID)), options, cache);
                    }
                    if (runtimeId == null) {
                        runtimeId = NReservedMavenUtilsBoot.resolveLatestMavenId(NBootId.of(NBootConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, resolveBootRuntimeRepositories(true), options, cache);
                    }
                    if (runtimeId == null) {
                        runtimeId = getFallbackCache(NBootId.RUNTIME_ID, false, false).id;
                    }
                    if (runtimeId == null) {
                        bLog.log(Level.FINEST, "FAIL", NBootMsg.ofPlain("unable to resolve latest runtime-id version (is connection ok?)"));
                    }
                    options.setRuntimeId(runtimeId == null ? null : runtimeId.toString());
                    options.setRuntimeBootDescriptor(null);
                }
                if (options.getRuntimeId() == null) {
                    options.setRuntimeId((resolveDefaultRuntimeId(options.getApiVersion())));
                    bLog.log(Level.CONFIG, "READ", NBootMsg.ofC("consider default runtime-id : %s", options.getRuntimeId()));
                }
                NBootId runtimeIdObject = NBootId.of(options.getRuntimeId());
                if (NBootUtils.isBlank(runtimeIdObject.getVersion())) {
                    options.setRuntimeId(resolveDefaultRuntimeId(options.getApiVersion()));
                }

                //resolve runtime libraries
                if (options.getRuntimeBootDescriptor() == null && !isRuntimeLoaded()) {
                    Set<NBootId> loadedDeps = null;
                    String rid = options.getRuntimeId();
                    Path nutsRuntimeCacheConfigPath = Paths.get(options.getStoreType("CONF") + File.separator + NBootConstants.Folders.ID).resolve(NBootUtils.resolveIdPath(bootApiId)).resolve(NBootConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
                    try {
                        boolean cacheLoaded = false;
                        if (!NBootUtils.firstNonNull(options.getRecover(), false) && !resetFlag && !resetHardFlag && NBootUtils.isFileAccessible(nutsRuntimeCacheConfigPath, options.getExpireTime(), bLog)) {
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
                            loadedDeps = NReservedMavenUtilsBoot.loadDependenciesFromId(NBootId.of(options.getRuntimeId()), bLog, resolveBootRuntimeRepositories(false), cache);
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
                    options.setRuntimeBootDescriptor(new NBootDescriptor().setId(options.getRuntimeId()).setDependencies(loadedDeps.stream().map(NBootId::toDependency).collect(Collectors.toList())));
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
                    options.setBootRepositories(bootRepositories.stream().map(NBootRepositoryLocation::toString).collect(Collectors.joining(";")));
                }

                //resolve extension libraries
                if (options.getExtensionBootDescriptors() == null) {
                    LinkedHashSet<String> excludedExtensions = new LinkedHashSet<>();
                    if (options.getExcludedExtensions() != null) {
                        for (String excludedExtensionGroup : options.getExcludedExtensions()) {
                            for (String excludedExtension : NBootUtils.split(excludedExtensionGroup, ";,", true, true)) {
                                excludedExtensions.add(NBootId.of(excludedExtension).getShortName());
                            }
                        }
                    }
                    if (options.getExtensionsSet() != null) {
                        List<NBootDescriptor> all = new ArrayList<>();
                        for (String extension : options.getExtensionsSet()) {
                            NBootId eid = NBootId.of(extension);
                            if (!excludedExtensions.contains(eid.getShortName()) && !excludedExtensions.contains(eid.getArtifactId())) {
                                Path extensionFile = Paths.get(options.getStoreType("CONF") + File.separator + NBootConstants.Folders.ID).resolve(NBootUtils.resolveIdPath(bootApiId)).resolve(NBootConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
                                Set<NBootId> loadedDeps = null;
                                if (isLoadFromCache() && NBootUtils.isFileAccessible(extensionFile, options.getExpireTime(), bLog)) {
                                    try {
                                        Properties obj = NBootUtils.loadURLProperties(extensionFile, bLog);
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
                        options.setExtensionBootDescriptors(all);
                    } else {
                        options.setExtensionBootDescriptors(new ArrayList<>());
                    }
                }
            }
            newInstanceRequirements = checkRequirements(true);
            if (newInstanceRequirements == 0) {
                options.setJavaCommand(null);
                options.setJavaOptions(null);
            }
            return true;
        }
        return false;
    }

    private boolean isAskConfirm(NBootOptionsInfo o) {
        return NBootUtils.sameEnum(NBootUtils.enumName(NBootUtils.firstNonNull(o.getConfirm(), "ASK")), "ASK");
    }

    private boolean isPlainTrace() {
        return NBootUtils.firstNonNull(options.getTrace(), true)
                && !NBootUtils.firstNonNull(options.getBot(), false)
                && (NBootUtils.sameEnum(options.getOutputFormat(), "PLAIN") || NBootUtils.isBlank(options.getOutputFormat()));
    }

    private boolean isLoadFromCache() {
        return !NBootUtils.firstNonNull(options.getRecover(), false) && !NBootUtils.firstNonNull(options.getReset(), false);
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     *
     * @return NWorkspace instance as object
     */
    @Override
    public NWorkspaceBase getWorkspace() {
        if (loadedWorkspace != null) {
            return loadedWorkspace;
        }
        if (exceptionRunnable != null) {
            exceptionRunnable.run();
            return loadedWorkspace;
        }
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NBootUnsatisfiedRequirementsException(NBootMsg.ofC("unable to open a distinct version : %s from nuts#%s", getRequirementsHelpString(true), NBootWorkspaceImpl.NUTS_BOOT_VERSION));
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (NBootUtils.isEmptyList(options.getApplicationArguments())
                && NBootUtils.firstNonNull(options.getSkipBoot(), false) && (NBootUtils.firstNonNull(options.getRecover(), false) || NBootUtils.firstNonNull(options.getReset(), false))) {
            if (isPlainTrace()) {
                bLog.warn(NBootMsg.ofC("workspace erased : %s", options.getWorkspace()));
            }
            throw new NBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NWorkspaceBase wsInstance = null;
        NBootErrorInfoList errorList = new NBootErrorInfoList();
        try {
            Path configFile = Paths.get(options.getWorkspace()).resolve(NBootConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            if (NBootUtils.sameEnum(options.getOpenMode(), "OPEN_OR_ERROR")) {
                //add fail fast test!!
                if (!Files.isRegularFile(configFile)) {
                    throw new NBootWorkspaceNotFoundException(options.getWorkspace());
                }
            } else if (NBootUtils.sameEnum(options.getOpenMode(), "CREATE_OR_ERROR")) {
                if (Files.exists(configFile)) {
                    throw new NBootWorkspaceAlreadyExistsException(options.getWorkspace());
                }
            }
            if (NBootUtils.isBlank(options.getApiVersion())
                    || NBootUtils.isBlank(options.getRuntimeId())
                    || (!isRuntimeLoaded() && options.getRuntimeBootDescriptor() == null)
                    || options.getExtensionBootDescriptors() == null
//                    || (!runtimeLoaded && (computedOptions.getBootRepositories().isBlank()))
            ) {
                throw new NBootException(NBootMsg.ofPlain("invalid workspace state"));
            }
            boolean recover = NBootUtils.firstNonNull(options.getRecover(), false) || NBootUtils.firstNonNull(options.getReset(), false);

            List<NBootClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = options.getStoreType("LIB") + File.separator + NBootConstants.Folders.ID;

            NBootRepositoryLocation[] repositories = NBootUtils.split(options.getBootRepositories(), "\n;", true, true).stream().map(NBootRepositoryLocation::of).toArray(NBootRepositoryLocation[]::new);

            NBootRepositoryLocation workspaceBootLibFolderRepo = NBootRepositoryLocation.of("nuts@" + workspaceBootLibFolder);
            options.setRuntimeBootDependencyNode(
                    isRuntimeLoaded() ? null :
                            createClassLoaderNode(options.getRuntimeBootDescriptor(), repositories, workspaceBootLibFolderRepo, recover, errorList, true)
            );

            if (options.getExtensionBootDescriptors() != null) {
                for (NBootDescriptor nutsBootDescriptor : options.getExtensionBootDescriptors()) {
                    deps.add(createClassLoaderNode(nutsBootDescriptor, repositories, workspaceBootLibFolderRepo, recover, errorList, false));
                }
            }
            options.setExtensionBootDependencyNodes(deps);
            deps.add(0, options.getRuntimeBootDependencyNode());

            bootClassWorldURLs = NBootUtils.resolveClassWorldURLs(deps.toArray(new NBootClassLoaderNode[0]), getContextClassLoader(), bLog);
            workspaceClassLoader = /*bootClassWorldURLs.length == 0 ? getContextClassLoader() : */ new NBootClassLoader(deps.toArray(new NBootClassLoaderNode[0]), getContextClassLoader());
            options.setClassWorldLoader(workspaceClassLoader);
            if (bLog.isLoggable(Level.CONFIG)) {
                if (bootClassWorldURLs.length == 0) {
                    bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofPlain("empty nuts class world. All dependencies are already loaded in classpath, most likely"));
                } else if (bootClassWorldURLs.length == 1) {
                    bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofC("resolve nuts class world to : %s %s", NBootUtils.getURLDigest(bootClassWorldURLs[0], bLog), bootClassWorldURLs[0]));
                } else {
                    bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofPlain("resolve nuts class world to : "));
                    for (URL u : bootClassWorldURLs) {
                        bLog.log(Level.CONFIG, "SUCCESS", NBootMsg.ofC("    %s : %s", NBootUtils.getURLDigest(u, bLog), u));
                    }
                }
            }
            options.setClassWorldURLs(Arrays.asList(bootClassWorldURLs));
            bLog.log(Level.CONFIG, "INFO", NBootMsg.ofPlain("search for NutsBootWorkspaceFactory service implementations"));
            ServiceLoader<NBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NBootWorkspaceFactory.class, workspaceClassLoader);
            List<NBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new NBootWorkspaceFactoryComparator(options));
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
                    options.setBootWorkspaceFactory(factoryInstance);
                    wsInstance = a.createWorkspace(options);
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    bLog.error(NBootMsg.ofC("unable to create workspace using factory %s", a), ex);
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
                bLog.error(NBootMsg.ofC("unable to load Workspace \"%s\" from ClassPath :", options.getName()));
                for (URL url : bootClassWorldURLs) {
                    bLog.error(NBootMsg.ofC("\t %s", NBootUtils.formatURL(url)));
                }
                if (exceptions.isEmpty()) {
                    bLog.error(NBootMsg.ofC("current classpath does not any Nuts Workspace implementation at %s", options.getWorkspace()));
                }
                for (Throwable exception : exceptions) {
                    bLog.error(NBootMsg.ofC("%s", exception), exception);
                }
                bLog.error(NBootMsg.ofC("unable to load Workspace Component from ClassPath : %s", Arrays.asList(bootClassWorldURLs)));
                throw new NBootInvalidWorkspaceException(this.options.getWorkspace(), NBootMsg.ofC("unable to load Workspace Component from ClassPath : %s%n  caused by:%n\t%s", Arrays.asList(bootClassWorldURLs), exceptions.stream().map(Throwable::toString).collect(Collectors.joining("\n\t"))));
            }
//        } catch (NReadOnlyException | NCancelException | NNoSessionCancelException ex) {
//            throw ex;
        } catch (UnsatisfiedLinkError | AbstractMethodError ex) {
            URL[] finalBootClassWorldURLs = bootClassWorldURLs;
            NBootMsg errorMessage = NBootMsg.ofC("unable to boot nuts workspace because the installed binaries are incompatible with the current nuts bootstrap version %s\nusing '-N' command line flag should fix the problem", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
            errorList.insert(0, new NReservedErrorInfo(null, null, null, errorMessage + ": " + ex, ex));
            exceptionRunnable = () -> {
                logError(finalBootClassWorldURLs, errorList, options);
                throw new NBootException(errorMessage, ex);
            };
            exceptionRunnable.run();
        } catch (Throwable ex) {
            NBootMsg message = NBootMsg.ofPlain("unable to bootstrap nuts workspace");
            if (ex instanceof NBootException) {
                errorList.insert(0, new NReservedErrorInfo(null, null, null, ex.getMessage(), ex));
            } else {
                errorList.insert(0, new NReservedErrorInfo(null, null, null, message + " : " + ex, ex));
            }
            URL[] finalBootClassWorldURLs1 = bootClassWorldURLs;
            exceptionRunnable = () -> {
                logError(finalBootClassWorldURLs1, errorList, options);
                if (ex instanceof NBootException) {
                    throw (NBootException) ex;
                }
                throw new NBootException(message, ex);
            };
            exceptionRunnable.run();
        }
        return loadedWorkspace = wsInstance;
    }

    private ClassLoader getContextClassLoader() {
        Supplier<ClassLoader> classLoaderSupplier = options.getClassLoaderSupplier();
        if (classLoaderSupplier != null) {
            ClassLoader classLoader = classLoaderSupplier.get();
            if (classLoader != null) {
                return classLoader;
            }
        }
        return Thread.currentThread().getContextClassLoader();
    }

    public NBootWorkspace runWorkspace() {
        try {
            if (NBootUtils.firstNonNull(options.getCommandHelp(), false)) {
                NBootWorkspaceHelper.runCommandHelp(options, bLog);
            } else if (NBootUtils.firstNonNull(options.getCommandVersion(), false)) {
                NBootWorkspaceHelper.runCommandVersion(() -> getApiDigestOrInternal(), options, bLog);
            } else {
                if (hasUnsatisfiedRequirements()) {
                    runNewProcess();
                    return this;
                }
                NWorkspaceBase ws = this.getWorkspace();
                ws.runBootCommand();
            }
        } catch (Exception ex) {
            throw doLogException(ex);
        }
        return this;
    }

    private RuntimeException doLogException(Exception ex) {
        NExceptionWithExitCodeBase ec = NBootUtils.findThrowable(ex, NExceptionWithExitCodeBase.class, null);
        int c = ec == null ? 254 : ec.getExitCode();
        if (c != 0) {
            NExceptionBootAware u = NBootUtils.findThrowable(ex, NExceptionBootAware.class, null);
            if (u != null) {
                try {
                    u.processThrowable(options, bLog);
                } catch (Exception any) {
                    NBootUtils.processThrowable(ex, bLog, true, NBootUtils.resolveShowStackTrace(options), NBootUtils.resolveGui(options));
                }
            } else {
                NBootUtils.processThrowable(ex, bLog, true, NBootUtils.resolveShowStackTrace(options), NBootUtils.resolveGui(options));
            }
        }
        if (ec instanceof RuntimeException) {
            return (RuntimeException) ec;
        }
        return new RuntimeException(ex);
    }


//    public NWorkspaceBase runWorkspace() {
//        try {
//            return runWorkspace0();
//        } catch (Exception ex) {
//            NExceptionBootAware u = NBootUtils.findThrowable(ex, NExceptionBootAware.class, null);
//            if (u != null) {
//                u.processThrowable();
//            } else {
//                NBootUtils.processThrowable(ex, bLog, true, NBootUtils.resolveShowStackTrace(options), NBootUtils.resolveGui(options));
//            }
//            throw ex;
//        }
//    }

    private void fallbackInstallActionUnavailable(String message) {
        bLog.error(NBootMsg.ofPlain(message));
    }

    private void logError(URL[] bootClassWorldURLs, NBootErrorInfoList ths, NBootOptionsInfo options) {
        if (options == null) {
            options = this.options;
        }
        if (options == null) {
            options = new NBootOptionsInfo();
        }
        boolean showStackTrace = NBootUtils.resolveShowStackTrace(options);
        boolean showGui = NBootUtils.resolveGui(options);

        String workspace = options.getWorkspace();
        Map<String, String> rbc_locations = options.getStoreLocations();
        if (rbc_locations == null) {
            rbc_locations = new HashMap<>();
        }
        String apiDigestOrInternal = getApiDigestOrInternal();
        if ("<internal>".equals(apiDigestOrInternal)) {
            apiDigestOrInternal = null;
        }
        bLog.setOptions(options);
        if (!showStackTrace) {
            bLog.error(NBootMsg.ofC("unable to bootstrap nuts %s %s", NUTS_BOOT_VERSION, NBootUtils.isBlank(apiDigestOrInternal) ? "" : ("(digest " + apiDigestOrInternal + ")")));
            for (NReservedErrorInfo e : ths.list()) {
                bLog.error(NBootMsg.ofC("%s", e.toString()));
            }
            return;
        }
        bLog.error(NBootMsg.ofC("unable to bootstrap nuts %s %s", NUTS_BOOT_VERSION, NBootUtils.isBlank(apiDigestOrInternal) ? "" : ("(digest " + apiDigestOrInternal + ")")));
        if (!ths.list().isEmpty()) {
            bLog.error(NBootMsg.ofC("%s", ths.list().get(0)));
        }
        bLog.error(NBootMsg.ofPlain("here after current environment info:"));
        bLog.error(NBootMsg.ofC("  nuts-boot-version                : %s", NUTS_BOOT_VERSION));
        bLog.error(NBootMsg.ofC("  nuts-boot-api-version            : %s", NBootUtils.desc(options.getApiVersion())));
        bLog.error(NBootMsg.ofC("  nuts-boot-runtime                : %s", NBootUtils.desc(options.getRuntimeId())));
        bLog.error(NBootMsg.ofC("  nuts-boot-repositories           : %s", NBootUtils.desc(options.getBootRepositories())));
        bLog.error(NBootMsg.ofC("  workspace-location               : %s", NBootUtils.firstNonNull(workspace, "<default-location>")));
        bLog.error(NBootMsg.ofC("  nuts-store-bin                   : %s", NBootUtils.desc(rbc_locations.get("BIN"))));
        bLog.error(NBootMsg.ofC("  nuts-store-conf                  : %s", NBootUtils.desc(rbc_locations.get("CONF"))));
        bLog.error(NBootMsg.ofC("  nuts-store-var                   : %s", NBootUtils.desc(rbc_locations.get("VAR"))));
        bLog.error(NBootMsg.ofC("  nuts-store-log                   : %s", NBootUtils.desc(rbc_locations.get("LOG"))));
        bLog.error(NBootMsg.ofC("  nuts-store-temp                  : %s", NBootUtils.desc(rbc_locations.get("TEMP"))));
        bLog.error(NBootMsg.ofC("  nuts-store-cache                 : %s", NBootUtils.desc(rbc_locations.get("CACHE"))));
        bLog.error(NBootMsg.ofC("  nuts-store-run                   : %s", NBootUtils.desc(rbc_locations.get("RUN"))));
        bLog.error(NBootMsg.ofC("  nuts-store-lib                   : %s", NBootUtils.desc(rbc_locations.get("LIB"))));
        bLog.error(NBootMsg.ofC("  nuts-store-strategy              : %s", NBootUtils.desc(options.getStoreStrategy())));
        bLog.error(NBootMsg.ofC("  nuts-store-layout                : %s", NBootUtils.desc(options.getStoreLayout())));
        bLog.error(NBootMsg.ofC("  nuts-boot-args                   : %s", asCmdLine(options, null)));
        bLog.error(NBootMsg.ofC("  nuts-app-args                    : %s", NBootUtils.nonNullStrList(options.getApplicationArguments())));
        bLog.error(NBootMsg.ofC("  option-read-only                 : %s", NBootUtils.firstNonNull(options.getReadOnly(), false)));
        bLog.error(NBootMsg.ofC("  option-trace                     : %s", NBootUtils.firstNonNull(options.getTrace(), false)));
        bLog.error(NBootMsg.ofC("  option-progress                  : %s", NBootUtils.desc(options.getProgressOptions())));
        bLog.error(NBootMsg.ofC("  option-open-mode                 : %s", NBootUtils.desc(NBootUtils.firstNonNull(options.getOpenMode(), "OPEN_OR_CREATE"))));

        NBootClassLoaderNode rtn = options.getRuntimeBootDependencyNode();
        String rtHash = "";
        if (rtn != null) {
            rtHash = NBootUtils.getURLDigest(rtn.getURL(), bLog);
        }
        bLog.error(NBootMsg.ofC("  nuts-runtime-digest              : %s", NBootUtils.desc(rtHash)));

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            bLog.error(NBootMsg.ofC("  nuts-runtime-classpath           : %s", "<undefined>"));
        } else {
            bLog.error(NBootMsg.ofC("  nuts-runtime-hash                : %s", "<undefined>"));
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    bLog.error(NBootMsg.ofC("  nuts-runtime-classpath           : %s", NBootUtils.formatURL(bootClassWorldURL)));
                } else {
                    bLog.error(NBootMsg.ofC("                                     %s", NBootUtils.formatURL(bootClassWorldURL)));
                }
            }
        }
        bLog.error(NBootMsg.ofC("  java-version                     : %s", System.getProperty("java.version")));
        bLog.error(NBootMsg.ofC("  java-executable                  : %s", NBootUtils.desc(NBootUtils.resolveJavaCommand(null))));
        bLog.error(NBootMsg.ofC("  java-class-path                  : %s", System.getProperty("java.class.path")));
        bLog.error(NBootMsg.ofC("  java-library-path                : %s", System.getProperty("java.library.path")));
        bLog.error(NBootMsg.ofC("  os-name                          : %s", System.getProperty("os.name")));
        bLog.error(NBootMsg.ofC("  os-arch                          : %s", System.getProperty("os.arch")));
        bLog.error(NBootMsg.ofC("  os-version                       : %s", System.getProperty("os.version")));
        bLog.error(NBootMsg.ofC("  user-name                        : %s", System.getProperty("user.name")));
        bLog.error(NBootMsg.ofC("  user-home                        : %s", System.getProperty("user.home")));
        bLog.error(NBootMsg.ofC("  user-dir                         : %s", System.getProperty("user.dir")));
        bLog.error(NBootMsg.ofPlain(""));
        NBootLogConfig logConfig = options.getLogConfig();
        if (logConfig == null || logConfig.getLogTermLevel() == null || (logConfig.getLogFileLevel() != null && logConfig.getLogFileLevel().intValue() > Level.FINEST.intValue())) {
            bLog.error(NBootMsg.ofPlain("If the problem persists you may want to get more debug info by adding '--verbose' arguments."));
        }
        if (!NBootUtils.firstNonNull(options.getReset(), false) && !NBootUtils.firstNonNull(options.getRecover(), false) && options.getExpireTime() == null) {
            bLog.error(NBootMsg.ofPlain("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments."));
            bLog.error(NBootMsg.ofPlain("Here is the proper command : "));
            bLog.error(NBootMsg.ofPlain("  java -jar nuts.jar --verbose --recover --expire [...]"));
        } else if (!NBootUtils.firstNonNull(options.getReset(), false) && NBootUtils.firstNonNull(options.getRecover(), false) && options.getExpireTime() == null) {
            bLog.error(NBootMsg.ofPlain("You may also enable full reset mode to ignore existing configuration with '--reset' argument."));
            bLog.error(NBootMsg.ofPlain("ATTENTION: this will delete all your nuts configuration. Use it at your own risk."));
            bLog.error(NBootMsg.ofPlain("Here is the proper command : "));
            bLog.error(NBootMsg.ofPlain("  java -jar nuts.jar --verbose --reset [...]"));
        }
        if (!ths.list().isEmpty()) {
            bLog.error(NBootMsg.ofPlain("error stack trace is:"));
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
                bLog.error(NBootMsg.ofC(msg.toString(), msgParams.toArray()));
                bLog.error(NBootMsg.ofPlain(th.toString()), th.getThrowable());
            }
        } else {
            bLog.error(NBootMsg.ofPlain("no stack trace is available."));
        }
        bLog.error(NBootMsg.ofPlain("now exiting nuts, Bye!"));
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NBootUtils.isBlank(options.getApiVersion())) {
            if (!unsatisfiedOnly || !options.getApiVersion().equals(NBootWorkspaceImpl.NUTS_BOOT_VERSION)) {
                req += 1;
            }
        }
        if (!unsatisfiedOnly || !NBootUtils.isActualJavaCommand(options.getJavaCommand())) {
            req += 2;
        }
        if (!unsatisfiedOnly || !NBootUtils.isActualJavaOptions(options.getJavaOptions())) {
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
            sb.append("nuts version ").append(NBootId.ofApi(options.getApiVersion()));
        }
        if ((req & 2) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java command ").append(options.getJavaCommand());
        }
        if ((req & 4) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java options ").append(options.getJavaOptions());
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
        File file = NReservedMavenUtilsBoot.getBootCacheJar(NBootId.of(options.getRuntimeId()), repositories, workspaceBootLibFolder, !recover, name, options.getExpireTime(), errorList, options, pathExpansionConverter, bLog, cache);
        rt.setId(id.toString());
        rt.setUrl(file.toURI().toURL());
        rt.setIncludedInClasspath(NBootUtils.isLoadedClassPath(rt.getURL(), getContextClassLoader(), bLog));

        if (bLog.isLoggable(Level.CONFIG)) {
            String rtHash = "";
            if (options.getRuntimeId() != null) {
                rtHash = NBootUtils.getFileOrDirectoryDigest(file.toPath());
                if (rtHash == null) {
                    rtHash = "";
                }
            }
            bLog.log(Level.CONFIG, "INFO", NBootMsg.ofC("detect %s version %s - digest %s from %s", name, id.toString(), rtHash, file));
        }

        for (NBootDependency s : deps) {
            NBootClassLoaderNodeBuilder x = new NBootClassLoaderNodeBuilder();
            if (NBootUtils.isAcceptDependency(s, options)) {
                x.setId(s.toString()).setUrl(NReservedMavenUtilsBoot.getBootCacheJar(s.toId(), repositories, workspaceBootLibFolder, !recover, name + " dependency", options.getExpireTime(), errorList, options, pathExpansionConverter, bLog, cache).toURI().toURL());
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
