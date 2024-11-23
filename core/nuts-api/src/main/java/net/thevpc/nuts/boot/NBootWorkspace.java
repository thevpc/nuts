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

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.*;
import net.thevpc.nuts.reserved.boot.NReservedBootClassLoader;
import net.thevpc.nuts.reserved.boot.NReservedBootConfigLoader;
import net.thevpc.nuts.reserved.boot.NReservedBootLog;
import net.thevpc.nuts.reserved.boot.NReservedBootWorkspaceFactoryComparator;
import net.thevpc.nuts.reserved.exception.NReservedErrorInfo;
import net.thevpc.nuts.reserved.exception.NReservedErrorInfoList;
import net.thevpc.nuts.reserved.io.NReservedIOUtils;
import net.thevpc.nuts.reserved.parser.NReservedJsonParser;
import net.thevpc.nuts.reserved.io.NReservedPath;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

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

    private final Instant creationTime = Instant.now();
    private final NWorkspaceOptions userOptions;
    private final NReservedBootLog bLog;
    private final NBootOptionsBuilder computedOptions = new DefaultNBootOptionsBuilder();
    private final NRepositoryDB repositoryDB;
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "workspace":
                    return computedOptions.getWorkspace().orNull();
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
                    return NReservedUtils.getHome(NStoreType.valueOf(from.substring("home.".length()).toUpperCase()), computedOptions);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<NStoreType, String> s = computedOptions.getStoreLocations().orElse(Collections.emptyMap());
                    String v = s.get(NStoreType.parse(from).orNull());
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
    private NBootOptionsBuilder lastWorkspaceOptions;
    //private Set<NRepositoryLocation> parsedBootRuntimeDependenciesRepositories;
    private Set<NRepositoryLocation> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private Scanner scanner;
    private NBootCache cache = new NBootCache();
    Boolean runtimeLoaded;
    NId runtimeLoadedId;

    public NBootWorkspace(NWorkspaceTerminalOptions bootTerminal, String... args) {
        this.bLog = new NReservedBootLog(bootTerminal);
        NWorkspaceOptionsBuilder userOptions = new DefaultNWorkspaceOptionsBuilder();
        if (bootTerminal != null) {
            userOptions.setStdin(bootTerminal.getIn());
            userOptions.setStdout(bootTerminal.getOut());
            userOptions.setStderr(bootTerminal.getErr());
        }
        InputStream in = userOptions.getStdin().orNull();
        scanner = new Scanner(in == null ? System.in : in);
        userOptions.setCmdLine(args);
        if (userOptions.getSkipErrors().orElse(false)) {
            StringBuilder errorMessage = new StringBuilder();
            for (NMsg s : userOptions.getErrors().orElseGet(Collections::emptyList)) {
                errorMessage.append(s).append("\n");
            }
            errorMessage.append("Try 'nuts --help' for more information.");
            bLog.log(Level.WARNING, NLogVerb.WARNING, NMsg.ofC("Error : %s", errorMessage));
        }
        this.userOptions = userOptions.readOnly();
        repositoryDB = NRepositoryDB.ofDefault();
        this.postInit();
    }

    public NBootWorkspace(NWorkspaceOptions userOptions) {
        if (userOptions == null) {
            userOptions = DefaultNWorkspaceOptions.BLANK;
        }
        this.bLog = new NReservedBootLog(new NWorkspaceTerminalOptions(userOptions.getStdin().orNull(), userOptions.getStdout().orNull(), userOptions.getStderr().orNull()));
        this.userOptions = userOptions.readOnly();
        repositoryDB = NRepositoryDB.ofDefault();
        this.postInit();
    }

    private void postInit() {
        this.computedOptions.setAll(userOptions);
        this.computedOptions.setUserOptions(this.userOptions);
        this.computedOptions.setIsolationLevel(this.computedOptions.getIsolationLevel().orElse(NIsolationLevel.SYSTEM));
        this.computedOptions.setExecutionType(this.computedOptions.getExecutionType().orElse(NExecutionType.SPAWN));
        this.computedOptions.setConfirm(this.computedOptions.getConfirm().orElse(NConfirmationMode.ASK));
        this.computedOptions.setFetchStrategy(this.computedOptions.getFetchStrategy().orElse(NFetchStrategy.ONLINE));
        this.computedOptions.setOpenMode(this.computedOptions.getOpenMode().orElse(NOpenMode.OPEN_OR_CREATE));
        this.computedOptions.setRunAs(this.computedOptions.getRunAs().orElse(NRunAs.CURRENT_USER));
        this.computedOptions.setLogConfig(this.computedOptions.getLogConfig().orElseGet(NLogConfig::new));
        this.computedOptions.setStdin(this.computedOptions.getStdin().orElse(System.in));
        this.computedOptions.setStdout(this.computedOptions.getStdout().orElse(System.out));
        this.computedOptions.setStderr(this.computedOptions.getStderr().orElse(System.err));
        this.computedOptions.setLocale(this.computedOptions.getLocale().orElse(Locale.getDefault().toString()));
        this.computedOptions.setOutputFormat(this.computedOptions.getOutputFormat().orElse(NContentType.PLAIN));
        this.computedOptions.setCreationTime(this.computedOptions.getCreationTime().orElse(creationTime));
        if (this.computedOptions.getApplicationArguments().isEmpty()) {
            this.computedOptions.setApplicationArguments(new ArrayList<>());
        }
        this.bLog.setOptions(this.computedOptions);
    }

    private static void revalidateLocations(NBootOptionsBuilder bootOptions, String workspaceName, boolean immediateLocation, NIsolationLevel sandboxMode) {
        if (NBlankable.isBlank(bootOptions.getName())) {
            bootOptions.setName(workspaceName);
        }
        boolean system = bootOptions.getSystem().orElse(false);
        if (sandboxMode.compareTo(NIsolationLevel.SANDBOX) >= 0) {
            bootOptions.setStoreStrategy(NStoreStrategy.STANDALONE);
            bootOptions.setRepositoryStoreStrategy(NStoreStrategy.EXPLODED);
            system = false;
        } else {
            if (bootOptions.getStoreStrategy().isNotPresent()) {
                bootOptions.setStoreStrategy(immediateLocation ? NStoreStrategy.EXPLODED : NStoreStrategy.STANDALONE);
            }
            if (bootOptions.getRepositoryStoreStrategy().isNotPresent()) {
                bootOptions.setRepositoryStoreStrategy(NStoreStrategy.EXPLODED);
            }
        }
        Map<NStoreType, String> storeLocations =
                NPlatformHome.of(bootOptions.getStoreLayout().orNull(), system)
                        .buildLocations(bootOptions.getStoreStrategy().orNull(), bootOptions.getStoreLocations().orNull(), bootOptions.getHomeLocations().orNull(), bootOptions.getWorkspace().orNull() //no session!
                        );
        if (new HashSet<>(storeLocations.values()).size() != storeLocations.size()) {
            Map<String, List<NStoreType>> conflicts = new LinkedHashMap<>();
            for (Map.Entry<NStoreType, String> e : storeLocations.entrySet()) {
                conflicts.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
            }
            StringBuilder error = new StringBuilder();
            error.append("invalid store locations. Two or more stores point to the same location:");
            List<Object> errorParams = new ArrayList<>();
            for (Map.Entry<String, List<NStoreType>> e : conflicts.entrySet()) {
                List<NStoreType> ev = e.getValue();
                if (ev.size() > 1) {
                    String ek = e.getKey();
                    error.append("\n");
                    error.append("all of (").append(ev.stream().map(x -> "%s").collect(Collectors.joining(","))).append(") point to %s");
                    errorParams.addAll(ev);
                    errorParams.add(ek);
                }
            }
            throw new NBootException(NMsg.ofC(error.toString(), errorParams));
        }
        bootOptions.setStoreLocations(storeLocations);
    }

    private static final class ApiDigestHolder {
        static final String apiDigest = NApiUtilsRPI.resolveNutsIdDigest();
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
//                nLog.with().level(Level.FINE).verb(NLogVerb.START).log(NMsg.ofC("start new process : %s", NCmdLine.of(processCmdLine)));
//            } else {
                bLog.log(Level.FINE, NLogVerb.START, NMsg.ofC("start new process : %s", NCmdLine.of(processCmdLine)));
//            }
            result = new ProcessBuilder(processCmdLine).inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ex) {
            throw new NBootException(NMsg.ofPlain("failed to run new nuts process"), ex);
        }
        if (result != 0) {
            throw new NBootException(NMsg.ofC("failed to exec new process. returned %s", result));
        }
    }

    /**
     * repositories used to locale nuts-runtime artifact or its dependencies
     *
     * @param dependencies when true search for runtime dependencies, when
     *                     false, search for runtime
     * @return repositories
     */
    public Set<NRepositoryLocation> resolveBootRuntimeRepositories(boolean dependencies) {
//        if (dependencies) {
//            if (parsedBootRuntimeDependenciesRepositories != null) {
//                return parsedBootRuntimeDependenciesRepositories;
//            }
//            bLog.log(Level.FINE, NLogVerb.START, NMsg.ofC("resolve boot repositories to load nuts-runtime dependencies from options : %s and config: %s", computedOptions.getRepositories().orElseGet(Collections::emptyList).toString(), computedOptions.getBootRepositories().ifBlankEmpty().orElse("[]")));
//        } else {
        if (parsedBootRuntimeRepositories != null) {
            return parsedBootRuntimeRepositories;
        }
        bLog.log(Level.FINE, NLogVerb.START, NMsg.ofC("resolve boot repositories to load nuts-runtime from options : %s and config: %s", computedOptions.getRepositories().orElseGet(Collections::emptyList).toString(), computedOptions.getBootRepositories().ifBlankEmpty().orElse("[]")));
//        }
        NRepositorySelectorList bootRepositoriesSelector = NRepositorySelectorList.of(computedOptions.getRepositories().orNull(), repositoryDB).get();
        NRepositorySelector[] old = NRepositorySelectorList.of(Arrays.asList(computedOptions.getBootRepositories().orNull()), repositoryDB).get().toArray();
        NRepositoryLocation[] result;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositoriesSelector.resolve(new NRepositoryLocation[]{
                    Boolean.getBoolean("nomaven") ? null : new NRepositoryLocation("maven", "maven", "maven")
            }, repositoryDB);
        } else {
            result = bootRepositoriesSelector.resolve(Arrays.stream(old).map(x -> NRepositoryLocation.of(x.getName(), x.getUrl())).toArray(NRepositoryLocation[]::new), repositoryDB);
        }
        result = Arrays.stream(result).map(
                r -> {
                    if (NBlankable.isBlank(r.getLocationType()) || NBlankable.isBlank(r.getName())) {
                        boolean fileExists = false;
                        if (r.getPath() != null) {
                            NReservedPath r1 = new NReservedPath(r.getPath()).toAbsolute();
                            if (!r.getPath().equals(r1.getPath())) {
                                r = r.setPath(r1.getPath());
                            }
                            NReservedPath r2 = r1.resolve(".nuts-repository");
                            NReservedJsonParser parser = null;
                            try {
                                byte[] bytes = r2.readAllBytes(bLog);
                                if (bytes != null) {
                                    fileExists = true;
                                    parser = new NReservedJsonParser(new InputStreamReader(new ByteArrayInputStream(bytes)));
                                    Map<String, Object> jsonObject = parser.parseObject();
                                    if (NBlankable.isBlank(r.getLocationType())) {
                                        Object o = jsonObject.get("repositoryType");
                                        if (o instanceof String && !NBlankable.isBlank(o)) {
                                            r = r.setLocationType(String.valueOf(o));
                                        }
                                    }
                                    if (NBlankable.isBlank(r.getName())) {
                                        Object o = jsonObject.get("repositoryName");
                                        if (o instanceof String && !NBlankable.isBlank(o)) {
                                            r = r.setName(String.valueOf(o));
                                        }
                                    }
                                    if (NBlankable.isBlank(r.getName())) {
                                        r = r.setName(r.getName());
                                    }
                                }
                            } catch (Exception e) {
                                bLog.log(Level.CONFIG, NLogVerb.WARNING, NMsg.ofC("unable to load %s", r2));
                            }
                        }
                        if (fileExists) {
                            if (NBlankable.isBlank(r.getLocationType())) {
                                r = r.setLocationType(NConstants.RepoTypes.NUTS);
                            }
                        }
                    }
                    return r;
                }
        ).toArray(NRepositoryLocation[]::new);
        Set<NRepositoryLocation> rr = Arrays.stream(result).collect(Collectors.toCollection(LinkedHashSet::new));
