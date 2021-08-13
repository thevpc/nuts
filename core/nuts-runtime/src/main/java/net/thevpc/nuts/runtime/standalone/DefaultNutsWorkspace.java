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
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.MapToFunction;
import net.thevpc.nuts.runtime.core.AbstractNutsWorkspace;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.app.DefaultNutsCommandLineManager;
import net.thevpc.nuts.runtime.core.app.DefaultNutsWorkspaceLocationManager;
import net.thevpc.nuts.runtime.core.app.DefaultNutsWorkspaceLocationModel;
import net.thevpc.nuts.runtime.core.commands.ws.DefaultNutsExecutionContextBuilder;
import net.thevpc.nuts.runtime.core.commands.ws.NutsExecutionContextBuilder;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.events.DefaultNutsInstallEvent;
import net.thevpc.nuts.runtime.core.events.DefaultNutsUpdateEvent;
import net.thevpc.nuts.runtime.core.events.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterManager;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterModel;
import net.thevpc.nuts.runtime.core.format.DefaultNutsInfoFormat;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFormat;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManagerModel;
import net.thevpc.nuts.runtime.core.log.DefaultNutsLogManager;
import net.thevpc.nuts.runtime.core.log.DefaultNutsLogModel;
import net.thevpc.nuts.runtime.core.log.DefaultNutsLogger;
import net.thevpc.nuts.runtime.core.model.DefaultNutsId;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;
import net.thevpc.nuts.runtime.core.repos.DefaultNutsRepositoryManager;
import net.thevpc.nuts.runtime.core.repos.DefaultNutsRepositoryModel;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.repos.NutsRepositorySelector;
import net.thevpc.nuts.runtime.core.util.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootManager;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.config.*;
import net.thevpc.nuts.runtime.standalone.ext.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.runtime.standalone.ext.DefaultNutsWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.installers.CommandForIdNutsInstallerComponent;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsIOManager;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsIOModel;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsTerminalManager;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsTerminalModel;
import net.thevpc.nuts.runtime.standalone.manager.DefaultNutsDependencyManager;
import net.thevpc.nuts.runtime.standalone.manager.DefaultNutsDescriptorManager;
import net.thevpc.nuts.runtime.standalone.manager.DefaultNutsIdManager;
import net.thevpc.nuts.runtime.standalone.manager.DefaultNutsVersionManager;
import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsWorkspaceSecurityManager;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsWorkspaceSecurityModel;
import net.thevpc.nuts.runtime.standalone.security.ReadOnlyNutsWorkspaceOptions;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.*;
import net.thevpc.nuts.spi.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/6/17.
 */
@NutsPrototype
public class DefaultNutsWorkspace extends AbstractNutsWorkspace implements NutsWorkspaceExt {

    public static final String VERSION_INSTALL_INFO_CONFIG = "0.8.0";
    public static final String VERSION_SDK_LOCATION = "0.8.0";
    public static final String VERSION_REPOSITORY_CONFIG = "0.8.0";
    public static final String VERSION_REPOSITORY_REF = "0.8.0";
    public static final String VERSION_WS_CONFIG_API = "0.8.0";
    public static final String VERSION_WS_CONFIG_BOOT = "0.8.0";
    public static final String VERSION_WS_CONFIG_MAIN = "0.8.0";
    public static final String VERSION_WS_CONFIG_RUNTIME = "0.8.0";
    public static final String VERSION_WS_CONFIG_SECURITY = "0.8.0";
    public static final String VERSION_COMMAND_ALIAS_CONFIG = "0.8.0";
    public static final String VERSION_COMMAND_ALIAS_CONFIG_FACTORY = "0.8.0";
    public static final String VERSION_USER_CONFIG = "0.8.0";
    public NutsLogger LOG;
    protected DefaultNutsWorkspaceSecurityModel securityModel;
    protected DefaultNutsFilterModel filtersModel;
    protected DefaultNutsWorkspaceConfigModel configModel;
    protected DefaultNutsWorkspaceLocationModel locationsModel;
    protected DefaultNutsRepositoryModel repositoryModel;
    protected DefaultNutsWorkspaceEventModel eventsModel;
    protected DefaultNutsTextManagerModel textModel;
    protected DefaultNutsIOModel ioModel;
    protected DefaultNutsSdkModel sdkModel;
    protected DefaultNutsTerminalModel termModel;
    protected String uuid;
    protected String location;
    protected String name;
    protected NutsVersion apiVersion;
    protected NutsId apiId;
    protected NutsId runtimeId;
    private DefaultNutsInstalledRepository installedRepository;
    private DefaultNutsLogModel logModel;
    private DefaultNutsWorkspaceEnvManagerModel envModel;
    private DefaultNutsWorkspaceExtensionModel extensionModel;
    private DefaultCustomCommandsModel aliasesModel;
    private DefaultImportModel importModel;
    private DefaultNutsConcurrentModel concurrentModel;

    public DefaultNutsWorkspace(NutsWorkspaceInitInformation info) {
        initWorkspace(info);
    }

    //    /**
//     * creates a zip file based on the folder. The folder should contain a
//     * descriptor file at its root
//     *
//     * @return bundled nuts file, the nuts is neither deployed nor installed!
//     */
//    @Deprecated
//    public NutsDefinition createBundle(Path contentFolder, Path destFile, NutsQueryOptions queryOptions, NutsSession session) {
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
//                            throw new UncheckedIOException(ex);
//                        }
//                        return new DefaultNutsDefinition(
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
//    public boolean isFetched(NutsId parseList, NutsSession session) {
//        session = CoreNutsUtils.validateSession(session, this);
//        NutsSession offlineSession = session.copy();
//        try {
//            NutsDefinition found = fetch().parseList(parseList).offline().setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).getResultDefinition();
//            return found != null;
//        } catch (Exception e) {
//            return false;
//        }
//    }
    private static Set<NutsBootId> toIds(NutsBootDescriptor[] all) {
        Set<NutsBootId> set = new LinkedHashSet<>();
        for (NutsBootDescriptor i : all) {
            set.add(i.getId());
            Collections.addAll(set, i.getDependencies());
        }
        return set;
    }

