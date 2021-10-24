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
import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.boot.NutsBootDescriptor;
import net.thevpc.nuts.boot.NutsBootId;
import net.thevpc.nuts.boot.NutsBootVersion;
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
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
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
import net.thevpc.nuts.runtime.core.model.DefaultNutsProperties;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsVersionParser;
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
import net.thevpc.nuts.runtime.standalone.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.deploy.DefaultNutsDeployCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultNutsExecCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.fetch.DefaultNutsFetchCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.install.DefaultNutsInstallCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.push.DefaultNutsPushCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.search.DefaultNutsSearchCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.DefaultNutsUpdateStatisticsCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.undeploy.DefaultNutsUndeployCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.uninstall.DefaultNutsUninstallCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.update.DefaultNutsUpdateCommand;
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
    private DefaultNutsUtilModel utilModel;

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
        try {
            info = new CoreNutsWorkspaceInitInformation(info, this, null);
            this.uuid = info.getUuid();
            this.bootModel = new DefaultNutsBootModel(this, info);
            ((CoreNutsWorkspaceInitInformation) info).setSession(defaultSession());
            this.utilModel = new DefaultNutsUtilModel(this);
            this.ioModel = new DefaultNutsIOModel(this, bootModel);
            this.logModel = new DefaultNutsLogModel(this, info);
            this.logModel.setDefaultSession(defaultSession());
            this.LOG = defaultSession().log().of(DefaultNutsWorkspace.class);
            ((DefaultNutsLogger) LOG).suspendTerminal();
            this.name = Paths.get(info.getWorkspaceLocation()).getFileName().toString();
            this.termModel = new DefaultNutsTerminalModel(this);
            this.concurrentModel = new DefaultNutsConcurrentModel(this);
            this.filtersModel = new DefaultNutsFilterModel(this);
            this.installedRepository = new DefaultNutsInstalledRepository(this, info);
            this.repositoryModel = new DefaultNutsRepositoryModel(this);
            this.configModel = new DefaultNutsWorkspaceConfigModel(this, info);
            this.envModel = new DefaultNutsWorkspaceEnvManagerModel(this, info, defaultSession());
            this.aliasesModel = new DefaultCustomCommandsModel(this);
            this.importModel = new DefaultImportModel(this);
            this.locationsModel = new DefaultNutsWorkspaceLocationModel(this, info, Paths.get(info.getWorkspaceLocation()).toString());
            this.eventsModel = new DefaultNutsWorkspaceEventModel(this);
            this.textModel = new DefaultNutsTextManagerModel(this, info);
            this.location = info.getWorkspaceLocation();
            DefaultNutsVersionParser vparser = new DefaultNutsVersionParser(defaultSession());
            this.apiVersion = vparser.parse(Nuts.getVersion());
            this.apiId = new DefaultNutsId("net.thevpc.nuts", "nuts", apiVersion, null, (Map<String, String>) null, defaultSession());
            this.runtimeId = new DefaultNutsId(
                    info.getRuntimeId().getGroupId(),
                    info.getRuntimeId().getArtifactId(),
                    vparser.parse(info.getRuntimeId().getVersion().toString()),
                    null,
                    (Map<String, String>) null,
                    defaultSession());

            boolean errorTheme = false;
            NutsTextManager text = defaultSession().text().setSession(defaultSession());
            try {
                NutsTextFormatTheme theme = text.getTheme();
            } catch (Exception ex) {
                errorTheme = true;
                //unable to load theme;
                text.setTheme("");//set default!
            }

            NutsLoggerOp LOGCRF = LOG.with().level(Level.CONFIG).verb(NutsLogVerb.READ).session(defaultSession());
            NutsLoggerOp LOGCSF = LOG.with().level(Level.CONFIG).verb(NutsLogVerb.START).session(defaultSession());
//        NutsFormatManager formats = this.formats().setSession(defaultSession());
            if (errorTheme) {
                LOG.with().level(Level.CONFIG).verb(NutsLogVerb.FAIL).session(defaultSession())
                        .log(NutsMessage.jstyle("unable to load theme {0}. Reset to default!", info.getOptions().getTheme()));
            }
            if (LOG.isLoggable(Level.CONFIG)) {
                LOGCSF.log(NutsMessage.jstyle(" ==============================================================================="));
                String s = CoreIOUtils.loadString(getClass().getResourceAsStream("/net/thevpc/nuts/runtime/includes/standard-header.ntf"), true);
                s = s.replace("${nuts.workspace-runtime.version}", Nuts.getVersion());
                for (String s1 : s.split("\n")) {
                    LOGCSF.log(NutsMessage.jstyle(s1));
                }
                LOGCSF.log(NutsMessage.jstyle(" "));
                LOGCSF.log(NutsMessage.jstyle(" = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ="));
                LOGCSF.log(NutsMessage.jstyle(" "));
                LOGCSF.log(NutsMessage.jstyle("start ```sh nuts``` ```primary3 {0}``` at {1}", Nuts.getVersion(), CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(info.getOptions().getCreationTime()))));
                NutsCommandLineManager commandLine = defaultSession().commandLine().setSession(defaultSession());
                LOGCRF.log(NutsMessage.jstyle("open Nuts Workspace               : {0}",
                        info.getOptions().formatter().getBootCommandLine()
                ));
                LOGCRF.log(NutsMessage.jstyle("open Nuts Workspace (compact)     : {0}", info.getOptions().formatter().setCompact(true).getBootCommandLine()));

                LOGCRF.log(NutsMessage.jstyle("open Workspace with config        : "));
                LOGCRF.log(NutsMessage.jstyle("   nuts-workspace-uuid            : {0}", CoreNutsUtils.desc(info.getUuid(), text)));
                LOGCRF.log(NutsMessage.jstyle("   nuts-workspace-name            : {0}", CoreNutsUtils.desc(info.getName(), text)));
                LOGCRF.log(NutsMessage.jstyle("   nuts-api-version               : {0}", defaultSession().version().parser().parse(Nuts.getVersion())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-api-url                   : {0}", defaultSession().io().path(getApiURL())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-api-digest                : {0}", text.ofStyled(getApiDigest(), NutsTextStyle.version())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-boot-repositories         : {0}", CoreNutsUtils.desc(info.getBootRepositories(), text)));
                LOGCRF.log(NutsMessage.jstyle("   nuts-runtime                   : {0}", getRuntimeId()));
                LOGCRF.log(NutsMessage.jstyle("   nuts-runtime-digest            : {0}",
                        text.ofStyled(new CoreDigestHelper().append(info.getClassWorldURLs()).getDigest(), NutsTextStyle.version())
                ));
                LOGCRF.log(NutsMessage.jstyle("   nuts-runtime-dependencies      : {0}",
                        text.builder().appendJoined(text.ofStyled(";", NutsTextStyle.separator()),
                                Arrays.stream(info.getRuntimeBootDescriptor().getDependencies())
                                        .map(x -> defaultSession().id().parser().parse(x.toString()))
                                        .collect(Collectors.toList())
                        )
                ));
                NutsIOManager io = defaultSession().io();
                LOGCRF.log(NutsMessage.jstyle("   nuts-runtime-urls              : {0}",
                        text.builder().appendJoined(text.ofStyled(";", NutsTextStyle.separator()),
                                Arrays.stream(info.getClassWorldURLs())
                                        .map(x -> io.path(x.toString()))
                                        .collect(Collectors.toList())
                        )
                ));
                LOGCRF.log(NutsMessage.jstyle("   nuts-extension-dependencies    : {0}",
                        text.builder().appendJoined(text.ofStyled(";", NutsTextStyle.separator()),
                                toIds(info.getExtensionBootDescriptors()).stream()
                                        .map(x ->
                                                defaultSession().id().parser().parse(x.toString())
                                        )
                                        .collect(Collectors.toList())
                        )
                ));
//            if (hasUnsatisfiedRequirements()) {
//                LOG.log(Level.CONFIG, "\t execution-requirements         : unsatisfied {0}", getRequirementsHelpString(true));
//            } else {
//                LOG.log(Level.CONFIG, "\t execution-requirements         : satisfied");
//            }
                LOGCRF.log(NutsMessage.jstyle("   nuts-workspace                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getWorkspace(), info.getWorkspaceLocation())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-hash-name                 : {0}", CoreNutsUtils.formatLogValue(text, getHashName(), info.getWorkspaceLocation())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-apps                : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.APPS), info.getStoreLocation(NutsStoreLocation.APPS))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-config              : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.CONFIG), info.getStoreLocation(NutsStoreLocation.CONFIG))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-var                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.VAR), info.getStoreLocation(NutsStoreLocation.VAR))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-log                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.LOG), info.getStoreLocation(NutsStoreLocation.LOG))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-temp                : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.TEMP), info.getStoreLocation(NutsStoreLocation.TEMP))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-cache               : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.CACHE), info.getStoreLocation(NutsStoreLocation.CACHE))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-run                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.RUN), info.getStoreLocation(NutsStoreLocation.RUN))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-lib                 : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocation(NutsStoreLocation.LIB), info.getStoreLocation(NutsStoreLocation.LIB))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-strategy            : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocationStrategy(), info.getStoreLocationStrategy())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-repos-store-strategy      : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getRepositoryStoreLocationStrategy(), info.getRepositoryStoreLocationStrategy())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-layout              : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getStoreLocationLayout(), info.getStoreLocationLayout() == null ? "system" : info.getStoreLocationLayout())));
                LOGCRF.log(NutsMessage.jstyle("   option-read-only               : {0}", info.getOptions().isReadOnly()));
                LOGCRF.log(NutsMessage.jstyle("   option-trace                   : {0}", info.getOptions().isTrace()));
                LOGCRF.log(NutsMessage.jstyle("   option-progress                : {0}", CoreNutsUtils.desc(info.getOptions().getProgressOptions(), text)));
                LOGCRF.log(NutsMessage.jstyle("   inherited                      : {0}", info.getOptions().isInherited()));
                LOGCRF.log(NutsMessage.jstyle("   inherited-nuts-boot-args       : {0}", System.getProperty("nuts.boot.args") == null ? CoreNutsUtils.desc(null, text)
                        : CoreNutsUtils.desc(commandLine.setCommandlineFamily(NutsShellFamily.SH).parse(System.getProperty("nuts.boot.args")), text)
                ));
                LOGCRF.log(NutsMessage.jstyle("   inherited-nuts-args            : {0}", System.getProperty("nuts.args") == null ? CoreNutsUtils.desc(null, text)
                        : CoreNutsUtils.desc(text.toText(commandLine.setCommandlineFamily(NutsShellFamily.SH).parse(System.getProperty("nuts.args"))), text)
                ));
                LOGCRF.log(NutsMessage.jstyle("   option-open-mode               : {0}", CoreNutsUtils.formatLogValue(text, info.getOptions().getOpenMode(), info.getOptions().getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : info.getOptions().getOpenMode())));
                LOGCRF.log(NutsMessage.jstyle("   java-home                      : {0}", System.getProperty("java.home")));
                LOGCRF.log(NutsMessage.jstyle("   java-classpath                 : {0}", System.getProperty("java.class.path")));
                LOGCRF.log(NutsMessage.jstyle("   java-library-path              : {0}", System.getProperty("java.library.path")));
                LOGCRF.log(NutsMessage.jstyle("   os-name                        : {0}", System.getProperty("os.name")));
                NutsWorkspaceEnvManager senv = defaultSession().env();
                LOGCRF.log(NutsMessage.jstyle("   os-dist                        : {0}", senv.getOsDist().getArtifactId()));
                LOGCRF.log(NutsMessage.jstyle("   os-arch                        : {0}", System.getProperty("os.arch")));
                LOGCRF.log(NutsMessage.jstyle("   os-version                     : {0}", senv.getOsDist().getVersion()));
                LOGCRF.log(NutsMessage.jstyle("   user-name                      : {0}", System.getProperty("user.name")));
                LOGCRF.log(NutsMessage.jstyle("   user-dir                       : {0}", io.path(System.getProperty("user.dir"))));
                LOGCRF.log(NutsMessage.jstyle("   user-home                      : {0}", io.path(System.getProperty("user.home"))));
            }
            securityModel = new DefaultNutsWorkspaceSecurityModel(this);
            String workspaceLocation = info.getWorkspaceLocation();
            String apiVersion = info.getApiVersion();
            NutsBootId runtimeId = info.getRuntimeId();
            String repositories = info.getBootRepositories();
            NutsWorkspaceOptions uoptions = info.getOptions();
            NutsBootWorkspaceFactory bootFactory = info.getBootWorkspaceFactory();
            ClassLoader bootClassLoader = info.getClassWorldLoader();
            NutsWorkspaceConfigManager _config = defaultSession().config().setSession(defaultSession());
            NutsBootManager _boot = defaultSession().boot();
            NutsWorkspaceEnvManager _env = defaultSession().env();
            if (uoptions == null) {
                uoptions = new ReadOnlyNutsWorkspaceOptions(_config.optionsBuilder().build(), defaultSession());
            } else {
                //builder().build() (just to make a copy)
                uoptions = new ReadOnlyNutsWorkspaceOptions(uoptions.builder().build(), defaultSession());
            }
            long now = System.currentTimeMillis();
            if (uoptions.getCreationTime() == 0 || uoptions.getCreationTime() > now) {
                configModel.setStartCreateTimeMillis(now);
            } else {
                configModel.setStartCreateTimeMillis(uoptions.getCreationTime());
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

            NutsSystemTerminalBase termb = defaultSession().extensions().setSession(defaultSession()).createSupported(NutsSystemTerminalBase.class, null);
            if (termb == null) {
                throw new NutsExtensionNotFoundException(defaultSession(), NutsSystemTerminalBase.class, "SystemTerminalBase");
            }
            NutsTerminalManager _term = defaultSession().term();
            _term
                    .setSystemTerminal(termb)
                    .setTerminal(_term.createTerminal()
                    );
            bootModel.bootSession().setTerminal(bootModel.bootSession().term().createTerminal());
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
            boolean justInstalled = false;
            if (!loadWorkspace(defaultSession(), uoptions.getExcludedExtensions(), null)) {
                bootModel.setFirstBoot(true);
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                } else {
                    //this is the uuid of a freshly 'reset' workspace. We retain the uuid.
                }
                //workspace wasn't loaded. Create new configuration...
                justInstalled = true;
                NutsWorkspaceUtils.of(defaultSession()).checkReadOnly();
                LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS)
                        .log(NutsMessage.jstyle("creating {0} workspace at {1}",
                                defaultSession().text().ofStyled("NEW", NutsTextStyle.info()),
                                defaultSession().io().path(defaultSession().locations().getWorkspaceLocation())
                        ));
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
                        .build(defaultSession().locations().getWorkspaceLocation(), defaultSession()));
