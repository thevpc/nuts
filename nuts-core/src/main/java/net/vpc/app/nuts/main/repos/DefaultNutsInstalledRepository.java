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
import java.util.function.Predicate;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.repos.AbstractNutsRepository;
import net.vpc.app.nuts.core.repos.NutsInstalledRepository;
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
public class DefaultNutsInstalledRepository extends AbstractNutsRepository implements NutsInstalledRepository {

    public static class InstallInfoConfig {

        private NutsId id;
        private Instant installDate;
        private String installUser;

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
        public String toString() {
            return "InstallInfoConfig{" + "id=" + id + ", installDate=" + installDate + ", installUser=" + installUser + '}';
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.id);
            hash = 89 * hash + Objects.hashCode(this.installDate);
            hash = 89 * hash + Objects.hashCode(this.installUser);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InstallInfoConfig other = (InstallInfoConfig) obj;
            if (!Objects.equals(this.installUser, other.installUser)) {
                return false;
            }
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.installDate, other.installDate)) {
                return false;
            }
            return true;
        }

    }

    private static final String NUTS_INSTALL_FILE = "nuts-install.json";

    private final NutsRepositoryFolderHelper deployments;
    private final Map<NutsId, String> cachedDefaultVersions = new LRUMap<>(200);

    public DefaultNutsInstalledRepository(NutsWorkspace ws, NutsWorkspaceInitInformation info) {
        this.workspace = ws;
        deployments = new NutsRepositoryFolderHelper(null, ws, Paths.get(info.getStoreLocation(NutsStoreLocation.LIB)).resolve(NutsConstants.Folders.ID));
        configManager = new NutsRepositoryConfigManager() {
            @Override
            public String getUuid() {
                return "<installed>";
            }

            @Override
            public String uuid() {
                return "<installed>";
            }

            @Override
            public String getName() {
                return "<installed>";
            }

            @Override
            public String name() {
                return "<installed>";
            }

            @Override
            public String getGlobalName() {
                return "<installed>";
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
                return "<installed>";
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
        };
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

    public InstallInfoConfig getInstallInfoConfig(NutsId id, NutsSession session) {
        Path p = getPath(id, NUTS_INSTALL_FILE);
        if (Files.isRegularFile(p)) {
            return workspace.io().lock().source(id).session(session).run(
                    () -> readJson(id, NUTS_INSTALL_FILE, InstallInfoConfig.class)
                    , CoreNutsUtils.LOCK_TIME, CoreNutsUtils.LOCK_TIME_UNIT
            );
        }
        return null;
    }

    @Override
    public NutsInstallInformation getInstallInformation(NutsId id, NutsSession session) {
        InstallInfoConfig ii = getInstallInfoConfig(id, session);
        if (ii != null) {
            return new DefaultNutsInstallInfo(true, isDefaultVersion(id, session),
                    workspace.config().getStoreLocation(id, NutsStoreLocation.APPS),
                    ii.getInstallDate(),
                    ii.getInstallUser()
            );
        }
        return null;
    }

    @Override
    public boolean isInstalled(NutsId id, NutsSession session) {
        return contains(id, NUTS_INSTALL_FILE);
    }

    @Override
    public void uninstall(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.of(workspace).checkReadOnly();
        session = NutsWorkspaceUtils.of(workspace).validateSession(session);
        if (!contains(id, NUTS_INSTALL_FILE)) {
            throw new NutsNotInstalledException(workspace, id);
        }
        try {
            remove(id, NUTS_INSTALL_FILE);
            String v = getDefaultVersion(id, session);
            if (v != null && v.equals(id.getVersion().getValue())) {
                Iterator<NutsId> versions = searchVersions().setId(id)
                        .setFilter(new NutsIdFilterTopInstalled(null)) //search only in installed, ignore deployed!
                        .setSession(NutsWorkspaceHelper.createRepositorySession(session, this, NutsFetchMode.INSTALLED)).getResult();
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
    public NutsInstallInformation install(NutsDefinition def, NutsSession session) {
        for (NutsDependency dependency : def.getDependencies()) {
            Iterator<NutsId> old = searchVersions().setId(dependency.getId()).setSession(NutsWorkspaceHelper.createRepositorySession(session, this, NutsFetchMode.INSTALLED)).getResult();
            if (!old.hasNext()) {
                throw new IllegalArgumentException("Unable to install " + def.getId() + " as dependencies are missing.");
            }
        }
        this.deploy()
                .setId(def.getId())
                .setContent(def.getPath())
                .setSession(NutsWorkspaceHelper.createNoRepositorySession(session, NutsFetchMode.LOCAL))
                .setDescriptor(def.getDescriptor())
                .run();
        NutsId id = def.getId();
        Instant now = Instant.now();
        String user = workspace.security().getCurrentUsername();
        NutsWorkspaceUtils.of(workspace).checkReadOnly();
        InstallInfoConfig ii;
        try {
            ii = new InstallInfoConfig();
            ii.setId(id);
            ii.setInstallDate(now);
            ii.setInstallUser(user);
            addJson(id, NUTS_INSTALL_FILE, ii);
        } catch (UncheckedIOException ex) {
            throw new NutsNotInstallableException(workspace, id.toString(), "Unable to install "
                    + id.builder().setNamespace(null).build() + " : " + ex.getMessage(), ex);
        }
        return new DefaultNutsInstallInfo(true, isDefaultVersion(id, session), workspace.config().getStoreLocation(id, NutsStoreLocation.APPS), ii.getInstallDate(), ii.getInstallUser());
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

    public void addJson(NutsId id, String name, InstallInfoConfig value) {
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
                deployments.deploy(this);
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
                result=deployments.fetchDescriptorImpl(getId(),getSession());
                return this;
            }
        };
    }

    @Override
    public NutsFetchContentRepositoryCommand fetchContent() {
        return new AbstractNutsFetchContentRepositoryCommand(this) {
            @Override
            public NutsFetchContentRepositoryCommand run() {
                result=deployments.fetchContentImpl(getId(),getLocalPath(),getSession());
                return this;
            }
        };
    }

    @Override
    public NutsSearchRepositoryCommand search() {
        return new AbstractNutsSearchRepositoryCommand(this) {
            @Override
            public NutsSearchRepositoryCommand run() {
                NutsIdFilter filter = getFilter();
                if (filter instanceof NutsIdFilterTopInstalled) {
                    final Path folder = workspace.config().getStoreLocation(NutsStoreLocation.CONFIG);
                    int maxDepth = Integer.MAX_VALUE;
                    NutsSession session = getSession().getSession();
                    if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
                        result = IteratorUtils.emptyIterator();
                    } else {
                        result = new FolderObjectIterator<NutsId>(workspace, folder, new Predicate<NutsId>() {
                            @Override
                            public boolean test(NutsId nutsId) {
                                return filter == null || filter.accept(nutsId, session);
                            }
                        }, session, new FolderObjectIterator.FolderIteratorModel<NutsId>() {

                            @Override
                            public boolean isObjectFile(Path pathname) {
                                return pathname.getFileName().toString().equals(NUTS_INSTALL_FILE);
                            }

                            @Override
                            public NutsId parseObject(Path pathname, NutsSession session) throws IOException {
                                return workspace.json().parse(pathname, InstallInfoConfig.class).getId();
                            }
                        }, maxDepth);
                    }
                } else {
                    result = deployments.searchImpl(filter, getSession());
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

}
