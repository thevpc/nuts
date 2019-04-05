/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.util.CharacterizedFile;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsQuery;
import net.vpc.app.nuts.core.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.core.executors.JavaExecutorOptions;
import net.vpc.app.nuts.core.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsExclusionDependencyFilter;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.core.filters.id.NutsPatternIdFilter;
import net.vpc.app.nuts.core.repos.NutsFolderRepository;
import net.vpc.app.nuts.core.terminals.DefaultNutsSystemTerminalBase;
import net.vpc.app.nuts.core.terminals.DefaultSystemTerminal;
import net.vpc.app.nuts.core.terminals.UnmodifiableTerminal;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.*;
import net.vpc.common.mvn.PomId;
import net.vpc.common.mvn.PomIdResolver;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsWorkspace implements NutsWorkspace, NutsWorkspaceImpl {

    public static final Logger log = Logger.getLogger(DefaultNutsWorkspace.class.getName());
    public static final NutsInstallInfo NOT_INSTALLED = new NutsInstallInfo(false, null);
    private NutsDefinition nutsComponentId;
    private final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    private boolean initializing;
    protected final DefaultNutsWorkspaceSecurityManager securityManager = new DefaultNutsWorkspaceSecurityManager(this);
    protected final NutsWorkspaceConfigManagerExt configManager = new DefaultNutsWorkspaceConfigManager(this);
    protected DefaultNutsWorkspaceExtensionManager extensionManager;
    private final ObservableMap<String, Object> userProperties = new ObservableMap<>();

    private NutsSessionTerminal terminal;
    private NutsSystemTerminal systemTerminal;
    private NutsIOManager ioManager;
    private NutsParseManager parseManager;
    private NutsFormatManager formatManager;
    private DefaultNutsInstalledRepository installedRepository;
    private List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();

    public DefaultNutsWorkspace() {

    }

    @Override
    public String getUuid() {
        return config().getUuid();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new DefaultNutsSession();
        nutsSession.setTerminal(getTerminal());
        return nutsSession;
    }

    @Override
    public NutsDescriptorBuilder createDescriptorBuilder() {
        return new DefaultNutsDescriptorBuilder();
    }

    public DefaultNutsWorkspaceConfigManager config0() {
        return (DefaultNutsWorkspaceConfigManager) configManager;
    }

    @Override
    public NutsWorkspaceConfigManager config() {
        return configManager;
    }

    @Override
    public NutsWorkspaceSecurityManager security() {
        return securityManager;
    }

    public boolean isInitializing() {
        return initializing;
    }

    @Override
    public void removeWorkspaceListener(NutsWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    @Override
    public void addWorkspaceListener(NutsWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    @Override
    public NutsWorkspaceListener[] getWorkspaceListeners() {
        return workspaceListeners.toArray(new NutsWorkspaceListener[0]);
    }

    @Override
    public NutsWorkspace openWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        NutsWorkspaceFactory newFactory = extensions().createSupported(NutsWorkspaceFactory.class, this);
        if (newFactory == null) {
            throw new NutsExtensionMissingException(NutsWorkspaceFactory.class, "WorkspaceFactory");
        }
        NutsWorkspace nutsWorkspace = extensions().createSupported(NutsWorkspace.class, this);
        if (nutsWorkspace == null) {
            throw new NutsExtensionMissingException(NutsWorkspace.class, "Workspace");
        }
        NutsWorkspaceImpl nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
        if (nutsWorkspaceImpl.initializeWorkspace(newFactory,
                new NutsBootConfig(config().getRunningContext()),
                new NutsBootConfig(config().getBootContext()),
                config().getBootClassWorldURLs(),
                configManager.getBootClassLoader(), options.copy())) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "workspace created");
            }
        }
        return nutsWorkspace;
    }

    @Override
    public NutsWorkspace setSystemTerminal(NutsSystemTerminalBase term) {
        if (term == null) {
            throw new NutsExtensionMissingException(NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst = null;
        if ((term instanceof NutsSystemTerminal)) {
            syst = (NutsSystemTerminal) term;
        } else {
            try {
                syst = new DefaultSystemTerminal(term);
                syst.install(this);
            } catch (Exception ex) {
                syst = new DefaultSystemTerminal(new DefaultNutsSystemTerminalBase());
                syst.install(this);

            }
        }
        if (this.systemTerminal != null) {
            this.systemTerminal.uninstall();
        }
        NutsSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            for (NutsWorkspaceListener workspaceListener : workspaceListeners) {
                workspaceListener.onUpdateProperty("systemTerminal", old, this.systemTerminal);
            }
        }
        return this;
    }

    @Override
    public boolean initializeWorkspace(NutsWorkspaceFactory factory,
            NutsBootConfig runningBootConfig, NutsBootConfig wsBootConfig,
            URL[] bootClassWorldURLs, ClassLoader bootClassLoader,
            NutsWorkspaceOptions options) {

        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        if (options.getCreationTime() == 0) {
            configManager.setStartCreateTimeMillis(System.currentTimeMillis());
        }
        installedRepository = new DefaultNutsInstalledRepository(this);
        ioManager = new DefaultNutsIOManager(this);
        parseManager = new DefaultNutsParseManager(this);
        formatManager = new DefaultNutsFormatManager(this);
        extensionManager = new DefaultNutsWorkspaceExtensionManager(this, factory);
        configManager.onInitializeWorkspace(options,
                new DefaultNutsBootContext(runningBootConfig),
                new DefaultNutsBootContext(wsBootConfig),
                bootClassWorldURLs,
                bootClassLoader == null ? Thread.currentThread().getContextClassLoader() : bootClassLoader);

        boolean exists = config().isValidWorkspaceFolder();
        NutsWorkspaceOpenMode openMode = options.getOpenMode();
        if (openMode != null) {
            switch (openMode) {
                case OPEN_EXISTING: {
                    if (!exists) {
                        throw new NutsWorkspaceNotFoundException(runningBootConfig.getWorkspace());
                    }
                    break;
                }
                case CREATE_NEW: {
                    if (exists) {
                        throw new NutsWorkspaceAlreadyExistsException(runningBootConfig.getWorkspace());
                    }
                    break;
                }
            }
        }
        extensionManager.onInitializeWorkspace(bootClassLoader);

        NutsSystemTerminalBase termb = extensions().createSupported(NutsSystemTerminalBase.class, null);

        setSystemTerminal(termb);
        setTerminal(io().createTerminal());
        NutsSession session = createSession();

        initializing = true;
        try {
            if (!reloadWorkspace(session, options.getExcludedExtensions(), options.getExcludedRepositories())) {
                CoreNutsUtils.checkReadOnly(this);
                log.log(Level.CONFIG, "Unable to load existing workspace. Creating new one at {0}", config().getRunningContext().getWorkspace());
                exists = false;
                NutsWorkspaceConfig config = new NutsWorkspaceConfig();
                //load from config with resolution applied
                config.setBootApiVersion(wsBootConfig.getApiVersion());
                config.setBootRuntime(wsBootConfig.getRuntimeId());
                config.setBootRuntimeDependencies(wsBootConfig.getRuntimeDependencies());
                config.setBootRepositories(wsBootConfig.getRepositories());
                config.setBootJavaCommand(wsBootConfig.getJavaCommand());
                config.setBootJavaOptions(wsBootConfig.getJavaOptions());
                config.setStoreLocationStrategy(wsBootConfig.getStoreLocationStrategy());
                config.setRepositoryStoreLocationStrategy(wsBootConfig.getRepositoryStoreLocationStrategy());
                config.setStoreLocationLayout(wsBootConfig.getStoreLocationLayout());
                config.setGlobal(wsBootConfig.isGlobal());

                CoreNutsUtils.optionsToWconfig(options, config);
                configManager.setConfig(config);
                initializeWorkspace(options.getArchetype(), session);
                if (!config().isReadOnly()) {
                    config().save();
                }
                reconfigurePostInstall(new NutsInstallCompanionOptions().setAsk(true).setForce(false).setTrace(true), session);
                for (NutsWorkspaceListener workspaceListener : workspaceListeners) {
                    workspaceListener.onCreateWorkspace(this);
                }
            } else {
                if (options.getInitMode() == NutsBootInitMode.RECOVER) {
                    //should re
                    configManager.setBootApiVersion(wsBootConfig.getApiVersion());
                    configManager.setBootRuntime(wsBootConfig.getRuntimeId());
                    configManager.setBootRuntimeDependencies(wsBootConfig.getRuntimeDependencies());
                    configManager.setBootRepositories(wsBootConfig.getRepositories());
                    if (!config().isReadOnly()) {
                        config().save();
                    }
                }
            }
            if (configManager.getRepositoryRefs().length == 0) {
                initializeWorkspace(options.getArchetype(), session);
            }
            List<String> transientRepositoriesSet = options.getTransientRepositories() == null ? null : new ArrayList<>(Arrays.asList(options.getTransientRepositories()));
            for (String loc : transientRepositoriesSet) {
                String uuid = "transient_" + UUID.randomUUID().toString().replace("-", "");
                config()
                        .addRepository(
                                new NutsCreateRepositoryOptions()
                                        .setTemporay(true)
                                        .setName(uuid)
                                        .setFailSafe(false)
                                        .setLocation(loc)
                                        .setEnabled(true)
                        );
            }
            if (options.getLogin() != null && options.getLogin().trim().length() > 0) {
                String password = options.getPassword();
                if (StringUtils.isEmpty(password)) {
                    password = this.getTerminal().readPassword("Password : ");
                }
                this.security().login(options.getLogin(), password);
            }
            configManager.setStartCreateTimeMillis(options.getCreationTime());
            configManager.setEndCreateTimeMillis(System.currentTimeMillis());
            if (!options.isReadOnly()) {
                config().save(false);
            }
            log.log(Level.FINE, "Nuts Workspace loaded in {0}", Chronometer.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis()));
            if (options.isPerf()) {
                getTerminal().getFormattedOut().printf("**Nuts** Workspace loaded in [[%s]]\n",
                        Chronometer.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis())
                );
            }
        } finally {
            initializing = false;
        }
        return !exists;
    }

    public void reconfigurePostInstall(NutsInstallCompanionOptions options, NutsSession session) {
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Workspace created. running post creation configurator...");
        }
        if (options == null) {
            options = new NutsInstallCompanionOptions();
        }
        session = CoreNutsUtils.validateSession(session, this);
        if (!config().getOptions().isSkipInstallCompanions()) {
            if (options.isTrace()) {
                PrintStream out = terminal.getFormattedOut();
                StringBuilder version = new StringBuilder(config().getRunningContext().getRuntimeId().getVersion().toString());
                while (version.length() < 25) {
                    version.append(' ');
                }
                out.println("{{/------------------------------------------------------------------------------\\\\}}");
                out.println("{{|}}==``      _   __      __        ``==                                                  {{|}}");
                out.println("{{|}}==``     / | / /_  __/ /______  ``== ==N==etwork ==U==pdatable ==T==hings ==S==ervices                {{|}}");
                out.println("{{|}}==``    /  |/ / / / / __/ ___/  ``== <<The Open Source Package Manager for __Java__ (TM)>>    {{|}}");
                out.println("{{|}}==``   / /|  / /_/ / /_(__  )   ``== <<and other __things__>> ... by ==vpc==                      {{|}}");
                out.println("{{|}}==``  /_/ |_/\\\\____/\\\\__/____/``==     __http://github.com/thevpc/nuts__                    {{|}}");
                out.println("{{|}}      version [[" + version + "]]                                       {{|}}");
                out.println("{{|------------------------------------------------------------------------------|}}");
                out.println("{{|}}  This is the very {{first}} time ==Nuts== has been started for this workspace...     {{|}}");
                out.println("{{\\\\------------------------------------------------------------------------------/}}");
                out.println();
            }
            installCompanionTools(options, session);
        }
    }

    protected String[] getCompanionTools() {
        return new String[]{
            "net.vpc.app.nuts.toolbox:nsh",
            "net.vpc.app.nuts.toolbox:nfind",
            "net.vpc.app.nuts.toolbox:nadmin",
            "net.vpc.app.nuts.toolbox:ndi"
//                "mvn",
        };
    }

    @Override
    public void installCompanionTools(NutsInstallCompanionOptions options, NutsSession session) {
        if (options == null) {
            options = new NutsInstallCompanionOptions();
        }
        if (config().getOptions().isYes()) {
            //ok;
        } else if (config().getOptions().isNo()) {
            //ok;
            return;
        } else {
            NutsQuestion<Boolean> q = NutsQuestion.forBoolean("Would you like to install recommended companion tools").setDefautValue(true);
            if (options.isAsk() && !terminal.ask(q)) {
                return;
            }
        }
        String[] companionTools = getCompanionTools();
        if (companionTools.length > 0) {
            Chronometer cr = new Chronometer();
            if (log.isLoggable(Level.CONFIG)) {
                log.log(Level.FINE, "Installing companion tools...");
            }
            PrintStream out = terminal.getFormattedOut();
            int companionCount = 0;
//        NutsCommandExecBuilder e = createExecBuilder();
//        e.addCommand("net.vpc.app.nuts.toolbox:ndi","install","-f");
            for (String companionTool : companionTools) {
                if (options.isForce() || !isInstalled(companionTool, false, session)) {
                    if (options.isTrace()) {
                        if (companionCount == 0) {
                            out.println("Installation of Nuts companion tools...");
                        }
                        String d = fetch(companionTool).fetchDescriptor().getDescription();
                        out.printf("##\\### Installing ==%s== (%s)...\n", companionTool, d);
                    }
                    if (log.isLoggable(Level.CONFIG)) {
                        log.log(Level.FINE, "Installing companion tool : {0}", companionTool);
                    }
                    install(companionTool, new String[]{"--!silent", "--force"}, new NutsInstallOptions().setForce(true), session);
                    companionCount++;
                }
//            e.addCommand(companionTool.getId());
            }
            if (companionCount > 0) {
//            e.exec();
                if (options.isTrace()) {
                    out.printf("Installation of ==%s== companion tools in ==%s== ##succeeded##...\n", companionCount, cr);
                }
            } else {
                out.print("All companion tools are already installed...\n");
            }
            if (log.isLoggable(Level.CONFIG)) {
                log.log(Level.FINE, "Installed {0} companion tools in {1}...", new Object[]{companionCount, cr});
            }
        }
    }

    @Override
    public NutsDefinition fetchApiDefinition(NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (nutsComponentId == null) {
            nutsComponentId = fetch(NutsConstants.NUTS_ID_BOOT_API).setSession(session).fetchDefinition();
        }
        return nutsComponentId;
    }

    @Override
    public NutsDefinition fetchRuntimeDefinition(NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        return fetch(config().getRuntimeId()).setSession(session).fetchDefinition();
    }

    @Override
    public NutsDefinition install(String id, String[] args, NutsInstallOptions foundAction, NutsSession session) {
        return install(parser().parseRequiredId(id), args, foundAction, session);
    }

    @Override
    public NutsDefinition install(NutsId id, String[] args, NutsInstallOptions options, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (options == null) {
            options = new NutsInstallOptions();
        }
        PrintStream out = resolveOut(session);
        security().checkAllowed(NutsConstants.RIGHT_INSTALL, "install");
        NutsDefinition def = fetch(id).setSession(session).setAcceptOptional(false).includeDependencies().setIncludeInstallInformation(true).fetchDefinition();
        if (def != null && def.getContent().getPath() != null) {
            if (def.getInstallation().isInstalled()) {
                if (!options.isForce()) {
                    if (options.isTrace()) {
                        out.printf(formatter().createIdFormat().toString(def.getId()) + " already installed\n");
                    }
                    return def;
                }
            }
            installImpl(def, args, null, session, true, options.isTrace());
//            if (options.isTrace()) {
//                if (!def.getInstallation().isInstalled()) {
//                    if (!def.getContent().isCached()) {
//                        if (def.getContent().isTemporary()) {
//                            out.printf(formatter().createIdFormat().format(def.getId()) + " installed successfully from temporarily file %s\n", def.getContent().getPath());
//                        } else {
//                            out.printf(formatter().createIdFormat().format(def.getId()) + " installed successfully from remote repository\n");
//                        }
//                    } else {
//                        if (def.getContent().isTemporary()) {
//                            out.printf(formatter().createIdFormat().format(def.getId()) + " installed from local temporarily file %s \n", def.getContent().getPath());
//                        } else {
//                            out.printf(formatter().createIdFormat().format(def.getId()) + " installed from local repository\n");
//                        }
//                    }
//                } else {
//                    out.printf(formatter().createIdFormat().format(def.getId()) + " installed successfully\n");
//                }
//            }

        }
        return def;
    }

    @Override
    public NutsId commit(Path folder, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        security().checkAllowed(NutsConstants.RIGHT_DEPLOY, "commit");
        if (folder == null || !Files.isDirectory(folder)) {
            throw new NutsIllegalArgumentException("Not a directory " + folder);
        }

        Path file = folder.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
        NutsDescriptor d = parser().parseDescriptor(file);
        String oldVersion = StringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - NutsConstants.VERSION_CHECKED_OUT_EXTENSION.length());
            String newVersion = parser().parseVersion(oldVersion).inc().getValue();
            NutsDefinition newVersionFound = null;
            try {
                newVersionFound = fetch(d.getId().setVersion(newVersion)).setSession(session).fetchDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            if (newVersionFound == null) {
                d = d.setId(d.getId().setVersion(newVersion));
            } else {
                d = d.setId(d.getId().setVersion(oldVersion + ".1"));
            }
            NutsId newId = deploy(createDeploymentBuilder().setContent(folder).setDescriptor(d).build(), session);
            formatter().createDescriptorFormat().setPretty(true).format(d, file);
            try {
                IOUtils.delete(folder);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return newId;
        } else {
            throw new NutsUnsupportedOperationException("commit not supported");
        }
    }

    @Override
    public NutsDefinition checkout(String id, Path folder, NutsSession session) {
        return checkout(parser().parseRequiredId(id), folder, session);
    }

    @Override
    public NutsDefinition checkout(NutsId id, Path folder, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        security().checkAllowed(NutsConstants.RIGHT_INSTALL, "checkout");
        NutsDefinition nutToInstall = fetch(id).setSession(session).setAcceptOptional(false).includeDependencies().fetchDefinition();
        if ("zip".equals(nutToInstall.getDescriptor().getPackaging())) {

            try {
                ZipUtils.unzip(nutToInstall.getContent().getPath().toString(), io().expandPath(folder), new UnzipOptions().setSkipRoot(false));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

            Path file = folder.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
            NutsDescriptor d = parser().parseDescriptor(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().setVersion(oldVersion + NutsConstants.VERSION_CHECKED_OUT_EXTENSION);
            d = d.setId(newId);

            formatter().createDescriptorFormat().setPretty(true).format(d, file);

            return new DefaultNutsDefinition(
                    this, nutToInstall.getRepository(),
                    newId,
                    d,
                    new NutsContent(folder,
                            false,
                            false),
                    null
            );
        } else {
            throw new NutsUnsupportedOperationException("Checkout not supported");
        }
    }

    public NutsUpdate checkUpdates(String id, String bootApiVersion, NutsSession session) {
        return checkUpdates(parser().parseRequiredId(id), bootApiVersion, session);
    }

    public NutsUpdate checkUpdates(NutsId id, String bootApiVersion, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsId oldId = null;
        NutsDefinition oldFile = null;
        NutsDefinition newFile = null;
        NutsId newId = null;
        List<NutsId> dependencies = new ArrayList<>();
//        NutsSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        if (id.getSimpleName().equals(NutsConstants.NUTS_ID_BOOT_API)) {
            oldId = config().getConfigContext().getApiId();
            NutsId confId = config().getConfigContext().getApiId();
            if (confId != null) {
                oldId = confId;
            }
            String v = bootApiVersion;
            if (StringUtils.isEmpty(v)) {
                v = "LATEST";
            }
            try {
                oldFile = fetch(oldId).setSession(session).wired().fetchDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = this.fetch(NutsConstants.NUTS_ID_BOOT_API + "#" + v).setSession(session).wired().fetchDefinition();
                newId = newFile.getId();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        } else if (id.getSimpleName().equals(NutsConstants.NUTS_ID_BOOT_RUNTIME)) {
            oldId = config().getRunningContext().getRuntimeId();
            NutsId confId = config().getConfigContext().getRuntimeId();
            if (confId != null) {
                oldId = confId;
            }
            try {
                oldFile = fetch(oldId).setSession(session).wired().fetchDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = createQuery()
                        .addId(oldFile != null ? oldFile.getId().setVersion("").toString() : NutsConstants.NUTS_ID_BOOT_RUNTIME)
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .latestVersions()
                        .wired()
                        .setSession(session)
                        .fetchFirst();
                if (newFile != null) {
                    for (NutsDefinition d : createQuery().addId(newFile.getId()).latestVersions()
                            .wired()
                            .setSession(session).dependenciesOnly().fetch()) {
                        dependencies.add(d.getId());
                    }
                }

            } catch (NutsNotFoundException ex) {
                //ignore
            }
            if (newFile != null) {
                newId = newFile.getId();
            }
        } else {
            try {
                oldId = fetch(id).setIncludeEffective(true).setSession(session)
                        .offline().fetchId();
                oldFile = fetch(oldId).setSession(session).fetchDefinition();
            } catch (Exception ex) {
                //ignore
            }
            try {
                newFile = createQuery().addId(NutsConstants.NUTS_ID_BOOT_RUNTIME)
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .latestVersions().setSession(session).online().mainAndDependencies().fetchFirst();
                for (NutsDefinition d : createQuery().addId(newFile.getId()).latestVersions()
                        .wired().setSession(session).dependenciesOnly().fetch()) {
                    dependencies.add(d.getId());
                }
            } catch (Exception ex) {
                //ignore
            }
            if (newFile != null) {
                newId = newFile.getId();
            }
        }

        //compare canonical forms
        NutsId cnewId = toCanonicalForm(newId);
        NutsId coldId = toCanonicalForm(oldId);
        if (cnewId != null && coldId != null && cnewId.getVersion().compareTo(coldId.getVersion()) > 0) {
            String sOldFile = oldFile == null ? null : oldFile.getContent().getPath().toString();
            String sNewFile = newFile.getContent() == null ? null : newFile.getContent().getPath().toString();
            return new NutsUpdate(id, oldId, newId, sOldFile, sNewFile, dependencies.toArray(new NutsId[0]), false);
        }
        return null;
    }

    @Override
    public NutsUpdate[] checkWorkspaceUpdates(NutsWorkspaceUpdateOptions options, NutsSession session) {
        if (options == null) {
            options = new NutsWorkspaceUpdateOptions();
        }
        NutsBootContext actualBootConfig = config().getRunningContext();
//        NutsBootContext jsonBootConfig = getConfigManager().getBootContext();
        session = CoreNutsUtils.validateSession(session, this);
        Map<String, NutsUpdate> allUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdate> extUpdates = new LinkedHashMap<>();
        NutsUpdate bootUpdate = null;
        String bootVersion = config().getRunningContext().getApiId().getVersion().toString();
        if (!StringUtils.isEmpty(options.getForceBootAPIVersion())) {
            bootVersion = options.getForceBootAPIVersion();
        }
        if (options.isEnableMajorUpdates()) {
            bootUpdate = checkUpdates(NutsConstants.NUTS_ID_BOOT_API, options.getForceBootAPIVersion(), session);
            if (bootUpdate != null) {
                bootVersion = bootUpdate.getAvailableId().getVersion().toString();
                allUpdates.put(NutsConstants.NUTS_ID_BOOT_API, bootUpdate);
            }
        }
        NutsUpdate runtimeUpdate = null;
        if (requiresCoreExtension()) {

            runtimeUpdate = checkUpdates(actualBootConfig.getRuntimeId().getSimpleName(),
                    bootUpdate != null && bootUpdate.getAvailableId() != null ? bootUpdate.getAvailableId().toString()
                    : bootVersion, session);
            if (runtimeUpdate != null) {
                allUpdates.put(runtimeUpdate.getAvailableId().getSimpleName(), runtimeUpdate);
            }
        }
        if (options.isUpdateExtensions()) {
            for (NutsId ext : extensions().getExtensions()) {
                NutsUpdate extUpdate = checkUpdates(ext, bootVersion, session);
                if (extUpdate != null) {
                    allUpdates.put(extUpdate.getAvailableId().getSimpleName(), extUpdate);
                    extUpdates.put(extUpdate.getAvailableId().getSimpleName(), extUpdate);
                }
            }
        }
        NutsUpdate[] updates = allUpdates.values().toArray(new NutsUpdate[0]);
        PrintStream out = resolveOut(session);
        if (options.isTrace()) {
            if (updates.length == 0) {
                out.printf("Workspace is [[up-to-date]]. You are running latest version ==%s==\n", actualBootConfig.getRuntimeId().getVersion());
                return updates;
            } else {
                out.printf("Workspace has ##%s## component%s to update.\n", updates.length, (updates.length > 1 ? "s" : ""));
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NutsUpdate update : updates) {
                    widthCol1 = Math.max(widthCol1, update.getAvailableId().getSimpleName().length());
                    widthCol2 = Math.max(widthCol2, update.getLocalId().getVersion().toString().length());
                }
                for (NutsUpdate update : updates) {
                    out.printf("((%s))  : %s => [[%s]]\n",
                            StringUtils.alignLeft(update.getLocalId().getVersion().toString(), widthCol2),
                            StringUtils.alignLeft(update.getAvailableId().getSimpleName(), widthCol1),
                            update.getAvailableId().getVersion().toString());
                }
            }
        }
        if (!allUpdates.isEmpty() && options.isApplyUpdates()) {
            Path bootstrapFolder = config().getStoreLocation(NutsStoreLocation.CACHE).resolve("bootstrap");
            if (bootUpdate != null) {
                if (bootUpdate.getAvailableId() != null) {
                    CoreNutsUtils.checkReadOnly(this);
                    NutsBootConfig bc = config().getBootConfig();
                    bc.setApiVersion(bootUpdate.getAvailableId().getVersion().toString());
                    config().setBootConfig(bc);
                    io().copy().from(bootUpdate.getAvailableIdFile()).to(config().getStoreLocation(bootUpdate.getAvailableId(), bootstrapFolder)
                            .resolve(config().getDefaultIdFilename(bootUpdate.getAvailableId().setFaceComponent().setPackaging("jar")))
                    ).run();
                    if (runtimeUpdate == null) {

                    }
                }
            }
            if (runtimeUpdate != null) {
                NutsBootConfig bc = config().getBootConfig();
                bc.setRuntimeId(runtimeUpdate.getAvailableId().getVersion().toString());
                StringBuilder sb = new StringBuilder();
                for (NutsId dependency : runtimeUpdate.getDependencies()) {
                    if (sb.length() > 0) {
                        sb.append(";");
                    }
                    sb.append(dependency.setNamespace(null).toString());
                }
                bc.setRuntimeDependencies(sb.toString());
                CoreNutsUtils.checkReadOnly(this);
                config().setBootConfig(bc);
                io().copy().from(runtimeUpdate.getAvailableIdFile()).to(config().getStoreLocation(runtimeUpdate.getAvailableId(), bootstrapFolder)
                            .resolve(config().getDefaultIdFilename(runtimeUpdate.getAvailableId().setFaceComponent().setPackaging("jar")))
                    ).run();
            }
            for (NutsUpdate extension : extUpdates.values()) {
                extensions().updateExtension(extension.getAvailableId());
            }
            if (configManager.isConfigurationChanged()) {
                config().save(false);
                if (log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "Workspace is updated. Nuts should be restarted for changes to take effect.");
                }
            }
        }
        return updates;
    }

    /**
     * true when core extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : core extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION</code> is forced to
     * false
     *
     * @return true when core extension is required for running this workspace
     */
    public boolean requiresCoreExtension() {
        boolean exclude = false;
        if (extensions().getExtensions().length > 0) {
            exclude = Boolean.parseBoolean(config().getEnv(NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION, "false"));
        }
        if (!exclude) {
            boolean coreFound = false;
            for (NutsId ext : extensions().getExtensions()) {
                if (ext.equalsSimpleName(config().getRunningContext().getRuntimeId())) {
                    coreFound = true;
                    break;
                }
            }
            if (!coreFound) {
                return true;
            }
        }
        return false;
    }

    private Properties getBootInfo(NutsId id) {
        if (id.getVersion().isEmpty()) {
            id = id.setVersion("LATEST");
        }
        List<NutsURLLocation> bootUrls = new ArrayList<>();
        for (NutsURLLocation r : extensionManager.getExtensionURLLocations(id, NutsConstants.NUTS_ID_BOOT_API, "properties")) {
            bootUrls.add(r);
            if (r.getUrl() != null) {
                Properties p = IOUtils.loadURLProperties(r.getUrl());
                if (!p.isEmpty() && p.containsKey("runtimeId")) {
                    return p;
                }
            }
        }
        if (bootUrls.isEmpty()) {
            if (log.isLoggable(Level.CONFIG)) {
                log.log(Level.CONFIG, "Inaccessible runtime info. Fatal error");
            }
        }
        for (NutsURLLocation bootUrl : bootUrls) {
            log.log(Level.CONFIG, "Inaccessible runtime info URL : {0}", bootUrl.getPath());
        }
        throw new NutsIllegalArgumentException("Inaccessible runtime info : " + bootUrls);
    }

    @Override
    public NutsUpdateResult[] update(String[] toUpdateIds, String[] toRetainDependencies, NutsUpdateOptions foundAction, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        Map<String, NutsUpdateResult> all = new HashMap<>();
        for (String id : new HashSet<>(Arrays.asList(toUpdateIds))) {
            NutsUpdateResult updated = update(id, foundAction, session);
            all.put(updated.getId().getSimpleName(), updated);
        }
        if (toRetainDependencies != null) {
            for (String d : new HashSet<>(Arrays.asList(toRetainDependencies))) {
                NutsDependency dd = CoreNutsUtils.parseNutsDependency(d);
                if (all.containsKey(dd.getSimpleName())) {
                    NutsUpdateResult updated = all.get(dd.getSimpleName());
                    //FIX ME
                    if (!dd.getVersion().toFilter().accept(updated.getId().getVersion())) {
                        throw new NutsIllegalArgumentException(dd + " unsatisfied  : " + updated.getId().getVersion());
                    }
                }
            }
        }
        return all.values().toArray(new NutsUpdateResult[0]);
    }

    @Override
    public NutsUpdateResult update(String id, NutsUpdateOptions uptoDateAction, NutsSession session) {
        return update(parser().parseRequiredId(id), uptoDateAction, session);
    }

    @Override
    public NutsUpdateResult update(NutsId id, NutsUpdateOptions options, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (options == null) {
            options = new NutsUpdateOptions();
        }
        security().checkAllowed(NutsConstants.RIGHT_INSTALL, "update");
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException("Version is too restrictive. You would use fetch or install instead");
        }

        NutsUpdateResult r = new NutsUpdateResult().setId(id.getSimpleNameId());

        final PrintStream out = resolveOut(session);
        NutsDefinition d0 = fetch(id).setSession(session).offline().setAcceptOptional(false).fetchDefinitionOrNull();
        NutsDefinition d1 = fetch(id).setSession(session).setAcceptOptional(false).includeDependencies().fetchDefinitionOrNull();
        r.setLocalVersion(d0);
        r.setAvailableVersion(d1);
        final String simpleName = d0 != null ? d0.getId().getSimpleName() : d1 != null ? d1.getId().getSimpleName() : id.getSimpleName();
        if (d0 == null) {
            if (!options.isEnableInstall()) {
                throw new NutsIllegalArgumentException("No version is installed to be updated for " + id);
            }
            if (d1 == null) {
                throw new NutsNotFoundException(id);
            }
            r.setUpdateAvailable(true);
            r.setUpdateForced(false);
            if (options.isApplyUpdates()) {
                installImpl(d1, new String[0], null, session, true, options.isTrace());
                r.setUpdateApplied(true);
                if (options.isTrace()) {
                    out.printf("==%s== is [[forced]] to latest version ==%s==\n", simpleName, d1.getId().getVersion());
                }
            } else {
                if (options.isTrace()) {
                    out.printf("==%s== is [[not-installed]] . New version is available ==%s==\n", simpleName, d1.getId().getVersion());
                }
            }
        } else if (d1 == null) {
            //this is very interisting. Why the hell is this happening?
            r.setAvailableVersion(d0);
            if (options.isTrace()) {
                out.printf("==%s== is [[up-to-date]]. You are running latest version ==%s==\n", d0.getId().getSimpleName(), d0.getId().getVersion());
            }
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (options.isForce()) {
                    if (options.isApplyUpdates()) {
                        installImpl(d1, new String[0], null, session, true, options.isTrace());
                        r.setUpdateApplied(true);
                        r.setUpdateForced(true);
                        if (options.isTrace()) {
                            out.printf("==%s== is [[forced]] from ==%s== to older version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                        }
                    } else {
                        r.setUpdateForced(true);
                        if (options.isTrace()) {
                            out.printf("==%s== would be [[forced]] from ==%s== to older version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                        }
                    }
                } else {
                    if (options.isTrace()) {
                        out.printf("==%s== is [[up-to-date]]. You are running latest version ==%s==\n", simpleName, d0.getId().getVersion());
                    }
                }
            } else {
                r.setUpdateAvailable(true);
                if (options.isApplyUpdates()) {
                    installImpl(d1, new String[0], null, session, true, options.isTrace());
                    r.setUpdateApplied(true);
                    if (options.isTrace()) {
                        out.printf("==%s== is [[updated]] from ==%s== to latest version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                    }
                } else {
                    if (options.isTrace()) {
                        out.printf("==%s== is [[updatable]] from ==%s== to latest version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                    }
                }
            }
        }
        return r;
    }

    public boolean isInstalled(String id, boolean checkDependencies, NutsSession session) {
        return isInstalled(parser().parseRequiredId(id), checkDependencies, session);
    }

    public boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsDefinition nutToInstall = null;
        try {
            nutToInstall = fetch(id).setSession(session).setTransitive(false).includeDependencies(checkDependencies)
                    .offline()
                    .setAcceptOptional(false)
                    .setIncludeInstallInformation(true)
                    .fetchDefinition();
        } catch (NutsNotFoundException e) {
            return false;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error", e);
            return false;
        }
        return installedRepository.isInstalled(nutToInstall.getId());
    }

    @Override
    public boolean uninstall(String id, String[] args, NutsUninstallOptions options, NutsSession session) {
        return uninstall(parser().parseRequiredId(id), args, options, session);
    }

    @Override
    public boolean uninstall(NutsId id, String[] args, NutsUninstallOptions options, NutsSession session) {
        CoreNutsUtils.checkReadOnly(this);
        session = CoreNutsUtils.validateSession(session, this);
        security().checkAllowed(NutsConstants.RIGHT_UNINSTALL, "uninstall");
        NutsDefinition nutToInstall = fetch(id).setSession(session.copy()).setTransitive(false).setAcceptOptional(false).includeDependencies()
                .setIncludeInstallInformation(true).fetchDefinition();
        if (!nutToInstall.getInstallation().isInstalled()) {
            throw new NutsIllegalArgumentException(id + " Not Installed");
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        PrintStream out = resolveOut(session);
        if (ii != null) {
//        NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, args, new String[0], session, true, null);
            ii.uninstall(executionContext, options.isErase());
            try {
                IOUtils.delete(config().getStoreLocation(id, NutsStoreLocation.PROGRAMS).toFile());
                IOUtils.delete(config().getStoreLocation(id, NutsStoreLocation.TEMP).toFile());
                IOUtils.delete(config().getStoreLocation(id, NutsStoreLocation.LOGS).toFile());
                if (options.isErase()) {
                    IOUtils.delete(config().getStoreLocation(id, NutsStoreLocation.VAR).toFile());
                    IOUtils.delete(config().getStoreLocation(id, NutsStoreLocation.CONFIG).toFile());
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if (options.isTrace()) {
                out.printf(formatter().createIdFormat().toString(id) + " uninstalled ##successfully##\n");
            }
        } else {
            if (options.isTrace()) {
                out.printf(formatter().createIdFormat().toString(id) + " @@could not@@ be uninstalled\n");
            }
        }
        return true;
    }

    @Override
    public NutsCommandExecBuilder createExecBuilder() {
        return new DefaultNutsCommandExecBuilder(this);
    }

    @Override
    public boolean isFetched(String id, NutsSession session) {
        return isFetched(parser().parseRequiredId(id), session);
    }

    protected DefaultNutsDefinition fetchDescriptorAsDefinition(NutsId id, NutsQueryOptions options, NutsSession session, NutsFetchMode mode) {
        NutsRepositoryFilter repositoryFilter = null;
        if (mode == NutsFetchMode.INSTALLED) {
            final String[] all = getInstalledVersions(id);
            if (all.length > 0) {
                id = id.setVersion(all[all.length - 1]);
                mode = NutsFetchMode.LOCAL;
            } else {
                throw new NutsNotFoundException(id);
            }
        }
        for (NutsRepository repo : getEnabledRepositories(NutsWorkspaceHelper.FilterMode.FIND, id, repositoryFilter, session, mode, options)) {
            try {
                NutsDescriptor descriptor = repo.fetchDescriptor(id, NutsWorkspaceHelper.createRepositorySession(session, repo, mode,
                        options
                ));
                if (descriptor != null) {
                    NutsId nutsId = resolveEffectiveId(descriptor,
                            options,
                            session);
                    NutsIdBuilder newIdBuilder = nutsId.builder();
                    if (StringUtils.isEmpty(newIdBuilder.getNamespace())) {
                        newIdBuilder.setNamespace(repo.getName());
                    }
                    //inherit classifier from requested id
                    String classifier = id.getClassifier();
                    if (!StringUtils.isEmpty(classifier)) {
                        newIdBuilder.setClassifier(classifier);
                    }
                    Map<String, String> q = id.getQueryMap();
                    if (!CoreNutsUtils.isDefaultScope(q.get(NutsConstants.QUERY_SCOPE))) {
                        newIdBuilder.setScope(q.get(NutsConstants.QUERY_SCOPE));
                    }
                    if (!CoreNutsUtils.isDefaultOptional(q.get(NutsConstants.QUERY_OPTIONAL))) {
                        newIdBuilder.setOptional(q.get(NutsConstants.QUERY_OPTIONAL));
                    }
                    NutsId newId = newIdBuilder.build();
                    return new DefaultNutsDefinition(
                            this,
                            repo,
                            newId,
                            descriptor,
                            null,
                            null
                    );
                }
            } catch (NutsNotFoundException exc) {
                //
            }
        }
        throw new NutsNotFoundException(id);
    }

    public NutsDefinition fetchDefinition(NutsId id, NutsQueryOptions options, NutsSession session) {
        long startTime = System.currentTimeMillis();
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getFetchStrategy());
        session = CoreNutsUtils.validateSession(session, this);
        if (log.isLoggable(Level.FINEST)) {
            traceMessage(nutsFetchModes, id, TraceResult.START, "Fetch component", 0);
        }
        DefaultNutsDefinition foundDefinition = null;
        try {
            //add env parameters to fetch adequate nuts
            id = NutsWorkspaceHelper.configureFetchEnv(id, this);
            NutsFetchMode modeForSuccessfulDescRetreival = null;
            //use
            for (NutsFetchMode mode : nutsFetchModes) {
                try {
                    if (id.getGroup() == null) {
                        String[] groups = config().getImports();
                        for (String group : groups) {
                            try {
                                foundDefinition = fetchDescriptorAsDefinition(id.setGroup(group), options, session, mode);
                                if (foundDefinition != null) {
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
                                //not found
                            }
                        }
                        if (foundDefinition != null) {
                            modeForSuccessfulDescRetreival = mode;
                            break;
                        }
                        throw new NutsNotFoundException(id);
                    }
                    foundDefinition = fetchDescriptorAsDefinition(id, options, session, mode);
                    if (foundDefinition != null) {
                        modeForSuccessfulDescRetreival = mode;
                        break;
                    }
                } catch (NutsNotFoundException ex) {
                    //ignore
                }
            }
            if (foundDefinition != null) {
                if (options.isIncludeEffective()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(resolveEffectiveDescriptor(foundDefinition.getDescriptor(), session));
                    } catch (NutsNotFoundException ex) {
                        //ignore
                        log.log(Level.WARNING, "Nuts Descriptor Found, but its parent is not: {0} with parent {1}", new Object[]{id.toString(), Arrays.toString(foundDefinition.getDescriptor().getParents())});
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (options.isIncludeFile() || options.isIncludeInstallInformation()) {
                        NutsId id1 = config().createComponentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
                        Path copyTo = options.getLocation();
                        if (copyTo != null && Files.isDirectory(copyTo)) {
                            copyTo = copyTo.resolve(config().getDefaultIdFilename(id1));
                        }
                        for (NutsFetchMode mode : nutsFetchModes) {
                            try {
                                NutsRepository repo = foundDefinition.getRepository();
                                NutsContent content = repo.fetchContent(id1, copyTo,
                                        NutsWorkspaceHelper.createRepositorySession(session, repo, mode, options));
                                if (content != null) {
                                    foundDefinition.setContent(content);
                                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getPath()));
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
                                if (mode.ordinal() < modeForSuccessfulDescRetreival.ordinal()) {
                                    //ignore because actually there is more chance to find it in later modes!
                                } else {
                                    log.log(Level.WARNING, "Nuts Descriptor Found, but component could not be resolved : {0}", id.toString());
                                }
                            }
                        }
                        if (foundDefinition.getContent() == null || foundDefinition.getContent().getPath() == null) {
                            traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetched Descriptor but failed to fetch Component", startTime);
                            foundDefinition = null;
                        }
                    }
                    if (foundDefinition != null && options.isIncludeInstallInformation()) {
                        NutsInstallerComponent installer = null;
                        if (foundDefinition.getContent().getPath() != null) {
                            installer = getInstaller(foundDefinition, session);
                        }
                        if (installer != null) {
                            if (installedRepository.isInstalled(foundDefinition.getId())) {
                                foundDefinition.setInstallation(new NutsInstallInfo(true,
                                        config().getStoreLocation(foundDefinition.getId(), NutsStoreLocation.PROGRAMS)
                                ));
                            } else {
                                foundDefinition.setInstallation(NOT_INSTALLED);
                            }
                        } else {
                            foundDefinition.setInstallation(NOT_INSTALLED);
                        }
                    }
                }
            }
        } catch (NutsNotFoundException ex) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
        if (foundDefinition != null) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(nutsFetchModes, id, TraceResult.SUCCESS, "Fetch component", startTime);
            }
            return foundDefinition;
        }
        throw new NutsNotFoundException(id);
    }

    public Iterator<NutsId> findIterator(DefaultNutsSearch search) {
        NutsSession session = CoreNutsUtils.validateSession(search.getOptions().getSession(), this);
        NutsVersionFilter versionFilter = search.getVersionFilter();
        NutsIdFilter idFilter = search.getIdFilter();
        NutsRepositoryFilter repositoryFilter = search.getRepositoryFilter();
        NutsDescriptorFilter descriptorFilter = search.getDescriptorFilter();
        String[] goodIds = search.getIds();
        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(search.getOptions().getFetchStrategy());
        if (goodIds.length > 0) {
            List<Iterator<NutsId>> result = new ArrayList<>();
            for (String id : goodIds) {
                NutsId nutsId = parser().parseId(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (nutsId.getGroup() == null) {
                        for (String aImport : config().getImports()) {
                            nutsId2.add(nutsId.setGroup(aImport));
                        }
                    } else {
                        nutsId2.add(nutsId);
                    }
                    List<Iterator<NutsId>> coalesce = new ArrayList<>();
                    for (NutsFetchMode mode : fetchMode) {
                        List<Iterator<NutsId>> all = new ArrayList<>();
                        for (NutsId nutsId1 : nutsId2) {
                            if (mode == NutsFetchMode.INSTALLED) {
                                all.add(
                                        IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                            @Override
                                            public Iterator<NutsId> iterator() {
                                                NutsIdFilter filter = new DefaultNutsIdMultiFilter(
                                                        nutsId1.getQueryMap(), idFilter,
                                                        versionFilter, descriptorFilter, null,
                                                        NutsWorkspaceHelper.createNoRepositorySession(session, mode,
                                                                search.getOptions())
                                                ).simplify();
                                                return installedRepository.findVersions(nutsId1, filter);
                                            }
                                        }).safeIgnore().iterator());
                            } else {
                                for (NutsRepository repo : getEnabledRepositories(NutsWorkspaceHelper.FilterMode.FIND, nutsId1, repositoryFilter, session, mode, search.getOptions())) {
                                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                        NutsIdFilter filter = new DefaultNutsIdMultiFilter(nutsId1.getQueryMap(), idFilter,
                                                versionFilter, descriptorFilter, repo, NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions())).simplify();
                                        all.add(
                                                IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                                    @Override
                                                    public Iterator<NutsId> iterator() {
                                                        return repo.findVersions(nutsId1, filter, NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions()));
                                                    }
                                                }).safeIgnore().iterator()
                                        );
                                    }
                                }
                            }
                        }
                        coalesce.add(IteratorUtils.concat(all));
                    }
                    if (nutsId.getGroup() == null) {
                        //now will look with *:artifactId pattern
                        NutsQuery search2 = createQuery()
                                .copyFrom(search.getOptions())
                                .addId(search.getIds())
                                .setIdFilter(search.getIdFilter())
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setVersionFilter(search.getVersionFilter())
                                .setDescriptorFilter(search.getDescriptorFilter())
                                .setFetchStratery(search.getOptions().getFetchStrategy())
                                .setSession(session)
                                .setIds();
                        search2.setIdFilter(new NutsIdFilterOr(
                                new NutsPatternIdFilter(new String[]{nutsId.setGroup("*").toString()}),
                                CoreNutsUtils.simplify(search2.getIdFilter())
                        ));
                        coalesce.add(search2.findIterator());
                    }
                    result.add(fetchMode.isStopFast()
                            ? IteratorUtils.coalesce(coalesce)
                            : IteratorUtils.concat(coalesce)
                    );
                }
            }
            return IteratorUtils.concat(result);
        }

        if (idFilter instanceof NutsPatternIdFilter) {
            String[] ids = ((NutsPatternIdFilter) idFilter).getIds();
            if (ids.length == 1) {
                String id = ids[0];
                if (id.indexOf('*') < 0 && id.indexOf(':') > 0) {
                    NutsId nid = parser().parseId(id);
                    if (nid != null) {
                        List<Iterator<NutsId>> coalesce = new ArrayList<>();
                        for (NutsFetchMode mode : fetchMode) {
                            if (mode == NutsFetchMode.INSTALLED) {
                                coalesce.add(new LazyIterator<>(new Iterable<NutsId>() {
                                    @Override
                                    public Iterator<NutsId> iterator() {
                                        NutsIdFilter filter = new DefaultNutsIdMultiFilter(nid.getQueryMap(), idFilter, versionFilter,
                                                descriptorFilter, null, NutsWorkspaceHelper.createNoRepositorySession(session, mode, search.getOptions())).simplify();
                                        return installedRepository.findVersions(nid, filter);
                                    }
                                }));

                            } else {
                                List<Iterator<NutsId>> all = new ArrayList<>();
                                for (NutsRepository repo : getEnabledRepositories(repositoryFilter)) {
                                    try {
                                        if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                            NutsRepositorySession ss = NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions());
                                            DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(nid.getQueryMap(), idFilter,
                                                    versionFilter, descriptorFilter, repo, ss);
                                            all.add(new LazyIterator<>(new Iterable<NutsId>() {
                                                @Override
                                                public Iterator<NutsId> iterator() {
                                                    return repo.findVersions(nid, filter, ss);
                                                }
                                            }));
                                        }
                                    } catch (Exception exc) {
                                        //
                                    }
                                }
                                coalesce.add(IteratorUtils.concat(all));
                            }
                        }
                        return fetchMode.isStopFast()
                                ? IteratorUtils.coalesce(coalesce)
                                : IteratorUtils.concat(coalesce);
                    }
                }
            }
        }

        List<Iterator<NutsId>> coalesce = new ArrayList<>();
        for (NutsFetchMode mode : fetchMode) {
            List<Iterator<NutsId>> all = new ArrayList<>();
            for (NutsRepository repo : getEnabledRepositories(repositoryFilter)) {
                if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                    NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions());
                    NutsIdFilter filter = new DefaultNutsIdMultiFilter(null, idFilter, versionFilter, descriptorFilter, repo, rsession).simplify();
                    all.add(
                            IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                @Override
                                public Iterator<NutsId> iterator() {
                                    return repo.find(filter, rsession);
                                }
                            }).safeIgnore().iterator()
                    );
                }

            }
            coalesce.add(IteratorUtils.concat(all));
        }
        return fetchMode.isStopFast() ? IteratorUtils.coalesce(coalesce) : IteratorUtils.concat(coalesce);
    }

    protected void traceMessage(NutsFetchStrategy fetchMode, NutsId id, TraceResult tracePhase, String message, long startTime) {
        String timeMessage = "";
        if (startTime != 0) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                timeMessage = " (" + time + "ms)";
            }
        }
        String tracePhaseString = "";
        switch (tracePhase) {
            case ERROR: {
                tracePhaseString = "[ERROR  ] ";
                break;
            }
            case SUCCESS: {
                tracePhaseString = "[SUCCESS] ";
                break;
            }
            case START: {
                tracePhaseString = "[START  ] ";
                break;
            }
        }
        String fetchString = fetchString = "[" + StringUtils.alignLeft(fetchMode.name(), 7) + "] ";
        log.log(Level.FINEST, tracePhaseString + fetchString
                + StringUtils.alignLeft(message, 18) + " " + id + timeMessage);
    }

    @Override
    public void push(String id, NutsPushOptions options, NutsSession session) {
        push(parser().parseRequiredId(id), options, session);
    }

    @Override
    public void push(NutsId id, NutsPushOptions options, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsRepositoryFilter repositoryFilter = null;
        if (StringUtils.trim(id.getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            throw new NutsIllegalArgumentException("Invalid Version " + id.getVersion());
        }
        NutsDefinition file = fetch(id).setSession(session).setTransitive(false).fetchDefinition();
        if (file == null) {
            throw new NutsIllegalArgumentException("Nothing to push");
        }
        if (options == null) {
            options = new NutsPushOptions();
        }
        NutsQueryOptions fetchOptions = createQueryOptions().setTransitive(true);
        if (StringUtils.isEmpty(options.getRepository())) {
            Set<String> errors = new LinkedHashSet<>();
            //TODO : CHEK ME, why offline?
            for (NutsRepository repo : getEnabledRepositories(NutsWorkspaceHelper.FilterMode.DEPLOY, file.getId(), repositoryFilter, session, NutsFetchMode.LOCAL, fetchOptions)) {
                NutsDescriptor descr = null;
                NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, options.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE, fetchOptions);
                try {
                    descr = repo.fetchDescriptor(file.getId(), rsession);
                } catch (Exception e) {
                    errors.add(StringUtils.exceptionToString(e));
                    //
                }
                if (descr != null && repo.config().isSupportedMirroring()) {
                    NutsId id2 = config().createComponentFaceId(resolveEffectiveId(descr,
                            new DefaultNutsQueryOptions().setTransitive(true),
                            session
                    ), descr);
                    try {
                        repo.push(id2, options, rsession);
                        return;
                    } catch (Exception e) {
                        errors.add(StringUtils.exceptionToString(e));
                        //
                    }
                }
            }
            throw new NutsRepositoryNotFoundException(options.getRepository() + " : " + StringUtils.join("\n", errors));
        } else {
            NutsRepository repo = config().getRepository(options.getRepository());
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, options.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE,
                    fetchOptions
            );
            checkEnabled(repo.getName());
            NutsId effId = config().createComponentFaceId(id.unsetQuery(), file.getDescriptor()).setAlternative(StringUtils.trim(file.getDescriptor().getAlternative()));
            NutsRepositoryDeploymentOptions dep = new DefaultNutsRepositoryDeploymentOptions()
                    .setId(effId)
                    .setContent(file.getContent().getPath())
                    .setDescriptor(file.getDescriptor())
                    .setRepository(repo.getName())
                    .setTrace(options.isTrace())
                    .setForce(options.isForce())
                    .setTransitive(true)
                    .setOffline(options.isOffline());
            repo.deploy(dep, rsession);
        }
    }

    @Override
    public NutsDefinition createBundle(Path contentFolder, Path destFile, NutsQueryOptions queryOptions, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (Files.isDirectory(contentFolder)) {
            NutsDescriptor descriptor = null;
            Path ext = contentFolder.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
            if (Files.exists(ext)) {
                descriptor = parser().parseDescriptor(ext);
            } else {
                descriptor = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, CoreIOUtils.toInputStreamSource(contentFolder), queryOptions, session);
            }
            if (descriptor != null) {
                if ("zip".equals(descriptor.getPackaging())) {
                    if (destFile == null) {
                        destFile = io().path(io().expandPath(contentFolder.getParent().resolve(descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip")));
                    }
                    try {
                        ZipUtils.zip(contentFolder.toString(), new ZipOptions(), destFile.toString());
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                    return new DefaultNutsDefinition(
                            this, null,
                            descriptor.getId(),
                            descriptor,
                            new NutsContent(destFile,
                                    true,
                                    false),
                            null
                    );
                } else {
                    throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                }
            }
            throw new NutsIllegalArgumentException("Invalid Nut Folder source. unable to detect descriptor");
        } else {
            throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
        }
    }

    @Override
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsQueryOptions options, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (descriptor == null) {
            throw new NutsNotFoundException("<null>");
        }
        NutsId thisId = descriptor.getId();
        if (CoreNutsUtils.isEffectiveId(thisId)) {
            return thisId.setAlternative(descriptor.getAlternative());
        }
        String g = thisId.getGroup();
        String v = thisId.getVersion().getValue();
        if ((StringUtils.isEmpty(g)) || (StringUtils.isEmpty(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = fetch(parent).copyFrom(options).setIncludeEffective(true).setSession(session).fetchId();
                if (StringUtils.isEmpty(g)) {
                    g = p.getGroup();
                }
                if (StringUtils.isEmpty(v)) {
                    v = p.getVersion().getValue();
                }
                if (!StringUtils.isEmpty(g) && !StringUtils.isEmpty(v)) {
                    break;
                }
            }
            if (StringUtils.isEmpty(g) || StringUtils.isEmpty(v)) {
                throw new NutsNotFoundException(thisId, "Unable to fetchEffective for " + thisId + ". Best Result is " + thisId.toString(), null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v)) {
            Map<String, String> p = descriptor.getProperties();
            NutsId bestId = new DefaultNutsId(null, g, thisId.getName(), v, "");
            bestId = bestId.apply(new MapStringMapper(p));
            if (CoreNutsUtils.isEffectiveId(bestId)) {
                return bestId.setAlternative(descriptor.getAlternative());
            }
            Stack<NutsId> all = new Stack<>();
            NutsId[] parents = descriptor.getParents();
            all.addAll(Arrays.asList(parents));
            while (!all.isEmpty()) {
                NutsId parent = all.pop();
                NutsDescriptor dd = fetch(parent).copyFrom(options).setIncludeEffective(true).setSession(session).fetchDescriptor();
                bestId.apply(new MapStringMapper(dd.getProperties()));
                if (CoreNutsUtils.isEffectiveId(bestId)) {
                    return bestId.setAlternative(descriptor.getAlternative());
                }
                all.addAll(Arrays.asList(dd.getParents()));
            }
            throw new NutsNotFoundException(bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
        NutsId bestId = new DefaultNutsId(null, g, thisId.getName(), v, "");
        if (!CoreNutsUtils.isEffectiveId(bestId)) {
            throw new NutsNotFoundException(bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
        return bestId.setAlternative(descriptor.getAlternative());
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        if (!descriptor.getId().getVersion().isEmpty() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            Path l = config().getStoreLocation(descriptor.getId(), NutsStoreLocation.CACHE);
            String nn = config().getDefaultIdFilename(descriptor.getId().setFace("cache-eff-nuts"));
            Path eff = l.resolve(nn);
            if (Files.isRegularFile(eff)) {
                try {
                    NutsDescriptor d = parser().parseDescriptor(eff);
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    //
                }
            }
        } else {
            //System.out.println("Why");
        }
        NutsDescriptor effDesc = _resolveEffectiveDescriptor(descriptor, session);
        Path l = config().getStoreLocation(effDesc.getId(), NutsStoreLocation.CACHE);
        String nn = config().getDefaultIdFilename(effDesc.getId().setFace("cache-eff-nuts"));
        Path eff = l.resolve(nn);
        try {
            formatter().createDescriptorFormat().setPretty(true).format(effDesc, eff);
        } catch (Exception ex) {
            //
        }
        return effDesc;
    }

    public NutsDescriptor _resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetch(parents[i]).setIncludeEffective(false).setSession(session).fetchDescriptor(),
                    session
            );
        }
        NutsDescriptor nutsDescriptor = descriptor.applyParents(parentDescriptors).applyProperties();
//        if (nutsDescriptor.getPackaging().isEmpty()) {
//            descriptor.applyParents(parentDescriptors).applyProperties();
//        }
        NutsDependency[] old = nutsDescriptor.getDependencies();
        List<NutsDependency> newDeps = new ArrayList<>();
        boolean someChange = false;

        for (int i = 0; i < old.length; i++) {
            NutsDependency d = old[i];
            if (StringUtils.isEmpty(d.getScope())
                    || d.getVersion().isEmpty()
                    || StringUtils.isEmpty(d.getOptional())) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : nutsDescriptor.getStandardDependencies()) {
                    if (standardDependency.getSimpleName().equals(d.getId().getSimpleName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (StringUtils.isEmpty(d.getScope())
                            && !StringUtils.isEmpty(standardDependencyOk.getScope())) {
                        someChange = true;
                        d = d.setScope(standardDependencyOk.getScope());
                    }
                    if (StringUtils.isEmpty(d.getOptional())
                            && !StringUtils.isEmpty(standardDependencyOk.getOptional())) {
                        someChange = true;
                        d = d.setOptional(standardDependencyOk.getOptional());
                    }
                    if (d.getVersion().isEmpty()
                            && !standardDependencyOk.getVersion().isEmpty()) {
                        someChange = true;
                        d = d.setVersion(standardDependencyOk.getVersion());
                    }
                }
            }

            if ("import".equals(d.getScope())) {
                someChange = true;
                for (NutsDependency dependency : fetch(d.getId()).setIncludeEffective(true).setSession(session).fetchDescriptor().getDependencies()) {
                    newDeps.add(dependency);
                }
            } else {
                newDeps.add(d);
            }
        }
        if (someChange) {
            nutsDescriptor = nutsDescriptor.setDependencies(newDeps.toArray(new NutsDependency[0]));
        }
        return nutsDescriptor;
    }

    @Override
    public NutsId deploy(NutsDeployment deployment, NutsSession session) {
        CoreNutsUtils.checkReadOnly(this);
        try {
            Path tempFile = null;
            Object content = deployment.getContent();
            InputStreamSource contentSource;
            contentSource = CoreIOUtils.toInputStreamSource(content);
            NutsDescriptor descriptor = deployment.getDescriptor();

            CharacterizedFile characterizedFile = null;
            Path contentFile2 = null;
            try {
                if (descriptor == null) {
                    NutsQueryOptions p = createQueryOptions();
                    p.setTransitive(deployment.isTransitive());
                    characterizedFile = CoreNutsUtils.characterize(this, contentSource, p, session);
                    if (characterizedFile.descriptor == null) {
                        throw new NutsIllegalArgumentException("Missing descriptor");
                    }
                    descriptor = characterizedFile.descriptor;
                }
                String name = config().getDefaultIdFilename(descriptor.getId().setFaceDescriptor());
                tempFile = io().createTempFile(name);
                io().copy().from(contentSource.open()).to(tempFile).safeCopy().run();
                contentFile2 = tempFile;

                Path contentFile0 = contentFile2;
                String repository = deployment.getRepository();

                CoreNutsUtils.checkReadOnly(this);
                Path contentFile = contentFile0;
                session = CoreNutsUtils.validateSession(session, this);
                Path tempFile2 = null;
                NutsQueryOptions fetchOptions = CoreNutsUtils.createQueryOptions().setTransitive(deployment.isTransitive());
                try {
                    if (Files.isDirectory(contentFile)) {
                        Path descFile = contentFile.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
                        NutsDescriptor descriptor2;
                        if (Files.exists(descFile)) {
                            descriptor2 = parser().parseDescriptor(descFile);
                        } else {
                            descriptor2 = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(contentFile), fetchOptions, session);
                        }
                        if (descriptor == null) {
                            descriptor = descriptor2;
                        } else {
                            if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                                formatter().createDescriptorFormat().setPretty(true).format(descriptor, descFile);
                            }
                        }
                        if (descriptor != null) {
                            if ("zip".equals(descriptor.getPackaging())) {
                                Path zipFilePath = io().path(this.io().expandPath(contentFile.toString() + ".zip"));
                                try {
                                    ZipUtils.zip(contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                                } catch (IOException ex) {
                                    throw new UncheckedIOException(ex);
                                }
                                contentFile = zipFilePath;
                                tempFile2 = contentFile;
                            } else {
                                throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                            }
                        }
                    } else {
                        if (descriptor == null) {
                            descriptor = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(contentFile), fetchOptions, session);
                        }
                    }
                    if (descriptor == null) {
                        throw new NutsNotFoundException(" at " + contentFile);
                    }
                    //remove workspace
                    descriptor = descriptor.setId(descriptor.getId().setNamespace(null));
                    if (StringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
                        throw new NutsIllegalArgumentException("Invalid Version " + descriptor.getId().getVersion());
                    }

                    NutsId effId = resolveEffectiveId(descriptor, new DefaultNutsQueryOptions().setTransitive(true), session);
                    for (String os : descriptor.getOs()) {
                        CorePlatformUtils.checkSupportedOs(parser().parseRequiredId(os).getSimpleName());
                    }
                    for (String arch : descriptor.getArch()) {
                        CorePlatformUtils.checkSupportedArch(parser().parseRequiredId(arch).getSimpleName());
                    }
                    if (StringUtils.isEmpty(repository)) {
                        NutsRepositoryFilter repositoryFilter = null;
                        List<NutsRepository> possible = new ArrayList<>();
                        //TODO CHECK ME, why offline
                        for (NutsRepository repo : getEnabledRepositories(NutsWorkspaceHelper.FilterMode.DEPLOY, effId, repositoryFilter, session, NutsFetchMode.LOCAL, fetchOptions)) {
                            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, deployment.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE, fetchOptions);

                            effId = config().createComponentFaceId(effId.unsetQuery(), descriptor).setAlternative(StringUtils.trim(descriptor.getAlternative()));
                            repo.deploy(
                                    new DefaultNutsRepositoryDeploymentOptions()
                                            .setForce(deployment.isForce())
                                            .setOffline(deployment.isOffline())
                                            .setTrace(deployment.isTrace())
                                            .setTransitive(deployment.isTransitive())
                                            .setId(effId).setContent(contentFile).setDescriptor(descriptor).setRepository(repository), rsession);
                            return effId;
                        }
                    } else {
                        NutsRepository repo = getEnabledRepositoryOrError(repository);
                        if (repo == null) {
                            throw new NutsRepositoryNotFoundException(repository);
                        }
                        NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, deployment.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE, fetchOptions);
                        effId = config().createComponentFaceId(effId.unsetQuery(), descriptor).setAlternative(StringUtils.trim(descriptor.getAlternative()));
                        repo.deploy(new DefaultNutsRepositoryDeploymentOptions()
                                .setForce(deployment.isForce())
                                .setOffline(deployment.isOffline())
                                .setTrace(deployment.isTrace())
                                .setTransitive(deployment.isTransitive())
                                .setId(effId).setContent(contentFile).setDescriptor(descriptor).setRepository(repository), rsession);
                        return effId;
                    }
                    throw new NutsRepositoryNotFoundException(repository);
                } finally {
                    if (tempFile2 != null) {
                        try {
                            Files.delete(tempFile2);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                }
            } finally {
                if (characterizedFile != null) {
                    characterizedFile.close();
                }
                if (tempFile != null) {
                    IOUtils.delete(tempFile);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    //    public NutsId deploy(InputStream contentInputStream, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) {
//        session = validateSession(session);
//        File tempFile = null;
//        try {
//            tempFile = CoreIOUtils.createTempFile(descriptor, false);
//            try {
//                CoreIOUtils.copy(contentInputStream, tempFile, true, true);
//                return deploy(tempFile, sha1, descriptor, repositoryId, session);
//            } finally {
//                tempFile.delete();
//            }
//        } catch (IOException e) {
//            throw new NutsIOException(e);
//        }
//    }
    @Override
    public Path copyTo(String id, Path localPath, NutsSession session) {
        return copyTo(parser().parseRequiredId(id), localPath, session);
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return extensionManager;
    }

    public List<NutsRepository> getEnabledRepositories(NutsRepositoryFilter repositoryFilter) {
        List<NutsRepository> repos = _getEnabledRepositories(this, repositoryFilter);
        Collections.sort(repos, SpeedRepositoryComparator.INSTANCE);
        return repos;
    }

    protected static List<NutsRepository> _getEnabledRepositories(Object parent, NutsRepositoryFilter repositoryFilter) {
        List<NutsRepository> repos = new ArrayList<>();
        if (parent instanceof NutsWorkspace) {
            List<NutsRepository> subrepos = new ArrayList<>();
            NutsWorkspace ws = (NutsWorkspace) parent;
            for (NutsRepository repository : ws.config().getRepositories()) {
                boolean ok = false;
                if (repository.config().isEnabled()) {
                    if (repositoryFilter == null || repositoryFilter.accept(repository)) {
                        repos.add(repository);
                        ok = true;
                    }
                    if (!ok) {
                        subrepos.add(repository);
                    }
                }
            }
            for (NutsRepository subrepo : subrepos) {
                repos.addAll(_getEnabledRepositories(subrepo, repositoryFilter));
            }
        } else if (parent instanceof NutsRepository) {
            NutsRepository repo = (NutsRepository) parent;
            if (repo.config().isSupportedMirroring()) {
                List<NutsRepository> subrepos = new ArrayList<>();
                boolean ok = false;
                for (NutsRepository repository : repo.config().getMirrors()) {
                    if (repository.config().isEnabled()) {
                        if (repositoryFilter == null || repositoryFilter.accept(repository)) {
                            repos.add(repository);
                            ok = true;
                        }
                        if (!ok) {
                            subrepos.add(repository);
                        }
                    }
                }
                for (NutsRepository subrepo : subrepos) {
                    repos.addAll(_getEnabledRepositories(subrepo, repositoryFilter));
                }
            }
        }
        return repos;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    public int exec(Path nutsJarFile, String[] args, boolean copyCurrentToFile, boolean waitFor, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        PrintStream out = resolveOut(session);
        if (copyCurrentToFile) {
            Path acFile = config().resolveNutsJarFile();
            if (nutsJarFile == null) {
                nutsJarFile = acFile;
            } else {
                if (acFile != null) {
                    if (!Files.exists(acFile)) {
                        throw new NutsIllegalArgumentException("Could not apply update from non existing source " + acFile);
                    }
                    if (Files.isDirectory(acFile)) {
                        throw new NutsIllegalArgumentException("Could not apply update from directory source " + acFile);
                    }
                    if (Files.exists(nutsJarFile)) {
                        if (Files.isDirectory(nutsJarFile)) {
                            throw new NutsIllegalArgumentException("Could not apply update on folder " + nutsJarFile);
                        }
                        if (Files.exists(nutsJarFile)) {
                            int index = 1;
                            Path nn = null;
                            while (Files.exists(nn = nutsJarFile.resolveSibling(nutsJarFile.getFileName().toString() + "." + index))) {
                                index++;
                            }
                            out.printf("copying [[%s]] to [[%s.%s]]\n", nutsJarFile, nn, index);
                            io().copy().from(nutsJarFile).to(nn).safeCopy().run();
                        }
                        out.printf("copying [[%s]] to [[%s]]\n", acFile, nutsJarFile);
                        io().copy().from(acFile).to(nutsJarFile).safeCopy().run();
                    } else if (nutsJarFile.getFileName().toString().endsWith(".jar")) {
                        out.printf("copying [[%s]] to [[%s]]\n", acFile, nutsJarFile);
                        io().copy().from(acFile).to(nutsJarFile).safeCopy().run();
                    } else {
                        throw new NutsIllegalArgumentException("Could not apply update to target " + nutsJarFile + ". expected jar file name");
                    }
                } else {
                    throw new NutsIllegalArgumentException("Unable to resolve source to update from");
                }
                List<String> all = new ArrayList<>();
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];
                    switch (arg) {
                        case "--update":
                        case "--check-updates":
                            //do nothing...
                            break;
                        case "--apply-updates":
                            i++;
                            break;
                        default:
                            all.add(arg);
                            break;
                    }
                }
                out.printf("nuts patched ===successfully===...\n");
                if (all.size() > 0) {
                    out.printf("running command (%s) with newly patched version (%s)\n", all, nutsJarFile);
                    args = all.toArray(new String[0]);
                }
            }
        }

        if (nutsJarFile == null) {
            nutsJarFile = config().resolveNutsJarFile();
        }
        if (nutsJarFile == null) {
            throw new NutsIllegalArgumentException("Unable to locate nutsJarFile");
        }
        List<String> all = new ArrayList<>();
        all.add(CoreNutsUtils.resolveJavaCommand(null));
        if (Files.isDirectory(nutsJarFile)) {
            all.add("-classpath");
            all.add(nutsJarFile.toString());
        } else {
            all.add("-jar");
            all.add(nutsJarFile.toString());
        }
        all.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(all);
        pb.inheritIO();
        Process process = null;
        try {
            process = pb.start();
            if (waitFor) {
                return process.waitFor();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new NutsException(e);
        }
        return 0;
    }

    private PrintStream resolveOut(NutsSession session) {
        return CoreNutsUtils.resolveOut(this, session);
    }

    protected void initializeWorkspace(String archetype, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (StringUtils.isEmpty(archetype)) {
            archetype = "default";
        }
        //should be here but the problem is that no repository is already
        //registered so where would we install extension from ?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_CORE_ID, session);
//        }catch(Exception ex){
//            log.log(Level.SEVERE, "Unable to load Nuts-core. The tool is running in minimal mode.");
//        }
        NutsWorkspaceArchetypeComponent instance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NutsWorkspaceArchetypeComponent ac : extensions().createAllSupported(NutsWorkspaceArchetypeComponent.class, this)) {
            if (archetype.equals(ac.getName())) {
                instance = ac;
                break;
            }
            validValues.add(ac.getName());
        }
        if (instance == null) {
            //get the default implementation
            throw new NutsException("Invalid archetype " + archetype + ". Valid values are : " + validValues);
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        security().setUserCredentials(NutsConstants.USER_ADMIN, "admin");

        instance.initialize(this, session);

//        //isn't it too late for adding extensions?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_ID_BOOT_RUNTIME, session);
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, "Unable to load Nuts-core. The tool is running in minimal mode.");
//        }
    }

    protected NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (nutToInstall != null && nutToInstall.getContent().getPath() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsDefinition runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetch(installerDescriptor.getId()).setSession(session)
                            .setTransitive(false)
                            .setAcceptOptional(false)
                            .includeDependencies()
                            .setIncludeInstallInformation(true)
                            .fetchDefinition();
                }
            }
            if (runnerFile == null) {
                runnerFile = nutToInstall;
            }
            NutsInstallerComponent best = extensions().createSupported(NutsInstallerComponent.class, runnerFile);
            if (best != null) {
                return best;
            }
        }
        return new CommandForIdNutsInstallerComponent();
    }

    /**
     * return installed version
     *
     * @param id
     * @return
     */
    protected String[] getInstalledVersions(NutsId id) {
        return Arrays.stream(installedRepository.findInstalledVersions(id))
                .map(x -> x.getVersion().getValue())
                .sorted((a, b) -> DefaultNutsVersion.compareVersions(a, b))
                .toArray(String[]::new);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsId nutsId) {
        for (NutsExecutorComponent nutsExecutorComponent : extensions().createAll(NutsExecutorComponent.class)) {
            if (nutsExecutorComponent.getId().equalsSimpleName(nutsId)
                    || nutsExecutorComponent.getId().getName().equals(nutsId.toString())
                    || nutsExecutorComponent.getId().toString().equals("net.vpc.app.nuts.exec:exec-" + nutsId.toString())) {
                return nutsExecutorComponent;
            }
        }
        return new CustomNutsExecutorComponent(nutsId);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsDefinition nutsDefinition) {
        NutsExecutorComponent executorComponent = extensions().createSupported(NutsExecutorComponent.class, nutsDefinition);
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsExecutorNotFoundException(nutsDefinition.getId());
    }

    protected int exec(NutsDefinition nutToRun, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsSession session, boolean embedded) {
        security().checkAllowed(NutsConstants.RIGHT_EXEC, "exec");
        session = CoreNutsUtils.validateSession(session, this);
        if (nutToRun != null && nutToRun.getContent().getPath() != null) {
            NutsDescriptor descriptor = nutToRun.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            if (!embedded) {
                NutsExecutorDescriptor executor = descriptor.getExecutor();
                NutsExecutorComponent execComponent = null;
                List<String> executorArgs = new ArrayList<>();
                Properties execProps = null;
                if (executor == null) {
                    execComponent = resolveNutsExecutorComponent(nutToRun);
                } else {
                    if (executor.getId() == null) {
                        execComponent = resolveNutsExecutorComponent(nutToRun);
                    } else {
                        execComponent = resolveNutsExecutorComponent(executor.getId());
                    }
                    executorArgs.addAll(Arrays.asList(executor.getOptions()));
                    execProps = executor.getProperties();
                }
                executorArgs.addAll(Arrays.asList(executorOptions));
                final NutsExecutionContext executionContext = new NutsExecutionContextImpl(nutToRun,
                        appArgs, executorArgs.toArray(new String[0]),
                        env, execProps, dir, session, this, failFast, commandName);
                return execComponent.exec(executionContext);
            } else {
                JavaExecutorOptions options = new JavaExecutorOptions(
                        nutToRun, appArgs, executorOptions, dir, this, session
                );
                ClassLoader classLoader = null;
                Throwable th = null;
                try {
                    classLoader = new NutsURLClassLoader(
                            this,
                            options.getClassPath().toArray(new String[0]),
                            config().getBootClassLoader()
                    );
                    Class<?> cls = Class.forName(options.getMainClass(), true, classLoader);
                    boolean isNutsApp = false;
                    Method mainMethod = null;
                    Object nutsApp = null;
                    try {
                        mainMethod = cls.getMethod("launch", NutsWorkspace.class, String[].class);
                        mainMethod.setAccessible(true);
                        Class p = cls.getSuperclass();
                        while (p != null) {
                            if (p.getName().equals("net.vpc.app.nuts.app.NutsApplication")) {
                                isNutsApp = true;
                                break;
                            }
                            p = p.getSuperclass();
                        }
                        if (isNutsApp) {
                            isNutsApp = false;
                            nutsApp = cls.newInstance();
                            isNutsApp = true;
                        }
                    } catch (Exception rr) {
                        //ignore

                    }
                    if (isNutsApp) {
                        //NutsWorkspace
                        return ((Integer) mainMethod.invoke(nutsApp, new Object[]{this, options.getApp().toArray(new String[0])}));
                    } else {
                        //NutsWorkspace
                        mainMethod = cls.getMethod("main", String[].class);
                        mainMethod.invoke(null, new Object[]{options.getApp().toArray(new String[0])});
                    }

                } catch (MalformedURLException e) {
                    th = e;
                } catch (NoSuchMethodException e) {
                    th = e;
                } catch (SecurityException e) {
                    th = e;
                } catch (IllegalAccessException e) {
                    th = e;
                } catch (IllegalArgumentException e) {
                    th = e;
                } catch (InvocationTargetException e) {
                    th = e;
                } catch (ClassNotFoundException e) {
                    th = e;
                }
                if (th != null) {
                    throw new NutsExecutionException("Error Executing " + nutToRun.getId(), th, 204);
                }
            }
        }
        throw new NutsNotFoundException(nutToRun == null ? null : nutToRun.getId());
    }

    public NutsDependencyFilter createNutsDependencyFilter(NutsDependencyFilter filter, NutsId[] exclusions) {
        if (exclusions == null || exclusions.length == 0) {
            return filter;
        }
        return new NutsExclusionDependencyFilter(filter, exclusions);
    }

    @Override
    public Path copyTo(NutsId id, Path localPath, NutsSession session) {
        return fetch(id).setSession(session).setLocation(localPath).fetchContent().getPath();
    }

    protected NutsRepository getEnabledRepositoryOrError(String repoId) {
        NutsRepository r = config().getRepository(repoId);
        if (r != null) {
            if (!r.config().isEnabled()) {
                throw new NutsRepositoryNotFoundException("Repository " + repoId + " is disabled.");
            }
        }
        return r;
    }

    protected void checkEnabled(String repoId) {
        if (!config().getRepository(repoId).config().isEnabled()) {
            throw new NutsIllegalArgumentException("Repository " + repoId + " is disabled");
        }
    }

    private void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean resolveInstaller, boolean trace) {
        if (def == null) {
            return;
        }
        if (resolveInstaller) {
            if (installerComponent == null) {
                if (def.getContent().getPath() != null) {
                    installerComponent = getInstaller(def, session);
                }
            }
        }
        session = CoreNutsUtils.validateSession(session, this);
        boolean reinstall = def.getInstallation().isInstalled();
        PrintStream out = session.getTerminal().getFormattedOut();
        if (installerComponent != null) {
            if (def.getContent().getPath() != null) {
                NutsExecutionContext executionContext = createNutsExecutionContext(def, args, new String[0], session, true, null);
                installedRepository.install(executionContext.getNutsDefinition().getId());
                try {
                    installerComponent.install(executionContext);
//                    out.print(getFormatManager().createIdFormat().format(def.getId()) + " installed ##successfully##.\n");
                } catch (NutsReadOnlyException ex) {
                    throw ex;
                } catch (Exception ex) {
                    if (trace) {
                        out.printf(formatter().createIdFormat().toString(def.getId()) + " @@Failed@@ to install : %s.\n", ex.toString());
                    }
                    installedRepository.uninstall(executionContext.getNutsDefinition().getId());
                    throw new NutsExecutionException("Unable to install " + def.getId().toString(), ex, 1);
                }
                Path installFolder = config().getStoreLocation(def.getId(), NutsStoreLocation.PROGRAMS);
                ((DefaultNutsDefinition) def).setInstallation(new NutsInstallInfo(true, installFolder));
            }
        }
        for (NutsInstallListener nutsListener : session.getListeners(NutsInstallListener.class)) {
            nutsListener.onInstall(def, reinstall, session);
        }
        def.getInstallation().setJustInstalled(true);
        if (trace) {
            if (!def.getInstallation().isInstalled()) {
                if (!def.getContent().isCached()) {
                    if (def.getContent().isTemporary()) {
                        out.printf(formatter().createIdFormat().toString(def.getId()) + " installed ##successfully## from temporarily file %s\n", def.getContent().getPath());
                    } else {
                        out.printf(formatter().createIdFormat().toString(def.getId()) + " installed ##successfully## from remote repository\n");
                    }
                } else {
                    if (def.getContent().isTemporary()) {
                        out.printf(formatter().createIdFormat().toString(def.getId()) + " installed from local temporarily file %s \n", def.getContent().getPath());
                    } else {
                        out.printf(formatter().createIdFormat().toString(def.getId()) + " installed from local repository\n");
                    }
                }
            } else {
                out.printf(formatter().createIdFormat().toString(def.getId()) + " installed ##successfully##\n");
            }
        }
    }

    private NutsExecutionContext createNutsExecutionContext(NutsDefinition nutToInstall, String[] args, String[] executorArgs, NutsSession session, boolean failFast, String commandName) {
        if (commandName == null) {
            commandName = resolveCommandName(nutToInstall.getId());
        }
        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutorDescriptor installer = descriptor.getInstaller();
        List<String> eargs = new ArrayList<>();
        List<String> aargs = new ArrayList<>();
        Properties props = null;
        if (installer != null) {
            if (installer.getOptions() != null) {
                eargs.addAll(Arrays.asList(installer.getOptions()));
            }
            props = installer.getProperties();
        }
        if (executorArgs != null) {
            eargs.addAll(Arrays.asList(executorArgs));
        }
        if (args != null) {
            aargs.addAll(Arrays.asList(args));
        }
        Path installFolder = config().getStoreLocation(nutToInstall.getId(), NutsStoreLocation.PROGRAMS);
        Properties env = new Properties();
        return new NutsExecutionContextImpl(nutToInstall, aargs.toArray(new String[0]), eargs.toArray(new String[0]), env, props, installFolder.toString(), session, this, failFast, commandName);
    }

    private NutsId toCanonicalForm(NutsId id) {
        if (id != null) {
            id = id.setNamespace(null);
            if (NutsConstants.QUERY_FACE_DEFAULT_VALUE.equals(id.getQueryMap().get(NutsConstants.QUERY_FACE))) {
                id = id.setQueryProperty(NutsConstants.QUERY_FACE, null);
            }
        }
        return id;
    }

    public String resolveCommandName(NutsId id) {
        String nn = id.getName();
        NutsWorkspaceCommand c = config().findCommand(nn);
        if (c != null) {
            if (c.getOwner().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getName() + "-" + id.getVersion();
        c = config().findCommand(nn);
        if (c != null) {
            if (c.getOwner().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroup() + "." + id.getName() + "-" + id.getVersion();
        c = config().findCommand(nn);
        if (c != null) {
            if (c.getOwner().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        throw new NutsElementNotFoundException("Unable to resolve command name for " + id.toString());
    }

    protected boolean reloadWorkspace(NutsSession session, String[] excludedExtensions, String[] excludedRepositories) {
        Set<String> excludedExtensionsSet = excludedExtensions == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedExtensions), " ,;"));
        Set<String> excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedRepositories), " ,;"));
        session = CoreNutsUtils.validateSession(session, this);
        boolean loadedConfig = false;
        try {
            loadedConfig = configManager.load();
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Erroneous config file. Unable to load file " + configManager.getConfigFile() + " : " + ex.toString(), ex);
            if (!config().isReadOnly()) {
                Path newfile = config().getWorkspaceLocation().resolve("nuts-workspace-"
                        + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())
                        + ".json");
                log.log(Level.SEVERE, "Erroneous config file will replace by fresh one. Old config is copied to {0}", newfile);
                try {
                    Files.move(configManager.getConfigFile(), newfile);
                } catch (IOException e) {
                    throw new UncheckedIOException("Unable to load and re-create config file " + configManager.getConfigFile() + " : " + e.toString(), new IOException(ex));
                }
            } else {
                throw new UncheckedIOException("Unable to load config file " + configManager.getConfigFile(), new IOException(ex));
            }
        }
        if (loadedConfig) {
            config0().removeAllRepositories();

            //extensions already wired... this is needless!
            for (NutsId extensionId : extensions().getExtensions()) {
                if (excludedExtensionsSet != null && CoreNutsUtils.findNutsIdBySimpleNameInStrings(extensionId, excludedExtensionsSet) != null) {
                    continue;
                }
                NutsSession sessionCopy = session.copy();
                extensionManager.wireExtension(extensionId,
                        new DefaultNutsQueryOptions().setFetchStratery(NutsFetchStrategy.ONLINE)
                                .setTransitive(true),
                        sessionCopy);
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }

            for (NutsRepositoryRef ref : configManager.getRepositoryRefs()) {
                if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(ref.getName())) {
                    continue;
                }
                config0().wireRepository(config().createRepository(
                        CoreNutsUtils.refToOptions(ref), config0().getRepositoriesRoot(), null)
                );
            }

            NutsUserConfig adminSecurity = config().getUser(NutsConstants.USER_ADMIN);
            if (adminSecurity == null
                    || (StringUtils.isEmpty(adminSecurity.getAuthenticationAgent())
                    && StringUtils.isEmpty(adminSecurity.getCredentials()))) {
                if (log.isLoggable(Level.CONFIG)) {
                    log.log(Level.CONFIG, NutsConstants.USER_ADMIN + " user has no credentials. reset to default");
                }
                security().setUserAuthenticationAgent(NutsConstants.USER_ADMIN, "");
                security().setUserCredentials(NutsConstants.USER_ADMIN, "admin");
            }
            for (NutsWorkspaceCommandFactoryConfig commandFactory : configManager.getCommandFactories()) {
                try {
                    config().installCommandFactory(commandFactory, session);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Unable to instantiate Command Factory {0}", commandFactory);
                }
            }
            for (NutsWorkspaceListener listener : workspaceListeners) {
                listener.onReloadWorkspace(this);
            }
            //if save is needed, will be applied
            config().save(false);
            return true;
        }
        return false;
    }

    private List<NutsRepository> getEnabledRepositories(NutsWorkspaceHelper.FilterMode fmode, NutsId nutsId, NutsRepositoryFilter repositoryFilter, NutsSession session, NutsFetchMode mode, NutsQueryOptions options) {
        session = CoreNutsUtils.validateSession(session, this);
        return NutsWorkspaceHelper.filterRepositories(getEnabledRepositories(repositoryFilter), fmode, nutsId, repositoryFilter, session, mode, options);
    }

    public void checkSupportedRepositoryType(String type) {
        if (!config().isSupportedRepositoryType(type)) {
            throw new NutsIllegalArgumentException("Unsupported repository type " + type);
        }
    }

    private static class BootAPINutsDescriptorFilter implements NutsDescriptorFilter {

        private final String bootApiVersion;

        public BootAPINutsDescriptorFilter(String bootApiVersion) {
            this.bootApiVersion = bootApiVersion;
        }

        @Override
        public boolean accept(NutsDescriptor descriptor) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                if (dependency.getSimpleName().equals(NutsConstants.NUTS_ID_BOOT_API)) {
                    if (dependency.getVersion().matches("]" + bootApiVersion + "]")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public void addUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.addListener(listener);
    }

    @Override
    public void removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.removeListener(listener);
    }

    @Override
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return userProperties.getListeners();
    }

    @Override
    public boolean isFetched(NutsId id, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsSession offlineSession = session.copy();
        try {
            NutsDefinition found = fetch(id).offline().setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).fetchDefinition();
            return found != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configManager
                + '}';
    }

    @Override
    public NutsId resolveIdForClass(Class clazz) {
        PomId u = PomIdResolver.resolvePomId(clazz, null);
        if (u == null) {
            return null;
        }
        return parser().parseId(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public String resolveDefaultHelpForClass(Class clazz) {
        NutsId nutsId = resolveIdForClass(clazz);
        if (nutsId != null) {
            String urlPath = "/" + nutsId.getGroup().replace('.', '/') + "/" + nutsId.getName() + ".help";
            URL resource = getClass().getResource(urlPath);
            if (resource == null) {
                return null;
            }
            String s;
            try {
                s = IOUtils.loadString(resource);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if (!StringUtils.isEmpty(s)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public NutsId[] resolveIdsForClass(Class clazz) {
        PomId[] u = PomIdResolver.resolvePomIds(clazz);
        NutsId[] all = new NutsId[u.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = parser().parseId(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
    }

//    @Override
//    public String resolveRepositoryPath(String repositoryLocation) {
//        String root = repositoryManager.getRepositoriesRoot();
//        NutsWorkspaceConfigManager configManager = this.getConfigManager();
//        return CoreIOUtils.expandPath(repositoryLocation,
//                root != null ? new File(root) : CoreIOUtils.createFile(
//                        configManager.getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES),
//                configManager.getHomeLocation()).getIdPath();
//    }
    @Override
    public NutsIdBuilder createIdBuilder() {
        return new DefaultNutsIdBuilder();
    }

    @Override
    public NutsDependencyBuilder createDependencyBuilder() {
        return new DefaultNutsDependencyBuilder();
    }

    //    public NutsVesionBuilder createNutsVersionBuilder() {
//        return new DefaultVersionBuilder();
//    }
    @Override
    public NutsQuery createQuery() {
        return new DefaultNutsQuery(this);
    }

    @Override
    public String getFileName(NutsId id, String ext) {
        if (StringUtils.isEmpty(ext)) {
            ext = "jar";
        }
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        String cls = id.getClassifier();
        String alt = id.getAlternative();
        return id.getName() + "-" + id.getVersion() + ext;
    }

    @Override
    public String createRegex(String pattern) {
        return CoreStringUtils.simpexpToRegexp(pattern, true);
    }

    @Override
    public void updateRepositoryIndex(String repository) {
        NutsRepository r = this.config().getRepository(repository);
        if (r != null) {
            ((NutsFolderRepository) r).reindexFolder();
        }
    }

    @Override
    public void updateRepositoryIndex(Path path) {
        CoreNutsUtils.checkReadOnly(this);
        String nn = UUID.randomUUID().toString();
        NutsRepository r = config().createRepository(
                new NutsCreateRepositoryOptions()
                        .setName(nn)
                        .setTemporay(true)
                        .setLocation(path == null ? null : path.toString())
                        .setCreate(false), io().path(System.getProperty("user.dir")), null);
        if (r != null) {
            if (r instanceof NutsFolderRepository) {
                ((NutsFolderRepository) r).reindexFolder();
            } else {
                throw new NutsIllegalArgumentException("Repository does not supoport indexing at path " + path);
            }
        } else {
            throw new NutsIllegalArgumentException("Invalid or inaccssible path " + path);
        }
    }

    @Override
    public void updateAllRepositoryIndices() {
        CoreNutsUtils.checkReadOnly(this);
        for (NutsRepository nutsRepository : config().getRepositories()) {
            if (nutsRepository instanceof NutsFolderRepository) {
                ((NutsFolderRepository) nutsRepository).reindexFolder();
            }
        }
    }

    @Override
    public String getHelpText() {
        return this.io().getResourceString("/net/vpc/app/nuts/nuts-help.help", getClass(), "no help found");
    }

    @Override
    public String getWelcomeText() {
        return this.io().getResourceString("/net/vpc/app/nuts/nuts-welcome.help", getClass(), "no welcome found");
    }

    @Override
    public String getLicenseText() {
        return this.io().getResourceString("/net/vpc/app/nuts/nuts-license.help", getClass(), "no license found");
    }

    @Override
    public NutsClassLoaderBuilder createClassLoaderBuilder() {
        return new DefaultNutsClassLoaderBuilder(this);
    }

    @Override
    public NutsSystemTerminal getSystemTerminal() {
        return systemTerminal;
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public void setTerminal(NutsSessionTerminal terminal) {
        if (terminal == null) {
            terminal = io().createTerminal();
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal);
        }
        this.terminal = terminal;
    }

    private NutsDescriptor resolveExecProperties(NutsDescriptor nutsDescriptor, Path jar) {
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isNutsApplication();
        if (jar.getFileName().toString().toLowerCase().endsWith(".jar") && Files.isRegularFile(jar)) {
            Path f = config().getStoreLocation(nutsDescriptor.getId(), NutsStoreLocation.CACHE).resolve(config().getDefaultIdFilename(nutsDescriptor.getId().setFace("cache-info"))
            );
            Map<String, String> map = null;
            try {
                if (Files.isRegularFile(f)) {
                    map = this.io().readJson(f, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    NutsExecutionEntry[] t = this.parser().parseExecutionEntries(jar);
                    if (t.length > 0) {
                        executable = true;
                        if (t[0].isApp()) {
                            nutsApp = true;
                        }
                    }
                    try {
                        map = new LinkedHashMap<>();
                        map.put("executable", String.valueOf(executable));
                        map.put("nutsApplication", String.valueOf(nutsApp));
                        this.io().writeJson(map, f, true);
                    } catch (Exception ex) {
                        //
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        nutsDescriptor = nutsDescriptor.setExecutable(executable);
        nutsDescriptor = nutsDescriptor.setNutsApplication(nutsApp);

        return nutsDescriptor;
    }

    private static class CommandForIdNutsInstallerComponent implements NutsInstallerComponent {

        @Override
        public void install(NutsExecutionContext executionContext) {
            CoreNutsUtils.checkReadOnly(executionContext.getWorkspace());
            NutsId id = executionContext.getNutsDefinition().getId();
            NutsDescriptor descriptor = executionContext.getNutsDefinition().getDescriptor();

            if (descriptor.isNutsApplication()) {
                int r = executionContext.getWorkspace().createExecBuilder()
                        .setCommand(
                                id.setNamespace(null).toString(),
                                "--nuts-execution-mode=on-install"
                        ).addExecutorOptions()
                        .addCommand(executionContext.getArgs())
                        .exec().setFailFast().getResult();
            }
//            NutsWorkspaceConfigManager cc = executionContext.getWorkspace().getConfigManager();
//            NutsWorkspaceCommand c = cc.findCommand(id.getName());
//            if (c != null) {
//
//            } else {
//                //
//                cc.installCommand(new DefaultNutsWorkspaceCommand()
//                        .setId(id.setNamespace(""))
//                        .setName(id.getName())
//                        .setCommand(new String[0])
//                );
//            }
        }

        @Override
        public void uninstall(NutsExecutionContext executionContext, boolean deleteData) {
            CoreNutsUtils.checkReadOnly(executionContext.getWorkspace());
            NutsId id = executionContext.getNutsDefinition().getId();
            if ("jar".equals(executionContext.getNutsDefinition().getDescriptor().getPackaging())) {
                NutsExecutionEntry[] executionEntries = executionContext.getWorkspace().parser().parseExecutionEntries(executionContext.getNutsDefinition().getContent().getPath());
                for (NutsExecutionEntry executionEntry : executionEntries) {
                    if (executionEntry.isApp()) {
                        //
                        int r = executionContext.getWorkspace().createExecBuilder()
                                .setCommand(
                                        id.toString(),
                                        "--nuts-execution-mode=on-uninstall"
                                )
                                .addCommand(executionContext.getArgs())
                                .exec().getResult();
                        executionContext.getWorkspace().getTerminal().getFormattedOut().printf("Installation Exited with code : " + r);
                    }
                }
            }
//            NutsId id = executionContext.getPrivateStoreNutsDefinition().getId();
//            NutsWorkspaceConfigManager cc = executionContext.getWorkspace().getConfigManager();
//            for (NutsWorkspaceCommand command : cc.findCommands(id)) {
//                //install if installed with the very same version !!
//                if (id.getLongName().equals(command.getId().getLongName())) {
//                    cc.uninstallCommand(command.getName());
//                }
//            }
        }

        @Override
        public int getSupportLevel(NutsDefinition criteria) {
            return 0;
        }
    }

    @Override
    public NutsQueryOptions createQueryOptions() {
        return new DefaultNutsQueryOptions();
    }

    @Override
    public NutsDeploymentBuilder createDeploymentBuilder() {
        return new DefaultNutsDeploymentBuilder(this);
    }

    @Override
    public NutsIOManager io() {
        return ioManager;
    }

    @Override
    public NutsParseManager parser() {
        return parseManager;
    }

    @Override
    public NutsFormatManager formatter() {
        return formatManager;
    }

    @Override
    public NutsFetch fetch(NutsId id) {
        return new DefaultNutsFetch(this).setId(id);
    }

    @Override
    public NutsFetch fetch(String id) {
        return new DefaultNutsFetch(this).setId(id);
    }

    @Override
    public void removeRepositoryListener(NutsRepositoryListener listener) {
        repositoryListeners.add(listener);
    }

    @Override
    public void addRepositoryListener(NutsRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
    }

    @Override
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
    }
}
