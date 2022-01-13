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
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootManager;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.boot.NutsBootConfig;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsEnvCondition;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsProperties;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NutsDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.event.*;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.dependency.util.NutsClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.extension.NutsExtensionListHelper;
import net.thevpc.nuts.runtime.standalone.id.DefaultNutsId;
import net.thevpc.nuts.runtime.standalone.installer.CommandForIdNutsInstallerComponent;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.log.DefaultNutsLogModel;
import net.thevpc.nuts.runtime.standalone.log.DefaultNutsLogger;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNutsRepositoryManager;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNutsRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsWorkspaceSecurityManager;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsWorkspaceSecurityModel;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.session.DefaultNutsSession;
import net.thevpc.nuts.runtime.standalone.text.DefaultNutsTextManagerModel;
import net.thevpc.nuts.runtime.standalone.text.util.NutsTextUtils;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.DefaultNutsFilterModel;
import net.thevpc.nuts.runtime.standalone.version.DefaultNutsVersion;
import net.thevpc.nuts.runtime.standalone.version.DefaultNutsVersionParser;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNutsExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy.DefaultNutsDeployCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNutsExecCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNutsFetchCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.info.DefaultNutsInfoCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNutsInstallCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.push.DefaultNutsPushCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.RequestQueryInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.DefaultNutsSearchCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.updatestats.DefaultNutsUpdateStatisticsCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy.DefaultNutsUndeployCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall.DefaultNutsUninstallCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNutsUpdateCommand;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.spi.*;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/6/17.
 */
