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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NIOException;
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
public final class NBootWorkspace {

    private final Instant creationTime = Instant.now();
    private final NWorkspaceOptions userOptions;
    private final NReservedBootLog bLog;
    private final NBootOptionsBuilder computedOptions = new DefaultNBootOptionsBuilder();
    private final NReservedBootRepositoryDB repositoryDB;
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
                    return NReservedUtils.getHome(NStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()), computedOptions);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<NStoreLocation, String> s = computedOptions.getStoreLocations().orElse(Collections.emptyMap());
                    String v = s.get(NStoreLocation.parse(from).orNull());
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
    private Set<NRepositoryLocation> parsedBootRuntimeDependenciesRepositories;
    private Set<NRepositoryLocation> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private NLog nLog;
    private NSession nLogSession;
    private Scanner scanner;

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
        userOptions.setCommandLine(args, null);
        if (userOptions.getSkipErrors().orElse(false)) {
            StringBuilder errorMessage = new StringBuilder();
            for (NMsg s : userOptions.getErrors().orElseGet(Collections::emptyList)) {
                errorMessage.append(s).append("\n");
            }
            errorMessage.append("Try 'nuts --help' for more information.");
            bLog.log(Level.WARNING, NLogVerb.WARNING, NMsg.ofC("Error : %s", errorMessage));
        }
        this.userOptions = userOptions.readOnly();
        repositoryDB = new NReservedBootRepositoryDB(nLog);
        this.postInit();
    }

    public NBootWorkspace(NWorkspaceOptions userOptions) {
        if (userOptions == null) {
            userOptions = DefaultNWorkspaceOptions.BLANK;
        }
        this.bLog = new NReservedBootLog(new NWorkspaceTerminalOptions(userOptions.getStdin().orNull(), userOptions.getStdout().orNull(), userOptions.getStderr().orNull()));
        this.userOptions = userOptions.readOnly();
        repositoryDB = new NReservedBootRepositoryDB(nLog);
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
        this.computedOptions.setIsolationLevel(this.computedOptions.getIsolationLevel().orElse(NIsolationLevel.SYSTEM));
        this.computedOptions.setTransitive(this.computedOptions.getTransitive().orElse(true));
        this.computedOptions.setTrace(this.computedOptions.getTrace().orElse(true));
        this.computedOptions.setCached(this.computedOptions.getCached().orElse(true));
        this.computedOptions.setIndexed(this.computedOptions.getIndexed().orElse(true));
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
        if(this.computedOptions.getApplicationArguments().isEmpty()){
            this.computedOptions.setApplicationArguments(new ArrayList<>());
        }
        this.bLog.setOptions(this.computedOptions);
    }

    private static void revalidateLocations(NBootOptionsBuilder bootOptions, String workspaceName, boolean immediateLocation, NIsolationLevel sandboxMode) {
        if (NBlankable.isBlank(bootOptions.getName())) {
            bootOptions.setName(workspaceName);
        }
        boolean global = bootOptions.getGlobal().orElse(false);
        if (sandboxMode.compareTo(NIsolationLevel.SANDBOX) >= 0) {
            bootOptions.setStoreLocationStrategy(NStoreLocationStrategy.STANDALONE);
            bootOptions.setRepositoryStoreLocationStrategy(NStoreLocationStrategy.EXPLODED);
            global = false;
        } else {
            if (bootOptions.getStoreLocationStrategy().isNotPresent()) {
                bootOptions.setStoreLocationStrategy(immediateLocation ? NStoreLocationStrategy.EXPLODED : NStoreLocationStrategy.STANDALONE);
            }
            if (bootOptions.getRepositoryStoreLocationStrategy().isNotPresent()) {
                bootOptions.setRepositoryStoreLocationStrategy(NStoreLocationStrategy.EXPLODED);
            }
        }
        Map<NStoreLocation, String> storeLocations = NPlatformUtils.buildLocations(bootOptions.getStoreLocationLayout().orNull(), bootOptions.getStoreLocationStrategy().orNull(), bootOptions.getStoreLocations().orNull(), bootOptions.getHomeLocations().orNull(), global, bootOptions.getWorkspace().orNull(), null//no session!
        );
        if (new HashSet<>(storeLocations.values()).size() != storeLocations.size()) {
            Map<String, List<NStoreLocation>> conflicts = new LinkedHashMap<>();
            for (Map.Entry<NStoreLocation, String> e : storeLocations.entrySet()) {
                conflicts.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
            }
            StringBuilder error = new StringBuilder();
            error.append("invalid store locations. Two or more stores point to the same location:");
            List<Object> errorParams = new ArrayList<>();
            for (Map.Entry<String, List<NStoreLocation>> e : conflicts.entrySet()) {
                List<NStoreLocation> ev = e.getValue();
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
        static final String apiDigest = NApiUtils.resolveNutsIdDigestOrError();
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
                nLog.with().session(nLogSession).level(Level.FINE).verb(NLogVerb.START).log(NMsg.ofJ("start new process : {0}", NCmdLine.of(processCommandLine)));
            } else {
                bLog.log(Level.FINE, NLogVerb.START, NMsg.ofJ("start new process : {0}", NCmdLine.of(processCommandLine)));
            }
            result = new ProcessBuilder(processCommandLine).inheritIO().start().waitFor();
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
        if (dependencies) {
            if (parsedBootRuntimeDependenciesRepositories != null) {
                return parsedBootRuntimeDependenciesRepositories;
            }
            bLog.log(Level.FINE, NLogVerb.START, NMsg.ofJ("resolve boot repositories to load nuts-runtime dependencies from options : {0} and config: {1}", computedOptions.getRepositories().orElseGet(Collections::emptyList).toString(), computedOptions.getBootRepositories().ifBlankEmpty().orElse("[]")));
        } else {
            if (parsedBootRuntimeRepositories != null) {
                return parsedBootRuntimeRepositories;
            }
            bLog.log(Level.FINE, NLogVerb.START, NMsg.ofJ("resolve boot repositories to load nuts-runtime from options : {0} and config: {1}", computedOptions.getRepositories().orElseGet(Collections::emptyList).toString(), computedOptions.getBootRepositories().ifBlankEmpty().orElse("[]")));
        }
        NRepositorySelectorList bootRepositories = NRepositorySelectorList.ofAll(computedOptions.getRepositories().orNull(), repositoryDB, null);
        NRepositorySelector[] old = NRepositorySelectorList.ofAll(Arrays.asList(computedOptions.getBootRepositories().orNull()), repositoryDB, null).toArray();
        NRepositoryLocation[] result;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositories.resolve(NReservedMavenUtils.loadAllMavenRepos(nLog).toArray(new NRepositoryLocation[0]), repositoryDB);
        } else {
            result = bootRepositories.resolve(Arrays.stream(old).map(x -> NRepositoryLocation.of(x.getName(), x.getUrl())).toArray(NRepositoryLocation[]::new), repositoryDB);
        }
        Set<NRepositoryLocation> rr = Arrays.stream(result).collect(Collectors.toCollection(LinkedHashSet::new));
        if (dependencies) {
            parsedBootRuntimeDependenciesRepositories = rr;
        } else {
            parsedBootRuntimeRepositories = rr;
        }
        return rr;
    }

    public String[] createProcessCommandLine() {
        prepareWorkspace();
        bLog.log(Level.FINE, NLogVerb.START, NMsg.ofJ("running version {0}.  {1}", computedOptions.getApiVersion().orNull(), getRequirementsHelpString(true)));
        String defaultWorkspaceLibFolder = computedOptions.getStoreLocation(NStoreLocation.LIB).orNull();
        List<NRepositoryLocation> repos = new ArrayList<>();
        repos.add(NRepositoryLocation.of("nuts@" + defaultWorkspaceLibFolder));
        Collection<NRepositoryLocation> bootRepositories = resolveBootRuntimeRepositories(true);
        repos.addAll(bootRepositories);
        NReservedErrorInfoList errorList = new NReservedErrorInfoList();
        File file = NReservedMavenUtils.resolveOrDownloadJar(NId.ofApi(computedOptions.getApiVersion().orNull()).get(), repos.toArray(new NRepositoryLocation[0]), NRepositoryLocation.of("nuts@" + computedOptions.getStoreLocation(NStoreLocation.LIB).get() + File.separator + NConstants.Folders.ID), bLog, false, computedOptions.getExpireTime().orNull(), errorList);
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
        cmd.addAll(computedOptions.toCommandLine(new NWorkspaceOptionsConfig().setCompact(true).setApiVersion(computedOptions.getApiVersion().orNull())).toStringList());
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            bLog.log(Level.FINE, NLogVerb.START, NMsg.ofJ("[exec] {0}", sb));
        }
        return cmd.toArray(new String[0]);
    }

    public NBootOptionsBuilder getOptions() {
        return computedOptions;
    }

    @SuppressWarnings("unchecked")
    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {
            preparedWorkspace = true;
            NIsolationLevel isolationMode = computedOptions.getIsolationLevel().orElse(NIsolationLevel.SYSTEM);
            if (bLog.isLoggable(Level.CONFIG)) {
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("bootstrap Nuts version {0}{1}- digest {1}...", Nuts.getVersion(), isolationMode == NIsolationLevel.SYSTEM ? "" : isolationMode == NIsolationLevel.USER ? " (user mode)" : isolationMode == NIsolationLevel.CONFINED ? " (confined mode)" : isolationMode == NIsolationLevel.SANDBOX ? " (sandbox mode)" : " (unsupported mode)", getApiDigest()));
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofPlain("boot-class-path:"));
                for (String s : NStringUtils.split(System.getProperty("java.class.path"), File.pathSeparator, true, true)) {
                    bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("                  {0}", s));
                }
                ClassLoader thisClassClassLoader = getClass().getClassLoader();
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("class-loader: {0}", thisClassClassLoader));
                for (URL url : NReservedClassLoaderUtils.resolveClasspathURLs(thisClassClassLoader, false)) {
                    bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("                 {0}", url));
                }
                ClassLoader tctxloader = Thread.currentThread().getContextClassLoader();
                if (tctxloader != thisClassClassLoader) {
                    bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("thread-class-loader: {0}", tctxloader));
                    for (URL url : NReservedClassLoaderUtils.resolveClasspathURLs(tctxloader, false)) {
                        bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("                 {0}", url));
                    }
                }
                ClassLoader contextClassLoader = getContextClassLoader();
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("ctx-class-loader: {0}", contextClassLoader));
                if (contextClassLoader != null) {
                    for (URL url : NReservedClassLoaderUtils.resolveClasspathURLs(contextClassLoader, false)) {
                        bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("                 {0}", url));
                    }
                }
                bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("system-properties:", contextClassLoader));
                Map<String, String> m = (Map) System.getProperties();
                int max = m.keySet().stream().mapToInt(String::length).max().getAsInt();
                for (String k : new TreeSet<String>(m.keySet())) {
                    bLog.log(Level.CONFIG, NLogVerb.START, NMsg.ofJ("    {0} = {1}", NStringUtils.formatAlign(k, max, NPositionType.FIRST), NStringUtils.formatStringLiteral(m.get(k), NStringUtils.QuoteType.DOUBLE)));
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
                if (computedOptions.getGlobal().orElse(false)) {
                    throw new NBootException(NMsg.ofNtf("you cannot specify option '--global' in sandbox mode"));
                }
                if (computedOptions.getWorkspace().ifBlankEmpty().isPresent()) {
                    throw new NBootException(NMsg.ofNtf("you cannot specify '--workspace' in sandbox mode"));
                }
                if (computedOptions.getStoreLocationStrategy().orElse(NStoreLocationStrategy.STANDALONE) != NStoreLocationStrategy.STANDALONE) {
                    throw new NBootException(NMsg.ofNtf("you cannot specify '--exploded' in sandbox mode"));
                }
                if (computedOptions.getGlobal().orElse(false)) {
                    throw new NBootException(NMsg.ofNtf("you cannot specify '--global' in sandbox mode"));
                }
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            } else {
                if (isolationMode.compareTo(NIsolationLevel.SYSTEM) > 0 && userOptions.getGlobal().orElse(false)) {
                    if (userOptions.getReset().orElse(false)) {
                        throw new NBootException(NMsg.ofC("invalid option 'global' in %s mode", isolationMode));
                    }
                }
                if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                    //this is a protocol based workspace
                    //String protocol=ws.substring(0,ws.indexOf("://"));
                    workspaceName = "remote-bootstrap";
                    lastNutsWorkspaceJsonConfigPath = NPlatformUtils.getWorkspaceLocation(null, computedOptions.getGlobal().orElse(false), NReservedUtils.resolveValidWorkspaceName(workspaceName));
                    lastConfigLoaded = NReservedBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                    immediateLocation = true;

                } else {
                    immediateLocation = NReservedUtils.isValidWorkspaceName(_ws);
                    int maxDepth = 36;
                    for (int i = 0; i < maxDepth; i++) {
                        lastNutsWorkspaceJsonConfigPath = NReservedUtils.isValidWorkspaceName(_ws) ? NPlatformUtils.getWorkspaceLocation(null, computedOptions.getGlobal().orElse(false), NReservedUtils.resolveValidWorkspaceName(_ws)) : NReservedIOUtils.getAbsolutePath(_ws);

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
                curr.setExtensionsSet(NReservedCollectionUtils.nonNullSet(lastConfigLoaded.getExtensionsSet().orNull()));
                curr.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy().orNull());
                curr.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy().orNull());
                curr.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout().orNull());
                curr.setStoreLocations(NReservedCollectionUtils.nonNullMap(lastConfigLoaded.getStoreLocations().orNull()));
                curr.setHomeLocations(NReservedCollectionUtils.nonNullMap(lastConfigLoaded.getHomeLocations().orNull()));
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
                        countDeleted = NReservedUtils.deleteStoreLocations(lastWorkspaceOptions, getOptions(), true, bLog, NStoreLocation.values(), () -> scanner.nextLine());
                        NReservedUtils.ndiUndo(bLog);
                    }
                } else {
                    if (dryFlag) {
                        bLog.log(Level.INFO, NLogVerb.DEBUG, NMsg.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, NLogVerb.WARNING, NMsg.ofPlain("reset workspace"));
                        countDeleted = NReservedUtils.deleteStoreLocations(computedOptions, getOptions(), true, bLog, NStoreLocation.values(), () -> scanner.nextLine());
                        NReservedUtils.ndiUndo(bLog);
                    }
                }
            } else if (computedOptions.getRecover().orElse(false)) {
                if (dryFlag) {
                    bLog.log(Level.INFO, NLogVerb.DEBUG, NMsg.ofPlain("[dry] [recover] delete CACHE/TEMP workspace folders"));
                } else {
                    bLog.log(Level.CONFIG, NLogVerb.WARNING, NMsg.ofPlain("recover workspace."));
                    List<Object> folders = new ArrayList<>();
                    folders.add(NStoreLocation.CACHE);
                    folders.add(NStoreLocation.TEMP);
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = NReservedUtils.getStoreLocationPath(computedOptions, NStoreLocation.LIB);
                    if (p != null) {
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts"));
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = NReservedUtils.deleteStoreLocations(computedOptions, getOptions(), false, bLog, folders.toArray(), () -> scanner.nextLine());
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
                    computedOptions.setStoreLocationLayout(lastWorkspaceOptions.getStoreLocationLayout().orElse(NOsFamily.getCurrent()));
                } else {
                    computedOptions.setHomeLocations(Collections.emptyMap());
                }
            }

            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (computedOptions.getApplicationArguments().get().size() == 0 && computedOptions.getSkipBoot().orElse(false) && (computedOptions.getRecover().orElse(false) || resetFlag)) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        bLog.log(Level.WARNING, NLogVerb.WARNING, NMsg.ofJ("workspace erased : {0}", computedOptions.getWorkspace()));
                    } else {
                        bLog.log(Level.WARNING, NLogVerb.WARNING, NMsg.ofJ("workspace is not erased because it does not exist : {0}", computedOptions.getWorkspace()));
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
            Path nutsApiConfigBootPath = Paths.get(computedOptions.getStoreLocation(NStoreLocation.CONFIG).get() + File.separator + NConstants.Folders.ID).resolve(NReservedUtils.idToPath(bootApiId)).resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && NReservedIOUtils.isFileAccessible(nutsApiConfigBootPath, computedOptions.getExpireTime().orNull(), bLog)) {
                try {
                    Map<String, Object> obj = NReservedJsonParser.parse(nutsApiConfigBootPath);
                    if (!obj.isEmpty()) {
                        bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofJ("loaded {0} file : {1}", nutsApiConfigBootPath.getFileName(), nutsApiConfigBootPath.toString()));
                        loadedApiConfig = true;
                        if (computedOptions.getRuntimeId().isNotPresent()) {
                            String runtimeId = (String) obj.get("runtimeId");
                            if (NBlankable.isBlank(runtimeId)) {
                                bLog.log(Level.CONFIG, NLogVerb.FAIL, NMsg.ofJ("{0} does not contain runtime-id", nutsApiConfigBootPath));
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
                    bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofJ("unable to read {0}", nutsApiConfigBootPath));
                }
            }
            if (!loadedApiConfig || computedOptions.getRuntimeId().isNotPresent() || computedOptions.getRuntimeBootDescriptor().isNotPresent() || computedOptions.getExtensionBootDescriptors().isNotPresent() || computedOptions.getBootRepositories().isNotPresent()) {

                //resolve runtime id
                if (computedOptions.getRuntimeId().isNotPresent()) {
                    NVersion apiVersion = computedOptions.getApiVersion().orNull();
                    //load from local lib folder
                    NId runtimeId = null;
                    if (!resetFlag && !computedOptions.getRecover().orElse(false)) {
                        runtimeId = NReservedMavenUtils.resolveLatestMavenId(NId.of(NConstants.Ids.NUTS_RUNTIME).get(), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, Collections.singletonList(NRepositoryLocation.of("nuts@" + computedOptions.getStoreLocation(NStoreLocation.LIB).get() + File.separatorChar + NConstants.Folders.ID)), computedOptions);
                    }
                    if (runtimeId == null) {
                        runtimeId = NReservedMavenUtils.resolveLatestMavenId(NId.of(NConstants.Ids.NUTS_RUNTIME).get(), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, resolveBootRuntimeRepositories(true), computedOptions);
                    }
                    if (runtimeId == null) {
                        bLog.log(Level.FINEST, NLogVerb.FAIL, NMsg.ofPlain("unable to resolve latest runtime-id version (is connection ok?)"));
                    }
                    computedOptions.setRuntimeId(runtimeId);
                    computedOptions.setRuntimeBootDescriptor(null);
                }
                if (computedOptions.getRuntimeId().isNotPresent()) {
                    computedOptions.setRuntimeId((resolveDefaultRuntimeId(computedOptions.getApiVersion().orNull())));
                    bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofJ("consider default runtime-id : {0}", computedOptions.getRuntimeId().orNull()));
                }
                NId runtimeIdObject = computedOptions.getRuntimeId().get();
                if (runtimeIdObject.getVersion().isBlank()) {
                    computedOptions.setRuntimeId(resolveDefaultRuntimeId(computedOptions.getApiVersion().orNull()));
                }

                //resolve runtime libraries
                if (computedOptions.getRuntimeBootDescriptor().isNotPresent()) {
                    Set<NId> loadedDeps = null;
                    NId rid = computedOptions.getRuntimeId().get();
                    Path nutsRuntimeCacheConfigPath = Paths.get(computedOptions.getStoreLocation(NStoreLocation.CONFIG).get() + File.separator + NConstants.Folders.ID).resolve(NReservedUtils.idToPath(bootApiId)).resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
                    try {
                        boolean cacheLoaded = false;
                        if (!computedOptions.getRecover().orElse(false) && !resetFlag && NReservedIOUtils.isFileAccessible(nutsRuntimeCacheConfigPath, computedOptions.getExpireTime().orNull(), bLog)) {
                            try {
                                Map<String, Object> obj = NReservedJsonParser.parse(nutsRuntimeCacheConfigPath);
                                bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofJ("loaded {0} file : {1}", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString()));
                                loadedDeps = NId.ofSet((String) obj.get("dependencies")).orElse(new LinkedHashSet<>());
                            } catch (Exception ex) {
                                bLog.log(Level.FINEST, NLogVerb.FAIL, NMsg.ofJ("unable to load {0} file : {1} : {2}", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString(), ex.toString()));
                                //ignore...
                            }
                            cacheLoaded = true;
                        }

                        if (!cacheLoaded || loadedDeps == null) {
                            loadedDeps = NReservedMavenUtils.loadDependenciesFromId(computedOptions.getRuntimeId().get(), bLog, resolveBootRuntimeRepositories(false));
                            bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofJ("detect runtime dependencies : {0}", loadedDeps));
                        }
                    } catch (Exception ex) {
                        bLog.log(Level.FINEST, NLogVerb.FAIL, NMsg.ofJ("unable to load {0} file : {1}", nutsRuntimeCacheConfigPath.getFileName(), ex.toString()));
                        //
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
                            bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofJ("workspace bootRepositories resolved to : {0}", bootRepositories.toArray()[0]));
                        } else {
                            bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofPlain("workspace bootRepositories resolved to : "));
                            for (NRepositoryLocation repository : bootRepositories) {
                                bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofJ("    {0}", repository));
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
                                Path extensionFile = Paths.get(computedOptions.getStoreLocation(NStoreLocation.CONFIG).get() + File.separator + NConstants.Folders.ID).resolve(NReservedUtils.idToPath(bootApiId)).resolve(NConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
                                Set<NId> loadedDeps = null;
                                if (isLoadFromCache() && NReservedIOUtils.isFileAccessible(extensionFile, computedOptions.getExpireTime().orNull(), bLog)) {
                                    try {
                                        Properties obj = NReservedIOUtils.loadURLProperties(extensionFile, bLog);
                                        bLog.log(Level.CONFIG, NLogVerb.READ, NMsg.ofJ("loaded {0} file : {1}", extensionFile.getFileName(), extensionFile.toString()));
                                        loadedDeps = new LinkedHashSet<>(NId.ofList((String) obj.get("dependencies")).orElse(new ArrayList<>()));
                                    } catch (Exception ex) {
                                        bLog.log(Level.CONFIG, NLogVerb.FAIL, NMsg.ofJ("unable to load {0} file : {1} : {2}", extensionFile.getFileName(), extensionFile.toString(), ex.toString()));
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    loadedDeps = NReservedMavenUtils.loadDependenciesFromId(eid, bLog, resolveBootRuntimeRepositories(true));
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

    public NSession openWorkspace() {
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NUnsatisfiedRequirementsException(NMsg.ofC("unable to open a distinct version : %s from nuts#%s", getRequirementsHelpString(true), Nuts.getVersion()));
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (computedOptions.getApplicationArguments().get().size() == 0 && computedOptions.getSkipBoot().orElse(false) && (computedOptions.getRecover().orElse(false) || computedOptions.getReset().orElse(false))) {
            if (isPlainTrace()) {
                bLog.log(Level.WARNING, NLogVerb.WARNING, NMsg.ofJ("workspace erased : {0}", computedOptions.getWorkspace()));
            }
            throw new NBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NWorkspace nWorkspace = null;
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
            if (computedOptions.getApiVersion().isBlank() || computedOptions.getRuntimeId().isBlank() || computedOptions.getBootRepositories().isBlank() || computedOptions.getRuntimeBootDescriptor().isNotPresent() || computedOptions.getExtensionBootDescriptors().isNotPresent()) {
                throw new NBootException(NMsg.ofPlain("invalid workspace state"));
            }
            boolean recover = computedOptions.getRecover().orElse(false) || computedOptions.getReset().orElse(false);

            List<NClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = computedOptions.getStoreLocation(NStoreLocation.LIB).get() + File.separator + NConstants.Folders.ID;

            NRepositoryLocation[] repositories = NStringUtils.split(computedOptions.getBootRepositories().orNull(), "\n;", true, true).stream().map(NRepositoryLocation::of).toArray(NRepositoryLocation[]::new);

            NRepositoryLocation workspaceBootLibFolderRepo = NRepositoryLocation.of("nuts@" + workspaceBootLibFolder);
            computedOptions.setRuntimeBootDependencyNode(createClassLoaderNode(computedOptions.getRuntimeBootDescriptor().orNull(), repositories, workspaceBootLibFolderRepo, recover, errorList, true));

            for (NDescriptor nutsBootDescriptor : computedOptions.getExtensionBootDescriptors().orElseGet(ArrayList::new)) {
                deps.add(createClassLoaderNode(nutsBootDescriptor, repositories, workspaceBootLibFolderRepo, recover, errorList, false));
            }
            computedOptions.setExtensionBootDependencyNodes(deps);
            deps.add(0, computedOptions.getRuntimeBootDependencyNode().orNull());

            bootClassWorldURLs = NReservedClassLoaderUtils.resolveClassWorldURLs(deps.toArray(new NClassLoaderNode[0]), getContextClassLoader(), bLog);
            workspaceClassLoader = /*bootClassWorldURLs.length == 0 ? getContextClassLoader() : */ new NReservedBootClassLoader(deps.toArray(new NClassLoaderNode[0]), getContextClassLoader());
            computedOptions.setClassWorldLoader(workspaceClassLoader);
            if (bLog.isLoggable(Level.CONFIG)) {
                if (bootClassWorldURLs.length == 0) {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofPlain("empty nuts class world. All dependencies are already loaded in classpath, most likely"));
                } else if (bootClassWorldURLs.length == 1) {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofJ("resolve nuts class world to : {0} {1}", NReservedIOUtils.getURLDigest(bootClassWorldURLs[0], bLog), bootClassWorldURLs[0]));
                } else {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofPlain("resolve nuts class world is to : "));
                    for (URL u : bootClassWorldURLs) {
                        bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofJ("    {0} : {1}", NReservedIOUtils.getURLDigest(u, bLog), u));
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
                if (factories.isEmpty()) {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofPlain("unable to detect NutsBootWorkspaceFactory service implementations"));
                } else if (factories.size() == 1) {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofJ("detect NutsBootWorkspaceFactory service implementation : {0}", factories.get(0).getClass().getName()));
                } else {
                    bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofPlain("detect NutsBootWorkspaceFactory service implementations are :"));
                    for (NBootWorkspaceFactory u : factories) {
                        bLog.log(Level.CONFIG, NLogVerb.SUCCESS, NMsg.ofJ("    {0}", u.getClass().getName()));
                    }
                }
            }
            NBootWorkspaceFactory factoryInstance;
            List<Throwable> exceptions = new ArrayList<>();
            for (NBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    if (bLog.isLoggable(Level.CONFIG)) {
                        bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofJ("create workspace using {0}", factoryInstance.getClass().getName()));
                    }
                    computedOptions.setBootWorkspaceFactory(factoryInstance);
                    nWorkspace = a.createWorkspace(computedOptions);
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("unable to create workspace using factory {0}", a), ex);
                    // if the creation generates an error
                    // just stop
                    break;
                }
                if (nWorkspace != null) {
                    break;
                }
            }
            if (nWorkspace == null) {
                //should never happen
                bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("unable to load Workspace \"{0}\" from ClassPath :", computedOptions.getName()));
                for (URL url : bootClassWorldURLs) {
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("\t {0}", NReservedUtils.formatURL(url)));
                }
                for (Throwable exception : exceptions) {
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("{0}", exception), exception);
                }
                bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("unable to load Workspace Component from ClassPath : {0}", Arrays.asList(bootClassWorldURLs)));
                throw new NInvalidWorkspaceException(this.computedOptions.getWorkspace().orNull(), NMsg.ofC("unable to load Workspace Component from ClassPath : %s%n  caused by:%n\t%s", Arrays.asList(bootClassWorldURLs), exceptions.stream().map(Throwable::toString).collect(Collectors.joining("\n\t"))));
            }
            return nWorkspace.createSession();
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

    public NSession runWorkspace() {
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
        NSession session = this.openWorkspace();
        NWorkspace workspace = session.getWorkspace();
        String message = "workspace started successfully";
        NBootOptions o = this.getOptions();
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NBootException(NMsg.ofC("workspace not available to run : %s", NCmdLine.of(o.getApplicationArguments().get())));
        }

        session.setAppId(workspace.getApiId());
        if (nLog == null) {
            nLog = NLog.of(NBootWorkspace.class, session);
            nLogSession = session;
        }
        NLogOp logOp = nLog.with().session(session).level(Level.CONFIG);
        logOp.verb(NLogVerb.SUCCESS).log(NMsg.ofJ("running workspace in {0} mode", getRunModeString()));
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
            NExecCommand.of(session.setDry(computedOptions.getDry().orElse(false))).addCommand("welcome").addExecutorOptions(o.getExecutorOptions().orNull()).setExecutionType(o.getExecutionType().orElse(NExecutionType.SPAWN)).setFailFast(true).run();
        } else {
            NExecCommand.of(session.setDry(computedOptions.getDry().orElse(false))).addCommand(o.getApplicationArguments().get()).addExecutorOptions(o.getExecutorOptions().orNull()).setExecutionType(o.getExecutionType().orElse(NExecutionType.SPAWN)).setFailFast(true).run();
        }
        return session;
    }

    private void fallbackInstallActionUnavailable(String message) {
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain(message));
    }

    private void logError(URL[] bootClassWorldURLs, NReservedErrorInfoList ths) {
        String workspace = computedOptions.getWorkspace().orNull();
        Map<NStoreLocation, String> rbc_locations = computedOptions.getStoreLocations().orElse(Collections.emptyMap());
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("unable to bootstrap nuts (digest {0}):", getApiDigest()));
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("{0}", ths.list().get(0)));
        }
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofPlain("here after current environment info:"));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-boot-api-version            : {0}", computedOptions.getApiVersion().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-boot-runtime                : {0}", computedOptions.getRuntimeId().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-boot-repositories           : {0}", computedOptions.getBootRepositories().map(Object::toString).orElse("<?> Not Found!")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  workspace-location               : {0}", NOptional.of(workspace).orElse("<default-location>")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-apps                  : {0}", rbc_locations.get(NStoreLocation.APPS)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-config                : {0}", rbc_locations.get(NStoreLocation.CONFIG)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-var                   : {0}", rbc_locations.get(NStoreLocation.VAR)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-log                   : {0}", rbc_locations.get(NStoreLocation.LOG)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-temp                  : {0}", rbc_locations.get(NStoreLocation.TEMP)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-cache                 : {0}", rbc_locations.get(NStoreLocation.CACHE)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-run                   : {0}", rbc_locations.get(NStoreLocation.RUN)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-lib                   : {0}", rbc_locations.get(NStoreLocation.LIB)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-strategy              : {0}", NReservedUtils.desc(computedOptions.getStoreLocationStrategy().orNull())));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-store-layout                : {0}", NReservedUtils.desc(computedOptions.getStoreLocationLayout().orNull())));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-boot-args                   : {0}", this.computedOptions.toCommandLine()));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-app-args                    : {0}", this.computedOptions.getApplicationArguments().get()));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  option-read-only                 : {0}", this.computedOptions.getReadOnly().orElse(false)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  option-trace                     : {0}", this.computedOptions.getTrace().orElse(false)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  option-progress                  : {0}", NReservedUtils.desc(this.computedOptions.getProgressOptions().orNull())));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  option-open-mode                 : {0}", NReservedUtils.desc(this.computedOptions.getOpenMode().orElse(NOpenMode.OPEN_OR_CREATE))));

        NClassLoaderNode rtn = this.computedOptions.getRuntimeBootDependencyNode().orNull();
        String rtHash = "";
        if (rtn != null) {
            rtHash = NReservedIOUtils.getURLDigest(rtn.getURL(), bLog);
        }
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-runtime-digest                : {0}", rtHash));

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-runtime-classpath           : {0}", "<none>"));
        } else {
            bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-runtime-hash                : {0}", "<none>"));
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  nuts-runtime-classpath           : {0}", NReservedUtils.formatURL(bootClassWorldURL)));
                } else {
                    bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("                                     {0}", NReservedUtils.formatURL(bootClassWorldURL)));
                }
            }
        }
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  java-version                     : {0}", System.getProperty("java.version")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  java-executable                  : {0}", NReservedUtils.resolveJavaCommand(null)));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  java-class-path                  : {0}", System.getProperty("java.class.path")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  java-library-path                : {0}", System.getProperty("java.library.path")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  os-name                          : {0}", System.getProperty("os.name")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  os-arch                          : {0}", System.getProperty("os.arch")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  os-version                       : {0}", System.getProperty("os.version")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  user-name                        : {0}", System.getProperty("user.name")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  user-home                        : {0}", System.getProperty("user.home")));
        bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ("  user-dir                         : {0}", System.getProperty("user.dir")));
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
                bLog.log(Level.SEVERE, NLogVerb.FAIL, NMsg.ofJ(msg.toString(), msgParams.toArray()));
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
        File file = NReservedMavenUtils.getBootCacheJar(computedOptions.getRuntimeId().get(), repositories, workspaceBootLibFolder, !recover, name, computedOptions.getExpireTime().orNull(), errorList, computedOptions, pathExpansionConverter, bLog);
        rt.setId(id.toString());
        rt.setUrl(file.toURI().toURL());
        rt.setIncludedInClasspath(NReservedClassLoaderUtils.isLoadedClassPath(rt.getURL(), getContextClassLoader(), bLog));

        if (bLog.isLoggable(Level.CONFIG)) {
            String rtHash = "";
            if (computedOptions.getRuntimeId().isNotPresent()) {
                rtHash = NReservedIOUtils.getFileOrDirectoryDigest(file.toPath());
                if (rtHash == null) {
                    rtHash = "";
                }
            }
            bLog.log(Level.CONFIG, NLogVerb.INFO, NMsg.ofJ("detect {0} version {1} - digest {2} from {3}", name, id.toString(), rtHash, file));
        }

        for (NDependency s : deps) {
            NClassLoaderNodeBuilder x = new NClassLoaderNodeBuilder();
            if (NReservedUtils.isAcceptDependency(s, computedOptions)) {
                x.setId(s.toString()).setUrl(NReservedMavenUtils.getBootCacheJar(s.toId(), repositories, workspaceBootLibFolder, !recover, name + " dependency", computedOptions.getExpireTime().orNull(), errorList, computedOptions, pathExpansionConverter, bLog).toURI().toURL());
                x.setIncludedInClasspath(NReservedClassLoaderUtils.isLoadedClassPath(x.getURL(), getContextClassLoader(), bLog));
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
