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
    protected NutsWorkspaceRepositoryManagerExt repositoryManager;
    private final ObservableMap<String, Object> userProperties = new ObservableMap<>();

    private NutsSessionTerminal terminal;
    private NutsSystemTerminal systemTerminal;
    private NutsIOManager ioManager;
    private NutsParseManager parseManager;
    private NutsFormatManager formatManager;
    private DefaultNutsInstalledRepository installedRepository;

    public DefaultNutsWorkspace() {

    }

    @Override
    public String getUuid() {
        return getConfigManager().getUuid();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new NutsSessionImpl();
        nutsSession.setTerminal(getTerminal());
        return nutsSession;
    }

    @Override
    public NutsDescriptorBuilder createDescriptorBuilder() {
        return new DefaultNutsDescriptorBuilder();
    }

    @Override
    public NutsWorkspaceRepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    @Override
    public NutsWorkspaceConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public NutsWorkspaceSecurityManager getSecurityManager() {
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
        NutsWorkspaceFactory newFactory = getExtensionManager().createSupported(NutsWorkspaceFactory.class, this);
        if (newFactory == null) {
            throw new NutsExtensionMissingException(NutsWorkspaceFactory.class, "WorkspaceFactory");
        }
        NutsWorkspace nutsWorkspace = getExtensionManager().createSupported(NutsWorkspace.class, this);
        if (nutsWorkspace == null) {
            throw new NutsExtensionMissingException(NutsWorkspace.class, "Workspace");
        }
        NutsWorkspaceImpl nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
        if (nutsWorkspaceImpl.initializeWorkspace(newFactory,
                new NutsBootConfig(getConfigManager().getRunningContext()),
                new NutsBootConfig(getConfigManager().getBootContext()),
                getConfigManager().getBootClassWorldURLs(),
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
        repositoryManager = new DefaultNutsWorkspaceRepositoryManager(this);
        configManager.onInitializeWorkspace(options,
                new DefaultNutsBootContext(runningBootConfig),
                new DefaultNutsBootContext(wsBootConfig),
                bootClassWorldURLs,
                bootClassLoader == null ? Thread.currentThread().getContextClassLoader() : bootClassLoader);

        boolean exists = getConfigManager().isValidWorkspaceFolder();
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

        NutsSystemTerminalBase termb = getExtensionManager().createSupported(NutsSystemTerminalBase.class, null);

        setSystemTerminal(termb);
        setTerminal(getIOManager().createTerminal());
        NutsSession session = createSession();

        initializing = true;
        try {
            if (!reloadWorkspace(session, options.getExcludedExtensions(), options.getExcludedRepositories())) {
                CoreNutsUtils.checkReadOnly(this);
                log.log(Level.CONFIG, "Unable to load existing workspace. Creating new one at {0}", getConfigManager().getRunningContext().getWorkspace());
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
                config.setProgramsStoreLocation(options.getProgramsStoreLocation());
                //load from options with NO resolution applied
                config.setLibStoreLocation(options.getLibStoreLocation());
                config.setCacheStoreLocation(options.getCacheStoreLocation());
                config.setTempStoreLocation(options.getTempStoreLocation());
                config.setLogsStoreLocation(options.getLogsStoreLocation());
                config.setVarStoreLocation(options.getVarStoreLocation());
                config.setConfigStoreLocation(options.getConfigStoreLocation());
                configManager.setConfig(config);
                initializeWorkspace(options.getArchetype(), session);
                if (!getConfigManager().isReadOnly()) {
                    save();
                }
                reconfigurePostInstall(new NutsInstallCompanionOptions().setAsk(true).setForce(false).setTrace(true), session);
                for (NutsWorkspaceListener workspaceListener : workspaceListeners) {
                    workspaceListener.onCreateWorkspace(this);
                }
            } else {
                if (options.isRecover()) {
                    //should re
                    configManager.setBootApiVersion(wsBootConfig.getApiVersion());
                    configManager.setBootRuntime(wsBootConfig.getRuntimeId());
                    configManager.setBootRuntimeDependencies(wsBootConfig.getRuntimeDependencies());
                    configManager.setBootRepositories(wsBootConfig.getRepositories());
                    if (!getConfigManager().isReadOnly()) {
                        getConfigManager().save();
                    }
                }
            }
            if (configManager.getRepositories().length == 0) {
                initializeWorkspace(options.getArchetype(), session);
            }
            List<String> transientRepositoriesSet = options.getTransientRepositories() == null ? null : new ArrayList<>(Arrays.asList(options.getTransientRepositories()));
            for (String loc : transientRepositoriesSet) {
                String uuid = UUID.randomUUID().toString();
                getRepositoryManager()
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
                this.getSecurityManager().login(options.getLogin(), password);
            }
            configManager.setStartCreateTimeMillis(options.getCreationTime());
            configManager.setEndCreateTimeMillis(System.currentTimeMillis());
            if (!options.isReadOnly()) {
                getConfigManager().save(false);
            }
            log.log(Level.FINE, "Nuts Workspace loaded in {0}", Chronometer.formatPeriodMilli(getConfigManager().getCreationFinishTimeMillis() - getConfigManager().getCreationStartTimeMillis()));
            if (options.isPerf()) {
                getTerminal().getFormattedOut().printf("**Nuts** Workspace loaded in [[%s]]\n",
                        Chronometer.formatPeriodMilli(getConfigManager().getCreationFinishTimeMillis() - getConfigManager().getCreationStartTimeMillis())
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
        PrintStream out = terminal.getFormattedOut();
        if (options.isTrace()) {
            StringBuilder version = new StringBuilder(getConfigManager().getRunningContext().getRuntimeId().getVersion().toString());
            while (version.length() < 25) {
                version.append(' ');
            }
            out.println("{{+------------------------------------------------------------------------------+}}");
            out.println("{{|}}==``      _   __      __        ``==                                                  {{|}}");
            out.println("{{|}}==``     / | / /_  __/ /______  ``== ==N==etwork ==U==pdatable ==T==hings ==S==ervices                {{|}}");
            out.println("{{|}}==``    /  |/ / / / / __/ ___/  ``== <<The Open Source Package Manager for __Java__ (TM)>>    {{|}}");
            out.println("{{|}}==``   / /|  / /_/ / /_(__  )   ``== <<and other __things__>> ... by ==vpc==                      {{|}}");
            out.println("{{|}}==``  /_/ |_/\\\\____/\\\\__/____/``==     __http://github.com/thevpc/nuts__                    {{|}}");
            out.println("{{|}}      version [[" + version + "]]                                       {{|}}");
            out.println("{{+------------------------------------------------------------------------------+}}");
            out.println("{{|}}  This is the very {{First}} time ==Nuts== has been started for this workspace...     {{|}}");
            out.println("{{+------------------------------------------------------------------------------+}}");
            out.println();
        }
        if (!getConfigManager().getOptions().isSkipPostCreateInstallCompanionTools()) {
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
        if (getConfigManager().getOptions().isYes()) {
            //ok;
        } else if (getConfigManager().getOptions().isNo()) {
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
    public NutsDefinition install(String id, String[] args, NutsInstallOptions foundAction, NutsSession session) {
        return install(getParseManager().parseRequiredId(id), args, foundAction, session);
    }

    @Override
    public NutsDefinition install(NutsId id, String[] args, NutsInstallOptions options, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (options == null) {
            options = new NutsInstallOptions();
        }
        PrintStream out = resolveOut(session);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_INSTALL, "install");
        NutsDefinition def = fetch(id).setSession(session).setAcceptOptional(false).includeDependencies().setIncludeInstallInformation(true).fetchDefinition();
        if (def != null && def.getContent().getFile() != null) {
            if (def.getInstallation().isInstalled()) {
                if (!options.isForce()) {
                    if (options.isTrace()) {
                        out.printf(getFormatManager().createIdFormat().format(def.getId()) + " already installed\n");
                    }
                    return def;
                }
            }
            postInstall(def, args, null, session, true);
            if (options.isTrace()) {
                if (!def.getInstallation().isInstalled()) {
                    if (!def.getContent().isCached()) {
                        if (def.getContent().isTemporary()) {
                            out.printf(getFormatManager().createIdFormat().format(def.getId()) + " installed successfully from temporarily file %s\n", def.getContent().getFile());
                        } else {
                            out.printf(getFormatManager().createIdFormat().format(def.getId()) + " installed successfully from remote repository\n");
                        }
                    } else {
                        if (def.getContent().isTemporary()) {
                            out.printf(getFormatManager().createIdFormat().format(def.getId()) + " installed from local temporarily file %s \n", def.getContent().getFile());
                        } else {
                            out.printf(getFormatManager().createIdFormat().format(def.getId()) + " installed from local repository\n");
                        }
                    }
                } else {
                    out.printf(getFormatManager().createIdFormat().format(def.getId()) + " installed successfully\n");
                }
            }

        }
        return def;
    }

    @Override
    public NutsId commit(String folder, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_DEPLOY, "commit");
        if (folder == null || !new File(folder).isDirectory()) {
            throw new NutsIllegalArgumentException("Not a directory " + folder);
        }

        File file = new File(folder, NutsConstants.NUTS_DESC_FILE_NAME);
        NutsDescriptor d = getParseManager().parseDescriptor(file);
        String oldVersion = StringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - NutsConstants.VERSION_CHECKED_OUT_EXTENSION.length());
            String newVersion = getParseManager().parseVersion(oldVersion).inc().getValue();
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
            NutsId newId = deploy(createDeploymentBuilder().setContent(new File(folder)).setDescriptor(d).build(), session);
            getFormatManager().createDescriptorFormat().setPretty(true).format(d, file);
            IOUtils.delete(new File(folder));
            return newId;
        } else {
            throw new NutsUnsupportedOperationException("commit not supported");
        }
    }

    @Override
    public NutsDefinition checkout(String id, String folder, NutsSession session) {
        return checkout(getParseManager().parseRequiredId(id), folder, session);
    }

    @Override
    public NutsDefinition checkout(NutsId id, String folder, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_INSTALL, "checkout");
        NutsDefinition nutToInstall = fetch(id).setSession(session).setAcceptOptional(false).includeDependencies().fetchDefinition();
        if ("zip".equals(nutToInstall.getDescriptor().getPackaging())) {

            ZipUtils.unzip(nutToInstall.getContent().getFile(), getIOManager().expandPath(folder), new UnzipOptions().setSkipRoot(false));

            File file = new File(folder, NutsConstants.NUTS_DESC_FILE_NAME);
            NutsDescriptor d = getParseManager().parseDescriptor(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().setVersion(oldVersion + NutsConstants.VERSION_CHECKED_OUT_EXTENSION);
            d = d.setId(newId);

            getFormatManager().createDescriptorFormat().setPretty(true).format(d, file);

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
        return checkUpdates(getParseManager().parseRequiredId(id), bootApiVersion, session);
    }

    public NutsUpdate checkUpdates(NutsId id, String bootApiVersion, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsId oldId = null;
        NutsDefinition oldFile = null;
        NutsDefinition newFile = null;
        NutsId newId = null;
        NutsSession sessionOnline = session.copy().setFetchMode(NutsFetchMode.ONLINE);
        List<NutsId> dependencies = new ArrayList<>();
//        NutsSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        if (id.getSimpleName().equals(NutsConstants.NUTS_ID_BOOT_API)) {
            oldId = getConfigManager().getConfigContext().getApiId();
            NutsId confId = getConfigManager().getConfigContext().getApiId();
            if (confId != null) {
                oldId = confId;
            }
            String v = bootApiVersion;
            if (StringUtils.isEmpty(v)) {
                v = "LATEST";
            }
            try {
                oldFile = fetch(oldId).setSession(sessionOnline).fetchDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = this.fetch(NutsConstants.NUTS_ID_BOOT_API + "#" + v).setSession(sessionOnline).fetchDefinition();
                newId = newFile.getId();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        } else if (id.getSimpleName().equals(NutsConstants.NUTS_ID_BOOT_RUNTIME)) {
            oldId = getConfigManager().getRunningContext().getRuntimeId();
            NutsId confId = getConfigManager().getConfigContext().getRuntimeId();
            if (confId != null) {
                oldId = confId;
            }
            try {
                oldFile = fetch(oldId).setSession(sessionOnline).fetchDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = createQuery()
                        .addId(oldFile != null ? oldFile.getId().setVersion(null).toString() : NutsConstants.NUTS_ID_BOOT_RUNTIME)
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .setLatestVersions(true)
                        .setSession(sessionOnline)
                        .fetchFirst();
                if (newFile != null) {
                    for (NutsDefinition d : createQuery().addId(newFile.getId()).setLatestVersions(true).setSession(sessionOnline).dependenciesOnly().fetch()) {
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
                oldId = fetch(id).setIncludeEffective(true).setSession(session.setFetchMode(NutsFetchMode.OFFLINE)).fetchId();
                oldFile = fetch(oldId).setSession(session).fetchDefinition();
            } catch (Exception ex) {
                //ignore
            }
            try {
                newFile = createQuery().addId(NutsConstants.NUTS_ID_BOOT_RUNTIME)
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .setLatestVersions(true).setSession(sessionOnline).mainAndDependencies().fetchFirst();
                for (NutsDefinition d : createQuery().addId(newFile.getId()).setLatestVersions(true).setSession(sessionOnline).dependenciesOnly().fetch()) {
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
        if (cnewId != null && !cnewId.equals(coldId)) {
            String sOldFile = oldFile == null ? null : oldFile.getContent().getFile();
            String sNewFile = newFile.getContent().getFile();
            return new NutsUpdate(id, oldId, newId, sOldFile, sNewFile, dependencies.toArray(new NutsId[0]), false);
        }
        return null;
    }

    @Override
    public NutsUpdate[] checkWorkspaceUpdates(NutsWorkspaceUpdateOptions options, NutsSession session) {
        if (options == null) {
            options = new NutsWorkspaceUpdateOptions();
        }
        NutsBootContext actualBootConfig = getConfigManager().getRunningContext();
//        NutsBootContext jsonBootConfig = getConfigManager().getBootContext();
        session = CoreNutsUtils.validateSession(session, this);
        Map<String, NutsUpdate> allUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdate> extUpdates = new LinkedHashMap<>();
        NutsUpdate bootUpdate = null;
        String bootVersion = getConfigManager().getRunningContext().getApiId().getVersion().toString();
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
            for (NutsId ext : getConfigManager().getExtensions()) {
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
            if (bootUpdate != null) {
                if (bootUpdate.getAvailableId() != null) {
                    CoreNutsUtils.checkReadOnly(this);
                    NutsBootConfig bc = getConfigManager().getBootConfig();
                    bc.setApiVersion(bootUpdate.getAvailableId().getVersion().toString());
                    getConfigManager().setBootConfig(bc);
                    if (runtimeUpdate == null) {

                    }
                }
            }
            if (runtimeUpdate != null) {
                NutsBootConfig bc = getConfigManager().getBootConfig();
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
                getConfigManager().setBootConfig(bc);
            }
            for (NutsUpdate extension : extUpdates.values()) {
                getConfigManager().updateExtension(extension.getAvailableId());
            }
            if (configManager.isConfigurationChanged()) {
                getConfigManager().save(false);
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
        if (getConfigManager().getExtensions().length > 0) {
            exclude = Boolean.parseBoolean(getConfigManager().getEnv(NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION, "false"));
        }
        if (!exclude) {
            boolean coreFound = false;
            for (NutsId ext : getConfigManager().getExtensions()) {
                if (ext.equalsSimpleName(getConfigManager().getRunningContext().getRuntimeId())) {
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
        return update(getParseManager().parseRequiredId(id), uptoDateAction, session);
    }

    @Override
    public NutsUpdateResult update(NutsId id, NutsUpdateOptions options, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (options == null) {
            options = new NutsUpdateOptions();
        }
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_INSTALL, "update");
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException("Version is too restrictive. You would use fetch or install instead");
        }

        NutsUpdateResult r = new NutsUpdateResult().setId(id.getSimpleNameId());

        final PrintStream out = resolveOut(session);
        NutsDefinition d0 = fetch(id).setSession(session.copy().setFetchMode(NutsFetchMode.OFFLINE)).setAcceptOptional(false).setInstalledOnly(true).fetchDefinitionOrNull();
        NutsDefinition d1 = fetch(id).setSession(session).setAcceptOptional(false).setPreferInstalled(false).includeDependencies().fetchDefinitionOrNull();
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
                postInstall(d1, new String[0], null, session, true);
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
                        postInstall(d1, new String[0], null, session, true);
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
                    postInstall(d1, new String[0], null, session, true);
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
        return isInstalled(getParseManager().parseRequiredId(id), checkDependencies, session);
    }

    public boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsDefinition nutToInstall = null;
        try {
            nutToInstall = fetch(id).setSession(session.copy().setFetchMode(NutsFetchMode.OFFLINE).setTransitive(false)).includeDependencies(checkDependencies)
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
        return uninstall(getParseManager().parseRequiredId(id), args, options, session);
    }

    @Override
    public boolean uninstall(NutsId id, String[] args, NutsUninstallOptions options, NutsSession session) {
        CoreNutsUtils.checkReadOnly(this);
        session = CoreNutsUtils.validateSession(session, this);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_UNINSTALL, "uninstall");
        NutsDefinition nutToInstall = fetch(id).setSession(session.copy().setTransitive(false)).setAcceptOptional(false).includeDependencies()
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
            IOUtils.delete(new File(getConfigManager().getStoreLocation(id, NutsStoreFolder.PROGRAMS)));
            IOUtils.delete(new File(getConfigManager().getStoreLocation(id, NutsStoreFolder.TEMP)));
            IOUtils.delete(new File(getConfigManager().getStoreLocation(id, NutsStoreFolder.LOGS)));
            if (options.isErase()) {
                IOUtils.delete(new File(getConfigManager().getStoreLocation(id, NutsStoreFolder.VAR)));
                IOUtils.delete(new File(getConfigManager().getStoreLocation(id, NutsStoreFolder.CONFIG)));
            }
            if (options.isTrace()) {
                out.printf(getFormatManager().createIdFormat().format(id) + " uninstalled ##successfully##\n");
            }
        } else {
            if (options.isTrace()) {
                out.printf(getFormatManager().createIdFormat().format(id) + " @@could not@@ be uninstalled\n");
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
        return isFetched(getParseManager().parseRequiredId(id), session);
    }

    protected DefaultNutsDefinition fetchDescriptorAsDefinition(NutsId id, DefaultFetchOptions options, NutsSession session) {
        NutsRepositoryFilter repositoryFilter = null;
        if (session.getFetchMode() == NutsFetchMode.OFFLINE) {
            if ((options.isPreferInstalled() && id.getVersion().isEmpty()) || options.isInstalledOnly()) {
                //in this case will try to resolve latest installed version only!!
                final String[] all = getInstalledVersions(id);
                if (all.length > 0) {
                    id = id.setVersion(all[all.length - 1]);
                } else {
                    if (options.isInstalledOnly()) {
                        throw new NutsNotFoundException(id);
                    }
                }
            }
        }

        for (NutsRepository repo : getEnabledRepositories(id, repositoryFilter, session)) {
            try {
                NutsDescriptor descriptor = repo.fetchDescriptor(id, session);
                if (descriptor != null) {
                    NutsId nutsId = resolveEffectiveId(descriptor, session);
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

    public NutsDefinition fetchDefinition(NutsId id, DefaultFetchOptions options, NutsSession session) {
        long startTime = System.currentTimeMillis();
        session = CoreNutsUtils.validateSession(session, this);
        if (log.isLoggable(Level.FINEST)) {
            traceMessage(session, id, TraceResult.START, "Fetch component", 0);
        }
        DefaultNutsDefinition foundDefinition = null;
        try {
            //add env parameters to fetch adequate nuts
            id = NutsWorkspaceHelper.configureFetchEnv(id, this);

            //use
            NutsFetchMode[] nutsFetchModes = resolveFetchModes(session.getFetchMode());
            for (NutsFetchMode mode : nutsFetchModes) {
                NutsSession sessionCopy = session.copy().setFetchMode(mode);
                try {
                    if (id.getGroup() == null) {
                        String[] groups = getConfigManager().getImports();
                        for (String group : groups) {
                            try {
                                foundDefinition = fetchDescriptorAsDefinition(id.setGroup(group), options, sessionCopy);
                                if (foundDefinition != null) {
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
                                //not found
                            }
                        }
                        if (foundDefinition != null) {
                            break;
                        }
                        throw new NutsNotFoundException(id);
                    }
                    foundDefinition = fetchDescriptorAsDefinition(id, options, sessionCopy);
                    if (foundDefinition != null) {
                        break;
                    }
                } catch (NutsNotFoundException ex) {
                    //ignore
                }
            }
            if (foundDefinition != null) {
                if (options.isEffectiveDesc()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(resolveEffectiveDescriptor(foundDefinition.getDescriptor(), session));
                    } catch (NutsNotFoundException ex) {
                        //ignore
                        log.log(Level.WARNING, "Nuts Descriptor Found, but its parent is not: {0} with parent {1}", new Object[]{id.toString(), Arrays.toString(foundDefinition.getDescriptor().getParents())});
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (options.isContent() || options.isInstallInfo()) {
                        NutsId id1 = getConfigManager().createComponentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
                        String copyTo = options.getCopyTo();
                        if (StringUtils.isEmpty(copyTo)) {
                            copyTo = null;
                        } else if (new File(copyTo).isDirectory()) {
                            copyTo = new File(copyTo, getConfigManager().getDefaultIdFilename(id1)).getPath();
                        }
                        for (NutsFetchMode mode : nutsFetchModes) {
                            NutsSession sessionCopy = session.copy().setFetchMode(mode);
                            try {
                                NutsContent content1 = foundDefinition.getRepository().fetchContent(id1, copyTo, sessionCopy);
                                if (content1 != null) {
                                    foundDefinition.setContent(content1);
                                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), new File(content1.getFile())));
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
                                log.log(Level.WARNING, "Nuts Descriptor Found, but component could not be resolved : {0}", id.toString());
                            }
                        }
                        if (foundDefinition.getContent() == null || foundDefinition.getContent().getFile() == null) {
                            traceMessage(session, id, TraceResult.ERROR, "Fetched Descriptor but failed to fetch Component", startTime);
                            foundDefinition = null;
                        }
                    }
                    if (foundDefinition != null && options.isInstallInfo()) {
                        NutsInstallerComponent installer = null;
                        if (foundDefinition.getContent().getFile() != null) {
                            installer = getInstaller(foundDefinition, session);
                        }
                        if (installer != null) {
                            if (installedRepository.isInstalled(foundDefinition.getId())) {
                                foundDefinition.setInstallation(new NutsInstallInfo(true,
                                        getConfigManager().getStoreLocation(foundDefinition.getId(), NutsStoreFolder.PROGRAMS)
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
                traceMessage(session, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
        if (foundDefinition != null) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.SUCCESS, "Fetch component", startTime);
            }
            return foundDefinition;
        }
        throw new NutsNotFoundException(id);
    }

    public Iterator<NutsId> findIterator(DefaultNutsSearch search) {
        Iterator<NutsId> result = findIteratorBase(search);
        //post search operations
        if (search.isLatestVersions()) {
            List<NutsId> list = CoreNutsUtils.filterNutsIdByLatestVersion(CollectionUtils.toList(result));
            if (search.isSort()) {
                list.sort(DefaultNutsIdComparator.INSTANCE);
            }
            return list.iterator();
        } else if (search.isSort()) {
            List<NutsId> list = CollectionUtils.toList(result);
            list.sort(DefaultNutsIdComparator.INSTANCE);
            return list.iterator();
        }
        return result;
    }

    private Iterator<NutsId> findIteratorBase(DefaultNutsSearch search) {
        NutsSession session = CoreNutsUtils.validateSession(search.getSession(), this);
        NutsVersionFilter versionFilter = search.getVersionFilter();
        NutsIdFilter idFilter = search.getIdFilter();
        NutsRepositoryFilter repositoryFilter = search.getRepositoryFilter();
        NutsDescriptorFilter descriptorFilter = search.getDescriptorFilter();
        String[] goodIds = search.getIds();
        if (goodIds.length > 0) {
            IteratorList<NutsId> result = new IteratorList<>();
            for (String id : goodIds) {
                Iterator<NutsId> good = null;
                NutsId nutsId = getParseManager().parseId(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (nutsId.getGroup() == null) {
                        for (String aImport : getConfigManager().getImports()) {
                            nutsId2.add(nutsId.setGroup(aImport));
                        }
                    } else {
                        nutsId2.add(nutsId);
                    }
                    for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
                        NutsSession session2 = session.copy().setFetchMode(mode);

                        List<NutsId> all = new ArrayList<>();
                        for (NutsId nutsId1 : nutsId2) {
                            for (NutsRepository repo : getEnabledRepositories(nutsId1, repositoryFilter, session2)) {
                                if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                    try {
                                        DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(nutsId1.getQueryMap(), idFilter, versionFilter, descriptorFilter, repo, session);
                                        List<NutsId> child = repo.findVersions(nutsId1, filter, session2);
                                        all.addAll(child);
                                    } catch (NutsNotFoundException exc) {
                                        //
                                    }
                                }
                            }
                        }
                        if (!all.isEmpty()) {
                            good = all.iterator();
                            break;
                        }
                    }
                    if (good != null) {
                        result.addNonEmpty(good);
                    } else if (nutsId.getGroup() == null) {
                        //now will look with *:artifactId pattern
                        NutsQuery search2 = createQuery()
                                .addId(search.getIds())
                                .setIdFilter(search.getIdFilter())
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setVersionFilter(search.getVersionFilter())
                                .setDescriptorFilter(search.getDescriptorFilter())
                                .setSession(session)
                                .setIds();

                        search2.setIdFilter(new NutsIdFilterOr(
                                new NutsPatternIdFilter(new String[]{nutsId.setGroup("*").toString()}),
                                CoreNutsUtils.simplify(search2.getIdFilter())
                        ));
                        Iterator<NutsId> b = search2.findIterator();
                        b = CollectionUtils.nullifyIfEmpty(b);
                        if (b != null) {
                            result.addNonEmpty(b);
                        }
                    }
                }
            }
            return result;
        }

        if (idFilter instanceof NutsPatternIdFilter) {
            String[] ids = ((NutsPatternIdFilter) idFilter).getIds();
            if (ids.length == 1) {
                String id = ids[0];
                if (id.indexOf('*') < 0 && id.indexOf(':') > 0) {
                    NutsId nid = getParseManager().parseId(id);
                    if (nid != null) {

                        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
                            NutsSession session2 = session.copy().setFetchMode(mode);
                            List<NutsId> all = new ArrayList<>();
                            for (NutsRepository repo : getEnabledRepositories(repositoryFilter)) {
                                try {
                                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                        DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(nid.getQueryMap(), idFilter, versionFilter, descriptorFilter, repo, session);
                                        List<NutsId> child = repo.findVersions(nid, filter, session2);
                                        all.addAll(child);
                                    }
                                } catch (Exception exc) {
                                    //
                                }
                            }
                            if (!all.isEmpty()) {
                                return all.iterator();
                            }
                        }
                        return Collections.emptyIterator();
                    }
                }
            }
        }

        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            IteratorList<NutsId> all = new IteratorList<>();
            for (NutsRepository repo : getEnabledRepositories(repositoryFilter)) {
                try {
                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                        DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(null, idFilter, versionFilter, descriptorFilter, repo, session);
                        Iterator<NutsId> child = repo.find(filter, session2);
                        all.addNonEmpty(child);
                    }
                } catch (Exception exc) {
                    //
                }
            }
            Iterator<NutsId> b = CollectionUtils.nullifyIfEmpty(all);
            if (b != null) {
                return b;
            }
        }
        return Collections.emptyIterator();
    }

    protected void traceMessage(NutsSession session, NutsId id, TraceResult tracePhase, String message, long startTime) {
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
        String fetchString = "";
        switch (session.getFetchMode()) {
            case OFFLINE: {
                fetchString = "[OFFLINE] ";
                break;
            }
            case ONLINE: {
                fetchString = "[ONLINE ] ";
                break;
            }
            case REMOTE: {
                fetchString = "[REMOTE ] ";
                break;
            }
        }
        log.log(Level.FINEST, tracePhaseString + fetchString
                + StringUtils.alignLeft(message, 18) + " " + id + timeMessage);
    }

    @Override
    public void push(String id, String repositoryId, NutsPushOptions options, NutsSession session) {
        push(getParseManager().parseRequiredId(id), repositoryId, options, session);
    }

    @Override
    public void push(NutsId id, String repositoryId, NutsPushOptions options, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsRepositoryFilter repositoryFilter = null;
        if (StringUtils.trim(id.getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            throw new NutsIllegalArgumentException("Invalid Version " + id.getVersion());
        }
        NutsSession nonTransitiveSession = session.copy().setTransitive(false);
        NutsDefinition file = fetch(id).setSession(nonTransitiveSession).fetchDefinition();
        if (file == null) {
            throw new NutsIllegalArgumentException("Nothing to push");
        }
        if (options == null) {
            options = new NutsPushOptions();
        }
        if (StringUtils.isEmpty(repositoryId)) {
            Set<String> errors = new LinkedHashSet<>();
            for (NutsRepository repo : getEnabledRepositories(file.getId(), repositoryFilter, session)) {
                NutsDescriptor descr = null;
                try {
                    descr = repo.fetchDescriptor(file.getId(), session);
                } catch (Exception e) {
                    errors.add(StringUtils.exceptionToString(e));
                    //
                }
                if (descr != null && repo.isSupportedMirroring()) {
                    NutsId id2 = getConfigManager().createComponentFaceId(resolveEffectiveId(descr, session), descr);
                    try {
                        repo.push(id2, repositoryId, options, session);
                        return;
                    } catch (Exception e) {
                        errors.add(StringUtils.exceptionToString(e));
                        //
                    }
                }
            }
            throw new NutsRepositoryNotFoundException(repositoryId + " : " + StringUtils.join("\n", errors));
        } else {
            NutsRepository repository = getRepositoryManager().findRepository(repositoryId);
            checkEnabled(repository.getName());
            repository.deploy(file.getId(), file.getDescriptor(), file.getContent().getFile(), CoreNutsUtils.createNutsDeployOptions(options), session);
        }
    }

    @Override
    public NutsDefinition createBundle(String contentFolder, String destFile, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        File contentFolderObj = new File(contentFolder);
        if (contentFolderObj.isDirectory()) {
            NutsDescriptor descriptor = null;
            File ext = new File(contentFolder, NutsConstants.NUTS_DESC_FILE_NAME);
            if (ext.exists()) {
                descriptor = getParseManager().parseDescriptor(ext);
            } else {
                descriptor = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(contentFolderObj, null, null, null), session);
            }
            if (descriptor != null) {
                if ("zip".equals(descriptor.getPackaging())) {
                    if (destFile == null) {
                        destFile = getIOManager().expandPath(contentFolderObj.getParent()
                                + "/" + descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip");
                    }
                    ZipUtils.zip(contentFolderObj.getPath(), new ZipOptions(), destFile);
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
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession session) {
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
                NutsId p = fetch(parent).setIncludeEffective(true).setSession(session).fetchId();
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
            NutsId bestId = new DefaultNutsId(null, g, thisId.getName(), v, "");
            String bestResult = bestId.toString();
            if (StringUtils.isEmpty(g) || StringUtils.isEmpty(v)) {
                throw new NutsNotFoundException(bestResult, "Unable to fetchEffective for " + thisId + ". Best Result is " + bestResult, null);
            }
            return bestId.setAlternative(descriptor.getAlternative());
        } else {
            return thisId.setAlternative(descriptor.getAlternative());
        }
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        if (!descriptor.getId().getVersion().isEmpty() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            String l = getConfigManager().getStoreLocation(descriptor.getId(), NutsStoreFolder.CACHE);
            File eff = new File(l, "effective.nuts");
            if (eff.isFile()) {
                try {
                    NutsDescriptor d = getParseManager().parseDescriptor(eff);
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
        String l = getConfigManager().getStoreLocation(effDesc.getId(), NutsStoreFolder.CACHE);
        File eff = new File(l, "effective.nuts");
        try {
            getFormatManager().createDescriptorFormat().setPretty(true).format(effDesc, eff);
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

        File tempFile = null;
        Object content = deployment.getContent();
        InputStreamSource contentSource = IOUtils.toInputStreamSource(content, null, null, null);

        Object vdescriptor = deployment.getDescriptor();
        NutsDescriptor descriptor = null;
        if (vdescriptor != null) {
            if (NutsDescriptor.class.isInstance(vdescriptor)) {
                descriptor = (NutsDescriptor) vdescriptor;
                if (deployment.getDescSHA1() != null && !getIOManager().getSHA1(descriptor).equals(deployment.getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }
            } else if (IOUtils.isValidInputStreamSource(vdescriptor.getClass())) {
                InputStreamSource inputStreamSource = IOUtils.toInputStreamSource(vdescriptor, null, null, null);
                if (deployment.getDescSHA1() != null && !CoreSecurityUtils.evalSHA1(inputStreamSource.open(), true).equals(deployment.getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }
                descriptor = getParseManager().parseDescriptor(inputStreamSource.open(), true);
            } else {
                throw new NutsException("Unexpected type " + vdescriptor.getClass().getName());
            }
        }

        CharacterizedFile characterizedFile = null;
        File contentFile2 = null;
        try {
            if (descriptor == null) {
                characterizedFile = CoreNutsUtils.characterize(this, contentSource, session);
                if (characterizedFile.descriptor == null) {
                    throw new NutsIllegalArgumentException("Missing descriptor");
                }
                descriptor = characterizedFile.descriptor;
            }
            String name = getConfigManager().getDefaultIdFilename(descriptor.getId().setFaceDescriptor());
            tempFile = getIOManager().createTempFile(name);
            IOUtils.copy(contentSource.open(), tempFile, true, true);
            contentFile2 = tempFile;
            return deploy(contentFile2.getPath(), descriptor, deployment.getRepositoryName(), deployment.getOptions(), session);
        } finally {
            if (characterizedFile != null) {
                characterizedFile.close();
            }
            if (tempFile != null) {
                tempFile.delete();
            }
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
    public String copyTo(String id, String localPath, NutsSession session) {
        return copyTo(getParseManager().parseRequiredId(id), localPath, session);
    }

    @Override
    public NutsWorkspaceExtensionManager getExtensionManager() {
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
            for (NutsRepository repository : ws.getRepositoryManager().getRepositories()) {
                boolean ok = false;
                if (repository.isEnabled()) {
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
            if (repo.isSupportedMirroring()) {
                List<NutsRepository> subrepos = new ArrayList<>();
                boolean ok = false;
                for (NutsRepository repository : repo.getMirrors()) {
                    if (repository.isEnabled()) {
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

    public int exec(String nutsJarFile0, String[] args, boolean copyCurrentToFile, boolean waitFor, NutsSession session) {
        File nutsJarFile = nutsJarFile0 == null ? null : new File(nutsJarFile0);
        session = CoreNutsUtils.validateSession(session, this);
        PrintStream out = resolveOut(session);
        if (copyCurrentToFile) {
            File acFile = CoreIOUtils.fileByPath(getConfigManager().resolveNutsJarFile());
            if (nutsJarFile == null) {
                nutsJarFile = acFile;
            } else {
                if (acFile != null) {
                    if (!acFile.exists()) {
                        throw new NutsIllegalArgumentException("Could not apply update from non existing source " + acFile.getPath());
                    }
                    if (acFile.isDirectory()) {
                        throw new NutsIllegalArgumentException("Could not apply update from directory source " + acFile.getPath());
                    }
                    if (nutsJarFile.exists()) {
                        if (nutsJarFile.isDirectory()) {
                            throw new NutsIllegalArgumentException("Could not apply update on folder " + nutsJarFile);
                        }
                        if (nutsJarFile.exists()) {
                            int index = 1;
                            while (new File(nutsJarFile.getPath() + "." + index).exists()) {
                                index++;
                            }
                            out.printf("copying [[%s]] to [[%s.%s]]\n", nutsJarFile, nutsJarFile.getPath(), index);
                            try {
                                CoreIOUtils.copy(nutsJarFile, new File(nutsJarFile.getPath() + "." + index));
                            } catch (IOException e) {
                                throw new NutsIOException(e);
                            }
                        }
                        out.printf("copying [[%s]] to [[%s]]\n", acFile.getPath(), nutsJarFile.getPath());
                        try {
                            CoreIOUtils.copy(acFile, nutsJarFile);
                        } catch (IOException e) {
                            throw new NutsIOException(e);
                        }
                    } else if (nutsJarFile.getName().endsWith(".jar")) {
                        out.printf("copying [[%s]] to [[%s]]\n", acFile.getPath(), nutsJarFile.getPath());
                        try {
                            CoreIOUtils.copy(acFile, nutsJarFile);
                        } catch (IOException e) {
                            throw new NutsIOException(e);
                        }
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
            nutsJarFile = CoreIOUtils.fileByPath(getConfigManager().resolveNutsJarFile());
        }
        if (nutsJarFile == null) {
            throw new NutsIllegalArgumentException("Unable to locate nutsJarFile");
        }
        List<String> all = new ArrayList<>();
        all.add(CoreNutsUtils.resolveJavaCommand(null));
        if (nutsJarFile.isDirectory()) {
            all.add("-classpath");
            all.add(nutsJarFile.getPath());
        } else {
            all.add("-jar");
            all.add(nutsJarFile.getPath());
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
            throw new NutsIOException(e);
        } catch (InterruptedException e) {
            throw new NutsException(e);
        }
        return 0;
    }

    private NutsFetchMode[] resolveFetchModes(NutsFetchMode fetchMode) {
        return fetchMode == NutsFetchMode.ONLINE ? new NutsFetchMode[]{NutsFetchMode.OFFLINE, NutsFetchMode.REMOTE} : new NutsFetchMode[]{fetchMode};
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
        for (NutsWorkspaceArchetypeComponent ac : getExtensionManager().createAllSupported(NutsWorkspaceArchetypeComponent.class, this)) {
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
        getSecurityManager().setUserCredentials(NutsConstants.USER_ADMIN, "admin");

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
        if (nutToInstall != null && nutToInstall.getContent().getFile() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsDefinition runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetch(installerDescriptor.getId()).setSession(session.copy().setTransitive(false))
                            .setAcceptOptional(false)
                            .includeDependencies()
                            .setIncludeInstallInformation(true)
                            .fetchDefinition();
                }
            }
            if (runnerFile == null) {
                runnerFile = nutToInstall;
            }
            NutsInstallerComponent best = getExtensionManager().createSupported(NutsInstallerComponent.class, runnerFile);
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
        for (NutsExecutorComponent nutsExecutorComponent : getExtensionManager().createAll(NutsExecutorComponent.class)) {
            if (nutsExecutorComponent.getId().equalsSimpleName(nutsId)
                    || nutsExecutorComponent.getId().getName().equals(nutsId.toString())
                    || nutsExecutorComponent.getId().toString().equals("net.vpc.app.nuts.exec:exec-" + nutsId.toString())) {
                return nutsExecutorComponent;
            }
        }
        return new CustomNutsExecutorComponent(nutsId);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsDefinition nutsDefinition) {
        NutsExecutorComponent executorComponent = getExtensionManager().createSupported(NutsExecutorComponent.class, nutsDefinition);
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsExecutorNotFoundException(nutsDefinition.getId());
    }

    protected int exec(NutsDefinition nutToRun, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsSession session, boolean embedded) {
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_EXEC, "exec");
        session = CoreNutsUtils.validateSession(session, this);
        if (nutToRun != null && nutToRun.getContent().getFile() != null) {
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
                            options.getClassPath().toArray(new String[0]),
                            getConfigManager().getBootClassLoader()
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

    protected NutsId deploy(String contentFile0, NutsDescriptor descriptor, String repositoryId, NutsDeployOptions options, NutsSession session) {
        CoreNutsUtils.checkReadOnly(this);
        File contentFile = contentFile0 == null ? null : new File(contentFile0);
        session = CoreNutsUtils.validateSession(session, this);
        File tempFile = null;
        try {
            if (contentFile.isDirectory()) {
                File descFile = new File(contentFile, NutsConstants.NUTS_DESC_FILE_NAME);
                NutsDescriptor descriptor2;
                if (descFile.exists()) {
                    descriptor2 = getParseManager().parseDescriptor(descFile);
                } else {
                    descriptor2 = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(contentFile), session);
                }
                if (descriptor == null) {
                    descriptor = descriptor2;
                } else {
                    if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                        getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor, descFile);
                    }
                }
                if (descriptor != null) {
                    if ("zip".equals(descriptor.getPackaging())) {
                        File zipFilePath = new File(this.getIOManager().expandPath(contentFile.getPath() + ".zip"));
                        ZipUtils.zip(contentFile.getPath(), new ZipOptions(), zipFilePath.getPath());
                        contentFile = zipFilePath;
                        tempFile = contentFile;
                    } else {
                        throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else {
                if (descriptor == null) {
                    descriptor = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(contentFile), session);
                }
            }
            if (descriptor == null) {
                throw new NutsNotFoundException(" at " + contentFile);
            }
//            if (StringUtils.isEmpty(descriptor.getExt())) {
//                int r = contentFile.getName().lastIndexOf(".");
//                if (r >= 0) {
//                    descriptor = descriptor.setExt(contentFile.getName().substring(r + 1));
//                }
//            }
            //remove workspace
            descriptor = descriptor.setId(descriptor.getId().setNamespace(null));
            if (StringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
                throw new NutsIllegalArgumentException("Invalid Version " + descriptor.getId().getVersion());
            }

            NutsSession transitiveSession = session.copy().setTransitive(true);

            NutsId effId = resolveEffectiveId(descriptor, transitiveSession);
            for (String os : descriptor.getOs()) {
                CorePlatformUtils.checkSupportedOs(getParseManager().parseRequiredId(os).getSimpleName());
            }
            for (String arch : descriptor.getArch()) {
                CorePlatformUtils.checkSupportedArch(getParseManager().parseRequiredId(arch).getSimpleName());
            }
            if (StringUtils.isEmpty(repositoryId)) {
                NutsRepositoryFilter repositoryFilter = null;
                class NutsRepositoryInfo implements Comparable<NutsRepositoryInfo> {

                    NutsRepository repo;
                    int supportLevel;
                    int deployOrder;

                    @Override
                    public int compareTo(NutsRepositoryInfo o) {
                        int x = Integer.compare(o.deployOrder, this.deployOrder);
                        if (x != 0) {
                            return x;
                        }
                        x = Integer.compare(o.supportLevel, this.supportLevel);
                        if (x != 0) {
                            return x;
                        }
                        return 0;
                    }
                }
                List<NutsRepositoryInfo> possible = new ArrayList<>();
                for (NutsRepository repo : getEnabledRepositories(effId, repositoryFilter, session)) {
                    int t = 0;
                    try {
                        t = repo.getSupportLevel(effId, session);
                    } catch (Exception e) {
                        //ignore...
                    }
                    if (t > 0) {
                        NutsRepositoryInfo e = new NutsRepositoryInfo();
                        e.repo = repo;
                        e.supportLevel = t;
                        e.deployOrder = Convert.toInt(
                                repo.getConfigManager().getEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "0", false),
                                IntegerParserConfig.LENIENT);
                        possible.add(e);
                    }
                }
                if (possible.size() > 0) {
                    Collections.sort(possible);
                    return possible.get(0).repo.deploy(effId, descriptor, contentFile.getPath(), options, session);
                }
            } else {
                NutsRepository goodRepo = getEnabledRepositoryOrError(repositoryId);
                if (goodRepo == null) {
                    throw new NutsRepositoryNotFoundException(repositoryId);
                }
                return goodRepo.deploy(effId, descriptor, contentFile.getPath(), options, session);
            }
            throw new NutsRepositoryNotFoundException(repositoryId);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    @Override
    public String copyTo(NutsId id, String localPath, NutsSession session) {
        return fetch(id).setSession(session).setLocation(localPath).fetchContent().getFile();
    }

    protected NutsRepository getEnabledRepositoryOrError(String repoId) {
        NutsRepository r = getRepositoryManager().findRepository(repoId);
        if (r != null) {
            if (!r.isEnabled()) {
                throw new NutsRepositoryNotFoundException("Repository " + repoId + " is disabled.");
            }
        }
        return r;
    }

    protected void checkEnabled(String repoId) {
        if (!getRepositoryManager().findRepository(repoId).isEnabled()) {
            throw new NutsIllegalArgumentException("Repository " + repoId + " is disabled");
        }
    }

    private void postInstall(NutsDefinition nutToInstall, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean resolveInstaller) {
        if (nutToInstall == null) {
            return;
        }
        if (resolveInstaller) {
            if (installerComponent == null) {
                if (nutToInstall.getContent().getFile() != null) {
                    installerComponent = getInstaller(nutToInstall, session);
                }
            }
        }
        session = CoreNutsUtils.validateSession(session, this);
        boolean reinstall = nutToInstall.getInstallation().isInstalled();
        if (installerComponent != null) {
            if (nutToInstall.getContent().getFile() != null) {
                NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, args, new String[0], session, true, null);
                installedRepository.install(executionContext.getNutsDefinition().getId());
                try {
                    installerComponent.install(executionContext);
                    executionContext.getWorkspace().getTerminal().getFormattedOut().print(getFormatManager().createIdFormat().format(nutToInstall.getId()) + " installed ##successfully##.\n");
                } catch (NutsReadOnlyException ex) {
                    throw ex;
                } catch (Exception ex) {
                    executionContext.getWorkspace().getTerminal().getFormattedOut().printf(getFormatManager().createIdFormat().format(nutToInstall.getId()) + " @@Failed@@ to install : %s.\n", ex.toString());
                    installedRepository.uninstall(executionContext.getNutsDefinition().getId());
                    throw new NutsExecutionException("Unable to install " + nutToInstall.getId().toString(), ex, 1);
                }
                String installFolder = getConfigManager().getStoreLocation(nutToInstall.getId(), NutsStoreFolder.PROGRAMS);
                ((DefaultNutsDefinition) nutToInstall).setInstallation(new NutsInstallInfo(true, installFolder));
            }
        }
        for (NutsInstallListener nutsListener : session.getListeners(NutsInstallListener.class)) {
            nutsListener.onInstall(nutToInstall, reinstall, session);
        }
        nutToInstall.getInstallation().setJustInstalled(true);
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
        String installFolder = getConfigManager().getStoreLocation(nutToInstall.getId(), NutsStoreFolder.PROGRAMS);
        Properties env = new Properties();
        return new NutsExecutionContextImpl(nutToInstall, aargs.toArray(new String[0]), eargs.toArray(new String[0]), env, props, installFolder, session, this, failFast, commandName);
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
        NutsWorkspaceCommand c = getConfigManager().findCommand(nn);
        if (c != null) {
            if (c.getOwner().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getName() + "-" + id.getVersion();
        c = getConfigManager().findCommand(nn);
        if (c != null) {
            if (c.getOwner().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroup() + "." + id.getName() + "-" + id.getVersion();
        c = getConfigManager().findCommand(nn);
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
            if (!getConfigManager().isReadOnly()) {
                File newfile = CoreIOUtils.createFile(getConfigManager().getWorkspaceLocation(), "nuts-workspace-"
                        + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())
                        + ".json");
                log.log(Level.SEVERE, "Erroneous config file will replace by fresh one. Old config is copied to {0}", newfile.getPath());
                try {
                    CoreIOUtils.move(configManager.getConfigFile(), newfile
                    );
                } catch (IOException e) {
                    throw new NutsIOException("Unable to load and re-create config file " + configManager.getConfigFile() + " : " + e.toString(), ex);
                }
            } else {
                throw new NutsIOException("Unable to load config file " + configManager.getConfigFile(), ex);
            }
        }
        if (loadedConfig) {
            repositoryManager.removeAllRepositories();

            //extensions already wired... this is needless!
            for (NutsId extensionId : getConfigManager().getExtensions()) {
                if (excludedExtensionsSet != null && CoreNutsUtils.findNutsIdBySimpleNameInStrings(extensionId, excludedExtensionsSet) != null) {
                    continue;
                }
                NutsSession sessionCopy = session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE);
                extensionManager.wireExtension(extensionId, sessionCopy);
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }

            for (NutsRepositoryRef ref : configManager.getRepositories()) {
                if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(ref.getName())) {
                    continue;
                }
                repositoryManager.wireRepository(repositoryManager.createRepository(
                        CoreNutsUtils.refToOptions(ref), repositoryManager.getRepositoriesRoot(), null)
                );
            }

            NutsUserConfig adminSecurity = getConfigManager().getUser(NutsConstants.USER_ADMIN);
            if (adminSecurity == null
                    || (StringUtils.isEmpty(adminSecurity.getAuthenticationAgent())
                    && StringUtils.isEmpty(adminSecurity.getCredentials()))) {
                if (log.isLoggable(Level.CONFIG)) {
                    log.log(Level.CONFIG, NutsConstants.USER_ADMIN + " user has no credentials. reset to default");
                }
                getSecurityManager().setUserAuthenticationAgent(NutsConstants.USER_ADMIN, "");
                getSecurityManager().setUserCredentials(NutsConstants.USER_ADMIN, "admin");
            }
            for (NutsWorkspaceCommandFactoryConfig commandFactory : configManager.getCommandFactories()) {
                try {
                    getConfigManager().installCommandFactory(commandFactory, session);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Unable to instantiate Command Factory {0}", commandFactory);
                }
            }
            for (NutsWorkspaceListener listener : workspaceListeners) {
                listener.onReloadWorkspace(this);
            }
            //if save is needed, will be applied
            save(false);
            return true;
        }
        return false;
    }

    private List<NutsRepository> getEnabledRepositories(NutsId nutsId, NutsRepositoryFilter repositoryFilter, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        return NutsWorkspaceHelper.filterRepositories(getEnabledRepositories(repositoryFilter), nutsId, repositoryFilter, session);
    }

    public void checkSupportedRepositoryType(String type) {
        if (!getRepositoryManager().isSupportedRepositoryType(type)) {
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
        NutsSession offlineSession = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        try {
            NutsDefinition found = fetch(id).setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).fetchDefinition();
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
        return getParseManager().parseId(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
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
            String s = IOUtils.loadString(resource);
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
            all[i] = getParseManager().parseId(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
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
        return id.getName() + "-" + id.getVersion() + ext;
    }

    @Override
    public String createRegex(String pattern, boolean contains) {
        return CoreStringUtils.simpexpToRegexp(pattern, contains);
    }

    @Override
    public void updateRepositoryIndex(String path) {
        CoreNutsUtils.checkReadOnly(this);
        if (path.contains("/") || path.contains("\\")) {
            String nn = UUID.randomUUID().toString();
            NutsRepository r = repositoryManager.createRepository(
                    new NutsCreateRepositoryOptions()
                            .setName(nn)
                            .setTemporay(true)
                            .setLocation(path)
                            .setCreate(false), System.getProperty("user.dir"), null);
            if (r != null) {
                if (r instanceof NutsFolderRepository) {
                    ((NutsFolderRepository) r).reindexFolder();
                } else {
                    throw new NutsIllegalArgumentException("Repository does not supoport indexing at path " + path);
                }
            } else {
                throw new NutsIllegalArgumentException("Invalid or inaccssible path " + path);
            }
        } else {
            NutsRepository r = this.getRepositoryManager().findRepository(path);
            if (r != null) {
                ((NutsFolderRepository) r).reindexFolder();
            }
        }
    }

    @Override
    public void updateAllRepositoryIndices() {
        CoreNutsUtils.checkReadOnly(this);
        for (NutsRepository nutsRepository : getRepositoryManager().getRepositories()) {
            if (nutsRepository instanceof NutsFolderRepository) {
                ((NutsFolderRepository) nutsRepository).reindexFolder();
            }
        }
    }

    public String getHelpText() {
        return this.getIOManager().getResourceString("/net/vpc/app/nuts/nuts-help.help", getClass(), "no help found");
    }

    public String getWelcomeText() {
        return this.getIOManager().getResourceString("/net/vpc/app/nuts/nuts-welcome.help", getClass(), "no welcome found");
    }

    @Override
    public String getLicenseText() {
        return this.getIOManager().getResourceString("/net/vpc/app/nuts/nuts-license.help", getClass(), "no license found");
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
            terminal = getIOManager().createTerminal();
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal);
        }
        this.terminal = terminal;
    }

    private NutsDescriptor resolveExecProperties(NutsDescriptor nutsDescriptor, File jar) {
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isNutsApplication();
        if (jar.getName().toLowerCase().endsWith(".jar") && jar.isFile()) {
            File f = new File(getConfigManager().getStoreLocation(nutsDescriptor.getId(), NutsStoreFolder.CACHE),
                    "components" + File.separator + jar.getName() + ".info"
            );
            Map<String, String> map = null;
            try {
                if (f.isFile()) {
                    map = this.getIOManager().readJson(f, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    NutsExecutionEntry[] t = this.getParseManager().parseExecutionEntries(jar);
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
                        this.getIOManager().writeJson(map, f, true);
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
                NutsExecutionEntry[] executionEntries = executionContext.getWorkspace().getParseManager().parseExecutionEntries(new File(executionContext.getNutsDefinition().getContent().getFile()));
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
    public NutsDeploymentBuilder createDeploymentBuilder() {
        return new DefaultNutsDeploymentBuilder(this);
    }

    @Override
    public NutsIOManager getIOManager() {
        return ioManager;
    }

    @Override
    public NutsParseManager getParseManager() {
        return parseManager;
    }

    @Override
    public NutsFormatManager getFormatManager() {
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
    public void save() {
        getConfigManager().save();
        NutsException error = null;
        for (NutsRepository repo : getRepositoryManager().getRepositories()) {
            try {
                repo.save();
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }
    }

    @Override
    public void save(boolean force) {
        getConfigManager().save(force);
        NutsException error = null;
        for (NutsRepository repo : getRepositoryManager().getRepositories()) {
            try {
                repo.save(force);
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }
    }
}
