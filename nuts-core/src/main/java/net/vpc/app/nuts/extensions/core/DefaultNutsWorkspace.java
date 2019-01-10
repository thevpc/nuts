/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsQuery;
import net.vpc.app.nuts.extensions.DefaultNutsDeploymentBuilder;
import net.vpc.app.nuts.extensions.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.extensions.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.extensions.filters.dependency.NutsExclusionDependencyFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.extensions.filters.id.NutsPatternIdFilter;
import net.vpc.app.nuts.extensions.repos.NutsFolderRepository;
import net.vpc.app.nuts.extensions.terminals.UnmodifiableTerminal;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.common.io.*;
import net.vpc.common.mvn.PomId;
import net.vpc.common.mvn.PomIdResolver;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Chronometer;
import net.vpc.common.util.CollectionUtils;
import net.vpc.common.util.IteratorList;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsWorkspace implements NutsWorkspace, NutsWorkspaceImpl {

    public static final Logger log = Logger.getLogger(DefaultNutsWorkspace.class.getName());
    private NutsDefinition nutsComponentId;
    private final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    private boolean initializing;
    protected final DefaultNutsWorkspaceSecurityManager securityManager = new DefaultNutsWorkspaceSecurityManager(this);
    protected final DefaultNutsWorkspaceConfigManager configManager = new DefaultNutsWorkspaceConfigManager(this);
    protected DefaultNutsWorkspaceExtensionManager extensionManager;
    protected final DefaultNutsWorkspaceRepositoryManager repositoryManager = new DefaultNutsWorkspaceRepositoryManager(this);
    private ObservableMap<String, Object> userProperties = new ObservableMap<String, Object>();

    private NutsTerminal terminal;
    private NutsSystemTerminal systemTerminal;
    private NutsIOManager ioManager;
    private NutsParseManager parseManager;
    private NutsFormatManager formatManager;

    public DefaultNutsWorkspace() {

    }

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
        if (options.isNoColors()) {
            nutsWorkspace.getUserProperties().put("no-colors", "true");
        }
        NutsWorkspaceImpl nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
        if (nutsWorkspaceImpl.initializeWorkspace(newFactory,
                new NutsBootConfig(configManager.getRunningContext()),
                new NutsBootConfig(configManager.getBootContext()),
                configManager.getBootClassWorldURLs(),
                configManager.getBootClassLoader(), options.copy().setIgnoreIfFound(true))) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "workspace created");
            }
        }
        return nutsWorkspace;
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
        if (options.isNoColors()) {
            this.getUserProperties().put("no-colors", "true");
        }
        ioManager = new DefaultNutsIOManager(this);
        parseManager = new DefaultNutsParseManager(this);
        formatManager = new DefaultNutsFormatManager(this);
        extensionManager = new DefaultNutsWorkspaceExtensionManager(this, factory);
        configManager.onInitializeWorkspace(options,
                new DefaultNutsBootContext(runningBootConfig),
                new DefaultNutsBootContext(wsBootConfig),
                bootClassWorldURLs,
                bootClassLoader == null ? Thread.currentThread().getContextClassLoader() : bootClassLoader);

        boolean exists = configManager.isValidWorkspaceFolder();
        if (!options.isCreateIfNotFound() && !exists) {
            throw new NutsWorkspaceNotFoundException(runningBootConfig.getWorkspace());
        }
        if (!options.isIgnoreIfFound() && exists) {
            throw new NutsWorkspaceAlreadyExistsException(runningBootConfig.getWorkspace());
        }

        extensionManager.onInitializeWorkspace(bootClassLoader);

        NutsSystemTerminalBase termb = getExtensionManager().createSupported(NutsSystemTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionMissingException(NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        this.systemTerminal = new DefaultSystemTerminal(termb);
        this.systemTerminal.install(this);

        setTerminal(getIOManager().createTerminal());
        NutsSession session = createSession();

        initializing = true;
        try {
            if (!reloadWorkspace(options.isSaveIfCreated(), session, options.getExcludedExtensions(), options.getExcludedRepositories())) {
                if (!options.isCreateIfNotFound()) {
                    throw new NutsWorkspaceNotFoundException(runningBootConfig.getWorkspace());
                }
                CoreNutsUtils.checkReadOnly(this);
                exists = false;
                NutsWorkspaceConfig config = new NutsWorkspaceConfig();
                config.setBootApiVersion(wsBootConfig.getApiVersion());
                config.setBootRuntime(wsBootConfig.getRuntimeId());
                config.setBootRuntimeDependencies(wsBootConfig.getRuntimeDependencies());
                config.setBootRepositories(wsBootConfig.getRepositories());
                config.setBootJavaCommand(wsBootConfig.getJavaCommand());
                config.setBootJavaOptions(wsBootConfig.getJavaOptions());
                configManager.setConfig(config);
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    getConfigManager().save();
                }
                if (log.isLoggable(Level.CONFIG)) {
                    log.log(Level.CONFIG, "workspace created. running post creation configurator...");
                }
                reconfigurePostInstall(true, false, false, session);
                for (NutsWorkspaceListener workspaceListener : workspaceListeners) {
                    workspaceListener.onCreateWorkspace(this);
                }
            } else if (configManager.getConfig().getRepositories().length == 0) {
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    getConfigManager().save();
                }
            }
            List<String> transientRepositoriesSet = options.getTransientRepositories() == null ? null : new ArrayList<>(Arrays.asList(options.getTransientRepositories()));
            for (String s : transientRepositoriesSet) {
                getRepositoryManager().addRepository(UUID.randomUUID().toString(), s, null, false);
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
            log.log(Level.FINE, "Nuts Workspace loaded in {0}", Chronometer.formatPeriodMilli(configManager.getCreationFinishTimeMillis() - configManager.getCreationStartTimeMillis()));
            if (options.isPerf()) {
                getTerminal().getFormattedOut().printf("**Nuts** Workspace loaded in [[%s]]\n",
                        Chronometer.formatPeriodMilli(configManager.getCreationFinishTimeMillis() - configManager.getCreationStartTimeMillis())
                );
            }
        } finally {
            initializing = false;
        }
        return !exists;
    }

    public void reconfigurePostInstall(boolean ask, boolean force, boolean silent, NutsSession session) {
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "post install configuration...");
        }
        session = CoreNutsUtils.validateSession(session, this);
        PrintStream out = terminal.getFormattedOut();
        if (!silent) {
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
        NutsQuestion<Boolean> q = NutsQuestion.forBoolean("Would you like to install recommended companion tools").setDefautValue(true);
        if (!ask || terminal.ask(q)) {
            installCompanionTools(force, silent, session);
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
    public void installCompanionTools(boolean force, boolean silent, NutsSession session) {
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.FINE, "installing companion tools...");
        }
        PrintStream out = terminal.getFormattedOut();
        int companionCount = 0;
        if (!silent) {
            out.println("Installation of Nuts companion tools...");
        }
//        NutsCommandExecBuilder e = createExecBuilder();
//        e.addCommand("net.vpc.app.nuts.toolbox:ndi","install","-f");
        for (String companionTool : getCompanionTools()) {
            if (force || !isInstalled(companionTool, false, session)) {
                if (!silent) {
                    String d=fetch(companionTool,null).getDescriptor().getDescription();
                    out.printf("##\\### installing ==%s== (%s)...\n", companionTool, d);
                }
                if (log.isLoggable(Level.CONFIG)) {
                    log.log(Level.FINE, "Installing companion tool : {0}", companionTool);
                }
                install(companionTool, new String[]{"--!silent", "--force"}, NutsConfirmAction.FORCE, session);
                companionCount++;
            }
//            e.addCommand(companionTool.getId());
        }
        if (companionCount > 0) {
//            e.exec();
            if (!silent) {
                out.printf("Installation of ==%s== companion tools ##succeeded##...\n", companionCount);
            }
        }
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.FINE, "Installed {0} companion tools...", companionCount);
        }
    }

    @Override
    public NutsDefinition fetchApiDefinition(NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (nutsComponentId == null) {
            nutsComponentId = fetch(NutsConstants.NUTS_ID_BOOT_API, session);
        }
        return nutsComponentId;
    }

    @Override
    public NutsDefinition install(String id, String[] args, NutsConfirmAction foundAction, NutsSession session) {
        return install(getParseManager().parseRequiredId(id), args, foundAction, session);
    }

    @Override
    public NutsDefinition install(NutsId id, String[] args, NutsConfirmAction foundAction, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (foundAction == null) {
            foundAction = NutsConfirmAction.ERROR;
        }
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_INSTALL, "install");
        NutsDefinition nutToInstall = fetchWithDependencies(id, null, false, session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            if (nutToInstall.isInstalled()) {
                switch (foundAction) {
                    case ERROR: {
                        throw new NutsAlreadytInstalledException(nutToInstall.getId());
                    }
                    case IGNORE: {
                        return nutToInstall;
                    }
                }
            }
            NutsInstallerComponent installer = null;
            if (nutToInstall.getFile() != null) {
                installer = getInstaller(nutToInstall, session);
            }
            postInstall(nutToInstall, args, installer, session);
        }
        return nutToInstall;
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
                newVersionFound = fetch(d.getId().setVersion(newVersion), session);
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            if (newVersionFound == null) {
                d = d.setId(d.getId().setVersion(newVersion));
            } else {
                d = d.setId(d.getId().setVersion(oldVersion + ".1"));
            }
            NutsId newId = deploy(createDeploymentBuilder().setContent(new File(folder)).setDescriptor(d).build(), session);
            getFormatManager().createDescriptorFormat().setPretty(true).format(d,file);
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
        NutsDefinition nutToInstall = fetchWithDependencies(id, null, false, session);
        if ("zip".equals(nutToInstall.getDescriptor().getExt())) {

            ZipUtils.unzip(nutToInstall.getFile(), getIOManager().resolvePath(folder), new UnzipOptions().setSkipRoot(false));

            File file = new File(folder, NutsConstants.NUTS_DESC_FILE_NAME);
            NutsDescriptor d = getParseManager().parseDescriptor(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().setVersion(oldVersion + NutsConstants.VERSION_CHECKED_OUT_EXTENSION);
            d = d.setId(newId);

            getFormatManager().createDescriptorFormat().setPretty(true).format(d,file);

            return new NutsDefinition(
                    newId,
                    d,
                    folder,
                    false,
                    false,
                    null,
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
                oldFile = fetch(oldId, sessionOnline);
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = this.fetch(NutsConstants.NUTS_ID_BOOT_API + "#" + v, sessionOnline);
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
                oldFile = fetch(oldId, sessionOnline);
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
                for (NutsDefinition d : createQuery().addId(newFile.getId()).setLatestVersions(true).setSession(sessionOnline).dependenciesOnly().fetch()) {
                    dependencies.add(d.getId());
                }

            } catch (NutsNotFoundException ex) {
                //ignore
            }
            if (newFile != null) {
                newId = newFile.getId();
            }
        } else {
            try {
                oldId = fetchDescriptor(id, true, session.setFetchMode(NutsFetchMode.OFFLINE)).getId();
                oldFile = fetch(oldId, session);
            } catch (Exception ex) {
                //ignore
            }
            try {
                newFile = createQuery().addId(NutsConstants.NUTS_ID_BOOT_RUNTIME)
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .setLatestVersions(true).setSession(sessionOnline).includeDependencies().fetchFirst();
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
            String sOldFile = oldFile == null ? null : oldFile.getFile();
            String sNewFile = newFile.getFile();
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

            runtimeUpdate = checkUpdates(actualBootConfig.getRuntimeId().getSimpleName(), bootVersion, session);
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
        if (options.isLogUpdates()) {
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
                            StringUtils.alignLeft(update.getAvailableId().getSimpleName(), widthCol1),
                            StringUtils.alignLeft(update.getLocalId().getVersion().toString(), widthCol2),
                            update.getAvailableId().getVersion().toString());
                }
            }
        }
        if (!allUpdates.isEmpty() && options.isApplyUpdates()) {
            boolean requireSave = false;
            if (bootUpdate != null) {
                if (bootUpdate.getAvailableIdFile() != null && bootUpdate.getOldIdFile() != null) {
                    CoreNutsUtils.checkReadOnly(this);
                    NutsBootConfig bc = getConfigManager().getBootConfig();
                    bc.setApiVersion(bootUpdate.getAvailableId().getVersion().toString());
                    getConfigManager().setBootConfig(bc);
                    requireSave = true;
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
                requireSave = true;
            }
            for (NutsUpdate extension : extUpdates.values()) {
                getConfigManager().updateExtension(extension.getAvailableId());
                requireSave = true;
            }
            if (requireSave) {
                getConfigManager().save();
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
        List<URLLocation> bootUrls = new ArrayList<>();
        for (URLLocation r : extensionManager.getExtensionURLLocations(id, NutsConstants.NUTS_ID_BOOT_API, "properties")) {
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
        for (URLLocation bootUrl : bootUrls) {
            log.log(Level.CONFIG, "Inaccessible runtime info URL : {0}", bootUrl.getPath());
        }
        throw new NutsIllegalArgumentException("Inaccessible runtime info : " + bootUrls);
    }

    @Override
    public NutsDefinition[] update(String[] toUpdateIds, String[] toRetainDependencies, NutsConfirmAction foundAction, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        Map<String, NutsDefinition> all = new HashMap<>();
        for (String id : new HashSet<>(Arrays.asList(toUpdateIds))) {
            NutsDefinition updated = update(id, foundAction, session);
            all.put(updated.getId().getSimpleName(), updated);
        }
        if (toRetainDependencies != null) {
            for (String d : new HashSet<>(Arrays.asList(toRetainDependencies))) {
                NutsDependency dd = CoreNutsUtils.parseNutsDependency(d);
                if (all.containsKey(dd.getLongName())) {
                    NutsDefinition updated = all.get(dd.getLongName());
                    if (!dd.getVersion().toFilter().accept(updated.getId().getVersion())) {
                        throw new NutsIllegalArgumentException(dd + " unsatisfied  : " + updated.getId().getVersion());
                    }
                }
            }
        }
        return all.values().toArray(new NutsDefinition[0]);
    }

    @Override
    public NutsDefinition update(String id, NutsConfirmAction uptoDateAction, NutsSession session) {
        return update(getParseManager().parseRequiredId(id), uptoDateAction, session);
    }

    @Override
    public NutsDefinition update(NutsId id, NutsConfirmAction uptoDateAction, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_INSTALL, "update");
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException("Version is too restrictive. You would use fetch or install instead");
        }
        NutsDefinition nutToInstall = fetchWithDependencies(id, null, false, session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            boolean requiredUpdate = !nutToInstall.isInstalled() && !nutToInstall.isCached();
            if (!requiredUpdate) {
                switch (uptoDateAction) {
                    case ERROR: {
                        throw new NutsAlreadytInstalledException(nutToInstall.getId());
                    }
                    case IGNORE: {
                        return nutToInstall;
                    }
                }
            }
            //TODO should consider postUpdate
            postInstall(nutToInstall, new String[0], getInstaller(nutToInstall, session), session);
        }
        return nutToInstall;
    }

    @Override
    public boolean isInstalled(String id, boolean checkDependencies, NutsSession session) {
        return isInstalled(getParseManager().parseRequiredId(id), checkDependencies, session);
    }

    @Override
    public boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsDefinition nutToInstall = null;
        try {
            if (checkDependencies) {
                nutToInstall = fetchWithDependencies(id, null, false, session.copy().setFetchMode(NutsFetchMode.OFFLINE).setTransitive(false));
            } else {
                nutToInstall = fetch(id, session.copy().setFetchMode(NutsFetchMode.OFFLINE).setTransitive(false));
            }
        } catch (Exception e) {
            return false;
        }
        return isInstalled(nutToInstall, session);
    }

    @Override
    public boolean uninstall(String id, String[] args, NutsConfirmAction notFoundAction, boolean deleteData, NutsSession session) {
        return uninstall(getParseManager().parseRequiredId(id), args, notFoundAction, deleteData, session);
    }

    @Override
    public boolean uninstall(NutsId id, String[] args, NutsConfirmAction notFoundAction, boolean deleteData, NutsSession session) {
        CoreNutsUtils.checkReadOnly(this);
        session = CoreNutsUtils.validateSession(session, this);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_UNINSTALL, "uninstall");
        NutsDefinition nutToInstall = fetchWithDependencies(id, null, false, session.copy().setTransitive(false));
        if (!isInstalled(nutToInstall, session)) {
            throw new NutsIllegalArgumentException(id + " Not Installed");
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        if (ii == null) {
            return false;
        }
//        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, args, new String[0], session, true, null);
        ii.uninstall(executionContext, deleteData);
        IOUtils.delete(new File(getConfigManager().getStoreLocation(id, StoreFolder.PROGRAMS)));
        IOUtils.delete(new File(getConfigManager().getStoreLocation(id, StoreFolder.TEMP)));
        IOUtils.delete(new File(getConfigManager().getStoreLocation(id, StoreFolder.LOGS)));
        IOUtils.delete(new File(getConfigManager().getStoreLocation(id, StoreFolder.VAR)));
        IOUtils.delete(new File(getConfigManager().getStoreLocation(id, StoreFolder.CONFIG)));
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

    @Override
    public NutsDefinition fetchWithDependencies(String id, NutsDependencyScope[] scopes, boolean includeOptional, NutsSession session) {
        return fetchWithDependencies(getParseManager().parseRequiredId(id), null, false, session);
    }

    @Override
    public NutsDefinition fetchWithDependencies(NutsId id, NutsDependencyScope[] scopes, boolean includeOptional, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsDefinition fetched = fetch(id, session);
        if (scopes == null || scopes.length == 0) {
            scopes = new NutsDependencyScope[]{NutsDependencyScope.PROFILE_RUN};
        }
        createQuery().addId(fetched.getId()).setSession(session)
                .addScope(scopes)
                .setIncludeOptional(includeOptional)
                .dependenciesOnly().fetch();
        return fetched;
    }

    @Override
    public NutsDefinition fetch(String id, NutsSession session) {
        return fetch(getParseManager().parseRequiredId(id), session);
    }

    @Override
    public NutsDefinition fetch(NutsId id, NutsSession session) {
        return fetchSimple(id, session);
    }

    @Override
    public NutsId resolveId(String id, NutsSession session) {
        NutsId nutsId = getParseManager().parseRequiredId(id);
        return resolveId(nutsId, session);
    }

    protected NutsId resolveId0(NutsId id, NutsSession session) {
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsRepository repo : getEnabledRepositories(id, repositoryFilter, session)) {
            try {
                NutsDescriptor child = repo.fetchDescriptor(id, session);
                if (child != null) {
                    NutsIdBuilder id2 = child.getId().builder();
                    if (StringUtils.isEmpty(id2.getNamespace())) {
                        id2.setNamespace(repo.getRepositoryId());
                    }
                    //inherit classifier from requested id
                    id2.setClassifier(id.getClassifier());
                    return id2.build();
                }
            } catch (NutsNotFoundException exc) {
                //
            }
        }
        throw new NutsNotFoundException(id);
    }

    public NutsId resolveId(NutsId id, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        //add env parameters to fetch adequate nuts
        id = NutsWorkspaceHelper.configureFetchEnv(id, this);

        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession sessionCopy = session.copy().setFetchMode(mode);
            try {
                if (id.getGroup() == null) {
                    String[] groups = getConfigManager().getImports();
                    for (String group : groups) {
                        try {
                            NutsId f = resolveId0(id.setGroup(group), sessionCopy);
                            if (f != null) {
                                return f;
                            }
                        } catch (NutsNotFoundException ex) {
                            //not found
                        }
                    }
                    throw new NutsNotFoundException(id);
                }
                return resolveId0(id, sessionCopy);
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        }
        throw new NutsNotFoundException(id);
    }

    public Iterator<NutsId> findIterator(DefaultNutsSearch search) {
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
//                                .setIdFilter(search.getDependencyFilter())
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setVersionFilter(search.getVersionFilter())
                                .setDescriptorFilter(search.getDescriptorFilter())
//                                .setScope(search.getScope())
                                .setLatestVersions(search.isLatestVersions())
                                .setSort(search.isSort())
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
            if (search.isLatestVersions()) {
                return CoreNutsUtils.filterNutsIdByLatestVersion(CollectionUtils.toList(result)).iterator();
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
                                if (search.isLatestVersions()) {
                                    return CoreNutsUtils.filterNutsIdByLatestVersion(all).iterator();
                                }
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
                if (search.isLatestVersions()) {
                    return CoreNutsUtils.filterNutsIdByLatestVersion(CollectionUtils.toList(b)).iterator();
                }
                return b;
            }
        }
        return Collections.emptyIterator();
    }

    @Override
    public NutsDescriptor fetchDescriptor(String idString, boolean effective, NutsSession session) {
        return fetchDescriptor(getParseManager().parseRequiredId(idString), effective, session);
    }

    @Override
    public NutsDescriptor fetchDescriptor(NutsId id, boolean effective, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        long startTime = System.currentTimeMillis();
        if (log.isLoggable(Level.FINEST)) {
            traceMessage(session, id, TraceResult.START, "Fetch descriptor", 0);
        }
        try {
            NutsDescriptor v = fetchDescriptor0(id, effective, session);
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.SUCCESS, "Fetch descriptor", startTime);
            }
            return v;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.ERROR, "Fetch descriptor", startTime);
            }
            throw ex;
        }
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

    protected NutsDescriptor fetchDescriptor0(NutsId id, boolean effective, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        id = NutsWorkspaceHelper.configureFetchEnv(id, this);
        Set<String> errors = new LinkedHashSet<>();
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            try {
                if (id.getGroup() == null) {
                    String[] groups = getConfigManager().getImports();
                    for (String group : groups) {
                        try {
                            NutsDescriptor f = fetchDescriptor0(id.setGroup(group), effective, session2);
                            if (f != null) {
                                return f;
                            }
                        } catch (NutsNotFoundException exc) {
                            errors.add(StringUtils.exceptionToString(exc));
                            //not found
                        }
                    }
                    throw new NutsNotFoundException(id);
                }

                for (NutsRepository repo : getEnabledRepositories(id, repositoryFilter, session2)) {
                    try {
                        NutsDescriptor child = repo.fetchDescriptor(id, session2);
                        if (child != null) {
//                            if (StringUtils.isEmpty(child.getId().getNamespace())) {
//                                child = child.setId(child.getId().setNamespace(repo.getRepositoryId()));
//                            }
                            if (effective) {
                                try {
                                    return resolveEffectiveDescriptor(child, session2);
                                } catch (NutsNotFoundException ex) {
                                    if (log.isLoggable(Level.FINE)) {
                                        log.log(Level.FINE, "Unable to resolve Effective descriptor for " + id);
                                    }
                                    //ignore
                                }
                            } else {
                                return child;
                            }
                        }
                    } catch (NutsNotFoundException exc) {
                        //
                    }
                }
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        }
        throw new NutsNotFoundException(id, StringUtils.join("\n", errors), null);
    }

    @Override
    public String fetchHash(String id, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsId nutsId = getParseManager().parseRequiredId(id);
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(nutsId, repositoryFilter, session2)) {
                try {
                    String hash = repo.fetchHash(nutsId, session2);
                    if (hash != null) {
                        return hash;
                    }
                } catch (NutsNotFoundException exc) {
                    //
                }
            }
        }
        return null;
    }

    @Override
    public String fetchDescriptorHash(String id, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsId nutsId = getParseManager().parseRequiredId(id);
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(nutsId, repositoryFilter, session2)) {
                try {
                    String hash = repo.fetchDescriptorHash(nutsId, session2);
                    if (hash != null) {
                        return hash;
                    }
                } catch (NutsNotFoundException exc) {
                    //
                }
            }
        }
        return null;
    }

    @Override
    public void push(String id, String repositoryId, NutsConfirmAction foundAction, NutsSession session) {
        push(getParseManager().parseRequiredId(id), repositoryId, foundAction, session);
    }

    @Override
    public void push(NutsId id, String repositoryId, NutsConfirmAction foundAction, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsRepositoryFilter repositoryFilter = null;
        if (StringUtils.trim(id.getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            throw new NutsIllegalArgumentException("Invalid Version " + id.getVersion());
        }
        NutsSession nonTransitiveSession = session.copy().setTransitive(false);
        NutsDefinition file = fetch(id, nonTransitiveSession);
        if (file == null) {
            throw new NutsIllegalArgumentException("Nothing to push");
        }
        if (StringUtils.isEmpty(repositoryId)) {
            Set<String> errors = new LinkedHashSet<>();
            for (NutsRepository repo : getEnabledRepositories(file.getId(), repositoryFilter, session)) {
                NutsDefinition id2 = null;
                try {
                    id2 = repo.fetch(file.getId(), session);
                } catch (Exception e) {
                    errors.add(StringUtils.exceptionToString(e));
                    //
                }
                if (id2 != null && repo.isSupportedMirroring()) {
                    try {
                        repo.push(id, repositoryId, foundAction, session);
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
            checkEnabled(repository.getRepositoryId());
            repository.deploy(file.getId(), file.getDescriptor(), file.getFile(), foundAction, session);
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
                descriptor = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(contentFolderObj, null, null, new File(getConfigManager().getCwd())), session);
            }
            if (descriptor != null) {
                if ("zip".equals(descriptor.getExt())) {
                    if (destFile == null) {
                        destFile = getIOManager().resolvePath(contentFolderObj.getParent()
                                + "/" + descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip");
                    }
                    ZipUtils.zip(contentFolderObj.getPath(), new ZipOptions(), destFile);
                    return new NutsDefinition(
                            descriptor.getId(),
                            descriptor,
                            destFile,
                            true,
                            false,
                            null,
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
                NutsId p = resolveEffectiveId(fetchDescriptor(parent, false, session), session);
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
        session = CoreNutsUtils.validateSession(session, this);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetchDescriptor(parents[i], false, session),
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
            if (
                    StringUtils.isEmpty(d.getScope())
                            || d.getVersion().isEmpty()
                            || StringUtils.isEmpty(d.getOptional())
            ) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : nutsDescriptor.getStandardDependencies()) {
                    if (standardDependency.getSimpleName().equals(d.getId().getSimpleName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (
                            StringUtils.isEmpty(d.getScope())
                                    &&
                                    !StringUtils.isEmpty(standardDependencyOk.getScope())
                    ) {
                        someChange = true;
                        d = d.setScope(standardDependencyOk.getScope());
                    }
                    if (
                            StringUtils.isEmpty(d.getOptional())
                                    &&
                                    !StringUtils.isEmpty(standardDependencyOk.getOptional())
                    ) {
                        someChange = true;
                        d = d.setOptional(standardDependencyOk.getOptional());
                    }
                    if (
                            d.getVersion().isEmpty()
                                    &&
                                    !standardDependencyOk.getVersion().isEmpty()
                    ) {
                        someChange = true;
                        d = d.setVersion(standardDependencyOk.getVersion());
                    }
                }
            }

            if ("import".equals(d.getScope())) {
                someChange = true;
                for (NutsDependency dependency : fetchDescriptor(d.getId(), true, session).getDependencies()) {
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
        InputStreamSource contentSource = IOUtils.toInputStreamSource(content, null, null, new File(this.getConfigManager().getCwd()));

        Object vdescriptor = deployment.getDescriptor();
        NutsDescriptor descriptor = null;
        if (vdescriptor != null) {
            if (NutsDescriptor.class.isInstance(vdescriptor)) {
                descriptor = (NutsDescriptor) vdescriptor;
                if (deployment.getDescSHA1() != null && !getIOManager().getSHA1(descriptor).equals(deployment.getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }
            } else if (IOUtils.isValidInputStreamSource(vdescriptor.getClass())) {
                InputStreamSource inputStreamSource = IOUtils.toInputStreamSource(vdescriptor, null, null, new File(this.getConfigManager().getCwd()));
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
            tempFile = CoreIOUtils.createTempFile(descriptor, false);
            IOUtils.copy(contentSource.open(), tempFile, true, true);
            contentFile2 = tempFile;
            return deploy(contentFile2.getPath(), descriptor, deployment.getRepositoryId(), deployment.getFoundAction(), session);
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
    public String copyTo(String id, String localPath, NutsSession session) {
        return copyTo(getParseManager().parseRequiredId(id), session, localPath);
    }

    public NutsWorkspaceExtensionManager getExtensionManager() {
        return extensionManager;
    }

    public List<NutsRepository> getEnabledRepositories(NutsRepositoryFilter repositoryFilter) {
        List<NutsRepository> repos = new ArrayList<>();
        for (NutsRepository repository : getRepositoryManager().getRepositories()) {
            if (isEnabledRepository(repository.getRepositoryId())) {
                if (repositoryFilter == null || repositoryFilter.accept(repository)) {
                    repos.add(repository);
                }
            }
        }
        Collections.sort(repos, SpeedRepositoryComparator.INSTANCE);
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
                                Files.copy(nutsJarFile.toPath(), new File(nutsJarFile.getPath() + "." + index).toPath());
                            } catch (IOException e) {
                                throw new NutsIOException(e);
                            }
                        }
                        out.printf("copying [[%s]] to [[%s]]\n", acFile.getPath(), nutsJarFile.getPath());
                        try {
                            Files.copy(acFile.toPath(), nutsJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new NutsIOException(e);
                        }
                    } else if (nutsJarFile.getName().endsWith(".jar")) {
                        out.printf("copying [[%s]] to [[%s]]\n", acFile.getPath(), nutsJarFile.getPath());
                        try {
                            Files.copy(acFile.toPath(), nutsJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
        all.add(System.getProperty("java.home") + "/bin/java");
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
        session = CoreNutsUtils.validateSession(session, this);
        return (session == null || session.getTerminal() == null) ? this.getIOManager().createNullPrintStream() : session.getTerminal().getOut();
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
        NutsWorkspaceArchetypeComponent instance=null;
        TreeSet<String> validValues=new TreeSet<>();
        for (NutsWorkspaceArchetypeComponent ac : getExtensionManager().createAllSupported(NutsWorkspaceArchetypeComponent.class, this)) {
            if(archetype.equals(ac.getName())){
                instance=ac;
                break;
            }
            validValues.add(ac.getName());
        }
        if (instance == null) {
            //get the default implementation
            throw new NutsException("Invalid archetype "+archetype+". Valid values are : "+validValues);
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
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsDefinition runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetchWithDependencies(installerDescriptor.getId(), null, false, session.copy().setTransitive(false));
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

    protected boolean isInstalled(NutsDefinition nutToInstall, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
//        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
//        if (ii == null) {
//            return true;
//        }
        NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, new String[0], new String[0], session, true, nutToInstall.getId().getSimpleName());
        File installFolder = new File(executionContext.getWorkspace().getConfigManager().getStoreLocation(executionContext.getNutsDefinition().getId(), StoreFolder.PROGRAMS));
        File log = new File(installFolder, ".nuts-install.log");
        return log.exists();
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsId nutsId) {
        for (NutsExecutorComponent nutsExecutorComponent : getExtensionManager().createAll(NutsExecutorComponent.class)) {
            if (nutsExecutorComponent.getId().equalsSimpleName(nutsId)) {
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

    protected int exec(NutsDefinition nutToRun, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failSafe, NutsSession session) {
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_EXEC, "exec");
        session = CoreNutsUtils.validateSession(session, this);
        if (nutToRun != null && nutToRun.getFile() != null) {
            NutsDescriptor descriptor = nutToRun.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
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
                    appArgs, executorArgs.toArray(new String[0])
                    , env, execProps, dir, session, this, failSafe, commandName);
            return execComponent.exec(executionContext);
        }
        throw new NutsNotFoundException(nutToRun.getId());
    }

    public NutsDependencyFilter createNutsDependencyFilter(NutsDependencyFilter filter, NutsId[] exclusions) {
        if (exclusions == null || exclusions.length == 0) {
            return filter;
        }
        return new NutsExclusionDependencyFilter(filter, exclusions);
    }

    protected NutsId deploy(String contentFile0, NutsDescriptor descriptor, String repositoryId, NutsConfirmAction foundAction, NutsSession session) {
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
                    descriptor2 = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(new File(contentFile, getConfigManager().getCwd())), session);
                }
                if (descriptor == null) {
                    descriptor = descriptor2;
                } else {
                    if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                        getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor,descFile);
                    }
                }
                if (descriptor != null) {
                    if ("zip".equals(descriptor.getExt())) {
                        File zipFilePath = new File(this.getIOManager().resolvePath(contentFile.getPath() + ".zip"));
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
            if (StringUtils.isEmpty(descriptor.getExt())) {
                int r = contentFile.getName().lastIndexOf(".");
                if (r >= 0) {
                    descriptor = descriptor.setExt(contentFile.getName().substring(r + 1));
                }
            }
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
                        e.deployOrder = CoreStringUtils.parseInt(repo.getConfigManager().getEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "0", false), 0);
                        possible.add(e);
                    }
                }
                if (possible.size() > 0) {
                    Collections.sort(possible);
                    return possible.get(0).repo.deploy(effId, descriptor, contentFile.getPath(), foundAction, session);
                }
            } else {
                NutsRepository goodRepo = getEnabledRepositoryOrError(repositoryId);
                if (goodRepo == null) {
                    throw new NutsRepositoryNotFoundException(repositoryId);
                }
                return goodRepo.deploy(effId, descriptor, contentFile.getPath(), foundAction, session);
            }
            throw new NutsRepositoryNotFoundException(repositoryId);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    protected String copyTo(NutsId id, NutsSession session, String localPath) {
        session = CoreNutsUtils.validateSession(session, this);
        id = resolveId(id, session);
//        id = configureFetchEnv(id);
        Set<String> errors = new LinkedHashSet<>();
        NutsSession transitiveSession = session.copy().setTransitive(true);
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(id, repositoryFilter, transitiveSession)) {
                try {
                    String fetched = null;
                    try {
                        fetched = repo.copyTo(id, localPath, session2);
                    } catch (SecurityException ex) {
                        //ignore
                    }
                    if (fetched != null) {
                        return fetched;
                    } else {
                        errors.add(StringUtils.exceptionToString(new NutsNotFoundException(id)));
                    }
                } catch (Exception ex) {
                    errors.add(StringUtils.exceptionToString(ex));
                }
            }
        }
        throw new NutsNotFoundException(id, StringUtils.join("\n", errors), null);
    }

    protected NutsRepository getEnabledRepositoryOrError(String repoId) {
        NutsRepository r = getRepositoryManager().findRepository(repoId);
        if (r != null) {
            if (!isEnabledRepository(repoId)) {
                throw new NutsRepositoryNotFoundException("Repository " + repoId + " is disabled.");
            }
        }
        return r;
    }

    protected boolean isEnabledRepository(String repoId) {
        return getConfigManager().isRepositoryEnabled(repoId);
    }

    protected void checkEnabled(String repoId) {
        if (!isEnabledRepository(repoId)) {
            throw new NutsIllegalArgumentException("Repository " + repoId + " is disabled");
        }
    }


    private boolean isInstallable(NutsDefinition nutToInstall, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsInstallerComponent nutsInstallerComponent = getInstaller(nutToInstall, session);
            if (nutsInstallerComponent == null) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void postInstall(NutsDefinition nutToInstall, String[] args, NutsInstallerComponent installerComponent, NutsSession session) {
        if (nutToInstall == null) {
            return;
        }
        session = CoreNutsUtils.validateSession(session, this);
        boolean reinstall = nutToInstall.isInstalled();
        if (installerComponent != null) {
            if (nutToInstall.getFile() != null) {
                NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, args, new String[0], session, true, null);
                setInstalled(executionContext);
                try {
                    installerComponent.install(executionContext);
                    executionContext.getWorkspace().getTerminal().getFormattedOut().print(getFormatManager().createIdFormat().format(nutToInstall.getId()) + " installed ##successfully##.\n");
                } catch (NutsReadOnlyException ex) {
                    throw ex;
                } catch (Exception ex) {
                    executionContext.getWorkspace().getTerminal().getFormattedOut().printf(getFormatManager().createIdFormat().format(nutToInstall.getId()) + " @@Failed@@ to install : %s.\n", ex.toString());
                    File installFolder = new File(executionContext.getWorkspace().getConfigManager().getStoreLocation(executionContext.getNutsDefinition().getId(), StoreFolder.PROGRAMS));
                    File log = new File(installFolder, ".nuts-install.log");
                    if (log.isFile()) {
                        log.delete();
                    }
                    throw new NutsExecutionException("Unable to install " + nutToInstall.getId().toString(), ex, 1);
                }
                String installFolder = getConfigManager().getStoreLocation(nutToInstall.getId(), StoreFolder.PROGRAMS);
                nutToInstall.setInstallFolder(installFolder);
            }
        }
        for (NutsInstallListener nutsListener : session.getListeners(NutsInstallListener.class)) {
            nutsListener.onInstall(nutToInstall, reinstall, session);
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
        String installFolder = getConfigManager().getStoreLocation(nutToInstall.getId(), StoreFolder.PROGRAMS);
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

    public NutsDefinition fetchSimple(NutsId id, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (log.isLoggable(Level.FINEST)) {
            traceMessage(session, id, TraceResult.START, "Fetch component", 0);
        }
        long startTime = System.currentTimeMillis();
        try {
            LinkedHashSet<String> errors = new LinkedHashSet<>();
            NutsDefinition main = null;
            NutsId goodId = resolveId(id, session);

            String ns = goodId.getNamespace();
            if (!StringUtils.isEmpty(ns)) {
                for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
                    if (main != null) {
                        break;
                    }
                    NutsSession session2 = session.copy().setFetchMode(mode);
                    try {
                        List<NutsRepository> enabledRepositories = new ArrayList<>();
                        if (!StringUtils.isEmpty(ns)) {
                            try {
                                NutsRepository repository = getRepositoryManager().findRepository(ns);
                                if (repository != null) {
                                    enabledRepositories.add(repository);
                                }
                            } catch (NutsRepositoryNotFoundException ex) {
                                //
                            }
                        }
                        main = fetchHelperNutsDefinition(goodId, errors, session2, enabledRepositories);
                    } catch (NutsNotFoundException ex) {
                        //
                    }
                }
            }

            //try to load component from all repositories
            if (main == null) {
                main = fetchBestHelperNutsDefinition(session, goodId);
            }
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.SUCCESS, "Fetch component", startTime);
            }
            if (main != null) {
                NutsInstallerComponent installer = null;
                if (main.getFile() != null) {
                    installer = getInstaller(main, session);
                }
                if (installer != null) {
                    if (isInstalled(main, session)) {
                        main.setInstalled(true);
                        String installFolder = getConfigManager().getStoreLocation(main.getId(), StoreFolder.PROGRAMS);
                        main.setInstallFolder(installFolder);
                    } else {
                        main.setInstalled(false);
                        main.setInstallFolder(null);
                    }
                } else {
                    main.setInstalled(true);
                    main.setInstallFolder(null);
                }
            }
            if (main != null) {
                Map<String, String> q = id.getQueryMap();
                if (!CoreNutsUtils.isDefaultScope(q.get("scope"))) {
                    main.setScope(q.get("scope"));
                    main.setId(main.getId().setQueryProperty("scope", q.get("scope")));
                }
                if (q.get("optional") != null) {
                    main.setOptional(q.get("optional"));
                    main.setId(main.getId().setQueryProperty("optional", q.get("optional")));
                }
            }
            return main;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
    }

    public String resolveCommandName(NutsId id) {
        String nn = id.getName();
        NutsWorkspaceCommand c = getConfigManager().findCommand(nn);
        if (c != null) {
            if (c.getId().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getName() + "-" + id.getVersion();
        c = getConfigManager().findCommand(nn);
        if (c != null) {
            if (c.getId().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroup() + "." + id.getName() + "-" + id.getVersion();
        c = getConfigManager().findCommand(nn);
        if (c != null) {
            if (c.getId().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        throw new NutsElementNotFoundException("Unable to resolve command name for " + id.toString());
    }


    protected boolean reloadWorkspace(boolean save, NutsSession session, String[] excludedExtensions, String[] excludedRepositories) {
        Set<String> excludedExtensionsSet = excludedExtensions == null ? null : new HashSet<String>(CoreStringUtils.split(Arrays.asList(excludedExtensions), " ,;"));
        Set<String> excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<String>(CoreStringUtils.split(Arrays.asList(excludedRepositories), " ,;"));
        session = CoreNutsUtils.validateSession(session, this);
        File file = CoreIOUtils.createFile(getConfigManager().getWorkspaceLocation(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        NutsWorkspaceConfig config = file.isFile() ? getIOManager().readJson(file, NutsWorkspaceConfig.class) : null;
        if (config != null) {
            repositoryManager.removeAllRepositories();
            configManager.setConfig(config);

            //extensions already wired... this is needless!
            for (NutsId extensionId : config.getExtensions()) {
                if (excludedExtensionsSet != null && CoreNutsUtils.findNutsIdBySimpleNameInStrings(extensionId, excludedExtensionsSet) != null) {
                    continue;
                }
                NutsSession sessionCopy = session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE);
                extensionManager.wireExtension(extensionId, sessionCopy);
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }

            for (NutsRepositoryLocation repositoryConfig : config.getRepositories()) {
                if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(repositoryConfig.getId())) {
                    continue;
                }
                repositoryManager.openRepository(repositoryConfig.getId(), repositoryConfig.getLocation(), repositoryConfig.getType(), repositoryManager.getRepositoriesRoot(), true);
            }

            NutsUserConfig adminSecurity = getConfigManager().getUser(NutsConstants.USER_ADMIN);
            if (adminSecurity == null ||

                    (StringUtils.isEmpty(adminSecurity.getAuthenticationAgent())
                            && StringUtils.isEmpty(adminSecurity.getCredentials())
                    )
            ) {
                if (log.isLoggable(Level.CONFIG)) {
                    log.log(Level.CONFIG, NutsConstants.USER_ADMIN + " user has no credentials. reset to default");
                }
                getSecurityManager().setUserAuthenticationAgent(NutsConstants.USER_ADMIN, "");
                getSecurityManager().setUserCredentials(NutsConstants.USER_ADMIN, "admin");
                if (save) {
                    getConfigManager().save();
                }
            }
            if (config.getCommandFactories() != null) {
                for (NutsWorkspaceCommandFactoryConfig commandFactory : config.getCommandFactories().toArray(new NutsWorkspaceCommandFactoryConfig[0])) {
                    try {
                        getConfigManager().installCommandFactory(commandFactory);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Unable to instantiate Command Factory " + commandFactory);
                    }
                }
            }
            for (NutsWorkspaceListener listener : workspaceListeners) {
                listener.onReloadWorkspace(this);
            }
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
    public void addUserPropertyListener(MapListener<String, Object> listener) {
        userProperties.addListener(listener);
    }

    @Override
    public void removeUserPropertyListener(MapListener<String, Object> listener) {
        userProperties.removeListener(listener);
    }

    @Override
    public MapListener<String, Object>[] getUserPropertyListeners() {
        return userProperties.getListeners();
    }


    @Override
    public boolean isFetched(NutsId id, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsSession offlineSession = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        try {
            NutsDefinition found = fetch(id, offlineSession);
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
//        return CoreIOUtils.resolvePath(repositoryLocation,
//                root != null ? new File(root) : CoreIOUtils.createFile(
//                        configManager.getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES),
//                configManager.getHomeLocation()).getPath();
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
            NutsFolderRepository r = new NutsFolderRepository(
                    "temp",
                    path,
                    this,
                    null
            );
            r.getConfigManager().setComponentsLocation(".");
            ((NutsFolderRepository) r).reindexFolder();
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
    public NutsTerminal getTerminal() {
        return terminal;
    }

    @Override
    public void setTerminal(NutsTerminal terminal) {
        if (terminal == null) {
            terminal = getIOManager().createTerminal();
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal);
        }
        this.terminal = terminal;
    }


    private NutsDefinition fetchHelperNutsDefinition(NutsId id, LinkedHashSet<String> errors, NutsSession session2, List<NutsRepository> enabledRepositories) {
        NutsDefinition found = null;
        try {
            for (NutsRepository repo : enabledRepositories) {
                NutsDefinition fetch = null;
                try {
                    fetch = repo.fetch(id, session2);
                } catch (Exception ex) {
                    errors.add(StringUtils.exceptionToString(ex));
                }
                if (fetch != null) {
                    NutsIdBuilder b = fetch.getId().builder();
                    if (StringUtils.isEmpty(fetch.getId().getNamespace())) {
                        b.setNamespace(repo.getRepositoryId());
                    }
                    String classifier = id.getClassifier();
                    if (!StringUtils.isEmpty(classifier)) {
                        b.setClassifier(classifier);
                    }
                    fetch.setId(b.build());
                    found = fetch;
                    break;
                }
            }
        } catch (NutsNotFoundException ex) {
            //
        }
        return found;
    }

    private NutsId fetchHelperNutsId(NutsId id, LinkedHashSet<String> errors, NutsSession session2, List<NutsRepository> enabledRepositories) {
        NutsId found = null;
        try {
            for (NutsRepository repo : enabledRepositories) {
                NutsId fetch = null;
                try {
                    fetch = repo.fetchDescriptor(id, session2).getId();
                } catch (Exception ex) {
                    errors.add(StringUtils.exceptionToString(ex));
                }
                if (fetch != null) {
                    if (StringUtils.isEmpty(fetch.getNamespace())) {
                        fetch = fetch.setNamespace(repo.getRepositoryId());
                    }
                    found = fetch;
                    break;
                }
            }
        } catch (NutsNotFoundException ex) {
            //
        }
        return found;
    }

    public NutsDefinition fetchBestHelperNutsDefinition(NutsSession session, NutsId id) {
        LinkedHashSet<String> errors = new LinkedHashSet<>();
        NutsDefinition main = null;
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsFetchMode mode : this.resolveFetchModes(session.getFetchMode())) {
            if (main != null) {
                break;
            }
            NutsSession session2 = session.copy().setFetchMode(mode);
            main = fetchHelperNutsDefinition(id, errors, session2, getEnabledRepositories(id, repositoryFilter, session2.copy().setTransitive(true)));
        }
        if (main == null) {
            throw new NutsNotFoundException(id, StringUtils.join("\n", errors), null);
        }
        return main;
    }

    public NutsId fetchBestHelperNutsId(NutsSession session, NutsId id) {
        LinkedHashSet<String> errors = new LinkedHashSet<>();
        NutsId main = null;
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsFetchMode mode : this.resolveFetchModes(session.getFetchMode())) {
            if (main != null) {
                break;
            }
            NutsSession session2 = session.copy().setFetchMode(mode);
            main = fetchHelperNutsId(id, errors, session2, getEnabledRepositories(id, repositoryFilter, session2.copy().setTransitive(true)));
        }
        if (main == null) {
            throw new NutsNotFoundException(id, StringUtils.join("\n", errors), null);
        }
        return main;
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
                NutsExecutionEntry[] executionEntries = CorePlatformUtils.parseMainClasses(new File(executionContext.getNutsDefinition().getFile()));
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

    protected void setInstalled(NutsExecutionContext executionContext) {
        CoreNutsUtils.checkReadOnly(executionContext.getWorkspace());
        File installFolder = new File(executionContext.getWorkspace().getConfigManager().getStoreLocation(executionContext.getNutsDefinition().getId(), StoreFolder.PROGRAMS));
        File log = new File(installFolder, ".nuts-install.log");
        try {
            CoreNutsUtils.copy(new ByteArrayInputStream(String.valueOf(new Date()).getBytes()), log, true, true);
        } catch (NutsIOException ex) {
            throw new NutsNotInstallableException(executionContext.getNutsDefinition().getId().toString(), "Unable to install "
                    + executionContext.getNutsDefinition().getId().setNamespace(null) + " : " + ex.getMessage(), ex);
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
}


