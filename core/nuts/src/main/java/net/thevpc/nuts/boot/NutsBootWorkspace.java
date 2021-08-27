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

    private static final String DELETE_FOLDERS_HEADER = "ATTENTION ! You are about to delete nuts workspace files.";
    private final long creationTime = System.currentTimeMillis();
    private final NutsWorkspaceOptions options;
    private final PrivateNutsLog LOG = new PrivateNutsLog();
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
                    return PrivateNutsUtils.getHome(NutsStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()),workspaceInformation);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<String, String> s = workspaceInformation.getStoreLocations();
                    if (s == null) {
                        return "${" + from + "}";
                    }
                    return s.get(from);
                }
            }
            return "${" + from + "}";
        }
    };
    private Set<String> parsedBootRepositories;
    private boolean preparedWorkspace;
    private NutsLogger LOG2;

    public NutsBootWorkspace(String... args) {
        this(Nuts.createOptionsBuilder().parseArguments(args).build());
    }

    public NutsBootWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            this.options = Nuts.createOptionsBuilder().setCreationTime(creationTime).build();
        } else if (options.getCreationTime() == 0) {
            NutsWorkspaceOptionsBuilder copy = options.builder();
            copy.setCreationTime(creationTime);
            this.options = copy.build();
        } else {
            this.options = options;
        }
        LOG.setOptions(options);
        newInstanceRequirements = 0;
    }

    public boolean hasUnsatisfiedRequirements() {
        prepareWorkspace();
        return newInstanceRequirements != 0;
    }

    public int startNewProcess() {
        try {
            return createProcessBuilder().inheritIO().start().waitFor();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (InterruptedException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    public ProcessBuilder createProcessBuilder() {
        return new ProcessBuilder(createProcessCommandLine());
    }

    public Set<String> resolveBootRepositories() {
        if (parsedBootRepositories != null) {
            return parsedBootRepositories;
        }
        LOG.log(Level.FINE, NutsLogVerb.START, "resolve boot repositories from options : {0}, config: {1}", new Object[]{
                (options.getRepositories() == null ? "[]" : Arrays.toString(options.getRepositories()))
                , NutsUtilStrings.isBlank(workspaceInformation.getBootRepositories()) ? "[]" : workspaceInformation.getBootRepositories()
        });
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
        return parsedBootRepositories
                = Arrays.stream(result)
                .map(x -> x.getUrl())
                .collect(Collectors.toSet());
    }

    public String[] createProcessCommandLine() {
        prepareWorkspace();
        LOG.log(Level.FINE, NutsLogVerb.START, "running version {0}.  {1}", new Object[]{workspaceInformation.getApiVersion(), getRequirementsHelpString(true)});
        StringBuilder errors = new StringBuilder();
        String defaultWorkspaceLibFolder = workspaceInformation.getStoreLocation(NutsStoreLocation.LIB);
        List<String> repos = new ArrayList<>();
        repos.add(defaultWorkspaceLibFolder);
        Collection<String> bootRepositories = resolveBootRepositories();
        repos.addAll(bootRepositories);
        PrivateNutsErrorInfoList errorList = new PrivateNutsErrorInfoList();
        File file = PrivateNutsMavenUtils.resolveOrDownloadJar(NutsConstants.Ids.NUTS_API + "#" + workspaceInformation.getApiVersion(),
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
            PrivateNutsUtils.out_printf("[EXEC] %s%n", sb);
        }
        return cmd.toArray(new String[0]);
    }

    public NutsWorkspaceOptions getOptions() {
        return options;
    }

    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {
            preparedWorkspace = true;
            LOG.log(Level.CONFIG, NutsLogVerb.START, "booting Nuts version {0} - build {1}...", new Object[]{Nuts.getVersion(), Nuts.getBuildNumber()});
            workspaceInformation = new PrivateNutsWorkspaceInitInformation();
            workspaceInformation.setOptions(options);

            String _ws = options.getWorkspace();
            String workspaceName;
            String lastConfigPath = null;
            boolean immediateLocation = false;
            PrivateNutsWorkspaceInitInformation lastConfigLoaded = null;
            if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                //this is a protocol based workspace
                //String protocol=ws.substring(0,ws.indexOf("://"));
                workspaceName = "remote-bootstrap";
                lastConfigPath = NutsUtilPlatforms.getPlatformHomeFolder(null, null, null,
                        workspaceInformation.isGlobal(),
                        PrivateNutsUtils.resolveValidWorkspaceName(workspaceName));
                lastConfigLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastConfigPath, LOG);
                immediateLocation = true;

            } else {
                immediateLocation = PrivateNutsUtils.isValidWorkspaceName(_ws);
                int maxDepth = 36;
                for (int i = 0; i < maxDepth; i++) {
                    lastConfigPath
                            = PrivateNutsUtils.isValidWorkspaceName(_ws)
                            ? NutsUtilPlatforms.getPlatformHomeFolder(
                            null, null, null,
                            workspaceInformation.isGlobal(),
                            PrivateNutsUtils.resolveValidWorkspaceName(_ws)
                    ) : PrivateNutsUtils.getAbsolutePath(_ws);

                    PrivateNutsWorkspaceInitInformation configLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastConfigPath, LOG);
                    if (configLoaded == null) {
                        //not loaded
                        break;
                    }
                    if (NutsUtilStrings.isBlank(configLoaded.getWorkspaceLocation())) {
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
            workspaceInformation.setWorkspaceLocation(lastConfigPath);
            if (lastConfigLoaded != null) {
                workspaceInformation.setWorkspaceLocation(lastConfigPath);
                workspaceInformation.setName(lastConfigLoaded.getName());
                workspaceInformation.setUuid(lastConfigLoaded.getUuid());
                if (!options.isReset()) {
                    workspaceInformation.setBootRepositories(lastConfigLoaded.getBootRepositories());
                    workspaceInformation.setJavaCommand(lastConfigLoaded.getJavaCommand());
                    workspaceInformation.setJavaOptions(lastConfigLoaded.getJavaOptions());
                    workspaceInformation.setExtensionsSet(lastConfigLoaded.getExtensionsSet() == null ? new LinkedHashSet<>()
                            : new LinkedHashSet<>(lastConfigLoaded.getExtensionsSet()));
                }
                workspaceInformation.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
                workspaceInformation.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
                workspaceInformation.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
                workspaceInformation.setStoreLocations(lastConfigLoaded.getStoreLocations() == null ? null : new LinkedHashMap<>(lastConfigLoaded.getStoreLocations()));
                workspaceInformation.setHomeLocations(lastConfigLoaded.getHomeLocations() == null ? null : new LinkedHashMap<>(lastConfigLoaded.getHomeLocations()));
            }
            if (NutsUtilStrings.isBlank(workspaceInformation.getName())) {
                workspaceInformation.setName(workspaceName);
            }
            Map<String, String> homeLocations = workspaceInformation.getHomeLocations();
            if (workspaceInformation.getStoreLocationStrategy() == null) {
                workspaceInformation.setStoreLocationStrategy(immediateLocation ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
            }
            if (workspaceInformation.getRepositoryStoreLocationStrategy() == null) {
                workspaceInformation.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
            }
            String workspace = workspaceInformation.getWorkspaceLocation();
            String[] homes = new String[NutsStoreLocation.values().length];
            for (NutsStoreLocation type : NutsStoreLocation.values()) {
                homes[type.ordinal()] = NutsUtilPlatforms.getPlatformHomeFolder(workspaceInformation.getStoreLocationLayout(), type, homeLocations,
                        workspaceInformation.isGlobal(), workspaceInformation.getName());
                if (NutsUtilStrings.isBlank(homes[type.ordinal()])) {
                    throw new NutsBootException(NutsMessage.cstyle("missing Home for %s", type.id()));
                }
            }
            NutsStoreLocationStrategy storeLocationStrategy = workspaceInformation.getStoreLocationStrategy();
            if (storeLocationStrategy == null) {
                storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
            }
            Map<String, String> storeLocations = workspaceInformation.getStoreLocations() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(workspaceInformation.getStoreLocations());
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String typeId = location.id();
                String _storeLocation = storeLocations.get(typeId);
                if (NutsUtilStrings.isBlank(_storeLocation)) {
                    switch (storeLocationStrategy) {
                        case STANDALONE: {
                            storeLocations.put(typeId, (workspace + File.separator + typeId));
                            break;
                        }
                        case EXPLODED: {
                            storeLocations.put(typeId, homes[location.ordinal()]);
                            break;
                        }
                    }
                } else if (!PrivateNutsUtils.isAbsolutePath(_storeLocation)) {
                    switch (storeLocationStrategy) {
                        case STANDALONE: {
                            storeLocations.put(typeId, (workspace + File.separator + location.id()));
                            break;
                        }
                        case EXPLODED: {
                            storeLocations.put(typeId, homes[location.ordinal()] + PrivateNutsUtils.syspath("/" + _storeLocation));
                            break;
                        }
                    }
                }
            }
            workspaceInformation.setStoreLocations(storeLocations);
            workspaceInformation.setApiVersion(options.getApiVersion());
            long countDeleted = 0;
            //now that config information is prepared proceed to any cleanup
            if (options.isReset()) {
                if (options.isDry()) {
                    PrivateNutsUtils.out_printf("[dry] [reset] delete ALL workspace folders and configurations%n");
                } else {
                    LOG.log(Level.CONFIG, NutsLogVerb.WARNING, "reset workspace.");
                    countDeleted = deleteStoreLocations(true, (Object[]) NutsStoreLocation.values());
                    PrivateNutsLauncherUtils.ndiUndo(LOG);
                }
            } else if (options.isRecover()) {
                if (options.isDry()) {
                    PrivateNutsUtils.out_printf("[dry] [recover] delete CACHE/TEMP workspace folders%n");
                } else {
                    LOG.log(Level.CONFIG, NutsLogVerb.WARNING, "recover workspace.");
                    List<Object> folders = new ArrayList<>();
                    folders.add(NutsStoreLocation.CACHE);
                    folders.add(NutsStoreLocation.TEMP);
                    String p = getStoreLocationPath(NutsStoreLocation.LIB);
                    if (p != null) {
                        folders.add(new File(p, "id/net/thevpc/nuts/nuts"));
                        folders.add(new File(p, "id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = deleteStoreLocations(false, folders.toArray());
                }
            }
            //if recover or reset mode with -K option (SkipWelcome)
            //as long as there are no applications to run, will exit before creating workspace
            if (options.getApplicationArguments().length == 0 && options.isSkipBoot()
                    && (options.isRecover() || options.isReset())) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        PrivateNutsUtils.out_printf("workspace erased : %s%n", workspaceInformation.getWorkspaceLocation());
                    } else {
                        PrivateNutsUtils.out_printf("workspace is not erased because it does not exist : %s%n", workspaceInformation.getWorkspaceLocation());
                    }
                }
                throw new NutsBootException(NutsMessage.cstyle(""), 0);
            }
            //after eventual clean up
            if (options.isInherited()) {//when Inherited, always use the current Api version!
                if (NutsConstants.Versions.LATEST.equalsIgnoreCase(workspaceInformation.getApiVersion())
                        || NutsConstants.Versions.RELEASE.equalsIgnoreCase(workspaceInformation.getApiVersion())
//                    || workspaceInformation.getApiVersion() == null
//                    || "".equals(workspaceInformation.getApiVersion())
                ) {
                    NutsBootId s = PrivateNutsMavenUtils.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_API), null, LOG, resolveBootRepositories());
                    if (s == null) {
                        throw new NutsBootException(NutsMessage.plain("unable to load latest nuts version"));
                    }
                    workspaceInformation.setApiVersion(s.getVersion().toString());
                }
            }
            if (NutsUtilStrings.isBlank(workspaceInformation.getApiVersion())) {
                workspaceInformation.setApiVersion(Nuts.getVersion());
            }

            Path apiPath = Paths.get(workspaceInformation.getStoreLocations().get(NutsStoreLocation.CONFIG.id()))
                    .resolve(NutsConstants.Folders.ID)
                    .resolve("net/thevpc/nuts/nuts").resolve(workspaceInformation.getApiVersion()).resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(apiPath, options.getExpireTime(), LOG)) {
                try {
                    Map<String, Object> obj = PrivateNutsJsonParser.parse(apiPath);
                    LOG.log(Level.CONFIG, NutsLogVerb.READ, "loaded {0} file : {1}", new String[]{NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME, apiPath.toString()});
                    loadedApiConfig = true;
                    if (workspaceInformation.getRuntimeId() == null) {
                        String runtimeId = (String) obj.get("runtimeId");
                        if (NutsUtilStrings.isBlank(runtimeId)) {
                            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "{0} does not contain runtime-id", new Object[]{NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME});
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
                    LOG.log(Level.CONFIG, NutsLogVerb.READ, "unable to read {0}", apiPath.toString());
                }
            }
            if (!loadedApiConfig || workspaceInformation.getRuntimeId() == null
                    || workspaceInformation.getRuntimeBootDescriptor() == null
                    || workspaceInformation.getExtensionBootDescriptors() == null
                    || workspaceInformation.getBootRepositories() == null) {

                //resolve extension id
                if (workspaceInformation.getRuntimeId() == null) {
                    String apiVersion = workspaceInformation.getApiId().substring(workspaceInformation.getApiId().indexOf('#') + 1);
                    NutsBootId runtimeId = PrivateNutsMavenUtils.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getFrom().startsWith(apiVersion + "."), LOG, resolveBootRepositories());
                    if (runtimeId != null) {
                        //LOG.log(Level.FINEST, "[success] Resolved latest runtime-id : {0}", new Object[]{runtimeId});
                    } else {
                        LOG.log(Level.FINEST, NutsLogVerb.FAIL, "unable to resolve latest runtime-id (is connection ok?)", new Object[0]);
                    }
                    workspaceInformation.setRuntimeId(runtimeId);
                    workspaceInformation.setRuntimeBootDescriptor(null);
                    workspaceInformation.setBootRepositories(null);
                }
                if (workspaceInformation.getRuntimeId() == null) {
                    workspaceInformation.setRuntimeId(
                            new NutsBootId("net.thevpc.nuts", "nuts-runtime",
                                    NutsBootVersion.parse(workspaceInformation.getApiVersion() + ".0")
                                    , false, null, null)
                    );
                    LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "consider default runtime-id : {0}", new Object[]{workspaceInformation.getRuntimeId()});
                }
                if (workspaceInformation.getRuntimeId().getVersion().isBlank()) {
                    workspaceInformation.setRuntimeId(
                            new NutsBootId(workspaceInformation.getRuntimeId().getGroupId(), workspaceInformation.getRuntimeId().getArtifactId(),
                                    NutsBootVersion.parse(workspaceInformation.getApiVersion() + ".0"), false, null, null)
                    );
                }

                Collection<String> bootRepositories0 = resolveBootRepositories();
                //resolve runtime libraries
                if (workspaceInformation.getRuntimeBootDescriptor() == null) {
                    Set<NutsBootId> loadedDeps = null;
                    String extraBootRepositories = null;
                    NutsBootId rid = workspaceInformation.getRuntimeId();
                    try {
                        Path runtimeFile = Paths.get(workspaceInformation.getCacheBoot())
                                .resolve(PrivateNutsUtils.idToPath(rid)).resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME);
                        boolean cacheLoaded = false;
                        if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(runtimeFile, options.getExpireTime(), LOG)) {
                            try {
                                Map<String, Object> obj = PrivateNutsJsonParser.parse(runtimeFile);
                                LOG.log(Level.CONFIG, NutsLogVerb.READ, "loaded {0} file : {1}", new String[]{NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, runtimeFile.toString()});
                                loadedDeps = PrivateNutsUtils.parseDependencies((String) obj.get("dependencies"));
                                extraBootRepositories = (String) obj.get("bootRepositories");
                            } catch (Exception ex) {
                                LOG.log(Level.FINEST, NutsLogVerb.FAIL, "unable to load {0} file : {1} : {2}", new String[]{NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, runtimeFile.toString(), ex.toString()});
                                //ignore...
                            }
                            cacheLoaded = true;
                        }
                        if (!cacheLoaded || loadedDeps == null) {
                            PrivateNutsUtils.Deps depsAndRepos = PrivateNutsMavenUtils.loadDependencies(workspaceInformation.getRuntimeId(),
                                    LOG, bootRepositories0);
                            if (depsAndRepos != null) {
                                loadedDeps = depsAndRepos.deps;
                                extraBootRepositories = String.join(";", depsAndRepos.repos);
                            }
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.FINEST, NutsLogVerb.FAIL, "unable to load {0} file : {1}", new String[]{NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, ex.toString()});
                        //
                    }
                    if (loadedDeps == null) {
                        throw new NutsBootException(NutsMessage.cstyle("unable to load dependencies for %s", rid));
                    }
                    workspaceInformation.setRuntimeBootDescriptor(new NutsBootDescriptor(
                            workspaceInformation.getRuntimeId(),
                            loadedDeps.toArray(new NutsBootId[0])
                    ));

                    LinkedHashSet<String> bootRepositories = new LinkedHashSet<>();
                    if (extraBootRepositories != null) {
                        for (String v : PrivateNutsUtils.split(extraBootRepositories, ";", true)) {
                            if (v.length() > 0) {
                                bootRepositories.add(v);
                            }
                        }
                    }
                    bootRepositories.addAll(bootRepositories0);
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
                                        Map<String, Object> obj = PrivateNutsJsonParser.parse(apiPath);
                                        LOG.log(Level.CONFIG, NutsLogVerb.READ, "loaded {0} file : {1}", new String[]{NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME, extensionFile.toString()});
                                        loadedDeps = PrivateNutsUtils.parseDependencies((String) obj.get("dependencies"));
                                    } catch (Exception ex) {
                                        LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "unable to load {0} file : {1} : {2}", new String[]{NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME, extensionFile.toString(), ex.toString()});
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    PrivateNutsUtils.Deps depsAndRepos = PrivateNutsMavenUtils.loadDependencies(eid, LOG, bootRepositories0);
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
                PrivateNutsUtils.out_printf("workspace erased : %s%n", workspaceInformation.getWorkspaceLocation());
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
            if (NutsUtilStrings.isBlank(workspaceInformation.getApiId())
                    || workspaceInformation.getRuntimeId() == null
                    || NutsUtilStrings.isBlank(workspaceInformation.getBootRepositories())
                    || workspaceInformation.getRuntimeBootDescriptor() == null
                    || workspaceInformation.getExtensionBootDescriptors() == null) {
                throw new NutsBootException(NutsMessage.plain("invalid workspace state"));
            }
            boolean recover = options.isRecover() || options.isReset();

