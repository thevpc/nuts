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
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.concurrent.NLocks;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.io.util.FolderObjectIterator;
import net.thevpc.nuts.runtime.standalone.io.util.NInstallStatusIdFilter;
import net.thevpc.nuts.runtime.standalone.repository.cmd.deploy.AbstractNDeployRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.AbstractNFetchContentRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.AbstractNFetchDescriptorRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.push.AbstractNPushRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.AbstractNSearchRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.AbstractNSearchVersionsRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy.AbstractNRepositoryUndeployCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats.AbstractNUpdateRepositoryStatsCmd;
import net.thevpc.nuts.runtime.standalone.repository.impl.AbstractNRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt0;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.util.NLRUMap;
import net.thevpc.nuts.runtime.standalone.util.filters.NIdFilterToPredicate;
import net.thevpc.nuts.util.NIteratorBuilder;
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
    private final Map<NId, String> cachedDefaultVersions = new NLRUMap<>(200);

    public DefaultNInstalledRepository(NWorkspace ws, NBootOptions bOptions) {
        super(ws);
        this.deployments = new NRepositoryFolderHelper(this,
                ws,
                NPath.of(bOptions.getStoreType(NStoreType.LIB).get()).resolve(NConstants.Folders.ID)
                , false,
                "lib", NElements.of().ofObject().set("repoKind", "lib").build()
        );
        configModel = new InstalledRepositoryConfigModel(workspace, this);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNInstalledRepository.class);
    }

