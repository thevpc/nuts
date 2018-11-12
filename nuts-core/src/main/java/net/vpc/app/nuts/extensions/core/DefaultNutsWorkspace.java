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
import net.vpc.app.nuts.NutsSearchBuilder;
import net.vpc.app.nuts.extensions.archetypes.DefaultNutsWorkspaceArchetypeComponent;
import net.vpc.app.nuts.extensions.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.extensions.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.extensions.filters.dependency.NutsExclusionDependencyFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.extensions.filters.id.NutsPatternIdFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsSimpleIdFilter;
import net.vpc.app.nuts.extensions.repos.NutsBootFolderRepository;
import net.vpc.app.nuts.extensions.repos.NutsFolderRepository;
import net.vpc.app.nuts.extensions.terminals.DefaultNutsTerminal;
import net.vpc.app.nuts.extensions.terminals.textparsers.DefaultNutsTextParser;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.common.io.*;
import net.vpc.common.mvn.PomId;
import net.vpc.common.mvn.PomIdResolver;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.CollectionUtils;
import net.vpc.common.util.IteratorList;

import java.io.*;
import java.lang.reflect.Proxy;
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
    private static final NutsDescriptor TEMP_DESC = new DefaultNutsDescriptorBuilder()
            .setId(CoreNutsUtils.parseNutsId("temp:exe#1.0"))
            .setExt("exe")
            .setPackaging("exe")
            .setExecutable(true)
            .setExecutor(new NutsExecutorDescriptor(CoreNutsUtils.parseNutsId("exec")))
            .build();
    private NutsFile nutsComponentId;
    private final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    private boolean initializing;
    private NutsRepository bootstrapNutsRepository;
    protected final DefaultNutsWorkspaceSecurityManager securityManager = new DefaultNutsWorkspaceSecurityManager(this);
    protected final DefaultNutsWorkspaceConfigManager configManager = new DefaultNutsWorkspaceConfigManager(this);
    protected DefaultNutsWorkspaceExtensionManager extensionManager;
    protected final DefaultNutsWorkspaceRepositoryManager repositoryManager = new DefaultNutsWorkspaceRepositoryManager(this);
    private NutsId platformOs;
    private NutsId platformArch;
    private NutsId platformOsdist;
    private NutsId platformOsLib;
    private Properties properties = new Properties();
    private NutsWorkspaceCreateOptions options;

    public DefaultNutsWorkspace() {

    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public NutsSession createSession() {
        return getExtensionManager().createSession();
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
        return workspaceListeners.toArray(new NutsWorkspaceListener[workspaceListeners.size()]);
    }

    @Override
    public NutsWorkspace openWorkspace(NutsWorkspaceCreateOptions options) {
        if (options == null) {
            options = new NutsWorkspaceCreateOptions();
        }
        NutsWorkspaceFactory newFactory = getExtensionManager().createSupported(NutsWorkspaceFactory.class, self());
        NutsWorkspace nutsWorkspace = getExtensionManager().createSupported(NutsWorkspace.class, self());
        if (options.isNoColors()) {
            nutsWorkspace.getProperties().setProperty("nocolors", "true");
        }
        NutsWorkspaceImpl nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
        if (nutsWorkspaceImpl.initializeWorkspace(configManager.getWorkspaceBoot(), newFactory,
                configManager.getWorkspaceBootId().toString(), configManager.getWorkspaceRuntimeId().toString(),
                options.getWorkspace(),
                configManager.getBootClassWorldURLs(),
                configManager.getBootClassLoader(), options.copy().setIgnoreIfFound(true))) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "workspace created : " + configManager.getWorkspaceBoot());
            }
        }
        return nutsWorkspace;
    }

    public NutsWorkspaceCreateOptions getOptions() {
        return options.copy();
    }

    public NutsBootOptions getBootOptions() {
        return getConfigManager().getBoot().getBootOptions();
    }

    @Override
    public boolean initializeWorkspace(NutsBootWorkspace workspaceBoot, NutsWorkspaceFactory factory, String workspaceBootId, String workspaceRuntimeId, String workspace,
                                       URL[] bootClassWorldURLs, ClassLoader bootClassLoader,
                                       NutsWorkspaceCreateOptions options) {

        if (options == null) {
            options = new NutsWorkspaceCreateOptions();
        }
        this.options = options;
        extensionManager = new DefaultNutsWorkspaceExtensionManager(self(), factory);
        configManager.onInitializeWorkspace(workspaceBoot,
                StringUtils.isEmpty(workspaceBoot.getNutsHomeLocation()) ? NutsConstants.DEFAULT_NUTS_HOME : workspaceBoot.getNutsHomeLocation(),
                factory, getExtensionManager().parseNutsId(workspaceBootId),
                getExtensionManager().parseNutsId(workspaceRuntimeId), resolveWorkspacePath(workspace),
                bootClassWorldURLs,
                bootClassLoader == null ? Thread.currentThread().getContextClassLoader() : bootClassLoader);

        boolean exists = isWorkspaceFolder(configManager.getWorkspaceLocation());
        if (!options.isCreateIfNotFound() && !exists) {
            throw new NutsWorkspaceNotFoundException(workspace);
        }
        if (!options.isIgnoreIfFound() && exists) {
            throw new NutsWorkspaceAlreadyExistsException(workspace);
        }

        this.bootstrapNutsRepository = new NutsBootFolderRepository(workspaceBoot, self(), null);

        extensionManager.oninitializeWorkspace(bootClassLoader);

        NutsSession session = createSession();

        initializing = true;
        try {
            if (!reloadWorkspace(options.isSaveIfCreated(), session, options.getExcludedExtensions(), options.getExcludedRepositories())) {
                if (!options.isCreateIfNotFound()) {
                    throw new NutsWorkspaceNotFoundException(workspace);
                }
                exists = false;
                configManager.setConfig(new NutsWorkspaceConfig());
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    getConfigManager().save();
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

    @Override
    public NutsFile fetchBootFile(NutsSession session) {
        session = validateSession(session);
        if (nutsComponentId == null) {
            nutsComponentId = fetch(NutsConstants.NUTS_ID_BOOT, session);
        }
        return nutsComponentId;
    }

    @Override
    public NutsFile install(String id, NutsConfirmAction foundAction, NutsSession session) {
        session = validateSession(session);
        if (foundAction == null) {
            foundAction = NutsConfirmAction.ERROR;
        }
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsFile nutToInstall = fetchWithDependencies(id, session);
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
            postInstall(nutToInstall, installer, session);
        }
        return nutToInstall;
    }

    @Override
    public NutsId commit(String folder, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_DEPLOY)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_DEPLOY);
        }
        if (folder == null || !new File(folder).isDirectory()) {
            throw new NutsIllegalArgumentException("Not a directory " + folder);
        }

        File file = new File(folder, NutsConstants.NUTS_DESC_FILE_NAME);
        NutsDescriptor d = CoreNutsUtils.parseNutsDescriptor(file);
        String oldVersion = StringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - NutsConstants.VERSION_CHECKED_OUT_EXTENSION.length());
            String newVersion = createVersion(oldVersion).inc().getValue();
            NutsFile newVersionFound = null;
            try {
                newVersionFound = fetch(d.getId().setVersion(newVersion).toString(), session);
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
    public NutsFile checkout(String id, String folder, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsFile nutToInstall = fetchWithDependencies(id, session);
        if ("zip".equals(nutToInstall.getDescriptor().getExt())) {

            ZipUtils.unzip(nutToInstall.getFile(), resolvePath(folder), new UnzipOptions().setSkipRoot(false));

            File file = new File(folder, NutsConstants.NUTS_DESC_FILE_NAME);
            NutsDescriptor d = CoreNutsUtils.parseNutsDescriptor(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().setVersion(oldVersion + NutsConstants.VERSION_CHECKED_OUT_EXTENSION);
            d = d.setId(newId);

            d.write(file, true);

            return new NutsFile(
                    newId,
                    d,
                    folder,
                    false,
                    false,
                    null
            );
        } else {
            throw new NutsUnsupportedOperationException("Checkout not supported");
        }
    }

    @Override
    public NutsUpdate checkUpdates(String id, NutsSession session) {
        session = validateSession(session);
        NutsId baseId = CoreNutsUtils.parseOrErrorNutsId(id);
        NutsVersion version = baseId.getVersion();
        NutsId oldId = null;
        NutsId newId = null;
        boolean runtime = false;
        URL runtimeURL = null;
        //if (version.isSingleValue()) {
        // check runtime value
        try {
            String urlPath = "/META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
            URL resource = Nuts.class.getResource(urlPath);
            if (resource != null) {
                runtimeURL = CorePlatformUtils.resolveURLFromResource(Nuts.class, urlPath);
                oldId = baseId.setVersion(IOUtils.loadProperties(resource).getProperty("version", "0.0.0"));
                runtime = true;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        if (oldId == null) {
            try {
                String urlPath = "/META-INF/nuts/" + baseId.getGroup() + "/" + baseId.getName() + "/nuts.properties";
                URL resource = Nuts.class.getResource(urlPath);
                if (resource != null) {
                    runtimeURL = CorePlatformUtils.resolveURLFromResource(Nuts.class, urlPath);
                    oldId = baseId.setVersion(IOUtils.loadProperties(resource).getProperty("project.version", "0.0.0"));
                    runtime = true;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        //}
        if (oldId == null) {
            if (version.isSingleValue()) {
                try {
                    oldId = bootstrapNutsRepository.fetchDescriptor(getExtensionManager().parseNutsId(id), session.setFetchMode(NutsFetchMode.OFFLINE)).getId();
                } catch (Exception ex) {
                    //ignore
                }
            } else {
                try {
                    oldId = bootstrapNutsRepository.fetchDescriptor(getExtensionManager().parseNutsId(id), session.setFetchMode(NutsFetchMode.OFFLINE)).getId();
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        NutsFile newFileId = null;
        try {
            newId = bootstrapNutsRepository.fetchDescriptor(getExtensionManager().parseNutsId(id), session.setFetchMode(NutsFetchMode.ONLINE)).getId();
            newFileId = bootstrapNutsRepository.fetch(newId, session);
        } catch (Exception ex) {
            //ignore
        }

        //compare canonical forms
        NutsId cnewId = toCanonicalForm(newId);
        NutsId coldId = toCanonicalForm(oldId);
        if (cnewId != null && (coldId == null || !cnewId.equals(coldId))) {
            String oldFile = runtimeURL == null ? null : URLUtils.toFile(runtimeURL).getPath();
            String newFile = newFileId == null ? null : newFileId.getFile();
            return new NutsUpdate(baseId, oldId, newId, oldFile, newFile, runtime);
        }
        return null;
    }

    @Override
    public NutsUpdate[] checkWorkspaceUpdates(boolean applyUpdates, String[] args, NutsSession session) {
        session = validateSession(session);
        List<NutsUpdate> found = new ArrayList<>();
        NutsUpdate r = checkUpdates(NutsConstants.NUTS_ID_BOOT, session);
        if (r != null) {
            found.add(r);
        }
        if (requiresCoreExtension()) {
            r = checkUpdates(getRuntimeId().getFullName(), session);
            if (r != null) {
                found.add(r);
            }
        }
        for (NutsId ext : getConfigManager().getExtensions()) {
            r = checkUpdates(ext.toString(), session);
            if (r != null) {
                found.add(r);
            }
        }
        NutsUpdate[] updates = found.toArray(new NutsUpdate[found.size()]);
        NutsPrintStream out = resolveOut(session);
        if (updates.length == 0) {
            out.printf("Workspace is [[up-to-date]]\n");
            return updates;
        } else {
            out.printf("Workspace has %s component%s to update\n", updates.length, (updates.length > 1 ? "s" : ""));
            for (NutsUpdate update : updates) {
                out.printf("%s  : %s => [[%s]]\n", update.getBaseId(), update.getLocalId(), update.getAvailableId());
            }
        }

        if (applyUpdates) {
            String myNutsJar = null;
            String newNutsJar = null;
            for (NutsUpdate update : updates) {
                if (update.getAvailableIdFile() != null && update.getOldIdFile() != null) {
                    if (update.getBaseId().getFullName().equals(NutsConstants.NUTS_ID_BOOT)) {
                        myNutsJar = update.getOldIdFile();
                        newNutsJar = update.getAvailableIdFile();
                    }
                }
            }

            if (myNutsJar != null) {
                List<String> all = new ArrayList<>();
                all.add("--apply-updates");
                all.add(FileUtils.getCanonicalPath(new File(myNutsJar)));
                for (String arg : args) {
                    if (!"--update".equals(arg) && !"--check-updates".equals(arg)) {
                        all.add(arg);
                    }
                }
                out.printf("applying nuts patch to [[%s]] ...\n", FileUtils.getCanonicalPath(new File(myNutsJar)));
                exec(newNutsJar, all.toArray(new String[all.size()]), false, false, session);
                return updates;
            }
        }
        return updates;
    }

    public NutsId getBootId() {
        String bootId = configManager.getWorkspaceBoot().getBootId();
        if (StringUtils.isEmpty(bootId)) {
            bootId = NutsConstants.NUTS_ID_BOOT;
        }
        return getExtensionManager().parseNutsId(bootId);
    }

    public NutsId getRuntimeId() {
        String runtimeId = configManager.getWorkspaceBoot().getRuntimeId();
        if (StringUtils.isEmpty(runtimeId)) {
            runtimeId = NutsConstants.NUTS_ID_RUNTIME;
        }
        return getExtensionManager().parseNutsId(runtimeId);
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
                if (ext.isSameFullName(getExtensionManager().parseNutsId(getRuntimeId().getFullName()))) {
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
        for (URLLocation r : extensionManager.getExtensionURLLocations(id.toString(), NutsConstants.NUTS_ID_BOOT, "properties")) {
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

    @Override
    public NutsFile updateWorkspace(String version, NutsConfirmAction foundAction, NutsSession session) {
        session = validateSession(session);
        String nutsIdStr = NutsConstants.NUTS_ID_BOOT + (StringUtils.isEmpty(version) ? "" : ("#") + version);
        NutsFile[] bootIdFile = bootstrapUpdate(nutsIdStr, foundAction, session);
        Properties bootInfo = getBootInfo(bootIdFile[0].getId());
        String runtimeId = bootInfo.getProperty("runtimeId");
        NutsFile[] runtimeIdFiles = bootstrapUpdate(runtimeId, foundAction, session);
        if (runtimeIdFiles.length == 0) {
            throw new NutsBootException("Unable to locate update for " + runtimeId);
        }
        Properties bootProperties = new Properties();
        final NutsId runtimeIdFile = runtimeIdFiles[0].getId();
        bootProperties.setProperty("runtimeId", runtimeIdFile.toString());
        NutsRepository[] repositories = getRepositoryManager().getRepositories();
        List<String> repositoryUrls = new ArrayList<>();
        repositoryUrls.add(expandPath(NutsConstants.URL_COMPONENTS_LOCAL));
        for (NutsRepository repository : repositories) {
            if (repository.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS) || repository.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN)) {
                repositoryUrls.add(repository.getConfigManager().getLocation());
            } else {
                for (NutsRepository mirror : repository.getMirrors()) {
                    if (mirror.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS) || repository.getRepositoryType().equals(NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN)) {
                        repositoryUrls.add(mirror.getConfigManager().getLocation());
                    }
                }
            }
        }
        String repositoriesPath = StringUtils.join(";", repositoryUrls);
        bootProperties.setProperty("repositories", repositoriesPath);
        File r = new File(bootstrapNutsRepository.getConfigManager().getLocationFolder());
        IOUtils.saveProperties(bootProperties, null, new File(r, CoreNutsUtils.getPath(runtimeIdFile, ".properties", File.separator)));

        Properties coreProperties = new Properties();
        List<String> dependencies = new ArrayList<>();
        for (NutsFile fetchDependency : runtimeIdFiles) {
            dependencies.add(fetchDependency.getId().setNamespace(null).toString());
        }
        coreProperties.put("project.id", runtimeIdFile.getFullName());
        coreProperties.put("project.version", runtimeIdFile.getVersion().toString());
        coreProperties.put("project.repositories", repositoriesPath);
        coreProperties.put("project.dependencies.compile", StringUtils.join(";", dependencies));
        IOUtils.saveProperties(coreProperties, null, new File(r, CoreNutsUtils.getPath(runtimeIdFile, ".properties", File.separator)));

        List<NutsFile> updatedExtensions = new ArrayList<>();
        for (NutsId ext : getConfigManager().getExtensions()) {
            NutsVersion nversion = ext.getVersion();
            if (!nversion.isSingleValue()) {
                //will update bootstrap workspace so that next time
                //it will be loaded
                NutsFile[] newVersion = bootstrapUpdate(ext.toString(), foundAction, session);
                if (!newVersion[0].getId().getVersion().equals(nversion)) {
                    updatedExtensions.add(newVersion[0]);
                }
            }
        }
        if (updatedExtensions.size() > 0) {
            if (log.isLoggable(Level.INFO)) {
                log.log(Level.INFO, "Some extensions were updated. Nuts should be restarted for extensions to take effect.");
            }
        }
        return bootIdFile[0];
    }

    @Override
    public NutsFile[] update(String[] toUpdateIds, String[] toRetainDependencies, NutsConfirmAction foundAction, NutsSession session) {
        session = validateSession(session);
        Map<String, NutsFile> all = new HashMap<>();
        for (String id : new HashSet<>(Arrays.asList(toUpdateIds))) {
            NutsFile updated = update(id, foundAction, session);
            all.put(updated.getId().getFullName(), updated);
        }
        if (toRetainDependencies != null) {
            for (String d : new HashSet<>(Arrays.asList(toRetainDependencies))) {
                NutsDependency dd = CoreNutsUtils.parseNutsDependency(d);
                if (all.containsKey(dd.getFullName())) {
                    NutsFile updated = all.get(dd.getFullName());
                    if (!dd.getVersion().toFilter().accept(updated.getId().getVersion())) {
                        throw new NutsIllegalArgumentException(dd + " unsatisfied  : " + updated.getId().getVersion());
                    }
                }
            }
        }
        return all.values().toArray(new NutsFile[all.size()]);
    }

    @Override
    public NutsFile update(String id, NutsConfirmAction uptoDateAction, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsVersion version = CoreNutsUtils.parseOrErrorNutsId(id).getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException("Version is too restrictive. You would use fetch or install instead");
        }
        NutsFile nutToInstall = fetchWithDependencies(id, session);
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
            postInstall(nutToInstall, getInstaller(nutToInstall, session), session);
        }
        return nutToInstall;
    }

    @Override
    public boolean isInstalled(String id, boolean checkDependencies, NutsSession session) {
        session = validateSession(session);
        NutsFile nutToInstall = null;
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
    public boolean uninstall(String id, boolean deleteData, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_UNINSTALL)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_UNINSTALL);
        }
        NutsFile nutToInstall = fetchWithDependencies(id, session.copy().setTransitive(false));
        if (!isInstalled(nutToInstall, session)) {
            throw new NutsIllegalArgumentException(id + " Not Installed");
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        if (ii == null) {
            return false;
        }
        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutorDescriptor installer = descriptor.getInstaller();
        String installFolder = getStoreRoot(nutToInstall.getId(), RootFolderType.PROGRAMS);
        NutsExecutionContext executionContext = new NutsExecutionContextImpl(
                nutToInstall, new String[0],
                installer == null ? null : installer.getArgs(), installer == null ? null : installer.getProperties(),
                new Properties(),
                installFolder,
                session, self());
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
    public int exec(String[] cmd, Properties env, String dir, NutsSession session) {
        session = validateSession(session);
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException("Missing command");
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String id = cmd[0];
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_EXEC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_EXEC + " : " + id);
        }
        if (id.contains("/") || id.contains("\\")) {
            try (CharacterizedFile c = characterize(IOUtils.toInputStreamSource(id, "path", id, new File(getConfigManager().getCwd())), session)) {
                if (c.descriptor == null) {
                    //this is a native file?
                    c.descriptor = TEMP_DESC;
                }
                NutsFile nutToRun = new NutsFile(
                        c.descriptor.getId(),
                        c.descriptor,
                        ((File) c.contentFile.getSource()).getPath(),
                        false,
                        c.temps.size() > 0,
                        null
                );
                return exec(nutToRun, args, env, dir, session);
            }
        } else {
            NutsFile nutToRun = fetchWithDependencies(id, session);
            //load all needed dependencies!
            fetchDependencies(new NutsDependencySearch(nutToRun.getId()), session);
            return exec(nutToRun, args, env, dir, session);
        }
    }

    @Override
    public boolean isFetched(String id, NutsSession session) {
        session = validateSession(session);
        return isFetched(CoreNutsUtils.parseOrErrorNutsId(id), session);
    }

    @Override
    public NutsFile fetchWithDependencies(String id, NutsSession session) {
        session = validateSession(session);
        NutsFile fetched = fetch(id, session);
        fetchDependencies(new NutsDependencySearch(id), session);
        return fetched;
    }

    @Override
    public NutsFile fetch(String id, NutsSession session) {
        session = validateSession(session);
        return fetchSimple(CoreNutsUtils.parseOrErrorNutsId(id), session);
    }

    @Override
    public NutsFile[] fetchDependencies(NutsDependencySearch search, NutsSession session) {
        session = validateSession(session);
        return fetchDependencies(search, new NutsIdGraph(), session);
    }

    @Override
    public NutsId resolveId(String id, NutsSession session) {
        session = validateSession(session);
        NutsId nutsId = CoreNutsUtils.parseOrErrorNutsId(id);
        return resolveId(nutsId, session);
    }

    public NutsId resolveId0(NutsId id, NutsSession session) {
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
        session = validateSession(session);
        //add env parameters to fetch adequate nuts
        id = NutsWorkspaceHelper.configureFetchEnv(id, this);

        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            session = session.copy().setFetchMode(mode);
            try {
                if (id.getGroup() == null) {
                    String[] groups = getConfigManager().getImports();
                    for (String group : groups) {
                        try {
                            NutsId f = resolveId0(id.setGroup(group), session);
                            if (f != null) {
                                return f;
                            }
                        } catch (NutsNotFoundException ex) {
                            //not found
                        }
                    }
                    throw new NutsNotFoundException(id);
                }
                return resolveId0(id, session);
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        }
        throw new NutsNotFoundException(id);
    }

    @Override
    public NutsId findOne(NutsSearch search, NutsSession session) {
        List<NutsId> r = find(search, session);
        if (r.isEmpty()) {
            return null;
        }
        if (r.size() > 1) {
            throw new IllegalArgumentException("Too many results (" + r.size() + " but expected one only)");
        }
        return r.get(0);
    }

    @Override
    public NutsId findFirst(NutsSearch search, NutsSession session) {
        List<NutsId> r = find(search, session);
        if (r.isEmpty()) {
            return null;
        }
        return r.get(0);
    }

    @Override
    public List<NutsId> find(NutsSearch search, NutsSession session) {
        session = validateSession(session);
        List<NutsId> li = CollectionUtils.toList(findIterator(search, session));
        if (search.isSort()) {
            li.sort(new Comparator<NutsId>() {
                @Override
                public int compare(NutsId o1, NutsId o2) {
                    int x = o1.getFullName().compareTo(o2.getFullName());
                    if (x != 0) {
                        return x;
                    }
                    //latests versions first
                    x = o1.getVersion().compareTo(o2.getVersion());
                    return -x;
                }
            });
        }
        return li;
    }

    @Override
    public Iterator<NutsId> findIterator(NutsSearch search, NutsSession session) {
        session = validateSession(session);
        HashSet<String> someIds = new HashSet<>(Arrays.asList(search.getIds()));
        HashSet<String> goodIds = new HashSet<>();
        HashSet<String> wildcardIds = new HashSet<>();
        for (String someId : someIds) {
            if (NutsPatternIdFilter.containsWildcad(someId)) {
                wildcardIds.add(someId);
            } else {
                goodIds.add(someId);
            }
        }
        NutsRepositoryFilter repositoryFilter = CoreNutsUtils.createNutsRepositoryFilter(search.getRepositoryFilter());
        NutsVersionFilter versionFilter = CoreNutsUtils.createNutsVersionFilter(search.getVersionFilter());
        NutsDescriptorFilter descriptorFilter = CoreNutsUtils.createNutsDescriptorFilter(search.getDescriptorFilter());
        NutsIdFilter idFilter = CoreNutsUtils.simplify(CoreNutsUtils.createNutsIdFilter(search.getIdFilter()));
        if (idFilter instanceof NutsPatternIdFilter) {
            NutsPatternIdFilter f = (NutsPatternIdFilter) idFilter;
            for (String id : f.getIds()) {
                if (NutsPatternIdFilter.containsWildcad(id)) {
                    wildcardIds.add(id);
                } else {
                    goodIds.add(id);
                }
            }
            idFilter = null;
        }
        if (idFilter instanceof NutsSimpleIdFilter) {
            NutsSimpleIdFilter f = (NutsSimpleIdFilter) idFilter;
            goodIds.add(f.getId().toString());
            idFilter = null;
        }
        if (!wildcardIds.isEmpty()) {
            NutsPatternIdFilter ff = new NutsPatternIdFilter(wildcardIds.toArray(new String[wildcardIds.size()]));
            idFilter = CoreNutsUtils.simplify(new NutsIdFilterOr(idFilter, ff));
        }
        if (goodIds.size() > 0) {
            IteratorList<NutsId> result = new IteratorList<>();
            for (String id : goodIds) {
                Iterator<NutsId> good = null;
                NutsId nutsId = getExtensionManager().parseNutsId(id);
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
                        NutsSearch search2 = new NutsSearch(search);
                        search2.setIds();
                        search2.setIdFilter(new NutsIdFilterOr(
                                new NutsPatternIdFilter(new String[]{nutsId.setGroup("*").toString()}),
                                CoreNutsUtils.simplify(CoreNutsUtils.createNutsIdFilter(search2.getIdFilter()))
                        ));
                        Iterator<NutsId> b = findIterator(search2, session);
                        b = CollectionUtils.nullifyIfEmpty(b);
                        if (b != null) {
                            result.addNonEmpty(b);
                        }
                    }
                }
            }
            if (search.isLastestVersions()) {
                return CoreNutsUtils.filterNutsIdByLatestVersion(CollectionUtils.toList(result)).iterator();
            }
            return result;
        }

        if (idFilter instanceof NutsPatternIdFilter) {
            String[] ids = ((NutsPatternIdFilter) idFilter).getIds();
            if (ids.length == 1) {
                String id = ids[0];
                if (id.indexOf('*') < 0 && id.indexOf(':') > 0) {
                    NutsId nid = getExtensionManager().parseNutsId(id);
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
                                if (search.isLastestVersions()) {
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
            IteratorList<NutsId> all = new IteratorList<NutsId>();
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
                if (search.isLastestVersions()) {
                    return CoreNutsUtils.filterNutsIdByLatestVersion(CollectionUtils.toList(b)).iterator();
                }
                return b;
            }
        }
        return Collections.emptyIterator();
    }

    @Override
    public NutsDescriptor fetchDescriptor(String idString, boolean effective, NutsSession session) {
        session = validateSession(session);
        long startTime = System.currentTimeMillis();
        NutsId id = CoreNutsUtils.parseOrErrorNutsId(idString);
        if (log.isLoggable(Level.FINEST)) {
            traceMessage(session, id, TraceResult.START, "Fetch descriptor", 0);
        }
        try {
            NutsDescriptor v = fetchDescriptor0(idString, effective, session);
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

    protected NutsDescriptor fetchDescriptor0(String idString, boolean effective, NutsSession session) {
        session = validateSession(session);
        NutsId id = CoreNutsUtils.parseOrErrorNutsId(idString);
        id = NutsWorkspaceHelper.configureFetchEnv(id, this);
        Set<String> errors = new LinkedHashSet<>();
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            session = session.copy().setFetchMode(mode);
            try {
                if (id.getGroup() == null) {
                    String[] groups = getConfigManager().getImports();
                    for (String group : groups) {
                        try {
                            NutsDescriptor f = fetchDescriptor0(id.setGroup(group).toString(), effective, session);
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

                for (NutsRepository repo : getEnabledRepositories(id, repositoryFilter, session)) {
                    try {
                        NutsDescriptor child = repo.fetchDescriptor(id, session);
                        if (child != null) {
//                            if (StringUtils.isEmpty(child.getId().getNamespace())) {
//                                child = child.setId(child.getId().setNamespace(repo.getRepositoryId()));
//                            }
                            if (effective) {
                                try {
                                    return resolveEffectiveDescriptor(child, session);
                                } catch (NutsNotFoundException ex) {
                                    if (log.isLoggable(Level.FINE)) {
                                        log.log(Level.FINE, "Unable to resolve Effective descriptor for " + idString);
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
        throw new NutsNotFoundException(idString, StringUtils.join("\n", errors), null);
    }

    @Override
    public String fetchHash(String id, NutsSession session) {
        session = validateSession(session);
        long startTime = System.currentTimeMillis();
        NutsId nutsId = CoreNutsUtils.parseOrErrorNutsId(id);
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
        session = validateSession(session);
        NutsId nutsId = CoreNutsUtils.parseOrErrorNutsId(id);
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
        session = validateSession(session);
        NutsId nid = CoreNutsUtils.parseOrErrorNutsId(id);
        session = validateSession(session);
        NutsRepositoryFilter repositoryFilter = null;
        if (StringUtils.trim(nid.getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            throw new NutsIllegalArgumentException("Invalid Version " + nid.getVersion());
        }
        NutsSession nonTransitiveSession = session.copy().setTransitive(false);
        NutsFile file = fetch(id, nonTransitiveSession);
        if (file == null) {
            throw new NutsIllegalArgumentException("Nothing to push");
        }
        if (StringUtils.isEmpty(repositoryId)) {
            Set<String> errors = new LinkedHashSet<>();
            for (NutsRepository repo : getEnabledRepositories(file.getId(), repositoryFilter, session)) {
                NutsFile id2 = null;
                try {
                    id2 = repo.fetch(file.getId(), session);
                } catch (Exception e) {
                    errors.add(StringUtils.exceptionToString(e));
                    //
                }
                if (id2 != null && repo.isSupportedMirroring()) {
                    try {
                        repo.push(nid, repositoryId, foundAction, session);
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
    public NutsFile createBundle(String contentFolder, String destFile, NutsSession session) {
        session = validateSession(session);
        File contentFolderObj = new File(contentFolder);
        if (contentFolderObj.isDirectory()) {
            NutsDescriptor descriptor = null;
            File ext = new File(contentFolder, NutsConstants.NUTS_DESC_FILE_NAME);
            if (ext.exists()) {
                descriptor = CoreNutsUtils.parseNutsDescriptor(ext);
            } else {
                descriptor = resolveNutsDescriptorFromFileContent(IOUtils.toInputStreamSource(contentFolderObj, new File(getConfigManager().getCwd())), session);
            }
            if (descriptor != null) {
                if ("zip".equals(descriptor.getExt())) {
                    if (destFile == null) {
                        destFile = resolvePath(contentFolderObj.getParent()
                                + "/" + descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip");
                    }
                    ZipUtils.zip(contentFolderObj.getPath(), new ZipOptions(), destFile);
                    return new NutsFile(
                            descriptor.getId(),
                            descriptor,
                            destFile,
                            true,
                            false,
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
        session = validateSession(session);
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
                NutsId p = resolveEffectiveId(fetchDescriptor(parent.toString(), false, session), session);
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
        session = validateSession(session);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetchDescriptor(parents[i].toString(), false, session),
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
                characterizedFile = characterize(contentSource, session);
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
        session = validateSession(session);
        return copyTo(CoreNutsUtils.parseOrErrorNutsId(id), session, localPath);
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
    public int getSupportLevel(NutsBootWorkspace criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public int exec(String nutsJarFile0, String[] args, boolean copyCurrentToFile, boolean waitFor, NutsSession session) {
        File nutsJarFile = nutsJarFile0 == null ? null : new File(nutsJarFile0);
        session = validateSession(session);
        NutsPrintStream out = resolveOut(session);
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
                    args = all.toArray(new String[all.size()]);
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

    private NutsPrintStream resolveOut(NutsSession session) {
        session = validateSession(session);
        return (session == null || session.getTerminal() == null) ? getExtensionManager().createNullPrintStream() : session.getTerminal().getOut();
    }

    protected NutsSession validateSession(NutsSession session) {
        if (session == null) {
            session = createSession();
        }
        return session;
    }

    protected void initializeWorkspace(String archetype, NutsSession session) {
        session = validateSession(session);
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

        NutsWorkspaceArchetypeComponent instance = getExtensionManager().createSupported(NutsWorkspaceArchetypeComponent.class, self());
        if (instance == null) {
            //get the default implementation
            instance = new DefaultNutsWorkspaceArchetypeComponent();
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        getSecurityManager().setUserCredentials(NutsConstants.USER_ADMIN, "admin");

        instance.initialize(self(), session);

//        //isn't it too late for adding extensions?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_ID_RUNTIME, session);
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, "Unable to load Nuts-core. The tool is running in minimal mode.");
//        }
    }

    protected NutsInstallerComponent getInstaller(NutsFile nutToInstall, NutsSession session) {
        session = validateSession(session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsFile runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetchWithDependencies(installerDescriptor.getId().toString(), session.copy().setTransitive(false));
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
        return null;
    }

    protected boolean isInstalled(NutsFile nutToInstall, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        if (ii == null) {
            return true;
        }

        NutsExecutorDescriptor installer = nutToInstall.getDescriptor().getInstaller();
        NutsExecutionContext executionContext = new NutsExecutionContextImpl(
                nutToInstall, new String[0], installer == null ? null : installer.getArgs(), null,
                installer == null ? null : installer.getProperties(),
                getStoreRoot(nutToInstall.getId(), RootFolderType.PROGRAMS),
                session, this);
        return ii.isInstalled(executionContext);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsId nutsId) {
        for (NutsExecutorComponent nutsExecutorComponent : getExtensionManager().createAll(NutsExecutorComponent.class)) {
            if (nutsExecutorComponent.getId().isSameFullName(nutsId)) {
                return nutsExecutorComponent;
            }
        }
        return new CustomNutsExecutorComponent(nutsId);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsFile nutsFile) {
        NutsExecutorComponent executorComponent = getExtensionManager().createSupported(NutsExecutorComponent.class, nutsFile);
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsNotFoundException("Nuts Executor not found for " + nutsFile);
    }

    protected int exec(NutsFile nutToRun, String[] appArgs, Properties env, String dir, NutsSession session) {
        session = validateSession(session);
        if (nutToRun != null && nutToRun.getFile() != null) {
            NutsDescriptor descriptor = nutToRun.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NutsExecutorDescriptor executor = descriptor.getExecutor();
            NutsExecutorComponent execComponent = null;
            String[] executrorArgs = null;
            Properties execProps = null;
            if (executor == null) {
                execComponent = resolveNutsExecutorComponent(nutToRun);
            } else {
                if (executor.getId() == null) {
                    execComponent = resolveNutsExecutorComponent(nutToRun);
                } else {
                    execComponent = resolveNutsExecutorComponent(executor.getId());
//                    NutsFile runnerFile = fetch(executor.getId(), session, true);
//                    execComponent = resolveNutsExecutorComponent(runnerFile);
                }
                executrorArgs = executor.getArgs();
                execProps = executor.getProperties();
            }
            boolean nowait = false;
            if (appArgs.length > 0 && "&".equals(appArgs[appArgs.length - 1])) {
                String[] arg2 = new String[appArgs.length - 1];
                System.arraycopy(appArgs, 0, arg2, 0, arg2.length);
                appArgs = arg2;
                nowait = true;
            }
            if (appArgs.length > 0 && ">null".equals(appArgs[appArgs.length - 1])) {
                String[] arg2 = new String[appArgs.length - 1];
                System.arraycopy(appArgs, 0, arg2, 0, arg2.length);
                appArgs = arg2;
                session = session.copy();
                NutsPrintStream nostream = getExtensionManager().createNullPrintStream();
                NutsTerminal t = getExtensionManager().createTerminal(null, nostream, nostream);
                session.setTerminal(t);
            }
            final NutsExecutionContext executionContext = new NutsExecutionContextImpl(nutToRun, appArgs, executrorArgs, env, execProps, dir, session, self(), nutToRun.getDescriptor().getExecutor());
            if (nowait) {
                final NutsExecutorComponent finalExecComponent = execComponent;
                NutsSession finalSession = session;
                Thread thread = new Thread("Exec-" + nutToRun.getId().toString()) {
                    @Override
                    public void run() {
                        try {
                            int result = finalExecComponent.exec(executionContext);
                        } catch (Exception e) {
                            e.printStackTrace(finalSession.getTerminal().getErr());
                        }
                    }
                };
                thread.setDaemon(true);
                thread.start();
                return 0;
            } else {
                return execComponent.exec(executionContext);
            }
        }
        throw new NutsNotFoundException("Nuts not found " + nutToRun);
    }

    protected NutsDependencyFilter createNutsDependencyFilter(NutsDependencyFilter filter, NutsId[] exclusions) {
        if (exclusions == null || exclusions.length == 0) {
            return filter;
        }
        return new NutsExclusionDependencyFilter(filter, exclusions);
    }

    protected NutsId deploy(String contentFile0, NutsDescriptor descriptor, String repositoryId, NutsConfirmAction foundAction, NutsSession session) {
        File contentFile = contentFile0 == null ? null : new File(contentFile0);
        session = validateSession(session);
        File tempFile = null;
        try {
            if (contentFile.isDirectory()) {
                File descFile = new File(contentFile, NutsConstants.NUTS_DESC_FILE_NAME);
                NutsDescriptor descriptor2;
                if (descFile.exists()) {
                    descriptor2 = CoreNutsUtils.parseNutsDescriptor(descFile);
                } else {
                    descriptor2 = resolveNutsDescriptorFromFileContent(IOUtils.toInputStreamSource(new File(contentFile, getConfigManager().getCwd())), session);
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
                    descriptor = resolveNutsDescriptorFromFileContent(IOUtils.toInputStreamSource(contentFile, new File(getConfigManager().getCwd())), session);
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
                CorePlatformUtils.checkSupportedOs(CoreNutsUtils.parseOrErrorNutsId(os).getFullName());
            }
            for (String arch : descriptor.getArch()) {
                CorePlatformUtils.checkSupportedArch(CoreNutsUtils.parseOrErrorNutsId(arch).getFullName());
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
        session = validateSession(session);
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
                        errors.add(StringUtils.exceptionToString(new NutsNotFoundException(id.toString())));
                    }
                } catch (Exception ex) {
                    errors.add(StringUtils.exceptionToString(ex));
                }
            }
        }
        throw new NutsNotFoundException(id.toString(), StringUtils.join("\n", errors), null);
    }

    protected void checkNutsId(NutsId id, String right) {
        if (id == null) {
            throw new NutsIllegalArgumentException("Missing id");
        }
        if (!getSecurityManager().isAllowed(right)) {
            throw new NutsSecurityException("Not Allowed " + right);
        }
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsIllegalArgumentException("Missing group");
        }
        if (StringUtils.isEmpty(id.getName())) {
            throw new NutsIllegalArgumentException("Missing name");
        }
    }

    protected NutsRepository getEnabledRepositoryOrError(String repoId) {
        NutsRepository r = getRepositoryManager().findRepository(repoId);
        if (r != null) {
            if (!isEnabledRepository(repoId)) {
                throw new NutsNotFoundException("Repository " + repoId + " is disabled.");
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

    private NutsDescriptor resolveNutsDescriptorFromFileContent(InputStreamSource localPath, NutsSession session) {
        session = validateSession(session);
        if (localPath != null) {
            List<NutsDescriptorContentParserComponent> allParsers = getExtensionManager().createAllSupported(NutsDescriptorContentParserComponent.class, self());
            if (allParsers.size() > 0) {
                String fileExtension = FileUtils.getFileExtension(localPath.getName());
                NutsDescriptorContentParserContext ctx = new DefaultNutsDescriptorContentParserContext(self(), session, localPath, fileExtension, null, null);
                for (NutsDescriptorContentParserComponent parser : allParsers) {
                    NutsDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (desc != null) {
                        return desc;
                    }
                }
            }
        }
        return null;
    }

    private String resolveWorkspacePath(String workspace) {
        if (StringUtils.isEmpty(workspace)) {
            File file = CoreIOUtils.resolvePath(getConfigManager().getNutsHomeLocation() + "/" + NutsConstants.DEFAULT_WORKSPACE_NAME, null, getConfigManager().getNutsHomeLocation());
            workspace = file == null ? null : file.getPath();
        } else {
            File file = CoreIOUtils.resolvePath(workspace, null, getConfigManager().getNutsHomeLocation());
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

    private boolean isInstallable(NutsFile nutToInstall, NutsSession session) {
        session = validateSession(session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsInstallerComponent nutsInstallerComponent = getInstaller(nutToInstall, session);
            if (nutsInstallerComponent == null) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void postInstall(NutsFile nutToInstall, NutsInstallerComponent installerComponent, NutsSession session) {
        if (nutToInstall == null) {
            return;
        }
        session = validateSession(session);
        boolean reinstall = nutToInstall.isInstalled();
        if (installerComponent != null) {
            if (nutToInstall.getFile() != null) {
                NutsExecutionContext executionContext = createNutsExecutionContext(nutToInstall, session);
                if (!installerComponent.isInstalled(executionContext)) {
                    installerComponent.install(executionContext);
                    nutToInstall.setInstalled(true);
                    String installFolder = getStoreRoot(nutToInstall.getId(), RootFolderType.PROGRAMS);
                    nutToInstall.setInstallFolder(installFolder);
                }
            }
        }
        for (NutsInstallListener nutsListener : session.getListeners(NutsInstallListener.class)) {
            nutsListener.onInstall(nutToInstall, reinstall, session);
        }
    }

    private NutsExecutionContext createNutsExecutionContext(NutsFile nutToInstall, NutsSession session) {
        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutorDescriptor installer = descriptor.getInstaller();
        String[] args = null;
        Properties props = null;
        String dir = null;
        if (installer != null) {
            args = installer.getArgs();
            props = installer.getProperties();

        }
        String installFolder = getStoreRoot(nutToInstall.getId(), RootFolderType.PROGRAMS);
        Properties env = new Properties();
        return new NutsExecutionContextImpl(nutToInstall, new String[0], args, env, props, installFolder, session, self());
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

    private NutsFile[] bootstrapUpdate(String id, NutsConfirmAction foundAction, NutsSession session) {
        session = validateSession(session);
        NutsFile[] deps = fetchDependencies(new NutsDependencySearch(id).setIncludeMain(true), session);
        for (NutsFile dep : deps) {
            if (dep.getFile() != null && !NutsConstants.DEFAULT_REPOSITORY_NAME.equals(dep.getId().getNamespace())) {
                bootstrapNutsRepository.deploy(dep.getId(),
                        dep.getDescriptor(),
                        dep.getFile(), foundAction,
                        session
                );
            }
        }
        return deps;
    }

    private NutsFile fetchSimple(NutsId id, NutsSession session) {
        if (log.isLoggable(Level.FINEST)) {
            traceMessage(session, id, TraceResult.START, "Fetch component", 0);
        }
        long startTime = System.currentTimeMillis();
        try {
            LinkedHashSet<String> errors = new LinkedHashSet<>();
            NutsFile main = null;
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
                        main = fetchHelperNutsFile(goodId, errors, session2, enabledRepositories);
                    } catch (NutsNotFoundException ex) {
                        //
                    }
                }
            }

            //try to load component from all repositories
            if (main == null) {
                main = fetchBestHelperNutsFile(session, goodId);
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
                    NutsExecutionContext executionContext = createNutsExecutionContext(main, session);
                    if (installer.isInstalled(executionContext)) {
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
            return main;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
    }

    private NutsFile[] fetchDependencies(NutsDependencySearch search, NutsIdGraph graph, NutsSession session) {
        session = validateSession(session);
        NutsDependencyFilter dependencyFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(
                search.getScope() == NutsDependencyScope.ALL ? null
                        : search.getScope() == NutsDependencyScope.RUN ? CoreNutsUtils.SCOPE_RUN
                        : search.getScope() == NutsDependencyScope.TEST ? CoreNutsUtils.SCOPE_TEST
                        : search.getScope() == NutsDependencyScope.ALL ? null : CoreNutsUtils.SCOPE_RUN,
                CoreNutsUtils.createNutsDependencyFilter(search.getDependencyFilter())
        ));

        Set<NutsId> mains = new HashSet<>();
        Stack<NutsFileAndNutsDependencyFilterItem> stack = new Stack<>();
        for (String sid : search.getIds()) {
            NutsId id = getExtensionManager().parseNutsId(sid);
            NutsFile main = null;
            try {
                main = fetchSimple(id, session);
            } catch (NutsNotFoundException ex) {
                if (search.isTrackNotFound()) {
                    search.getNoFoundResult().add(getExtensionManager().parseNutsId(ex.getNuts()));
                } else {
                    throw ex;
                }
            }
            if (main == null) {
                main = fetchBestHelperNutsFile(session, id);
            }
            mains.add(main.getId());
            stack.push(new NutsFileAndNutsDependencyFilterItem(main, dependencyFilter));
        }

        while (!stack.isEmpty()) {
            NutsFileAndNutsDependencyFilterItem curr = stack.pop();
            if (!graph.contains(curr.file.getId())) {
                if (curr.file.getDescriptor() != null) {
                    graph.set(curr.file);
                    for (NutsDependency dept : resolveEffectiveDescriptor(curr.file.getDescriptor(), session).getDependencies()) {
                        NutsId[] exclusions = dept.getExclusions();
                        if (dependencyFilter == null || dependencyFilter.accept(dept)) {
                            NutsId item = dept.toId();
                            //if (!graph.contains(curr.file.getId())) {
                            try {
                                NutsFile itemFile = fetchSimple(item, session);
                                graph.add(curr.file, itemFile);
                                if (!graph.contains(itemFile.getId())) {
                                    stack.push(new NutsFileAndNutsDependencyFilterItem(itemFile, createNutsDependencyFilter(curr.filter, exclusions)));
                                }
                            } catch (NutsNotFoundException ex) {
                                if (dept.isOptional()) {
                                    //ignore
                                } else if (!graph.contains(item)) {
                                    stack.push(new NutsFileAndNutsDependencyFilterItem(new NutsFile(item, null, null, false, false, null), createNutsDependencyFilter(curr.filter, exclusions)));
                                }
                            }
                            //}
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, Set<NutsId>> conflict : graph.resolveConflicts().entrySet()) {
            NutsVersion v = null;
            for (NutsId n : conflict.getValue()) {
                if (v == null || n.getVersion().compareTo(v) > 0) {
                    v = n.getVersion();
                }
            }
            for (NutsId n : conflict.getValue()) {
                if (!n.getVersion().equals(v)) {
                    graph.remove(n);
                }
            }
        }
        ArrayList<NutsFile> collected = new ArrayList<>();
        for (NutsId main : mains) {
            graph.visit(main, collected);
        }
        if (!search.isIncludeMain()) {
            Iterator<NutsFile> it = collected.iterator();
            while (it.hasNext()) {
                NutsFile next = it.next();
                if (mains.contains(next.getId())) {
                    it.remove();
                    break;
                }
            }
        }
        return collected.toArray(new NutsFile[collected.size()]);
    }

    private NutsFile fetchBestHelperNutsFile(NutsSession session, NutsId id) {
        LinkedHashSet<String> errors = new LinkedHashSet<>();
        NutsFile main = null;
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            if (main != null) {
                break;
            }
            NutsSession session2 = session.copy().setFetchMode(mode);
            main = fetchHelperNutsFile(id, errors, session2, getEnabledRepositories(id, repositoryFilter, session2.copy().setTransitive(true)));
        }
        if (main == null) {
            throw new NutsNotFoundException(id.toString(), StringUtils.join("\n", errors), null);
        }
        return main;
    }

    private NutsFile fetchHelperNutsFile(NutsId id, LinkedHashSet<String> errors, NutsSession session2, List<NutsRepository> enabledRepositories) {
        NutsFile found = null;
        try {
            for (NutsRepository repo : enabledRepositories) {
                NutsFile fetch = null;
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

    private CharacterizedFile characterize(InputStreamSource contentFile, NutsSession session) {
        session = validateSession(session);
        CharacterizedFile c = new CharacterizedFile();
        c.contentFile = contentFile;
        if (c.contentFile.getSource() instanceof File) {
            //okkay
        } else {
            File temp = CoreIOUtils.createTempFile(contentFile.getName(), false, null);
            IOUtils.copy(contentFile.open(), temp, true, true);
            c.contentFile = IOUtils.toInputStreamSource(temp, new File(getConfigManager().getCwd()));
            c.addTemp(temp);
            return characterize(IOUtils.toInputStreamSource(temp, new File(getConfigManager().getCwd())), session);
        }
        File fileSource = (File) c.contentFile.getSource();
        if ((!fileSource.exists())) {
            throw new NutsIllegalArgumentException("File does not exists " + fileSource);
        }
        if (fileSource.isDirectory()) {
            File ext = new File(fileSource, NutsConstants.NUTS_DESC_FILE_NAME);
            if (ext.exists()) {
                c.descriptor = CoreNutsUtils.parseNutsDescriptor(ext);
            } else {
                c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile, session);
            }
            if (c.descriptor != null) {
                if ("zip".equals(c.descriptor.getExt())) {
                    File zipFilePath = new File(resolvePath(fileSource.getPath() + ".zip"));
                    ZipUtils.zip(fileSource.getPath(), new ZipOptions(), zipFilePath.getPath());
                    c.contentFile = IOUtils.toInputStreamSource(zipFilePath, new File(getConfigManager().getCwd()));
                    c.addTemp(zipFilePath);
                } else {
                    throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                }
            }
        } else if (fileSource.isFile()) {
            File ext = new File(resolvePath(fileSource.getPath() + "." + NutsConstants.NUTS_DESC_FILE_NAME));
            if (ext.exists()) {
                c.descriptor = CoreNutsUtils.parseNutsDescriptor(ext);
            } else {
                c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile, session);
            }
        } else {
            throw new NutsIllegalArgumentException("Path does not denote a valid file or folder " + c.contentFile);
        }

        return c;
    }

    protected boolean reloadWorkspace(boolean save, NutsSession session, String[] excludedExtensions, String[] excludedRepositories) {
        Set<String> excludedExtensionsSet = excludedExtensions == null ? null : new HashSet<String>(Arrays.asList(excludedExtensions));
        Set<String> excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<String>(Arrays.asList(excludedRepositories));
        session = validateSession(session);
        File file = CoreIOUtils.createFile(getConfigManager().getWorkspaceLocation(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        NutsWorkspaceConfig config = CoreJsonUtils.loadJson(file, NutsWorkspaceConfig.class);
        if (config != null) {
            repositoryManager.removeAllRepositories();
            configManager.setConfig(config);

            //extensions already wired... this is needless!
            for (NutsId extensionId : config.getExtensions()) {
                if (excludedExtensionsSet != null && CoreNutsUtils.finNutsIdByFullNameInStrings(extensionId, excludedExtensionsSet) != null) {
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
            for (NutsWorkspaceListener listener : workspaceListeners) {
                listener.onReloadWorkspace(self());
            }
            return true;
        }
        return false;
    }

    public List<NutsRepository> getEnabledRepositories(NutsId nutsId, NutsRepositoryFilter repositoryFilter, NutsSession session) {
        session = validateSession(session);
        return NutsWorkspaceHelper.filterRepositories(getEnabledRepositories(repositoryFilter), nutsId, repositoryFilter, session);
    }

    public void checkSupportedRepositoryType(String type) {
        if (!getRepositoryManager().isSupportedRepositoryType(type)) {
            throw new NutsIllegalArgumentException("Unsupported repository type " + type);
        }
    }

    protected class NutsFileAndNutsDependencyFilterItem {

        NutsFile file;
        NutsDependencyFilter filter;

        public NutsFileAndNutsDependencyFilterItem(NutsFile file, NutsDependencyFilter filter) {
            this.file = file;
            this.filter = filter;
        }
    }

    public boolean isFetched(NutsId id, NutsSession session) {
        session = validateSession(session);
        NutsSession offlineSession = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        try {
            NutsFile found = fetch(id.toString(), offlineSession);
            return found != null;
        } catch (Exception e) {
            return false;
        }
    }

    private NutsWorkspace _self;

    @Override
    public NutsWorkspace self() {
        if (_self == null) {
            _self = (NutsWorkspace) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{
                    NutsWorkspace.class,
                    NutsWorkspaceImpl.class
            }, NutsEnvironmentContext.createHandler((NutsWorkspace) this));
        }
        return _self;
    }

    @Override
    public String getStoreRoot(String id, RootFolderType folderType) {
        return getStoreRoot(getExtensionManager().parseNutsId(id), folderType);
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
                getConfigManager().getNutsHomeLocation()).getPath();
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configManager
                + '}';
    }

    @Override
    public NutsId createNutsId(String id) {
        return getExtensionManager().parseNutsId(id);
    }

    @Override
    public NutsId createNutsId(String namespace, String group, String name, String version, String query) {
        return new NutsIdImpl(
                namespace, group, name, version, query
        );
    }

    @Override
    public NutsId createNutsId(String namespace, String group, String name, String version, Map<String, String> query) {
        return new NutsIdImpl(
                namespace, group, name, version, query
        );
    }

    public NutsId createNutsId(String group, String name, String version) {
        return new NutsIdImpl(
                null, group, name, version, (Map<String, String>) null
        );
    }

    @Override
    public NutsId resolveNutsIdForClass(Class clazz) {
        PomId u = PomIdResolver.resolvePomId(clazz, null);
        if (u == null) {
            return null;
        }
        return getExtensionManager().parseNutsId(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public NutsId[] resolveNutsIdsForClass(Class clazz) {
        PomId[] u = PomIdResolver.resolvePomIds(clazz);
        NutsId[] all = new NutsId[u.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = createNutsId(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
    }

    @Override
    public NutsId getPlatformOs() {
        if (platformOs == null) {
            platformOs = getExtensionManager().parseNutsId(CorePlatformUtils.getPlatformOs());
        }
        return platformOs;
    }

    @Override
    public NutsId getPlatformOsDist() {
        if (platformOsdist == null) {
            platformOsdist = getExtensionManager().parseNutsId(CorePlatformUtils.getPlatformOsDist());
        }
        return platformOsdist;
    }

    @Override
    public NutsId getPlatformOsLib() {
        if (platformOsLib == null) {
            platformOsLib = getExtensionManager().parseNutsId(CorePlatformUtils.getPlatformOsLib());
        }
        return platformOsLib;
    }

    @Override
    public NutsId getPlatformArch() {
        if (platformArch == null) {
            platformArch = getExtensionManager().parseNutsId(CorePlatformUtils.getPlatformArch());
        }
        return platformArch;
    }

    @Override
    public ClassLoader createClassLoader(String[] nutsIds, ClassLoader parentClassLoader, NutsSession session) {
        return getExtensionManager().createClassLoader(nutsIds, parentClassLoader, session);
    }

    @Override
    public ClassLoader createClassLoader(String[] nutsIds, NutsDependencyScope scope, ClassLoader parentClassLoader, NutsSession session) {
        return getExtensionManager().createClassLoader(nutsIds, scope, parentClassLoader, session);
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
                configManager.getNutsHomeLocation()).getPath();
    }

    @Override
    public NutsDescriptorBuilder createDescriptorBuilder() {
        return new DefaultNutsDescriptorBuilder();
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
    public NutsId parseOrErrorNutsId(String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    @Override
    public NutsVersion createVersion(String version) {
        return version==null?new NutsVersionImpl("") : new NutsVersionImpl(version);
    }

    @Override
    public NutsSearchBuilder createSearchBuilder() {
        return new DefaultNutsSearchBuilder();
    }

    @Override
    public String getNutsFileName(NutsId id, String ext) {
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
        return DefaultNutsTextParser.INSTANCE.filterText(value);
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
    public String resolveJavaMainClass(File file) {
        return CorePlatformUtils.resolveMainClass(file);
    }

    @Override
    public String[] resolveJavaMainClasses(File file) {
        return CorePlatformUtils.resolveMainClasses(file);
    }

    @Override
    public String[] resolveJavaMainClasses(InputStream inputStream) {
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
    public void reindex(String path) {
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
    public void reindexAll() {
        for (NutsRepository nutsRepository : getRepositoryManager().getRepositories()) {
            if (nutsRepository instanceof NutsFolderRepository) {
                ((NutsFolderRepository) nutsRepository).reindexFolder();
            }
        }
    }

    @Override
    public void downloadPath(String from, File to, NutsSession session) {
        CoreIOUtils.downloadPath(from,to,null,this,session);
    }

    @Override
    public String evalContentHash(InputStream input) {
        return CoreSecurityUtils.evalSHA1(input, false);
    }

}
