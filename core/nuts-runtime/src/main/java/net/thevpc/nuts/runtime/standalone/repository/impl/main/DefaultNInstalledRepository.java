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
import net.thevpc.nuts.boot.NBootOptions;
import net.thevpc.nuts.concurrent.NLocks;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.io.util.FolderObjectIterator;
import net.thevpc.nuts.runtime.standalone.io.util.NInstallStatusIdFilter;
import net.thevpc.nuts.runtime.standalone.repository.cmd.deploy.AbstractNDeployRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.AbstractNFetchContentRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.AbstractNFetchDescriptorRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.push.AbstractNPushRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.AbstractNSearchRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.AbstractNSearchVersionsRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy.AbstractNRepositoryUndeployCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats.AbstractNUpdateRepositoryStatisticsCommand;
import net.thevpc.nuts.runtime.standalone.repository.impl.AbstractNRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt0;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.LRUMap;
import net.thevpc.nuts.runtime.standalone.util.filters.NIdFilterToPredicate;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNInstalledRepository extends AbstractNRepository implements NInstalledRepository, NRepositoryExt0 {

    public static final String INSTALLED_REPO_UUID = "<main>";
    private static final String NUTS_INSTALL_FILE = "nuts-install.json";
    private final NRepositoryFolderHelper deployments;
    private final Map<NId, String> cachedDefaultVersions = new LRUMap<>(200);
    private NLog LOG;

    public DefaultNInstalledRepository(NWorkspace ws, NBootOptions bOptions) {
        this.workspace = ws;
        this.initSession = NSessionUtils.defaultSession(ws);
        this.deployments = new NRepositoryFolderHelper(this,
                NSessionUtils.defaultSession(ws),
                NPath.of(bOptions.getStoreType(NStoreType.LIB).get(), initSession).resolve(NConstants.Folders.ID)
                , false,
                "lib", NElements.of(initSession).ofObject().set("repoKind", "lib").build()
        );
        configModel = new InstalledRepositoryConfigModel(workspace, this);
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNInstalledRepository.class, session);
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
    public boolean isDefaultVersion(NId id, NSession session) {
        String v = getDefaultVersion(id, session);
        return v.equals(id.getVersion().toString());
    }

    @Override
    public NIterator<NInstallInformation> searchInstallInformation(NSession session) {
        NPath rootFolder = NLocations.of(session).getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.ID);
        return new FolderObjectIterator<NInstallInformation>("NutsInstallInformation",
                rootFolder,
                null, -1, session, new FolderObjectIterator.FolderIteratorModel<NInstallInformation>() {
            @Override
            public boolean isObjectFile(NPath pathname) {
                return pathname.getName().equals(NUTS_INSTALL_FILE);
            }

            @Override
            public NInstallInformation parseObject(NPath path, NSession session) throws IOException {
                try {
                    InstallInfoConfig c = getInstallInfoConfig(null, path, session);
                    if (c != null) {
                        return getInstallInformation(c, session);
                    }
                } catch (Exception ex) {
                    _LOGOP(session).error(ex)
                            .log(NMsg.ofJ("unable to parse {0}", path));
                }
                return null;
            }
        }
        );
    }

    @Override
    public String getDefaultVersion(NId id, NSession session) {
        NId baseVersion = id.getShortId();
        synchronized (cachedDefaultVersions) {
            String p = cachedDefaultVersions.get(baseVersion);
            if (p != null) {
                return p;
            }
        }
        NPath pp = NLocations.of(session).getStoreLocation(id
                        //.setAlternative("")
                        .builder().setVersion("ANY").build(), NStoreType.CONF)
                .resolveSibling("default-version");
        String defaultVersion = "";
        if (pp.isRegularFile()) {
            try {
                defaultVersion = new String(pp.readBytes()).trim();
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
    public void setDefaultVersion(NId id, NSession session) {
        NId baseVersion = id.getShortId();
        String version = id.getVersion().getValue();
        NPath pp = NLocations.of(session).getStoreLocation(id
                        //                .setAlternative("")
                        .builder().setVersion("ANY").build(), NStoreType.CONF)
                .resolveSibling("default-version");
        if (NBlankable.isBlank(version)) {
            if (pp.isRegularFile()) {
                pp.delete();
            }
        } else {
            pp.mkParentDirs();
            pp.writeString(version.trim());
        }
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, version);
        }
    }

    @Override
    public NInstallInformation getInstallInformation(NId id, NSession session) {
        InstallInfoConfig c = getInstallInfoConfig(id, null, session);
        return c != null ? getInstallInformation(c, session) : DefaultNInstallInfo.notInstalled(id);
    }

    @Override
    public NInstallStatus getInstallStatus(NId id, NSession session) {
        NInstallInformation ii = getInstallInformation(id, session);
        if (ii == null) {
            return NInstallStatus.NONE;
        }
        return ii.getInstallStatus();
    }

    @Override
    public void install(NId id, NSession session, NId forId) {
        boolean succeeded = false;
        NWorkspaceUtils.of(session).checkReadOnly();
        InstallInfoConfig ii = getInstallInfoConfig(id, null, session);
        try {
            invalidateInstallationDigest(session);
            String repository = id.getRepository();
            NRepository r = NRepositories.of(session).findRepository(repository).orNull();
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
        } catch (UncheckedIOException | NIOException ex) {
            throw new NNotInstallableException(session, id,
                    NMsg.ofC("failed to install %s : %s", id, ex)
                    , ex);
        } finally {
            addLog(NInstallLogAction.INSTALL, id, forId, null, succeeded, session);
        }
    }

    @Override
    public NInstallInformation install(NDefinition def, NSession session) {
        boolean succeeded = false;
        try {
            NInstallInformation a = updateInstallInformation(def, true, null, true, session);
            succeeded = true;
            return a;
        } finally {
            addLog(NInstallLogAction.INSTALL, def.getId(), null, null, succeeded, session);
        }
    }

    @Override
    public void uninstall(NDefinition def, NSession session) {
        boolean succeeded = false;
        NWorkspaceUtils.of(session).checkReadOnly();
        NSessionUtils.checkSession(workspace, session);
        NId id = def.getId();
        NInstallStatus installStatus = getInstallStatus(id, session);
        if (!installStatus.isInstalled()) {
            throw new NNotInstalledException(session, id);
        }
        try {
            String pck = def.getDescriptor().getPackaging();
            undeploy().setId(id.builder().setPackaging(NBlankable.isBlank(pck) ? "jar" : pck).build())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .setSession(session)
                    .run();
            remove(id, NUTS_INSTALL_FILE, session);
            String v = getDefaultVersion(id, session);
            if (v != null && v.equals(id.getVersion().getValue())) {
                Iterator<NId> versions = searchVersions().setId(id)
                        .setFilter(NIdFilters.of(session).byInstallStatus(
                                NInstallStatusFilters.of(session).byInstalled(true)
                        )) //search only in installed, ignore deployed!
                        .setFetchMode(NFetchMode.LOCAL)
                        .setSession(session).getResult();
                List<NId> nutsIds = NCollections.list(versions == null ? Collections.emptyIterator() : versions);
                nutsIds.sort(null);
                if (nutsIds.size() > 0) {
                    setDefaultVersion(nutsIds.get(0), session);
                } else {
                    setDefaultVersion(id.builder().setVersion("").build(), session);
                }
            }
            succeeded = true;
        } catch (Exception ex) {
            throw new NNotInstalledException(session, id);
        } finally {
            addLog(NInstallLogAction.UNINSTALL, id, null, null, succeeded, session);
        }
    }

    @Override
    public NInstallInformation require(NDefinition def, boolean deploy, NId[] forIds, NDependencyScope scope, NSession session) {
        boolean succeeded = false;
        NId requiredId = def.getId();
        NInstallInformation nInstallInformation = updateInstallInformation(def, null, true, deploy, session);
        if (forIds != null) {
            for (NId requestorId : forIds) {
                if (requestorId != null) {
                    succeeded = false;
                    try {
                        if (scope == null) {
                            scope = NDependencyScope.API;
                        }
                        //remove repository requiredId id!
                        requiredId = requiredId.builder().setRepository(null).build();


                        InstallInfoConfig fi = getInstallInfoConfig(requiredId, null, session);
                        if (fi == null) {
                            throw new NInstallException(session, requiredId);
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
                        addLog(NInstallLogAction.REQUIRE, requiredId, requestorId, null, succeeded, session);
                    }
                }
            }
        }
        return nInstallInformation;
    }

    @Override
    public void unrequire(NId requiredId, NId requestorId, NDependencyScope scope, NSession session) {
        Instant now = Instant.now();
        String user = NWorkspaceSecurityManager.of(session).getCurrentUsername();
        boolean succeeded = false;
        try {
            if (scope == null) {
                scope = NDependencyScope.API;
            }
            InstallInfoConfig fi = getInstallInfoConfig(requiredId, null, session);
            if (fi == null) {
                throw new NInstallException(session, requiredId);
            }
            InstallInfoConfig ti = getInstallInfoConfig(requestorId, null, session);
            if (ti == null) {
                throw new NInstallException(session, requestorId);
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
            addLog(NInstallLogAction.UNREQUIRE, requiredId, requestorId, null, succeeded, session);
        }
    }

    @Override
    public NStream<NInstallLogRecord> findLog(NSession session) {
        return InstallLogItemTable.of(session).stream(session);
    }

    public NId pathToId(NPath path, NSession session) {
        NPath rootFolder = NLocations.of(session).getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.ID);
        String p = path.toString().substring(rootFolder.toString().length());
        List<String> split = StringTokenizerUtils.split(p, "/\\");
        if (split.size() >= 4) {
            return NIdBuilder.of()
                    .setGroupId(String.join(".", split.subList(0, split.size() - 3)))
                    .setArtifactId(split.get(split.size() - 3))
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


    public InstallInfoConfig getInstallInfoConfig(NId id, NPath path, NSession session) {
        if (id == null && path == null) {
            CoreNIdUtils.checkShortId(id,session);
        }
        if (path == null) {
            path = getPath(id, NUTS_INSTALL_FILE, session);
        }
//        if (id == null) {
//            path = getPath(id, NUTS_INSTALL_FILE);
//        }
        NPath finalPath = path;
        if (path.isRegularFile()) {
            NElements elem = NElements.of(session);
            InstallInfoConfig c = NLocks.of(session).setSource(path).call(
                    () -> elem.json().parse(finalPath, InstallInfoConfig.class),
                    CoreNUtils.LOCK_TIME, CoreNUtils.LOCK_TIME_UNIT
            );
            if (c != null) {
                boolean changeStatus = false;
                NVersion v = c.getConfigVersion();
                if (NBlankable.isBlank(v)) {
                    c.setInstalled(true);
                    c.setConfigVersion(NVersion.of("0.5.8").get()); //last version before 0.6
                    changeStatus = true;
                }
                NId idOk = c.getId();
                if (idOk == null) {
                    if (id != null) {
                        c.setId(id);
                        changeStatus = true;
                    } else {
                        NId idOk2 = pathToId(path, session);
                        if (idOk2 != null) {
                            c.setId(idOk2);
                            changeStatus = true;
                        } else {
                            return null;
                        }
                    }
                }
                if (changeStatus && !NConfigs.of(session).isReadOnly()) {
                    NLocks.of(session).setSource(path).call(() -> {
                                _LOGOP(session).level(Level.CONFIG)
                                        .log(NMsg.ofJ("install-info upgraded {0}", finalPath));
                                c.setConfigVersion(workspace.getApiVersion());
                                elem.json().setValue(c)
                                        .setNtf(false)
                                        .print(finalPath);
                                return null;
                            },
                            CoreNUtils.LOCK_TIME, CoreNUtils.LOCK_TIME_UNIT
                    );
                }
            }
            return c;
        }
        return null;
    }

    public NIterator<InstallInfoConfig> searchInstallConfig(NSession session) {
        NPath rootFolder = NLocations.of(session).getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.ID);
        return new FolderObjectIterator<InstallInfoConfig>("InstallInfoConfig",
                rootFolder,
                null, -1, session, new FolderObjectIterator.FolderIteratorModel<InstallInfoConfig>() {
            @Override
            public boolean isObjectFile(NPath pathname) {
                return pathname.getName().equals(NUTS_INSTALL_FILE);
            }

            @Override
            public InstallInfoConfig parseObject(NPath path, NSession session) throws IOException {
                try {
                    InstallInfoConfig c = getInstallInfoConfig(null, path, session);
                    if (c != null) {
                        return c;
                    }
                } catch (Exception ex) {
                    _LOGOP(session).error(ex).log(NMsg.ofJ("unable to parse {0}", path));
                }
                return null;
            }
        }
        );
    }

    public NInstallInformation getInstallInformation(InstallInfoConfig ii, NSession session) {
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
        NInstallStatus s = NInstallStatus.of(ii.isInstalled(), ii.isRequired(), obsolete, defaultVersion);
        return new DefaultNInstallInfo(ii.getId(),
                s,
                NLocations.of(session).getStoreLocation(ii.getId(), NStoreType.BIN),
                ii.getCreationDate(),
                ii.getLastModificationDate(),
                ii.getCreationUser(),
                ii.getSourceRepoName(),
                ii.getSourceRepoUUID(),
                false, false //will be processed later!
        );
    }

    private NInstallInformation updateInstallInformation(NDefinition def, Boolean install, Boolean require, boolean deploy, NSession session) {
        invalidateInstallationDigest(session);
        NId id1 = def.getId();
        InstallInfoConfig ii = getInstallInfoConfig(id1, null, session);
        boolean wasInstalled = false;
        boolean wasRequired = false;
        if (deploy) {
            this.deploy()
                    .setId(id1)
                    .setSession(session.copy().setConfirm(NConfirmationMode.YES))
                    .setContent(def.getContent().orNull())
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
            NId id = id1;
            NWorkspaceUtils.of(session).checkReadOnly();
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
                ii.setConfigVersion(DefaultNWorkspace.VERSION_INSTALL_INFO_CONFIG);
                ii.setId(id);
                ii.setInstalled(_install);
                ii.setRequired(_require);
                saveCreate(ii,session);
            } catch (UncheckedIOException | NIOException ex) {
                throw new NNotInstallableException(session, id, NMsg.ofC("failed to install %s : %s", id, ex), ex);
            }
            DefaultNInstallInfo uu = (DefaultNInstallInfo) getInstallInformation(ii, session);
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
            DefaultNInstallInfo uu = (DefaultNInstallInfo) getInstallInformation(ii, session);
            uu.setWasInstalled(wasInstalled);
            uu.setWasRequired(wasRequired);
            uu.setJustInstalled(install != null && install);
            uu.setJustRequired(require != null && require);
            return uu;
        }
    }

    private void saveCreate(InstallInfoConfig ii, NSession session) {
        Instant now = Instant.now();
        String user = NWorkspaceSecurityManager.of(session).getCurrentUsername();
        if (ii.getCreationUser() == null) {
            ii.setCreationUser(user);
        }
        if (ii.getCreationDate() == null) {
            ii.setCreationDate(now);
        }
        ii.setConfigVersion(DefaultNWorkspace.VERSION_INSTALL_INFO_CONFIG);
        printJson(ii.getId(), NUTS_INSTALL_FILE, ii, session);
    }

    private void saveUpdate(InstallInfoConfig ii, InstallInfoConfig ii0, NSession session) {
        Instant now = Instant.now();
        String user = NWorkspaceSecurityManager.of(session).getCurrentUsername();
        if (ii.getCreationUser() == null) {
            ii.setCreationUser(user);
        }
        if (ii.getCreationDate() == null) {
            ii.setCreationDate(now);
        }
        if (!ii.equals(ii0)) {
            ii.setLastModificationDate(now);
            ii.setLastModificationUser(user);
            ii.setConfigVersion(DefaultNWorkspace.VERSION_INSTALL_INFO_CONFIG);
            printJson(ii.getId(), NUTS_INSTALL_FILE, ii, session);
        }
    }

    public void addString(NId id, String name, String value, NSession session) {
        getPath(id, name, session).writeString(value);
    }

    public <T> T readJson(NId id, String name, Class<T> clazz, NSession session) {
        return NElements.of(session)
                .setSession(session).json()
                .parse(getPath(id, name, session), clazz);
    }

    public void printJson(NId id, String name, InstallInfoConfig value, NSession session) {
        value.setConfigVersion(workspace.getApiVersion());
        NElements.of(session).setNtf(false)
                .setSession(session).json().setValue(value)
                .print(getPath(id, name, session));
    }

    public void remove(NId id, String name, NSession session) {
        NPath path = getPath(id, name, session);
        path.delete();
    }

    public boolean contains(NId id, String name, NSession session) {
        return getPath(id, name, session).isRegularFile();
    }

    public NPath getPath(NId id, String name, NSession session) {
        return NLocations.of(session).getStoreLocation(id, NStoreType.CONF).resolve(name);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of repository
    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public NRepositorySecurityManager security() {
        throw new IllegalArgumentException("unsupported security() for " + getName() + " repository");
    }

    @Override
    public NDeployRepositoryCommand deploy() {
        return new AbstractNDeployRepositoryCommand(this) {
            @Override
            public NDeployRepositoryCommand run() {
                invalidateInstallationDigest(getSession());
                boolean succeeded = false;
                try {
                    NDescriptor rep = deployments.deploy(this, NConfirmationMode.YES);
                    this.setDescriptor(rep);
                    this.setId(rep.getId());
                    succeeded = true;
                } finally {
                    addLog(NInstallLogAction.DEPLOY, getId(), null, null, succeeded, getSession());
                }
                return this;
            }
        };
    }

    @Override
    public NRepositoryUndeployCommand undeploy() {
        return new AbstractNRepositoryUndeployCommand(this) {
            @Override
            public NRepositoryUndeployCommand run() {
                invalidateInstallationDigest(getSession());
                boolean succeeded = false;
                try {
                    deployments.undeploy(this);
                    succeeded = true;
                } finally {
                    addLog(NInstallLogAction.UNDEPLOY, getId(), null, null, succeeded, getSession());
                }
                return this;
            }
        };
    }

    private static void invalidateInstallationDigest(NSession session) {
        String uuid = UUID.randomUUID().toString();
        NWorkspaceExt.of(session).setInstallationDigest(uuid,session);
    }

    @Override
    public NPushRepositoryCommand push() {
        return new AbstractNPushRepositoryCommand(this) {
            @Override
            public NPushRepositoryCommand run() {
                throw new NIllegalArgumentException(getSession(),
                        NMsg.ofC("unsupported push() for %s repository", getName())
                );
            }
        };
    }

    @Override
    public NFetchDescriptorRepositoryCommand fetchDescriptor() {
        return new AbstractNFetchDescriptorRepositoryCommand(this) {
            @Override
            public NFetchDescriptorRepositoryCommand run() {
                result = deployments.fetchDescriptorImpl(getId(), getSession());
                return this;
            }
        };
    }

    @Override
    public NFetchContentRepositoryCommand fetchContent() {
        return new AbstractNFetchContentRepositoryCommand(this) {
            @Override
            public NFetchContentRepositoryCommand run() {
                result = deployments.fetchContentImpl(getId(), getSession());
                return this;
            }
        };
    }

    @Override
    public NSearchRepositoryCommand search() {
        return new AbstractNSearchRepositoryCommand(this) {
            @Override
            public NSearchRepositoryCommand run() {
                NIterator<InstallInfoConfig> installIter = searchInstallConfig(getSession());
                NIterator<NId> idIter = IteratorBuilder.of(installIter, getSession()).map(NFunction.of(InstallInfoConfig::getId, "NutsInstallInformation->Id"))
                        .build();
                NIdFilter ff = getFilter();
                if (ff != null) {
                    idIter = IteratorBuilder.of(idIter, getSession()).filter(new NIdFilterToPredicate(ff, getSession())).build();
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
    public NSearchVersionsRepositoryCommand searchVersions() {
        return new AbstractNSearchVersionsRepositoryCommand(this) {
            @Override
            public NSearchVersionsRepositoryCommand run() {
                if (getFilter() instanceof NInstallStatusIdFilter) {
                    NPath installFolder
                            = NLocations.of(getSession()).getStoreLocation(getId()
                            .builder().setVersion("ANY").build(), NStoreType.CONF).getParent();
                    if (installFolder.isDirectory()) {
                        final NVersionFilter filter0 = getId().getVersion().filter(getSession());
                        result = IteratorBuilder.of(installFolder.stream().iterator(), getSession())
                                .map(NFunction.of(
                                        new Function<NPath, NId>() {
                                            @Override
                                            public NId apply(NPath folder) {
                                                if (folder.isDirectory()
                                                        && folder.resolve(NUTS_INSTALL_FILE).isRegularFile()) {
                                                    NVersion vv = NVersion.of(folder.getName()).get(getSession());
                                                    NIdFilter filter = getFilter();
                                                    NSession session = getSession();
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
    public NUpdateRepositoryStatisticsCommand updateStatistics() {
        return new AbstractNUpdateRepositoryStatisticsCommand(this) {
            @Override
            public NUpdateRepositoryStatisticsCommand run() {
                invalidateInstallationDigest(getSession());
                deployments.reindexFolder(getSession());
                return this;
            }
        };
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode, NSession session) {
        return mode == NFetchMode.LOCAL;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    public void addLog(NInstallLogAction action, NId id, NId requestor, String message, boolean succeeded, NSession session) {
        InstallLogItemTable.of(session)
                .add(new NInstallLogRecord(
                        Instant.now(),
                        NWorkspaceSecurityManager.of(session).getCurrentUsername(),
                        action,
                        id, requestor, message, succeeded
                ), session);
    }

}