//        if (dependencies) {
//            parsedBootRuntimeDependenciesRepositories = rr;
//        } else {
        parsedBootRuntimeRepositories = rr;
//        }
        return rr;
    }

    public String[] createProcessCmdLine() {
        prepareWorkspace();
        bLog.log(Level.FINE, NLogVerb.START, NMsg.ofC("running version %s.  %s", computedOptions.getApiVersion().orNull(), getRequirementsHelpString(true)));
        String defaultWorkspaceLibFolder = computedOptions.getStoreType(NStoreType.LIB).orNull();
        List<NRepositoryLocation> repos = new ArrayList<>();
        repos.add(NRepositoryLocation.of("nuts@" + defaultWorkspaceLibFolder));
        Collection<NRepositoryLocation> bootRepositories = resolveBootRuntimeRepositories(true);
        repos.addAll(bootRepositories);
        NReservedErrorInfoList errorList = new NReservedErrorInfoList();
        File file = NReservedMavenUtils.resolveOrDownloadJar(NId.ofApi(computedOptions.getApiVersion().orNull()).get(), repos.toArray(new NRepositoryLocation[0]), NRepositoryLocation.of("nuts@" + computedOptions.getStoreType(NStoreType.LIB).get() + File.separator + NConstants.Folders.ID), bLog, false, computedOptions.getExpireTime().orNull(), errorList);
        if (file == null) {
            errorList.insert(0, new NReservedErrorInfo(null, null, null, "unable to load nuts " + computedOptions.getApiVersion().orNull(), null));
            logError(null, errorList);
            throw new NBootException(NMsg.ofC("unable to load %s#%s", NConstants.Ids.NUTS_API, computedOptions.getApiVersion().orNull()));
        }

        List<String> cmd = new ArrayList<>();
        String jc = computedOptions.getJavaCommand().orNull();
        if (jc == null || jc.trim().isEmpty()) {
            jc = NReservedUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : NCmdLine.parseDefault(computedOptions.getJavaOptions().orNull()).get().toStringArray()) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        if (computedOptions.getJavaOptions().isNotPresent()) {
            Collections.addAll(cmd, NCmdLine.parseDefault(computedOptions.getJavaOptions().orNull()).get().toStringArray());
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(computedOptions.toCmdLine(new NWorkspaceOptionsConfig().setCompact(true).setApiVersion(computedOptions.getApiVersion().orNull())).toStringList());
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            bLog.log(Level.FINE, NLogVerb.START, NMsg.ofC("[exec] %s", sb));
        }
        return cmd.toArray(new String[0]);
    }

    public NBootOptionsBuilder getOptions() {
        return computedOptions;
    }


    private NIdCache getFallbackCache(NId baseId, boolean lastWorkspace, boolean copyTemp) {
        NIdCache old = cache.fallbackIdMap.get(baseId);
        if (old != null) {
            return old;
        }

        NIdCache fid = new NIdCache();
        fid.baseId = baseId;
        cache.fallbackIdMap.put(fid.baseId, fid);
        String s = (lastWorkspace ? lastWorkspaceOptions : computedOptions).getStoreLocations().get().get(NStoreType.LIB) + "/id/"
                + NIdUtils.resolveIdPath(baseId.getShortId());
        //
        Path ss = Paths.get(s);
        NId bestId = null;
        NVersion bestVersion = null;
        Path bestPath = null;

        if (Files.isDirectory(ss)) {
            try (Stream<Path> stream = Files.list(ss)) {
                for (Path path : stream.collect(Collectors.toList())) {
                    NVersion version = NVersion.of(path.getFileName().toString()).orNull();
                    if (version != null) {
                        if (Files.isDirectory(path)) {
                            NId rId = baseId.builder().setVersion(version).build();
                            Path jar = ss.resolve(version.toString()).resolve(NIdUtils.resolveFileName(
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
            Path descNutsPath = bestPath.resolveSibling(NIdUtils.resolveFileName(bestId, "nuts"));
            Set<NId> dependencies = NReservedMavenUtils.loadDependenciesFromNutsUrl(descNutsPath.toString(), bLog);
            if (dependencies != null) {
                fid.deps = dependencies.stream()
                        .filter(x -> NReservedUtils.isAcceptDependency(x.toDependency(), computedOptions))
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
                    throw new NBootException(NMsg.ofPlain("error storing nuts-runtime.jar"), e);
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
                runtimeLoadedId = (NId) c.getField("RUNTIME_ID").get(null);
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
            NIsolationLevel isolationMode = computedOptions.getIsolationLevel().orElse(NIsolationLevel.SYSTEM);
            if (bLog.isLoggable(Level.CONFIG)) {
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("bootstrap Nuts version %s %s digest %s...", Nuts.getVersion(), isolationMode == NIsolationLevel.SYSTEM ? "" : isolationMode == NIsolationLevel.USER ? " (user mode)" : isolationMode == NIsolationLevel.CONFINED ? " (confined mode)" : isolationMode == NIsolationLevel.SANDBOX ? " (sandbox mode)" : " (unsupported mode)", getApiDigestOrInternal()));
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofPlain("boot-class-path:"));
                for (String s : NStringUtils.split(System.getProperty("java.class.path"), File.pathSeparator, true, true)) {
                    bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("                  %s", s));
                }
                ClassLoader thisClassClassLoader = getClass().getClassLoader();
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("class-loader: %s", thisClassClassLoader));
                for (URL url : NReservedLangUtils.resolveClasspathURLs(thisClassClassLoader, false)) {
                    bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("                 %s", url));
                }
                ClassLoader tctxloader = Thread.currentThread().getContextClassLoader();
                if (tctxloader != null && tctxloader != thisClassClassLoader) {
                    bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("thread-class-loader: %s", tctxloader));
                    for (URL url : NReservedLangUtils.resolveClasspathURLs(tctxloader, false)) {
                        bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("                 %s", url));
                    }
                }
                ClassLoader contextClassLoader = getContextClassLoader();
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("ctx-class-loader: %s", contextClassLoader));
                if (contextClassLoader != null && contextClassLoader != thisClassClassLoader) {
                    for (URL url : NReservedLangUtils.resolveClasspathURLs(contextClassLoader, false)) {
                        bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("                 %s", url));
                    }
                }
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofPlain("system-properties:"));
                Map<String, String> m = (Map) System.getProperties();
                int max = m.keySet().stream().mapToInt(String::length).max().getAsInt();
                for (String k : new TreeSet<String>(m.keySet())) {
                    bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofC("    %s = %s", NStringUtils.formatAlign(k, max, NPositionType.FIRST), NStringUtils.formatStringLiteral(m.get(k), NQuoteType.DOUBLE)));
                }
            }
            String workspaceName = null;
            NBootOptionsBuilder lastConfigLoaded = null;
            String lastNutsWorkspaceJsonConfigPath = null;
            boolean immediateLocation = false;
            boolean resetFlag = computedOptions.getReset().orElse(false);
            boolean dryFlag = computedOptions.getDry().orElse(false);
            String _ws = computedOptions.getWorkspace().orNull();
            if (isolationMode == NIsolationLevel.SANDBOX) {
                Path t = null;
                try {
                    t = Files.createTempDirectory("nuts-sandbox-" + Instant.now().toString().replace(':', '-'));
                } catch (IOException e) {
                    throw new NBootException(NMsg.ofNtf("unable to create temporary/sandbox folder"), e);
                }
                lastNutsWorkspaceJsonConfigPath = t.toString();
                immediateLocation = true;
                workspaceName = t.getFileName().toString();
                resetFlag = false; //no need for reset
                if (computedOptions.getSystem().orElse(false)) {
                    throw new NBootException(NMsg.ofNtf("you cannot specify option '--global' in sandbox mode"));
                }
                if (computedOptions.getWorkspace().ifBlankEmpty().isPresent()) {
                    throw new NBootException(NMsg.ofNtf("you cannot specify '--workspace' in sandbox mode"));
                }
                if (computedOptions.getStoreStrategy().orElse(NStoreStrategy.STANDALONE) != NStoreStrategy.STANDALONE) {
                    throw new NBootException(NMsg.ofNtf("you cannot specify '--exploded' in sandbox mode"));
                }
                if (computedOptions.getSystem().orElse(false)) {
                    throw new NBootException(NMsg.ofNtf("you cannot specify '--global' in sandbox mode"));
                }
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            } else {
                if (isolationMode.compareTo(NIsolationLevel.SYSTEM) > 0 && userOptions.getSystem().orElse(false)) {
                    if (userOptions.getReset().orElse(false)) {
                        throw new NBootException(NMsg.ofC("invalid option 'global' in %s mode", isolationMode));
                    }
                }
                if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                    //this is a protocol based workspace
                    //String protocol=ws.substring(0,ws.indexOf("://"));
                    workspaceName = "remote-bootstrap";
                    lastNutsWorkspaceJsonConfigPath = NPlatformHome.of(null, computedOptions.getSystem().orElse(false)).getWorkspaceLocation(NReservedUtils.resolveValidWorkspaceName(workspaceName));
                    lastConfigLoaded = NReservedBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                    immediateLocation = true;

                } else {
                    immediateLocation = NReservedUtils.isValidWorkspaceName(_ws);
                    int maxDepth = 36;
                    for (int i = 0; i < maxDepth; i++) {
                        lastNutsWorkspaceJsonConfigPath = NReservedUtils.isValidWorkspaceName(_ws) ? NPlatformHome.of(null, computedOptions.getSystem().orElse(false)).getWorkspaceLocation(NReservedUtils.resolveValidWorkspaceName(_ws)) : NReservedIOUtils.getAbsolutePath(_ws);

                        NBootOptionsBuilder configLoaded = NReservedBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                        if (configLoaded == null) {
                            //not loaded
                            break;
                        }
                        if (NBlankable.isBlank(configLoaded.getWorkspace())) {
                            lastConfigLoaded = configLoaded;
                            break;
                        }
                        _ws = configLoaded.getWorkspace().orNull();
                        if (i >= maxDepth - 1) {
                            throw new NBootException(NMsg.ofPlain("cyclic workspace resolution"));
                        }
                    }
                    workspaceName = NReservedUtils.resolveValidWorkspaceName(computedOptions.getWorkspace().orNull());
                }
            }
            computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            if (lastConfigLoaded != null) {
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                computedOptions.setName(lastConfigLoaded.getName().orNull());
                computedOptions.setUuid(lastConfigLoaded.getUuid().orNull());
                NBootOptionsBuilder curr;
                if (!resetFlag) {
                    curr = computedOptions;
                } else {
                    lastWorkspaceOptions = new DefaultNBootOptionsBuilder();
                    curr = lastWorkspaceOptions;
                    curr.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                    curr.setName(lastConfigLoaded.getName().orNull());
                    curr.setUuid(lastConfigLoaded.getUuid().orNull());
                }
                curr.setBootRepositories(lastConfigLoaded.getBootRepositories().orNull());
                curr.setJavaCommand(lastConfigLoaded.getJavaCommand().orNull());
                curr.setJavaOptions(lastConfigLoaded.getJavaOptions().orNull());
                curr.setExtensionsSet(NReservedLangUtils.nonNullSet(lastConfigLoaded.getExtensionsSet().orNull()));
                curr.setStoreStrategy(lastConfigLoaded.getStoreStrategy().orNull());
                curr.setRepositoryStoreStrategy(lastConfigLoaded.getRepositoryStoreStrategy().orNull());
                curr.setStoreLayout(lastConfigLoaded.getStoreLayout().orNull());
                curr.setStoreLocations(NReservedLangUtils.nonNullMap(lastConfigLoaded.getStoreLocations().orNull()));
                curr.setHomeLocations(NReservedLangUtils.nonNullMap(lastConfigLoaded.getHomeLocations().orNull()));
            }
            revalidateLocations(computedOptions, workspaceName, immediateLocation, isolationMode);
            long countDeleted = 0;
            //now that config is prepared proceed to any cleanup
            if (resetFlag) {
                //force loading version early, it will be used later-on
                Nuts.getVersion();
                if (lastWorkspaceOptions != null) {
                    revalidateLocations(lastWorkspaceOptions, workspaceName, immediateLocation, isolationMode);
                    if (dryFlag) {
                        bLog.log(Level.INFO, NLogVerb.DEBUG, NMsg.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, NLogVerb.WARNING, NMsg.ofPlain("reset workspace"));
                        getFallbackCache(NId.RUNTIME_ID, true, true);
                        countDeleted = NReservedIOUtils.deleteStoreLocations(lastWorkspaceOptions, getOptions(), true, bLog, NStoreType.values(), () -> scanner.nextLine());
                        NReservedUtils.ndiUndo(bLog);
                    }
                } else {
                    if (dryFlag) {
                        bLog.log(Level.INFO, NLogVerb.DEBUG, NMsg.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, NLogVerb.WARNING, NMsg.ofPlain("reset workspace"));
                        getFallbackCache(NId.RUNTIME_ID, false, true);
                        countDeleted = NReservedIOUtils.deleteStoreLocations(computedOptions, getOptions(), true, bLog, NStoreType.values(), () -> scanner.nextLine());
                        NReservedUtils.ndiUndo(bLog);
                    }
                }
            } else if (computedOptions.getRecover().orElse(false)) {
                if (dryFlag) {
                    bLog.log(Level.INFO, NLogVerb.DEBUG, NMsg.ofPlain("[dry] [recover] delete CACHE/TEMP workspace folders"));
                } else {
                    bLog.log(Level.CONFIG, NLogVerb.WARNING, NMsg.ofPlain("recover workspace."));
                    List<Object> folders = new ArrayList<>();
                    folders.add(NStoreType.CACHE);
                    folders.add(NStoreType.TEMP);
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = NReservedIOUtils.getStoreLocationPath(computedOptions, NStoreType.LIB);
                    if (p != null) {
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts"));
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = NReservedIOUtils.deleteStoreLocations(computedOptions, getOptions(), false, bLog, folders.toArray(), () -> scanner.nextLine());
                }
            }
            if (computedOptions.getExtensionsSet().isNotPresent()) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setExtensionsSet(lastWorkspaceOptions.getExtensionsSet().orElse(Collections.emptySet()));
                } else {
                    computedOptions.setExtensionsSet(Collections.emptySet());
                }
            }
            if (computedOptions.getHomeLocations().isNotPresent()) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setHomeLocations(lastWorkspaceOptions.getHomeLocations().orElse(Collections.emptyMap()));
                } else {
                    computedOptions.setHomeLocations(Collections.emptyMap());
                }
            }
            if (computedOptions.getStoreLayout().isNotPresent()) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setStoreLayout(lastWorkspaceOptions.getStoreLayout().orElse(NOsFamily.getCurrent()));
                } else {
                    computedOptions.setHomeLocations(Collections.emptyMap());
                }
            }

            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (computedOptions.getApplicationArguments().get().size() == 0 && computedOptions.getSkipBoot().orElse(false) && (computedOptions.getRecover().orElse(false) || resetFlag)) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        bLog.log(Level.WARNING, NLogVerb.WARNING, NMsg.ofC("workspace erased : %s", computedOptions.getWorkspace()));
                    } else {
                        bLog.log(Level.WARNING, NLogVerb.WARNING, NMsg.ofC("workspace is not erased because it does not exist : %s", computedOptions.getWorkspace()));
                    }
                }
                throw new NBootException(NMsg.ofPlain(""), 0);
            }
            //after eventual clean up
            if (computedOptions.getInherited().orElse(false)) {
                //when Inherited, always use the current Api version!
                computedOptions.setApiVersion(Nuts.getVersion());
            } else {
                NVersion nutsVersion = computedOptions.getApiVersion().orElse(NVersion.BLANK);
                if (nutsVersion.isLatestVersion() || nutsVersion.isReleaseVersion()) {
                    NId s = NReservedMavenUtils.resolveLatestMavenId(NId.ofApi("").get(), null, bLog, resolveBootRuntimeRepositories(true), computedOptions);
                    if (s == null) {
                        throw new NBootException(NMsg.ofPlain("unable to load latest nuts version"));
                    }
                    computedOptions.setApiVersion(s.getVersion());
                }
                if (nutsVersion.isBlank()) {
                    computedOptions.setApiVersion(Nuts.getVersion());
                }
            }

            NId bootApiId = NId.ofApi(computedOptions.getApiVersion().orNull()).get();
            Path nutsApiConfigBootPath = Paths.get(computedOptions.getStoreType(NStoreType.CONF).get() + File.separator + NConstants.Folders.ID).resolve(NIdUtils.resolveIdPath(bootApiId)).resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && NReservedIOUtils.isFileAccessible(nutsApiConfigBootPath, computedOptions.getExpireTime().orNull(), bLog)) {
                try {
                    Map<String, Object> obj = NReservedJsonParser.parse(nutsApiConfigBootPath);
                    if (!obj.isEmpty()) {
                        bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofC("loaded %s file : %s", nutsApiConfigBootPath.getFileName(), nutsApiConfigBootPath.toString()));
                        loadedApiConfig = true;
                        if (computedOptions.getRuntimeId().isNotPresent()) {
                            String runtimeId = (String) obj.get("runtimeId");
                            if (NBlankable.isBlank(runtimeId)) {
                                bLog.log(Level.CONFIG, NLogVerb.FAIL, NMsg.ofC("%s does not contain runtime-id", nutsApiConfigBootPath));
                            }
                            computedOptions.setRuntimeId(NId.of(runtimeId).get());
                        }
                        if (computedOptions.getJavaCommand().isNotPresent()) {
                            computedOptions.setJavaCommand((String) obj.get("javaCommand"));
                        }
                        if (computedOptions.getJavaOptions().isNotPresent()) {
                            computedOptions.setJavaOptions((String) obj.get("javaOptions"));
                        }
                    }
                } catch (UncheckedIOException | NIOException e) {
                    bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofC("unable to read %s", nutsApiConfigBootPath));
                }
            }
            if (!loadedApiConfig || computedOptions.getRuntimeId().isNotPresent() || computedOptions.getRuntimeBootDescriptor().isNotPresent() || computedOptions.getExtensionBootDescriptors().isNotPresent() || computedOptions.getBootRepositories().isNotPresent()) {

                NVersion apiVersion = computedOptions.getApiVersion().orNull();
                if (isRuntimeLoaded() && (NBlankable.isBlank(apiVersion) || Nuts.getVersion().equals(apiVersion))) {
                    if (computedOptions.getRuntimeId().isNotPresent()) {
                        computedOptions.setRuntimeId(runtimeLoadedId);
                        computedOptions.setRuntimeBootDescriptor(null);
                    }
                }
                //resolve runtime id
                if (computedOptions.getRuntimeId().isNotPresent()) {
                    //load from local lib folder
                    NId runtimeId = null;
                    if (!resetFlag && !computedOptions.getRecover().orElse(false)) {
                        runtimeId = NReservedMavenUtils.resolveLatestMavenId(NId.of(NConstants.Ids.NUTS_RUNTIME).get(), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, Collections.singletonList(NRepositoryLocation.of("nuts@" + computedOptions.getStoreType(NStoreType.LIB).get() + File.separatorChar + NConstants.Folders.ID)), computedOptions);
                    }
                    if (runtimeId == null) {
                        runtimeId = NReservedMavenUtils.resolveLatestMavenId(NId.of(NConstants.Ids.NUTS_RUNTIME).get(), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, resolveBootRuntimeRepositories(true), computedOptions);
                    }
                    if (runtimeId == null) {
                        runtimeId = getFallbackCache(NId.RUNTIME_ID, false, false).id;
                    }
                    if (runtimeId == null) {
                        bLog.log(Level.FINEST, NLogVerb.FAIL, NMsg.ofPlain("unable to resolve latest runtime-id version (is connection ok?)"));
                    }
                    computedOptions.setRuntimeId(runtimeId);
                    computedOptions.setRuntimeBootDescriptor(null);
                }
                if (computedOptions.getRuntimeId().isNotPresent()) {
                    computedOptions.setRuntimeId((resolveDefaultRuntimeId(computedOptions.getApiVersion().orNull())));
                    bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofC("consider default runtime-id : %s", computedOptions.getRuntimeId().orNull()));
                }
                NId runtimeIdObject = computedOptions.getRuntimeId().get();
                if (runtimeIdObject.getVersion().isBlank()) {
                    computedOptions.setRuntimeId(resolveDefaultRuntimeId(computedOptions.getApiVersion().orNull()));
                }

                //resolve runtime libraries
                if (computedOptions.getRuntimeBootDescriptor().isNotPresent() && !isRuntimeLoaded()) {
                    Set<NId> loadedDeps = null;
                    NId rid = computedOptions.getRuntimeId().get();
                    Path nutsRuntimeCacheConfigPath = Paths.get(computedOptions.getStoreType(NStoreType.CONF).get() + File.separator + NConstants.Folders.ID).resolve(NIdUtils.resolveIdPath(bootApiId)).resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
                    try {
                        boolean cacheLoaded = false;
                        if (!computedOptions.getRecover().orElse(false) && !resetFlag && NReservedIOUtils.isFileAccessible(nutsRuntimeCacheConfigPath, computedOptions.getExpireTime().orNull(), bLog)) {
                            try {
                                Map<String, Object> obj = NReservedJsonParser.parse(nutsRuntimeCacheConfigPath);
                                bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofC("loaded %s file : %s", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString()));
                                loadedDeps = NId.ofSet((String) obj.get("dependencies")).orElse(new LinkedHashSet<>());
                            } catch (Exception ex) {
                                bLog.log(Level.FINEST, NLogVerb.FAIL, NMsg.ofC("unable to load %s file : %s : %s", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString(), ex.toString()));
                                //ignore...
                            }
                            cacheLoaded = true;
                        }

                        if (!cacheLoaded || loadedDeps == null) {
                            loadedDeps = NReservedMavenUtils.loadDependenciesFromId(computedOptions.getRuntimeId().get(), bLog, resolveBootRuntimeRepositories(false), cache);
                            bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofC("detect runtime dependencies : %s", loadedDeps));
                        }
                    } catch (Exception ex) {
                        bLog.log(Level.FINEST, NLogVerb.FAIL, NMsg.ofC("unable to load %s file : %s", nutsRuntimeCacheConfigPath.getFileName(), ex.toString()));
                        //
                    }
                    if (loadedDeps == null) {
                        NIdCache r = getFallbackCache(NId.RUNTIME_ID, false, false);
                        loadedDeps = r.deps;
                    }

                    if (loadedDeps == null) {
                        throw new NBootException(NMsg.ofC("unable to load dependencies for %s", rid));
                    }
                    computedOptions.setRuntimeBootDescriptor(new DefaultNDescriptorBuilder().setId(computedOptions.getRuntimeId().get()).setDependencies(loadedDeps.stream().map(NId::toDependency).collect(Collectors.toList())));
                    Set<NRepositoryLocation> bootRepositories = resolveBootRuntimeRepositories(false);
                    if (bLog.isLoggable(Level.CONFIG)) {
                        if (bootRepositories.size() == 0) {
                            bLog.log(Level.CONFIG, NLogVerb.FAIL, NMsg.ofPlain("workspace bootRepositories could not be resolved"));
                        } else if (bootRepositories.size() == 1) {
                            bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofC("workspace bootRepositories resolved to : %s", bootRepositories.toArray()[0]));
                        } else {
                            bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofPlain("workspace bootRepositories resolved to : "));
                            for (NRepositoryLocation repository : bootRepositories) {
                                bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofC("    %s", repository));
                            }
                        }
                    }
                    computedOptions.setBootRepositories(bootRepositories.stream().map(NRepositoryLocation::toString).collect(Collectors.joining(";")));
                }

                //resolve extension libraries
                if (computedOptions.getExtensionBootDescriptors().isNotPresent()) {
                    LinkedHashSet<String> excludedExtensions = new LinkedHashSet<>();
                    if (computedOptions.getExcludedExtensions().isPresent()) {
                        for (String excludedExtensionGroup : computedOptions.getExcludedExtensions().get()) {
                            for (String excludedExtension : NStringUtils.split(excludedExtensionGroup, ";,", true, true)) {
                                excludedExtensions.add(NId.of(excludedExtension).get().getShortName());
                            }
                        }
                    }
                    if (computedOptions.getExtensionsSet().isPresent()) {
                        List<NDescriptor> all = new ArrayList<>();
                        for (String extension : computedOptions.getExtensionsSet().orElseGet(Collections::emptySet)) {
                            NId eid = NId.of(extension).get();
                            if (!excludedExtensions.contains(eid.getShortName()) && !excludedExtensions.contains(eid.getArtifactId())) {
                                Path extensionFile = Paths.get(computedOptions.getStoreType(NStoreType.CONF).get() + File.separator + NConstants.Folders.ID).resolve(NIdUtils.resolveIdPath(bootApiId)).resolve(NConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
                                Set<NId> loadedDeps = null;
                                if (isLoadFromCache() && NReservedIOUtils.isFileAccessible(extensionFile, computedOptions.getExpireTime().orNull(), bLog)) {
                                    try {
                                        Properties obj = NReservedIOUtils.loadURLProperties(extensionFile, bLog);
                                        bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofC("loaded %s file : %s", extensionFile.getFileName(), extensionFile.toString()));
                                        loadedDeps = new LinkedHashSet<>(NId.ofList((String) obj.get("dependencies")).orElse(new ArrayList<>()));
                                    } catch (Exception ex) {
                                        bLog.log(Level.CONFIG, NLogVerb.FAIL, NMsg.ofC("unable to load %s file : %s : %s", extensionFile.getFileName(), extensionFile.toString(), ex.toString()));
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    loadedDeps = NReservedMavenUtils.loadDependenciesFromId(eid, bLog, resolveBootRuntimeRepositories(true), cache);
                                }
                                if (loadedDeps == null) {
                                    throw new NBootException(NMsg.ofC("unable to load dependencies for %s", eid));
                                }
                                all.add(new DefaultNDescriptorBuilder().setId(NId.of(extension).get()).setDependencies(loadedDeps.stream().map(NId::toDependency).collect(Collectors.toList())).build());
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
        return computedOptions.getTrace().orElse(true) && !computedOptions.getBot().orElse(false) && (computedOptions.getOutputFormat().orNull() == NContentType.PLAIN || computedOptions.getOutputFormat().isNotPresent());
    }

    private boolean isLoadFromCache() {
        return !computedOptions.getRecover().orElse(false) && !computedOptions.getReset().orElse(false);
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     * @return NWorkspace instance as object
     */
    public Object openWorkspace() {
        return openOrRunWorkspace(false);
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     * @return NWorkspace instance as object
     */
    private Object openOrRunWorkspace(boolean run) {
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NUnsatisfiedRequirementsException(NMsg.ofC("unable to open a distinct version : %s from nuts#%s", getRequirementsHelpString(true), Nuts.getVersion()));
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (computedOptions.getApplicationArguments().get().size() == 0 && computedOptions.getSkipBoot().orElse(false) && (computedOptions.getRecover().orElse(false) || computedOptions.getReset().orElse(false))) {
            if (isPlainTrace()) {
                bLog.log(Level.WARNING, NLogVerb.WARNING, NMsg.ofC("workspace erased : %s", computedOptions.getWorkspace()));
            }
            throw new NBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        Object wsInstance = null;
        NReservedErrorInfoList errorList = new NReservedErrorInfoList();
        try {
            Path configFile = Paths.get(computedOptions.getWorkspace().get()).resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            if (computedOptions.getOpenMode().orNull() == NOpenMode.OPEN_OR_ERROR) {
                //add fail fast test!!
                if (!Files.isRegularFile(configFile)) {
                    throw new NWorkspaceNotFoundException(computedOptions.getWorkspace().orNull());
                }
            } else if (computedOptions.getOpenMode().orNull() == NOpenMode.CREATE_OR_ERROR) {
                if (Files.exists(configFile)) {
                    throw new NWorkspaceAlreadyExistsException(computedOptions.getWorkspace().orNull());
                }
            }
            if (computedOptions.getApiVersion().isBlank()
                    || computedOptions.getRuntimeId().isBlank()
                    || (!isRuntimeLoaded() && computedOptions.getRuntimeBootDescriptor().isNotPresent())
                    || computedOptions.getExtensionBootDescriptors().isNotPresent()
//                    || (!runtimeLoaded && (computedOptions.getBootRepositories().isBlank()))
            ) {
                throw new NBootException(NMsg.ofPlain("invalid workspace state"));
            }
            boolean recover = computedOptions.getRecover().orElse(false) || computedOptions.getReset().orElse(false);

            List<NClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = computedOptions.getStoreType(NStoreType.LIB).get() + File.separator + NConstants.Folders.ID;

            NRepositoryLocation[] repositories = NStringUtils.split(computedOptions.getBootRepositories().orNull(), "\n;", true, true).stream().map(NRepositoryLocation::of).toArray(NRepositoryLocation[]::new);

            NRepositoryLocation workspaceBootLibFolderRepo = NRepositoryLocation.of("nuts@" + workspaceBootLibFolder);
            computedOptions.setRuntimeBootDependencyNode(
                    isRuntimeLoaded() ? null :
                            createClassLoaderNode(computedOptions.getRuntimeBootDescriptor().orNull(), repositories, workspaceBootLibFolderRepo, recover, errorList, true)
            );

            for (NDescriptor nutsBootDescriptor : computedOptions.getExtensionBootDescriptors().orElseGet(ArrayList::new)) {
                deps.add(createClassLoaderNode(nutsBootDescriptor, repositories, workspaceBootLibFolderRepo, recover, errorList, false));
            }
            computedOptions.setExtensionBootDependencyNodes(deps);
            deps.add(0, computedOptions.getRuntimeBootDependencyNode().orNull());

            bootClassWorldURLs = NReservedLangUtils.resolveClassWorldURLs(deps.toArray(new NClassLoaderNode[0]), getContextClassLoader(), bLog);
            workspaceClassLoader = /*bootClassWorldURLs.length == 0 ? getContextClassLoader() : */ new NReservedBootClassLoader(deps.toArray(new NClassLoaderNode[0]), getContextClassLoader());
            computedOptions.setClassWorldLoader(workspaceClassLoader);
            if (bLog.isLoggable(Level.CONFIG)) {
                if (bootClassWorldURLs.length == 0) {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofPlain("empty nuts class world. All dependencies are already loaded in classpath, most likely"));
                } else if (bootClassWorldURLs.length == 1) {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofC("resolve nuts class world to : %s %s", NReservedIOUtils.getURLDigest(bootClassWorldURLs[0], bLog), bootClassWorldURLs[0]));
                } else {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofPlain("resolve nuts class world to : "));
                    for (URL u : bootClassWorldURLs) {
                        bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofC("    %s : %s", NReservedIOUtils.getURLDigest(u, bLog), u));
                    }
                }
            }
            computedOptions.setClassWorldURLs(Arrays.asList(bootClassWorldURLs));
            bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofPlain("search for NutsBootWorkspaceFactory service implementations"));
            ServiceLoader<NBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NBootWorkspaceFactory.class, workspaceClassLoader);
            List<NBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new NReservedBootWorkspaceFactoryComparator(computedOptions));
            if (bLog.isLoggable(Level.CONFIG)) {
                switch (factories.size()) {
                    case 0: {
                        bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofPlain("unable to detect NutsBootWorkspaceFactory service implementations"));
                        break;
                    }
                    case 1: {
                        bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofC("detect NutsBootWorkspaceFactory service implementation : %s", factories.get(0).getClass().getName()));
                        break;
                    }
                    default: {
                        bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofPlain("detect NutsBootWorkspaceFactory service implementations are :"));
                        for (NBootWorkspaceFactory u : factories) {
                            bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofC("    %s", u.getClass().getName()));
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
                        bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofC("create workspace using %s", factoryInstance.getClass().getName()));
                    }
                    computedOptions.setBootWorkspaceFactory(factoryInstance);
                    if(run){
                        wsInstance = a.runWorkspace(computedOptions);
                    }else {
                        wsInstance = a.createWorkspace(computedOptions);
                    }
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("unable to create workspace using factory %s", a), ex);
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
                bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("unable to load Workspace \"%s\" from ClassPath :", computedOptions.getName().orNull()));
                for (URL url : bootClassWorldURLs) {
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("\t %s", NReservedIOUtils.formatURL(url)));
                }
                for (Throwable exception : exceptions) {
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("%s", exception), exception);
                }
                bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("unable to load Workspace Component from ClassPath : %s", Arrays.asList(bootClassWorldURLs)));
                throw new NInvalidWorkspaceException(this.computedOptions.getWorkspace().orNull(), NMsg.ofC("unable to load Workspace Component from ClassPath : %s%n  caused by:%n\t%s", Arrays.asList(bootClassWorldURLs), exceptions.stream().map(Throwable::toString).collect(Collectors.joining("\n\t"))));
            }
            return wsInstance;
        } catch (NReadOnlyException | NCancelException | NNoSessionCancelException ex) {
            throw ex;
        } catch (UnsatisfiedLinkError | AbstractMethodError ex) {
            NMsg errorMessage = NMsg.ofC("unable to boot nuts workspace because the installed binaries are incompatible with the current nuts bootstrap version %s\nusing '-N' command line flag should fix the problem", Nuts.getVersion());
            errorList.insert(0, new NReservedErrorInfo(null, null, null, errorMessage + ": " + ex, ex));
            logError(bootClassWorldURLs, errorList);
            throw new NBootException(errorMessage, ex);
        } catch (Throwable ex) {
            NMsg message = NMsg.ofPlain("unable to locate valid nuts-runtime package");
            errorList.insert(0, new NReservedErrorInfo(null, null, null, message + " : " + ex, ex));
            logError(bootClassWorldURLs, errorList);
            if (ex instanceof NException) {
                throw (NException) ex;
            }
            if (ex instanceof NSecurityException) {
                throw (NSecurityException) ex;
            }
            if (ex instanceof NBootException) {
                throw (NBootException) ex;
            }
            throw new NBootException(message, ex);
        }
    }

    private ClassLoader getContextClassLoader() {
        return computedOptions.getClassLoaderSupplier().orElse(() -> Thread.currentThread().getContextClassLoader()).get();
    }

    private void runCommandHelp() {
        NContentType f = computedOptions.getOutputFormat().orElse(NContentType.PLAIN);
        if (computedOptions.getDry().orElse(false)) {
            printDryCommand("help");
        } else {
            String msg = "nuts is an open source package manager mainly for java applications. Type 'nuts help' or visit https://github.com/thevpc/nuts for more help.";
            switch (f) {
                case JSON: {
                    bLog.outln("{");
                    bLog.outln("  \"help\": \"%s\"", msg);
                    bLog.outln("}");
                    return;
                }
                case TSON: {
                    bLog.outln("{");
                    bLog.outln("  help: \"%s\"", msg);
                    bLog.outln("}");
                    return;
                }
                case YAML: {
                    bLog.outln("help: %s", msg);
                    return;
                }
                case TREE: {
                    bLog.outln("- help: %s", msg);
                    return;
                }
                case TABLE: {
                    bLog.outln("help  %s", msg);
                    return;
                }
                case XML: {
                    bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    bLog.outln("<string>");
                    bLog.outln(" %s", msg);
                    bLog.outln("</string>");
                    return;
                }
                case PROPS: {
                    bLog.outln("help=%s", msg);
                    return;
                }
            }
            bLog.outln("%s", msg);
        }
    }

    private void printDryCommand(String cmd) {
        NContentType f = computedOptions.getOutputFormat().orElse(NContentType.PLAIN);
        if (computedOptions.getDry().orElse(false)) {
            switch (f) {
                case JSON: {
                    bLog.outln("{");
                    bLog.outln("  \"dryCommand\": \"%s\"", cmd);
                    bLog.outln("}");
                    return;
                }
                case TSON: {
                    bLog.outln("{");
                    bLog.outln("  dryCommand: \"%s\"", cmd);
                    bLog.outln("}");
                    return;
                }
                case YAML: {
                    bLog.outln("dryCommand: %s", cmd);
                    return;
                }
                case TREE: {
                    bLog.outln("- dryCommand: %s", cmd);
                    return;
                }
                case TABLE: {
                    bLog.outln("dryCommand  %s", cmd);
                    return;
                }
                case XML: {
                    bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    bLog.outln("<object>");
                    bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "dryCommand", cmd);
                    bLog.outln("</object>");
                    return;
                }
                case PROPS: {
                    bLog.outln("dryCommand=%s", cmd);
                    return;
                }
            }
            bLog.outln("[Dry] %s", Nuts.getVersion());
        }
    }

    private void runCommandVersion() {
        NContentType f = computedOptions.getOutputFormat().orElse(NContentType.PLAIN);
        if (computedOptions.getDry().orElse(false)) {
            printDryCommand("version");
            return;
        }
        switch (f) {
            case JSON: {
                bLog.outln("{");
                bLog.outln("  \"version\": \"%s\",", Nuts.getVersion());
                bLog.outln("  \"digest\": \"%s\"", getApiDigestOrInternal());
                bLog.outln("}");
                return;
            }
            case TSON: {
                bLog.outln("{");
                bLog.outln("  version: \"%s\",", Nuts.getVersion());
                bLog.outln("  digest: \"%s\"", getApiDigestOrInternal());
                bLog.outln("}");
                return;
            }
            case YAML: {
                bLog.outln("version: %s", Nuts.getVersion());
                bLog.outln("digest: %s", getApiDigestOrInternal());
                return;
            }
            case TREE: {
                bLog.outln("- version: %s", Nuts.getVersion());
                bLog.outln("- digest: %s", getApiDigestOrInternal());
                return;
            }
            case TABLE: {
                bLog.outln("version      %s", Nuts.getVersion());
                bLog.outln("digest  %s", getApiDigestOrInternal());
                return;
            }
            case XML: {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("<object>");
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "version", Nuts.getVersion());
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "digest", getApiDigestOrInternal());
                bLog.outln("</object>");
                return;
            }
            case PROPS: {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("version=%s", Nuts.getVersion());
                bLog.outln("digest=%s", getApiDigestOrInternal());
                bLog.outln("</object>");
                return;
            }
        }
        bLog.outln("%s", Nuts.getVersion());
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     * @return NWorkspace instance as object
     */
    public Object runWorkspace() {
        if (computedOptions.getCommandHelp().orElse(false)) {
            runCommandHelp();
            return null;
        } else if (computedOptions.getCommandVersion().orElse(false)) {
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
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain(message));
    }

    private void logError(URL[] bootClassWorldURLs, NReservedErrorInfoList ths) {
        String workspace = computedOptions.getWorkspace().orNull();
        Map<NStoreType, String> rbc_locations = computedOptions.getStoreLocations().orElse(Collections.emptyMap());
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("unable to bootstrap nuts (digest %s):", getApiDigestOrInternal()));
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("%s", ths.list().get(0)));
        }
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("here after current environment info:"));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-boot-api-version            : %s", computedOptions.getApiVersion().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-boot-runtime                : %s", computedOptions.getRuntimeId().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-boot-repositories           : %s", computedOptions.getBootRepositories().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  workspace-location               : %s", NOptional.of(workspace).orElse("<default-location>")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-bin                   : %s", rbc_locations.get(NStoreType.BIN)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-conf                  : %s", rbc_locations.get(NStoreType.CONF)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-var                   : %s", rbc_locations.get(NStoreType.VAR)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-log                   : %s", rbc_locations.get(NStoreType.LOG)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-temp                  : %s", rbc_locations.get(NStoreType.TEMP)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-cache                 : %s", rbc_locations.get(NStoreType.CACHE)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-run                   : %s", rbc_locations.get(NStoreType.RUN)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-lib                   : %s", rbc_locations.get(NStoreType.LIB)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-strategy              : %s", NReservedUtils.desc(computedOptions.getStoreStrategy().orNull())));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-store-layout                : %s", NReservedUtils.desc(computedOptions.getStoreLayout().orNull())));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-boot-args                   : %s", this.computedOptions.toCmdLine()));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-app-args                    : %s", this.computedOptions.getApplicationArguments().get()));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  option-read-only                 : %s", this.computedOptions.getReadOnly().orElse(false)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  option-trace                     : %s", this.computedOptions.getTrace().orElse(false)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  option-progress                  : %s", NReservedUtils.desc(this.computedOptions.getProgressOptions().orNull())));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  option-open-mode                 : %s", NReservedUtils.desc(this.computedOptions.getOpenMode().orElse(NOpenMode.OPEN_OR_CREATE))));

        NClassLoaderNode rtn = this.computedOptions.getRuntimeBootDependencyNode().orNull();
        String rtHash = "";
        if (rtn != null) {
            rtHash = NReservedIOUtils.getURLDigest(rtn.getURL(), bLog);
        }
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-runtime-digest                : %s", rtHash));

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-runtime-classpath           : %s", "<none>"));
        } else {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-runtime-hash                : %s", "<none>"));
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  nuts-runtime-classpath           : %s", NReservedIOUtils.formatURL(bootClassWorldURL)));
                } else {
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("                                     %s", NReservedIOUtils.formatURL(bootClassWorldURL)));
                }
            }
        }
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  java-version                     : %s", System.getProperty("java.version")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  java-executable                  : %s", NReservedUtils.resolveJavaCommand(null)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  java-class-path                  : %s", System.getProperty("java.class.path")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  java-library-path                : %s", System.getProperty("java.library.path")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  os-name                          : %s", System.getProperty("os.name")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  os-arch                          : %s", System.getProperty("os.arch")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  os-version                       : %s", System.getProperty("os.version")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  user-name                        : %s", System.getProperty("user.name")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  user-home                        : %s", System.getProperty("user.home")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC("  user-dir                         : %s", System.getProperty("user.dir")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain(""));
        if (this.computedOptions.getLogConfig().get().getLogTermLevel() == null || this.computedOptions.getLogConfig().get().getLogFileLevel().intValue() > Level.FINEST.intValue()) {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("If the problem persists you may want to get more debug info by adding '--verbose' arguments."));
        }
        if (!this.computedOptions.getReset().orElse(false) && !this.computedOptions.getRecover().orElse(false) && this.computedOptions.getExpireTime().isNotPresent()) {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments."));
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("Here is the proper command : "));
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("  java -jar nuts.jar --verbose --recover --expire [...]"));
        } else if (!this.computedOptions.getReset().orElse(false) && this.computedOptions.getRecover().orElse(false) && this.computedOptions.getExpireTime().isPresent()) {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("You may also enable full reset mode to ignore existing configuration with '--reset' argument."));
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("ATTENTION: this will delete all your nuts configuration. Use it at your own risk."));
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("Here is the proper command : "));
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("  java -jar nuts.jar --verbose --reset [...]"));
        }
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("error stack trace is:"));
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
                bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofC(msg.toString(), msgParams.toArray()));
                bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain(th.toString()), th.getThrowable());
            }
        } else {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("no stack trace is available."));
        }
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("now exiting nuts, Bye!"));
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NBlankable.isBlank(computedOptions.getApiVersion().orNull())) {
            if (!unsatisfiedOnly || !computedOptions.getApiVersion().get().equals(Nuts.getVersion())) {
                req += 1;
            }
        }
        if (!unsatisfiedOnly || !NReservedUtils.isActualJavaCommand(computedOptions.getJavaCommand().orNull())) {
            req += 2;
        }
        if (!unsatisfiedOnly || !NReservedUtils.isActualJavaOptions(computedOptions.getJavaOptions().orNull())) {
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
            sb.append("nuts version ").append(NId.ofApi(computedOptions.getApiVersion().orNull()));
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

    private NClassLoaderNode createClassLoaderNode(NDescriptor descr, NRepositoryLocation[] repositories, NRepositoryLocation workspaceBootLibFolder, boolean recover, NReservedErrorInfoList errorList, boolean runtimeDep) throws MalformedURLException {
        NId id = descr.getId();
        List<NDependency> deps = descr.getDependencies();
        NClassLoaderNodeBuilder rt = new NClassLoaderNodeBuilder();
        String name = runtimeDep ? "runtime" : ("extension " + id.toString());
        File file = NReservedMavenUtils.getBootCacheJar(computedOptions.getRuntimeId().get(), repositories, workspaceBootLibFolder, !recover, name, computedOptions.getExpireTime().orNull(), errorList, computedOptions, pathExpansionConverter, bLog, cache);
        rt.setId(id.toString());
        rt.setUrl(file.toURI().toURL());
        rt.setIncludedInClasspath(NReservedLangUtils.isLoadedClassPath(rt.getURL(), getContextClassLoader(), bLog));

        if (bLog.isLoggable(Level.CONFIG)) {
            String rtHash = "";
            if (computedOptions.getRuntimeId().isPresent()) {
                rtHash = NReservedIOUtils.getFileOrDirectoryDigest(file.toPath());
                if (rtHash == null) {
                    rtHash = "";
                }
            }
            bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofC("detect %s version %s - digest %s from %s", name, id.toString(), rtHash, file));
        }

        for (NDependency s : deps) {
            NClassLoaderNodeBuilder x = new NClassLoaderNodeBuilder();
            if (NReservedUtils.isAcceptDependency(s, computedOptions)) {
                x.setId(s.toString()).setUrl(NReservedMavenUtils.getBootCacheJar(s.toId(), repositories, workspaceBootLibFolder, !recover, name + " dependency", computedOptions.getExpireTime().orNull(), errorList, computedOptions, pathExpansionConverter, bLog, cache).toURI().toURL());
                x.setIncludedInClasspath(NReservedLangUtils.isLoadedClassPath(x.getURL(), getContextClassLoader(), bLog));
                rt.addDependency(x.build());
            }
        }
        return rt.build();
    }

    private NId resolveDefaultRuntimeId(NVersion apiVersion) {
        String sApiVersion = apiVersion.getValue();
        // check fo qualifier
        int q = sApiVersion.indexOf('-');
        if (q > 0) {
            return NId.ofRuntime((sApiVersion.substring(0, q) + ".0" + sApiVersion.substring(q))).get();
        }
        return NId.ofRuntime(sApiVersion + ".0").get();
    }
}