//                NutsUpdateOptions updateOptions = new NutsUpdateOptions().setSession(session);
                configModel.setConfigBoot(bconfig, defaultSession());
                configModel.setConfigApi(aconfig, defaultSession());
                configModel.setConfigRuntime(rconfig, defaultSession());
                initializeWorkspace(uoptions.getArchetype(), defaultSession());
                if (!_config.isReadOnly()) {
                    _config.save();
                }
                NutsVersion nutsVersion = getRuntimeId().getVersion();
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS)
                            .log(NutsMessage.jstyle("nuts workspace v{0} created.", nutsVersion));
                }
                //should install default
                if (defaultSession().isPlainTrace() && !_boot.getBootOptions().isSkipWelcome()) {
                    NutsPrintStream out = defaultSession().out();
                    out.resetLine();
                    StringBuilder version = new StringBuilder(nutsVersion.toString());
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
                                    .append(defaultSession().locations().getWorkspaceLocation(), NutsTextStyle.path())
                                    .append(" ")
                                    .append(" (")
                                    .append(getHashName())
                                    .append(")")
                    );

                    defaultSession().formats().setSession(defaultSession()).table().setValue(
                            NutsTableModel.of(defaultSession())
                                    .addCell(
                                            txt.builder()
                                                    .append(" This is the very first time ")
                                                    .appendCode("sh", "nuts")
                                                    .append(" has been launched for this workspace ")
                                    )
                    ).println(out);
                    out.println();
                }
                for (URL bootClassWorldURL : _boot.getBootClassWorldURLs()) {
                    NutsInstalledRepository repo = getInstalledRepository();
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(defaultSession()).repoSPI(repo);
                    NutsDeployRepositoryCommand desc = repoSPI.deploy()
                            .setSession(defaultSession().copy().setConfirm(NutsConfirmationMode.YES))
                            .setContent(bootClassWorldURL)
                            //.setFetchMode(NutsFetchMode.LOCAL)
                            .run();
                    if (desc.getId().getLongId().equals(getApiId().getLongId())
                            || desc.getId().getLongId().equals(getRuntimeId().getLongId())) {
                        repo.install(desc.getId(), defaultSession(), null);
                    } else {
                        repo.install(desc.getId(), defaultSession(), getRuntimeId());
                    }
                }
                configModel.installBootIds(defaultSession());
            } else {
                bootModel.setFirstBoot(false);
                uuid = configModel.getStoreModelBoot().getUuid();
                if (NutsBlankable.isBlank(uuid)) {
                    uuid = UUID.randomUUID().toString();
                    configModel.getStoreModelBoot().setUuid(uuid);
                }
                if (uoptions.isRecover()) {
                    configModel.setBootApiVersion(cfg.getApiVersion(), defaultSession());
                    configModel.setBootRuntimeId(cfg.getRuntimeId(), defaultSession());
                    configModel.setBootRuntimeDependencies(
                            Arrays.stream(info.getRuntimeBootDescriptor().getDependencies())
                                    .map(NutsBootId::toString)
                                    .collect(Collectors.joining(";")),
                            defaultSession());
                    configModel.setBootRepositories(cfg.getBootRepositories(), defaultSession());
                    try {
                        defaultSession().install().setInstalled(true).setSession(defaultSession()).getResult();
                    } catch (Exception ex) {
                        LOG.with().session(defaultSession()).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                                .error(ex)
                                .log(NutsMessage.jstyle("reinstall artifacts failed : {0}", ex));
                    }
                }
                if (defaultSession().repos().getRepositories().length == 0) {
                    LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("workspace has no repositories. Will re-create defaults"));
                    initializeWorkspace(uoptions.getArchetype(), defaultSession());
                }
                List<String> transientRepositoriesSet = uoptions.getRepositories() == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(uoptions.getRepositories()));
                NutsRepositorySelector.SelectorList expected = NutsRepositorySelector.parse(transientRepositoriesSet.toArray(new String[0]));
                for (NutsRepositorySelector.Selection loc : expected.resolveSelectors(null)) {
                    NutsAddRepositoryOptions d = NutsRepositorySelector.createRepositoryOptions(loc, false, defaultSession());
                    String n = d.getName();
                    String ruuid = (NutsBlankable.isBlank(n) ? "temporary" : n) + "_" + UUID.randomUUID().toString().replace("-", "");
                    d.setName(ruuid);
                    d.setTemporary(true);
                    d.setEnabled(true);
                    d.setFailSafe(false);
                    if (d.getConfig() != null) {
                        d.getConfig().setName(NutsBlankable.isBlank(n) ? ruuid : n);
                        d.getConfig().setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                    }
                    defaultSession().repos().addRepository(d);
                }
            }

            if (!_config.isReadOnly()) {
                _config.save(false);
            }
            configModel.setEndCreateTimeMillis(System.currentTimeMillis());
            if (justInstalled) {
                installSettings(defaultSession());
                if (!_boot.getBootOptions().isSkipCompanions()) {
                    installCompanions(defaultSession());
                }
                DefaultNutsWorkspaceEvent workspaceCreatedEvent = new DefaultNutsWorkspaceEvent(defaultSession(), null, null, null, null);
                for (NutsWorkspaceListener workspaceListener : defaultSession().events().getWorkspaceListeners()) {
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
                if (password == null || NutsBlankable.isBlank(new String(password))) {
                    password = defaultSession().term().getTerminal().readPassword("Password : ");
                }
                defaultSession().security().setSession(defaultSession()).login(uoptions.getUserName(), password);
            }
            LOG.with().session(defaultSession()).level(Level.FINE).verb(NutsLogVerb.SUCCESS)
                    .log(
                            NutsMessage.jstyle("```sh nuts``` workspace loaded in ```error {0}```",
                                    CoreTimeUtils.formatPeriodMilli(_boot.getCreationFinishTimeMillis() - _boot.getCreationStartTimeMillis())
                            )
                    );

            if (CoreBooleanUtils.getSysBoolNutsProperty("perf", false)) {
                defaultSession().out().printf("```sh nuts``` workspace loaded in %s%n",
                        defaultSession().text().ofStyled(CoreTimeUtils.formatPeriodMilli(_boot.getCreationFinishTimeMillis() - _boot.getCreationStartTimeMillis()),
                                NutsTextStyle.error()
                        )
                );
            }
        } finally {
            if (bootModel != null) {
                bootModel.setInitializing(false);
            }
        }
