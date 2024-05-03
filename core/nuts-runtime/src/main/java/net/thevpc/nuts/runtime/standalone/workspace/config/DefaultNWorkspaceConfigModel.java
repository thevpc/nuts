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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NClassLoaderNode;
import net.thevpc.nuts.boot.NBootOptions;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.dependency.solver.NDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.extension.NExtensionListHelper;
import net.thevpc.nuts.runtime.standalone.io.path.NPathFromSPI;
import net.thevpc.nuts.runtime.standalone.io.path.spi.*;
import net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs.HtmlfsPath;
import net.thevpc.nuts.runtime.standalone.io.terminal.AbstractSystemTerminalAdapter;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSessionTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.io.terminal.UnmodifiableSessionTerminal;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.TimePeriod;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.NVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v502.NVersionCompat502;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v506.NVersionCompat506;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507.NVersionCompat507;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803.NVersionCompat803;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNWorkspaceConfigModel {
    private static Pattern PRELOAD_EXTENSION_PATH_PATTERN = Pattern.compile("^(?<protocol>[a-z][a-z0-9_-]*):.*");

    private final DefaultNWorkspace ws;
    private final Map<String, NUserConfig> configUsers = new LinkedHashMap<>();
    private final NWorkspaceStoredConfig storedConfig = new NWorkspaceStoredConfigImpl();
    private final ClassLoader bootClassLoader;
    private final List<URL> bootClassWorldURLs;
    private final Function<String, String> pathExpansionConverter;
    private final WorkspaceSystemTerminalAdapter workspaceSystemTerminalAdapter;
    private final List<NPathFactorySPI> pathFactories = new ArrayList<>();
    private final NPathFactorySPI invalidPathFactory;
    private final DefaultNBootModel bootModel;
    protected NWorkspaceConfigBoot storeModelBoot = new NWorkspaceConfigBoot();
    protected NWorkspaceConfigApi storeModelApi = new NWorkspaceConfigApi();
    protected NWorkspaceConfigRuntime storeModelRuntime = new NWorkspaceConfigRuntime();
    protected NWorkspaceConfigSecurity storeModelSecurity = new NWorkspaceConfigSecurity();
    protected NWorkspaceConfigMain storeModelMain = new NWorkspaceConfigMain();
    protected Map<String, NDependencySolverFactory> dependencySolvers;
    private NLog LOG;
    private DefaultNWorkspaceCurrentConfig currentConfig;
    private boolean storeModelBootChanged = false;
    private boolean storeModelApiChanged = false;
    private boolean storeModelRuntimeChanged = false;
    private boolean storeModelSecurityChanged = false;
    private boolean storeModelMainChanged = false;
    private Instant startCreateTime;
    private Instant endCreateTime;
    private NIndexStoreFactory indexStoreClientFactory;
    //    private Set<String> excludedRepositoriesSet = new HashSet<>();
    private NStoreLocationsMap preUpdateConfigStoreLocations;
    private NRepositorySelectorList parsedBootRepositoriesList;
    //    private NutsRepositorySelector[] parsedBootRepositoriesArr;
    private ExecutorService executorService;
    private NSessionTerminal terminal;
    private Map<String, NId> protocolToExtensionMap = new HashMap<>(
            NMaps.of(
                    "ssh", NId.of("net.thevpc.nuts.ext:next-ssh").get()
            )
    );
    //    private final NutsLogger LOG;

    public DefaultNWorkspaceConfigModel(final DefaultNWorkspace ws) {
        this.ws = ws;
        NBootOptions bOptions = NWorkspaceExt.of(ws).getModel().bootModel.getBootEffectiveOptions();
        this.bootClassLoader = bOptions.getClassWorldLoader().orElseGet(() -> Thread.currentThread().getContextClassLoader());
        this.bootClassWorldURLs = CoreCollectionUtils.nonNullList(bOptions.getClassWorldURLs().orNull());
        workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter(ws);

        this.pathExpansionConverter = NWorkspaceVarExpansionFunction.of(NSessionUtils.defaultSession(ws));
        this.bootModel = ((NWorkspaceExt) ws).getModel().bootModel;
        addPathFactory(new FilePath.FilePathFactory(ws));
        addPathFactory(new ClassLoaderPath.ClasspathFactory(ws));
        addPathFactory(new URLPath.URLPathFactory(ws));
        addPathFactory(new NResourcePath.NResourceFactory(ws));
        addPathFactory(new HtmlfsPath.HtmlfsFactory(ws));
        addPathFactory(new DotfilefsPath.DotfilefsFactory(ws));
        addPathFactory(new GithubfsPath.GithubfsFactory(ws));
        addPathFactory(new GenericFilePath.GenericPathFactory(ws));
        invalidPathFactory = new InvalidFilePathFactory();
        //        this.excludedRepositoriesSet = this.options.getExcludedRepositories() == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(this.options.getExcludedRepositories()), " ,;"));
    }


    public void onNewComponent(Class componentType, NSession session) {
        if (NPathFactorySPI.class.isAssignableFrom(componentType)) {
            DefaultNWorkspaceFactory aa = (DefaultNWorkspaceFactory) (((NWorkspaceExt) ws).getModel().extensionModel.getObjectFactory());
            addPathFactory(
                    (NPathFactorySPI) aa.newInstance(componentType, NPathFactorySPI.class, session)
            );
        }
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNWorkspaceConfigModel.class, session);
        }
        return LOG;
    }

    public DefaultNWorkspaceCurrentConfig getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(DefaultNWorkspaceCurrentConfig currentConfig) {
        this.currentConfig = currentConfig;
    }

    public NWorkspaceStoredConfig stored() {
        return storedConfig;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public List<URL> getBootClassWorldURLs() {
        return bootClassWorldURLs == null ? Collections.emptyList() : bootClassWorldURLs;
    }

    public boolean isReadOnly() {
        return NWorkspaceExt.of(ws).getModel().bootModel.getBootUserOptions().getReadOnly().orElse(false);
    }

    public boolean save(boolean force, NSession session) {
        if (!force && !isConfigurationChanged()) {
            return false;
        }
        NWorkspaceUtils.of(session).checkReadOnly();
        NSessionUtils.checkSession(this.ws, session);
        boolean ok = false;
        NWorkspaceSecurityManager.of(session).checkAllowed(NConstants.Permissions.SAVE, "save");
        NPath apiVersionSpecificLocation = NLocations.of(session).getStoreLocation(session.getWorkspace().getApiId(), NStoreType.CONF);
        NElements elem = NElements.of(session);
        if (force || storeModelBootChanged) {

            Path file = NLocations.of(session).getWorkspaceLocation().toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            storeModelBoot.setConfigVersion(DefaultNWorkspace.VERSION_WS_CONFIG_BOOT);
            if (storeModelBoot.getExtensions() != null) {
                for (NWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            elem.json().setValue(storeModelBoot)
                    .setNtf(false).print(file);
            storeModelBootChanged = false;
            ok = true;
        }

        NPath configVersionSpecificLocation = NLocations.of(session).getStoreLocation(session.getWorkspace().getApiId(), NStoreType.CONF);
        if (force || storeModelSecurityChanged) {
            storeModelSecurity.setUsers(configUsers.isEmpty() ? null : configUsers.values().toArray(new NUserConfig[0]));

            NPath file = configVersionSpecificLocation.resolve(CoreNConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
            storeModelSecurity.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NUserConfig extension : storeModelSecurity.getUsers()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            elem.setSession(session).json().setValue(storeModelSecurity)
                    .setNtf(false).print(file);
            storeModelSecurityChanged = false;
            ok = true;
        }

        if (force || storeModelMainChanged) {
            List<NPlatformLocation> plainSdks = new ArrayList<>();
            plainSdks.addAll(NPlatforms.of(session).findPlatforms().toList());
            storeModelMain.setPlatforms(plainSdks);
            storeModelMain.setRepositories(
                    NRepositories.of(session).getRepositories().stream().filter(x -> !x.config().isTemporary())
                            .map(x -> x.config().getRepositoryRef()).collect(Collectors.toList())
            );

            NPath file = configVersionSpecificLocation.resolve(CoreNConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
            storeModelMain.setConfigVersion(current().getApiVersion());
            if (storeModelMain.getCommandFactories() != null) {
                for (NCommandFactoryConfig item : storeModelMain.getCommandFactories()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            if (storeModelMain.getRepositories() != null) {
                for (NRepositoryRef item : storeModelMain.getRepositories()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            if (storeModelMain.getPlatforms() != null) {
                for (NPlatformLocation item : storeModelMain.getPlatforms()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            elem.setSession(session).json().setValue(storeModelMain)
                    .setNtf(false).print(file);
            storeModelMainChanged = false;
            ok = true;
        }

        if (force || storeModelApiChanged) {
            NPath afile = apiVersionSpecificLocation.resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            storeModelApi.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NUserConfig item : storeModelSecurity.getUsers()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            elem.setSession(session).json().setValue(storeModelApi)
                    .setNtf(false).print(afile);
            storeModelApiChanged = false;
            ok = true;
        }
        if (force || storeModelRuntimeChanged) {
            NPath runtimeVersionSpecificLocation = NLocations.of(session).getStoreLocation(NStoreType.CONF)
                    .resolve(NConstants.Folders.ID).resolve(NLocations.of(session).getDefaultIdBasedir(session.getWorkspace().getRuntimeId()));
            NPath afile = runtimeVersionSpecificLocation.resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
            storeModelRuntime.setConfigVersion(current().getApiVersion());
            elem.setSession(session).json().setValue(storeModelRuntime)
                    .setNtf(false).print(afile);
            storeModelRuntimeChanged = false;
            ok = true;
        }
        NException error = null;
        for (NRepository repo : NRepositories.of(session).getRepositories()) {
            try {
                if (repo.config() instanceof NRepositoryConfigManagerExt) {
                    ok |= ((NRepositoryConfigManagerExt) (repo.config())).getModel().save(force, session);
                }
            } catch (NException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }

        return ok;
    }

    public boolean save(NSession session) {
        return save(true, session);
    }

    public NWorkspaceBootConfig loadBootConfig(String _ws, boolean system, boolean followLinks, NSession session) {
        String _ws0 = _ws;
        String effWorkspaceName = null;
        String lastConfigPath = null;
        NWorkspaceConfigBoot lastConfigLoaded = null;
        boolean defaultLocation = false;
        NPlatformHome plocs = NPlatformHome.of(null, system);
        if (_ws != null && _ws.matches("[a-z-]+://.*")) {
            //this is a protocol based workspace
            //String protocol=ws.substring(0,ws.indexOf("://"));
            effWorkspaceName = "remote-bootstrap";
            lastConfigPath = plocs.getWorkspaceLocation(CoreNUtils.resolveValidWorkspaceName(effWorkspaceName));
            lastConfigLoaded = parseBootConfig(NPath.of(lastConfigPath, session), session);
            defaultLocation = true;
            return new DefaultNWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else if (followLinks) {
            defaultLocation = CoreNUtils.isValidWorkspaceName(_ws);
            int maxDepth = 36;
            for (int i = 0; i < maxDepth; i++) {
                lastConfigPath
                        = CoreNUtils.isValidWorkspaceName(_ws)
                        ? plocs.getWorkspaceLocation(CoreNUtils.resolveValidWorkspaceName(_ws)
                ) : CoreIOUtils.getAbsolutePath(_ws);

                NWorkspaceConfigBoot configLoaded = parseBootConfig(NPath.of(lastConfigPath, session), session);
                if (configLoaded == null) {
                    //not loaded
                    break;
                }
                if (NBlankable.isBlank(configLoaded.getWorkspace())) {
                    lastConfigLoaded = configLoaded;
                    break;
                }
                _ws = configLoaded.getWorkspace();
                if (i >= maxDepth - 1) {
                    throw new NIllegalArgumentException(session, NMsg.ofPlain("cyclic workspace resolution"));
                }
            }
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else {
            defaultLocation = CoreNUtils.isValidWorkspaceName(_ws);
            lastConfigPath
                    = CoreNUtils.isValidWorkspaceName(_ws)
                    ? plocs.getWorkspaceLocation(CoreNUtils.resolveValidWorkspaceName(_ws)
            ) : CoreIOUtils.getAbsolutePath(_ws);

            lastConfigLoaded = parseBootConfig(NPath.of(lastConfigPath, session), session);
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        }
    }

    public boolean isExcludedExtension(String extensionId, NWorkspaceOptions options, NSession session) {
        if (extensionId != null && options != null) {
            NId pnid = NId.of(extensionId).get(session);
            String shortName = pnid.getShortName();
            String artifactId = pnid.getArtifactId();
            for (String excludedExtensionList : options.getExcludedExtensions().orElseGet(Collections::emptyList)) {
                for (String s : StringTokenizerUtils.splitDefault(excludedExtensionList)) {
                    if (s.length() > 0) {
                        if (s.equals(shortName) || s.equals(artifactId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public NWorkspaceOptions getBootUserOptions(NSession session) {
        return NWorkspaceExt.of(ws).getModel().bootModel.getBootUserOptions();
    }

    public boolean isSupportedRepositoryType(String repositoryType, NSession session) {
        if (NBlankable.isBlank(repositoryType)) {
            repositoryType = NConstants.RepoTypes.NUTS;
        }
        return session.extensions().createComponents(NRepositoryFactoryComponent.class,
                new NRepositoryConfig().setLocation(
                        NRepositoryLocation.of(repositoryType + "@")
                )).size() > 0;
    }

    public List<NAddRepositoryOptions> getDefaultRepositories(NSession session) {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        List<NAddRepositoryOptions> all = new ArrayList<>();
        for (NRepositoryFactoryComponent provider : session.extensions()
                .createAll(NRepositoryFactoryComponent.class)) {
            for (NAddRepositoryOptions d : provider.getDefaultRepositories(session)) {
                all.add(d);
            }
        }
        Collections.sort(all, new Comparator<NAddRepositoryOptions>() {

            public int compare(NAddRepositoryOptions o1, NAddRepositoryOptions o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        return all;
    }

    public Set<String> getAvailableArchetypes(NSession session) {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NWorkspaceArchetypeComponent extension : session.extensions()
                .createComponents(NWorkspaceArchetypeComponent.class, null)) {
            set.add(extension.getName());
        }
        return set;
    }

    public NPath resolveRepositoryPath(NPath repositoryLocation, NSession session) {
        NPath root = this.getRepositoriesRoot(session);
        return repositoryLocation
                .toAbsolute(root != null ? root :
                        NLocations.of(session).getStoreLocation(NStoreType.CONF)
                                .resolve(NConstants.Folders.REPOSITORIES))
                ;
    }

    public NIndexStoreFactory getIndexStoreClientFactory() {
        return indexStoreClientFactory;
    }

    public String getBootRepositories() {
        return current().getBootRepositories();
    }

    public String getJavaCommand() {
        return current().getJavaCommand();
    }

    public String getJavaOptions() {
        return current().getJavaOptions();
    }

    public boolean isSystem() {
        return current().getSystem();
    }

    public Instant getCreationStartTime() {
        return startCreateTime;
    }

    public Instant getCreationFinishTime() {
        return endCreateTime;
    }

    public Duration getCreateDuration() {
        if (startCreateTime == null || endCreateTime == null) {
            return Duration.ofMillis(0);
        }
        return Duration.between(startCreateTime, endCreateTime);
    }

    public NWorkspaceConfigMain getStoreModelMain() {
        return storeModelMain;
    }

    public DefaultNWorkspaceCurrentConfig current() {
        if (currentConfig == null) {
            throw new IllegalStateException("unable to use workspace.current(). Still in initialize status");
        }
        return currentConfig;
    }

    public void setStartCreateTime(Instant startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public void setConfigBoot(NWorkspaceConfigBoot config, NSession options) {
        setConfigBoot(config, options, true);
    }

    public void setConfigApi(NWorkspaceConfigApi config, NSession session) {
        setConfigApi(config, session, true);
    }

    public void setConfigRuntime(NWorkspaceConfigRuntime config, NSession options) {
        setConfigRuntime(config, options, true);
    }

    public void setConfigSecurity(NWorkspaceConfigSecurity config, NSession session) {
        setConfigSecurity(config, session, true);
    }

    public void setConfigMain(NWorkspaceConfigMain config, NSession session) {
        setConfigMain(config, session, true);
    }

    public void setEndCreateTime(Instant endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    public void installBootIds(NSession session) {
        NWorkspaceModel wsModel = NWorkspaceExt.of(ws).getModel();
        NId iruntimeId = wsModel.bootModel.getBootEffectiveOptions().getRuntimeId().orNull();
        if(wsModel.bootModel.getBootEffectiveOptions().getRuntimeBootDescriptor().isPresent()){
            //not present in shaded jar mode
            NDescriptor d = wsModel.bootModel.getBootEffectiveOptions().getRuntimeBootDescriptor().get(session);
            iruntimeId = NId.of(d.getId().toString()).get(session);
        }
        wsModel.configModel.prepareBootClassPathConf(NIdType.API, ws.getApiId(), null, iruntimeId, false, false, session);
        NBootDef nBootNutsApi=null;
        try {
            nBootNutsApi = wsModel.configModel.prepareBootClassPathJar(ws.getApiId(), null, iruntimeId, false, session);
        } catch (Exception ex) {
            _LOGOP(session).level(Level.SEVERE).log(NMsg.ofC("unable to install boot id (api) %s", ws.getApiId()));
        }
        if(nBootNutsApi==null){
            //no need to install runtime if api is not there
            return;
        }

        NBootDef nBootNutsRuntime=null;
        try {
            wsModel.configModel.prepareBootClassPathConf(NIdType.RUNTIME, iruntimeId, ws.getApiId(), null, false, true, session);
            nBootNutsRuntime = wsModel.configModel.prepareBootClassPathJar(iruntimeId, ws.getApiId(), null, true, session);
        } catch (Exception ex) {
            LOG.with().level(Level.SEVERE).log(NMsg.ofC("unable to install boot id (runtime) %s", iruntimeId));
        }
        if(nBootNutsRuntime==null){
            //no need to install extensions if runtime is not there
            return;
        }
        List<NWorkspaceConfigBoot.ExtensionConfig> extensions = getStoredConfigBoot().getExtensions();
        if (extensions != null) {
            for (NWorkspaceConfigBoot.ExtensionConfig extension : extensions) {
                if (extension.isEnabled()) {
                    try {
                        wsModel.configModel.prepareBootClassPathConf(NIdType.EXTENSION,
                                extension.getId(),
                                ws.getApiId(),
                                null, false,
                                true,
                                session);
                        wsModel.configModel.prepareBootClassPathJar(
                                extension.getId(),
                                ws.getApiId(),
                                null,
                                true,
                                session);
                    } catch (Exception ex) {
                        LOG.with().level(Level.SEVERE).log(NMsg.ofC("unable to install boot id (extension) %s", extension.getId()));
                    }
                }
            }
        }
    }

    public boolean isConfigurationChanged() {
        return storeModelBootChanged || storeModelApiChanged || storeModelRuntimeChanged || storeModelSecurityChanged || storeModelMainChanged;
    }

    public boolean loadWorkspace(NSession session) {
        try {
            NSessionUtils.checkSession(ws, session);
            NWorkspaceConfigBoot _config = parseBootConfig(session);
            if (_config == null) {
                return false;
            }
            DefaultNWorkspaceCurrentConfig cConfig = new DefaultNWorkspaceCurrentConfig(ws).merge(_config, session);
            NBootOptions bOptions = NWorkspaceExt.of(ws).getModel().bootModel.getBootEffectiveOptions();
            if (cConfig.getApiId() == null) {
                cConfig.setApiId(NId.ofApi(bOptions.getApiVersion().orNull()).get(session));
            }
            if (cConfig.getRuntimeId() == null) {
                cConfig.setRuntimeId(bOptions.getRuntimeId().orNull(), session);
            }
            if (cConfig.getRuntimeBootDescriptor() == null) {
                cConfig.setRuntimeBootDescriptor(bOptions.getRuntimeBootDescriptor().get());
            }
            if (cConfig.getExtensionBootDescriptors() == null) {
                cConfig.setExtensionBootDescriptors(bOptions.getExtensionBootDescriptors().orNull());
            }
            if (cConfig.getBootRepositories() == null) {
                cConfig.setBootRepositories(bOptions.getBootRepositories().orNull());
            }
            cConfig.merge(getBootUserOptions(session), session);

            setCurrentConfig(cConfig.build(NLocations.of(session).getWorkspaceLocation(), session));

            NVersionCompat compat = createNutsVersionCompat(Nuts.getVersion(), session);
            NId apiId = session.getWorkspace().getApiId();
            NWorkspaceConfigApi aconfig = compat.parseApiConfig(apiId, session);
            NId toImportOlderId = null;
            if (aconfig != null) {
                cConfig.merge(aconfig, session);
            } else {
                // will try to find older versions
                List<NId> olderIds = findOlderNutsApiIds(session);
                for (NId olderId : olderIds) {
                    aconfig = compat.parseApiConfig(olderId, session);
                    if (aconfig != null) {
                        // ask
                        if (NAsk.of(session).forBoolean(NMsg.ofC("import older config %s into %s", olderId, apiId))
                                .setDefaultValue(true)
                                .getBooleanValue()
                        ) {
                            toImportOlderId = olderId;
                            aconfig.setRuntimeId(null);
                            aconfig.setApiVersion(null);
                            cConfig.merge(aconfig, session);
                        }
                        break;
                    }
                }
            }
            if (cConfig.getApiId() == null) {
                cConfig.setApiId(NId.ofApi(Nuts.getVersion()).get(session));
            }
            if (cConfig.getRuntimeId() == null) {
                cConfig.setRuntimeId(bOptions.getRuntimeId().orNull());
            }
            NWorkspaceConfigRuntime rconfig = compat.parseRuntimeConfig(session);
            if (rconfig != null) {
                cConfig.merge(rconfig, session);
            }
            NWorkspaceConfigSecurity sconfig = compat.parseSecurityConfig(apiId, session);
            if (sconfig == null) {
                if (toImportOlderId != null) {
                    sconfig = compat.parseSecurityConfig(toImportOlderId, session);
                }
            }
            NWorkspaceConfigMain mconfig = compat.parseMainConfig(apiId, session);
            if (mconfig == null) {
                if (toImportOlderId != null) {
                    mconfig = compat.parseMainConfig(toImportOlderId, session);
                }
            }

            if (bOptions.getUserOptions().get().getRecover().orElse(false) || bOptions.getUserOptions().get().getReset().orElse(false)) {
                //always reload boot resolved versions!
                cConfig.setApiId(NId.ofApi(bOptions.getApiVersion().orNull()).get(session));
                cConfig.setRuntimeId(bOptions.getRuntimeId().orNull(), session);
                cConfig.setRuntimeBootDescriptor(bOptions.getRuntimeBootDescriptor().get());
                cConfig.setExtensionBootDescriptors(bOptions.getExtensionBootDescriptors().orNull());
                cConfig.setBootRepositories(bOptions.getBootRepositories().orNull());
            }
            setCurrentConfig(cConfig
                    .build(NLocations.of(session).getWorkspaceLocation(), session)
            );
            if (aconfig == null) {
                aconfig = new NWorkspaceConfigApi();
            }
            if (aconfig.getApiVersion() == null) {
                aconfig.setApiVersion(cConfig.getApiId().getVersion());
            }
            if (aconfig.getRuntimeId() == null) {
                aconfig.setRuntimeId(cConfig.getRuntimeId());
            }
            setConfigBoot(_config, session, false);
            setConfigApi(aconfig, session, false);
            setConfigRuntime(rconfig, session, false);
            setConfigSecurity(sconfig, session, false);
            setConfigMain(mconfig, session, false);
            storeModelBootChanged = false;
            storeModelApiChanged = false;
            storeModelRuntimeChanged = false;
            storeModelSecurityChanged = false;
            storeModelMainChanged = false;
            return true;
        } catch (RuntimeException ex) {
            if (NBootManager.of(session).getBootOptions().getRecover().orElse(false)) {
                onLoadWorkspaceError(ex, session);
            } else {
                throw ex;
            }
        }
        return false;
    }

    private List<NId> findOlderNutsApiIds(NSession session) {
        NId apiId = session.getWorkspace().getApiId();
        NPath path = NLocations.of(session).getStoreLocation(apiId, NStoreType.CONF)
                .getParent();
        List<NId> olderIds = path.stream().filter(NPath::isDirectory)
                .withDesc(NEDesc.of("isDirectory"))
                .map(x -> NVersion.of(x.getName()).get(session))
                .withDesc(NEDesc.of("toVersion"))
                .filter(x -> x.compareTo(apiId.getVersion()) < 0)
                .withDesc(NEDesc.of("older"))
                .sorted(new NComparator<NVersion>() {
                    @Override
                    public int compare(NVersion o1, NVersion o2) {
                        return Comparator.<NVersion>reverseOrder().compare(o1, o2);
                    }

                    @Override
                    public NElement describe(NSession session) {
                        return NElements.of(session).ofString("reverseOrder");
                    }
                }).map(x -> apiId.builder().setVersion(x).build())
                .withDesc(NEDesc.of("toId"))
                .toList();
        return olderIds;
    }

    public void setBootApiVersion(NVersion value, NSession session) {
        if (!Objects.equals(value, storeModelApi.getApiVersion())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setApiVersion(value);
            fireConfigurationChanged("api-version", session, ConfigEventType.API);
        }
    }

    public void setExtraBootExtensionId(NId apiId, NId extensionId, List<NDependency> deps, NSession session) {
        String newDeps = deps.stream().map(Object::toString).collect(Collectors.joining(";"));
        NWorkspaceConfigBoot.ExtensionConfig cc = new NWorkspaceConfigBoot.ExtensionConfig();
        cc.setId(apiId);
        cc.setDependencies(newDeps);
        cc.setEnabled(true);
        if (apiId.getVersion().equals(session.getWorkspace().getApiId().getVersion())) {
            NExtensionListHelper h = new NExtensionListHelper(session.getWorkspace().getApiId(),
                    getStoredConfigBoot().getExtensions()).save();
            if (h.add(extensionId, deps)) {
                getStoredConfigBoot().setExtensions(h.getConfs());
                NWorkspaceExt.of(ws).deployBoot(session, extensionId, true);
                fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
                DefaultNWorkspaceConfigModel configModel = NWorkspaceExt.of(session).getModel().configModel;
                configModel.save(session);
            }
        } else {
            //TODO, how to get old deps ?
            NExtensionListHelper h2 = new NExtensionListHelper(session.getWorkspace().getApiId(), new ArrayList<>());
            if (h2.add(extensionId, deps)) {
                NWorkspaceExt.of(ws).deployBoot(session, extensionId, true);
            }
        }
        NPath runtimeVersionSpecificLocation = NLocations.of(session).getStoreLocation(NStoreType.CONF)
                .resolve(NConstants.Folders.ID).resolve(NLocations.of(session).getDefaultIdBasedir(extensionId));
        NPath afile = runtimeVersionSpecificLocation.resolve(NConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
        cc.setConfigVersion(current().getApiVersion());
        NElements.of(session).json().setValue(cc)
                .setNtf(false).print(afile);
    }

    public void setExtraBootRuntimeId(NId apiId, NId runtimeId, List<NDependency> deps, NSession session) {
        String newDeps = deps.stream().map(Object::toString).collect(Collectors.joining(";"));
        if (apiId == null || apiId.getVersion().equals(session.getWorkspace().getApiId().getVersion())) {
            if (!Objects.equals(runtimeId.toString(), storeModelApi.getRuntimeId())
                    || !Objects.equals(newDeps, storeModelRuntime.getDependencies())
            ) {
//            options = CoreNutsUtils.validate(options, ws);
                storeModelApi.setRuntimeId(runtimeId);
                storeModelRuntime.setDependencies(newDeps);
                setConfigRuntime(storeModelRuntime, session, true);
                fireConfigurationChanged("runtime-id", session, ConfigEventType.API);
            }
            setBootRuntimeId(runtimeId,
                    newDeps,
                    session);
            save(session);
            return;
        }
        NWorkspaceConfigApi estoreModelApi = new NWorkspaceConfigApi();
        estoreModelApi.setApiVersion(apiId.getVersion());
        estoreModelApi.setRuntimeId(runtimeId);
        estoreModelApi.setConfigVersion(current().getApiVersion());
        NPath apiVersionSpecificLocation = NLocations.of(session).getStoreLocation(apiId, NStoreType.CONF);
        NPath afile = apiVersionSpecificLocation.resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        NElements elems = NElements.of(session);
        elems.json().setValue(estoreModelApi)
                .setNtf(false).print(afile);

        NWorkspaceConfigRuntime storeModelRuntime = new NWorkspaceConfigRuntime();
        storeModelRuntime.setId(runtimeId);
        storeModelRuntime.setDependencies(
                newDeps
        );

        NPath runtimeVersionSpecificLocation = NLocations.of(session).getStoreLocation(NStoreType.CONF)
                .resolve(NConstants.Folders.ID).resolve(NLocations.of(session).getDefaultIdBasedir(runtimeId));
        afile = runtimeVersionSpecificLocation.resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        storeModelRuntime.setConfigVersion(current().getApiVersion());
        elems.setSession(session).json().setValue(storeModelRuntime)
                .setNtf(false).print(afile);

    }

    public void setBootRuntimeId(NId value, String dependencies, NSession session) {
        if (!Objects.equals(value, storeModelApi.getRuntimeId())
                || !Objects.equals(dependencies, storeModelRuntime.getDependencies())
        ) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setRuntimeId(value);
            storeModelRuntime.setDependencies(dependencies);
            setConfigRuntime(storeModelRuntime, session, true);
            fireConfigurationChanged("runtime-id", session, ConfigEventType.API);
        }
    }

    public void setBootRuntimeDependencies(String dependencies, NSession session) {
        if (!Objects.equals(dependencies, storeModelRuntime.getDependencies())) {
//            options = CoreNutsUtils.validate(options, ws);
        }
    }

    public void setBootRepositories(String value, NSession session) {
        if (!Objects.equals(value, storeModelBoot.getBootRepositories())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelBoot.setBootRepositories(value);
            fireConfigurationChanged("boot-repositories", session, ConfigEventType.API);
        }
    }

    public NWorkspaceConfigBoot.ExtensionConfig getBootExtension(String value, NSession session) {
        NId newId = NId.of(value).get(session);
        for (NWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
            NId id = extension.getId();
            if (newId.equalsShortId(id)) {
                return extension;
            }
        }
        return null;
    }

    public void setBootExtension(String value, String dependencies, boolean enabled, NSession session) {
        NId newId = NId.of(value).get(session);
        for (NWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
            NId id = extension.getId();
            if (newId.equalsShortId(id)) {
                extension.setId(newId);
                extension.setEnabled(enabled);
                extension.setDependencies(dependencies);
                fireConfigurationChanged("boot-extensions", session, ConfigEventType.API);
                return;
            }
        }
        storeModelBoot.getExtensions().add(new NWorkspaceConfigBoot.ExtensionConfig(newId, dependencies, true));
    }

    public NUserConfig getUser(String userId, NSession session) {
        NSessionUtils.checkSession(ws, session);
        NUserConfig _config = getSecurity(userId);
        if (_config == null) {
            if (NConstants.Users.ADMIN.equals(userId) || NConstants.Users.ANONYMOUS.equals(userId)) {
                _config = new NUserConfig(userId, null, null, null);
                setUser(_config, session);
            }
        }
        return _config;
    }

    public NUserConfig[] getUsers(NSession session) {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        return configUsers.values().toArray(new NUserConfig[0]);
    }

    public void setUser(NUserConfig config, NSession session) {
        if (config != null) {
            configUsers.put(config.getUser(), config);
            fireConfigurationChanged("user", session, ConfigEventType.SECURITY);
        }
    }

    public void removeUser(String userId, NSession session) {
        NUserConfig old = getSecurity(userId);
        if (old != null) {
            configUsers.remove(userId);
            fireConfigurationChanged("users", session, ConfigEventType.SECURITY);
        }
    }

    public void setSecure(boolean secure, NSession session) {
        if (secure != storeModelSecurity.isSecure()) {
            storeModelSecurity.setSecure(secure);
            fireConfigurationChanged("secure", session, ConfigEventType.SECURITY);
        }
    }

    public void fireConfigurationChanged(String configName, NSession session, ConfigEventType t) {
//        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        ((DefaultNImports) NImports.of(session)).getModel().invalidateCache();
        switch (t) {
            case API: {
                storeModelApiChanged = true;
                break;
            }
            case RUNTIME: {
                storeModelRuntimeChanged = true;
                break;
            }
            case SECURITY: {
                storeModelSecurityChanged = true;
                break;
            }
            case MAIN: {
                storeModelMainChanged = true;
                break;
            }
            case BOOT: {
                storeModelBootChanged = true;
                break;
            }
        }
        DefaultNWorkspaceEvent evt = new DefaultNWorkspaceEvent(session, null, "config." + configName, null, true);
        for (NWorkspaceListener workspaceListener : NEvents.of(session).getWorkspaceListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    //    
    public NWorkspaceConfigApi getStoredConfigApi() {
        if (storeModelApi.getApiVersion() == null || storeModelApi.getApiVersion().isBlank()) {
            storeModelApi.setApiVersion(Nuts.getVersion());
        }
        return storeModelApi;
    }

    public NWorkspaceConfigBoot getStoredConfigBoot() {
        return storeModelBoot;
    }

    public NWorkspaceConfigSecurity getStoredConfigSecurity() {
        return storeModelSecurity;
    }

    public NWorkspaceConfigMain getStoredConfigMain() {
        return storeModelMain;
    }

    public NWorkspace getWorkspace() {
        return ws;
    }

    public NDependencySolver createDependencySolver(String name, NSession session) {
        NDependencySolverFactory c = getSolversMap(session).get(NDependencySolverUtils.resolveSolverName(name));
        if (c != null) {
            return c.create(session);
        }
        throw new NIllegalArgumentException(session, NMsg.ofC("dependency solver not found %s", name));
    }

    private Map<String, NDependencySolverFactory> getSolversMap(NSession session) {
        if (dependencySolvers == null) {
            dependencySolvers = new LinkedHashMap<>();
            for (NDependencySolverFactory nutsDependencySolver : session.extensions().createComponents(NDependencySolverFactory.class, null)) {
                dependencySolvers.put(nutsDependencySolver.getName(), nutsDependencySolver);
            }
        }
        return dependencySolvers;
    }

    public NDependencySolverFactory[] getDependencySolvers(NSession session) {
        return getSolversMap(session).values().toArray(new NDependencySolverFactory[0]);
    }


    public NPath getRepositoriesRoot(NSession session) {
        return NLocations.of(session).getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.REPOSITORIES);
    }

    public NPath getTempRepositoriesRoot(NSession session) {
        return NLocations.of(session).getStoreLocation(NStoreType.TEMP).resolve(NConstants.Folders.REPOSITORIES);
    }

    public boolean isValidWorkspaceFolder(NSession session) {
        Path file = NLocations.of(session).getWorkspaceLocation().toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        return Files.isRegularFile(file);
    }

    public NAuthenticationAgent createAuthenticationAgent(String authenticationAgent, NSession session) {
        authenticationAgent = NStringUtils.trim(authenticationAgent);
        NAuthenticationAgent supported = null;
        if (authenticationAgent.isEmpty()) {
            supported = session.extensions().createComponent(NAuthenticationAgent.class, "").get();
        } else {
            List<NAuthenticationAgent> agents = session.extensions().createComponents(NAuthenticationAgent.class, authenticationAgent);
            for (NAuthenticationAgent agent : agents) {
                if (agent.getId().equals(authenticationAgent)) {
                    supported = agent;
                }
            }
        }
        if (supported == null) {
            return NOptional.<NAuthenticationAgent>ofNamedEmpty(NMsg.ofC("extensions component %s with agent=%s", NAuthenticationAgent.class, authenticationAgent)).get();
        }
        NSessionUtils.setSession(supported, session);
        return supported;
    }

    //
//    public void setExcludedRepositories(String[] excludedRepositories, NutsUpdateOptions options) {
//        excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedRepositories), " ,;"));
//    }
    public void setUsers(NUserConfig[] users, NSession session) {
        for (NUserConfig u : getUsers(session)) {
            removeUser(u.getUser(), session);
        }
        for (NUserConfig conf : users) {
            setUser(conf, session);
        }
    }

    public NWorkspaceConfigRuntime getStoredConfigRuntime() {
        return storeModelRuntime;
    }

    public NId createSdkId(String type, String version, NSession session) {
        return NWorkspaceUtils.of(session).createSdkId(type, version);
    }

    public void onExtensionsPrepared(NSession session) {
        try {
            indexStoreClientFactory = session.extensions().createComponent(NIndexStoreFactory.class).orNull();
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNIndexStoreFactory();
        }
    }

    public void setConfigApi(NWorkspaceConfigApi config, NSession session, boolean fire) {
        this.storeModelApi = config == null ? new NWorkspaceConfigApi() : config;
        if (fire) {
            fireConfigurationChanged("boot-api-config", session, ConfigEventType.API);
        }
    }

    public void setConfigRuntime(NWorkspaceConfigRuntime config, NSession session, boolean fire) {
        this.storeModelRuntime = config == null ? new NWorkspaceConfigRuntime() : config;
        if (fire) {
            fireConfigurationChanged("boot-runtime-config", session, ConfigEventType.RUNTIME);
        }
    }

    private void setConfigSecurity(NWorkspaceConfigSecurity config, NSession session, boolean fire) {
        this.storeModelSecurity = config == null ? new NWorkspaceConfigSecurity() : config;
        configUsers.clear();
        if (this.storeModelSecurity.getUsers() != null) {
            for (NUserConfig s : this.storeModelSecurity.getUsers()) {
                configUsers.put(s.getUser(), s);
            }
        }
        storeModelSecurityChanged = true;
        if (fire) {
            fireConfigurationChanged("config-security", session, ConfigEventType.SECURITY);
        }
    }

    private void setConfigMain(NWorkspaceConfigMain config, NSession session, boolean fire) {
        this.storeModelMain = config == null ? new NWorkspaceConfigMain() : config;
        DefaultNPlatforms d = (DefaultNPlatforms) NPlatforms.of(session);
        d.getModel().setPlatforms(this.storeModelMain.getPlatforms().toArray(new NPlatformLocation[0]), session);
        NRepositories repos = NRepositories.of(session);
        repos.removeAllRepositories();
        List<NRepositoryRef> refsToLoad = this.storeModelMain.getRepositories();
        if (refsToLoad != null) {
            refsToLoad = new ArrayList<>(refsToLoad);
            //reset config because add will add it again...
            this.storeModelMain.setRepositories(new ArrayList<>());
            for (NRepositoryRef ref : refsToLoad) {
                repos
                        .addRepository(
                                NRepositoryUtils.refToOptions(ref)
                        );
            }
        }

        storeModelMainChanged = true;
        if (fire) {
            fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
        }
    }

    private void setConfigBoot(NWorkspaceConfigBoot config, NSession session, boolean fire) {
        this.storeModelBoot = config;
        if (NBlankable.isBlank(config.getUuid())) {
            config.setUuid(UUID.randomUUID().toString());
            fire = true;
        }
        if (fire) {
            fireConfigurationChanged("config-master", session, ConfigEventType.BOOT);
        }
    }

    public String toString() {
        String s1 = "NULL";
        String s2 = "NULL";
        s1 = ws == null ? "?" : ws.getApiId().toString();
        s2 = ws == null ? "?" : String.valueOf(ws.getRuntimeId());
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((currentConfig == null) ? "NULL" : ("'"
                +
                (ws == null ? "?" : "" + ((DefaultNLocations) NLocations.of(NSessionUtils.defaultSession(ws))
                ).getModel().getWorkspaceLocation()) + '\''))
                + '}';
    }

    public void collect(NClassLoaderNode n, LinkedHashMap<String, NClassLoaderNode> deps) {
        if (!deps.containsKey(n.getId())) {
            deps.put(n.getId(), n);
            for (NClassLoaderNode d : n.getDependencies()) {
                collect(d, deps);
            }
        }
    }

    public NBootDef fetchBootDef(NId id, boolean content, NSession session) {
        NDefinition nd = NFetchCmd.of(id, session)
                .setDependencies(true).setContent(content)
                .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                .setFailFast(false).getResultDefinition();
        if (nd != null) {
            if (content && nd.getContent().isNotPresent()) {
                //this is an unexpected behaviour, fail fast
                throw new NNotFoundException(session, id);
            }
            return new NBootDef(nd.getId(), nd.getDependencies().get(session).transitive().toList(),
                    (content && nd.getContent().isPresent()) ? nd.getContent().get() : null);
        }
        if (isFirstBoot()) {
            NClassLoaderNode n = searchBootNode(id, session);
            if (n != null) {
                LinkedHashMap<String, NClassLoaderNode> dm = new LinkedHashMap<>();
                for (NClassLoaderNode d : n.getDependencies()) {
                    collect(d, dm);
                }
                return new NBootDef(
                        id,
                        dm.values().stream().map(x -> NDependency.of(x.getId()).get(session)).collect(Collectors.toList()),
                        NPath.of(n.getURL(), session)
                );
            }
            String contentPath = NIdUtils.resolveFilePath(id, null);
            NPath jarPath = null;
            NPath pomPath = null;
            for (NRepositoryLocation nutsRepositoryLocation : resolveBootRepositoriesBootSelectionArray(session)) {
                NPath base = NPath.of(nutsRepositoryLocation.getPath(), session);
                if (base.isLocal() && base.isDirectory()) {
                    NPath a = base.resolve(contentPath + ".jar");
                    NPath b = base.resolve(contentPath + ".pom");
                    if (a.isRegularFile() && b.isRegularFile()) {
                        jarPath = a;
                        pomPath = b;
                        break;
                    }
                }
            }
            if (jarPath != null) {
                NDescriptor d = NDescriptorParser.of(session)
                        .setDescriptorStyle(NDescriptorStyle.MAVEN)
                        .parse(pomPath).get(session);
                //see only first level deps!
                return new NBootDef(
                        id,
                        d.getDependencies(),
                        jarPath
                );
            }
        }
        throw new NNotFoundException(session, id);
    }

    public void prepareBootClassPathConf(NIdType idType, NId id, NId forId, NId forceRuntimeId, boolean force, boolean processDependencies, NSession session) {
        //do not create boot file for nuts (it has no dependencies anyways!)
        switch (idType) {
            case API: {
                return;
            }
            case RUNTIME: {
                NBootDef d = fetchBootDef(id, false, session);
                for (NId apiId : CoreNUtils.resolveNutsApiIdsFromDependencyList(d.deps, session)) {
                    setExtraBootRuntimeId(apiId, d.id, d.deps, session);
                }
                break;
            }
            case EXTENSION: {
                NBootDef d = fetchBootDef(id, false, session);
                for (NId apiId : CoreNUtils.resolveNutsApiIdsFromDependencyList(d.deps, session)) {
                    setExtraBootExtensionId(apiId, d.id, d.deps, session);
                }
            }
        }
    }

    public NBootDef prepareBootClassPathJar(NId id, NId forId, NId forceRuntimeId, boolean processDependencies, NSession session) {
        NBootDef d = fetchBootDef(id, true, session);
        if (deployToInstalledRepository(d.content.toPath().get(), session)) {
            if (processDependencies) {
                for (NDependency dep : d.deps) {
                    prepareBootClassPathJar(dep.toId(), id, forceRuntimeId, true, session);
                }
            }
        }
        return d;
    }

    private boolean isFirstBoot() {
        return this.bootModel.isFirstBoot();
    }

    private boolean deployToInstalledRepository(Path tmp, NSession session) {
//        NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
//        NutsDeployRepositoryCommand desc = repoSPI.deploy()
//                .setId(id)
//                .setSession(session.copy().setConfirm(NutsConfirmationMode.YES))
//                .setContent(contentPath)
//                //.setFetchMode(NutsFetchMode.LOCAL)
//                .run();
//        repo.install(id, session, forId);

        NInstalledRepository ins = NWorkspaceExt.of(session.getWorkspace()).getInstalledRepository();
        NDescriptor descriptor = NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(tmp, null, session);
        if (descriptor != null) {
            DefaultNDefinition b = new DefaultNDefinition(
                    null, null,
                    descriptor.getId(),
                    descriptor, NPath.of(tmp, session).setUserCache(true).setUserTemporary(true),
                    new DefaultNInstallInfo(descriptor.getId(), NInstallStatus.NONE, null, null, null, null, null, null, false, false),
                    null, session
            );
            ins.install(b, session);
            return true;
        }
        return false;
    }

    private NClassLoaderNode searchBootNode(NId id, NSession session) {
        NBootManager boot = NBootManager.of(session);
        List<NClassLoaderNode> all = new ArrayList<>();
        all.add(boot.getBootRuntimeClassLoaderNode());
        all.addAll(boot.getBootExtensionClassLoaderNode());
        return searchBootNode(id, all);
    }

    private NClassLoaderNode searchBootNode(NId id, List<NClassLoaderNode> into) {
        for (NClassLoaderNode n : into) {
            if (n != null) {
                if (id.getLongName().equals(n.getId())) {
                    return n;
                }
            }
            NClassLoaderNode a = searchBootNode(id, n.getDependencies());
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    public void onPreUpdateConfig(String confName, NSession session) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NStoreLocationsMap(currentConfig.getStoreLocations());
    }

    public void onPostUpdateConfig(String confName, NSession session) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NStoreLocationsMap(currentConfig.getStoreLocations());
        DefaultNWorkspaceCurrentConfig d = currentConfig;
        d.setUserStoreLocations(new NStoreLocationsMap(storeModelBoot.getStoreLocations()).toMapOrNull());
        d.setHomeLocations(new NHomeLocationsMap(storeModelBoot.getHomeLocations()).toMapOrNull());
        d.build(NLocations.of(session).getWorkspaceLocation(), session);
        NStoreLocationsMap newSL = new NStoreLocationsMap(currentConfig.getStoreLocations());
        for (NStoreType sl : NStoreType.values()) {
            String oldPath = preUpdateConfigStoreLocations.get(sl);
            String newPath = newSL.get(sl);
            if (!oldPath.equals(newPath)) {
                Path oldPathObj = Paths.get(oldPath);
                if (Files.exists(oldPathObj)) {
                    CoreIOUtils.copyFolder(oldPathObj, Paths.get(newPath), session);
                }
            }
        }
        fireConfigurationChanged(confName, session, ConfigEventType.API);
    }

    private void onLoadWorkspaceError(Throwable ex, NSession session) {
        DefaultNWorkspaceConfigModel wconfig = this;
        Path file = NLocations.of(session).getWorkspaceLocation().toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        if (wconfig.isReadOnly()) {
            throw new NIOException(session, NMsg.ofC("unable to load config file %s", file), ex);
        }
        String fileSuffix = Instant.now().toString();
        fileSuffix = fileSuffix.replace(':', '-');
        String fileName = "nuts-workspace-" + fileSuffix;
        NPath logError = NLocations.of(session).getStoreLocation(ws.getApiId(), NStoreType.LOG).resolve("invalid-config");
        NPath logFile = logError.resolve(fileName + ".error");
        _LOGOP(session).level(Level.SEVERE).verb(NLogVerb.FAIL)
                .log(NMsg.ofC("erroneous workspace config file. Unable to load file %s : %s", file, ex));

        try {
            logFile.mkParentDirs();
        } catch (Exception ex1) {
            throw new NIOException(session, NMsg.ofC("unable to log workspace error while loading config file %s : %s", file, ex1), ex);
        }
        NPath newfile = logError.resolve(fileName + ".json");
        _LOGOP(session).level(Level.SEVERE).verb(NLogVerb.FAIL)
                .log(NMsg.ofC("erroneous workspace config file will be replaced by a fresh one. Old config is copied to %s\n error logged to  %s", newfile.toString(), logFile));
        try {
            Files.move(file, newfile.toPath().get());
        } catch (IOException e) {
            throw new NIOException(session, NMsg.ofC("unable to load and re-create config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logFile.getOutputStream())) {
            o.println("workspace.path:");
            o.println(NLocations.of(session).getWorkspaceLocation());
            o.println("workspace.options:");
            o.println(wconfig.getBootUserOptions(session)
                    .toCmdLine(
                            new NWorkspaceOptionsConfig()
                                    .setCompact(false)
                    )
            );
            for (NStoreType location : NStoreType.values()) {
                o.println("location." + location.id() + ":");
                o.println(NLocations.of(session).getStoreLocation(location));
            }
            o.println("java.class.path:");
            o.println(System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

    public NUserConfig getSecurity(String id) {
        return configUsers.get(id);
    }

    private NWorkspaceConfigBoot parseBootConfig(NSession session) {
        return parseBootConfig(NLocations.of(session).getWorkspaceLocation(), session);
    }

    private NWorkspaceConfigBoot parseBootConfig(NPath path, NSession session) {
        Path file = path.toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(file, session);
        if (bytes == null) {
            return null;
        }
        try {
            Map<String, Object> a_config0 = NElements.of(session).json().parse(bytes, Map.class);
            NVersion version = NVersion.of((String) a_config0.get("configVersion")).ifBlankEmpty().orNull();
            if (version == null) {
                version = NVersion.of((String) a_config0.get("createApiVersion")).ifBlankEmpty().orNull();
                if (version == null) {
                    version = Nuts.getVersion();
                }
            }
            return createNutsVersionCompat(version, session).parseConfig(bytes, session);
        } catch (Exception ex) {
            _LOGOP(session).level(Level.SEVERE).verb(NLogVerb.FAIL)
                    .log(NMsg.ofC("erroneous workspace config file. Unable to load file %s : %s",
                            file, ex));
            throw new NIOException(session, NMsg.ofC("unable to load config file %s", file), ex);
        }
    }

    private NVersionCompat createNutsVersionCompat(NVersion apiVersion, NSession session) {
        int buildNumber = CoreNUtils.getApiVersionOrdinalNumber(apiVersion);
        if (buildNumber >= 803) {
            return new NVersionCompat803(session, apiVersion);
        } else if (buildNumber >= 507) {
            return new NVersionCompat507(session, apiVersion);
        } else if (buildNumber >= 506) {
            return new NVersionCompat506(session, apiVersion);
        } else {
            return new NVersionCompat502(session, apiVersion);
        }
    }

    public NRepositoryLocation[] resolveBootRepositoriesBootSelectionArray(NSession session) {
        List<NRepositoryLocation> defaults = new ArrayList<>();
        DefaultNConfigs rm = (DefaultNConfigs) NConfigs.of(session);
        for (NAddRepositoryOptions d : rm.getDefaultRepositories()) {
            defaults.add(NRepositoryLocation.of(d.getName(), null));
        }
        return resolveBootRepositoriesList(session).resolve(defaults.toArray(new NRepositoryLocation[0]),
                NRepositoryDB.of(session)
        );
    }

    public NRepositorySelectorList resolveBootRepositoriesList(NSession session) {
        if (parsedBootRepositoriesList != null) {
            return parsedBootRepositoriesList;
        }
        NBootOptions bOptions = NWorkspaceExt.of(ws).getModel().bootModel.getBootEffectiveOptions();
        parsedBootRepositoriesList = NRepositorySelectorList.of(
                bOptions.getUserOptions().get().getRepositories().orNull(), NRepositoryDB.of(session), session).get();
        return parsedBootRepositoriesList;
    }

    public NWorkspaceConfigBoot getStoreModelBoot() {
        return storeModelBoot;
    }

    public NWorkspaceConfigApi getStoreModelApi() {
        return storeModelApi;
    }

    public NWorkspaceConfigRuntime getStoreModelRuntime() {
        return storeModelRuntime;
    }

    public NWorkspaceConfigSecurity getStoreModelSecurity() {
        return storeModelSecurity;
    }

    public ExecutorService executorService(NSession session) {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    executorService = NBootManager.of(session).getBootOptions().getExecutorService().orNull();
                    if (executorService == null) {

                        int minPoolSize = getConfigProperty("nuts.threads.min", session).flatMap(NLiteral::asInt).orElse(2);
                        if (minPoolSize < 1) {
                            minPoolSize = 60;
                        } else if (minPoolSize > 500) {
                            minPoolSize = 500;
                        }
                        int maxPoolSize = getConfigProperty("nuts.threads.max", session).flatMap(NLiteral::asInt).orElse(60);
                        if (maxPoolSize < 1) {
                            maxPoolSize = 60;
                        } else if (maxPoolSize > 500) {
                            maxPoolSize = 500;
                        }
                        if (minPoolSize > maxPoolSize) {
                            minPoolSize = maxPoolSize;
                        }
                        TimePeriod defaultPeriod = new TimePeriod(3, TimeUnit.SECONDS);
                        TimePeriod period = TimePeriod.parse(
                                getConfigProperty("nuts.threads.keep-alive", session).flatMap(NLiteral::asString).orNull(),
                                TimeUnit.SECONDS
                        ).orElse(defaultPeriod);
                        if (period.getCount() < 0) {
                            period = defaultPeriod;
                        }
                        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(CoreNUtils.N_DEFAULT_THREAD_FACTORY);
                        poolExecutor.setCorePoolSize(minPoolSize);
                        poolExecutor.setKeepAliveTime(period.getCount(), period.getUnit());
                        poolExecutor.setMaximumPoolSize(maxPoolSize);
                        executorService = poolExecutor;
                    }
                }
            }
        }
        return executorService;
    }

    public NSessionTerminal getTerminal() {
        return terminal;
    }

    public void setTerminal(NSessionTerminal terminal, NSession session) {
        NSessionUtils.checkSession(ws, session);
        if (terminal == null) {
            terminal = createTerminal(session);
        }
        if (!(terminal instanceof UnmodifiableSessionTerminal)) {
            terminal = new UnmodifiableSessionTerminal(terminal, session);
        }
        this.terminal = terminal;
    }

    public NSessionTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err, NSession session) {
        NSessionTerminal t = createTerminal(session);
        if (in != null) {
            t.setIn(in);
        }
        if (out != null) {
            t.setOut(out);
        }
        if (err != null) {
            t.setErr(err);
        }
        return t;
    }

    public NSessionTerminal createTerminal(NSession session) {
        return new DefaultNSessionTerminalFromSystem(
                session, workspaceSystemTerminalAdapter
        );
//        return createTerminal(null, session);
    }

    public void addPathFactory(NPathFactorySPI f) {
        if (f != null && !pathFactories.contains(f)) {
            pathFactories.add(f);
        }
    }

    public void removePathFactory(NPathFactorySPI f) {
        pathFactories.remove(f);
    }

    public NPath resolve(String path, NSession session, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        ClassLoader finalClassLoader = classLoader;
        Matcher m = PRELOAD_EXTENSION_PATH_PATTERN.matcher(path);
        if (m.find()) {
            String protocol = m.group("protocol");
            NId eid = protocolToExtensionMap.get(protocol);
            if (eid != null) {
                NExtensions.of(session).loadExtension(eid);
            }
        }
        NCallableSupport<NPathSPI> z = Arrays.stream(getPathFactories())
                .map(x -> {
                    NCallableSupport<NPathSPI> v = null;
                    try {
                        v = x.createPath(path, session, finalClassLoader);
                    } catch (Exception ex) {
                        //
                    }
                    return v;
                })
                .filter(x -> x != null && x.getSupportLevel() > 0)
                .max(Comparator.comparingInt(NCallableSupport::getSupportLevel))
                .orElse(null);
        NPathSPI s = z == null ? null : z.call(session);
        if (s != null) {
            if (s instanceof NPath) {
                return (NPath) s;
            }
            return new NPathFromSPI(s);
        }
        return null;
    }

    public NPathFactorySPI[] getPathFactories() {
        List<NPathFactorySPI> all = new ArrayList<>(pathFactories.size() + 1);
        all.addAll(pathFactories);
        all.add(invalidPathFactory);
        return all.toArray(new NPathFactorySPI[0]);
    }

    public DefaultNBootModel getBootModel() {
        return bootModel;
    }

    public Map<String, String> getConfigMap() {
        Map<String, String> p = new LinkedHashMap<>();
        if (getStoreModelMain().getEnv() != null) {
            p.putAll(getStoreModelMain().getEnv());
        }
//        p.putAll(options);
        return p;
    }

    public NOptional<NLiteral> getConfigProperty(String property, NSession session) {
        Map<String, String> env = getStoreModelMain().getEnv();
        if (env != null) {
            String v = env.get(property);
            return NOptional.of(v == null ? null : NLiteral.of(v));
        }
        return NOptional.ofEmpty(s -> NMsg.ofC("config property not found : %s", property));
    }

    public void setConfigProperty(String property, String value, NSession session) {
        Map<String, String> env = getStoreModelMain().getEnv();
//        session = CoreNutsUtils.validate(session, workspace);
        if (NBlankable.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                NConfigsExt.of(NConfigs.of(session))
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                getStoreModelMain().setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                NConfigsExt.of(NConfigs.of(session))
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        }
    }

    private static class WorkspaceSystemTerminalAdapter extends AbstractSystemTerminalAdapter {

        private final NWorkspace workspace;

        public WorkspaceSystemTerminalAdapter(NWorkspace workspace) {
            this.workspace = workspace;
        }

        public NSystemTerminalBase getBase() {
            return NIO.of(NSessionUtils.defaultSession(workspace))
                    .getSystemTerminal();
        }
    }

    private class NBootDef {
        NId id;
        List<NDependency> deps;
        NPath content;

        public NBootDef(NId id, List<NDependency> deps, NPath content) {
            this.id = id;
            this.deps = deps;
            this.content = content;
        }
    }

    private class NWorkspaceStoredConfigImpl implements NWorkspaceStoredConfig {

        public NWorkspaceStoredConfigImpl() {
        }

        @Override
        public String getName() {
            return getStoredConfigBoot().getName();
        }

        @Override
        public NStoreStrategy getStoreStrategy() {
            return getStoredConfigBoot().getStoreStrategy();
        }

        @Override
        public NStoreStrategy getRepositoryStoreStrategy() {
            return getStoredConfigBoot().getStoreStrategy();
        }

        @Override
        public NOsFamily getStoreLayout() {
            return getStoredConfigBoot().getStoreLayout();
        }

        @Override
        public Map<NStoreType, String> getStoreLocations() {
            return getStoredConfigBoot().getStoreLocations();
        }

        @Override
        public Map<NHomeLocation, String> getHomeLocations() {
            return getStoredConfigBoot().getHomeLocations();
        }

        @Override
        public String getStoreLocation(NStoreType folderType) {
            return new NStoreLocationsMap(getStoredConfigBoot().getStoreLocations()).get(folderType);
        }

        @Override
        public String getHomeLocation(NHomeLocation homeLocation) {
            return new NHomeLocationsMap(getStoredConfigBoot().getHomeLocations()).get(homeLocation);
        }

        @Override
        public NId getApiId() {
            NVersion v = getStoredConfigApi().getApiVersion();
            NSession session = NSessionUtils.defaultSession(DefaultNWorkspaceConfigModel.this.ws);

            return (v == null || v.isBlank()) ? null : NId.ofApi(v).get(session);
        }

        @Override
        public NId getRuntimeId() {
            return getStoredConfigApi().getRuntimeId();
        }

        @Override
        public String getRuntimeDependencies() {
            return getStoredConfigRuntime().getDependencies();
        }

        //        @Override
//        public String getExtensionDependencies() {
//            return getStoredConfigApi().getExtensionDependencies();
//        }
        @Override
        public String getBootRepositories() {
            return getStoredConfigBoot().getBootRepositories();
        }

        @Override
        public String getJavaCommand() {
            return getStoredConfigApi().getJavaCommand();
        }

        @Override
        public String getJavaOptions() {
            return getStoredConfigApi().getJavaOptions();
        }

        @Override
        public boolean isSystem() {
            return getStoredConfigBoot().isSystem();
        }

    }

    private class InvalidFilePathFactory implements NPathFactorySPI {
        @Override
        public NCallableSupport<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
            NSessionUtils.checkSession(getWorkspace(), session);
            try {
                return NCallableSupport.of(1, () -> new InvalidFilePath(path, session));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            String path = context.getConstraints();
            return 1;
        }
    }

}
