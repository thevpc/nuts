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
import net.thevpc.nuts.spi.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static String apiDigest;
    private final long creationTime = System.currentTimeMillis();
    private final NutsBootOptions userOptions;
    private final PrivateNutsBootLog bLog;
    private final NutsBootOptions computedOptions;
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
                    return PrivateNutsUtils.getHome(NutsStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()), computedOptions);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<NutsStoreLocation, String> s = computedOptions.getStoreLocations();
                    String v = s.get(NutsStoreLocation.parseLenient(from));
                    if (v == null) {
                        return "${" + from + "}";
                    }
                    return v;
                }
            }
            return "${" + from + "}";
        }
    };
    private int newInstanceRequirements;
    private NutsBootOptions lastWorkspaceOptions;
    private Set<String> parsedBootRuntimeDependenciesRepositories;
    private Set<String> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private NutsLogger nLog;
    private NutsSession nLogSession;

    public NutsBootWorkspace(NutsBootTerminal bootTerminal, String... args) {
        this.bLog = new PrivateNutsBootLog(bootTerminal);
        NutsBootOptions o = new NutsBootOptions()
                .setCreationTime(creationTime);
        if (bootTerminal != null) {
            o.setStdin(bootTerminal.getIn());
            o.setStdout(bootTerminal.getOut());
            o.setStderr(bootTerminal.getErr());
        }
        NutsApiUtils.parseNutsArguments(args, o, bLog);
        this.userOptions = o;
        this.computedOptions = new NutsBootOptions();
        this.computedOptions.setAll(this.userOptions);
        this.bLog.setOptions(this.computedOptions);
        this.newInstanceRequirements = 0;
    }

    public NutsBootWorkspace(NutsBootOptions userOptions) {
        if (userOptions == null) {
            this.bLog = new PrivateNutsBootLog(null);
            this.userOptions = new NutsBootOptions().setCreationTime(creationTime);
            this.computedOptions = new NutsBootOptions();
            this.computedOptions.setAll(this.userOptions);
        } else {
            bLog = new PrivateNutsBootLog(
                    new NutsBootTerminal(
                            userOptions.getStdin(),
                            userOptions.getStdout(),
                            userOptions.getStderr()
                    )
            );
            this.userOptions = userOptions;
            this.computedOptions = new NutsBootOptions();
            this.computedOptions.setAll(userOptions);
            if (userOptions.getCreationTime() == 0) {
                computedOptions.setCreationTime(creationTime);
            }
        }
        this.bLog.setOptions(this.computedOptions);
        this.newInstanceRequirements = 0;
    }

    private static void revalidateLocations(NutsBootOptions options, String workspaceName, boolean immediateLocation) {
        if (NutsBlankable.isBlank(options.getName())) {
            options.setName(workspaceName);
        }
        if (options.getStoreLocationStrategy() == null) {
            options.setStoreLocationStrategy(
                    immediateLocation ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE
            );
        }
        if (options.getRepositoryStoreLocationStrategy() == null) {
            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
        }
        Map<NutsStoreLocation, String> storeLocations = NutsUtilPlatforms.buildLocations(
                options.getStoreLocationLayout(), options.getStoreLocationStrategy(),
                options.getStoreLocations(), options.getHomeLocations(),
                options.isGlobal(),
                options.getWorkspace(),
                null//no session!
        );
        if (new HashSet<>(storeLocations.values()).size() != storeLocations.size()) {
            Map<String, List<NutsStoreLocation>> conflicts = new LinkedHashMap<>();
            for (Map.Entry<NutsStoreLocation, String> e : storeLocations.entrySet()) {
                conflicts
                        .computeIfAbsent(e.getValue(), k -> new ArrayList<>())
                        .add(e.getKey())
                ;
            }
            StringBuilder error = new StringBuilder();
            error.append("invalid store locations. Two or more stores point to the same location:");
            List<Object> errorParams = new ArrayList<>();
            for (Map.Entry<String, List<NutsStoreLocation>> e : conflicts.entrySet()) {
                List<NutsStoreLocation> ev = e.getValue();
                if (ev.size() > 1) {
                    String ek = e.getKey();
                    error.append("\n");
                    error.append("all of (").append(ev.stream().map(x -> "%s").collect(Collectors.joining(",")))
                            .append(") point to %s");
                    errorParams.addAll(ev);
                    errorParams.add(ek);
                }
            }
            throw new NutsBootException(
                    NutsMessage.cstyle(error.toString(), errorParams)
            );
        }
        options.setStoreLocations(storeLocations);
    }

    /**
     * current nuts version, loaded from pom file
     *
     * @return current nuts version
     */
    private static String getApiDigest() {
        if (apiDigest == null) {
            synchronized (Nuts.class) {
                if (apiDigest == null) {
                    apiDigest = NutsApiUtils.resolveNutsIdDigestOrError();
                }
            }
        }
        return apiDigest;
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
                nLog.with().session(nLogSession).level(Level.FINE).verb(NutsLogVerb.START)
                        .log(
                                NutsMessage.jstyle("start new process : {0}",
                                        NutsCommandLine.of(processCommandLine, nLogSession)
                                )
                        );
            } else {
                bLog.log(Level.FINE,
                        NutsLogVerb.START,
                        NutsMessage.jstyle("start new process : {0}",
                                NutsCommandLine.of(processCommandLine, nLogSession))
                );
            }
            result = new ProcessBuilder(processCommandLine).inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ex) {
            throw new NutsBootException(NutsMessage.cstyle("failed to run new nuts process"), ex);
        }
        if (result != 0) {
            throw new NutsBootException(NutsMessage.cstyle("failed to exec new process. returned %s", result));
        }
    }

    /**
     * repositories used to locale nuts-runtime artifact or its dependencies
     *
     * @param dependencies when true search for runtime dependencies, when false, search for runtime
     * @return repositories
     */
    public Set<String> resolveBootRuntimeRepositories(boolean dependencies) {
        if (dependencies) {
            if (parsedBootRuntimeDependenciesRepositories != null) {
                return parsedBootRuntimeDependenciesRepositories;
            }
            bLog.log(Level.FINE, NutsLogVerb.START, NutsMessage.jstyle("resolve boot repositories to load nuts-runtime dependencies from options : {0} and config: {1}",
                    (computedOptions.getRepositories() == null ? "[]" : Arrays.toString(computedOptions.getRepositories()))
                    , NutsBlankable.isBlank(computedOptions.getBootRepositories()) ? "[]" : computedOptions.getBootRepositories()
            ));
        } else {
            if (parsedBootRuntimeRepositories != null) {
                return parsedBootRuntimeRepositories;
            }
            bLog.log(Level.FINE, NutsLogVerb.START, NutsMessage.jstyle("resolve boot repositories to load nuts-runtime from options : {0} and config: {1}",
                    (computedOptions.getRepositories() == null ? "[]" : Arrays.toString(computedOptions.getRepositories()))
                    , NutsBlankable.isBlank(computedOptions.getBootRepositories()) ? "[]" : computedOptions.getBootRepositories()
            ));
        }
        NutsRepositorySelectorList bootRepositories = NutsRepositorySelectorList.ofAll(computedOptions.getRepositories(), PrivateNutsBootRepositoryDB.INSTANCE, null);
        NutsRepositorySelector[] old = NutsRepositorySelectorList.ofAll(
                new String[]{computedOptions.getBootRepositories()},
                PrivateNutsBootRepositoryDB.INSTANCE, null
        ).toArray();
        NutsRepositoryURL[] result = null;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositories.resolve(new NutsRepositoryURL[]{
                    NutsRepositoryURL.of("maven-local", null),
                    NutsRepositoryURL.of("maven-central", null),
            }, PrivateNutsBootRepositoryDB.INSTANCE);
        } else {
            result = bootRepositories.resolve(Arrays.stream(old).map(x -> NutsRepositoryURL.of(x.getName(), x.getUrl()))
                            .toArray(NutsRepositoryURL[]::new)
                    , PrivateNutsBootRepositoryDB.INSTANCE);
        }
        Set<String> rr = Arrays.stream(result).map(NutsRepositoryURL::getLocation).collect(Collectors.toCollection(LinkedHashSet::new));
        if (dependencies) {
            parsedBootRuntimeDependenciesRepositories = rr;
        } else {
            parsedBootRuntimeRepositories = rr;
        }
        return rr;
    }

    public String[] createProcessCommandLine() {
        prepareWorkspace();
        bLog.log(Level.FINE, NutsLogVerb.START, NutsMessage.jstyle("running version {0}.  {1}", computedOptions.getApiVersion(), getRequirementsHelpString(true)));
        StringBuilder errors = new StringBuilder();
        String defaultWorkspaceLibFolder = computedOptions.getStoreLocation(NutsStoreLocation.LIB);
        List<String> repos = new ArrayList<>();
        repos.add(defaultWorkspaceLibFolder);
        Collection<String> bootRepositories = resolveBootRuntimeRepositories(true);
        repos.addAll(bootRepositories);
        PrivateNutsErrorInfoList errorList = new PrivateNutsErrorInfoList();
        File file = PrivateNutsUtilMavenRepos.resolveOrDownloadJar(
                new NutsBootId("net.thevpc.nuts", "nuts", NutsBootVersion.parse(computedOptions.getApiVersion())),
                repos.toArray(new String[0]),
                computedOptions.getStoreLocation(NutsStoreLocation.LIB) + File.separator + NutsConstants.Folders.ID
                , bLog,
                false,
                computedOptions.getExpireTime(),
                errorList
        );
        if (file == null) {
            errors.append("unable to load nuts ").append(computedOptions.getApiVersion()).append("\n");
            for (PrivateNutsErrorInfo errorInfo : errorList.list()) {
                errors.append(errorInfo.toString()).append("\n");
            }
            showError(null,
                    errors.toString(),
                    errorList
            );
            throw new NutsBootException(
                    NutsMessage.cstyle("unable to load %s#%s", NutsConstants.Ids.NUTS_API, computedOptions.getApiVersion())
            );
        }

        List<String> cmd = new ArrayList<>();
        String jc = computedOptions.getJavaCommand();
        if (jc == null || jc.trim().isEmpty()) {
            jc = PrivateNutsUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : PrivateNutsCommandLine.parseCommandLineArray(computedOptions.getJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        if (computedOptions.getJavaOptions() != null) {
            Collections.addAll(cmd, PrivateNutsCommandLine.parseCommandLineArray(computedOptions.getJavaOptions()));
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(Arrays.asList(
                new PrivateNutsWorkspaceOptionsFormat(computedOptions)
                        .setCompact(true).setApiVersion(computedOptions.getApiVersion()).getBootCommandLine()
                        .toStringArray()
        ));
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            bLog.log(Level.FINE, NutsLogVerb.START, NutsMessage.jstyle("[exec] {0}", sb));
        }
        return cmd.toArray(new String[0]);
    }

    public NutsBootOptions getOptions() {
        return computedOptions;
    }

    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {
            preparedWorkspace = true;
            if (bLog.isLoggable(Level.CONFIG)) {
                bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("bootstrap Nuts version {0} - digest {1}...", Nuts.getVersion(), getApiDigest()));
                bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("boot-class-path:"));
                for (String s : System.getProperty("java.class.path").split(File.pathSeparator)) {
                    bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("                  {0}", s));
                }
                ClassLoader thisClassClassLoader = getClass().getClassLoader();
                bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("class-loader: {0}", thisClassClassLoader));
                for (URL url : PrivateNutsUtilClassLoader.resolveClasspathURLs(thisClassClassLoader, false)) {
                    bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("                 {0}", url));
                }
                ClassLoader tctxloader = Thread.currentThread().getContextClassLoader();
                if (tctxloader != thisClassClassLoader) {
                    bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("thread-class-loader: {0}", tctxloader));
                    for (URL url : PrivateNutsUtilClassLoader.resolveClasspathURLs(tctxloader, false)) {
                        bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("                 {0}", url));
                    }
                }
                ClassLoader contextClassLoader = getContextClassLoader();
                bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("ctx-class-loader: {0}", contextClassLoader));
                if (contextClassLoader != null) {
                    for (URL url : PrivateNutsUtilClassLoader.resolveClasspathURLs(contextClassLoader, false)) {
                        bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("                 {0}", url));
                    }
                }
                bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("system-properties:", contextClassLoader));
                Map<String, String> m = (Map) System.getProperties();
                int max = m.keySet().stream().mapToInt(String::length).max().getAsInt();
                for (String k : new TreeSet<String>(m.keySet())) {
                    bLog.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("    {0} = {1}",
                            PrivateNutsUtils.leftAlign(k, max),
                            PrivateNutsUtils.compressString(m.get(k))
                    ));
                }
            }
            String _ws = computedOptions.getWorkspace();
            String workspaceName;
            String lastNutsWorkspaceJsonConfigPath = null;
            boolean immediateLocation = false;
            NutsBootOptions lastConfigLoaded = null;
            if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                //this is a protocol based workspace
                //String protocol=ws.substring(0,ws.indexOf("://"));
                workspaceName = "remote-bootstrap";
                lastNutsWorkspaceJsonConfigPath = NutsUtilPlatforms.getWorkspaceLocation(null,
                        computedOptions.isGlobal(),
                        PrivateNutsUtils.resolveValidWorkspaceName(workspaceName));
                lastConfigLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                immediateLocation = true;

            } else {
                immediateLocation = PrivateNutsUtils.isValidWorkspaceName(_ws);
                int maxDepth = 36;
                for (int i = 0; i < maxDepth; i++) {
                    lastNutsWorkspaceJsonConfigPath
                            = PrivateNutsUtils.isValidWorkspaceName(_ws)
                            ? NutsUtilPlatforms.getWorkspaceLocation(
                            null,
                            computedOptions.isGlobal(),
                            PrivateNutsUtils.resolveValidWorkspaceName(_ws)
                    ) : PrivateNutsUtilIO.getAbsolutePath(_ws);

                    NutsBootOptions configLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                    if (configLoaded == null) {
                        //not loaded
                        break;
                    }
                    if (NutsBlankable.isBlank(configLoaded.getWorkspace())) {
                        lastConfigLoaded = configLoaded;
                        break;
                    }
                    _ws = configLoaded.getWorkspace();
                    if (i >= maxDepth - 1) {
                        throw new NutsBootException(NutsMessage.cstyle("cyclic workspace resolution"));
                    }
                }
                workspaceName = PrivateNutsUtils.resolveValidWorkspaceName(computedOptions.getWorkspace());
            }
            computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            if (lastConfigLoaded != null) {
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                computedOptions.setName(lastConfigLoaded.getName());
                computedOptions.setUuid(lastConfigLoaded.getUuid());
                if (!computedOptions.isReset()) {
                    computedOptions.setBootRepositories(lastConfigLoaded.getBootRepositories());
                    computedOptions.setJavaCommand(lastConfigLoaded.getJavaCommand());
                    computedOptions.setJavaOptions(lastConfigLoaded.getJavaOptions());
                    computedOptions.setExtensionsSet(PrivateNutsUtils.copy(lastConfigLoaded.getExtensionsSet()));
                    computedOptions.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
                    computedOptions.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
                    computedOptions.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
                    computedOptions.setStoreLocations(PrivateNutsUtils.copy(lastConfigLoaded.getStoreLocations()));
                    computedOptions.setHomeLocations(PrivateNutsUtils.copy(lastConfigLoaded.getHomeLocations()));
                } else {
                    lastWorkspaceOptions = new NutsBootOptions();
                    lastWorkspaceOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                    lastWorkspaceOptions.setName(lastConfigLoaded.getName());
                    lastWorkspaceOptions.setUuid(lastConfigLoaded.getUuid());
                    lastWorkspaceOptions.setBootRepositories(lastConfigLoaded.getBootRepositories());
                    lastWorkspaceOptions.setJavaCommand(lastConfigLoaded.getJavaCommand());
                    lastWorkspaceOptions.setJavaOptions(lastConfigLoaded.getJavaOptions());
                    lastWorkspaceOptions.setExtensionsSet(PrivateNutsUtils.copy(lastConfigLoaded.getExtensionsSet()));
                    lastWorkspaceOptions.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
                    lastWorkspaceOptions.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
                    lastWorkspaceOptions.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
                    lastWorkspaceOptions.setStoreLocations(PrivateNutsUtils.copy(lastConfigLoaded.getStoreLocations()));
                    lastWorkspaceOptions.setHomeLocations(PrivateNutsUtils.copy(lastConfigLoaded.getHomeLocations()));
                }
            }
            revalidateLocations(computedOptions, workspaceName, immediateLocation);
            long countDeleted = 0;
            //now that config is prepared proceed to any cleanup
            if (computedOptions.isReset()) {
                //force loading version early, it will be used later-on
                Nuts.getVersion();
                if (lastWorkspaceOptions != null) {
                    revalidateLocations(lastWorkspaceOptions, workspaceName, immediateLocation);
                    if (computedOptions.isDry()) {
                        bLog.log(Level.INFO, NutsLogVerb.DEBUG, NutsMessage.jstyle("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, NutsLogVerb.WARNING, NutsMessage.jstyle("reset workspace"));
                        countDeleted = PrivateNutsUtilDeleteFiles.deleteStoreLocations(lastWorkspaceOptions, getOptions(), true, bLog, NutsStoreLocation.values());
                        PrivateNutsUtilLauncher.ndiUndo(bLog);
                    }
                } else {
                    if (computedOptions.isDry()) {
                        bLog.log(Level.INFO, NutsLogVerb.DEBUG, NutsMessage.jstyle("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, NutsLogVerb.WARNING, NutsMessage.jstyle("reset workspace"));
                        countDeleted = PrivateNutsUtilDeleteFiles.deleteStoreLocations(computedOptions, getOptions(), true, bLog, NutsStoreLocation.values());
                        PrivateNutsUtilLauncher.ndiUndo(bLog);
                    }
                }
            } else if (computedOptions.isRecover()) {
                if (computedOptions.isDry()) {
                    bLog.log(Level.INFO, NutsLogVerb.DEBUG, NutsMessage.jstyle("[dry] [recover] delete CACHE/TEMP workspace folders"));
                } else {
                    bLog.log(Level.CONFIG, NutsLogVerb.WARNING, NutsMessage.jstyle("recover workspace."));
                    List<Object> folders = new ArrayList<>();
                    folders.add(NutsStoreLocation.CACHE);
                    folders.add(NutsStoreLocation.TEMP);
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = PrivateNutsUtilDeleteFiles.getStoreLocationPath(computedOptions, NutsStoreLocation.LIB);
                    if (p != null) {
                        folders.add(new File(p, "id/net/thevpc/nuts/nuts"));
                        folders.add(new File(p, "id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = PrivateNutsUtilDeleteFiles.deleteStoreLocations(computedOptions, getOptions(), false, bLog, folders.toArray());
                }
            }
            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (computedOptions.getApplicationArguments().length == 0 && computedOptions.isSkipBoot()
                    && (computedOptions.isRecover() || computedOptions.isReset())) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        bLog.log(Level.WARNING, NutsLogVerb.WARNING, NutsMessage.jstyle("workspace erased : {0}", computedOptions.getWorkspace()));
                    } else {
                        bLog.log(Level.WARNING, NutsLogVerb.WARNING, NutsMessage.jstyle("workspace is not erased because it does not exist : {0}", computedOptions.getWorkspace()));
                    }
                }
                throw new NutsBootException(NutsMessage.cstyle(""), 0);
            }
            //after eventual clean up
            if (computedOptions.isInherited()) {
                //when Inherited, always use the current Api version!
                computedOptions.setApiVersion(Nuts.getVersion());
            } else {
                if (NutsConstants.Versions.LATEST.equalsIgnoreCase(computedOptions.getApiVersion())
                        || NutsConstants.Versions.RELEASE.equalsIgnoreCase(computedOptions.getApiVersion())
                ) {
                    NutsBootId s = PrivateNutsUtilMavenRepos.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_API), null, bLog, resolveBootRuntimeRepositories(true));
                    if (s == null) {
                        throw new NutsBootException(NutsMessage.plain("unable to load latest nuts version"));
                    }
                    computedOptions.setApiVersion(s.getVersion().toString());
                }
                if (NutsBlankable.isBlank(computedOptions.getApiVersion())) {
                    computedOptions.setApiVersion(Nuts.getVersion());
                }
            }

            NutsBootId bootApiId = new NutsBootId("net.thevpc.nuts", "nuts", NutsBootVersion.parse(computedOptions.getApiVersion()));
            Path nutsApiConfigBootPath =
                    Paths.get(computedOptions.getStoreLocation(NutsStoreLocation.LIB) + File.separator + NutsConstants.Folders.ID)
                            .resolve(PrivateNutsUtils.idToPath(bootApiId)).resolve(NutsConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(nutsApiConfigBootPath, computedOptions.getExpireTime(), bLog)) {
                try {
                    Map<String, Object> obj = PrivateNutsJsonParser.parse(nutsApiConfigBootPath);
                    if (!obj.isEmpty()) {
                        bLog.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("loaded {0} file : {1}",
                                nutsApiConfigBootPath.getFileName(), nutsApiConfigBootPath.toString()));
                        loadedApiConfig = true;
                        if (computedOptions.getRuntimeId() == null) {
                            String runtimeId = (String) obj.get("runtimeId");
                            if (NutsBlankable.isBlank(runtimeId)) {
                                bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("{0} does not contain runtime-id",
                                        nutsApiConfigBootPath));
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
                } catch (UncheckedIOException | NutsIOException e) {
                    bLog.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("unable to read {0}", nutsApiConfigBootPath));
                }
            }
            if (!loadedApiConfig || computedOptions.getRuntimeId() == null
                    || computedOptions.getRuntimeBootDescriptor() == null
                    || computedOptions.getExtensionBootDescriptors() == null
                    || computedOptions.getBootRepositories() == null) {

                //resolve runtime id
                if (computedOptions.getRuntimeId() == null) {
                    String apiVersion = computedOptions.getApiVersion();
                    //load from local lib folder
                    NutsBootId runtimeId = null;
                    if (!computedOptions.isReset() && !computedOptions.isRecover()) {
                        runtimeId = PrivateNutsUtilMavenRepos.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_RUNTIME),
                                (rtVersion) -> rtVersion.getFrom().startsWith(apiVersion + "."), bLog,
                                Collections.singletonList(
                                        "nuts@" + computedOptions.getStoreLocation(NutsStoreLocation.LIB) + File.separatorChar + NutsConstants.Folders.ID
                                )
                        );
                    }
                    if (runtimeId == null) {
                        runtimeId = PrivateNutsUtilMavenRepos.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getFrom().startsWith(apiVersion + "."), bLog, resolveBootRuntimeRepositories(true));
                    }
                    if (runtimeId == null) {
                        bLog.log(Level.FINEST, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to resolve latest runtime-id version (is connection ok?)"));
                    }
                    computedOptions.setRuntimeId(runtimeId == null ? null : runtimeId.toString());
                    computedOptions.setRuntimeBootDescriptor(null);
                    computedOptions.setBootRepositories(null);
                }
                if (computedOptions.getRuntimeId() == null) {
                    computedOptions.setRuntimeId(
                            new NutsBootId("net.thevpc.nuts", "nuts-runtime",
                                    NutsBootVersion.parse(computedOptions.getApiVersion() + ".0")
                            ).toString()
                    );
                    bLog.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("consider default runtime-id : {0}", computedOptions.getRuntimeId()));
                }
                NutsBootId runtimeIdObject = NutsBootId.parse(computedOptions.getRuntimeId());
                if (runtimeIdObject.getVersion().isBlank()) {
                    computedOptions.setRuntimeId(
                            new NutsBootId(runtimeIdObject.getGroupId(), runtimeIdObject.getArtifactId(),
                                    NutsBootVersion.parse(computedOptions.getApiVersion() + ".0"))
                                    .toString()
                    );
                }

                //resolve runtime libraries
                if (computedOptions.getRuntimeBootDescriptor() == null) {
                    Set<NutsBootId> loadedDeps = null;
                    NutsBootId rid = NutsBootId.parse(computedOptions.getRuntimeId());
                    Path nutsRuntimeCacheConfigPath =
                            Paths.get(computedOptions.getStoreLocation(NutsStoreLocation.LIB) + File.separator + NutsConstants.Folders.ID)
                                    .resolve(PrivateNutsUtils.idToPath(bootApiId)).resolve(NutsConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
                    try {
                        boolean cacheLoaded = false;
                        if (!computedOptions.isRecover() && !computedOptions.isReset() && PrivateNutsUtils.isFileAccessible(nutsRuntimeCacheConfigPath, computedOptions.getExpireTime(), bLog)) {
                            try {
                                Map<String,Object> obj = PrivateNutsJsonParser.parse(nutsRuntimeCacheConfigPath);
                                bLog.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("loaded {0} file : {1}", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString()));
                                loadedDeps = PrivateNutsUtils.parseDependencies((String) obj.get("dependencies"));
                            } catch (Exception ex) {
                                bLog.log(Level.FINEST, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load {0} file : {1} : {2}", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString(), ex.toString()));
                                //ignore...
                            }
                            cacheLoaded = true;
                        }

                        if (!cacheLoaded || loadedDeps == null) {
                            loadedDeps = PrivateNutsUtilMavenRepos.loadDependenciesFromId(NutsBootId.parse(computedOptions.getRuntimeId()),
                                    bLog, resolveBootRuntimeRepositories(false));
                            bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("detect runtime dependencies : {0}", loadedDeps));
                        }
                    } catch (Exception ex) {
                        bLog.log(Level.FINEST, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load {0} file : {1}", nutsRuntimeCacheConfigPath.getFileName(), ex.toString()));
                        //
                    }
                    if (loadedDeps == null) {
                        throw new NutsBootException(NutsMessage.cstyle("unable to load dependencies for %s", rid));
                    }
                    computedOptions.setRuntimeBootDescriptor(new NutsBootDescriptor(
                            NutsBootId.parse(computedOptions.getRuntimeId()),
                            loadedDeps.toArray(new NutsBootId[0])
                    ));
                    Set<String> bootRepositories = resolveBootRuntimeRepositories(false);
                    if (bLog.isLoggable(Level.CONFIG)) {
                        if (bootRepositories.size() == 0) {
                            bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("workspace bootRepositories could not be resolved"));
                        } else if (bootRepositories.size() == 1) {
                            bLog.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("workspace bootRepositories resolved to : {0}", bootRepositories.toArray()[0]));
                        } else {
                            bLog.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("workspace bootRepositories resolved to : "));
                            for (String repository : bootRepositories) {
                                bLog.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("    {0}", repository));
                            }
                        }
                    }
                    computedOptions.setBootRepositories(String.join(";", bootRepositories));
                }

                //resolve extension libraries
                if (computedOptions.getExtensionBootDescriptors() == null) {
                    LinkedHashSet<String> excludedExtensions = new LinkedHashSet<>();
                    if (computedOptions.getExcludedExtensions() != null) {
                        for (String excludedExtensionGroup : computedOptions.getExcludedExtensions()) {
                            for (String excludedExtension : excludedExtensionGroup.split("[;, ]")) {
                                if (excludedExtension.length() > 0) {
                                    excludedExtensions.add(NutsBootId.parse(excludedExtension).getShortName());
                                }
                            }
                        }
                    }
                    if (computedOptions.getExtensionsSet() != null) {
                        List<NutsBootDescriptor> all = new ArrayList<>();
                        for (String extension : computedOptions.getExtensionsSet()) {
                            NutsBootId eid = NutsBootId.parse(extension);
                            if (!excludedExtensions.contains(eid.getShortName()) && !excludedExtensions.contains(eid.getArtifactId())) {
                                Path extensionFile =
                                        Paths.get(computedOptions.getStoreLocation(NutsStoreLocation.LIB) + File.separator + NutsConstants.Folders.ID)
                                                .resolve(PrivateNutsUtils.idToPath(bootApiId)).resolve(NutsConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
                                Set<NutsBootId> loadedDeps = null;
                                if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(extensionFile, computedOptions.getExpireTime(), bLog)) {
                                    try {
                                        Properties obj = PrivateNutsUtilIO.loadURLProperties(extensionFile, bLog);
                                        bLog.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("loaded {0} file : {1}", extensionFile.getFileName(), extensionFile.toString()));
                                        loadedDeps = PrivateNutsUtils.parseDependencies(obj.getProperty("dependencies"));
                                    } catch (Exception ex) {
                                        bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load {0} file : {1} : {2}", extensionFile.getFileName(), extensionFile.toString(), ex.toString()));
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    loadedDeps = PrivateNutsUtilMavenRepos.loadDependenciesFromId(eid, bLog, resolveBootRuntimeRepositories(true));
                                }
                                all.add(new NutsBootDescriptor(NutsBootId.parse(extension), loadedDeps.toArray(new NutsBootId[0])));
                            }
                        }
                        computedOptions.setExtensionBootDescriptors(all.toArray(new NutsBootDescriptor[0]));
                    } else {
                        computedOptions.setExtensionBootDescriptors(new NutsBootDescriptor[0]);
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
        return computedOptions.isTrace() && !computedOptions.isBot()
                && (computedOptions.getOutputFormat() == NutsContentType.PLAIN
                || computedOptions.getOutputFormat() == null
        );
    }


    private boolean isLoadFromCache() {
        return !computedOptions.isRecover() && !computedOptions.isReset();
    }

    public NutsSession openWorkspace() {
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsException(
                    NutsMessage.cstyle("unable to open a distinct version : %s from nuts#%s",
                            getRequirementsHelpString(true), Nuts.getVersion())
            );
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (computedOptions.getApplicationArguments().length == 0 && computedOptions.isSkipBoot()
                && (computedOptions.isRecover() || computedOptions.isReset())) {
            if (isPlainTrace()) {
                bLog.log(Level.WARNING, NutsLogVerb.WARNING, NutsMessage.jstyle("workspace erased : {0}", computedOptions.getWorkspace()));
            }
            throw new NutsBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
        PrivateNutsErrorInfoList errorList = new PrivateNutsErrorInfoList();
        try {
            if (computedOptions.getOpenMode() == NutsOpenMode.OPEN_OR_ERROR) {
                //add fail fast test!!
                if (!new File(computedOptions.getWorkspace(), NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME).isFile()) {
                    throw new NutsWorkspaceNotFoundException(computedOptions.getWorkspace());
                }
            } else if (computedOptions.getOpenMode() == NutsOpenMode.CREATE_OR_ERROR) {
                if (new File(computedOptions.getWorkspace(), NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME).exists()) {
                    throw new NutsWorkspaceAlreadyExistsException(computedOptions.getWorkspace());
                }
            }
            if (NutsBlankable.isBlank(NutsConstants.Ids.NUTS_API + "#" + computedOptions.getApiVersion())
                    || computedOptions.getRuntimeId() == null
                    || NutsBlankable.isBlank(computedOptions.getBootRepositories())
                    || computedOptions.getRuntimeBootDescriptor() == null
                    || computedOptions.getExtensionBootDescriptors() == null) {
                throw new NutsBootException(NutsMessage.plain("invalid workspace state"));
            }
            boolean recover = computedOptions.isRecover() || computedOptions.isReset();

            List<NutsClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = computedOptions.getStoreLocation(NutsStoreLocation.LIB) + File.separator + NutsConstants.Folders.ID;

            String[] repositories =
                    Arrays.stream((computedOptions.getBootRepositories() == null ? "" : computedOptions.getBootRepositories())
                            .split("[\n;]")
                    ).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);

            computedOptions.setRuntimeBootDependencyNode(
                    createClassLoaderNode(computedOptions.getRuntimeBootDescriptor(),
                            repositories, workspaceBootLibFolder, recover, errorList, true));

            for (NutsBootDescriptor nutsBootDescriptor : computedOptions.getExtensionBootDescriptors()) {
                deps.add(createClassLoaderNode(nutsBootDescriptor, repositories, workspaceBootLibFolder, recover,
                        errorList, false));
            }
            computedOptions.setExtensionBootDependencyNodes(deps.toArray(new NutsClassLoaderNode[0]));
            deps.add(0, computedOptions.getRuntimeBootDependencyNode());

            bootClassWorldURLs = PrivateNutsUtilClassLoader.resolveClassWorldURLs(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader(), bLog);
            workspaceClassLoader = /*bootClassWorldURLs.length == 0 ? getContextClassLoader() : */
                    new PrivateNutsBootClassLoader(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader());
            computedOptions.setClassWorldLoader(workspaceClassLoader);
            if (bLog.isLoggable(Level.CONFIG)) {
                if (bootClassWorldURLs.length == 0) {
                    bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("empty nuts class world. All dependencies are already loaded in classpath, most likely"));
                } else if (bootClassWorldURLs.length == 1) {
                    bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("resolve nuts class world to : {0} {1}",
                            PrivateNutsUtilDigest.getURLDigest(bootClassWorldURLs[0], bLog), bootClassWorldURLs[0]));
                } else {
                    bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("resolve nuts class world is to : "));
                    for (URL u : bootClassWorldURLs) {
                        bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("    {0} : {1}",
                                PrivateNutsUtilDigest.getURLDigest(u, bLog), u));
                    }
                }
            }
            computedOptions.setClassWorldURLs(bootClassWorldURLs);
            bLog.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("search for NutsBootWorkspaceFactory service implementations"));
            ServiceLoader<NutsBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsBootWorkspaceFactory.class, workspaceClassLoader);
            List<NutsBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NutsBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new PrivateNutsBootWorkspaceFactoryComparator(computedOptions));
            if (bLog.isLoggable(Level.CONFIG)) {
                if (factories.isEmpty()) {
                    bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("unable to detect NutsBootWorkspaceFactory service implementations"));
                } else if (factories.size() == 1) {
                    bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("detect NutsBootWorkspaceFactory service implementation : {0}", factories.get(0).getClass().getName()));
                } else {
                    bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("detect NutsBootWorkspaceFactory service implementations are :"));
                    for (NutsBootWorkspaceFactory u : factories) {
                        bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("    {0}",
                                u.getClass().getName()));
                    }
                }
            }
            NutsBootWorkspaceFactory factoryInstance = null;
            List<Throwable> exceptions = new ArrayList<>();
            for (NutsBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    if (bLog.isLoggable(Level.CONFIG)) {
                        bLog.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("create workspace using {0}", factoryInstance.getClass().getName()));
                    }
                    computedOptions.setBootWorkspaceFactory(factoryInstance);
                    nutsWorkspace = a.createWorkspace(computedOptions);
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    bLog.log(Level.SEVERE, NutsMessage.jstyle("unable to create workspace using factory {0}", a), ex);
                }
                if (nutsWorkspace != null) {
                    break;
                }
            }
            if (nutsWorkspace == null) {
                //should never happen
                bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load Workspace \"{0}\" from ClassPath :", computedOptions.getName()));
                for (URL url : bootClassWorldURLs) {
                    bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("\t {0}", PrivateNutsUtils.formatURL(url)));
                }
                for (Throwable exception : exceptions) {
                    bLog.log(Level.SEVERE, NutsMessage.jstyle("{0}", exception), exception);
                }
                bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load Workspace Component from ClassPath : {0}", Arrays.asList(bootClassWorldURLs)));
                throw new NutsInvalidWorkspaceException(this.computedOptions.getWorkspace(),
                        NutsMessage.cstyle("unable to load Workspace Component from ClassPath : %s%n  caused by:%n\t%s"
                                , Arrays.asList(bootClassWorldURLs),
                                exceptions.stream().map(Throwable::toString).collect(Collectors.joining("\n\t"))
                        )
                );
            }
            return nutsWorkspace.createSession();
        } catch (NutsReadOnlyException | NutsUserCancelException | PrivateNutsBootCancelException ex) {
            throw ex;
        } catch (Throwable ex) {
            StringBuilder errors = new StringBuilder();
            errorList.add(new PrivateNutsErrorInfo(
                    null, null, null, "unable to boot workspace : " + ex,
                    ex
            ));
            for (PrivateNutsErrorInfo errorInfo : errorList.list()) {
                errors.append(errorInfo.toString()).append("\n");
            }
            showError(bootClassWorldURLs,
                    errors.toString(),
                    errorList
            );
            if (ex instanceof NutsException) {
                throw (NutsException) ex;
            }
            if (ex instanceof NutsSecurityException) {
                throw (NutsSecurityException) ex;
            }
            if (ex instanceof NutsBootException) {
                throw (NutsBootException) ex;
            }
            throw new NutsBootException(NutsMessage.plain("unable to locate valid nuts-runtime package"), ex);
        }
    }


    private ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        if (computedOptions.getClassLoaderSupplier() != null) {
            cl = computedOptions.getClassLoaderSupplier().get();
        }
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        return cl;
    }

    private String getRunModeString() {
        if (this.getOptions().isReset()) {
            return "reset";
        } else if (this.getOptions().isRecover()) {
            return "recover";
        } else {
            return "exec";
        }
    }

    private void runCommandHelp() {
        NutsContentType f = computedOptions.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (computedOptions.isDry()) {
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
        NutsContentType f = computedOptions.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (computedOptions.isDry()) {
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
        NutsContentType f = computedOptions.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (computedOptions.isDry()) {
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
        if (computedOptions.isCommandHelp()) {
            runCommandHelp();
            return null;
        } else if (computedOptions.isCommandVersion()) {
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
        NutsBootOptions o = this.getOptions();
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NutsBootException(
                    NutsMessage.cstyle("workspace not available to run : %s",
                            NutsCommandLine.of(o.getApplicationArguments(), session)
                    )
            );
        }

        session.setAppId(workspace.getApiId());
        if (nLog == null) {
            nLog = NutsLogger.of(NutsBootWorkspace.class, session);
            nLogSession = session;
        }
        NutsLoggerOp logOp = nLog.with().session(session).level(Level.CONFIG);
        logOp.verb(NutsLogVerb.SUCCESS).log(
                NutsMessage.jstyle("running workspace in {0} mode", getRunModeString())
        );
        if (workspace == null && o.getApplicationArguments().length > 0) {
            switch (o.getApplicationArguments()[0]) {
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
        if (o.getApplicationArguments().length == 0) {
            if (o.isSkipWelcome()) {
                return session;
            }
            session.exec()
                    .addCommand("welcome")
                    .addExecutorOptions(o.getExecutorOptions())
                    .setExecutionType(o.getExecutionType())
                    .setFailFast(true)
                    .setDry(computedOptions.isDry())
                    .run();
        } else {
            session.exec()
                    .addCommand(o.getApplicationArguments())
                    .addExecutorOptions(o.getExecutorOptions())
                    .setExecutionType(o.getExecutionType())
                    .setFailFast(true)
                    .setDry(computedOptions.isDry())
                    .run();
        }
        return session;
    }

    private void fallbackInstallActionUnavailable(String message) {
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle(message));
    }

    private void showError(URL[] bootClassWorldURLs, String extraMessage, PrivateNutsErrorInfoList ths) {
        String workspace = computedOptions.getWorkspace();
        Map<NutsStoreLocation, String> rbc_locations = computedOptions.getStoreLocations();
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to bootstrap nuts (digest {0}):", getApiDigest()));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("{0}", extraMessage));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("here after current environment info:"));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-boot-api-version            : {0}", PrivateNutsUtils.coalesce(computedOptions.getApiVersion(), "<?> Not Found!")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-boot-runtime                : {0}", PrivateNutsUtils.coalesce(computedOptions.getRuntimeId(), "<?> Not Found!")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-boot-repositories           : {0}", PrivateNutsUtils.coalesce(computedOptions.getBootRepositories(), "<?> Not Found!")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  workspace-location               : {0}", PrivateNutsUtils.coalesce(workspace, "<default-location>")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-apps                  : {0}", rbc_locations.get(NutsStoreLocation.APPS)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-config                : {0}", rbc_locations.get(NutsStoreLocation.CONFIG)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-var                   : {0}", rbc_locations.get(NutsStoreLocation.VAR)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-log                   : {0}", rbc_locations.get(NutsStoreLocation.LOG)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-temp                  : {0}", rbc_locations.get(NutsStoreLocation.TEMP)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-cache                 : {0}", rbc_locations.get(NutsStoreLocation.CACHE)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-run                   : {0}", rbc_locations.get(NutsStoreLocation.RUN)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-lib                   : {0}", rbc_locations.get(NutsStoreLocation.LIB)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-strategy              : {0}", PrivateNutsUtils.desc(computedOptions.getStoreLocationStrategy())));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-layout                : {0}", PrivateNutsUtils.desc(computedOptions.getStoreLocationLayout())));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-boot-args                   : {0}", new PrivateNutsWorkspaceOptionsFormat(this.computedOptions).getBootCommandLine()));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-app-args                    : {0}", Arrays.toString(this.computedOptions.getApplicationArguments())));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  option-read-only                 : {0}", this.computedOptions.isReadOnly()));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  option-trace                     : {0}", this.computedOptions.isTrace()));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  option-progress                  : {0}", PrivateNutsUtils.desc(this.computedOptions.getProgressOptions())));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  option-open-mode                 : {0}", PrivateNutsUtils.desc(this.computedOptions.getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : this.computedOptions.getOpenMode())));

        NutsClassLoaderNode rtn = this.computedOptions.getRuntimeBootDependencyNode();
        String rtHash = "";
        if (rtn != null) {
            rtHash = PrivateNutsUtilDigest.getURLDigest(rtn.getURL(), bLog);
        }
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-runtime-digest                : {0}", rtHash));

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-runtime-classpath           : {0}", "<none>"));
        } else {
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-runtime-hash                : {0}", "<none>"));
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-runtime-classpath           : {0}", PrivateNutsUtils.formatURL(bootClassWorldURL)));
                } else {
                    bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("                                     {0}", PrivateNutsUtils.formatURL(bootClassWorldURL)));
                }
            }
        }
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java-version                     : {0}", System.getProperty("java.version")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java-executable                  : {0}", PrivateNutsUtils.resolveJavaCommand(null)));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java-class-path                  : {0}", System.getProperty("java.class.path")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java-library-path                : {0}", System.getProperty("java.library.path")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  os-name                          : {0}", System.getProperty("os.name")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  os-arch                          : {0}", System.getProperty("os.arch")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  os-version                       : {0}", System.getProperty("os.version")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  user-name                        : {0}", System.getProperty("user.name")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  user-home                        : {0}", System.getProperty("user.home")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  user-dir                         : {0}", System.getProperty("user.dir")));
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle(""));
        if (this.computedOptions.getLogConfig() == null
                || this.computedOptions.getLogConfig().getLogTermLevel() == null
                || this.computedOptions.getLogConfig().getLogFileLevel().intValue() > Level.FINEST.intValue()) {
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("If the problem persists you may want to get more debug info by adding '--verbose' arguments."));
        }
        if (!this.computedOptions.isReset() && !this.computedOptions.isRecover() && this.computedOptions.getExpireTime() == null) {
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments."));
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("Here is the proper command : "));
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java -jar nuts.jar --verbose --recover --expire [...]"));
        } else if (!this.computedOptions.isReset() && this.computedOptions.isRecover() && this.computedOptions.getExpireTime() != null) {
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("You may also enable full reset mode to ignore existing configuration with '--reset' argument."));
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("ATTENTION: this will delete all your nuts configuration. Use it at your own risk."));
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("Here is the proper command : "));
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java -jar nuts.jar --verbose --reset [...]"));
        }
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("error stack trace is:"));
            for (PrivateNutsErrorInfo th : ths.list()) {
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
                bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle(msg.toString(), msgParams.toArray()));
                bLog.log(Level.SEVERE, NutsMessage.jstyle(th.toString()), th.getThrowable());
            }
        } else {
            bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("no stack trace is available."));
        }
        bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("now exiting nuts, Bye!"));
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NutsBlankable.isBlank(computedOptions.getApiVersion())) {
            if (!unsatisfiedOnly || !computedOptions.getApiVersion().equals(Nuts.getVersion())) {
                req += 1;
            }
        }
        if (!unsatisfiedOnly || !PrivateNutsUtils.isActualJavaCommand(computedOptions.getJavaCommand())) {
            req += 2;
        }
        if (!unsatisfiedOnly || !PrivateNutsUtils.isActualJavaOptions(computedOptions.getJavaOptions())) {
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
            sb.append("nuts version ").append(NutsConstants.Ids.NUTS_API + "#").append(computedOptions.getApiVersion());
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

    private NutsClassLoaderNode createClassLoaderNode(NutsBootDescriptor descr, String[] repositories,
                                                      String workspaceBootLibFolder, boolean recover, PrivateNutsErrorInfoList errorList,
                                                      boolean runtimeDep
    ) throws MalformedURLException {
        NutsBootId id = descr.getId();
        NutsBootId[] deps = descr.getDependencies();
        NutsClassLoaderNodeBuilder rt = new NutsClassLoaderNodeBuilder();
        String name = runtimeDep ? "runtime" : ("extension " + id.toString());
        File file = PrivateNutsUtilMavenRepos.getBootCacheJar(
                NutsBootId.parse(computedOptions.getRuntimeId()), repositories, workspaceBootLibFolder,
                !recover, name, computedOptions.getExpireTime(), errorList,
                computedOptions, pathExpansionConverter, bLog);
        rt.setId(id.toString());
        rt.setUrl(file.toURI().toURL());
        rt.setIncludedInClasspath(
                PrivateNutsUtilClassLoader.isLoadedClassPath(
                        rt.getURL(), getContextClassLoader(), bLog));

        if (bLog.isLoggable(Level.CONFIG)) {
            String rtHash = "";
            if (computedOptions.getRuntimeId() != null) {
                rtHash = PrivateNutsUtilDigest.getFileOrDirectoryDigest(file.toPath());
                if (rtHash == null) {
                    rtHash = "";
                }
            }
            bLog.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("detect {0} version {1} - digest {2} from {3}",
                    name, id.toString(), rtHash, file));
        }

        for (NutsBootId s : deps) {
            NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
            if (PrivateNutsUtilBootId.isAcceptDependency(s, computedOptions)) {
                x.setId(s.toString())
                        .setUrl(PrivateNutsUtilMavenRepos.getBootCacheJar(s, repositories, workspaceBootLibFolder, !recover,
                                        name + " dependency",
                                        computedOptions.getExpireTime(), errorList, computedOptions, pathExpansionConverter, bLog)
                                .toURI().toURL()
                        );
                x.setIncludedInClasspath(PrivateNutsUtilClassLoader.isLoadedClassPath(
                        x.getURL(), getContextClassLoader(), bLog));
                rt.addDependency(x.build());
            }
        }
        return rt.build();
    }

}
