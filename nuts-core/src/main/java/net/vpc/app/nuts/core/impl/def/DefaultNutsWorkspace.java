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
package net.vpc.app.nuts.core.impl.def;

import net.vpc.app.nuts.core.*;
import net.vpc.app.nuts.core.impl.def.config.*;
import net.vpc.app.nuts.core.impl.def.installers.CommandForIdNutsInstallerComponent;
import net.vpc.app.nuts.core.impl.def.repos.DefaultNutsInstalledRepository;
import net.vpc.app.nuts.core.io.DefaultNutsIOManager;
import net.vpc.app.nuts.core.security.DefaultNutsWorkspaceSecurityManager;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.util.common.*;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsSearchCommand;
import net.vpc.app.nuts.core.security.ReadOnlyNutsWorkspaceOptions;
import net.vpc.app.nuts.core.impl.def.wscommands.*;

/**
 * Created by vpc on 1/6/17.
 */
@NutsPrototype
public class DefaultNutsWorkspace extends AbstractNutsWorkspace implements NutsWorkspaceExt {

    public static final Logger LOG = Logger.getLogger(DefaultNutsWorkspace.class.getName());
    public static final NutsInstallInformation NOT_INSTALLED = new DefaultNutsInstallInfo(false, false, null, null, null);
    private DefaultNutsInstalledRepository installedRepository;

    public DefaultNutsWorkspace(NutsWorkspaceInitInformation info) {
        userProperties = new DefaultObservableMap<>();
        securityManager=new DefaultNutsWorkspaceSecurityManager(this);
        String workspaceLocation=info.getWorkspaceLocation();
        String apiVersion=info.getApiVersion();
        String runtimeId=info.getRuntimeId();
        String runtimeDependencies=info.getRuntimeDependencies();
        String extensionDependencies=info.getExtensionDependencies();
        String repositories=info.getBootRepositories();
        NutsWorkspaceOptions uoptions=info.getOptions();
        NutsBootWorkspaceFactory bootFactory=info.getBootWorkspaceFactory();
        ClassLoader bootClassLoader=info.getClassWorldLoader();
        if (uoptions == null) {
            uoptions = new ReadOnlyNutsWorkspaceOptions(new NutsDefaultWorkspaceOptions());
        } else {
            uoptions = new ReadOnlyNutsWorkspaceOptions(uoptions.copy());
        }
        if (uoptions.getCreationTime() == 0) {
            configManager.setStartCreateTimeMillis(System.currentTimeMillis());
        }

        NutsBootConfig cfg = new NutsBootConfig();
        cfg.setWorkspace(workspaceLocation);
        cfg.setApiVersion(apiVersion);
        cfg.setRuntimeId(runtimeId);
        cfg.setRuntimeDependencies(runtimeDependencies);
        cfg.setExtensionDependencies(extensionDependencies);
        installedRepository = new DefaultNutsInstalledRepository(this);
        ioManager = new DefaultNutsIOManager(this);
        extensionManager = new DefaultNutsWorkspaceExtensionManager(this,bootFactory, uoptions.getExcludedExtensions());
        configManager = new DefaultNutsWorkspaceConfigManager(this,info);
        boolean exists = NutsWorkspaceConfigManagerExt.of(config()).isValidWorkspaceFolder();
        NutsWorkspaceOpenMode openMode = uoptions.getOpenMode();
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
            if (!loadWorkspace(session, uoptions.getExcludedExtensions(), null)) {
                //workspace wasn't loaded. Create new configuration...
                NutsWorkspaceUtils.checkReadOnly(this);
                LOG.log(Level.CONFIG, "[SUCCESS] Creating NEW workspace at {0}", config().getWorkspaceLocation());
                exists = false;
                NutsWorkspaceConfigBoot bconfig = new NutsWorkspaceConfigBoot();
                //load from config with resolution applied
                bconfig.setUuid(UUID.randomUUID().toString());
                NutsWorkspaceConfigApi aconfig=new NutsWorkspaceConfigApi();
                aconfig.setApiVersion(apiVersion);
                aconfig.setRuntimeId(runtimeId);
                aconfig.setJavaCommand(uoptions.getJavaCommand());
                aconfig.setJavaOptions(uoptions.getJavaOptions());

                NutsWorkspaceConfigRuntime rconfig=new NutsWorkspaceConfigRuntime();
                rconfig.setDependencies(runtimeDependencies);
                rconfig.setId(runtimeId);

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

                configManager.setCurrentConfig(new DefaultNutsWorkspaceCurrentConfig(this)
                        .merge(aconfig)
                        .merge(bconfig)
                        .build(config().getWorkspaceLocation()));
                NutsUpdateOptions updateOptions = new NutsUpdateOptions().session(session);
                configManager.setConfigBoot(bconfig, updateOptions);
                configManager.setConfigApi(aconfig, updateOptions);
                configManager.setConfigRuntime(rconfig, updateOptions);
                initializeWorkspace(uoptions.getArchetype(), session);
                if (!config().isReadOnly()) {
                    config().save(session);
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
                if (uoptions.isRecover()) {
                    NutsUpdateOptions updateOptions = new NutsUpdateOptions().session(session);
                    configManager.setBootApiVersion(cfg.getApiVersion(), updateOptions);
                    configManager.setBootRuntimeId(cfg.getRuntimeId(), updateOptions);
                    configManager.setBootRuntimeDependencies(cfg.getRuntimeDependencies(), updateOptions);
                    configManager.setBootRepositories(cfg.getBootRepositories(), updateOptions);
                }
            }
            if (configManager.getRepositoryRefs().length == 0) {
                LOG.log(Level.CONFIG, "Workspace has no repositories. Will re-create defaults");
                initializeWorkspace(uoptions.getArchetype(), session);
            }
            List<String> transientRepositoriesSet = uoptions.getTransientRepositories() == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(uoptions.getTransientRepositories()));
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
            configManager.prepareBoot(false);
            if (!config().isReadOnly()) {
                config().save(false,session);
            }
            configManager.setStartCreateTimeMillis(uoptions.getCreationTime());
            configManager.setEndCreateTimeMillis(System.currentTimeMillis());
            if (uoptions.getUserName() != null && uoptions.getUserName().trim().length() > 0) {
                char[] password = uoptions.getCredentials();
                if (CoreStringUtils.isBlank(password)) {
                    password = io().getTerminal().readPassword("Password : ");
                }
                this.security().login(uoptions.getUserName(), password);
            }
            LOG.log(Level.FINE, "[SUCCESS] Nuts Workspace loaded in {0}", CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis()));
            if (CoreCommonUtils.getSysBoolNutsProperty("perf", false)) {
                session.out().printf("**Nuts** Workspace loaded in [[%s]]%n",
                        CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis())
                );
            }
        } finally {
            initializing = false;
        }
