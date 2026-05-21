/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.boot.NBootDescriptor;
import net.thevpc.nuts.command.NCommandFactoryConfig;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.command.NInstallStatus;
import net.thevpc.nuts.concurrent.NScoredCallable;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryConfig;
import net.thevpc.nuts.core.NRepositoryRef;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder;
import net.thevpc.nuts.runtime.standalone.extension.NExtensionUtils;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.security.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
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
import net.thevpc.nuts.runtime.standalone.workspace.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.NVersionCompat;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNWorkspaceConfigModel {
    public static final Comparator<NRepositorySpec> REPOSITORY_ORDER_COMPARATOR = new Comparator<NRepositorySpec>() {

        public int compare(NRepositorySpec o1, NRepositorySpec o2) {
            return Integer.compare(o1.order(), o2.order());
        }
    };
    private static final Pattern PRELOAD_EXTENSION_PATH_PATTERN = Pattern.compile("^(?<protocol>[a-z][a-z0-9_-]*):.*");

    private final DefaultNWorkspace workspace;
    private final List<NRepositoryAccessConfig> configRepoUsers = new ArrayList<>();
    private final List<NNamedCredential> configNamedCredentials = new ArrayList<>();
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


    public DefaultNWorkspaceConfigModel(final DefaultNWorkspace workspace) {
        this.workspace = workspace;
        NBootOptions bOptions = NWorkspaceExt.of().getModel().bootModel.getBootEffectiveOptions();
        this.bootClassLoader = bOptions.classWorldLoader().orElseGet(() -> Thread.currentThread().getContextClassLoader());
        this.bootClassWorldURLs = NCollections.nonNullList(bOptions.classWorldURLs().orNull());
        this.workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter();
        this.bootModel = workspace.getModel().bootModel;
        addPathFactory(new FilePath.FilePathFactory());
        addPathFactory(new ClassLoaderPath.ClasspathFactory());
        addPathFactory(new URLPath.URLPathFactory());
        addPathFactory(new NResourcePath.NResourceFactory());
        addPathFactory(new HtmlfsPath.HtmlfsFactory());
        addPathFactory(new DotfilefsPath.DotfilefsFactory());
        addPathFactory(new GithubfsPath.GithubfsFactory());
        addPathFactory(new GenericFilePath.GenericPathFactory());
        this.invalidPathFactory = new InvalidFilePathFactory();
        //        this.excludedRepositoriesSet = this.options.getExcludedRepositories() == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(this.options.getExcludedRepositories()), " ,;"));
    }

    public void onNewComponent(Class componentType) {
        if (NPathFactorySPI.class.isAssignableFrom(componentType)) {
            DefaultNWorkspaceFactory aa = (DefaultNWorkspaceFactory) (workspace.getModel().extensionModel.getObjectFactory());
            addPathFactory(
                    aa.newInstance(componentType, NPathFactorySPI.class)
            );
        }
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
        return NWorkspaceExt.of().getModel().bootModel.getBootUserOptions().readOnly().orElse(false);
    }

    public boolean save(boolean force) {
        if (!force && !isConfigurationChanged()) {
            return false;
        }
        NWorkspaceUtils.of().checkReadOnly();
        boolean ok = false;
        NSecurityManager.of().checkAllowed(NConstants.Permissions.SAVE, "save");
        if (force || storeModelBootChanged) {

            storeModelBoot.configVersion(DefaultNWorkspace.VERSION_WS_CONFIG_BOOT);
            if (storeModelBoot.getExtensions() != null) {
                for (NWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
                    //inherited
                    extension.configVersion(null);
                }
            }
            workspace.store().saveWorkspaceConfigBoot(storeModelBoot);
            storeModelBootChanged = false;
            ok = true;
        }

        if (force || storeModelSecurityChanged) {
            storeModelSecurity.setUsers(configUsers.isEmpty() ? null : configUsers.values().toArray(new NUserConfig[0]));
            storeModelSecurity.setRepositories(configRepoUsers.isEmpty() ? null : configRepoUsers.toArray(new NRepositoryAccessConfig[0]));

            storeModelSecurity.configVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NUserConfig extension : storeModelSecurity.getUsers()) {
                    //inherited
                    extension.configVersion(null);
                }
            }
            workspace.store().saveConfigSecurity(storeModelSecurity);
            storeModelSecurityChanged = false;
            ok = true;
        }

        if (force || storeModelMainChanged) {
            List<NExecutionEngineLocation> plainSdks = new ArrayList<>();
            plainSdks.addAll(NExecutionEngines.of().findExecutionEngines().toList());
            storeModelMain.setPlatforms(plainSdks);
            storeModelMain.setRepositories(
                    workspace.repositories().stream().filter(x -> !x.config().isTemporary())
                            .map(x -> x.config().repositoryRef()).collect(Collectors.toList())
            );

            storeModelMain.configVersion(current().getApiVersion());
            if (storeModelMain.getCommandFactories() != null) {
                for (NCommandFactoryConfig item : storeModelMain.getCommandFactories()) {
                    //inherited
                    item.configVersion(null);
                }
            }
            if (storeModelMain.getRepositories() != null) {
                for (NRepositoryRef item : storeModelMain.getRepositories()) {
                    //inherited
                    item.configVersion(null);
                }
            }
            if (storeModelMain.getPlatforms() != null) {
                for (NExecutionEngineLocation item : storeModelMain.getPlatforms()) {
                    //inherited
                    item.configVersion(null);
                }
            }
            workspace.store().saveConfigMain(storeModelMain);
            storeModelMainChanged = false;
            ok = true;
        }

        if (force || storeModelApiChanged) {
            storeModelApi.configVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NUserConfig item : storeModelSecurity.getUsers()) {
                    //inherited
                    item.configVersion(null);
                }
            }
            workspace.store().saveConfigApi(storeModelApi);
            storeModelApiChanged = false;
            ok = true;
        }
        if (force || storeModelRuntimeChanged) {
            storeModelRuntime.configVersion(current().getApiVersion());
            workspace.store().saveConfigRuntime(storeModelRuntime);
            storeModelRuntimeChanged = false;
            ok = true;
        }
        NException error = null;
        for (NRepository repo : workspace.repositories()) {
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
            String shortName = pnid.shortName();
            String artifactId = pnid.artifactId();
            for (String excludedExtensionList : options.excludedExtensions().orElseGet(Collections::emptyList)) {
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
        return NExtensions.of().createAllSupported(NRepositoryFactoryComponent.class,
                new NRepositoryConfig().location(
                        NRepositoryLocation.of(repositoryType + "@")
                )).size() > 0;
    }

    public List<NRepositorySpec> getRuntimeRepositoryDefinitions() {
        List<NRepositorySpec> all = new ArrayList<>();
        for (NRepositorySpecRuntimeResolverComponent provider : NExtensions.of()
                .createAll(NRepositorySpecRuntimeResolverComponent.class)) {
            for (NRepositorySpec d : provider.runtimeRepositoryDefinitions()) {
                all.add(d);
            }
        }
        Collections.sort(all, REPOSITORY_ORDER_COMPARATOR);
        return all;
    }

    public List<NRepositorySpec> getTemplateRepositoryDefinitions() {
        List<NRepositorySpec> all = new ArrayList<>();
        for (NRepositorySpecTemplateResolverComponent provider : NExtensions.of()
                .createAll(NRepositorySpecTemplateResolverComponent.class)) {
            for (NRepositorySpec d : provider.templateRepositoryDefinitions()) {
                all.add(d.copy());
            }
        }
        Collections.sort(all, REPOSITORY_ORDER_COMPARATOR);
        return all;
    }

    public List<NRepositorySpec> getDefaultRepositoryDefinitions() {
        List<NRepositorySpec> all = new ArrayList<>();
        for (NRepositorySpecDefaultResolverComponent provider : NExtensions.of()
                .createAll(NRepositorySpecDefaultResolverComponent.class)) {
            for (NRepositorySpec d : provider.defaultRepositoryDefinitions()) {
                all.add(d);
            }
        }
        Collections.sort(all, REPOSITORY_ORDER_COMPARATOR);
        return all;
    }

    public Set<String> getAvailableArchetypes() {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NWorkspaceArchetypeComponent extension : NExtensions.of()
                .createAllSupported(NWorkspaceArchetypeComponent.class, null)) {
            set.add(extension.name());
        }
        return set;
    }

    public NPath resolveRepositoryPath(NPath repositoryLocation) {
        NPath root = this.getRepositoriesRoot();
        return repositoryLocation
                .toAbsolute(root != null ? root :
                        NPath.of(NStoreKey.ofConf())
                        .resolve(NConstants.Folders.REPOSITORIES))
                ;
    }

    public NIndexStoreFactory getIndexStoreClientFactory() {
        return indexStoreClientFactory;
    }

    public List<String> getBootRepositories() {
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

    public NDuration getCreateDuration() {
        if (startCreateTime == null || endCreateTime == null) {
            return NDuration.ofMillis(0);
        }
        return NDuration.between(startCreateTime, endCreateTime);
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
        NWorkspaceModel wsModel = workspace.getModel();
        NId iruntimeId = wsModel.bootModel.getBootEffectiveOptions().runtimeId().orNull();
        if (wsModel.bootModel.getBootEffectiveOptions().runtimeBootDescriptor().isPresent()) {
            //not present in shaded jar mode
            NBootDescriptor d = wsModel.bootModel.getBootEffectiveOptions().runtimeBootDescriptor().get();
            iruntimeId = NId.get(d.getId().toString()).get();
        }
        wsModel.configModel.prepareBootClassPathConf(NIdType.API, workspace.apiId(), null, iruntimeId, false, false);
        NBootDef nBootNutsApi = null;
        try {
            nBootNutsApi = wsModel.configModel.prepareBootClassPathJar(workspace.apiId(), null, iruntimeId, false);
        } catch (Exception ex) {
            _LOG().log(NMsg.ofC("unable to install boot id (api) %s", workspace.apiId())
                    .withLevel(Level.SEVERE)
            );
        }
        if (nBootNutsApi == null) {
            //no need to install runtime if api is not there
            return;
        }

        NBootDef nBootNutsRuntime = null;
        try {
            wsModel.configModel.prepareBootClassPathConf(NIdType.RUNTIME, iruntimeId, workspace.apiId(), null, false, true);
            nBootNutsRuntime = wsModel.configModel.prepareBootClassPathJar(iruntimeId, workspace.apiId(), null, true);
        } catch (Exception ex) {
            _LOG().log(NMsg.ofC("unable to install boot id (runtime) %s", iruntimeId).withLevel(Level.SEVERE));
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
                                workspace.apiId(),
                                null, false,
                                true
                        );
                        wsModel.configModel.prepareBootClassPathJar(
                                extension.getId(),
                                workspace.apiId(),
                                null,
                                true
                        );
                    } catch (Exception ex) {
                        _LOG().log(NMsg.ofC("unable to install boot id (extension) %s", extension.getId()).asError());
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
            DefaultNWorkspaceCurrentConfig cConfig = new DefaultNWorkspaceCurrentConfig().merge(_config);
            DefaultNBootModel bm = NWorkspaceExt.of().getModel().bootModel;
            NBootOptions effOptions = bm.getBootEffectiveOptions();
            NBootOptions userOptions = bm.getBootUserOptions();
            if (cConfig.getApiId() == null) {
                cConfig.setApiId(NId.getApi(effOptions.apiVersion().orNull()).get());
            }
            if (cConfig.getRuntimeId() == null) {
                cConfig.setRuntimeId(effOptions.runtimeId().orNull());
            }
            if (cConfig.getRuntimeBootDescriptor() == null) {
                cConfig.setRuntimeBootDescriptor(effOptions.runtimeBootDescriptor().map(x -> new DefaultNDescriptorBuilder().copyFrom(x).build()).orNull());
            }
            if (cConfig.getExtensionBootDescriptors() == null) {
                cConfig.setExtensionBootDescriptors(effOptions.extensionBootDescriptors().map(x ->
                                x.stream().map(y -> y == null ? null : new DefaultNDescriptorBuilder().copyFrom(y).build()).collect(Collectors.toList())
                        )
                        .orNull());
            }
            if (cConfig.getBootRepositories() == null) {
                cConfig.setBootRepositories(effOptions.bootRepositories().orNull());
            }
            cConfig.merge(getBootUserOptions().toWorkspaceOptions());

            setCurrentConfig(cConfig.build(NWorkspace.of().workspaceLocation()));

            NVersionCompat compat = NVersionCompat.of(Nuts.version());
            NId apiId = workspace.apiId();
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
                        if (NIn.ask().forBoolean(NMsg.ofC("import older config %s into %s", olderId, apiId))
                                .defaultValue(true)
                                .booleanValue()
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
                cConfig.setApiId(NId.getApi(Nuts.version()).get());
            }
            if (cConfig.getRuntimeId() == null) {
                cConfig.setRuntimeId(effOptions.runtimeId().orNull());
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

            if (userOptions.recover().orElse(false) || userOptions.reset().orElse(false)) {
                //always reload boot resolved versions!
                cConfig.setApiId(NId.getApi(effOptions.apiVersion().orNull()).get());
                cConfig.setRuntimeId(effOptions.runtimeId().orNull());
                cConfig.setRuntimeBootDescriptor(NBootHelper.toDescriptor(effOptions.runtimeBootDescriptor().orNull()));
                cConfig.setExtensionBootDescriptors(NBootHelper.toDescriptorList(effOptions.extensionBootDescriptors().orNull()));
                cConfig.setBootRepositories(effOptions.bootRepositories().orNull());
            }
            setCurrentConfig(cConfig
                    .build(NWorkspace.of().workspaceLocation())
            );
            if (aconfig == null) {
                aconfig = new NWorkspaceConfigApi();
            }
            if (aconfig.getApiVersion() == null) {
                aconfig.setApiVersion(cConfig.getApiId().version());
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
            if (NWorkspace.of().bootOptions().recover().orElse(false)) {
                onLoadWorkspaceError(ex);
            } else {
                throw ex;
            }
        }
        return false;
    }

    private List<NId> findOlderNutsApiIds() {
        NId apiId = workspace.apiId();
        NPath path = NPath.of(NStoreKey.ofConf(apiId))
                .parent();
        List<NId> olderIds = path.stream().filter(NPath::isDirectory)
                .withDescription(NDescribables.ofDesc("isDirectory"))
                .map(x -> NVersion.get(x.name()).get())
                .withDescription(NDescribables.ofDesc("toVersion"))
                .filter(x -> x.compareTo(apiId.version()) < 0)
                .withDescription(NDescribables.ofDesc("older"))
                .sorted(new NComparator<NVersion>() {
                    @Override
                    public int compare(NVersion o1, NVersion o2) {
                        return Comparator.<NVersion>reverseOrder().compare(o1, o2);
                    }

                    @Override
                    public NElement describe() {
                        return NElement.ofString("reverseOrder");
                    }
                }).map(x -> apiId.builder().version(x).build())
                .withDescription(NDescribables.ofDesc("toId"))
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
        if (apiId.version().equals(session.workspace().apiId().version())) {
            NExtensionListHelper h = new NExtensionListHelper(session.workspace().apiId(),
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
            NExtensionListHelper h2 = new NExtensionListHelper(session.workspace().apiId(), new ArrayList<>());
            if (h2.add(extensionId, deps)) {
                NWorkspaceExt.of().deployBoot(extensionId, true);
            }
        }
        NPath runtimeVersionSpecificLocation = NPath.of(NStoreKey.ofConf())
                .resolve(NConstants.Folders.ID).resolve(NWorkspace.of().getDefaultIdBasedir(extensionId));
        NPath afile = runtimeVersionSpecificLocation.resolve(NConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
        cc.configVersion(current().getApiVersion());
        NElementWriter.ofJson().write(cc, afile);
    }

    public void setExtraBootRuntimeId(NId apiId, NId runtimeId, List<NDependency> deps) {
        String newDeps = deps.stream().map(Object::toString).collect(Collectors.joining(";"));
        NSession session = getWorkspace().currentSession();
        if (apiId == null || apiId.version().equals(session.workspace().apiId().version())) {
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
        estoreModelApi.setApiVersion(apiId.version());
        estoreModelApi.setRuntimeId(runtimeId);
        estoreModelApi.configVersion(current().getApiVersion());
        NPath apiVersionSpecificLocation = NPath.of(NStoreKey.ofConf(apiId));
        NPath afile = apiVersionSpecificLocation.resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        NElementWriter.ofJson().write(estoreModelApi, afile);

        NWorkspaceConfigRuntime storeModelRuntime = new NWorkspaceConfigRuntime();
        storeModelRuntime.setId(runtimeId);
        storeModelRuntime.setDependencies(
                newDeps
        );

        NPath runtimeVersionSpecificLocation = NPath.of(NStoreKey.ofConf())
                .resolve(NConstants.Folders.ID).resolve(NWorkspace.of().getDefaultIdBasedir(runtimeId));
        afile = runtimeVersionSpecificLocation.resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        storeModelRuntime.configVersion(current().getApiVersion());
        NElementWriter.ofJson().write(storeModelRuntime, afile);

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

    public void setBootRepositories(List<String> value) {
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
//        if (_config == null) {
//            if (NConstants.Users.ADMIN.equals(userId) || NConstants.Users.ANONYMOUS.equals(userId)) {
//                _config = new NUserConfig(userId, null, null, null);
//                addOrUpdateUser(_config);
//            }
//        }
        return _config;
    }

    public NUserConfig[] getUsers() {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        return configUsers.values().toArray(new NUserConfig[0]);
    }

    public NRepositoryAccessConfig[] getRepositoryUsers() {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        return configRepoUsers.toArray(new NRepositoryAccessConfig[0]);
    }

    public NOptional<NRepositoryAccessConfig> getRepositoryUser(String repository, String user) {
        NRepository crepository = workspace.getRepositoryModel().getRepository(repository).orNull();
        if (crepository == null) {
            return NOptional.ofNamedEmpty(repository + "/" + user);
        }
        Optional<NRepositoryAccessConfig> o = configRepoUsers.stream().filter(
                x ->
                        Objects.equals(x.userName(), user)
                                && Objects.equals(x.repository(), crepository.uuid())
        ).findFirst().map(NRepositoryAccessConfig::copy);
        return NOptional.ofNamedOptional(o, repository + "/" + user);
    }

    public boolean removeRepositoryUser(String repository, String user) {
        NRepository crepository = workspace.getRepositoryModel().getRepository(repository).orNull();
        if (crepository == null) {
            return false;
        }
        if (configRepoUsers.removeIf(x -> Objects.equals(x.userName(), user)
                && Objects.equals(x.repository(), crepository.uuid()))) {
            fireConfigurationChanged("repository-user", ConfigEventType.SECURITY);
            return true;
        }
        return false;
    }

    public void addNamedCredentials(NNamedCredential credential) {
        NAssert.requireNamedNonNull(configUsers.get(credential.userName()), "user " + credential.userName());
        workspace.getModel().securityModel.requiredAdminOrUser(credential.userName());
        for (int i = 0; i < configNamedCredentials.size(); i++) {
            NNamedCredential x = configNamedCredentials.get(i);
            if (Objects.equals(x.name(), credential.name())
                    && Objects.equals(x.userName(), credential.userName())) {
                if (!credential.equals(x)) {
                    configNamedCredentials.set(i, credential);
                    fireConfigurationChanged("named-credential", ConfigEventType.SECURITY);
                }
                return;
            }
        }
        configNamedCredentials.add(credential);
        fireConfigurationChanged("named-credential", ConfigEventType.SECURITY);
    }

    public NOptional<NUserConfig> resolveAsValidUserConfig(String user) {
        if (NBlankable.isBlank(user)) {
            user = workspace.getModel().securityModel.getCurrentUsername();
        }
        return NOptional.ofNamed(configUsers.get(user), "user " + user);
    }

    public void removeNamedCredentials(String name, String user) {
        String finalUser = resolveAsValidUserConfig(user).get().userName();
        workspace.getModel().securityModel.requiredAdminOrUser(finalUser);
        if (configNamedCredentials.removeIf(x -> Objects.equals(x.name(), name)
                && Objects.equals(x.userName(), finalUser))) {
            fireConfigurationChanged("named-credential", ConfigEventType.SECURITY);
        }
    }

    public List<NNamedCredential> findAllNamedCredentials() {
        if (workspace.getModel().securityModel.isAdmin()) {
            return new ArrayList<>(configNamedCredentials);
        }
        return findNamedCredentials();
    }

    public NOptional<NNamedCredential> findNamedCredential(String name, String user) {
        if (user == null) {
            user = workspace.getModel().securityModel.getCurrentUsername();
        }
        workspace.getModel().securityModel.requiredAdminOrUser(user);
        String finalUser = user;
        return NOptional.ofNamedOptional(configNamedCredentials.stream().filter(x ->
                Objects.equals(x.userName(), finalUser)
                        && Objects.equals(x.name(), name)
        ).findFirst(), "named credential " + user + "/" + name);

    }

    public List<NNamedCredential> findNamedCredentials(String user) {
        if (user == null) {
            user = workspace.getModel().securityModel.getCurrentUsername();
        }
        workspace.getModel().securityModel.requiredAdminOrUser(user);
        String finalUser = user;
        return configNamedCredentials.stream().filter(x -> Objects.equals(x.userName(), finalUser)).collect(Collectors.toList());
    }

    public List<NNamedCredential> findNamedCredentials() {
        String u = workspace.getModel().securityModel.getCurrentUsername();
        return configNamedCredentials.stream().filter(x -> Objects.equals(x.userName(), u)).collect(Collectors.toList());
    }

    public void addRepositoryUser(NRepositoryAccessConfig config) {
        if (config != null) {
            NAssert.requireNamedNonNull(getUser(config.userName()), "user " + config.userName());
            NRepository repository = workspace.getRepositoryModel().getRepository(config.repository()).get();
            NRepositoryAccessConfig cconfig = config.copy();
            cconfig.repository(repository.uuid());
            if (configRepoUsers.stream().anyMatch(
                    x ->
                            x.userName().equals(cconfig.userName())
                                    && x.repository().equals(cconfig.repository())
            )) {
                return;
            }
            configRepoUsers.add(cconfig);
            fireConfigurationChanged("repository-user", ConfigEventType.SECURITY);
        }
    }

    public void addOrUpdateUser(NUserConfig config) {
        if (config != null) {
            configUsers.put(config.userName(), config);
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
        for (NWorkspaceListener workspaceListener : workspace.workspaceListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    //    
    public NWorkspaceConfigApi getStoredConfigApi() {
        if (storeModelApi.getApiVersion() == null || storeModelApi.getApiVersion().isBlank()) {
            storeModelApi.setApiVersion(Nuts.version());
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
            for (NDependencySolverFactory nutsDependencySolver : NExtensions.of().createAllSupported(NDependencySolverFactory.class, null)) {
                dependencySolvers.put(nutsDependencySolver.name(), nutsDependencySolver);
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
                .map(NDependencySolverFactory::name)
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (!o1.equals(o2)) {
                            String n = NDependencySolverUtils.resolveSolverName(NSession.of().dependencySolver());
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
        return NPath.of(NStoreKey.ofConf()).resolve(NConstants.Folders.REPOSITORIES);
    }

    public NPath getTempRepositoriesRoot() {
        return NPath.of(NStoreKey.ofTemp()).resolve(NConstants.Folders.REPOSITORIES);
    }

    public NAuthenticationAgent createAuthenticationAgent(String authenticationAgent) {
        authenticationAgent = NStringUtils.trim(authenticationAgent);
        NAuthenticationAgent supported = null;
        if (authenticationAgent.isEmpty()) {
            supported = NExtensions.of().createSupported(NAuthenticationAgent.class, "").get();
        } else {
            List<NAuthenticationAgent> agents = NExtensions.of().createAllSupported(NAuthenticationAgent.class, authenticationAgent);
            for (NAuthenticationAgent agent : agents) {
                if (agent.id().equals(authenticationAgent)) {
                    supported = agent;
                }
            }
        }
        if (supported == null) {
            return NOptional.<NAuthenticationAgent>ofNamedEmpty(NMsg.ofC("extensions component %s with agent=%s", NAuthenticationAgent.class, authenticationAgent)).get();
        }
        return supported;
    }

    public void setUsers(NUserConfig[] users) {
        for (NUserConfig u : getUsers()) {
            removeUser(u.userName());
        }
        for (NUserConfig conf : users) {
            addOrUpdateUser(conf);
        }
    }

    public NWorkspaceConfigRuntime getStoredConfigRuntime() {
        return storeModelRuntime;
    }

    public NId createSdkId(String type, String version) {
        return NWorkspaceUtils.of().createSdkId(type, version);
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
                configUsers.put(s.userName(), s);
            }
        }
        storeModelSecurityChanged = true;
        if (fire) {
            fireConfigurationChanged("config-security", ConfigEventType.SECURITY);
        }
    }

    private void setConfigMain(NWorkspaceConfigMain config, boolean fire) {
        this.storeModelMain = config == null ? new NWorkspaceConfigMain() : config;
        workspace.getModel().sdkModel.setExecutionEngines(this.storeModelMain.getPlatforms().toArray(new NExecutionEngineLocation[0]));
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
        s1 = workspace == null ? "?" : workspace.apiId().toString();
        s2 = workspace == null ? "?" : String.valueOf(workspace.runtimeId());
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((currentConfig == null) ? "NULL" : ("'"
                                                                        +
                                                                        (workspace == null ? "?" : "" + NWorkspaceExt.of(workspace).getLocationModel().getWorkspaceLocation()) + '\''))
                + '}';
    }

    public void collect(NClassLoaderNode n, LinkedHashMap<NId, NClassLoaderNode> deps) {
        if (!deps.containsKey(n.id())) {
            deps.put(n.id(), n);
            for (NClassLoaderNode d : n.dependencies()) {
                collect(d, deps);
            }
        }
    }

    public NBootDef fetchBootDef(NId id, boolean content) {
        NDefinition nd = NFetch.of(id)
                .dependencyFilter(NDependencyFilters.of().byRunnable())
                .failFast(false).getResultDefinition();
        if (nd != null) {
            if (content && nd.content().isNotPresent()) {
                //this is an unexpected behaviour, fail fast
                throw new NArtifactNotFoundException(id.longId());
            }
            return new NBootDef(nd.id(), nd.dependencies().get().transitive().toList(),
                    (content && nd.content().isPresent()) ? nd.content().get() : null);
        }
        if (isFirstBoot()) {
            NClassLoaderNode n = searchBootNode(id);
            if (n != null) {
                LinkedHashMap<NId, NClassLoaderNode> dm = new LinkedHashMap<>();
                for (NClassLoaderNode d : n.dependencies()) {
                    collect(d, dm);
                }
                return new NBootDef(
                        id,
                        dm.values().stream().map(x -> NDependency.get(x.id()).get()).collect(Collectors.toList()),
                        NPath.of(n.url())
                );
            }
            String contentPath = id.getMavenPath(null);
            NPath jarPath = null;
            NPath pomPath = null;
            for (NRepositorySpec nutsRepositoryLocation : resolveBootRepositoriesBootSelectionArray()) {
                NPath base = NPath.of(nutsRepositoryLocation.sourceLocation().path());
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
                        .descriptorStyle(NDescriptorStyle.MAVEN)
                        .parse(pomPath).get();
                //see only first level deps!
                return new NBootDef(
                        id,
                        d.dependencies(),
                        jarPath
                );
            }
        }
        throw new NArtifactNotFoundException(id.longId());
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
            NDefinition b = new DefaultNDefinitionBuilder()
                    .id(descriptor.id())
                    .dependency(descriptor.id().toDependency())
                    .dependency(descriptor.id().toDependency())
                    .descriptor(descriptor)
                    .content(NPath.of(tmp).userCache(true).userTemporary(true))
                    .installInformation(new DefaultNInstallInfo(descriptor.id(), NInstallStatus.NONE, null, null, null, null, null, null, false, false)
                    ).build();
            ins.install(b);
            return true;
        }
        return false;
    }

    private NClassLoaderNode searchBootNode(NId id) {
        List<NClassLoaderNode> all = new ArrayList<>();
        all.add(workspace.bootRuntimeClassLoaderNode());
        all.addAll(workspace.bootExtensionClassLoaderNodes());
        return searchBootNode(id, all);
    }

    private NClassLoaderNode searchBootNode(NId id, List<NClassLoaderNode> into) {
        for (NClassLoaderNode n : into) {
            if (n != null) {
                if (id.equalsLongId(n.id())) {
                    return n;
                }
                NClassLoaderNode a = searchBootNode(id, n.dependencies());
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
        d.build(NWorkspace.of().workspaceLocation());
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
        Path file = NWorkspace.of().workspaceLocation().toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        if (wconfig.isReadOnly()) {
            throw new NIOException(NMsg.ofC("unable to load config file %s", file), ex);
        }
        String fileSuffix = Instant.now().toString();
        fileSuffix = fileSuffix.replace(':', '-');
        String fileName = "nuts-workspace-" + fileSuffix;
        NPath logError = NPath.of(NStoreKey.ofLog(workspace.apiId())).resolve("invalid-config");
        NPath logFile = logError.resolve(fileName + ".error");
        _LOG()
                .log(NMsg.ofC("erroneous workspace config file. Unable to load file %s : %s", file, ex)
                        .withLevel(Level.SEVERE).withIntent(NMsgIntent.FAIL)
                );

        try {
            logFile.mkParentDirs();
        } catch (Exception ex1) {
            throw new NIOException(NMsg.ofC("unable to log workspace error while loading config file %s : %s", file, ex1), ex);
        }
        NPath newfile = logError.resolve(fileName + ".json");
        _LOG()
                .log(NMsg.ofC("erroneous workspace config file will be replaced by a fresh one. Old config is copied to %s\n error logged to  %s", newfile.toString(), logFile)
                        .withLevel(Level.SEVERE).withIntent(NMsgIntent.FAIL)
                );
        try {
            Files.move(file, newfile.toPath().get());
        } catch (IOException e) {
            throw new NIOException(NMsg.ofC("unable to load and re-create config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logFile.outputStream())) {
            o.println("workspace.path:");
            o.println(NWorkspace.of().workspaceLocation());
            o.println("workspace.options:");
            o.println(wconfig.getBootUserOptions()
                    .toCmdLine(
                            new NWorkspaceOptionsConfig()
                                    .compact(false)
                    )
            );
            for (NStoreType storeType : NStoreType.values()) {
                o.println("location." + storeType.id() + ":");
                o.println(NPath.of(NStoreKey.of(storeType)));
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


    public NRepositorySpec[] resolveBootRepositoriesBootSelectionArray() {
        List<NRepositorySpec> defaults = new ArrayList<>();
        for (NRepositorySpec d : workspace.defaultRepositories()) {
            defaults.add(new NRepositorySpec().sourceLocation(NRepositoryLocation.of(d.name(), null)));
        }
        return NRepositoryUtils.resolve(resolveBootRepositoriesList(), defaults.toArray(new NRepositorySpec[0]));
    }

    public NRepositorySelectorList resolveBootRepositoriesList() {
        if (parsedBootRepositoriesList != null) {
            return parsedBootRepositoriesList;
        }
        DefaultNBootModel bm = NWorkspaceExt.of().getModel().bootModel;
        parsedBootRepositoriesList = NRepositoryUtils.createRepositorySelectorList(
                bm.getBootUserOptions().repositories().orNull()).get();
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
                    executorService = NWorkspace.of().bootOptions().executorService().orNull();
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
            terminal = new UnmodifiableTerminal(terminal);
        }
        this.terminal = terminal;
    }

    public NTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err) {
        NTerminal t = createTerminal();
        if (in != null) {
            t.in(in);
        }
        if (out != null) {
            t.out(out);
        }
        if (err != null) {
            t.err(err);
        }
        return t;
    }

    public NTerminal createTerminal() {
        return new DefaultNTerminalFromSystem(
                workspaceSystemTerminalAdapter
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
        final String protocol;
        if (m.find()) {
            protocol = m.group("protocol");
            NExtensionUtils.ensureExtensionLoadedForProtocol(protocol);
        } else {
            protocol = null;
        }
        NScoredCallable<NPathSPI> z = NScorable.<NScoredCallable<NPathSPI>>query()
                .fromStream(
                        Arrays.stream(getPathFactories())
                                .map(x -> {
                                    NScoredCallable<NPathSPI> v = null;
                                    try {
                                        v = x.createPath(path, protocol, finalClassLoader);
                                    } catch (Exception ex) {
                                        //
                                    }
                                    return v;
                                })
                )
                .best().orNull();
        NPathSPI s = z == null ? null : z.call();
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

    public boolean isSecure() {
        return getStoredConfigSecurity().isSecure();
    }

    private static class WorkspaceSystemTerminalAdapter extends AbstractSystemTerminalAdapter {

        public WorkspaceSystemTerminalAdapter() {
            super();
        }

        public NSystemTerminalBase base() {
            return NIO.of()
                    .systemTerminal();
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
        public String name() {
            return getStoredConfigBoot().getName();
        }

        @Override
        public NStoreStrategy storeStrategy() {
            return getStoredConfigBoot().getStoreStrategy();
        }

        @Override
        public NStoreStrategy repositoryStoreStrategy() {
            return getStoredConfigBoot().getStoreStrategy();
        }

        @Override
        public NOsFamily storeLayout() {
            return getStoredConfigBoot().getStoreLayout();
        }

        @Override
        public Map<NStoreType, String> storeLocations() {
            return getStoredConfigBoot().getStoreLocations();
        }

        @Override
        public Map<NHomeLocation, String> homeLocations() {
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
        public NId apiId() {
            NVersion v = getStoredConfigApi().getApiVersion();
            return (v == null || v.isBlank()) ? null : NId.getApi(v).get();
        }

        @Override
        public NId runtimeId() {
            return getStoredConfigApi().getRuntimeId();
        }

        @Override
        public String runtimeDependencies() {
            return getStoredConfigRuntime().getDependencies();
        }

        @Override
        public List<String> bootRepositories() {
            return getStoredConfigBoot().getBootRepositories();
        }

        @Override
        public String javaCommand() {
            return getStoredConfigApi().getJavaCommand();
        }

        @Override
        public String javaOptions() {
            return getStoredConfigApi().getJavaOptions();
        }

        @Override
        public boolean isSystem() {
            return getStoredConfigBoot().isSystem();
        }

    }

    private static class InvalidFilePathFactory implements NPathFactorySPI {
        @Override
        public NScoredCallable<NPathSPI> createPath(String path, String protocol, ClassLoader classLoader) {
            try {
                return NScoredCallable.of(1, () -> new InvalidFilePath(path));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }

        @NScore
        public static int getScore(NScorableContext context) {
            return NScorable.DEFAULT_SCORE;
        }
    }

    public void invalidateStoreModelMain() {
        this.storeModelMainChanged = true;
    }
}
