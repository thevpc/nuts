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
import net.thevpc.nuts.boot.NWorkspaceBootOptions;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.cmdline.NCommandLines;
import net.thevpc.nuts.elem.NElementNotFoundException;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.format.NTableModel;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.DefaultNProperties;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.boot.NBootConfig;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.event.*;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.extension.NExtensionListHelper;
import net.thevpc.nuts.runtime.standalone.id.util.NIdUtils;
import net.thevpc.nuts.runtime.standalone.installer.CommandForIdNInstallerComponent;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogModel;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogger;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNWorkspaceSecurityModel;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreTimeUtils;
import net.thevpc.nuts.runtime.standalone.util.MapToFunction;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.DefaultNFilterModel;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.NRecommendationPhase;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.RequestQueryInfo;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

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
@NComponentScope(NComponentScopeType.PROTOTYPE)
public class DefaultNWorkspace extends AbstractNWorkspace implements NWorkspaceExt {

    public static final NVersion VERSION_INSTALL_INFO_CONFIG = NVersion.of("0.8.0").get();
    public static final NVersion VERSION_SDK_LOCATION = NVersion.of("0.8.0").get();
    public static final NVersion VERSION_REPOSITORY_CONFIG = NVersion.of("0.8.0").get();
    public static final String VERSION_REPOSITORY_REF = "0.8.0";
    public static final String VERSION_WS_CONFIG_API = "0.8.0";
    public static final NVersion VERSION_WS_CONFIG_BOOT = NVersion.of("0.8.0").get();
    public static final String VERSION_WS_CONFIG_MAIN = "0.8.0";
    public static final String VERSION_WS_CONFIG_RUNTIME = "0.8.0";
    public static final String VERSION_WS_CONFIG_SECURITY = "0.8.0";
    public static final String VERSION_COMMAND_ALIAS_CONFIG = "0.8.0";
    public static final String VERSION_COMMAND_ALIAS_CONFIG_FACTORY = "0.8.0";
    public static final String VERSION_USER_CONFIG = "0.8.0";
    public NLogger LOG;
    private NWorkspaceModel wsModel;