//            LinkedHashMap<String, PrivateNutsBootClassLoader.NutsClassLoaderNode> allExtensionFiles = new LinkedHashMap<>();
            List<NutsClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = workspaceInformation.getLib();

            String[] repositories = PrivateNutsUtils.splitUrlStrings(workspaceInformation.getBootRepositories()).toArray(new String[0]);

            NutsClassLoaderNodeBuilder rt = new NutsClassLoaderNodeBuilder();
            rt.setId(workspaceInformation.getRuntimeId().toString())
                    .setUrl(PrivateNutsMavenUtils.getBootCacheJar(workspaceInformation.getRuntimeId(), repositories, workspaceBootLibFolder, !recover, "runtime", options.getExpireTime(), errorList,workspaceInformation,pathExpansionConverter, LOG)
                            .toURI().toURL());
            for (NutsBootId s : workspaceInformation.getRuntimeBootDescriptor().getDependencies()) {
                NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
                if (PrivateNutsBootIdUtils.isAcceptDependency(s, workspaceInformation.getOptions())) {
                    x.setId(s.toString())
                            .setUrl(PrivateNutsMavenUtils.getBootCacheJar(s, repositories, workspaceBootLibFolder, !recover, "runtime dependency", options.getExpireTime(), errorList,workspaceInformation,pathExpansionConverter,LOG)
                                    .toURI().toURL()
                            );
                    rt.addDependency(x.build());
                }
            }
            workspaceInformation.setRuntimeBootDependencyNode(rt.build());

            for (NutsBootDescriptor nutsBootDescriptor : workspaceInformation.getExtensionBootDescriptors()) {
                NutsClassLoaderNodeBuilder rt2 = new NutsClassLoaderNodeBuilder();
                rt2.setId(nutsBootDescriptor.getId().toString())
                        .setUrl(PrivateNutsMavenUtils.getBootCacheJar(workspaceInformation.getRuntimeId(), repositories, workspaceBootLibFolder, !recover, "extension " + nutsBootDescriptor.getId(), options.getExpireTime(), errorList,workspaceInformation,pathExpansionConverter,LOG)
                                .toURI().toURL());
                for (NutsBootId s : nutsBootDescriptor.getDependencies()) {
                    if (PrivateNutsBootIdUtils.isAcceptDependency(s, workspaceInformation.getOptions())) {
                        NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
                        x.setId(s.toString())
                                .setUrl(PrivateNutsMavenUtils.getBootCacheJar(s, repositories, workspaceBootLibFolder, !recover, "extension " + nutsBootDescriptor.getId() + " dependency", options.getExpireTime(), errorList,workspaceInformation,pathExpansionConverter,LOG)
                                        .toURI().toURL()
                                );
                        rt2.addDependency(x.build());
                    }
                }
                deps.add(rt2.build());
            }
            workspaceInformation.setExtensionBootDependencyNodes(deps.toArray(new NutsClassLoaderNode[0]));
            deps.add(0, workspaceInformation.getRuntimeBootDependencyNode());

            bootClassWorldURLs = PrivateNutsClasspathUtils.resolveClassWorldURLs(deps.toArray(new NutsClassLoaderNode[0]),getContextClassLoader(), LOG);
            workspaceClassLoader = bootClassWorldURLs.length == 0 ? getContextClassLoader() : new PrivateNutsBootClassLoader(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader());
            workspaceInformation.setWorkspaceClassLoader(workspaceClassLoader);
            workspaceInformation.setBootClassWorldURLs(bootClassWorldURLs);
            ServiceLoader<NutsBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsBootWorkspaceFactory.class, workspaceClassLoader);
            List<NutsBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NutsBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new PrivateNutsBootWorkspaceFactoryComparator(options));
            NutsBootWorkspaceFactory factoryInstance = null;
            List<Throwable> exceptions = new ArrayList<>();
            for (NutsBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    workspaceInformation.setBootWorkspaceFactory(factoryInstance);
                    nutsWorkspace = a.createWorkspace(workspaceInformation);
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    LOG.log(Level.SEVERE, "unable to create workspace using factory " + a, ex);
                }
                if (nutsWorkspace != null) {
                    break;
                }
            }
            if (nutsWorkspace == null) {
                //should never happen
                PrivateNutsUtils.err_printf("unable to load Workspace \"%s\" from ClassPath : \n", workspaceInformation.getName());
                for (URL url : bootClassWorldURLs) {
                    PrivateNutsUtils.err_printf("\t %s%n", PrivateNutsUtils.formatURL(url));
                }
                for (Throwable exception : exceptions) {
                    PrivateNutsUtils.err_printStack(exception);
                }
                LOG.log(Level.SEVERE, NutsLogVerb.FAIL, "unable to load Workspace Component from ClassPath : {0}", new Object[]{Arrays.asList(bootClassWorldURLs)});
                throw new NutsInvalidWorkspaceException(this.workspaceInformation.getWorkspaceLocation(),
                        NutsMessage.cstyle("unable to load Workspace Component from ClassPath : %s", Arrays.asList(bootClassWorldURLs))
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
        if(f==null){
            f=NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            printDryCommand("help");
        } else {
            String msg = "nuts is an open source package manager mainly for java applications. Type 'nuts help' or visit https://github.com/thevpc/nuts for more help.";
            switch (f){
                case JSON:{
                    PrivateNutsUtils.out_printf("{%n");
                    PrivateNutsUtils.out_printf("  \"help\": \"%s\"%n",msg);
                    PrivateNutsUtils.out_printf("}%n");
                    return;
                }
                case TSON:{
                    PrivateNutsUtils.out_printf("{%n");
                    PrivateNutsUtils.out_printf("  help: \"%s\"%n",msg);
                    PrivateNutsUtils.out_printf("}%n");
                    return;
                }
                case YAML:{
                    PrivateNutsUtils.out_printf("help: %s%n",msg);
                    return;
                }
                case TREE:{
                    PrivateNutsUtils.out_printf("- help: %s%n",msg);
                    return;
                }
                case TABLE:{
                    PrivateNutsUtils.out_printf("help  %s%n",msg);
                    return;
                }
                case XML:{
                    PrivateNutsUtils.out_printf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%n");
                    PrivateNutsUtils.out_printf("<string>%n");
                    PrivateNutsUtils.out_printf(" %s%n",msg);
                    PrivateNutsUtils.out_printf("</string>%n");
                    return;
                }
                case PROPS:{
                    PrivateNutsUtils.out_printf("help=%s%n",msg);
                    return;
                }
            }
            PrivateNutsUtils.out_printf("%s%n",msg);
        }
    }

    private void printDryCommand(String cmd) {
        NutsContentType f = options.getOutputFormat();
        if(f==null){
            f=NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            switch (f){
                case JSON:{
                    PrivateNutsUtils.out_printf("{%n");
                    PrivateNutsUtils.out_printf("  \"dryCommand\": \"%s\"%n",cmd);
                    PrivateNutsUtils.out_printf("}%n");
                    return;
                }
                case TSON:{
                    PrivateNutsUtils.out_printf("{%n");
                    PrivateNutsUtils.out_printf("  dryCommand: \"%s\"%n",cmd);
                    PrivateNutsUtils.out_printf("}%n");
                    return;
                }
                case YAML:{
                    PrivateNutsUtils.out_printf("dryCommand: %s%n",cmd);
                    return;
                }
                case TREE:{
                    PrivateNutsUtils.out_printf("- dryCommand: %s%n",cmd);
                    return;
                }
                case TABLE:{
                    PrivateNutsUtils.out_printf("dryCommand  %s%n",cmd);
                    return;
                }
                case XML:{
                    PrivateNutsUtils.out_printf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%n");
                    PrivateNutsUtils.out_printf("<object>%n");
                    PrivateNutsUtils.out_printf("  <string key=\"%s\" value=\"%s\"/>%n","dryCommand",cmd);
                    PrivateNutsUtils.out_printf("</object>%n");
                    return;
                }
                case PROPS:{
                    PrivateNutsUtils.out_printf("dryCommand=%s%n",cmd);
                    return;
                }
            }
            PrivateNutsUtils.out_printf("[Dry] %s%n",Nuts.getVersion());
        }
    }
    private void runCommandVersion() {
//        if (options.isDry()) {
//            session.out().println("[boot-internal-command] show-version");
//        } else {
//            session.out().println("nuts-version :" + Nuts.getVersion());
//        }
        NutsContentType f = options.getOutputFormat();
        if(f==null){
            f=NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            printDryCommand("version");
            return;
        }
        switch (f){
            case JSON:{
                PrivateNutsUtils.out_printf("{%n");
                PrivateNutsUtils.out_printf("  \"version\": \"%s\",%n",Nuts.getVersion());
                PrivateNutsUtils.out_printf("  \"buildNumber\": \"%s\"%n",Nuts.getBuildNumber());
                PrivateNutsUtils.out_printf("}%n");
                return;
            }
            case TSON:{
                PrivateNutsUtils.out_printf("{%n");
                PrivateNutsUtils.out_printf("  version: \"%s\",%n",Nuts.getVersion());
                PrivateNutsUtils.out_printf("  buildNumber: \"%s\"%n",Nuts.getBuildNumber());
                PrivateNutsUtils.out_printf("}%n");
                return;
            }
            case YAML:{
                PrivateNutsUtils.out_printf("version: %s%n",Nuts.getVersion());
                PrivateNutsUtils.out_printf("buildNumber: %s%n",Nuts.getBuildNumber());
                return;
            }
            case TREE:{
                PrivateNutsUtils.out_printf("- version: %s%n",Nuts.getVersion());
                PrivateNutsUtils.out_printf("- buildNumber: %s%n",Nuts.getBuildNumber());
                return;
            }
            case TABLE:{
                PrivateNutsUtils.out_printf("version      %s%n",Nuts.getVersion());
                PrivateNutsUtils.out_printf("buildNumber  %s%n",Nuts.getBuildNumber());
                return;
            }
            case XML:{
                PrivateNutsUtils.out_printf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%n");
                PrivateNutsUtils.out_printf("<object>%n");
                PrivateNutsUtils.out_printf("  <string key=\"%s\" value=\"%s\"/>%n","version",Nuts.getVersion());
                PrivateNutsUtils.out_printf("  <string key=\"%s\" value=\"%s\"/>%n","buildNumber",Nuts.getBuildNumber());
                PrivateNutsUtils.out_printf("</object>%n");
                return;
            }
            case PROPS:{
                PrivateNutsUtils.out_printf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%n");
                PrivateNutsUtils.out_printf("version=%s%n",Nuts.getVersion());
                PrivateNutsUtils.out_printf("buildNumber=%s%n",Nuts.getBuildNumber());
                PrivateNutsUtils.out_printf("</object>%n");
                return;
            }
        }
        PrivateNutsUtils.out_printf("%s%n",Nuts.getVersion());
    }

    public void runWorkspace() {
        if(options.isCommandHelp()){
            runCommandHelp();
            return;
        }else if(options.isCommandVersion()){
           runCommandVersion();
            return;
        }
        if (hasUnsatisfiedRequirements()) {
            startNewProcess();
            return;
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
            LOG2 = workspace.log().setSession(session).of(NutsBootWorkspace.class);
        }
        NutsLoggerOp logOp = LOG2.with().session(session).level(Level.CONFIG);
        logOp.verb(NutsLogVerb.SUCCESS).log("running workspace in {0} mode", getWorkspaceRunModeString());
        if (workspace == null && o.getApplicationArguments().length > 0) {
            switch (o.getApplicationArguments()[0]) {
                case "version": {
                    runCommandVersion();
                    return;
                }
                case "help": {
                    runCommandHelp();
                    return;
                }
            }
        }
        if (o.getApplicationArguments().length == 0) {
            if (o.isSkipWelcome()) {
                return;
            }
            workspace.exec()
                    .setSession(session)
                    .addCommand("welcome")
                    .addExecutorOptions(o.getExecutorOptions())
                    .setExecutionType(o.getExecutionType())
                    .setFailFast(true)
                    .setDry(options.isDry())
                    .run();
        } else {
            workspace.exec()
                    .setSession(session)
                    .addCommand(o.getApplicationArguments())
                    .addExecutorOptions(o.getExecutorOptions())
                    .setExecutionType(o.getExecutionType())
                    .setFailFast(true)
                    .setDry(options.isDry())
                    .run();
        }
    }

    private String getStoreLocationPath(NutsStoreLocation value) {
        Map<String, String> storeLocations = workspaceInformation.getStoreLocations();
        if (storeLocations != null) {
            return storeLocations.get(value.id());
        }
        return null;
    }

    /**
     * @param includeRoot true if include root
     * @param locations   of type NutsStoreLocation, Path of File
     */
    private long deleteStoreLocations(boolean includeRoot, Object... locations) {
        NutsWorkspaceOptions o = getOptions();
        NutsConfirmationMode confirm = o.getConfirm() == null ? NutsConfirmationMode.ASK : o.getConfirm();
        if (confirm == NutsConfirmationMode.ASK
                && this.getOptions().getOutputFormat() != null
                && this.getOptions().getOutputFormat() != NutsContentType.PLAIN) {
            throw new NutsBootException(
                    NutsMessage.cstyle("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), 243);
        }
        LOG.log(Level.FINE, NutsLogVerb.WARNING, "delete location : {0}", new Object[]{workspaceInformation.getWorkspaceLocation()});
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
                PrivateNutsUtils.err_printf("reset cancelled (applied '--no' argument)%n");
                throw new PrivateNutsBootCancelException(NutsMessage.plain("cancel delete locations"));
            }
        }
        NutsWorkspaceConfigManager conf = null;
        List<File> folders = new ArrayList<>();
        if (includeRoot) {
            folders.add(new File(workspaceInformation.getWorkspaceLocation()));
        }
        for (Object ovalue : locations) {
            if (ovalue != null) {
                if (ovalue instanceof NutsStoreLocation) {
                    NutsStoreLocation value = (NutsStoreLocation) ovalue;
                    String p = getStoreLocationPath(value);
                    if (p != null) {
                        folders.add(new File(p));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue).toFile());
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue));
                } else {
                    throw new NutsBootException(NutsMessage.cstyle("unsupported path type : %s", ovalue));
                }
            }
        }
        NutsWorkspaceOptionsBuilder optionsCopy = options.builder();
        if (optionsCopy.isBot() || !PrivateNutsGui.isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return PrivateNutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force, DELETE_FOLDERS_HEADER, null, LOG, optionsCopy.build());
    }

    private void fallbackInstallActionUnavailable(String message) {
        PrivateNutsUtils.out_printf("%s%n", message);
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, message, new Object[0]);
    }

    private void showError(PrivateNutsWorkspaceInitInformation actualBootConfig, String workspace, URL[] bootClassWorldURLs, String extraMessage, PrivateNutsErrorInfoList ths) {
        Map<String, String> rbc_locations = actualBootConfig.getStoreLocations();
        if (rbc_locations == null) {
            rbc_locations = Collections.emptyMap();
        }
        PrivateNutsUtils.err_printf("unable to bootstrap nuts (build-number %s):%n", Nuts.getBuildNumber());
        PrivateNutsUtils.err_printf("%s%n", extraMessage);
        PrivateNutsUtils.err_printf("here after current environment info:%n");
        PrivateNutsUtils.err_printf("  nuts-boot-api-version            : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getApiVersion(), "<?> Not Found!"));
        PrivateNutsUtils.err_printf("  nuts-boot-runtime                : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getRuntimeId(), "<?> Not Found!"));
        PrivateNutsUtils.err_printf("  nuts-boot-repositories           : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getBootRepositories(), "<?> Not Found!"));
        PrivateNutsUtils.err_printf("  workspace-location               : %s%n", PrivateNutsUtils.nvl(workspace, "<default-location>"));
        PrivateNutsUtils.err_printf("  nuts-store-apps                  : %s%n", rbc_locations.get(NutsStoreLocation.APPS.id()));
        PrivateNutsUtils.err_printf("  nuts-store-config                : %s%n", rbc_locations.get(NutsStoreLocation.CONFIG.id()));
        PrivateNutsUtils.err_printf("  nuts-store-var                   : %s%n", rbc_locations.get(NutsStoreLocation.VAR.id()));
        PrivateNutsUtils.err_printf("  nuts-store-log                   : %s%n", rbc_locations.get(NutsStoreLocation.LOG.id()));
        PrivateNutsUtils.err_printf("  nuts-store-temp                  : %s%n", rbc_locations.get(NutsStoreLocation.TEMP.id()));
        PrivateNutsUtils.err_printf("  nuts-store-cache                 : %s%n", rbc_locations.get(NutsStoreLocation.CACHE.id()));
        PrivateNutsUtils.err_printf("  nuts-store-run                   : %s%n", rbc_locations.get(NutsStoreLocation.RUN.id()));
        PrivateNutsUtils.err_printf("  nuts-store-lib                   : %s%n", rbc_locations.get(NutsStoreLocation.LIB.id()));
        PrivateNutsUtils.err_printf("  nuts-store-strategy              : %s%n", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationStrategy()));
        PrivateNutsUtils.err_printf("  nuts-store-layout                : %s%n", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationLayout()));
        PrivateNutsUtils.err_printf("  nuts-boot-args                   : %s%n", options.formatter().getBootCommandLine());
        PrivateNutsUtils.err_printf("  nuts-app-args                    : %s%n", Arrays.toString(options.getApplicationArguments()));
        PrivateNutsUtils.err_printf("  option-read-only                 : %s%n", options.isReadOnly());
        PrivateNutsUtils.err_printf("  option-trace                     : %s%n", options.isTrace());
        PrivateNutsUtils.err_printf("  option-progress                  : %s%n", PrivateNutsUtils.desc(options.getProgressOptions()));
        PrivateNutsUtils.err_printf("  option-open-mode                 : %s%n", PrivateNutsUtils.desc(options.getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            PrivateNutsUtils.err_printf("  nuts-runtime-classpath           : %s%n", "<none>");
        } else {
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    PrivateNutsUtils.err_printf("  nuts-runtime-classpath           : %s%n", PrivateNutsUtils.formatURL(bootClassWorldURL));
                } else {
                    PrivateNutsUtils.err_printf("                                     %s%n", PrivateNutsUtils.formatURL(bootClassWorldURL));
                }
            }
        }
        PrivateNutsUtils.err_printf("  java-version                     : %s%n", System.getProperty("java.version"));
        PrivateNutsUtils.err_printf("  java-executable                  : %s%n", PrivateNutsUtils.resolveJavaCommand(null));
        PrivateNutsUtils.err_printf("  java-class-path                  : %s%n", System.getProperty("java.class.path"));
        PrivateNutsUtils.err_printf("  java-library-path                : %s%n", System.getProperty("java.library.path"));
        PrivateNutsUtils.err_printf("  os-name                          : %s%n", System.getProperty("os.name"));
        PrivateNutsUtils.err_printf("  os-arch                          : %s%n", System.getProperty("os.arch"));
        PrivateNutsUtils.err_printf("  os-version                       : %s%n", System.getProperty("os.version"));
        PrivateNutsUtils.err_printf("  user-name                        : %s%n", System.getProperty("user.name"));
        PrivateNutsUtils.err_printf("  user-home                        : %s%n", System.getProperty("user.home"));
        PrivateNutsUtils.err_printf("  user-dir                         : %s%n", System.getProperty("user.dir"));
        PrivateNutsUtils.err_printf("%n");
        if (options.getLogConfig() == null
                || options.getLogConfig().getLogTermLevel() == null
                || options.getLogConfig().getLogFileLevel().intValue() > Level.FINEST.intValue()) {
            PrivateNutsUtils.err_printf("If the problem persists you may want to get more debug info by adding '--verbose' arguments.%n");
        }
        if (!options.isReset() && !options.isRecover() && options.getExpireTime() == null) {
            PrivateNutsUtils.err_printf("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments.%n");
            PrivateNutsUtils.err_printf("Here is the proper command : %n");
            PrivateNutsUtils.err_printf("  java -jar nuts.jar --verbose --recover --expire [...]%n");
        } else if (!options.isReset() && options.isRecover() && options.getExpireTime() != null) {
            PrivateNutsUtils.err_printf("You may also enable full reset mode to ignore existing configuration with '--reset' argument.%n");
            PrivateNutsUtils.err_printf("ATTENTION: this will delete all your nuts configuration. Use it at your own risk.%n");
            PrivateNutsUtils.err_printf("Here is the proper command : %n");
            PrivateNutsUtils.err_printf("  java -jar nuts.jar --verbose --reset [...]%n");
        }
        if (!ths.list().isEmpty()) {
            PrivateNutsUtils.err_printf("error stack trace is:%n");
            for (PrivateNutsErrorInfo th : ths.list()) {
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
                msg.append(" %n");
                PrivateNutsUtils.err_printf(msg.toString(), msgParams.toArray());
                PrivateNutsUtils.err_printStack(th.getThrowable());
            }
        } else {
            PrivateNutsUtils.err_printf("no stack trace is available.%n");
        }
        PrivateNutsUtils.err_printf("now exiting nuts, Bye!%n");
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NutsUtilStrings.isBlank(workspaceInformation.getApiVersion())) {
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
     * return a string representing unsatisfied contrains
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return a string representing unsatisfied contrains
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

}
