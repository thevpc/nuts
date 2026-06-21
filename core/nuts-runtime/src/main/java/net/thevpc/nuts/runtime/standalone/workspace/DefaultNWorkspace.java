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
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NApplicationHandleMode;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.app.NAppImpl;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.security.NSecureString;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.security.NUserSpec;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.security.NUserConfig;
import net.thevpc.nuts.text.NDescriptorWriter;
import net.thevpc.nuts.text.NVersionWriter;
import net.thevpc.nuts.log.NLogFactorySPI;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.NWorkspaceProfilerImpl;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryModel;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NSettingsNdiSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.SystemNdi;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.text.NTableModel;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.internal.NScopedWorkspace;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.DefaultNProperties;
import net.thevpc.nuts.runtime.standalone.boot.NBootConfig;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.event.*;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.installer.CommandForIdNInstallerComponent;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNWorkspaceSecurityModel;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.DefaultNFilterModel;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.NRecommendationPhase;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.RequestQueryInfo;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/6/17.
 */
@NComponentScope(NScopeType.PROTOTYPE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNWorkspace extends AbstractNWorkspace implements NWorkspaceExt {

    public static final NVersion VERSION_INSTALL_INFO_CONFIG = NVersion.get("0.8.0").get();
    public static final NVersion VERSION_SDK_LOCATION = NVersion.get("0.8.0").get();
    public static final NVersion VERSION_REPOSITORY_CONFIG = NVersion.get("0.8.0").get();
    public static final String VERSION_REPOSITORY_REF = "0.8.0";
    public static final String VERSION_WS_CONFIG_API = "0.8.0";
    public static final NVersion VERSION_WS_CONFIG_BOOT = NVersion.get("0.8.7").get();
    public static final String VERSION_WS_CONFIG_MAIN = "0.8.0";
    public static final String VERSION_WS_CONFIG_RUNTIME = "0.8.0";
    public static final String VERSION_WS_CONFIG_SECURITY = "0.8.0";
    public static final String VERSION_COMMAND_ALIAS_CONFIG = "0.8.0";
    public static final String VERSION_COMMAND_ALIAS_CONFIG_FACTORY = "0.8.0";
    public static final String VERSION_USER_CONFIG = "0.8.0";
    public static final String RUNTIME_VERSION = "0.8.9.0";
    public static final String RUNTIME_VERSION_STRING = NConstants.Ids.NUTS_RUNTIME + "#" + RUNTIME_VERSION;
    public static final NId RUNTIME_ID = NId.get(RUNTIME_VERSION_STRING).get();
    public static final String WEAK_ADMIN_PASSWORD = "admin";
    //    public NLog LOG;
    private NWorkspaceModel wsModel;
    protected NBootOptionsInfo callerBootOptionsInfo;
    public Map<String, String> env;
    /**
     * using currentApp so that we can change NApp when calling embedded apps
     */
    public NApp currentApp;

    public DefaultNWorkspace(NBootOptionsInfo callerBootOptionsInfo, NBootOptions info) {
        this.callerBootOptionsInfo = callerBootOptionsInfo;
        initWorkspace(info);
    }

    public Map<String, String> getSysEnv() {
        return env;
    }

    @Override
    public NApp getApp() {
        return currentApp;
    }

    public NBootOptionsInfo getCallerBootOptionsInfo() {
        return callerBootOptionsInfo;
    }

    @Override
    public NWorkspaceStore store() {
        return wsModel.store;
    }

    //    /**
//     * creates a zip file based on the folder. The folder should contain a
//     * descriptor file at its root
//     *
//     * @return bundled nuts file, the nuts is neither deployed nor installed!
//     */
//    @Deprecated
//    public NutsDefinition createBundle(Path contentFolder, Path destFile, NutsQueryOptions queryOptions) {
//        session = CoreNutsUtils.validateSession(session, this);
//        if (Files.isDirectory(contentFolder)) {
//            NutsDescriptor descriptor = null;
//            Path ext = contentFolder.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
//            if (Files.exists(ext)) {
//                descriptor = parseList().descriptor(ext);
//                if (descriptor != null) {
//                    if ("zip".equals(descriptor.getPackaging())) {
//                        if (destFile == null) {
//                            destFile = io().path(io().expandPath(contentFolder.getParent().resolve(descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip")));
//                        }
//                        try {
//                            ZipUtils.zip(contentFolder.toString(), new ZipOptions(), destFile.toString());
//                        } catch (IOException ex) {
//                            throw new NutsIOException(session,ex);
//                        }
//                        return new DefaultNDefinition(
//                                this, null,
//                                descriptor.getId(),
//                                descriptor,
//                                new NutsContent(destFile,
//                                        true,
//                                        false),
//                                null
//                        );
//                    } else {
//                        throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
//                    }
//                }
//            }
//            throw new NutsIllegalArgumentException("Invalid Nut Folder source. unable to detect descriptor");
//        } else {
//            throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
//        }
//    }
//    @Override
//    public boolean isFetched(NutsId parseList) {
//        session = CoreNutsUtils.validateSession(session, this);
//        NSession offlineSession = session.copy();
//        try {
//            NutsDefinition found = fetch().parseList(parseList).offline().setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).getResultDefinition();
//            return found != null;
//        } catch (Exception e) {
//            return false;
//        }
//    }
    private static Set<NId> toIds(List<NDescriptor> all) {
        Set<NId> set = new LinkedHashSet<>();
        for (NDescriptor i : all) {
            set.add(i.id());
            set.addAll(i.dependencies().stream().map(NDependency::toId).collect(Collectors.toList()));
        }
        return set;
    }

    private static class InitWorkspaceData {
        NBootOptions initialBootOptions;
        NBootOptions effectiveBootOptions;
        List<String> bootRepositories;
        NTexts text;
        NElementFactory elems;
        boolean justInstalled;
        NWorkspaceArchetypeComponent justInstalledArchetype;
        NBootConfig cfg;
        NIO terminals;
    }

    private void initWorkspace(NBootOptions initialBootOptions0) {
        NAssert.requireNamedNonNull(initialBootOptions0, "boot options");
        InitWorkspaceData data = new InitWorkspaceData();
        data.initialBootOptions = initialBootOptions0.toReadOnly();
        try {
            this.wsModel = new NWorkspaceModel(this, data.initialBootOptions);
            this.runWith(() -> {
                currentApp = new NAppImpl();
                this.wsModel.init();
                _preloadWorkspace(data);
                if (!loadWorkspace(data.effectiveBootOptions.excludedExtensions().orElseGet(Collections::emptyList), null)) {
                    _createWorkspaceFirstBoot(data);
                } else {
                    _createWorkspaceNonFirstBoot(data);
                }
                _postCreateWorkspace(data);
            });

        } catch (RuntimeException ex) {
            if (wsModel != null && wsModel.recomm != null) {
                this.runWith(() -> {
                    new Thread(() -> {
                        try {
                            NId runtimeId = runtimeId();
                            String sRuntimeId = runtimeId == null ? NId.getRuntime("").get().toString() : runtimeId.toString();
                            this.runWith(() -> {
                                displayRecommendations(wsModel.recomm.getRecommendations(new RequestQueryInfo(sRuntimeId, ex), NRecommendationPhase.BOOTSTRAP, true));
                            });
                        } catch (Exception ex2) {
                            //just ignore
                        }
                    }).start();
                });
            }
            throw ex;
        } finally {
            if (wsModel != null && wsModel.bootModel != null) {
                wsModel.bootModel.setInitializing(false);
            }
        }
    }

    private void _preloadWorkspace(InitWorkspaceData data) {
        wsModel.LOG.debug(NMsg.ofC(NI18n.of("detected terminal flags %s"), this.wsModel.bootModel.getBootTerminal().getFlags()));
        data.effectiveBootOptions = this.wsModel.bootModel.getBootEffectiveOptions();
        this.wsModel.configModel = new DefaultNWorkspaceConfigModel(this);
        String workspaceLocation = data.effectiveBootOptions.workspace().orNull();
        data.bootRepositories = data.effectiveBootOptions.bootRepositories().orNull();
        NBootWorkspaceFactory bootFactory = data.effectiveBootOptions.bootWorkspaceFactory().orNull();
        this.wsModel.extensionModel = new DefaultNWorkspaceExtensionModel(this, bootFactory,
                data.effectiveBootOptions.excludedExtensions().orElse(Collections.emptyList()));
        this.wsModel.filtersModel = new DefaultNFilterModel(this);
        this.wsModel.installedRepository = new DefaultNInstalledRepository(data.effectiveBootOptions);
        this.wsModel.sdkModel = new DefaultNPlatformModel(this.wsModel);
        this.wsModel.location = data.effectiveBootOptions.workspace().orNull();
        this.wsModel.locationsModel = new DefaultNWorkspaceLocationModel(this,
                this.wsModel.location == null ? null : Paths.get(this.wsModel.location).toString());

        this.wsModel.extensionModel.onInitializeWorkspace(data.effectiveBootOptions, wsModel.bootClassLoader);
        this.wsModel.logModel.setFactorySPI(
                NExtensions.of().createSupported(NLogFactorySPI.class, null).orElse(this.wsModel.logModel.getFactorySPI())
        );
        this.wsModel.textModel.loadExtensions();
        data.cfg = new NBootConfig();
        data.cfg.setWorkspace(workspaceLocation);
        data.cfg.setApiVersion(this.wsModel.askedApiVersion);
        data.cfg.setRuntimeId(this.wsModel.askedRuntimeId);
        data.cfg.setRuntimeBootDescriptor(NBootHelper.toDescriptor(data.effectiveBootOptions.runtimeBootDescriptor().orNull()));
        data.cfg.setExtensionBootDescriptors(NBootHelper.toDescriptorList(data.effectiveBootOptions.extensionBootDescriptors().orNull()));


        this.wsModel.bootModel.onInitializeWorkspace();

        NSystemTerminalBase termb = wsModel.extensions
                .createComponent(NSystemTerminalBase.class).get();
        data.terminals = NIO.of();
        data.terminals
                .systemTerminal(termb)
                .defaultTerminal(NTerminal.ofSystem())
        ;
        wsModel.bootModel.bootSession().terminal(NTerminal.ofSystem());
        wsModel.logModel.getTermHandler().resumeTerminal();

        for (NPathFactorySPI nPathFactorySPI : wsModel.extensions.createServiceLoader(NPathFactorySPI.class, NWorkspace.class).loadAll(this)) {
            this.wsModel.configModel.addPathFactory(nPathFactorySPI);
        }

        data.text = NTexts.of();
        try {
            data.text.theme();
        } catch (Exception ex) {
            wsModel.LOG
                    .log(NMsg.ofJ("unable to load theme {0}. Reset to default!", data.effectiveBootOptions.theme())
                            .withLevel(Level.CONFIG).withIntent(NMsgIntent.FAIL)
                    );
            data.text.theme("");//set default!
        }

//        NutsFormatManager formats = this.formats().setSession(defaultSession());
        data.elems = NElementFactory.of();
        _initLog(data);
        wsModel.securityModel = new DefaultNWorkspaceSecurityModel(this);

        Instant now = Instant.now();
        if (data.effectiveBootOptions.creationTime().get().compareTo(now) > 0) {
            wsModel.configModel.setStartCreateTime(now);
        } else {
            wsModel.configModel.setStartCreateTime(data.effectiveBootOptions.creationTime().get());
        }

        boolean exists = wsModel.store.isValidWorkspaceFolder();
        NOpenMode openMode = data.effectiveBootOptions.openMode().orNull();
        if (openMode != null) {
            switch (openMode) {
                case OPEN_OR_ERROR: {
                    if (!exists) {
                        throw new NBootWorkspaceNotFoundException(workspaceLocation);
                    }
                    break;
                }
                case CREATE_OR_ERROR: {
                    if (exists) {
                        throw new NBootWorkspaceAlreadyExistsException(workspaceLocation);
                    }
                    break;
                }
            }
        }
        wsModel.configModel.onExtensionsPrepared();
    }

    private void _postCreateWorkspace(InitWorkspaceData data) {
        if (!isReadOnly()) {
            saveConfig(false);
        }
        wsModel.configModel.setEndCreateTime(Instant.now());
        NSession session = currentSession();
        if (data.justInstalled) {
            NLiteral enableRecommendations = wsModel.bootModel.getCustomBootOptions().get("---recommendations");
            if (enableRecommendations == null || enableRecommendations.asBoolean().orElse(true)) {
                this.runWith(() -> {
                    new Thread(() -> {
                        try {
                            Map rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(apiId().toString(), ""), NRecommendationPhase.BOOTSTRAP, false);
                            if (rec != null) {
                                if (rec.get("companions") instanceof List) {
                                    List<String> recommendedCompanions = (List<String>) rec.get("companions");
                                    if (recommendedCompanions != null) {
                                        wsModel.recommendedCompanions.addAll(recommendedCompanions);
                                    }
                                }
                            }
                        } catch (Exception ex2) {
                            //just ignore
                        }
                    }).start();
                });
            }
            data.justInstalledArchetype.startWorkspace();
            DefaultNWorkspaceEvent workspaceCreatedEvent = new DefaultNWorkspaceEvent(session, null, null, null, null);
            for (NWorkspaceListener workspaceListener : workspaceListeners()) {
                workspaceListener.onCreateWorkspace(workspaceCreatedEvent);
            }
        }
        if (data.effectiveBootOptions.userName().orElse("").trim().length() > 0) {
            char[] password = data.effectiveBootOptions.credential().orNull();
            try {
                if (password == null) {
                    password = new char[0];
                } else {
                    //make a copy because we are not the owner of boot credentials
                    password = Arrays.copyOf(password, password.length);
                }
                if (NBlankable.isBlank(new String(password))) {
                    password = data.terminals.defaultTerminal().readPassword(NMsg.ofPlain("Password : "));
                }
                try (NSecureString s = NSecureString.ofSecure(password)) {
                    NSecurityManager.of().login(data.effectiveBootOptions.userName().get(), s);
                }
            } finally {
                if (password != null) {
                    Arrays.fill(password, '\0');
                }
            }
        }
        wsModel.configModel.setEndCreateTime(Instant.now());
        wsModel.LOG
                .log(
                        NMsg.ofC("%s workspace loaded in %s",
                                NMsg.ofCode("nuts"),
                                creationDuration()
                        ).asFine().withIntent(NMsgIntent.SUCCESS)
                );
        if (data.effectiveBootOptions.sharedInstance().orElse(false)) {

            NWorkspace o = NScopedWorkspace.setSharedWorkspaceInstance(this);
            if (o != null) {
                wsModel.LOG
                        .log(
                                NMsg.ofC("%s workspace set as main instance overriding existing workspace",
                                        NMsg.ofCode("nuts")
                                ).withLevel(Level.WARNING).withIntent(NMsgIntent.SUCCESS)
                        );
            } else {
                wsModel.LOG
                        .log(
                                NMsg.ofC("%s workspace set as main instance",
                                        NMsg.ofCode("nuts")
                                ).asFine().withIntent(NMsgIntent.SUCCESS)
                        );
            }
        }

        // load default NRepositoryModel instances
        for (NRepositoryModel m : wsModel.extensionModel.createAll(NRepositoryModel.class)) {
            NWorkspace.of().addRepository(
                    new NRepositorySpec()
                            .sourceModel(m)
                            .temporary(true)

            );
        }

        if (CoreNUtils.isCustomFalse("---perf")) {
            if (session.isPlainOut()) {
                NOut.println(NMsg.ofC("%s workspace loaded in %s",
                        NMsg.ofCode("nuts"),
                        creationDuration()

                ));
            } else {
                session.eout().add(data.elems.ofObjectBuilder()
                        .set("workspace-loaded-in",
                                data.elems.ofObjectBuilder()
                                        .set("ms", this.creationDuration().toMillis())
                                        .set("text", this.creationDuration().normalize().toString())
                                        .build()

                        )
                        .build());
            }
        }
        NWorkspaceProfilerImpl.debug();
    }

    private void _createWorkspaceNonFirstBoot(InitWorkspaceData data) {
        NBootConfig cfg = data.cfg;
        NBootOptions effectiveBootOptions = data.effectiveBootOptions;
        wsModel.bootModel.setFirstBoot(false);
        wsModel.uuid = wsModel.configModel.getStoreModelBoot().getUuid();
        if (NBlankable.isBlank(wsModel.uuid)) {
            wsModel.uuid = UUID.randomUUID().toString();
            wsModel.configModel.getStoreModelBoot().setUuid(wsModel.uuid);
        }
        if (effectiveBootOptions.recover().orElse(false)) {
            wsModel.configModel.setBootApiVersion(cfg.getApiVersion());
            wsModel.configModel.setBootRuntimeId(cfg.getRuntimeId(),
                    effectiveBootOptions.runtimeBootDescriptor().isEmpty() ? "" :
                            NBootHelper.toDependencyList(effectiveBootOptions.runtimeBootDescriptor().get().getDependencies()).stream()
                            .map(NDependency::toString)
                            .collect(Collectors.joining(";"))
            );
            wsModel.configModel.setBootRepositories(cfg.getBootRepositories());
            try {
                NInstall.of().installed(true).getResultStream();
            } catch (Exception ex) {
                wsModel.LOG
                        .log(NMsg.ofJ("reinstall artifacts failed : {0}", ex).asError(ex));
            }
        }
        if (repositories().isEmpty()) {
            wsModel.LOG
                    .log(NMsg.ofPlain("workspace has no repositories. Will re-create defaults")
                            .withLevel(Level.CONFIG).withIntent(NMsgIntent.FAIL)
                    );
            data.justInstalledArchetype = initializeWorkspace(effectiveBootOptions.archetype().orNull());
        }
        List<String> transientRepositoriesSet =
                NCollections.nonNullList(effectiveBootOptions.repositories().orElseGet(Collections::emptyList));
        NRepositorySelectorList expected = NRepositoryUtils.createRepositorySelectorList(transientRepositoriesSet).get();
        for (NRepositorySpec d : NRepositoryUtils.resolve(expected,null)) {
            String n = d.name();
            String ruuid = (NBlankable.isBlank(n) ? "temporary" : n) + "_" + UUID.randomUUID().toString().replace("-", "");
            d.name(ruuid);
            d.temporary(true);
            d.enabled(true);
            d.failSafe(false);
            d.storeStrategy(NStoreStrategy.STANDALONE);
            addRepository(d);
        }
        loadRuntimeRepositories(expected);
    }

    public void loadRuntimeRepositories(NRepositorySelectorList expected) {
        if (expected == null) {
            List<String> transientRepositoriesSet =
                    NCollections.nonNullList(bootOptions().repositories().orElseGet(Collections::emptyList));
            expected = NRepositoryUtils.createRepositorySelectorList(transientRepositoriesSet).get();
        }
        for (NRepositorySpec liveRepository : getConfigModel().getRuntimeRepositoryDefinitions()) {
            NOptional<NRepository> n = findRepositoryByName(liveRepository.name());
            if (!n.isPresent()) {
                if(expected.acceptExisting(liveRepository)){
                    liveRepository.temporary(true);
                    liveRepository.storeStrategy(NStoreStrategy.STANDALONE);
                    addRepository(liveRepository);
                }
            }
        }
    }



    private void _createWorkspaceFirstBoot(InitWorkspaceData data) {
        NBootOptions effectiveBootOptions = data.effectiveBootOptions;
        wsModel.bootModel.setFirstBoot(true);
        if (wsModel.uuid == null) {
            wsModel.uuid = UUID.randomUUID().toString();
        }
        //workspace wasn't loaded. Create new configuration...
        data.justInstalled = true;
        NWorkspaceUtils.of().checkReadOnly();
        wsModel.LOG
                .log(NMsg.ofC("creating %s workspace at %s",
                                data.text.ofStyled("new", NTextStyle.info()),
                                this.workspaceLocation()
                        ).withLevel(Level.CONFIG).withIntent(NMsgIntent.SUCCESS)
                );
        NWorkspaceConfigBoot bconfig = new NWorkspaceConfigBoot();
        //load from config with resolution applied
        bconfig.setUuid(wsModel.uuid);
        NWorkspaceConfigApi aconfig = new NWorkspaceConfigApi();
        aconfig.setApiVersion(this.wsModel.askedApiVersion);
        aconfig.setRuntimeId(this.wsModel.askedRuntimeId);
        aconfig.setJavaCommand(effectiveBootOptions.javaCommand().orNull());
        aconfig.setJavaOptions(effectiveBootOptions.javaOptions().orNull());

        NWorkspaceConfigRuntime rconfig = new NWorkspaceConfigRuntime();
        rconfig.setDependencies(
                effectiveBootOptions.runtimeBootDescriptor().isEmpty() ? "" :
                        NBootHelper.toDependencyList(effectiveBootOptions.runtimeBootDescriptor().get().getDependencies()).stream()
                        .map(NDependency::toString)
                        .collect(Collectors.joining(";"))
        );
        rconfig.setId(this.wsModel.askedRuntimeId);

        bconfig.setBootRepositories(data.bootRepositories);
        bconfig.setStoreStrategy(effectiveBootOptions.storeStrategy().orNull());
        bconfig.setRepositoryStoreStrategy(effectiveBootOptions.repositoryStoreStrategy().orNull());
        bconfig.setStoreLayout(effectiveBootOptions.storeLayout().orNull());
        bconfig.setSystem(effectiveBootOptions.system().orElse(false));
        bconfig.setStoreLocations(new NStoreLocationsMap(effectiveBootOptions.storeLocations().orNull()).toMapOrNull());
        bconfig.setHomeLocations(new NHomeLocationsMap(effectiveBootOptions.homeLocations().orNull()).toMapOrNull());

        boolean namedWorkspace = CoreNUtils.isValidWorkspaceName(effectiveBootOptions.workspace().orNull());
        if (bconfig.getStoreStrategy() == null) {
            bconfig.setStoreStrategy(namedWorkspace ? NStoreStrategy.EXPLODED : NStoreStrategy.STANDALONE);
        }
        if (bconfig.getRepositoryStoreStrategy() == null) {
            bconfig.setRepositoryStoreStrategy(NStoreStrategy.EXPLODED);
        }
        bconfig.setName(CoreNUtils.resolveValidWorkspaceName(effectiveBootOptions.workspace().orNull()));

        wsModel.configModel.setCurrentConfig(new DefaultNWorkspaceCurrentConfig()
                .merge(aconfig)
                .merge(bconfig)
                .build(this.workspaceLocation()));
        wsModel.configModel.setConfigBoot(bconfig);
        wsModel.configModel.setConfigApi(aconfig);
        wsModel.configModel.setConfigRuntime(rconfig);
        //load all "---config.*" custom options into persistent config
        for (String customOption : effectiveBootOptions.customOptions().orElseGet(Collections::emptyList)) {
            NArg a = NArg.of(customOption);
            if (a.getKey().asString().get().startsWith("config.")) {
                if (a.isUncommented()) {
                    this.setConfigProperty(
                            a.getKey().asString().orElse("").substring("config.".length()),
                            a.getStringValue().orNull()
                    );
                }
            }
        }
        data.justInstalledArchetype = initializeWorkspace(effectiveBootOptions.archetype().orNull());
        NVersion nutsVersion = runtimeId().version();
        if (wsModel.LOG.isLoggable(Level.CONFIG)) {
            wsModel.LOG
                    .log(NMsg.ofJ("nuts workspace v{0} created.", nutsVersion)
                            .withLevel(Level.CONFIG).withIntent(NMsgIntent.SUCCESS)
                    );
        }
        //should install default
        NSession session = currentSession();
        if (session.isPlainTrace() && !this.bootOptions().skipWelcome().orElse(false)) {
            NPrintStream out = session.out();
            StringBuilder version = new StringBuilder(nutsVersion.toString());
            CoreStringUtils.fillString(' ', 25 - version.length(), version);
            NPath p = NPath.of("classpath:/net/thevpc/nuts/runtime/includes/standard-header.ntf", getClass().getClassLoader());
            NText n = data.text.parser().parse(p);
            n = data.text.transform(n, new NTextTransformConfig()
                    .currentDir(p.parent())
                    .importClassLoader(getClass().getClassLoader())
                    .processAll(true)
            );
            out.println(n == null ? "no help found" : n);
            NIsolationLevel il = wsModel.bootModel.getBootUserOptions().isolationLevel().orElse(NIsolationLevel.USER);
            if (il == NIsolationLevel.MEMORY) {
//                out.println(
//                        data.text.ofBuilder()
//                                .append("location", NTextStyle.underlined())
//                                .append(":")
//                                .append("<in-memory>")
//                                .append(" ")
//                );
            } else if (NWorkspaceUtils.isUserDefaultWorkspace()) {
                out.println(
                        data.text.ofBuilder()
                                .append("location", NTextStyle.underlined())
                                .append(":")
                                .append(this.workspaceLocation())
                                .append(" ")
                                .append(" (")
                                .append(digestName())
                                .append(")"));
            } else {
                out.println(
                        data.text.ofBuilder()
                                .append("location", NTextStyle.underlined())
                                .append(":")
                                .append(this.workspaceLocation())
                                .append(" ")
                                .append(" (")
                                .append(digestName())
                                .append(")")
                );
            }
            switch (il) {
                case USER: {
                    out.println(
                            NTextArt.of().tableRenderer()
                                    .get()
                                    .render(
                                            NTableModel.of()
                                                    .addCell(
                                                            data.text.ofBuilder()
                                                                    .append(" This is the first time ")
                                                                    .appendCode("sh", "nuts")
                                                                    .append(" is launched for this workspace ")
                                                    )
                                    )
                    );
                    break;
                }
                case SYSTEM: {
                    out.println(NTextArt.of().tableRenderer()
                            .get().render(
                                    NTableModel.of()
                                            .addCell(
                                                    data.text.ofBuilder()
                                                            .append(" This is the first time ")
                                                            .appendCode("sh", "nuts")
                                                            .append(" is launched as system for this workspace ")
                                            )
                            ));
                    break;
                }
                case CONFINED: {
                    out.println(NTextArt.of().tableRenderer()
                            .get().render(
                                    NTableModel.of()
                                            .addCell(
                                                    data.text.ofBuilder()
                                                            .append(" This is a confined workspace ")
                                            )
                            ));
                    break;
                }
                case SANDBOX: {
                    out.println(NTextArt.of().tableRenderer()
                            .get().render(
                                    NTableModel.of()
                                            .addCell(
                                                    data.text.ofBuilder()
                                                            .append(" This is a sandbox workspace ")
                                            )
                            ));
                    break;
                }
                case MEMORY: {
                    out.println(NTextArt.of().tableRenderer()
                            .get().render(
                                    NTableModel.of()
                                            .addCell(
                                                    data.text.ofBuilder()
                                                            .append(" This is an in-memory workspace ")
                                            )
                            ));
                    break;
                }
            }
            out.println();
        }
        if (wsModel.bootModel.getBootUserOptions().isolationLevel().orNull() != NIsolationLevel.MEMORY) {
            //wsModel.configModel.installBootIds();
        }
    }

    private void _initLog(InitWorkspaceData data) {
        NMsgBuilder mread = NMsgBuilder.of().withLevel(Level.CONFIG).withIntent(NMsgIntent.READ);
        NMsgBuilder mstart = NMsgBuilder.of().withLevel(Level.CONFIG).withIntent(NMsgIntent.START);
        if (wsModel.LOG.isLoggable(Level.CONFIG)) {
            NTexts text = data.text;
            NBootOptions effectiveBootOptions = data.effectiveBootOptions;
            NBootOptions userBootOptions = data.initialBootOptions;
            //just log known implementations
            NCmdLines.of();
            NIO.of();
            NVersionWriter.of();
            NIdWriter.of();

            wsModel.LOG.log(mstart.withMsgPlain(" ==============================================================================="));
            String s = NIOUtils.loadString(getClass().getResourceAsStream("/net/thevpc/nuts/runtime/includes/standard-header.ntf"), true);
            s = s.replace("${nuts.workspace-runtime.version}", Nuts.version().toString());
            for (String s1 : s.split("\n")) {
                wsModel.LOG.log(mstart.withMsgNtf(s1));
            }
            wsModel.LOG.log(mstart.withMsgPlain(" "));
            wsModel.LOG.log(mstart.withMsgPlain(" = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ="));
            wsModel.LOG.log(mstart.withMsgPlain(" "));
            wsModel.LOG.log(mstart.withMsgC("start ```sh nuts``` %s at %s", Nuts.version(), CoreNUtils.DEFAULT_DATE_TIME_FORMATTER.format(data.initialBootOptions.creationTime().get())));
            wsModel.LOG.log(mread.withMsgC("open Nuts Workspace               : %s",
                    effectiveBootOptions.toCmdLine()
            ));
            wsModel.LOG.log(mread.withMsgC("open Nuts Workspace (compact)     : %s",
                    effectiveBootOptions.toCmdLine(new NWorkspaceOptionsConfig().compact(true))));

            wsModel.LOG.log(mread.withMsgPlain("open Workspace with config        : "));
            wsModel.LOG.log(mread.withMsgC("   nuts-workspace-uuid            : %s", NTextUtils.desc(effectiveBootOptions.uuid().orNull(), text)));
            wsModel.LOG.log(mread.withMsgC("   nuts-workspace-name            : %s", NTextUtils.desc(effectiveBootOptions.name().orNull(), text)));
            wsModel.LOG.log(mread.withMsgC("   nuts-api-version               : %s", Nuts.version()));
            wsModel.LOG.log(mread.withMsgC("   nuts-api-url                   : %s", NPath.of(getApiURL())));
            wsModel.LOG.log(mread.withMsgC("   nuts-api-digest                : %s", text.ofStyled(getApiDigest(), NTextStyle.version())));
            wsModel.LOG.log(mread.withMsgC("   nuts-boot-repositories         : %s", NTextUtils.desc(effectiveBootOptions.bootRepositories().orNull(), text)));
            wsModel.LOG.log(mread.withMsgC("   nuts-runtime                   : %s", runtimeId()));
            wsModel.LOG.log(mread.withMsgC("   nuts-runtime-digest            : %s",
                    text.ofStyled(new CoreDigestHelper().append(effectiveBootOptions.classWorldURLs().orNull()).getDigest(), NTextStyle.version())
            ));
            if (effectiveBootOptions.runtimeBootDescriptor().isPresent()) {
                wsModel.LOG.log(mread.withMsgC("   nuts-runtime-dependencies      : %s",
                        text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                                effectiveBootOptions.runtimeBootDescriptor().get().getDependencies().stream()
                                        .map(x -> NId.get(x.toString()).get())
                                        .collect(Collectors.toList())
                        )
                ));
            }
            wsModel.LOG.log(mread.withMsgC("   nuts-runtime-urls              : %s",
                    text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                            effectiveBootOptions.classWorldURLs().get().stream()
                                    .map(x -> NPath.of(x.toString()))
                                    .collect(Collectors.toList())
                    )
            ));
            wsModel.LOG.log(mread.withMsgC("   nuts-extension-dependencies    : %s",
                    text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                            toIds(
                                    NBootHelper.toDescriptorList(effectiveBootOptions.extensionBootDescriptors().orElseGet(Collections::emptyList))
                            ).stream()
                                    .map(x
                                            -> NId.get(x.toString()).get()
                                    )
                                    .collect(Collectors.toList())
                    )
            ));
            wsModel.LOG.log(mread.withMsgC("   nuts-workspace                 : %s", NTextUtils.formatLogValue(text, userBootOptions.workspace().orNull(), effectiveBootOptions.workspace().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-hash-name                 : %s", digestName()));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-bin                 : %s", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.BIN).orNull(), effectiveBootOptions.getStoreType(NStoreType.BIN).orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-conf                : %s", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.CONF).orNull(), effectiveBootOptions.getStoreType(NStoreType.CONF).orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-var                 : %s", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.VAR).orNull(), effectiveBootOptions.getStoreType(NStoreType.VAR).orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-log                 : %s", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.LOG).orNull(), effectiveBootOptions.getStoreType(NStoreType.LOG).orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-temp                : %s", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.TEMP).orNull(), effectiveBootOptions.getStoreType(NStoreType.TEMP).orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-cache               : %s", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.CACHE).orNull(), effectiveBootOptions.getStoreType(NStoreType.CACHE).orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-run                 : %s", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.RUN).orNull(), effectiveBootOptions.getStoreType(NStoreType.RUN).orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-lib                 : %s", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.LIB).orNull(), effectiveBootOptions.getStoreType(NStoreType.LIB).orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-strategy            : %s", NTextUtils.formatLogValue(text, userBootOptions.storeStrategy().orNull(), effectiveBootOptions.storeStrategy().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-repos-store-strategy      : %s", NTextUtils.formatLogValue(text, userBootOptions.repositoryStoreStrategy().orNull(), effectiveBootOptions.repositoryStoreStrategy().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-store-layout              : %s", NTextUtils.formatLogValue(text, userBootOptions.storeLayout().orNull(), effectiveBootOptions.storeLayout().isNotPresent() ? "system" : effectiveBootOptions.storeLayout().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-username                  : %s", NTextUtils.formatLogValue(text, userBootOptions.userName().orNull(), effectiveBootOptions.userName().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-read-only                 : %s", NTextUtils.formatLogValue(text, userBootOptions.readOnly().orNull(), effectiveBootOptions.readOnly().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-trace                     : %s", NTextUtils.formatLogValue(text, userBootOptions.trace().orNull(), effectiveBootOptions.trace().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-progress                  : %s", NTextUtils.formatLogValue(text, userBootOptions.progressOptions().orNull(), effectiveBootOptions.progressOptions().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-bot                       : %s", NTextUtils.formatLogValue(text, userBootOptions.bot().orNull(), effectiveBootOptions.bot().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-cached                    : %s", NTextUtils.formatLogValue(text, userBootOptions.cached().orNull(), effectiveBootOptions.cached().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-transitive                : %s", NTextUtils.formatLogValue(text, userBootOptions.transitive().orNull(), effectiveBootOptions.transitive().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-confirm                   : %s", NTextUtils.formatLogValue(text, userBootOptions.confirm().orNull(), effectiveBootOptions.confirm().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-debug                     : %s", NTextUtils.formatLogValue(text, userBootOptions.debug().orNull(), effectiveBootOptions.debug().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-dry                       : %s", NTextUtils.formatLogValue(text, userBootOptions.dry().orNull(), effectiveBootOptions.dry().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-execution-type            : %s", NTextUtils.formatLogValue(text, userBootOptions.executionType().orNull(), effectiveBootOptions.executionType().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-out-line-prefix           : %s", NTextUtils.formatLogValue(text, userBootOptions.outLinePrefix().orNull(), effectiveBootOptions.outLinePrefix().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-err-line-prefix           : %s", NTextUtils.formatLogValue(text, userBootOptions.errLinePrefix().orNull(), effectiveBootOptions.errLinePrefix().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-init-platforms            : %s", NTextUtils.formatLogValue(text, userBootOptions.initPlatforms().orNull(), effectiveBootOptions.initPlatforms().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-init-java                 : %s", NTextUtils.formatLogValue(text, userBootOptions.initJava().orNull(), effectiveBootOptions.initJava().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-init-launchers            : %s", NTextUtils.formatLogValue(text, userBootOptions.initLaunchers().orNull(), effectiveBootOptions.initLaunchers().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-init-scripts              : %s", NTextUtils.formatLogValue(text, userBootOptions.initScripts().orNull(), effectiveBootOptions.initScripts().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-init-scripts              : %s", NTextUtils.formatLogValue(text, userBootOptions.initScripts().orNull(), effectiveBootOptions.initScripts().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-desktop-launcher          : %s", NTextUtils.formatLogValue(text, userBootOptions.desktopLauncher().orNull(), effectiveBootOptions.desktopLauncher().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-menu-launcher             : %s", NTextUtils.formatLogValue(text, userBootOptions.menuLauncher().orNull(), effectiveBootOptions.menuLauncher().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-user-launcher             : %s", NTextUtils.formatLogValue(text, userBootOptions.userLauncher().orNull(), effectiveBootOptions.userLauncher().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-isolation-level           : %s", NTextUtils.formatLogValue(text, userBootOptions.isolationLevel().orNull(), effectiveBootOptions.isolationLevel().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-open-mode                 : %s", NTextUtils.formatLogValue(text, userBootOptions.openMode().orNull(), effectiveBootOptions.openMode().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-inherited                 : %s", NTextUtils.formatLogValue(text, userBootOptions.inherited().orNull(), effectiveBootOptions.inherited().orNull())));
            wsModel.LOG.log(mread.withMsgC("   nuts-inherited-nuts-boot-args  : %s", System.getProperty("nuts.boot.args") == null ? NTextUtils.desc(null, text)
                    : NTextUtils.desc(NCmdLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH), text)
            ));
            wsModel.LOG.log(mread.withMsgC("   nuts-inherited-nuts-args       : %s", System.getProperty("nuts.args") == null ? NTextUtils.desc(null, text)
                    : NTextUtils.desc(text.of(NCmdLine.of(System.getProperty("nuts.args"), NShellFamily.SH)), text)
            ));
            wsModel.LOG.log(mread.withMsgC("   nuts-open-mode                 : %s", NTextUtils.formatLogValue(text, effectiveBootOptions.openMode().orNull(), effectiveBootOptions.openMode().orElse(NOpenMode.OPEN_OR_CREATE))));
            NEnv senvs = NEnv.of();
            wsModel.LOG.log(mread.withMsgC("   java-home                      : %s", System.getProperty("java.home")));
            wsModel.LOG.log(mread.withMsgC("   java-classpath                 : %s", System.getProperty("java.class.path")));
            wsModel.LOG.log(mread.withMsgC("   java-library-path              : %s", System.getProperty("java.library.path")));
            wsModel.LOG.log(mread.withMsgC("   os-name                        : %s", System.getProperty("os.name")));
            wsModel.LOG.log(mread.withMsgC("   os-family                      : %s", senvs.osFamily()));
            wsModel.LOG.log(mread.withMsgC("   os-dist                        : %s", senvs.osDist().artifactId()));
            wsModel.LOG.log(mread.withMsgC("   os-arch                        : %s", System.getProperty("os.arch")));
            wsModel.LOG.log(mread.withMsgC("   os-shell                       : %s", senvs.shellFamily()));
            wsModel.LOG.log(mread.withMsgC("   os-shells                      : %s", text.ofBuilder().appendJoined(",", senvs.shellFamilies())));
            NWorkspaceTerminalOptions b = getModel().bootModel.getBootTerminal();
            wsModel.LOG.log(mread.withMsgC("   os-terminal-flags              : %s", String.join(", ", b.getFlags())));
            NTerminalMode terminalMode = wsModel.bootModel.getBootUserOptions().terminalMode().orElse(NTerminalMode.DEFAULT);
            wsModel.LOG.log(mread.withMsgC("   os-terminal-mode               : %s", terminalMode));
            wsModel.LOG.log(mread.withMsgC("   os-desktop                     : %s", senvs.desktopEnvironment()));
            wsModel.LOG.log(mread.withMsgC("   os-desktop-family              : %s", senvs.desktopEnvironmentFamily()));
            wsModel.LOG.log(mread.withMsgC("   os-desktops                    : %s", text.ofBuilder().appendJoined(",", (senvs.desktopEnvironments()))));
            wsModel.LOG.log(mread.withMsgC("   os-desktop-families            : %s", text.ofBuilder().appendJoined(",", (senvs.desktopEnvironmentFamilies()))));
            wsModel.LOG.log(mread.withMsgC("   os-desktop-path                : %s", senvs.desktopPath()));
            wsModel.LOG.log(mread.withMsgC("   os-desktop-integration         : %s", senvs.getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP)));
            wsModel.LOG.log(mread.withMsgC("   os-menu-integration            : %s", senvs.getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU)));
            wsModel.LOG.log(mread.withMsgC("   os-shortcut-integration        : %s", senvs.getDesktopIntegrationSupport(NDesktopIntegrationItem.USER)));
            wsModel.LOG.log(mread.withMsgC("   os-version                     : %s", senvs.osDist().version()));
            wsModel.LOG.log(mread.withMsgC("   os-username                    : %s", System.getProperty("user.name")));
            wsModel.LOG.log(mread.withMsgC("   os-user-dir                    : %s", NPath.of(System.getProperty("user.dir"))));
            wsModel.LOG.log(mread.withMsgC("   os-user-home                   : %s", NPath.of(System.getProperty("user.home"))));
            wsModel.LOG.log(mread.withMsgC("   os-user-locale                 : %s", Locale.getDefault()));
            wsModel.LOG.log(mread.withMsgC("   os-user-time-zone              : %s", TimeZone.getDefault()));
        }

    }

    private void displayRecommendations(Object r) {
        Map<String, Object> a = new HashMap<>();
        a.put("recommendations", r);
        NOut.println(a);
    }

    private URL getApiURL() {
        NId nid = NId.getApi(Nuts.version()).get();
        return ExtraApiUtils.findClassLoaderJar(nid, NClassLoaderUtils.resolveClasspathURLs(Thread.currentThread().getContextClassLoader()));
    }

    private String getApiDigest() {
        if (NBlankable.isBlank(wsModel.apiDigest)) {
            wsModel.apiDigest = new CoreDigestHelper().append(getApiURL()).getDigest();
        }
        return wsModel.apiDigest;
    }

    protected NDescriptor _applyParentDescriptors(NDescriptor descriptor) {
        List<NId> parents = descriptor.parents();
        List<NDescriptor> parentDescriptors = new ArrayList<>();
        for (NId parent : parents) {
            parentDescriptors.add(
                    _applyParentDescriptors(
                            NFetch.of(parent)
                                    .dependencyFilter(NDependencyFilters.of().byRunnable())
                                    .getResultDescriptor()
                    )
            );
        }
        if (parentDescriptors.size() > 0) {
            NDescriptorBuilder descrWithParents = descriptor.builder();
            NDescriptorUtils.applyParents(descrWithParents, parentDescriptors);
            return descrWithParents.build();
        }
        return descriptor;
    }

    protected NDescriptor _resolveEffectiveDescriptor(NDescriptor descriptor, NDescriptorEffectiveConfig effectiveNDescriptorConfig) {
        wsModel.LOG
                .log(NMsg.ofC("resolve effective %s using %s", descriptor.id(), effectiveNDescriptorConfig)
                        .withLevel(Level.FINEST).withIntent(NMsgIntent.START)
                );
        NDescriptorBuilder descrWithParents = _applyParentDescriptors(descriptor).builder();
        //now apply conditions!
        List<NDescriptorProperty> properties = descrWithParents.properties().stream()
                .filter(x -> effectiveNDescriptorConfig.isIgnoreCurrentEnvironment() || CoreFilterUtils.acceptCondition(x.condition(), false)
                ).collect(Collectors.toList());
        if (!properties.isEmpty()) {
            DefaultNProperties pp = new DefaultNProperties();
            if (!effectiveNDescriptorConfig.isIgnoreCurrentEnvironment()) {
                List<NDescriptorProperty> n = new ArrayList<>();
                pp.addAll(properties);
                for (String s : pp.keySet()) {
                    NDescriptorProperty[] a = pp.getAll(s);
                    if (a.length == 1) {
                        n.add(a[0].builder().condition((NEnvCondition) null).build());
                    } else {
                        NDescriptorProperty z = null;
                        for (NDescriptorProperty zz : a) {
                            if (z == null) {
                                z = zz;
                                boolean wasZZ = NBlankable.isBlank(zz);
                                if (!wasZZ) {
                                    break; //match first condition!
                                }
                            } else {
                                boolean wasZ = NBlankable.isBlank(z);
                                boolean wasZZ = NBlankable.isBlank(zz);
                                if (wasZ == wasZZ || !wasZ) {
                                    z = zz;
                                }
                                if (!wasZZ) {
                                    break; //match first condition!
                                }
                            }
                        }
                        if (z != null) {
                            n.add(z.builder().condition((NEnvCondition) null).build());
                        }
                    }
                }
                properties = n;
            }
        }

        descrWithParents.setProperties(properties);

        NDescriptor effectiveDescriptor = NDescriptorUtils.applyProperties(descrWithParents).build();
        List<NDependency> oldDependencies = new ArrayList<>();
        if (!effectiveNDescriptorConfig.isIgnoreCurrentEnvironment()) {
            for (NDependency d : effectiveDescriptor.dependencies()) {
                if (CoreFilterUtils.acceptDependency(d)) {
                    oldDependencies.add(d.builder().condition((NEnvCondition) null).build());
                }
            }
        } else {
            oldDependencies.addAll(effectiveDescriptor.dependencies());
        }

        List<NDependency> newDeps = new ArrayList<>();
        boolean someChange = false;
        LinkedHashSet<NDependency> effStandardDeps = new LinkedHashSet<>();
        for (NDependency standardDependency : effectiveDescriptor.standardDependencies()) {
            if ("import".equals(standardDependency.scope())) {
                NDescriptor dd = NFetch.of(standardDependency.toId())
                        .dependencyFilter(NDependencyFilters.of().byRunnable())
                        .getResultEffectiveDescriptor();
                for (NDependency dependency : dd.standardDependencies()) {
                    if (effectiveNDescriptorConfig.isIgnoreCurrentEnvironment() || CoreFilterUtils.acceptDependency(dependency)) {
                        effStandardDeps.add(dependency);
                    }
                }
            } else {
                if (effectiveNDescriptorConfig.isIgnoreCurrentEnvironment() || CoreFilterUtils.acceptDependency(standardDependency)) {
                    effStandardDeps.add(standardDependency);
                }
            }
        }
        for (NDependency d : oldDependencies) {
            if (NBlankable.isBlank(d.scope())
                    || d.version().isBlank()
                    || NBlankable.isBlank(d.optional())) {
                NDependency standardDependencyOk = null;
                for (NDependency standardDependency : effStandardDeps) {
                    if (standardDependency.shortName().equals(d.toId().shortName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (NBlankable.isBlank(d.scope())
                            && !NBlankable.isBlank(standardDependencyOk.scope())) {
                        someChange = true;
                        d = d.builder().scope(standardDependencyOk.scope()).build();
                    }
                    if (NBlankable.isBlank(d.optional())
                            && !NBlankable.isBlank(standardDependencyOk.optional())) {
                        someChange = true;
                        d = d.builder().optional(standardDependencyOk.optional()).build();
                    }
                    if (d.version().isBlank()
                            && !standardDependencyOk.version().isBlank()) {
                        someChange = true;
                        d = d.builder().version(standardDependencyOk.version()).build();
                    }
                }
                if (d.version().isBlank()) {
                    wsModel.LOG
                            .log(NMsg.ofC("failed to resolve effective version for %s", d).asFineFail());
                }
            }

            if ("import".equals(d.scope())) {
                someChange = true;
                newDeps.addAll(NFetch.of(d.toId())
                        .dependencyFilter(NDependencyFilters.of().byRunnable())
                        .getResultDescriptor().dependencies());
            } else {
                newDeps.add(d);
            }
        }
        effectiveDescriptor = effectiveDescriptor.builder().dependencies(newDeps).build();
        return effectiveDescriptor;
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + (wsModel == null ? null : wsModel.configModel)
                + '}';
    }

    protected NWorkspaceArchetypeComponent initializeWorkspace(String archetype) {
        if (NBlankable.isBlank(archetype)) {
            archetype = "default";
        }
        NWorkspaceArchetypeComponent archetypeInstance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NWorkspaceArchetypeComponent ac : wsModel.extensions.createAllSupported(NWorkspaceArchetypeComponent.class, archetype)) {
            if (archetype.equals(ac.name())) {
                archetypeInstance = ac;
                break;
            }
            validValues.add(ac.name());
        }
        if (archetypeInstance == null) {
            //get the default implementation
            throw new NException(
                    NMsg.ofC("invalid archetype %s. Valid values are : %s", archetype, validValues)
            );
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        if (NSecurityManager.of().getUser(NConstants.Users.ADMIN).isEmpty()) {
            try (NSecureString s = NSecureString.ofSecure("admin".toCharArray())) {
                NSecurityManager.of().addUser(
                        NUserSpec.of(NConstants.Users.ADMIN)
                                .credential(s)
                );
            }
        }

        archetypeInstance.initializeWorkspace();
        //now that all repos are created we need to update boot repositories
        loadRuntimeRepositories(null);
        if (!isReadOnly()) {
            saveConfig();
        }
        return archetypeInstance;
    }

    private NId resolveApiId(NId id, Set<NId> visited) {
        if (visited.contains(id.longId())) {
            return null;
        }
        visited.add(id.longId());
        if (NId.getApi("").get().equalsShortId(id)) {
            return id;
        }
        for (NDependency dependency : NFetch.of(id)
                .dependencyFilter(NDependencyFilters.of().byRunnable())
                .getResultDescriptor().dependencies()) {
            NId q = resolveApiId(dependency.toId(), visited);
            if (q != null) {
                return q;
            }
        }
        return null;
    }


    public String resolveCommandName(NId id) {
        String nn = id.artifactId();
        NWorkspace aliases = this;
        NCustomCmd c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreFilterUtils.matchesSimpleNameStaticVersion(c.owner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.artifactId() + "-" + id.version();
        c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreFilterUtils.matchesSimpleNameStaticVersion(c.owner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.groupId() + "." + id.artifactId() + "-" + id.version();
        c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreFilterUtils.matchesSimpleNameStaticVersion(c.owner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        throw new NElementNotFoundException(
                NMsg.ofC("unable to resolve command name for %s", id
                ));
    }

    protected boolean loadWorkspace(List<String> excludedExtensions, String[] excludedRepositories) {
        if (wsModel.configModel.loadWorkspace()) {
            //extensions already wired... this is needless!
            for (NId extensionId : wsModel.extensions.configExtensions()) {
                if (wsModel.extensionModel.isExcludedExtension(extensionId)) {
                    continue;
                }
                wsModel.extensionModel.wireExtension(extensionId,
                        NFetch.of()
                );
            }
            NUserConfig adminSecurity = getConfigModel()
                    .getUser(NConstants.Users.ADMIN);
            if (adminSecurity == null) {
                if (wsModel.LOG.isLoggable(Level.CONFIG)) {
                    wsModel.LOG
                            .log(NMsg.ofC("%s user is missing. reset to default", NConstants.Users.ADMIN)
                                    .withLevel(Level.CONFIG).withIntent(NMsgIntent.FAIL)
                            );
                }
                adminSecurity = new NUserConfig();
                adminSecurity.userName(NConstants.Users.ADMIN);
                try (NSecureString s = NSecureString.ofSecure("admin".toCharArray())) {
                    adminSecurity.credential(NSecurityManager.of().addOneWayCredential(s).toString());
                }
                getConfigModel().addOrUpdateUser(adminSecurity);
            } else if (NBlankable.isBlank(adminSecurity.credential())) {
                if (wsModel.LOG.isLoggable(Level.CONFIG)) {
                    wsModel.LOG
                            .log(NMsg.ofC("%s user has no credentials. reset to default", NConstants.Users.ADMIN)
                                    .withLevel(Level.CONFIG).withIntent(NMsgIntent.FAIL)
                            );
                }
                adminSecurity = adminSecurity.copy();
                try (NSecureString s = NSecureString.ofSecure(WEAK_ADMIN_PASSWORD.toCharArray())) {
                    adminSecurity.credential(NSecurityManager.of().addOneWayCredential(s).toString());
                }
                getConfigModel().addOrUpdateUser(adminSecurity);
            }
            for (NCommandFactoryConfig commandFactory : this.commandFactories()) {
                try {
                    this.addCommandFactory(commandFactory);
                } catch (Exception e) {
                    wsModel.LOG
                            .log(NMsg.ofJ("unable to instantiate Command Factory {0} : {1}", commandFactory, e).asError(e));
                }
            }
            DefaultNWorkspaceEvent workspaceReloadedEvent = new DefaultNWorkspaceEvent(currentSession(), null, null, null, null);
            for (NWorkspaceListener listener : workspaceListeners()) {
                listener.onReloadWorkspace(workspaceReloadedEvent);
            }
            //if save is needed, will be applied
            //config().save(false, session);
            return true;
        }
        return false;
    }

    @Override
    public NText getWelcomeText() {
        return callWith(() -> {
            NTexts txt = NTexts.of();
            NPath p = NPath.of("classpath:/net/thevpc/nuts/runtime/nuts-welcome.ntf", getClass().getClassLoader());
            NText n = txt.parser().parse(p);
            n = txt.transform(n, new NTextTransformConfig().processAll(true)
                    .importClassLoader(getClass().getClassLoader())
                    .currentDir(p.parent()));
            return (n == null ? txt.ofStyled("no welcome found!", NTextStyle.error()) : n);
        });
    }


    @Override
    public NText getHelpText() {
        return callWith(() -> {
            NTexts txt = NTexts.of();
            NPath path = NPath.of("classpath:/net/thevpc/nuts/runtime/nuts-help.ntf", getClass().getClassLoader());
            NText n = txt.parser().parse(path);
            n = txt.transform(n, new NTextTransformConfig()
                    .processAll(true)
                    .rootLevel(1));
            return (n == null ? txt.ofStyled("no help found", NTextStyle.error()) : n);
        });
    }

    @Override
    public NText resolveDefaultHelp(Class<?> clazz) {
        return callWith(() -> {
            NId nutsId = NId.getForClass(clazz).orNull();
            if (nutsId != null) {
                NPath urlPath = NPath.of("classpath:/" + nutsId.shortId().mavenFolder() + ".ntf", clazz == null ? null : clazz.getClassLoader());
                NTexts txt = NTexts.of();
                NText n = txt.parser().parse(urlPath);
                n = txt.transform(n, new NTextTransformConfig()
                        .processAll(true)
                        .importClassLoader(clazz == null ? null : clazz.getClassLoader())
                        .currentDir(urlPath.parent())
                        .rootLevel(1));
                if (n == null) {
                    return txt.ofStyled(
                            NMsg.ofC(
                                    "no default help found at %s for %s", urlPath, (clazz == null ? null : clazz.getName())
                            )
                            , NTextStyle.error()
                    );
                }
                return n;
            }
            return null;
        });
    }

    @Override
    public NText getLicenseText() {
        return callWith(() -> {
            NTexts txt = NTexts.of();
            NPath p = NPath.of("classpath:/net/thevpc/nuts/runtime/nuts-license.ntf", getClass().getClassLoader());
            NText n = txt.parser().parse(p);
            return (n == null ? NText.ofStyled("no license found", NTextStyle.error()) : n);
        });
    }


    @Override
    public NId resolveEffectiveId(NDescriptor descriptor) {
        if (descriptor == null) {
            throw new NArtifactNotFoundException(null);
        }
        NId thisId = descriptor.id();
        String a = thisId.artifactId();
        String g = thisId.groupId();
        String v = thisId.version().value();
        if ((NBlankable.isBlank(g)) || (NBlankable.isBlank(v))) {
            List<NId> parents = descriptor.parents();
            for (NId parent : parents) {
                NId p = NFetch.of(parent)
                        .dependencyFilter(NDependencyFilters.of().byRunnable())
                        .getResultId();
                if (NBlankable.isBlank(g)) {
                    g = p.groupId();
                }
                if (NBlankable.isBlank(v)) {
                    v = p.version().value();
                }
                if (!NBlankable.isBlank(g) && !NBlankable.isBlank(v)) {
                    break;
                }
            }
            if (NBlankable.isBlank(g) || NBlankable.isBlank(v)) {
                throw new NArtifactNotFoundException(thisId,
                        NMsg.ofC("unable to fetchEffective for %s. best Result is %s", thisId, thisId),
                        null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v) || CoreStringUtils.containsVars(a)) {
            Map<String, String> p = NDescriptorUtils.getPropertiesMap(descriptor.properties());
            NId bestId = NIdBuilder.of(g, thisId.artifactId()).version(v).build();
            bestId = NDescriptorUtils.applyProperties(bestId.builder(), new MapToFunction(p)).build();
            if (CoreNUtils.isEffectiveId(bestId)) {
                return bestId;
            }
            Stack<NId> all = new Stack<>();
            List<NId> parents = descriptor.parents();
            all.addAll(parents);
            while (!all.isEmpty()) {
                NId parent = all.pop();
                NDescriptor dd = NFetch.of(parent)
                        .dependencyFilter(NDependencyFilters.of().byRunnable())
                        .getResultDescriptor();
                bestId = NDescriptorUtils.applyProperties(bestId.builder(), new MapToFunction(NDescriptorUtils.getPropertiesMap(dd.properties()))).build();
                if (CoreNUtils.isEffectiveId(bestId)) {
                    return bestId;
                }
                all.addAll(dd.parents());
            }
            throw new NArtifactNotFoundException(bestId,
                    NMsg.ofC("unable to fetchEffective for %s. best Result is %s", bestId, bestId), null);
        }
        NId bestId = NIdBuilder.of(g, thisId.artifactId()).version(v).build();
        if (!CoreNUtils.isEffectiveId(bestId)) {
            throw new NArtifactNotFoundException(bestId,
                    NMsg.ofC("unable to fetchEffective for %s. best Result is %s", thisId, bestId), null);
        }
        return bestId;
    }

    @Override
    public NIdType resolveNutsIdType(NId id) {
        NIdType idType = NIdType.REGULAR;
        String shortName = id.shortName();
        if (shortName.equals(NConstants.Ids.NUTS_API)) {
            idType = NIdType.API;
        } else if (shortName.equals(NConstants.Ids.NUTS_RUNTIME)) {
            idType = NIdType.RUNTIME;
        } else {
            for (NId companionTool : wsModel.extensions.companionIds()) {
                if (companionTool.shortName().equals(shortName)) {
                    idType = NIdType.COMPANION;
                }
            }
        }
        return idType;
    }

    @Override
    public NInstallerComponent getInstaller(NDefinition nutToInstall) {
        if (nutToInstall != null && nutToInstall.content().isPresent()) {
            NDescriptor descriptor = nutToInstall.descriptor();
            NArtifactCall installerDescriptor = descriptor.installer();
            NDefinition runnerFile = null;
            if (installerDescriptor != null) {
                NId installerId = installerDescriptor.id();
                if (installerId != null) {
                    // nsh is the only installer that does not need to have groupId!
                    if (NBlankable.isBlank(installerId.groupId())
                            && "nsh".equals(installerId.artifactId())
                    ) {
                        installerId = installerId.builder().groupId("net.thevpc.nsh").build();
                    }
                    //ensure installer is always well qualified!
                    CoreNIdUtils.checkShortId(installerId);
                    runnerFile = NSearch.of().id(installerId)
                            .dependencyFilter(NDependencyFilters.of().byRunnable())
                            .latest(true)
                            .distinct(true)
                            .getResultDefinitions()
                            .findFirst().orNull();

                }
            }
            NInstallerComponent best = wsModel.extensions
                    .createSupported(NInstallerComponent.class, runnerFile == null ? nutToInstall : runnerFile).orNull();
            if (best != null) {
                return best;
            }
            return new CommandForIdNInstallerComponent(runnerFile);
        }
        return new CommandForIdNInstallerComponent(null);
    }


    /**
     * true when core extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : core extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_RUNTIME_EXTENSION</code> is forced to
     * false
     *
     * @return true when core extension is required for running this workspace
     */
    @Override
    public boolean requiresRuntimeExtension() {
        boolean coreFound = false;
        for (NId ext : wsModel.extensions.configExtensions()) {
            if (ext.equalsShortId(runtimeId())) {
                coreFound = true;
                break;
            }
        }
        return !coreFound;
    }

    @Override
    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor) {
        return resolveEffectiveDescriptor(descriptor, null);
    }

    @Override
    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor, NDescriptorEffectiveConfig effectiveNDescriptorConfig) {
        if (effectiveNDescriptorConfig == null) {
            effectiveNDescriptorConfig = new NDescriptorEffectiveConfig();
        }
        String cacheId = null;
        if (effectiveNDescriptorConfig.equals(new NDescriptorEffectiveConfig())) {
            cacheId = "eff-nuts.cache";
        }
        if (cacheId != null) {
            if (!descriptor.id().version().isBlank() && descriptor.id().version().isSingleValue()
                    && descriptor.id().toString().indexOf('$') < 0) {
                try {
                    NDescriptor d = store().loadLocationKey(NStoreKey.ofCacheFaced(descriptor.id(), null, cacheId), NDescriptor.class);
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    wsModel.LOG.log(NMsg.ofC("failed to load eff-nuts.cache for %s", descriptor.id()).asError(ex));
                    //
                }
            }
        }
        //
        NDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor, effectiveNDescriptorConfig);
        NDescriptorUtils.checkValidEffectiveDescriptor(effectiveDescriptor);

        if (cacheId != null) {
            try {
                store().saveLocationKey(NStoreKey.ofCacheFaced(effectiveDescriptor.id(), null, cacheId), effectiveDescriptor);
            } catch (Exception ex) {
                wsModel.LOG
                        .log(NMsg.ofC("failed to save eff-nuts.cache for %s", effectiveDescriptor.id()).asError(ex));
                //
            }
        }
        return effectiveDescriptor;
    }

    @Override
    public NInstalledRepository getInstalledRepository() {
        return wsModel.installedRepository;
    }

    @Override
    public NInstallStatus getInstallStatus(NId id, boolean checkDependencies) {
        NDefinition nutToInstall;
        try {
            nutToInstall = NSearch.of().transitive(false).addId(id)
                    .inlineDependencies(checkDependencies)
                    .definitionFilter(NDefinitionFilters.of().byDeployed(true))
                    .dependencyFilter(NDependencyFilters.of().byRunnable())
                    .getResultDefinitions()
                    .findFirst().orNull();
            if (nutToInstall == null) {
                return NInstallStatus.NONE;
            }
        } catch (NArtifactNotFoundException e) {
            return NInstallStatus.NONE;
        } catch (Exception ex) {
            wsModel.LOG.log(NMsg.ofJ("error: %s", ex).asError(ex));
            return NInstallStatus.NONE;
        }
        return getInstalledRepository().getInstallStatus(nutToInstall.id());
    }

    @Override
    public NExecutionContextBuilder createExecutionContext() {
        NSession session = NSession.of();
        return new DefaultNExecutionContextBuilder()
                .setDry(session.isDry())
                .setBot(session.isBot())
                .setExecutionType(this.bootOptions().executionType().orNull())
                ;
    }

    @Override
    public void deployBoot(NId id, boolean withDependencies) {
        runWith(() -> {
            Map<NId, NDefinition> defs = new HashMap<>();
            NDependencyFilter dependencyRunFilter = NDependencyFilters.of().byRunnable();
            NDefinition m = NFetch.of(id).failFast(false).dependencyFilter(dependencyRunFilter).getResultDefinition();
            Map<String, String> a = new LinkedHashMap<>();
            a.put("configVersion", Nuts.version().toString());
            a.put("id", id.longName());
            a.put("dependencies", m.dependencies().get().transitive()
                    .map(NDependency::longName)
                    .withDescription(NDescribables.ofDesc("getLongName"))
                    .collect(Collectors.joining(";")));
            defs.put(m.id().longId(), m);
            if (withDependencies) {
                for (NDependency dependency : m.dependencies().get()) {
                    if (!defs.containsKey(dependency.toId().longId())) {
                        m = NFetch.of(id).failFast(false).dependencyFilter(dependencyRunFilter).getResultDefinition();
                        defs.put(m.id().longId(), m);
                    }
                }
            }
            for (NDefinition def : defs.values()) {
                NPath bootstrapFolder = getLocationModel().getStoreLocation(NStoreScope.WORKSPACE, NStoreType.LIB).resolve(NConstants.Folders.ID);
                NId id2 = def.id();
                NCp.of().from(def.content().get())
                        .to(bootstrapFolder.resolve(this.getDefaultIdBasedir(id2))
                                .resolve(this.getDefaultIdFilename(id2.builder().faceContent().packaging("jar").build()))
                        ).run();
                NDescriptorWriter.of().ntf(false)
                        .print(NFetch.of(id2).dependencyFilter(dependencyRunFilter).getResultDescriptor(), bootstrapFolder.resolve(this.getDefaultIdBasedir(id2))
                                .resolve(this.getDefaultIdFilename(id2.builder().faceDescriptor().build())));

                Map<String, String> pr = new LinkedHashMap<>();
                pr.put("file.updated.date", Instant.now().toString());
                pr.put("project.id", def.id().shortId().toString());
                pr.put("project.name", def.id().shortId().toString());
                pr.put("project.version", def.id().version().toString());
                pr.put("repositories", "~/.m2/repository"
                        + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryUtils.createRepositoryLocation("vpc-public-maven").get(), true).sourceLocation()
                        + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryUtils.createRepositoryLocation("maven-central").get(), true).sourceLocation()
                        + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryUtils.createRepositoryLocation("nuts-public").get(), true).sourceLocation()
                );
                pr.put("project.dependencies.compile",
                        String.join(";",
                                def.dependencies().get().transitive()
                                        .filter(
                                                NPredicate.of(
                                                        x -> !x.isOptional()
                                                                && dependencyRunFilter
                                                                .acceptDependency(x, def.id())
                                                        , NElement.ofName("optionalAndRunnable")
                                                )
                                        )
                                        .map(x -> x.toId().longName())
                                        .withDescription(NDescribables.ofDesc("toId.getLongName"))
                                        .toList()
                        )
                );

                try (Writer writer = bootstrapFolder.resolve(this.getDefaultIdBasedir(def.id().longId()))
                        .resolve("nuts.properties").getWriter()
                ) {
                    NPropsTransformer.storeProperties(pr, writer, false);
                } catch (IOException ex) {
                    throw new NIOException(ex);
                }
            }
        });
    }

    public NSession defaultSession() {
        return wsModel.bootModel.bootSession();
    }

    @Override
    public NWorkspaceModel getModel() {
        return wsModel;
    }

    @Override
    public String uuid() {
        return wsModel.uuid;
    }

    @Override
    public String name() {
        return wsModel.name;
    }

    @Override
    public String digestName() {
        if (wsModel.hashName == null) {
            runWith(() -> {
                wsModel.hashName = NDigestName.of().digestName(this);
            });
        }
        return wsModel.hashName;
    }

    @Override
    public NVersion apiVersion() {
        return Nuts.version();
    }

    @Override
    public NVersion bootVersion() {
        return Nuts.bootVersion();
    }

    @Override
    public NId apiId() {
        return wsModel.apiId;
    }

    @Override
    public NId appId() {
        return NId.get(wsModel.apiId.groupId(), NConstants.Ids.NUTS_APP_ARTIFACT_ID, Nuts.bootVersion()).get();
    }

    @Override
    public NId runtimeId() {
        return wsModel.runtimeId;
    }

    @Override
    public NPath location() {
        return wsModel.location == null ? null : NPath.of(wsModel.location);
    }

    @Override
    public NSession createSession() {
        return callWith(() -> {
            NSession nSession = new DefaultNSession(this);
            nSession.terminal(NTerminal.ofSystem());
            nSession.expireTime(this.bootOptions().expireTime().orNull());
            return nSession;
        });
    }

    public DefaultCustomCommandsModel getCommandModel() {
        return wsModel.commandModel;
    }

    public DefaultNWorkspaceConfigModel getConfigModel() {
        return wsModel.configModel;
    }

    public DefaultImportModel getImportModel() {
        return wsModel.importModel;
    }


    @Override
    public String getInstallationDigest() {
        return wsModel.installationDigest;
    }

    @Override
    public void setInstallationDigest(String value) {
        this.wsModel.installationDigest = value;
        store().saveLocationKey(NStoreKey.ofConf(apiId(), null, "installation-digest"), NStringUtils.trimToNull(value));
    }

    @Override
    public NExtensions extensions() {
        return wsModel.extensions;
    }

    @Override
    public NSession currentSession() {
        NSession old = sessionScopes().get();
        if (old == null) {
            return defaultSession();
        }
        return old;
    }

    @Override
    public NScopedValue<NSession> sessionScopes() {
        return wsModel.sessionScopes;
    }

//    public enum InstallStrategy0 implements NEnum {
//        INSTALL,
//        UPDATE,
//        REQUIRE;
//        private final String id;
//
//        InstallStrategy0() {
//            this.id = NNameFormat.ID_NAME.format(name());
//        }
//
//        public static NOptional<InstallStrategy0> parse(String value) {
//            return NEnumUtils.parseEnum(value, InstallStrategy0.class);
//        }
//
//        @Override
//        public String id() {
//            return id;
//        }
//    }

    public DefaultNRepositoryModel getRepositoryModel() {
        return wsModel.repositoryModel;
    }


    @Override
    public List<NRepository> repositories() {
        return Arrays.stream(getRepositoryModel().getRepositories())
                .collect(Collectors.toList());
    }

    @Override
    public NOptional<NRepository> findRepositoryById(String repositoryNameOrId) {
        return getRepositoryModel().findRepositoryById(repositoryNameOrId);
    }

    @Override
    public NOptional<NRepository> findRepositoryByName(String repositoryNameOrId) {
        return getRepositoryModel().findRepositoryByName(repositoryNameOrId);
    }

    @Override
    public NOptional<NRepository> getRepository(String repositoryNameOrId) {
        return getRepositoryModel().getRepository(repositoryNameOrId);
    }

    @Override
    public NWorkspace removeRepository(String repositoryId) {
        getRepositoryModel().removeRepository(repositoryId);
        return this;
    }

    @Override
    public NWorkspace removeAllRepositories() {
        getRepositoryModel().removeAllRepositories();
        return this;
    }

    @Override
    public NRepository addRepository(NRepositorySpec options) {
        return getRepositoryModel().addRepository(options);
    }

    @Override
    public NRepository addRepository(String repositoryNamedUrl) {
        return getRepositoryModel().addRepository(repositoryNamedUrl);
    }


    /// ////////////////////////////

    public DefaultNWorkspaceLocationModel getLocationsModel() {
        return wsModel.locationsModel;
    }


    @Override
    public Map<String, Object> properties() {
        return wsModel.getProperties();
    }

    @Override
    public NOptional<Object> getProperty(String property) {
        return wsModel.getProperty(property);
    }

    @Override
    public <T> NOptional<T> getProperty(Class<T> propertyTypeAndName) {
        if (propertyTypeAndName == null) {
            return NOptional.ofNamedEmpty("<empty-type>");
        }
        return getProperty(propertyTypeAndName.getName()).instanceOf(propertyTypeAndName);
    }

    public <T> T getOrComputeProperty(Class<T> property, Supplier<T> supplier) {
        return getOrComputeProperty(property == null ? null : property.getName(), supplier);
    }

    public <T> T getOrComputeProperty(String property, Supplier<T> supplier) {
        return wsModel.getOrCreateProperty(property, supplier);
    }

    @Override
    public NWorkspace setProperty(String property, Object value) {
        wsModel.setProperty(property, value);
        return this;
    }


    @Override
    public String pid() {
        return wsModel.getPid();
    }

    public void addLauncher(NLauncherOptions launcher) {
        //apply isolation!
        NIsolationLevel isolation = this.bootOptions().isolationLevel().orElse(NIsolationLevel.SYSTEM);
        if (isolation.compareTo(NIsolationLevel.CONFINED) >= 0) {
            launcher.createDesktopLauncher(NSupportMode.NEVER);
            launcher.createMenuLauncher(NSupportMode.NEVER);
            launcher.createUserLauncher(NSupportMode.NEVER);
            launcher.switchWorkspace(false);
            launcher.switchWorkspaceLocation(null);
        }
        SystemNdi ndi = NSettingsNdiSubCommand.createNdi();
        if (ndi != null) {
            ndi.addScript(
                    new NdiScriptOptions()
                            .setLauncher(launcher.copy()),
                    new String[]{launcher.id().builder().fullName()}
            );
        }
    }

    @Override
    public List<String> buildEffectiveCommand(String[] cmd, NRunAs runAsMode, Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich, Boolean gui, String rootName, String userName, String[] executorOptions) {
        return NSysExecUtils.buildEffectiveCommand(cmd, runAsMode, de, sysWhich, gui, rootName, userName, executorOptions);
    }

    @Override
    public NPath getHomeLocation(NStoreType folderType) {
        return getLocationModel().getHomeLocation(folderType);
    }

    public DefaultNWorkspaceLocationModel getLocationModel() {
        return NWorkspaceExt.of().getModel().locationsModel;
    }

    @Override
    public NStoreStrategy storeStrategy() {
        return getLocationModel().getStoreStrategy();
    }

    @Override
    public NStoreStrategy repositoryStoreStrategy() {
        return getLocationModel().getRepositoryStoreStrategy();
    }

    @Override
    public NOsFamily storeLayout() {
        return getLocationModel().getStoreLayout();
    }

    @Override
    public Map<NStoreType, String> storeLocations() {
        return getLocationModel().getStoreLocations();
    }

    @Override
    public String getDefaultIdFilename(NId id) {
        return getLocationModel().getDefaultIdFilename(id);
    }

    @Override
    public NPath getDefaultIdBasedir(NId id) {
        return getLocationModel().getDefaultIdBasedir(id);
    }

    @Override
    public String getDefaultIdContentExtension(String packaging) {
        return getLocationModel().getDefaultIdContentExtension(packaging);
    }

    @Override
    public String getDefaultIdExtension(NId id) {
        return getLocationModel().getDefaultIdExtension(id);
    }

    @Override
    public Map<NHomeLocation, String> homeLocations() {
        return getLocationModel().getHomeLocations();
    }

    @Override
    public NPath getHomeLocation(NHomeLocation location) {
        return getLocationModel().getHomeLocation(location);
    }

    @Override
    public NPath workspaceLocation() {
        return getLocationModel().getWorkspaceLocation();
    }

    @Override
    public NWorkspace setStoreLocation(NStoreType folderType, String location) {
        getLocationModel().setStoreLocation(folderType, location);
        return this;
    }


    @Override
    public NWorkspace storeStrategy(NStoreStrategy strategy) {
        getLocationModel().setStoreStrategy(strategy);
        return this;
    }

    @Override
    public NWorkspace storeLayout(NOsFamily storeLayout) {
        getLocationModel().setStoreLayout(storeLayout);
        return this;
    }

    @Override
    public NWorkspace setHomeLocation(NHomeLocation homeType, String location) {
        getLocationModel().setHomeLocation(homeType, location);
        return this;
    }

    /// ////////////


    @Override
    public NOptional<String> findSysCommand(String commandName) {
        char pathSeparatorChar = File.pathSeparatorChar;
        if (!NBlankable.isBlank(commandName)) {
            if (!commandName.contains("/") && !commandName.contains("\\") && !commandName.equals(".") && !commandName.equals("..")) {
                switch (NEnv.of().osFamily()) {
                    case WINDOWS: {
                        List<String> paths = NStringUtils.split(NEnv.of().getEnv("PATH").orNull(), "" + pathSeparatorChar, true, true);
                        List<String> execExtensions = NStringUtils.split(NEnv.of().getEnv("PATHEXT").orNull(), "" + pathSeparatorChar, true, true);
                        if (paths.isEmpty()) {
                            paths.addAll(Arrays.asList("C:\\Windows\\system32", "C:\\Windows"));
                        }
                        if (execExtensions.isEmpty()) {
                            execExtensions.addAll(Collections.singletonList(".COM;.EXE;.BAT;.CMD;.VBS;.VBE;.JS;.JSE;.WSF;.WSH;.MSC"));
                        }
                        for (String z : paths) {
                            NPath t = NPath.of(z);
                            NPath p = t.resolve(commandName);
                            if (p.isRegularFile()) {
                                return NOptional.of(p.toString());
                            }
                            for (String ext : execExtensions) {
                                ext = ext.toLowerCase();
                                if (!(commandName.toLowerCase().endsWith(ext))) {
                                    p = t.resolve(commandName + ext);
                                    if (p.isRegularFile()) {
                                        return NOptional.of(p.toString());
                                    }
                                }
                            }
                        }
                        break;
                    }
                    default: {
                        List<String> paths = NStringUtils.split(NEnv.of().getEnv("PATH").orNull(), "" + pathSeparatorChar, true, true);
                        for (String z : paths) {
                            NPath t = NPath.of(z);
                            NPath p = t.resolve(commandName);
                            if (p.isRegularFile()) {
                                //if(Files.isExecutable(fp)) {
                                return NOptional.of(p.toString());
                                //}
                            }
                        }
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("command %s", commandName));
    }


    @Override
    public NWorkspace addImports(String... importExpressions) {
        getImportModel().add(importExpressions);
        return this;
    }


    @Override
    public NWorkspace clearImports() {
        getImportModel().removeAll();
        return this;
    }

    @Override
    public NWorkspace removeImports(String... importExpressions) {
        getImportModel().remove(importExpressions);
        return this;
    }

    @Override
    public NWorkspace updateImports(String[] imports) {
        getImportModel().set(imports);
        return this;
    }

    @Override
    public boolean isImportedGroupId(String groupId) {
        return getImportModel().isImportedGroupId(groupId);
    }

    @Override
    public Set<String> allImports() {
        return getImportModel().getAll();
    }

    /// ////////////////////////////////////


    @Override
    public NWorkspaceStoredConfig storedConfig() {
        return getConfigModel().stored();
    }

    @Override
    public boolean isReadOnly() {
        return getConfigModel().isReadOnly();
    }

    @Override
    public boolean saveConfig(boolean force) {
        return getConfigModel().save(force);
    }

    @Override
    public boolean saveConfig() {
        return getConfigModel().save();
    }

    @Override
    public NWorkspaceBootConfig loadBootConfig(String _ws, boolean global, boolean followLinks) {
        String _ws0 = _ws;
        String effWorkspaceName = null;
        String lastConfigPath = null;
        NWorkspaceConfigBoot lastConfigLoaded = null;
        boolean defaultLocation = false;
        NPlatformHome plocs = NPlatformHome.of(null, global);
        if (_ws != null && _ws.matches("[a-z-]+://.*")) {
            //this is a protocol based workspace
            //String protocol=ws.substring(0,ws.indexOf("://"));
            effWorkspaceName = "remote-bootstrap";
            lastConfigPath = plocs.getWorkspaceLocation(CoreNUtils.resolveValidWorkspaceName(effWorkspaceName));
            lastConfigLoaded = store().loadWorkspaceConfigBoot(NPath.of(lastConfigPath));
            defaultLocation = true;
            return new DefaultNWorkspaceBootConfig(_ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else if (followLinks) {
            defaultLocation = CoreNUtils.isValidWorkspaceName(_ws);
            int maxDepth = 36;
            for (int i = 0; i < maxDepth; i++) {
                lastConfigPath
                        = CoreNUtils.isValidWorkspaceName(_ws)
                        ? plocs.getWorkspaceLocation(CoreNUtils.resolveValidWorkspaceName(_ws)
                ) : NIOUtils.getAbsolutePath(_ws);

                NWorkspaceConfigBoot configLoaded = store().loadWorkspaceConfigBoot(NPath.of(lastConfigPath));
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
                    throw new NIllegalArgumentException(NMsg.ofPlain("cyclic workspace resolution"));
                }
            }
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNWorkspaceBootConfig(_ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else {
            defaultLocation = CoreNUtils.isValidWorkspaceName(_ws);
            lastConfigPath
                    = CoreNUtils.isValidWorkspaceName(_ws)
                    ? plocs.getWorkspaceLocation(CoreNUtils.resolveValidWorkspaceName(_ws)
            ) : NIOUtils.getAbsolutePath(_ws);

            lastConfigLoaded = store().loadWorkspaceConfigBoot(NPath.of(lastConfigPath));
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNWorkspaceBootConfig(_ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        }
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        return getConfigModel().isSupportedRepositoryType(repositoryType);
    }

    @Override
    public List<NRepositorySpec> defaultRepositories() {
        return getConfigModel().getDefaultRepositoryDefinitions();
    }

    @Override
    public Set<String> availableArchetypes() {
        return getConfigModel().getAvailableArchetypes();
    }

    @Override
    public NPath resolveRepositoryPath(String repositoryLocation) {
        return getConfigModel().resolveRepositoryPath(NPath.of(repositoryLocation));
    }

    @Override
    public NIndexStoreFactory indexStoreClientFactory() {
        return getConfigModel().getIndexStoreClientFactory();
    }

    @Override
    public String javaCommand() {
        return getConfigModel().getJavaCommand();
    }

    @Override
    public String javaOptions() {
        return getConfigModel().getJavaOptions();
    }

    @Override
    public boolean isSystemWorkspace() {
        return getConfigModel().isSystem();
    }

    public List<String> getDependencySolverNames() {
        return getConfigModel().getDependencySolverNames();
    }

    public NDependencySolver createDependencySolver(String name) {
        return getConfigModel().createDependencySolver(name);
    }

    @Override
    public Map<String, String> configMap() {
        return getConfigModel().getConfigMap();
    }

    @Override
    public NOptional<NLiteral> getConfigProperty(String property) {
        return getConfigModel().getConfigProperty(property);
    }

    @Override
    public NWorkspace setConfigProperty(String property, String value) {
        getConfigModel().setConfigProperty(property, value);
        getConfigModel().save();
        return this;
    }

    /// ///////////////////

    @Override
    public List<NCommandFactoryConfig> commandFactories() {
        return Arrays.asList(getCommandModel().getFactories());
    }

    @Override
    public void addCommandFactory(NCommandFactoryConfig commandFactoryConfig) {
        getCommandModel().addFactory(commandFactoryConfig);
    }

    @Override
    public void removeCommandFactory(String commandFactoryId) {
        getCommandModel().removeFactory(commandFactoryId);
    }

    @Override
    public boolean removeCommandFactoryIfExists(String commandFactoryId) {
        return getCommandModel().removeFactoryIfExists(commandFactoryId);
    }

    @Override
    public boolean commandExists(String command) {
        return findCommand(command) != null;
    }

    @Override
    public boolean commandFactoryExists(String factoryId) {
        return getCommandModel().commandFactoryExists(factoryId);
    }

    @Override
    public boolean addCommand(NCommandConfig command) {
        return getCommandModel().add(command);
    }

    @Override
    public boolean updateCommand(NCommandConfig command) {
        return getCommandModel().update(command);
    }

    @Override
    public void removeCommand(String command) {
        getCommandModel().remove(command);
    }

    @Override
    public boolean removeCommandIfExists(String name) {
        if (getCommandModel().find(name) != null) {
            getCommandModel().remove(name);
            return true;
        }
        return false;
    }

    @Override
    public NCustomCmd findCommand(String name, NId forId, NId forOwner) {
        return getCommandModel().find(name, forId, forOwner);
    }

    @Override
    public NCustomCmd findCommand(String name) {
        return getCommandModel().find(name);
    }

    @Override
    public List<NCustomCmd> findAllCommands() {
        return getCommandModel().findAll();
    }

    @Override
    public List<NCustomCmd> findCommandsByOwner(NId id) {
        return getCommandModel().findByOwner(id);
    }


    /// ///////////////////////


    public DefaultNBootModel getBootModel() {
        return NWorkspaceExt.of().getModel().bootModel;
    }

    @Override
    public boolean isFirstBoot() {
        return getBootModel().isFirstBoot();
    }

    @Override
    public NOptional<NLiteral> getCustomBootOption(String... names) {
        return getBootModel().getCustomBootOption(names);
    }


    @Override
    public NBootOptions bootOptions() {
        return getBootModel().getBootEffectiveOptions();
    }

    @Override
    public ClassLoader bootClassLoader() {
        return getConfigModel().getBootClassLoader();
    }

    @Override
    public List<URL> bootClassWorldURLs() {
        return Collections.unmodifiableList(getConfigModel().getBootClassWorldURLs());
    }

    @Override
    public List<String> bootRepositories() {
        return getConfigModel().getBootRepositories();
    }

    @Override
    public Instant creationStartTime() {
        return getConfigModel().getCreationStartTime();
    }

    @Override
    public Instant creationFinishTime() {
        return getConfigModel().getCreationFinishTime();
    }

    @Override
    public NDuration creationDuration() {
        return getConfigModel().getCreateDuration();
    }

    public NClassLoaderNode bootRuntimeClassLoaderNode() {
        return getBootModel().getBootUserOptions().runtimeBootDependencyNode().get();
    }

    public List<NClassLoaderNode> bootExtensionClassLoaderNodes() {
        return getBootModel().getBootUserOptions().extensionBootDependencyNodes().orElseGet(Collections::emptyList);
    }

    @Override
    public NWorkspaceTerminalOptions bootTerminal() {
        return getBootModel().getBootTerminal();
    }


    @Override
    public void runApplication(NApplicationHandleMode handleMode) {
        NWorkspaceHelper.runApplication(this, handleMode);
    }

    @Override
    public void runBootCommand() {
        NWorkspaceHelper.runBootCommand(this);
    }

}