    public DefaultNWorkspace(NWorkspaceBootOptions info) {
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
    private static Set<NId> toIds(List<NDescriptor> all) {
        Set<NId> set = new LinkedHashSet<>();
        for (NDescriptor i : all) {
            set.add(i.getId());
            set.addAll(i.getDependencies().stream().map(NDependency::toId).collect(Collectors.toList()));
        }
        return set;
    }

    private void initWorkspace(NWorkspaceBootOptions bOption0) {
        Objects.requireNonNull(bOption0, () -> "boot options could not be null");
        bOption0 = bOption0.readOnly();
        try {
            this.wsModel = new NWorkspaceModel(this);
            this.wsModel.bootModel = new DefaultNBootModel(this);
            this.wsModel.bootModel.init(bOption0);
            this.LOG = new DefaultNLogger(this, defaultSession(), DefaultNWorkspace.class, true);

            NWorkspaceBootOptions bootOptions = this.wsModel.bootModel.getBootEffectiveOptions();
            NWorkspaceOptions userOptions = bootOptions.getUserOptions().get();
            this.wsModel.configModel = new DefaultNWorkspaceConfigModel(this);
            String workspaceLocation = bootOptions.getWorkspace().orNull();
            NVersion apiVersion = bootOptions.getApiVersion().orNull();
            NId runtimeId = bootOptions.getRuntimeId().orNull();
            if (runtimeId == null) {
                runtimeId = NId.ofRuntime("").get();
            }
            String repositories = bootOptions.getBootRepositories().orNull();
            NBootWorkspaceFactory bootFactory = bootOptions.getBootWorkspaceFactory().orNull();
            ClassLoader bootClassLoader = bootOptions.getClassWorldLoader().orNull();
            this.wsModel.extensionModel = new DefaultNWorkspaceExtensionModel(this, bootFactory,
                    bootOptions.getExcludedExtensions().orElse(Collections.emptyList()));
            this.wsModel.logModel = new DefaultNLogModel(this, bootOptions);
            this.wsModel.logModel.setDefaultSession(defaultSession());
            this.wsModel.extensionModel.onInitializeWorkspace(bootOptions, bootClassLoader, defaultSession());
            NWorkspaceConfigManager _config = defaultSession().config();
            NBootManager _boot = defaultSession().boot();
            NBootConfig cfg = new NBootConfig();
            cfg.setWorkspace(workspaceLocation);
            cfg.setApiVersion(apiVersion);
            cfg.setRuntimeId(runtimeId);
            cfg.setRuntimeBootDescriptor(bootOptions.getRuntimeBootDescriptor().orNull());
            cfg.setExtensionBootDescriptors(bootOptions.getExtensionBootDescriptors().orNull());

            this.wsModel.apiVersion = Nuts.getVersion();
            this.wsModel.apiId = NId.ofApi(apiVersion).get();
            this.wsModel.runtimeId = NId.of(
                    runtimeId.getGroupId(),
                    runtimeId.getArtifactId(),
                    NVersion.of(runtimeId.getVersion().toString()).get()).get();

            this.wsModel.filtersModel = new DefaultNFilterModel(this);
            this.wsModel.installedRepository = new DefaultNInstalledRepository(this, bootOptions);
            this.wsModel.repositoryModel = new DefaultNRepositoryModel(this);
            this.wsModel.envModel = new DefaultNWorkspaceEnvManagerModel(this, defaultSession());
            this.wsModel.sdkModel = new DefaultNPlatformModel(this.wsModel.envModel);
            this.wsModel.aliasesModel = new DefaultCustomCommandsModel(this);
            this.wsModel.importModel = new DefaultImportModel(this);
            this.wsModel.locationsModel = new DefaultNWorkspaceLocationModel(this,
                    Paths.get(bootOptions.getWorkspace().orNull()).toString());
            this.wsModel.eventsModel = new DefaultNWorkspaceEventModel(this);
            this.wsModel.textModel = new DefaultNTextManagerModel(this);
            this.wsModel.location = bootOptions.getWorkspace().orNull();

            this.wsModel.bootModel.onInitializeWorkspace();

            NSystemTerminalBase termb = defaultSession().extensions()
                    .createSupported(NSystemTerminalBase.class);
            defaultSession().config()
                    .setSystemTerminal(termb)
                    .setDefaultTerminal(NSessionTerminal.of(defaultSession())
                    );
            wsModel.bootModel.bootSession().setTerminal(NSessionTerminal.of(wsModel.bootModel.bootSession()));
            ((DefaultNLogger) LOG).resumeTerminal(defaultSession());

            NTexts text = NTexts.of(defaultSession());
            try {
                text.getTheme();
            } catch (Exception ex) {
                LOG.with().level(Level.CONFIG).verb(NLoggerVerb.FAIL).session(defaultSession())
                        .log(NMsg.ofJstyle("unable to load theme {0}. Reset to default!", bootOptions.getTheme()));
                text.setTheme("");//set default!
            }

            NLoggerOp LOGCRF = LOG.with().level(Level.CONFIG).verb(NLoggerVerb.READ).session(defaultSession());
            NLoggerOp LOGCSF = LOG.with().level(Level.CONFIG).verb(NLoggerVerb.START).session(defaultSession());
//        NutsFormatManager formats = this.formats().setSession(defaultSession());
            NElements elems = NElements.of(defaultSession());
            if (LOG.isLoggable(Level.CONFIG)) {
                //just log known implementations
                NCommandLines.of(defaultSession());
                NTerminals.of(defaultSession());
                NPrintStreams.of(defaultSession());
                NVersionFormat.of(defaultSession());
                NIdFormat.of(defaultSession());
                NIO.of(defaultSession());

                LOGCSF.log(NMsg.ofPlain(" ==============================================================================="));
                String s = CoreIOUtils.loadString(getClass().getResourceAsStream("/net/thevpc/nuts/runtime/includes/standard-header.ntf"), true, defaultSession());
                s = s.replace("${nuts.workspace-runtime.version}", Nuts.getVersion().toString());
                for (String s1 : s.split("\n")) {
                    LOGCSF.log(NMsg.ofPlain(s1));
                }
                LOGCSF.log(NMsg.ofPlain(" "));
                LOGCSF.log(NMsg.ofPlain(" = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = ="));
                LOGCSF.log(NMsg.ofPlain(" "));
                LOGCSF.log(NMsg.ofJstyle("start ```sh nuts``` ```primary3 {0}``` at {1}", Nuts.getVersion(), CoreNUtils.DEFAULT_DATE_TIME_FORMATTER.format(bOption0.getCreationTime().get())));
                LOGCRF.log(NMsg.ofJstyle("open Nuts Workspace               : {0}",
                        bootOptions.toCommandLine()
                ));
                LOGCRF.log(NMsg.ofJstyle("open Nuts Workspace (compact)     : {0}",
                        bootOptions.toCommandLine(new NWorkspaceOptionsConfig().setCompact(true))));

                LOGCRF.log(NMsg.ofPlain("open Workspace with config        : "));
                LOGCRF.log(NMsg.ofJstyle("   nuts-workspace-uuid            : {0}", NTextUtils.desc(bootOptions.getUuid().orNull(), text)));
                LOGCRF.log(NMsg.ofJstyle("   nuts-workspace-name            : {0}", NTextUtils.desc(bootOptions.getName().orNull(), text)));
                LOGCRF.log(NMsg.ofJstyle("   nuts-api-version               : {0}", Nuts.getVersion()));
                LOGCRF.log(NMsg.ofJstyle("   nuts-api-url                   : {0}", NPath.of(getApiURL(), defaultSession())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-api-digest                : {0}", text.ofStyled(getApiDigest(), NTextStyle.version())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-boot-repositories         : {0}", NTextUtils.desc(bootOptions.getBootRepositories().orNull(), text)));
                LOGCRF.log(NMsg.ofJstyle("   nuts-runtime                   : {0}", getRuntimeId()));
                LOGCRF.log(NMsg.ofJstyle("   nuts-runtime-digest            : {0}",
                        text.ofStyled(new CoreDigestHelper(defaultSession()).append(bootOptions.getClassWorldURLs().orNull()).getDigest(), NTextStyle.version())
                ));
                LOGCRF.log(NMsg.ofJstyle("   nuts-runtime-dependencies      : {0}",
                        text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                                bootOptions.getRuntimeBootDescriptor().get().getDependencies().stream()
                                        .map(x -> NId.of(x.toString()).get())
                                        .collect(Collectors.toList())
                        )
                ));
                LOGCRF.log(NMsg.ofJstyle("   nuts-runtime-urls              : {0}",
                        text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                                bootOptions.getClassWorldURLs().get().stream()
                                        .map(x -> NPath.of(x.toString(), defaultSession()))
                                        .collect(Collectors.toList())
                        )
                ));
                LOGCRF.log(NMsg.ofJstyle("   nuts-extension-dependencies    : {0}",
                        text.ofBuilder().appendJoined(text.ofStyled(";", NTextStyle.separator()),
                                toIds(bootOptions.getExtensionBootDescriptors().orElseGet(Collections::emptyList)).stream()
                                        .map(x
                                                -> NId.of(x.toString()).get()
                                        )
                                        .collect(Collectors.toList())
                        )
                ));
                LOGCRF.log(NMsg.ofJstyle("   nuts-workspace                 : {0}", NTextUtils.formatLogValue(text, userOptions.getWorkspace().orNull(), bootOptions.getWorkspace().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-hash-name                 : {0}", getHashName()));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-apps                : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocation(NStoreLocation.APPS).orNull(), bootOptions.getStoreLocation(NStoreLocation.APPS).orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-config              : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocation(NStoreLocation.CONFIG).orNull(), bootOptions.getStoreLocation(NStoreLocation.CONFIG).orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-var                 : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocation(NStoreLocation.VAR).orNull(), bootOptions.getStoreLocation(NStoreLocation.VAR).orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-log                 : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocation(NStoreLocation.LOG).orNull(), bootOptions.getStoreLocation(NStoreLocation.LOG).orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-temp                : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocation(NStoreLocation.TEMP).orNull(), bootOptions.getStoreLocation(NStoreLocation.TEMP).orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-cache               : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocation(NStoreLocation.CACHE).orNull(), bootOptions.getStoreLocation(NStoreLocation.CACHE).orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-run                 : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocation(NStoreLocation.RUN).orNull(), bootOptions.getStoreLocation(NStoreLocation.RUN).orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-lib                 : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocation(NStoreLocation.LIB).orNull(), bootOptions.getStoreLocation(NStoreLocation.LIB).orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-strategy            : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocationStrategy().orNull(), bootOptions.getStoreLocationStrategy().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-repos-store-strategy      : {0}", NTextUtils.formatLogValue(text, userOptions.getRepositoryStoreLocationStrategy().orNull(), bootOptions.getRepositoryStoreLocationStrategy().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-store-layout              : {0}", NTextUtils.formatLogValue(text, userOptions.getStoreLocationLayout().orNull(), bootOptions.getStoreLocationLayout().isNotPresent() ? "system" : bootOptions.getStoreLocationLayout().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-username                  : {0}", NTextUtils.formatLogValue(text, userOptions.getUserName().orNull(), bootOptions.getUserName().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-read-only                 : {0}", NTextUtils.formatLogValue(text, userOptions.getReadOnly().orNull(), bootOptions.getReadOnly().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-trace                     : {0}", NTextUtils.formatLogValue(text, userOptions.getTrace().orNull(), bootOptions.getTrace().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-progress                  : {0}", NTextUtils.formatLogValue(text, userOptions.getProgressOptions().orNull(), bootOptions.getProgressOptions().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-bot                       : {0}", NTextUtils.formatLogValue(text, userOptions.getBot().orNull(), bootOptions.getBot().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-cached                    : {0}", NTextUtils.formatLogValue(text, userOptions.getCached().orNull(), bootOptions.getCached().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-transitive                : {0}", NTextUtils.formatLogValue(text, userOptions.getTransitive().orNull(), bootOptions.getTransitive().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-confirm                   : {0}", NTextUtils.formatLogValue(text, userOptions.getConfirm().orNull(), bootOptions.getConfirm().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-debug                     : {0}", NTextUtils.formatLogValue(text, userOptions.getDebug().orNull(), bootOptions.getDebug().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-dry                       : {0}", NTextUtils.formatLogValue(text, userOptions.getDry().orNull(), bootOptions.getDry().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-execution-type            : {0}", NTextUtils.formatLogValue(text, userOptions.getExecutionType().orNull(), bootOptions.getExecutionType().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-out-line-prefix           : {0}", NTextUtils.formatLogValue(text, userOptions.getOutLinePrefix().orNull(), bootOptions.getOutLinePrefix().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-err-line-prefix           : {0}", NTextUtils.formatLogValue(text, userOptions.getErrLinePrefix().orNull(), bootOptions.getErrLinePrefix().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-init-platforms            : {0}", NTextUtils.formatLogValue(text, userOptions.getInitPlatforms().orNull(), bootOptions.getInitPlatforms().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-init-java                 : {0}", NTextUtils.formatLogValue(text, userOptions.getInitJava().orNull(), bootOptions.getInitJava().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-init-launchers            : {0}", NTextUtils.formatLogValue(text, userOptions.getInitLaunchers().orNull(), bootOptions.getInitLaunchers().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-init-scripts              : {0}", NTextUtils.formatLogValue(text, userOptions.getInitScripts().orNull(), bootOptions.getInitScripts().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-init-scripts              : {0}", NTextUtils.formatLogValue(text, userOptions.getInitScripts().orNull(), bootOptions.getInitScripts().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-desktop-launcher          : {0}", NTextUtils.formatLogValue(text, userOptions.getDesktopLauncher().orNull(), bootOptions.getDesktopLauncher().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-menu-launcher             : {0}", NTextUtils.formatLogValue(text, userOptions.getMenuLauncher().orNull(), bootOptions.getMenuLauncher().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-user-launcher             : {0}", NTextUtils.formatLogValue(text, userOptions.getUserLauncher().orNull(), bootOptions.getUserLauncher().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-isolation-level           : {0}", NTextUtils.formatLogValue(text, userOptions.getIsolationLevel().orNull(), bootOptions.getIsolationLevel().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-open-mode                 : {0}", NTextUtils.formatLogValue(text, userOptions.getOpenMode().orNull(), bootOptions.getOpenMode().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-inherited                 : {0}", NTextUtils.formatLogValue(text, userOptions.getInherited().orNull(), bootOptions.getInherited().orNull())));
                LOGCRF.log(NMsg.ofJstyle("   nuts-inherited-nuts-boot-args  : {0}", System.getProperty("nuts.boot.args") == null ? NTextUtils.desc(null, text)
                        : NTextUtils.desc(NCommandLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH, defaultSession()), text)
                ));
                LOGCRF.log(NMsg.ofJstyle("   nuts-inherited-nuts-args     : {0}", System.getProperty("nuts.args") == null ? NTextUtils.desc(null, text)
                        : NTextUtils.desc(text.ofText(NCommandLine.of(System.getProperty("nuts.args"), NShellFamily.SH, defaultSession())), text)
                ));
                LOGCRF.log(NMsg.ofJstyle("   nuts-open-mode               : {0}", NTextUtils.formatLogValue(text, bootOptions.getOpenMode().orNull(), bootOptions.getOpenMode().orElse(NOpenMode.OPEN_OR_CREATE))));
                NWorkspaceEnvManager senv = defaultSession().env();
                LOGCRF.log(NMsg.ofJstyle("   java-home                      : {0}", System.getProperty("java.home")));
                LOGCRF.log(NMsg.ofJstyle("   java-classpath                 : {0}", System.getProperty("java.class.path")));
                LOGCRF.log(NMsg.ofJstyle("   java-library-path              : {0}", System.getProperty("java.library.path")));
                LOGCRF.log(NMsg.ofJstyle("   os-name                        : {0}", System.getProperty("os.name")));
                LOGCRF.log(NMsg.ofJstyle("   os-family                      : {0}", senv.getOsFamily()));
                LOGCRF.log(NMsg.ofJstyle("   os-dist                        : {0}", senv.getOsDist().getArtifactId()));
                LOGCRF.log(NMsg.ofJstyle("   os-arch                        : {0}", System.getProperty("os.arch")));
                LOGCRF.log(NMsg.ofJstyle("   os-shell                       : {0}", senv.getShellFamily()));
                LOGCRF.log(NMsg.ofJstyle("   os-shells                      : {0}", text.ofBuilder().appendJoined(",", senv.getShellFamilies())));
                NWorkspaceTerminalOptions b = getModel().bootModel.getBootTerminal();
                LOGCRF.log(NMsg.ofJstyle("   os-terminal-flags              : {0}", String.join(", ", b.getFlags())));
                NTerminalMode terminalMode = wsModel.bootModel.getBootUserOptions().getTerminalMode().orElse(NTerminalMode.DEFAULT);
                LOGCRF.log(NMsg.ofJstyle("   os-terminal-mode               : {0}", terminalMode));
                LOGCRF.log(NMsg.ofJstyle("   os-desktop                     : {0}", senv.getDesktopEnvironment()));
                LOGCRF.log(NMsg.ofJstyle("   os-desktop-family              : {0}", senv.getDesktopEnvironmentFamily()));
                LOGCRF.log(NMsg.ofJstyle("   os-desktops                    : {0}", text.ofBuilder().appendJoined(",", (senv.getDesktopEnvironments()))));
                LOGCRF.log(NMsg.ofJstyle("   os-desktop-families            : {0}", text.ofBuilder().appendJoined(",", (senv.getDesktopEnvironmentFamilies()))));
                LOGCRF.log(NMsg.ofJstyle("   os-desktop-path                : {0}", senv.getDesktopPath()));
                LOGCRF.log(NMsg.ofJstyle("   os-desktop-integration         : {0}", senv.getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP)));
                LOGCRF.log(NMsg.ofJstyle("   os-menu-integration            : {0}", senv.getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU)));
                LOGCRF.log(NMsg.ofJstyle("   os-shortcut-integration        : {0}", senv.getDesktopIntegrationSupport(NDesktopIntegrationItem.USER)));
                LOGCRF.log(NMsg.ofJstyle("   os-version                     : {0}", senv.getOsDist().getVersion()));
                LOGCRF.log(NMsg.ofJstyle("   os-username                    : {0}", System.getProperty("user.name")));
                LOGCRF.log(NMsg.ofJstyle("   os-user-dir                    : {0}", NPath.of(System.getProperty("user.dir"), defaultSession())));
                LOGCRF.log(NMsg.ofJstyle("   os-user-home                   : {0}", NPath.of(System.getProperty("user.home"), defaultSession())));
                LOGCRF.log(NMsg.ofJstyle("   os-user-locale                 : {0}", Locale.getDefault()));
                LOGCRF.log(NMsg.ofJstyle("   os-user-time-zone              : {0}", TimeZone.getDefault()));
            }
            wsModel.securityModel = new DefaultNWorkspaceSecurityModel(this);

            Instant now = Instant.now();
            if (bootOptions.getCreationTime().get().compareTo(now) > 0) {
                wsModel.configModel.setStartCreateTime(now);
            } else {
                wsModel.configModel.setStartCreateTime(bootOptions.getCreationTime().get());
            }

            boolean exists = NWorkspaceConfigManagerExt.of(_config).getModel().isValidWorkspaceFolder(defaultSession());
            NOpenMode openMode = bootOptions.getOpenMode().orNull();
            if (openMode != null) {
                switch (openMode) {
                    case OPEN_OR_ERROR: {
                        if (!exists) {
                            throw new NWorkspaceNotFoundException(workspaceLocation);
                        }
                        break;
                    }
                    case CREATE_OR_ERROR: {
                        if (exists) {
                            throw new NWorkspaceAlreadyExistsException(workspaceLocation);
                        }
                        break;
                    }
                }
            }


            wsModel.configModel.onExtensionsPrepared(defaultSession());
            boolean justInstalled = false;
            NWorkspaceArchetypeComponent justInstalledArchetype = null;
            if (!loadWorkspace(defaultSession(), bootOptions.getExcludedExtensions().orElseGet(Collections::emptyList), null)) {
                wsModel.bootModel.setFirstBoot(true);
                if (wsModel.uuid == null) {
                    wsModel.uuid = UUID.randomUUID().toString();
                }
                //workspace wasn't loaded. Create new configuration...
                justInstalled = true;
                NWorkspaceUtils.of(defaultSession()).checkReadOnly();
                LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NLoggerVerb.SUCCESS)
                        .log(NMsg.ofJstyle("creating {0} workspace at {1}",
                                text.ofStyled("new", NTextStyle.info()),
                                defaultSession().locations().getWorkspaceLocation()
                        ));
                NWorkspaceConfigBoot bconfig = new NWorkspaceConfigBoot();
                //load from config with resolution applied
                bconfig.setUuid(wsModel.uuid);
                NWorkspaceConfigApi aconfig = new NWorkspaceConfigApi();
                aconfig.setApiVersion(apiVersion);
                aconfig.setRuntimeId(runtimeId);
                aconfig.setJavaCommand(bootOptions.getJavaCommand().orNull());
                aconfig.setJavaOptions(bootOptions.getJavaOptions().orNull());

                NWorkspaceConfigRuntime rconfig = new NWorkspaceConfigRuntime();
                rconfig.setDependencies(
                        bootOptions.getRuntimeBootDescriptor().get().getDependencies().stream()
                                .map(NDependency::toString)
                                .collect(Collectors.joining(";"))
                );
                rconfig.setId(runtimeId);

                bconfig.setBootRepositories(repositories);
                bconfig.setStoreLocationStrategy(bootOptions.getStoreLocationStrategy().orNull());
                bconfig.setRepositoryStoreLocationStrategy(bootOptions.getRepositoryStoreLocationStrategy().orNull());
                bconfig.setStoreLocationLayout(bootOptions.getStoreLocationLayout().orNull());
                bconfig.setGlobal(bootOptions.getGlobal().orElse(false));
                bconfig.setStoreLocations(new NStoreLocationsMap(bootOptions.getStoreLocations().orNull()).toMapOrNull());
                bconfig.setHomeLocations(new NHomeLocationsMap(bootOptions.getHomeLocations().orNull()).toMapOrNull());

                boolean namedWorkspace = CoreNUtils.isValidWorkspaceName(bootOptions.getWorkspace().orNull());
                if (bconfig.getStoreLocationStrategy() == null) {
                    bconfig.setStoreLocationStrategy(namedWorkspace ? NStoreLocationStrategy.EXPLODED : NStoreLocationStrategy.STANDALONE);
                }
                if (bconfig.getRepositoryStoreLocationStrategy() == null) {
                    bconfig.setRepositoryStoreLocationStrategy(NStoreLocationStrategy.EXPLODED);
                }
                bconfig.setName(CoreNUtils.resolveValidWorkspaceName(bootOptions.getWorkspace().orNull()));

                wsModel.configModel.setCurrentConfig(new DefaultNWorkspaceCurrentConfig(this)
                        .merge(aconfig, defaultSession())
                        .merge(bconfig, defaultSession())
                        .build(defaultSession().locations().getWorkspaceLocation(), defaultSession()));
                wsModel.configModel.setConfigBoot(bconfig, defaultSession());
                wsModel.configModel.setConfigApi(aconfig, defaultSession());
                wsModel.configModel.setConfigRuntime(rconfig, defaultSession());
                //load all "---config.*" custom options into persistent config
                for (String customOption : bootOptions.getCustomOptions().orElseGet(Collections::emptyList)) {
                    NArgument a = NArgument.of(customOption);
                    if (a.getKey().asString().get().startsWith("config.")) {
                        if (a.isActive()) {
                            defaultSession().config().setConfigProperty(
                                    a.getKey().asString().orElse("").substring("config.".length()),
                                    a.getStringValue().orNull()
                            );
                        }
                    }
                }
                justInstalledArchetype = initializeWorkspace(bootOptions.getArchetype().orNull(), defaultSession());
                if (!_config.isReadOnly()) {
                    _config.save();
                }
                NVersion nutsVersion = getRuntimeId().getVersion();
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NLoggerVerb.SUCCESS)
                            .log(NMsg.ofJstyle("nuts workspace v{0} created.", nutsVersion));
                }
                //should install default
                if (defaultSession().isPlainTrace() && !_boot.getBootOptions().getSkipWelcome().orElse(false)) {
                    NStream out = defaultSession().out();
                    out.resetLine();
                    StringBuilder version = new StringBuilder(nutsVersion.toString());
                    CoreStringUtils.fillString(' ', 25 - version.length(), version);
                    NTexts txt = text.setSession(defaultSession());
                    NPath p = NPath.of("classpath:/net/thevpc/nuts/runtime/includes/standard-header.ntf", getClass().getClassLoader(), defaultSession());
                    NText n = txt.parser().parse(p);
                    n = txt.transform(n, new NTextTransformConfig()
                            .setCurrentDir(p.getParent())
                            .setImportClassLoader(getClass().getClassLoader())
                            .setProcessAll(true)
                    );
                    out.println(n == null ? "no help found" : n);
                    if (NWorkspaceUtils.isUserDefaultWorkspace(defaultSession())) {
                        out.println(
                                txt.ofBuilder()
                                        .append("location", NTextStyle.underlined())
                                        .append(":")
                                        .append(defaultSession().locations().getWorkspaceLocation())
                                        .append(" ")
                        );
                    } else {
                        out.println(
                                txt.ofBuilder()
                                        .append("location", NTextStyle.underlined())
                                        .append(":")
                                        .append(defaultSession().locations().getWorkspaceLocation())
                                        .append(" ")
                                        .append(" (")
                                        .append(getHashName())
                                        .append(")")
                        );
                    }
                    NTableFormat.of(defaultSession()).setValue(
                            NTableModel.of(defaultSession())
                                    .addCell(
                                            txt.ofBuilder()
                                                    .append(" This is the first time ")
                                                    .appendCode("sh", "nuts")
                                                    .append(" is launched for this workspace ")
                                    )
                    ).println(out);
                    out.println();
                }
                wsModel.configModel.installBootIds(defaultSession().copy().setConfirm(NConfirmationMode.YES));
            } else {
                wsModel.bootModel.setFirstBoot(false);
                wsModel.uuid = wsModel.configModel.getStoreModelBoot().getUuid();
                if (NBlankable.isBlank(wsModel.uuid)) {
                    wsModel.uuid = UUID.randomUUID().toString();
                    wsModel.configModel.getStoreModelBoot().setUuid(wsModel.uuid);
                }
                if (bootOptions.getRecover().orElse(false)) {
                    wsModel.configModel.setBootApiVersion(cfg.getApiVersion(), defaultSession());
                    wsModel.configModel.setBootRuntimeId(cfg.getRuntimeId(),
                            bootOptions.getRuntimeBootDescriptor().get().getDependencies().stream()
                                    .map(NDependency::toString)
                                    .collect(Collectors.joining(";")),
                            defaultSession());
                    wsModel.configModel.setBootRepositories(cfg.getBootRepositories(), defaultSession());
                    try {
                        defaultSession().install().setInstalled(true).setSession(defaultSession()).getResult();
                    } catch (Exception ex) {
                        LOG.with().session(defaultSession()).level(Level.SEVERE).verb(NLoggerVerb.FAIL)
                                .error(ex)
                                .log(NMsg.ofJstyle("reinstall artifacts failed : {0}", ex));
                    }
                }
                if (defaultSession().repos().getRepositories().size() == 0) {
                    LOG.with().session(defaultSession()).level(Level.CONFIG).verb(NLoggerVerb.FAIL)
                            .log(NMsg.ofPlain("workspace has no repositories. Will re-create defaults"));
                    justInstalledArchetype = initializeWorkspace(bootOptions.getArchetype().orNull(), defaultSession());
                }
                List<String> transientRepositoriesSet =
                        CoreCollectionUtils.nonNullList(bootOptions.getRepositories().orElseGet(Collections::emptyList));
                NRepositoryDB repoDB = NRepositoryDB.of(defaultSession());
                NRepositorySelectorList expected = NRepositorySelectorList.ofAll(
                        transientRepositoriesSet, repoDB, defaultSession());
                for (NRepositoryLocation loc : expected.resolve(null, repoDB)) {
                    NAddRepositoryOptions d = NRepositorySelectorHelper.createRepositoryOptions(loc, false, defaultSession());
                    String n = d.getName();
                    String ruuid = (NBlankable.isBlank(n) ? "temporary" : n) + "_" + UUID.randomUUID().toString().replace("-", "");
                    d.setName(ruuid);
                    d.setTemporary(true);
                    d.setEnabled(true);
                    d.setFailSafe(false);
                    if (d.getConfig() != null) {
                        d.getConfig().setName(NBlankable.isBlank(n) ? ruuid : n);
                        d.getConfig().setStoreLocationStrategy(NStoreLocationStrategy.STANDALONE);
                    }
                    defaultSession().repos().addRepository(d);
                }
            }

            if (!_config.isReadOnly()) {
                _config.save(false);
            }
            wsModel.configModel.setEndCreateTime(Instant.now());
            if (justInstalled) {
                try {
                    Map rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(getApiId().toString(), ""), NRecommendationPhase.BOOTSTRAP, false, defaultSession());
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
                DefaultNWorkspaceEvent workspaceCreatedEvent = new DefaultNWorkspaceEvent(defaultSession(), null, null, null, null);
                for (NWorkspaceListener workspaceListener : defaultSession().events().getWorkspaceListeners()) {
                    workspaceListener.onCreateWorkspace(workspaceCreatedEvent);
                }
            }
            if (bootOptions.getUserName().orElse("").trim().length() > 0) {
                char[] password = bootOptions.getCredentials().orNull();
                if (password == null || NBlankable.isBlank(new String(password))) {
                    password = defaultSession().config().getDefaultTerminal().readPassword("Password : ");
                }
                defaultSession().security().setSession(defaultSession()).login(bootOptions.getUserName().get(), password);
            }
            wsModel.configModel.setEndCreateTime(Instant.now());
            LOG.with().session(defaultSession()).level(Level.FINE).verb(NLoggerVerb.SUCCESS)
                    .log(
                            NMsg.ofCstyle("%s workspace loaded in %s",
                                    NMsg.ofCode("nuts"),
                                    CoreTimeUtils.formatPeriodMilli(_boot.getCreationDuration())
                            )
                    );
            if (CoreNUtils.isCustomFalse("---perf", defaultSession())) {
                if (defaultSession().isPlainOut()) {
                    defaultSession().out().printlnf("%s workspace loaded in %s",
                            NMsg.ofCode("nuts"),
                            text.ofStyled(CoreTimeUtils.formatPeriodMilli(_boot.getCreationDuration()),
                                    NTextStyle.error()
                            )
                    );
                } else {
                    defaultSession().eout().add(elems.ofObject()
                            .set("workspace-loaded-in",
                                    elems.ofObject()
                                            .set("ms", _boot.getCreationDuration().toMillis())
                                            .set("text", CoreTimeUtils.formatPeriodMilli(_boot.getCreationDuration()))
                                            .build()

                            )
                            .build());
                }
            }
        } catch (RuntimeException ex) {
            if (wsModel != null && wsModel.recomm != null) {
                try {
                    NSession s = defaultSession();
                    NId runtimeId = getRuntimeId();
                    String sRuntimeId = runtimeId == null ? NId.ofRuntime("").get().toString() : runtimeId.toString();
                    displayRecommendations(wsModel.recomm.getRecommendations(new RequestQueryInfo(sRuntimeId, ex), NRecommendationPhase.BOOTSTRAP, true, s), s);
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

    private void displayRecommendations(Object r, NSession s) {
        if (s != null) {
            Map<String, Object> a = new HashMap<>();
            a.put("recommendations", r);
            s.out().printlnf(a);
        }
    }

    private URL getApiURL() {
        NId nid = NId.ofApi(Nuts.getVersion()).get();
        return NApiUtils.findClassLoaderJar(nid, NClassLoaderUtils.resolveClasspathURLs(Thread.currentThread().getContextClassLoader()));
    }

    private String getApiDigest() {
        if (NBlankable.isBlank(wsModel.apiDigest)) {
            wsModel.apiDigest = new CoreDigestHelper(defaultSession()).append(getApiURL()).getDigest();
        }
        return wsModel.apiDigest;
    }

    protected NDescriptor _applyParentDescriptors(NDescriptor descriptor, NSession session) {
        checkSession(session);
        List<NId> parents = descriptor.getParents();
        List<NDescriptor> parentDescriptors = new ArrayList<>();
        for (NId parent : parents) {
            parentDescriptors.add(
                    _applyParentDescriptors(
                            session.fetch().setId(parent).setSession(session).getResultDescriptor(),
                            session
                    )
            );
        }
        if (parentDescriptors.size() > 0) {
            NDescriptorBuilder descrWithParents = descriptor.builder();
            NDescriptorUtils.applyParents(descrWithParents, parentDescriptors, session);
            return descrWithParents.build();
        }
        return descriptor;
    }

    protected NDescriptor _resolveEffectiveDescriptor(NDescriptor descriptor, NSession session) {
        LOG.with().session(session).level(Level.FINEST).verb(NLoggerVerb.START)
                .log(NMsg.ofJstyle("resolve effective {0}", descriptor.getId()));
        checkSession(session);
        NDescriptorBuilder descrWithParents = _applyParentDescriptors(descriptor, session).builder();
        //now apply conditions!
        List<NDescriptorProperty> properties = descrWithParents.getProperties().stream().filter(x -> CoreFilterUtils.acceptCondition(
                x.getCondition(), false, session)).collect(Collectors.toList());
        if (properties.size() > 0) {
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

        NDescriptor effectiveDescriptor = NDescriptorUtils.applyProperties(descrWithParents, session).build();
        List<NDependency> oldDependencies = new ArrayList<>();
        for (NDependency d : effectiveDescriptor.getDependencies()) {
            if (CoreFilterUtils.acceptDependency(d, session)) {
                oldDependencies.add(d.builder().setCondition(null).build());
            }
        }

        List<NDependency> newDeps = new ArrayList<>();
        boolean someChange = false;
        LinkedHashSet<NDependency> effStandardDeps = new LinkedHashSet<>();
        for (NDependency standardDependency : effectiveDescriptor.getStandardDependencies()) {
            if ("import".equals(standardDependency.getScope())) {
                NDescriptor dd = session.fetch().setId(standardDependency.toId()).setEffective(true).setSession(session).getResultDescriptor();
                for (NDependency dependency : dd.getStandardDependencies()) {
                    if (CoreFilterUtils.acceptDependency(dependency, session)) {
                        effStandardDeps.add(dependency);
                    }
                }
            } else {
                if (CoreFilterUtils.acceptDependency(standardDependency, session)) {
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
                    LOG.with().session(session).level(Level.FINE).verb(NLoggerVerb.FAIL)
                            .log(NMsg.ofJstyle("failed to resolve effective version for {0}", d));
                }
            }

            if ("import".equals(d.getScope())) {
                someChange = true;
                newDeps.addAll(session.fetch().setId(d.toId()).setEffective(true).setSession(session).getResultDescriptor().getDependencies());
            } else {
                newDeps.add(d);
            }
        }
        effectiveDescriptor = effectiveDescriptor.builder().setDependencies(newDeps).build();
        return effectiveDescriptor;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + wsModel.configModel
                + '}';
    }

    protected NWorkspaceArchetypeComponent initializeWorkspace(String archetype, NSession session) {
        checkSession(session);
        if (NBlankable.isBlank(archetype)) {
            archetype = "default";
        }
        NWorkspaceArchetypeComponent instance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NWorkspaceArchetypeComponent ac : session.extensions().setSession(session).createAllSupported(NWorkspaceArchetypeComponent.class, archetype)) {
            if (archetype.equals(ac.getName())) {
                instance = ac;
                break;
            }
            validValues.add(ac.getName());
        }
        if (instance == null) {
            //get the default implementation
            throw new NException(session,
                    NMsg.ofCstyle("invalid archetype %s. Valid values are : %s", archetype, validValues)
            );
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        session.security().setSession(session).updateUser(NConstants.Users.ADMIN).setCredentials("admin".toCharArray()).run();

        instance.initializeWorkspace(session);

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
        if (NId.ofApi("").get().equalsShortId(id)) {
            return id;
        }
        for (NDependency dependency : session.fetch().setId(id).getResultDescriptor().getDependencies()) {
            NId q = resolveApiId(dependency.toId(), visited, session);
            if (q != null) {
                return q;
            }
        }
        return null;
    }

    public void installOrUpdateImpl(NDefinition def, String[] args, boolean resolveInstaller, boolean updateDefaultVersion, InstallStrategy0 strategy0, boolean requireDependencies, NId[] forIds, NDependencyScope scope, NSession session) {
        checkSession(session);
        if (def == null) {
            return;
        }
        boolean requireParents = true;
        NInstallerComponent installerComponent = null;
        try {
            Map rec = null;
            if (strategy0 == InstallStrategy0.INSTALL) {
                rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString()), NRecommendationPhase.INSTALL, false, session);
            } else if (strategy0 == InstallStrategy0.UPDATE) {
                rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString()), NRecommendationPhase.UPDATE, false, session);
            } else {
                //just ignore any dependencies. recommendations are related to main artifacts
            }
            //TODO: should check here for any security issue!
        } catch (Exception ex2) {
            //just ignore
        }
        NStream out = session.out();
        NInstallInformation newNInstallInformation = null;
        boolean remoteRepo = true;
        try {
            NDependencyFilter ndf = NDependencyFilters.of(session).byRunnable();
            if (def.getEffectiveDescriptor().isNotPresent()
                    || (!NDescriptorUtils.isNoContent(def.getDescriptor()) && def.getContent().isNotPresent())) {
                // reload def
                NFetchCommand fetch2 = session.fetch()
                        .setSession(session)
                        .setId(def.getId())
                        .setContent(true)
                        .setRepositoryFilter(session.repos().filter().installedRepo())
                        .setFailFast(true);
                if (def.getDependencies().isPresent()) {
                    fetch2.setDependencyFilter(def.getDependencies().get(session).filter());
                    fetch2.setDependencies(true);
                }
                def = fetch2.getResultDefinition();
            }

            boolean reinstall = false;
            NInstalledRepository installedRepository = getInstalledRepository();
            NWorkspaceUtils wu = NWorkspaceUtils.of(session);

            if (session.isPlainTrace()) {
                NTexts text = NTexts.of(session);
                if (strategy0 == InstallStrategy0.UPDATE) {
                    session.out().resetLine().printf("%s %s ...%n",
                            text.ofStyled("update", NTextStyle.warn()),
                            def.getId().getLongId()
                    );
                } else if (strategy0 == InstallStrategy0.REQUIRE) {
                    reinstall = def.getInstallInformation().get(session).getInstallStatus().isRequired();
                    if (reinstall) {
                        //session.out().println("re-requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                    } else {
                        //session.out().println("requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                    }
                } else {
                    reinstall = def.getInstallInformation().get(session).getInstallStatus().isInstalled();
                    if (reinstall) {
                        session.out().resetLine().printf(
                                "%s %s ...%n",
                                text.ofStyled("re-install", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined())),
                                def.getId().getLongId()
                        );
                    } else {
                        session.out().resetLine().printf("%s %s ...%n",
                                text.ofStyled("install", NTextStyle.success()),
                                def.getId().getLongId()
                        );
                    }
                }
            }
            NRepositorySPI installedRepositorySPI = wu.repoSPI(installedRepository);
            if (resolveInstaller) {
                installerComponent = getInstaller(def, session);
            }
            if (reinstall) {
                uninstallImpl(def, new String[0], resolveInstaller, true, false, false, session);
                //must re-fetch def!
                NDefinition d2 = session.fetch().setId(def.getId())
                        .setContent(true)
                        .setEffective(true)
                        .setDependencies(true)
                        .setFailFast(false)
                        .setOptional(false)
                        .addScope(NDependencyScopePattern.RUN)
                        .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                        .getResultDefinition();
                if (d2 == null) {
                    // perhaps the version does no more exist
                    // search latest!
                    d2 = session.search().setId(def.getId().getShortId())
                            .setEffective(true)
                            .setFailFast(true)
                            .setLatest(true)
                            .setOptional(false)
                            .addScope(NDependencyScopePattern.RUN)
                            .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                            .getResultDefinitions().required();
                }
                def = d2;
            }
//        checkSession(session);
            NDefinition oldDef = null;
            if (strategy0 == InstallStrategy0.UPDATE) {
                switch (def.getDescriptor().getIdType()) {
                    case API: {
                        oldDef = session.fetch().setSession(
                                session.copy().setFetchStrategy(NFetchStrategy.ONLINE)
                        ).setId(NId.ofApi(Nuts.getVersion()).get()).setFailFast(false).getResultDefinition();
                        break;
                    }
                    case RUNTIME: {
                        oldDef = session.fetch().setSession(session.copy().setFetchStrategy(NFetchStrategy.ONLINE)).setId(getRuntimeId())
                                .setFailFast(false).getResultDefinition();
                        break;
                    }
                    default: {
                        oldDef = session.search().setSession(session).addId(def.getId().getShortId())
                                .setInstallStatus(NInstallStatusFilters.of(session).byDeployed(true))
                                .setFailFast(false).getResultDefinitions().first();
                        break;
                    }
                }
            }
            out.flush();
            NWorkspaceConfigManager config = session.config().setSession(session);
            if (def.getContent().isPresent() || NDescriptorUtils.isNoContent(def.getDescriptor())) {
                if (requireParents) {
                    List<NDefinition> requiredDefinitions = new ArrayList<>();
                    for (NId parent : def.getDescriptor().getParents()) {
                        if (!installedRepositorySPI.
                                searchVersions().setId(parent)
                                .setFetchMode(NFetchMode.LOCAL)
                                .setSession(session)
                                .getResult()
                                .hasNext()) {
                            NDefinition dd = session.search().addId(parent).setLatest(true)
                                    .setEffective(true)
                                    .getResultDefinitions().first();
                            if (dd != null) {
                                requiredDefinitions.add(dd);
                            }
                        }
                    }
                    //install required
                    for (NDefinition dd : requiredDefinitions) {
                        requireImpl(dd,
                                false, new NId[]{def.getId()}, session
                                //transitive dependencies already evaluated
                        );
                    }
                }
                if (requireDependencies) {
                    def.getDependencies().get(session);
                    List<NDefinition> requiredDefinitions = new ArrayList<>();
                    //fetch required
                    for (NDependency dependency : def.getDependencies().get(session)) {
                        if (ndf == null || ndf.acceptDependency(def.getId(), dependency, session)) {
                            if (!installedRepositorySPI.
                                    searchVersions().setId(dependency.toId())
                                    .setFetchMode(NFetchMode.LOCAL)
                                    .setSession(session)
                                    .getResult()
                                    .hasNext()) {
                                NDefinition dd = session.search().addId(dependency.toId()).setContent(true).setLatest(true)
                                        //.setDependencies(true)
                                        .setEffective(true)
                                        .getResultDefinitions().first();
                                if (dd != null) {
                                    if (dd.getContent().isNotPresent()) {
                                        throw new NInstallException(session, def.getId(),
                                                NMsg.ofCstyle("unable to install %s. required dependency content is missing for %s", def.getId(), dependency.toId()),
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
                                false, new NId[]{def.getId()}, session
                                //transitive dependencies already evaluated
                        );
                    }
                }

                //should change def to reflect install location!
                NExecutionContextBuilder cc = createExecutionContext()
                        .setSession(session.copy())
                        .setExecSession(session.copy())
                        .setDefinition(def).setArguments(args).setFailFast(true).setTemporary(false)
                        .setExecutionType(session.boot().getBootOptions().getExecutionType().orNull())
                        .setRunAs(NRunAs.currentUser())// install or update always uses current user
                        ;
                NArtifactCall installer = def.getDescriptor().getInstaller();
                if (installer != null) {
                    cc.addExecutorOptions(installer.getArguments());
                    cc.addExecutorProperties(installer.getProperties());
                }
                cc.setWorkspace(cc.getSession().getWorkspace());
                NExecutionContext executionContext = cc.build();

                if (strategy0 == InstallStrategy0.REQUIRE) {
                    newNInstallInformation = installedRepository.require(executionContext.getDefinition(), true, forIds, scope, session);
                } else if (strategy0 == InstallStrategy0.UPDATE) {
                    newNInstallInformation = installedRepository.install(executionContext.getDefinition(), session);
                } else if (strategy0 == InstallStrategy0.INSTALL) {
                    newNInstallInformation = installedRepository.install(executionContext.getDefinition(), session);
                }
                if (updateDefaultVersion) {
                    installedRepository.setDefaultVersion(def.getId(), session);
                }

                //now should reload definition
                NFetchCommand fetch2 = session.fetch()
                        .setSession(session)
                        .setId(executionContext.getDefinition().getId())
                        .setContent(true)
                        .setRepositoryFilter(session.repos().filter().installedRepo())
                        .setFailFast(true);
                if (def.getDependencies().isPresent()) {
                    fetch2.setDependencyFilter(def.getDependencies().get(session).filter());
                    fetch2.setDependencies(true);
                }
                NDefinition def2 = fetch2
                        .getResultDefinition();

                //update definition in the execution context
                cc.setDefinition(def2);
                executionContext = cc.build();
                NRepository rep = session.repos().findRepository(def.getRepositoryUuid());
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
                                out.resetLine().printf("%s ```error failed``` to update : %s.%n", def.getId(), ex);
                            }
                            throw new NExecutionException(session,
                                    NMsg.ofCstyle("unable to update %s", def.getId()),
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
                                out.resetLine().printf("```error error: failed to install``` %s: %s.%n", def.getId(), ex);
                            }
                            try {
                                installedRepository.uninstall(executionContext.getDefinition(), session);
                            } catch (Exception ex2) {
                                LOG.with().session(session).level(Level.FINE).error(ex)
                                        .log(NMsg.ofJstyle("failed to uninstall  {0}", executionContext.getDefinition().getId()));
                                //ignore if we could not uninstall
                                try {
                                    Map rec = null;
                                    if (strategy0 == InstallStrategy0.INSTALL) {
                                        rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ex2), NRecommendationPhase.UPDATE, true, session);
                                    } else if (strategy0 == InstallStrategy0.UPDATE) {
                                        rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ex2), NRecommendationPhase.UPDATE, true, session);
                                    } else {
                                        //just ignore any dependencies. recommendations are related to main artifacts
                                    }
                                    //TODO: should check here for any security issue!
                                } catch (Exception ex3) {
                                    //just ignore
                                }
                            }
                            throw new NExecutionException(session, NMsg.ofCstyle("unable to install %s", def.getId()), ex);
                        }
                    }
                }
            } else {
                throw new NExecutionException(session,
                        NMsg.ofCstyle("unable to install %s: unable to locate content", def.getId()),
                        101);
            }

            NId forId = (forIds == null || forIds.length == 0) ? null : forIds[0];
            switch (def.getDescriptor().getIdType()) {
                case API: {
                    wsModel.configModel.prepareBootClassPathConf(NIdType.API, def.getId(),
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
                wu.events().fireOnUpdate(new DefaultNUpdateEvent(oldDef, def, session, reinstall));
            } else if (strategy0 == InstallStrategy0.REQUIRE) {
                wu.events().fireOnRequire(new DefaultNInstallEvent(def, session, forIds, reinstall));
            } else if (strategy0 == InstallStrategy0.INSTALL) {
                wu.events().fireOnInstall(new DefaultNInstallEvent(def, session, new NId[0], reinstall));
            }

            if (def.getDescriptor().getIdType() == NIdType.EXTENSION) {
                NWorkspaceConfigManagerExt wcfg = NWorkspaceConfigManagerExt.of(config);
                NExtensionListHelper h = new NExtensionListHelper(
                        session.getWorkspace().getApiId(),
                        wcfg.getModel().getStoredConfigBoot().getExtensions())
                        .save();
                h.add(def.getId(), def.getDependencies().get(session).transitiveWithSource()
                        .toArray(NDependency[]::new));
                wcfg.getModel().getStoredConfigBoot().setExtensions(h.getConfs());
                wcfg.getModel().fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
            }
        } catch (RuntimeException ex) {
            try {
                Map rec = null;
                if (strategy0 == InstallStrategy0.INSTALL) {
                    rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ex), NRecommendationPhase.INSTALL, true, session);
                } else if (strategy0 == InstallStrategy0.UPDATE) {
                    rec = wsModel.recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ex), NRecommendationPhase.UPDATE, true, session);
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
            NTexts text = NTexts.of(session);
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
                            out.resetLine().printf("%s %s from %s repository (%s).%s%n",
                                    installedString,
                                    def.getId().getLongId(),
                                    remoteRepo ? "remote" : "local",
                                    def.getRepositoryName(),
                                    text.parse(setAsDefaultString)
                            );
                        }
                    } else if (!def.getContent().get(session).isUserCache()) {
                        if (def.getContent().get(session).isUserTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s) temporarily file %s.%s%n",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getContent().orNull(),
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
                        if (def.getContent().get(session).isUserTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().printf("%s %s from %s repository (%s) temporarily file %s.%s%n",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getContent().orNull(),
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
                        out.resetLine().printf("%s  %s %s.%s%n",
                                installedString,
                                def.getId().getLongId(),
                                text.ofStyled("successfully", NTextStyle.success()),
                                text.parse(setAsDefaultString)
                        );
                    }
                }
            }
        }
    }

    public String resolveCommandName(NId id, NSession session) {
        checkSession(session);
        String nn = id.getArtifactId();
        NCustomCommandManager aliases = session.commands().setSession(session);
        NWorkspaceCustomCommand c = aliases.findCommand(nn);
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
        throw new NElementNotFoundException(session,
                NMsg.ofCstyle("unable to resolve command name for %s", id
                ));
    }

    protected boolean loadWorkspace(NSession session, List<String> excludedExtensions, String[] excludedRepositories) {
        checkSession(session);
        if (wsModel.configModel.loadWorkspace(session)) {
            //extensions already wired... this is needless!
            for (NId extensionId : session.extensions().setSession(session).getConfigExtensions()) {
                if (wsModel.extensionModel.isExcludedExtension(extensionId)) {
                    continue;
                }
                NSession sessionCopy = session.copy();
                wsModel.extensionModel.wireExtension(extensionId,
                        session.fetch().setSession(sessionCopy
                                .copy()
                                .setFetchStrategy(NFetchStrategy.ONLINE)
                                .setTransitive(true)
                        )
                );
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }
            NUserConfig adminSecurity = NWorkspaceConfigManagerExt.of(session.config())
                    .getModel()
                    .getUser(NConstants.Users.ADMIN, session);
            if (adminSecurity == null || NBlankable.isBlank(adminSecurity.getCredentials())) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(session).level(Level.CONFIG).verb(NLoggerVerb.FAIL)
                            .log(NMsg.ofJstyle("{0} user has no credentials. reset to default", NConstants.Users.ADMIN));
                }
                session.security()
                        .updateUser(NConstants.Users.ADMIN).credentials("admin".toCharArray())
                        .run();
            }
            for (NCommandFactoryConfig commandFactory : session.commands().setSession(session).getCommandFactories()) {
                try {
                    session.commands().setSession(session).addCommandFactory(commandFactory);
                } catch (Exception e) {
                    LOG.with().session(session).level(Level.SEVERE).verb(NLoggerVerb.FAIL)
                            .log(NMsg.ofJstyle("unable to instantiate Command Factory {0}", commandFactory));
                }
            }
            DefaultNWorkspaceEvent worksppaeReloadedEvent = new DefaultNWorkspaceEvent(session, null, null, null, null);
            for (NWorkspaceListener listener : defaultSession().events().getWorkspaceListeners()) {
                listener.onReloadWorkspace(worksppaeReloadedEvent);
            }
            //if save is needed, will be applied
            //config().save(false, session);
            return true;
        }
        return false;
    }

    @Override
    public NText getWelcomeText(NSession session) {
        NTexts txt = NTexts.of(session);
        NPath p = NPath.of("classpath:/net/thevpc/nuts/runtime/nuts-welcome.ntf", getClass().getClassLoader(), session);
        NText n = txt.parser().parse(p);
        n = txt.transform(n, new NTextTransformConfig().setProcessAll(true)
                .setImportClassLoader(getClass().getClassLoader())
                .setCurrentDir(p.getParent()));
        return (n == null ? txt.ofStyled("no welcome found!", NTextStyle.error()) : n);
    }


    @Override
    public NText getHelpText(NSession session) {
        NTexts txt = NTexts.of(session);
        NPath path = NPath.of("classpath:/net/thevpc/nuts/runtime/nuts-help.ntf", getClass().getClassLoader(), session);
        NText n = txt.parser().parse(path);
        n = txt.transform(n, new NTextTransformConfig()
                .setProcessAll(true)
                .setRootLevel(1));
        return (n == null ? txt.ofStyled("no help found", NTextStyle.error()) : n);
    }

    @Override
    public NText resolveDefaultHelp(Class clazz, NSession session) {
        NId nutsId = NIdResolver.of(session).resolveId(clazz);
        if (nutsId != null) {
            NPath urlPath = NPath.of("classpath:/" + nutsId.getGroupId().replace('.', '/') + "/" + nutsId.getArtifactId() + ".ntf", clazz == null ? null : clazz.getClassLoader(), session);
            NTexts txt = NTexts.of(session);
            NText n = txt.parser().parse(urlPath);
            n = txt.transform(n, new NTextTransformConfig()
                    .setProcessAll(true)
                    .setImportClassLoader(clazz == null ? null : clazz.getClassLoader())
                    .setCurrentDir(urlPath.getParent())
                    .setRootLevel(1));
            if (n == null) {
                return txt.ofStyled(
                        NMsg.ofCstyle(
                                "no default help found at %s for %s", urlPath, (clazz == null ? null : clazz.getName())
                        )
                        , NTextStyle.error()
                );
            }
            return n;
        }
        return null;
    }

    @Override
    public NText getLicenseText(NSession session) {
        NTexts txt = NTexts.of(session);
        NPath p = NPath.of("classpath:/net/thevpc/nuts/runtime/nuts-license.ntf", getClass().getClassLoader(), session);
        NText n = txt.parser().parse(p);
        return (n == null ? NTexts.of(session).ofStyled("no license found", NTextStyle.error()) : n);
    }


    @Override
    public NId resolveEffectiveId(NDescriptor descriptor, NSession session) {
        checkSession(session);
        if (descriptor == null) {
            throw new NNotFoundException(session, null);
        }
        NId thisId = descriptor.getId();
        String a = thisId.getArtifactId();
        String g = thisId.getGroupId();
        String v = thisId.getVersion().getValue();
        if ((NBlankable.isBlank(g)) || (NBlankable.isBlank(v))) {
            List<NId> parents = descriptor.getParents();
            for (NId parent : parents) {
                NId p = session.fetch().setSession(session).setId(parent).setEffective(true).getResultId();
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
                throw new NNotFoundException(session, thisId,
                        NMsg.ofCstyle("unable to fetchEffective for %s. best Result is %s", thisId, thisId),
                        null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v) || CoreStringUtils.containsVars(a)) {
            Map<String, String> p = NDescriptorUtils.getPropertiesMap(descriptor.getProperties(), session);
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
                NDescriptor dd = session.fetch().setSession(session).setId(parent).setEffective(true).getResultDescriptor();
                bestId = NDescriptorUtils.applyProperties(bestId.builder(), new MapToFunction(NDescriptorUtils.getPropertiesMap(dd.getProperties(), session))).build();
                if (CoreNUtils.isEffectiveId(bestId)) {
                    return bestId;
                }
                all.addAll(dd.getParents());
            }
            throw new NNotFoundException(session, bestId,
                    NMsg.ofCstyle("unable to fetchEffective for %s. best Result is %s", bestId, bestId), null);
        }
        NId bestId = NIdBuilder.of(g, thisId.getArtifactId()).setVersion(v).build();
        if (!CoreNUtils.isEffectiveId(bestId)) {
            throw new NNotFoundException(session, bestId,
                    NMsg.ofCstyle("unable to fetchEffective for %s. best Result is %s", thisId, bestId), null);
        }
        return bestId;
    }

    @Override
    public NIdType resolveNutsIdType(NId id, NSession session) {
        NIdType idType = NIdType.REGULAR;
        String shortName = id.getShortName();
        if (shortName.equals(NConstants.Ids.NUTS_API)) {
            idType = NIdType.API;
        } else if (shortName.equals(NConstants.Ids.NUTS_RUNTIME)) {
            idType = NIdType.RUNTIME;
        } else {
            for (NId companionTool : session.extensions().getCompanionIds()) {
                if (companionTool.getShortName().equals(shortName)) {
                    idType = NIdType.COMPANION;
                }
            }
        }
        return idType;
    }

    @Override
    public NInstallerComponent getInstaller(NDefinition nutToInstall, NSession session) {
        checkSession(session);
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
                    NIdUtils.checkShortId(installerId, session);
                    runnerFile = session.search().setId(installerId)
                            .setOptional(false)
                            .setContent(true)
                            .setDependencies(true)
                            .setLatest(true)
                            .setDistinct(true)
                            .getResultDefinitions().first();

                }
            }
            NInstallerComponent best = session.extensions().setSession(session)
                    .createSupported(NInstallerComponent.class, false, runnerFile == null ? nutToInstall : runnerFile);
            if (best != null) {
                return best;
            }
            return new CommandForIdNInstallerComponent(runnerFile);
        }
        return new CommandForIdNInstallerComponent(null);
    }

    @Override
    public void requireImpl(NDefinition def, boolean withDependencies, NId[] forId, NSession session) {
        installOrUpdateImpl(def, new String[0], true, false, InstallStrategy0.REQUIRE, withDependencies, forId, null, session);
    }

    @Override
    public void installImpl(NDefinition def, String[] args, boolean updateDefaultVersion, NSession session) {
        installOrUpdateImpl(def, args, true, updateDefaultVersion, InstallStrategy0.INSTALL, true, null, null, session);
    }

    @Override
    public void updateImpl(NDefinition def, String[] args, boolean updateDefaultVersion, NSession session) {
        installOrUpdateImpl(def, args, true, updateDefaultVersion, InstallStrategy0.UPDATE, true, null, null, session);
    }

    public void uninstallImpl(NDefinition def, String[] args,
                              boolean runInstaller,
                              boolean deleteFiles,
                              boolean eraseFiles,
                              boolean traceBeforeEvent,
                              NSession session) {
        NStream out = CoreIOUtils.resolveOut(session);
        if (runInstaller) {
            NInstallerComponent installerComponent = getInstaller(def, session);
            if (installerComponent != null) {
                NExecutionContext executionContext = createExecutionContext()
                        .setDefinition(def)
                        .setArguments(args)
                        .setExecSession(session)
                        .setSession(session)
                        .setWorkspace(session.getWorkspace())
                        .setFailFast(true)
                        .setTemporary(false)
                        .setExecutionType(session.boot().getBootOptions().getExecutionType().orNull())
                        .setRunAs(NRunAs.currentUser())//uninstall always uses current user
                        .build();
                installerComponent.uninstall(executionContext, eraseFiles);
            }
        }

        getInstalledRepository().uninstall(def, session);
        NId id = def.getId();
        if (deleteFiles) {
            if (session.locations().getStoreLocation(id, NStoreLocation.APPS).exists()) {
                session.locations().getStoreLocation(id, NStoreLocation.APPS).deleteTree();
            }
            if (session.locations().getStoreLocation(id, NStoreLocation.LIB).exists()) {
                session.locations().getStoreLocation(id, NStoreLocation.LIB).deleteTree();
            }
            if (session.locations().getStoreLocation(id, NStoreLocation.LOG).exists()) {
                session.locations().getStoreLocation(id, NStoreLocation.LOG).deleteTree();
            }
            if (session.locations().getStoreLocation(id, NStoreLocation.CACHE).exists()) {
                session.locations().getStoreLocation(id, NStoreLocation.CACHE).deleteTree();
            }
            if (eraseFiles) {
                if (session.locations().getStoreLocation(id, NStoreLocation.VAR).exists()) {
                    session.locations().getStoreLocation(id, NStoreLocation.VAR).deleteTree();
                }
                if (session.locations().getStoreLocation(id, NStoreLocation.CONFIG).exists()) {
                    session.locations().getStoreLocation(id, NStoreLocation.CONFIG).deleteTree();
                }
            }
        }

        if (def.getDescriptor().getIdType() == NIdType.EXTENSION) {
            NWorkspaceConfigManagerExt wcfg = NWorkspaceConfigManagerExt.of(session.config());
            NExtensionListHelper h = new NExtensionListHelper(
                    session.getWorkspace().getApiId(),
                    wcfg.getModel().getStoredConfigBoot().getExtensions())
                    .save();
            h.remove(id);
            wcfg.getModel().getStoredConfigBoot().setExtensions(h.getConfs());
            wcfg.getModel().fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
        }
        if (traceBeforeEvent && session.isPlainTrace()) {
            out.printf("%s uninstalled %s%n", id, NTexts.of(session).ofStyled(
                    "successfully", NTextStyle.success()
            ));
        }
        NWorkspaceUtils.of(session).events().fireOnUninstall(new DefaultNInstallEvent(def, session, new NId[0], eraseFiles));
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
    public boolean requiresRuntimeExtension(NSession session) {
        boolean coreFound = false;
        for (NId ext : session.extensions().setSession(session).getConfigExtensions()) {
            if (ext.equalsShortId(getRuntimeId())) {
                coreFound = true;
                break;
            }
        }
        return !coreFound;
    }

    @Override
    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor, NSession session) {
        NPath eff = null;
        NWorkspaceLocationManager loc = session.locations().setSession(session);
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue()
                && descriptor.getId().toString().indexOf('$') < 0) {
            NPath l = loc.getStoreLocation(descriptor.getId(), NStoreLocation.CACHE);
            String nn = loc.getDefaultIdFilename(descriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
            if (eff.isRegularFile()) {
                try {
                    NDescriptor d = NDescriptorParser.of(session).parse(eff).orNull();
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.FINE).error(ex)
                            .log(NMsg.ofJstyle("failed to parse {0}", eff));
                    //
                }
            }
        } else {
            //
        }
        NDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor, session);
        NDescriptorUtils.checkValidEffectiveDescriptor(effectiveDescriptor, session);
        if (eff == null) {
            NPath l = session.locations().getStoreLocation(effectiveDescriptor.getId(), NStoreLocation.CACHE);
            String nn = loc.getDefaultIdFilename(effectiveDescriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
        }
        try {
            effectiveDescriptor.formatter(session).setNtf(false).print(eff);
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.FINE).error(ex)
                    .log(NMsg.ofJstyle("failed to print {0}", eff));
            //
        }
        return effectiveDescriptor;
    }

    @Override
    public NInstalledRepository getInstalledRepository() {
        return wsModel.installedRepository;
    }

    @Override
    public NInstallStatus getInstallStatus(NId id, boolean checkDependencies, NSession session) {
        NDefinition nutToInstall;
        try {
            nutToInstall = session.search().addId(id)
                    .setSession(session.copy().setTransitive(false))
                    .setInlineDependencies(checkDependencies)
                    .setInstallStatus(NInstallStatusFilters.of(session).byDeployed(true))
                    .setOptional(false)
                    .getResultDefinitions().first();
            if (nutToInstall == null) {
                return NInstallStatus.NONE;
            }
        } catch (NNotFoundException e) {
            return NInstallStatus.NONE;
        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex)
                    .log(NMsg.ofJstyle("error: %s", ex));
            return NInstallStatus.NONE;
        }
        return getInstalledRepository().getInstallStatus(nutToInstall.getId(), session);
    }

    @Override
    public NExecutionContextBuilder createExecutionContext() {
        return new DefaultNExecutionContextBuilder().setWorkspace(this);
    }

    @Override
    public void deployBoot(NSession session, NId id, boolean withDependencies) {
        Map<NId, NDefinition> defs = new HashMap<>();
        NDefinition m = session.fetch().setId(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
        Map<String, String> a = new LinkedHashMap<>();
        a.put("configVersion", Nuts.getVersion().toString());
        a.put("id", id.getLongName());
        a.put("dependencies", m.getDependencies().get(session).transitive().map(NDependency::getLongName, "getLongName")
                .collect(Collectors.joining(";")));
        defs.put(m.getId().getLongId(), m);
        if (withDependencies) {
            for (NDependency dependency : m.getDependencies().get(session)) {
                if (!defs.containsKey(dependency.toId().getLongId())) {
                    m = session.fetch().setId(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
                    defs.put(m.getId().getLongId(), m);
                }
            }
        }
        for (NDefinition def : defs.values()) {
            NPath bootstrapFolder = session.locations().getStoreLocation(NStoreLocation.LIB).resolve(NConstants.Folders.ID);
            NId id2 = def.getId();
            NCp.of(session).from(def.getContent().get(session))
                    .to(bootstrapFolder.resolve(session.locations().getDefaultIdBasedir(id2))
                            .resolve(session.locations().getDefaultIdFilename(id2.builder().setFaceContent().setPackaging("jar").build()))
                    ).run();
            session.fetch().setId(id2).getResultDescriptor().formatter(session).setNtf(false)
                    .print(bootstrapFolder.resolve(session.locations().getDefaultIdBasedir(id2))
                            .resolve(session.locations().getDefaultIdFilename(id2.builder().setFaceDescriptor().build())));

            Map<String, String> pr = new LinkedHashMap<>();
            pr.put("file.updated.date", Instant.now().toString());
            pr.put("project.id", def.getId().getShortId().toString());
            pr.put("project.name", def.getId().getShortId().toString());
            pr.put("project.version", def.getId().getVersion().toString());
            NRepositoryDB repoDB = NRepositoryDB.of(session);
            pr.put("repositories", "~/.m2/repository"
                    + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryLocation.of("vpc-public-maven", repoDB, session), true, session).getConfig().getLocation()
                    + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryLocation.of("maven-central", repoDB, session), true, session).getConfig().getLocation()
                    + ";" + NRepositorySelectorHelper.createRepositoryOptions(NRepositoryLocation.of("nuts-public", repoDB, session), true, session).getConfig().getLocation()
            );
            pr.put("project.dependencies.compile",
                    String.join(";",
                            def.getDependencies().get(session).transitive()
                                    .filter(x -> !x.isOptional()
                                                    && NDependencyFilters.of(session).byRunnable()
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
                throw new NIOException(session, ex);
            }
        }
    }

    public NSession defaultSession() {
        if (wsModel.initSession != null) {
            return wsModel.initSession;
        }
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
            wsModel.hashName = NHashName.of(defaultSession()).getHashName(this);
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
    public NId getRuntimeId() {
        return wsModel.runtimeId;
    }

    @Override
    public NPath getLocation() {
        return NPath.of(wsModel.location, wsModel.bootModel.bootSession());
    }

    @Override
    public NSession createSession() {
        NSession nSession = new DefaultNSession(this);
        nSession.setTerminal(NSessionTerminal.of(nSession));
        nSession.setExpireTime(nSession.boot().getBootOptions().getExpireTime().orNull());
        return nSession;
    }

    public DefaultNWorkspaceEnvManagerModel getEnvModel() {
        return wsModel.envModel;
    }

    public DefaultCustomCommandsModel getAliasesModel() {
        return wsModel.aliasesModel;
    }

    public DefaultNWorkspaceConfigModel getConfigModel() {
        return wsModel.configModel;
    }

    public DefaultImportModel getImportModel() {
        return wsModel.importModel;
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
            return NStringUtils.parseEnum(value, InstallStrategy0.class);
        }


        @Override
        public String id() {
            return id;
        }

    }

}
