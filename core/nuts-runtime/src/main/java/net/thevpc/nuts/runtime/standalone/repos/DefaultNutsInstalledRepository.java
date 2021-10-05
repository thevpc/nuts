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
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.collections.LRUMap;
import net.thevpc.nuts.runtime.bundles.io.FolderObjectIterator;
import net.thevpc.nuts.runtime.bundles.io.NutsInstallStatusIdFilter;
import net.thevpc.nuts.runtime.bundles.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.bundles.iter.LazyIterator;
import net.thevpc.nuts.runtime.bundles.parsers.StringTokenizerUtils;
import net.thevpc.nuts.runtime.core.repos.AbstractNutsRepository;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryConfigModel;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt0;
import net.thevpc.nuts.runtime.core.util.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.repocommands.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.*;

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

/**
 * @author thevpc
 */
public class DefaultNutsInstalledRepository extends AbstractNutsRepository implements NutsInstalledRepository, NutsRepositoryExt0 {

    public static final String INSTALLED_REPO_UUID = "<main>";
    private static final String NUTS_INSTALL_FILE = "nuts-install.json";
    private final NutsRepositoryFolderHelper deployments;
    private final Map<NutsId, String> cachedDefaultVersions = new LRUMap<>(200);
    private NutsLogger LOG;

