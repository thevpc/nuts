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

import net.vpc.app.nuts.core.config.NutsBootConfig;
import net.vpc.app.nuts.core.repos.DefaultNutsInstalledRepository;
import net.vpc.app.nuts.core.config.DefaultNutsWorkspaceCurrentConfig;
import net.vpc.app.nuts.core.config.DefaultNutsWorkspaceConfigManager;
import net.vpc.app.nuts.core.spi.NutsWorkspaceFactory;
import net.vpc.app.nuts.core.security.DefaultNutsWorkspaceSecurityManager;
import net.vpc.app.nuts.core.io.DefaultNutsIOManager;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.MapStringMapper;
import net.vpc.app.nuts.core.util.common.ObservableMap;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsSearchCommand;
import net.vpc.app.nuts.core.app.DefaultNutsCommandLine;
import net.vpc.app.nuts.core.format.DefaultNutsDependencyFormat;
import net.vpc.app.nuts.core.format.DefaultNutsIdFormat;
import net.vpc.app.nuts.core.format.DefaultNutsIncrementalOutputFormat;
import net.vpc.app.nuts.core.format.DefaultNutsInfoFormat;
import net.vpc.app.nuts.core.format.DefaultNutsObjectFormat;
import net.vpc.app.nuts.core.format.DefaultVersionFormat;
import net.vpc.app.nuts.core.format.elem.DefaultNutsElementFormat;
import net.vpc.app.nuts.core.format.json.DefaultNutsJsonFormat;
import net.vpc.app.nuts.core.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.core.format.table.DefaultTableFormat;
import net.vpc.app.nuts.core.format.tree.DefaultTreeFormat;
import net.vpc.app.nuts.core.format.xml.DefaultNutsXmlFormat;
import net.vpc.app.nuts.core.security.ReadOnlyNutsWorkspaceOptions;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsWorkspace implements NutsWorkspace, NutsWorkspaceSPI, NutsWorkspaceExt {

    public static final Logger LOG = Logger.getLogger(DefaultNutsWorkspace.class.getName());
    public static final NutsInstallInformation NOT_INSTALLED = new DefaultNutsInstallInfo(false, false, null, null, null);
    private final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    private final List<NutsInstallListener> installListeners = new ArrayList<>();
    private boolean initializing;
    protected final NutsWorkspaceSecurityManager securityManager = new DefaultNutsWorkspaceSecurityManager(this);
    protected NutsWorkspaceConfigManagerExt configManager;
    protected DefaultNutsWorkspaceExtensionManager extensionManager;
    private final ObservableMap<String, Object> userProperties = new ObservableMap<>();

    private NutsIOManager ioManager;
    private DefaultNutsInstalledRepository installedRepository;
    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();

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
    public Map<String, Object> userProperties() {
        return userProperties;
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new DefaultNutsSession(this);
        nutsSession.setTerminal(io().createTerminal(io().getSystemTerminal()));
        return nutsSession;
    }

    public NutsWorkspaceConfigManagerExt configExt() {
        return NutsWorkspaceConfigManagerExt.of(configManager);
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
    public void removeInstallListener(NutsInstallListener listener) {
        installListeners.add(listener);
    }

    @Override
    public void addInstallListener(NutsInstallListener listener) {
        if (listener != null) {
            installListeners.add(listener);
        }
    }

    @Override
    public NutsInstallListener[] getInstallListeners() {
        return installListeners.toArray(new NutsInstallListener[0]);
    }

    @Override
    public boolean initializeWorkspace(String workspaceLocation, String apiVersion, String runtimeId, String runtimeDependencies, String repositories, NutsWorkspaceOptions options, NutsBootWorkspaceFactory factory, URL[] bootClassWorldURLs, ClassLoader bootClassLoader) {
        if (options == null) {
            options = new ReadOnlyNutsWorkspaceOptions(new NutsDefaultWorkspaceOptions());
        } else {
            options = new ReadOnlyNutsWorkspaceOptions(options.copy());
        }
        if (options.getCreationTime() == 0) {
            configManager.setStartCreateTimeMillis(System.currentTimeMillis());
        }

        NutsBootConfig cfg = new NutsBootConfig();
        cfg.setWorkspace(workspaceLocation);
        cfg.setApiVersion(apiVersion);
        cfg.setRuntimeId(runtimeId);
        cfg.setRuntimeDependencies(runtimeDependencies);

        NutsWorkspaceFactory bb = (NutsWorkspaceFactory) factory;
        if (factory instanceof DefaultNutsWorkspaceFactory) {
            ((DefaultNutsWorkspaceFactory) factory).initialize(this);
        }
        installedRepository = new DefaultNutsInstalledRepository(this);
        ioManager = new DefaultNutsIOManager(this);
        extensionManager = new DefaultNutsWorkspaceExtensionManager(this, bb);
        configManager = new DefaultNutsWorkspaceConfigManager(this);
        configManager.onInitializeWorkspace(
                io().path(workspaceLocation),
                options,
                bootClassWorldURLs,
                bootClassLoader == null ? Thread.currentThread().getContextClassLoader() : bootClassLoader);
        configManager.setExcludedRepositories(options.getExcludedRepositories());
        extensionManager.setExcludedExtensions(options.getExcludedExtensions());
        boolean exists = NutsWorkspaceConfigManagerExt.of(config()).isValidWorkspaceFolder();
        NutsWorkspaceOpenMode openMode = options.getOpenMode();
        if (openMode != null) {
            switch (openMode) {
                case OPEN_EXISTING: {
                    if (!exists) {
                        throw new NutsWorkspaceNotFoundException(this, workspaceLocation);
                    }
                    break;
                }
                case CREATE_NEW: {
                    if (exists) {
                        throw new NutsWorkspaceAlreadyExistsException(this, workspaceLocation);
                    }
                    break;
                }
            }
        }
        extensionManager.onInitializeWorkspace(bootClassLoader);

        NutsSystemTerminalBase termb = extensions().createSupported(NutsSystemTerminalBase.class, null);

        io().setSystemTerminal(termb);
        io().setTerminal(io().createTerminal());
        NutsSession session = createSession();

        initializing = true;
        try {
            if (!loadWorkspace(session, options.getExcludedExtensions(), null)) {
                //workspace wasn't loaded. Create new configuration...
                NutsWorkspaceUtils.checkReadOnly(this);
                LOG.log(Level.CONFIG, "Workspace not found. Creating new one at {0}", config().getWorkspaceLocation());
                exists = false;
                NutsWorkspaceConfig config = new NutsWorkspaceConfig();
                //load from config with resolution applied
                config.setUuid(UUID.randomUUID().toString());
                config.setApiVersion(apiVersion);
                config.setRuntimeId(runtimeId);
                config.setRuntimeDependencies(runtimeDependencies);
                config.setBootRepositories(repositories);
                config.setJavaCommand(options.getJavaCommand());
                config.setJavaOptions(options.getJavaOptions());
                config.setStoreLocationStrategy(options.getStoreLocationStrategy());
                config.setRepositoryStoreLocationStrategy(options.getRepositoryStoreLocationStrategy());
                config.setStoreLocationLayout(options.getStoreLocationLayout());
                config.setGlobal(options.isGlobal());
                config.setStoreLocations(new NutsStoreLocationsMap(options.getStoreLocations()).toMapOrNull());
                config.setHomeLocations(new NutsHomeLocationsMap(options.getHomeLocations()).toMapOrNull());

                boolean namedWorkspace = CoreNutsUtils.isValidWorkspaceName(options.getWorkspace());
                if (config.getStoreLocationStrategy() == null) {
                    config.setStoreLocationStrategy(namedWorkspace ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
                }
                if (config.getRepositoryStoreLocationStrategy() == null) {
                    config.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                }
                config.setName(CoreNutsUtils.resolveValidWorkspaceName(options.getWorkspace()));

                configManager.setCurrentConfig(new DefaultNutsWorkspaceCurrentConfig(this).merge(config).build(config().getWorkspaceLocation()));
                configManager.setConfig(config, session);
                initializeWorkspace(options.getArchetype(), session);
                if (!config().isReadOnly()) {
                    config().save();
                }
                String nutsVersion = config().getRuntimeId().getVersion().toString();
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.log(Level.CONFIG, "nuts workspace v{0} created.", new Object[]{nutsVersion});
                }

                if (session.isPlainTrace()) {
                    PrintStream out = session.out();
                    out.printf("==nuts== workspace v[[%s]] created.%n", nutsVersion);
                }

                reconfigurePostInstall(session);
                DefaultNutsWorkspaceEvent workspaceCreatedEvent = new DefaultNutsWorkspaceEvent(session, null, null, null, null);
                for (NutsWorkspaceListener workspaceListener : workspaceListeners) {
                    workspaceListener.onCreateWorkspace(workspaceCreatedEvent);
                }
            } else {
                if (options.isRecover()) {
                    configManager.setBootApiVersion(cfg.getApiVersion());
                    configManager.setBootRuntime(cfg.getRuntimeId());
                    configManager.setBootRuntimeDependencies(cfg.getRuntimeDependencies());
                    configManager.setBootRepositories(cfg.getBootRepositories());
                    if (!config().isReadOnly()) {
                        config().save();
                    }
                }
            }
            if (configManager.getRepositoryRefs().length == 0) {
                LOG.log(Level.CONFIG, "Workspace has no repositrories. Will re-create defaults");
                initializeWorkspace(options.getArchetype(), session);
            }
            List<String> transientRepositoriesSet = options.getTransientRepositories() == null ? null : new ArrayList<>(Arrays.asList(options.getTransientRepositories()));
            for (String loc : transientRepositoriesSet) {
                String uuid = "transient_" + UUID.randomUUID().toString().replace("-", "");
                config()
                        .addRepository(
                                new NutsCreateRepositoryOptions()
                                        .setTemporary(true)
                                        .setName(uuid)
                                        .setFailSafe(false)
                                        .setLocation(loc)
                                        .setEnabled(true)
                        );
            }
            if (options.getUserName() != null && options.getUserName().trim().length() > 0) {
                char[] password = options.getPassword();
                if (CoreStringUtils.isBlank(password)) {
                    password = io().getTerminal().readPassword("Password : ");
                }
                this.security().login(options.getUserName(), password);
            }
            configManager.setStartCreateTimeMillis(options.getCreationTime());
            configManager.setEndCreateTimeMillis(System.currentTimeMillis());
//            if (!options.isReadOnly()) {
//                config().save(false);
//            }
            LOG.log(Level.FINE, "Nuts Workspace loaded in {0}", CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis()));
            if (CoreCommonUtils.getSysBoolNutsProperty("perf", false)) {
                session.out().printf("**Nuts** Workspace loaded in [[%s]]%n",
                        CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis())
                );
            }
        } finally {
            initializing = false;
        }
        return !exists;
    }