//        return !exists;
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new DefaultNutsSession(this);
        nutsSession.setTerminal(io().createTerminal(io().getSystemTerminal()));
        return nutsSession;
    }

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
                String[] companionIds = getCompanionIds();
                out.println("Looking for recommended companion tools to install... detected : "+Arrays.stream(companionIds)
                        .map(x->id().set(id().parse(x)).format()).collect(Collectors.toList())
                );
            }
            install().companions().session(session).run();
            if (session.isPlainTrace()) {
                PrintStream out = session.out();
                out.println("Workspace is ##ready##!");
            }
        }
    }

    @Override
    public String[] getCompanionIds() {
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
            if (ext.equalsShortName(config().getRuntimeId())) {
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
//        if (CoreNutsUtils.isEffectiveId(thisId)) {
//            return thisId.setAlternative(descriptor.getAlternative());
//        }
        String g = thisId.getGroupId();
        String v = thisId.getVersion().getValue();
        if ((CoreStringUtils.isBlank(g)) || (CoreStringUtils.isBlank(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = fetch().copyFrom(options).id(parent).setEffective(true).setSession(options.getSession()).getResultId();
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
                throw new NutsNotFoundException(this, thisId, "Unable to fetchEffective for " + thisId + ". Best Result is " + thisId.toString(), null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v)) {
            Map<String, String> p = descriptor.getProperties();
            NutsId bestId = new DefaultNutsId(null, g, thisId.getArtifactId(), v, "");
            bestId = bestId.builder().apply(new MapStringMapper(p)).build();
//            if (CoreNutsUtils.isEffectiveId(bestId)) {
//                return bestId.setAlternative(descriptor.getAlternative());
//            }
            Stack<NutsId> all = new Stack<>();
            NutsId[] parents = descriptor.getParents();
            all.addAll(Arrays.asList(parents));
            while (!all.isEmpty()) {
                NutsId parent = all.pop();
                NutsDescriptor dd = fetch().copyFrom(options).id(parent).setEffective(true).getResultDescriptor();
                bestId=bestId.builder().apply(new MapStringMapper(dd.getProperties())).build();
//                if (CoreNutsUtils.isEffectiveId(bestId)) {
//                    return bestId.setAlternative(descriptor.getAlternative());
//                }
                all.addAll(Arrays.asList(dd.getParents()));
            }
            throw new NutsNotFoundException(this, bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
        NutsId bestId = new DefaultNutsId(null, g, thisId.getArtifactId(), v, "");
        if (!CoreNutsUtils.isEffectiveId(bestId)) {
            throw new NutsNotFoundException(this, bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
//        return bestId.setAlternative(descriptor.getAlternative());
        return bestId;
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        Path eff = null;
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            Path l = config().getStoreLocation(descriptor.getId(), NutsStoreLocation.CACHE);
            String nn = config().getDefaultIdFilename(descriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
            if (Files.isRegularFile(eff)) {
                try {
                    NutsDescriptor d = descriptor().parse(eff);
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Failed to parse  "+ eff,ex);
                    //
                }
            }
        } else {
            //System.out.println("Why");
        }
        NutsDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor, session);
        if (eff == null) {
            Path l = config().getStoreLocation(effectiveDescriptor.getId(), NutsStoreLocation.CACHE);
            String nn = config().getDefaultIdFilename(effectiveDescriptor.getId().builder().setFace("cache-eff-nuts").build());
            eff = l.resolve(nn);
        }
        try {
            descriptor().value(effectiveDescriptor).print(eff);
        } catch (Exception ex) {
            LOG.log(Level.FINE, "Failed to print "+ eff,ex);
            //
        }
        return effectiveDescriptor;
    }

    protected NutsDescriptor _resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        LOG.log(Level.CONFIG, "[START  ] Resolve Effective {0}", new Object[]{descriptor.getId()});
        session = NutsWorkspaceUtils.validateSession(this, session);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetch().id(parents[i]).setEffective(false).setSession(session).getResultDescriptor(),
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
                    if (standardDependency.getSimpleName().equals(d.getId().getShortName())) {
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
                newDeps.addAll(Arrays.asList(fetch().id(d.getId()).setEffective(true).setSession(session).getResultDescriptor().getDependencies()));
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
            NutsArtifactCall installerDescriptor = descriptor.getInstaller();
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
        if(session.isPlainTrace()){
            if(isUpdate){
                session.out().println("updating "+id().set(def.getId().getLongNameId()).format()+" ...");
            }else{
                session.out().println("installing "+id().set(def.getId().getLongNameId()).format()+" ...");
            }
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
        if(def.isApi()) {
            configManager.prepareBootApi(def.getId(), null, true);
        }else if(def.isRuntime()){
            configManager.prepareBootRuntime(def.getId(), true);
        }else if(def.isExtension()){
            configManager.prepareBootExtension(def.getId(), true);
        }
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
                            getInstalledRepository().uninstall(executionContext.getDefinition().getId(), session);
                        } catch (Exception ex2) {
                            LOG.log(Level.FINE, "Failed to uninstall  "+ executionContext.getDefinition().getId(),ex);
                            //ignore if we could not uninstall
                        }
                        throw new NutsExecutionException(this, "Unable to install " + def.getId().toString(), ex);
                    }
                }
                ((DefaultNutsDefinition) def).setInstallInformation(getInstalledRepository().getInstallInfo(def.getId()));
            }
        }
        if (isUpdate) {
            NutsWorkspaceUtils.Events.fireOnUpdate(this,new DefaultNutsInstallEvent(def, session, reinstall));
        } else {
            NutsWorkspaceUtils.Events.fireOnInstall(this,new DefaultNutsInstallEvent(def, session, reinstall));
        }
        ((DefaultNutsInstallInfo) def.getInstallInformation()).setJustInstalled(true);
        if (updateDefaultVersion) {
            getInstalledRepository().setDefaultVersion(def.getId(), session);
        }

        if(def.isExtension()){
            NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(config());
            NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getStoredConfigBoot().getExtensions())
                    .save();
            h.add(def.getId());
            wcfg.getStoredConfigBoot().setExtensions(h.getConfs());
            wcfg.fireConfigurationChanged("extensions",session, DefaultNutsWorkspaceConfigManager.ConfigEventType.BOOT);
            wcfg.prepareBootExtension(def.getId(),true);
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
                            out.printf("installed  %N ##successfully## from temporarily file %s.%N%n", id().value(def.getId().getLongNameId()).format(), def.getPath(), setAsDefaultString);
                        }
                    } else {
                        if (session.isPlainTrace()) {
                            out.printf("installed  %N ##successfully## from remote repository.%N%n", id().value(def.getId().getLongNameId()).format(), setAsDefaultString);
                        }
                    }
                } else {
                    if (def.getContent().isTemporary()) {
                        if (session.isPlainTrace()) {
                            out.printf("installed  %N from local temporarily file %s.%N%n", id().value(def.getId().getLongNameId()).format(), def.getPath(), setAsDefaultString);
                        }
                    } else {
                        if (session.isPlainTrace()) {
                            out.printf("installed  %N from local repository.%N%n", id().value(def.getId().getLongNameId()).format(), setAsDefaultString);
                        }
                    }
                }
            } else {
                if (session.isPlainTrace()) {
                    out.printf("installed  %N ##successfully##.%N%n", id().value(def.getId().getLongNameId()).format(), setAsDefaultString);
                }
            }
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
        NutsArtifactCall installer = descriptor.getInstaller();
        List<String> eargs = new ArrayList<>();
        List<String> aargs = new ArrayList<>();
        Map<String,String> props = null;
        if (installer != null) {
            if (installer.getArguments() != null) {
                eargs.addAll(Arrays.asList(installer.getArguments()));
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
        Map<String,String> env = new LinkedHashMap<>();
        return new DefaultNutsExecutionContext(def, aargs.toArray(new String[0]), eargs.toArray(new String[0]), env, props, installFolder.toString(), session, this, failFast, temporary, executionType, commandName);
    }

    public String resolveCommandName(NutsId id) {
        String nn = id.getArtifactId();
        NutsWorkspaceCommandAlias c = config().findCommandAlias(nn);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getArtifactId() + "-" + id.getVersion();
        c = config().findCommandAlias(nn);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroupId() + "." + id.getArtifactId() + "-" + id.getVersion();
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
            //config().save(false, session);
            return true;
        }
        return false;
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
            String urlPath = "/" + nutsId.getGroupId().replace('.', '/') + "/" + nutsId.getArtifactId() + ".help";
            return io().loadFormattedString(urlPath, clazz.getClassLoader(), "no help found");
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
    public NutsFetchCommand fetch() {
        return new DefaultNutsFetchCommand(this);
    }

    @Override
    public DefaultNutsInstalledRepository getInstalledRepository() {
        return installedRepository;
    }



    @Override
    public void deployBoot(NutsSession session, NutsId id, boolean withDependencies) {
        Map<NutsId, NutsDefinition> todo = new HashMap<>();
        NutsDefinition m = fetch().id(id).content().dependencies().failFast(false).getResultDefinition();
        Map<String,String> a=new LinkedHashMap<>();
        a.put("configVersion",Nuts.getVersion());
        a.put("id",id.getLongName());
        a.put("dependencies",Arrays.stream(m.getDependencies()).map(NutsDependency::getLongName).collect(Collectors.joining(";")));
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
                            .resolve(cfg.getDefaultIdFilename(id2.builder().setFaceContent().setPackaging("jar").build()))
                    ).run();
            this.descriptor().value(this.fetch().id(id2).getResultDescriptor())
                    .print(bootstrapFolder.resolve(cfg.getDefaultIdBasedir(id2))
                            .resolve(cfg.getDefaultIdFilename(id2.builder().setFaceDescriptor().build())));

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

//    /**
//     * creates a zip file based on the folder. The folder should contain a
//     * descriptor file at its root
//     *
//     * @return bundled nuts file, the nuts is neither deployed nor installed!
//     */
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
}
