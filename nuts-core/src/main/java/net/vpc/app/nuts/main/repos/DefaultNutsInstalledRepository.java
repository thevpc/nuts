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
package net.vpc.app.nuts.main.repos;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.WriteType;
import net.vpc.app.nuts.core.repos.AbstractNutsRepository;
import net.vpc.app.nuts.core.repos.NutsInstalledRepository;
import net.vpc.app.nuts.NutsInstallStatus;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt0;
import net.vpc.app.nuts.runtime.repocommands.*;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.io.FolderObjectIterator;
import net.vpc.app.nuts.runtime.util.io.NutsIdFilterTopInstalled;
import net.vpc.app.nuts.runtime.util.iter.IteratorBuilder;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;
import net.vpc.app.nuts.runtime.util.common.LRUMap;
import net.vpc.app.nuts.runtime.util.common.LazyIterator;
import net.vpc.app.nuts.runtime.DefaultNutsInstallInfo;

/**
 * @author vpc
 */
public class DefaultNutsInstalledRepository extends AbstractNutsRepository implements NutsInstalledRepository, NutsRepositoryExt0 {
    public static final String INSTALLED_REPO_UUID = "<main>";

    public static class InstallInfoConfig extends NutsConfigItem {

        private NutsId id;
        private boolean installed;
        private int dependencyCounter;
        private Instant installDate;
        private String installUser;

        public boolean isInstalled() {
            return installed;
        }

        public void setInstalled(boolean installed) {
            this.installed = installed;
        }

        public int getDependencyCounter() {
            return dependencyCounter;
        }

        public void setDependencyCounter(int dependencyCounter) {
            this.dependencyCounter = dependencyCounter;
        }

        public NutsId getId() {
            return id;
        }

        public String getInstallUser() {
            return installUser;
        }

        public void setInstallUser(String installUser) {
            this.installUser = installUser;
        }

        public void setId(NutsId id) {
            this.id = id;
        }

        public Instant getInstallDate() {
            return installDate;
        }

        public void setInstallDate(Instant installDate) {
            this.installDate = installDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InstallInfoConfig that = (InstallInfoConfig) o;
            return dependencyCounter == that.dependencyCounter &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(installDate, that.installDate) &&
                    Objects.equals(installUser, that.installUser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, dependencyCounter, installDate, installUser);
        }

        @Override
        public String toString() {
            return "InstallInfoConfig{" +
                    "id=" + id +
                    ", provided=" + dependencyCounter +
                    ", installDate=" + installDate +
                    ", installUser='" + installUser + '\'' +
                    '}';
        }
    }

    private static final String NUTS_INSTALL_FILE = "nuts-install.json";

    private final NutsRepositoryFolderHelper deployments;
    private final Map<NutsId, String> cachedDefaultVersions = new LRUMap<>(200);
    private final NutsLogger LOG;

    public DefaultNutsInstalledRepository(NutsWorkspace ws, NutsWorkspaceInitInformation info) {
        this.workspace = ws;
        deployments = new NutsRepositoryFolderHelper(this, ws, Paths.get(info.getStoreLocation(NutsStoreLocation.LIB)).resolve(NutsConstants.Folders.ID));
        configManager = new InstalledRepositoryConfigManager(ws);
        LOG = workspace.log().of(DefaultNutsInstalledRepository.class);
    }

    public Set<NutsId> getChildrenDependencies(NutsId id, NutsSession session) {
        return Collections.emptySet();
    }

    public Set<NutsId> getParentDependencies(NutsId id, NutsSession session) {
        return Collections.emptySet();
    }

    public void addDependency(NutsId id, NutsId parentId, NutsSession session) {

    }

    public void removeDependency(NutsId id, NutsId parentId, NutsSession session) {

    }

    @Override
    public boolean isDefaultVersion(NutsId id, NutsSession session) {
        String v = getDefaultVersion(id, session);
        return v.equals(id.getVersion().toString());
    }

