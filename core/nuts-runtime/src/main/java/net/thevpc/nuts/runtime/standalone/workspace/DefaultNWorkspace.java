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
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootWorkspaceFactory;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.NBootWorkspaceAlreadyExistsException;
import net.thevpc.nuts.boot.NBootWorkspaceNotFoundException;
import net.thevpc.nuts.NWorkspaceTerminalOptions;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.format.NVersionFormat;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryModel;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NSettingsNdiSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.SystemNdi;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElementNotFoundException;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.format.NTableModel;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.NScopedWorkspace;
import net.thevpc.nuts.runtime.standalone.NLocationKey;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLog;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.DefaultNProperties;
import net.thevpc.nuts.runtime.standalone.boot.NBootConfig;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.event.*;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.extension.NExtensionListHelper;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.installer.CommandForIdNInstallerComponent;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogModel;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNWorkspaceSecurityModel;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/6/17.
 */
@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNWorkspace extends AbstractNWorkspace implements NWorkspaceExt {
    public static final Pattern UNIX_USER_DIRS_PATTERN = Pattern.compile("^\\s*(?<k>[A-Z_]+)\\s*=\\s*(?<v>.*)$");

    public static final NVersion VERSION_INSTALL_INFO_CONFIG = NVersion.get("0.8.0").get();
    public static final NVersion VERSION_SDK_LOCATION = NVersion.get("0.8.0").get();
    public static final NVersion VERSION_REPOSITORY_CONFIG = NVersion.get("0.8.0").get();
    public static final String VERSION_REPOSITORY_REF = "0.8.0";
    public static final String VERSION_WS_CONFIG_API = "0.8.0";
    public static final NVersion VERSION_WS_CONFIG_BOOT = NVersion.get("0.8.0").get();
    public static final String VERSION_WS_CONFIG_MAIN = "0.8.0";
    public static final String VERSION_WS_CONFIG_RUNTIME = "0.8.0";
    public static final String VERSION_WS_CONFIG_SECURITY = "0.8.0";
    public static final String VERSION_COMMAND_ALIAS_CONFIG = "0.8.0";
    public static final String VERSION_COMMAND_ALIAS_CONFIG_FACTORY = "0.8.0";
    public static final String VERSION_USER_CONFIG = "0.8.0";
    public static final String RUNTIME_VERSION = "0.8.5.0";
    public static final NId RUNTIME_ID = NId.get(NConstants.Ids.NUTS_RUNTIME + "#" + RUNTIME_VERSION).get();
    public NLog LOG;
    private NWorkspaceModel wsModel;

    public DefaultNWorkspace(NBootOptionsInfo callerBootOptionsInfo, NBootOptions info) {
        super(callerBootOptionsInfo);
        initWorkspace(info);
    }

    //    /**
//     * creates a zip file based on the folder. The folder should contain a
//     * descriptor file at its root
//     *
//     * @return bundled nuts file, the nuts is neither deployed nor installed!
//     */
//    @Deprecated
//    public NutsDefinition createBundle(Path contentFolder, Path destFile, NutsQueryOptions queryOptions, NSession session) {
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
//    public boolean isFetched(NutsId parseList, NSession session) {
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
            set.add(i.getId());
            set.addAll(i.getDependencies().stream().map(NDependency::toId).collect(Collectors.toList()));
        }
        return set;
    }

    private static class InitWorkspaceData {
        NBootOptions initialBootOptions;
        NBootOptions effectiveBootOptions;
        String repositories;
        NTexts text;
        NElements elems;
        boolean justInstalled;
        NWorkspaceArchetypeComponent justInstalledArchetype;
        NBootConfig cfg;
        NIO terminals;
    }

    private void initWorkspace(NBootOptions initialBootOptions0) {
        Objects.requireNonNull(initialBootOptions0, () -> "boot options could not be null");
        InitWorkspaceData data = new InitWorkspaceData();
        data.initialBootOptions = initialBootOptions0.readOnly();
        try {
            this.wsModel = new NWorkspaceModel(this, data.initialBootOptions);
            this.runWith(() -> {
                this.wsModel.init();
                _preloadWorkspace(data);
                if (!loadWorkspace(data.effectiveBootOptions.getExcludedExtensions().orElseGet(Collections::emptyList), null)) {
                    _createWorkspaceFirstBoot(data);
                } else {
                    _createWorkspaceNonFirstBoot(data);
                }
                _postCreateWorkspace(data);
            });

        } catch (RuntimeException ex) {
            if (wsModel != null && wsModel.recomm != null) {
                try {
                    NId runtimeId = getRuntimeId();
                    String sRuntimeId = runtimeId == null ? NId.getRuntime("").get().toString() : runtimeId.toString();
                    this.runWith(() -> {
                        displayRecommendations(wsModel.recomm.getRecommendations(new RequestQueryInfo(sRuntimeId, ex), NRecommendationPhase.BOOTSTRAP, true));
                    });
                } catch (Exception ex2) {
                    //just ignore
                }
            }
            throw ex;
        } finally {
            if (wsModel != null && wsModel.bootModel != null) {
                wsModel.bootModel.setInitializing(false);
            }
        }
    }

    private void _preloadWorkspace(InitWorkspaceData data) {
        this.LOG = new DefaultNLog(this, DefaultNWorkspace.class, true);
        data.effectiveBootOptions = this.wsModel.bootModel.getBootEffectiveOptions();
        this.wsModel.configModel = new DefaultNWorkspaceConfigModel(this);
        String workspaceLocation = data.effectiveBootOptions.getWorkspace().orNull();
        data.repositories = data.effectiveBootOptions.getBootRepositories().orNull();
        NBootWorkspaceFactory bootFactory = data.effectiveBootOptions.getBootWorkspaceFactory().orNull();
        ClassLoader bootClassLoader = data.effectiveBootOptions.getClassWorldLoader().orNull();
        this.wsModel.extensionModel = new DefaultNWorkspaceExtensionModel(this, bootFactory,
                data.effectiveBootOptions.getExcludedExtensions().orElse(Collections.emptyList()));
        this.wsModel.logModel = new DefaultNLogModel(this, data.effectiveBootOptions, data.initialBootOptions);
        this.wsModel.filtersModel = new DefaultNFilterModel(this);
        this.wsModel.installedRepository = new DefaultNInstalledRepository(this, data.effectiveBootOptions);
        this.wsModel.envModel = new DefaultNWorkspaceEnvManagerModel(this);
        this.wsModel.sdkModel = new DefaultNPlatformModel(this.wsModel.envModel);
        this.wsModel.locationsModel = new DefaultNWorkspaceLocationModel(this,
                Paths.get(data.effectiveBootOptions.getWorkspace().orNull()).toString());

        this.wsModel.extensionModel.onInitializeWorkspace(data.effectiveBootOptions, bootClassLoader);
        this.wsModel.textModel.loadExtensions();
        data.cfg = new NBootConfig();
        data.cfg.setWorkspace(workspaceLocation);
        data.cfg.setApiVersion(this.wsModel.askedApiVersion);
        data.cfg.setRuntimeId(this.wsModel.askedRuntimeId);
        data.cfg.setRuntimeBootDescriptor(NBootHelper.toDescriptor(data.effectiveBootOptions.getRuntimeBootDescriptor().orNull()));
        data.cfg.setExtensionBootDescriptors(NBootHelper.toDescriptorList(data.effectiveBootOptions.getExtensionBootDescriptors().orNull()));

        this.wsModel.location = data.effectiveBootOptions.getWorkspace().orNull();

        this.wsModel.bootModel.onInitializeWorkspace();

        NSystemTerminalBase termb = wsModel.extensions
                .createComponent(NSystemTerminalBase.class).get();
        data.terminals = NIO.of();
        data.terminals
                .setSystemTerminal(termb)
                .setDefaultTerminal(NTerminal.of())
        ;
        wsModel.bootModel.bootSession().setTerminal(NTerminal.of());
        ((DefaultNLog) LOG).resumeTerminal();
        data.text = NTexts.of();
        try {
            data.text.getTheme();
        } catch (Exception ex) {
            LOG.with().level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("unable to load theme {0}. Reset to default!", data.effectiveBootOptions.getTheme()));
            data.text.setTheme("");//set default!
        }

