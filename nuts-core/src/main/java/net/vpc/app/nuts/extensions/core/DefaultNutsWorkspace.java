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
import net.vpc.app.nuts.NullOutputStream;
import net.vpc.app.nuts.NutsQuery;
import net.vpc.app.nuts.extensions.archetypes.DefaultNutsWorkspaceArchetypeComponent;
import net.vpc.app.nuts.extensions.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.extensions.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.extensions.filters.dependency.NutsExclusionDependencyFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.extensions.filters.id.NutsPatternIdFilter;
import net.vpc.app.nuts.extensions.repos.NutsFolderRepository;
import net.vpc.app.nuts.extensions.terminals.DefaultNutsTerminal;
import net.vpc.app.nuts.extensions.terminals.NutsDefaultFormattedPrintStream;
import net.vpc.app.nuts.extensions.terminals.NutsTerminalDelegate;
import net.vpc.app.nuts.extensions.terminals.UnmodifiableTerminal;
import net.vpc.common.fprint.parser.FormattedPrintStreamParser;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.common.io.*;
import net.vpc.common.mvn.PomId;
import net.vpc.common.mvn.PomIdResolver;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.CollectionUtils;
import net.vpc.common.util.IteratorList;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.And;

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
    private NutsId platformOs;
    private NutsId platformArch;
    private NutsId platformOsdist;
    private String platformOsLibPath;
    private ObservableMap<String, Object> userProperties = new ObservableMap<String, Object>();

    private NutsTerminal terminal;

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
    public InputStream createNullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public PrintStream createNullPrintStream() {
        return createPrintStream(NullOutputStream.INSTANCE, false);
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

    @Override
    public JsonIO getJsonIO() {
        return CoreJsonUtils.get();
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
        NutsWorkspace nutsWorkspace = getExtensionManager().createSupported(NutsWorkspace.class, this);
        if (options.isNoColors()) {
            nutsWorkspace.getUserProperties().put("no-colors", "true");
        }
        NutsWorkspaceImpl nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
        if (nutsWorkspaceImpl.initializeWorkspace(newFactory,
                configManager.getBootConfig(), configManager.getWorkspaceBootConfig(), options.getWorkspace(),
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
                                       NutsBootConfig actualBootConfig, NutsBootConfig wsBootConfig,
                                       String workspace,
                                       URL[] bootClassWorldURLs, ClassLoader bootClassLoader,
                                       NutsWorkspaceOptions options) {

        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        extensionManager = new DefaultNutsWorkspaceExtensionManager(this, factory);
        String home=options.getHome();
        configManager.onInitializeWorkspace(options,StringUtils.isEmpty(home) ? NutsConstants.DEFAULT_NUTS_HOME : home,
                factory,
                parseId(NutsConstants.NUTS_ID_BOOT_API + "#" + actualBootConfig.getBootAPIVersion()),
                parseId(actualBootConfig.getBootRuntime()),

                parseId(NutsConstants.NUTS_ID_BOOT_API + "#" + wsBootConfig.getBootAPIVersion()),
                parseId(wsBootConfig.getBootRuntime()),

                resolveWorkspacePath(workspace),
                bootClassWorldURLs,
                bootClassLoader == null ? Thread.currentThread().getContextClassLoader() : bootClassLoader);

        boolean exists = isWorkspaceFolder(configManager.getWorkspaceLocation());
        if (!options.isCreateIfNotFound() && !exists) {
            throw new NutsWorkspaceNotFoundException(workspace);
        }
        if (!options.isIgnoreIfFound() && exists) {
            throw new NutsWorkspaceAlreadyExistsException(workspace);
        }

        extensionManager.onInitializeWorkspace(bootClassLoader);

        setTerminal(createTerminal());
        NutsSession session = createSession();

        initializing = true;
        try {
            if (!reloadWorkspace(options.isSaveIfCreated(), session, options.getExcludedExtensions(), options.getExcludedRepositories())) {
                if (!options.isCreateIfNotFound()) {
                    throw new NutsWorkspaceNotFoundException(workspace);
                }
                exists = false;
                NutsWorkspaceConfig config = new NutsWorkspaceConfig();
                config.setBootAPIVersion(wsBootConfig.getBootAPIVersion());
                config.setBootRuntime(wsBootConfig.getBootRuntime());
                config.setBootRuntimeDependencies(wsBootConfig.getBootRuntimeDependencies());
                config.setBootRepositories(wsBootConfig.getBootRepositories());
                config.setBootJavaCommand(wsBootConfig.getBootJavaCommand());
                config.setBootJavaOptions(wsBootConfig.getBootJavaOptions());
                configManager.setConfig(config);
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    getConfigManager().save();
                }
                autoConfigPostInstall(session, "");
                for (NutsWorkspaceListener workspaceListener : workspaceListeners) {
                    workspaceListener.onCreateWorkspace(this);
                }
            } else if (configManager.getConfig().getRepositories().length == 0) {
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    getConfigManager().save();
                }
            }
        } finally {
            initializing = false;
        }
        return !exists;
    }

    public void autoConfigPostInstall(NutsSession session, String autoConfig) {
        if (autoConfig == null) {
            autoConfig = "ask";
        }
        boolean configure;
        boolean ask ;
        boolean silent ;
        switch (autoConfig) {
            case "silent": {
                configure = true;
                ask = false;
                silent = true;
                break;
            }
            case "run": {
                configure = true;
                ask = false;
                silent = false;
                break;
            }
            case "skip": {
                configure = false;
                ask = false;
                silent = false;
                break;
            }
            case "ask": {
                configure = true;
                ask = true;
                silent = false;
                break;
            }
            default: {
                configure = true;
                ask = true;
                silent = false;
                break;
            }
        }
        if (configure) {
            reconfigurePostInstall(session, ask, silent);
        }
    }

    public void reconfigurePostInstall(NutsSession session, boolean ask, boolean silent) {
        session = CoreNutsUtils.validateSession(session, this);
        PrintStream out = terminal.getFormattedOut();
        if (!silent) {
            out.println(
                    "==``    _   __      __        \n" +
                            "   / | / /_  __/ /______    \n" +
                            "  /  |/ / / / / __/ ___/   \n" +
                            " / /|  / /_/ / /_(__  )    \n" +
                            "/_/ |_/\\\\__,_/\\\\__/____/  `` == version [[" + getConfigManager().getBootRuntime().getVersion() + "]]"
            );
            out.println("[[--------------------------------------------------------------------------------]]");
            out.println("This is the very @@First@@ time Nuts has benn started for this workspace...");
            out.println("[[--------------------------------------------------------------------------------]]");
        }
        NutsQuestion<Boolean> q = NutsQuestion.forBoolean("Would you like to install recommended companion tools").setDefautValue(true);
        if (!ask || terminal.ask(q)) {
            installCompanionTools(silent, session);
        }
    }

    @Override
    public void installCompanionTools(boolean silent, NutsSession session){
        PrintStream out = terminal.getFormattedOut();
        out.println("Installation of Nuts companion tools...");
        out.println("##\\### installing ==nsh== (Nuts bash shell companion)...");
        install("nsh", new String[0], NutsConfirmAction.FORCE, session);
        out.println("##\\### installing ==ndi== (Nuts Desktop Integration companion)...");
        install("ndi", new String[0], NutsConfirmAction.FORCE, session);
        out.println("##\\### installing ==mvn== (Maven command line companion)...");
        install("mvn", new String[0], NutsConfirmAction.FORCE, session);
        out.println("Installation of companion tools ##succeeded##...");
    }

    @Override
    public NutsDefinition fetchBootFile(NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (nutsComponentId == null) {
            nutsComponentId = fetch(NutsConstants.NUTS_ID_BOOT_API, session);
        }
        return nutsComponentId;
    }

    @Override
    public NutsDefinition install(String id, String[] args, NutsConfirmAction foundAction, NutsSession session) {
        return install(parseRequiredId(id), args, foundAction, session);
    }

    @Override
    public NutsDefinition install(NutsId id, String[] args, NutsConfirmAction foundAction, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (foundAction == null) {
            foundAction = NutsConfirmAction.ERROR;
        }
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_INSTALL, "install");
        NutsDefinition nutToInstall = fetchWithDependencies(id, session);
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
        NutsDescriptor d = CoreNutsUtils.parseNutsDescriptor(file);
        String oldVersion = StringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - NutsConstants.VERSION_CHECKED_OUT_EXTENSION.length());
            String newVersion = parseVersion(oldVersion).inc().getValue();
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
            NutsId newId = deploy(new NutsDeployment().setContent(new File(folder)).setDescriptor(d), session);
            d.write(file);
            IOUtils.delete(new File(folder));
            return newId;
        } else {
            throw new NutsUnsupportedOperationException("commit not supported");
        }
    }

    @Override
    public NutsDefinition checkout(String id, String folder, NutsSession session) {
        return checkout(parseRequiredId(id), folder, session);
    }

    @Override
    public NutsDefinition checkout(NutsId id, String folder, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_INSTALL, "checkout");
        NutsDefinition nutToInstall = fetchWithDependencies(id, session);
        if ("zip".equals(nutToInstall.getDescriptor().getExt())) {

            ZipUtils.unzip(nutToInstall.getFile(), resolvePath(folder), new UnzipOptions().setSkipRoot(false));

            File file = new File(folder, NutsConstants.NUTS_DESC_FILE_NAME);
            NutsDescriptor d = CoreNutsUtils.parseNutsDescriptor(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().setVersion(oldVersion + NutsConstants.VERSION_CHECKED_OUT_EXTENSION);
            d = d.setId(newId);

            d.write(file, true);

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

    public NutsUpdate checkUpdates(String id, String bootAPIVersion, NutsSession session) {
        return checkUpdates(parseRequiredId(id), bootAPIVersion, session);
    }

    public NutsUpdate checkUpdates(NutsId id, String bootAPIVersion, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsId oldId = null;
        NutsDefinition oldFile = null;
        NutsDefinition newFile = null;
        NutsId newId = null;
        NutsSession sessionOnline = session.copy().setFetchMode(NutsFetchMode.ONLINE);
        NutsSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        if (id.getSimpleName().equals(NutsConstants.NUTS_ID_BOOT_API)) {
            oldId = getConfigManager().getBootAPI();
            String v = bootAPIVersion;
            if (StringUtils.isEmpty(v)) {
                v = "LATEST";
            }
            oldFile = fetch(oldId, sessionOffline);
            newFile = this.fetch(NutsConstants.NUTS_ID_BOOT_API + "#" + v, sessionOnline);
            newId = newFile.getId();
        } else if (id.getSimpleName().equals(NutsConstants.NUTS_ID_BOOT_RUNTIME)) {
            oldId = getConfigManager().getBootRuntime();
            oldFile = fetch(oldId, sessionOffline);
            newId = createQuery()
                    .addId(NutsConstants.NUTS_ID_BOOT_RUNTIME)
                    .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootAPIVersion))
                    .setLatestVersions(true)
                    .setSession(sessionOnline)
                    .findFirst();
            if (newId != null) {
                try {
                    newFile = fetchWithDependencies(newId, session);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } else {
            try {
                oldId = fetchDescriptor(id, true, session.setFetchMode(NutsFetchMode.OFFLINE)).getId();
                oldFile = fetch(oldId, session);
            } catch (Exception ex) {
                //ignore
            }
            newId = createQuery().addId(NutsConstants.NUTS_ID_BOOT_RUNTIME)
                    .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootAPIVersion))
                    .setLatestVersions(true).setSession(sessionOnline).findFirst();
            if (newId != null) {
                try {
                    newFile = fetchWithDependencies(newId, session);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }

        //compare canonical forms
        NutsId cnewId = toCanonicalForm(newId);
        NutsId coldId = toCanonicalForm(oldId);
        if (cnewId != null && !cnewId.equals(coldId) && newFile != null) {
            String sOldFile = oldFile == null ? null : oldFile.getFile();
            String sNewFile = newFile.getFile();
            return new NutsUpdate(id, oldId, newId, sOldFile, sNewFile, false);
        }
        return null;
    }

    @Override
    public NutsUpdate[] checkWorkspaceUpdates(NutsWorkspaceUpdateOptions options, NutsSession session) {
        if (options == null) {
            options = new NutsWorkspaceUpdateOptions();
        }
        session = CoreNutsUtils.validateSession(session, this);
        Map<String, NutsUpdate> allUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdate> extUpdates = new LinkedHashMap<>();
        NutsUpdate bootUpdate = null;
        String bootVersion = getConfigManager().getBootAPI().getVersion().toString();
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

            runtimeUpdate = checkUpdates(getConfigManager().getBootRuntime().getSimpleName(), bootVersion, session);
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
                out.printf("Workspace is [[up-to-date]]. You are running latest version ==%s==\n", getConfigManager().getBootRuntime().getVersion());
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
            if (bootUpdate != null) {
                if (bootUpdate.getAvailableIdFile() != null && bootUpdate.getOldIdFile() != null) {
                    NutsBootConfig bc = getConfigManager().getWorkspaceBootConfig();
                    //will be re-evaluated later!
                    bc.setBootRuntime(null);
                    bc.setBootAPIVersion(bootUpdate.getAvailableId().getVersion().toString());
                    getConfigManager().setBootConfig(bc);
                }
            }
            if (runtimeUpdate != null) {
                NutsBootConfig bc = getConfigManager().getWorkspaceBootConfig();
                //will be re-evaluated later!
                bc.setBootRuntime(runtimeUpdate.getAvailableId().getVersion().toString());
                bc.setBootAPIVersion(runtimeUpdate.getAvailableId().getVersion().toString());
                getConfigManager().setBootConfig(bc);
            }
            for (NutsUpdate extension : extUpdates.values()) {
                getConfigManager().updateExtension(extension.getAvailableId());
            }
            getConfigManager().save();
            if (log.isLoggable(Level.INFO)) {
                log.log(Level.INFO, "Workspace is updated. Nuts should be restarted for changes to take effect.");
            }
        }
        return updates;
    }

//    public NutsId getBootAPI() {
//        String bootId = configManager.getWorkspaceBoot().getBootAPI();
//        if (StringUtils.isEmpty(bootId)) {
//            bootId = NutsConstants.NUTS_ID_BOOT_API;
//        }
//        return parseNutsId(bootId);
//    }

//    public NutsId getBootRuntime() {
//        String runtimeId = configManager.getWorkspaceBoot().getBootRuntime();
//        if (StringUtils.isEmpty(runtimeId)) {
//            runtimeId = NutsConstants.NUTS_ID_BOOT_RUNTIME;
//        }
//        return parseNutsId(runtimeId);
//    }

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
                if (ext.equalsSimpleName(getConfigManager().getBootRuntime())) {
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
            log.log(Level.CONFIG, "Inaccessible runtime info url : {0}", bootUrl.getPath());
        }
        throw new NutsIllegalArgumentException("Inaccessible runtime info : " + bootUrls);
    }

    protected String expandPath(String path) {
        return CoreNutsUtils.expandPath(path, this);
    }