//        return !exists;
    }

    private URL getApiURL() {
        NutsBootId nid = new NutsBootId("net.thevpc.nuts", "nuts", NutsBootVersion.parse(Nuts.getVersion()));
        return NutsApiUtils.findClassLoaderJar(nid, NutsClassLoaderUtils.resolveClasspathURLs(Thread.currentThread().getContextClassLoader()));
    }

    private String getApiDigest() {
        return new CoreDigestHelper().append(getApiURL()).getDigest();
    }

    private void installSettings(NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        NutsBootManager boot = session.boot();
        NutsWorkspaceEnvManager env = session.env();
        NutsWorkspaceConfigManager config = session.config();
        boolean initializeAllPlatforms = boot.getCustomBootOption("init-platforms").getBoolean(true, false);
        if (initializeAllPlatforms && boot.getCustomBootOption("init-java").getBoolean(true, false)) {
            try {
                if (session.isPlainTrace()) {
                    session.out().resetLine().println("looking for java installations in default locations...");
                }
                NutsPlatformLocation[] found = env.platforms()
                        .searchSystemPlatforms(NutsPlatformType.JAVA);
                int someAdded = 0;
                for (NutsPlatformLocation java : found) {
                    if (env.platforms().addPlatform(java)) {
                        someAdded++;
                    }
                }
                NutsTextManager factory = session.text();
                if (session.isPlainTrace()) {
                    if (someAdded == 0) {
                        session.out().print("```error no new``` java installation locations found...\n");
                    } else if (someAdded == 1) {
                        session.out().printf("%s new java installation location added...\n", factory.ofStyled("1", NutsTextStyle.primary2()));
                    } else {
                        session.out().printf("%s new java installation locations added...\n", factory.ofStyled("" + someAdded, NutsTextStyle.primary2()));
                    }
                    session.out().println("you can always add another installation manually using 'nuts settings add java' command.");
                }
                if (!config.isReadOnly()) {
                    config.save();
                }
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).error(ex)
                        .log(NutsMessage.jstyle("unable to resolve default JRE/JDK locations : {0}", ex));
                if (session.isPlainTrace()) {
                    NutsPrintStream out = session.out();
                    out.resetLine();
                    out.printf("```unable to resolve default JRE/JDK locations``` :  %s%n", ex);
                }
            }
        } else {
            //at least add current vm
            try {
                if (session.isPlainTrace()) {
                    session.out().resetLine().println("adding current JVM...");
                }
                NutsPlatformLocation found0 = env.platforms()
                        .resolvePlatform(NutsPlatformType.JAVA, System.getProperty("java.home"), null);
                NutsPlatformLocation[] found = found0 == null ? new NutsPlatformLocation[0] : new NutsPlatformLocation[]{found0};
                int someAdded = 0;
                for (NutsPlatformLocation java : found) {
                    if (env.platforms().addPlatform(java)) {
                        someAdded++;
                    }
                }
                NutsTextManager factory = session.text();
                if (session.isPlainTrace()) {
                    if (someAdded == 0) {
                        session.out().print("```error no new``` java installation locations found...\n");
                    } else if (someAdded == 1) {
                        session.out().printf("%s new java installation location added...\n", factory.ofStyled("1", NutsTextStyle.primary2()));
                    } else {
                        session.out().printf("%s new java installation locations added...\n", factory.ofStyled("" + someAdded, NutsTextStyle.primary2()));
                    }
                    session.out().println("you can always add another installation manually using 'nuts settings add java' command.");
                }
                if (!config.isReadOnly()) {
                    config.save();
                }
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).error(ex)
                        .log(NutsMessage.jstyle("unable to resolve default JRE/JDK locations : {0}", ex));
                if (session.isPlainTrace()) {
                    NutsPrintStream out = session.out();
                    out.resetLine();
                    out.printf("```unable to resolve default JRE/JDK locations``` :  %s%n", ex);
                }
            }
        }
        if (boot.getCustomBootOption("init-launchers").getBoolean(true, false)) {
            try {
                env.addLauncher(
                        new NutsLauncherOptions()
                                .setId(getApiId())
                                .setCreateScript(true)
                                .setSystemWideConfig(
                                        session.boot().getBootOptions().isSwitchWorkspace()
                                )
                                .setCreateDesktopShortcut(NutsSupportCondition.PREFERRED)
                                .setCreateMenuShortcut(NutsSupportCondition.SUPPORTED)
                );
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).error(ex)
                        .log(NutsMessage.jstyle("unable to install desktop launchers : {0}", ex));
                if (session.isPlainTrace()) {
                    NutsPrintStream out = session.out();
                    out.resetLine();
                    out.printf("```error unable to install desktop launchers``` :  %s%n", ex);
                }
            }
        }
    }

    public void installCompanions(NutsSession session) {
        NutsWorkspaceUtils.checkSession(this, session);
        NutsTextManager text = session.text();
        Set<NutsId> companionIds = session.extensions().getCompanionIds();
        if (companionIds.isEmpty()) {
            return;
        }
        if (session.isPlainTrace()) {
            NutsPrintStream out = session.out();
            out.resetLine();
            out.printf("looking for recommended companion tools to install... detected : %s%n",
                    text.builder().appendJoined(text.ofPlain(","),
                            companionIds
                    )
            );
        }
        try {
            session.install().companions().setSession(session.copy().setTrace(session.isTrace() && session.isPlainOut()))
                    .run();
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).error(ex)
                    .log(NutsMessage.jstyle("unable to install companions : {0}", ex));
            if (session.isPlainTrace()) {
                NutsPrintStream out = session.out();
                out.resetLine();
                out.printf("```error unable to install companion tools``` :  %s \n"
                                + "this happens when none of the following repositories are able to locate them : %s\n",
                        ex,
                        text.builder().appendJoined(text.ofPlain(", "),
                                Arrays.stream(session.repos().getRepositories()).map(x
                                        -> text.builder().append(x.getName(), NutsTextStyle.primary3())
                                ).collect(Collectors.toList())
                        )
                );
            }
        }
    }

    protected NutsDescriptor _resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START)
                .log(NutsMessage.jstyle("resolve effective {0}", descriptor.getId()));
        checkSession(session);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    session.fetch().setId(parents[i]).setEffective(false).setSession(session).getResultDescriptor(),
                    session
            );
        }
        //compute effective!
        NutsDescriptorBuilder descrWithParents = descriptor.builder().applyParents(parentDescriptors);
        //now apply conditions!
        NutsDescriptorProperty[] properties = Arrays.stream(descrWithParents.getProperties()).filter(x -> CoreFilterUtils.acceptCondition(
                x.getCondition(), false, session)).toArray(NutsDescriptorProperty[]::new);
        if (properties.length > 0) {
            DefaultNutsProperties pp = new DefaultNutsProperties();
            List<NutsDescriptorProperty> n = new ArrayList<>();
            pp.addAll(properties);
            for (String s : pp.keySet()) {
                NutsDescriptorProperty[] a = pp.getAll(s);
                if (a.length == 1) {
                    n.add(a[0].builder().setCondition((NutsEnvCondition) null).build());
                } else {
                    NutsDescriptorProperty z = null;
                    for (NutsDescriptorProperty zz : a) {
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
                        n.add(z.builder().setCondition((NutsEnvCondition) null).build());
                    }
                }
            }
            properties = n.toArray(new NutsDescriptorProperty[0]);
        }

        descrWithParents.setProperties(properties);

        NutsDescriptor effectiveDescriptor = descrWithParents.applyProperties().build();
        NutsDependency[] oldDependencies = effectiveDescriptor.getDependencies();
        List<NutsDependency> newDeps = new ArrayList<>();
        boolean someChange = false;

        for (NutsDependency d : oldDependencies) {
            if (NutsBlankable.isBlank(d.getScope())
                    || d.getVersion().isBlank()
                    || NutsBlankable.isBlank(d.getOptional())) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : effectiveDescriptor.getStandardDependencies()) {
                    if (standardDependency.getSimpleName().equals(d.toId().getShortName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (NutsBlankable.isBlank(d.getScope())
                            && !NutsBlankable.isBlank(standardDependencyOk.getScope())) {
                        someChange = true;
                        d = d.builder().setScope(standardDependencyOk.getScope()).build();
                    }
                    if (NutsBlankable.isBlank(d.getOptional())
                            && !NutsBlankable.isBlank(standardDependencyOk.getOptional())) {
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
                newDeps.addAll(Arrays.asList(session.fetch().setId(d.toId()).setEffective(true).setSession(session).getResultDescriptor().getDependencies()));
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
        if (NutsBlankable.isBlank(archetype)) {
            archetype = "default";
        }
        NutsWorkspaceArchetypeComponent instance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NutsWorkspaceArchetypeComponent ac : session.extensions().setSession(session).createAllSupported(NutsWorkspaceArchetypeComponent.class, archetype)) {
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
        session.security().setSession(session).updateUser(NutsConstants.Users.ADMIN).setCredentials("admin".toCharArray()).run();

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
            NutsFetchCommand fetch2 = session.fetch()
                    .setSession(session)
                    .setId(def.getId())
                    .setContent(true)
                    .setRepositoryFilter(session.repos().filter().installedRepo())
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
            NutsTextManager text = session.text();
            if (strategy0 == InstallStrategy0.UPDATE) {
                session.out().resetLine().printf("%s %s ...%n",
                        text.ofStyled("update", NutsTextStyle.warn()),
                        def.getId().getLongId()
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
                            text.ofStyled("re-install", NutsTextStyles.of(NutsTextStyle.success(), NutsTextStyle.underlined())),
                            def.getId().getLongId()
                    );
                } else {
                    session.out().resetLine().printf("%s %s ...%n",
                            text.ofStyled("install", NutsTextStyle.success()),
                            def.getId().getLongId()
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
                    oldDef = session.fetch().setSession(
                            session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE)
                    ).setId(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion()).setFailFast(false).getResultDefinition();
                    break;
                }
                case RUNTIME: {
                    oldDef = session.fetch().setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE)).setId(getRuntimeId())
                            .setFailFast(false).getResultDefinition();
                    break;
                }
                default: {
                    oldDef = session.search().setSession(session).addId(def.getId().getShortId())
                            .setInstallStatus(session.filters().setSession(session).installStatus().byDeployed(true))
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
        NutsWorkspaceConfigManager config = session.config().setSession(session);
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
                            NutsDefinition dd = session.search().addId(dependency.toId()).setContent(true).setLatest(true)
                                    //.setDependencies(true)
                                    .setEffective(true)
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

            //should change def to reflect install location!
            NutsExecutionContextBuilder cc = createExecutionContext()
                    .setTraceSession(session.copy())
                    .setExecSession(session.copy())
                    .setDefinition(def).setArguments(args).setFailFast(true).setTemporary(false)
                    .setExecutionType(session.boot().getBootOptions().getExecutionType())
                    .setRunAs(NutsRunAs.currentUser())// install or update always uses current user
                    ;
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
            NutsFetchCommand fetch2 = session.fetch()
                    .setSession(session)
                    .setId(executionContext.getDefinition().getId())
                    .setContent(true)
                    .setRepositoryFilter(session.repos().filter().installedRepo())
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
            NutsRepository rep = session.repos().findRepository(def.getRepositoryUuid());
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
                            LOG.with().session(session).level(Level.FINE).error(ex)
                                    .log(NutsMessage.jstyle("failed to uninstall  {0}", executionContext.getDefinition().getId()));
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
            NutsTextManager text = session.text();
            if (updateDefaultVersion) {
                setAsDefaultString = " set as " + text.builder().append("default", NutsTextStyle.primary1()) + ".";
            }
            if (newNutsInstallInformation != null
                    && (newNutsInstallInformation.isJustInstalled()
                    || newNutsInstallInformation.isJustRequired())) {
                NutsText installedString = null;
                if (newNutsInstallInformation != null) {
                    if (newNutsInstallInformation.isJustReInstalled()) {
                        installedString = text.ofStyled("re-install", NutsTextStyles.of(NutsTextStyle.success(), NutsTextStyle.underlined()));
                    } else if (newNutsInstallInformation.isJustInstalled()) {
                        installedString = text.ofStyled("install", NutsTextStyle.success());
                    } else if (newNutsInstallInformation.isJustReRequired()) {
                        installedString = text.ofStyled("re-require", NutsTextStyles.of(NutsTextStyle.info(), NutsTextStyle.underlined()));
                    } else if (newNutsInstallInformation.isJustRequired()) {
                        installedString = text.ofStyled("require", NutsTextStyle.info());
                    }
                }
                if (installedString != null) {
                    //(reinstalled ? "re-installed" : "installed")
                    if (!def.getContent().isCached()) {
                        if (def.getContent().isTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s) temporarily file %s.%s%n",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getLocation(),
                                        text.parse(setAsDefaultString)
                                );
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s).%s%n", installedString,
                                        def.getId().getLongId(),
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
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getLocation(),
                                        text.parse(setAsDefaultString));
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s).%s%n",
                                        installedString,
                                        def.getId().getLongId(),
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
                                def.getId().getLongId(),
                                text.ofStyled("successfully", NutsTextStyle.success()),
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
        NutsCustomCommandManager aliases = session.commands().setSession(session);
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
            for (NutsId extensionId : session.extensions().setSession(session).getConfigExtensions()) {
                if (extensionModel.isExcludedExtension(extensionId)) {
                    continue;
                }
                NutsSession sessionCopy = session.copy();
                extensionModel.wireExtension(extensionId,
                        session.fetch().setSession(sessionCopy
                                .copy()
                                .setFetchStrategy(NutsFetchStrategy.ONLINE)
                                .setTransitive(true)
                        )
                );
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }
            NutsUserConfig adminSecurity = NutsWorkspaceConfigManagerExt.of(session.config())
                    .getModel()
                    .getUser(NutsConstants.Users.ADMIN, session);
            if (adminSecurity == null || NutsBlankable.isBlank(adminSecurity.getCredentials())) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("{0} user has no credentials. reset to default", NutsConstants.Users.ADMIN));
                }
                session.security()
                        .updateUser(NutsConstants.Users.ADMIN).credentials("admin".toCharArray())
                        .run();
            }
            for (NutsCommandFactoryConfig commandFactory : session.commands().setSession(session).getCommandFactories()) {
                try {
                    session.commands().setSession(session).addCommandFactory(commandFactory);
                } catch (Exception e) {
                    LOG.with().session(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("unable to instantiate Command Factory {0}", commandFactory));
                }
            }
            DefaultNutsWorkspaceEvent worksppaeReloadedEvent = new DefaultNutsWorkspaceEvent(session, null, null, null, null);
            for (NutsWorkspaceListener listener : defaultSession().events().getWorkspaceListeners()) {
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
        NutsTextManager txt = session.text();
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-welcome.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no welcome found" : n.toString());
    }

    @Override
    public String getHelpText(NutsSession session) {
        NutsTextManager txt = session.text();
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-help.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no help found" : n.toString());
    }

    @Override
    public String getLicenseText(NutsSession session) {
        NutsTextManager txt = session.text();
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-license.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no license found" : n.toString());
    }

    @Override
    public String resolveDefaultHelp(Class clazz, NutsSession session) {
        NutsId nutsId = session.id().resolveId(clazz);
        if (nutsId != null) {
            String urlPath = "/" + nutsId.getGroupId().replace('.', '/') + "/" + nutsId.getArtifactId() + ".ntf";

            NutsTextManager txt = session.text();
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
        if ((NutsBlankable.isBlank(g)) || (NutsBlankable.isBlank(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = session.fetch().setSession(session).setId(parent).setEffective(true).getResultId();
                if (NutsBlankable.isBlank(g)) {
                    g = p.getGroupId();
                }
                if (NutsBlankable.isBlank(v)) {
                    v = p.getVersion().getValue();
                }
                if (!NutsBlankable.isBlank(g) && !NutsBlankable.isBlank(v)) {
                    break;
                }
            }
            if (NutsBlankable.isBlank(g) || NutsBlankable.isBlank(v)) {
                throw new NutsNotFoundException(session, thisId,
                        NutsMessage.cstyle("unable to fetchEffective for %s. best Result is %s", thisId, thisId)
                        , null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v) || CoreStringUtils.containsVars(a)) {
            Map<String, String> p = CoreNutsUtils.getPropertiesMap(descriptor.getProperties());
            NutsId bestId = session.id().builder().setGroupId(g).setArtifactId(thisId.getArtifactId()).setVersion(v).build();
            bestId = bestId.builder().apply(new MapToFunction(p)).build();
            if (CoreNutsUtils.isEffectiveId(bestId)) {
                return bestId;
            }
            Stack<NutsId> all = new Stack<>();
            NutsId[] parents = descriptor.getParents();
            all.addAll(Arrays.asList(parents));
            while (!all.isEmpty()) {
                NutsId parent = all.pop();
                NutsDescriptor dd = session.fetch().setSession(session).setId(parent).setEffective(true).getResultDescriptor();
                bestId = bestId.builder().apply(new MapToFunction(CoreNutsUtils.getPropertiesMap(dd.getProperties()))).build();
                if (CoreNutsUtils.isEffectiveId(bestId)) {
                    return bestId;
                }
                all.addAll(Arrays.asList(dd.getParents()));
            }
            throw new NutsNotFoundException(session, bestId,
                    NutsMessage.cstyle("unable to fetchEffective for %s. best Result is %s", bestId, bestId), null);
        }
        NutsId bestId = session.id().setSession(session).builder().setGroupId(g).setArtifactId(thisId.getArtifactId()).setVersion(v).build();
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
            for (NutsId companionTool : session.extensions().getCompanionIds()) {
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
                    runnerFile = session.fetch().setId(installerDescriptor.getId())
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
            NutsInstallerComponent best = session.extensions().setSession(session).createSupported(NutsInstallerComponent.class, runnerFile);
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
        for (NutsId ext : session.extensions().setSession(session).getConfigExtensions()) {
            if (ext.equalsShortId(getRuntimeId())) {
                coreFound = true;
                break;
            }
        }
        return !coreFound;
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        Path eff = null;
        NutsWorkspaceLocationManager loc = session.locations().setSession(session);
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            Path l = Paths.get(loc.getStoreLocation(descriptor.getId(), NutsStoreLocation.CACHE));
            String nn = loc.getDefaultIdFilename(descriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
            if (Files.isRegularFile(eff)) {
                try {
                    NutsDescriptor d = session.descriptor().parser().setSession(session).parse(eff);
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.FINE).error(ex)
                            .log(NutsMessage.jstyle("failed to parse {0}", eff));
                    //
                }
            }
        } else {
            //
        }
        NutsDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor, session);
        if (eff == null) {
            Path l = Paths.get(session.locations().getStoreLocation(effectiveDescriptor.getId(), NutsStoreLocation.CACHE));
            String nn = loc.getDefaultIdFilename(effectiveDescriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
        }
        try {
            session.descriptor().setSession(session).formatter(effectiveDescriptor).setNtf(false).print(eff);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.FINE).error(ex)
                    .log(NutsMessage.jstyle("failed to print {0}", eff));
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
        NutsDefinition nutToInstall;
        try {
            nutToInstall = session.search().addId(id)
                    .setSession(session.copy().setTransitive(false))
                    .setInlineDependencies(checkDependencies)
                    .setInstallStatus(session.filters().installStatus().byDeployed(true))
                    .setOptional(false)
                    .getResultDefinitions().first();
            if (nutToInstall == null) {
                return NutsInstallStatus.NONE;
            }
        } catch (NutsNotFoundException e) {
            return NutsInstallStatus.NONE;
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex)
                    .log(NutsMessage.jstyle("error: %s", ex));
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
        NutsDefinition m = session.fetch().setId(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
        Map<String, String> a = new LinkedHashMap<>();
        a.put("configVersion", Nuts.getVersion());
        a.put("id", id.getLongName());
        a.put("dependencies", m.getDependencies().stream().map(NutsDependency::getLongName).collect(Collectors.joining(";")));
        defs.put(m.getId().getLongId(), m);
        if (withDependencies) {
            for (NutsDependency dependency : m.getDependencies()) {
                if (!defs.containsKey(dependency.toId().getLongId())) {
                    m = session.fetch().setId(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
                    defs.put(m.getId().getLongId(), m);
                }
            }
        }
        for (NutsDefinition def : defs.values()) {
            Path bootstrapFolder = Paths.get(session.locations().getStoreLocation(NutsStoreLocation.LIB)).resolve(NutsConstants.Folders.ID);
            NutsId id2 = def.getId();
            session.io().copy().from(def.getPath())
                    .to(bootstrapFolder.resolve(session.locations().getDefaultIdBasedir(id2))
                            .resolve(session.locations().getDefaultIdFilename(id2.builder().setFaceContent().setPackaging("jar").build()))
                    ).run();
            session.descriptor().formatter(session.fetch().setId(id2).getResultDescriptor()).setNtf(false)
                    .print(bootstrapFolder.resolve(session.locations().getDefaultIdBasedir(id2))
                            .resolve(session.locations().getDefaultIdFilename(id2.builder().setFaceDescriptor().build())));

            Map<String, String> pr = new LinkedHashMap<>();
            pr.put("file.updated.date", Instant.now().toString());
            pr.put("project.id", def.getId().getShortId().toString());
            pr.put("project.name", def.getId().getShortId().toString());
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
                    bootstrapFolder.resolve(session.locations().getDefaultIdBasedir(def.getId().getLongId()))
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
        return defaultSession().config().getHashName(this);
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

//    @Override
//    public Set<NutsId> getCompanionIds(NutsSession session) {
//        NutsWorkspaceUtils.checkSession(this, session);
//        NutsIdParser parser = id().setSession(session).parser();
//        return Collections.unmodifiableSet(new HashSet<>(
//                        Arrays.asList(parser.parse("net.thevpc.nuts.toolbox:nsh"))
//                )
//        );
//    }

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
    public NutsUtilManager util() {
        return new DefaultNutsUtilManager(utilModel);
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
        private final String id;

        InstallStrategy0() {
            this.id = name().toLowerCase().replace('_', '-');
        }

        public static InstallStrategy0 parse(String value, NutsSession session) {
            return parse(value, null, session);
        }

        public static InstallStrategy0 parse(String value, InstallStrategy0 emptyValue, NutsSession session) {
            InstallStrategy0 v = parseLenient(value, emptyValue, null);
            if (v == null) {
                if (!NutsBlankable.isBlank(value)) {
                    throw new NutsParseEnumException(session, value, InstallStrategy0.class);
                }
            }
            return v;
        }

        public static InstallStrategy0 parseLenient(String value) {
            return parseLenient(value, null);
        }

        public static InstallStrategy0 parseLenient(String value, InstallStrategy0 emptyOrErrorValue) {
            return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
        }

        public static InstallStrategy0 parseLenient(String value, InstallStrategy0 emptyValue, InstallStrategy0 errorValue) {
            if (value == null) {
                value = "";
            } else {
                value = value.toUpperCase().trim().replace('-', '_');
            }
            if (value.isEmpty()) {
                return emptyValue;
            }
            try {
                return InstallStrategy0.valueOf(value.toUpperCase());
            } catch (Exception notFound) {
                return errorValue;
            }
        }

        @Override
        public String id() {
            return id;
        }

    }

}
