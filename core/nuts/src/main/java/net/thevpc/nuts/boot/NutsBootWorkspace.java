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
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
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
    private final NutsWorkspaceOptions options;
    private final PrivateNutsLog LOG;
    private Supplier<ClassLoader> contextClassLoaderSupplier;
    private int newInstanceRequirements;
    private PrivateNutsWorkspaceInitInformation workspaceInformation;
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "workspace":
                    return workspaceInformation.getWorkspaceLocation();
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
                    return PrivateNutsUtils.getHome(NutsStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()), workspaceInformation);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<NutsStoreLocation, String> s = workspaceInformation.getStoreLocations();
                    if (s == null) {
                        return "${" + from + "}";
                    }
                    return s.get(NutsStoreLocation.parseLenient(from));
                }
            }
            return "${" + from + "}";
        }
    };
    private PrivateNutsWorkspaceInitInformation lastWorkspaceInformation;
    private Set<String> parsedBootRuntimeDependenciesRepositories;
    private Set<String> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private NutsLogger LOG2;
    private NutsSession LOG2_SESSION;

    public NutsBootWorkspace(NutsBootTerminal bootTerminal, String... args) {
        LOG = new PrivateNutsLog(bootTerminal);
        this.options = new PrivateBootWorkspaceOptions(LOG)
                .parseArguments(args)
                .setBootTerminal(bootTerminal)
                .setCreationTime(creationTime)
                .build();
        LOG.setOptions(this.options);
        newInstanceRequirements = 0;
    }

    public NutsBootWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            LOG = new PrivateNutsLog(null);
            this.options = new PrivateBootWorkspaceOptions(LOG).setCreationTime(creationTime).build();
            LOG.setOptions(this.options);
        } else {
            LOG = new PrivateNutsLog(options.getBootTerminal());
            LOG.setOptions(options);
            if (options.getCreationTime() == 0) {
                NutsWorkspaceOptionsBuilder copy = options.builder();
                copy.setCreationTime(creationTime);
                this.options = copy.build();
                LOG.setOptions(this.options);
            } else {
                this.options = options;
            }
        }
        newInstanceRequirements = 0;
    }

    private static void revalidateLocations(PrivateNutsWorkspaceInitInformation workspaceInformation, String workspaceName, boolean immediateLocation) {
        if (NutsBlankable.isBlank(workspaceInformation.getName())) {
            workspaceInformation.setName(workspaceName);
        }
        if (workspaceInformation.getStoreLocationStrategy() == null) {
            workspaceInformation.setStoreLocationStrategy(
                    immediateLocation ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE
            );
        }
        if (workspaceInformation.getRepositoryStoreLocationStrategy() == null) {
            workspaceInformation.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
        }
        Map<NutsStoreLocation, String> storeLocations = NutsUtilPlatforms.buildLocations(
                workspaceInformation.getStoreLocationLayout(), workspaceInformation.getStoreLocationStrategy(),
                workspaceInformation.getStoreLocations(), workspaceInformation.getHomeLocations(),
                workspaceInformation.isGlobal(),
                workspaceInformation.getWorkspaceLocation(),//workspaceInformation.getName(),
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
        workspaceInformation.setStoreLocations(storeLocations);
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
            if (LOG2 != null) {
                LOG2.with().session(LOG2_SESSION).level(Level.FINE).verb(NutsLogVerb.START)
                        .log(
                                NutsMessage.jstyle("start new process : {0}",
                                        new PrivateNutsCommandLine(processCommandLine))
                        );
            } else {
                LOG.log(Level.FINE,
                        NutsLogVerb.START,
                        NutsMessage.jstyle("start new process : {0}",
                                new PrivateNutsCommandLine(processCommandLine))
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
     * repositories used to locale nuts-runtime artifact
     *
     * @return repositories
     */
    public Set<String> resolveBootRuntimeRepositories() {
        if (parsedBootRuntimeRepositories != null) {
            return parsedBootRuntimeRepositories;
        }
        LOG.log(Level.FINE, NutsLogVerb.START, NutsMessage.jstyle("resolve boot repositories to load nuts-runtime from options : {0} and config: {1}",
                (options.getRepositories() == null ? "[]" : Arrays.toString(options.getRepositories()))
                , NutsBlankable.isBlank(workspaceInformation.getBootRepositories()) ? "[]" : workspaceInformation.getBootRepositories()
        ));
        PrivateNutsRepositorySelectorList bootRepositories = PrivateNutsRepositorySelector.parse(options.getRepositories());
        PrivateNutsRepositorySelector[] old = PrivateNutsRepositorySelector.parse(new String[]{workspaceInformation.getBootRepositories()}).toArray();
        PrivateNutsRepositorySelection[] result = null;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositories.resolveSelectors(new PrivateNutsRepositorySelection[]{
                    new PrivateNutsRepositorySelection("maven-local", null),
                    new PrivateNutsRepositorySelection("maven-central", null),
            });
        } else {
            result = bootRepositories.resolveSelectors(Arrays.stream(old).map(x -> new PrivateNutsRepositorySelection(x.getName(), x.getUrl()))
                    .toArray(PrivateNutsRepositorySelection[]::new));
        }
        return parsedBootRuntimeRepositories
                = Arrays.stream(result)
                .map(x -> x.getUrl())
                .collect(Collectors.toSet());
    }

    /**
     * repositories used to locale nuts-runtime dependencies nad extensions artifacts
     *
     * @return repositories
     */
    public Set<String> resolveBootRuntimeDependenciesRepositories() {
        if (parsedBootRuntimeDependenciesRepositories != null) {
            return parsedBootRuntimeDependenciesRepositories;
        }
        LOG.log(Level.FINE, NutsLogVerb.START, NutsMessage.jstyle("resolve boot repositories to load nuts-runtime dependencies from options : {0} and config: {1}",
                (options.getRepositories() == null ? "[]" : Arrays.toString(options.getRepositories()))
                , NutsBlankable.isBlank(workspaceInformation.getBootRepositories()) ? "[]" : workspaceInformation.getBootRepositories()
        ));
        PrivateNutsRepositorySelectorList bootRepositories = PrivateNutsRepositorySelector.parse(options.getRepositories());
        PrivateNutsRepositorySelector[] old = PrivateNutsRepositorySelector.parse(new String[]{workspaceInformation.getBootRepositories()}).toArray();
        PrivateNutsRepositorySelection[] result = null;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositories.resolveSelectors(new PrivateNutsRepositorySelection[]{
                    new PrivateNutsRepositorySelection("maven-local", null),
                    new PrivateNutsRepositorySelection("maven-central", null),
            });
        } else {
            result = bootRepositories.resolveSelectors(Arrays.stream(old).map(x -> new PrivateNutsRepositorySelection(x.getName(), x.getUrl()))
                    .toArray(PrivateNutsRepositorySelection[]::new));
        }
        return parsedBootRuntimeDependenciesRepositories
                = Arrays.stream(result)
                .map(x -> x.getUrl())
                .collect(Collectors.toSet());
    }

    public String[] createProcessCommandLine() {
        prepareWorkspace();
        LOG.log(Level.FINE, NutsLogVerb.START, NutsMessage.jstyle("running version {0}.  {1}", workspaceInformation.getApiVersion(), getRequirementsHelpString(true)));
        StringBuilder errors = new StringBuilder();
        String defaultWorkspaceLibFolder = workspaceInformation.getStoreLocation(NutsStoreLocation.LIB);
        List<String> repos = new ArrayList<>();
        repos.add(defaultWorkspaceLibFolder);
        Collection<String> bootRepositories = resolveBootRuntimeDependenciesRepositories();
        repos.addAll(bootRepositories);
        PrivateNutsErrorInfoList errorList = new PrivateNutsErrorInfoList();
        File file = PrivateNutsUtilMaven.resolveOrDownloadJar(
                new NutsBootId("net.thevpc.nuts", "nuts", NutsBootVersion.parse(workspaceInformation.getApiVersion())),
                repos.toArray(new String[0]),
                workspaceInformation.getLib(), LOG,
                false,
                options.getExpireTime(),
                errorList
        );
        if (file == null) {
            errors.append("unable to load nuts ").append(workspaceInformation.getApiVersion()).append("\n");
            for (PrivateNutsErrorInfo errorInfo : errorList.list()) {
                errors.append(errorInfo.toString()).append("\n");
            }
            showError(workspaceInformation,
                    options.getWorkspace(), null,
                    errors.toString(),
                    errorList
            );
            throw new NutsBootException(
                    NutsMessage.cstyle("unable to load %s#%s", NutsConstants.Ids.NUTS_API, workspaceInformation.getApiVersion())
            );
        }

        List<String> cmd = new ArrayList<>();
        String jc = workspaceInformation.getJavaCommand();
        if (jc == null || jc.trim().isEmpty()) {
            jc = PrivateNutsUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : PrivateNutsCommandLine.parseCommandLineArray(options.getJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        if (workspaceInformation.getJavaOptions() != null) {
            Collections.addAll(cmd, PrivateNutsCommandLine.parseCommandLineArray(workspaceInformation.getJavaOptions()));
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(Arrays.asList(
                options.formatter().setCompact(true).setApiVersion(workspaceInformation.getApiVersion()).getBootCommandLine()
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
            LOG.log(Level.FINE, NutsLogVerb.START, NutsMessage.jstyle("[exec] {0}", sb));
        }
        return cmd.toArray(new String[0]);
    }

    public NutsWorkspaceOptions getOptions() {
        return options;
    }

    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {
            preparedWorkspace = true;
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("bootstrap Nuts version {0} - digest {1}...", Nuts.getVersion(), getApiDigest()));
                LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("boot-class-path:"));
                for (String s : System.getProperty("java.class.path").split(File.pathSeparator)) {
                    LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("                  {0}", s));
                }
                ClassLoader thisClassClassLoader = getClass().getClassLoader();
                LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("class-loader: {0}", thisClassClassLoader));
                for (URL url : PrivateNutsUtilClassLoader.resolveClasspathURLs(thisClassClassLoader, false)) {
                    LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("                 {0}", url));
                }
                ClassLoader tctxloader = Thread.currentThread().getContextClassLoader();
                if (tctxloader != thisClassClassLoader) {
                    LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("thread-class-loader: {0}", tctxloader));
                    for (URL url : PrivateNutsUtilClassLoader.resolveClasspathURLs(tctxloader, false)) {
                        LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("                 {0}", url));
                    }
                }
                ClassLoader contextClassLoader = getContextClassLoader();
                LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("ctx-class-loader: {0}", contextClassLoader));
                if (contextClassLoader != null) {
                    for (URL url : PrivateNutsUtilClassLoader.resolveClasspathURLs(contextClassLoader, false)) {
                        LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("                 {0}", url));
                    }
                }
                LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("system-properties:", contextClassLoader));
                Map<String, String> m = (Map) System.getProperties();
                int max = m.keySet().stream().mapToInt(String::length).max().getAsInt();
                for (Map.Entry<String, String> e : m.entrySet()) {
                    LOG.log(Level.CONFIG, NutsLogVerb.START, NutsMessage.jstyle("    {0} = {1}",
                            PrivateNutsUtils.leftAlign(e.getKey(), max),
                            PrivateNutsUtils.compressString(e.getValue())
                    ));
                }
            }
            workspaceInformation = new PrivateNutsWorkspaceInitInformation();
            workspaceInformation.setOptions(options);

            String _ws = options.getWorkspace();
            String workspaceName;
            String lastNutsWorkspaceJsonConfigPath = null;
            boolean immediateLocation = false;
            PrivateNutsWorkspaceInitInformation lastConfigLoaded = null;
            if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                //this is a protocol based workspace
                //String protocol=ws.substring(0,ws.indexOf("://"));
                workspaceName = "remote-bootstrap";
                lastNutsWorkspaceJsonConfigPath = NutsUtilPlatforms.getWorkspaceLocation(null,
                        workspaceInformation.isGlobal(),
                        PrivateNutsUtils.resolveValidWorkspaceName(workspaceName));
                lastConfigLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, LOG);
                immediateLocation = true;

            } else {
                immediateLocation = PrivateNutsUtils.isValidWorkspaceName(_ws);
                int maxDepth = 36;
                for (int i = 0; i < maxDepth; i++) {
                    lastNutsWorkspaceJsonConfigPath
                            = PrivateNutsUtils.isValidWorkspaceName(_ws)
                            ? NutsUtilPlatforms.getWorkspaceLocation(
                            null,
                            workspaceInformation.isGlobal(),
                            PrivateNutsUtils.resolveValidWorkspaceName(_ws)
                    ) : PrivateNutsUtilIO.getAbsolutePath(_ws);

                    PrivateNutsWorkspaceInitInformation configLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, LOG);
                    if (configLoaded == null) {
                        //not loaded
                        break;
                    }
                    if (NutsBlankable.isBlank(configLoaded.getWorkspaceLocation())) {
                        lastConfigLoaded = configLoaded;
                        break;
                    }
                    _ws = configLoaded.getWorkspaceLocation();
                    if (i >= maxDepth - 1) {
                        throw new NutsBootException(NutsMessage.cstyle("cyclic workspace resolution"));
                    }
                }
                workspaceName = PrivateNutsUtils.resolveValidWorkspaceName(options.getWorkspace());
            }
            workspaceInformation.setWorkspaceLocation(lastNutsWorkspaceJsonConfigPath);
            if (lastConfigLoaded != null) {
                workspaceInformation.setWorkspaceLocation(lastNutsWorkspaceJsonConfigPath);
                workspaceInformation.setName(lastConfigLoaded.getName());
                workspaceInformation.setUuid(lastConfigLoaded.getUuid());
                if (!options.isReset()) {
                    workspaceInformation.setBootRepositories(lastConfigLoaded.getBootRepositories());
                    workspaceInformation.setJavaCommand(lastConfigLoaded.getJavaCommand());
                    workspaceInformation.setJavaOptions(lastConfigLoaded.getJavaOptions());
                    workspaceInformation.setExtensionsSet(PrivateNutsUtils.copy(lastConfigLoaded.getExtensionsSet()));
                    workspaceInformation.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
                    workspaceInformation.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
                    workspaceInformation.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
                    workspaceInformation.setStoreLocations(PrivateNutsUtils.copy(lastConfigLoaded.getStoreLocations()));
                    workspaceInformation.setHomeLocations(PrivateNutsUtils.copy(lastConfigLoaded.getHomeLocations()));
                } else {
                    lastWorkspaceInformation = new PrivateNutsWorkspaceInitInformation();
                    lastWorkspaceInformation.setWorkspaceLocation(lastNutsWorkspaceJsonConfigPath);
                    lastWorkspaceInformation.setName(lastConfigLoaded.getName());
                    lastWorkspaceInformation.setUuid(lastConfigLoaded.getUuid());
                    lastWorkspaceInformation.setBootRepositories(lastConfigLoaded.getBootRepositories());
                    lastWorkspaceInformation.setJavaCommand(lastConfigLoaded.getJavaCommand());
                    lastWorkspaceInformation.setJavaOptions(lastConfigLoaded.getJavaOptions());
                    lastWorkspaceInformation.setExtensionsSet(PrivateNutsUtils.copy(lastConfigLoaded.getExtensionsSet()));
                    lastWorkspaceInformation.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
                    lastWorkspaceInformation.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
                    lastWorkspaceInformation.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
                    lastWorkspaceInformation.setStoreLocations(PrivateNutsUtils.copy(lastConfigLoaded.getStoreLocations()));
                    lastWorkspaceInformation.setHomeLocations(PrivateNutsUtils.copy(lastConfigLoaded.getHomeLocations()));
                }
            }
            revalidateLocations(workspaceInformation, workspaceName, immediateLocation);
            workspaceInformation.setApiVersion(options.getApiVersion());
            long countDeleted = 0;
            //now that config information is prepared proceed to any cleanup
            if (options.isReset()) {
                if (lastWorkspaceInformation != null) {
                    revalidateLocations(lastWorkspaceInformation, workspaceName, immediateLocation);
                    if (options.isDry()) {
                        LOG.log(Level.INFO, NutsLogVerb.DEBUG, NutsMessage.jstyle("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        LOG.log(Level.CONFIG, NutsLogVerb.WARNING, NutsMessage.jstyle("reset workspace"));
                        countDeleted = PrivateNutsUtilDeleteFiles.deleteStoreLocations(lastWorkspaceInformation, getOptions(), true, LOG, NutsStoreLocation.values());
                        PrivateNutsUtilLauncher.ndiUndo(LOG);
                    }
                } else {
                    if (options.isDry()) {
                        LOG.log(Level.INFO, NutsLogVerb.DEBUG, NutsMessage.jstyle("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        LOG.log(Level.CONFIG, NutsLogVerb.WARNING, NutsMessage.jstyle("reset workspace"));
                        countDeleted = PrivateNutsUtilDeleteFiles.deleteStoreLocations(workspaceInformation, getOptions(), true, LOG, NutsStoreLocation.values());
                        PrivateNutsUtilLauncher.ndiUndo(LOG);
                    }
                }
            } else if (options.isRecover()) {
                if (options.isDry()) {
                    LOG.log(Level.INFO, NutsLogVerb.DEBUG, NutsMessage.jstyle("[dry] [recover] delete CACHE/TEMP workspace folders"));
                } else {
                    LOG.log(Level.CONFIG, NutsLogVerb.WARNING, NutsMessage.jstyle("recover workspace."));
                    List<Object> folders = new ArrayList<>();
                    folders.add(NutsStoreLocation.CACHE);
                    folders.add(NutsStoreLocation.TEMP);
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = PrivateNutsUtilDeleteFiles.getStoreLocationPath(workspaceInformation, NutsStoreLocation.LIB);
                    if (p != null) {
                        folders.add(new File(p, "id/net/thevpc/nuts/nuts"));
                        folders.add(new File(p, "id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = PrivateNutsUtilDeleteFiles.deleteStoreLocations(workspaceInformation, getOptions(), false, LOG, folders.toArray());
                }
            }
            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (options.getApplicationArguments().length == 0 && options.isSkipBoot()
                    && (options.isRecover() || options.isReset())) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        LOG.log(Level.WARNING, NutsLogVerb.WARNING, NutsMessage.jstyle("workspace erased : {0}", workspaceInformation.getWorkspaceLocation()));
                    } else {
                        LOG.log(Level.WARNING, NutsLogVerb.WARNING, NutsMessage.jstyle("workspace is not erased because it does not exist : {0}", workspaceInformation.getWorkspaceLocation()));
                    }
                }
                throw new NutsBootException(NutsMessage.cstyle(""), 0);
            }
            //after eventual clean up
            if (options.isInherited()) {
                //when Inherited, always use the current Api version!
                workspaceInformation.setApiVersion(Nuts.getVersion());
            } else {
                if (NutsConstants.Versions.LATEST.equalsIgnoreCase(workspaceInformation.getApiVersion())
                        || NutsConstants.Versions.RELEASE.equalsIgnoreCase(workspaceInformation.getApiVersion())
                ) {
                    NutsBootId s = PrivateNutsUtilMaven.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_API), null, LOG, resolveBootRuntimeDependenciesRepositories());
                    if (s == null) {
                        throw new NutsBootException(NutsMessage.plain("unable to load latest nuts version"));
                    }
                    workspaceInformation.setApiVersion(s.getVersion().toString());
                }
                if (NutsBlankable.isBlank(workspaceInformation.getApiVersion())) {
                    workspaceInformation.setApiVersion(Nuts.getVersion());
                }
            }

            String confStoreLocationString = workspaceInformation.getStoreLocation(NutsStoreLocation.CONFIG);
            Path nutsApiConfigJsonPath = confStoreLocationString == null ? null : Paths.get(confStoreLocationString)
                    .resolve(NutsConstants.Folders.ID)
                    .resolve("net/thevpc/nuts/nuts").resolve(workspaceInformation.getApiVersion()).resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(nutsApiConfigJsonPath, options.getExpireTime(), LOG)) {
                try {
                    Map<String, Object> obj = PrivateNutsJsonParser.parse(nutsApiConfigJsonPath);
                    LOG.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("loaded {0} file : {1}", NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME, nutsApiConfigJsonPath.toString()));
                    loadedApiConfig = true;
                    if (workspaceInformation.getRuntimeId() == null) {
                        String runtimeId = (String) obj.get("runtimeId");
                        if (NutsBlankable.isBlank(runtimeId)) {
                            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("{0} does not contain runtime-id", NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME));
                        }
                        workspaceInformation.setRuntimeId(NutsBootId.parse(runtimeId));
                    }
                    if (workspaceInformation.getBootRepositories() == null) {
                        workspaceInformation.setBootRepositories((String) obj.get("bootRepositories"));
                    }
                    if (workspaceInformation.getJavaCommand() == null) {
                        workspaceInformation.setJavaCommand((String) obj.get("javaCommand"));
                    }
                    if (workspaceInformation.getJavaOptions() == null) {
                        workspaceInformation.setJavaOptions((String) obj.get("javaOptions"));
                    }
                } catch (UncheckedIOException | NutsIOException e) {
                    LOG.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("unable to read {0}", nutsApiConfigJsonPath));
                }
            }
            if (!loadedApiConfig || workspaceInformation.getRuntimeId() == null
                    || workspaceInformation.getRuntimeBootDescriptor() == null
                    || workspaceInformation.getExtensionBootDescriptors() == null
                    || workspaceInformation.getBootRepositories() == null) {

                //resolve extension id
                if (workspaceInformation.getRuntimeId() == null) {
                    String apiVersion = workspaceInformation.getApiId().substring(workspaceInformation.getApiId().indexOf('#') + 1);
                    NutsBootId runtimeId = PrivateNutsUtilMaven.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getFrom().startsWith(apiVersion + "."), LOG, resolveBootRuntimeRepositories());
                    if (runtimeId != null) {
                        //LOG.log(Level.FINEST, "[success] Resolved latest runtime-id : {0}", new Object[]{runtimeId});
                    } else {
                        LOG.log(Level.FINEST, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to resolve latest runtime-id version (is connection ok?)"));
                    }
                    workspaceInformation.setRuntimeId(runtimeId);
                    workspaceInformation.setRuntimeBootDescriptor(null);
                    workspaceInformation.setBootRepositories(null);
                }
                if (workspaceInformation.getRuntimeId() == null) {
                    workspaceInformation.setRuntimeId(
                            new NutsBootId("net.thevpc.nuts", "nuts-runtime",
                                    NutsBootVersion.parse(workspaceInformation.getApiVersion() + ".0")
                            )
                    );
                    LOG.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("consider default runtime-id : {0}", workspaceInformation.getRuntimeId()));
                }
                if (workspaceInformation.getRuntimeId().getVersion().isBlank()) {
                    workspaceInformation.setRuntimeId(
                            new NutsBootId(workspaceInformation.getRuntimeId().getGroupId(), workspaceInformation.getRuntimeId().getArtifactId(),
                                    NutsBootVersion.parse(workspaceInformation.getApiVersion() + ".0"))
                    );
                }

                //resolve runtime libraries
                if (workspaceInformation.getRuntimeBootDescriptor() == null) {
                    Set<NutsBootId> loadedDeps = null;
                    String extraBootRepositories = null;
                    NutsBootId rid = workspaceInformation.getRuntimeId();
                    try {
                        Path nutsRuntimeCacheConfigPath = Paths.get(workspaceInformation.getCacheBoot())
                                .resolve(PrivateNutsUtils.idToPath(rid)).resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME);
                        boolean cacheLoaded = false;
                        if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(nutsRuntimeCacheConfigPath, options.getExpireTime(), LOG)) {
                            try {
                                Map<String, Object> obj = PrivateNutsJsonParser.parse(nutsRuntimeCacheConfigPath);
                                LOG.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("loaded {0} file : {1}", NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, nutsRuntimeCacheConfigPath.toString()));
                                loadedDeps = PrivateNutsUtils.parseDependencies((String) obj.get("dependencies"));
                                extraBootRepositories = (String) obj.get("bootRepositories");
                            } catch (Exception ex) {
                                LOG.log(Level.FINEST, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load {0} file : {1} : {2}", NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, nutsRuntimeCacheConfigPath.toString(), ex.toString()));
                                //ignore...
                            }
                            cacheLoaded = true;
                        }

                        if (!cacheLoaded || loadedDeps == null) {
                            PrivateNutsUtils.Deps depsAndRepos = PrivateNutsUtilMaven.loadDependencies(workspaceInformation.getRuntimeId(),
                                    LOG, resolveBootRuntimeRepositories());
                            if (depsAndRepos != null) {
                                loadedDeps = depsAndRepos.deps;
                                extraBootRepositories = String.join(";", depsAndRepos.repos);
                                LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("detect runtime dependencies : {0}", depsAndRepos.deps));
                                LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("detect runtime repos        : {0}", depsAndRepos.repos));
                            }
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.FINEST, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load {0} file : {1}", NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, ex.toString()));
                        //
                    }
                    if (loadedDeps == null) {
                        throw new NutsBootException(NutsMessage.cstyle("unable to load dependencies for %s", rid));
                    }
                    workspaceInformation.setRuntimeBootDescriptor(new NutsBootDescriptor(
                            workspaceInformation.getRuntimeId(),
                            loadedDeps.toArray(new NutsBootId[0])
                    ));
                    LinkedHashSet<String> bootRepositories =
                            Arrays.stream((extraBootRepositories == null ? "" : extraBootRepositories).split(";"))
                                    .map(String::trim).filter(x -> x.length() > 0)
                                    .collect(Collectors.toCollection(LinkedHashSet::new));
                    bootRepositories.addAll(resolveBootRuntimeRepositories());
                    if (LOG.isLoggable(Level.CONFIG)) {
                        if (bootRepositories.size() == 0) {
                            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("workspace bootRepositories could not be resolved"));
                        } else if (bootRepositories.size() == 1) {
                            LOG.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("workspace bootRepositories resolved to : {0}", bootRepositories.toArray()[0]));
                        } else {
                            LOG.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("workspace bootRepositories resolved to : "));
                            for (String repository : bootRepositories) {
                                LOG.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("    {0}", repository));
                            }
                        }
                    }
                    workspaceInformation.setBootRepositories(String.join(";", bootRepositories));
                }

                //resolve extension libraries
                if (workspaceInformation.getExtensionBootDescriptors() == null) {
//                    LinkedHashSet<String> allExtDependencies = new LinkedHashSet<>();
                    LinkedHashSet<String> excludedExtensions = new LinkedHashSet<>();
                    if (options.getExcludedExtensions() != null) {
                        for (String excludedExtensionGroup : options.getExcludedExtensions()) {
                            for (String excludedExtension : excludedExtensionGroup.split("[;, ]")) {
                                if (excludedExtension.length() > 0) {
                                    excludedExtensions.add(NutsBootId.parse(excludedExtension).getShortName());
                                }
                            }
                        }
                    }
                    if (workspaceInformation.getExtensionsSet() != null) {
                        List<NutsBootDescriptor> all = new ArrayList<>();
                        for (String extension : workspaceInformation.getExtensionsSet()) {
                            NutsBootId eid = NutsBootId.parse(extension);
                            if (!excludedExtensions.contains(eid.getShortName()) && !excludedExtensions.contains(eid.getArtifactId())) {
                                Path extensionFile = Paths.get(workspaceInformation.getCacheBoot())
                                        .resolve(PrivateNutsUtils.idToPath(eid)).resolve(NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME);
                                Set<NutsBootId> loadedDeps = null;
                                if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(extensionFile, options.getExpireTime(), LOG)) {
                                    try {
                                        Map<String, Object> obj = PrivateNutsJsonParser.parse(nutsApiConfigJsonPath);
                                        LOG.log(Level.CONFIG, NutsLogVerb.READ, NutsMessage.jstyle("loaded {0} file : {1}", NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME, extensionFile.toString()));
                                        loadedDeps = PrivateNutsUtils.parseDependencies((String) obj.get("dependencies"));
                                    } catch (Exception ex) {
                                        LOG.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load {0} file : {1} : {2}", NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME, extensionFile.toString(), ex.toString()));
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    PrivateNutsUtils.Deps depsAndRepos = PrivateNutsUtilMaven.loadDependencies(eid, LOG, resolveBootRuntimeDependenciesRepositories());
                                    if (depsAndRepos != null) {
                                        loadedDeps = depsAndRepos.deps;
                                    }
                                }
                                if (loadedDeps != null) {
                                    all.add(new NutsBootDescriptor(NutsBootId.parse(extension), loadedDeps.toArray(new NutsBootId[0])));
                                } else {
                                    throw new NutsBootException(NutsMessage.cstyle("unable to load dependencies for %s", eid));
                                }
                            }
                        }
                        workspaceInformation.setExtensionBootDescriptors(all.toArray(new NutsBootDescriptor[0]));
                    } else {
                        workspaceInformation.setExtensionBootDescriptors(new NutsBootDescriptor[0]);
                    }
                }
            }

            newInstanceRequirements = checkRequirements(true);
            if (newInstanceRequirements == 0) {
                workspaceInformation.setJavaCommand(null);
                workspaceInformation.setJavaOptions(null);
            }
            this.contextClassLoaderSupplier = options.getClassLoaderSupplier() == null ? () -> Thread.currentThread().getContextClassLoader()
                    : options.getClassLoaderSupplier();
            return true;
        }
        return false;
    }

    private boolean isPlainTrace() {
        return options.isTrace() && !options.isBot()
                && (options.getOutputFormat() == NutsContentType.PLAIN
                || options.getOutputFormat() == null
        );
    }


    private boolean isLoadFromCache() {
        return !options.isRecover() && !options.isReset();
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
        if (options.getApplicationArguments().length == 0 && options.isSkipBoot()
                && (options.isRecover() || options.isReset())) {
            if (isPlainTrace()) {
                LOG.log(Level.WARNING, NutsLogVerb.WARNING, NutsMessage.jstyle("workspace erased : {0}", workspaceInformation.getWorkspaceLocation()));
            }
            throw new NutsBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
        PrivateNutsErrorInfoList errorList = new PrivateNutsErrorInfoList();
        try {
            if (options.getOpenMode() == NutsOpenMode.OPEN_OR_ERROR) {
                //add fail fast test!!
                if (!new File(workspaceInformation.getWorkspaceLocation(), NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME).isFile()) {
                    throw new NutsWorkspaceNotFoundException(workspaceInformation.getWorkspaceLocation());
                }
            } else if (options.getOpenMode() == NutsOpenMode.CREATE_OR_ERROR) {
                if (new File(workspaceInformation.getWorkspaceLocation(), NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME).exists()) {
                    throw new NutsWorkspaceAlreadyExistsException(workspaceInformation.getWorkspaceLocation());
                }
            }
            if (NutsBlankable.isBlank(workspaceInformation.getApiId())
                    || workspaceInformation.getRuntimeId() == null
                    || NutsBlankable.isBlank(workspaceInformation.getBootRepositories())
                    || workspaceInformation.getRuntimeBootDescriptor() == null
                    || workspaceInformation.getExtensionBootDescriptors() == null) {
                throw new NutsBootException(NutsMessage.plain("invalid workspace state"));
            }
            boolean recover = options.isRecover() || options.isReset();

            List<NutsClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = workspaceInformation.getLib();

            String[] repositories =
                    Arrays.stream((workspaceInformation.getBootRepositories() == null ? "" : workspaceInformation.getBootRepositories())
                            .split("[\n;]")
                    ).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);

            workspaceInformation.setRuntimeBootDependencyNode(
                    createClassLoaderNode(workspaceInformation.getRuntimeBootDescriptor(),
                            repositories, workspaceBootLibFolder, recover, errorList, true));

            for (NutsBootDescriptor nutsBootDescriptor : workspaceInformation.getExtensionBootDescriptors()) {
                deps.add(createClassLoaderNode(nutsBootDescriptor, repositories, workspaceBootLibFolder, recover,
                        errorList, false));
            }
            workspaceInformation.setExtensionBootDependencyNodes(deps.toArray(new NutsClassLoaderNode[0]));
            deps.add(0, workspaceInformation.getRuntimeBootDependencyNode());

            bootClassWorldURLs = PrivateNutsUtilClassLoader.resolveClassWorldURLs(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader(), LOG);
            workspaceClassLoader = /*bootClassWorldURLs.length == 0 ? getContextClassLoader() : */
                    new PrivateNutsBootClassLoader(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader());
            workspaceInformation.setWorkspaceClassLoader(workspaceClassLoader);
            if (LOG.isLoggable(Level.CONFIG)) {
                if (bootClassWorldURLs.length == 0) {
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("empty nuts class world. All dependencies are already loaded in classpath, most likely"));
                } else if (bootClassWorldURLs.length == 1) {
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("resolve nuts class world to : {0} {1}",
                            PrivateNutsUtilDigest.getURLDigest(bootClassWorldURLs[0], LOG), bootClassWorldURLs[0]));
                } else {
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("resolve nuts class world is to : "));
                    for (URL u : bootClassWorldURLs) {
                        LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("    {0} : {1}",
                                PrivateNutsUtilDigest.getURLDigest(u, LOG), u));
                    }
                }
            }
            workspaceInformation.setBootClassWorldURLs(bootClassWorldURLs);
            LOG.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("search for NutsBootWorkspaceFactory service implementations"));
            ServiceLoader<NutsBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsBootWorkspaceFactory.class, workspaceClassLoader);
            List<NutsBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NutsBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new PrivateNutsBootWorkspaceFactoryComparator(options));
            if (LOG.isLoggable(Level.CONFIG)) {
                if (factories.isEmpty()) {
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("unable to detect NutsBootWorkspaceFactory service implementations"));
                } else if (factories.size() == 1) {
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("detect NutsBootWorkspaceFactory service implementation : {0}", factories.get(0).getClass().getName()));
                } else {
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("detect NutsBootWorkspaceFactory service implementations are :"));
                    for (NutsBootWorkspaceFactory u : factories) {
                        LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("    {0}",
                                u.getClass().getName()));
                    }
                }
            }
            NutsBootWorkspaceFactory factoryInstance = null;
            List<Throwable> exceptions = new ArrayList<>();
            for (NutsBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    if (LOG.isLoggable(Level.CONFIG)) {
                        LOG.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("create workspace using {0}", factoryInstance.getClass().getName()));
                    }
                    workspaceInformation.setBootWorkspaceFactory(factoryInstance);
                    nutsWorkspace = a.createWorkspace(workspaceInformation);
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    LOG.log(Level.SEVERE, NutsMessage.jstyle("unable to create workspace using factory {0}", a), ex);
                }
                if (nutsWorkspace != null) {
                    break;
                }
            }
            if (nutsWorkspace == null) {
                //should never happen
                LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load Workspace \"{0}\" from ClassPath :", workspaceInformation.getName()));
                for (URL url : bootClassWorldURLs) {
                    LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("\t {0}", PrivateNutsUtils.formatURL(url)));
                }
                for (Throwable exception : exceptions) {
                    LOG.log(Level.SEVERE, NutsMessage.jstyle("{0}", exception), exception);
                }
                LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load Workspace Component from ClassPath : {0}", Arrays.asList(bootClassWorldURLs)));
                throw new NutsInvalidWorkspaceException(this.workspaceInformation.getWorkspaceLocation(),
                        NutsMessage.cstyle("unable to load Workspace Component from ClassPath : %s%n  caused by:%n\t%s"
                                , Arrays.asList(bootClassWorldURLs),
                                exceptions.stream().map(Throwable::toString).collect(Collectors.joining("\n\t"))
                        )
                );
            }