    public DefaultNutsInstalledRepository(NutsWorkspace ws, NutsWorkspaceInitInformation info) {
        this.workspace = ws;
        deployments = new NutsRepositoryFolderHelper(this, ws,
                Paths.get(info.getStoreLocation(NutsStoreLocation.LIB)).resolve(NutsConstants.Folders.ID)
                , false
        );
        configModel = new InstalledRepositoryConfigModel(workspace, this);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = this.workspace.log().setSession(session).of(DefaultNutsInstalledRepository.class);
        }
        return LOG;
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
    public Iterator<NutsInstallInformation> searchInstallInformation(NutsSession session) {
        Path rootFolder = Paths.get(session.locations().getStoreLocation(NutsStoreLocation.CONFIG)).resolve(NutsConstants.Folders.ID);
        return new FolderObjectIterator<NutsInstallInformation>("NutsInstallInformation",
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
        NutsId baseVersion = id.getShortNameId();
        synchronized (cachedDefaultVersions) {
            String p = cachedDefaultVersions.get(baseVersion);
            if (p != null) {
                return p;
            }
        }
        Path pp = Paths.get(session.locations().getStoreLocation(id
                        //.setAlternative("")
                        .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG))
                .resolveSibling("default-version");
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
        Path pp = Paths.get(session.locations().getStoreLocation(id
                        //                .setAlternative("")
                        .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG))
                .resolveSibling("default-version");
        if (NutsBlankable.isBlank(version)) {
            if (Files.isRegularFile(pp)) {
                try {
                    Files.delete(pp);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        } else {
            try {
                CoreIOUtils.mkdirs(pp.getParent(),session);
                Files.write(pp, version.trim().getBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
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
        Instant now = Instant.now();
        String user = session.security().getCurrentUsername();
        NutsWorkspaceUtils.of(session).checkReadOnly();
        InstallInfoConfig ii;
        try {
            String repository = id.getRepository();
            NutsRepository r = session.repos().findRepository(repository);
            ii = new InstallInfoConfig();
            ii.setConfigVersion(DefaultNutsWorkspace.VERSION_INSTALL_INFO_CONFIG);
            ii.setId(id);
            ii.setCreatedDate(now);
            ii.setInstallUser(user);
            ii.setInstalled(forId == null);
            if (r != null) {
                ii.setSourceRepoName(r.getName());
                ii.setSourceRepoUUID(r.getUuid());
            }
            printJson(id, NUTS_INSTALL_FILE, ii, session);
        } catch (UncheckedIOException | NutsIOException ex) {
            throw new NutsNotInstallableException(session, id,
                    NutsMessage.cstyle("failed to install %s : %s", id, ex)
                    , ex);
        }
    }

    @Override
    public NutsInstallInformation install(NutsDefinition def, NutsSession session) {
        return updateInstallInformation(def, true, null, true, session);
    }

    @Override
    public void uninstall(NutsDefinition def, NutsSession session) {
        NutsWorkspaceUtils.of(session).checkReadOnly();
        NutsWorkspaceUtils.checkSession(workspace, session);
        NutsInstallStatus installStatus = getInstallStatus(def.getId(), session);
        if (!installStatus.isInstalled()) {
            throw new NutsNotInstalledException(session, def.getId());
        }
        try {
            String pck = def.getDescriptor().getPackaging();
            undeploy().setId(def.getId().builder().setPackaging(NutsBlankable.isBlank(pck) ? "jar" : pck).build())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .setSession(session)
                    .run();
            remove(def.getId(), NUTS_INSTALL_FILE, session);
            String v = getDefaultVersion(def.getId(), session);
            if (v != null && v.equals(def.getId().getVersion().getValue())) {
                Iterator<NutsId> versions = searchVersions().setId(def.getId())
                        .setFilter(session.id().filter().byInstallStatus(
                                session.filters().installStatus().byInstalled(true)
                        )) //search only in installed, ignore deployed!
                        .setFetchMode(NutsFetchMode.LOCAL)
                        .setSession(session).getResult();
                List<NutsId> nutsIds = CoreCollectionUtils.toList(versions == null ? Collections.emptyIterator() : versions);
                nutsIds.sort(null);
                if (nutsIds.size() > 0) {
                    setDefaultVersion(nutsIds.get(0), session);
                } else {
                    setDefaultVersion(def.getId().builder().setVersion("").build(), session);
                }
            }
        } catch (Exception ex) {
            throw new NutsNotInstalledException(session, def.getId());
        }
    }

    @Override
    public NutsInstallInformation require(NutsDefinition def, boolean deploy, NutsId[] forIds, NutsDependencyScope scope, NutsSession session) {
        NutsInstallInformation nutsInstallInformation = updateInstallInformation(def, null, true, deploy, session);
        if (forIds != null) {
            for (NutsId otherId : forIds) {
                addDependency(def.getId(), otherId, scope, session);
            }
        }
        return nutsInstallInformation;
    }

    @Override
    public void unrequire(NutsId id, NutsId forId, NutsDependencyScope scope, NutsSession session) {
        removeDependency(id, forId, scope, session);
    }

    public NutsId pathToId(Path path, NutsSession session) {
        Path rootFolder = Paths.get(session.locations().getStoreLocation(NutsStoreLocation.CONFIG)).resolve(NutsConstants.Folders.ID);
        String p = path.toString().substring(rootFolder.toString().length());
        List<String> split = StringTokenizerUtils.split(p, "/\\");
        if (split.size() >= 4) {
            return session.id().builder().setArtifactId(split.get(split.size() - 3))
                    .setGroupId(String.join(".", split.subList(0, split.size() - 3)))
                    .setVersion(split.get(split.size() - 2)).build();

        }
        return null;
    }

    public void updateInstallInfoConfigInstallDate(NutsId id, Instant instant, NutsSession session) {
        Path path = getPath(id, NUTS_INSTALL_FILE, session);
        InstallInfoConfig ii = getInstallInfoConfig(id, path, session);
        if (ii == null) {
            throw new NutsNotFoundException(session, id);
        }
        ii.setCreatedDate(instant);
        try {
            printJson(id, NUTS_INSTALL_FILE, ii, session);
        } catch (UncheckedIOException | NutsIOException ex) {
            throw new NutsNotInstallableException(session, id,
                    NutsMessage.cstyle("failed to install %s : %s", id, ex), ex);
        }
    }

    public Path getDepsPath(NutsId id, boolean from, NutsDependencyScope scope, NutsSession session) {
        if (from) {
            return getPath(id, "nuts-deps-from-" + scope.id() + ".json", session);
        } else {
            return getPath(id, "nuts-deps-to-" + scope.id() + ".json", session);
        }
    }

    /**
     * @param from    the reference id
     * @param to      the id for which (the reason) 'from' has been required/installed
     * @param scope   dependency scope
     * @param session session
     */
    public synchronized void addDependency(NutsId from, NutsId to, NutsDependencyScope scope, NutsSession session) {
        if (scope == null) {
            scope = NutsDependencyScope.API;
        }
        InstallInfoConfig fi = getInstallInfoConfig(from, null, session);
        if (fi == null) {
            throw new NutsInstallException(session, from);
        }
        //there is no need to check for the target dependency (the reason why the 'from' needs to be installed)
//        InstallInfoConfig ft = getInstallInfoConfig(to, null, session);
//        if (ft == null) {
//            throw new NutsInstallException(session, to);
//        }
        if (!fi.required) {
            fi.required = true;
            printJson(from, NUTS_INSTALL_FILE, fi, session);
        }
        Set<NutsId> list = findDependenciesFrom(from, scope, session);
        NutsElementFormat element = session.elem().setSession(session);
        if (!list.contains(to)) {
            list.add(to);
            element.setContentType(NutsContentType.JSON).setValue(list.toArray(new NutsId[0]))
                    .setSession(session)
                    .print(getDepsPath(from, true, scope, session));
        }
        list = findDependenciesTo(to, scope, session);
        if (!list.contains(from)) {
            list.add(from);
            element.setContentType(NutsContentType.JSON).setValue(list.toArray(new NutsId[0]))
                    .setSession(session)
                    .print(getDepsPath(to, false, scope, session));
        }
    }

    public synchronized void removeDependency(NutsId from, NutsId to, NutsDependencyScope scope, NutsSession session) {
        if (scope == null) {
            scope = NutsDependencyScope.API;
        }
        InstallInfoConfig fi = getInstallInfoConfig(from, null, session);
        if (fi == null) {
            throw new NutsInstallException(session, from);
        }
        InstallInfoConfig ft = getInstallInfoConfig(to, null, session);
        if (ft == null) {
            throw new NutsInstallException(session, to);
        }
        Set<NutsId> list = findDependenciesFrom(from, scope, session);
        boolean stillRequired = false;
        if (list.contains(to)) {
            list.remove(to);
            stillRequired = list.size() > 0;
            session.elem().setContentType(NutsContentType.JSON).setValue(list.toArray(new NutsId[0])).print(getDepsPath(from, true, scope, session));
        }
        list = findDependenciesTo(to, scope, session);
        if (list.contains(from)) {
            list.remove(from);
            session.elem().setContentType(NutsContentType.JSON).setValue(list.toArray(new NutsId[0])).print(getDepsPath(to, false, scope, session));
        }
        if (fi.required != stillRequired) {
            fi.required = true;
            printJson(from, NUTS_INSTALL_FILE, fi, session);
        }
    }

    public synchronized Set<NutsId> findDependenciesFrom(NutsId from, NutsDependencyScope scope, NutsSession session) {
        Path path = getDepsPath(from, true, scope, session);
        if (Files.isRegularFile(path)) {
            NutsId[] old = session.elem().setSession(session).setContentType(NutsContentType.JSON).parse(path, NutsId[].class);
            return new HashSet<NutsId>(Arrays.asList(old));
        }
        return new HashSet<>();
    }

    public synchronized Set<NutsId> findDependenciesTo(NutsId from, NutsDependencyScope scope, NutsSession session) {
        Path path = getDepsPath(from, false, scope, session);
        if (Files.isRegularFile(path)) {
            NutsId[] old = session.elem().setSession(session).setContentType(NutsContentType.JSON).parse(path, NutsId[].class);
            return new HashSet<NutsId>(Arrays.asList(old));
        }
        return new HashSet<>();
    }

    public InstallInfoConfig getInstallInfoConfig(NutsId id, Path path, NutsSession session) {
        if (id == null && path == null) {
            NutsWorkspaceUtils.of(session).checkSimpleNameNutsId(id);
        }
        if (path == null) {
            path = getPath(id, NUTS_INSTALL_FILE, session);
        }
//        if (id == null) {
//            path = getPath(id, NUTS_INSTALL_FILE);
//        }
        Path finalPath = path;
        NutsWorkspace workspace = session.getWorkspace();
        if (Files.isRegularFile(path)) {
            InstallInfoConfig c = workspace.concurrent().lock().setSource(path).setSession(session).call(
                    () -> workspace.elem().setSession(session).setContentType(NutsContentType.JSON).parse(finalPath, InstallInfoConfig.class),
                    CoreNutsUtils.LOCK_TIME, CoreNutsUtils.LOCK_TIME_UNIT
            );
            if (c != null) {
                boolean changeStatus = false;
                NutsVersion v = workspace.version().parser().parse(c.getConfigVersion());
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
                if (changeStatus && !workspace.config().isReadOnly()) {
                    workspace.concurrent().lock().setSource(path).setSession(session).call(() -> {
                                _LOGOP(session).level(Level.CONFIG)
                                        .log(NutsMessage.jstyle("install-info upgraded {0}", finalPath));
                                c.setConfigVersion(workspace.getApiVersion().toString());
                                workspace.elem().setSession(session).setContentType(NutsContentType.JSON).setValue(c).print(finalPath);
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

    public Iterator<InstallInfoConfig> searchInstallConfig(NutsSession session) {
        Path rootFolder = Paths.get(session.locations().getStoreLocation(NutsStoreLocation.CONFIG)).resolve(NutsConstants.Folders.ID);
        return new FolderObjectIterator<InstallInfoConfig>("InstallInfoConfig",
                rootFolder,
                null, -1, session, new FolderObjectIterator.FolderIteratorModel<InstallInfoConfig>() {
            @Override
            public boolean isObjectFile(Path pathname) {
                return pathname.getFileName().toString().equals(NUTS_INSTALL_FILE);
            }

            @Override
            public InstallInfoConfig parseObject(Path path, NutsSession session) throws IOException {
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
            defaultVersion = isDefaultVersion(ii.id, session);
        }
        if (session.getExpireTime() != null && (ii.isInstalled() || ii.isRequired())) {
            if (ii.isInstalled() || ii.isRequired()) {
                Instant lastModifiedDate = ii.getLastModifiedDate();
                if (lastModifiedDate == null) {
                    lastModifiedDate = ii.getCreatedDate();
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
                ii.getCreatedDate(),
                ii.getLastModifiedDate(),
                ii.getInstallUser(),
                ii.getSourceRepoName(),
                ii.getSourceRepoUUID(),
                false, false //will be processed later!
        );
    }

    public NutsInstallInformation updateInstallInformation(NutsDefinition def, Boolean install, Boolean require, boolean deploy, NutsSession session) {
        InstallInfoConfig ii = getInstallInfoConfig(def.getId(), null, session);
        boolean wasInstalled = false;
        boolean wasRequired = false;
        if (deploy) {
            this.deploy()
                    .setId(def.getId())
                    .setContent(def.getPath())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .setSession(session.copy().setConfirm(NutsConfirmationMode.YES))
                    .setDescriptor(def.getDescriptor())
                    .run();
        }
        Instant now = Instant.now();
        if (ii == null) {
//            for (NutsDependency dependency : def.getDependencies()) {
//                Iterator<NutsId> it = searchVersions().setId(dependency.getId()).setSession(NutsWorkspaceHelper.createRepositorySession(session, this, NutsFetchMode.DEPLOYED)).getResult();
//                if (!it.hasNext()) {
//                    throw new IllegalArgumentException("failed to install " + def.getId() + " as dependencies are missing.");
//                }
//            }
            NutsId id = def.getId();
            String user = session.security().setSession(session).getCurrentUsername();
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
                ii.setCreatedDate(now);
                ii.setLastModifiedDate(now);
                ii.setInstallUser(user);
                ii.setInstalled(_install);
                ii.setRequired(_require);
                printJson(id, NUTS_INSTALL_FILE, ii, session);
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
            wasInstalled = ii.isInstalled();
            wasRequired = ii.isRequired();
            if (ii.getCreatedDate() == null) {
                ii.setCreatedDate(now);
            }
            ii.setLastModifiedDate(now);
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
            printJson(ii.getId(), NUTS_INSTALL_FILE, ii, session);
            DefaultNutsInstallInfo uu = (DefaultNutsInstallInfo) getInstallInformation(ii, session);
            uu.setWasInstalled(wasInstalled);
            uu.setWasRequired(wasRequired);
            uu.setJustInstalled(install != null && install);
            uu.setJustRequired(require != null && require);
            return uu;
        }
    }

    public void addString(NutsId id, String name, String value, NutsSession session) {
        try {
            Files.write(getPath(id, name, session), value.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public <T> T readJson(NutsId id, String name, Class<T> clazz, NutsSession session) {
        return session.elem()
                .setSession(session).setContentType(NutsContentType.JSON)
                .parse(getPath(id, name, session), clazz);
    }

    public void printJson(NutsId id, String name, InstallInfoConfig value, NutsSession session) {
        value.setConfigVersion(workspace.getApiVersion().toString());
        session.elem()
                .setSession(session).setContentType(NutsContentType.JSON).setValue(value)
                .print(getPath(id, name, session));
    }

    public void remove(NutsId id, String name, NutsSession session) {
        try {
            Path path = getPath(id, name, session);
            Files.delete(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean contains(NutsId id, String name, NutsSession session) {
        return Files.isRegularFile(getPath(id, name, session));
    }

    public Path getPath(NutsId id, String name, NutsSession session) {
        return Paths.get(session.locations().setSession(session).getStoreLocation(id, NutsStoreLocation.CONFIG)).resolve(name);
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
                NutsDescriptor rep = deployments.deploy(this, NutsConfirmationMode.YES);
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
                throw new NutsIllegalArgumentException(getSession(),
                NutsMessage.cstyle("unsupported push() for %s repository",getName())
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
                Iterator<InstallInfoConfig> installIter = searchInstallConfig(getSession());
                Iterator<NutsId> idIter = IteratorUtils.map(installIter, x -> x.getId(), "NutsInstallInformation->Id");
                NutsIdFilter ff = getFilter();
                if (ff != null) {
                    idIter = IteratorUtils.filter(idIter, new NutsIdFilterToPredicate(ff, getSession()));
                }
                result = idIter; //deployments.searchImpl(getFilter(), getSession())
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
                if (getFilter() instanceof NutsInstallStatusIdFilter) {
                    result = new LazyIterator<NutsId>() {
                        @Override
                        protected Iterator<NutsId> iterator() {
                            File installFolder
                                    = Paths.get(getSession().getWorkspace().locations().getStoreLocation(getId()
                                    .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG)).toFile().getParentFile();
                            if (installFolder.isDirectory()) {
                                final NutsVersionFilter filter0 = getId().getVersion().filter();
                                return IteratorBuilder.of(Arrays.asList(installFolder.listFiles()).iterator())
                                        .map(new Function<File, NutsId>() {
                                            @Override
                                            public NutsId apply(File folder) {
                                                if (folder.isDirectory()
                                                        && new File(folder, NUTS_INSTALL_FILE).isFile()) {
                                                    NutsVersion vv = workspace.version().parser().parse(folder.getName());
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

    public static class InstallInfoConfig extends NutsConfigItem {

        private NutsId id;
        private boolean installed;
        private boolean required;
        private Instant createdDate;
        private Instant lastModifiedDate;
        private String installUser;
        private String sourceRepoName;
        private String sourceRepoUUID;

        public String getSourceRepoName() {
            return sourceRepoName;
        }

        public void setSourceRepoName(String sourceRepoName) {
            this.sourceRepoName = sourceRepoName;
        }

        public String getSourceRepoUUID() {
            return sourceRepoUUID;
        }

        public void setSourceRepoUUID(String sourceRepoUUID) {
            this.sourceRepoUUID = sourceRepoUUID;
        }

        public boolean isInstalled() {
            return installed;
        }

        public void setInstalled(boolean installed) {
            this.installed = installed;
        }

        public boolean isRequired() {
            return required;
        }

        public InstallInfoConfig setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public NutsId getId() {
            return id;
        }

        public void setId(NutsId id) {
            this.id = id;
        }

        public String getInstallUser() {
            return installUser;
        }

        public void setInstallUser(String installUser) {
            this.installUser = installUser;
        }

        public Instant getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(Instant createdDate) {
            this.createdDate = createdDate;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, createdDate, installUser);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InstallInfoConfig that = (InstallInfoConfig) o;
            return Objects.equals(id, that.id)
                    && Objects.equals(createdDate, that.createdDate)
                    && Objects.equals(lastModifiedDate, that.lastModifiedDate)
                    && Objects.equals(installUser, that.installUser);
        }

        @Override
        public String toString() {
            return "InstallInfoConfig{"
                    + "id=" + id
                    + ", installed=" + installed
                    + ", required=" + required
                    + ", installDate=" + createdDate
                    + ", lastModifiedDate=" + lastModifiedDate
                    + ", installUser='" + installUser + '\''
                    + '}';
        }

        public Instant getLastModifiedDate() {
            return lastModifiedDate;
        }

        public InstallInfoConfig setLastModifiedDate(Instant lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
            return this;
        }
    }

    private static class InstalledRepositoryConfigModel implements NutsRepositoryConfigModel {

        private final NutsWorkspace ws;
        private final NutsRepository repo;

        public InstalledRepositoryConfigModel(NutsWorkspace ws, NutsRepository repo) {
            this.ws = ws;
            this.repo = repo;
        }

        @Override
        public boolean save(boolean force, NutsSession session) {
            return false;
        }

        @Override
        public NutsRepository getRepository() {
            return repo;
        }

        @Override
        public NutsWorkspace getWorkspace() {
            return ws;
        }

        @Override
        public void addMirror(NutsRepository repo, NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : addMirror"));
        }

        @Override
        public NutsRepository addMirror(NutsAddRepositoryOptions options, NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : addMirror"));
        }

        @Override
        public NutsRepository findMirror(String repositoryIdOrName, NutsSession session) {
            return null;
        }

        @Override
        public NutsRepository findMirrorById(String repositoryNameOrId, NutsSession session) {
            return null;
        }

        @Override
        public NutsRepository findMirrorByName(String repositoryNameOrId, NutsSession session) {
            return null;
        }

        @Override
        public int getDeployOrder(NutsSession session) {
            return Integer.MAX_VALUE;
        }

        @Override
        public String getGlobalName(NutsSession session) {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public String getGroups(NutsSession session) {
            return null;
        }

        @Override
        public String getLocation(boolean expand, NutsSession session) {
            return null;
        }

        @Override
        public NutsRepository getMirror(String repositoryIdOrName, NutsSession session) {
            return null;
        }

        @Override
        public NutsRepository[] getMirrors(NutsSession session) {
            return new NutsRepository[0];
        }

        @Override
        public String getName() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public NutsRepositoryRef getRepositoryRef(NutsSession session) {
            return null;
        }

        @Override
        public int getSpeed(NutsSession session) {
            return 0;
        }

        @Override
        public String getStoreLocation() {
            return null;
        }

        @Override
        public String getStoreLocation(NutsStoreLocation folderType, NutsSession session) {
            return null;
        }

        //        @Override
//        public int getSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode fetchMode, boolean transitive) {
//            return 0;
//        }
        @Override
        public NutsStoreLocationStrategy getStoreLocationStrategy(NutsSession session) {
            return ws.locations().getRepositoryStoreLocationStrategy();
        }

        //        @Override
//        public Map<String, String> getEnv() {
//            return Collections.emptyMap();
//        }
//
//        @Override
//        public String getEnv(String property, String defaultValue) {
//            return null;
//        }
        @Override
        public String getType(NutsSession session) {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public String getUuid() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public String getLocation() {
            return INSTALLED_REPO_UUID;
        }

        @Override
        public boolean isEnabled(NutsSession session) {
            return false;
        }

        @Override
        public boolean isIndexEnabled(NutsSession session) {
            return false;
        }

        @Override
        public boolean isIndexSubscribed(NutsSession session) {
            return false;
        }

        @Override
        public boolean isSupportedMirroring(NutsSession session) {
            return false;
        }

        //        @Override
//        public void setEnv(String property, String value, NutsUpdateOptions options) {
//            //
//        }
        @Override
        public boolean isTemporary(NutsSession session) {
            return false;
        }

        @Override
        public void removeMirror(String repositoryId, NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : removeMirror"));
        }

        @Override
        public void setEnabled(boolean enabled, NutsSession options) {
        }

        //        @Override
//        public Map<String, String> getEnv(boolean inherit) {
//            return Collections.emptyMap();
//        }
//
//        @Override
//        public String getEnv(String key, String defaultValue, boolean inherit) {
//            return null;
//        }
        @Override
        public void setIndexEnabled(boolean enabled, NutsSession session) {
        }

        @Override
        public void setMirrorEnabled(String repoName, boolean enabled, NutsSession session) {
        }

        @Override
        public void setTemporary(boolean enabled, NutsSession options) {

        }

        @Override
        public void subscribeIndex(NutsSession session) {
        }

        @Override
        public void unsubscribeIndex(NutsSession session) {
        }

        @Override
        public Path getTempMirrorsRoot(NutsSession session) {
            return null;
        }

        @Override
        public Path getMirrorsRoot(NutsSession session) {
            return null;
        }

        @Override
        public NutsUserConfig[] getUsers(NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : getUsers"));
        }

        @Override
        public NutsUserConfig getUser(String userId, NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : getUser"));
        }

        @Override
        public NutsRepositoryConfig getStoredConfig(NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : getStoredConfig"));
        }

        @Override
        public void fireConfigurationChanged(String configName, NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : fireConfigurationChanged"));
        }

        @Override
        public void setUser(NutsUserConfig user, NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : setUser"));
        }

        @Override
        public void removeUser(String userId, NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : removeUser"));
        }

        @Override
        public NutsRepositoryConfig getConfig(NutsSession session) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : getConfig"));
        }

    }
}