//    @Override
//    public NutsDefinition updateWorkspace(String version, NutsConfirmAction foundAction, NutsSession session) {
//        session = validateSession(session);
//        String nutsIdStr = NutsConstants.NUTS_ID_BOOT_API + (StringUtils.isEmpty(version) ? "" : ("#") + version);
//        NutsDefinition[] bootIdFile = bootstrapUpdate(nutsIdStr, foundAction, session);
//        Properties bootInfo = getBootInfo(bootIdFile[0].getId());
//        String runtimeId = bootInfo.getProperty("runtimeId");
//        NutsDefinition[] runtimeIdFiles = bootstrapUpdate(runtimeId, foundAction, session);
//        if (runtimeIdFiles.length == 0) {
//            throw new NutsBootException("Unable to locate update for " + runtimeId);
//        }
//        Properties bootProperties = new Properties();
//        final NutsId runtimeIdFile = runtimeIdFiles[0].getId();
//        bootProperties.setProperty("runtimeId", runtimeIdFile.toString());
//        NutsRepository[] repositories = getRepositoryManager().getRepositories();
//        List<String> repositoryUrls = new ArrayList<>();
//        repositoryUrls.add(expandPath(NutsConstants.URL_COMPONENTS_LOCAL));
//        for (NutsRepository repository : repositories) {
//            if (repository.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS) || repository.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN)) {
//                repositoryUrls.add(repository.getConfigManager().getLocation());
//            } else {
//                for (NutsRepository mirror : repository.getMirrors()) {
//                    if (mirror.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS) || repository.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN)) {
//                        repositoryUrls.add(mirror.getConfigManager().getLocation());
//                    }
//                }
//            }
//        }
//        String repositoriesPath = StringUtils.join(";", repositoryUrls);
//        bootProperties.setProperty("repositories", repositoriesPath);
//        File r = new File(bootstrapNutsRepository.getConfigManager().getLocationFolder());
//        IOUtils.saveProperties(bootProperties, null, new File(r, CoreNutsUtils.getPath(runtimeIdFile, ".userProperties", File.separator)));
//
//        Properties coreProperties = new Properties();
//        List<String> dependencies = new ArrayList<>();
//        for (NutsDefinition fetchDependency : runtimeIdFiles) {
//            dependencies.add(fetchDependency.getId().setNamespace(null).toString());
//        }
//        coreProperties.put("project.id", runtimeIdFile.getSimpleName());
//        coreProperties.put("project.version", runtimeIdFile.getVersion().toString());
//        coreProperties.put("project.repositories", repositoriesPath);
//        coreProperties.put("project.dependencies.compile", StringUtils.join(";", dependencies));
//        IOUtils.saveProperties(coreProperties, null, new File(r, CoreNutsUtils.getPath(runtimeIdFile, ".userProperties", File.separator)));
//
//        List<NutsDefinition> updatedExtensions = new ArrayList<>();
//        for (NutsId ext : getConfigManager().getExtensions()) {
//            NutsVersion nversion = ext.getVersion();
//            if (!nversion.isSingleValue()) {
//                //will update bootstrap workspace so that next time
//                //it will be loaded
//                NutsDefinition[] newVersion = bootstrapUpdate(ext.toString(), foundAction, session);
//                if (!newVersion[0].getId().getVersion().equals(nversion)) {
//                    updatedExtensions.add(newVersion[0]);
//                }
//            }
//        }
//        if (updatedExtensions.size() > 0) {
//            if (log.isLoggable(Level.INFO)) {
//                log.log(Level.INFO, "Some extensions were updated. Nuts should be restarted for extensions to take effect.");
//            }
//        }
//        return bootIdFile[0];
//    }

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
        return update(parseRequiredId(id), uptoDateAction, session);
    }

    @Override
    public NutsDefinition update(NutsId id, NutsConfirmAction uptoDateAction, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_INSTALL, "update");
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException("Version is too restrictive. You would use fetch or install instead");
        }
        NutsDefinition nutToInstall = fetchWithDependencies(id, session);
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
        return isInstalled(parseRequiredId(id), checkDependencies, session);
    }

    @Override
    public boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsDefinition nutToInstall = null;
        try {
            if (checkDependencies) {
                nutToInstall = fetchWithDependencies(id, session.copy().setFetchMode(NutsFetchMode.OFFLINE).setTransitive(false));
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
        return uninstall(parseRequiredId(id), args, notFoundAction, deleteData, session);
    }

    @Override
    public boolean uninstall(NutsId id, String[] args, NutsConfirmAction notFoundAction, boolean deleteData, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_UNINSTALL, "uninstall");
        NutsDefinition nutToInstall = fetchWithDependencies(id, session.copy().setTransitive(false));
        if (!isInstalled(nutToInstall, session)) {
            throw new NutsIllegalArgumentException(id + " Not Installed");
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        if (ii == null) {
            return false;
        }
//        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, args, session, true, null);
        ii.uninstall(executionContext, deleteData);
        IOUtils.delete(new File(getStoreRoot(id, RootFolderType.PROGRAMS)));
        IOUtils.delete(new File(getStoreRoot(id, RootFolderType.TEMP)));
        IOUtils.delete(new File(getStoreRoot(id, RootFolderType.LOGS)));
        IOUtils.delete(new File(getStoreRoot(id, RootFolderType.VAR)));
        IOUtils.delete(new File(getStoreRoot(id, RootFolderType.CONFIG)));
        return true;
    }

    @Override
    public NutsCommandExecBuilder createExecBuilder() {
        return new DefaultNutsCommandExecBuilder(this);
    }


    @Override
    public boolean isFetched(String id, NutsSession session) {
        return isFetched(parseRequiredId(id), session);
    }

    @Override
    public NutsDefinition fetchWithDependencies(String id, NutsSession session) {
        return fetchWithDependencies(parseRequiredId(id), session);
    }

    @Override
    public NutsDefinition fetchWithDependencies(NutsId id, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsDefinition fetched = fetch(id, session);
        createQuery().addId(fetched.getId()).setSession(session).dependenciesOnly().fetch();
        return fetched;
    }

    @Override
    public NutsDefinition fetch(String id, NutsSession session) {
        return fetch(parseRequiredId(id), session);
    }

    @Override
    public NutsDefinition fetch(NutsId id, NutsSession session) {
        return fetchSimple(id, session);
    }

    @Override
    public NutsId resolveId(String id, NutsSession session) {
        NutsId nutsId = parseRequiredId(id);
        return resolveId(nutsId, session);
    }

    protected NutsId resolveId0(NutsId id, NutsSession session) {
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsRepository repo : getEnabledRepositories(id, repositoryFilter, session)) {
            try {
                NutsDescriptor child = repo.fetchDescriptor(id, session);
                if (child != null) {
                    NutsId id2 = child.getId();
                    if (StringUtils.isEmpty(id2.getNamespace())) {
                        id2 = id2.setNamespace(repo.getRepositoryId());
                    }
                    return id2;
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
                NutsId nutsId = parseId(id);
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
                    NutsId nid = parseId(id);
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
        return fetchDescriptor(parseRequiredId(idString), effective, session);
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
        NutsId nutsId = parseRequiredId(id);
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
        NutsId nutsId = parseRequiredId(id);
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
        push(parseRequiredId(id), repositoryId, foundAction, session);
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
                descriptor = CoreNutsUtils.parseNutsDescriptor(ext);
            } else {
                descriptor = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(contentFolderObj, new File(getConfigManager().getCwd())), session);
            }
            if (descriptor != null) {
                if ("zip".equals(descriptor.getExt())) {
                    if (destFile == null) {
                        destFile = resolvePath(contentFolderObj.getParent()
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
            return thisId.setFace(descriptor.getFace());
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
            NutsId bestId = new NutsIdImpl(null, g, thisId.getName(), v, "");
            String bestResult = bestId.toString();
            if (StringUtils.isEmpty(g) || StringUtils.isEmpty(v)) {
                throw new NutsNotFoundException(bestResult, "Unable to fetchEffective for " + thisId + ". Best Result is " + bestResult, null);
            }
            return bestId.setFace(descriptor.getFace());
        } else {
            return thisId.setFace(descriptor.getFace());
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
        if (nutsDescriptor.getPackaging().isEmpty()) {
            descriptor.applyParents(parentDescriptors).applyProperties();
        }
        return nutsDescriptor;
    }

    @Override
    public NutsId deploy(NutsDeployment deployment, NutsSession session) {

        File tempFile = null;
        TypedObject content = deployment.getContent();
        if (content == null || content.getValue() == null) {
            throw new NutsIllegalArgumentException("Missing content");
        }

        TypedObject vdescriptor = deployment.getDescriptor();
        NutsDescriptor descriptor = null;
        if (vdescriptor != null && vdescriptor.getValue() != null) {
            if (NutsDescriptor.class.equals(vdescriptor.getType())) {
                descriptor = (NutsDescriptor) vdescriptor.getValue();
                if (deployment.getDescSHA1() != null && !descriptor.getSHA1().equals(deployment.getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }
            } else if (IOUtils.isValidInputStreamSource(vdescriptor.getType())) {
                InputStreamSource inputStreamSource = IOUtils.toInputStreamSource(vdescriptor.getValue(), vdescriptor.getVariant(), null, new File(getConfigManager().getCwd()));
                if (deployment.getDescSHA1() != null && !CoreSecurityUtils.evalSHA1(inputStreamSource.open(), true).equals(deployment.getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }

                descriptor = CoreNutsUtils.parseNutsDescriptor(inputStreamSource.open(), true);
            } else {
                throw new NutsException("Unexpected type " + vdescriptor.getType());
            }
        }
        InputStreamSource contentSource = IOUtils.toInputStreamSource(content.getValue(), content.getVariant(), null, new File(getConfigManager().getCwd()));

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
        return copyTo(parseRequiredId(id), session, localPath);
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
        return (session == null || session.getTerminal() == null) ? createNullPrintStream() : session.getTerminal().getOut();
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

        NutsWorkspaceArchetypeComponent instance = getExtensionManager().createSupported(NutsWorkspaceArchetypeComponent.class, this);
        if (instance == null) {
            //get the default implementation
            instance = new DefaultNutsWorkspaceArchetypeComponent();
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
                    runnerFile = fetchWithDependencies(installerDescriptor.getId(), session.copy().setTransitive(false));
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
        NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, new String[0], session, true, nutToInstall.getId().getSimpleName());
        File installFolder = new File(executionContext.getWorkspace().getStoreRoot(executionContext.getNutsDefinition().getId(), RootFolderType.PROGRAMS));
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
        File contentFile = contentFile0 == null ? null : new File(contentFile0);
        session = CoreNutsUtils.validateSession(session, this);
        File tempFile = null;
        try {
            if (contentFile.isDirectory()) {
                File descFile = new File(contentFile, NutsConstants.NUTS_DESC_FILE_NAME);
                NutsDescriptor descriptor2;
                if (descFile.exists()) {
                    descriptor2 = CoreNutsUtils.parseNutsDescriptor(descFile);
                } else {
                    descriptor2 = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(new File(contentFile, getConfigManager().getCwd())), session);
                }
                if (descriptor == null) {
                    descriptor = descriptor2;
                } else {
                    if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                        descriptor.write(descFile);
                    }
                }
                if (descriptor != null) {
                    if ("zip".equals(descriptor.getExt())) {
                        File zipFilePath = new File(resolvePath(contentFile.getPath() + ".zip"));
                        ZipUtils.zip(contentFile.getPath(), new ZipOptions(), zipFilePath.getPath());
                        contentFile = zipFilePath;
                        tempFile = contentFile;
                    } else {
                        throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else {
                if (descriptor == null) {
                    descriptor = CoreNutsUtils.resolveNutsDescriptorFromFileContent(this, IOUtils.toInputStreamSource(contentFile, new File(getConfigManager().getCwd())), session);
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
                CorePlatformUtils.checkSupportedOs(parseRequiredId(os).getSimpleName());
            }
            for (String arch : descriptor.getArch()) {
                CorePlatformUtils.checkSupportedArch(parseRequiredId(arch).getSimpleName());
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


    private String resolveWorkspacePath(String workspace) {
        if (StringUtils.isEmpty(workspace)) {
            File file = CoreIOUtils.resolvePath(getConfigManager().getHomeLocation() + "/" + NutsConstants.DEFAULT_WORKSPACE_NAME, null, getConfigManager().getHomeLocation());
            workspace = file == null ? null : file.getPath();
        } else {
            File file = CoreIOUtils.resolvePath(workspace, null, getConfigManager().getHomeLocation());
            workspace = file == null ? null : file.getPath();
        }

        Set<String> visited = new HashSet<String>();
        while (true) {
            File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
            NutsWorkspaceConfig nutsWorkspaceConfig = CoreJsonUtils.loadJson(file, NutsWorkspaceConfig.class);
            if (nutsWorkspaceConfig != null) {
                String nextWorkspace = nutsWorkspaceConfig.getWorkspace();
                if (nextWorkspace != null && nextWorkspace.trim().length() > 0) {
                    if (visited.contains(nextWorkspace)) {
                        throw new NutsException("Circular Workspace Dependency : " + nextWorkspace);
                    }
                    visited.add(nextWorkspace);
                    workspace = nextWorkspace;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return workspace;
    }

    private boolean isWorkspaceFolder(String workspace) {
        workspace = resolveWorkspacePath(workspace);
        File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        if (file.isFile() && file.exists()) {
            return true;
        }
        return false;
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
                NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, args, session, true, null);
                if (!isInstalled(executionContext.getNutsDefinition(), session)) {
                    setInstalled(executionContext);
                    try {
                        installerComponent.install(executionContext);
                        executionContext.getWorkspace().getTerminal().getFormattedOut().printf(formatId(nutToInstall.getId()) + " Installed successfully.\n");
                    } catch (Exception ex) {
                        executionContext.getWorkspace().getTerminal().getFormattedOut().printf(formatId(nutToInstall.getId()) + " @@Failed@@ to install : %s.\n", ex.toString());
                        File installFolder = new File(executionContext.getWorkspace().getStoreRoot(executionContext.getNutsDefinition().getId(), RootFolderType.PROGRAMS));
                        File log = new File(installFolder, ".nuts-install.log");
                        if (log.isFile()) {
                            log.delete();
                        }
                        throw new NutsExecutionException("Unable to install "+nutToInstall.getId().toString(),ex, 1);
                    }
                    String installFolder = getStoreRoot(nutToInstall.getId(), RootFolderType.PROGRAMS);
                    nutToInstall.setInstallFolder(installFolder);
                }
            }
        }
        for (NutsInstallListener nutsListener : session.getListeners(NutsInstallListener.class)) {
            nutsListener.onInstall(nutToInstall, reinstall, session);
        }
    }

    private NutsExecutionContext createNutsExecutionContext(NutsDefinition nutToInstall, String[] args, NutsSession session, boolean failFast, String commandName) {
        if (commandName == null) {
            commandName = resolveCommandName(nutToInstall.getId());
        }
        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutorDescriptor installer = descriptor.getInstaller();
        List<String> iargs = new ArrayList<>();
        Properties props = null;
        if (installer != null) {
            if (installer.getOptions() != null) {
                iargs.addAll(Arrays.asList(installer.getOptions()));
            }
            props = installer.getProperties();
        }
        if (args != null) {
            iargs.addAll(Arrays.asList(args));
        }
        String installFolder = getStoreRoot(nutToInstall.getId(), RootFolderType.PROGRAMS);
        Properties env = new Properties();
        return new NutsExecutionContextImpl(nutToInstall, new String[0], iargs.toArray(new String[0]), env, props, installFolder, session, this, failFast, commandName);
    }

    private NutsId toCanonicalForm(NutsId id) {
        if (id != null) {
            id = id.setNamespace(null);
            if ("default".equals(id.getQueryMap().get("face"))) {
                id = id.setQueryProperty("face", null);
            }
        }
        return id;
    }

//    private NutsDefinition[] bootstrapUpdate(String id, NutsConfirmAction foundAction, NutsSession session) {
//        session = validateSession(session);
//        NutsDefinition[] deps = fetchDependencies(new NutsDependencySearch(id).setIncludeMain(true), session);
//        for (NutsDefinition dep : deps) {
//            if (dep.getFile() != null && !NutsConstants.DEFAULT_REPOSITORY_NAME.equals(dep.getId().getNamespace())) {
//                bootstrapNutsRepository.deploy(dep.getId(),
//                        dep.getDescriptor(),
//                        dep.getFile(), foundAction,
//                        session
//                );
//            }
//        }
//        return deps;
//    }

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
                        String installFolder = getStoreRoot(main.getId(), RootFolderType.PROGRAMS);
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
        Set<String> excludedExtensionsSet = excludedExtensions == null ? null : new HashSet<String>(Arrays.asList(excludedExtensions));
        Set<String> excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<String>(Arrays.asList(excludedRepositories));
        session = CoreNutsUtils.validateSession(session, this);
        File file = CoreIOUtils.createFile(getConfigManager().getWorkspaceLocation(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        NutsWorkspaceConfig config = CoreJsonUtils.loadJson(file, NutsWorkspaceConfig.class);
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
        private final String bootAPIVersion;

        public BootAPINutsDescriptorFilter(String bootAPIVersion) {
            this.bootAPIVersion = bootAPIVersion;
        }

        @Override
        public boolean accept(NutsDescriptor descriptor) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                if (dependency.getLongName().equals(NutsConstants.NUTS_ID_BOOT_API)) {
                    if (dependency.getVersion().matches("]" + bootAPIVersion + "]")) {
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
    public String getStoreRoot(String id, RootFolderType folderType) {
        return getStoreRoot(parseId(id), folderType);
    }

    public String getStoreRoot(NutsId id, RootFolderType folderType) {
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        File groupFolder = new File(getStoreRoot(folderType), id.getGroup().replaceAll("\\.", File.separator));
        if (StringUtils.isEmpty(id.getName())) {
            throw new NutsElementNotFoundException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsElementNotFoundException("Missing version for " + id.toString());
        }
        return new File(artifactFolder, id.getVersion().getValue()).getPath();
    }

    public String getStoreRoot(RootFolderType folderType) {
        if (folderType == null) {
            folderType = RootFolderType.PROGRAMS;
        }
        String k = null;
        String v = null;
        switch (folderType) {
            case PROGRAMS: {
                k = NutsConstants.ENV_STORE_PROGRAMS;
                v = NutsConstants.DEFAULT_STORE_PROGRAM;
                break;
            }
            case VAR: {
                k = NutsConstants.ENV_STORE_VAR;
                v = NutsConstants.DEFAULT_STORE_VAR;
                break;
            }
            case LOGS: {
                k = NutsConstants.ENV_STORE_LOGS;
                v = NutsConstants.DEFAULT_STORE_LOG;
                break;
            }
            case TEMP: {
                k = NutsConstants.ENV_STORE_TEMP;
                v = NutsConstants.DEFAULT_STORE_TEMP;
                break;
            }
            case CONFIG: {
                k = NutsConstants.ENV_STORE_CONFIG;
                v = NutsConstants.DEFAULT_STORE_CONFIG;
                break;
            }
            default: {
                k = NutsConstants.ENV_STORE_TEMP;
                v = NutsConstants.DEFAULT_STORE_TEMP;
                break;
            }
        }
        return CoreIOUtils.resolvePath(getConfigManager().getEnv(k, v),
                new File(resolvePath(getConfigManager().getWorkspaceLocation())),
                getConfigManager().getHomeLocation()).getPath();
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configManager
                + '}';
    }

    @Override
    public NutsId parseId(String id) {
        return CoreNutsUtils.parseNutsId(id);
    }

    @Override
    public NutsId resolveIdForClass(Class clazz) {
        PomId u = PomIdResolver.resolvePomId(clazz, null);
        if (u == null) {
            return null;
        }
        return parseId(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
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
            all[i] = parseId(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
    }

    @Override
    public NutsId getPlatformOs() {
        if (platformOs == null) {
            platformOs = parseId(CorePlatformUtils.getPlatformOs());
        }
        return platformOs;
    }

    @Override
    public NutsId getPlatformOsDist() {
        if (platformOsdist == null) {
            platformOsdist = parseId(CorePlatformUtils.getPlatformOsDist());
        }
        return platformOsdist;
    }

    @Override
    public String getPlatformOsLibPath() {
        if (platformOsLibPath == null) {
            platformOsLibPath = CorePlatformUtils.getPlatformOsLib();
        }
        return platformOsLibPath;
    }

    @Override
    public NutsId getPlatformArch() {
        if (platformArch == null) {
            platformArch = parseId(CorePlatformUtils.getPlatformArch());
        }
        return platformArch;
    }

    @Override
    public String resolvePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        return FileUtils.getAbsolutePath(new File(getConfigManager().getCwd()), path);
    }

    @Override
    public String resolveRepositoryPath(String repositoryLocation) {
        String root = repositoryManager.getRepositoriesRoot();
        NutsWorkspaceConfigManager configManager = this.getConfigManager();
        return CoreIOUtils.resolvePath(repositoryLocation,
                root != null ? new File(root) : CoreIOUtils.createFile(
                        configManager.getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES),
                configManager.getHomeLocation()).getPath();
    }

    @Override
    public NutsIdBuilder createIdBuilder() {
        return new DefaultNutsIdBuilder();
    }

    //    public NutsVesionBuilder createNutsVersionBuilder() {
//        return new DefaultVersionBuilder();
//    }
    @Override
    public NutsDescriptor parseDescriptor(URL url) {
        return CoreNutsUtils.parseNutsDescriptor(url);
    }

    @Override
    public NutsDescriptor parseDescriptor(File file) {
        return CoreNutsUtils.parseNutsDescriptor(file);
    }

    @Override
    public NutsDescriptor parseDescriptor(InputStream stream) {
        return CoreNutsUtils.parseNutsDescriptor(stream, false);
    }

    @Override
    public NutsDescriptor parseDescriptor(String descriptorString) {
        return CoreNutsUtils.parseNutsDescriptor(descriptorString);
    }

    @Override
    public NutsDependency parseDependency(String dependency) {
        return CoreNutsUtils.parseNutsDependency(dependency);
    }

    @Override
    public NutsId parseRequiredId(String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    @Override
    public NutsVersion parseVersion(String version) {
        return version == null ? new NutsVersionImpl("") : new NutsVersionImpl(version);
    }

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
    public String filterText(String value) {
        return FormattedPrintStreamParser.INSTANCE.filterText(value);
    }

    @Override
    public String escapeText(String str) {
        if (str == null) {
            return "";
        }
        str = str.replace("`", "\\`");
        return "``" + str + "``";
    }

    @Override
    public ExecutionEntry[] resolveExecutionEntries(File file) {
        return CorePlatformUtils.resolveMainClasses(file);
    }

    @Override
    public ExecutionEntry[] resolveExecutionEntries(InputStream inputStream, String type) {
        return CorePlatformUtils.resolveMainClasses(inputStream);
    }

    @Override
    public String createRegex(String pattern, boolean contains) {
        return CoreStringUtils.simpexpToRegexp(pattern, contains);
    }


    @Override
    public String getResourceString(String resource, Class cls, String defaultValue) {
        String help = null;
        try {
            InputStream s = null;
            try {
                s = cls.getResourceAsStream(resource);
                if (s != null) {
                    help = IOUtils.loadString(s, true);
                }
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Nuts.class.getName()).log(Level.SEVERE, "Unable to load main help", e);
        }
        if (help == null) {
            help = defaultValue;//"no help found";
        }
        HashMap<String, String> props = new HashMap<>((Map) System.getProperties());
        props.putAll(getConfigManager().getRuntimeProperties());
        help = CoreStringUtils.replaceVars(help, new MapStringMapper(props));
        return help;
    }

    @Override
    public void updateRepositoryIndex(String path) {
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
        for (NutsRepository nutsRepository : getRepositoryManager().getRepositories()) {
            if (nutsRepository instanceof NutsFolderRepository) {
                ((NutsFolderRepository) nutsRepository).reindexFolder();
            }
        }
    }

    @Override
    public void downloadPath(String from, File to, NutsSession session) {
        CoreIOUtils.downloadPath(from, to, null, this, session);
    }

    @Override
    public String evalContentHash(InputStream input) {
        return CoreSecurityUtils.evalSHA1(input, false);
    }

    @Override
    public void printHelp(PrintStream out) {
        if (out == null) {
            out = createPrintStream(System.out, true);
        }
        String help = getResourceString("/net/vpc/app/nuts/nuts-help.help", getClass(), "no help found");
        out.println(help);
    }

    @Override
    public void printLicense(PrintStream out) {
        if (out == null) {
            out = createPrintStream(System.out, true);
        }
        String help = getResourceString("/net/vpc/app/nuts/nuts-license.help", getClass(), "no help found");
        out.println(help);
    }

    @Override
    public void printVersion(PrintStream out, Properties extraProperties, String options) {
        if (out == null) {
            out = createPrintStream(System.out, true);
        }
        if (options == null) {
            options = "";
        }
        Set<String> optionsSet = new HashSet<>(Arrays.asList(options.split(",")));
        NutsWorkspaceConfigManager configManager = getConfigManager();
        if (optionsSet.contains("min")) {
            out.printf("%s\n", configManager.getBootAPI().getVersion());
            return;
        }
        boolean fancy = false;
        if (optionsSet.contains("fancy")) {
            fancy = true;
        }
        out = createPrintStream(out, true);
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        int len = 21;
        for (String extraKey : extraKeys) {
            int x = escapeText(extraKey).length();
            if (x > len) {
                len = x;
            }
        }
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        props.put("nuts-version", configManager.getBootAPI().getVersion().toString());
        props.put("nuts-boot-api", configManager.getBootAPI().toString());
        props.put("nuts-boot-runtime", configManager.getBootRuntime().toString());
        URL[] cl = configManager.getBootClassWorldURLs();
        List<String> runtimeClassPath = new ArrayList<>();
        if (cl != null) {
            for (URL url : cl) {
                if (url != null) {
                    String s = url.toString();
                    try {
                        s = Paths.get(url.toURI()).toFile().getPath();
                    } catch (URISyntaxException ex) {
                        s = s.replace(":", "\\:");
                    }
                    runtimeClassPath.add(s);
                }
            }
        }

        props.put("nuts-boot-runtime-path", StringUtils.join(":", runtimeClassPath));
        props.put("nuts-home", configManager.getHomeLocation());
        props.put("nuts-workspace", configManager.getWorkspaceLocation());
        props.put("java-version", System.getProperty("java.version"));
        props.put("java-executable", System.getProperty("java.home") + FileUtils.getNativePath("/bin/java"));
        props.put("java.class.path", System.getProperty("java.class.path"));
        props.put("java.library.path", System.getProperty("java.library.path"));
        props.put("os.name", getPlatformOs().toString());
        props.put("os.dist", getPlatformOsDist().toString());
        props.put("os.arch", getPlatformArch().toString());
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.getProperty(extraKey));
        }
        for (Map.Entry<String, String> e : props.entrySet()) {
            boolean requireFancy = false;
            String fancySep = ":";
            String key = e.getKey();
            if (key.equals("nuts-runtime-path")) {
                requireFancy = true;
                fancySep = ":";
            }
            if (key.equals("java.class.path")) {
                requireFancy = true;
                fancySep = File.pathSeparator;
            }
            if (key.equals("java.library.path")) {
                requireFancy = true;
                fancySep = File.pathSeparator;
            }
            if (fancy && requireFancy) {
                out.printf(StringUtils.formatLeft(key, len - key.length() + escapeText(key).length()) + " : \n");
                String space = StringUtils.formatLeft("", len + 7) + "[[%s]]\n";
                for (String s : e.getValue().split(fancySep)) {
                    out.printf(space, s);
                }
            } else {
                out.printf(StringUtils.formatLeft(key, len - key.length() + escapeText(key).length()) + " : [[%s]]\n", e.getValue());
            }
        }
    }

    @Override
    public PrintStream createPrintStream(File out) {
        if (out == null) {
            return null;
        }
        try {
            return new PrintStream(out);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public NutsTerminal createDefaultTerminal(InputStream in, PrintStream out, PrintStream err) {
        DefaultNutsTerminal v = new DefaultNutsTerminal();
        v.install(this, in, out, err);
        return v;
    }

    @Override
    public NutsTerminal createTerminal() {
        return createTerminal(null, null, null);
    }

    @Override
    public NutsTerminal createTerminal(NutsTerminal delegated, InputStream in, PrintStream out, PrintStream err) {
        NutsTerminalDelegate term = new NutsTerminalDelegate(delegated, in, out, err, false);
        term.install(this, in, out, err);
        return term;
    }

    @Override
    public NutsTerminal createTerminal(InputStream in, PrintStream out, PrintStream err) {
        NutsTerminalBase termb = getExtensionManager().createSupported(NutsTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionMissingException(NutsTerminal.class, "Terminal");
        }
        try {
            NutsTerminal term = (termb instanceof NutsTerminal) ? (NutsTerminal) termb : new NutsTerminalDelegate(termb, true);
            term.install(this, in, out, err);
            return term;
        } catch (Exception anyException) {
            return createDefaultTerminal(in, out, err);
        }
    }

    @Override
    public ClassLoader createClassLoader(String[] nutsIds, ClassLoader parentClassLoader, NutsSession session) {
        return createClassLoader(nutsIds, null, parentClassLoader, session);
    }

    @Override
    public ClassLoader createClassLoader(String[] nutsIds, NutsDependencyScope scope, ClassLoader parentClassLoader, NutsSession session) {
        if (scope == null) {
            scope = NutsDependencyScope.RUN;
        }
        session = CoreNutsUtils.validateSession(session, this);
        List<NutsDefinition> nutsDefinitions = createQuery().addId(nutsIds).setSession(session).setScope(scope).includeDependencies().fetch();
        URL[] all = new URL[nutsDefinitions.size()];
        for (int i = 0; i < all.length; i++) {
            all[i] = URLUtils.toURL(new File(nutsDefinitions.get(i).getFile()));
        }
        return new NutsURLClassLoader(all, parentClassLoader);
    }

    @Override
    public PrintStream createPrintStream(OutputStream out, boolean inputFormatted) {
        if (out == null) {
            out = IOUtils.NULL_PRINT_STREAM;
        }
        if ("true".equals(String.valueOf(getUserProperties().get("no-colors")))) {
            inputFormatted = false;
        }
        if (inputFormatted) {
            if (out instanceof NutsFormattedPrintStream) {
                return ((PrintStream) out);
            }
            //return new NutsDefaultFormattedPrintStream(out);
            return (PrintStream) getExtensionManager().createSupported(NutsFormattedPrintStream.class, out, new Class[]{OutputStream.class}, new Object[]{out});
        } else {
            if (out instanceof NutsFormattedPrintStream) {
                return ((PrintStream) out);
            }
            if (out instanceof PrintStream) {
                return (PrintStream) out;
            }
//            return (PrintStream) objectFactory.createSupported(NutsNonFormattedPrintStream.class, out, new Class[]{OutputStream.class}, new Object[]{out});
            return new PrintStream(out);
        }
    }

    /**
     * FORMATTED_TO_UNFORMATTED : create printSteam that accepts formatted text but discard all text to produce non formatted text
     * FORMATTED_TO_FORMATTED   : create printSteam that accepts formatted text but discard all text to produce non formatted text
     *
     * @param out
     * @param inputFormatted
     * @param forceNoColors
     * @return
     */
    @Override
    public PrintStream createPrintStream(OutputStream out, boolean inputFormatted, boolean forceNoColors) {
        if (out == null) {
            out = IOUtils.NULL_PRINT_STREAM;
            return new PrintStream(out);
        }
        if (inputFormatted) {
            if (out instanceof NutsFormattedPrintStream) {
                if (forceNoColors) {
                    NutsFormattedPrintStream r = (NutsFormattedPrintStream) out;
                    return new NutsDefaultFormattedPrintStream(r.getUnformattedInstance());
//                    FormattedPrintStream rr = (FormattedPrintStream) r;
//                    return new NutsDefaultFormattedPrintStream(new UnformattedPrintStream(rr));
                }
                return ((PrintStream) out);
            }
            if (forceNoColors) {
                return new NutsDefaultFormattedPrintStream(out);
            }
            return (PrintStream) getExtensionManager().createSupported(NutsFormattedPrintStream.class, out, new Class[]{OutputStream.class}, new Object[]{out});
        } else {
            if (out instanceof NutsFormattedPrintStream) {
                NutsFormattedPrintStream r = (NutsFormattedPrintStream) out;
//                FormattedPrintStream rr = (FormattedPrintStream) r;
//                return new NutsDefaultFormattedPrintStream(new UnformattedPrintStream(rr));
                return r.getUnformattedInstance();
            }
            if (out instanceof PrintStream) {
                return (PrintStream) out;
            }
//            return (PrintStream) objectFactory.createSupported(NutsNonFormattedPrintStream.class, out, new Class[]{OutputStream.class}, new Object[]{out});
            return new PrintStream(out);
        }
    }

    @Override
    public InputStream monitorInputStream(String path, String name, NutsSession session) {
        InputStream stream = null;
        URLHeader header = null;
        long size = -1;
        try {
            if (URLUtils.isURL(path)) {
                if (URLUtils.isFileURL(new URL(path))) {
                    path = URLUtils.toFile(new URL(path)).getPath();
                    size = new File(path).length();
                    stream = new FileInputStream(path);
                } else {
                    NutsHttpConnectionFacade f = CoreHttpUtils.getHttpClientFacade(this, path);
                    try {

                        header = f.getURLHeader();
                        size = header.getContentLength();
                    } catch (Exception ex) {
                        //ignore error
                    }
                    stream = f.open();
                }
            } else {
                //this is file!
                size = new File(path).length();
                stream = new FileInputStream(path);
            }
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        return monitorInputStream(stream, size, (name == null ? path : name), session);
    }

    @Override
    public InputStream monitorInputStream(InputStream stream, long length, String name, NutsSession session) {
        if (length > 0) {
            return IOUtils.monitor(stream, null, (name == null ? "Stream" : name), length, new DefaultInputStreamMonitor(session.getTerminal().getOut()));
        } else {
            return stream;
        }
    }


    @Override
    public NutsTerminal getTerminal() {
        return terminal;
    }

    @Override
    public void setTerminal(NutsTerminal terminal) {
        if (terminal == null) {
            terminal = createTerminal();
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal);
        }
        this.terminal = terminal;
    }

    @Override
    public boolean isStandardOutputStream(OutputStream out) {
        if (out == null) {
            return true;
        }
        if (out == System.out) {
            return true;
        }
        if (out instanceof OutputStreamTransparentAdapter) {
            return isStandardOutputStream(((OutputStreamTransparentAdapter) out).baseOutputStream());
        }
        return false;
    }

    @Override
    public boolean isStandardErrorStream(OutputStream out) {
        if (out == null) {
            return true;
        }
        if (out == System.err) {
            return true;
        }
        if (out instanceof OutputStreamTransparentAdapter) {
            return isStandardErrorStream(((OutputStreamTransparentAdapter) out).baseOutputStream());
        }
        return false;
    }

    @Override
    public boolean isStandardInputStream(InputStream in) {
        if (in == null) {
            return true;
        }
        if (in == System.in) {
            return true;
        }
        if (in instanceof InputStreamTransparentAdapter) {
            return isStandardInputStream(((InputStreamTransparentAdapter) in).baseInputStream());
        }
        return false;
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
                    if (StringUtils.isEmpty(fetch.getId().getNamespace())) {
                        fetch.setId(fetch.getId().setNamespace(repo.getRepositoryId()));
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

    @Override
    public String formatId(NutsId id) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(id.getNamespace())) {
            sb.append(id.getNamespace()).append("://");
        }
        if (!StringUtils.isEmpty(id.getGroup())) {
            boolean importedGroup = false;
            for (String anImport : getConfigManager().getImports()) {
                if (id.getGroup().equals(anImport)) {
                    importedGroup = true;
                    break;
                }
            }
            if (importedGroup) {
                sb.append("==");
                sb.append(escapeText(id.getGroup()));
                sb.append("==");
            } else {
                sb.append(escapeText(id.getGroup()));
            }
            sb.append(":");
        }
        sb.append("[[");
        sb.append(escapeText(id.getName()));
        sb.append("]]");
        if (!StringUtils.isEmpty(id.getVersion().getValue())) {
            sb.append("#");
            sb.append(escapeText(id.getVersion().toString()));
        }
        if (!StringUtils.isEmpty(id.getQuery())) {
            sb.append("?");
            sb.append(escapeText(id.getQuery()));
        }
        return sb.toString();
    }

    @Override
    public String formatDependency(NutsDependency id) {
        Map<String, String> m = id.toId().getQueryMap();
        if (!StringUtils.isEmpty(id.getScope())) {
            m.put("scope", id.getScope());
        }
        if (!StringUtils.isEmpty(id.getOptional()) && !"false".equals(id.getOptional())) {
            m.put("optional", id.getOptional());
        }
        return formatId(id.toId().setQuery(m));
    }


    private static class CommandForIdNutsInstallerComponent implements NutsInstallerComponent {
        @Override
        public void install(NutsExecutionContext executionContext) {
            NutsId id = executionContext.getNutsDefinition().getId();
            NutsDescriptor descriptor = executionContext.getNutsDefinition().getDescriptor();
            if (descriptor.isNutsApplication()) {
                int r = executionContext.getWorkspace().createExecBuilder()
                        .setCommand(
                                id.setNamespace(null).toString(),
                                "--nuts-execution-mode=on-install"
                        )
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
            NutsId id = executionContext.getNutsDefinition().getId();
            if ("jar".equals(executionContext.getNutsDefinition().getDescriptor().getPackaging())) {
                ExecutionEntry[] executionEntries = CorePlatformUtils.resolveMainClasses(new File(executionContext.getNutsDefinition().getFile()));
                for (ExecutionEntry executionEntry : executionEntries) {
                    if (executionEntry.isApp()) {
                        //
                        int r = executionContext.getWorkspace().createExecBuilder()
                                .setCommand(
                                        id.toString(),
                                        "--nuts-execution-mode=on-uninstall"
                                )
                                .exec().getResult();
                        executionContext.getWorkspace().getTerminal().getFormattedOut().printf("Installation Exited with code : " + r);
                    }
                }
            }
//            NutsId id = executionContext.getNutsDefinition().getId();
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
        File installFolder = new File(executionContext.getWorkspace().getStoreRoot(executionContext.getNutsDefinition().getId(), RootFolderType.PROGRAMS));
        File log = new File(installFolder, ".nuts-install.log");
        IOUtils.copy(new ByteArrayInputStream(String.valueOf(new Date()).getBytes()), log, true, true);
    }
}