//        NutsFormatManager formats = this.formats().setSession(defaultSession());
        data.elems = NElements.of();
        _initLog(data);
        wsModel.securityModel = new DefaultNWorkspaceSecurityModel(this);

        Instant now = Instant.now();
        if (data.effectiveBootOptions.getCreationTime().get().compareTo(now) > 0) {
            wsModel.configModel.setStartCreateTime(now);
        } else {
            wsModel.configModel.setStartCreateTime(data.effectiveBootOptions.getCreationTime().get());
        }

        boolean exists = NWorkspaceExt.of().getConfigModel().isValidWorkspaceFolder();
        NOpenMode openMode = data.effectiveBootOptions.getOpenMode().orNull();
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
            try {
                Map rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(getApiId().toString(), ""), NRecommendationPhase.BOOTSTRAP, false);
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
            data.justInstalledArchetype.startWorkspace();
            DefaultNWorkspaceEvent workspaceCreatedEvent = new DefaultNWorkspaceEvent(session, null, null, null, null);
            for (NWorkspaceListener workspaceListener : getWorkspaceListeners()) {
                workspaceListener.onCreateWorkspace(workspaceCreatedEvent);
            }
        }
        if (data.effectiveBootOptions.getUserName().orElse("").trim().length() > 0) {
            char[] password = data.effectiveBootOptions.getCredentials().orNull();
            if (password == null || NBlankable.isBlank(new String(password))) {
                password = data.terminals.getDefaultTerminal().readPassword(NMsg.ofPlain("Password : "));
            }
            NWorkspaceSecurityManager.of().login(data.effectiveBootOptions.getUserName().get(), password);
        }
        wsModel.configModel.setEndCreateTime(Instant.now());
        LOG.with().level(Level.FINE).verb(NLogVerb.SUCCESS)
                .log(
                        NMsg.ofC("%s workspace loaded in %s",
                                NMsg.ofCode("nuts"),
                                NDuration.ofDuration(getCreationDuration())
                        )
                );
        if (data.effectiveBootOptions.getSharedInstance().orElse(false)) {

            NWorkspace o = NScopedWorkspace.setSharedWorkspaceInstance(this);
            if (o != null) {
                LOG.with().level(Level.WARNING).verb(NLogVerb.SUCCESS)
                        .log(
                                NMsg.ofC("%s workspace set as main instance overriding existing workspace",
                                        NMsg.ofCode("nuts")
                                )
                        );
            } else {
                LOG.with().level(Level.FINE).verb(NLogVerb.SUCCESS)
                        .log(
                                NMsg.ofC("%s workspace set as main instance",
                                        NMsg.ofCode("nuts")
                                )
                        );
            }
        }
        if (CoreNUtils.isCustomFalse("---perf")) {
            if (session.isPlainOut()) {
                session.out().println(NMsg.ofC("%s workspace loaded in %s",
                        NMsg.ofCode("nuts"),
                        getCreationDuration()

                ));
            } else {
                session.eout().add(data.elems.ofObject()
                        .set("workspace-loaded-in",
                                data.elems.ofObject()
                                        .set("ms", this.getCreationDuration().toMillis())
                                        .set("text", CoreTimeUtils.formatPeriodMilli(this.getCreationDuration()))
                                        .build()

                        )
                        .build());
            }
        }
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
        if (effectiveBootOptions.getRecover().orElse(false)) {
            wsModel.configModel.setBootApiVersion(cfg.getApiVersion());
            wsModel.configModel.setBootRuntimeId(cfg.getRuntimeId(),
                    effectiveBootOptions.getRuntimeBootDescriptor().isEmpty() ? "" :
                            NBootHelper.toDependencyList(effectiveBootOptions.getRuntimeBootDescriptor().get().getDependencies()).stream()
                                    .map(NDependency::toString)
                                    .collect(Collectors.joining(";"))
            );
            wsModel.configModel.setBootRepositories(cfg.getBootRepositories());
            try {
                NInstallCmd.of().setInstalled(true).getResult();
            } catch (Exception ex) {
                LOG.with().level(Level.SEVERE).verb(NLogVerb.FAIL)
                        .error(ex)
                        .log(NMsg.ofJ("reinstall artifacts failed : {0}", ex));
            }
        }
        if (getRepositories().isEmpty()) {
            LOG.with().level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofPlain("workspace has no repositories. Will re-create defaults"));
            data.justInstalledArchetype = initializeWorkspace(effectiveBootOptions.getArchetype().orNull());
        }
        List<String> transientRepositoriesSet =
                NCoreCollectionUtils.nonNullList(effectiveBootOptions.getRepositories().orElseGet(Collections::emptyList));
        NRepositoryDB repoDB = NRepositoryDB.of();
        NRepositorySelectorList expected = NRepositorySelectorList.of(transientRepositoriesSet, repoDB).get();
        for (NRepositoryLocation loc : expected.resolve(null, repoDB)) {
            NAddRepositoryOptions d = NRepositorySelectorHelper.createRepositoryOptions(loc, false);
            String n = d.getName();
            String ruuid = (NBlankable.isBlank(n) ? "temporary" : n) + "_" + UUID.randomUUID().toString().replace("-", "");
            d.setName(ruuid);
            d.setTemporary(true);
            d.setEnabled(true);
            d.setFailSafe(false);
            if (d.getConfig() != null) {
                d.getConfig().setName(NBlankable.isBlank(n) ? ruuid : n);
                d.getConfig().setStoreStrategy(NStoreStrategy.STANDALONE);
            }
            addRepository(d);
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
        NWorkspaceUtils.of(this).checkReadOnly();
        LOG.with().level(Level.CONFIG).verb(NLogVerb.SUCCESS)
                .log(NMsg.ofJ("creating {0} workspace at {1}",
                        data.text.ofStyled("new", NTextStyle.info()),
                        this.getWorkspaceLocation()
                ));
        NWorkspaceConfigBoot bconfig = new NWorkspaceConfigBoot();
        //load from config with resolution applied
        bconfig.setUuid(wsModel.uuid);
        NWorkspaceConfigApi aconfig = new NWorkspaceConfigApi();
        aconfig.setApiVersion(this.wsModel.askedApiVersion);
        aconfig.setRuntimeId(this.wsModel.askedRuntimeId);
        aconfig.setJavaCommand(effectiveBootOptions.getJavaCommand().orNull());
        aconfig.setJavaOptions(effectiveBootOptions.getJavaOptions().orNull());

        NWorkspaceConfigRuntime rconfig = new NWorkspaceConfigRuntime();
        rconfig.setDependencies(
                effectiveBootOptions.getRuntimeBootDescriptor().isEmpty() ? "" :
                        NBootHelper.toDependencyList(effectiveBootOptions.getRuntimeBootDescriptor().get().getDependencies()).stream()
                                .map(NDependency::toString)
                                .collect(Collectors.joining(";"))
        );
        rconfig.setId(this.wsModel.askedRuntimeId);

        bconfig.setBootRepositories(data.repositories);
        bconfig.setStoreStrategy(effectiveBootOptions.getStoreStrategy().orNull());
        bconfig.setRepositoryStoreStrategy(effectiveBootOptions.getRepositoryStoreStrategy().orNull());
        bconfig.setStoreLayout(effectiveBootOptions.getStoreLayout().orNull());
        bconfig.setSystem(effectiveBootOptions.getSystem().orElse(false));
        bconfig.setStoreLocations(new NStoreLocationsMap(effectiveBootOptions.getStoreLocations().orNull()).toMapOrNull());
        bconfig.setHomeLocations(new NHomeLocationsMap(effectiveBootOptions.getHomeLocations().orNull()).toMapOrNull());

        boolean namedWorkspace = CoreNUtils.isValidWorkspaceName(effectiveBootOptions.getWorkspace().orNull());
        if (bconfig.getStoreStrategy() == null) {
            bconfig.setStoreStrategy(namedWorkspace ? NStoreStrategy.EXPLODED : NStoreStrategy.STANDALONE);
        }
        if (bconfig.getRepositoryStoreStrategy() == null) {
            bconfig.setRepositoryStoreStrategy(NStoreStrategy.EXPLODED);
        }
        bconfig.setName(CoreNUtils.resolveValidWorkspaceName(effectiveBootOptions.getWorkspace().orNull()));

        wsModel.configModel.setCurrentConfig(new DefaultNWorkspaceCurrentConfig(this)
                .merge(aconfig)
                .merge(bconfig)
                .build(this.getWorkspaceLocation()));
        wsModel.configModel.setConfigBoot(bconfig);
        wsModel.configModel.setConfigApi(aconfig);
        wsModel.configModel.setConfigRuntime(rconfig);
        //load all "---config.*" custom options into persistent config
        for (String customOption : effectiveBootOptions.getCustomOptions().orElseGet(Collections::emptyList)) {
            NArg a = NArg.of(customOption);
            if (a.getKey().asString().get().startsWith("config.")) {
                if (a.isActive()) {
                    this.setConfigProperty(
                            a.getKey().asString().orElse("").substring("config.".length()),
                            a.getStringValue().orNull()
                    );
                }
            }
        }
        data.justInstalledArchetype = initializeWorkspace(effectiveBootOptions.getArchetype().orNull());
        NVersion nutsVersion = getRuntimeId().getVersion();
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().level(Level.CONFIG).verb(NLogVerb.SUCCESS)
                    .log(NMsg.ofJ("nuts workspace v{0} created.", nutsVersion));
        }
        //should install default
        NSession session = currentSession();
        if (session.isPlainTrace() && !this.getBootOptions().getSkipWelcome().orElse(false)) {
            NPrintStream out = session.out();
            out.resetLine();
            StringBuilder version = new StringBuilder(nutsVersion.toString());
            CoreStringUtils.fillString(' ', 25 - version.length(), version);
            NPath p = NPath.of("classpath:/net/thevpc/nuts/runtime/includes/standard-header.ntf", getClass().getClassLoader());
            NText n = data.text.parser().parse(p);
            n = data.text.transform(n, new NTextTransformConfig()
                    .setCurrentDir(p.getParent())
                    .setImportClassLoader(getClass().getClassLoader())
                    .setProcessAll(true)
            );
            out.println(n == null ? "no help found" : n);
            if (NWorkspaceUtils.isUserDefaultWorkspace()) {
                out.println(
                        data.text.ofBuilder()
                                .append("location", NTextStyle.underlined())
                                .append(":")
                                .append(this.getWorkspaceLocation())
                                .append(" ")
                );
            } else {
                out.println(
                        data.text.ofBuilder()
                                .append("location", NTextStyle.underlined())
                                .append(":")
                                .append(this.getWorkspaceLocation())
                                .append(" ")
                                .append(" (")
                                .append(getHashName())
                                .append(")")
                );
            }
            NTableFormat.of()
                    .setValue(
                            NTableModel.of()
                                    .addCell(
                                            data.text.ofBuilder()
                                                    .append(" This is the first time ")
                                                    .appendCode("sh", "nuts")
                                                    .append(" is launched for this workspace ")
                                    )
                    ).println(out);
            out.println();
        }
        wsModel.configModel.installBootIds();
    }

    private void _initLog(InitWorkspaceData data) {
        NLogOp LOGCRF = LOG.with().level(Level.CONFIG).verb(NLogVerb.READ);
        NLogOp LOGCSF = LOG.with().level(Level.CONFIG).verb(NLogVerb.START);
        if (LOG.isLoggable(Level.CONFIG)) {
            NTexts text = data.text;
            NBootOptions effectiveBootOptions = data.effectiveBootOptions;
            NBootOptions userBootOptions = data.initialBootOptions;
            //just log known implementations
            NCmdLines.of();
            NIO.of();
            NVersionFormat.of();
            NIdFormat.of();

            LOGCSF.log(NMsg.ofPlain(" ==============================================================================="));
            String s = NIOUtils.loadString(getClass().getResourceAsStream("/net/thevpc/nuts/runtime/includes/standard-header.ntf"), true);
            s = s.replace("${nuts.workspace-runtime.version}", Nuts.getVersion().toString());
            for (String s1 : s.split("\n")) {
                LOGCSF.log(NMsg.ofNtf(s1));
            }
            LOGCSF.log(NMsg.ofPlain(" "));
            LOGCSF.log(NMsg.ofPlain(" = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ="));
            LOGCSF.log(NMsg.ofPlain(" "));
            LOGCSF.log(NMsg.ofJ("start ```sh nuts``` {0} at {1}", Nuts.getVersion(), CoreNUtils.DEFAULT_DATE_TIME_FORMATTER.format(data.initialBootOptions.getCreationTime().get())));
            LOGCRF.log(NMsg.ofJ("open Nuts Workspace               : {0}",
                    effectiveBootOptions.toCmdLine()
            ));
            LOGCRF.log(NMsg.ofJ("open Nuts Workspace (compact)     : {0}",
                    effectiveBootOptions.toCmdLine(new NWorkspaceOptionsConfig().setCompact(true))));

            LOGCRF.log(NMsg.ofPlain("open Workspace with config        : "));
            LOGCRF.log(NMsg.ofJ("   nuts-workspace-uuid            : {0}", NTextUtils.desc(effectiveBootOptions.getUuid().orNull(), text)));
            LOGCRF.log(NMsg.ofJ("   nuts-workspace-name            : {0}", NTextUtils.desc(effectiveBootOptions.getName().orNull(), text)));
            LOGCRF.log(NMsg.ofJ("   nuts-api-version               : {0}", Nuts.getVersion()));
            LOGCRF.log(NMsg.ofJ("   nuts-api-url                   : {0}", NPath.of(getApiURL())));
            LOGCRF.log(NMsg.ofJ("   nuts-api-digest                : {0}", text.ofStyled(getApiDigest(), NTextStyle.version())));
            LOGCRF.log(NMsg.ofJ("   nuts-boot-repositories         : {0}", NTextUtils.desc(effectiveBootOptions.getBootRepositories().orNull(), text)));
            LOGCRF.log(NMsg.ofJ("   nuts-runtime                   : {0}", getRuntimeId()));
            LOGCRF.log(NMsg.ofJ("   nuts-runtime-digest            : {0}",
                    text.ofStyled(new CoreDigestHelper().append(effectiveBootOptions.getClassWorldURLs().orNull()).getDigest(), NTextStyle.version())
            ));
            if (effectiveBootOptions.getRuntimeBootDescriptor().isPresent()) {
                LOGCRF.log(NMsg.ofJ("   nuts-runtime-dependencies      : {0}",
                        text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                                effectiveBootOptions.getRuntimeBootDescriptor().get().getDependencies().stream()
                                        .map(x -> NId.get(x.toString()).get())
                                        .collect(Collectors.toList())
                        )
                ));
            }
            LOGCRF.log(NMsg.ofJ("   nuts-runtime-urls              : {0}",
                    text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                            effectiveBootOptions.getClassWorldURLs().get().stream()
                                    .map(x -> NPath.of(x.toString()))
                                    .collect(Collectors.toList())
                    )
            ));
            LOGCRF.log(NMsg.ofJ("   nuts-extension-dependencies    : {0}",
                    text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                            toIds(
                                    NBootHelper.toDescriptorList(effectiveBootOptions.getExtensionBootDescriptors().orElseGet(Collections::emptyList))
                            ).stream()
                                    .map(x
                                            -> NId.get(x.toString()).get()
                                    )
                                    .collect(Collectors.toList())
                    )
            ));
            LOGCRF.log(NMsg.ofJ("   nuts-workspace                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getWorkspace().orNull(), effectiveBootOptions.getWorkspace().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-hash-name                 : {0}", getHashName()));
            LOGCRF.log(NMsg.ofJ("   nuts-store-bin                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.BIN).orNull(), effectiveBootOptions.getStoreType(NStoreType.BIN).orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-conf                : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.CONF).orNull(), effectiveBootOptions.getStoreType(NStoreType.CONF).orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-var                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.VAR).orNull(), effectiveBootOptions.getStoreType(NStoreType.VAR).orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-log                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.LOG).orNull(), effectiveBootOptions.getStoreType(NStoreType.LOG).orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-temp                : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.TEMP).orNull(), effectiveBootOptions.getStoreType(NStoreType.TEMP).orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-cache               : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.CACHE).orNull(), effectiveBootOptions.getStoreType(NStoreType.CACHE).orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-run                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.RUN).orNull(), effectiveBootOptions.getStoreType(NStoreType.RUN).orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-lib                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreType(NStoreType.LIB).orNull(), effectiveBootOptions.getStoreType(NStoreType.LIB).orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-strategy            : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreStrategy().orNull(), effectiveBootOptions.getStoreStrategy().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-repos-store-strategy      : {0}", NTextUtils.formatLogValue(text, userBootOptions.getRepositoryStoreStrategy().orNull(), effectiveBootOptions.getRepositoryStoreStrategy().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-store-layout              : {0}", NTextUtils.formatLogValue(text, userBootOptions.getStoreLayout().orNull(), effectiveBootOptions.getStoreLayout().isNotPresent() ? "system" : effectiveBootOptions.getStoreLayout().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-username                  : {0}", NTextUtils.formatLogValue(text, userBootOptions.getUserName().orNull(), effectiveBootOptions.getUserName().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-read-only                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getReadOnly().orNull(), effectiveBootOptions.getReadOnly().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-trace                     : {0}", NTextUtils.formatLogValue(text, userBootOptions.getTrace().orNull(), effectiveBootOptions.getTrace().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-progress                  : {0}", NTextUtils.formatLogValue(text, userBootOptions.getProgressOptions().orNull(), effectiveBootOptions.getProgressOptions().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-bot                       : {0}", NTextUtils.formatLogValue(text, userBootOptions.getBot().orNull(), effectiveBootOptions.getBot().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-cached                    : {0}", NTextUtils.formatLogValue(text, userBootOptions.getCached().orNull(), effectiveBootOptions.getCached().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-transitive                : {0}", NTextUtils.formatLogValue(text, userBootOptions.getTransitive().orNull(), effectiveBootOptions.getTransitive().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-confirm                   : {0}", NTextUtils.formatLogValue(text, userBootOptions.getConfirm().orNull(), effectiveBootOptions.getConfirm().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-debug                     : {0}", NTextUtils.formatLogValue(text, userBootOptions.getDebug().orNull(), effectiveBootOptions.getDebug().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-dry                       : {0}", NTextUtils.formatLogValue(text, userBootOptions.getDry().orNull(), effectiveBootOptions.getDry().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-execution-type            : {0}", NTextUtils.formatLogValue(text, userBootOptions.getExecutionType().orNull(), effectiveBootOptions.getExecutionType().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-out-line-prefix           : {0}", NTextUtils.formatLogValue(text, userBootOptions.getOutLinePrefix().orNull(), effectiveBootOptions.getOutLinePrefix().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-err-line-prefix           : {0}", NTextUtils.formatLogValue(text, userBootOptions.getErrLinePrefix().orNull(), effectiveBootOptions.getErrLinePrefix().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-init-platforms            : {0}", NTextUtils.formatLogValue(text, userBootOptions.getInitPlatforms().orNull(), effectiveBootOptions.getInitPlatforms().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-init-java                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getInitJava().orNull(), effectiveBootOptions.getInitJava().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-init-launchers            : {0}", NTextUtils.formatLogValue(text, userBootOptions.getInitLaunchers().orNull(), effectiveBootOptions.getInitLaunchers().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-init-scripts              : {0}", NTextUtils.formatLogValue(text, userBootOptions.getInitScripts().orNull(), effectiveBootOptions.getInitScripts().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-init-scripts              : {0}", NTextUtils.formatLogValue(text, userBootOptions.getInitScripts().orNull(), effectiveBootOptions.getInitScripts().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-desktop-launcher          : {0}", NTextUtils.formatLogValue(text, userBootOptions.getDesktopLauncher().orNull(), effectiveBootOptions.getDesktopLauncher().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-menu-launcher             : {0}", NTextUtils.formatLogValue(text, userBootOptions.getMenuLauncher().orNull(), effectiveBootOptions.getMenuLauncher().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-user-launcher             : {0}", NTextUtils.formatLogValue(text, userBootOptions.getUserLauncher().orNull(), effectiveBootOptions.getUserLauncher().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-isolation-level           : {0}", NTextUtils.formatLogValue(text, userBootOptions.getIsolationLevel().orNull(), effectiveBootOptions.getIsolationLevel().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-open-mode                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getOpenMode().orNull(), effectiveBootOptions.getOpenMode().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-inherited                 : {0}", NTextUtils.formatLogValue(text, userBootOptions.getInherited().orNull(), effectiveBootOptions.getInherited().orNull())));
            LOGCRF.log(NMsg.ofJ("   nuts-inherited-nuts-boot-args  : {0}", System.getProperty("nuts.boot.args") == null ? NTextUtils.desc(null, text)
                    : NTextUtils.desc(NCmdLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH), text)
            ));
            LOGCRF.log(NMsg.ofJ("   nuts-inherited-nuts-args       : {0}", System.getProperty("nuts.args") == null ? NTextUtils.desc(null, text)
                    : NTextUtils.desc(text.of(NCmdLine.of(System.getProperty("nuts.args"), NShellFamily.SH)), text)
            ));
            LOGCRF.log(NMsg.ofJ("   nuts-open-mode                 : {0}", NTextUtils.formatLogValue(text, effectiveBootOptions.getOpenMode().orNull(), effectiveBootOptions.getOpenMode().orElse(NOpenMode.OPEN_OR_CREATE))));
            NWorkspace senvs = this;
            LOGCRF.log(NMsg.ofJ("   java-home                      : {0}", System.getProperty("java.home")));
            LOGCRF.log(NMsg.ofJ("   java-classpath                 : {0}", System.getProperty("java.class.path")));
            LOGCRF.log(NMsg.ofJ("   java-library-path              : {0}", System.getProperty("java.library.path")));
            LOGCRF.log(NMsg.ofJ("   os-name                        : {0}", System.getProperty("os.name")));
            LOGCRF.log(NMsg.ofJ("   os-family                      : {0}", senvs.getOsFamily()));
            LOGCRF.log(NMsg.ofJ("   os-dist                        : {0}", senvs.getOsDist().getArtifactId()));
            LOGCRF.log(NMsg.ofJ("   os-arch                        : {0}", System.getProperty("os.arch")));
            LOGCRF.log(NMsg.ofJ("   os-shell                       : {0}", senvs.getShellFamily()));
            LOGCRF.log(NMsg.ofJ("   os-shells                      : {0}", text.ofBuilder().appendJoined(",", senvs.getShellFamilies())));
            NWorkspaceTerminalOptions b = getModel().bootModel.getBootTerminal();
            LOGCRF.log(NMsg.ofJ("   os-terminal-flags              : {0}", String.join(", ", b.getFlags())));
            NTerminalMode terminalMode = wsModel.bootModel.getBootUserOptions().getTerminalMode().orElse(NTerminalMode.DEFAULT);
            LOGCRF.log(NMsg.ofJ("   os-terminal-mode               : {0}", terminalMode));
            LOGCRF.log(NMsg.ofJ("   os-desktop                     : {0}", senvs.getDesktopEnvironment()));
            LOGCRF.log(NMsg.ofJ("   os-desktop-family              : {0}", senvs.getDesktopEnvironmentFamily()));
            LOGCRF.log(NMsg.ofJ("   os-desktops                    : {0}", text.ofBuilder().appendJoined(",", (senvs.getDesktopEnvironments()))));
            LOGCRF.log(NMsg.ofJ("   os-desktop-families            : {0}", text.ofBuilder().appendJoined(",", (senvs.getDesktopEnvironmentFamilies()))));
            LOGCRF.log(NMsg.ofJ("   os-desktop-path                : {0}", senvs.getDesktopPath()));
            LOGCRF.log(NMsg.ofJ("   os-desktop-integration         : {0}", senvs.getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP)));
            LOGCRF.log(NMsg.ofJ("   os-menu-integration            : {0}", senvs.getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU)));
            LOGCRF.log(NMsg.ofJ("   os-shortcut-integration        : {0}", senvs.getDesktopIntegrationSupport(NDesktopIntegrationItem.USER)));
            LOGCRF.log(NMsg.ofJ("   os-version                     : {0}", senvs.getOsDist().getVersion()));
            LOGCRF.log(NMsg.ofJ("   os-username                    : {0}", System.getProperty("user.name")));
            LOGCRF.log(NMsg.ofJ("   os-user-dir                    : {0}", NPath.of(System.getProperty("user.dir"))));
            LOGCRF.log(NMsg.ofJ("   os-user-home                   : {0}", NPath.of(System.getProperty("user.home"))));
            LOGCRF.log(NMsg.ofJ("   os-user-locale                 : {0}", Locale.getDefault()));
            LOGCRF.log(NMsg.ofJ("   os-user-time-zone              : {0}", TimeZone.getDefault()));
        }

    }

    private void displayRecommendations(Object r) {
        Map<String, Object> a = new HashMap<>();
        a.put("recommendations", r);
        NSession.of().out().println(a);
    }

    private URL getApiURL() {
        NId nid = NId.getApi(Nuts.getVersion()).get();
        return ExtraApiUtils.findClassLoaderJar(nid, NClassLoaderUtils.resolveClasspathURLs(Thread.currentThread().getContextClassLoader()));
    }

    private String getApiDigest() {
        if (NBlankable.isBlank(wsModel.apiDigest)) {
            wsModel.apiDigest = new CoreDigestHelper().append(getApiURL()).getDigest();
        }
        return wsModel.apiDigest;
    }

    protected NDescriptor _applyParentDescriptors(NDescriptor descriptor) {
        List<NId> parents = descriptor.getParents();
        List<NDescriptor> parentDescriptors = new ArrayList<>();
        for (NId parent : parents) {
            parentDescriptors.add(
                    _applyParentDescriptors(
                            NFetchCmd.of(parent).getResultDescriptor()
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

    protected NDescriptor _resolveEffectiveDescriptor(NDescriptor descriptor) {
        LOG.with().level(Level.FINEST).verb(NLogVerb.START)
                .log(NMsg.ofJ("resolve effective {0}", descriptor.getId()));
        NDescriptorBuilder descrWithParents = _applyParentDescriptors(descriptor).builder();
        //now apply conditions!
        List<NDescriptorProperty> properties = descrWithParents.getProperties().stream().filter(x -> CoreFilterUtils.acceptCondition(
                x.getCondition(), false)).collect(Collectors.toList());
        if (!properties.isEmpty()) {
            DefaultNProperties pp = new DefaultNProperties();
            List<NDescriptorProperty> n = new ArrayList<>();
            pp.addAll(properties);
            for (String s : pp.keySet()) {
                NDescriptorProperty[] a = pp.getAll(s);
                if (a.length == 1) {
                    n.add(a[0].builder().setCondition(null).build());
                } else {
                    NDescriptorProperty z = null;
                    for (NDescriptorProperty zz : a) {
                        if (z == null) {
                            z = zz;
                            boolean wasZZ = (zz.getCondition() == null || zz.getCondition().isBlank());
                            if (!wasZZ) {
                                break; //match first condition!
                            }
                        } else {
                            boolean wasZ = (z.getCondition() == null || z.getCondition().isBlank());
                            boolean wasZZ = (zz.getCondition() == null || zz.getCondition().isBlank());
                            if (wasZ == wasZZ || !wasZ) {
                                z = zz;
                            }
                            if (!wasZZ) {
                                break; //match first condition!
                            }
                        }
                    }
                    if (z != null) {
                        n.add(z.builder().setCondition(null).build());
                    }
                }
            }
            properties = n;
        }

        descrWithParents.setProperties(properties);

        NDescriptor effectiveDescriptor = NDescriptorUtils.applyProperties(descrWithParents).build();
        List<NDependency> oldDependencies = new ArrayList<>();
        for (NDependency d : effectiveDescriptor.getDependencies()) {
            if (CoreFilterUtils.acceptDependency(d)) {
                oldDependencies.add(d.builder().setCondition(null).build());
            }
        }

        List<NDependency> newDeps = new ArrayList<>();
        boolean someChange = false;
        LinkedHashSet<NDependency> effStandardDeps = new LinkedHashSet<>();
        for (NDependency standardDependency : effectiveDescriptor.getStandardDependencies()) {
            if ("import".equals(standardDependency.getScope())) {
                NDescriptor dd = NFetchCmd.of(standardDependency.toId()).setEffective(true).getResultDescriptor();
                for (NDependency dependency : dd.getStandardDependencies()) {
                    if (CoreFilterUtils.acceptDependency(dependency)) {
                        effStandardDeps.add(dependency);
                    }
                }
            } else {
                if (CoreFilterUtils.acceptDependency(standardDependency)) {
                    effStandardDeps.add(standardDependency);
                }
            }
        }
        for (NDependency d : oldDependencies) {
            if (NBlankable.isBlank(d.getScope())
                    || d.getVersion().isBlank()
                    || NBlankable.isBlank(d.getOptional())) {
                NDependency standardDependencyOk = null;
                for (NDependency standardDependency : effStandardDeps) {
                    if (standardDependency.getSimpleName().equals(d.toId().getShortName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (NBlankable.isBlank(d.getScope())
                            && !NBlankable.isBlank(standardDependencyOk.getScope())) {
                        someChange = true;
                        d = d.builder().setScope(standardDependencyOk.getScope()).build();
                    }
                    if (NBlankable.isBlank(d.getOptional())
                            && !NBlankable.isBlank(standardDependencyOk.getOptional())) {
                        someChange = true;
                        d = d.builder().setOptional(standardDependencyOk.getOptional()).build();
                    }
                    if (d.getVersion().isBlank()
                            && !standardDependencyOk.getVersion().isBlank()) {
                        someChange = true;
                        d = d.builder().setVersion(standardDependencyOk.getVersion()).build();
                    }
                }
                if (d.getVersion().isBlank()) {
                    LOG.with().level(Level.FINE).verb(NLogVerb.FAIL)
                            .log(NMsg.ofJ("failed to resolve effective version for {0}", d));
                }
            }

            if ("import".equals(d.getScope())) {
                someChange = true;
                newDeps.addAll(NFetchCmd.of(d.toId()).setEffective(true).getResultDescriptor().getDependencies());
            } else {
                newDeps.add(d);
            }
        }
        effectiveDescriptor = effectiveDescriptor.builder().setDependencies(newDeps).build();
        return effectiveDescriptor;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NConstants.Support.DEFAULT_SUPPORT;
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
        NWorkspaceArchetypeComponent instance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NWorkspaceArchetypeComponent ac : wsModel.extensions.createComponents(NWorkspaceArchetypeComponent.class, archetype)) {
            if (archetype.equals(ac.getName())) {
                instance = ac;
                break;
            }
            validValues.add(ac.getName());
        }
        if (instance == null) {
            //get the default implementation
            throw new NException(
                    NMsg.ofC("invalid archetype %s. Valid values are : %s", archetype, validValues)
            );
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        NWorkspaceSecurityManager.of().updateUser(NConstants.Users.ADMIN).setCredentials("admin".toCharArray()).run();

        instance.initializeWorkspace();
        NWorkspace envs = this;
        if (!envs.isReadOnly()) {
            envs.saveConfig();
        }
        return instance;
    }

    private void checkSession(NSession session) {
        NSessionUtils.checkSession(this, session);
    }

    private NId resolveApiId(NId id, Set<NId> visited, NSession session) {
        if (visited.contains(id.getLongId())) {
            return null;
        }
        visited.add(id.getLongId());
        if (NId.getApi("").get().equalsShortId(id)) {
            return id;
        }
        for (NDependency dependency : NFetchCmd.of(id).getResultDescriptor().getDependencies()) {
            NId q = resolveApiId(dependency.toId(), visited, session);
            if (q != null) {
                return q;
            }
        }
        return null;
    }

    public void installOrUpdateImpl(NDefinition def, String[] args, boolean resolveInstaller, boolean updateDefaultVersion, InstallStrategy0 strategy0, boolean requireDependencies, NId[] forIds, NDependencyScope scope) {
        if (def == null) {
            return;
        }
        boolean requireParents = true;
        NInstallerComponent installerComponent = null;
        try {
            Map rec = null;
            if (strategy0 == InstallStrategy0.INSTALL) {
                rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString()), NRecommendationPhase.INSTALL, false);
            } else if (strategy0 == InstallStrategy0.UPDATE) {
                rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString()), NRecommendationPhase.UPDATE, false);
            } else {
                //just ignore any dependencies. recommendations are related to main artifacts
            }
            //TODO: should check here for any security issue!
        } catch (Exception ex2) {
            //just ignore
        }
        NSession session = wsModel.workspace.currentSession();
        NPrintStream out = session.out();
        NInstallInformation newNInstallInformation = null;
        boolean remoteRepo = true;
        try {
            NDependencyFilter ndf = NDependencyFilters.of().byRunnable();
            if (def.getEffectiveDescriptor().isNotPresent()
                    || (!def.getDescriptor().isNoContent() && def.getContent().isNotPresent())) {
                // reload def
                NFetchCmd fetch2 = NFetchCmd.of(def.getId())
                        .content()
                        .setRepositoryFilter(NRepositoryFilters.of().installedRepo())
                        .failFast();
                if (def.getDependencies().isPresent()) {
                    fetch2.setDependencyFilter(def.getDependencies().get().filter());
                    fetch2.dependencies();
                }
                def = fetch2.getResultDefinition();
            }

            boolean reinstall = false;
            NInstalledRepository installedRepository = getInstalledRepository();
            NWorkspaceUtils wu = NWorkspaceUtils.of(wsModel.workspace);

            if (session.isPlainTrace()) {
                NTexts text = NTexts.of();
                if (strategy0 == InstallStrategy0.UPDATE) {
                    session.out().resetLine().println(NMsg.ofC("%s %s ...",
                            text.ofStyled("update", NTextStyle.warn()),
                            def.getId().getLongId()
                    ));
                } else if (strategy0 == InstallStrategy0.REQUIRE) {
                    reinstall = def.getInstallInformation().get().getInstallStatus().isRequired();
                    if (reinstall) {
                        //session.out().println("re-requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                    } else {
                        //session.out().println("requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                    }
                } else {
                    reinstall = def.getInstallInformation().get().getInstallStatus().isInstalled();
                    if (reinstall) {
                        session.out().resetLine().println(NMsg.ofC(
                                "%s %s ...",
                                text.ofStyled("re-install", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined())),
                                def.getId().getLongId()
                        ));
                    } else {
                        session.out().resetLine().println(NMsg.ofC("%s %s ...",
                                text.ofStyled("install", NTextStyle.success()),
                                def.getId().getLongId()
                        ));
                    }
                }
            }
            NRepositorySPI installedRepositorySPI = wu.repoSPI(installedRepository);
            if (resolveInstaller) {
                installerComponent = getInstaller(def);
            }
            if (reinstall) {
                uninstallImpl(def, new String[0], resolveInstaller, true, false, false);
                //must re-fetch def!
                NDefinition d2 = NFetchCmd.of(def.getId())
                        .setContent(true)
                        .setEffective(true)
                        .setDependencies(true)
                        .setFailFast(false)
                        .setOptional(false)
                        .addScope(NDependencyScopePattern.RUN)
                        .setDependencyFilter(NDependencyFilters.of().byRunnable())
                        .getResultDefinition();
                if (d2 == null) {
                    // perhaps the version does no more exist
                    // search latest!
                    d2 = NSearchCmd.of().setId(def.getId().getShortId())
                            .effective()
                            .failFast()
                            .latest()
                            .setOptional(false)
                            .addScope(NDependencyScopePattern.RUN)
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .getResultDefinitions().findFirst().get();
                }
                def = d2;
            }
//        checkSession(session);
            NDefinition oldDef = null;
            if (strategy0 == InstallStrategy0.UPDATE) {
                switch (def.getDescriptor().getIdType()) {
                    case API: {
                        oldDef = NFetchCmd.of(
                                        NId.getApi(Nuts.getVersion()).get())
                                .setFetchStrategy(NFetchStrategy.ONLINE)
                                .setFailFast(false).getResultDefinition();
                        break;
                    }
                    case RUNTIME: {
                        oldDef = NFetchCmd.of(
                                        getRuntimeId())
                                .setFetchStrategy(NFetchStrategy.ONLINE)
                                .setFailFast(false).getResultDefinition();
                        break;
                    }
                    default: {
                        oldDef = NSearchCmd.of().addId(def.getId().getShortId())
                                .setInstallStatus(NInstallStatusFilters.of().byDeployed(true))
                                .setFailFast(false).getResultDefinitions()
                                .findFirst().orNull();
                        break;
                    }
                }
            }
            out.flush();
            NWorkspace envs = this;
            if (def.getContent().isPresent() || def.getDescriptor().isNoContent()) {
                if (requireParents) {
                    List<NDefinition> requiredDefinitions = new ArrayList<>();
                    for (NId parent : def.getDescriptor().getParents()) {
                        if (!installedRepositorySPI.
                                searchVersions().setId(parent)
                                .setFetchMode(NFetchMode.LOCAL)
                                .getResult()
                                .hasNext()) {
                            NDefinition dd = NSearchCmd.of().addId(parent).setLatest(true)
                                    .setEffective(true)
                                    .getResultDefinitions()
                                    .findFirst().orNull();
                            if (dd != null) {
                                requiredDefinitions.add(dd);
                            }
                        }
                    }
                    //install required
                    for (NDefinition dd : requiredDefinitions) {
                        requireImpl(dd,
                                false, new NId[]{def.getId()}
                                //transitive dependencies already evaluated
                        );
                    }
                }
                if (requireDependencies) {
                    def.getDependencies().get();
                    List<NDefinition> requiredDefinitions = new ArrayList<>();
                    //fetch required
                    for (NDependency dependency : def.getDependencies().get()) {
                        if (ndf == null || ndf.acceptDependency(def.getId(), dependency)) {
                            if (!installedRepositorySPI.
                                    searchVersions().setId(dependency.toId())
                                    .setFetchMode(NFetchMode.LOCAL)
                                    .getResult()
                                    .hasNext()) {
                                NDefinition dd = NSearchCmd.of().addId(dependency.toId()).setContent(true).setLatest(true)
                                        //.setDependencies(true)
                                        .setEffective(true)
                                        .getResultDefinitions()
                                        .findFirst().orNull();
                                if (dd != null) {
                                    if (dd.getContent().isNotPresent()) {
                                        throw new NInstallException(def.getId(),
                                                NMsg.ofC("unable to install %s. required dependency content is missing for %s", def.getId(), dependency.toId()),
                                                null);
                                    }
                                    requiredDefinitions.add(dd);
                                }
                            }
                        }
                    }
                    //install required
                    for (NDefinition dd : requiredDefinitions) {
                        requireImpl(dd,
                                false, new NId[]{def.getId()}
                                //transitive dependencies already evaluated
                        );
                    }
                }

                //should change def to reflect install location!
                NExecutionContextBuilder cc = createExecutionContext()
                        .setDefinition(def).setArguments(args).failFast().setTemporary(false)
                        .setRunAs(NRunAs.currentUser())// install or update always uses current user
                        ;
                NArtifactCall installer = def.getDescriptor().getInstaller();
                if (installer != null) {
                    cc.addExecutorOptions(installer.getArguments());
                }
                cc.setWorkspace(cc.getWorkspace());
                NExecutionContext executionContext = cc.build();

                if (strategy0 == InstallStrategy0.REQUIRE) {
                    newNInstallInformation = installedRepository.require(executionContext.getDefinition(), true, forIds, scope);
                } else if (strategy0 == InstallStrategy0.UPDATE) {
                    newNInstallInformation = installedRepository.install(executionContext.getDefinition());
                } else if (strategy0 == InstallStrategy0.INSTALL) {
                    newNInstallInformation = installedRepository.install(executionContext.getDefinition());
                }
                if (updateDefaultVersion) {
                    installedRepository.setDefaultVersion(def.getId());
                }

                //now should reload definition
                NFetchCmd fetch2 = NFetchCmd.of(executionContext.getDefinition().getId())
                        .content()
                        .setRepositoryFilter(NRepositoryFilters.of().installedRepo())
                        .failFast();
                if (def.getDependencies().isPresent()) {
                    fetch2.setDependencyFilter(def.getDependencies().get().filter());
                    fetch2.dependencies();
                }
                NDefinition def2 = fetch2
                        .getResultDefinition();

                //update definition in the execution context
                cc.setDefinition(def2);
                executionContext = cc.build();
                NRepository rep = findRepository(def.getRepositoryUuid()).orNull();
                remoteRepo = rep == null || rep.isRemote();
                if (strategy0 == InstallStrategy0.REQUIRE) {
                    //
                } else if (strategy0 == InstallStrategy0.UPDATE) {
                    if (installerComponent != null) {
                        try {
                            installerComponent.update(executionContext);
                        } catch (NReadOnlyException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s ```error failed``` to update : %s.", def.getId(), ex));
                            }
                            throw new NExecutionException(
                                    NMsg.ofC("unable to update %s", def.getId()),
                                    ex);
                        }
                    }
                } else if (strategy0 == InstallStrategy0.INSTALL) {
                    if (installerComponent != null) {
                        try {
                            installerComponent.install(executionContext);
                        } catch (NReadOnlyException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("```error error: failed to install``` %s: %s.", def.getId(), ex));
                            }
                            try {
                                installedRepository.uninstall(executionContext.getDefinition());
                            } catch (Exception ex2) {
                                LOG.with().level(Level.FINE).error(ex)
                                        .log(NMsg.ofJ("failed to uninstall  {0}", executionContext.getDefinition().getId()));
                                //ignore if we could not uninstall
                                try {
                                    Map rec = null;
                                    if (strategy0 == InstallStrategy0.INSTALL) {
                                        rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ex2), NRecommendationPhase.UPDATE, true);
                                    } else if (strategy0 == InstallStrategy0.UPDATE) {
                                        rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ex2), NRecommendationPhase.UPDATE, true);
                                    } else {
                                        //just ignore any dependencies. recommendations are related to main artifacts
                                    }
                                    //TODO: should check here for any security issue!
                                } catch (Exception ex3) {
                                    //just ignore
                                }
                            }
                            throw new NExecutionException(NMsg.ofC("unable to install %s", def.getId()), ex);
                        }
                    }
                }
            } else {
                throw new NExecutionException(
                        NMsg.ofC("unable to install %s: unable to locate content", def.getId()),
                        NExecutionException.ERROR_2);
            }

            NId forId = (forIds == null || forIds.length == 0) ? null : forIds[0];
            switch (def.getDescriptor().getIdType()) {
                case API: {
                    wsModel.configModel.prepareBootClassPathConf(NIdType.API, def.getId(),
                            forId
                            , null, true, false);
                    break;
                }
                case RUNTIME:
                case EXTENSION: {
                    wsModel.configModel.prepareBootClassPathConf(
                            def.getDescriptor().getIdType(),
                            def.getId(),
                            forId
                            , null, true, true);
                    break;
                }
            }

            if (strategy0 == InstallStrategy0.UPDATE) {
                wu.events().fireOnUpdate(new DefaultNUpdateEvent(oldDef, def, session, reinstall));
            } else if (strategy0 == InstallStrategy0.REQUIRE) {
                wu.events().fireOnRequire(new DefaultNInstallEvent(def, session, forIds, reinstall));
            } else if (strategy0 == InstallStrategy0.INSTALL) {
                wu.events().fireOnInstall(new DefaultNInstallEvent(def, session, new NId[0], reinstall));
            }

            if (def.getDescriptor().getIdType() == NIdType.EXTENSION) {
                NExtensionListHelper h = new NExtensionListHelper(
                        session.getWorkspace().getApiId(),
                        this.getConfigModel().getStoredConfigBoot().getExtensions())
                        .save();
                NDependencies nDependencies = null;
                if (!def.getDependencies().isPresent()) {
                    nDependencies = NFetchCmd.of(def.getId()).setDependencies(true)
                            .getResultDefinition().getDependencies().get();
                } else {
                    nDependencies = def.getDependencies().get();
                }
                h.add(def.getId(), nDependencies.transitiveWithSource().toList());
                this.getConfigModel().getStoredConfigBoot().setExtensions(h.getConfs());
                this.getConfigModel().fireConfigurationChanged("extensions", ConfigEventType.BOOT);
            }
        } catch (RuntimeException ex) {
            try {
                Map rec = null;
                if (strategy0 == InstallStrategy0.INSTALL) {
                    rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ex), NRecommendationPhase.INSTALL, true);
                } else if (strategy0 == InstallStrategy0.UPDATE) {
                    rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ex), NRecommendationPhase.UPDATE, true);
                } else {
                    //just ignore any dependencies. recommendations are related to main artifacts
                }
                //TODO: should check here for any recommendations to process
            } catch (Exception ex2) {
                //just ignore
            }
            throw ex;
        }
        if (session.isPlainTrace()) {
            String setAsDefaultString = "";
            NTexts text = NTexts.of();
            if (updateDefaultVersion) {
                setAsDefaultString = " set as " + text.ofBuilder().append("default", NTextStyle.primary1()) + ".";
            }
            if (newNInstallInformation != null
                    && (newNInstallInformation.isJustInstalled()
                    || newNInstallInformation.isJustRequired())) {
                NText installedString = null;
                if (newNInstallInformation != null) {
                    if (newNInstallInformation.isJustReInstalled()) {
                        installedString = text.ofStyled("re-install", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined()));
                    } else if (newNInstallInformation.isJustInstalled()) {
                        installedString = text.ofStyled("install", NTextStyle.success());
                    } else if (newNInstallInformation.isJustReRequired()) {
                        installedString = text.ofStyled("re-require", NTextStyles.of(NTextStyle.info(), NTextStyle.underlined()));
                    } else if (newNInstallInformation.isJustRequired()) {
                        installedString = text.ofStyled("require", NTextStyle.info());
                    }
                }
                if (installedString != null) {
                    //(reinstalled ? "re-installed" : "installed")
                    if (def.getContent().isNotPresent()) {
                        //this happens when deploying a 'pom' artifact
                        if (session.isPlainTrace()) {
                            out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s).%s",
                                    installedString,
                                    def.getId().getLongId(),
                                    remoteRepo ? "remote" : "local",
                                    def.getRepositoryName(),
                                    text.of(setAsDefaultString)
                            ));
                        }
                    } else if (!def.getContent().get().isUserCache()) {
                        if (def.getContent().get().isUserTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s) temporarily file %s.%s",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getContent().orNull(),
                                        text.of(setAsDefaultString)
                                ));
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s).%s", installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        text.of(setAsDefaultString)));
                            }
                        }
                    } else {
                        if (def.getContent().get().isUserTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s) temporarily file %s.%s",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getContent().orNull(),
                                        text.of(setAsDefaultString)));
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s).%s",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        text.of(setAsDefaultString)
                                ));
                            }
                        }
                    }
                }
            } else {
                String installedString = null;
                if (newNInstallInformation != null) {
                    if (newNInstallInformation.isJustReInstalled()) {
                        installedString = "re-installed";
                    } else if (newNInstallInformation.isJustInstalled()) {
                        installedString = "installed";
                    } else if (newNInstallInformation.isJustReRequired()) {
                        installedString = "re-required";
                    } else if (newNInstallInformation.isJustRequired()) {
                        installedString = "required";
                    }
                }
                if (installedString != null) {
                    if (session.isPlainTrace()) {
                        out.resetLine().println(NMsg.ofC("%s  %s %s.%s",
                                installedString,
                                def.getId().getLongId(),
                                text.ofStyled("successfully", NTextStyle.success()),
                                text.of(setAsDefaultString)
                        ));
                    }
                }
            }
        }
    }

    public String resolveCommandName(NId id) {
        String nn = id.getArtifactId();
        NWorkspace aliases = this;
        NCustomCmd c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreFilterUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getArtifactId() + "-" + id.getVersion();
        c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreFilterUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroupId() + "." + id.getArtifactId() + "-" + id.getVersion();
        c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreFilterUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
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
            for (NId extensionId : wsModel.extensions.getConfigExtensions()) {
                if (wsModel.extensionModel.isExcludedExtension(extensionId)) {
                    continue;
                }
                wsModel.extensionModel.wireExtension(extensionId,
                        NFetchCmd.of()
                );
            }
            NUserConfig adminSecurity = getConfigModel()
                    .getUser(NConstants.Users.ADMIN);
            if (adminSecurity == null || NBlankable.isBlank(adminSecurity.getCredentials())) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().level(Level.CONFIG).verb(NLogVerb.FAIL)
                            .log(NMsg.ofJ("{0} user has no credentials. reset to default", NConstants.Users.ADMIN));
                }
                NWorkspaceSecurityManager.of()
                        .updateUser(NConstants.Users.ADMIN).credentials("admin".toCharArray())
                        .run();
            }
            for (NCommandFactoryConfig commandFactory : this.getCommandFactories()) {
                try {
                    this.addCommandFactory(commandFactory);
                } catch (Exception e) {
                    LOG.with().level(Level.SEVERE).verb(NLogVerb.FAIL)
                            .log(NMsg.ofJ("unable to instantiate Command Factory {0}", commandFactory));
                }
            }
            DefaultNWorkspaceEvent workspaceReloadedEvent = new DefaultNWorkspaceEvent(currentSession(), null, null, null, null);
            for (NWorkspaceListener listener : getWorkspaceListeners()) {
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
            n = txt.transform(n, new NTextTransformConfig().setProcessAll(true)
                    .setImportClassLoader(getClass().getClassLoader())
                    .setCurrentDir(p.getParent()));
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
                    .setProcessAll(true)
                    .setRootLevel(1));
            return (n == null ? txt.ofStyled("no help found", NTextStyle.error()) : n);
        });
    }

    @Override
    public NText resolveDefaultHelp(Class<?> clazz) {
        return callWith(() -> {
            NId nutsId = NId.getForClass(clazz).orNull();
            if (nutsId != null) {
                NPath urlPath = NPath.of("classpath:/" + ExtraApiUtils.resolveIdPath(nutsId.getShortId()) + ".ntf", clazz == null ? null : clazz.getClassLoader());
                NTexts txt = NTexts.of();
                NText n = txt.parser().parse(urlPath);
                n = txt.transform(n, new NTextTransformConfig()
                        .setProcessAll(true)
                        .setImportClassLoader(clazz == null ? null : clazz.getClassLoader())
                        .setCurrentDir(urlPath.getParent())
                        .setRootLevel(1));
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
            throw new NNotFoundException(null);
        }
        NId thisId = descriptor.getId();
        String a = thisId.getArtifactId();
        String g = thisId.getGroupId();
        String v = thisId.getVersion().getValue();
        if ((NBlankable.isBlank(g)) || (NBlankable.isBlank(v))) {
            List<NId> parents = descriptor.getParents();
            for (NId parent : parents) {
                NId p = NFetchCmd.of(parent).setEffective(true).getResultId();
                if (NBlankable.isBlank(g)) {
                    g = p.getGroupId();
                }
                if (NBlankable.isBlank(v)) {
                    v = p.getVersion().getValue();
                }
                if (!NBlankable.isBlank(g) && !NBlankable.isBlank(v)) {
                    break;
                }
            }
            if (NBlankable.isBlank(g) || NBlankable.isBlank(v)) {
                throw new NNotFoundException(thisId,
                        NMsg.ofC("unable to fetchEffective for %s. best Result is %s", thisId, thisId),
                        null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v) || CoreStringUtils.containsVars(a)) {
            Map<String, String> p = NDescriptorUtils.getPropertiesMap(descriptor.getProperties());
            NId bestId = NIdBuilder.of(g, thisId.getArtifactId()).setVersion(v).build();
            bestId = NDescriptorUtils.applyProperties(bestId.builder(), new MapToFunction(p)).build();
            if (CoreNUtils.isEffectiveId(bestId)) {
                return bestId;
            }
            Stack<NId> all = new Stack<>();
            List<NId> parents = descriptor.getParents();
            all.addAll(parents);
            while (!all.isEmpty()) {
                NId parent = all.pop();
                NDescriptor dd = NFetchCmd.of(parent).setEffective(true).getResultDescriptor();
                bestId = NDescriptorUtils.applyProperties(bestId.builder(), new MapToFunction(NDescriptorUtils.getPropertiesMap(dd.getProperties()))).build();
                if (CoreNUtils.isEffectiveId(bestId)) {
                    return bestId;
                }
                all.addAll(dd.getParents());
            }
            throw new NNotFoundException(bestId,
                    NMsg.ofC("unable to fetchEffective for %s. best Result is %s", bestId, bestId), null);
        }
        NId bestId = NIdBuilder.of(g, thisId.getArtifactId()).setVersion(v).build();
        if (!CoreNUtils.isEffectiveId(bestId)) {
            throw new NNotFoundException(bestId,
                    NMsg.ofC("unable to fetchEffective for %s. best Result is %s", thisId, bestId), null);
        }
        return bestId;
    }

    @Override
    public NIdType resolveNutsIdType(NId id) {
        NIdType idType = NIdType.REGULAR;
        String shortName = id.getShortName();
        if (shortName.equals(NConstants.Ids.NUTS_API)) {
            idType = NIdType.API;
        } else if (shortName.equals(NConstants.Ids.NUTS_RUNTIME)) {
            idType = NIdType.RUNTIME;
        } else {
            for (NId companionTool : wsModel.extensions.getCompanionIds()) {
                if (companionTool.getShortName().equals(shortName)) {
                    idType = NIdType.COMPANION;
                }
            }
        }
        return idType;
    }

    @Override
    public NInstallerComponent getInstaller(NDefinition nutToInstall) {
        if (nutToInstall != null && nutToInstall.getContent().isPresent()) {
            NDescriptor descriptor = nutToInstall.getDescriptor();
            NArtifactCall installerDescriptor = descriptor.getInstaller();
            NDefinition runnerFile = null;
            if (installerDescriptor != null) {
                NId installerId = installerDescriptor.getId();
                if (installerId != null) {
                    // nsh is the only installer that does not need to have groupId!
                    if (NBlankable.isBlank(installerId.getGroupId())
                            && "nsh".equals(installerId.getArtifactId())
                    ) {
                        installerId = installerId.builder().setGroupId("net.thevpc.nuts.toolbox").build();
                    }
                    //ensure installer is always well qualified!
                    CoreNIdUtils.checkShortId(installerId);
                    runnerFile = NSearchCmd.of().setId(installerId)
                            .setOptional(false)
                            .setContent(true)
                            .setDependencies(true)
                            .setLatest(true)
                            .setDistinct(true)
                            .getResultDefinitions()
                            .findFirst().orNull();

                }
            }
            NInstallerComponent best = wsModel.extensions
                    .createComponent(NInstallerComponent.class, runnerFile == null ? nutToInstall : runnerFile).orNull();
            if (best != null) {
                return best;
            }
            return new CommandForIdNInstallerComponent(runnerFile);
        }
        return new CommandForIdNInstallerComponent(null);
    }

    @Override
    public void requireImpl(NDefinition def, boolean withDependencies, NId[] forId) {
        installOrUpdateImpl(def, new String[0], true, false, InstallStrategy0.REQUIRE, withDependencies, forId, null);
    }

    @Override
    public void installImpl(NDefinition def, String[] args, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, true, updateDefaultVersion, InstallStrategy0.INSTALL, true, null, null);
    }

    @Override
    public void updateImpl(NDefinition def, String[] args, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, true, updateDefaultVersion, InstallStrategy0.UPDATE, true, null, null);
    }

    public void uninstallImpl(NDefinition def, String[] args,
                              boolean runInstaller,
                              boolean deleteFiles,
                              boolean eraseFiles,
                              boolean traceBeforeEvent) {
        NPrintStream out = CoreIOUtils.resolveOut();
        if (runInstaller) {
            NInstallerComponent installerComponent = getInstaller(def);
            if (installerComponent != null) {
                NExecutionContext executionContext = createExecutionContext()
                        .setDefinition(def)
                        .setArguments(args)
                        .failFast()
                        .setTemporary(false)
                        .setRunAs(NRunAs.currentUser())//uninstall always uses current user
                        .build();
                installerComponent.uninstall(executionContext, eraseFiles);
            }
        }

        getInstalledRepository().uninstall(def);
        NId id = def.getId();
        if (deleteFiles) {
            if (this.getStoreLocation(id, NStoreType.BIN).exists()) {
                this.getStoreLocation(id, NStoreType.BIN).deleteTree();
            }
            if (this.getStoreLocation(id, NStoreType.LIB).exists()) {
                this.getStoreLocation(id, NStoreType.LIB).deleteTree();
            }
            if (this.getStoreLocation(id, NStoreType.LOG).exists()) {
                this.getStoreLocation(id, NStoreType.LOG).deleteTree();
            }
            if (this.getStoreLocation(id, NStoreType.CACHE).exists()) {
                this.getStoreLocation(id, NStoreType.CACHE).deleteTree();
            }
            if (eraseFiles) {
                if (this.getStoreLocation(id, NStoreType.VAR).exists()) {
                    this.getStoreLocation(id, NStoreType.VAR).deleteTree();
                }
                if (this.getStoreLocation(id, NStoreType.CONF).exists()) {
                    this.getStoreLocation(id, NStoreType.CONF).deleteTree();
                }
            }
        }

        if (def.getDescriptor().getIdType() == NIdType.EXTENSION) {
            NExtensionListHelper h = new NExtensionListHelper(
                    this.getApiId(),
                    this.getConfigModel().getStoredConfigBoot().getExtensions())
                    .save();
            h.remove(id);
            this.getConfigModel().getStoredConfigBoot().setExtensions(h.getConfs());
            this.getConfigModel().fireConfigurationChanged("extensions", ConfigEventType.BOOT);
        }
        if (traceBeforeEvent && NSession.of().isPlainTrace()) {
            out.println(NMsg.ofC("%s uninstalled %s", id, NText.ofStyled(
                    "successfully", NTextStyle.success()
            )));
        }
        NWorkspaceUtils.of(wsModel.workspace).events().fireOnUninstall(new DefaultNInstallEvent(def, NSession.of(), new NId[0], eraseFiles));
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
        for (NId ext : wsModel.extensions.getConfigExtensions()) {
            if (ext.equalsShortId(getRuntimeId())) {
                coreFound = true;
                break;
            }
        }
        return !coreFound;
    }

    @Override
    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor) {
        NPath eff = null;
        NWorkspace loc = this;
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue()
                && descriptor.getId().toString().indexOf('$') < 0) {
            NPath l = loc.getStoreLocation(descriptor.getId(), NStoreType.CACHE);
            String nn = loc.getDefaultIdFilename(descriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
            if (eff.isRegularFile()) {
                try {
                    NDescriptor d = NDescriptorParser.of().parse(eff).orNull();
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    LOG.with().level(Level.FINE).error(ex)
                            .log(NMsg.ofJ("failed to parse {0}", eff));
                    //
                }
            }
        } else {
            //
        }
        NDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor);
        NDescriptorUtils.checkValidEffectiveDescriptor(effectiveDescriptor);
        if (eff == null) {
            NPath l = this.getStoreLocation(effectiveDescriptor.getId(), NStoreType.CACHE);
            String nn = loc.getDefaultIdFilename(effectiveDescriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
        }
        try {
            NDescriptorFormat.of(effectiveDescriptor).setNtf(false).print(eff);
        } catch (Exception ex) {
            LOG.with().level(Level.FINE).error(ex)
                    .log(NMsg.ofJ("failed to print {0}", eff));
            //
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
            nutToInstall = NSearchCmd.of().setTransitive(false).addId(id)
                    .setInlineDependencies(checkDependencies)
                    .setInstallStatus(NInstallStatusFilters.of().byDeployed(true))
                    .setOptional(false)
                    .getResultDefinitions()
                    .findFirst().orNull();
            if (nutToInstall == null) {
                return NInstallStatus.NONE;
            }
        } catch (NNotFoundException e) {
            return NInstallStatus.NONE;
        } catch (Exception ex) {
            LOG.with().level(Level.SEVERE).error(ex)
                    .log(NMsg.ofJ("error: %s", ex));
            return NInstallStatus.NONE;
        }
        return getInstalledRepository().getInstallStatus(nutToInstall.getId());
    }

    @Override
    public NExecutionContextBuilder createExecutionContext() {
        NSession session = NSession.of();
        return new DefaultNExecutionContextBuilder()
                .setWorkspace(this)
                .setDry(session.isDry())
                .setBot(session.isBot())
                .setExecutionType(this.getBootOptions().getExecutionType().orNull())
                ;
    }

    @Override
    public void deployBoot(NId id, boolean withDependencies) {
        runWith(() -> {
            Map<NId, NDefinition> defs = new HashMap<>();
            NDefinition m = NFetchCmd.of(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
            Map<String, String> a = new LinkedHashMap<>();
            a.put("configVersion", Nuts.getVersion().toString());
            a.put("id", id.getLongName());
            a.put("dependencies", m.getDependencies().get().transitive()
                    .map(NDependency::getLongName)
                    .withDesc(NEDesc.of("getLongName"))
                    .collect(Collectors.joining(";")));
            defs.put(m.getId().getLongId(), m);
            if (withDependencies) {
                for (NDependency dependency : m.getDependencies().get()) {
                    if (!defs.containsKey(dependency.toId().getLongId())) {
                        m = NFetchCmd.of(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
                        defs.put(m.getId().getLongId(), m);
                    }
                }
            }
            for (NDefinition def : defs.values()) {
                NPath bootstrapFolder = this.getStoreLocation(NStoreType.LIB).resolve(NConstants.Folders.ID);
                NId id2 = def.getId();
                NCp.of().from(def.getContent().get())
                        .to(bootstrapFolder.resolve(this.getDefaultIdBasedir(id2))
                                .resolve(this.getDefaultIdFilename(id2.builder().setFaceContent().setPackaging("jar").build()))
                        ).run();
                NDescriptorFormat.of(NFetchCmd.of(id2).getResultDescriptor()).setNtf(false)
                        .print(bootstrapFolder.resolve(this.getDefaultIdBasedir(id2))
                                .resolve(this.getDefaultIdFilename(id2.builder().setFaceDescriptor().build())));

                Map<String, String> pr = new LinkedHashMap<>();
                pr.put("file.updated.date", Instant.now().toString());
                pr.put("project.id", def.getId().getShortId().toString());
                pr.put("project.name", def.getId().getShortId().toString());
                pr.put("project.version", def.getId().getVersion().toString());
                NRepositoryDB repoDB = NRepositoryDB.of();
                pr.put("repositories", "~/.m2/repository"
                        + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryLocation.of("vpc-public-maven", repoDB).get(), true).getConfig().getLocation()
                        + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryLocation.of("maven-central", repoDB).get(), true).getConfig().getLocation()
                        + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryLocation.of("nuts-public", repoDB).get(), true).getConfig().getLocation()
                );
                pr.put("project.dependencies.compile",
                        String.join(";",
                                def.getDependencies().get().transitive()
                                        .filter(x -> !x.isOptional()
                                                && NDependencyFilters.of().byRunnable()
                                                .acceptDependency(def.getId(), x)
                                        ).withDesc(NEDesc.of("isOptional && runnable"))
                                        .map(x -> x.toId().getLongName())
                                        .withDesc(NEDesc.of("toId.getLongName"))
                                        .toList()
                        )
                );

                try (Writer writer = bootstrapFolder.resolve(this.getDefaultIdBasedir(def.getId().getLongId()))
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
    public String getUuid() {
        return wsModel.uuid;
    }

    @Override
    public String getName() {
        return wsModel.name;
    }

    @Override
    public String getHashName() {
        if (wsModel.hashName == null) {
            runWith(() -> {
                wsModel.hashName = NHashName.of().getHashName(this);
            });
        }
        return wsModel.hashName;
    }

    @Override
    public NVersion getApiVersion() {
        return wsModel.apiVersion;
    }

    @Override
    public NId getApiId() {
        return wsModel.apiId;
    }

    @Override
    public NId getAppId() {
        return NId.get(wsModel.apiId.getGroupId(), "nuts", wsModel.apiId.getVersion()).get();
    }

    @Override
    public NId getRuntimeId() {
        return wsModel.runtimeId;
    }

    @Override
    public NPath getLocation() {
        return wsModel.location == null ? null : NPath.of(wsModel.location);
    }

    @Override
    public NSession createSession() {
        return callWith(() -> {
            NSession nSession = new DefaultNSession(this);
            nSession.setTerminal(NTerminal.of());
            nSession.setExpireTime(this.getBootOptions().getExpireTime().orNull());
            return nSession;
        });
    }

    public DefaultNWorkspaceEnvManagerModel getEnvModel() {
        return wsModel.envModel;
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
        this.wsModel.confDB.storeStringNonBlank(NLocationKey.of(NStoreType.CONF, getApiId(), "installation-digest"), value);
    }

    @Override
    public NExtensions extensions() {
        return wsModel.extensions;
    }

    @Override
    public NSession currentSession() {
        return sessionScopes().peek();
    }

    @Override
    public Stack<NSession> sessionScopes() {
        InheritableThreadLocal<Stack<NSession>> ss = wsModel.sessionScopes;
        Stack<NSession> nSessions = ss.get();
        if (nSessions == null) {
            nSessions = new Stack<>();
            ss.set(nSessions);
        }
        if (nSessions.isEmpty()) {
            nSessions.push(defaultSession());
        }
        return nSessions;
    }

    public enum InstallStrategy0 implements NEnum {
        INSTALL,
        UPDATE,
        REQUIRE;
        private final String id;

        InstallStrategy0() {
            this.id = NNameFormat.ID_NAME.format(name());
        }

        public static NOptional<InstallStrategy0> parse(String value) {
            return NEnumUtils.parseEnum(value, InstallStrategy0.class);
        }

        @Override
        public String id() {
            return id;
        }
    }

    public DefaultNRepositoryModel getRepositoryModel() {
        return wsModel.repositoryModel;
    }


    @Override
    public List<NRepository> getRepositories() {
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
    public NOptional<NRepository> findRepository(String repositoryNameOrId) {
        return getRepositoryModel().findRepository(repositoryNameOrId);
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
    public NRepository addRepository(NAddRepositoryOptions options) {
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

    public DefaultNPlatformModel getSdkModel() {
        return wsModel.sdkModel;
    }


    @Override
    public Map<String, Object> getProperties() {
        return getEnvModel().getProperties();
    }

    @Override
    public NOptional<NLiteral> getProperty(String property) {
        return getEnvModel().getProperty(property);
    }

    @Override
    public NWorkspace setProperty(String property, Object value) {
        getEnvModel().setProperty(property, value);
        return this;
    }

    @Override
    public NOsFamily getOsFamily() {
//        checkSession();
        return getEnvModel().getOsFamily();
    }

    @Override
    public String getHostName() {
        return getEnvModel().getHostName();
    }

    @Override
    public String getPid() {
        return getEnvModel().getPid();
    }

    @Override
    public Set<NShellFamily> getShellFamilies() {
        return getEnvModel().getShellFamilies();
    }

    @Override
    public NShellFamily getShellFamily() {
        return getEnvModel().getShellFamily();
    }

    @Override
    public NId getDesktopEnvironment() {
        return getDesktopEnvironments().stream().findFirst().get();
    }

    @Override
    public Set<NId> getDesktopEnvironments() {
        return getEnvModel().getDesktopEnvironments();
    }

    @Override
    public NDesktopEnvironmentFamily getDesktopEnvironmentFamily() {
        return getEnvModel().getDesktopEnvironmentFamily();
    }

    @Override
    public Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies() {
        return getEnvModel().getDesktopEnvironmentFamilies();
    }

    @Override
    public NId getPlatform() {
//        checkSession();
        return getEnvModel().getPlatform();
    }

    @Override
    public NId getOs() {
//        checkSession();
        return getEnvModel().getOs();
    }

    public NId getOsDist() {
//        checkSession();
        return getEnvModel().getOsDist();
    }

    @Override
    public NId getArch() {
//        checkSession();
        return getEnvModel().getArch();
    }

    @Override
    public NArchFamily getArchFamily() {
//        checkSession();
        return getEnvModel().getArchFamily();
    }

    @Override
    public boolean isGraphicalDesktopEnvironment() {
        return getEnvModel().isGraphicalDesktopEnvironment();
    }

    @Override
    public NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem item) {
        NAssert.requireNonBlank(item, "item");
        switch (item) {
            case DESKTOP: {
                NSupportMode a = this.getBootOptions().getDesktopLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
            case MENU: {
                NSupportMode a = this.getBootOptions().getMenuLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
            case USER: {
                NSupportMode a = this.getBootOptions().getUserLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
        }
        switch (getOsFamily()) {
            case LINUX: {
                switch (item) {
                    case DESKTOP: {
                        return NSupportMode.SUPPORTED;
                    }
                    case MENU: {
                        return NSupportMode.PREFERRED;
                    }
                    case USER: {
                        return NSupportMode.PREFERRED;
                    }
                }
                break;
            }
            case UNIX: {
                return NSupportMode.NEVER;
            }
            case WINDOWS: {
                switch (item) {
                    case DESKTOP: {
                        if (Files.isDirectory(getDesktopPath())) {
                            return NSupportMode.PREFERRED;
                        }
                        return NSupportMode.SUPPORTED;
                    }
                    case MENU: {
                        return NSupportMode.PREFERRED;
                    }
                    case USER: {
                        return NSupportMode.PREFERRED;
                    }
                }
                break;
            }
            case MACOS: {
                return NSupportMode.NEVER;
            }
            case UNKNOWN: {
                return NSupportMode.NEVER;
            }
        }
        return NSupportMode.NEVER;
    }

    public Path getDesktopPath() {
        switch (getOsFamily()) {
            case LINUX:
            case UNIX:
            case MACOS: {
                File f = new File(System.getProperty("user.home"), ".config/user-dirs.dirs");
                if (f.exists()) {
                    try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("#")) {
                                //ignore
                            } else {
                                Matcher m = UNIX_USER_DIRS_PATTERN.matcher(line);
                                if (m.find()) {
                                    String k = m.group("k");
                                    if (k.equals("XDG_DESKTOP_DIR")) {
                                        String v = m.group("v");
                                        v = v.trim();
                                        if (v.startsWith("\"")) {
                                            int last = v.indexOf('\"', 1);
                                            String s = v.substring(1, last);
                                            s = s.replace("$HOME", System.getProperty("user.home"));
                                            return Paths.get(s);
                                        } else {
                                            return Paths.get(v);
                                        }
                                    }
                                } else {
                                    //this is unexpected format!
                                    break;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        //ignore
                    }
                }
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
            case WINDOWS: {
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
            default: {
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
        }
    }

    public void addLauncher(NLauncherOptions launcher) {
        //apply isolation!
        NIsolationLevel isolation = this.getBootOptions().getIsolationLevel().orElse(NIsolationLevel.SYSTEM);
        if (isolation.compareTo(NIsolationLevel.CONFINED) >= 0) {
            launcher.setCreateDesktopLauncher(NSupportMode.NEVER);
            launcher.setCreateMenuLauncher(NSupportMode.NEVER);
            launcher.setCreateUserLauncher(NSupportMode.NEVER);
            launcher.setSwitchWorkspace(false);
            launcher.setSwitchWorkspaceLocation(null);
        }
        SystemNdi ndi = NSettingsNdiSubCommand.createNdi(this.getEnvModel().getWorkspace());
        if (ndi != null) {
            ndi.addScript(
                    new NdiScriptOptions()
                            .setLauncher(launcher.copy()),
                    new String[]{launcher.getId().builder().getFullName()}
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
    public NPath getStoreLocation(NStoreType folderType) {
        return getLocationModel().getStoreLocation(folderType);
    }

    @Override
    public NPath getStoreLocation(NId id, NStoreType folderType) {
        return getLocationModel().getStoreLocation(id, folderType);
    }

    @Override
    public NPath getStoreLocation(NStoreType folderType, String repositoryIdOrName) {
        return getLocationModel().getStoreLocation(folderType, repositoryIdOrName);
    }

    @Override
    public NPath getStoreLocation(NId id, NStoreType folderType, String repositoryIdOrName) {
        return getLocationModel().getStoreLocation(id, folderType, repositoryIdOrName);
    }

    @Override
    public NStoreStrategy getStoreStrategy() {
        return getLocationModel().getStoreStrategy();
    }

    @Override
    public NStoreStrategy getRepositoryStoreStrategy() {
        return getLocationModel().getRepositoryStoreStrategy();
    }

    @Override
    public NOsFamily getStoreLayout() {
        return getLocationModel().getStoreLayout();
    }

    @Override
    public Map<NStoreType, String> getStoreLocations() {
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
    public Map<NHomeLocation, String> getHomeLocations() {
        return getLocationModel().getHomeLocations();
    }

    @Override
    public NPath getHomeLocation(NHomeLocation location) {
        return getLocationModel().getHomeLocation(location);
    }

    @Override
    public NPath getWorkspaceLocation() {
        return getLocationModel().getWorkspaceLocation();
    }

    @Override
    public NWorkspace setStoreLocation(NStoreType folderType, String location) {
        getLocationModel().setStoreLocation(folderType, location);
        return this;
    }


    @Override
    public NWorkspace setStoreStrategy(NStoreStrategy strategy) {
        getLocationModel().setStoreStrategy(strategy);
        return this;
    }

    @Override
    public NWorkspace setStoreLayout(NOsFamily storeLayout) {
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
    public boolean addPlatform(NPlatformLocation location) {
        return getSdkModel().addPlatform(location);
    }

    @Override
    public boolean updatePlatform(NPlatformLocation oldLocation, NPlatformLocation newLocation) {
        return getSdkModel().updatePlatform(oldLocation, newLocation);
    }

    @Override
    public boolean removePlatform(NPlatformLocation location) {
        return getSdkModel().removePlatform(location);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByName(NPlatformFamily platformType, String locationName) {
        return getSdkModel().findPlatformByName(platformType, locationName);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByPath(NPlatformFamily platformType, NPath path) {
        return getSdkModel().findPlatformByPath(platformType, path);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, String version) {
        return getSdkModel().findPlatformByVersion(platformType, version);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatform(NPlatformLocation location) {
        return getSdkModel().findPlatform(location);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, NVersionFilter requestedVersion) {
        return getSdkModel().findPlatformByVersion(platformType, requestedVersion);
    }

    @Override
    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformFamily) {
        return getSdkModel().searchSystemPlatforms(platformFamily);
    }

    @Override
    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformFamily, NPath path) {
        return getSdkModel().searchSystemPlatforms(platformFamily, path);
    }

    @Override
    public NOptional<NPlatformLocation> resolvePlatform(NPlatformFamily platformFamily, NPath path, String preferredName) {
        return getSdkModel().resolvePlatform(platformFamily, path, preferredName);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatform(NPlatformFamily platformFamily, Predicate<NPlatformLocation> filter) {
        return getSdkModel().findOnePlatform(platformFamily, filter);
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms(NPlatformFamily platformFamily, Predicate<NPlatformLocation> filter) {
        return getSdkModel().findPlatforms(platformFamily, filter);
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms() {
        return findPlatforms(null, null);
    }

    @Override
    public NWorkspace addDefaultPlatforms(NPlatformFamily type) {
        if (type == NPlatformFamily.JAVA) {
            NWorkspaceUtils.of(this).installAllJVM();
        }
        return this;
    }

    @Override
    public NWorkspace addDefaultPlatform(NPlatformFamily type) {
        if (type == NPlatformFamily.JAVA) {
            //at least add current vm
            NWorkspaceUtils.of(this).installCurrentJVM();
        }
        return this;
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms(NPlatformFamily type) {
        return getSdkModel().findPlatforms(type, null);
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
    public Set<String> getAllImports() {
        return getImportModel().getAll();
    }

    /// ////////////////////////////////////


    @Override
    public NWorkspaceStoredConfig getStoredConfig() {
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
        return getConfigModel().loadBootConfig(_ws, global, followLinks);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        return getConfigModel().isSupportedRepositoryType(repositoryType);
    }

    @Override
    public List<NAddRepositoryOptions> getDefaultRepositories() {
        return getConfigModel().getDefaultRepositories();
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        return getConfigModel().getAvailableArchetypes();
    }

    @Override
    public NPath resolveRepositoryPath(String repositoryLocation) {
        return getConfigModel().resolveRepositoryPath(NPath.of(repositoryLocation));
    }

    @Override
    public NIndexStoreFactory getIndexStoreClientFactory() {
        return getConfigModel().getIndexStoreClientFactory();
    }

    @Override
    public String getJavaCommand() {
        return getConfigModel().getJavaCommand();
    }

    @Override
    public String getJavaOptions() {
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
    public Map<String, String> getConfigMap() {
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
    public List<NCommandFactoryConfig> getCommandFactories() {
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
    public NBootOptions getBootOptions() {
        return getConfigModel().getBootModel().getBootEffectiveOptions();
    }

    @Override
    public ClassLoader getBootClassLoader() {
        return getConfigModel().getBootClassLoader();
    }

    @Override
    public List<URL> getBootClassWorldURLs() {
        return Collections.unmodifiableList(getConfigModel().getBootClassWorldURLs());
    }

    @Override
    public String getBootRepositories() {
        return getConfigModel().getBootRepositories();
    }

    @Override
    public Instant getCreationStartTime() {
        return getConfigModel().getCreationStartTime();
    }

    @Override
    public Instant getCreationFinishTime() {
        return getConfigModel().getCreationFinishTime();
    }

    @Override
    public Duration getCreationDuration() {
        return getConfigModel().getCreateDuration();
    }

    public NClassLoaderNode getBootRuntimeClassLoaderNode() {
        return getBootModel().getBootUserOptions().getRuntimeBootDependencyNode().get();
    }

    public List<NClassLoaderNode> getBootExtensionClassLoaderNode() {
        return getBootModel().getBootUserOptions().getExtensionBootDependencyNodes().orElseGet(Collections::emptyList);
    }

    @Override
    public NWorkspaceTerminalOptions getBootTerminal() {
        return getBootModel().getBootTerminal();
    }

}