    private void initWorkspace(NutsWorkspaceInitInformation info) {
        this.uuid = info.getUuid();
        this.bootModel = new DefaultNutsBootModel(this, info);
        ioModel = new DefaultNutsIOModel(this, bootModel);
        this.logModel = new DefaultNutsLogModel(this, info);
        logModel.setDefaultSession(defaultSession());
        this.LOG = log().setSession(defaultSession()).of(DefaultNutsWorkspace.class);
        ((DefaultNutsLogger) LOG).suspendTerminal();
        this.name = Paths.get(info.getWorkspaceLocation()).getFileName().toString();
        termModel = new DefaultNutsTerminalModel(this);
        concurrentModel = new DefaultNutsConcurrentModel(this);
        sdkModel = new DefaultNutsSdkModel(this);
        filtersModel = new DefaultNutsFilterModel(this);
        installedRepository = new DefaultNutsInstalledRepository(this, info);
        repositoryModel = new DefaultNutsRepositoryModel(this);
        configModel = new DefaultNutsWorkspaceConfigModel(this, info);
        envModel = new DefaultNutsWorkspaceEnvManagerModel(this, info, defaultSession());
        aliasesModel = new DefaultCustomCommandsModel(this);
        importModel = new DefaultImportModel(this);
        locationsModel = new DefaultNutsWorkspaceLocationModel(this, info, Paths.get(info.getWorkspaceLocation()).toString());
        eventsModel = new DefaultNutsWorkspaceEventModel(this);
        textModel = new DefaultNutsTextManagerModel(this, info);
        location = info.getWorkspaceLocation();
        apiVersion = DefaultNutsVersion.valueOf(Nuts.getVersion(), defaultSession());
        apiId = new DefaultNutsId("net.thevpc.nuts", "nuts", apiVersion.toString(), defaultSession());
        runtimeId = new DefaultNutsId(
                info.getRuntimeId().getGroupId(),
                info.getRuntimeId().getArtifactId(),
                info.getRuntimeId().getVersion(),
                defaultSession());

        NutsLoggerOp LOGCRF = LOG.with().level(Level.CONFIG).verb(NutsLogVerb.READ).formatted().session(defaultSession());
        NutsLoggerOp LOGCSF = LOG.with().level(Level.CONFIG).verb(NutsLogVerb.START).formatted().session(defaultSession());
//        NutsFormatManager formats = this.formats().setSession(defaultSession());
        NutsTextManager text = text().setSession(defaultSession());
        if (LOG.isLoggable(Level.CONFIG)) {
            LOGCSF.log(" ===============================================================================");
            String s = CoreIOUtils.loadString(getClass().getResourceAsStream("/net/thevpc/nuts/runtime/includes/standard-header.ntf"), true);
            s = s.replace("${nuts.workspace-runtime.version}", Nuts.getVersion());
            for (String s1 : s.split("\n")) {
                LOGCSF.log(s1);
            }
            LOGCSF.log(" ");
            LOGCSF.log(" = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
            LOGCSF.log(" ");
            LOGCSF.log("start ```sh nuts``` ```primary3 {0}``` at {1}", Nuts.getVersion(), CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(info.getOptions().getCreationTime())));
            NutsCommandLineManager commandLine = commandLine().setSession(defaultSession());
            LOGCRF.log("open Nuts Workspace               : {0}", commandLine.create(info.getOptions().format().getBootCommand()));
            LOGCRF.log("open Nuts Workspace (compact)     : {0}", commandLine.create(info.getOptions().format().compact().getBootCommand()));

            LOGCRF.log("open Workspace with config        : ");
            LOGCRF.log("   nuts-uuid                      : {0}", CoreNutsUtils.desc(info.getUuid(), text));
            LOGCRF.log("   nuts-name                      : {0}", CoreNutsUtils.desc(info.getName(), text));
            LOGCRF.log("   nuts-api-version               : {0}", version().setSession(defaultSession()).parser().parse(Nuts.getVersion()));
            LOGCRF.log("   nuts-boot-repositories         : {0}", CoreNutsUtils.desc(info.getBootRepositories(), text));
            LOGCRF.log("   nuts-runtime-dependencies      : {0}",
                    text.builder().appendJoined(text.forStyled(";", NutsTextStyle.separator()),
                            Arrays.stream(info.getRuntimeBootDescriptor().getDependencies())
                                    .map(x -> id().setSession(defaultSession()).parser().parse(x.toString()))
                                    .collect(Collectors.toList())
                    )
            );
            NutsIOManager io = io().setSession(defaultSession());
            LOGCRF.log("   nuts-runtime-urls              : {0}",
                    text.builder().appendJoined(text.forStyled(";", NutsTextStyle.separator()),
                            Arrays.stream(info.getClassWorldURLs())
                                    .map(x -> io.path(x.toString()))
                                    .collect(Collectors.toList())
                    )
            );
            LOGCRF.log("   nuts-extension-dependencies    : {0}",
                    text.builder().appendJoined(text.forStyled(";", NutsTextStyle.separator()),
                            toIds(info.getExtensionBootDescriptors()).stream()
                                    .map(x ->
                                            id().setSession(defaultSession()).parser().parse(x.toString())
                                    )
                                    .collect(Collectors.toList())
                    )
            );
//            if (hasUnsatisfiedRequirements()) {
//                LOG.log(Level.CONFIG, "\t execution-requirements         : unsatisfied {0}", getRequirementsHelpString(true));
//            } else {
//                LOG.log(Level.CONFIG, "\t execution-requirements         : satisfied");
//            }
            LOGCRF.log("   nuts-workspace                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getWorkspace(), info.getWorkspaceLocation()));
            LOGCRF.log("   nuts-hash-name                 : {0}", CoreNutsUtils.formatLogValue(text, getHashName(), info.getWorkspaceLocation()));
            LOGCRF.log("   nuts-store-apps                : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.APPS), info.getStoreLocation(NutsStoreLocation.APPS)));
            LOGCRF.log("   nuts-store-config              : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.CONFIG), info.getStoreLocation(NutsStoreLocation.CONFIG)));
            LOGCRF.log("   nuts-store-var                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.VAR), info.getStoreLocation(NutsStoreLocation.VAR)));
            LOGCRF.log("   nuts-store-log                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.LOG), info.getStoreLocation(NutsStoreLocation.LOG)));
            LOGCRF.log("   nuts-store-temp                : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.TEMP), info.getStoreLocation(NutsStoreLocation.TEMP)));
            LOGCRF.log("   nuts-store-cache               : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.CACHE), info.getStoreLocation(NutsStoreLocation.CACHE)));
            LOGCRF.log("   nuts-store-run                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.RUN), info.getStoreLocation(NutsStoreLocation.RUN)));
            LOGCRF.log("   nuts-store-lib                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.LIB), info.getStoreLocation(NutsStoreLocation.LIB)));
            LOGCRF.log("   nuts-store-strategy            : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocationStrategy(), info.getStoreLocationStrategy()));
            LOGCRF.log("   nuts-repos-store-strategy      : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getRepositoryStoreLocationStrategy(), info.getRepositoryStoreLocationStrategy()));
            LOGCRF.log("   nuts-store-layout              : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocationLayout(), info.getStoreLocationLayout() == null ? "system" : info.getStoreLocationLayout()));
            LOGCRF.log("   option-read-only               : {0}", info.getOptions().isReadOnly());
            LOGCRF.log("   option-trace                   : {0}", info.getOptions().isTrace());
            LOGCRF.log("   option-progress                : {0}", CoreNutsUtils.desc(info.getOptions().getProgressOptions(), text));
            LOGCRF.log("   inherited                      : {0}", info.getOptions().isInherited());
            LOGCRF.log("   inherited-nuts-boot-args       : {0}", System.getProperty("nuts.boot.args") == null ? CoreNutsUtils.desc(null, text)
                    : CoreNutsUtils.desc(commandLine.parse(System.getProperty("nuts.boot.args")), text)
            );
            LOGCRF.log("   inherited-nuts-args            : {0}", System.getProperty("nuts.args") == null ? CoreNutsUtils.desc(null, text)
                    : CoreNutsUtils.desc(text.toText(commandLine.parse(System.getProperty("nuts.args"))), text)
            );
            LOGCRF.log("   option-open-mode               : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getOpenMode(), info.getOptions().getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : info.getOptions().getOpenMode()));
            LOGCRF.log("   java-home                      : {0}", System.getProperty("java.home"));
            LOGCRF.log("   java-classpath                 : {0}", System.getProperty("java.class.path"));
            LOGCRF.log("   java-library-path              : {0}", System.getProperty("java.library.path"));
            LOGCRF.log("   os-name                        : {0}", System.getProperty("os.name"));
            NutsWorkspaceEnvManager senv = env().setSession(defaultSession());
            LOGCRF.log("   os-dist                        : {0}", senv.getOsDist().getArtifactId());
            LOGCRF.log("   os-arch                        : {0}", System.getProperty("os.arch"));
            LOGCRF.log("   os-version                     : {0}", senv.getOsDist().getVersion());
            LOGCRF.log("   user-name                      : {0}", System.getProperty("user.name"));
            LOGCRF.log("   user-dir                       : {0}", io.path(System.getProperty("user.dir")));
            LOGCRF.log("   user-home                      : {0}", io.path(System.getProperty("user.home")));
        }
        securityModel = new DefaultNutsWorkspaceSecurityModel(this);
        String workspaceLocation = info.getWorkspaceLocation();
        String apiVersion = info.getApiVersion();
        NutsBootId runtimeId = info.getRuntimeId();
        String repositories = info.getBootRepositories();
        NutsWorkspaceOptions uoptions = info.getOptions();
        NutsBootWorkspaceFactory bootFactory = info.getBootWorkspaceFactory();
        ClassLoader bootClassLoader = info.getClassWorldLoader();
        NutsWorkspaceConfigManager _config = config().setSession(defaultSession());
        NutsWorkspaceEnvManager _env = env().setSession(defaultSession());
        if (uoptions == null) {
            uoptions = new ReadOnlyNutsWorkspaceOptions(_config.optionsBuilder());
        } else {
            uoptions = new ReadOnlyNutsWorkspaceOptions(uoptions.copy());
        }
        if (uoptions.getCreationTime() == 0) {
            configModel.setStartCreateTimeMillis(System.currentTimeMillis());
        }

        NutsBootConfig cfg = new NutsBootConfig();
        cfg.setWorkspace(workspaceLocation);
        cfg.setApiVersion(apiVersion);
        cfg.setRuntimeId(runtimeId == null ? null : runtimeId.toString());
        cfg.setRuntimeBootDescriptor(info.getRuntimeBootDescriptor());
        cfg.setExtensionBootDescriptors(info.getExtensionBootDescriptors());
        extensionModel = new DefaultNutsWorkspaceExtensionModel(this, bootFactory, uoptions.getExcludedExtensions(), defaultSession());
        boolean exists = NutsWorkspaceConfigManagerExt.of(_config).getModel().isValidWorkspaceFolder(defaultSession());
        NutsOpenMode openMode = uoptions.getOpenMode();
        if (openMode != null) {
            switch (openMode) {
                case OPEN_OR_ERROR: {
                    if (!exists) {
                        throw new NutsWorkspaceNotFoundException(workspaceLocation);
                    }
                    break;
                }
                case CREATE_OR_ERROR: {
                    if (exists) {
                        throw new NutsWorkspaceAlreadyExistsException(workspaceLocation);
                    }
                    break;
                }
            }
        }
        extensionModel.onInitializeWorkspace(info, bootClassLoader, defaultSession());
//        List<DefaultNutsWorkspaceExtensionManager.RegInfo> regInfos = extensionManager.buildRegInfos(bootSession);
//        for (Iterator<DefaultNutsWorkspaceExtensionManager.RegInfo> iterator = regInfos.iterator(); iterator.hasNext(); ) {
//            DefaultNutsWorkspaceExtensionManager.RegInfo regInfo = iterator.next();
//            switch (regInfo.getExtensionPointType().getName()) {
//                case "net.thevpc.nuts.NutsSystemTerminalBase":
//                case "net.thevpc.nuts.NutsSessionTerminalBase":{
//                    extensionManager.registerType(regInfo.getExtensionPointType(),regInfo.getExtensionTypeImpl(),
//                            getApiId()
//                            , bootSession);
//                    iterator.remove();
//                    break;
//                }
//                case "net.thevpc.nuts.runtime.core.io.NutsFormattedPrintStream": {
//                    extensionManager.registerType(regInfo.getExtensionPointType(),regInfo.getExtensionTypeImpl(),
//                            getRuntimeId()
//                            , bootSession);
//                    iterator.remove();
//                    break;
//                }
//            }
//        }

        NutsSystemTerminalBase termb = extensions().setSession(defaultSession()).createSupported(NutsSystemTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionNotFoundException(defaultSession(), NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsTerminalManager _term = term().setSession(defaultSession());
        _term
                .setSystemTerminal(termb)
                .setTerminal(_term.createTerminal()
                );
        bootModel.bootSession().setTerminal(term().setSession(bootModel.bootSession()).createTerminal());
//        if (defaultSession().isPlainOut()) {
//            defaultSession().getTerminal().out().run(NutsTerminalCommand.LATER_RESET_LINE);
//        }
        ((DefaultNutsLogger) LOG).resumeTerminal(defaultSession());

//        for (Iterator<DefaultNutsWorkspaceExtensionManager.RegInfo> iterator = regInfos.iterator(); iterator.hasNext(); ) {
//            DefaultNutsWorkspaceExtensionManager.RegInfo regInfo = iterator.next();
//            extensionManager.registerType(regInfo.getExtensionPointType(),re, session);
//            iterator.remove();
//        }
        configModel.onExtensionsPrepared(defaultSession());
        initializing = true;
        boolean justInstalled = false;
        try {
            if (!loadWorkspace(defaultSession(), uoptions.getExcludedExtensions(), null)) {
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                } else {
                    //this is the uuid of a freshly 'reset' workspace. We retain the uuid.
                }
                //workspace wasn't loaded. Create new configuration...
                justInstalled = true;
                NutsWorkspaceUtils.of(defaultSession()).checkReadOnly();
                LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS).log("creating NEW workspace at {0}", locations().getWorkspaceLocation());
                NutsWorkspaceConfigBoot bconfig = new NutsWorkspaceConfigBoot();
                //load from config with resolution applied
                bconfig.setUuid(uuid);
                NutsWorkspaceConfigApi aconfig = new NutsWorkspaceConfigApi();
                aconfig.setApiVersion(apiVersion);
                aconfig.setRuntimeId(runtimeId == null ? null : runtimeId.toString());
                aconfig.setJavaCommand(uoptions.getJavaCommand());
                aconfig.setJavaOptions(uoptions.getJavaOptions());

                NutsWorkspaceConfigRuntime rconfig = new NutsWorkspaceConfigRuntime();
                rconfig.setDependencies(
                        Arrays.stream(info.getRuntimeBootDescriptor().getDependencies())
                                .map(x -> x.toString())
                                .collect(Collectors.joining(";"))
                );
                rconfig.setId(runtimeId == null ? null : runtimeId.toString());

                bconfig.setBootRepositories(repositories);
                bconfig.setStoreLocationStrategy(uoptions.getStoreLocationStrategy());
                bconfig.setRepositoryStoreLocationStrategy(uoptions.getRepositoryStoreLocationStrategy());
                bconfig.setStoreLocationLayout(uoptions.getStoreLocationLayout());
                bconfig.setGlobal(uoptions.isGlobal());
                bconfig.setStoreLocations(new NutsStoreLocationsMap(uoptions.getStoreLocations()).toMapOrNull());
                bconfig.setHomeLocations(new NutsHomeLocationsMap(uoptions.getHomeLocations()).toMapOrNull());

                boolean namedWorkspace = CoreNutsUtils.isValidWorkspaceName(uoptions.getWorkspace());
                if (bconfig.getStoreLocationStrategy() == null) {
                    bconfig.setStoreLocationStrategy(namedWorkspace ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
                }
                if (bconfig.getRepositoryStoreLocationStrategy() == null) {
                    bconfig.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                }
                bconfig.setName(CoreNutsUtils.resolveValidWorkspaceName(uoptions.getWorkspace()));

                configModel.setCurrentConfig(new DefaultNutsWorkspaceCurrentConfig(this)
                        .merge(aconfig, defaultSession())
                        .merge(bconfig, defaultSession())
                        .build(locations().getWorkspaceLocation(), defaultSession()));
//                NutsUpdateOptions updateOptions = new NutsUpdateOptions().setSession(session);
                configModel.setConfigBoot(bconfig, defaultSession());
                configModel.setConfigApi(aconfig, defaultSession());
                configModel.setConfigRuntime(rconfig, defaultSession());
                initializeWorkspace(uoptions.getArchetype(), defaultSession());
                if (!_config.isReadOnly()) {
                    _config.save();
                }
                String nutsVersion = getRuntimeId().getVersion().toString();
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS).log("nuts workspace v{0} created.", nutsVersion);
                }
                //should install default
                if (defaultSession().isPlainTrace() && !_env.getBootOptions().isSkipWelcome()) {
                    NutsPrintStream out = defaultSession().out();
                    out.resetLine();
                    StringBuilder version = new StringBuilder(nutsVersion);
                    CoreStringUtils.fillString(' ', 25 - version.length(), version);
                    NutsTextManager txt = text.setSession(defaultSession());
                    NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/includes/standard-header.ntf",
                            txt.parser().createLoader(getClass().getClassLoader())
                    );
                    out.println(n == null ? "no help found" : n.toString().trim());
                    out.println(
                            txt.builder()
                                    .append("location", NutsTextStyle.underlined())
                                    .append(":")
                                    .append(locations().getWorkspaceLocation(), NutsTextStyle.path())
                                    .append(" ")
                                    .append(" (")
                                    .append(getHashName())
                                    .append(")")
                    );
                    out.println(
                            CoreNutsUtils.createBox(txt,
                                    txt.builder()
                                            .append("This is the very first time ")
                                            .appendCode("sh", "nuts")
                                            .append(" has been started for this workspace")
                            )
                    );
                    out.println();
                }
                for (URL bootClassWorldURL : _env.getBootClassWorldURLs()) {
                    NutsInstalledRepository repo = getInstalledRepository();
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(defaultSession()).repoSPI(repo);
                    NutsDeployRepositoryCommand desc = repoSPI.deploy()
                            .setContent(bootClassWorldURL)
                            //.setFetchMode(NutsFetchMode.LOCAL)
                            .setSession(defaultSession().copy().setConfirm(NutsConfirmationMode.YES))
                            .run();
                    if (desc.getId().getLongNameId().equals(getApiId().getLongNameId())
                            || desc.getId().getLongNameId().equals(getRuntimeId().getLongNameId())) {
                        repo.install(desc.getId(), defaultSession(), null);
                    } else {
                        repo.install(desc.getId(), defaultSession(), getRuntimeId());
                    }
                }

            } else {
                uuid = configModel.getStoreModelBoot().getUuid();
                if (CoreStringUtils.isBlank(uuid)) {
                    uuid = UUID.randomUUID().toString();
                    configModel.getStoreModelBoot().setUuid(uuid);
                }
                if (uoptions.isRecover()) {
                    configModel.setBootApiVersion(cfg.getApiVersion(), defaultSession());
                    configModel.setBootRuntimeId(cfg.getRuntimeId(), defaultSession());
                    configModel.setBootRuntimeDependencies(
                            Arrays.stream(info.getRuntimeBootDescriptor().getDependencies())
                                    .map(x -> x.toString())
                                    .collect(Collectors.joining(";")),
                            defaultSession());
                    configModel.setBootRepositories(cfg.getBootRepositories(), defaultSession());
                    try {
                        install().setInstalled(true).setSession(defaultSession()).getResult();
                    } catch (Exception ex) {
                        LOG.with().session(defaultSession()).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                                .log("reinstall artifacts failed : " + CoreStringUtils.exceptionToString(ex));
                        LOG.with().session(defaultSession()).level(Level.FINEST).verb(NutsLogVerb.FAIL)
                                .log("reinstall artifacts failed : " + CoreStringUtils.exceptionToString(ex), ex);
                    }
                }
                if (repos().setSession(defaultSession()).getRepositories().length == 0) {
                    LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NutsLogVerb.FAIL).log("workspace has no repositories. Will re-create defaults");
                    initializeWorkspace(uoptions.getArchetype(), defaultSession());
                }
                List<String> transientRepositoriesSet = uoptions.getRepositories() == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(uoptions.getRepositories()));
                NutsRepositorySelector.SelectorList expected = NutsRepositorySelector.parse(transientRepositoriesSet.toArray(new String[0]));
                for (NutsRepositorySelector.Selection loc : expected.resolveSelectors(null)) {
                    NutsAddRepositoryOptions d = NutsRepositorySelector.createRepositoryOptions(loc, false, defaultSession());
                    String n = d.getName();
                    String ruuid = (CoreStringUtils.isBlank(n) ? "temporary" : n) + "_" + UUID.randomUUID().toString().replace("-", "");
                    d.setName(ruuid);
                    d.setTemporary(true);
                    d.setEnabled(true);
                    d.setFailSafe(false);
                    if (d.getConfig() != null) {
                        d.getConfig().setName(CoreStringUtils.isBlank(n) ? ruuid : n);
                        d.getConfig().setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                    }
                    repos().setSession(defaultSession()).addRepository(d);
                }
            }

            configModel.prepareBoot(false, defaultSession());
            if (!_config.isReadOnly()) {
                _config.save(false);
            }
            configModel.setStartCreateTimeMillis(uoptions.getCreationTime());
            configModel.setEndCreateTimeMillis(System.currentTimeMillis());
            if (justInstalled) {
                if (!_env.getBootOptions().isSkipCompanions()) {
                    installCompanions(defaultSession());
                }
                DefaultNutsWorkspaceEvent workspaceCreatedEvent = new DefaultNutsWorkspaceEvent(defaultSession(), null, null, null, null);
                for (NutsWorkspaceListener workspaceListener : events().getWorkspaceListeners()) {
                    workspaceListener.onCreateWorkspace(workspaceCreatedEvent);
                }
            }
//            if (session.isPlainTrace()) {
//                PrintStream out = session.out();
//                io().term().sendTerminalCommand(out, NutsTerminalManager.MOVE_LINE_START);
//                out.printf("workspace is %s!%n", formats().text().builder().append("ready"));
//            }
            if (uoptions.getUserName() != null && uoptions.getUserName().trim().length() > 0) {
                char[] password = uoptions.getCredentials();
                if (CoreStringUtils.isBlank(password)) {
                    password = term().setSession(defaultSession()).getTerminal().readPassword("Password : ");
                }
                this.security().setSession(defaultSession()).login(uoptions.getUserName(), password);
            }
            LOG.with().session(defaultSession()).level(Level.FINE).verb(NutsLogVerb.SUCCESS)
                    .formatted().log("```sh nuts``` workspace loaded in ```error {0}```",
                            CoreTimeUtils.formatPeriodMilli(_env.getCreationFinishTimeMillis() - _env.getCreationStartTimeMillis())
                    );

            if (CoreBooleanUtils.getSysBoolNutsProperty("perf", false)) {
                defaultSession().out().printf("```sh nuts``` workspace loaded in %s%n",
                        configModel.getWorkspace().text().forStyled(CoreTimeUtils.formatPeriodMilli(_env.getCreationFinishTimeMillis() - _env.getCreationStartTimeMillis()),
                                NutsTextStyle.error()
                        )
                );
            }
        } finally {
            initializing = false;
        }
//        return !exists;
    }

    public void installCompanions(NutsSession session) {
        NutsWorkspaceUtils.checkSession(this, session);
        NutsTextManager text = text().setSession(session);
        if (session.isPlainTrace()) {
            NutsPrintStream out = session.out();
            Set<NutsId> companionIds = getCompanionIds(session);
            out.resetLine();
            out.printf("looking for recommended companion tools to install... detected : %s%n",
                    text.builder().appendJoined(text.forPlain(","),
                            companionIds
                    )
            );
        }
        try {
            install().companions().setSession(session.copy().setTrace(session.isTrace() && session.isPlainOut()))
                    .addConditionalArgs(d -> d.getId().getShortName().equals("net.thevpc.nuts:nadmin")
                                    && env().setSession(session).getBootOptions().getSwitchWorkspace() != null,
                            "--switch=" + env().setSession(session).getBootOptions().getSwitchWorkspace())
                    .run();
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).error(ex).log("unable to install companions : " + ex.toString());
            if (session.isPlainTrace()) {
                NutsPrintStream out = session.out();
                out.resetLine();
                out.printf("```error unable to install companion tools``` :  %s \n"
                                + "this happens when none of the following repositories are able to locate them : %s\n",
                        ex,
                        text.builder().appendJoined(text.forPlain(", "),
                                Arrays.stream(repos().setSession(session).getRepositories()).map(x
                                        -> text.builder().append(x.getName(), NutsTextStyle.primary3())
                                ).collect(Collectors.toList())
                        )
                );
            }
        }
    }

    protected NutsDescriptor _resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START).formatted()
                .log("resolve effective {0}", descriptor.getId());
        checkSession(session);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetch().setId(parents[i]).setEffective(false).setSession(session).getResultDescriptor(),
                    session
            );
        }
        NutsDescriptor effectiveDescriptor = descriptor.builder().applyParents(parentDescriptors).applyProperties().build();
        NutsDependency[] oldDependencies = effectiveDescriptor.getDependencies();
        List<NutsDependency> newDeps = new ArrayList<>();
        boolean someChange = false;

        for (NutsDependency d : oldDependencies) {
            if (CoreStringUtils.isBlank(d.getScope())
                    || d.getVersion().isBlank()
                    || CoreStringUtils.isBlank(d.getOptional())) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : effectiveDescriptor.getStandardDependencies()) {
                    if (standardDependency.getSimpleName().equals(d.toId().getShortName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (CoreStringUtils.isBlank(d.getScope())
                            && !CoreStringUtils.isBlank(standardDependencyOk.getScope())) {
                        someChange = true;
                        d = d.builder().setScope(standardDependencyOk.getScope()).build();
                    }
                    if (CoreStringUtils.isBlank(d.getOptional())
                            && !CoreStringUtils.isBlank(standardDependencyOk.getOptional())) {
                        someChange = true;
                        d = d.builder().setOptional(standardDependencyOk.getOptional()).build();
                    }
                    if (d.getVersion().isBlank()
                            && !standardDependencyOk.getVersion().isBlank()) {
                        someChange = true;
                        d = d.builder().setVersion(standardDependencyOk.getVersion()).build();
                    }
                }
            }

            if ("import".equals(d.getScope())) {
                someChange = true;
                newDeps.addAll(Arrays.asList(fetch().setId(d.toId()).setEffective(true).setSession(session).getResultDescriptor().getDependencies()));
            } else {
                newDeps.add(d);
            }
        }
        if (someChange) {
            effectiveDescriptor = effectiveDescriptor.builder().setDependencies(newDeps.toArray(new NutsDependency[0])).build();
        }
        return effectiveDescriptor;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsWorkspaceOptions> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configModel
                + '}';
    }

    protected void initializeWorkspace(String archetype, NutsSession session) {
        checkSession(session);
        if (CoreStringUtils.isBlank(archetype)) {
            archetype = "default";
        }
        NutsWorkspaceArchetypeComponent instance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NutsWorkspaceArchetypeComponent ac : extensions().setSession(session).createAllSupported(NutsWorkspaceArchetypeComponent.class, archetype)) {
            if (archetype.equals(ac.getName())) {
                instance = ac;
                break;
            }
            validValues.add(ac.getName());
        }
        if (instance == null) {
            //get the default implementation
            throw new NutsException(session,
                    NutsMessage.cstyle("invalid archetype %s. Valid values are : %s", archetype, validValues)
            );
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        security().setSession(session).updateUser(NutsConstants.Users.ADMIN).setCredentials("admin".toCharArray()).run();

        instance.initializeWorkspace(session);

//        //isn't it too late for adding extensions?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_ID_BOOT_RUNTIME, session);
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, "Unable to loadWorkspace nuts-runtime. The tool is running in minimal mode.");
//        }
    }

    private void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(this, session);
    }

    public void installOrUpdateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session,
                                    boolean resolveInstaller, boolean updateDefaultVersion, InstallStrategy0 strategy0, boolean requireDependencies, NutsId[] forIds,
                                    NutsDependencyScope scope) {
        checkSession(session);
        if (def == null) {
            return;
        }
        NutsDependencyFilter ndf = CoreNutsDependencyUtils.createJavaRunDependencyFilter(session);
        if (!def.isSetEffectiveDescriptor() || def.getContent() == null) {
            // reload def
            NutsFetchCommand fetch2 = fetch()
                    .setSession(session)
                    .setId(def.getId())
                    .setContent(true)
                    .setRepositoryFilter(repos().filter().setSession(session).installedRepo())
                    .setFailFast(true);
            if (def.isSetDependencies()) {
                fetch2.setDependencyFilter(def.getDependencies().filter());
                fetch2.setDependencies(true);
            }
            def = fetch2.getResultDefinition();
        }


        boolean reinstall = false;
        NutsInstalledRepository installedRepository = getInstalledRepository();
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);

        if (session.isPlainTrace()) {
            NutsIdManager vid = id().setSession(session);
            NutsTextManager text = session.getWorkspace().text();
            if (strategy0 == InstallStrategy0.UPDATE) {
                session.out().resetLine().printf("%s %s ...%n",
                        text.forStyled("update", NutsTextStyle.warn()),
                        def.getId().getLongNameId()
                );
            } else if (strategy0 == InstallStrategy0.REQUIRE) {
                reinstall = def.getInstallInformation().getInstallStatus().isRequired();
                if (reinstall) {
                    //session.out().println("re-requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                } else {
                    //session.out().println("requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                }
            } else {
                reinstall = def.getInstallInformation().getInstallStatus().isInstalled();
                if (reinstall) {
                    session.out().resetLine().printf(
                            "%s %s ...%n",
                            text.forStyled("re-install", NutsTextStyles.of(NutsTextStyle.success(), NutsTextStyle.underlined())),
                            def.getId().getLongNameId()
                    );
                } else {
                    session.out().resetLine().printf("%s %s ...%n",
                            text.forStyled("install", NutsTextStyle.success()),
                            def.getId().getLongNameId()
                    );
                }
            }
        }
        NutsRepositorySPI installedRepositorySPI = wu.repoSPI(installedRepository);
        boolean remoteRepo = true;
        if (resolveInstaller) {
            if (installerComponent == null) {
                if (def.getPath() != null) {
                    installerComponent = getInstaller(def, session);
                }
            }
        }
//        checkSession(session);
        NutsDefinition oldDef = null;
        if (strategy0 == InstallStrategy0.UPDATE) {
            switch (def.getType()) {
                case API: {
                    oldDef = fetch().setSession(
                            CoreNutsUtils.silent(session).copy().setFetchStrategy(NutsFetchStrategy.ONLINE)
                    ).setId(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion()).setFailFast(false).getResultDefinition();
                    break;
                }
                case RUNTIME: {
                    oldDef = fetch().setSession(CoreNutsUtils.silent(session).copy().setFetchStrategy(NutsFetchStrategy.ONLINE)).setId(getRuntimeId())
                            .setFailFast(false).getResultDefinition();
                    break;
                }
                default: {
                    oldDef = search().setSession(CoreNutsUtils.silent(session)).addId(def.getId().getShortNameId())
                            .setInstallStatus(this.filters().setSession(session).installStatus().byDeployed(true))
                            .setFailFast(false).getResultDefinitions().first();
                    break;
                }
            }
        }
        NutsPrintStream out = session.out();
        out.flush();
        switch (def.getType()) {
            case API: {
                configModel.prepareBootApi(def.getId(), null, true, session);
                break;
            }
            case RUNTIME: {
                configModel.prepareBootRuntime(def.getId(), true, session);
                break;
            }
            case EXTENSION: {
                configModel.prepareBootExtension(def.getId(), true, session);
                break;
            }
        }
        NutsInstallInformation newNutsInstallInformation = null;
        NutsWorkspaceConfigManager config = config().setSession(session);
        if (def.getPath() != null) {
            if (requireDependencies) {
                def.getDependencies();
                List<NutsDefinition> requiredDefinitions = new ArrayList<>();
                //fetch required
                for (NutsDependency dependency : def.getDependencies()) {
                    if (ndf == null || ndf.acceptDependency(def.getId(), dependency, session)) {
                        if (!installedRepositorySPI.
                                searchVersions().setId(dependency.toId())
                                .setFetchMode(NutsFetchMode.LOCAL)
                                .setSession(session)
                                .getResult()
                                .hasNext()) {
                            NutsDefinition dd = search().addId(dependency.toId()).setContent(true).setLatest(true)
                                    //.setDependencies(true)
                                    .setEffective(true)
                                    .setSession(session)
                                    .getResultDefinitions().first();
                            if (dd != null) {
                                if (dd.getPath() == null) {
                                    throw new NutsInstallException(session, def.getId(),
                                            NutsMessage.cstyle("unable to install %s. required dependency content is missing for %s", def.getId(), dependency.toId())
                                            , null);
                                }
                                requiredDefinitions.add(dd);
                            }
                        }
                    }
                }
                //install required
                for (NutsDefinition dd : requiredDefinitions) {
                    requireImpl(dd,
                            session,
                            false, //transitive dependencies already evaluated
                            new NutsId[]{def.getId()});
                }
            }

//            installedRepositorySPI.deploy()
//                    .setId(def.getId())
//                    .setContent(def.getPath())
//                    //.setFetchMode(NutsFetchMode.LOCAL)
//                    .setSession(session)
//                    .setDescriptor(def.getDescriptor())
//                    .run();

            //should change def to reflect install location!

            NutsExecutionContextBuilder cc = createExecutionContext()
                    .setTraceSession(session.copy())
                    .setExecSession(session.copy())
                    .setDefinition(def).setArguments(args).setFailFast(true).setTemporary(false)
                    .setExecutionType(env().setSession(session).getBootOptions().getExecutionType());
            NutsArtifactCall installer = def.getDescriptor().getInstaller();
            if (installer != null) {
                cc.addExecutorArguments(installer.getArguments());
                cc.addExecutorProperties(installer.getProperties());
            }
            cc.setWorkspace(cc.getTraceSession().getWorkspace());
            NutsExecutionContext executionContext = cc.build();

            if (strategy0 == InstallStrategy0.REQUIRE) {
                newNutsInstallInformation = installedRepository.require(executionContext.getDefinition(), true, forIds, scope, session);
            } else if (strategy0 == InstallStrategy0.UPDATE) {
                newNutsInstallInformation = installedRepository.install(executionContext.getDefinition(), session);
            } else if (strategy0 == InstallStrategy0.INSTALL) {
                newNutsInstallInformation = installedRepository.install(executionContext.getDefinition(), session);
            }
            if (updateDefaultVersion) {
                installedRepository.setDefaultVersion(def.getId(), session);
            }

            //now should reload definition
            NutsFetchCommand fetch2 = fetch()
                    .setSession(session)
                    .setId(executionContext.getDefinition().getId())
                    .setContent(true)
                    .setRepositoryFilter(repos().filter().setSession(session).installedRepo())
                    .setFailFast(true);
            if (def.isSetDependencies()) {
                fetch2.setDependencyFilter(def.getDependencies().filter());
                fetch2.setDependencies(true);
            }
            NutsDefinition def2 = fetch2
                    .getResultDefinition();

            //update definition in the execution context
            cc.setDefinition(def2);
            executionContext = cc.build();
            NutsRepository rep = repos().setSession(session).findRepository(def.getRepositoryUuid());
            remoteRepo = rep == null || rep.isRemote();
            if (strategy0 == InstallStrategy0.REQUIRE) {
                //
            } else if (strategy0 == InstallStrategy0.UPDATE) {
                if (installerComponent != null) {
                    try {
                        installerComponent.update(executionContext);
                    } catch (NutsReadOnlyException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (session.isPlainTrace()) {
                            out.resetLine().printf("%s ```error failed``` to update : %s.%n", def.getId(), ex);
                        }
                        throw new NutsExecutionException(session,
                                NutsMessage.cstyle("unable to update %s", def.getId())
                                , ex);
                    }
                }
            } else if (strategy0 == InstallStrategy0.INSTALL) {
                if (installerComponent != null) {
                    try {
                        installerComponent.install(executionContext);
//                    out.print(getFormatManager().parseList().print(def.getId()) + " installed ##successfully##.\n");
                    } catch (NutsReadOnlyException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (session.isPlainTrace()) {
                            out.resetLine().printf("```error error: failed to install``` %s: %s.%n", def.getId(), ex);
                        }
                        try {
                            installedRepository.uninstall(executionContext.getDefinition(), session);
                        } catch (Exception ex2) {
                            LOG.with().session(session).level(Level.FINE).error(ex).formatted().log("failed to uninstall  {0}", executionContext.getDefinition().getId());
                            //ignore if we could not uninstall
                        }
                        throw new NutsExecutionException(session, NutsMessage.cstyle("unable to install %s", def.getId()), ex);
                    }
                }
            }
        } else {
            throw new NutsExecutionException(session,
                    NutsMessage.cstyle("unable to install %s: unable to locate content", def.getId())
                    , 101);
        }

//        if (def.getInstallInformation() instanceof DefaultNutsInstallInfo) {
//            DefaultNutsInstallInfo t = (DefaultNutsInstallInfo) def.getInstallInformation();
//            t.setJustInstalled(true);
//            t.setJustReInstalled(reinstall);
//        }

        if (strategy0 == InstallStrategy0.UPDATE) {
            wu.events().fireOnUpdate(new DefaultNutsUpdateEvent(oldDef, def, session, reinstall));
        } else if (strategy0 == InstallStrategy0.REQUIRE) {
            wu.events().fireOnRequire(new DefaultNutsInstallEvent(def, session, forIds, reinstall));
        } else if (strategy0 == InstallStrategy0.INSTALL) {
            wu.events().fireOnInstall(new DefaultNutsInstallEvent(def, session, new NutsId[0], reinstall));
        }

        if (def.getType() == NutsIdType.EXTENSION) {
            NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(config);
            NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getModel().getStoredConfigBoot().getExtensions())
                    .save();
            h.add(def.getId());
            wcfg.getModel().getStoredConfigBoot().setExtensions(h.getConfs());
            wcfg.getModel().fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
            wcfg.getModel().prepareBootExtension(def.getId(), true, session);
        }

        if (session.isPlainTrace()) {
            String setAsDefaultString = "";
            NutsTextManager text = text().setSession(session);
            if (updateDefaultVersion) {
                setAsDefaultString = " set as " + text.builder().append("default", NutsTextStyle.primary1()) + ".";
            }
            if (newNutsInstallInformation != null
                    && (newNutsInstallInformation.isJustInstalled()
                    || newNutsInstallInformation.isJustRequired())) {
                NutsText installedString = null;
                if (newNutsInstallInformation != null) {
                    if (newNutsInstallInformation.isJustReInstalled()) {
                        installedString = text.forStyled("re-install", NutsTextStyles.of(NutsTextStyle.success(), NutsTextStyle.underlined()));
                    } else if (newNutsInstallInformation.isJustInstalled()) {
                        installedString = text.forStyled("install", NutsTextStyle.success());
                    } else if (newNutsInstallInformation.isJustReRequired()) {
                        installedString = text.forStyled("re-require", NutsTextStyles.of(NutsTextStyle.info(), NutsTextStyle.underlined()));
                    } else if (newNutsInstallInformation.isJustRequired()) {
                        installedString = text.forStyled("require", NutsTextStyle.info());
                    }
                }
                if (installedString != null) {
                    //(reinstalled ? "re-installed" : "installed")
                    if (!def.getContent().isCached()) {
                        if (def.getContent().isTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s) temporarily file %s.%s%n",
                                        installedString,
                                        def.getId().getLongNameId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getLocation(),
                                        text.parse(setAsDefaultString)
                                );
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s).%s%n", installedString,
                                        def.getId().getLongNameId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        text.parse(setAsDefaultString));
                            }
                        }
                    } else {
                        if (def.getContent().isTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s) temporarily file %s.%s%n",
                                        installedString,
                                        def.getId().getLongNameId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getLocation(),
                                        text.parse(setAsDefaultString));
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s).%s%n",
                                        installedString,
                                        def.getId().getLongNameId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        text.parse(setAsDefaultString)
                                );
                            }
                        }
                    }
                }
            } else {
                String installedString = null;
                if (newNutsInstallInformation != null) {
                    if (newNutsInstallInformation.isJustReInstalled()) {
                        installedString = "re-installed";
                    } else if (newNutsInstallInformation.isJustInstalled()) {
                        installedString = "installed";
                    } else if (newNutsInstallInformation.isJustReRequired()) {
                        installedString = "re-required";
                    } else if (newNutsInstallInformation.isJustRequired()) {
                        installedString = "required";
                    }
                }
                if (installedString != null) {
                    if (session.isPlainTrace()) {
                        out.resetLine().printf("%s  %s %s.%s%n",
                                installedString,
                                def.getId().getLongNameId(),
                                text.forStyled("successfully", NutsTextStyle.success()),
                                text.parse(setAsDefaultString)
                        );
                    }
                }
            }
        }
    }

    public String resolveCommandName(NutsId id, NutsSession session) {
        checkSession(session);
        String nn = id.getArtifactId();
        NutsCustomCommandManager aliases = commands().setSession(session);
        NutsWorkspaceCustomCommand c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getArtifactId() + "-" + id.getVersion();
        c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroupId() + "." + id.getArtifactId() + "-" + id.getVersion();
        c = aliases.findCommand(nn);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        throw new NutsElementNotFoundException(session,
                NutsMessage.cstyle("unable to resolve command name for %s", id
                ));
    }

    protected boolean loadWorkspace(NutsSession session, String[] excludedExtensions, String[] excludedRepositories) {
        checkSession(session);
        if (configModel.loadWorkspace(session)) {
            //extensions already wired... this is needless!
            for (NutsId extensionId : extensions().setSession(session).getConfigExtensions()) {
                if (extensionModel.isExcludedExtension(extensionId)) {
                    continue;
                }
                NutsSession sessionCopy = session.copy();
                extensionModel.wireExtension(extensionId,
                        fetch().setSession(sessionCopy
                                .copy()
                                .setFetchStrategy(NutsFetchStrategy.ONLINE)
                                .setTransitive(true)
                        )
                );
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }
            NutsUserConfig adminSecurity = NutsWorkspaceConfigManagerExt.of(config())
                    .getModel()
                    .getUser(NutsConstants.Users.ADMIN, session);
            if (adminSecurity == null || CoreStringUtils.isBlank(adminSecurity.getCredentials())) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL).log(NutsConstants.Users.ADMIN + " user has no credentials. reset to default");
                }
                security().setSession(session)
                        .updateUser(NutsConstants.Users.ADMIN).credentials("admin".toCharArray())
                        .run();
            }
            for (NutsCommandFactoryConfig commandFactory : commands().setSession(session).getCommandFactories()) {
                try {
                    commands().setSession(session).addCommandFactory(commandFactory);
                } catch (Exception e) {
                    LOG.with().session(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("unable to instantiate Command Factory {0}", commandFactory);
                }
            }
            DefaultNutsWorkspaceEvent worksppaeReloadedEvent = new DefaultNutsWorkspaceEvent(session, null, null, null, null);
            for (NutsWorkspaceListener listener : events().getWorkspaceListeners()) {
                listener.onReloadWorkspace(worksppaeReloadedEvent);
            }
            //if save is needed, will be applied
            //config().save(false, session);
            return true;
        }
        return false;
    }

    @Override
    public String getWelcomeText(NutsSession session) {
        NutsTextManager txt = text().setSession(session);
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-welcome.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no welcome found" : n.toString());
    }

    @Override
    public String getHelpText(NutsSession session) {
        NutsTextManager txt = text().setSession(session);
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-help.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no help found" : n.toString());
    }

    @Override
    public String getLicenseText(NutsSession session) {
        NutsTextManager txt = text().setSession(session);
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-license.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no license found" : n.toString());
    }

    @Override
    public String resolveDefaultHelp(Class clazz, NutsSession session) {
        NutsId nutsId = id().setSession(session).resolveId(clazz);
        if (nutsId != null) {
            String urlPath = "/" + nutsId.getGroupId().replace('.', '/') + "/" + nutsId.getArtifactId() + ".ntf";

            NutsTextManager txt = text().setSession(session);
            NutsText n = txt.parser().parseResource(urlPath,
                    txt.parser().createLoader(getClass().getClassLoader())
            );
            return (n == null ? "no license found" : n.toString());
        }
        return null;
    }

    @Override
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession session) {
        checkSession(session);
        if (descriptor == null) {
            throw new NutsNotFoundException(session, null);
        }
        NutsId thisId = descriptor.getId();
//        if (CoreNutsUtils.isEffectiveId(thisId)) {
//            return thisId.setAlternative(descriptor.getAlternative());
//        }
        String a = thisId.getArtifactId();
        String g = thisId.getGroupId();
        String v = thisId.getVersion().getValue();
        if ((CoreStringUtils.isBlank(g)) || (CoreStringUtils.isBlank(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = fetch().setSession(session).setId(parent).setEffective(true).getResultId();
                if (CoreStringUtils.isBlank(g)) {
                    g = p.getGroupId();
                }
                if (CoreStringUtils.isBlank(v)) {
                    v = p.getVersion().getValue();
                }
                if (!CoreStringUtils.isBlank(g) && !CoreStringUtils.isBlank(v)) {
                    break;
                }
            }
            if (CoreStringUtils.isBlank(g) || CoreStringUtils.isBlank(v)) {
                throw new NutsNotFoundException(session, thisId,
                        NutsMessage.cstyle("unable to fetchEffective for %s. best Result is %s", thisId, thisId)
                        , null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v) || CoreStringUtils.containsVars(a)) {
            Map<String, String> p = descriptor.getProperties();
            NutsId bestId = id().builder().setGroupId(g).setArtifactId(thisId.getArtifactId()).setVersion(v).build();
            bestId = bestId.builder().apply(new MapToFunction(p)).build();
            if (CoreNutsUtils.isEffectiveId(bestId)) {
                return bestId;
            }
            Stack<NutsId> all = new Stack<>();
            NutsId[] parents = descriptor.getParents();
            all.addAll(Arrays.asList(parents));
            while (!all.isEmpty()) {
                NutsId parent = all.pop();
                NutsDescriptor dd = fetch().setSession(session).setId(parent).setEffective(true).getResultDescriptor();
                bestId = bestId.builder().apply(new MapToFunction(dd.getProperties())).build();
                if (CoreNutsUtils.isEffectiveId(bestId)) {
                    return bestId;
                }
                all.addAll(Arrays.asList(dd.getParents()));
            }
            throw new NutsNotFoundException(session, bestId,
                    NutsMessage.cstyle("unable to fetchEffective for %s. best Result is %s", bestId, bestId), null);
        }
        NutsId bestId = id().setSession(session).builder().setGroupId(g).setArtifactId(thisId.getArtifactId()).setVersion(v).build();
        if (!CoreNutsUtils.isEffectiveId(bestId)) {
            throw new NutsNotFoundException(session, bestId,
                    NutsMessage.cstyle("unable to fetchEffective for %s. best Result is %s", thisId, bestId), null);
        }
//        return bestId.setAlternative(descriptor.getAlternative());
        return bestId;
    }

    @Override
    public NutsIdType resolveNutsIdType(NutsId id, NutsSession session) {
        NutsIdType idType = NutsIdType.REGULAR;
        String shortName = id.getShortName();
        if (shortName.equals(NutsConstants.Ids.NUTS_API)) {
            idType = NutsIdType.API;
        } else if (shortName.equals(NutsConstants.Ids.NUTS_RUNTIME)) {
            idType = NutsIdType.RUNTIME;
        } else {
            for (NutsId companionTool : getCompanionIds(session)) {
                if (companionTool.getShortName().equals(shortName)) {
                    idType = NutsIdType.COMPANION;
                }
            }
        }
        return idType;
    }

    @Override
    public NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session) {
        checkSession(session);
        if (nutToInstall != null && nutToInstall.getPath() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsArtifactCall installerDescriptor = descriptor.getInstaller();
            NutsDefinition runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetch().setId(installerDescriptor.getId())
                            .setSession(
                                    session.copy().setTransitive(false)
                            )
                            .setOptional(false)
                            .setContent(true)
                            .setDependencies(true)
                            .getResultDefinition();
                }
            }
            if (runnerFile == null) {
                runnerFile = nutToInstall;
            }
            NutsInstallerComponent best = extensions().setSession(session).createSupported(NutsInstallerComponent.class, runnerFile);
            if (best != null) {
                return best;
            }
        }
        return new CommandForIdNutsInstallerComponent();
    }

    @Override
    public void requireImpl(NutsDefinition def, NutsSession session, boolean withDependencies, NutsId[] forId) {
        installOrUpdateImpl(def, new String[0], null, session, true, false, InstallStrategy0.REQUIRE, withDependencies, forId, null);
    }

    @Override
    public void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, installerComponent, session, true, updateDefaultVersion, InstallStrategy0.INSTALL, true, null, null);
    }

    @Override
    public void updateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, installerComponent, session, true, updateDefaultVersion, InstallStrategy0.UPDATE, true, null, null);
    }

    /**
     * true when core extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : core extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_RUNTIME_EXTENSION</code> is forced to
     * false
     *
     * @param session session
     * @return true when core extension is required for running this workspace
     */
    @Override
    public boolean requiresRuntimeExtension(NutsSession session) {
        boolean coreFound = false;
        for (NutsId ext : extensions().setSession(session).getConfigExtensions()) {
            if (ext.equalsShortName(getRuntimeId())) {
                coreFound = true;
                break;
            }
        }
        return !coreFound;
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        Path eff = null;
        NutsWorkspaceLocationManager loc = locations().setSession(session);
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            Path l = Paths.get(loc.getStoreLocation(descriptor.getId(), NutsStoreLocation.CACHE));
            String nn = loc.getDefaultIdFilename(descriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
            if (Files.isRegularFile(eff)) {
                try {
                    NutsDescriptor d = descriptor().setSession(session).parser().setSession(session).parse(eff);
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.FINE).error(ex).log("failed to parse {0}", eff);
                    //
                }
            }
        } else {
            //
        }
        NutsDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor, session);
        if (eff == null) {
            Path l = Paths.get(locations().getStoreLocation(effectiveDescriptor.getId(), NutsStoreLocation.CACHE));
            String nn = loc.getDefaultIdFilename(effectiveDescriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
        }
        try {
            descriptor().setSession(session).formatter(effectiveDescriptor).setNtf(false).print(eff);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.FINE).error(ex).log("failed to print {0}", eff);
            //
        }
        return effectiveDescriptor;
    }

    @Override
    public NutsInstalledRepository getInstalledRepository() {
        return installedRepository;
    }

    @Override
    public NutsInstallStatus getInstallStatus(NutsId id, boolean checkDependencies, NutsSession session) {
        session = NutsWorkspaceUtils.of(session).validateSilentSession(session);
        NutsDefinition nutToInstall;
        try {
            nutToInstall = search().addId(id)
                    .setSession(session.copy().setTransitive(false))
                    .setInlineDependencies(checkDependencies)
                    .setInstallStatus(this.filters().installStatus().byDeployed(true))
                    .setOptional(false)
                    .getResultDefinitions().first();
            if (nutToInstall == null) {
                return NutsInstallStatus.NONE;
            }
        } catch (NutsNotFoundException e) {
            return NutsInstallStatus.NONE;
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex).log("error: %s", ex);
            return NutsInstallStatus.NONE;
        }
        return getInstalledRepository().getInstallStatus(nutToInstall.getId(), session);
    }

    @Override
    public NutsExecutionContextBuilder createExecutionContext() {
        return new DefaultNutsExecutionContextBuilder().setWorkspace(this);
    }

    @Override
    public void deployBoot(NutsSession session, NutsId id, boolean withDependencies) {
        Map<NutsId, NutsDefinition> defs = new HashMap<>();
        NutsDefinition m = fetch().setId(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
        Map<String, String> a = new LinkedHashMap<>();
        a.put("configVersion", Nuts.getVersion());
        a.put("id", id.getLongName());
        a.put("dependencies", m.getDependencies().stream().map(NutsDependency::getLongName).collect(Collectors.joining(";")));
        defs.put(m.getId().getLongNameId(), m);
        if (withDependencies) {
            for (NutsDependency dependency : m.getDependencies()) {
                if (!defs.containsKey(dependency.toId().getLongNameId())) {
                    m = fetch().setId(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
                    defs.put(m.getId().getLongNameId(), m);
                }
            }
        }
        for (NutsDefinition def : defs.values()) {
            Path bootstrapFolder = Paths.get(locations().getStoreLocation(NutsStoreLocation.LIB)).resolve(NutsConstants.Folders.ID);
            NutsId id2 = def.getId();
            this.io().copy().setSession(session).from(def.getPath())
                    .to(bootstrapFolder.resolve(locations().getDefaultIdBasedir(id2))
                            .resolve(locations().getDefaultIdFilename(id2.builder().setFaceContent().setPackaging("jar").build()))
                    ).run();
            this.descriptor().formatter(this.fetch().setId(id2).getResultDescriptor()).setNtf(false)
                    .print(bootstrapFolder.resolve(locations().getDefaultIdBasedir(id2))
                            .resolve(locations().getDefaultIdFilename(id2.builder().setFaceDescriptor().build())));

            Map<String, String> pr = new LinkedHashMap<>();
            pr.put("file.updated.date", Instant.now().toString());
            pr.put("project.id", def.getId().getShortNameId().toString());
            pr.put("project.name", def.getId().getShortNameId().toString());
            pr.put("project.version", def.getId().getVersion().toString());
            pr.put("repositories", "~/.m2/repository"
                    + ";" + NutsRepositorySelector.createRepositoryOptions(NutsRepositorySelector.parseSelection("vpc-public-maven"), true, session).getConfig().getLocation()
                    + ";" + NutsRepositorySelector.createRepositoryOptions(NutsRepositorySelector.parseSelection("maven-central"), true, session).getConfig().getLocation()
                    + ";" + NutsRepositorySelector.createRepositoryOptions(NutsRepositorySelector.parseSelection("vpc-public-nuts"), true, session).getConfig().getLocation()
            );
//            pr.put("bootRuntimeId", runtimeUpdate.getAvailable().getId().getLongName());
            pr.put("project.dependencies.compile",
                    String.join(";",
                            def.getDependencies().stream()
                                    .filter(x -> !x.isOptional() &&
                                            CoreNutsDependencyUtils.createJavaRunDependencyFilter(session)
                                                    .acceptDependency(def.getId(), x, session))
                                    .map(x -> x.toId().getLongName())
                                    .collect(Collectors.toList())
                    )
            );

            try (Writer writer = Files.newBufferedWriter(
                    bootstrapFolder.resolve(this.locations().getDefaultIdBasedir(def.getId().getLongNameId()))
                            .resolve("nuts.properties")
            )) {
                CoreIOUtils.storeProperties(pr, writer, false);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHashName() {
        return config().getHashName(this);
    }

    @Override
    public NutsVersion getApiVersion() {
        return apiVersion;
    }

    @Override
    public NutsId getApiId() {
        return apiId;
    }

    @Override
    public NutsId getRuntimeId() {
        return runtimeId;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public Set<NutsId> getCompanionIds(NutsSession session) {
        NutsWorkspaceUtils.checkSession(this, session);
        NutsIdParser parser = id().setSession(session).parser();
        return Collections.unmodifiableSet(new HashSet<>(
                        Arrays.asList(parser.parse("net.thevpc.nuts.toolbox:nsh"),
                                parser.parse("net.thevpc.nuts.toolbox:nadmin")
                                //            "net.thevpc.nuts.toolbox:mvn"
                        )
                )
        );
    }

    @Override
    public NutsSearchCommand search() {
        return new DefaultNutsSearchCommand(this);
    }

    @Override
    public NutsFetchCommand fetch() {
        return new DefaultNutsFetchCommand(this);
    }

    @Override
    public NutsDeployCommand deploy() {
        return new DefaultNutsDeployCommand(this);
    }
//    public NutsExecutionContext createNutsExecutionContext(
//            NutsDefinition def,
//            String[] args,
//            String[] executorArgs,
//            NutsSession traceSession,
//            NutsSession execSession,
//            boolean failFast,
//            boolean temporary,
//            NutsExecutionType executionType,
//            String commandName
//            ,long sleepMillis
//    ) {
//        if (commandName == null) {
//            commandName = resolveCommandName(def.getId(), traceSession);
//        }
//        NutsDescriptor descriptor = def.getDescriptor();
//        NutsArtifactCall installer = descriptor.getInstaller();
//        List<String> eargs = new ArrayList<>();
//        List<String> aargs = new ArrayList<>();
//        Map<String, String> props = null;
//        if (installer != null) {
//            if (installer.getArguments() != null) {
//                eargs.addAll(Arrays.asList(installer.getArguments()));
//            }
//            props = installer.getProperties();
//        }
//        if (executorArgs != null) {
//            eargs.addAll(Arrays.asList(executorArgs));
//        }
//        if (args != null) {
//            aargs.addAll(Arrays.asList(args));
//        }
//        Path installFolder = locations().getStoreLocation(def.getId(), NutsStoreLocation.APPS);
//        Map<String, String> env = new LinkedHashMap<>();
//        return new DefaultNutsExecutionContext(def, aargs.resolveSelectors(new String[0]), eargs.resolveSelectors(new String[0]), env, props, installFolder.toString(),
//                traceSession,
//                execSession, this, failFast, temporary, executionType, commandName,sleepMillis);
//    }

    @Override
    public NutsUndeployCommand undeploy() {
        return new DefaultNutsUndeployCommand(this);
    }

    @Override
    public NutsExecCommand exec() {
        return new DefaultNutsExecCommand(this);
    }

    @Override
    public NutsInstallCommand install() {
        return new DefaultNutsInstallCommand(this);
    }

    @Override
    public NutsUninstallCommand uninstall() {
        return new DefaultNutsUninstallCommand(this);
    }

    @Override
    public NutsUpdateCommand update() {
        return new DefaultNutsUpdateCommand(this);
    }

    @Override
    public NutsPushCommand push() {
        return new DefaultNutsPushCommand(this);
    }

    @Override
    public NutsUpdateStatisticsCommand updateStatistics() {
        return new DefaultNutsUpdateStatisticsCommand(this);
    }

    @Override
    public NutsWorkspaceAppsManager apps() {
        return new DefaultNutsWorkspaceAppsManager(this);
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return new DefaultNutsWorkspaceExtensionManager(extensionModel);
    }

    @Override
    public NutsWorkspaceConfigManager config() {
        return new DefaultNutsWorkspaceConfigManager(configModel);
    }

    @Override
    public NutsRepositoryManager repos() {
        return new DefaultNutsRepositoryManager(repositoryModel);
    }

    @Override
    public NutsWorkspaceSecurityManager security() {
        return new DefaultNutsWorkspaceSecurityManager(securityModel);
    }

    @Override
    public NutsFilterManager filters() {
        return new DefaultNutsFilterManager(filtersModel);
    }

    @Override
    public NutsIOManager io() {
        return new DefaultNutsIOManager(ioModel);
    }

    @Override
    public NutsLogManager log() {
        return new DefaultNutsLogManager(logModel);
    }

    @Override
    public NutsWorkspaceEventManager events() {
        return new DefaultNutsWorkspaceEventManager(eventsModel);
    }

    @Override
    public NutsCommandLineManager commandLine() {
        return new DefaultNutsCommandLineManager(this);
    }

    @Override
    public NutsIdManager id() {
        return new DefaultNutsIdManager(this);
    }

    @Override
    public NutsVersionManager version() {
        return new DefaultNutsVersionManager(this);
    }

    @Override
    public NutsInfoFormat info() {
        return new DefaultNutsInfoFormat(this);
    }

    @Override
    public NutsDescriptorManager descriptor() {
        return new DefaultNutsDescriptorManager(this);
    }

    @Override
    public NutsDependencyManager dependency() {
        return new DefaultNutsDependencyManager(this);
    }

    @Override
    public NutsFormatManager formats() {
        return new DefaultNutsFormatManager(this, textModel);
    }

    @Override
    public NutsConcurrentManager concurrent() {
        return new DefaultNutsConcurrentManager(concurrentModel);
    }

    @Override
    public NutsSdkManager sdks() {
        return new DefaultNutsSdkManager(sdkModel);
    }

    @Override
    public NutsImportManager imports() {
        return new DefaultImportManager(importModel);
    }

    @Override
    public NutsCustomCommandManager commands() {
        return new DefaultCustomCommandManager(aliasesModel);
    }

    @Override
    public NutsWorkspaceLocationManager locations() {
        return new DefaultNutsWorkspaceLocationManager(locationsModel);
    }

    @Override
    public NutsWorkspaceEnvManager env() {
        return new DefaultNutsWorkspaceEnvManager(envModel);
    }

    @Override
    public NutsBootManager boot() {
        return new DefaultNutsBootManager(bootModel);
    }

    @Override
    public NutsTerminalManager term() {
        return new DefaultNutsTerminalManager(termModel);
    }

    @Override
    public NutsTextManager text() {
        return new DefaultNutsTextManager(this, textModel);//.setSession(getSession());
    }

    @Override
    public NutsElementFormat elem() {
        return new DefaultNutsElementFormat(textModel);
    }

    public DefaultNutsWorkspaceEnvManagerModel getEnvModel() {
        return envModel;
    }

    public DefaultCustomCommandsModel getAliasesModel() {
        return aliasesModel;
    }

    public DefaultNutsWorkspaceConfigModel getConfigModel() {
        return configModel;
    }

    public DefaultImportModel getImportModel() {
        return importModel;
    }

    public enum InstallStrategy0 implements NutsEnum {
        INSTALL,
        UPDATE,
        REQUIRE;
        private String id;

        InstallStrategy0() {
            this.id = name().toLowerCase().replace('_', '-');
        }

        @Override
        public String id() {
            return id;
        }
    }
}