//    /**
//     * createConfig from Options
//     *
//     * @param config boot config. Should contain home,workspace, and all
//     * StoreLocation information
//     * @return resolved config
//     */
//    private NutsBootConfig createConfig(NutsWorkspaceOptions options) {
//        NutsBootConfig config = new NutsBootConfig(options);
//        String ws = options.getWorkspace();
//        int maxDepth = 36;
//        NutsBootConfig lastConfigLoaded = null;
//        String lastConfigPath = null;
//        String workspace0 = config.getWorkspace();
//        for (int i = 0; i < maxDepth; i++) {
//            lastConfigPath
//                    = CoreNutsUtils.isValidWorkspaceName(ws)
//                    ? NutsPlatformUtils.resolveHomeFolder(
//                            null, null, null,
//                            config.isGlobal(),
//                            CoreNutsUtils.resolveValidWorkspaceName(ws)
//                    ) : CoreIOUtils.getAbsolutePath(ws);
//
//            NutsBootConfig configLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastConfigPath);
//            if (configLoaded == null) {
//                //not loaded
//                break;
//            }
//            if (CoreStringUtils.isBlank(configLoaded.getWorkspace())) {
//                lastConfigLoaded = configLoaded;
//                break;
//            }
//            ws = configLoaded.getWorkspace();
//            if (i >= maxDepth - 1) {
//                throw new NutsIllegalArgumentException(null, "Cyclic Workspace resolution");
//            }
//        }
//        boolean namedWorkspace = CoreNutsUtils.isValidWorkspaceName(workspace0);
//        config.setWorkspace(lastConfigPath);
//        if (lastConfigLoaded != null) {
//            config.setWorkspace(lastConfigPath);
//            config.setName(lastConfigLoaded.getName());
//            config.setUuid(lastConfigLoaded.getUuid());
//            config.setApiVersion(lastConfigLoaded.getApiVersion());
//            config.setRuntimeId(lastConfigLoaded.getRuntimeId());
//            config.setRuntimeDependencies(lastConfigLoaded.getRuntimeDependencies());
//            config.setExtensionDependencies(lastConfigLoaded.getExtensionDependencies());
//            config.setRepositories(lastConfigLoaded.getBootRepositories());
//            config.setJavaCommand(lastConfigLoaded.getJavaCommand());
//            config.setJavaOptions(lastConfigLoaded.getJavaOptions());
//            config.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
//            config.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
//            config.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
//            config.setStoreLocations(lastConfigLoaded.getStoreLocations() == null ? null : new LinkedHashMap<>(lastConfigLoaded.getStoreLocations()));
//            config.setHomeLocations(lastConfigLoaded.getHomeLocations() == null ? null : new LinkedHashMap<>(lastConfigLoaded.getHomeLocations()));
//        }
//        if (CoreStringUtils.isBlank(config.getName())) {
//            config.setName(CoreNutsUtils.resolveValidWorkspaceName(workspace0));
//        }
//
//        Map<String, String> homeLocations = config.getHomeLocations();
//        final NutsOsFamily storeLocationLayout = config.getStoreLocationLayout();
//        if (storeLocationLayout == null) {
//            config.setStoreLocationLayout(null);
//        }
//        if (config.getStoreLocationStrategy() == null) {
//            config.setStoreLocationStrategy(namedWorkspace ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
//        }
//        if (config.getRepositoryStoreLocationStrategy() == null) {
//            config.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
//        }
//
//        String workspace = config.getWorkspace();
//        String[] homes = new String[NutsStoreLocation.values().length];
//        for (NutsStoreLocation type : NutsStoreLocation.values()) {
//            homes[type.ordinal()] = NutsPlatformUtils.resolveHomeFolder(storeLocationLayout, type, homeLocations,
//                    config.isGlobal(), config.getName());
//            if (CoreStringUtils.isBlank(homes[type.ordinal()])) {
//                throw new NutsIllegalArgumentException(this, "Missing Home for " + type.name().toLowerCase());
//            }
//        }
//        NutsStoreLocationStrategy storeLocationStrategy = config.getStoreLocationStrategy();
//        if (storeLocationStrategy == null) {
//            storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
//        }
//        Map<String, String> storeLocations = config.getStoreLocations() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(config.getStoreLocations());
//        for (NutsStoreLocation location : NutsStoreLocation.values()) {
//            String typeId = location.id();
//            switch (location) {
//                default: {
//                    if (CoreStringUtils.isBlank(storeLocations.get(typeId))) {
//                        switch (storeLocationStrategy) {
//                            case STANDALONE: {
//                                storeLocations.put(typeId, (workspace + File.separator + location.name().toLowerCase()));
//                                break;
//                            }
//                            case EXPLODED: {
//                                storeLocations.put(typeId, homes[location.ordinal()]);
//                                break;
//                            }
//                        }
//                    } else if (!CoreIOUtils.isAbsolutePath(storeLocations.get(typeId))) {
//                        switch (storeLocationStrategy) {
//                            case STANDALONE: {
//                                storeLocations.put(typeId, (workspace + File.separator + location.name().toLowerCase()));
//                                break;
//                            }
//                            case EXPLODED: {
//                                storeLocations.put(typeId, homes[location.ordinal()] + CoreIOUtils.syspath("/" + storeLocations.get(typeId)));
//                                break;
//                            }
//                        }
//                    }
//
//                }
//            }
//        }
//        config.setStoreLocations(storeLocations);
//        return config;
//    }
    public void reconfigurePostInstall(NutsSession session) {
        String nutsVersion = config().getRuntimeId().getVersion().toString();
        session = NutsWorkspaceUtils.validateSession(this, session);
        if (!config().options().isSkipCompanions()) {
            if (session.isPlainTrace()) {
                PrintStream out = session.out();

                StringBuilder version = new StringBuilder(nutsVersion);
                CoreStringUtils.fillString(' ', 25 - version.length(), version);
                out.println(io().loadFormattedString("/net/vpc/app/nuts/includes/standard-header.help", getClass().getClassLoader(), "no help found"));
                out.println("{{/------------------------------------------------------------------------------\\\\}}");
                out.println("{{|}}  This is the very {{first}} time ==Nuts== has been started for this workspace...     {{|}}");
                out.println("{{\\\\------------------------------------------------------------------------------/}}");
                out.println();
            }
            install().includeCompanions().session(session).run();
        }
    }

    @Override
    public String[] getCompanionTools() {
        return new String[]{
            "net.vpc.app.nuts.toolbox:nsh",
            "net.vpc.app.nuts.toolbox:nadmin",
            "net.vpc.app.nuts.toolbox:ndi", //            "net.vpc.app.nuts.toolbox:mvn"
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
    @Override
    public boolean requiresCoreExtension() {
        boolean coreFound = false;
        for (NutsId ext : extensions().getExtensions()) {
            if (ext.equalsSimpleName(config().getRuntimeId())) {
                coreFound = true;
                break;
            }
        }
        return !coreFound;
    }

    @Override
    public boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        NutsSession searchSession = session.copy().trace(false);
        NutsDefinition nutToInstall;
        try {
            nutToInstall = search().id(id).setSession(searchSession).setTransitive(false).inlineDependencies(checkDependencies)
                    .installed()
                    .setOptional(false)
                    .setInstallInformation(true)
                    .getResultDefinitions().first();
            if (nutToInstall == null) {
                return false;
            }
        } catch (NutsNotFoundException e) {
            return false;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error", e);
            return false;
        }
        return getInstalledRepository().isInstalled(nutToInstall.getId());
    }

    @Override
    public NutsExecCommand exec() {
        return new DefaultNutsExecCommand(this);
    }

    @Override
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsFetchCommand options) {
        options = NutsWorkspaceUtils.validateSession(this, options);
        if (descriptor == null) {
            throw new NutsNotFoundException(this, "<null>");
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
                NutsId p = fetch().copyFrom(options).id(parent).setEffective(true).setSession(options.getSession()).getResultId();
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
                throw new NutsNotFoundException(this, thisId, "Unable to fetchEffective for " + thisId + ". Best Result is " + thisId.toString(), null);
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
                NutsDescriptor dd = fetch().copyFrom(options).id(parent).setEffective(true).getResultDescriptor();
                bestId.apply(new MapStringMapper(dd.getProperties()));
                if (CoreNutsUtils.isEffectiveId(bestId)) {
                    return bestId.setAlternative(descriptor.getAlternative());
                }
                all.addAll(Arrays.asList(dd.getParents()));
            }
            throw new NutsNotFoundException(this, bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
        NutsId bestId = new DefaultNutsId(null, g, thisId.getName(), v, "");
        if (!CoreNutsUtils.isEffectiveId(bestId)) {
            throw new NutsNotFoundException(this, bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
        return bestId.setAlternative(descriptor.getAlternative());
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        Path eff = null;
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            Path l = config().getStoreLocation(descriptor.getId(), NutsStoreLocation.CACHE);
            String nn = config().getDefaultIdFilename(descriptor.getId().setFace("eff-nuts.cache"));
            eff = l.resolve(nn);
            if (Files.isRegularFile(eff)) {
                try {
                    NutsDescriptor d = descriptor().parse(eff);
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
        NutsDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor, session);
        if (eff == null) {
            Path l = config().getStoreLocation(effectiveDescriptor.getId(), NutsStoreLocation.CACHE);
            String nn = config().getDefaultIdFilename(effectiveDescriptor.getId().setFace("cache-eff-nuts"));
            eff = l.resolve(nn);
        }
        try {
            descriptor().value(effectiveDescriptor).print(eff);
        } catch (Exception ex) {
            //
        }
        return effectiveDescriptor;
    }

    protected NutsDescriptor _resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetch().id(parents[i]).setEffective(false).setSession(session).getResultDescriptor(),
                    session
            );
        }
        NutsDescriptor effectiveDescriptor = descriptor.applyParents(parentDescriptors).applyProperties();
        NutsDependency[] oldDependencies = effectiveDescriptor.getDependencies();
        List<NutsDependency> newDeps = new ArrayList<>();
        boolean someChange = false;

        for (NutsDependency d : oldDependencies) {
            if (CoreStringUtils.isBlank(d.getScope())
                    || d.getVersion().isBlank()
                    || CoreStringUtils.isBlank(d.getOptional())) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : effectiveDescriptor.getStandardDependencies()) {
                    if (standardDependency.getSimpleName().equals(d.getId().getShortName())) {
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
                    if (d.getVersion().isBlank()
                            && !standardDependencyOk.getVersion().isBlank()) {
                        someChange = true;
                        d = d.setVersion(standardDependencyOk.getVersion());
                    }
                }
            }

            if ("import".equals(d.getScope())) {
                someChange = true;
                newDeps.addAll(Arrays.asList(fetch().id(d.getId()).setEffective(true).setSession(session).getResultDescriptor().getDependencies()));
            } else {
                newDeps.add(d);
            }
        }
        if (someChange) {
            effectiveDescriptor = effectiveDescriptor.setDependencies(newDeps.toArray(new NutsDependency[0]));
        }
        return effectiveDescriptor;
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return extensionManager;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsWorkspaceOptions> criteria) {
        return DEFAULT_SUPPORT;
    }

    protected void initializeWorkspace(String archetype, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        if (CoreStringUtils.isBlank(archetype)) {
            archetype = "default";
        }
        NutsWorkspaceArchetypeComponent instance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NutsWorkspaceArchetypeComponent ac : extensions().createAllSupported(NutsWorkspaceArchetypeComponent.class, new DefaultNutsSupportLevelContext<>(this, archetype))) {
            if (archetype.equals(ac.getName())) {
                instance = ac;
                break;
            }
            validValues.add(ac.getName());
        }
        if (instance == null) {
            //get the default implementation
            throw new NutsException(this, "Invalid archetype " + archetype + ". Valid values are : " + validValues);
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        security().updateUser(NutsConstants.Users.ADMIN).setCredentials("admin".toCharArray()).run();

        instance.initialize(session);

//        //isn't it too late for adding extensions?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_ID_BOOT_RUNTIME, session);
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, "Unable to loadWorkspace Nuts-core. The tool is running in minimal mode.");
//        }
    }

    @Override
    public NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        if (nutToInstall != null && nutToInstall.getPath() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsDefinition runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetch().id(installerDescriptor.getId()).setSession(session)
                            .setTransitive(false)
                            .setOptional(false)
                            .content()
                            .dependencies()
                            .setInstallInformation(true)
                            .getResultDefinition();
                }
            }
            if (runnerFile == null) {
                runnerFile = nutToInstall;
            }
            NutsInstallerComponent best = extensions().createSupported(NutsInstallerComponent.class, new DefaultNutsSupportLevelContext<NutsDefinition>(this, runnerFile));
            if (best != null) {
                return best;
            }
        }
        return new CommandForIdNutsInstallerComponent();
    }

    /**
     * return installed parse
     *
     * @param id
     * @return
     */
    @Override
    public String[] getInstalledVersions(NutsId id, NutsSession session) {
        NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session,
                null, NutsFetchMode.INSTALLED, new DefaultNutsFetchCommand(this));
        return Arrays.stream(getInstalledRepository().findInstalledVersions(id, rsession))
                .map(x -> x.getVersion().getValue())
                .sorted((a, b) -> DefaultNutsVersion.compareVersions(a, b))
                .toArray(String[]::new);
    }

    @Override
    public void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, installerComponent, session, true, updateDefaultVersion, false);
    }

    @Override
    public void updateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, installerComponent, session, true, updateDefaultVersion, true);
    }

    public void installOrUpdateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean resolveInstaller, boolean updateDefaultVersion, boolean isUpdate) {
        if (def == null) {
            return;
        }
        def.getContent();
        def.getDependencies();
        def.getEffectiveDescriptor();
        def.getInstallInformation();
        if (resolveInstaller) {
            if (installerComponent == null) {
                if (def.getPath() != null) {
                    installerComponent = getInstaller(def, session);
                }
            }
        }
        session = NutsWorkspaceUtils.validateSession(this, session);
        boolean reinstall = def.getInstallInformation().isInstalled();
        PrintStream out = session.out();
        out.flush();
        if (installerComponent != null) {
            if (def.getPath() != null) {
                NutsExecutionContext executionContext = createNutsExecutionContext(def, args, new String[0], session,
                        true,
                        false,
                        config().options().getExecutionType(),
                        null);
                getInstalledRepository().install(executionContext.getDefinition().getId());
                if (isUpdate) {
                    try {
                        installerComponent.update(executionContext);
                    } catch (NutsReadOnlyException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (session.isPlainTrace()) {
                            out.printf("%N @@Failed@@ to update : %s.%n", id().value(def.getId()).format(), ex.toString());
                        }
                        throw new NutsExecutionException(this, "Unable to update " + def.getId().toString(), ex);
                    }
                } else {
                    try {
                        installerComponent.install(executionContext);
//                    out.print(getFormatManager().parse().print(def.getId()) + " installed ##successfully##.\n");
                    } catch (NutsReadOnlyException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (session.isPlainTrace()) {
                            out.printf("%N @@Failed@@ to install : %s.%n", id().value(def.getId()).format(), ex.toString());
                        }
                        try {
                            getInstalledRepository().uninstall(executionContext.getDefinition().getId());
                        } catch (Exception ex2) {
                            //ignore if we could not uninstall
                        }
                        throw new NutsExecutionException(this, "Unable to install " + def.getId().toString(), ex);
                    }
                }
                ((DefaultNutsDefinition) def).setInstallInformation(getInstalledRepository().getInstallInfo(def.getId()));
            }
        }
        if (isUpdate) {
            fireOnUpdate(new DefaultNutsInstallEvent(def, session, reinstall));
        } else {
            fireOnInstall(new DefaultNutsInstallEvent(def, session, reinstall));
        }
        ((DefaultNutsInstallInfo) def.getInstallInformation()).setJustInstalled(true);
        if (updateDefaultVersion) {
            getInstalledRepository().setDefaultVersion(def.getId());
        }
        if (session.isPlainTrace()) {
            String setAsDefaultString = "";
            if (updateDefaultVersion) {
                setAsDefaultString = " Set as ##default##.";
            }
            if (!def.getInstallInformation().isInstalled()) {
                if (!def.getContent().isCached()) {
                    if (def.getContent().isTemporary()) {
                        if (session.isPlainTrace()) {
                            out.printf("%N installed ##successfully## from temporarily file %s.%N%n", id().value(def.getId()).format(), def.getPath(), setAsDefaultString);
                        }
                    } else {
                        if (session.isPlainTrace()) {
                            out.printf("%N installed ##successfully## from remote repository.%N%n", id().value(def.getId()).format(), setAsDefaultString);
                        }
                    }
                } else {
                    if (def.getContent().isTemporary()) {
                        if (session.isPlainTrace()) {
                            out.printf("%N installed from local temporarily file %s.%N%n", id().value(def.getId()).format(), def.getPath(), setAsDefaultString);
                        }
                    } else {
                        if (session.isPlainTrace()) {
                            out.printf("%N installed from local repository.%N%n", id().value(def.getId()).format(), setAsDefaultString);
                        }
                    }
                }
            } else {
                if (session.isPlainTrace()) {
                    out.printf("%N installed ##successfully##.%N%n", id().value(def.getId()).format(), setAsDefaultString);
                }
            }
        }
    }

    @Override
    public void fireOnInstall(NutsInstallEvent event) {
        for (NutsInstallListener listener : getInstallListeners()) {
            listener.onInstall(event);
        }
        for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
            listener.onInstall(event);
        }
    }

    @Override
    public void fireOnUpdate(NutsInstallEvent event) {
        for (NutsInstallListener listener : getInstallListeners()) {
            listener.onUpdate(event);
        }
        for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
            listener.onUpdate(event);
        }
    }

    @Override
    public void fireOnUninstall(NutsInstallEvent event) {
        for (NutsInstallListener listener : getInstallListeners()) {
            listener.onUninstall(event);
        }
        for (NutsInstallListener listener : event.getSession().getListeners(NutsInstallListener.class)) {
            listener.onUninstall(event);
        }
    }

    @Override
    public NutsExecutionContext createNutsExecutionContext(
            NutsDefinition def,
            String[] args,
            String[] executorArgs,
            NutsSession session,
            boolean failFast,
            boolean temporary,
            NutsExecutionType executionType,
            String commandName
    ) {
        if (commandName == null) {
            commandName = resolveCommandName(def.getId());
        }
        NutsDescriptor descriptor = def.getDescriptor();
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
        Path installFolder = config().getStoreLocation(def.getId(), NutsStoreLocation.APPS);
        Properties env = new Properties();
        return new DefaultNutsExecutionContext(def, aargs.toArray(new String[0]), eargs.toArray(new String[0]), env, props, installFolder.toString(), session, this, failFast, temporary, executionType, commandName);
    }

    public String resolveCommandName(NutsId id) {
        String nn = id.getName();
        NutsWorkspaceCommandAlias c = config().findCommandAlias(nn);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getName() + "-" + id.getVersion();
        c = config().findCommandAlias(nn);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroup() + "." + id.getName() + "-" + id.getVersion();
        c = config().findCommandAlias(nn);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        throw new NutsElementNotFoundException(this, "Unable to resolve command name for " + id.toString());
    }

    protected boolean loadWorkspace(NutsSession session, String[] excludedExtensions, String[] excludedRepositories) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        if (configManager.loadWorkspace(session)) {
            //extensions already wired... this is needless!
            for (NutsId extensionId : extensions().getExtensions()) {
                if (extensionManager.isExcludedExtension(extensionId)) {
                    continue;
                }
                NutsSession sessionCopy = session.copy();
                extensionManager.wireExtension(extensionId,
                        fetch().session(sessionCopy).setFetchStratery(NutsFetchStrategy.ONLINE)
                                .setTransitive(true)
                );
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }
            NutsUserConfig adminSecurity = NutsWorkspaceConfigManagerExt.of(config()).getUser(NutsConstants.Users.ADMIN);
            if (adminSecurity == null || CoreStringUtils.isBlank(adminSecurity.getCredentials())) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.log(Level.CONFIG, NutsConstants.Users.ADMIN + " user has no credentials. reset to default");
                }
                security().updateUser(NutsConstants.Users.ADMIN).credentials("admin".toCharArray()).session(session).run();
            }
            for (NutsCommandAliasFactoryConfig commandFactory : config().getCommandFactories()) {
                try {
                    config().addCommandAliasFactory(commandFactory, new NutsAddOptions().session(session));
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Unable to instantiate Command Factory {0}", commandFactory);
                }
            }
            DefaultNutsWorkspaceEvent worksppaeReloadedEvent = new DefaultNutsWorkspaceEvent(session, null, null, null, null);
            for (NutsWorkspaceListener listener : workspaceListeners) {
                listener.onReloadWorkspace(worksppaeReloadedEvent);
            }
            //if save is needed, will be applied
            config().save(false);
            return true;
        }
        return false;
    }

    

