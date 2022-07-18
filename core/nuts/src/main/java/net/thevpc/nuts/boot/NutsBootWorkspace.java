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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.reserved.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
public final class NutsBootWorkspace {

    private final Instant creationTime = Instant.now();
    private final NutsWorkspaceOptions userOptions;
    private final NutsReservedBootLog bLog;
    private final NutsWorkspaceBootOptionsBuilder computedOptions = new DefaultNutsWorkspaceBootOptionsBuilder();
    private final NutsReservedBootRepositoryDB repositoryDB;
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
                    return NutsReservedUtils.getHome(NutsStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()), computedOptions);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<NutsStoreLocation, String> s = computedOptions.getStoreLocations().orElse(Collections.emptyMap());
                    String v = s.get(NutsStoreLocation.parse(from).orNull());
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
    private NutsWorkspaceBootOptionsBuilder lastWorkspaceOptions;
    private Set<NutsRepositoryLocation> parsedBootRuntimeDependenciesRepositories;
    private Set<NutsRepositoryLocation> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private NutsLogger nLog;
    private NutsSession nLogSession;
    private Scanner scanner;

    public NutsBootWorkspace(NutsWorkspaceTerminalOptions bootTerminal, String... args) {
        this.bLog = new NutsReservedBootLog(bootTerminal);
        NutsWorkspaceOptionsBuilder userOptions = new DefaultNutsWorkspaceOptionsBuilder();
        if (bootTerminal != null) {
            userOptions.setStdin(bootTerminal.getIn());
            userOptions.setStdout(bootTerminal.getOut());
            userOptions.setStderr(bootTerminal.getErr());
        }
        InputStream in = userOptions.getStdin().orNull();
        scanner = new Scanner(in == null ? System.in : in);
        userOptions.setCommandLine(args, null);
        if (userOptions.getSkipErrors().orElse(false)) {
            StringBuilder errorMessage = new StringBuilder();
            for (NutsMessage s : userOptions.getErrors().orElseGet(Collections::emptyList)) {
                errorMessage.append(s).append("\n");
            }
            errorMessage.append("Try 'nuts --help' for more information.");
            bLog.log(Level.WARNING, NutsLoggerVerb.WARNING, NutsMessage.ofCstyle("Error : %s", errorMessage));
        }
        this.userOptions = userOptions.readOnly();
        repositoryDB = new NutsReservedBootRepositoryDB(nLog);
        this.postInit();
    }

    public NutsBootWorkspace(NutsWorkspaceOptions userOptions) {
        if (userOptions == null) {
            userOptions = DefaultNutsWorkspaceOptions.BLANK;
        }
        this.bLog = new NutsReservedBootLog(new NutsWorkspaceTerminalOptions(userOptions.getStdin().orNull(), userOptions.getStdout().orNull(), userOptions.getStderr().orNull()));
        this.userOptions = userOptions.readOnly();
        repositoryDB = new NutsReservedBootRepositoryDB(nLog);
        this.postInit();
    }

    private void postInit() {
        this.computedOptions.setAll(userOptions);
        this.computedOptions.setUserOptions(this.userOptions);
        this.computedOptions.setBot(this.computedOptions.getBot().orElse(false));
        this.computedOptions.setDry(this.computedOptions.getDry().orElse(false));
        this.computedOptions.setGlobal(this.computedOptions.getGlobal().orElse(false));
        this.computedOptions.setGui(this.computedOptions.getGui().orElse(false));
        this.computedOptions.setInherited(this.computedOptions.getInherited().orElse(false));
        this.computedOptions.setReadOnly(this.computedOptions.getReadOnly().orElse(false));
        this.computedOptions.setRecover(this.computedOptions.getRecover().orElse(false));
        this.computedOptions.setReset(this.computedOptions.getReset().orElse(false));
        this.computedOptions.setSkipErrors(this.computedOptions.getSkipErrors().orElse(false));
        this.computedOptions.setSkipWelcome(this.computedOptions.getSkipWelcome().orElse(false));
        this.computedOptions.setSkipCompanions(this.computedOptions.getSkipCompanions().orElse(false));
        this.computedOptions.setIsolationLevel(this.computedOptions.getIsolationLevel().orElse(NutsIsolationLevel.SYSTEM));
        this.computedOptions.setTransitive(this.computedOptions.getTransitive().orElse(true));
        this.computedOptions.setTrace(this.computedOptions.getTrace().orElse(true));
        this.computedOptions.setCached(this.computedOptions.getCached().orElse(true));
        this.computedOptions.setIndexed(this.computedOptions.getIndexed().orElse(true));
        this.computedOptions.setExecutionType(this.computedOptions.getExecutionType().orElse(NutsExecutionType.SPAWN));
        this.computedOptions.setConfirm(this.computedOptions.getConfirm().orElse(NutsConfirmationMode.ASK));
        this.computedOptions.setFetchStrategy(this.computedOptions.getFetchStrategy().orElse(NutsFetchStrategy.ONLINE));
        this.computedOptions.setOpenMode(this.computedOptions.getOpenMode().orElse(NutsOpenMode.OPEN_OR_CREATE));
        this.computedOptions.setRunAs(this.computedOptions.getRunAs().orElse(NutsRunAs.CURRENT_USER));
        this.computedOptions.setLogConfig(this.computedOptions.getLogConfig().orElseGet(NutsLogConfig::new));
        this.computedOptions.setStdin(this.computedOptions.getStdin().orElse(System.in));
        this.computedOptions.setStdout(this.computedOptions.getStdout().orElse(System.out));
        this.computedOptions.setStderr(this.computedOptions.getStderr().orElse(System.err));
        this.computedOptions.setLocale(this.computedOptions.getLocale().orElse(Locale.getDefault().toString()));
        this.computedOptions.setOutputFormat(this.computedOptions.getOutputFormat().orElse(NutsContentType.PLAIN));
        this.computedOptions.setCreationTime(this.computedOptions.getCreationTime().orElse(creationTime));
        this.bLog.setOptions(this.computedOptions);
    }

    private static void revalidateLocations(NutsWorkspaceBootOptionsBuilder bootOptions, String workspaceName, boolean immediateLocation, NutsIsolationLevel sandboxMode) {
        if (NutsBlankable.isBlank(bootOptions.getName())) {
            bootOptions.setName(workspaceName);
        }
        boolean global = bootOptions.getGlobal().orElse(false);
        if (sandboxMode.compareTo(NutsIsolationLevel.SANDBOX) >= 0) {
            bootOptions.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
            bootOptions.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
            global = false;
        } else {
            if (bootOptions.getStoreLocationStrategy().isNotPresent()) {
                bootOptions.setStoreLocationStrategy(immediateLocation ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
            }
            if (bootOptions.getRepositoryStoreLocationStrategy().isNotPresent()) {
                bootOptions.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
            }
        }
        Map<NutsStoreLocation, String> storeLocations = NutsPlatformUtils.buildLocations(bootOptions.getStoreLocationLayout().orNull(), bootOptions.getStoreLocationStrategy().orNull(), bootOptions.getStoreLocations().orNull(), bootOptions.getHomeLocations().orNull(), global, bootOptions.getWorkspace().orNull(), null//no session!
        );
        if (new HashSet<>(storeLocations.values()).size() != storeLocations.size()) {
            Map<String, List<NutsStoreLocation>> conflicts = new LinkedHashMap<>();
            for (Map.Entry<NutsStoreLocation, String> e : storeLocations.entrySet()) {
                conflicts.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
            }
            StringBuilder error = new StringBuilder();
            error.append("invalid store locations. Two or more stores point to the same location:");
            List<Object> errorParams = new ArrayList<>();
            for (Map.Entry<String, List<NutsStoreLocation>> e : conflicts.entrySet()) {
                List<NutsStoreLocation> ev = e.getValue();
                if (ev.size() > 1) {
                    String ek = e.getKey();
                    error.append("\n");
                    error.append("all of (").append(ev.stream().map(x -> "%s").collect(Collectors.joining(","))).append(") point to %s");
                    errorParams.addAll(ev);
                    errorParams.add(ek);
                }
            }
            throw new NutsBootException(NutsMessage.ofCstyle(error.toString(), errorParams));
        }
        bootOptions.setStoreLocations(storeLocations);
    }

    private static final class ApiDigestHolder {
        static final String apiDigest = NutsApiUtils.resolveNutsIdDigestOrError();
    }

    /**
     * current nuts version, loaded from pom file
     *
     * @return current nuts version
     */
    private static String getApiDigest() {
        return ApiDigestHolder.apiDigest;
    }

    public boolean hasUnsatisfiedRequirements() {
        prepareWorkspace();
        return newInstanceRequirements != 0;
    }

    public void runNewProcess() {
        String[] processCommandLine = createProcessCommandLine();
        int result;
        try {
            if (nLog != null) {
                nLog.with().session(nLogSession).level(Level.FINE).verb(NutsLoggerVerb.START).log(NutsMessage.ofJstyle("start new process : {0}", NutsCommandLine.of(processCommandLine)));
            } else {
                bLog.log(Level.FINE, NutsLoggerVerb.START, NutsMessage.ofJstyle("start new process : {0}", NutsCommandLine.of(processCommandLine)));
            }
            result = new ProcessBuilder(processCommandLine).inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ex) {
            throw new NutsBootException(NutsMessage.ofPlain("failed to run new nuts process"), ex);
        }
        if (result != 0) {
            throw new NutsBootException(NutsMessage.ofCstyle("failed to exec new process. returned %s", result));
        }
    }

    /**
     * repositories used to locale nuts-runtime artifact or its dependencies
     *
     * @param dependencies when true search for runtime dependencies, when
     *                     false, search for runtime
     * @return repositories
     */
    public Set<NutsRepositoryLocation> resolveBootRuntimeRepositories(boolean dependencies) {
        if (dependencies) {
            if (parsedBootRuntimeDependenciesRepositories != null) {
                return parsedBootRuntimeDependenciesRepositories;
            }
            bLog.log(Level.FINE, NutsLoggerVerb.START, NutsMessage.ofJstyle("resolve boot repositories to load nuts-runtime dependencies from options : {0} and config: {1}", computedOptions.getRepositories().orElseGet(Collections::emptyList).toString(), computedOptions.getBootRepositories().ifBlankNull().orElse("[]")));
        } else {
            if (parsedBootRuntimeRepositories != null) {
                return parsedBootRuntimeRepositories;
            }
            bLog.log(Level.FINE, NutsLoggerVerb.START, NutsMessage.ofJstyle("resolve boot repositories to load nuts-runtime from options : {0} and config: {1}", computedOptions.getRepositories().orElseGet(Collections::emptyList).toString(), computedOptions.getBootRepositories().ifBlankNull().orElse("[]")));
        }
        NutsRepositorySelectorList bootRepositories = NutsRepositorySelectorList.ofAll(computedOptions.getRepositories().orNull(), repositoryDB, null);
        NutsRepositorySelector[] old = NutsRepositorySelectorList.ofAll(Arrays.asList(computedOptions.getBootRepositories().orNull()), repositoryDB, null).toArray();
        NutsRepositoryLocation[] result;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositories.resolve(
                    NutsReservedMavenUtils.loadAllMavenRepos(nLog).toArray(new NutsRepositoryLocation[0])
                    , repositoryDB
            );
        } else {
            result = bootRepositories.resolve(Arrays.stream(old).map(x -> NutsRepositoryLocation.of(x.getName(), x.getUrl())).toArray(NutsRepositoryLocation[]::new), repositoryDB);
        }
        Set<NutsRepositoryLocation> rr = Arrays.stream(result).collect(Collectors.toCollection(LinkedHashSet::new));
        if (dependencies) {
            parsedBootRuntimeDependenciesRepositories = rr;
        } else {
            parsedBootRuntimeRepositories = rr;
        }
        return rr;
    }

    public String[] createProcessCommandLine() {
        prepareWorkspace();
        bLog.log(Level.FINE, NutsLoggerVerb.START, NutsMessage.ofJstyle("running version {0}.  {1}", computedOptions.getApiVersion().orNull(), getRequirementsHelpString(true)));
        String defaultWorkspaceLibFolder = computedOptions.getStoreLocation(NutsStoreLocation.LIB).orNull();
        List<NutsRepositoryLocation> repos = new ArrayList<>();
        repos.add(NutsRepositoryLocation.of("nuts@" + defaultWorkspaceLibFolder));
        Collection<NutsRepositoryLocation> bootRepositories = resolveBootRuntimeRepositories(true);
        repos.addAll(bootRepositories);
        NutsReservedErrorInfoList errorList = new NutsReservedErrorInfoList();
        File file = NutsReservedMavenUtils.resolveOrDownloadJar(NutsId.ofApi(computedOptions.getApiVersion().orNull()).get(), repos.toArray(new NutsRepositoryLocation[0]), NutsRepositoryLocation.of("nuts@" + computedOptions.getStoreLocation(NutsStoreLocation.LIB).get() + File.separator + NutsConstants.Folders.ID), bLog, false, computedOptions.getExpireTime().orNull(), errorList);
        if (file == null) {
            errorList.insert(
                    0, new NutsReservedErrorInfo(null, null, null, "unable to load nuts " + computedOptions.getApiVersion().orNull(), null)
            );
            logError(null, errorList);
            throw new NutsBootException(NutsMessage.ofCstyle("unable to load %s#%s", NutsConstants.Ids.NUTS_API, computedOptions.getApiVersion().orNull()));
        }

        List<String> cmd = new ArrayList<>();
        String jc = computedOptions.getJavaCommand().orNull();
        if (jc == null || jc.trim().isEmpty()) {
            jc = NutsReservedUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : NutsCommandLine.parseDefault(computedOptions.getJavaOptions().orNull()).get().toStringArray()) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        if (computedOptions.getJavaOptions().isNotPresent()) {
            Collections.addAll(cmd, NutsCommandLine.parseDefault(computedOptions.getJavaOptions().orNull()).get().toStringArray());
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(computedOptions.toCommandLine(new NutsWorkspaceOptionsConfig().setCompact(true).setApiVersion(computedOptions.getApiVersion().orNull())).toStringList());
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            bLog.log(Level.FINE, NutsLoggerVerb.START, NutsMessage.ofJstyle("[exec] {0}", sb));
        }
        return cmd.toArray(new String[0]);
    }

    public NutsWorkspaceBootOptionsBuilder getOptions() {
        return computedOptions;
    }

    @SuppressWarnings("unchecked")
    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {
            preparedWorkspace = true;
            NutsIsolationLevel isolationMode = computedOptions.getIsolationLevel().orElse(NutsIsolationLevel.SYSTEM);
            if (bLog.isLoggable(Level.CONFIG)) {
                bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("bootstrap Nuts version {0}{1}- digest {1}...", Nuts.getVersion(),
                        isolationMode == NutsIsolationLevel.SYSTEM ? "" :
                                isolationMode == NutsIsolationLevel.USER ? " (user mode)" :
                                        isolationMode == NutsIsolationLevel.CONFINED ? " (confined mode)" :
                                                isolationMode == NutsIsolationLevel.SANDBOX ? " (sandbox mode)" :
                                                        " (unsupported mode)",
                        getApiDigest()));
                bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofPlain("boot-class-path:"));
                for (String s : NutsStringUtils.split(System.getProperty("java.class.path"), File.pathSeparator, true, true)) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("                  {0}", s));
                }
                ClassLoader thisClassClassLoader = getClass().getClassLoader();
                bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("class-loader: {0}", thisClassClassLoader));
                for (URL url : NutsReservedClassLoaderUtils.resolveClasspathURLs(thisClassClassLoader, false)) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("                 {0}", url));
                }
                ClassLoader tctxloader = Thread.currentThread().getContextClassLoader();
                if (tctxloader != thisClassClassLoader) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("thread-class-loader: {0}", tctxloader));
                    for (URL url : NutsReservedClassLoaderUtils.resolveClasspathURLs(tctxloader, false)) {
                        bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("                 {0}", url));
                    }
                }
                ClassLoader contextClassLoader = getContextClassLoader();
                bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("ctx-class-loader: {0}", contextClassLoader));
                if (contextClassLoader != null) {
                    for (URL url : NutsReservedClassLoaderUtils.resolveClasspathURLs(contextClassLoader, false)) {
                        bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("                 {0}", url));
                    }
                }
                bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("system-properties:", contextClassLoader));
                Map<String, String> m = (Map) System.getProperties();
                int max = m.keySet().stream().mapToInt(String::length).max().getAsInt();
                for (String k : new TreeSet<String>(m.keySet())) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.START, NutsMessage.ofJstyle("    {0} = {1}", NutsStringUtils.formatAlign(k, max, NutsPositionType.FIRST), NutsStringUtils.formatStringLiteral(m.get(k), NutsStringUtils.QuoteType.DOUBLE)));
                }
            }
            String workspaceName = null;
            NutsWorkspaceBootOptionsBuilder lastConfigLoaded = null;
            String lastNutsWorkspaceJsonConfigPath = null;
            boolean immediateLocation = false;
            boolean resetFlag = computedOptions.getReset().orElse(false);
            boolean dryFlag = computedOptions.getDry().orElse(false);
            String _ws = computedOptions.getWorkspace().orNull();
            if (isolationMode == NutsIsolationLevel.SANDBOX) {
                Path t = null;
                try {
                    t = Files.createTempDirectory("nuts-sandbox-" + Instant.now().toString().replace(':', '-'));
                } catch (IOException e) {
                    throw new NutsBootException(NutsMessage.ofNtf("unable to create temporary/sandbox folder"), e);
                }
                lastNutsWorkspaceJsonConfigPath = t.toString();
                immediateLocation = true;
                workspaceName = t.getFileName().toString();
                resetFlag = false; //no need for reset
                if (computedOptions.getGlobal().orElse(false)) {
                    throw new NutsBootException(NutsMessage.ofNtf("you cannot specify option '--global' in sandbox mode"));
                }
                if (computedOptions.getWorkspace().ifBlankNull().isPresent()) {
                    throw new NutsBootException(NutsMessage.ofNtf("you cannot specify '--workspace' in sandbox mode"));
                }
                if (computedOptions.getStoreLocationStrategy().orElse(NutsStoreLocationStrategy.STANDALONE) != NutsStoreLocationStrategy.STANDALONE) {
                    throw new NutsBootException(NutsMessage.ofNtf("you cannot specify '--exploded' in sandbox mode"));
                }
                if (computedOptions.getGlobal().orElse(false)) {
                    throw new NutsBootException(NutsMessage.ofNtf("you cannot specify '--global' in sandbox mode"));
                }
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            } else {
                if (isolationMode.compareTo(NutsIsolationLevel.SYSTEM) > 0 && userOptions.getGlobal().orElse(false)) {
                    if (userOptions.getReset().orElse(false)) {
                        throw new NutsBootException(NutsMessage.ofCstyle("invalid option 'global' in %s mode", isolationMode));
                    }
                }
                if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                    //this is a protocol based workspace
                    //String protocol=ws.substring(0,ws.indexOf("://"));
                    workspaceName = "remote-bootstrap";
                    lastNutsWorkspaceJsonConfigPath = NutsPlatformUtils.getWorkspaceLocation(null, computedOptions.getGlobal().orElse(false), NutsReservedUtils.resolveValidWorkspaceName(workspaceName));
                    lastConfigLoaded = NutsReservedBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                    immediateLocation = true;

                } else {
                    immediateLocation = NutsReservedUtils.isValidWorkspaceName(_ws);
                    int maxDepth = 36;
                    for (int i = 0; i < maxDepth; i++) {
                        lastNutsWorkspaceJsonConfigPath = NutsReservedUtils.isValidWorkspaceName(_ws) ? NutsPlatformUtils.getWorkspaceLocation(null, computedOptions.getGlobal().orElse(false), NutsReservedUtils.resolveValidWorkspaceName(_ws)) : NutsReservedIOUtils.getAbsolutePath(_ws);

                        NutsWorkspaceBootOptionsBuilder configLoaded = NutsReservedBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                        if (configLoaded == null) {
                            //not loaded
                            break;
                        }
                        if (NutsBlankable.isBlank(configLoaded.getWorkspace())) {
                            lastConfigLoaded = configLoaded;
                            break;
                        }
                        _ws = configLoaded.getWorkspace().orNull();
                        if (i >= maxDepth - 1) {
                            throw new NutsBootException(NutsMessage.ofPlain("cyclic workspace resolution"));
                        }
                    }
                    workspaceName = NutsReservedUtils.resolveValidWorkspaceName(computedOptions.getWorkspace().orNull());
                }
            }
            computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            if (lastConfigLoaded != null) {
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                computedOptions.setName(lastConfigLoaded.getName().orNull());
                computedOptions.setUuid(lastConfigLoaded.getUuid().orNull());
                NutsWorkspaceBootOptionsBuilder curr;
                if (!resetFlag) {
                    curr = computedOptions;
                } else {
                    lastWorkspaceOptions = new DefaultNutsWorkspaceBootOptionsBuilder();
                    curr = lastWorkspaceOptions;
                    curr.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                    curr.setName(lastConfigLoaded.getName().orNull());
                    curr.setUuid(lastConfigLoaded.getUuid().orNull());
                }
                curr.setBootRepositories(lastConfigLoaded.getBootRepositories().orNull());
                curr.setJavaCommand(lastConfigLoaded.getJavaCommand().orNull());
                curr.setJavaOptions(lastConfigLoaded.getJavaOptions().orNull());
                curr.setExtensionsSet(NutsReservedCollectionUtils.nonNullSet(lastConfigLoaded.getExtensionsSet().orNull()));
                curr.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy().orNull());
                curr.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy().orNull());
                curr.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout().orNull());
                curr.setStoreLocations(NutsReservedCollectionUtils.nonNullMap(lastConfigLoaded.getStoreLocations().orNull()));
                curr.setHomeLocations(NutsReservedCollectionUtils.nonNullMap(lastConfigLoaded.getHomeLocations().orNull()));
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
                        bLog.log(Level.INFO, NutsLoggerVerb.DEBUG, NutsMessage.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, NutsLoggerVerb.WARNING, NutsMessage.ofPlain("reset workspace"));
                        countDeleted = NutsReservedUtils.deleteStoreLocations(lastWorkspaceOptions, getOptions(), true, bLog, NutsStoreLocation.values(), () -> scanner.nextLine());
                        NutsReservedUtils.ndiUndo(bLog);
                    }
                } else {
                    if (dryFlag) {
                        bLog.log(Level.INFO, NutsLoggerVerb.DEBUG, NutsMessage.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, NutsLoggerVerb.WARNING, NutsMessage.ofPlain("reset workspace"));
                        countDeleted = NutsReservedUtils.deleteStoreLocations(computedOptions, getOptions(), true, bLog, NutsStoreLocation.values(), () -> scanner.nextLine());
                        NutsReservedUtils.ndiUndo(bLog);
                    }
                }
            } else if (computedOptions.getRecover().orElse(false)) {
                if (dryFlag) {
                    bLog.log(Level.INFO, NutsLoggerVerb.DEBUG, NutsMessage.ofPlain("[dry] [recover] delete CACHE/TEMP workspace folders"));
                } else {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.WARNING, NutsMessage.ofPlain("recover workspace."));
                    List<Object> folders = new ArrayList<>();
                    folders.add(NutsStoreLocation.CACHE);
                    folders.add(NutsStoreLocation.TEMP);
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = NutsReservedUtils.getStoreLocationPath(computedOptions, NutsStoreLocation.LIB);
                    if (p != null) {
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts"));
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = NutsReservedUtils.deleteStoreLocations(computedOptions, getOptions(), false, bLog, folders.toArray(), () -> scanner.nextLine());
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
            if (computedOptions.getStoreLocationLayout().isNotPresent()) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setStoreLocationLayout(lastWorkspaceOptions.getStoreLocationLayout().orElse(NutsOsFamily.getCurrent()));
                } else {
                    computedOptions.setHomeLocations(Collections.emptyMap());
                }
            }

            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (computedOptions.getApplicationArguments().get().size() == 0 && computedOptions.getSkipBoot().orElse(false) && (computedOptions.getRecover().orElse(false) || resetFlag)) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        bLog.log(Level.WARNING, NutsLoggerVerb.WARNING, NutsMessage.ofJstyle("workspace erased : {0}", computedOptions.getWorkspace()));
                    } else {
                        bLog.log(Level.WARNING, NutsLoggerVerb.WARNING, NutsMessage.ofJstyle("workspace is not erased because it does not exist : {0}", computedOptions.getWorkspace()));
                    }
                }
                throw new NutsBootException(NutsMessage.ofPlain(""), 0);
            }
            //after eventual clean up
            if (computedOptions.getInherited().orElse(false)) {
                //when Inherited, always use the current Api version!
                computedOptions.setApiVersion(Nuts.getVersion());
            } else {
                NutsVersion nutsVersion = computedOptions.getApiVersion().orElse(NutsVersion.BLANK);
                if (nutsVersion.isLatestVersion() || nutsVersion.isReleaseVersion()) {
                    NutsId s = NutsReservedMavenUtils.resolveLatestMavenId(NutsId.ofApi("").get(), null, bLog, resolveBootRuntimeRepositories(true));
                    if (s == null) {
                        throw new NutsBootException(NutsMessage.ofPlain("unable to load latest nuts version"));
                    }
                    computedOptions.setApiVersion(s.getVersion());
                }
                if (nutsVersion.isBlank()) {
                    computedOptions.setApiVersion(Nuts.getVersion());
                }
            }

            NutsId bootApiId = NutsId.ofApi(computedOptions.getApiVersion().orNull()).get();
            Path nutsApiConfigBootPath = Paths.get(computedOptions.getStoreLocation(NutsStoreLocation.CONFIG).get() + File.separator + NutsConstants.Folders.ID).resolve(NutsReservedUtils.idToPath(bootApiId)).resolve(NutsConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && NutsReservedIOUtils.isFileAccessible(nutsApiConfigBootPath, computedOptions.getExpireTime().orNull(), bLog)) {
                try {
                    Map<String, Object> obj = NutsReservedJsonParser.parse(nutsApiConfigBootPath);
                    if (!obj.isEmpty()) {
                        bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("loaded {0} file : {1}", nutsApiConfigBootPath.getFileName(), nutsApiConfigBootPath.toString()));
                        loadedApiConfig = true;
                        if (computedOptions.getRuntimeId().isNotPresent()) {
                            String runtimeId = (String) obj.get("runtimeId");
                            if (NutsBlankable.isBlank(runtimeId)) {
                                bLog.log(Level.CONFIG, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("{0} does not contain runtime-id", nutsApiConfigBootPath));
                            }
                            computedOptions.setRuntimeId(NutsId.of(runtimeId).get());
                        }
                        if (computedOptions.getJavaCommand().isNotPresent()) {
                            computedOptions.setJavaCommand((String) obj.get("javaCommand"));
                        }
                        if (computedOptions.getJavaOptions().isNotPresent()) {
                            computedOptions.setJavaOptions((String) obj.get("javaOptions"));
                        }
                    }
                } catch (UncheckedIOException | NutsIOException e) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("unable to read {0}", nutsApiConfigBootPath));
                }
            }
            if (!loadedApiConfig || computedOptions.getRuntimeId().isNotPresent() || computedOptions.getRuntimeBootDescriptor().isNotPresent() || computedOptions.getExtensionBootDescriptors().isNotPresent() || computedOptions.getBootRepositories().isNotPresent()) {

                //resolve runtime id
                if (computedOptions.getRuntimeId().isNotPresent()) {
                    NutsVersion apiVersion = computedOptions.getApiVersion().orNull();
                    //load from local lib folder
                    NutsId runtimeId = null;
                    if (!resetFlag && !computedOptions.getRecover().orElse(false)) {
                        runtimeId = NutsReservedMavenUtils.resolveLatestMavenId(NutsId.of(NutsConstants.Ids.NUTS_RUNTIME).get(), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, Collections.singletonList(NutsRepositoryLocation.of("nuts@" + computedOptions.getStoreLocation(NutsStoreLocation.LIB).get() + File.separatorChar + NutsConstants.Folders.ID)));
                    }
                    if (runtimeId == null) {
                        runtimeId = NutsReservedMavenUtils.resolveLatestMavenId(NutsId.of(NutsConstants.Ids.NUTS_RUNTIME).get(), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, resolveBootRuntimeRepositories(true));
                    }
                    if (runtimeId == null) {
                        bLog.log(Level.FINEST, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("unable to resolve latest runtime-id version (is connection ok?)"));
                    }
                    computedOptions.setRuntimeId(runtimeId);
                    computedOptions.setRuntimeBootDescriptor(null);
                }
                if (computedOptions.getRuntimeId().isNotPresent()) {
                    computedOptions.setRuntimeId((resolveDefaultRuntimeId(computedOptions.getApiVersion().orNull())));
                    bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("consider default runtime-id : {0}", computedOptions.getRuntimeId().orNull()));
                }
                NutsId runtimeIdObject = computedOptions.getRuntimeId().get();
                if (runtimeIdObject.getVersion().isBlank()) {
                    computedOptions.setRuntimeId(resolveDefaultRuntimeId(computedOptions.getApiVersion().orNull()));
                }

                //resolve runtime libraries
                if (computedOptions.getRuntimeBootDescriptor().isNotPresent()) {
                    Set<NutsId> loadedDeps = null;
                    NutsId rid = computedOptions.getRuntimeId().get();
                    Path nutsRuntimeCacheConfigPath = Paths.get(computedOptions.getStoreLocation(NutsStoreLocation.CONFIG).get() + File.separator + NutsConstants.Folders.ID).resolve(NutsReservedUtils.idToPath(bootApiId)).resolve(NutsConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
                    try {
                        boolean cacheLoaded = false;
                        if (!computedOptions.getRecover().orElse(false) && !resetFlag && NutsReservedIOUtils.isFileAccessible(nutsRuntimeCacheConfigPath, computedOptions.getExpireTime().orNull(), bLog)) {
                            try {
                                Map<String, Object> obj = NutsReservedJsonParser.parse(nutsRuntimeCacheConfigPath);
                                bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("loaded {0} file : {1}", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString()));
                                loadedDeps = NutsId.ofSet((String) obj.get("dependencies")).orElse(new LinkedHashSet<>());
                            } catch (Exception ex) {
                                bLog.log(Level.FINEST, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("unable to load {0} file : {1} : {2}", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString(), ex.toString()));
                                //ignore...
                            }
                            cacheLoaded = true;
                        }

                        if (!cacheLoaded || loadedDeps == null) {
                            loadedDeps = NutsReservedMavenUtils.loadDependenciesFromId(computedOptions.getRuntimeId().get(), bLog, resolveBootRuntimeRepositories(false));
                            bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofJstyle("detect runtime dependencies : {0}", loadedDeps));
                        }
                    } catch (Exception ex) {
                        bLog.log(Level.FINEST, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("unable to load {0} file : {1}", nutsRuntimeCacheConfigPath.getFileName(), ex.toString()));
                        //
                    }
                    if (loadedDeps == null) {
                        throw new NutsBootException(NutsMessage.ofCstyle("unable to load dependencies for %s", rid));
                    }
                    computedOptions.setRuntimeBootDescriptor(new DefaultNutsDescriptorBuilder().setId(computedOptions.getRuntimeId().get()).setDependencies(loadedDeps.stream().map(NutsId::toDependency).collect(Collectors.toList())));
                    Set<NutsRepositoryLocation> bootRepositories = resolveBootRuntimeRepositories(false);
                    if (bLog.isLoggable(Level.CONFIG)) {
                        if (bootRepositories.size() == 0) {
                            bLog.log(Level.CONFIG, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("workspace bootRepositories could not be resolved"));
                        } else if (bootRepositories.size() == 1) {
                            bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofJstyle("workspace bootRepositories resolved to : {0}", bootRepositories.toArray()[0]));
                        } else {
                            bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofPlain("workspace bootRepositories resolved to : "));
                            for (NutsRepositoryLocation repository : bootRepositories) {
                                bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofJstyle("    {0}", repository));
                            }
                        }
                    }
                    computedOptions.setBootRepositories(bootRepositories.stream().map(NutsRepositoryLocation::toString).collect(Collectors.joining(";")));
                }

                //resolve extension libraries
                if (computedOptions.getExtensionBootDescriptors().isNotPresent()) {
                    LinkedHashSet<String> excludedExtensions = new LinkedHashSet<>();
                    if (computedOptions.getExcludedExtensions().isPresent()) {
                        for (String excludedExtensionGroup : computedOptions.getExcludedExtensions().get()) {
                            for (String excludedExtension : NutsStringUtils.split(excludedExtensionGroup, ";,", true, true)) {
                                excludedExtensions.add(NutsId.of(excludedExtension).get().getShortName());
                            }
                        }
                    }
                    if (computedOptions.getExtensionsSet().isPresent()) {
                        List<NutsDescriptor> all = new ArrayList<>();
                        for (String extension : computedOptions.getExtensionsSet().orElseGet(Collections::emptySet)) {
                            NutsId eid = NutsId.of(extension).get();
                            if (!excludedExtensions.contains(eid.getShortName()) && !excludedExtensions.contains(eid.getArtifactId())) {
                                Path extensionFile = Paths.get(computedOptions.getStoreLocation(NutsStoreLocation.CONFIG).get() + File.separator + NutsConstants.Folders.ID).resolve(NutsReservedUtils.idToPath(bootApiId)).resolve(NutsConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
                                Set<NutsId> loadedDeps = null;
                                if (isLoadFromCache() && NutsReservedIOUtils.isFileAccessible(extensionFile, computedOptions.getExpireTime().orNull(), bLog)) {
                                    try {
                                        Properties obj = NutsReservedIOUtils.loadURLProperties(extensionFile, bLog);
                                        bLog.log(Level.CONFIG, NutsLoggerVerb.READ, NutsMessage.ofJstyle("loaded {0} file : {1}", extensionFile.getFileName(), extensionFile.toString()));
                                        loadedDeps = new LinkedHashSet<>(NutsId.ofList((String) obj.get("dependencies")).orElse(new ArrayList<>()));
                                    } catch (Exception ex) {
                                        bLog.log(Level.CONFIG, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("unable to load {0} file : {1} : {2}", extensionFile.getFileName(), extensionFile.toString(), ex.toString()));
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    loadedDeps = NutsReservedMavenUtils.loadDependenciesFromId(eid, bLog, resolveBootRuntimeRepositories(true));
                                }
                                all.add(new DefaultNutsDescriptorBuilder().setId(NutsId.of(extension).get()).setDependencies(loadedDeps.stream().map(NutsId::toDependency).collect(Collectors.toList())).build());
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
        return computedOptions.getTrace().orElse(true) && !computedOptions.getBot().orElse(false) && (computedOptions.getOutputFormat().orNull() == NutsContentType.PLAIN || computedOptions.getOutputFormat().isNotPresent());
    }

    private boolean isLoadFromCache() {
        return !computedOptions.getRecover().orElse(false) && !computedOptions.getReset().orElse(false);
    }

    public NutsSession openWorkspace() {
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsException(NutsMessage.ofCstyle("unable to open a distinct version : %s from nuts#%s", getRequirementsHelpString(true), Nuts.getVersion()));
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (computedOptions.getApplicationArguments().get().size() == 0 && computedOptions.getSkipBoot().orElse(false) && (computedOptions.getRecover().orElse(false) || computedOptions.getReset().orElse(false))) {
            if (isPlainTrace()) {
                bLog.log(Level.WARNING, NutsLoggerVerb.WARNING, NutsMessage.ofJstyle("workspace erased : {0}", computedOptions.getWorkspace()));
            }
            throw new NutsBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
        NutsReservedErrorInfoList errorList = new NutsReservedErrorInfoList();
        try {
            Path configFile = Paths.get(computedOptions.getWorkspace().get()).resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            if (computedOptions.getOpenMode().orNull() == NutsOpenMode.OPEN_OR_ERROR) {
                //add fail fast test!!
                if (!Files.isRegularFile(configFile)) {
                    throw new NutsWorkspaceNotFoundException(computedOptions.getWorkspace().orNull());
                }
            } else if (computedOptions.getOpenMode().orNull() == NutsOpenMode.CREATE_OR_ERROR) {
                if (Files.exists(configFile)) {
                    throw new NutsWorkspaceAlreadyExistsException(computedOptions.getWorkspace().orNull());
                }
            }
            if (computedOptions.getApiVersion().isBlank() || computedOptions.getRuntimeId().isBlank() || computedOptions.getBootRepositories().isBlank() || computedOptions.getRuntimeBootDescriptor().isNotPresent() || computedOptions.getExtensionBootDescriptors().isNotPresent()) {
                throw new NutsBootException(NutsMessage.ofPlain("invalid workspace state"));
            }
            boolean recover = computedOptions.getRecover().orElse(false) || computedOptions.getReset().orElse(false);

            List<NutsClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = computedOptions.getStoreLocation(NutsStoreLocation.LIB).get() + File.separator + NutsConstants.Folders.ID;

            NutsRepositoryLocation[] repositories =
                    NutsStringUtils.split(computedOptions.getBootRepositories().orNull(), "\n;", true, true)
                            .stream().map(NutsRepositoryLocation::of).toArray(NutsRepositoryLocation[]::new);

            NutsRepositoryLocation workspaceBootLibFolderRepo = NutsRepositoryLocation.of("nuts@" + workspaceBootLibFolder);
            computedOptions.setRuntimeBootDependencyNode(createClassLoaderNode(computedOptions.getRuntimeBootDescriptor().orNull(), repositories, workspaceBootLibFolderRepo, recover, errorList, true));

            for (NutsDescriptor nutsBootDescriptor : computedOptions.getExtensionBootDescriptors().orElseGet(ArrayList::new)) {
                deps.add(createClassLoaderNode(nutsBootDescriptor, repositories, workspaceBootLibFolderRepo, recover, errorList, false));
            }
            computedOptions.setExtensionBootDependencyNodes(deps);
            deps.add(0, computedOptions.getRuntimeBootDependencyNode().orNull());

            bootClassWorldURLs = NutsReservedClassLoaderUtils.resolveClassWorldURLs(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader(), bLog);
            workspaceClassLoader = /*bootClassWorldURLs.length == 0 ? getContextClassLoader() : */ new NutsReservedBootClassLoader(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader());
            computedOptions.setClassWorldLoader(workspaceClassLoader);
            if (bLog.isLoggable(Level.CONFIG)) {
                if (bootClassWorldURLs.length == 0) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofPlain("empty nuts class world. All dependencies are already loaded in classpath, most likely"));
                } else if (bootClassWorldURLs.length == 1) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofJstyle("resolve nuts class world to : {0} {1}", NutsReservedIOUtils.getURLDigest(bootClassWorldURLs[0], bLog), bootClassWorldURLs[0]));
                } else {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofPlain("resolve nuts class world is to : "));
                    for (URL u : bootClassWorldURLs) {
                        bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofJstyle("    {0} : {1}", NutsReservedIOUtils.getURLDigest(u, bLog), u));
                    }
                }
            }
            computedOptions.setClassWorldURLs(Arrays.asList(bootClassWorldURLs));
            bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofPlain("search for NutsBootWorkspaceFactory service implementations"));
            ServiceLoader<NutsBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsBootWorkspaceFactory.class, workspaceClassLoader);
            List<NutsBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NutsBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new NutsReservedBootWorkspaceFactoryComparator(computedOptions));
            if (bLog.isLoggable(Level.CONFIG)) {
                if (factories.isEmpty()) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofPlain("unable to detect NutsBootWorkspaceFactory service implementations"));
                } else if (factories.size() == 1) {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofJstyle("detect NutsBootWorkspaceFactory service implementation : {0}", factories.get(0).getClass().getName()));
                } else {
                    bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofPlain("detect NutsBootWorkspaceFactory service implementations are :"));
                    for (NutsBootWorkspaceFactory u : factories) {
                        bLog.log(Level.CONFIG, NutsLoggerVerb.SUCCESS, NutsMessage.ofJstyle("    {0}", u.getClass().getName()));
                    }
                }
            }
            NutsBootWorkspaceFactory factoryInstance;
            List<Throwable> exceptions = new ArrayList<>();
            for (NutsBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    if (bLog.isLoggable(Level.CONFIG)) {
                        bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofJstyle("create workspace using {0}", factoryInstance.getClass().getName()));
                    }
                    computedOptions.setBootWorkspaceFactory(factoryInstance);
                    nutsWorkspace = a.createWorkspace(computedOptions);
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    bLog.log(Level.SEVERE, NutsMessage.ofJstyle("unable to create workspace using factory {0}", a), ex);
                    // if the creation generates an error
                    // just stop
                    break;
                }
                if (nutsWorkspace != null) {
                    break;
                }
            }
            if (nutsWorkspace == null) {
                //should never happen
                bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("unable to load Workspace \"{0}\" from ClassPath :", computedOptions.getName()));
                for (URL url : bootClassWorldURLs) {
                    bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("\t {0}", NutsReservedUtils.formatURL(url)));
                }
                for (Throwable exception : exceptions) {
                    bLog.log(Level.SEVERE, NutsMessage.ofJstyle("{0}", exception), exception);
                }
                bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("unable to load Workspace Component from ClassPath : {0}", Arrays.asList(bootClassWorldURLs)));
                throw new NutsInvalidWorkspaceException(this.computedOptions.getWorkspace().orNull(), NutsMessage.ofCstyle("unable to load Workspace Component from ClassPath : %s%n  caused by:%n\t%s", Arrays.asList(bootClassWorldURLs), exceptions.stream().map(Throwable::toString).collect(Collectors.joining("\n\t"))));
            }
            return nutsWorkspace.createSession();
        } catch (NutsReadOnlyException | NutsCancelException | NutsNoSessionCancelException ex) {
            throw ex;
        } catch (UnsatisfiedLinkError | AbstractMethodError ex) {
            NutsMessage errorMessage = NutsMessage.ofCstyle(
                    "unable to boot nuts workspace because the installed binaries are incompatible with the current nuts bootstrap version %s\nusing '-N' command line flag should fix the problem", Nuts.getVersion()
            );
            errorList.insert(0, new NutsReservedErrorInfo(null, null, null, errorMessage + ": " + ex, ex));
            logError(bootClassWorldURLs, errorList);
            throw new NutsBootException(errorMessage, ex);
        } catch (Throwable ex) {
            NutsMessage message = NutsMessage.ofPlain("unable to locate valid nuts-runtime package");
            errorList.insert(0, new NutsReservedErrorInfo(null, null, null, message + " : " + ex, ex));
            logError(bootClassWorldURLs, errorList);
            if (ex instanceof NutsException) {
                throw (NutsException) ex;
            }
            if (ex instanceof NutsSecurityException) {
                throw (NutsSecurityException) ex;
            }
            if (ex instanceof NutsBootException) {
                throw (NutsBootException) ex;
            }
            throw new NutsBootException(message, ex);
        }
    }

    private ClassLoader getContextClassLoader() {
        return computedOptions.getClassLoaderSupplier().orElse(() -> Thread.currentThread().getContextClassLoader()).get();
    }

    private String getRunModeString() {
        if (this.getOptions().getReset().orElse(false)) {
            return "reset";
        } else if (this.getOptions().getRecover().orElse(false)) {
            return "recover";
        } else {
            return "exec";
        }
    }

    private void runCommandHelp() {
        NutsContentType f = computedOptions.getOutputFormat().orElse(NutsContentType.PLAIN);
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
        NutsContentType f = computedOptions.getOutputFormat().orElse(NutsContentType.PLAIN);
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
        NutsContentType f = computedOptions.getOutputFormat().orElse(NutsContentType.PLAIN);
        if (computedOptions.getDry().orElse(false)) {
            printDryCommand("version");
            return;
        }
        switch (f) {
            case JSON: {
                bLog.outln("{");
                bLog.outln("  \"version\": \"%s\",", Nuts.getVersion());
                bLog.outln("  \"digest\": \"%s\"", getApiDigest());
                bLog.outln("}");
                return;
            }
            case TSON: {
                bLog.outln("{");
                bLog.outln("  version: \"%s\",", Nuts.getVersion());
                bLog.outln("  digest: \"%s\"", getApiDigest());
                bLog.outln("}");
                return;
            }
            case YAML: {
                bLog.outln("version: %s", Nuts.getVersion());
                bLog.outln("digest: %s", getApiDigest());
                return;
            }
            case TREE: {
                bLog.outln("- version: %s", Nuts.getVersion());
                bLog.outln("- digest: %s", getApiDigest());
                return;
            }
            case TABLE: {
                bLog.outln("version      %s", Nuts.getVersion());
                bLog.outln("digest  %s", getApiDigest());
                return;
            }
            case XML: {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("<object>");
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "version", Nuts.getVersion());
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "digest", getApiDigest());
                bLog.outln("</object>");
                return;
            }
            case PROPS: {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("version=%s", Nuts.getVersion());
                bLog.outln("digest=%s", getApiDigest());
                bLog.outln("</object>");
                return;
            }
        }
        bLog.outln("%s", Nuts.getVersion());
    }

    public NutsSession runWorkspace() {
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
        NutsSession session = this.openWorkspace();
        NutsWorkspace workspace = session.getWorkspace();
        String message = "workspace started successfully";
        NutsWorkspaceBootOptions o = this.getOptions();
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NutsBootException(NutsMessage.ofCstyle("workspace not available to run : %s", NutsCommandLine.of(o.getApplicationArguments().get())));
        }

        session.setAppId(workspace.getApiId());
        if (nLog == null) {
            nLog = NutsLogger.of(NutsBootWorkspace.class, session);
            nLogSession = session;
        }
        NutsLoggerOp logOp = nLog.with().session(session).level(Level.CONFIG);
        logOp.verb(NutsLoggerVerb.SUCCESS).log(NutsMessage.ofJstyle("running workspace in {0} mode", getRunModeString()));
        if (workspace == null && o.getApplicationArguments().get().size() > 0) {
            switch (o.getApplicationArguments().get().get(0)) {
                case "version": {
                    runCommandVersion();
                    return session;
                }
                case "help": {
                    runCommandHelp();
                    return session;
                }
            }
        }
        if (o.getApplicationArguments().get().size() == 0) {
            if (o.getSkipWelcome().orElse(false)) {
                return session;
            }
            session.exec().addCommand("welcome").addExecutorOptions(o.getExecutorOptions().orNull()).setExecutionType(o.getExecutionType().orElse(NutsExecutionType.SPAWN)).setFailFast(true).setDry(computedOptions.getDry().orElse(false)).run();
        } else {
            session.exec().addCommand(o.getApplicationArguments().get()).addExecutorOptions(o.getExecutorOptions().orNull()).setExecutionType(o.getExecutionType().orElse(NutsExecutionType.SPAWN)).setFailFast(true).setDry(computedOptions.getDry().orElse(false)).run();
        }
        return session;
    }

    private void fallbackInstallActionUnavailable(String message) {
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain(message));
    }

    private void logError(URL[] bootClassWorldURLs, NutsReservedErrorInfoList ths) {
        String workspace = computedOptions.getWorkspace().orNull();
        Map<NutsStoreLocation, String> rbc_locations = computedOptions.getStoreLocations().orElse(Collections.emptyMap());
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("unable to bootstrap nuts (digest {0}):", getApiDigest()));
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("{0}", ths.list().get(0)));
        }
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("here after current environment info:"));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-boot-api-version            : {0}", computedOptions.getApiVersion().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-boot-runtime                : {0}", computedOptions.getRuntimeId().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-boot-repositories           : {0}", computedOptions.getBootRepositories().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  workspace-location               : {0}", NutsOptional.of(workspace).orElse("<default-location>")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-apps                  : {0}", rbc_locations.get(NutsStoreLocation.APPS)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-config                : {0}", rbc_locations.get(NutsStoreLocation.CONFIG)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-var                   : {0}", rbc_locations.get(NutsStoreLocation.VAR)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-log                   : {0}", rbc_locations.get(NutsStoreLocation.LOG)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-temp                  : {0}", rbc_locations.get(NutsStoreLocation.TEMP)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-cache                 : {0}", rbc_locations.get(NutsStoreLocation.CACHE)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-run                   : {0}", rbc_locations.get(NutsStoreLocation.RUN)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-lib                   : {0}", rbc_locations.get(NutsStoreLocation.LIB)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-strategy              : {0}", NutsReservedUtils.desc(computedOptions.getStoreLocationStrategy().orNull())));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-store-layout                : {0}", NutsReservedUtils.desc(computedOptions.getStoreLocationLayout().orNull())));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-boot-args                   : {0}", this.computedOptions.toCommandLine()));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-app-args                    : {0}", this.computedOptions.getApplicationArguments().get()));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  option-read-only                 : {0}", this.computedOptions.getReadOnly().orElse(false)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  option-trace                     : {0}", this.computedOptions.getTrace().orElse(false)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  option-progress                  : {0}", NutsReservedUtils.desc(this.computedOptions.getProgressOptions().orNull())));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  option-open-mode                 : {0}", NutsReservedUtils.desc(this.computedOptions.getOpenMode().orElse(NutsOpenMode.OPEN_OR_CREATE))));

        NutsClassLoaderNode rtn = this.computedOptions.getRuntimeBootDependencyNode().orNull();
        String rtHash = "";
        if (rtn != null) {
            rtHash = NutsReservedIOUtils.getURLDigest(rtn.getURL(), bLog);
        }
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-runtime-digest                : {0}", rtHash));

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-runtime-classpath           : {0}", "<none>"));
        } else {
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-runtime-hash                : {0}", "<none>"));
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  nuts-runtime-classpath           : {0}", NutsReservedUtils.formatURL(bootClassWorldURL)));
                } else {
                    bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("                                     {0}", NutsReservedUtils.formatURL(bootClassWorldURL)));
                }
            }
        }
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  java-version                     : {0}", System.getProperty("java.version")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  java-executable                  : {0}", NutsReservedUtils.resolveJavaCommand(null)));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  java-class-path                  : {0}", System.getProperty("java.class.path")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  java-library-path                : {0}", System.getProperty("java.library.path")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  os-name                          : {0}", System.getProperty("os.name")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  os-arch                          : {0}", System.getProperty("os.arch")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  os-version                       : {0}", System.getProperty("os.version")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  user-name                        : {0}", System.getProperty("user.name")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  user-home                        : {0}", System.getProperty("user.home")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("  user-dir                         : {0}", System.getProperty("user.dir")));
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain(""));
        if (this.computedOptions.getLogConfig().get().getLogTermLevel() == null || this.computedOptions.getLogConfig().get().getLogFileLevel().intValue() > Level.FINEST.intValue()) {
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("If the problem persists you may want to get more debug info by adding '--verbose' arguments."));
        }
        if (!this.computedOptions.getReset().orElse(false) && !this.computedOptions.getRecover().orElse(false) && this.computedOptions.getExpireTime().isNotPresent()) {
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments."));
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("Here is the proper command : "));
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("  java -jar nuts.jar --verbose --recover --expire [...]"));
        } else if (!this.computedOptions.getReset().orElse(false) && this.computedOptions.getRecover().orElse(false) && this.computedOptions.getExpireTime().isPresent()) {
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("You may also enable full reset mode to ignore existing configuration with '--reset' argument."));
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("ATTENTION: this will delete all your nuts configuration. Use it at your own risk."));
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("Here is the proper command : "));
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("  java -jar nuts.jar --verbose --reset [...]"));
        }
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("error stack trace is:"));
            for (NutsReservedErrorInfo th : ths.list()) {
                StringBuilder msg = new StringBuilder();
                List<Object> msgParams = new ArrayList<>();
                int index = 0;
                msg.append("[error]");
                if (th.getNutsId() != null) {
                    msg.append(" <id>={").append(index).append("}");
                    index++;
                    msgParams.add(th.getNutsId());
                }
                if (th.getRepository() != null) {
                    msg.append(" <repo>={").append(index).append("}");
                    index++;
                    msgParams.add(th.getRepository());
                }
                if (th.getUrl() != null) {
                    msg.append(" <url>={").append(index).append("}");
                    index++;
                    msgParams.add(th.getUrl());
                }
                if (th.getThrowable() != null) {
                    msg.append(" <error>={").append(index).append("}");
                    index++;
                    msgParams.add(th.getThrowable().toString());
                } else {
                    msg.append(" <error>={").append(index).append("}");
                    index++;
                    msgParams.add("unexpected error");
                }
                bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle(msg.toString(), msgParams.toArray()));
                bLog.log(Level.SEVERE, NutsMessage.ofPlain(th.toString()), th.getThrowable());
            }
        } else {
            bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("no stack trace is available."));
        }
        bLog.log(Level.SEVERE, NutsLoggerVerb.FAIL, NutsMessage.ofPlain("now exiting nuts, Bye!"));
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NutsBlankable.isBlank(computedOptions.getApiVersion().orNull())) {
            if (!unsatisfiedOnly || !computedOptions.getApiVersion().get().equals(Nuts.getVersion())) {
                req += 1;
            }
        }
        if (!unsatisfiedOnly || !NutsReservedUtils.isActualJavaCommand(computedOptions.getJavaCommand().orNull())) {
            req += 2;
        }
        if (!unsatisfiedOnly || !NutsReservedUtils.isActualJavaOptions(computedOptions.getJavaOptions().orNull())) {
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
            sb.append("nuts version ").append(NutsId.ofApi(computedOptions.getApiVersion().orNull()));
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

    private NutsClassLoaderNode createClassLoaderNode(NutsDescriptor descr, NutsRepositoryLocation[] repositories, NutsRepositoryLocation workspaceBootLibFolder, boolean recover, NutsReservedErrorInfoList errorList, boolean runtimeDep) throws MalformedURLException {
        NutsId id = descr.getId();
        List<NutsDependency> deps = descr.getDependencies();
        NutsClassLoaderNodeBuilder rt = new NutsClassLoaderNodeBuilder();
        String name = runtimeDep ? "runtime" : ("extension " + id.toString());
        File file = NutsReservedMavenUtils.getBootCacheJar(computedOptions.getRuntimeId().get(), repositories, workspaceBootLibFolder, !recover, name, computedOptions.getExpireTime().orNull(), errorList, computedOptions, pathExpansionConverter, bLog);
        rt.setId(id.toString());
        rt.setUrl(file.toURI().toURL());
        rt.setIncludedInClasspath(NutsReservedClassLoaderUtils.isLoadedClassPath(rt.getURL(), getContextClassLoader(), bLog));

        if (bLog.isLoggable(Level.CONFIG)) {
            String rtHash = "";
            if (computedOptions.getRuntimeId().isNotPresent()) {
                rtHash = NutsReservedIOUtils.getFileOrDirectoryDigest(file.toPath());
                if (rtHash == null) {
                    rtHash = "";
                }
            }
            bLog.log(Level.CONFIG, NutsLoggerVerb.INFO, NutsMessage.ofJstyle("detect {0} version {1} - digest {2} from {3}", name, id.toString(), rtHash, file));
        }

        for (NutsDependency s : deps) {
            NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
            if (NutsReservedUtils.isAcceptDependency(s, computedOptions)) {
                x.setId(s.toString()).setUrl(NutsReservedMavenUtils.getBootCacheJar(s.toId(), repositories, workspaceBootLibFolder, !recover, name + " dependency", computedOptions.getExpireTime().orNull(), errorList, computedOptions, pathExpansionConverter, bLog).toURI().toURL());
                x.setIncludedInClasspath(NutsReservedClassLoaderUtils.isLoadedClassPath(x.getURL(), getContextClassLoader(), bLog));
                rt.addDependency(x.build());
            }
        }
        return rt.build();
    }

    private NutsId resolveDefaultRuntimeId(NutsVersion apiVersion) {
        String sApiVersion = apiVersion.getValue();
        // check fo qualifier
        int q = sApiVersion.indexOf('-');
        if (q > 0) {
            return NutsId.ofRuntime((sApiVersion.substring(0, q) + ".0" + sApiVersion.substring(q))).get();
        }
        return NutsId.ofRuntime(sApiVersion + ".0").get();
    }
}
