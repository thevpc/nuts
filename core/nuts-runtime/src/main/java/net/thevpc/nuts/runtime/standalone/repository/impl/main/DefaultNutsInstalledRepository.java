/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.io.util.FolderObjectIterator;
import net.thevpc.nuts.runtime.standalone.io.util.NutsInstallStatusIdFilter;
import net.thevpc.nuts.runtime.standalone.repository.cmd.deploy.AbstractNutsDeployRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.AbstractNutsFetchContentRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.AbstractNutsFetchDescriptorRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.push.AbstractNutsPushRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.AbstractNutsSearchRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.AbstractNutsSearchVersionsRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy.AbstractNutsRepositoryUndeployCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats.AbstractNutsUpdateRepositoryStatisticsCommand;
import net.thevpc.nuts.runtime.standalone.repository.impl.AbstractNutsRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt0;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.LRUMap;
import net.thevpc.nuts.runtime.standalone.util.filters.NutsIdFilterToPredicate;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.CoreNutsBootOptions;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNutsInstalledRepository extends AbstractNutsRepository implements NutsInstalledRepository, NutsRepositoryExt0 {

    public static final String INSTALLED_REPO_UUID = "<main>";
    private static final String NUTS_INSTALL_FILE = "nuts-install.json";
    private final NutsRepositoryFolderHelper deployments;
    private final Map<NutsId, String> cachedDefaultVersions = new LRUMap<>(200);
    private NutsLogger LOG;

    public DefaultNutsInstalledRepository(NutsWorkspace ws, CoreNutsBootOptions bOptions) {
        this.workspace = ws;
        this.initSession = NutsWorkspaceUtils.defaultSession(ws);
        this.deployments = new NutsRepositoryFolderHelper(this,
                NutsWorkspaceUtils.defaultSession(ws),
                NutsPath.of(bOptions.getStoreLocation(NutsStoreLocation.LIB), initSession).resolve(NutsConstants.Folders.ID)
                , false,
                "lib", NutsElements.of(initSession).ofObject().set("repoKind", "lib").build()
        );
        configModel = new InstalledRepositoryConfigModel(workspace, this);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsInstalledRepository.class, session);
        }
        return LOG;
    }