    @Override
    public String getDefaultVersion(NutsId id, NutsSession session) {
        NutsId baseVersion = id.getShortNameId();
        synchronized (cachedDefaultVersions) {
            String p = cachedDefaultVersions.get(baseVersion);
            if (p != null) {
                return p;
            }
        }
        Path pp = workspace.config().getStoreLocation(id
                //.setAlternative("")
                .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).resolveSibling("default-version");
        String defaultVersion = "";
        if (Files.isRegularFile(pp)) {
            try {
                defaultVersion = new String(Files.readAllBytes(pp)).trim();
            } catch (IOException ex) {
                defaultVersion = "";
            }
        }
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, defaultVersion);
        }
        return defaultVersion;
    }

    @Override
    public void setDefaultVersion(NutsId id, NutsSession session) {
        NutsId baseVersion = id.getShortNameId();
        String version = id.getVersion().getValue();
        Path pp = workspace.config().getStoreLocation(id
//                .setAlternative("")
                .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).resolveSibling("default-version");
        if (CoreStringUtils.isBlank(version)) {
            if (Files.isRegularFile(pp)) {
                try {
                    Files.delete(pp);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        } else {
            try {
                Files.createDirectories(pp.getParent());
                Files.write(pp, version.trim().getBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, version);
        }
    }

    public NutsId pathToId(Path path) {
        Path rootFolder = workspace.config().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.ID);
        String p = path.toString().substring(rootFolder.toString().length());
        List<String> split = CoreStringUtils.split(p, "/\\");
        if (split.size() >= 4) {
            return workspace.id().builder().artifactId(split.get(split.size() - 3))
                    .groupId(CoreStringUtils.join(".", split.subList(0, split.size() - 3)))
                    .version(split.get(split.size() - 2)).build();

        }
        return null;
    }

    public InstallInfoConfig getInstallInfoConfig(NutsId id, Path path, NutsSession session) {
        if (id == null && path == null) {
            throw new IllegalArgumentException("Missing id or path");
        }
        if (path == null) {
            path = getPath(id, NUTS_INSTALL_FILE);
        }
        if (id == null) {
            path = getPath(id, NUTS_INSTALL_FILE);
        }
        Path finalPath = path;
        if (Files.isRegularFile(path)) {
            InstallInfoConfig c = workspace.io().lock().source(path).session(session).call(
                    () -> workspace.json().parse(finalPath, InstallInfoConfig.class)
                    , CoreNutsUtils.LOCK_TIME, CoreNutsUtils.LOCK_TIME_UNIT
            );
            if (c != null) {
                boolean changeStatus=false;
                NutsVersion v = workspace.version().parse(c.getConfigVersion());
                if (v.isBlank()) {
                    c.setInstalled(true);
                    c.setDependencyCounter(0);
                    c.setConfigVersion("0.5.8"); //last version before 0.6
                    changeStatus = true;
                }
                NutsId idOk = c.getId();
                if (idOk == null) {
                    if (id != null) {
                        c.setId(id);
                        changeStatus = true;
                    } else {
                        NutsId idOk2 = pathToId(path);
                        if (idOk2 != null) {
                            c.setId(idOk2);
                            changeStatus = true;
                        } else {
                            return null;
                        }
                    }
                }
                if (changeStatus && !workspace.config().isReadOnly()) {
                    workspace.io().lock().source(path).session(session).call(
                            () -> {
                                LOG.with().level(Level.CONFIG).log("Upgraded {0}",finalPath.toString());
                                c.setConfigVersion(workspace.config().getApiVersion());
                                workspace.json().value(c).print(finalPath);
                                return null;
                            }
                            , CoreNutsUtils.LOCK_TIME, CoreNutsUtils.LOCK_TIME_UNIT
                    );
                }
            }
            return c;
        }
        return null;
    }

    @Override
    public Iterator<NutsInstallInformation> searchInstallInformation(NutsSession session) {
        Path rootFolder = workspace.config().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.ID);
        return new FolderObjectIterator<NutsInstallInformation>(
                rootFolder,
                null, -1, session, new FolderObjectIterator.FolderIteratorModel<NutsInstallInformation>() {
            @Override
            public boolean isObjectFile(Path pathname) {
                return pathname.getFileName().toString().equals(NUTS_INSTALL_FILE);
            }

            @Override
            public NutsInstallInformation parseObject(Path path, NutsSession session) throws IOException {
                try {
                    InstallInfoConfig c = getInstallInfoConfig(null, path, session);
                    if (c != null) {
                        return getInstallInformation(c, session);
                    }
                } catch (Exception ex) {
                    LOG.with().error(ex).log("Unable to parse {0}", path);
                }
                return null;
            }
        }
        );
    }

    @Override
    public NutsInstallInformation getInstallInformation(NutsId id, NutsSession session) {
        InstallInfoConfig c = getInstallInfoConfig(id, null, session);
        return c != null ? getInstallInformation(c, session) : DefaultNutsInstallInfo.notInstalled(id);
    }

    public NutsInstallInformation getInstallInformation(InstallInfoConfig ii, NutsSession session) {
        boolean defaultVersion = false;
        if (ii.isInstalled()) {
            defaultVersion = isDefaultVersion(ii.getId(), session);
        }
        return new DefaultNutsInstallInfo(ii.getId(),
                ii.isInstalled() ? NutsInstallStatus.INSTALLED : NutsInstallStatus.INCLUDED,
                defaultVersion,
                workspace.config().getStoreLocation(ii.getId(), NutsStoreLocation.APPS),
                ii.getInstallDate(),
                ii.getInstallUser()
        );
    }

    @Override
    public NutsInstallStatus getInstallStatus(NutsId id, NutsSession session) {
        NutsInstallInformation ii = getInstallInformation(id, session);
        if (ii == null) {
            return NutsInstallStatus.NOT_INSTALLED;
        }
        return ii.getInstallStatus();
    }

    @Override
    public void uninstall(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.of(workspace).checkReadOnly();
        session = NutsWorkspaceUtils.of(workspace).validateSession(session);
        NutsInstallStatus installStatus = getInstallStatus(id, session);
        if (installStatus != NutsInstallStatus.INSTALLED) {
            throw new NutsNotInstalledException(workspace, id);
        }
        try {
            remove(id, NUTS_INSTALL_FILE);
            String v = getDefaultVersion(id, session);
            if (v != null && v.equals(id.getVersion().getValue())) {
                Iterator<NutsId> versions = searchVersions().setId(id)
                        .setFilter(new NutsIdFilterTopInstalled(NutsInstallStatus.INSTALLED)) //search only in installed, ignore deployed!
                        .setSession(NutsWorkspaceHelper.createRepositorySession(session, this, NutsFetchMode.LOCAL)).getResult();
                List<NutsId> nutsIds = CoreCommonUtils.toList(versions == null ? Collections.emptyIterator() : versions);
                nutsIds.sort(null);
                if (nutsIds.size() > 0) {
                    setDefaultVersion(nutsIds.get(0), session);
                } else {
                    setDefaultVersion(id.builder().setVersion("").build(), session);
                }
            }
            undeploy().setId(id)
                    .setSession(NutsWorkspaceHelper.createNoRepositorySession(session, NutsFetchMode.LOCAL))
                    .run();
        } catch (Exception ex) {
            throw new NutsNotInstalledException(workspace, id);
        }
    }


    @Override
    public void install(NutsId id, NutsSession session, NutsId forId) {
        Instant now = Instant.now();
        String user = workspace.security().getCurrentUsername();
        NutsWorkspaceUtils.of(workspace).checkReadOnly();
        InstallInfoConfig ii;
        try {
            ii = new InstallInfoConfig();
            ii.setId(id);
            ii.setInstallDate(now);
            ii.setInstallUser(user);
            ii.setInstalled(forId == null);
            ii.setDependencyCounter(forId == null ? 0 : 1);
            printJson(id, NUTS_INSTALL_FILE, ii);
        } catch (UncheckedIOException ex) {
            throw new NutsNotInstallableException(workspace, id.toString(), "Unable to install "
                    + id.builder().setNamespace(null).build() + " : " + ex.getMessage(), ex);
        }
    }

    @Override
    public NutsInstallInformation install(NutsDefinition def, NutsId forId, NutsSession session) {
        InstallInfoConfig ii = getInstallInfoConfig(def.getId(), null, session);
        boolean alreadyInstalled=false;
        if (ii == null) {
//            for (NutsDependency dependency : def.getDependencies()) {
//                Iterator<NutsId> it = searchVersions().setId(dependency.getId()).setSession(NutsWorkspaceHelper.createRepositorySession(session, this, NutsFetchMode.DEPLOYED)).getResult();
//                if (!it.hasNext()) {
//                    throw new IllegalArgumentException("Unable to install " + def.getId() + " as dependencies are missing.");
//                }
//            }
            if(forId!=null) {
                if(!deployments.isDeployed(def.getId(),def.getDescriptor())) {
                    this.deploy()
                            .setId(def.getId())
                            .setContent(def.getPath())
                            .setSession(NutsWorkspaceHelper.createNoRepositorySession(session.copy().yes(), NutsFetchMode.LOCAL))
                            .setDescriptor(def.getDescriptor())
                            .run();
                }
            }else{
                this.deploy()
                        .setId(def.getId())
                        .setContent(def.getPath())
                        .setSession(NutsWorkspaceHelper.createNoRepositorySession(session.copy().yes(), NutsFetchMode.LOCAL))
                        .setDescriptor(def.getDescriptor())
                        .run();
            }
            NutsId id = def.getId();
            Instant now = Instant.now();
            String user = workspace.security().getCurrentUsername();
            NutsWorkspaceUtils.of(workspace).checkReadOnly();
            try {
                ii = new InstallInfoConfig();
                ii.setId(id);
                ii.setInstallDate(now);
                ii.setInstallUser(user);
                ii.setInstalled(forId == null);
                ii.setDependencyCounter(forId != null ? 1 : 0);
                printJson(id, NUTS_INSTALL_FILE, ii);
            } catch (UncheckedIOException ex) {
                throw new NutsNotInstallableException(workspace, id.toString(), "Unable to install "
                        + id.builder().setNamespace(null).build() + " : " + ex.getMessage(), ex);
            }
            DefaultNutsInstallInfo uu = (DefaultNutsInstallInfo)getInstallInformation(ii, session);
            uu.setJustInstalled(true);
            return uu;
        } else {
            alreadyInstalled=ii.isInstalled();
            if (alreadyInstalled) {
                //already installed, will deploy only
                this.deploy()
                        .setId(def.getId())
                        .setContent(def.getPath())
                        .setSession(NutsWorkspaceHelper.createNoRepositorySession(session, NutsFetchMode.LOCAL))
                        .setDescriptor(def.getDescriptor())
                        .run();
            } else {
                if (forId != null) {
                    ii.setDependencyCounter(ii.getDependencyCounter() + 1);
                } else {
                    ii.setInstalled(true);
                }
                printJson(ii.getId(), NUTS_INSTALL_FILE, ii);
            }
            DefaultNutsInstallInfo uu = (DefaultNutsInstallInfo)getInstallInformation(ii, session);
            uu.setJustInstalled(true);
            uu.setJustReInstalled(alreadyInstalled);
            return uu;
        }
    }

    public void addString(NutsId id, String name, String value) {
        try {
            Files.write(getPath(id, name), value.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public <T> T readJson(NutsId id, String name, Class<T> clazz) {
        return workspace.json().parse(getPath(id, name), clazz);
    }

    public void printJson(NutsId id, String name, InstallInfoConfig value) {
        value.setConfigVersion(workspace.config().getApiVersion());
        workspace.json().value(value).print(getPath(id, name));
    }

    public void remove(NutsId id, String name) {
        try {
            Path path = getPath(id, name);
            Files.delete(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean contains(NutsId id, String name) {
        return Files.isRegularFile(getPath(id, name));
    }

    public Path getPath(NutsId id, String name) {
        return workspace.config().getStoreLocation(id, NutsStoreLocation.CONFIG).resolve(name);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of repository
    /////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public NutsRepositorySecurityManager security() {
        throw new IllegalArgumentException("Unsupported security() for " + getName() + " repository");
    }

    @Override
    public NutsDeployRepositoryCommand deploy() {
        return new AbstractNutsDeployRepositoryCommand(this) {
            @Override
            public NutsDeployRepositoryCommand run() {
                NutsDescriptor rep = deployments.deploy(this, WriteType.FORCE);
                this.setDescriptor(rep);
                this.setId(rep.getId());
                return this;
            }
        };
    }

    @Override
    public NutsRepositoryUndeployCommand undeploy() {
        return new AbstractNutsRepositoryUndeployCommand(this) {
            @Override
            public NutsRepositoryUndeployCommand run() {
                deployments.undeploy(this);
                return this;
            }
        };
    }

    @Override
    public NutsPushRepositoryCommand push() {
        return new AbstractNutsPushRepositoryCommand(this) {
            @Override
            public NutsPushRepositoryCommand run() {
                throw new IllegalArgumentException("Unsupported push() for " + getName() + " repository");
            }
        };
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand fetchDescriptor() {
        return new AbstractNutsFetchDescriptorRepositoryCommand(this) {
            @Override
            public NutsFetchDescriptorRepositoryCommand run() {
                result = deployments.fetchDescriptorImpl(getId(), getSession());
                return this;
            }
        };
    }

    @Override
    public NutsFetchContentRepositoryCommand fetchContent() {
        return new AbstractNutsFetchContentRepositoryCommand(this) {
            @Override
            public NutsFetchContentRepositoryCommand run() {
                result = deployments.fetchContentImpl(getId(), getLocalPath(), getSession());
                return this;
            }
        };
    }

    @Override
    public NutsSearchRepositoryCommand search() {
        return new AbstractNutsSearchRepositoryCommand(this) {
            @Override
            public NutsSearchRepositoryCommand run() {
                result = deployments.searchImpl(getFilter(), getSession());
                if (result == null) {
                    result = IteratorUtils.emptyIterator();
                }
                return this;
            }
        };
    }

    @Override
    public NutsSearchVersionsRepositoryCommand searchVersions() {
        return new AbstractNutsSearchVersionsRepositoryCommand(this) {
            @Override
            public NutsSearchVersionsRepositoryCommand run() {
                if (getFilter() instanceof NutsIdFilterTopInstalled) {
                    result = new LazyIterator<NutsId>() {
                        @Override
                        protected Iterator<NutsId> iterator() {
                            File installFolder = workspace.config().getStoreLocation(getId()
                                    .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).toFile().getParentFile();
                            if (installFolder.isDirectory()) {
                                final NutsVersionFilter filter0 = getId().getVersion().filter();
                                return IteratorBuilder.of(Arrays.asList(installFolder.listFiles()).iterator())
                                        .map(new Function<File, NutsId>() {
                                            @Override
                                            public NutsId apply(File folder) {
                                                if (folder.isDirectory()
                                                        && new File(folder, NUTS_INSTALL_FILE).isFile()) {
                                                    NutsVersion vv = workspace.version().parse(folder.getName());
                                                    NutsIdFilter filter = getFilter();
                                                    NutsSession session = getSession().getSession();
                                                    if (filter0.accept(vv, session) && (filter == null || filter.accept(
                                                            getId().builder().setVersion(vv).build()
                                                            , session))) {
                                                        return getId().builder().setVersion(folder.getName()).build();
                                                    }
                                                }
                                                return null;
                                            }

                                        })
                                        .notNull().iterator();
                            }
                            //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
                            return IteratorUtils.emptyIterator();
                        }
                    };
                } else {
                    this.result = IteratorUtils.nonNull(deployments.searchVersions(getId(), getFilter(), true, getSession()));
                }
                return this;
            }
        };
    }

    @Override
    public NutsUpdateRepositoryStatisticsCommand updateStatistics() {
        return new AbstractNutsUpdateRepositoryStatisticsCommand(this) {
            @Override
            public NutsUpdateRepositoryStatisticsCommand run() {
                deployments.reindexFolder();
                return this;
            }
        };
    }


    private static class InstalledRepositoryConfigManager implements NutsRepositoryConfigManager {
        private final NutsWorkspace ws;

        public InstalledRepositoryConfigManager(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public String getUuid() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public String uuid() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public String getName() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public String name() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public String getGlobalName() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public Map<String, String> getEnv() {
            return Collections.emptyMap();
        }

        @Override
        public String getEnv(String property, String defaultValue) {
            return null;
        }

        @Override
        public String getType() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public String getGroups() {
            return null;
        }

        @Override
        public int getSpeed() {
            return 0;
        }

        @Override
        public int getSpeed(boolean transitive) {
            return 0;
        }

        @Override
        public void setEnv(String property, String value, NutsUpdateOptions options) {
            //
        }

        @Override
        public boolean isTemporary() {
            return false;
        }

        @Override
        public boolean isIndexSubscribed() {
            return false;
        }

        @Override
        public String getLocation(boolean expand) {
            return null;
        }

        @Override
        public Path getStoreLocation() {
            return null;
        }

        @Override
        public Path getStoreLocation(NutsStoreLocation folderType) {
            return null;
        }

        @Override
        public boolean save(boolean force, NutsSession session) {
            return false;
        }

        @Override
        public void save(NutsSession session) {

        }

        @Override
        public Map<String, String> getEnv(boolean inherit) {
            return Collections.emptyMap();
        }

        @Override
        public String getEnv(String key, String defaultValue, boolean inherit) {
            return null;
        }

        @Override
        public NutsRepositoryConfigManager setIndexEnabled(boolean enabled, NutsUpdateOptions options) {
            return this;
        }

        @Override
        public boolean isIndexEnabled() {
            return false;
        }

        @Override
        public NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled, NutsUpdateOptions options) {
            return this;
        }

        @Override
        public int getDeployOrder() {
            return Integer.MAX_VALUE;
        }

        @Override
        public NutsRepositoryConfigManager setEnabled(boolean enabled, NutsUpdateOptions options) {
            return this;
        }

        @Override
        public NutsRepositoryConfigManager setTemporary(boolean enabled, NutsUpdateOptions options) {
            return this;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public NutsRepositoryConfigManager subscribeIndex() {
            return this;
        }

        @Override
        public NutsRepositoryConfigManager unsubscribeIndex() {
            return this;
        }

        @Override
        public boolean isSupportedMirroring() {
            return false;
        }

        @Override
        public NutsRepository findMirrorById(String repositoryNameOrId, boolean transitive) {
            return null;
        }

        @Override
        public NutsRepository findMirrorByName(String repositoryNameOrId, boolean transitive) {
            return null;
        }

        @Override
        public NutsRepository[] getMirrors() {
            return new NutsRepository[0];
        }

        @Override
        public NutsRepository getMirror(String repositoryIdOrName, boolean transitive) {
            return null;
        }

        @Override
        public NutsRepository findMirror(String repositoryIdOrName, boolean transitive) {
            return null;
        }

        @Override
        public NutsRepository addMirror(NutsRepositoryDefinition definition) {
            throw new NutsIllegalArgumentException(ws, "Not supported : addMirror");
        }

        @Override
        public NutsRepository addMirror(NutsCreateRepositoryOptions options) {
            throw new NutsIllegalArgumentException(ws, "Not supported : addMirror");
        }

        @Override
        public NutsRepositoryConfigManager removeMirror(String repositoryId, NutsRemoveOptions options) {
            throw new NutsIllegalArgumentException(ws, "Not supported : removeMirror");
        }

        @Override
        public int getSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode fetchMode, boolean transitive) {
            return 0;
        }

        @Override
        public NutsStoreLocationStrategy getStoreLocationStrategy() {
            return ws.config().getRepositoryStoreLocationStrategy();
        }
    }
}