//    public Set<NutsId> getChildrenDependencies(NutsId id, NSession session) {
//        return Collections.emptySet();
//    }
//
//    public Set<NutsId> getParentDependencies(NutsId id, NSession session) {
//        return Collections.emptySet();
//    }
//
//    public void addDependency(NutsId id, NutsId parentId, NSession session) {
//
//    }
//
//    public void removeDependency(NutsId id, NutsId parentId, NSession session) {
//
//    }

    @Override
    public boolean isDefaultVersion(NId id) {
        String v = getDefaultVersion(id);
        return v.equals(id.getVersion().toString());
    }

    @Override
    public NIterator<NInstallInformation> searchInstallInformation() {
        NPath rootFolder = NWorkspace.of().getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.ID);
        return new FolderObjectIterator<NInstallInformation>("NutsInstallInformation",
                rootFolder,
                null, -1, new FolderObjectIterator.FolderIteratorModel<NInstallInformation>() {
            @Override
            public boolean isObjectFile(NPath pathname) {
                return pathname.getName().equals(NUTS_INSTALL_FILE);
            }

            @Override
            public NInstallInformation parseObject(NPath path) throws IOException {
                try {
                    InstallInfoConfig c = getInstallInfoConfig(null, path);
                    if (c != null) {
                        return getInstallInformation(c);
                    }
                } catch (Exception ex) {
                    _LOGOP().error(ex)
                            .log(NMsg.ofJ("unable to parse {0}", path));
                }
                return null;
            }
        }
        );
    }

    @Override
    public String getDefaultVersion(NId id) {
        NId baseVersion = id.getShortId();
        synchronized (cachedDefaultVersions) {
            String p = cachedDefaultVersions.get(baseVersion);
            if (p != null) {
                return p;
            }
        }
        NPath pp = NWorkspace.of().getStoreLocation(id
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
    public void setDefaultVersion(NId id) {
        NId baseVersion = id.getShortId();
        String version = id.getVersion().getValue();
        NPath pp = NWorkspace.of().getStoreLocation(id
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
    public NInstallInformation getInstallInformation(NId id) {
        InstallInfoConfig c = getInstallInfoConfig(id, null);
        return c != null ? getInstallInformation(c) : DefaultNInstallInfo.notInstalled(id);
    }

    @Override
    public NInstallStatus getInstallStatus(NId id) {
        NInstallInformation ii = getInstallInformation(id);
        if (ii == null) {
            return NInstallStatus.NONE;
        }
        return ii.getInstallStatus();
    }

    @Override
    public void install(NId id, NId forId) {
        boolean succeeded = false;
        NWorkspaceUtils.of(workspace).checkReadOnly();
        InstallInfoConfig ii = getInstallInfoConfig(id, null);
        try {
            invalidateInstallationDigest();
            String repository = id.getRepository();
            NRepository r = workspace.findRepository(repository).orNull();
            if (ii == null) {
                ii = new InstallInfoConfig();
                ii.setId(id);
                ii.setInstalled(forId == null);
                if (r != null) {
                    ii.setSourceRepoName(r.getName());
                    ii.setSourceRepoUUID(r.getUuid());
                }
                saveCreate(ii);
            } else {
                InstallInfoConfig ii0 = ii.copy();
                ii.setId(id);
                ii.setInstalled(forId == null);
                if (r != null) {
                    ii.setSourceRepoName(r.getName());
                    ii.setSourceRepoUUID(r.getUuid());
                }
                saveUpdate(ii, ii0);
            }

            succeeded = true;
        } catch (UncheckedIOException | NIOException ex) {
            throw new NNotInstallableException(id,
                    NMsg.ofC("failed to install %s : %s", id, ex)
                    , ex);
        } finally {
            addLog(NInstallLogAction.INSTALL, id, forId, null, succeeded);
        }
    }

    @Override
    public NInstallInformation install(NDefinition def) {
        boolean succeeded = false;
        try {
            NInstallInformation a = updateInstallInformation(def, true, null, true);
            succeeded = true;
            return a;
        } finally {
            addLog(NInstallLogAction.INSTALL, def.getId(), null, null, succeeded);
        }
    }

    @Override
    public void uninstall(NDefinition def) {
        boolean succeeded = false;
        NWorkspaceUtils.of(workspace).checkReadOnly();
        NId id = def.getId();
        NInstallStatus installStatus = getInstallStatus(id);
        if (!installStatus.isInstalled()) {
            throw new NNotInstalledException(id);
        }
        try {
            String pck = def.getDescriptor().getPackaging();
            undeploy().setId(id.builder().setPackaging(NBlankable.isBlank(pck) ? "jar" : pck).build())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .run();
            remove(id, NUTS_INSTALL_FILE);
            String v = getDefaultVersion(id);
            if (v != null && v.equals(id.getVersion().getValue())) {
                Iterator<NId> versions = searchVersions().setId(id)
                        .setFilter(NIdFilters.of().byInstallStatus(
                                NInstallStatusFilters.of().byInstalled(true)
                        )) //search only in installed, ignore deployed!
                        .setFetchMode(NFetchMode.LOCAL)
                        .getResult();
                List<NId> nutsIds = NCollections.list(versions == null ? Collections.emptyIterator() : versions);
                nutsIds.sort(null);
                if (nutsIds.size() > 0) {
                    setDefaultVersion(nutsIds.get(0));
                } else {
                    setDefaultVersion(id.builder().setVersion("").build());
                }
            }
            succeeded = true;
        } catch (Exception ex) {
            throw new NNotInstalledException(id);
        } finally {
            addLog(NInstallLogAction.UNINSTALL, id, null, null, succeeded);
        }
    }

    @Override
    public NInstallInformation require(NDefinition def, boolean deploy, NId[] forIds, NDependencyScope scope) {
        boolean succeeded = false;
        NId requiredId = def.getId();
        NInstallInformation nInstallInformation = updateInstallInformation(def, null, true, deploy);
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


                        InstallInfoConfig fi = getInstallInfoConfig(requiredId, null);
                        if (fi == null) {
                            throw new NInstallException(requiredId);
                        }
                        InstallInfoConfig fi0 = fi.copy();
                        InstallInfoConfig ti = getInstallInfoConfig(requestorId, null);
                        InstallInfoConfig ti0 = ti == null ? null : ti.copy();
                        //there is no need to check for the target dependency (the reason why the 'requiredId' needs to be installed)
                        if (!fi.isRequired()) {
                            fi.setRequired(true);
                            fi.setRequiredBy(addDistinct(fi.getRequiredBy(), new InstallDepConfig(requestorId, scope)));
                            saveUpdate(fi, fi0);
                        }
                        saveUpdate(fi, fi0);
                        if (ti == null) {
                            ti = new InstallInfoConfig();
                            ti.setId(requestorId);
                            ti.setRequires(addDistinct(ti.getRequires(), new InstallDepConfig(requiredId, scope)));
                            saveCreate(ti);
                        } else {
                            ti.setRequires(addDistinct(ti.getRequires(), new InstallDepConfig(requiredId, scope)));
                            saveUpdate(ti, ti0);
                        }
                        succeeded = true;
                    } finally {
                        addLog(NInstallLogAction.REQUIRE, requiredId, requestorId, null, succeeded);
                    }
                }
            }
        }
        return nInstallInformation;
    }

    @Override
    public void unrequire(NId requiredId, NId requestorId, NDependencyScope scope) {
        Instant now = Instant.now();
        String user = NWorkspaceSecurityManager.of().getCurrentUsername();
        boolean succeeded = false;
        try {
            if (scope == null) {
                scope = NDependencyScope.API;
            }
            InstallInfoConfig fi = getInstallInfoConfig(requiredId, null);
            if (fi == null) {
                throw new NInstallException(requiredId);
            }
            InstallInfoConfig ti = getInstallInfoConfig(requestorId, null);
            if (ti == null) {
                throw new NInstallException(requestorId);
            }
            InstallInfoConfig fi0 = fi.copy();
            InstallInfoConfig ti0 = ti.copy();

            fi.setRequiredBy(removeDistinct(fi.getRequiredBy(), new InstallDepConfig(requestorId, scope)));
            ti.setRequires(removeDistinct(ti.getRequires(), new InstallDepConfig(requiredId, scope)));
            if (fi.isRequired() != fi.getRequiredBy().size() > 0) {
                fi.setRequired(fi.getRequiredBy().size() > 0);
            }
            saveUpdate(fi, fi0);
            saveUpdate(ti, ti0);
            succeeded = true;
        } finally {
            addLog(NInstallLogAction.UNREQUIRE, requiredId, requestorId, null, succeeded);
        }
    }

    @Override
    public NStream<NInstallLogRecord> findLog() {
        return InstallLogItemTable.of(workspace).stream();
    }

    public NId pathToId(NPath path) {
        NPath rootFolder = NWorkspace.of().getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.ID);
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


    public InstallInfoConfig getInstallInfoConfig(NId id, NPath path) {
        if (id == null && path == null) {
            CoreNIdUtils.checkShortId(id);
        }
        if (path == null) {
            path = getPath(id, NUTS_INSTALL_FILE);
        }
//        if (id == null) {
//            path = getPath(id, NUTS_INSTALL_FILE);
//        }
        NPath finalPath = path;
        if (path.isRegularFile()) {
            NElements elem = NElements.of();
            InstallInfoConfig c = NLocks.of().setSource(path).call(
                    () -> elem.json().parse(finalPath, InstallInfoConfig.class),
                    CoreNUtils.LOCK_TIME, CoreNUtils.LOCK_TIME_UNIT
            );
            if (c != null) {
                boolean changeStatus = false;
                NVersion v = c.getConfigVersion();
                if (NBlankable.isBlank(v)) {
                    c.setInstalled(true);
                    c.setConfigVersion(NVersion.get("0.5.8").get()); //last version before 0.6
                    changeStatus = true;
                }
                NId idOk = c.getId();
                if (idOk == null) {
                    if (id != null) {
                        c.setId(id);
                        changeStatus = true;
                    } else {
                        NId idOk2 = pathToId(path);
                        if (idOk2 != null) {
                            c.setId(idOk2);
                            changeStatus = true;
                        } else {
                            return null;
                        }
                    }
                }
                if (changeStatus && !NWorkspace.of().isReadOnly()) {
                    NLocks.of().setSource(path).call(() -> {
                                _LOGOP().level(Level.CONFIG)
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

    public NIterator<InstallInfoConfig> searchInstallConfig() {
        NPath rootFolder = NWorkspace.of().getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.ID);
        return new FolderObjectIterator<InstallInfoConfig>("InstallInfoConfig",
                rootFolder,
                null, -1, new FolderObjectIterator.FolderIteratorModel<InstallInfoConfig>() {
            @Override
            public boolean isObjectFile(NPath pathname) {
                return pathname.getName().equals(NUTS_INSTALL_FILE);
            }

            @Override
            public InstallInfoConfig parseObject(NPath path) throws IOException {
                try {
                    InstallInfoConfig c = getInstallInfoConfig(null, path);
                    if (c != null) {
                        return c;
                    }
                } catch (Exception ex) {
                    _LOGOP().error(ex).log(NMsg.ofJ("unable to parse {0}", path));
                }
                return null;
            }
        }
        );
    }

    public NInstallInformation getInstallInformation(InstallInfoConfig ii) {
        NSession session = workspace.currentSession();
        boolean obsolete = false;
        boolean defaultVersion = false;
        if (ii.isInstalled()) {
            defaultVersion = isDefaultVersion(ii.getId());
        }
        Instant expireTime = session.getExpireTime().orNull();
        if (expireTime != null && (ii.isInstalled() || ii.isRequired())) {
            if (ii.isInstalled() || ii.isRequired()) {
                Instant lastModifiedDate = ii.getLastModificationDate();
                if (lastModifiedDate == null) {
                    lastModifiedDate = ii.getCreationDate();
                }
                if (lastModifiedDate == null || lastModifiedDate.isBefore(expireTime)) {
                    obsolete = true;
                }
            }
        }
        NInstallStatus s = NInstallStatus.of(ii.isInstalled(), ii.isRequired(), obsolete, defaultVersion);
        return new DefaultNInstallInfo(ii.getId(),
                s,
                NWorkspace.of().getStoreLocation(ii.getId(), NStoreType.BIN),
                ii.getCreationDate(),
                ii.getLastModificationDate(),
                ii.getCreationUser(),
                ii.getSourceRepoName(),
                ii.getSourceRepoUUID(),
                false, false //will be processed later!
        );
    }

    private NInstallInformation updateInstallInformation(NDefinition def, Boolean install, Boolean require, boolean deploy) {
        NSession session = workspace.currentSession();
        invalidateInstallationDigest();
        NId id1 = def.getId();
        InstallInfoConfig ii = getInstallInfoConfig(id1, null);
        boolean wasInstalled = false;
        boolean wasRequired = false;
        if (deploy) {
            session.copy().setConfirm(NConfirmationMode.YES).runWith(() ->
                    this.deploy()
                            .setId(id1)
                            .setContent(def.getContent().orNull())
                            //.setFetchMode(NutsFetchMode.LOCAL)
                            .setDescriptor(def.getDescriptor())
                            .run()
            );
        }
        if (ii == null) {
//            for (NutsDependency dependency : def.getDependencies()) {
//                Iterator<NutsId> it = searchVersions().setId(dependency.getId()).setSession(NutsWorkspaceHelper.createRepositorySession(session, this, NutsFetchMode.DEPLOYED)).getResult();
//                if (!it.hasNext()) {
//                    throw new IllegalArgumentException("failed to install " + def.getId() + " as dependencies are missing.");
//                }
//            }
            NId id = id1;
            NWorkspaceUtils.of(workspace).checkReadOnly();
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
                saveCreate(ii);
            } catch (UncheckedIOException | NIOException ex) {
                throw new NNotInstallableException(id, NMsg.ofC("failed to install %s : %s", id, ex), ex);
            }
            DefaultNInstallInfo uu = (DefaultNInstallInfo) getInstallInformation(ii);
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
            saveUpdate(ii, ii0);
            DefaultNInstallInfo uu = (DefaultNInstallInfo) getInstallInformation(ii);
            uu.setWasInstalled(wasInstalled);
            uu.setWasRequired(wasRequired);
            uu.setJustInstalled(install != null && install);
            uu.setJustRequired(require != null && require);
            return uu;
        }
    }

    private void saveCreate(InstallInfoConfig ii) {
        Instant now = Instant.now();
        String user = NWorkspaceSecurityManager.of().getCurrentUsername();
        if (ii.getCreationUser() == null) {
            ii.setCreationUser(user);
        }
        if (ii.getCreationDate() == null) {
            ii.setCreationDate(now);
        }
        ii.setConfigVersion(DefaultNWorkspace.VERSION_INSTALL_INFO_CONFIG);
        printJson(ii.getId(), NUTS_INSTALL_FILE, ii);
    }

    private void saveUpdate(InstallInfoConfig ii, InstallInfoConfig ii0) {
        Instant now = Instant.now();
        String user = NWorkspaceSecurityManager.of().getCurrentUsername();
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
            printJson(ii.getId(), NUTS_INSTALL_FILE, ii);
        }
    }

    public void addString(NId id, String name, String value, NSession session) {
        getPath(id, name).writeString(value);
    }

    public <T> T readJson(NId id, String name, Class<T> clazz, NSession session) {
        return NElements.of()
                .json()
                .parse(getPath(id, name), clazz);
    }

    public void printJson(NId id, String name, InstallInfoConfig value) {
        value.setConfigVersion(workspace.getApiVersion());
        NElements.of().setNtf(false)
                .json().setValue(value)
                .print(getPath(id, name));
    }

    public void remove(NId id, String name) {
        NPath path = getPath(id, name);
        path.delete();
    }

    public boolean contains(NId id, String name, NSession session) {
        return getPath(id, name).isRegularFile();
    }

    public NPath getPath(NId id, String name) {
        return NWorkspace.of().getStoreLocation(id, NStoreType.CONF).resolve(name);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of repository
    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public NRepositorySecurityManager security() {
        throw new IllegalArgumentException("unsupported security() for " + getName() + " repository");
    }

    @Override
    public NDeployRepositoryCmd deploy() {
        return new AbstractNDeployRepositoryCmd(this) {
            @Override
            public NDeployRepositoryCmd run() {
                invalidateInstallationDigest();
                boolean succeeded = false;
                try {
                    NDescriptor rep = deployments.deploy(this, NConfirmationMode.YES);
                    this.setDescriptor(rep);
                    this.setId(rep.getId());
                    succeeded = true;
                } finally {
                    addLog(NInstallLogAction.DEPLOY, getId(), null, null, succeeded);
                }
                return this;
            }
        };
    }

    @Override
    public NRepositoryUndeployCmd undeploy() {
        return new AbstractNRepositoryUndeployCmd(this) {
            @Override
            public NRepositoryUndeployCmd run() {
                invalidateInstallationDigest();
                boolean succeeded = false;
                try {
                    deployments.undeploy(this);
                    succeeded = true;
                } finally {
                    addLog(NInstallLogAction.UNDEPLOY, getId(), null, null, succeeded);
                }
                return this;
            }
        };
    }

    private static void invalidateInstallationDigest() {
        String uuid = UUID.randomUUID().toString();
        NWorkspaceExt.of().setInstallationDigest(uuid);
    }

    @Override
    public NPushRepositoryCmd push() {
        return new AbstractNPushRepositoryCmd(this) {
            @Override
            public NPushRepositoryCmd run() {
                throw new NIllegalArgumentException(
                        NMsg.ofC("unsupported push() for %s repository", getName())
                );
            }
        };
    }

    @Override
    public NFetchDescriptorRepositoryCmd fetchDescriptor() {
        return new AbstractNFetchDescriptorRepositoryCmd(this) {
            @Override
            public NFetchDescriptorRepositoryCmd run() {
                result = deployments.fetchDescriptorImpl(getId());
                return this;
            }
        };
    }

    @Override
    public NFetchContentRepositoryCmd fetchContent() {
        return new AbstractNFetchContentRepositoryCmd(this) {
            @Override
            public NFetchContentRepositoryCmd run() {
                result = deployments.fetchContentImpl(getId());
                return this;
            }
        };
    }

    @Override
    public NSearchRepositoryCmd search() {
        return new AbstractNSearchRepositoryCmd(this) {
            @Override
            public NSearchRepositoryCmd run() {
                NIterator<InstallInfoConfig> installIter = searchInstallConfig();
                NIterator<NId> idIter = NIteratorBuilder.of(installIter)
                        .map(NFunction.of(InstallInfoConfig::getId).withDesc(NEDesc.of("NutsInstallInformation->Id")))
                        .build();
                NIdFilter ff = getFilter();
                if (ff != null) {
                    idIter = NIteratorBuilder.of(idIter).filter(new NIdFilterToPredicate(ff)).build();
                }
                result = idIter; //deployments.searchImpl(getFilter(), session)
                if (result == null) {
                    result = NIteratorBuilder.emptyIterator();
                }
                return this;
            }

        };
    }

    @Override
    public NSearchVersionsRepositoryCmd searchVersions() {
        return new AbstractNSearchVersionsRepositoryCmd(this) {
            @Override
            public NSearchVersionsRepositoryCmd run() {
                if (getFilter() instanceof NInstallStatusIdFilter) {
                    NPath installFolder
                            = NWorkspace.of().getStoreLocation(getId()
                            .builder().setVersion("ANY").build(), NStoreType.CONF).getParent();
                    if (installFolder.isDirectory()) {
                        final NVersionFilter filter0 = getId().getVersion().filter();
                        result = NIteratorBuilder.of(installFolder.stream().iterator())
                                .map(NFunction.of(
                                        new Function<NPath, NId>() {
                                            @Override
                                            public NId apply(NPath folder) {
                                                if (folder.isDirectory()
                                                        && folder.resolve(NUTS_INSTALL_FILE).isRegularFile()) {
                                                    NVersion vv = NVersion.get(folder.getName()).get();
                                                    NIdFilter filter = getFilter();
                                                    if (filter0.acceptVersion(vv) && (filter == null || filter.acceptId(
                                                            getId().builder().setVersion(vv).build()
                                                    ))) {
                                                        return getId().builder().setVersion(folder.getName()).build();
                                                    }
                                                }
                                                return null;
                                            }
                                        }).withDesc(NEDesc.of("FileToVersion")))
                                .notNull().iterator();
                    } else {
                        //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
                        result = NIteratorBuilder.emptyIterator();
                    }
                } else {
                    this.result = NIteratorBuilder.of(deployments.searchVersions(getId(), getFilter(), true))
                            .named("searchVersionsInMain()")
                            .build()
                    ;
                }
                return this;
            }
        };
    }

    @Override
    public NUpdateRepositoryStatsCmd updateStatistics() {
        return new AbstractNUpdateRepositoryStatsCmd(this) {
            @Override
            public NUpdateRepositoryStatsCmd run() {
                invalidateInstallationDigest();
                deployments.reindexFolder();
                return this;
            }
        };
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode) {
        return mode == NFetchMode.LOCAL;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    public void addLog(NInstallLogAction action, NId id, NId requestor, String message, boolean succeeded) {
        InstallLogItemTable.of(workspace)
                .add(new NInstallLogRecord(
                        Instant.now(),
                        NWorkspaceSecurityManager.of().getCurrentUsername(),
                        action,
                        id, requestor, message, succeeded
                ));
    }

}