//            LOG2 = nutsWorkspace.log().of(NutsBootWorkspace.class);
//            if (LOG2.isLoggable(Level.FINE)) {
//                LOG2.with().session(nutsWorkspace.createSession())
//                        .level(Level.FINE).verb(NutsLogVerb.SUCCESS).log("end initialize workspace");
//            }
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
            showError(workspaceInformation,
                    options.getWorkspace(),
                    bootClassWorldURLs,
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


    private Supplier<ClassLoader> getContextClassLoaderSupplier() {
        return contextClassLoaderSupplier;
    }

    protected ClassLoader getContextClassLoader() {
        Supplier<ClassLoader> currentContextClassLoaderProvider = getContextClassLoaderSupplier();
        if (currentContextClassLoaderProvider == null) {
            return null;
        }
        return currentContextClassLoaderProvider.get();
    }

    private String getWorkspaceRunModeString() {
        if (this.getOptions().isReset()) {
            return "reset";
        } else if (this.getOptions().isRecover()) {
            return "recover";
        } else {
            return "exec";
        }
    }

    private void runCommandHelp() {
        NutsContentType f = options.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            printDryCommand("help");
        } else {
            String msg = "nuts is an open source package manager mainly for java applications. Type 'nuts help' or visit https://github.com/thevpc/nuts for more help.";
            switch (f) {
                case JSON: {
                    LOG.outln("{");
                    LOG.outln("  \"help\": \"%s\"", msg);
                    LOG.outln("}");
                    return;
                }
                case TSON: {
                    LOG.outln("{");
                    LOG.outln("  help: \"%s\"", msg);
                    LOG.outln("}");
                    return;
                }
                case YAML: {
                    LOG.outln("help: %s", msg);
                    return;
                }
                case TREE: {
                    LOG.outln("- help: %s", msg);
                    return;
                }
                case TABLE: {
                    LOG.outln("help  %s", msg);
                    return;
                }
                case XML: {
                    LOG.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    LOG.outln("<string>");
                    LOG.outln(" %s", msg);
                    LOG.outln("</string>");
                    return;
                }
                case PROPS: {
                    LOG.outln("help=%s", msg);
                    return;
                }
            }
            LOG.outln("%s", msg);
        }
    }

    private void printDryCommand(String cmd) {
        NutsContentType f = options.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            switch (f) {
                case JSON: {
                    LOG.outln("{");
                    LOG.outln("  \"dryCommand\": \"%s\"", cmd);
                    LOG.outln("}");
                    return;
                }
                case TSON: {
                    LOG.outln("{");
                    LOG.outln("  dryCommand: \"%s\"", cmd);
                    LOG.outln("}");
                    return;
                }
                case YAML: {
                    LOG.outln("dryCommand: %s", cmd);
                    return;
                }
                case TREE: {
                    LOG.outln("- dryCommand: %s", cmd);
                    return;
                }
                case TABLE: {
                    LOG.outln("dryCommand  %s", cmd);
                    return;
                }
                case XML: {
                    LOG.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    LOG.outln("<object>");
                    LOG.outln("  <string key=\"%s\" value=\"%s\"/>", "dryCommand", cmd);
                    LOG.outln("</object>");
                    return;
                }
                case PROPS: {
                    LOG.outln("dryCommand=%s", cmd);
                    return;
                }
            }
            LOG.outln("[Dry] %s", Nuts.getVersion());
        }
    }

    private void runCommandVersion() {
//        if (options.isDry()) {
//            session.out().println("[boot-internal-command] show-version");
//        } else {
//            session.out().println("nuts-version :" + Nuts.getVersion());
//        }
        NutsContentType f = options.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            printDryCommand("version");
            return;
        }
        switch (f) {
            case JSON: {
                LOG.outln("{");
                LOG.outln("  \"version\": \"%s\",", Nuts.getVersion());
                LOG.outln("  \"digest\": \"%s\"", getApiDigest());
                LOG.outln("}");
                return;
            }
            case TSON: {
                LOG.outln("{");
                LOG.outln("  version: \"%s\",", Nuts.getVersion());
                LOG.outln("  digest: \"%s\"", getApiDigest());
                LOG.outln("}");
                return;
            }
            case YAML: {
                LOG.outln("version: %s", Nuts.getVersion());
                LOG.outln("digest: %s", getApiDigest());
                return;
            }
            case TREE: {
                LOG.outln("- version: %s", Nuts.getVersion());
                LOG.outln("- digest: %s", getApiDigest());
                return;
            }
            case TABLE: {
                LOG.outln("version      %s", Nuts.getVersion());
                LOG.outln("digest  %s", getApiDigest());
                return;
            }
            case XML: {
                LOG.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                LOG.outln("<object>");
                LOG.outln("  <string key=\"%s\" value=\"%s\"/>", "version", Nuts.getVersion());
                LOG.outln("  <string key=\"%s\" value=\"%s\"/>", "digest", getApiDigest());
                LOG.outln("</object>");
                return;
            }
            case PROPS: {
                LOG.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                LOG.outln("version=%s", Nuts.getVersion());
                LOG.outln("digest=%s", getApiDigest());
                LOG.outln("</object>");
                return;
            }
        }
        LOG.outln("%s", Nuts.getVersion());
    }

    public NutsSession runWorkspace() {
        if (options.isCommandHelp()) {
            runCommandHelp();
            return null;
        } else if (options.isCommandVersion()) {
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
        NutsWorkspaceOptions o = this.getOptions();
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NutsBootException(
                    NutsMessage.cstyle("workspace not available to run : %s",
                            new PrivateNutsCommandLine(o.getApplicationArguments())
                    )
            );
        }

        session.setAppId(workspace.getApiId());
        if (LOG2 == null) {
            LOG2 = NutsLogger.of(NutsBootWorkspace.class, session);
            LOG2_SESSION = session;
        }
        NutsLoggerOp logOp = LOG2.with().session(session).level(Level.CONFIG);
        logOp.verb(NutsLogVerb.SUCCESS).log(
                NutsMessage.jstyle("running workspace in {0} mode", getWorkspaceRunModeString())
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
                    .setDry(options.isDry())
                    .run();
        } else {
            session.exec()
                    .addCommand(o.getApplicationArguments())
                    .addExecutorOptions(o.getExecutorOptions())
                    .setExecutionType(o.getExecutionType())
                    .setFailFast(true)
                    .setDry(options.isDry())
                    .run();
        }
        return session;
    }

    private void fallbackInstallActionUnavailable(String message) {
        //term.outln("%s", message);
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle(message));
    }

    private void showError(PrivateNutsWorkspaceInitInformation actualBootConfig, String workspace, URL[] bootClassWorldURLs, String extraMessage, PrivateNutsErrorInfoList ths) {
        Map<NutsStoreLocation, String> rbc_locations = actualBootConfig.getStoreLocations();
        if (rbc_locations == null) {
            rbc_locations = Collections.emptyMap();
        }
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to bootstrap nuts (hash {0}):", getApiDigest()));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("{0}", extraMessage));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("here after current environment info:"));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-boot-api-version            : {0}", PrivateNutsUtils.coalesce(actualBootConfig.getApiVersion(), "<?> Not Found!")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-boot-runtime                : {0}", PrivateNutsUtils.coalesce(actualBootConfig.getRuntimeId(), "<?> Not Found!")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-boot-repositories           : {0}", PrivateNutsUtils.coalesce(actualBootConfig.getBootRepositories(), "<?> Not Found!")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  workspace-location               : {0}", PrivateNutsUtils.coalesce(workspace, "<default-location>")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-apps                  : {0}", rbc_locations.get(NutsStoreLocation.APPS)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-config                : {0}", rbc_locations.get(NutsStoreLocation.CONFIG)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-var                   : {0}", rbc_locations.get(NutsStoreLocation.VAR)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-log                   : {0}", rbc_locations.get(NutsStoreLocation.LOG)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-temp                  : {0}", rbc_locations.get(NutsStoreLocation.TEMP)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-cache                 : {0}", rbc_locations.get(NutsStoreLocation.CACHE)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-run                   : {0}", rbc_locations.get(NutsStoreLocation.RUN)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-lib                   : {0}", rbc_locations.get(NutsStoreLocation.LIB)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-strategy              : {0}", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationStrategy())));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-store-layout                : {0}", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationLayout())));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-boot-args                   : {0}", options.formatter().getBootCommandLine()));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-app-args                    : {0}", Arrays.toString(options.getApplicationArguments())));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  option-read-only                 : {0}", options.isReadOnly()));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  option-trace                     : {0}", options.isTrace()));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  option-progress                  : {0}", PrivateNutsUtils.desc(options.getProgressOptions())));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  option-open-mode                 : {0}", PrivateNutsUtils.desc(options.getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : options.getOpenMode())));

        NutsClassLoaderNode rtn = workspaceInformation.getRuntimeBootDependencyNode();
        String rtHash = "";
        if (rtn != null) {
            rtHash = PrivateNutsUtilDigest.getURLDigest(rtn.getURL(), LOG);
        }
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-runtime-digest                : {0}", rtHash));

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-runtime-classpath           : {0}", "<none>"));
        } else {
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-runtime-hash                : {0}", "<none>"));
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  nuts-runtime-classpath           : {0}", PrivateNutsUtils.formatURL(bootClassWorldURL)));
                } else {
                    LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("                                     {0}", PrivateNutsUtils.formatURL(bootClassWorldURL)));
                }
            }
        }
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java-version                     : {0}", System.getProperty("java.version")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java-executable                  : {0}", PrivateNutsUtils.resolveJavaCommand(null)));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java-class-path                  : {0}", System.getProperty("java.class.path")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java-library-path                : {0}", System.getProperty("java.library.path")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  os-name                          : {0}", System.getProperty("os.name")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  os-arch                          : {0}", System.getProperty("os.arch")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  os-version                       : {0}", System.getProperty("os.version")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  user-name                        : {0}", System.getProperty("user.name")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  user-home                        : {0}", System.getProperty("user.home")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  user-dir                         : {0}", System.getProperty("user.dir")));
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle(""));
        if (options.getLogConfig() == null
                || options.getLogConfig().getLogTermLevel() == null
                || options.getLogConfig().getLogFileLevel().intValue() > Level.FINEST.intValue()) {
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("If the problem persists you may want to get more debug info by adding '--verbose' arguments."));
        }
        if (!options.isReset() && !options.isRecover() && options.getExpireTime() == null) {
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments."));
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("Here is the proper command : "));
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java -jar nuts.jar --verbose --recover --expire [...]"));
        } else if (!options.isReset() && options.isRecover() && options.getExpireTime() != null) {
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("You may also enable full reset mode to ignore existing configuration with '--reset' argument."));
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("ATTENTION: this will delete all your nuts configuration. Use it at your own risk."));
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("Here is the proper command : "));
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("  java -jar nuts.jar --verbose --reset [...]"));
        }
        if (!ths.list().isEmpty()) {
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("error stack trace is:"));
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
                LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle(msg.toString(), msgParams.toArray()));
                LOG.log(Level.SEVERE, NutsMessage.jstyle(th.toString()), th.getThrowable());
            }
        } else {
            LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("no stack trace is available."));
        }
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("now exiting nuts, Bye!"));
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NutsBlankable.isBlank(workspaceInformation.getApiVersion())) {
            if (!unsatisfiedOnly || !workspaceInformation.getApiVersion().equals(Nuts.getVersion())) {
                req += 1;
            }
        }
        if (!unsatisfiedOnly || !PrivateNutsUtils.isActualJavaCommand(workspaceInformation.getJavaCommand())) {
            req += 2;
        }
        if (!unsatisfiedOnly || !PrivateNutsUtils.isActualJavaOptions(workspaceInformation.getJavaOptions())) {
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
            sb.append("nuts version ").append(workspaceInformation.getApiId());
        }
        if ((req & 2) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java command ").append(workspaceInformation.getJavaCommand());
        }
        if ((req & 4) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java options ").append(workspaceInformation.getJavaOptions());
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
        File file = PrivateNutsUtilMaven.getBootCacheJar(workspaceInformation.getRuntimeId(), repositories, workspaceBootLibFolder,
                !recover, name, options.getExpireTime(), errorList,
                workspaceInformation, pathExpansionConverter, LOG);
        rt.setId(id.toString());
        rt.setUrl(file.toURI().toURL());
        rt.setIncludedInClasspath(
                PrivateNutsUtilClassLoader.isLoadedClassPath(
                        rt.getURL(), getContextClassLoader(), LOG));

        if (LOG.isLoggable(Level.CONFIG)) {
            String rtHash = "";
            if (workspaceInformation.getRuntimeId() != null) {
                rtHash = PrivateNutsUtilDigest.getFileOrDirectoryDigest(file.toPath());
                if (rtHash == null) {
                    rtHash = "";
                }
            }
            LOG.log(Level.CONFIG, NutsLogVerb.INFO, NutsMessage.jstyle("detect " + name + " version {0} - digest {1} from {2}", id.toString(), rtHash, file));
        }

        for (NutsBootId s : deps) {
            NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
            if (PrivateNutsUtilBootId.isAcceptDependency(s, workspaceInformation.getOptions())) {
                x.setId(s.toString())
                        .setUrl(PrivateNutsUtilMaven.getBootCacheJar(s, repositories, workspaceBootLibFolder, !recover,
                                        name + " dependency",
                                        options.getExpireTime(), errorList, workspaceInformation, pathExpansionConverter, LOG)
                                .toURI().toURL()
                        );
                x.setIncludedInClasspath(PrivateNutsUtilClassLoader.isLoadedClassPath(
                        x.getURL(), getContextClassLoader(), LOG));
                rt.addDependency(x.build());
            }
        }
        return rt.build();
    }

}