//    @Override
//    public List<NutsRepository> getEnabledRepositories(Nuts upportedAction fmode, NutsId nutsId, NutsRepositoryFilter repositoryFilter, NutsSession session, NutsFetchMode mode, NutsFetchCommand options) {
//        return NutsWorkspaceUtils.filterRepositories(this, fmode, nutsId, repositoryFilter, mode, options);
//    }
    public void checkSupportedRepositoryType(String type) {
        if (!config().isSupportedRepositoryType(type)) {
            throw new NutsIllegalArgumentException(this, "Unsupported repository type " + type);
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
    public String resolveDefaultHelp(Class clazz) {
        NutsId nutsId = id().resolveId(clazz);
        if (nutsId != null) {
            String urlPath = "/" + nutsId.getGroup().replace('.', '/') + "/" + nutsId.getName() + ".help";
            return io().loadFormattedString(urlPath, getClass().getClassLoader(), "no help found");
        }
        return null;
    }

    @Override
    public NutsSearchCommand search() {
        return new DefaultNutsSearchCommand(this);
    }

    @Override
    public String getHelpText() {
        return this.io().loadFormattedString("/net/vpc/app/nuts/nuts-help.help", getClass().getClassLoader(), "no help found");
    }

    @Override
    public String getWelcomeText() {
        return this.io().loadFormattedString("/net/vpc/app/nuts/nuts-welcome.help", getClass().getClassLoader(), "no welcome found");
    }

    @Override
    public NutsCommandLine commandLine() {
        return new DefaultNutsCommandLine(this);
    }

    @Override
    public String getLicenseText() {
        return this.io().loadFormattedString("/net/vpc/app/nuts/nuts-license.help", getClass().getClassLoader(), "no license found");
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
    public NutsIOManager io() {
        return ioManager;
    }

    @Override
    public NutsFetchCommand fetch() {
        return new DefaultNutsFetchCommand(this);
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

    @Override
    public DefaultNutsInstalledRepository getInstalledRepository() {
        return installedRepository;
    }

    @Override
    public NutsUpdateStatisticsCommand updateStatistics() {
        return new DefaultNutsUpdateStatisticsCommand(this);
    }

    @Override
    public void fireOnAddRepository(NutsWorkspaceEvent event) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "{0} add    repo {1}", new Object[]{CoreStringUtils.alignLeft(this.config().getName(), 20),
                event.getRepository().config().name()});
        }
//        NutsWorkspaceEvent event = null;
        for (NutsWorkspaceListener listener : getWorkspaceListeners()) {
            listener.onAddRepository(event);
        }
        for (NutsWorkspaceListener listener : event.getSession().getListeners(NutsWorkspaceListener.class)) {
            listener.onAddRepository(event);
        }
    }

    @Override
    public void fireOnRemoveRepository(NutsWorkspaceEvent event) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "{0} remove repo {1}", new Object[]{CoreStringUtils.alignLeft(this.config().getName(), 20),
                event.getRepository().config().name()});
        }
        for (NutsWorkspaceListener listener : getWorkspaceListeners()) {
            listener.onRemoveRepository(event);
        }
        for (NutsWorkspaceListener listener : event.getSession().getListeners(NutsWorkspaceListener.class)) {
            listener.onRemoveRepository(event);
        }
    }

    @Override
    public void deployBoot(NutsSession session, NutsId id, boolean withDependencies) {
        Map<NutsId, NutsDefinition> todo = new HashMap<>();
        NutsDefinition m = fetch().id(id).content().dependencies().failFast(false).getResultDefinition();
        todo.put(m.getId().getLongNameId(), m);
        if (withDependencies) {
            for (NutsDependency dependency : m.getDependencies()) {
                if (!todo.containsKey(dependency.getId().getLongNameId())) {
                    m = fetch().id(id).content().dependencies().failFast(false).getResultDefinition();
                    todo.put(m.getId().getLongNameId(), m);
                }
            }
        }
        NutsWorkspaceConfigManager cfg = config();
        for (NutsDefinition def : todo.values()) {
            Path bootstrapFolder = cfg.getStoreLocation(NutsStoreLocation.CACHE).resolve(NutsConstants.Folders.BOOT);
            NutsId id2 = def.getId();
            this.io().copy().session(session).from(def.getPath())
                    .to(bootstrapFolder.resolve(cfg.getDefaultIdBasedir(id2))
                            .resolve(cfg.getDefaultIdFilename(id2.setFaceComponent().setPackaging("jar")))
                    ).run();
            this.descriptor().value(this.fetch().id(id2).getResultDescriptor())
                    .print(bootstrapFolder.resolve(cfg.getDefaultIdBasedir(id2))
                            .resolve(cfg.getDefaultIdFilename(id2.setFaceDescriptor())));

            Map<String, String> pr = new LinkedHashMap<>();
            pr.put("file.updated.date", Instant.now().toString());
            pr.put("project.id", def.getId().getShortNameId().toString());
            pr.put("project.name", def.getId().getShortNameId().toString());
            pr.put("project.version", def.getId().getVersion().toString());
            pr.put("repositories", "~/.m2/repository;https\\://raw.githubusercontent.com/thevpc/vpc-public-maven/master;http\\://repo.maven.apache.org/maven2/;https\\://raw.githubusercontent.com/thevpc/vpc-public-nuts/master");
//            pr.put("bootRuntimeId", runtimeUpdate.getAvailable().getId().getLongName());
            pr.put("project.dependencies.compile",
                    CoreStringUtils.join(";",
                            Arrays.stream(def.getDependencies())
                                    .filter(new Predicate<NutsDependency>() {
                                        @Override
                                        public boolean test(NutsDependency x) {
                                            return !x.isOptional() && NutsDependencyScopes.SCOPE_RUN.accept(def.getId(), x, session);
                                        }
                                    })
                                    .map(x -> x.getId().getLongName())
                                    .collect(Collectors.toList())
                    )
            );

            try (Writer writer = Files.newBufferedWriter(
                    bootstrapFolder.resolve(this.config().getDefaultIdBasedir(def.getId().getLongNameId()))
                            .resolve("nuts.properties")
            )) {
                CoreIOUtils.storeProperties(pr, writer, false);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
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
//                descriptor = parse().descriptor(ext);
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
//    public boolean isFetched(NutsId parse, NutsSession session) {
//        session = CoreNutsUtils.validateSession(session, this);
//        NutsSession offlineSession = session.copy();
//        try {
//            NutsDefinition found = fetch().parse(parse).offline().setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).getResultDefinition();
//            return found != null;
//        } catch (Exception e) {
//            return false;
//        }
//    }
    @Override
    public NutsJsonFormat json() {
        return new DefaultNutsJsonFormat(this);
    }

    @Override
    public NutsElementFormat element() {
        return new DefaultNutsElementFormat(this);
    }

    @Override
    public NutsXmlFormat xml() {
        return new DefaultNutsXmlFormat(this);
    }

    @Override
    public NutsIdFormat id() {
        return new DefaultNutsIdFormat(this);
    }

    @Override
    public NutsVersionFormat version() {
        return new DefaultVersionFormat(this);
    }

    @Override
    public NutsInfoFormat info() {
        return new DefaultNutsInfoFormat(this);
    }

    @Override
    public NutsDescriptorFormat descriptor() {
        return new DefaultNutsDescriptorFormat(this);
    }

    @Override
    public NutsIterableOutput iter() {
        return new DefaultNutsIncrementalOutputFormat(this);
    }

    @Override
    public NutsTableFormat table() {
        return new DefaultTableFormat(this);
    }

    @Override
    public NutsPropertiesFormat props() {
        return new DefaultPropertiesFormat(this);
    }

    @Override
    public NutsTreeFormat tree() {
        return new DefaultTreeFormat(this);
    }

    @Override
    public NutsObjectFormat object() {
        return new DefaultNutsObjectFormat(this);
    }

    @Override
    public NutsDependencyFormat dependency() {
        return new DefaultNutsDependencyFormat(this);
    }
}