@NutsComponentScope(NutsComponentScopeType.PROTOTYPE)
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
    private NutsWorkspaceModel wsModel;

    public DefaultNutsWorkspace(NutsBootOptions info) {
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
//                            throw new NutsIOException(session,ex);
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

    private void initWorkspace(NutsBootOptions bOption0) {
        try {
            this.wsModel = new NutsWorkspaceModel(this);
            this.wsModel.bootModel = new DefaultNutsBootModel(this, bOption0);
            this.LOG = new DefaultNutsLogger(this, defaultSession(), DefaultNutsWorkspace.class, true);

            CoreNutsBootOptions bOptions = this.wsModel.bootModel.getCoreBootOptions();
            NutsWorkspaceOptions options = bOptions.getOptions();
            this.wsModel.configModel = new DefaultNutsWorkspaceConfigModel(this);
            String workspaceLocation = bOptions.getWorkspaceLocation();
            String apiVersion = bOptions.getApiVersion();
            NutsBootId runtimeId = bOptions.getRuntimeId();
            String repositories = bOptions.getBootRepositories();
            NutsBootWorkspaceFactory bootFactory = bOptions.getBootWorkspaceFactory();
            ClassLoader bootClassLoader = bOptions.getClassWorldLoader();
            NutsWorkspaceConfigManager _config = defaultSession().config();
            NutsBootManager _boot = defaultSession().boot();
            NutsBootConfig cfg = new NutsBootConfig();
            cfg.setWorkspace(workspaceLocation);
            cfg.setApiVersion(apiVersion);
            cfg.setRuntimeId(runtimeId == null ? null : runtimeId.toString());
            cfg.setRuntimeBootDescriptor(bOptions.getRuntimeBootDescriptor());
            cfg.setExtensionBootDescriptors(bOptions.getExtensionBootDescriptors());

            DefaultNutsVersionParser vparser = new DefaultNutsVersionParser(defaultSession());
            this.wsModel.apiVersion = vparser.parse(Nuts.getVersion());
            this.wsModel.apiId = new DefaultNutsId("net.thevpc.nuts", "nuts",
                    new DefaultNutsVersion(apiVersion, defaultSession()), null, (Map<String, String>) null,
                    new DefaultNutsEnvCondition(defaultSession()),
                    defaultSession());
            this.wsModel.runtimeId = new DefaultNutsId(
                    bOptions.getRuntimeId().getGroupId(),
                    bOptions.getRuntimeId().getArtifactId(),
                    vparser.parse(bOptions.getRuntimeId().getVersion().toString()),
                    null,
                    (Map<String, String>) null,
                    new DefaultNutsEnvCondition(defaultSession()),
                    defaultSession());

            this.wsModel.extensionModel = new DefaultNutsWorkspaceExtensionModel(this, bootFactory, options.getExcludedExtensions(), defaultSession());
            this.wsModel.extensionModel.onInitializeWorkspace(bOptions, bootClassLoader, defaultSession());
            this.wsModel.logModel = new DefaultNutsLogModel(this, bOptions);
            this.wsModel.logModel.setDefaultSession(defaultSession());
            this.wsModel.filtersModel = new DefaultNutsFilterModel(this);
            this.wsModel.installedRepository = new DefaultNutsInstalledRepository(this, bOptions);
            this.wsModel.repositoryModel = new DefaultNutsRepositoryModel(this);
            this.wsModel.envModel = new DefaultNutsWorkspaceEnvManagerModel(this, defaultSession());
            this.wsModel.aliasesModel = new DefaultCustomCommandsModel(this);
            this.wsModel.importModel = new DefaultImportModel(this);
            this.wsModel.locationsModel = new DefaultNutsWorkspaceLocationModel(this, Paths.get(bOptions.getWorkspaceLocation()).toString());
            this.wsModel.eventsModel = new DefaultNutsWorkspaceEventModel(this);
            this.wsModel.textModel = new DefaultNutsTextManagerModel(this);
            this.wsModel.location = bOptions.getWorkspaceLocation();

            this.wsModel.bootModel.onInitializeWorkspace();

            NutsSystemTerminalBase termb = defaultSession().extensions()
                    .createSupported(NutsSystemTerminalBase.class, true, null);
            defaultSession().config()
                    .setSystemTerminal(termb)
                    .setDefaultTerminal(NutsSessionTerminal.of(defaultSession())
                    );
            wsModel.bootModel.bootSession().setTerminal(NutsSessionTerminal.of(wsModel.bootModel.bootSession()));
            ((DefaultNutsLogger) LOG).resumeTerminal(defaultSession());

            NutsTexts text = NutsTexts.of(defaultSession());
            try {
                text.getTheme();
            } catch (Exception ex) {
                LOG.with().level(Level.CONFIG).verb(NutsLogVerb.FAIL).session(defaultSession())
                        .log(NutsMessage.jstyle("unable to load theme {0}. Reset to default!", options.getTheme()));
                text.setTheme("");//set default!
            }

            NutsLoggerOp LOGCRF = LOG.with().level(Level.CONFIG).verb(NutsLogVerb.READ).session(defaultSession());
            NutsLoggerOp LOGCSF = LOG.with().level(Level.CONFIG).verb(NutsLogVerb.START).session(defaultSession());
//        NutsFormatManager formats = this.formats().setSession(defaultSession());
            NutsElements elems = NutsElements.of(defaultSession());
            if (LOG.isLoggable(Level.CONFIG)) {
                //just log known implementations
                NutsCommandLines.of(defaultSession());
                NutsTerminals.of(defaultSession());
                NutsPrintStreams.of(defaultSession());
                NutsVersionFormat.of(defaultSession());
                NutsIdFormat.of(defaultSession());
                NutsInputStreams.of(defaultSession());
                NutsIdParser.of(defaultSession());

                LOGCSF.log(NutsMessage.jstyle(" ==============================================================================="));
                String s = CoreIOUtils.loadString(getClass().getResourceAsStream("/net/thevpc/nuts/runtime/includes/standard-header.ntf"), true, defaultSession());
                s = s.replace("${nuts.workspace-runtime.version}", Nuts.getVersion());
                for (String s1 : s.split("\n")) {
                    LOGCSF.log(NutsMessage.jstyle(s1));
                }
                LOGCSF.log(NutsMessage.jstyle(" "));
                LOGCSF.log(NutsMessage.jstyle(" = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ="));
                LOGCSF.log(NutsMessage.jstyle(" "));
                LOGCSF.log(NutsMessage.jstyle("start ```sh nuts``` ```primary3 {0}``` at {1}", Nuts.getVersion(), CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(options.getCreationTime()))));
                LOGCRF.log(NutsMessage.jstyle("open Nuts Workspace               : {0}",
                        options.formatter().getBootCommandLine()
                ));
                LOGCRF.log(NutsMessage.jstyle("open Nuts Workspace (compact)     : {0}", options.formatter().setCompact(true).getBootCommandLine()));

                LOGCRF.log(NutsMessage.jstyle("open Workspace with config        : "));
                LOGCRF.log(NutsMessage.jstyle("   nuts-workspace-uuid            : {0}", NutsTextUtils.desc(bOptions.getUuid(), text)));
                LOGCRF.log(NutsMessage.jstyle("   nuts-workspace-name            : {0}", NutsTextUtils.desc(bOptions.getName(), text)));
                LOGCRF.log(NutsMessage.jstyle("   nuts-api-version               : {0}", NutsVersion.of(Nuts.getVersion(), defaultSession())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-api-url                   : {0}", NutsPath.of(getApiURL(), defaultSession())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-api-digest                : {0}", text.ofStyled(getApiDigest(), NutsTextStyle.version())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-boot-repositories         : {0}", NutsTextUtils.desc(bOptions.getBootRepositories(), text)));
                LOGCRF.log(NutsMessage.jstyle("   nuts-runtime                   : {0}", getRuntimeId()));
                LOGCRF.log(NutsMessage.jstyle("   nuts-runtime-digest            : {0}",
                        text.ofStyled(new CoreDigestHelper(defaultSession()).append(bOptions.getClassWorldURLs()).getDigest(), NutsTextStyle.version())
                ));
                LOGCRF.log(NutsMessage.jstyle("   nuts-runtime-dependencies      : {0}",
                        text.builder().appendJoined(text.ofStyled(";", NutsTextStyle.separator()),
                                Arrays.stream(bOptions.getRuntimeBootDescriptor().getDependencies())
                                        .map(x -> NutsId.of(x.toString(), defaultSession()))
                                        .collect(Collectors.toList())
                        )
                ));
                LOGCRF.log(NutsMessage.jstyle("   nuts-runtime-urls              : {0}",
                        text.builder().appendJoined(text.ofStyled(";", NutsTextStyle.separator()),
                                Arrays.stream(bOptions.getClassWorldURLs())
                                        .map(x -> NutsPath.of(x.toString(), defaultSession()))
                                        .collect(Collectors.toList())
                        )
                ));
                LOGCRF.log(NutsMessage.jstyle("   nuts-extension-dependencies    : {0}",
                        text.builder().appendJoined(text.ofStyled(";", NutsTextStyle.separator()),
                                toIds(bOptions.getExtensionBootDescriptors()).stream()
                                        .map(x
                                                -> NutsId.of(x.toString(), defaultSession())
                                        )
                                        .collect(Collectors.toList())
                        )
                ));
//            if (hasUnsatisfiedRequirements()) {
//                LOG.log(Level.CONFIG, "\t execution-requirements         : unsatisfied {0}", getRequirementsHelpString(true));
//            } else {
//                LOG.log(Level.CONFIG, "\t execution-requirements         : satisfied");
//            }
                LOGCRF.log(NutsMessage.jstyle("   nuts-workspace                 : {0}", NutsTextUtils.formatLogValue(text, options.getWorkspace(), bOptions.getWorkspaceLocation())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-hash-name                 : {0}", getHashName()));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-apps                : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocation(NutsStoreLocation.APPS), bOptions.getStoreLocation(NutsStoreLocation.APPS))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-config              : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocation(NutsStoreLocation.CONFIG), bOptions.getStoreLocation(NutsStoreLocation.CONFIG))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-var                 : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocation(NutsStoreLocation.VAR), bOptions.getStoreLocation(NutsStoreLocation.VAR))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-log                 : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocation(NutsStoreLocation.LOG), bOptions.getStoreLocation(NutsStoreLocation.LOG))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-temp                : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocation(NutsStoreLocation.TEMP), bOptions.getStoreLocation(NutsStoreLocation.TEMP))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-cache               : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocation(NutsStoreLocation.CACHE), bOptions.getStoreLocation(NutsStoreLocation.CACHE))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-run                 : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocation(NutsStoreLocation.RUN), bOptions.getStoreLocation(NutsStoreLocation.RUN))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-lib                 : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocation(NutsStoreLocation.LIB), bOptions.getStoreLocation(NutsStoreLocation.LIB))));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-strategy            : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocationStrategy(), bOptions.getStoreLocationStrategy())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-repos-store-strategy      : {0}", NutsTextUtils.formatLogValue(text, options.getRepositoryStoreLocationStrategy(), bOptions.getRepositoryStoreLocationStrategy())));
                LOGCRF.log(NutsMessage.jstyle("   nuts-store-layout              : {0}", NutsTextUtils.formatLogValue(text, options.getStoreLocationLayout(), bOptions.getStoreLocationLayout() == null ? "system" : bOptions.getStoreLocationLayout())));
                LOGCRF.log(NutsMessage.jstyle("   option-read-only               : {0}", options.isReadOnly()));
                LOGCRF.log(NutsMessage.jstyle("   option-trace                   : {0}", options.isTrace()));
                LOGCRF.log(NutsMessage.jstyle("   option-progress                : {0}", NutsTextUtils.desc(options.getProgressOptions(), text)));
                LOGCRF.log(NutsMessage.jstyle("   inherited                      : {0}", options.isInherited()));
                LOGCRF.log(NutsMessage.jstyle("   inherited-nuts-boot-args       : {0}", System.getProperty("nuts.boot.args") == null ? NutsTextUtils.desc(null, text)
                        : NutsTextUtils.desc(NutsCommandLine.of(System.getProperty("nuts.boot.args"), NutsShellFamily.SH, defaultSession()), text)
                ));
                LOGCRF.log(NutsMessage.jstyle("   inherited-nuts-args            : {0}", System.getProperty("nuts.args") == null ? NutsTextUtils.desc(null, text)
                        : NutsTextUtils.desc(text.toText(NutsCommandLine.of(System.getProperty("nuts.args"), NutsShellFamily.SH, defaultSession())), text)
                ));
                LOGCRF.log(NutsMessage.jstyle("   option-open-mode               : {0}", NutsTextUtils.formatLogValue(text, options.getOpenMode(), options.getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : options.getOpenMode())));
                NutsBootTerminal b = getModel().bootModel.getBootTerminal();
                LOGCRF.log(NutsMessage.jstyle("   sys-terminal-flags             : {0}", String.join(", ", b.getFlags())));
                NutsTerminalMode terminalMode = wsModel.bootModel.getBootOptions().getTerminalMode();
                LOGCRF.log(NutsMessage.jstyle("   sys-terminal-mode              : {0}", terminalMode == null ? "default" : terminalMode));
                NutsWorkspaceEnvManager senv = defaultSession().env();
                LOGCRF.log(NutsMessage.jstyle("   java-home                      : {0}", System.getProperty("java.home")));
                LOGCRF.log(NutsMessage.jstyle("   java-classpath                 : {0}", System.getProperty("java.class.path")));
                LOGCRF.log(NutsMessage.jstyle("   java-library-path              : {0}", System.getProperty("java.library.path")));
                LOGCRF.log(NutsMessage.jstyle("   os-name                        : {0}", System.getProperty("os.name")));
                LOGCRF.log(NutsMessage.jstyle("   os-family                      : {0}", senv.getOsFamily()));
                LOGCRF.log(NutsMessage.jstyle("   os-dist                        : {0}", senv.getOsDist().getArtifactId()));
                LOGCRF.log(NutsMessage.jstyle("   os-arch                        : {0}", System.getProperty("os.arch")));
                LOGCRF.log(NutsMessage.jstyle("   os-shell                       : {0}", senv.getShellFamily()));
                LOGCRF.log(NutsMessage.jstyle("   os-shells                      : {0}", text.builder().appendJoined(",",Arrays.asList(senv.getShellFamilies()))));
                LOGCRF.log(NutsMessage.jstyle("   os-desktop                     : {0}", senv.getDesktopEnvironment()));
                LOGCRF.log(NutsMessage.jstyle("   os-desktop-family              : {0}", senv.getDesktopEnvironmentFamily()));
                LOGCRF.log(NutsMessage.jstyle("   os-desktops                    : {0}", text.builder().appendJoined(",",Arrays.asList(senv.getDesktopEnvironments()))));
                LOGCRF.log(NutsMessage.jstyle("   os-desktop-families            : {0}", text.builder().appendJoined(",",Arrays.asList(senv.getDesktopEnvironmentFamilies()))));
                LOGCRF.log(NutsMessage.jstyle("   os-desktop-path                : {0}", senv.getDesktopPath()));
                LOGCRF.log(NutsMessage.jstyle("   os-desktop-integration         : {0}", senv.getDesktopIntegrationSupport(NutsDesktopIntegrationItem.DESKTOP)));
                LOGCRF.log(NutsMessage.jstyle("   os-menu-integration            : {0}", senv.getDesktopIntegrationSupport(NutsDesktopIntegrationItem.MENU)));
                LOGCRF.log(NutsMessage.jstyle("   os-shortcut-integration        : {0}", senv.getDesktopIntegrationSupport(NutsDesktopIntegrationItem.SHORTCUT)));
                LOGCRF.log(NutsMessage.jstyle("   os-version                     : {0}", senv.getOsDist().getVersion()));
                LOGCRF.log(NutsMessage.jstyle("   user-name                      : {0}", System.getProperty("user.name")));
                LOGCRF.log(NutsMessage.jstyle("   user-dir                       : {0}", NutsPath.of(System.getProperty("user.dir"), defaultSession())));
                LOGCRF.log(NutsMessage.jstyle("   user-home                      : {0}", NutsPath.of(System.getProperty("user.home"), defaultSession())));
                LOGCRF.log(NutsMessage.jstyle("   user-locale                    : {0}", Locale.getDefault()));
                LOGCRF.log(NutsMessage.jstyle("   user-time-zone                 : {0}", TimeZone.getDefault()));
            }
            wsModel.securityModel = new DefaultNutsWorkspaceSecurityModel(this);

            long now = System.currentTimeMillis();
            if (options.getCreationTime() == 0 || options.getCreationTime() > now) {
                wsModel.configModel.setStartCreateTimeMillis(now);
            } else {
                wsModel.configModel.setStartCreateTimeMillis(options.getCreationTime());
            }

            boolean exists = NutsWorkspaceConfigManagerExt.of(_config).getModel().isValidWorkspaceFolder(defaultSession());
            NutsOpenMode openMode = options.getOpenMode();
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


            wsModel.configModel.onExtensionsPrepared(defaultSession());
            boolean justInstalled = false;
            NutsWorkspaceArchetypeComponent justInstalledArchetype = null;
            if (!loadWorkspace(defaultSession(), options.getExcludedExtensions(), null)) {
                wsModel.bootModel.setFirstBoot(true);
                if (wsModel.uuid == null) {
                    wsModel.uuid = UUID.randomUUID().toString();
                }
                //workspace wasn't loaded. Create new configuration...
                justInstalled = true;
                NutsWorkspaceUtils.of(defaultSession()).checkReadOnly();
                LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS)
                        .log(NutsMessage.jstyle("creating {0} workspace at {1}",
                                text.ofStyled("new", NutsTextStyle.info()),
                                defaultSession().locations().getWorkspaceLocation()
                        ));
                NutsWorkspaceConfigBoot bconfig = new NutsWorkspaceConfigBoot();
                //load from config with resolution applied
                bconfig.setUuid(wsModel.uuid);
                NutsWorkspaceConfigApi aconfig = new NutsWorkspaceConfigApi();
                aconfig.setApiVersion(apiVersion);
                aconfig.setRuntimeId(runtimeId == null ? null : runtimeId.toString());
                aconfig.setJavaCommand(options.getJavaCommand());
                aconfig.setJavaOptions(options.getJavaOptions());

                NutsWorkspaceConfigRuntime rconfig = new NutsWorkspaceConfigRuntime();
                rconfig.setDependencies(
                        Arrays.stream(bOptions.getRuntimeBootDescriptor().getDependencies())
                                .map(NutsBootId::toString)
                                .collect(Collectors.joining(";"))
                );
                rconfig.setId(runtimeId == null ? null : runtimeId.toString());

                bconfig.setBootRepositories(repositories);
                bconfig.setStoreLocationStrategy(options.getStoreLocationStrategy());
                bconfig.setRepositoryStoreLocationStrategy(options.getRepositoryStoreLocationStrategy());
                bconfig.setStoreLocationLayout(options.getStoreLocationLayout());
                bconfig.setGlobal(options.isGlobal());
                bconfig.setStoreLocations(new NutsStoreLocationsMap(options.getStoreLocations()).toMapOrNull());
                bconfig.setHomeLocations(new NutsHomeLocationsMap(options.getHomeLocations()).toMapOrNull());

                boolean namedWorkspace = CoreNutsUtils.isValidWorkspaceName(options.getWorkspace());
                if (bconfig.getStoreLocationStrategy() == null) {
                    bconfig.setStoreLocationStrategy(namedWorkspace ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
                }
                if (bconfig.getRepositoryStoreLocationStrategy() == null) {
                    bconfig.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                }
                bconfig.setName(CoreNutsUtils.resolveValidWorkspaceName(options.getWorkspace()));

                wsModel.configModel.setCurrentConfig(new DefaultNutsWorkspaceCurrentConfig(this)
                        .merge(aconfig, defaultSession())
                        .merge(bconfig, defaultSession())
                        .build(defaultSession().locations().getWorkspaceLocation(), defaultSession()));
                wsModel.configModel.setConfigBoot(bconfig, defaultSession());
                wsModel.configModel.setConfigApi(aconfig, defaultSession());
                wsModel.configModel.setConfigRuntime(rconfig, defaultSession());
                //load all "---config.*" custom options into persistent config
                for (String customOption : options.getCustomOptions()) {
                    NutsArgument a = NutsArgument.of(customOption, defaultSession());
                    if (a.getKey().getString().startsWith("config.")) {
                        if (a.isActive()) {
                            defaultSession().config().setConfigProperty(
                                    a.getKey().getString("").substring("config.".length()),
                                    a.getValue().getString()
                            );
                        }
                    }
                }
                justInstalledArchetype = initializeWorkspace(options.getArchetype(), defaultSession());
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
                    NutsTexts txt = text.setSession(defaultSession());
                    NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/includes/standard-header.ntf",
                            txt.parser().createLoader(getClass().getClassLoader())
                    );
                    out.println(n == null ? "no help found" : n.toString().trim());
                    if (NutsWorkspaceUtils.isUserDefaultWorkspace(defaultSession())) {
                        out.println(
                                txt.builder()
                                        .append("location", NutsTextStyle.underlined())
                                        .append(":")
                                        .append(defaultSession().locations().getWorkspaceLocation())
                                        .append(" ")
                        );
                    } else {
                        out.println(
                                txt.builder()
                                        .append("location", NutsTextStyle.underlined())
                                        .append(":")
                                        .append(defaultSession().locations().getWorkspaceLocation())
                                        .append(" ")
                                        .append(" (")
                                        .append(getHashName())
                                        .append(")")
                        );
                    }

                    NutsTableFormat.of(defaultSession()).setValue(
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
                wsModel.configModel.installBootIds(defaultSession().copy().setConfirm(NutsConfirmationMode.YES));
            } else {
                wsModel.bootModel.setFirstBoot(false);
                wsModel.uuid = wsModel.configModel.getStoreModelBoot().getUuid();
                if (NutsBlankable.isBlank(wsModel.uuid)) {
                    wsModel.uuid = UUID.randomUUID().toString();
                    wsModel.configModel.getStoreModelBoot().setUuid(wsModel.uuid);
                }
                if (options.isRecover()) {
                    wsModel.configModel.setBootApiVersion(cfg.getApiVersion(), defaultSession());
                    wsModel.configModel.setBootRuntimeId(cfg.getRuntimeId(),
                            Arrays.stream(bOptions.getRuntimeBootDescriptor().getDependencies())
                                    .map(NutsBootId::toString)
                                    .collect(Collectors.joining(";")),
                            defaultSession());
                    wsModel.configModel.setBootRepositories(cfg.getBootRepositories(), defaultSession());
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
                    justInstalledArchetype = initializeWorkspace(options.getArchetype(), defaultSession());
                }
                List<String> transientRepositoriesSet = options.getRepositories() == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(options.getRepositories()));
                NutsRepositoryDB repoDB = NutsRepositoryDB.of(defaultSession());
                NutsRepositorySelectorList expected = NutsRepositorySelectorList.ofAll(transientRepositoriesSet.toArray(new String[0]), repoDB, defaultSession());
                for (NutsRepositoryLocation loc : expected.resolve(null, repoDB)) {
                    NutsAddRepositoryOptions d = NutsRepositorySelectorHelper.createRepositoryOptions(loc, false, defaultSession());
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
            wsModel.configModel.setEndCreateTimeMillis(System.currentTimeMillis());
            if (justInstalled) {
                try {
                    Map rec = wsModel.recomm.askCompanionsRecommendations(new RequestQueryInfo(null, ""), defaultSession());
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
                justInstalledArchetype.startWorkspace(defaultSession());
                DefaultNutsWorkspaceEvent workspaceCreatedEvent = new DefaultNutsWorkspaceEvent(defaultSession(), null, null, null, null);
                for (NutsWorkspaceListener workspaceListener : defaultSession().events().getWorkspaceListeners()) {
                    workspaceListener.onCreateWorkspace(workspaceCreatedEvent);
                }
            }
            if (options.getUserName() != null && options.getUserName().trim().length() > 0) {
                char[] password = options.getCredentials();
                if (password == null || NutsBlankable.isBlank(new String(password))) {
                    password = defaultSession().config().getDefaultTerminal().readPassword("Password : ");
                }
                defaultSession().security().setSession(defaultSession()).login(options.getUserName(), password);
            }
            wsModel.configModel.setEndCreateTimeMillis(System.currentTimeMillis());
            LOG.with().session(defaultSession()).level(Level.FINE).verb(NutsLogVerb.SUCCESS)
                    .log(
                            NutsMessage.jstyle("```sh nuts``` workspace loaded in ```error {0}```",
                                    CoreTimeUtils.formatPeriodMilli(_boot.getCreationFinishTimeMillis() - _boot.getCreationStartTimeMillis())
                            )
                    );
            if (defaultSession().boot().getBootCustomBoolArgument(false, false, false, "---perf")) {
                if (defaultSession().isPlainOut()) {
                    defaultSession().out().printf("```sh nuts``` workspace loaded in %s%n",
                            text.ofStyled(CoreTimeUtils.formatPeriodMilli(_boot.getCreationFinishTimeMillis() - _boot.getCreationStartTimeMillis()),
                                    NutsTextStyle.error()
                            )
                    );
                } else {
                    defaultSession().eout().add(elems.ofObject()
                            .set("workspace-loaded-in",
                                    elems.ofObject()
                                            .set("ms", _boot.getCreationFinishTimeMillis() - _boot.getCreationStartTimeMillis())
                                            .set("text", CoreTimeUtils.formatPeriodMilli(_boot.getCreationFinishTimeMillis() - _boot.getCreationStartTimeMillis()))
                                            .build()

                            )
                            .build());
                }
            }
        } catch (RuntimeException ex) {
            if (wsModel != null && wsModel.recomm != null) {
                try {
                    NutsSession s = defaultSession();
                    displayRecommendations(wsModel.recomm.askBootstrapFailureRecommendations(new RequestQueryInfo(null, ex), s), s);
                } catch (Exception ex2) {
                    //just ignore
                }
            }
            throw ex;
        } finally {
            if (wsModel.bootModel != null) {
                wsModel.bootModel.setInitializing(false);
            }
        }
    }

    private void displayRecommendations(Object r, NutsSession s) {
        if (s != null) {
            Map<String, Object> a = new HashMap<>();
            a.put("recommendations", r);
            s.out().printlnf(a);
        }
    }

    private URL getApiURL() {
        NutsBootId nid = new NutsBootId("net.thevpc.nuts", "nuts", NutsBootVersion.parse(Nuts.getVersion()));
        return NutsApiUtils.findClassLoaderJar(nid, NutsClassLoaderUtils.resolveClasspathURLs(Thread.currentThread().getContextClassLoader()));
    }

    private String getApiDigest() {
        if (NutsBlankable.isBlank(wsModel.apiDigest)) {
            wsModel.apiDigest = new CoreDigestHelper(defaultSession()).append(getApiURL()).getDigest();
        }
        return wsModel.apiDigest;
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
        NutsDescriptorBuilder descrWithParents = descriptor.builder();
        if(parentDescriptors.length>0){
            descrWithParents.applyParents(parentDescriptors);
        }
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
        LinkedHashSet<NutsDependency> effStandardDeps=new LinkedHashSet<>();
        for (NutsDependency standardDependency : effectiveDescriptor.getStandardDependencies()) {
            if ("import".equals(standardDependency.getScope())) {
                NutsDescriptor dd = session.fetch().setId(standardDependency.toId()).setEffective(true).setSession(session).getResultDescriptor();
                Collections.addAll(effStandardDeps, dd.getStandardDependencies());
            }else{
                effStandardDeps.add(standardDependency);
            }
        }
        for (NutsDependency d : oldDependencies) {
            if (NutsBlankable.isBlank(d.getScope())
                    || d.getVersion().isBlank()
                    || NutsBlankable.isBlank(d.getOptional())) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : effStandardDeps) {
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
                if (d.getVersion().isBlank()) {
                    LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("failed to resolve effective version for {0}", d));
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
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + wsModel.configModel
                + '}';
    }

    protected NutsWorkspaceArchetypeComponent initializeWorkspace(String archetype, NutsSession session) {
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

        return instance;
    }

    private void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(this, session);
    }

    private NutsId resolveApiId(NutsId id, Set<NutsId> visited, NutsSession session) {
        if (visited.contains(id.getLongId())) {
            return null;
        }
        visited.add(id.getLongId());
        if (id.getShortName().equals("net.thevpc.nuts:nuts")) {
            return id;
        }
        for (NutsDependency dependency : session.fetch().setId(id).getResultDescriptor().getDependencies()) {
            NutsId q = resolveApiId(dependency.toId(), visited, session);
            if (q != null) {
                return q;
            }
        }
        return null;
    }

    public void installOrUpdateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session,
                                    boolean resolveInstaller, boolean updateDefaultVersion, InstallStrategy0 strategy0, boolean requireDependencies, NutsId[] forIds,
                                    NutsDependencyScope scope) {
        checkSession(session);
        if (def == null) {
            return;
        }
        try {
            Map rec = null;
            if (strategy0 == InstallStrategy0.INSTALL) {
                rec = wsModel.recomm.askInstallRecommendations(new RequestQueryInfo(def.getId().toString(), ""), session);
            } else if (strategy0 == InstallStrategy0.UPDATE) {
                rec = wsModel.recomm.askUpdateRecommendations(new RequestQueryInfo(def.getId().toString(), ""), session);
            } else {
                //just ignore any dependencies. recommendations are related to main artifacts
            }
            //TODO: should check here for any security issue!
        } catch (Exception ex2) {
            //just ignore
        }
        NutsDependencyFilter ndf = NutsDependencyFilters.of(session).byRunnable();
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
            NutsTexts text = NutsTexts.of(session);
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
                if (def.getFile() != null) {
                    installerComponent = getInstaller(def, session);
                }
            }
        }
//        checkSession(session);
        NutsDefinition oldDef = null;
        if (strategy0 == InstallStrategy0.UPDATE) {
            switch (def.getDescriptor().getIdType()) {
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
                            .setInstallStatus(NutsInstallStatusFilters.of(session).byDeployed(true))
                            .setFailFast(false).getResultDefinitions().first();
                    break;
                }
            }
        }
        NutsPrintStream out = session.out();
        out.flush();
        NutsInstallInformation newNutsInstallInformation = null;
        NutsWorkspaceConfigManager config = session.config().setSession(session);
        if (def.getFile() != null) {
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
                                if (dd.getFile() == null) {
                                    throw new NutsInstallException(session, def.getId(),
                                            NutsMessage.cstyle("unable to install %s. required dependency content is missing for %s", def.getId(), dependency.toId()),
                                            null);
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
                    .setSession(session.copy())
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
            cc.setWorkspace(cc.getSession().getWorkspace());
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
                                NutsMessage.cstyle("unable to update %s", def.getId()),
                                ex);
                    }
                }
            } else if (strategy0 == InstallStrategy0.INSTALL) {
                if (installerComponent != null) {
                    try {
                        installerComponent.install(executionContext);
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
                            try {
                                Map rec = null;
                                if (strategy0 == InstallStrategy0.INSTALL) {
                                    rec = wsModel.recomm.askInstallFailureRecommendations(new RequestQueryInfo(def.getId().toString(), ""), session);
                                } else if (strategy0 == InstallStrategy0.UPDATE) {
                                    rec = wsModel.recomm.askUpdateFailureRecommendations(new RequestQueryInfo(def.getId().toString(), ""), session);
                                } else {
                                    //just ignore any dependencies. recommendations are related to main artifacts
                                }
                                //TODO: should check here for any security issue!
                            } catch (Exception ex3) {
                                //just ignore
                            }
                        }
                        throw new NutsExecutionException(session, NutsMessage.cstyle("unable to install %s", def.getId()), ex);
                    }
                }
            }
        } else {
            throw new NutsExecutionException(session,
                    NutsMessage.cstyle("unable to install %s: unable to locate content", def.getId()),
                    101);
        }

        NutsId forId = (forIds == null || forIds.length == 0) ? null : forIds[0];
        switch (def.getDescriptor().getIdType()) {
            case API: {
                wsModel.configModel.prepareBootClassPathConf(NutsIdType.API, def.getId(),
                        forId
                        , null, true, false, session);
                break;
            }
            case RUNTIME:
            case EXTENSION: {
                wsModel.configModel.prepareBootClassPathConf(
                        def.getDescriptor().getIdType(),
                        def.getId(),
                        forId
                        , null, true, true, session);
                break;
            }
        }

        if (strategy0 == InstallStrategy0.UPDATE) {
            wu.events().fireOnUpdate(new DefaultNutsUpdateEvent(oldDef, def, session, reinstall));
        } else if (strategy0 == InstallStrategy0.REQUIRE) {
            wu.events().fireOnRequire(new DefaultNutsInstallEvent(def, session, forIds, reinstall));
        } else if (strategy0 == InstallStrategy0.INSTALL) {
            wu.events().fireOnInstall(new DefaultNutsInstallEvent(def, session, new NutsId[0], reinstall));
        }

        if (def.getDescriptor().getIdType() == NutsIdType.EXTENSION) {
            NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(config);
            NutsExtensionListHelper h = new NutsExtensionListHelper(
                    session.getWorkspace().getApiId(),
                    wcfg.getModel().getStoredConfigBoot().getExtensions())
                    .save();
            h.add(def.getId(), def.getDependencies().transitiveWithSource()
                    .toArray(NutsDependency[]::new));
            wcfg.getModel().getStoredConfigBoot().setExtensions(h.getConfs());
            wcfg.getModel().fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
        }

        if (session.isPlainTrace()) {
            String setAsDefaultString = "";
            NutsTexts text = NutsTexts.of(session);
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
                                        def.getPath(),
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
                                        def.getPath(),
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
        throw new NutsElementNotFoundException(session,
                NutsMessage.cstyle("unable to resolve command name for %s", id
                ));
    }

    protected boolean loadWorkspace(NutsSession session, String[] excludedExtensions, String[] excludedRepositories) {
        checkSession(session);
        if (wsModel.configModel.loadWorkspace(session)) {
            //extensions already wired... this is needless!
            for (NutsId extensionId : session.extensions().setSession(session).getConfigExtensions()) {
                if (wsModel.extensionModel.isExcludedExtension(extensionId)) {
                    continue;
                }
                NutsSession sessionCopy = session.copy();
                wsModel.extensionModel.wireExtension(extensionId,
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
        NutsTexts txt = NutsTexts.of(session);
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-welcome.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no welcome found" : n.toString());
    }

    @Override
    public String getHelpText(NutsSession session) {
        NutsTexts txt = NutsTexts.of(session);
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-help.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no help found" : n.toString());
    }

    @Override
    public String getLicenseText(NutsSession session) {
        NutsTexts txt = NutsTexts.of(session);
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/nuts-license.ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        return (n == null ? "no license found" : n.toString());
    }

    @Override
    public String resolveDefaultHelp(Class clazz, NutsSession session) {
        NutsId nutsId = NutsIdResolver.of(session).resolveId(clazz);
        if (nutsId != null) {
            String urlPath = "/" + nutsId.getGroupId().replace('.', '/') + "/" + nutsId.getArtifactId() + ".ntf";

            NutsTexts txt = NutsTexts.of(session);
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
                        NutsMessage.cstyle("unable to fetchEffective for %s. best Result is %s", thisId, thisId),
                        null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v) || CoreStringUtils.containsVars(a)) {
            Map<String, String> p = NutsDescriptorUtils.getPropertiesMap(descriptor.getProperties(), session);
            NutsId bestId = NutsIdBuilder.of(session).setGroupId(g).setArtifactId(thisId.getArtifactId()).setVersion(v).build();
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
                bestId = bestId.builder().apply(new MapToFunction(NutsDescriptorUtils.getPropertiesMap(dd.getProperties(), session))).build();
                if (CoreNutsUtils.isEffectiveId(bestId)) {
                    return bestId;
                }
                all.addAll(Arrays.asList(dd.getParents()));
            }
            throw new NutsNotFoundException(session, bestId,
                    NutsMessage.cstyle("unable to fetchEffective for %s. best Result is %s", bestId, bestId), null);
        }
        NutsId bestId = NutsIdBuilder.of(session).setGroupId(g).setArtifactId(thisId.getArtifactId()).setVersion(v).build();
        if (!CoreNutsUtils.isEffectiveId(bestId)) {
            throw new NutsNotFoundException(session, bestId,
                    NutsMessage.cstyle("unable to fetchEffective for %s. best Result is %s", thisId, bestId), null);
        }
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
        if (nutToInstall != null && nutToInstall.getFile() != null) {
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
            NutsInstallerComponent best = session.extensions().setSession(session)
                    .createSupported(NutsInstallerComponent.class, false, runnerFile);
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
        NutsPath eff = null;
        NutsWorkspaceLocationManager loc = session.locations().setSession(session);
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            NutsPath l = loc.getStoreLocation(descriptor.getId(), NutsStoreLocation.CACHE);
            String nn = loc.getDefaultIdFilename(descriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
            if (eff.isRegularFile()) {
                try {
                    NutsDescriptor d = NutsDescriptorParser.of(session).parse(eff);
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
        NutsDescriptorUtils.checkValidEffectiveDescriptor(effectiveDescriptor, session);
        if (eff == null) {
            NutsPath l = session.locations().getStoreLocation(effectiveDescriptor.getId(), NutsStoreLocation.CACHE);
            String nn = loc.getDefaultIdFilename(effectiveDescriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
        }
        try {
            effectiveDescriptor.formatter().setSession(session).setNtf(false).print(eff);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.FINE).error(ex)
                    .log(NutsMessage.jstyle("failed to print {0}", eff));
            //
        }
        return effectiveDescriptor;
    }

    @Override
    public NutsInstalledRepository getInstalledRepository() {
        return wsModel.installedRepository;
    }

    @Override
    public NutsInstallStatus getInstallStatus(NutsId id, boolean checkDependencies, NutsSession session) {
        NutsDefinition nutToInstall;
        try {
            nutToInstall = session.search().addId(id)
                    .setSession(session.copy().setTransitive(false))
                    .setInlineDependencies(checkDependencies)
                    .setInstallStatus(NutsInstallStatusFilters.of(session).byDeployed(true))
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
        a.put("dependencies", m.getDependencies().transitive().map(NutsDependency::getLongName, "getLongName")
                .collect(Collectors.joining(";")));
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
            NutsPath bootstrapFolder = session.locations().getStoreLocation(NutsStoreLocation.LIB).resolve(NutsConstants.Folders.ID);
            NutsId id2 = def.getId();
            NutsCp.of(session).from(def.getFile())
                    .to(bootstrapFolder.resolve(session.locations().getDefaultIdBasedir(id2))
                            .resolve(session.locations().getDefaultIdFilename(id2.builder().setFaceContent().setPackaging("jar").build()))
                    ).run();
            session.fetch().setId(id2).getResultDescriptor().formatter().setSession(session).setNtf(false)
                    .print(bootstrapFolder.resolve(session.locations().getDefaultIdBasedir(id2))
                            .resolve(session.locations().getDefaultIdFilename(id2.builder().setFaceDescriptor().build())));

            Map<String, String> pr = new LinkedHashMap<>();
            pr.put("file.updated.date", Instant.now().toString());
            pr.put("project.id", def.getId().getShortId().toString());
            pr.put("project.name", def.getId().getShortId().toString());
            pr.put("project.version", def.getId().getVersion().toString());
            NutsRepositoryDB repoDB = NutsRepositoryDB.of(session);
            pr.put("repositories", "~/.m2/repository"
                    + ";" + NutsRepositorySelectorHelper.createRepositoryOptions(NutsRepositoryLocation.of("vpc-public-maven", repoDB, session), true, session).getConfig().getLocation()
                    + ";" + NutsRepositorySelectorHelper.createRepositoryOptions(NutsRepositoryLocation.of("maven-central", repoDB, session), true, session).getConfig().getLocation()
                    + ";" + NutsRepositorySelectorHelper.createRepositoryOptions(NutsRepositoryLocation.of("vpc-public-nuts", repoDB, session), true, session).getConfig().getLocation()
            );
            pr.put("project.dependencies.compile",
                    String.join(";",
                            def.getDependencies().transitive()
                                    .filter(x -> !x.isOptional()
                                                    && NutsDependencyFilters.of(session).byRunnable()
                                                    .acceptDependency(def.getId(), x, session),
                                            "isOptional && runnable"
                                    )
                                    .map(x -> x.toId().getLongName(), "toId.getLongName")
                                    .toList()
                    )
            );

            try (Writer writer = bootstrapFolder.resolve(session.locations().getDefaultIdBasedir(def.getId().getLongId()))
                    .resolve("nuts.properties").getWriter()
            ) {
                CoreIOUtils.storeProperties(pr, writer, false, session);
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        }
    }

    public NutsSession defaultSession() {
        if (wsModel.initSession != null) {
            return wsModel.initSession;
        }
        return wsModel.bootModel.bootSession();
    }

    @Override
    public NutsWorkspaceModel getModel() {
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
            wsModel.hashName = NutsHashName.of(defaultSession()).getHashName(this);
        }
        return wsModel.hashName;
    }

    @Override
    public NutsVersion getApiVersion() {
        return wsModel.apiVersion;
    }

    @Override
    public NutsId getApiId() {
        return wsModel.apiId;
    }

    @Override
    public NutsId getRuntimeId() {
        return wsModel.runtimeId;
    }

    @Override
    public NutsPath getLocation() {
        return NutsPath.of(wsModel.location, wsModel.bootModel.bootSession());
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new DefaultNutsSession(this);
        nutsSession.setTerminal(NutsSessionTerminal.of(nutsSession));
        nutsSession.setExpireTime(nutsSession.boot().getBootOptions().getExpireTime());
        return nutsSession;
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
    public NutsInfoCommand info() {
        return new DefaultNutsInfoCommand(defaultSession());
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return new DefaultNutsWorkspaceExtensionManager(wsModel.extensionModel);
    }

    @Override
    public NutsWorkspaceConfigManager config() {
        return new DefaultNutsWorkspaceConfigManager(wsModel.configModel);
    }

    @Override
    public NutsRepositoryManager repos() {
        return new DefaultNutsRepositoryManager(wsModel.repositoryModel);
    }

    @Override
    public NutsWorkspaceSecurityManager security() {
        return new DefaultNutsWorkspaceSecurityManager(wsModel.securityModel);
    }

    @Override
    public NutsWorkspaceEventManager events() {
        return new DefaultNutsWorkspaceEventManager(wsModel.eventsModel);
    }

    @Override
    public NutsImportManager imports() {
        return new DefaultImportManager(wsModel.importModel);
    }

    @Override
    public NutsCustomCommandManager commands() {
        return new DefaultCustomCommandManager(wsModel.aliasesModel);
    }

    @Override
    public NutsWorkspaceLocationManager locations() {
        return new DefaultNutsWorkspaceLocationManager(wsModel.locationsModel);
    }

    @Override
    public NutsWorkspaceEnvManager env() {
        return new DefaultNutsWorkspaceEnvManager(wsModel.envModel);
    }

    @Override
    public NutsBootManager boot() {
        return new DefaultNutsBootManager(wsModel.bootModel);
    }

    public DefaultNutsWorkspaceEnvManagerModel getEnvModel() {
        return wsModel.envModel;
    }

    public DefaultCustomCommandsModel getAliasesModel() {
        return wsModel.aliasesModel;
    }

    public DefaultNutsWorkspaceConfigModel getConfigModel() {
        return wsModel.configModel;
    }

    public DefaultImportModel getImportModel() {
        return wsModel.importModel;
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