//    public Set<NutsId> getChildrenDependencies(NutsId id, NutsSession session) {
//        return Collections.emptySet();
//    }
//
//    public Set<NutsId> getParentDependencies(NutsId id, NutsSession session) {
//        return Collections.emptySet();
//    }
//
//    public void addDependency(NutsId id, NutsId parentId, NutsSession session) {
//
//    }
//
//    public void removeDependency(NutsId id, NutsId parentId, NutsSession session) {
//
//    }

    @Override
    public boolean isDefaultVersion(NutsId id, NutsSession session) {
        String v = getDefaultVersion(id, session);
        return v.equals(id.getVersion().toString());
    }

    @Override
    public NutsIterator<NutsInstallInformation> searchInstallInformation(NutsSession session) {
        NutsPath rootFolder = session.locations().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.ID);
        return new FolderObjectIterator<NutsInstallInformation>("NutsInstallInformation",
                rootFolder,
                null, -1, session, new FolderObjectIterator.FolderIteratorModel<NutsInstallInformation>() {
            @Override
            public boolean isObjectFile(NutsPath pathname) {
                return pathname.getName().equals(NUTS_INSTALL_FILE);
            }

            @Override
            public NutsInstallInformation parseObject(NutsPath path, NutsSession session) throws IOException {
                try {
                    InstallInfoConfig c = getInstallInfoConfig(null, path, session);
                    if (c != null) {
                        return getInstallInformation(c, session);
                    }
                } catch (Exception ex) {
                    _LOGOP(session).error(ex)
                            .log(NutsMessage.jstyle("unable to parse {0}", path));
                }
                return null;
            }
        }
        );
    }

    @Override
    public String getDefaultVersion(NutsId id, NutsSession session) {
        NutsId baseVersion = id.getShortId();
        synchronized (cachedDefaultVersions) {
            String p = cachedDefaultVersions.get(baseVersion);
            if (p != null) {
                return p;
            }
        }
        NutsPath pp = session.locations().getStoreLocation(id
                        //.setAlternative("")
                        .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG)
                .resolveSibling("default-version");
        String defaultVersion = "";
        if (pp.isRegularFile()) {
            try {
                defaultVersion = new String(pp.readAllBytes()).trim();
            } catch (Exception ex) {
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
        NutsId baseVersion = id.getShortId();
        String version = id.getVersion().getValue();
        NutsPath pp = session.locations().getStoreLocation(id
                        //                .setAlternative("")
                        .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG)
                .resolveSibling("default-version");
        if (NutsBlankable.isBlank(version)) {
            if (pp.isRegularFile()) {
                pp.delete();
            }
        } else {
            pp.mkParentDirs();
            pp.writeBytes(version.trim().getBytes());
        }
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, version);
        }
    }

    @Override
    public NutsInstallInformation getInstallInformation(NutsId id, NutsSession session) {
        InstallInfoConfig c = getInstallInfoConfig(id, null, session);
        return c != null ? getInstallInformation(c, session) : DefaultNutsInstallInfo.notInstalled(id);
    }

    @Override
    public NutsInstallStatus getInstallStatus(NutsId id, NutsSession session) {
        NutsInstallInformation ii = getInstallInformation(id, session);
        if (ii == null) {
            return NutsInstallStatus.NONE;
        }
        return ii.getInstallStatus();
    }

    @Override
    public void install(NutsId id, NutsSession session, NutsId forId) {
        boolean succeeded = false;
        NutsWorkspaceUtils.of(session).checkReadOnly();
        InstallInfoConfig ii = getInstallInfoConfig(id, null, session);
        try {
            String repository = id.getRepository();
            NutsRepository r = session.repos().findRepository(repository);
            if (ii == null) {
                ii = new InstallInfoConfig();
                ii.setId(id);
                ii.setInstalled(forId == null);
                if (r != null) {
                    ii.setSourceRepoName(r.getName());
                    ii.setSourceRepoUUID(r.getUuid());
                }
                saveCreate(ii,session);
            } else {
                InstallInfoConfig ii0 = ii.copy();
                ii.setId(id);
                ii.setInstalled(forId == null);
                if (r != null) {
                    ii.setSourceRepoName(r.getName());
                    ii.setSourceRepoUUID(r.getUuid());
                }
                saveUpdate(ii,ii0,session);
            }

            succeeded = true;
        } catch (UncheckedIOException | NutsIOException ex) {
            throw new NutsNotInstallableException(session, id,
                    NutsMessage.cstyle("failed to install %s : %s", id, ex)
                    , ex);
        } finally {
            addLog(NutsInstallLogAction.INSTALL, id, forId, null, succeeded, session);
        }
    }

    @Override
    public NutsInstallInformation install(NutsDefinition def, NutsSession session) {
        boolean succeeded = false;
        try {
            NutsInstallInformation a = updateInstallInformation(def, true, null, true, session);
            succeeded = true;
            return a;
        } finally {
            addLog(NutsInstallLogAction.INSTALL, def.getId(), null, null, succeeded, session);
        }
    }

    @Override
    public void uninstall(NutsDefinition def, NutsSession session) {
        boolean succeeded = false;
        NutsWorkspaceUtils.of(session).checkReadOnly();
        NutsWorkspaceUtils.checkSession(workspace, session);
        NutsId id = def.getId();
        NutsInstallStatus installStatus = getInstallStatus(id, session);
        if (!installStatus.isInstalled()) {
            throw new NutsNotInstalledException(session, id);
        }
        try {
            String pck = def.getDescriptor().getPackaging();
            undeploy().setId(id.builder().setPackaging(NutsBlankable.isBlank(pck) ? "jar" : pck).build())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .setSession(session)
                    .run();
            remove(id, NUTS_INSTALL_FILE, session);
            String v = getDefaultVersion(id, session);
            if (v != null && v.equals(id.getVersion().getValue())) {
                Iterator<NutsId> versions = searchVersions().setId(id)
                        .setFilter(NutsIdFilters.of(session).byInstallStatus(
                                NutsInstallStatusFilters.of(session).byInstalled(true)
                        )) //search only in installed, ignore deployed!
                        .setFetchMode(NutsFetchMode.LOCAL)
                        .setSession(session).getResult();
                List<NutsId> nutsIds = CoreCollectionUtils.toList(versions == null ? Collections.emptyIterator() : versions);
                nutsIds.sort(null);
                if (nutsIds.size() > 0) {
                    setDefaultVersion(nutsIds.get(0), session);
                } else {
                    setDefaultVersion(id.builder().setVersion("").build(), session);
                }
            }
            succeeded = true;
        } catch (Exception ex) {
            throw new NutsNotInstalledException(session, id);
        } finally {
            addLog(NutsInstallLogAction.UNINSTALL, id, null, null, succeeded, session);
        }
    }

    @Override
    public NutsInstallInformation require(NutsDefinition def, boolean deploy, NutsId[] forIds, NutsDependencyScope scope, NutsSession session) {
        boolean succeeded = false;
        NutsId requiredId = def.getId();
        NutsInstallInformation nutsInstallInformation = updateInstallInformation(def, null, true, deploy, session);
        if (forIds != null) {
            for (NutsId requestorId : forIds) {
                if (requestorId != null) {
                    succeeded = false;
                    try {
                        if (scope == null) {
                            scope = NutsDependencyScope.API;
                        }
                        //remove repository requiredId id!
                        requiredId = requiredId.builder().setRepository(null).build();


                        InstallInfoConfig fi = getInstallInfoConfig(requiredId, null, session);
                        if (fi == null) {
                            throw new NutsInstallException(session, requiredId);
                        }
                        InstallInfoConfig fi0 = fi.copy();
                        InstallInfoConfig ti = getInstallInfoConfig(requestorId, null, session);
                        InstallInfoConfig ti0 = ti == null ? null : ti.copy();
                        //there is no need to check for the target dependency (the reason why the 'requiredId' needs to be installed)
                        if (!fi.isRequired()) {
                            fi.setRequired(true);
                            fi.setRequiredBy(addDistinct(fi.getRequiredBy(), new InstallDepConfig(requestorId, scope)));
                            saveUpdate(fi,fi0,session);
                        }
                        saveUpdate(fi,fi0,session);
                        if (ti == null) {
                            ti = new InstallInfoConfig();
                            ti.setId(requestorId);
                            ti.setRequires(addDistinct(ti.getRequires(), new InstallDepConfig(requiredId, scope)));
                            saveCreate(ti,session);
                        } else {
                            ti.setRequires(addDistinct(ti.getRequires(), new InstallDepConfig(requiredId, scope)));
                            saveUpdate(ti,ti0,session);
                        }
                        succeeded = true;
                    } finally {
                        addLog(NutsInstallLogAction.REQUIRE, requiredId, requestorId, null, succeeded, session);
                    }
                }
            }
        }
        return nutsInstallInformation;
    }

    @Override
    public void unrequire(NutsId requiredId, NutsId requestorId, NutsDependencyScope scope, NutsSession session) {
        Instant now = Instant.now();
        String user = session.security().getCurrentUsername();
        boolean succeeded = false;
        try {
            if (scope == null) {
                scope = NutsDependencyScope.API;
            }
            InstallInfoConfig fi = getInstallInfoConfig(requiredId, null, session);
            if (fi == null) {
                throw new NutsInstallException(session, requiredId);
            }
            InstallInfoConfig ti = getInstallInfoConfig(requestorId, null, session);
            if (ti == null) {
                throw new NutsInstallException(session, requestorId);
            }
            InstallInfoConfig fi0 = fi.copy();
            InstallInfoConfig ti0 = ti.copy();

            fi.setRequiredBy(removeDistinct(fi.getRequiredBy(), new InstallDepConfig(requestorId, scope)));
            ti.setRequires(removeDistinct(ti.getRequires(), new InstallDepConfig(requiredId, scope)));
            if (fi.isRequired() != fi.getRequiredBy().size() > 0) {
                fi.setRequired(fi.getRequiredBy().size() > 0);
            }
            saveUpdate(fi,fi0,session);
            saveUpdate(ti,ti0,session);
            succeeded = true;
        } finally {
            addLog(NutsInstallLogAction.UNREQUIRE, requiredId, requestorId, null, succeeded, session);
        }
    }

    @Override
    public NutsStream<NutsInstallLogRecord> findLog(NutsSession session) {
        return InstallLogItemTable.of(session).stream(session);
    }

    public NutsId pathToId(NutsPath path, NutsSession session) {
        NutsPath rootFolder = session.locations().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.ID);
        String p = path.toString().substring(rootFolder.toString().length());
        List<String> split = StringTokenizerUtils.split(p, "/\\");
        if (split.size() >= 4) {
            return NutsIdBuilder.of(session).setArtifactId(split.get(split.size() - 3))
                    .setGroupId(String.join(".", split.subList(0, split.size() - 3)))
                    .setVersion(split.get(split.size() - 2)).build();

        }
        return null;
    }

    private <A> List<A> addDistinct(List<A> old, A v) {
        LinkedHashSet<A> s = new LinkedHashSet<>();
        if (old != null) {
            s.addAll(old);
        }
        if (v != null) {
            s.add(v);
        }
        return new ArrayList<>(s);
    }

    private <A> List<A> removeDistinct(List<A> old, A v) {
        LinkedHashSet<A> s = new LinkedHashSet<>();
        if (old != null) {
            s.addAll(old);
        }
        if (v != null) {
            s.remove(v);
        }
        return new ArrayList<>(s);
    }


    public InstallInfoConfig getInstallInfoConfig(NutsId id, NutsPath path, NutsSession session) {
        if (id == null && path == null) {
            NutsWorkspaceUtils.of(session).checkShortId(id);
        }
        if (path == null) {
            path = getPath(id, NUTS_INSTALL_FILE, session);
        }
//        if (id == null) {
//            path = getPath(id, NUTS_INSTALL_FILE);
//        }
        NutsPath finalPath = path;
        if (path.isRegularFile()) {
            NutsElements elem = NutsElements.of(session);
            InstallInfoConfig c = NutsLocks.of(session).setSource(path).call(
                    () -> elem.json().parse(finalPath, InstallInfoConfig.class),
                    CoreNutsUtils.LOCK_TIME, CoreNutsUtils.LOCK_TIME_UNIT
            );
            if (c != null) {
                boolean changeStatus = false;
                NutsVersion v = NutsVersion.of(c.getConfigVersion(), session);
                if (v.isBlank()) {
                    c.setInstalled(true);
                    c.setConfigVersion("0.5.8"); //last version before 0.6
                    changeStatus = true;
                }
                NutsId idOk = c.getId();
                if (idOk == null) {
                    if (id != null) {
                        c.setId(id);
                        changeStatus = true;
                    } else {
                        NutsId idOk2 = pathToId(path, session);
                        if (idOk2 != null) {
                            c.setId(idOk2);
                            changeStatus = true;
                        } else {
                            return null;
                        }
                    }
                }
                if (changeStatus && !session.config().isReadOnly()) {
                    NutsLocks.of(session).setSource(path).call(() -> {
                                _LOGOP(session).level(Level.CONFIG)
                                        .log(NutsMessage.jstyle("install-info upgraded {0}", finalPath));
                                c.setConfigVersion(workspace.getApiVersion().toString());
                                elem.json().setValue(c)
                                        .setNtf(false)
                                        .print(finalPath);
                                return null;
                            },
                            CoreNutsUtils.LOCK_TIME, CoreNutsUtils.LOCK_TIME_UNIT
                    );
                }
            }
            return c;
        }
        return null;
    }

    public NutsIterator<InstallInfoConfig> searchInstallConfig(NutsSession session) {
        NutsPath rootFolder = session.locations().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.ID);
        return new FolderObjectIterator<InstallInfoConfig>("InstallInfoConfig",
                rootFolder,
                null, -1, session, new FolderObjectIterator.FolderIteratorModel<InstallInfoConfig>() {
            @Override
            public boolean isObjectFile(NutsPath pathname) {
                return pathname.getName().equals(NUTS_INSTALL_FILE);
            }

            @Override
            public InstallInfoConfig parseObject(NutsPath path, NutsSession session) throws IOException {
                try {
                    InstallInfoConfig c = getInstallInfoConfig(null, path, session);
                    if (c != null) {
                        return c;
                    }
                } catch (Exception ex) {
                    _LOGOP(session).error(ex).log(NutsMessage.jstyle("unable to parse {0}", path));
                }
                return null;
            }
        }
        );
    }

    public NutsInstallInformation getInstallInformation(InstallInfoConfig ii, NutsSession session) {
        boolean obsolete = false;
        boolean defaultVersion = false;
        if (ii.isInstalled()) {
            defaultVersion = isDefaultVersion(ii.getId(), session);
        }
        if (session.getExpireTime() != null && (ii.isInstalled() || ii.isRequired())) {
            if (ii.isInstalled() || ii.isRequired()) {
                Instant lastModifiedDate = ii.getLastModificationDate();
                if (lastModifiedDate == null) {
                    lastModifiedDate = ii.getCreationDate();
                }
                if (lastModifiedDate == null || lastModifiedDate.isBefore(session.getExpireTime())) {
                    obsolete = true;
                }
            }
        }
        NutsInstallStatus s = NutsInstallStatus.of(ii.isInstalled(), ii.isRequired(), obsolete, defaultVersion);
        return new DefaultNutsInstallInfo(ii.getId(),
                s,
                session.locations().getStoreLocation(ii.getId(), NutsStoreLocation.APPS),
                ii.getCreationDate(),
                ii.getLastModificationDate(),
                ii.getCreationUser(),
                ii.getSourceRepoName(),
                ii.getSourceRepoUUID(),
                false, false //will be processed later!
        );
    }

    private NutsInstallInformation updateInstallInformation(NutsDefinition def, Boolean install, Boolean require, boolean deploy, NutsSession session) {
        NutsId id1 = def.getId();
        InstallInfoConfig ii = getInstallInfoConfig(id1, null, session);
        boolean wasInstalled = false;
        boolean wasRequired = false;
        if (deploy) {
            this.deploy()
                    .setId(id1)
                    .setSession(session.copy().setConfirm(NutsConfirmationMode.YES))
                    .setContent(def.getFile())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .setDescriptor(def.getDescriptor())
                    .run();
        }
        if (ii == null) {
//            for (NutsDependency dependency : def.getDependencies()) {
//                Iterator<NutsId> it = searchVersions().setId(dependency.getId()).setSession(NutsWorkspaceHelper.createRepositorySession(session, this, NutsFetchMode.DEPLOYED)).getResult();
//                if (!it.hasNext()) {
//                    throw new IllegalArgumentException("failed to install " + def.getId() + " as dependencies are missing.");
//                }
//            }
            NutsId id = id1;
            NutsWorkspaceUtils.of(session).checkReadOnly();
            try {
                boolean _install = false;
                boolean _require = false;
                if (install != null && require != null) {
                    _install = install;
                    _require = require;
                } else if (install != null) {
                    _install = install;
                    _require = !_install;
                } else if (require != null) {
                    _require = require;
                    _install = !_require;
                } else {
                    _install = true;
                    _require = false;
                }
                ii = new InstallInfoConfig();
                ii.setConfigVersion(DefaultNutsWorkspace.VERSION_INSTALL_INFO_CONFIG);
                ii.setId(id);
                ii.setInstalled(_install);
                ii.setRequired(_require);
                saveCreate(ii,session);
            } catch (UncheckedIOException | NutsIOException ex) {
                throw new NutsNotInstallableException(session, id, NutsMessage.cstyle("failed to install %s : %s", id, ex), ex);
            }
            DefaultNutsInstallInfo uu = (DefaultNutsInstallInfo) getInstallInformation(ii, session);
            uu.setWasInstalled(false);
            uu.setWasRequired(false);
            uu.setJustInstalled(install != null && install);
            uu.setJustRequired(require != null && require);
            return uu;
        } else {
            InstallInfoConfig ii0 = ii.copy();
            wasInstalled = ii.isInstalled();
            wasRequired = ii.isRequired();
            boolean _install = wasInstalled;
            boolean _require = wasRequired;
            if (install != null) {
                _install = install;
            }
            if (require != null) {
                _require = require;
            }
            ii.setInstalled(_install);
            ii.setRequired(_require);
            saveUpdate(ii,ii0,session);
            DefaultNutsInstallInfo uu = (DefaultNutsInstallInfo) getInstallInformation(ii, session);
            uu.setWasInstalled(wasInstalled);
            uu.setWasRequired(wasRequired);
            uu.setJustInstalled(install != null && install);
            uu.setJustRequired(require != null && require);
            return uu;
        }
    }

    private void saveCreate(InstallInfoConfig ii, NutsSession session) {
        Instant now = Instant.now();
        String user = session.security().getCurrentUsername();
        if (ii.getCreationUser() == null) {
            ii.setCreationUser(user);
        }
        if (ii.getCreationDate() == null) {
            ii.setCreationDate(now);
        }
        ii.setConfigVersion(DefaultNutsWorkspace.VERSION_INSTALL_INFO_CONFIG);
        printJson(ii.getId(), NUTS_INSTALL_FILE, ii, session);
    }

    private void saveUpdate(InstallInfoConfig ii, InstallInfoConfig ii0, NutsSession session) {
        Instant now = Instant.now();
        String user = session.security().getCurrentUsername();
        if (ii.getCreationUser() == null) {
            ii.setCreationUser(user);
        }
        if (ii.getCreationDate() == null) {
            ii.setCreationDate(now);
        }
        if (!ii.equals(ii0)) {
            ii.setLastModificationDate(now);
            ii.setLastModificationUser(user);
            ii.setConfigVersion(DefaultNutsWorkspace.VERSION_INSTALL_INFO_CONFIG);
            printJson(ii.getId(), NUTS_INSTALL_FILE, ii, session);
        }
    }

    public void addString(NutsId id, String name, String value, NutsSession session) {
        getPath(id, name, session).writeBytes(value.getBytes());
    }

    public <T> T readJson(NutsId id, String name, Class<T> clazz, NutsSession session) {
        return NutsElements.of(session)
                .setSession(session).json()
                .parse(getPath(id, name, session), clazz);
    }

    public void printJson(NutsId id, String name, InstallInfoConfig value, NutsSession session) {
        value.setConfigVersion(workspace.getApiVersion().toString());
        NutsElements.of(session).setNtf(false)
                .setSession(session).json().setValue(value)
                .print(getPath(id, name, session));
    }

    public void remove(NutsId id, String name, NutsSession session) {
        NutsPath path = getPath(id, name, session);
        path.delete();
    }

    public boolean contains(NutsId id, String name, NutsSession session) {
        return getPath(id, name, session).isRegularFile();
    }

    public NutsPath getPath(NutsId id, String name, NutsSession session) {
        return session.locations().setSession(session).getStoreLocation(id, NutsStoreLocation.CONFIG).resolve(name);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of repository
    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public NutsRepositorySecurityManager security() {
        throw new IllegalArgumentException("unsupported security() for " + getName() + " repository");
    }

    @Override
    public NutsDeployRepositoryCommand deploy() {
        return new AbstractNutsDeployRepositoryCommand(this) {
            @Override
            public NutsDeployRepositoryCommand run() {
                boolean succeeded = false;
                try {
                    NutsDescriptor rep = deployments.deploy(this, NutsConfirmationMode.YES);
                    this.setDescriptor(rep);
                    this.setId(rep.getId());
                    succeeded = true;
                } finally {
                    addLog(NutsInstallLogAction.DEPLOY, getId(), null, null, succeeded, getSession());
                }
                return this;
            }
        };
    }

    @Override
    public NutsRepositoryUndeployCommand undeploy() {
        return new AbstractNutsRepositoryUndeployCommand(this) {
            @Override
            public NutsRepositoryUndeployCommand run() {
                boolean succeeded = false;
                try {
                    deployments.undeploy(this);
                    succeeded = true;
                } finally {
                    addLog(NutsInstallLogAction.UNDEPLOY, getId(), null, null, succeeded, getSession());
                }
                return this;
            }
        };
    }

    @Override
    public NutsPushRepositoryCommand push() {
        return new AbstractNutsPushRepositoryCommand(this) {
            @Override
            public NutsPushRepositoryCommand run() {
                throw new NutsIllegalArgumentException(getSession(),
                        NutsMessage.cstyle("unsupported push() for %s repository", getName())
                );
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
                NutsIterator<InstallInfoConfig> installIter = searchInstallConfig(getSession());
                NutsIterator<NutsId> idIter = IteratorBuilder.of(installIter, getSession()).map(NutsFunction.of(InstallInfoConfig::getId, "NutsInstallInformation->Id"))
                        .build();
                NutsIdFilter ff = getFilter();
                if (ff != null) {
                    idIter = IteratorBuilder.of(idIter, getSession()).filter(new NutsIdFilterToPredicate(ff, getSession())).build();
                }
                result = idIter; //deployments.searchImpl(getFilter(), getSession())
                if (result == null) {
                    result = IteratorBuilder.emptyIterator();
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
                if (getFilter() instanceof NutsInstallStatusIdFilter) {
                    NutsPath installFolder
                            = getSession().locations().getStoreLocation(getId()
                            .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).getParent();
                    if (installFolder.isDirectory()) {
                        final NutsVersionFilter filter0 = getId().getVersion().filter();
                        result = IteratorBuilder.of(installFolder.list().iterator(), getSession())
                                .map(NutsFunction.of(
                                        new Function<NutsPath, NutsId>() {
                                            @Override
                                            public NutsId apply(NutsPath folder) {
                                                if (folder.isDirectory()
                                                        && folder.resolve(NUTS_INSTALL_FILE).isRegularFile()) {
                                                    NutsVersion vv = NutsVersion.of(folder.getName(), getSession());
                                                    NutsIdFilter filter = getFilter();
                                                    NutsSession session = getSession();
                                                    if (filter0.acceptVersion(vv, session) && (filter == null || filter.acceptId(
                                                            getId().builder().setVersion(vv).build(),
                                                            session))) {
                                                        return getId().builder().setVersion(folder.getName()).build();
                                                    }
                                                }
                                                return null;
                                            }
                                        }, "FileToVersion"))
                                .notNull().iterator();
                    } else {
                        //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
                        result = IteratorBuilder.emptyIterator();
                    }
                } else {
                    this.result = IteratorBuilder.of(deployments.searchVersions(getId(), getFilter(), true, getSession()), getSession())
                            .named("searchVersionsInMain()")
                            .build()
                    ;
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
                deployments.reindexFolder(getSession());
                return this;
            }
        };
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return mode == NutsFetchMode.LOCAL;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    public void addLog(NutsInstallLogAction action, NutsId id, NutsId requestor, String message, boolean succeeded, NutsSession session) {
        InstallLogItemTable.of(session)
                .add(new NutsInstallLogRecord(
                        Instant.now(),
                        session.security().getCurrentUsername(),
                        action,
                        id, requestor, message, succeeded
                ), session);
    }

}
