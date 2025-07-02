/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionFilterUtils;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionHelper;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
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
import net.thevpc.nuts.util.NLRUMap;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNInstalledRepository extends AbstractNRepository implements NInstalledRepository, NRepositoryExt0 {

    public static final String INSTALLED_REPO_UUID = "<main>";
    public static final String NUTS_INSTALL_FILE = "nuts-install.json";
    private final NRepositoryFolderHelper deployments;
    private final Map<NId, String> cachedDefaultVersions = new NLRUMap<>(200);

    public DefaultNInstalledRepository(NBootOptions bOptions) {
        super();
        this.deployments = new NRepositoryFolderHelper(this,
                NPath.of(bOptions.getStoreType(NStoreType.LIB).get()).resolve(NConstants.Folders.ID)
                , false,
                "lib", NElement.ofObjectBuilder().set("repoKind", "lib").build()
        );
        configModel = new InstalledRepositoryConfigModel(workspace, this);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNInstalledRepository.class);
    }

//    public Set<NutsId> getChildrenDependencies(NutsId id) {
//        return Collections.emptySet();
//    }
//
//    public Set<NutsId> getParentDependencies(NutsId id) {
//        return Collections.emptySet();
//    }
//
//    public void addDependency(NutsId id, NutsId parentId) {
//
//    }
//
//    public void removeDependency(NutsId id, NutsId parentId) {
//
//    }

    @Override
    public boolean isDefaultVersion(NId id) {
        String v = getDefaultVersion(id);
        return v.equals(id.getVersion().toString());
    }

    @Override
    public String getBootConnectionString() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public NIterator<NInstallInformation> searchInstallInformation() {
        return NStream.ofIterator(_wstore().searchInstalledVersions())
                .map(x -> {
                    try {
                        if (x != null) {
                            return getInstallInformation(x);
                        }
                    } catch (Exception ex) {
                        _LOGOP().error(ex)
                                .log(NMsg.ofJ("unable to parse {0}", x));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .iterator();
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
        String defaultVersion = NStringUtils.trim(_wstore().loadInstalledDefaultVersion(id));
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, defaultVersion);
        }
        return defaultVersion;
    }

    @Override
    public void setDefaultVersion(NId id) {
        NId baseVersion = id.getShortId();
        _wstore().saveInstalledDefaultVersion(id);
        String version = id.getVersion().getValue();
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, version);
        }
    }

    @Override
    public NInstallInformation getInstallInformation(NId id) {
        InstallInfoConfig c = _wstore().loadInstallInfoConfig(id);
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
        InstallInfoConfig ii = _wstore().loadInstallInfoConfig(id);
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
            _wstore().deleteInstallInfoConfig(id);
            String v = getDefaultVersion(id);
            if (v != null && v.equals(id.getVersion().getValue())) {
                Iterator<NId> versions = searchVersions().setId(id).setFilter(NDefinitionFilters.of().byInstalled(true)) //search only in installed, ignore deployed!
                        .setFetchMode(NFetchMode.LOCAL)
                        .getResult();
                List<NId> nutsIds = NCollections.list(versions == null ? Collections.emptyIterator() : versions);
                nutsIds.sort(null);
                if (!nutsIds.isEmpty()) {
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


                        InstallInfoConfig fi = _wstore().loadInstallInfoConfig(requiredId);
                        if (fi == null) {
                            throw new NInstallException(requiredId);
                        }
                        InstallInfoConfig fi0 = fi.copy();
                        InstallInfoConfig ti = _wstore().loadInstallInfoConfig(requestorId);
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
            InstallInfoConfig fi = _wstore().loadInstallInfoConfig(requiredId);
            if (fi == null) {
                throw new NInstallException(requiredId);
            }
            InstallInfoConfig ti = _wstore().loadInstallInfoConfig(requestorId);
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


    public NIterator<InstallInfoConfig> searchInstallConfig() {
        return NIterator.of(_wstore().searchInstalledVersions());
    }

    private NWorkspaceStore _wstore() {
        return ((NWorkspaceExt) workspace).store();
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
        InstallInfoConfig ii = _wstore().loadInstallInfoConfig(id1);
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
        _wstore().saveInstallInfoConfig(ii);
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
            _wstore().saveInstallInfoConfig(ii);
        }
    }

    public void addString(NId id, String name, String value) {
        getPath(id, name).writeString(value);
    }

    public <T> T readJson(NId id, String name, Class<T> clazz) {
        return NElementParser.ofJson().parse(getPath(id, name), clazz);
    }

    public void printJson(NId id, String name, InstallInfoConfig value) {
        value.setConfigVersion(workspace.getApiVersion());
        NElementWriter.ofJson().write(value, getPath(id, name));
    }

    public void remove(NId id, String name) {
        NPath path = getPath(id, name);
        path.delete();
    }

    public boolean contains(NId id, String name) {
        return getPath(id, name).isRegularFile();
    }

    public NPath getPath(NId id, String name) {
        return NWorkspace.of().getStoreLocation(id, NStoreType.CONF).resolve(name);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // implementation of repository

    /// //////////////////////////////////////////////////////////////////////////////////////////
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
                        .map(NFunction.of(InstallInfoConfig::getId).redescribe(NDescribableElementSupplier.of("NutsInstallInformation->Id")))
                        .build();
                NDefinitionFilter ff = getFilter();
                if (ff != null) {
                    idIter = NIteratorBuilder.of(idIter).filter(NDefinitionFilterUtils.toIdPredicate(ff)).build();
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
//                if (getFilter() instanceof NInstallStatusDefinitionFilter) {
                final NVersionFilter filter0 = getId().getVersion().filter();
                result = NStream.ofIterator(_wstore().searchInstalledVersions(getId()))
                        .map(vv -> {
                            NId newId = getId().builder().setVersion(vv).build();
                            if (filter0.acceptVersion(vv) && (filter == null || filter.acceptDefinition(NDefinitionHelper.ofIdOnlyFromRepo(newId, repo, "DefaultNInstalledRepository")))) {
                                return newId;
                            }
                            return null;
                        }).nonNull().redescribe(NDescribableElementSupplier.of("FileToVersion")).iterator();
//                } else {
//                    this.result = NIteratorBuilder.of(deployments.searchVersions(getId(), getFilter(), true))
//                            .named(NElements.ofUplet("searchVersionsInMain"))
//                            .build()
//                    ;
//                }
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
