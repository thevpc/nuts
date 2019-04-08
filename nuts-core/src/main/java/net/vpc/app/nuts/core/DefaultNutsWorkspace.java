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

import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.DefaultNutsSystemTerminalBase;
import net.vpc.app.nuts.core.terminals.DefaultSystemTerminal;
import net.vpc.app.nuts.core.terminals.UnmodifiableTerminal;
import net.vpc.app.nuts.core.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsFindCommand;
import net.vpc.app.nuts.core.util.bundledlibs.mvn.PomId;
import net.vpc.app.nuts.core.util.bundledlibs.mvn.PomIdResolver;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsWorkspace implements NutsWorkspace, NutsWorkspaceImpl, NutsWorkspaceExt {

    public static final Logger log = Logger.getLogger(DefaultNutsWorkspace.class.getName());
    public static final NutsInstallInfo NOT_INSTALLED = new NutsInstallInfo(false, null);
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
    public String uuid() {
        return getUuid();
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
        } else {
            options = options.copy();
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
                configManager.getBootClassLoader(), options)) {
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
                if (CoreStringUtils.isBlank(password)) {
                    password = this.getTerminal().readPassword("Password : ");
                }
                this.security().login(options.getLogin(), password);
            }
            configManager.setStartCreateTimeMillis(options.getCreationTime());
            configManager.setEndCreateTimeMillis(System.currentTimeMillis());
            if (!options.isReadOnly()) {
                config().save(false);
            }
            log.log(Level.FINE, "Nuts Workspace loaded in {0}", CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis()));
            if (options.isPerf()) {
                getTerminal().getFormattedOut().printf("**Nuts** Workspace loaded in [[%s]]\n",
                        CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis())
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
            install().setIncludecompanions(true).setSession(session).install();
        }
    }

    @Override
    public String[] getCompanionTools() {
        return new String[]{
            "net.vpc.app.nuts.toolbox:nsh",
            "net.vpc.app.nuts.toolbox:nfind",
            "net.vpc.app.nuts.toolbox:nadmin",
            "net.vpc.app.nuts.toolbox:ndi"
//                "mvn",
        };
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
    public NutsUpdateWorkspaceCommand updateWorkspace() {
        return new DefaultNutsUpdateWorkspaceCommand(this);
    }

    @Override
    public NutsPushCommand push() {
        return new DefaultNutsPushCommand(this);
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

//    private Properties getBootInfo(NutsId id) {
//        if (id.getVersion().isEmpty()) {
//            id = id.setVersion("LATEST");
//        }
//        List<NutsURLLocation> bootUrls = new ArrayList<>();
//        for (NutsURLLocation r : extensionManager.getExtensionURLLocations(id, NutsConstants.NUTS_ID_BOOT_API, "properties")) {
//            bootUrls.add(r);
//            if (r.getUrl() != null) {
//                Properties p = IOUtils.loadURLProperties(r.getUrl());
//                if (!p.isEmpty() && p.containsKey("runtimeId")) {
//                    return p;
//                }
//            }
//        }
//        if (bootUrls.isEmpty()) {
//            if (log.isLoggable(Level.CONFIG)) {
//                log.log(Level.CONFIG, "Inaccessible runtime info. Fatal error");
//            }
//        }
//        for (NutsURLLocation bootUrl : bootUrls) {
//            log.log(Level.CONFIG, "Inaccessible runtime info URL : {0}", bootUrl.getPath());
//        }
//        throw new NutsIllegalArgumentException("Inaccessible runtime info : " + bootUrls);
//    }
    public boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        NutsDefinition nutToInstall = null;
        try {
            nutToInstall = fetch().id(id).setSession(session).setTransitive(false).includeDependencies(checkDependencies)
                    .offline()
                    .setAcceptOptional(false)
                    .setIncludeInstallInformation(true)
                    .getResultDefinition();
        } catch (NutsNotFoundException e) {
            return false;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error", e);
            return false;
        }
        return installedRepository.isInstalled(nutToInstall.getId());
    }

    @Override
    public NutsExecCommand exec() {
        return new DefaultNutsExecCommand(this);
    }

    @Override
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsFetchCommand options) {
        options = CoreNutsUtils.validateSession(options, this);
        if (descriptor == null) {
            throw new NutsNotFoundException("<null>");
        }
        NutsId thisId = descriptor.getId();
        if (CoreNutsUtils.isEffectiveId(thisId)) {
            return thisId.setAlternative(descriptor.getAlternative());
        }
        String g = thisId.getGroup();
        String v = thisId.getVersion().getValue();
        if ((CoreStringUtils.isBlank(g)) || (CoreStringUtils.isBlank(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = fetch().id(parent).copyFrom(options).setEffective(true).setSession(options.getSession()).getResultId();
                if (CoreStringUtils.isBlank(g)) {
                    g = p.getGroup();
                }
                if (CoreStringUtils.isBlank(v)) {
                    v = p.getVersion().getValue();
                }
                if (!CoreStringUtils.isBlank(g) && !CoreStringUtils.isBlank(v)) {
                    break;
                }
            }
            if (CoreStringUtils.isBlank(g) || CoreStringUtils.isBlank(v)) {
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
                NutsDescriptor dd = fetch().id(parent).copyFrom(options).setEffective(true).getResultDescriptor();
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
                    fetch().id(parents[i]).setEffective(false).setSession(session).getResultDescriptor(),
                    session
            );
        }
        NutsDescriptor nutsDescriptor = descriptor.applyParents(parentDescriptors).applyProperties();
        NutsDependency[] old = nutsDescriptor.getDependencies();
        List<NutsDependency> newDeps = new ArrayList<>();
        boolean someChange = false;

        for (int i = 0; i < old.length; i++) {
            NutsDependency d = old[i];
            if (CoreStringUtils.isBlank(d.getScope())
                    || d.getVersion().isEmpty()
                    || CoreStringUtils.isBlank(d.getOptional())) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : nutsDescriptor.getStandardDependencies()) {
                    if (standardDependency.getSimpleName().equals(d.getId().getSimpleName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (CoreStringUtils.isBlank(d.getScope())
                            && !CoreStringUtils.isBlank(standardDependencyOk.getScope())) {
                        someChange = true;
                        d = d.setScope(standardDependencyOk.getScope());
                    }
                    if (CoreStringUtils.isBlank(d.getOptional())
                            && !CoreStringUtils.isBlank(standardDependencyOk.getOptional())) {
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
                for (NutsDependency dependency : fetch().id(d.getId()).setEffective(true).setSession(session).getResultDescriptor().getDependencies()) {
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
        all.add(CoreIOUtils.resolveJavaCommand(null));
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
        return CoreIOUtils.resolveOut(this, session);
    }

    protected void initializeWorkspace(String archetype, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (CoreStringUtils.isBlank(archetype)) {
            archetype = "default";
        }
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

    @Override
    public NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, this);
        if (nutToInstall != null && nutToInstall.getContent().getPath() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsDefinition runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetch().id(installerDescriptor.getId()).setSession(session)
                            .setTransitive(false)
                            .setAcceptOptional(false)
                            .includeDependencies()
                            .setIncludeInstallInformation(true)
                            .getResultDefinition();
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
    @Override
    public String[] getInstalledVersions(NutsId id) {
        return Arrays.stream(installedRepository.findInstalledVersions(id))
                .map(x -> x.getVersion().getValue())
                .sorted((a, b) -> DefaultNutsVersion.compareVersions(a, b))
                .toArray(String[]::new);
    }

    @Override
    public void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean resolveInstaller, boolean trace) {
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

    @Override
    public NutsExecutionContext createNutsExecutionContext(NutsDefinition nutToInstall, String[] args, String[] executorArgs, NutsSession session, boolean failFast, String commandName) {
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
                        fetch().session(session).setFetchStratery(NutsFetchStrategy.ONLINE)
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
                    || (CoreStringUtils.isBlank(adminSecurity.getAuthenticationAgent())
                    && CoreStringUtils.isBlank(adminSecurity.getCredentials()))) {
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

    @Override
    public List<NutsRepository> getEnabledRepositories(NutsWorkspaceHelper.FilterMode fmode, NutsId nutsId, NutsRepositoryFilter repositoryFilter, NutsSession session, NutsFetchMode mode, NutsFetchCommand options) {
        session = CoreNutsUtils.validateSession(session, this);
        return NutsWorkspaceHelper.filterRepositories(getEnabledRepositories(repositoryFilter), fmode, nutsId, repositoryFilter, session, mode, options);
    }

    public void checkSupportedRepositoryType(String type) {
        if (!config().isSupportedRepositoryType(type)) {
            throw new NutsIllegalArgumentException("Unsupported repository type " + type);
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
                s = CoreIOUtils.loadString(resource.openStream(),true);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if (!CoreStringUtils.isBlank(s)) {
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

    @Override
    public NutsIdBuilder createIdBuilder() {
        return new DefaultNutsIdBuilder();
    }

    @Override
    public NutsDependencyBuilder createDependencyBuilder() {
        return new DefaultNutsDependencyBuilder();
    }

    @Override
    public NutsFindCommand find() {
        return new DefaultNutsFindCommand(this);
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

    @Override
    public NutsDeployCommand deploy() {
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
    public NutsFetchCommand fetch() {
        return new DefaultNutsFetch(this);
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

    public DefaultNutsInstalledRepository getInstalledRepository() {
        return installedRepository;
    }

    /**
     * creates a zip file based on the folder. The folder should contain a
     * descriptor file at its root
     *
     * @param contentFolder folder to bundle
     * @param destFile created bundle file or null to create a file with the
     * very same name as the folder
     * @param session current session
     * @return bundled nuts file, the nuts is neither deployed nor installed!
     */
//    @Derecated
//    public NutsDefinition createBundle(Path contentFolder, Path destFile, NutsQueryOptions queryOptions, NutsSession session) {
//        session = CoreNutsUtils.validateSession(session, this);
//        if (Files.isDirectory(contentFolder)) {
//            NutsDescriptor descriptor = null;
//            Path ext = contentFolder.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
//            if (Files.exists(ext)) {
//                descriptor = parser().parseDescriptor(ext);
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
//    public boolean isFetched(NutsId id, NutsSession session) {
//        session = CoreNutsUtils.validateSession(session, this);
//        NutsSession offlineSession = session.copy();
//        try {
//            NutsDefinition found = fetch().id(id).offline().setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).getResultDefinition();
//            return found != null;
//        } catch (Exception e) {
//            return false;
//        }
//    }
}
