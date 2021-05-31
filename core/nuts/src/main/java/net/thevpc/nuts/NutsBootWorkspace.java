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
 *
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
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
 * @category SPI Base
 * @since 0.5.4
 */
public final class NutsBootWorkspace {

    private static final String DELETE_FOLDERS_HEADER = "ATTENTION ! You are about to delete nuts workspace files.";
    private final long creationTime = System.currentTimeMillis();
    private NutsWorkspaceOptions options;
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
                    return getHome(NutsStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()));
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
    private PrivateNutsLog LOG = new PrivateNutsLog();
    private NutsLogger LOG2;

    public NutsBootWorkspace(String... args) {
        this(Nuts.createOptions().parseArguments(args));
    }

    public NutsBootWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = Nuts.createOptions();
        }
        LOG.setOptions(options);
        if (options.getCreationTime() == 0) {
            NutsWorkspaceOptionsBuilder copy = options.copy();
            copy.setCreationTime(creationTime);
            options = copy;
        }
        this.options = (options instanceof NutsWorkspaceOptionsBuilder) ? ((NutsWorkspaceOptionsBuilder) options) : options.copy();
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
        Map<String, String> defaults = new HashMap<>();
        defaults.put("maven-local", null);
        defaults.put("maven-central", null);
        PrivateNutsRepositorySelector.SelectorList bootRepositories = PrivateNutsRepositorySelector.parse(options.getRepositories());
        return parsedBootRepositories
                = Arrays.stream(bootRepositories.resolveSelectors(defaults))
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
        File file = PrivateNutsUtils.Mvn.resolveOrDownloadJar(NutsConstants.Ids.NUTS_API + "#" + workspaceInformation.getApiVersion(),
                repos.toArray(new String[0]),
                workspaceInformation.getLib(), LOG,
                true,
                options.getExpireTime()
        );
        if (file == null) {
            errors.append("Unable to load nuts ").append(workspaceInformation.getApiVersion()).append("\n");
            showError(workspaceInformation,
                    options.getWorkspace(), null,
                    errors.toString()
            );
            throw new NutsBootException("unable to load " + NutsConstants.Ids.NUTS_API + "#" + workspaceInformation.getApiVersion());
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
        cmd.addAll(Arrays.asList(options.format().compact().setApiVersion(workspaceInformation.getApiVersion()).getBootCommand()));
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            System.out.println("[EXEC] " + sb);
        }
        return cmd.toArray(new String[0]);
    }

    public NutsWorkspaceOptions getOptions() {
        return options;
    }

    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {
            preparedWorkspace = true;
            LOG.log(Level.CONFIG, NutsLogVerb.START, "booting Nuts {0} ...", new Object[]{Nuts.getVersion()});
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
                lastConfigPath = PrivateNutsPlatformUtils.getPlatformHomeFolder(null, null, null,
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
                            ? PrivateNutsPlatformUtils.getPlatformHomeFolder(
                                    null, null, null,
                                    workspaceInformation.isGlobal(),
                                    PrivateNutsUtils.resolveValidWorkspaceName(_ws)
                            ) : PrivateNutsUtils.getAbsolutePath(_ws);

                    PrivateNutsWorkspaceInitInformation configLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastConfigPath, LOG);
                    if (configLoaded == null) {
                        //not loaded
                        break;
                    }
                    if (PrivateNutsUtils.isBlank(configLoaded.getWorkspaceLocation())) {
                        lastConfigLoaded = configLoaded;
                        break;
                    }
                    _ws = configLoaded.getWorkspaceLocation();
                    if (i >= maxDepth - 1) {
                        throw new NutsBootException("cyclic workspace resolution");
                    }
                }
                workspaceName = PrivateNutsUtils.resolveValidWorkspaceName(options.getWorkspace());
            }
            workspaceInformation.setWorkspaceLocation(lastConfigPath);
            if (lastConfigLoaded != null) {
                workspaceInformation.setWorkspaceLocation(lastConfigPath);
                workspaceInformation.setName(lastConfigLoaded.getName());
                workspaceInformation.setUuid(lastConfigLoaded.getUuid());
                workspaceInformation.setBootRepositories(lastConfigLoaded.getBootRepositories());
                workspaceInformation.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
                workspaceInformation.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
                workspaceInformation.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
                workspaceInformation.setStoreLocations(lastConfigLoaded.getStoreLocations() == null ? null : new LinkedHashMap<>(lastConfigLoaded.getStoreLocations()));
                workspaceInformation.setHomeLocations(lastConfigLoaded.getHomeLocations() == null ? null : new LinkedHashMap<>(lastConfigLoaded.getHomeLocations()));
            }
            if (PrivateNutsUtils.isBlank(workspaceInformation.getName())) {
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
                homes[type.ordinal()] = PrivateNutsPlatformUtils.getPlatformHomeFolder(workspaceInformation.getStoreLocationLayout(), type, homeLocations,
                        workspaceInformation.isGlobal(), workspaceInformation.getName());
                if (PrivateNutsUtils.isBlank(homes[type.ordinal()])) {
                    throw new NutsBootException("missing Home for " + type.id());
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
                if (PrivateNutsUtils.isBlank(_storeLocation)) {
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

            //now that config information is prepared proceed to any cleanup
            if (options.isReset()) {
                if (options.isDry()) {
                    System.out.println("[dry] [reset] delete ALL workspace folders and configurations");
                } else {
                    LOG.log(Level.CONFIG, NutsLogVerb.WARNING, "reset workspace.");
                    deleteStoreLocations(true, (Object[]) NutsStoreLocation.values());
                    ndiUndo();
                }
            } else if (options.isRecover()) {
                if (options.isDry()) {
                    System.out.println("[dry] [recover] delete CACHE/TEMP workspace folders");
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
                    deleteStoreLocations(false, folders.toArray());
                }
            }

            //after eventual clean up
            if (NutsConstants.Versions.LATEST.equalsIgnoreCase(workspaceInformation.getApiVersion())
                    || NutsConstants.Versions.RELEASE.equalsIgnoreCase(workspaceInformation.getApiVersion())) {
                NutsBootId s = PrivateNutsUtils.Mvn.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_API), null, LOG, resolveBootRepositories());
                if (s == null) {
                    throw new NutsBootException("unable to load latest nuts version");
                }
                workspaceInformation.setApiVersion(s.getVersion());
            }
            if (PrivateNutsUtils.isBlank(workspaceInformation.getApiVersion())) {
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
                        if (PrivateNutsUtils.isBlank(runtimeId)) {
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
                    || workspaceInformation.getRuntimeBootDescriptor() == null || workspaceInformation.getExtensionBootDescriptors() == null || workspaceInformation.getBootRepositories() == null) {

                //resolve extension id
                if (workspaceInformation.getRuntimeId() == null) {
                    String apiVersion = workspaceInformation.getApiId().substring(workspaceInformation.getApiId().indexOf('#') + 1);
                    NutsBootId runtimeId = PrivateNutsUtils.Mvn.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.startsWith(apiVersion + "."), LOG, resolveBootRepositories());
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
                            new NutsBootId("net.thevpc.nuts", "nuts-runtime", workspaceInformation.getApiVersion() + ".0", false, null, null)
                    );
                    LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "consider default runtime-id : {0}", new Object[]{workspaceInformation.getRuntimeId()});
                }
                if (workspaceInformation.getRuntimeId().getVersion().length() == 0) {
                    workspaceInformation.setRuntimeId(
                            new NutsBootId(workspaceInformation.getRuntimeId().getGroupId(), workspaceInformation.getRuntimeId().getArtifactId(),
                                    workspaceInformation.getApiVersion() + ".0", false, null, null)
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
                            PrivateNutsUtils.Deps depsAndRepos = PrivateNutsUtils.Mvn.loadDependencies(workspaceInformation.getRuntimeId(),
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
                        throw new NutsBootException("unable to load dependencies for " + rid);
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
                                    PrivateNutsUtils.Deps depsAndRepos = PrivateNutsUtils.Mvn.loadDependencies(eid, LOG, bootRepositories0);
                                    if (depsAndRepos != null) {
                                        loadedDeps = depsAndRepos.deps;
                                    }
                                }
                                if (loadedDeps != null) {
                                    all.add(new NutsBootDescriptor(NutsBootId.parse(extension), loadedDeps.toArray(new NutsBootId[0])));
                                } else {
                                    throw new NutsBootException("Unable to load dependencies for " + eid);
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

    private void ndiUndo() {
        //need to unset nadmin configuration.
        //what is the safest way to do so?
        NutsOsFamily os = Nuts.getPlatformOsFamily();
        //windows is ignored because it does not define a global nuts environment
        if (os == NutsOsFamily.LINUX || os == NutsOsFamily.MACOS) {
            String bashrc = os == NutsOsFamily.LINUX ? ".bashrc" : ".bash_profile";
            Path sysrcFile = Paths.get(System.getProperty("user.home")).resolve(bashrc);
            if (Files.exists(sysrcFile)) {

                //these two lines will remove older versions of nuts ( before 0.8.0)
                ndiRemoveFileCommented2Lines(sysrcFile, "net.vpc.app.nuts.toolbox.ndi configuration", true);
                ndiRemoveFileCommented2Lines(sysrcFile, "net.vpc.app.nuts configuration", true);

                //this line will remove 0.8.0+ versions of nuts
                ndiRemoveFileCommented2Lines(sysrcFile, "net.thevpc.nuts configuration", true);
            }

            // if we have deleted a non default workspace, we will fall back to the default one
            // and will consider the latest version of it.
            // this is helpful if we are playing with multiple workspaces. The default workspace will always be
            // accessible when deleting others
            String latestDefaultVersion = null;
            try {
                Path nbase = Paths.get(System.getProperty("user.home")).resolve(".local/share/nuts/apps/default-workspace/id/net/thevpc/nuts/nuts");
                if (Files.isDirectory(nbase)) {
                    latestDefaultVersion = Files.list(nbase).filter(f -> Files.exists(f.resolve(".nuts-bashrc")))
                            .map(x -> sysrcFile.getFileName().toString())
                            .sorted((o1, o2) -> -PrivateNutsUtils.compareRuntimeVersion(o1, o2))
                            .findFirst().orElse(null);
                }
                if (latestDefaultVersion != null) {
                    ndiAddFileLine(sysrcFile, "net.thevpc.nuts configuration",
                            "source " + nbase.resolve(latestDefaultVersion).resolve(".nuts-bashrc"),
                            true, "#!.*", "#!/bin/sh");
                }
            } catch (Exception e) {
                //ignore
                LOG.log(Level.FINEST, NutsLogVerb.FAIL, "unable to undo NDI : {0}", new String[]{e.toString()});
            }
        }
    }

    private boolean ndiAddFileLine(Path filePath, String commentLine, String goodLine, boolean force, String ensureHeader, String headerReplace) {
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

    private boolean ndiRemoveFileCommented2Lines(Path filePath, String commentLine, boolean force) {
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
            LOG.log(Level.WARNING, "unable to update update " + filePath, ex);
            return false;
        }
    }

    private boolean isLoadFromCache() {
        return !options.isRecover() && !options.isReset();
    }

    public NutsWorkspace openWorkspace() {
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsException("unable to open a distinct version : " + getRequirementsHelpString(true) + " from nuts#" + Nuts.getVersion());
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (options.getApplicationArguments().length == 0 && options.isSkipBoot()
                && (options.isRecover() || options.isReset())) {
            throw new NutsBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
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
            if (PrivateNutsUtils.isBlank(workspaceInformation.getApiId())
                    || workspaceInformation.getRuntimeId() == null
                    || PrivateNutsUtils.isBlank(workspaceInformation.getBootRepositories())
                    || workspaceInformation.getRuntimeBootDescriptor() == null
                    || workspaceInformation.getExtensionBootDescriptors() == null) {
                throw new NutsBootException("invalid workspace state");
            }
            boolean recover = options.isRecover() || options.isReset();

//            LinkedHashMap<String, NutsBootClassLoader.NutsClassLoaderNode> allExtensionFiles = new LinkedHashMap<>();
            List<NutsClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = workspaceInformation.getLib();

            String[] repositories = PrivateNutsUtils.splitUrlStrings(workspaceInformation.getBootRepositories()).toArray(new String[0]);

            NutsClassLoaderNodeBuilder rt = new NutsClassLoaderNodeBuilder();
            rt.setId(workspaceInformation.getRuntimeId().toString())
                    .setUrl(getBootCacheJar(workspaceInformation.getRuntimeId(), repositories, workspaceBootLibFolder, !recover, "runtime", options.getExpireTime())
                            .toURI().toURL());
            boolean bootOptionals = isBootOptional();
            for (NutsBootId s : workspaceInformation.getRuntimeBootDescriptor().getDependencies()) {
                NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
                if (isAcceptDependency(s, bootOptionals)) {
                    x.setId(s.toString())
                            .setUrl(getBootCacheJar(s, repositories, workspaceBootLibFolder, !recover, "runtime dependency", options.getExpireTime())
                                    .toURI().toURL()
                            );
                    rt.addDependency(x.build());
                }
            }
            workspaceInformation.setRuntimeBootDependencyNode(rt.build());

            for (NutsBootDescriptor nutsBootDescriptor : workspaceInformation.getExtensionBootDescriptors()) {
                NutsClassLoaderNodeBuilder rt2 = new NutsClassLoaderNodeBuilder();
                rt2.setId(nutsBootDescriptor.getId().toString())
                        .setUrl(getBootCacheJar(workspaceInformation.getRuntimeId(), repositories, workspaceBootLibFolder, !recover, "extension " + nutsBootDescriptor.getId(), options.getExpireTime())
                                .toURI().toURL());
                for (NutsBootId s : nutsBootDescriptor.getDependencies()) {
                    if (isAcceptDependency(s, bootOptionals)) {
                        NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
                        x.setId(s.toString())
                                .setUrl(getBootCacheJar(s, repositories, workspaceBootLibFolder, !recover, "extension " + nutsBootDescriptor.getId() + " dependency", options.getExpireTime())
                                        .toURI().toURL()
                                );
                        rt2.addDependency(x.build());
                    }
                }
                deps.add(rt2.build());
            }
            workspaceInformation.setExtensionBootDependencyNodes(deps.toArray(new NutsClassLoaderNode[0]));
            deps.add(0, workspaceInformation.getRuntimeBootDependencyNode());

            bootClassWorldURLs = resolveClassWorldURLs(deps.toArray(new NutsClassLoaderNode[0]));
            workspaceClassLoader = bootClassWorldURLs.length == 0 ? getContextClassLoader() : new NutsBootClassLoader(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader());
            workspaceInformation.setWorkspaceClassLoader(workspaceClassLoader);
            workspaceInformation.setBootClassWorldURLs(bootClassWorldURLs);
            ServiceLoader<NutsBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsBootWorkspaceFactory.class, workspaceClassLoader);
            List<NutsBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NutsBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new PrivateNutsBootWorkspaceFactoryComparator(options));
            NutsBootWorkspaceFactory factoryInstance = null;
            List<Exception> exceptions = new ArrayList<>();
            for (NutsBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    workspaceInformation.setBootWorkspaceFactory(factoryInstance);
                    nutsWorkspace = a.createWorkspace(workspaceInformation);
                } catch (Exception ex) {
                    exceptions.add(ex);
                    LOG.log(Level.SEVERE, "unable to create workspace using factory " + a, ex);
                }
                if (nutsWorkspace != null) {
                    break;
                }
            }
            if (nutsWorkspace == null) {
                //should never happen
                System.err.print("unable to load Workspace \"" + workspaceInformation.getName() + "\" from ClassPath : \n");
                for (URL url : bootClassWorldURLs) {
                    System.err.printf("\t %s%n", PrivateNutsUtils.formatURL(url));
                }
                for (Exception exception : exceptions) {
                    exception.printStackTrace(System.err);
                }
                LOG.log(Level.SEVERE, NutsLogVerb.FAIL, "unable to load Workspace Component from ClassPath : {0}", new Object[]{Arrays.asList(bootClassWorldURLs)});
                throw new NutsInvalidWorkspaceException(this.workspaceInformation.getWorkspaceLocation(),
                        "unable to load Workspace Component from ClassPath : " + Arrays.asList(bootClassWorldURLs)
                );
            }
//            LOG2 = nutsWorkspace.log().of(NutsBootWorkspace.class);
//            if (LOG2.isLoggable(Level.FINE)) {
//                LOG2.with().session(nutsWorkspace.createSession())
//                        .level(Level.FINE).verb(NutsLogVerb.SUCCESS).log("end initialize workspace");
//            }
            return nutsWorkspace;
        } catch (NutsReadOnlyException | NutsUserCancelException | PrivateNutsBootCancelException ex) {
            throw ex;
        } catch (Throwable ex) {
            showError(workspaceInformation,
                    options.getWorkspace(),
                    bootClassWorldURLs,
                    ex.toString()
            );
            if (ex instanceof NutsException) {
                throw (NutsException) ex;
            }
            throw new NutsBootException("unable to locate valid nuts-runtime package", ex);
        }
    }

    private void fillBootDependencyNodes(NutsClassLoaderNode info, Set<URL> urls) {
        urls.add(info.getURL());
        for (NutsClassLoaderNode dependency : info.getDependencies()) {
            fillBootDependencyNodes(dependency, urls);
        }
    }

    private URL[] resolveClassWorldURLs(NutsClassLoaderNode[] infos) {
        LinkedHashSet<URL> urls0 = new LinkedHashSet<>();
        for (NutsClassLoaderNode info : infos) {
            fillBootDependencyNodes(info, urls0);
        }
        List<URL> urls = new ArrayList<>();
        for (URL url0 : urls0) {
            if (url0 != null) {
                if (isLoadedClassPath(url0)) {
                    LOG.log(Level.WARNING, NutsLogVerb.CACHE, "url will not be loaded (already in classloader) : {0}", new Object[]{url0});
                } else {
                    urls.add(url0);
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    protected String getHome(NutsStoreLocation storeFolder) {
        return PrivateNutsPlatformUtils.getPlatformHomeFolder(
                workspaceInformation.getStoreLocationLayout(),
                storeFolder,
                workspaceInformation.getHomeLocations(),
                workspaceInformation.isGlobal(),
                workspaceInformation.getName()
        );
    }

    protected String expandPath(String path, String base) {
        path = PrivateNutsUtils.replaceDollarString(path, pathExpansionConverter);
        if (PrivateNutsUtils.isURL(path)) {
            return path;
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        if (base == null) {
            base = System.getProperty("user.dir");
        }
        if (new File(path).isAbsolute()) {
            return path;
        }
        return base + File.separator + path;
    }

    private File getBootCacheJar(NutsBootId vid, String[] repositories, String cacheFolder, boolean useCache, String name, Instant expire) {
        File f = getBootCacheFile(vid, getFileName(vid, "jar"), repositories, cacheFolder, useCache, expire);
        if (f == null) {
            throw new NutsInvalidWorkspaceException(this.workspaceInformation.getWorkspaceLocation(), "unable to load " + name + " " + vid + " from repositories " + Arrays.asList(repositories));
        }
        return f;
    }

    private File getBootCacheFile(NutsBootId vid, String fileName, String[] repositories, String cacheFolder, boolean useCache, Instant expire) {
        String path = getPathFile(vid, fileName);
        for (String repository : repositories) {
            File file = getBootCacheFile(path, repository, cacheFolder, useCache, expire);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private String getFileName(NutsBootId id, String ext) {
        return id.getArtifactId() + "-" + id.getVersion() + "." + ext;
    }

    private String getPathFile(NutsBootId id, String name) {
        return id.getGroupId().replace('.', '/') + '/' + id.getArtifactId() + '/' + id.getVersion() + "/" + name;
    }

    private File getBootCacheFile(String path, String repository, String cacheFolder, boolean useCache, Instant expire) {
        boolean cacheLocalFiles = true;//Boolean.getBoolean("nuts.cache.cache-local-files");
        repository = repository.trim();
        repository = expandPath(repository, workspaceInformation.getWorkspaceLocation());
        if (useCache && cacheFolder != null) {

            File f = new File(cacheFolder, path.replace('/', File.separatorChar));
            if (f.isFile() && PrivateNutsUtils.isFileAccessible(f.toPath(), expire, LOG)) {
                return f;
            }
            if (cacheFolder.equals(repository)) {
                return null;
            }
        }
        File localFile = null;
        if (PrivateNutsUtils.isURL(repository)) {
            try {
                localFile = PrivateNutsUtils.toFile(new URL(repository));
            } catch (Exception ex) {
                LOG.log(Level.FINE, "unable to convert url to file : " + repository, ex);
                //ignore
            }
        } else {
            localFile = new File(repository);
        }
        if (localFile == null) {
            if (cacheFolder == null) {
                return null;
            }
            File ok = null;
            File to = new File(cacheFolder, path);
            String urlPath = repository;
            if (!urlPath.endsWith("/")) {
                urlPath += "/";
            }
            urlPath += path;
            long start = System.currentTimeMillis();
            try {
                LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "loading  {0}", new Object[]{urlPath});
                PrivateNutsUtils.copy(new URL(urlPath), to, LOG);
                long end = System.currentTimeMillis();
                LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "loaded   {0} ({1}ms)", new Object[]{urlPath, end - start});
                ok = to;
            } catch (IOException ex) {
                long end = System.currentTimeMillis();
                LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "loaded   {0} ({1}ms)", new Object[]{urlPath, end - start});
                //not found
            }
            return ok;
        } else {
            repository = localFile.getPath();
        }
        File repoFolder = PrivateNutsUtils.Mvn.createFile(getHome(NutsStoreLocation.CONFIG), repository);
        File ff = null;

        if (repoFolder.isDirectory()) {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            if (file.isFile()) {
                ff = file;
            } else {
                LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "locating {0}", new Object[]{file});
            }
        } else {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "locating {0} ; repository is not a valid folder : {1}", new Object[]{file, repoFolder});
        }

        if (ff != null) {
            if (cacheFolder != null && cacheLocalFiles) {
                File to = new File(cacheFolder, path);
                String toc = PrivateNutsUtils.getAbsolutePath(to.getPath());
                String ffc = PrivateNutsUtils.getAbsolutePath(ff.getPath());
                if (ffc.equals(toc)) {
                    return ff;
                }
                try {
                    if (to.getParentFile() != null) {
                        to.getParentFile().mkdirs();
                    }
                    String ext = "config";
                    if (ff.getName().endsWith(".jar")) {
                        ext = "jar";
                    }
                    if (to.isFile()) {
                        PrivateNutsUtils.copy(ff, to, LOG);
                        LOG.log(Level.CONFIG, NutsLogVerb.CACHE, "recover cached " + ext + " file {0} to {1}", new Object[]{ff, to});
                    } else {
                        PrivateNutsUtils.copy(ff, to, LOG);
                        LOG.log(Level.CONFIG, NutsLogVerb.CACHE, "cached " + ext + " file {0} to {1}", new Object[]{ff, to});
                    }
                    return to;
                } catch (IOException ex) {
                    LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "error caching file {0} to {1} : {2}", new Object[]{ff, to, ex.toString()});
                    //not found
                }
                return ff;

            }
            return ff;
        }
        return null;
    }

    private boolean isLoadedClassPath(URL url) {
        try {
            if (url != null) {
                File file = null;
                try {
                    file = new File(url.toURI());
                } catch (URISyntaxException e) {
                    throw new NutsBootException("unsupported classpath item; expected a file path: " + url);
                }
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (!zname.endsWith("/") && zname.endsWith(".class")) {
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            try {
                                if (PrivateNutsUtils.Mvn.isInfiniteLoopThread(NutsBootWorkspace.class.getName(), "isLoadedClassPath")) {
                                    return false;
                                }
                                ClassLoader contextClassLoader = getContextClassLoader();
                                if (contextClassLoader == null) {
                                    return false;
                                }
                                Class<?> aClass = contextClassLoader.loadClass(clz);
                                LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "class {0} loaded successfully from {1}", new Object[]{aClass, file});
                                return true;
                            } catch (ClassNotFoundException e) {
                                return false;
                            }
                        }
                    }
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            //ignorereturn false;
                        }
                    }
                }

            }
        } catch (IOException e) {
            return false;
        }
        return false;
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

    public void runWorkspace() {
        if (hasUnsatisfiedRequirements()) {
            startNewProcess();
            return;
        }
        NutsWorkspace workspace = this.openWorkspace();
        String message = "workspace started successfully";
        NutsWorkspaceOptions o = this.getOptions();
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NutsBootException("workspace not available to run : " + new PrivateNutsCommandLine(o.getApplicationArguments()).toString());
        }

        NutsSession session = workspace.createSession();
        if (LOG2 == null) {
            LOG2 = workspace.log().setSession(session).of(NutsBootWorkspace.class);
        }
        NutsLoggerOp logOp = LOG2.with().session(session).level(Level.CONFIG);
        logOp.verb(NutsLogVerb.SUCCESS).log("running workspace in {0} mode", getWorkspaceRunModeString());
        if (workspace == null && o.getApplicationArguments().length > 0) {
            switch (o.getApplicationArguments()[0]) {
                case "version": {
                    if (options.isDry()) {
                        session.out().println("[boot-internal-command] show-version");
                    } else {
                        session.out().println("nuts-version :" + Nuts.getVersion());
                    }
                    return;
                }
                case "help": {
                    if (options.isDry()) {
                        session.out().println("[boot-internal-command] show-help");
                    } else {
                        session.out().println("nuts is a package manager mainly for java applications.");
                        session.out().println("unluckily it was unable to locate nuts-runtime package which is essential for its execution.\n");
                        session.out().println("nuts-version :" + Nuts.getVersion());
                        session.out().println("try to reinstall nuts (with internet access available) and type 'nuts help' to get a list of global options and commands");
                    }
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
     * @param locations of type NutsStoreLocation, Path of File
     */
    private void deleteStoreLocations(boolean includeRoot, Object... locations) {
        NutsWorkspaceOptions o = getOptions();
        NutsConfirmationMode confirm = o.getConfirm() == null ? NutsConfirmationMode.ASK : o.getConfirm();
        if (confirm == NutsConfirmationMode.ASK
                && this.getOptions().getOutputFormat() != null
                && this.getOptions().getOutputFormat() != NutsContentType.PLAIN) {
            throw new NutsBootException("unable to switch to interactive mode for non plain text output format. "
                    + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                    + "You was asked to confirm deleting folders as part as recover/reset option.", 243);
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
                System.err.println("reset cancelled (applied '--no' argument)");
                throw new PrivateNutsBootCancelException("cancel delete locations");
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
                    throw new NutsBootException("unsupported path type : " + ovalue);
                }
            }
        }
        PrivateNutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force, DELETE_FOLDERS_HEADER, null, null, LOG);
    }

    private void fallbackInstallActionUnavailable(String message) {
        System.out.println(message);
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, message, new Object[0]);
    }

    private void showError(PrivateNutsWorkspaceInitInformation actualBootConfig, String workspace, URL[] bootClassWorldURLs, String extraMessage) {
        Map<String, String> rbc_locations = actualBootConfig.getStoreLocations();
        if (rbc_locations == null) {
            rbc_locations = Collections.emptyMap();
        }
        System.err.printf("unable to bootstrap nuts. : %s%n", extraMessage);
        System.err.printf("here after current environment info:%n");
        System.err.printf("  nuts-boot-api-version            : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getApiVersion(), "<?> Not Found!"));
        System.err.printf("  nuts-boot-runtime                : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getRuntimeId(), "<?> Not Found!"));
        System.err.printf("  nuts-boot-repositories           : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getBootRepositories(), "<?> Not Found!"));
        System.err.printf("  workspace-location               : %s%n", PrivateNutsUtils.nvl(workspace, "<default-location>"));
        System.err.printf("  nuts-store-apps                  : %s%n", rbc_locations.get(NutsStoreLocation.APPS.id()));
        System.err.printf("  nuts-store-config                : %s%n", rbc_locations.get(NutsStoreLocation.CONFIG.id()));
        System.err.printf("  nuts-store-var                   : %s%n", rbc_locations.get(NutsStoreLocation.VAR.id()));
        System.err.printf("  nuts-store-log                   : %s%n", rbc_locations.get(NutsStoreLocation.LOG.id()));
        System.err.printf("  nuts-store-temp                  : %s%n", rbc_locations.get(NutsStoreLocation.TEMP.id()));
        System.err.printf("  nuts-store-cache                 : %s%n", rbc_locations.get(NutsStoreLocation.CACHE.id()));
        System.err.printf("  nuts-store-run                   : %s%n", rbc_locations.get(NutsStoreLocation.RUN.id()));
        System.err.printf("  nuts-store-lib                   : %s%n", rbc_locations.get(NutsStoreLocation.LIB.id()));
        System.err.printf("  nuts-store-strategy              : %s%n", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationStrategy()));
        System.err.printf("  nuts-store-layout                : %s%n", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationLayout()));
        System.err.printf("  nuts-boot-args                   : %s%n", options.format().getBootCommandLine());
        System.err.printf("  nuts-app-args                    : %s%n", Arrays.toString(options.getApplicationArguments()));
        System.err.printf("  option-read-only                 : %s%n", options.isReadOnly());
        System.err.printf("  option-trace                     : %s%n", options.isTrace());
        System.err.printf("  option-progress                  : %s%n", PrivateNutsUtils.desc(options.getProgressOptions()));
        System.err.printf("  option-open-mode                 : %s%n", PrivateNutsUtils.desc(options.getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            System.err.printf("  nuts-runtime-classpath           : %s%n", "<none>");
        } else {
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    System.err.printf("  nuts-runtime-classpath           : %s%n", PrivateNutsUtils.formatURL(bootClassWorldURL));
                } else {
                    System.err.printf("                                     %s%n", PrivateNutsUtils.formatURL(bootClassWorldURL));
                }
            }
        }
        System.err.printf("  java-version                     : %s%n", System.getProperty("java.version"));
        System.err.printf("  java-executable                  : %s%n", PrivateNutsUtils.resolveJavaCommand(null));
        System.err.printf("  java-class-path                  : %s%n", System.getProperty("java.class.path"));
        System.err.printf("  java-library-path                : %s%n", System.getProperty("java.library.path"));
        System.err.printf("  os-name                          : %s%n", System.getProperty("os.name"));
        System.err.printf("  os-arch                          : %s%n", System.getProperty("os.arch"));
        System.err.printf("  os-version                       : %s%n", System.getProperty("os.version"));
        System.err.printf("  user-name                        : %s%n", System.getProperty("user.name"));
        System.err.printf("  user-home                        : %s%n", System.getProperty("user.home"));
        System.err.printf("  user-dir                         : %s%n", System.getProperty("user.dir"));
        System.err.printf("%n");
        if (options.getLogConfig() == null
                || options.getLogConfig().getLogTermLevel() == null
                || options.getLogConfig().getLogFileLevel().intValue() > Level.FINEST.intValue()) {
            System.err.printf("If the problem persists you may want to get more debug info by adding '--verbose' arguments.%n");
        }
        if (!options.isReset() && !options.isRecover() && options.getExpireTime() == null) {
            System.err.printf("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments.%n");
            System.err.printf("Here is the proper command : %n");
            System.err.printf("  java -jar nuts.jar --verbose --recover --expire [...]%n");
        } else if (!options.isReset() && options.isRecover() && options.getExpireTime() != null) {
            System.err.printf("You may also enable full reset mode to ignore existing configuration with '--reset' argument.%n");
            System.err.printf("ATTENTION: this will delete all your nuts configuration. Use it at your own risk.%n");
            System.err.printf("Here is the proper command : %n");
            System.err.printf("  java -jar nuts.jar --verbose --reset [...]%n");
        }
        System.err.printf("now exiting nuts, Bye!%n");
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!PrivateNutsUtils.isBlank(workspaceInformation.getApiVersion())) {
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

    private boolean isBootOptional(String name) {
        for (String property : workspaceInformation.getOptions().getProperties()) {
            PrivateNutsCommandLine.ArgumentImpl a = new PrivateNutsCommandLine.ArgumentImpl(property, '=');
            if (a.getStringKey().equals("boot-" + name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBootOptional() {
        for (String property : workspaceInformation.getOptions().getProperties()) {
            PrivateNutsCommandLine.ArgumentImpl a = new PrivateNutsCommandLine.ArgumentImpl(property, '=');
            if (a.getStringKey().equals("boot-optional")) {
                return true;
            }
        }
        return false;
    }

    private boolean isAcceptDependency(NutsBootId s, boolean bootOptionals) {
        //by default ignore optionals
        if (s.isOptional()) {
            if (!bootOptionals && !isBootOptional(s.getArtifactId())) {
                return false;
            }
        }
        String os = s.getOs();
        String arch = s.getArch();
        if (os.isEmpty() && arch.isEmpty()) {
            return false;
        }
        if (!os.isEmpty()) {
            NutsOsFamily eos = PrivateNutsPlatformUtils.getPlatformOsFamily();
            boolean osOk = false;
            for (String e : os.split("[,; ]")) {
                if (!e.isEmpty()) {
                    if (e.equalsIgnoreCase(eos.id())) {
                        osOk = true;
                        break;
                    }
                }
            }
            if (!osOk) {
                return false;
            }
        }
        if (!arch.isEmpty()) {
            String earch = System.getProperty("os.arch");
            if (earch != null) {
                boolean archOk = false;
                for (String e : arch.split("[,; ]")) {
                    if (!e.isEmpty()) {
                        if (e.equalsIgnoreCase(earch)) {
                            archOk = true;
                            break;
                        }
                    }
                }
                if (!archOk) {
                    return false;
                }
            }
        }
        return true;
    }
}
