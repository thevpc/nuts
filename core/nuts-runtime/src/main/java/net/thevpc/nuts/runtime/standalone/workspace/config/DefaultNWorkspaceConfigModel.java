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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.NBootDescriptor;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.util.NMaps;
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
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.io.terminal.UnmodifiableTerminal;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.NVersionCompat;
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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNWorkspaceConfigModel {
    private static Pattern PRELOAD_EXTENSION_PATH_PATTERN = Pattern.compile("^(?<protocol>[a-z][a-z0-9_-]*):.*");

    private final DefaultNWorkspace workspace;
    private final Map<String, NUserConfig> configUsers = new LinkedHashMap<>();
    private final NWorkspaceStoredConfig storedConfig = new NWorkspaceStoredConfigImpl();
    private final ClassLoader bootClassLoader;
    private final List<URL> bootClassWorldURLs;
    //    private final Function<String, String> pathExpansionConverter;
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
    private NTerminal terminal;
    private Map<String, NId> protocolToExtensionMap = new HashMap<>(
            NMaps.of(
                    "ssh", NId.get("net.thevpc.nuts.ext:next-ssh").get()
            )
    );
    //    private final NutsLogger LOG;

    public DefaultNWorkspaceConfigModel(final DefaultNWorkspace workspace) {
        this.workspace = workspace;
        NBootOptions bOptions = NWorkspaceExt.of().getModel().bootModel.getBootEffectiveOptions();
        this.bootClassLoader = bOptions.getClassWorldLoader().orElseGet(() -> Thread.currentThread().getContextClassLoader());
        this.bootClassWorldURLs = NCoreCollectionUtils.nonNullList(bOptions.getClassWorldURLs().orNull());
        workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter(workspace);

//        this.pathExpansionConverter = NWorkspaceVarExpansionFunction.of();
        this.bootModel = workspace.getModel().bootModel;
        addPathFactory(new FilePath.FilePathFactory(workspace));
        addPathFactory(new ClassLoaderPath.ClasspathFactory(workspace));
        addPathFactory(new URLPath.URLPathFactory(workspace));
        addPathFactory(new NResourcePath.NResourceFactory(workspace));
        addPathFactory(new HtmlfsPath.HtmlfsFactory(workspace));
        addPathFactory(new DotfilefsPath.DotfilefsFactory(workspace));
        addPathFactory(new GithubfsPath.GithubfsFactory(workspace));
        addPathFactory(new GenericFilePath.GenericPathFactory(workspace));
        invalidPathFactory = new InvalidFilePathFactory();
        //        this.excludedRepositoriesSet = this.options.getExcludedRepositories() == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(this.options.getExcludedRepositories()), " ,;"));
    }


    public void onNewComponent(Class componentType) {
        if (NPathFactorySPI.class.isAssignableFrom(componentType)) {
            DefaultNWorkspaceFactory aa = (DefaultNWorkspaceFactory) (((NWorkspaceExt) workspace).getModel().extensionModel.getObjectFactory());
            addPathFactory(
                    (NPathFactorySPI) aa.newInstance(componentType, NPathFactorySPI.class)
            );
        }
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNWorkspaceConfigModel.class);
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
        return NWorkspaceExt.of().getModel().bootModel.getBootUserOptions().getReadOnly().orElse(false);
    }

    public boolean save(boolean force) {
        if (!force && !isConfigurationChanged()) {
            return false;
        }
        NWorkspaceUtils.of(getWorkspace()).checkReadOnly();
        boolean ok = false;
        NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.SAVE, "save");
        if (force || storeModelBootChanged) {

            storeModelBoot.setConfigVersion(DefaultNWorkspace.VERSION_WS_CONFIG_BOOT);
            if (storeModelBoot.getExtensions() != null) {
                for (NWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            workspace.store().saveWorkspaceConfigBoot(storeModelBoot);
            storeModelBootChanged = false;
            ok = true;
        }

        if (force || storeModelSecurityChanged) {
            storeModelSecurity.setUsers(configUsers.isEmpty() ? null : configUsers.values().toArray(new NUserConfig[0]));

            storeModelSecurity.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NUserConfig extension : storeModelSecurity.getUsers()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            workspace.store().saveConfigSecurity(storeModelSecurity);
            storeModelSecurityChanged = false;
            ok = true;
        }

        if (force || storeModelMainChanged) {
            List<NPlatformLocation> plainSdks = new ArrayList<>();
            plainSdks.addAll(NWorkspace.of().findPlatforms().toList());
            storeModelMain.setPlatforms(plainSdks);
            storeModelMain.setRepositories(
                    workspace.getRepositories().stream().filter(x -> !x.config().isTemporary())
                            .map(x -> x.config().getRepositoryRef()).collect(Collectors.toList())
            );

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
            workspace.store().saveConfigMain(storeModelMain);
            storeModelMainChanged = false;
            ok = true;
        }

        if (force || storeModelApiChanged) {
            storeModelApi.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NUserConfig item : storeModelSecurity.getUsers()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            workspace.store().saveConfigApi(storeModelApi);
            storeModelApiChanged = false;
            ok = true;
        }
        if (force || storeModelRuntimeChanged) {
            storeModelRuntime.setConfigVersion(current().getApiVersion());
            workspace.store().saveConfigRuntime(storeModelRuntime);
            storeModelRuntimeChanged = false;
            ok = true;
        }
        NException error = null;
        for (NRepository repo : workspace.getRepositories()) {
            try {
                if (repo.config() instanceof NRepositoryConfigManagerExt) {
                    ok |= ((NRepositoryConfigManagerExt) (repo.config())).getModel().save(force);
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

    public boolean save() {
        return save(true);
    }


    public boolean isExcludedExtension(String extensionId, NWorkspaceOptions options) {
        if (extensionId != null && options != null) {
            NId pnid = NId.get(extensionId).get();
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

    public NBootOptions getBootUserOptions() {
        return NWorkspaceExt.of().getModel().bootModel.getBootUserOptions();
    }

    public boolean isSupportedRepositoryType(String repositoryType) {
        if (NBlankable.isBlank(repositoryType)) {
            repositoryType = NConstants.RepoTypes.NUTS;
        }
        return NExtensions.of().createComponents(NRepositoryFactoryComponent.class,
                new NRepositoryConfig().setLocation(
                        NRepositoryLocation.of(repositoryType + "@")
                )).size() > 0;
    }

    public List<NAddRepositoryOptions> getDefaultRepositories() {
        List<NAddRepositoryOptions> all = new ArrayList<>();
        for (NRepositoryFactoryComponent provider : NExtensions.of()
                .createAll(NRepositoryFactoryComponent.class)) {
            for (NAddRepositoryOptions d : provider.getDefaultRepositories()) {
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

    public Set<String> getAvailableArchetypes() {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NWorkspaceArchetypeComponent extension : NExtensions.of()
                .createComponents(NWorkspaceArchetypeComponent.class, null)) {
            set.add(extension.getName());
        }
        return set;
    }

    public NPath resolveRepositoryPath(NPath repositoryLocation) {
        NPath root = this.getRepositoriesRoot();
        return repositoryLocation
                .toAbsolute(root != null ? root :
                        NWorkspace.of().getStoreLocation(NStoreType.CONF)
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

    public void setConfigBoot(NWorkspaceConfigBoot config) {
        setConfigBoot(config, true);
    }

    public void setConfigApi(NWorkspaceConfigApi config) {
        setConfigApi(config, true);
    }

    public void setConfigRuntime(NWorkspaceConfigRuntime config) {
        setConfigRuntime(config, true);
    }

    public void setConfigSecurity(NWorkspaceConfigSecurity config) {
        setConfigSecurity(config, true);
    }

    public void setConfigMain(NWorkspaceConfigMain config) {
        setConfigMain(config, true);
    }

    public void setEndCreateTime(Instant endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    public void installBootIds() {
        NWorkspaceModel wsModel = NWorkspaceExt.of().getModel();
        NId iruntimeId = wsModel.bootModel.getBootEffectiveOptions().getRuntimeId().orNull();
        if (wsModel.bootModel.getBootEffectiveOptions().getRuntimeBootDescriptor().isPresent()) {
            //not present in shaded jar mode
            NBootDescriptor d = wsModel.bootModel.getBootEffectiveOptions().getRuntimeBootDescriptor().get();
            iruntimeId = NId.get(d.getId().toString()).get();
        }
        wsModel.configModel.prepareBootClassPathConf(NIdType.API, workspace.getApiId(), null, iruntimeId, false, false);
        NBootDef nBootNutsApi = null;
        try {
            nBootNutsApi = wsModel.configModel.prepareBootClassPathJar(workspace.getApiId(), null, iruntimeId, false);
        } catch (Exception ex) {
            _LOGOP().level(Level.SEVERE).log(NMsg.ofC("unable to install boot id (api) %s", workspace.getApiId()));
        }
        if (nBootNutsApi == null) {
            //no need to install runtime if api is not there
            return;
        }

        NBootDef nBootNutsRuntime = null;
        try {
            wsModel.configModel.prepareBootClassPathConf(NIdType.RUNTIME, iruntimeId, workspace.getApiId(), null, false, true);
            nBootNutsRuntime = wsModel.configModel.prepareBootClassPathJar(iruntimeId, workspace.getApiId(), null, true);
        } catch (Exception ex) {
            _LOG().with().level(Level.SEVERE).log(NMsg.ofC("unable to install boot id (runtime) %s", iruntimeId));
        }
        if (nBootNutsRuntime == null) {
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
                                workspace.getApiId(),
                                null, false,
                                true
                        );
                        wsModel.configModel.prepareBootClassPathJar(
                                extension.getId(),
                                workspace.getApiId(),
                                null,
                                true
                        );
                    } catch (Exception ex) {
                        _LOG().with().level(Level.SEVERE).log(NMsg.ofC("unable to install boot id (extension) %s", extension.getId()));
                    }
                }
            }
        }
    }

    public boolean isConfigurationChanged() {
        return storeModelBootChanged || storeModelApiChanged || storeModelRuntimeChanged || storeModelSecurityChanged || storeModelMainChanged;
    }

    public boolean loadWorkspace() {
        try {
            boolean _storeModelBootChanged = false;
            boolean _storeModelApiChanged = false;
            boolean _storeModelRuntimeChanged = false;
            boolean _storeModelSecurityChanged = false;
            boolean _storeModelMainChanged = false;
            NWorkspaceConfigBoot _config = parseBootConfig();
            if (_config == null) {
                return false;
            }
            DefaultNWorkspaceCurrentConfig cConfig = new DefaultNWorkspaceCurrentConfig(workspace).merge(_config);
            DefaultNBootModel bm = NWorkspaceExt.of().getModel().bootModel;
            NBootOptions effOptions = bm.getBootEffectiveOptions();
            NBootOptions userOptions = bm.getBootUserOptions();
            if (cConfig.getApiId() == null) {
                cConfig.setApiId(NId.getApi(effOptions.getApiVersion().orNull()).get());
            }
            if (cConfig.getRuntimeId() == null) {
                cConfig.setRuntimeId(effOptions.getRuntimeId().orNull());
            }
            if (cConfig.getRuntimeBootDescriptor() == null) {
                cConfig.setRuntimeBootDescriptor(effOptions.getRuntimeBootDescriptor().map(x -> new DefaultNDescriptorBuilder().setAll(x).build()).orNull());
            }
            if (cConfig.getExtensionBootDescriptors() == null) {
                cConfig.setExtensionBootDescriptors(effOptions.getExtensionBootDescriptors().map(x ->
                                x.stream().map(y -> y == null ? null : new DefaultNDescriptorBuilder().setAll(y).build()).collect(Collectors.toList())
                        )
                        .orNull());
            }
            if (cConfig.getBootRepositories() == null) {
                cConfig.setBootRepositories(effOptions.getBootRepositories().orNull());
            }
            cConfig.merge(getBootUserOptions().toWorkspaceOptions());

            setCurrentConfig(cConfig.build(NWorkspace.of().getWorkspaceLocation()));

            NVersionCompat compat = NVersionCompat.of(Nuts.getVersion(), workspace);
            NId apiId = workspace.getApiId();
            NWorkspaceConfigApi aconfig = compat.parseApiConfig(apiId);
            NId toImportOlderId = null;
            if (aconfig != null) {
                cConfig.merge(aconfig);
            } else {
                // will try to find older versions
                List<NId> olderIds = findOlderNutsApiIds();
                for (NId olderId : olderIds) {
                    aconfig = compat.parseApiConfig(olderId);
                    if (aconfig != null) {
                        // ask
                        if (NAsk.of().forBoolean(NMsg.ofC("import older config %s into %s", olderId, apiId))
                                .setDefaultValue(true)
                                .getBooleanValue()
                        ) {
                            toImportOlderId = olderId;
                            aconfig.setRuntimeId(null);
                            aconfig.setApiVersion(null);
                            cConfig.merge(aconfig);
                            _storeModelApiChanged = true;
                        }
                        break;
                    }
                }
            }
            if (cConfig.getApiId() == null) {
                cConfig.setApiId(NId.getApi(Nuts.getVersion()).get());
            }
            if (cConfig.getRuntimeId() == null) {
                cConfig.setRuntimeId(effOptions.getRuntimeId().orNull());
            }
            NWorkspaceConfigRuntime rconfig = compat.parseRuntimeConfig();
            if (rconfig != null) {
                cConfig.merge(rconfig);
            }
            NWorkspaceConfigSecurity sconfig = compat.parseSecurityConfig(apiId);
            if (sconfig == null) {
                if (toImportOlderId != null) {
                    sconfig = compat.parseSecurityConfig(toImportOlderId);
                }
            }
            NWorkspaceConfigMain mconfig = compat.parseMainConfig(apiId);
            if (mconfig == null) {
                if (toImportOlderId != null) {
                    mconfig = compat.parseMainConfig(toImportOlderId);
                }
            }

            if (userOptions.getRecover().orElse(false) || userOptions.getReset().orElse(false)) {
                //always reload boot resolved versions!
                cConfig.setApiId(NId.getApi(effOptions.getApiVersion().orNull()).get());
                cConfig.setRuntimeId(effOptions.getRuntimeId().orNull());
                cConfig.setRuntimeBootDescriptor(NBootHelper.toDescriptor(effOptions.getRuntimeBootDescriptor().orNull()));
                cConfig.setExtensionBootDescriptors(NBootHelper.toDescriptorList(effOptions.getExtensionBootDescriptors().orNull()));
                cConfig.setBootRepositories(effOptions.getBootRepositories().orNull());
            }
            setCurrentConfig(cConfig
                    .build(NWorkspace.of().getWorkspaceLocation())
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
            setConfigBoot(_config, false);
            setConfigApi(aconfig, false);
            setConfigRuntime(rconfig, false);
            setConfigSecurity(sconfig, false);
            setConfigMain(mconfig, false);
            storeModelBootChanged = _storeModelBootChanged;
            storeModelApiChanged = _storeModelApiChanged;
            storeModelRuntimeChanged = _storeModelRuntimeChanged;
            storeModelSecurityChanged = _storeModelSecurityChanged;
            storeModelMainChanged = _storeModelMainChanged;
            return true;
        } catch (RuntimeException ex) {
            if (NWorkspace.of().getBootOptions().getRecover().orElse(false)) {
                onLoadWorkspaceError(ex);
            } else {
                throw ex;
            }
        }
        return false;
    }

    private List<NId> findOlderNutsApiIds() {
        NId apiId = workspace.getApiId();
        NPath path = NWorkspace.of().getStoreLocation(apiId, NStoreType.CONF)
                .getParent();
        List<NId> olderIds = path.stream().filter(NPath::isDirectory)
                .withDesc(NEDesc.of("isDirectory"))
                .map(x -> NVersion.get(x.getName()).get())
                .withDesc(NEDesc.of("toVersion"))
                .filter(x -> x.compareTo(apiId.getVersion()) < 0)
                .withDesc(NEDesc.of("older"))
                .sorted(new NComparator<NVersion>() {
                    @Override
                    public int compare(NVersion o1, NVersion o2) {
                        return Comparator.<NVersion>reverseOrder().compare(o1, o2);
                    }

                    @Override
                    public NElement describe() {
                        return NElements.of().ofString("reverseOrder");
                    }
                }).map(x -> apiId.builder().setVersion(x).build())
                .withDesc(NEDesc.of("toId"))
                .toList();
        return olderIds;
    }

    public void setBootApiVersion(NVersion value) {
        if (!Objects.equals(value, storeModelApi.getApiVersion())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setApiVersion(value);
            fireConfigurationChanged("api-version", ConfigEventType.API);
        }
    }

    public void setExtraBootExtensionId(NId apiId, NId extensionId, List<NDependency> deps) {
        String newDeps = deps.stream().map(Object::toString).collect(Collectors.joining(";"));
        NWorkspaceConfigBoot.ExtensionConfig cc = new NWorkspaceConfigBoot.ExtensionConfig();
        cc.setId(apiId);
        cc.setDependencies(newDeps);
        cc.setEnabled(true);
        NSession session = getWorkspace().currentSession();
        if (apiId.getVersion().equals(session.getWorkspace().getApiId().getVersion())) {
            NExtensionListHelper h = new NExtensionListHelper(session.getWorkspace().getApiId(),
                    getStoredConfigBoot().getExtensions()).save();
            if (h.add(extensionId, deps)) {
                getStoredConfigBoot().setExtensions(h.getConfs());
                NWorkspaceExt.of().deployBoot(extensionId, true);
                fireConfigurationChanged("extensions", ConfigEventType.BOOT);
                DefaultNWorkspaceConfigModel configModel = NWorkspaceExt.of().getModel().configModel;
                configModel.save();
            }
        } else {
            //TODO, how to get old deps ?
            NExtensionListHelper h2 = new NExtensionListHelper(session.getWorkspace().getApiId(), new ArrayList<>());
            if (h2.add(extensionId, deps)) {
                NWorkspaceExt.of().deployBoot(extensionId, true);
            }
        }
        NPath runtimeVersionSpecificLocation = NWorkspace.of().getStoreLocation(NStoreType.CONF)
                .resolve(NConstants.Folders.ID).resolve(NWorkspace.of().getDefaultIdBasedir(extensionId));
        NPath afile = runtimeVersionSpecificLocation.resolve(NConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
        cc.setConfigVersion(current().getApiVersion());
        NElements.of().json().setValue(cc)
                .setNtf(false).print(afile);
    }

    public void setExtraBootRuntimeId(NId apiId, NId runtimeId, List<NDependency> deps) {
        String newDeps = deps.stream().map(Object::toString).collect(Collectors.joining(";"));
        NSession session = getWorkspace().currentSession();
        if (apiId == null || apiId.getVersion().equals(session.getWorkspace().getApiId().getVersion())) {
            if (!Objects.equals(runtimeId.toString(), storeModelApi.getRuntimeId())
                    || !Objects.equals(newDeps, storeModelRuntime.getDependencies())
            ) {
//            options = CoreNutsUtils.validate(options, ws);
                storeModelApi.setRuntimeId(runtimeId);
                storeModelRuntime.setDependencies(newDeps);
                setConfigRuntime(storeModelRuntime, true);
                fireConfigurationChanged("runtime-id", ConfigEventType.API);
            }
            setBootRuntimeId(runtimeId,
                    newDeps
            );
            save();
            return;
        }
        NWorkspaceConfigApi estoreModelApi = new NWorkspaceConfigApi();
        estoreModelApi.setApiVersion(apiId.getVersion());
        estoreModelApi.setRuntimeId(runtimeId);
        estoreModelApi.setConfigVersion(current().getApiVersion());
        NPath apiVersionSpecificLocation = NWorkspace.of().getStoreLocation(apiId, NStoreType.CONF);
        NPath afile = apiVersionSpecificLocation.resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        NElements elems = NElements.of();
        elems.json().setValue(estoreModelApi)
                .setNtf(false).print(afile);

        NWorkspaceConfigRuntime storeModelRuntime = new NWorkspaceConfigRuntime();
        storeModelRuntime.setId(runtimeId);
        storeModelRuntime.setDependencies(
                newDeps
        );

        NPath runtimeVersionSpecificLocation = NWorkspace.of().getStoreLocation(NStoreType.CONF)
                .resolve(NConstants.Folders.ID).resolve(NWorkspace.of().getDefaultIdBasedir(runtimeId));
        afile = runtimeVersionSpecificLocation.resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        storeModelRuntime.setConfigVersion(current().getApiVersion());
        elems.json().setValue(storeModelRuntime)
                .setNtf(false).print(afile);

    }

    public void setBootRuntimeId(NId value, String dependencies) {
        if (!Objects.equals(value, storeModelApi.getRuntimeId())
                || !Objects.equals(dependencies, storeModelRuntime.getDependencies())
        ) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setRuntimeId(value);
            storeModelRuntime.setDependencies(dependencies);
            setConfigRuntime(storeModelRuntime, true);
            fireConfigurationChanged("runtime-id", ConfigEventType.API);
        }
    }

    public void setBootRuntimeDependencies(String dependencies) {
        if (!Objects.equals(dependencies, storeModelRuntime.getDependencies())) {
//            options = CoreNutsUtils.validate(options, ws);
        }
    }

    public void setBootRepositories(String value) {
        if (!Objects.equals(value, storeModelBoot.getBootRepositories())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelBoot.setBootRepositories(value);
            fireConfigurationChanged("boot-repositories", ConfigEventType.API);
        }
    }

    public NWorkspaceConfigBoot.ExtensionConfig getBootExtension(String value) {
        NId newId = NId.get(value).get();
        for (NWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
            NId id = extension.getId();
            if (newId.equalsShortId(id)) {
                return extension;
            }
        }
        return null;
    }

    public void setBootExtension(String value, String dependencies, boolean enabled) {
        NId newId = NId.get(value).get();
        for (NWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
            NId id = extension.getId();
            if (newId.equalsShortId(id)) {
                extension.setId(newId);
                extension.setEnabled(enabled);
                extension.setDependencies(dependencies);
                fireConfigurationChanged("boot-extensions", ConfigEventType.API);
                return;
            }
        }
        storeModelBoot.getExtensions().add(new NWorkspaceConfigBoot.ExtensionConfig(newId, dependencies, true));
    }

    public NUserConfig getUser(String userId) {
        NUserConfig _config = getSecurity(userId);
        if (_config == null) {
            if (NConstants.Users.ADMIN.equals(userId) || NConstants.Users.ANONYMOUS.equals(userId)) {
                _config = new NUserConfig(userId, null, null, null);
                setUser(_config);
            }
        }
        return _config;
    }

    public NUserConfig[] getUsers() {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        return configUsers.values().toArray(new NUserConfig[0]);
    }

    public void setUser(NUserConfig config) {
        if (config != null) {
            configUsers.put(config.getUser(), config);
            fireConfigurationChanged("user", ConfigEventType.SECURITY);
        }
    }

    public void removeUser(String userId) {
        NUserConfig old = getSecurity(userId);
        if (old != null) {
            configUsers.remove(userId);
            fireConfigurationChanged("users", ConfigEventType.SECURITY);
        }
    }

    public void setSecure(boolean secure) {
        if (secure != storeModelSecurity.isSecure()) {
            storeModelSecurity.setSecure(secure);
            fireConfigurationChanged("secure", ConfigEventType.SECURITY);
        }
    }

    public void fireConfigurationChanged(String configName, ConfigEventType t) {
        NSession session = getWorkspace().currentSession();
        workspace.getImportModel().invalidateCache();
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
        for (NWorkspaceListener workspaceListener : workspace.getWorkspaceListeners()) {
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
        return workspace;
    }

    public NDependencySolver createDependencySolver(String name) {
        NDependencySolverFactory c = getSolversMap().get(NDependencySolverUtils.resolveSolverName(name));
        if (c != null) {
            return c.create();
        }
        throw new NIllegalArgumentException(NMsg.ofC("dependency solver not found %s", name));
    }

    private Map<String, NDependencySolverFactory> getSolversMap() {
        if (dependencySolvers == null) {
            dependencySolvers = new LinkedHashMap<>();
            for (NDependencySolverFactory nutsDependencySolver : NExtensions.of().createComponents(NDependencySolverFactory.class, null)) {
                dependencySolvers.put(nutsDependencySolver.getName(), nutsDependencySolver);
            }
        }
        return dependencySolvers;
    }

    public NDependencySolverFactory[] getDependencySolvers() {
        return getSolversMap().values().toArray(new NDependencySolverFactory[0]);
    }

    public List<String> getDependencySolverNames() {
        // the first element is always the default one,
        // the rest is lexicographically sorter
        return Arrays.stream(getDependencySolvers())
                .map(NDependencySolverFactory::getName)
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (!o1.equals(o2)) {
                            String n = NDependencySolverUtils.resolveSolverName(NSession.get().get().getDependencySolver());
                            if (o1.equals(n)) {
                                return -1;
                            }
                            if (o2.equals(n)) {
                                return 1;
                            }
                        }
                        return o1.compareTo(o2);
                    }
                })
                .collect(Collectors.toList());
    }

    public NPath getRepositoriesRoot() {
        return NWorkspace.of().getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.REPOSITORIES);
    }

    public NPath getTempRepositoriesRoot() {
        return NWorkspace.of().getStoreLocation(NStoreType.TEMP).resolve(NConstants.Folders.REPOSITORIES);
    }

    public NAuthenticationAgent createAuthenticationAgent(String authenticationAgent) {
        authenticationAgent = NStringUtils.trim(authenticationAgent);
        NAuthenticationAgent supported = null;
        NSession session = getWorkspace().currentSession();
        if (authenticationAgent.isEmpty()) {
            supported = NExtensions.of().createComponent(NAuthenticationAgent.class, "").get();
        } else {
            List<NAuthenticationAgent> agents = NExtensions.of().createComponents(NAuthenticationAgent.class, authenticationAgent);
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
    public void setUsers(NUserConfig[] users) {
        for (NUserConfig u : getUsers()) {
            removeUser(u.getUser());
        }
        for (NUserConfig conf : users) {
            setUser(conf);
        }
    }

    public NWorkspaceConfigRuntime getStoredConfigRuntime() {
        return storeModelRuntime;
    }

    public NId createSdkId(String type, String version) {
        return NWorkspaceUtils.of(getWorkspace()).createSdkId(type, version);
    }

    public void onExtensionsPrepared() {
        try {
            indexStoreClientFactory = NExtensions.of().createComponent(NIndexStoreFactory.class).orNull();
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNIndexStoreFactory();
        }
    }

    public void setConfigApi(NWorkspaceConfigApi config, boolean fire) {
        this.storeModelApi = config == null ? new NWorkspaceConfigApi() : config;
        if (fire) {
            fireConfigurationChanged("boot-api-config", ConfigEventType.API);
        }
    }

    public void setConfigRuntime(NWorkspaceConfigRuntime config, boolean fire) {
        this.storeModelRuntime = config == null ? new NWorkspaceConfigRuntime() : config;
        if (fire) {
            fireConfigurationChanged("boot-runtime-config", ConfigEventType.RUNTIME);
        }
    }

    private void setConfigSecurity(NWorkspaceConfigSecurity config, boolean fire) {
        this.storeModelSecurity = config == null ? new NWorkspaceConfigSecurity() : config;
        configUsers.clear();
        if (this.storeModelSecurity.getUsers() != null) {
            for (NUserConfig s : this.storeModelSecurity.getUsers()) {
                configUsers.put(s.getUser(), s);
            }
        }
        storeModelSecurityChanged = true;
        if (fire) {
            fireConfigurationChanged("config-security", ConfigEventType.SECURITY);
        }
    }

    private void setConfigMain(NWorkspaceConfigMain config, boolean fire) {
        this.storeModelMain = config == null ? new NWorkspaceConfigMain() : config;
        workspace.getSdkModel().setPlatforms(this.storeModelMain.getPlatforms().toArray(new NPlatformLocation[0]));
        workspace.removeAllRepositories();
        List<NRepositoryRef> refsToLoad = this.storeModelMain.getRepositories();
        if (refsToLoad != null) {
            refsToLoad = new ArrayList<>(refsToLoad);
            //reset config because add will add it again...
            this.storeModelMain.setRepositories(new ArrayList<>());
            for (NRepositoryRef ref : refsToLoad) {
                workspace.addRepository(
                        NRepositoryUtils.refToOptions(ref)
                );
            }
        }

        storeModelMainChanged = true;
        if (fire) {
            fireConfigurationChanged("config-main", ConfigEventType.MAIN);
        }
    }

    private void setConfigBoot(NWorkspaceConfigBoot config, boolean fire) {
        this.storeModelBoot = config;
        if (NBlankable.isBlank(config.getUuid())) {
            config.setUuid(UUID.randomUUID().toString());
            fire = true;
        }
        if (fire) {
            fireConfigurationChanged("config-master", ConfigEventType.BOOT);
        }
    }

    public String toString() {
        String s1 = "NULL";
        String s2 = "NULL";
        s1 = workspace == null ? "?" : workspace.getApiId().toString();
        s2 = workspace == null ? "?" : String.valueOf(workspace.getRuntimeId());
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((currentConfig == null) ? "NULL" : ("'"
                +
                (workspace == null ? "?" : "" + NWorkspaceExt.of(workspace).getLocationModel().getWorkspaceLocation()) + '\''))
                + '}';
    }

    public void collect(NClassLoaderNode n, LinkedHashMap<NId, NClassLoaderNode> deps) {
        if (!deps.containsKey(n.getId())) {
            deps.put(n.getId(), n);
            for (NClassLoaderNode d : n.getDependencies()) {
                collect(d, deps);
            }
        }
    }

    public NBootDef fetchBootDef(NId id, boolean content) {
        NDefinition nd = NFetchCmd.of(id)
                .setDependencies(true).setContent(content)
                .setDependencyFilter(NDependencyFilters.of().byRunnable())
                .setFailFast(false).getResultDefinition();
        if (nd != null) {
            if (content && nd.getContent().isNotPresent()) {
                //this is an unexpected behaviour, fail fast
                throw new NNotFoundException(id);
            }
            return new NBootDef(nd.getId(), nd.getDependencies().get().transitive().toList(),
                    (content && nd.getContent().isPresent()) ? nd.getContent().get() : null);
        }
        if (isFirstBoot()) {
            NClassLoaderNode n = searchBootNode(id);
            if (n != null) {
                LinkedHashMap<NId, NClassLoaderNode> dm = new LinkedHashMap<>();
                for (NClassLoaderNode d : n.getDependencies()) {
                    collect(d, dm);
                }
                return new NBootDef(
                        id,
                        dm.values().stream().map(x -> NDependency.get(x.getId()).get()).collect(Collectors.toList()),
                        NPath.of(n.getURL())
                );
            }
            String contentPath = ExtraApiUtils.resolveFilePath(id, null);
            NPath jarPath = null;
            NPath pomPath = null;
            for (NRepositoryLocation nutsRepositoryLocation : resolveBootRepositoriesBootSelectionArray()) {
                NPath base = NPath.of(nutsRepositoryLocation.getPath());
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
                NDescriptor d = NDescriptorParser.of()
                        .setDescriptorStyle(NDescriptorStyle.MAVEN)
                        .parse(pomPath).get();
                //see only first level deps!
                return new NBootDef(
                        id,
                        d.getDependencies(),
                        jarPath
                );
            }
        }
        throw new NNotFoundException(id);
    }

    public void prepareBootClassPathConf(NIdType idType, NId id, NId forId, NId forceRuntimeId, boolean force, boolean processDependencies) {
        //do not create boot file for nuts (it has no dependencies anyways!)
        switch (idType) {
            case API: {
                return;
            }
            case RUNTIME: {
                NBootDef d = fetchBootDef(id, false);
                for (NId apiId : CoreNUtils.resolveNutsApiIdsFromDependencyList(d.deps)) {
                    setExtraBootRuntimeId(apiId, d.id, d.deps);
                }
                break;
            }
            case EXTENSION: {
                NBootDef d = fetchBootDef(id, false);
                for (NId apiId : CoreNUtils.resolveNutsApiIdsFromDependencyList(d.deps)) {
                    setExtraBootExtensionId(apiId, d.id, d.deps);
                }
            }
        }
    }

    public NBootDef prepareBootClassPathJar(NId id, NId forId, NId forceRuntimeId, boolean processDependencies) {
        NBootDef d = fetchBootDef(id, true);
        if (deployToInstalledRepository(d.content.toPath().get())) {
            if (processDependencies) {
                for (NDependency dep : d.deps) {
                    prepareBootClassPathJar(dep.toId(), id, forceRuntimeId, true);
                }
            }
        }
        return d;
    }

    private boolean isFirstBoot() {
        return this.bootModel.isFirstBoot();
    }

    private boolean deployToInstalledRepository(Path tmp) {
//        NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of().repoSPI(repo);
//        NutsDeployRepositoryCommand desc = repoSPI.deploy()
//                .setId(id)
//                .setSession(session.copy().setConfirm(NutsConfirmationMode.YES))
//                .setContent(contentPath)
//                //.setFetchMode(NutsFetchMode.LOCAL)
//                .run();
//        repo.install(id, session, forId);

        NInstalledRepository ins = NWorkspaceExt.of().getInstalledRepository();
        NDescriptor descriptor = NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(tmp, null);
        if (descriptor != null) {
            DefaultNDefinition b = new DefaultNDefinition(
                    null, null,
                    descriptor.getId(),
                    descriptor, NPath.of(tmp).setUserCache(true).setUserTemporary(true),
                    new DefaultNInstallInfo(descriptor.getId(), NInstallStatus.NONE, null, null, null, null, null, null, false, false),
                    null, getWorkspace()
            );
            ins.install(b);
            return true;
        }
        return false;
    }

    private NClassLoaderNode searchBootNode(NId id) {
        List<NClassLoaderNode> all = new ArrayList<>();
        all.add(workspace.getBootRuntimeClassLoaderNode());
        all.addAll(workspace.getBootExtensionClassLoaderNode());
        return searchBootNode(id, all);
    }

    private NClassLoaderNode searchBootNode(NId id, List<NClassLoaderNode> into) {
        for (NClassLoaderNode n : into) {
            if (n != null) {
                if (id.equalsLongId(n.getId())) {
                    return n;
                }
                NClassLoaderNode a = searchBootNode(id, n.getDependencies());
                if (a != null) {
                    return a;
                }
            }
        }
        return null;
    }

    public void onPreUpdateConfig(String confName) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NStoreLocationsMap(currentConfig.getStoreLocations());
    }

    public void onPostUpdateConfig(String confName) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NStoreLocationsMap(currentConfig.getStoreLocations());
        DefaultNWorkspaceCurrentConfig d = currentConfig;
        d.setUserStoreLocations(new NStoreLocationsMap(storeModelBoot.getStoreLocations()).toMapOrNull());
        d.setHomeLocations(new NHomeLocationsMap(storeModelBoot.getHomeLocations()).toMapOrNull());
        d.build(NWorkspace.of().getWorkspaceLocation());
        NStoreLocationsMap newSL = new NStoreLocationsMap(currentConfig.getStoreLocations());
        for (NStoreType sl : NStoreType.values()) {
            String oldPath = preUpdateConfigStoreLocations.get(sl);
            String newPath = newSL.get(sl);
            if (!oldPath.equals(newPath)) {
                Path oldPathObj = Paths.get(oldPath);
                if (Files.exists(oldPathObj)) {
                    NIOUtils.copyFolder(oldPathObj, Paths.get(newPath));
                }
            }
        }
        fireConfigurationChanged(confName, ConfigEventType.API);
    }

    private void onLoadWorkspaceError(Throwable ex) {
        DefaultNWorkspaceConfigModel wconfig = this;
        Path file = NWorkspace.of().getWorkspaceLocation().toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        if (wconfig.isReadOnly()) {
            throw new NIOException(NMsg.ofC("unable to load config file %s", file), ex);
        }
        String fileSuffix = Instant.now().toString();
        fileSuffix = fileSuffix.replace(':', '-');
        String fileName = "nuts-workspace-" + fileSuffix;
        NPath logError = NWorkspace.of().getStoreLocation(workspace.getApiId(), NStoreType.LOG).resolve("invalid-config");
        NPath logFile = logError.resolve(fileName + ".error");
        _LOGOP().level(Level.SEVERE).verb(NLogVerb.FAIL)
                .log(NMsg.ofC("erroneous workspace config file. Unable to load file %s : %s", file, ex));

        try {
            logFile.mkParentDirs();
        } catch (Exception ex1) {
            throw new NIOException(NMsg.ofC("unable to log workspace error while loading config file %s : %s", file, ex1), ex);
        }
        NPath newfile = logError.resolve(fileName + ".json");
        _LOGOP().level(Level.SEVERE).verb(NLogVerb.FAIL)
                .log(NMsg.ofC("erroneous workspace config file will be replaced by a fresh one. Old config is copied to %s\n error logged to  %s", newfile.toString(), logFile));
        try {
            Files.move(file, newfile.toPath().get());
        } catch (IOException e) {
            throw new NIOException(NMsg.ofC("unable to load and re-create config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logFile.getOutputStream())) {
            o.println("workspace.path:");
            o.println(NWorkspace.of().getWorkspaceLocation());
            o.println("workspace.options:");
            o.println(wconfig.getBootUserOptions()
                    .toCmdLine(
                            new NWorkspaceOptionsConfig()
                                    .setCompact(false)
                    )
            );
            for (NStoreType location : NStoreType.values()) {
                o.println("location." + location.id() + ":");
                o.println(NWorkspace.of().getStoreLocation(location));
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

    private NWorkspaceConfigBoot parseBootConfig() {
        return NWorkspaceExt.of().store().loadWorkspaceConfigBoot();
    }


    public NRepositoryLocation[] resolveBootRepositoriesBootSelectionArray() {
        List<NRepositoryLocation> defaults = new ArrayList<>();
        for (NAddRepositoryOptions d : workspace.getDefaultRepositories()) {
            defaults.add(NRepositoryLocation.of(d.getName(), (String) null));
        }
        return resolveBootRepositoriesList().resolve(defaults.toArray(new NRepositoryLocation[0]),
                NRepositoryDB.of()
        );
    }

    public NRepositorySelectorList resolveBootRepositoriesList() {
        if (parsedBootRepositoriesList != null) {
            return parsedBootRepositoriesList;
        }
        DefaultNBootModel bm = NWorkspaceExt.of().getModel().bootModel;
        parsedBootRepositoriesList = NRepositorySelectorList.of(
                bm.getBootUserOptions().getRepositories().orNull(), NRepositoryDB.of()).get();
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

    public ExecutorService executorService() {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    executorService = NWorkspace.of().getBootOptions().getExecutorService().orNull();
                    if (executorService == null) {

                        int minPoolSize = getConfigProperty("nuts.threads.min").flatMap(NLiteral::asInt).orElse(2);
                        if (minPoolSize < 1) {
                            minPoolSize = 60;
                        } else if (minPoolSize > 500) {
                            minPoolSize = 500;
                        }
                        int maxPoolSize = getConfigProperty("nuts.threads.max").flatMap(NLiteral::asInt).orElse(60);
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
                                getConfigProperty("nuts.threads.keep-alive").flatMap(NLiteral::asString).orNull(),
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

    public NTerminal getTerminal() {
        return terminal;
    }

    public void setTerminal(NTerminal terminal) {
        if (terminal == null) {
            terminal = createTerminal();
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal, workspace);
        }
        this.terminal = terminal;
    }

    public NTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err) {
        NTerminal t = createTerminal();
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

    public NTerminal createTerminal() {
        return new DefaultNTerminalFromSystem(
                workspace, workspaceSystemTerminalAdapter
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

    public NPath resolve(String path, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        ClassLoader finalClassLoader = classLoader;
        Matcher m = PRELOAD_EXTENSION_PATH_PATTERN.matcher(path);
        if (m.find()) {
            String protocol = m.group("protocol");
            NId eid = protocolToExtensionMap.get(protocol);
            if (eid != null) {
                NExtensions.of().loadExtension(eid);
            }
        }
        NCallableSupport<NPathSPI> z = Arrays.stream(getPathFactories())
                .map(x -> {
                    NCallableSupport<NPathSPI> v = null;
                    try {
                        v = x.createPath(path, finalClassLoader);
                    } catch (Exception ex) {
                        //
                    }
                    return v;
                })
                .filter(x -> x != null && x.getSupportLevel() > 0)
                .max(Comparator.comparingInt(NCallableSupport::getSupportLevel))
                .orElse(null);
        NPathSPI s = z == null ? null : z.call();
        if (s != null) {
            if (s instanceof NPath) {
                return (NPath) s;
            }
            return new NPathFromSPI(workspace, s);
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

    public NOptional<NLiteral> getConfigProperty(String property) {
        Map<String, String> env = getStoreModelMain().getEnv();
        if (env != null) {
            String v = env.get(property);
            return NOptional.of(v == null ? null : NLiteral.of(v));
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("config property not found : %s", property));
    }

    public void setConfigProperty(String property, String value) {
        Map<String, String> env = getStoreModelMain().getEnv();
        if (NBlankable.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                workspace
                        .getConfigModel()
                        .fireConfigurationChanged("env", ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                getStoreModelMain().setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                workspace
                        .getConfigModel()
                        .fireConfigurationChanged("env", ConfigEventType.MAIN);
            }
        }
    }

    private static class WorkspaceSystemTerminalAdapter extends AbstractSystemTerminalAdapter {

        public WorkspaceSystemTerminalAdapter(NWorkspace workspace) {
            super(workspace);
        }

        public NSystemTerminalBase getBase() {
            return NIO.of()
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
            return (v == null || v.isBlank()) ? null : NId.getApi(v).get();
        }

        @Override
        public NId getRuntimeId() {
            return getStoredConfigApi().getRuntimeId();
        }

        @Override
        public String getRuntimeDependencies() {
            return getStoredConfigRuntime().getDependencies();
        }

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
        public NCallableSupport<NPathSPI> createPath(String path, ClassLoader classLoader) {
            try {
                return NCallableSupport.of(1, () -> new InvalidFilePath(path, workspace));
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

    public void invalidateStoreModelMain() {
        this.storeModelMainChanged = true;
    }
}
