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
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootManager;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.dependency.solver.NutsDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NutsDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.event.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.extension.NutsExtensionListHelper;
import net.thevpc.nuts.runtime.standalone.io.path.NutsPathFromSPI;
import net.thevpc.nuts.runtime.standalone.io.path.spi.*;
import net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs.HtmlfsPath;
import net.thevpc.nuts.runtime.standalone.io.terminal.AbstractSystemTerminalAdapter;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNutsSessionTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.io.terminal.UnmodifiableSessionTerminal;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.util.NutsRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.security.ReadOnlyNutsWorkspaceOptions;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.TimePeriod;
import net.thevpc.nuts.runtime.standalone.workspace.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.NutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v502.NutsVersionCompat502;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v506.NutsVersionCompat506;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507.NutsVersionCompat507;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803.NutsVersionCompat803;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNutsWorkspaceConfigModel {

    private final DefaultNutsWorkspace ws;
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private final NutsWorkspaceStoredConfig storedConfig = new NutsWorkspaceStoredConfigImpl();
    private final ClassLoader bootClassLoader;
    private final URL[] bootClassWorldURLs;
    private final Function<String, String> pathExpansionConverter;
    private final WorkspaceSystemTerminalAdapter workspaceSystemTerminalAdapter;
    private final List<NutsPathFactory> pathFactories = new ArrayList<>();
    private final NutsPathFactory invalidPathFactory;
    private final DefaultNutsBootModel bootModel;
    protected NutsWorkspaceConfigBoot storeModelBoot = new NutsWorkspaceConfigBoot();
    protected NutsWorkspaceConfigApi storeModelApi = new NutsWorkspaceConfigApi();
    protected NutsWorkspaceConfigRuntime storeModelRuntime = new NutsWorkspaceConfigRuntime();
    protected NutsWorkspaceConfigSecurity storeModelSecurity = new NutsWorkspaceConfigSecurity();
    protected NutsWorkspaceConfigMain storeModelMain = new NutsWorkspaceConfigMain();
    protected Map<String, NutsDependencySolverFactory> dependencySolvers;
    private NutsLogger LOG;
    private DefaultNutsWorkspaceCurrentConfig currentConfig;
    private boolean storeModelBootChanged = false;
    private boolean storeModelApiChanged = false;
    private boolean storeModelRuntimeChanged = false;
    private boolean storeModelSecurityChanged = false;
    private boolean storeModelMainChanged = false;
    private long startCreateTime;
    private long endCreateTime;
    private NutsIndexStoreFactory indexStoreClientFactory;
    //    private Set<String> excludedRepositoriesSet = new HashSet<>();
    private NutsStoreLocationsMap preUpdateConfigStoreLocations;
    private NutsRepositorySelectorList parsedBootRepositoriesList;
    //    private NutsRepositorySelector[] parsedBootRepositoriesArr;
    private ExecutorService executorService;
    private NutsSessionTerminal terminal;
    //    private final NutsLogger LOG;

    public DefaultNutsWorkspaceConfigModel(final DefaultNutsWorkspace ws) {
        this.ws = ws;
        CoreNutsBootOptions bOptions = NutsWorkspaceExt.of(ws).getModel().bootModel.getCoreBootOptions();
        this.bootClassLoader = bOptions.getClassWorldLoader() == null ? Thread.currentThread().getContextClassLoader() : bOptions.getClassWorldLoader();
        this.bootClassWorldURLs = bOptions.getClassWorldURLs() == null ? null : Arrays.copyOf(bOptions.getClassWorldURLs(), bOptions.getClassWorldURLs().length);
        workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter(ws);

        this.pathExpansionConverter = new NutsWorkspaceVarExpansionFunction(NutsSessionUtils.defaultSession(ws));
        this.bootModel = (DefaultNutsBootModel) ((DefaultNutsBootManager) ws.boot()).getModel();
        addPathFactory(new FilePath.FilePathFactory(ws));
        addPathFactory(new ClassLoaderPath.ClasspathFactory(ws));
        addPathFactory(new URLPath.URLPathFactory(ws));
        addPathFactory(new NutsResourcePath.NutsResourceFactory(ws));
        addPathFactory(new HtmlfsPath.HtmlfsFactory(ws));
        addPathFactory(new DotfilefsPath.DotfilefsFactory(ws));
        addPathFactory(new GithubfsPath.GithubfsFactory(ws));
        addPathFactory(new GenericFilePath.GenericPathFactory(ws));
        invalidPathFactory = new InvalidFilePathFactory();
        //        this.excludedRepositoriesSet = this.options.getExcludedRepositories() == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(this.options.getExcludedRepositories()), " ,;"));
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsWorkspaceConfigModel.class, session);
        }
        return LOG;
    }

    public DefaultNutsWorkspaceCurrentConfig getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(DefaultNutsWorkspaceCurrentConfig currentConfig) {
        this.currentConfig = currentConfig;
    }

    public NutsWorkspaceStoredConfig stored() {
        return storedConfig;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public URL[] getBootClassWorldURLs() {
        return bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
    }

    public boolean isReadOnly() {
        CoreNutsBootOptions bOptions = NutsWorkspaceExt.of(ws).getModel().bootModel.getCoreBootOptions();
        return bOptions.getOptions().isReadOnly();
    }

    public boolean save(boolean force, NutsSession session) {
        if (!force && !isConfigurationChanged()) {
            return false;
        }
        NutsWorkspaceUtils.of(session).checkReadOnly();
        NutsSessionUtils.checkSession(this.ws, session);
        boolean ok = false;
        session.security().checkAllowed(NutsConstants.Permissions.SAVE, "save");
        NutsPath apiVersionSpecificLocation = session.locations().getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.CONFIG);
        NutsElements elem = NutsElements.of(session);
        if (force || storeModelBootChanged) {

            Path file = session.locations().getWorkspaceLocation().toFile().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            storeModelBoot.setConfigVersion(DefaultNutsWorkspace.VERSION_WS_CONFIG_BOOT);
            if (storeModelBoot.getExtensions() != null) {
                for (NutsWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            elem.json().setValue(storeModelBoot)
                    .setNtf(false).print(file);
            storeModelBootChanged = false;
            ok = true;
        }

        NutsPath configVersionSpecificLocation = session.locations().getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.CONFIG);
        if (force || storeModelSecurityChanged) {
            storeModelSecurity.setUsers(configUsers.isEmpty() ? null : configUsers.values().toArray(new NutsUserConfig[0]));

            NutsPath file = configVersionSpecificLocation.resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
            storeModelSecurity.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NutsUserConfig extension : storeModelSecurity.getUsers()) {
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
            List<NutsPlatformLocation> plainSdks = new ArrayList<>();
            plainSdks.addAll(Arrays.asList(session.env().platforms().findPlatforms()));
            storeModelMain.setPlatforms(plainSdks);
            storeModelMain.setRepositories(new ArrayList<>(
                    Arrays.stream(session.repos().getRepositories()).filter(x -> !x.config().isTemporary())
                            .map(x -> x.config().getRepositoryRef()).collect(Collectors.toList())
            ));

            NutsPath file = configVersionSpecificLocation.resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
            storeModelMain.setConfigVersion(current().getApiVersion());
            if (storeModelMain.getCommandFactories() != null) {
                for (NutsCommandFactoryConfig item : storeModelMain.getCommandFactories()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            if (storeModelMain.getRepositories() != null) {
                for (NutsRepositoryRef item : storeModelMain.getRepositories()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            if (storeModelMain.getPlatforms() != null) {
                for (NutsPlatformLocation item : storeModelMain.getPlatforms()) {
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
            NutsPath afile = apiVersionSpecificLocation.resolve(NutsConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            storeModelApi.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NutsUserConfig item : storeModelSecurity.getUsers()) {
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
            NutsPath runtimeVersionSpecificLocation = session.locations().getStoreLocation(NutsStoreLocation.CONFIG)
                    .resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(session.getWorkspace().getRuntimeId()));
            NutsPath afile = runtimeVersionSpecificLocation.resolve(NutsConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
            storeModelRuntime.setConfigVersion(current().getApiVersion());
            elem.setSession(session).json().setValue(storeModelRuntime)
                    .setNtf(false).print(afile);
            storeModelRuntimeChanged = false;
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repo : session.repos().getRepositories()) {
            try {
                if (repo.config() instanceof NutsRepositoryConfigManagerExt) {
                    ok |= ((NutsRepositoryConfigManagerExt) (repo.config())).getModel().save(force, session);
                }
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }

        return ok;
    }

    public boolean save(NutsSession session) {
        return save(true, session);
    }

    public NutsWorkspaceBootConfig loadBootConfig(String _ws, boolean global, boolean followLinks, NutsSession session) {
        String _ws0 = _ws;
        String effWorkspaceName = null;
        String lastConfigPath = null;
        NutsWorkspaceConfigBoot lastConfigLoaded = null;
        boolean defaultLocation = false;
        if (_ws != null && _ws.matches("[a-z-]+://.*")) {
            //this is a protocol based workspace
            //String protocol=ws.substring(0,ws.indexOf("://"));
            effWorkspaceName = "remote-bootstrap";
            lastConfigPath = NutsUtilPlatforms.getWorkspaceLocation(null,
                    global,
                    CoreNutsUtils.resolveValidWorkspaceName(effWorkspaceName));
            lastConfigLoaded = parseBootConfig(NutsPath.of(lastConfigPath, session), session);
            defaultLocation = true;
            return new DefaultNutsWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else if (followLinks) {
            defaultLocation = CoreNutsUtils.isValidWorkspaceName(_ws);
            int maxDepth = 36;
            for (int i = 0; i < maxDepth; i++) {
                lastConfigPath
                        = CoreNutsUtils.isValidWorkspaceName(_ws)
                        ? NutsUtilPlatforms.getWorkspaceLocation(
                        null,
                        global,
                        CoreNutsUtils.resolveValidWorkspaceName(_ws)
                ) : CoreIOUtils.getAbsolutePath(_ws);

                NutsWorkspaceConfigBoot configLoaded = parseBootConfig(NutsPath.of(lastConfigPath, session), session);
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
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cyclic workspace resolution"));
                }
            }
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNutsUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNutsWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else {
            defaultLocation = CoreNutsUtils.isValidWorkspaceName(_ws);
            lastConfigPath
                    = CoreNutsUtils.isValidWorkspaceName(_ws)
                    ? NutsUtilPlatforms.getWorkspaceLocation(
                    null,
                    global,
                    CoreNutsUtils.resolveValidWorkspaceName(_ws)
            ) : CoreIOUtils.getAbsolutePath(_ws);

            lastConfigLoaded = parseBootConfig(NutsPath.of(lastConfigPath, session), session);
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNutsUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNutsWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        }
    }

    public boolean isExcludedExtension(String extensionId, NutsWorkspaceOptions options, NutsSession session) {
        if (extensionId != null && options != null) {
            NutsId pnid = NutsId.of(extensionId, session);
            String shortName = pnid.getShortName();
            String artifactId = pnid.getArtifactId();
            for (String excludedExtensionList : options.getExcludedExtensions()) {
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

    public NutsWorkspaceOptions getOptions(NutsSession session) {
        CoreNutsBootOptions bOptions = NutsWorkspaceExt.of(ws).getModel().bootModel.getCoreBootOptions();
        return new ReadOnlyNutsWorkspaceOptions(bOptions.getOptions(), session);
    }

    public boolean isSupportedRepositoryType(String repositoryType, NutsSession session) {
        if (NutsBlankable.isBlank(repositoryType)) {
            repositoryType = NutsConstants.RepoTypes.NUTS;
        }
        return session.extensions().createAllSupported(NutsRepositoryFactoryComponent.class,
                new NutsRepositoryConfig().setLocation(
                        NutsRepositoryLocation.of(repositoryType + "@")
                )).size() > 0;
    }

    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        List<NutsAddRepositoryOptions> all = new ArrayList<>();
        for (NutsRepositoryFactoryComponent provider : session.extensions()
                .createAll(NutsRepositoryFactoryComponent.class)) {
            for (NutsAddRepositoryOptions d : provider.getDefaultRepositories(session)) {
                all.add(d);
            }
        }
        Collections.sort(all, new Comparator<NutsAddRepositoryOptions>() {

            public int compare(NutsAddRepositoryOptions o1, NutsAddRepositoryOptions o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        return all.toArray(new NutsAddRepositoryOptions[0]);
    }

    public Set<String> getAvailableArchetypes(NutsSession session) {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NutsWorkspaceArchetypeComponent extension : session.extensions()
                .createAllSupported(NutsWorkspaceArchetypeComponent.class, null)) {
            set.add(extension.getName());
        }
        return set;
    }

    public NutsPath resolveRepositoryPath(NutsPath repositoryLocation, NutsSession session) {
        NutsPath root = this.getRepositoriesRoot(session);
        return repositoryLocation
                .toAbsolute(root != null ? root :
                        session.locations().getStoreLocation(NutsStoreLocation.CONFIG)
                                .resolve(NutsConstants.Folders.REPOSITORIES))
                ;
    }

    public NutsIndexStoreFactory getIndexStoreClientFactory() {
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

    public boolean isGlobal() {
        return current().isGlobal();
    }

    public long getCreationStartTimeMillis() {
        return startCreateTime;
    }

    public long getCreationFinishTimeMillis() {
        return endCreateTime;
    }

    public long getCreationTimeMillis() {
        return endCreateTime - startCreateTime;
    }

    public NutsWorkspaceConfigMain getStoreModelMain() {
        return storeModelMain;
    }

    public DefaultNutsWorkspaceCurrentConfig current() {
        if (currentConfig == null) {
            throw new IllegalStateException("unable to use workspace.current(). Still in initialize status");
        }
        return currentConfig;
    }

    public void setStartCreateTimeMillis(long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public void setConfigBoot(NutsWorkspaceConfigBoot config, NutsSession options) {
        setConfigBoot(config, options, true);
    }

    public void setConfigApi(NutsWorkspaceConfigApi config, NutsSession session) {
        setConfigApi(config, session, true);
    }

    public void setConfigRuntime(NutsWorkspaceConfigRuntime config, NutsSession options) {
        setConfigRuntime(config, options, true);
    }

    public void setConfigSecurity(NutsWorkspaceConfigSecurity config, NutsSession session) {
        setConfigSecurity(config, session, true);
    }

    public void setConfigMain(NutsWorkspaceConfigMain config, NutsSession session) {
        setConfigMain(config, session, true);
    }

    public void setEndCreateTimeMillis(long endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    public void installBootIds(NutsSession session) {
        NutsWorkspaceModel wsModel = NutsWorkspaceExt.of(ws).getModel();
        NutsBootDescriptor d = wsModel.bootModel.getCoreBootOptions().getRuntimeBootDescriptor();
        NutsId iruntimeId = NutsId.of(d.getId().toString(), session);
        wsModel.configModel.prepareBootClassPathConf(NutsIdType.API, ws.getApiId(), null, iruntimeId, false, false, session);
        wsModel.configModel.prepareBootClassPathJar(ws.getApiId(), null, iruntimeId, false, session);
        wsModel.configModel.prepareBootClassPathConf(NutsIdType.RUNTIME, iruntimeId, ws.getApiId(), null, false, true, session);
        wsModel.configModel.prepareBootClassPathJar(iruntimeId, ws.getApiId(), null, true, session);
        List<NutsWorkspaceConfigBoot.ExtensionConfig> extensions = getStoredConfigBoot().getExtensions();
        if (extensions != null) {
            for (NutsWorkspaceConfigBoot.ExtensionConfig extension : extensions) {
                if (extension.isEnabled()) {
                    wsModel.configModel.prepareBootClassPathConf(NutsIdType.EXTENSION,
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
                }
            }
        }
    }

    public boolean isConfigurationChanged() {
        return storeModelBootChanged || storeModelApiChanged || storeModelRuntimeChanged || storeModelSecurityChanged || storeModelMainChanged;
    }

    public boolean loadWorkspace(NutsSession session) {
        try {
            NutsSessionUtils.checkSession(ws, session);
            NutsWorkspaceConfigBoot _config = parseBootConfig(session);
            if (_config == null) {
                return false;
            }
            DefaultNutsWorkspaceCurrentConfig cConfig = new DefaultNutsWorkspaceCurrentConfig(ws).merge(_config, session);
            CoreNutsBootOptions bOptions = NutsWorkspaceExt.of(ws).getModel().bootModel.getCoreBootOptions();
            if (cConfig.getApiId() == null) {
                cConfig.setApiId(NutsId.of(NutsConstants.Ids.NUTS_API + "#" + bOptions.getApiVersion(), session));
            }
            if (cConfig.getRuntimeId() == null) {
                cConfig.setRuntimeId(bOptions.getRuntimeId().toString(), session);
            }
            if (cConfig.getRuntimeBootDescriptor() == null) {
                cConfig.setRuntimeBootDescriptor(bOptions.getRuntimeBootDescriptor());
            }
            if (cConfig.getExtensionBootDescriptors() == null) {
                cConfig.setExtensionBootDescriptors(bOptions.getExtensionBootDescriptors());
            }
            if (cConfig.getBootRepositories() == null) {
                cConfig.setBootRepositories(bOptions.getBootRepositories());
            }
            cConfig.merge(getOptions(session), session);

            setCurrentConfig(cConfig.build(session.locations().getWorkspaceLocation(), session));

            NutsVersionCompat compat = createNutsVersionCompat(Nuts.getVersion(), session);
            NutsId apiId = session.getWorkspace().getApiId();
            NutsWorkspaceConfigApi aconfig = compat.parseApiConfig(apiId, session);
            NutsId toImportOlderId=null;
            if (aconfig != null) {
                cConfig.merge(aconfig, session);
            }else{
                // will try to find older versions
                List<NutsId> olderIds = findOlderNutsApiIds(session);
                for (NutsId olderId : olderIds) {
                    aconfig = compat.parseApiConfig(olderId, session);
                    if (aconfig != null) {
                        // ask
                        if(session.getTerminal().ask().forBoolean(NutsMessage.cstyle("import older config %s",apiId))
                                .setDefaultValue(true)
                                .getBooleanValue()
                        ){
                            toImportOlderId=olderId;
                            cConfig.merge(aconfig, session);
                        }
                        break;
                    }
                }

            }
            NutsWorkspaceConfigRuntime rconfig = compat.parseRuntimeConfig(session);
            if (rconfig != null) {
                cConfig.merge(rconfig, session);
            }
            NutsWorkspaceConfigSecurity sconfig = compat.parseSecurityConfig(apiId, session);
            if(sconfig==null){
                if(toImportOlderId!=null){
                    sconfig=compat.parseSecurityConfig(toImportOlderId, session);
                }
            }
            NutsWorkspaceConfigMain mconfig = compat.parseMainConfig(apiId, session);
            if(mconfig==null){
                if(toImportOlderId!=null){
                    mconfig=compat.parseMainConfig(toImportOlderId, session);
                }
            }

            if (bOptions.getOptions().isRecover() || bOptions.getOptions().isReset()) {
                //always reload boot resolved versions!
                cConfig.setApiId(NutsId.of(NutsConstants.Ids.NUTS_API + "#" + bOptions.getApiVersion(), session));
                cConfig.setRuntimeId(bOptions.getRuntimeId() == null ? null : bOptions.getRuntimeId().toString(), session);
                cConfig.setRuntimeBootDescriptor(bOptions.getRuntimeBootDescriptor());
                cConfig.setExtensionBootDescriptors(bOptions.getExtensionBootDescriptors());
                cConfig.setBootRepositories(bOptions.getBootRepositories());
            }
            setCurrentConfig(cConfig
                    .build(session.locations().getWorkspaceLocation(), session)
            );
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
            if (session.boot().getBootOptions().isRecover()) {
                onLoadWorkspaceError(ex, session);
            } else {
                throw ex;
            }
        }
        return false;
    }

    private List<NutsId> findOlderNutsApiIds(NutsSession session) {
        NutsId apiId=session.getWorkspace().getApiId();
        NutsPath path = session.locations().getStoreLocation(apiId, NutsStoreLocation.CONFIG)
                .getParent();
        List<NutsId> olderIds= path.list().filter(NutsPath::isDirectory, elems->elems.ofString("isDirectory"))
                .map(x->NutsVersion.of(x.getName(), session),"toVersion")
                .filter(x->x.compareTo(apiId.getVersion())<0, elems->elems.ofString("older"))
                .sorted(new NutsComparator<NutsVersion>() {
                    @Override
                    public int compare(NutsVersion o1, NutsVersion o2) {
                        return Comparator.<NutsVersion>reverseOrder().compare(o1,o2);
                    }

                    @Override
                    public NutsElement describe(NutsElements elems) {
                        return elems.ofString("reverseOrder");
                    }
                }).map(x-> apiId.builder().setVersion(x).build(), elems->elems.ofString("toId"))
                .toList();
        return olderIds;
    }

    public void setBootApiVersion(String value, NutsSession session) {
        if (!Objects.equals(value, storeModelApi.getApiVersion())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setApiVersion(value);
            fireConfigurationChanged("api-version", session, ConfigEventType.API);
        }
    }

    public void setExtraBootExtensionId(NutsId apiId, NutsId extensionId, NutsDependency[] deps, NutsSession session) {
        String newDeps = Arrays.stream(deps).map(Object::toString).collect(Collectors.joining(";"));
        NutsWorkspaceConfigBoot.ExtensionConfig cc = new NutsWorkspaceConfigBoot.ExtensionConfig();
        cc.setId(apiId);
        cc.setDependencies(newDeps);
        cc.setEnabled(true);
        if (apiId.getVersion().equals(session.getWorkspace().getApiId().getVersion())) {
            NutsExtensionListHelper h = new NutsExtensionListHelper(session.getWorkspace().getApiId(),
                    getStoredConfigBoot().getExtensions()).save();
            if (h.add(extensionId, deps)) {
                getStoredConfigBoot().setExtensions(h.getConfs());
                NutsWorkspaceExt.of(ws).deployBoot(session, extensionId, true);
                fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
                DefaultNutsWorkspaceConfigModel configModel = NutsWorkspaceExt.of(session).getModel().configModel;
                configModel.save(session);
            }
        } else {
            //TODO, how to get old deps ?
            NutsExtensionListHelper h2 = new NutsExtensionListHelper(session.getWorkspace().getApiId(), new ArrayList<>());
            if (h2.add(extensionId, deps)) {
                NutsWorkspaceExt.of(ws).deployBoot(session, extensionId, true);
            }
        }
        NutsPath runtimeVersionSpecificLocation = session.locations().getStoreLocation(NutsStoreLocation.CONFIG)
                .resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(extensionId));
        NutsPath afile = runtimeVersionSpecificLocation.resolve(NutsConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
        cc.setConfigVersion(current().getApiVersion());
        NutsElements.of(session).json().setValue(cc)
                .setNtf(false).print(afile);
    }

    public void setExtraBootRuntimeId(NutsId apiId, NutsId runtimeId, NutsDependency[] deps, NutsSession session) {
        String newDeps = Arrays.stream(deps).map(Object::toString).collect(Collectors.joining(";"));
        if (apiId == null || apiId.getVersion().equals(session.getWorkspace().getApiId().getVersion())) {
            if (!Objects.equals(runtimeId.toString(), storeModelApi.getRuntimeId())
                    || !Objects.equals(newDeps, storeModelRuntime.getDependencies())
            ) {
//            options = CoreNutsUtils.validate(options, ws);
                storeModelApi.setRuntimeId(runtimeId.toString());
                storeModelRuntime.setDependencies(newDeps);
                setConfigRuntime(storeModelRuntime, session, true);
                fireConfigurationChanged("runtime-id", session, ConfigEventType.API);
            }
            setBootRuntimeId(runtimeId.toString(),
                    newDeps,
                    session);
            save(session);
            return;
        }
        NutsWorkspaceConfigApi estoreModelApi = new NutsWorkspaceConfigApi();
        estoreModelApi.setApiVersion(apiId.getVersion().toString());
        estoreModelApi.setRuntimeId(runtimeId.toString());
        estoreModelApi.setConfigVersion(current().getApiVersion());
        NutsPath apiVersionSpecificLocation = session.locations().getStoreLocation(apiId, NutsStoreLocation.CONFIG);
        NutsPath afile = apiVersionSpecificLocation.resolve(NutsConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        NutsElements elems = NutsElements.of(session);
        elems.json().setValue(estoreModelApi)
                .setNtf(false).print(afile);

        NutsWorkspaceConfigRuntime storeModelRuntime = new NutsWorkspaceConfigRuntime();
        storeModelRuntime.setId(runtimeId.toString());
        storeModelRuntime.setDependencies(
                newDeps
        );

        NutsPath runtimeVersionSpecificLocation = session.locations().getStoreLocation(NutsStoreLocation.CONFIG)
                .resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(runtimeId));
        afile = runtimeVersionSpecificLocation.resolve(NutsConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        storeModelRuntime.setConfigVersion(current().getApiVersion());
        elems.setSession(session).json().setValue(storeModelRuntime)
                .setNtf(false).print(afile);

    }

    public void setBootRuntimeId(String value, String dependencies, NutsSession session) {
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

    public void setBootRuntimeDependencies(String dependencies, NutsSession session) {
        if (!Objects.equals(dependencies, storeModelRuntime.getDependencies())) {
//            options = CoreNutsUtils.validate(options, ws);
        }
    }

    public void setBootRepositories(String value, NutsSession session) {
        if (!Objects.equals(value, storeModelBoot.getBootRepositories())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelBoot.setBootRepositories(value);
            fireConfigurationChanged("boot-repositories", session, ConfigEventType.API);
        }
    }

    public NutsWorkspaceConfigBoot.ExtensionConfig getBootExtension(String value, NutsSession session) {
        NutsId newId = NutsId.of(value, session);
        for (NutsWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
            NutsId id = extension.getId();
            if (newId.equalsShortId(id)) {
                return extension;
            }
        }
        return null;
    }

    public void setBootExtension(String value, String dependencies, boolean enabled, NutsSession session) {
        NutsId newId = NutsId.of(value, session);
        for (NutsWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
            NutsId id = extension.getId();
            if (newId.equalsShortId(id)) {
                extension.setId(newId);
                extension.setEnabled(enabled);
                extension.setDependencies(dependencies);
                fireConfigurationChanged("boot-extensions", session, ConfigEventType.API);
                return;
            }
        }
        storeModelBoot.getExtensions().add(new NutsWorkspaceConfigBoot.ExtensionConfig(newId, dependencies, true));
    }

    public NutsUserConfig getUser(String userId, NutsSession session) {
        NutsSessionUtils.checkSession(ws, session);
        NutsUserConfig _config = getSecurity(userId);
        if (_config == null) {
            if (NutsConstants.Users.ADMIN.equals(userId) || NutsConstants.Users.ANONYMOUS.equals(userId)) {
                _config = new NutsUserConfig(userId, null, null, null);
                setUser(_config, session);
            }
        }
        return _config;
    }

    public NutsUserConfig[] getUsers(NutsSession session) {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        return configUsers.values().toArray(new NutsUserConfig[0]);
    }

    public void setUser(NutsUserConfig config, NutsSession session) {
        if (config != null) {
            configUsers.put(config.getUser(), config);
            fireConfigurationChanged("user", session, ConfigEventType.SECURITY);
        }
    }

    public void removeUser(String userId, NutsSession session) {
        NutsUserConfig old = getSecurity(userId);
        if (old != null) {
            configUsers.remove(userId);
            fireConfigurationChanged("users", session, ConfigEventType.SECURITY);
        }
    }

    public void setSecure(boolean secure, NutsSession session) {
        if (secure != storeModelSecurity.isSecure()) {
            storeModelSecurity.setSecure(secure);
            fireConfigurationChanged("secure", session, ConfigEventType.SECURITY);
        }
    }

    public void fireConfigurationChanged(String configName, NutsSession session, ConfigEventType t) {
//        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        ((DefaultImportManager) session.imports()).getModel().invalidateCache();
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
        DefaultNutsWorkspaceEvent evt = new DefaultNutsWorkspaceEvent(session, null, "config." + configName, null, true);
        for (NutsWorkspaceListener workspaceListener : session.events().getWorkspaceListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    //    
    public NutsWorkspaceConfigApi getStoredConfigApi() {
        if (storeModelApi.getApiVersion() == null) {
            storeModelApi.setApiVersion(Nuts.getVersion());
        }
        return storeModelApi;
    }

    public NutsWorkspaceConfigBoot getStoredConfigBoot() {
        return storeModelBoot;
    }

    public NutsWorkspaceConfigSecurity getStoredConfigSecurity() {
        return storeModelSecurity;
    }

    public NutsWorkspaceConfigMain getStoredConfigMain() {
        return storeModelMain;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public NutsDependencySolver createDependencySolver(String name, NutsSession session) {
        NutsDependencySolverFactory c = getSolversMap(session).get(NutsDependencySolverUtils.resolveSolverName(name));
        if (c != null) {
            return c.create(session);
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("dependency solver not found %s", name));
    }

    private Map<String, NutsDependencySolverFactory> getSolversMap(NutsSession session) {
        if (dependencySolvers == null) {
            dependencySolvers = new LinkedHashMap<>();
            for (NutsDependencySolverFactory nutsDependencySolver : session.extensions().createAllSupported(NutsDependencySolverFactory.class, null)) {
                dependencySolvers.put(nutsDependencySolver.getName(), nutsDependencySolver);
            }
        }
        return dependencySolvers;
    }

    public NutsDependencySolverFactory[] getDependencySolvers(NutsSession session) {
        return getSolversMap(session).values().toArray(new NutsDependencySolverFactory[0]);
    }


    public NutsPath getRepositoriesRoot(NutsSession session) {
        return session.locations().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    public NutsPath getTempRepositoriesRoot(NutsSession session) {
        return session.locations().getStoreLocation(NutsStoreLocation.TEMP).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    public boolean isValidWorkspaceFolder(NutsSession session) {
        Path file = session.locations().getWorkspaceLocation().toFile().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        return Files.isRegularFile(file);
    }

    public NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent, NutsSession session) {
        authenticationAgent = NutsUtilStrings.trim(authenticationAgent);
        NutsAuthenticationAgent supported = null;
        if (authenticationAgent.isEmpty()) {
            supported = session.extensions().createSupported(NutsAuthenticationAgent.class, true, "");
        } else {
            List<NutsAuthenticationAgent> agents = session.extensions().createAllSupported(NutsAuthenticationAgent.class, authenticationAgent);
            for (NutsAuthenticationAgent agent : agents) {
                if (agent.getId().equals(authenticationAgent)) {
                    supported = agent;
                }
            }
        }
        if (supported == null) {
            throw new NutsExtensionNotFoundException(session, NutsAuthenticationAgent.class, authenticationAgent);
        }
        NutsSessionUtils.setSession(supported, session);
        return supported;
    }

    //
//    public void setExcludedRepositories(String[] excludedRepositories, NutsUpdateOptions options) {
//        excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedRepositories), " ,;"));
//    }
    public void setUsers(NutsUserConfig[] users, NutsSession session) {
        for (NutsUserConfig u : getUsers(session)) {
            removeUser(u.getUser(), session);
        }
        for (NutsUserConfig conf : users) {
            setUser(conf, session);
        }
    }

    public NutsWorkspaceConfigRuntime getStoredConfigRuntime() {
        return storeModelRuntime;
    }

    public NutsId createSdkId(String type, String version, NutsSession session) {
        return NutsWorkspaceUtils.of(session).createSdkId(type, version);
    }

    public void onExtensionsPrepared(NutsSession session) {
        try {
            indexStoreClientFactory = session.extensions().createSupported(NutsIndexStoreFactory.class, false, null);
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNutsIndexStoreFactory();
        }
    }

    public void setConfigApi(NutsWorkspaceConfigApi config, NutsSession session, boolean fire) {
        this.storeModelApi = config == null ? new NutsWorkspaceConfigApi() : config;
        if (fire) {
            fireConfigurationChanged("boot-api-config", session, ConfigEventType.API);
        }
    }

    public void setConfigRuntime(NutsWorkspaceConfigRuntime config, NutsSession session, boolean fire) {
        this.storeModelRuntime = config == null ? new NutsWorkspaceConfigRuntime() : config;
        if (fire) {
            fireConfigurationChanged("boot-runtime-config", session, ConfigEventType.RUNTIME);
        }
    }

    private void setConfigSecurity(NutsWorkspaceConfigSecurity config, NutsSession session, boolean fire) {
        this.storeModelSecurity = config == null ? new NutsWorkspaceConfigSecurity() : config;
        configUsers.clear();
        if (this.storeModelSecurity.getUsers() != null) {
            for (NutsUserConfig s : this.storeModelSecurity.getUsers()) {
                configUsers.put(s.getUser(), s);
            }
        }
        storeModelSecurityChanged = true;
        if (fire) {
            fireConfigurationChanged("config-security", session, ConfigEventType.SECURITY);
        }
    }

    private void setConfigMain(NutsWorkspaceConfigMain config, NutsSession session, boolean fire) {
        this.storeModelMain = config == null ? new NutsWorkspaceConfigMain() : config;
        DefaultNutsPlatformManager d = (DefaultNutsPlatformManager) session.env().platforms();
        d.getModel().setPlatforms(this.storeModelMain.getPlatforms().toArray(new NutsPlatformLocation[0]), session);
        NutsRepositoryManager repos = session.repos();
        repos.removeAllRepositories();
        if (this.storeModelMain.getRepositories() != null) {
            for (NutsRepositoryRef ref : this.storeModelMain.getRepositories()) {
                repos
                        .addRepository(
                                NutsRepositoryUtils.refToOptions(ref)
                        );
            }
        }

        storeModelMainChanged = true;
        if (fire) {
            fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
        }
    }

    private void setConfigBoot(NutsWorkspaceConfigBoot config, NutsSession session, boolean fire) {
        this.storeModelBoot = config;
        if (NutsBlankable.isBlank(config.getUuid())) {
            config.setUuid(UUID.randomUUID().toString());
            fire = true;
        }
        if (fire) {
            fireConfigurationChanged("config-master", session, ConfigEventType.BOOT);
        }
    }

//    public String getBootClassWorldString(NutsSession session) {
//        StringBuilder sb = new StringBuilder();
//        for (URL bootClassWorldURL : getBootClassWorldURLs()) {
//            if (sb.length() > 0) {
//                sb.append(File.pathSeparator);
//            }
//            if (CoreIOUtils.isPathFile(bootClassWorldURL.toString())) {
//                File f = CoreIOUtils.toPathFile(bootClassWorldURL.toString(), session).toFile();
//                sb.append(f.getPath());
//            } else {
//                sb.append(bootClassWorldURL.toString().replace(":", "\\:"));
//            }
//        }
//        return sb.toString();
//    }

    //    
//    public Path getBootNutsJar() {
//        try {
//            NutsId baseId = ws.id().parseRequired(NutsConstants.Ids.NUTS_API);
//            String urlPath = "META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
//            URL resource = Nuts.class.getResource(urlPath);
//            if (resource != null) {
//                URL runtimeURL = CoreIOUtils.resolveURLFromResource(Nuts.class, urlPath);
//                return CoreIOUtils.resolveLocalPathFromURL(runtimeURL);
//            }
//        } catch (Exception e) {
//            //e.printStackTrace();
//        }
//        // This will happen when running app from  nuts dev project so that classes folder is considered as
//        // binary class path instead of a single jar file.
//        // In that case we will gather nuts from maven .m2 repository
//        PomId m = PomIdResolver.resolvePomId(Nuts.class, null);
//        if (m != null) {
//            Path f = ws.io().path(System.getProperty("user.home"), ".m2", "repository", m.getGroupId().replace('.', '/'), m.getArtifactId(), m.getVersion(),
//                    ws.locations().getDefaultIdFilename(
//                            ws.elements().setGroup(m.getGroupId()).setName(m.getArtifactId()).setVersion(m.getVersion())
//                                    .setFaceComponent()
//                                    .setPackaging("jar")
//                                    .build()
//                    ));
//            if (Files.exists(f)) {
//                return f;
//            }
//        }
//        return null;
//    }
    public String toString() {
        String s1 = "NULL";
        String s2 = "NULL";
        s1 = ws.getApiId().toString();
        s2 = String.valueOf(ws.getRuntimeId());
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((currentConfig == null) ? "NULL" : ("'"
                + ((DefaultNutsWorkspaceLocationManager) (NutsSessionUtils.defaultSession(ws))
                .locations()).getModel().getWorkspaceLocation() + '\''))
                + '}';
    }

    public void collect(NutsClassLoaderNode n, LinkedHashMap<String, NutsClassLoaderNode> deps) {
        if (!deps.containsKey(n.getId())) {
            deps.put(n.getId(), n);
            for (NutsClassLoaderNode d : n.getDependencies()) {
                collect(d, deps);
            }
        }
    }

    public NutsBootDef fetchBootDef(NutsId id, boolean content, NutsSession session) {
        NutsDefinition nd = session.fetch().setId(id)
                .setDependencies(true).setContent(content)
                .setDependencyFilter(NutsDependencyFilters.of(session).byRunnable())
                .setFailFast(false).getResultDefinition();
        if (nd != null) {
            if(content && nd.getContent()==null){
                //this is an unexpected behaviour, fail fast
                throw new NutsNotFoundException(session, id);
            }
            return new NutsBootDef(nd.getId(), nd.getDependencies().transitive().toList().toArray(new NutsDependency[0]),
                    (content && nd.getContent() != null) ? nd.getContent().getPath() : null);
        }
        if (isFirstBoot()) {
            NutsClassLoaderNode n = searchBootNode(id, session);
            if (n != null) {
                LinkedHashMap<String, NutsClassLoaderNode> dm = new LinkedHashMap<>();
                for (NutsClassLoaderNode d : n.getDependencies()) {
                    collect(d, dm);
                }
                return new NutsBootDef(
                        id,
                        dm.values().stream().map(x -> NutsDependency.of(x.getId(), session)).toArray(NutsDependency[]::new),
                        NutsPath.of(n.getURL(), session)
                );
            }
            MavenUtils.DepsAndRepos dd = MavenUtils.of(session).loadDependenciesAndRepositoriesFromPomPath(id,
                    resolveBootRepositoriesBootSelectionArray(session),
                    session);
            if (dd != null) {
                String contentPath = id.getGroupId().replace('.', '/')
                        + "/" + id.getArtifactId()
                        + "/" + id.getVersion()
                        + "/" + id.getArtifactId() + "-" + id.getVersion();
                NutsPath pp = null;
                for (String repo : dd.repos) {
                    NutsRepositoryLocation r = NutsRepositoryLocation.of(repo);
                    NutsPath base = NutsPath.of(r.getPath(), session);
                    if (base.isLocal() && base.isDirectory()) {
                        NutsPath a = base.resolve(contentPath + ".jar");
                        if (a.isRegularFile()) {
                            pp = a;
                            break;
                        }
                    }
                }
                if (pp != null) {
                    NutsDescriptor d = NutsDescriptorParser.of(session)
                            .setDescriptorStyle(NutsDescriptorStyle.MAVEN)
                            .parse(pp);
                    //see only first level deps!
                    return new NutsBootDef(
                            id,
                            d.getDependencies(),
                            pp
                    );
                }
                //bootDescFileContent.put("bootRepositories", String.join(";", dd.repos));
            }
        }
        throw new NutsNotFoundException(session, id);
    }

    public void prepareBootClassPathConf(NutsIdType idType, NutsId id, NutsId forId, NutsId forceRuntimeId, boolean force, boolean processDependencies, NutsSession session) {
        //do not create boot file for nuts (it has no dependencies anyways!)
        switch (idType) {
            case API: {
                return;
            }
            case RUNTIME: {
                NutsBootDef d = fetchBootDef(id, false, session);
                for (NutsId apiId : CoreNutsUtils.resolveNutsApiIds(d.deps, session)) {
                    setExtraBootRuntimeId(apiId, d.id, d.deps, session);
                }
                break;
            }
            case EXTENSION: {
                NutsBootDef d = fetchBootDef(id, false, session);
                for (NutsId apiId : CoreNutsUtils.resolveNutsApiIds(d.deps, session)) {
                    setExtraBootRuntimeId(apiId, d.id, d.deps, session);
                }
            }
        }
    }

    public void prepareBootClassPathJar(NutsId id, NutsId forId, NutsId forceRuntimeId, boolean processDependencies, NutsSession session) {
        NutsBootDef d = fetchBootDef(id, true, session);
        if (deployToInstalledRepository(d.content.toFile(), session)) {
            if (processDependencies) {
                for (NutsDependency dep : d.deps) {
                    prepareBootClassPathJar(dep.toId(), id, forceRuntimeId, true, session);
                }
            }
        }
    }

    private boolean isFirstBoot() {
        return ws.boot().isFirstBoot();
    }

    private boolean deployToInstalledRepository(Path tmp, NutsSession session) {
//        NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
//        NutsDeployRepositoryCommand desc = repoSPI.deploy()
//                .setId(id)
//                .setSession(session.copy().setConfirm(NutsConfirmationMode.YES))
//                .setContent(contentPath)
//                //.setFetchMode(NutsFetchMode.LOCAL)
//                .run();
//        repo.install(id, session, forId);

        NutsInstalledRepository ins = NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository();
        NutsDescriptor descriptor = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(tmp, null, session);
        if (descriptor != null) {
            DefaultNutsDefinition b = new DefaultNutsDefinition(
                    null, null,
                    descriptor.getId(),
                    descriptor, new NutsDefaultContent(NutsPath.of(tmp, session), true, true),
                    new DefaultNutsInstallInfo(descriptor.getId(), NutsInstallStatus.NONE, null, null, null, null, null, null, false, false),
                    null, session
            );
            ins.install(b, session);
            return true;
        }
        return false;
    }

    private NutsClassLoaderNode searchBootNode(NutsId id, NutsSession session) {
        NutsBootManager boot = session.boot();
        List<NutsClassLoaderNode> all = new ArrayList();
        all.add(boot.getBootRuntimeClassLoaderNode());
        all.addAll(Arrays.asList(boot.getBootExtensionClassLoaderNode()));
        return searchBootNode(id, all.toArray(new NutsClassLoaderNode[0]));
    }

    private NutsClassLoaderNode searchBootNode(NutsId id, NutsClassLoaderNode[] into) {
        for (NutsClassLoaderNode n : into) {
            if (n != null) {
                if (id.getLongName().equals(n.getId())) {
                    return n;
                }
            }
            NutsClassLoaderNode a = searchBootNode(id, n.getDependencies());
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    public void onPreUpdateConfig(String confName, NutsSession session) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
    }

    public void onPostUpdateConfig(String confName, NutsSession session) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        DefaultNutsWorkspaceCurrentConfig d = currentConfig;
        d.setUserStoreLocations(new NutsStoreLocationsMap(storeModelBoot.getStoreLocations()).toMapOrNull());
        d.setHomeLocations(new NutsHomeLocationsMap(storeModelBoot.getHomeLocations()).toMapOrNull());
        d.build(session.locations().getWorkspaceLocation(), session);
        NutsStoreLocationsMap newSL = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        for (NutsStoreLocation sl : NutsStoreLocation.values()) {
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

    private void onLoadWorkspaceError(Throwable ex, NutsSession session) {
        DefaultNutsWorkspaceConfigModel wconfig = this;
        Path file = session.locations().getWorkspaceLocation().toFile().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        if (wconfig.isReadOnly()) {
            throw new NutsIOException(session, NutsMessage.cstyle("unable to load config file %s", file), ex);
        }
        String fileSuffix = Instant.now().toString();
        fileSuffix = fileSuffix.replace(':', '-');
        String fileName = "nuts-workspace-" + fileSuffix;
        NutsPath logError = session.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.LOG).resolve("invalid-config");
        NutsPath logFile = logError.resolve(fileName + ".error");
        _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                .log(NutsMessage.jstyle("erroneous workspace config file. Unable to load file {0} : {1}", file, ex));

        try {
            logFile.mkParentDirs();
        } catch (Exception ex1) {
            throw new NutsIOException(session, NutsMessage.cstyle("unable to log workspace error while loading config file %s : %s", file, ex1), ex);
        }
        NutsPath newfile = logError.resolve(fileName + ".json");
        _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                .log(NutsMessage.jstyle("erroneous workspace config file will be replaced by a fresh one. Old config is copied to {0}\n error logged to  {1}", newfile.toString(), logFile));
        try {
            Files.move(file, newfile.toFile());
        } catch (IOException e) {
            throw new NutsIOException(session, NutsMessage.cstyle("unable to load and re-create config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logFile.getOutputStream())) {
            o.println("workspace.path:");
            o.println(session.locations().getWorkspaceLocation());
            o.println("workspace.options:");
            o.println(wconfig.getOptions(session).formatter().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                o.println("location." + location.id() + ":");
                o.println(session.locations().getStoreLocation(location));
            }
            o.println("java.class.path:");
            o.println(System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

    public NutsUserConfig getSecurity(String id) {
        return configUsers.get(id);
    }

    private NutsWorkspaceConfigBoot parseBootConfig(NutsSession session) {
        return parseBootConfig(session.locations().getWorkspaceLocation(), session);
    }

    private NutsWorkspaceConfigBoot parseBootConfig(NutsPath path, NutsSession session) {
        Path file = path.toFile().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(file, session);
        if (bytes == null) {
            return null;
        }
        try {
            Map<String, Object> a_config0 = NutsElements.of(session).json().parse(bytes, Map.class);
            String version = (String) a_config0.get("configVersion");
            if (version == null) {
                version = (String) a_config0.get("createApiVersion");
                if (version == null) {
                    version = Nuts.getVersion();
                }
            }
            return createNutsVersionCompat(version, session).parseConfig(bytes, session);
        } catch (Exception ex) {
            _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("erroneous workspace config file. Unable to load file {0} : {1}",
                            file, ex));
            throw new NutsIOException(session, NutsMessage.cstyle("unable to load config file %s", file), ex);
        }
    }

    private NutsVersionCompat createNutsVersionCompat(String apiVersion, NutsSession session) {
        int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(apiVersion);
        if (buildNumber >= 803) {
            return new NutsVersionCompat803(session, apiVersion);
        } else if (buildNumber >= 507) {
            return new NutsVersionCompat507(session, apiVersion);
        } else if (buildNumber >= 506) {
            return new NutsVersionCompat506(session, apiVersion);
        } else {
            return new NutsVersionCompat502(session, apiVersion);
        }
    }

    public NutsRepositoryLocation[] resolveBootRepositoriesBootSelectionArray(NutsSession session) {
        List<NutsRepositoryLocation> defaults = new ArrayList<>();
        DefaultNutsWorkspaceConfigManager rm = (DefaultNutsWorkspaceConfigManager) session.config();
        for (NutsAddRepositoryOptions d : rm.getDefaultRepositories()) {
            defaults.add(NutsRepositoryLocation.of(d.getName(), null));
        }
        return resolveBootRepositoriesList(session).resolve(defaults.toArray(new NutsRepositoryLocation[0]),
                NutsRepositoryDB.of(session)
        );
    }

    public NutsRepositorySelectorList resolveBootRepositoriesList(NutsSession session) {
        if (parsedBootRepositoriesList != null) {
            return parsedBootRepositoriesList;
        }
        CoreNutsBootOptions bOptions = NutsWorkspaceExt.of(ws).getModel().bootModel.getCoreBootOptions();
        parsedBootRepositoriesList = NutsRepositorySelectorList.ofAll(bOptions.getOptions().getRepositories(), NutsRepositoryDB.of(session), session);
        return parsedBootRepositoriesList;
    }

    public NutsWorkspaceConfigBoot getStoreModelBoot() {
        return storeModelBoot;
    }

    public NutsWorkspaceConfigApi getStoreModelApi() {
        return storeModelApi;
    }

    public NutsWorkspaceConfigRuntime getStoreModelRuntime() {
        return storeModelRuntime;
    }

    public NutsWorkspaceConfigSecurity getStoreModelSecurity() {
        return storeModelSecurity;
    }

    public ExecutorService executorService(NutsSession session) {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    executorService = session.boot().getBootOptions().getExecutorService();
                    if (executorService == null) {

                        int minPoolSize = getConfigProperty("nuts.threads.min", session).getInt(2);
                        if (minPoolSize < 1) {
                            minPoolSize = 60;
                        } else if (minPoolSize > 500) {
                            minPoolSize = 500;
                        }
                        int maxPoolSize = getConfigProperty("nuts.threads.max", session).getInt(60);
                        if (maxPoolSize < 1) {
                            maxPoolSize = 60;
                        } else if (maxPoolSize > 500) {
                            maxPoolSize = 500;
                        }
                        if (minPoolSize > maxPoolSize) {
                            minPoolSize = maxPoolSize;
                        }
                        TimePeriod defaultPeriod = new TimePeriod(3, TimeUnit.SECONDS);
                        TimePeriod period = TimePeriod.parseLenient(
                                getConfigProperty("nuts.threads.keep-alive", session).getString(),
                                TimeUnit.SECONDS, defaultPeriod,
                                defaultPeriod
                        );
                        if (period.getCount() < 0) {
                            period = defaultPeriod;
                        }
                        ThreadPoolExecutor executorService2 = (ThreadPoolExecutor) Executors.newCachedThreadPool(CoreNutsUtils.nutsDefaultThreadFactory);
                        executorService2.setCorePoolSize(minPoolSize);
                        executorService2.setKeepAliveTime(period.getCount(), period.getUnit());
                        executorService2.setMaximumPoolSize(maxPoolSize);
                        executorService = executorService2;
                    }
                }
            }
        }
        return executorService;
    }

    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    public void setTerminal(NutsSessionTerminal terminal, NutsSession session) {
        NutsSessionUtils.checkSession(ws, session);
        if (terminal == null) {
            terminal = createTerminal(session);
        }
        if (!(terminal instanceof UnmodifiableSessionTerminal)) {
            terminal = new UnmodifiableSessionTerminal(terminal, session);
        }
        this.terminal = terminal;
    }

    public NutsSessionTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err, NutsSession session) {
        NutsSessionTerminal t = createTerminal(session);
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

    public NutsSessionTerminal createTerminal(NutsSession session) {
        return new DefaultNutsSessionTerminalFromSystem(
                session, workspaceSystemTerminalAdapter
        );
//        return createTerminal(null, session);
    }

    public void addPathFactory(NutsPathFactory f) {
        if (f != null && !pathFactories.contains(f)) {
            pathFactories.add(f);
        }
    }

    public void removePathFactory(NutsPathFactory f) {
        pathFactories.remove(f);
    }

    public NutsPath resolve(String path, NutsSession session, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        //
        ClassLoader finalClassLoader = classLoader;
        NutsSupported<NutsPathSPI> z = Arrays.stream(getPathFactories())
                .map(x -> {
                    try {
                        return x.createPath(path, session, finalClassLoader);
                    } catch (Exception ex) {
                        //
                    }
                    return null;
                })
                .filter(x -> x != null && x.getSupportLevel() > 0)
                .max(Comparator.comparingInt(NutsSupported::getSupportLevel))
                .orElse(null);
        NutsPathSPI s = z == null ? null : z.getValue();
        if (s != null) {
            if (s instanceof NutsPath) {
                return (NutsPath) s;
            }
            return new NutsPathFromSPI(s);
        }
        return null;
    }

    public NutsPathFactory[] getPathFactories() {
        List<NutsPathFactory> all = new ArrayList<>(pathFactories.size() + 1);
        all.addAll(pathFactories);
        all.add(invalidPathFactory);
        return all.toArray(new NutsPathFactory[0]);
    }

    public DefaultNutsBootModel getBootModel() {
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

    public NutsPrimitiveElement getConfigProperty(String property, NutsSession session) {
        Map<String, String> env = getStoreModelMain().getEnv();
        if (env != null) {
            return NutsElements.of(session).ofString(env.get(property));
        }
        return NutsElements.of(session).ofNull();
    }

    public void setConfigProperty(String property, String value, NutsSession session) {
        Map<String, String> env = getStoreModelMain().getEnv();
//        session = CoreNutsUtils.validate(session, workspace);
        if (NutsBlankable.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                NutsWorkspaceConfigManagerExt.of(session.config())
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
                NutsWorkspaceConfigManagerExt.of(session.config())
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        }
    }

    private static class WorkspaceSystemTerminalAdapter extends AbstractSystemTerminalAdapter {

        private final NutsWorkspace workspace;

        public WorkspaceSystemTerminalAdapter(NutsWorkspace workspace) {
            this.workspace = workspace;
        }

        public NutsSystemTerminalBase getBase() {
            return NutsSessionUtils.defaultSession(workspace).config()
                    .getSystemTerminal();
        }
    }

    private class NutsBootDef {
        NutsId id;
        NutsDependency[] deps;
        NutsPath content;

        public NutsBootDef(NutsId id, NutsDependency[] deps, NutsPath content) {
            this.id = id;
            this.deps = deps;
            this.content = content;
        }
    }

    private class NutsWorkspaceStoredConfigImpl implements NutsWorkspaceStoredConfig {

        public NutsWorkspaceStoredConfigImpl() {
        }

        @Override
        public String getName() {
            return getStoredConfigBoot().getName();
        }

        @Override
        public NutsStoreLocationStrategy getStoreLocationStrategy() {
            return getStoredConfigBoot().getStoreLocationStrategy();
        }

        @Override
        public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
            return getStoredConfigBoot().getStoreLocationStrategy();
        }

        @Override
        public NutsOsFamily getStoreLocationLayout() {
            return getStoredConfigBoot().getStoreLocationLayout();
        }

        @Override
        public Map<NutsStoreLocation, String> getStoreLocations() {
            return getStoredConfigBoot().getStoreLocations();
        }

        @Override
        public Map<NutsHomeLocation, String> getHomeLocations() {
            return getStoredConfigBoot().getHomeLocations();
        }

        @Override
        public String getStoreLocation(NutsStoreLocation folderType) {
            return new NutsStoreLocationsMap(getStoredConfigBoot().getStoreLocations()).get(folderType);
        }

        @Override
        public String getHomeLocation(NutsHomeLocation homeLocation) {
            return new NutsHomeLocationsMap(getStoredConfigBoot().getHomeLocations()).get(homeLocation);
        }

        @Override
        public NutsId getApiId() {
            String v = getStoredConfigApi().getApiVersion();
            NutsSession ws = NutsSessionUtils.defaultSession(DefaultNutsWorkspaceConfigModel.this.ws);

            return v == null ? null
                    : NutsId.of(NutsConstants.Ids.NUTS_API + "#" + v, ws);
        }

        @Override
        public NutsId getRuntimeId() {
            String v = getStoredConfigApi().getRuntimeId();
            NutsSession ws = NutsSessionUtils.defaultSession(DefaultNutsWorkspaceConfigModel.this.ws);
            return v == null ? null : v.contains("#")
                    ? NutsId.of(v, ws)
                    : NutsId.of(NutsConstants.Ids.NUTS_RUNTIME + "#" + v, ws);
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
        public boolean isGlobal() {
            return getStoredConfigBoot().isGlobal();
        }

    }

    private class InvalidFilePathFactory implements NutsPathFactory {
        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsSessionUtils.checkSession(getWorkspace(), session);
            try {
                return NutsSupported.of(1, () -> new InvalidFilePath(path, session));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

}
