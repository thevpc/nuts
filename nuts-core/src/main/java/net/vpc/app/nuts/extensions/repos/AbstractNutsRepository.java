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
package net.vpc.app.nuts.extensions.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.*;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.app.nuts.extensions.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsSimpleIdFilter;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepository implements NutsRepository {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(AbstractNutsRepository.class.getName());
    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<String, String>();
    private String repositoryId;
    private NutsRepository parentRepository;
    private NutsWorkspace workspace;
    private Map<String, NutsRepository> mirors = new HashMap<>();
    private NutsRepositorySecurityManager securityManager = new DefaultNutsRepositorySecurityManager(this);
    private DefaultNutsRepositoryConfigManager configManager;

    public AbstractNutsRepository(NutsRepositoryConfig config, NutsWorkspace workspace, NutsRepository parentRepository, String root, int speed) {
        init(config, workspace, parentRepository, root, speed);
    }

    protected void init(NutsRepositoryConfig config, NutsWorkspace workspace, NutsRepository parentRepository, String root, int speed) {
        if (config == null) {
            throw new NutsIllegalArgumentException("Null Config");
        }
        checkNutsRepositoryConfig(config);
        if (root == null) {
            throw new IllegalArgumentException("Missing folder");
//            root = CoreIOUtils.resolvePath(location, CoreIOUtils.createFile(workspace.getConfigManager().getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES), workspace.getConfigManager().getNutsHomeLocation());
        } else {
            root = CoreIOUtils.resolvePath(null, new File(root), workspace.getConfigManager().getNutsHomeLocation()).getPath();
        }
        if (root == null || (new File(root).exists() && !new File(root).isDirectory())) {
            throw new NutsInvalidRepositoryException(String.valueOf(root), "Unable to resolve root to a valid folder " + root + "");
        }
        configManager = new DefaultNutsRepositoryConfigManager(this, root, config, Math.max(0, speed));

        this.repositoryId = config.getId();
        this.workspace = workspace;
        this.parentRepository = parentRepository;
    }

    @Override
    public String getRepositoryLocation() {
        return getConfigManager().getLocation();
    }

    public NutsRepository getParentRepository() {
        return parentRepository;
    }

    public NutsRepositoryConfigManager getConfigManager() {
        return configManager;
    }

    public NutsRepositorySecurityManager getSecurityManager() {
        return securityManager;
    }

    @Override
    public void open(boolean autoCreate) {
        File file = new File(getConfigManager().getLocationFolder(), NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME);
        boolean found = false;
        if (file.exists()) {
            NutsRepositoryConfig newConfig = CoreJsonUtils.loadJson(file, NutsRepositoryConfig.class);
            if (newConfig != null) {
                found = true;
                newConfig.setType(getRepositoryType());
                checkNutsRepositoryConfig(newConfig);
                configManager.setConfig(newConfig);
                repositoryId = getConfigManager().getId();
                for (NutsRepositoryLocation repositoryConfig : getConfigManager().getMirrors()) {
                    openRepository(repositoryConfig.getId(), repositoryConfig.getLocation(), repositoryConfig.getType(), new File(getMirorsRoot(), repositoryConfig.getId()).getPath(), true);
                }

            }
        }
        if (!found) {
            if (autoCreate) {
                NutsRepositoryConfig newConfig = new NutsRepositoryConfig(getRepositoryId(), getConfigManager().getLocation(), getRepositoryType());
                checkNutsRepositoryConfig(newConfig);
                configManager.setConfig(newConfig);
            } else {
                throw new NutsRepositoryNotFoundException(getRepositoryId());
            }
        }
    }

    protected NutsRepository openRepository(String repositoryId, String location, String type, String repositoryRoot, boolean autoCreate) {
        if (CoreStringUtils.isEmpty(type)) {
            type = NutsConstants.REPOSITORY_TYPE_NUTS;
        }
        NutsRepositoryFactoryComponent factory_ = getWorkspace().getExtensionManager().createSupported(NutsRepositoryFactoryComponent.class, new NutsRepoInfo(type, location));
        if (factory_ != null) {
            NutsRepository r = factory_.create(repositoryId, location, type, getWorkspace(), this, repositoryRoot);
            if (r != null) {
                r.open(autoCreate);
                wireRepository(r);
                return r;
            }
        }
        throw new NutsInvalidRepositoryException(repositoryId, "Invalid type " + type);
    }

    private String getMirorsRoot() {
        return new File(getConfigManager().getLocationFolder(), NutsConstants.FOLDER_NAME_REPOSITORIES).getPath();
    }

    protected void wireRepository(NutsRepository repository) {
        CoreNutsUtils.validateRepositoryId(repository.getRepositoryId());
        if (mirors.containsKey(repository.getRepositoryId())) {
            throw new NutsRepositoryAlreadyRegisteredException(repository.getRepositoryId());
        }
        mirors.put(repository.getRepositoryId(), repository);
        fireOnAddRepository(repository);
    }

    @Override
    public int getSupportLevel(NutsId id, NutsSession session) {
        checkSession(session);
        int namespaceSupport = getSupportLevelCurrent(id, session);
        if (session.isTransitive()) {
            NutsSession transitiveSession = session.copy().setTransitive(true);
            for (NutsRepository remote : mirors.values()) {
                int r = remote.getSupportLevel(id, transitiveSession);
                if (r > 0 && r > namespaceSupport) {
                    namespaceSupport = r;
                }
            }
        }
        return namespaceSupport;
    }

    protected int getSupportLevelCurrent(NutsId id, NutsSession session) {
        String groups = getConfigManager().getGroups();
        if (CoreStringUtils.isEmpty(groups)) {
            return 1;
        }
        return id.getGroup().matches(CoreStringUtils.simpexpToRegexp(groups)) ? groups.length() : 0;
    }

    @Override
    public String getRepositoryId() {
        return repositoryId;
    }

    @Override
    public String getRepositoryType() {
        return getConfigManager().getType();
    }

    protected void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    @Override
    public boolean save() {
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_SAVE_REPOSITORY)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_SAVE_REPOSITORY);
        }
        boolean b = getConfigManager().save();
        for (NutsRepository repository : mirors.values()) {
            b |= repository.save();
        }
        return b;
    }

    @Override
    public void removeMirror(String repositoryId) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException();
        }
//        NutsRepository repo = getMirror(repositoryId);
//        if (repo == null) {
//            throw new NutsRepositoryNotFoundException(repositoryId);
//        }
//        mirors.remove(repo);

        boolean updated = false;
        NutsRepository repo = null;
        try {
            repo = getMirror(repositoryId);
        } catch (NutsRepositoryNotFoundException ex) {
            //ignore
        }
        if (repo != null) {
            updated = true;
        }
        if (getConfigManager().getMirror(repositoryId) != null) {
            updated = true;
        }
        if (!updated) {
            throw new NutsRepositoryNotFoundException(repositoryId);
        }
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " remove repo " + repositoryId);
        }
        getConfigManager().removeMirror(repositoryId);
        if (repo != null) {
            mirors.remove(repositoryId);
            fireOnRemoveRepository(repo);
        }
    }

    public NutsRepository getMirror(String repositoryIdPath) {
        while (repositoryIdPath.startsWith("/")) {
            repositoryIdPath = repositoryIdPath.substring(1);
        }
        while (repositoryIdPath.endsWith("/")) {
            repositoryIdPath = repositoryIdPath.substring(0, repositoryIdPath.length() - 1);
        }

        if (repositoryIdPath.contains("/")) {
            int s = repositoryIdPath.indexOf("/");
            String child = repositoryIdPath.substring(0, s);
            NutsRepository r = mirors.get(child);
            if (r != null) {
                return r.getMirror(repositoryIdPath.substring(s + 1));
            }
            throw new NutsRepositoryNotFoundException(repositoryIdPath);
        } else {
            NutsRepository r = mirors.get(repositoryIdPath);
            if (r != null) {
                return r;
            }
            throw new NutsRepositoryNotFoundException(repositoryIdPath);
        }
    }

    @Override
    public NutsRepository[] getMirrors() {
        return mirors.values().toArray(new NutsRepository[mirors.size()]);
    }

    @Override
    public NutsRepository addMirror(String repositoryId, String location, String type, boolean autoCreate) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException();
        }
        if (CoreStringUtils.isEmpty(type)) {
            type = NutsConstants.REPOSITORY_TYPE_NUTS;
        }
        boolean supported = false;
        try {
            supported = getWorkspace().getRepositoryManager().isSupportedRepositoryType(type);
        } catch (Exception e) {
            //
        }
        if (!supported) {
            throw new NutsInvalidRepositoryException(repositoryId, "Invalid type " + type);
        }

        NutsRepositoryLocation newConf = new NutsRepositoryLocation(repositoryId, location, type);

        NutsRepositoryLocation repoConf = getConfigManager().getMirror(repositoryId);
        if (repoConf != null) {
            throw new NutsRepositoryAlreadyRegisteredException(repositoryId);
        }
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " add repo " + repositoryId);
        }
        getConfigManager().addMirror(newConf);

        NutsRepository repo = openRepository(repositoryId, location, type, getMirorsRoot(), autoCreate);
        return repo;
    }

    @Override
    public String toString() {
        return "id=" + getRepositoryId()
                + " ; impl=" + getClass().getSimpleName() + " ; folder=" + getConfigManager().getLocation() + (CoreStringUtils.isEmpty(getConfigManager().getLocation()) ? "" : (" ; location=" + getConfigManager().getLocation()));
    }

    public void checkAllowedFetch(NutsId id, NutsSession session) {
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
        return repositoryListeners.toArray(new NutsRepositoryListener[repositoryListeners.size()]);
    }

    @Override
    public NutsDescriptor fetchDescriptor(NutsId id, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        checkAllowedFetch(id.setFace("nuts"), session);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(session.getFetchMode().toString(), 7) + " " + CoreStringUtils.alignLeft(getRepositoryId(), 20) + " " + CoreStringUtils.alignLeft("Fetch descriptor", 24) + " " + id);
        }

        String versionString = id.getVersion().getValue();
        if (CoreVersionUtils.isStaticVersionPattern(versionString) || CoreStringUtils.isEmpty(versionString)) {
            if (CoreStringUtils.isEmpty(versionString) || "LATEST".equals(versionString)) {
                NutsId a = findLatestVersion(id.setVersion(null), null, session);
                if (a == null) {
                    throw new NutsNotFoundException(id.toString());
                }
                NutsDescriptor d = fetchDescriptorImpl(a, session);
                if (d == null) {
                    throw new NutsNotFoundException(id);
                }
                return d;
            }
            NutsDescriptor d = fetchDescriptorImpl(id, session);
            if (d == null) {
                throw new NutsNotFoundException(id);
            }
            return d;
        } else {
            DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(id.getQueryMap(), new NutsSimpleIdFilter(id), CoreVersionUtils.createNutsVersionFilter(versionString), null, this, session);
            NutsId a = findLatestVersion(id, filter, session);
            if (a == null) {
                throw new NutsNotFoundException(id.toString());
            }
            NutsDescriptor d = fetchDescriptorImpl(a, session);
            if (d == null) {
                throw new NutsNotFoundException(id);
            }
            return d;
        }

    }

    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsSession session) {
        Iterator<NutsId> allVersions = findVersions(id, filter, session);
        NutsId a = null;
        while (allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    @Override
    public String fetchHash(NutsId id, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        checkAllowedFetch(id.setFace("hash"), session);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " " + CoreStringUtils.alignLeft("Fetch component hash", 24) + " " + id);
        }
        String d = fetchHashImpl(id, session);
        if (d == null) {
            throw new NutsNotFoundException(id);
        }
        return d;
    }

    @Override
    public String fetchDescriptorHash(NutsId id, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        checkAllowedFetch(id.setFace("nutshash"), session);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " " + CoreStringUtils.alignLeft("Fetch desc hash", 24) + " " + id);
        }
        String d = fetchDescriptorHashImpl(id, session);
        if (d == null) {
            throw new NutsNotFoundException(id);
        }
        return d;
    }

    @Override
    public NutsId deploy(NutsId id, NutsDescriptor descriptor, String file, boolean force, NutsSession session) {
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_DEPLOY)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_DEPLOY);
        }
        if (CoreStringUtils.isEmpty(id.getGroup())) {
            throw new NutsIllegalArgumentException("Empty group");
        }
        if (CoreStringUtils.isEmpty(id.getName())) {
            throw new NutsIllegalArgumentException("Empty name");
        }
        if ((id.getVersion().isEmpty())) {
            throw new NutsIllegalArgumentException("Empty version");
        }
        if ("RELEASE".equals(id.getVersion().getValue())
                || "LATEST".equals(id.getVersion().getValue())
                || "RELEASE".equals(id.getVersion().getValue())) {
            throw new NutsIllegalArgumentException("Invalid version " + id.getVersion());
        }
//        if (descriptor.getArch().length > 0 || descriptor.getOs().length > 0 || descriptor.getOsdist().length > 0 || descriptor.getPlatform().length > 0) {
//            if (CoreStringUtils.isEmpty(descriptor.getFace())) {
//                throw new NutsIllegalArgumentException("face property '" + NutsConstants.QUERY_FACE + "' could not be null if env {arch,os,osdist,platform} is specified");
//            }
//        }
        id = id.unsetQuery();
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Deploy " + id);
        }
        id = id.setFace(descriptor.getFace());
        return deployImpl(id, descriptor, file, force, session);
    }

    @Override
    public void push(NutsId id, String repoId, boolean force, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_PUSH)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_PUSH);
        }
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Push " + id);
        }
        pushImpl(id, repoId, force, session);
    }

    public Iterator<NutsId> find(final NutsIdFilter filter, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        checkAllowedFetch(null, session);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Find components");
        }
        return findImpl(filter, session);
    }

    //    @Override
//    public NutsId resolveId(NutsId id, NutsSession session) {
//        checkSession(session);
//        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
//            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
//        }
//        checkAllowedFetch(session, id.setFace("content"));
//        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Resolve " + id);
//
//        String versionString = id.getVersion().getValue();
//        if (CoreVersionUtils.isStaticVersionPattern(versionString)) {
//            NutsId id2 = resolveIdImpl(id, session);
//            if (id2 == null) {
//                throw new NutsNotFoundException(id);
//            }
//            return id2;
//        } else {
//            DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(id.getQueryMap(), null, CoreVersionUtils.createNutsVersionFilter(versionString), null, this, session);
//            Iterator<NutsId> allVersions = findVersions(id, filter, session);
//
//            NutsId a = null;
//            while (allVersions.hasNext()) {
//                NutsId next = allVersions.next();
//                if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
//                    a = next;
//                }
//            }
//            if (a == null) {
//                throw new NutsNotFoundException(id.toString());
//            }
//            return a;
//        }
//    }
    @Override
    public NutsFile fetch(NutsId id, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_CONTENT)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_CONTENT);
        }
        checkAllowedFetch(id.setFace("content"), session);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " " + CoreStringUtils.alignLeft("Fetch component", 24) + " " + id);
        }
        NutsFile f = fetchImpl(id, session);
        if (f == null) {
            throw new NutsNotFoundException(id);
        }
        return f;
    }

    public String copyTo(NutsId id, String localPath, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_CONTENT)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_CONTENT);
        }
        checkAllowedFetch(id.setFace("content"), session);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " " + CoreStringUtils.alignLeft("Fetch component (local)", 24) + " " + id);
        }
        return copyToImpl(id, localPath, session);
    }

    @Override
    public String copyDescriptorTo(NutsId id, String localPath, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        checkAllowedFetch(id.setFace("descriptor"), session);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " " + CoreStringUtils.alignLeft("Fetch desc (local)", 24) + " " + id);
        }
        if (new File(localPath).isDirectory()) {
            localPath = new File(localPath, CoreNutsUtils.getNutsFileName(id, "pom")).getPath();
        }
        return copyDescriptorToImpl(id, localPath, session);
    }

    public Iterator<NutsId> findVersions(NutsId id, NutsIdFilter idFilter, NutsSession session) {
        checkSession(session);
        checkNutsId(id, NutsConstants.RIGHT_FETCH_DESC);
        checkAllowedFetch(id.setFace("content"), session);
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(session.getFetchMode().toString(), 7) + " " + CoreStringUtils.alignLeft(getRepositoryId(), 20) + " " + CoreStringUtils.alignLeft("Fetch versions for", 24) + " " + id);
        }
        return findVersionsImpl(id, idFilter, session);
    }

    public void undeploy(NutsId id, NutsSession session) {
        checkSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_UNDEPLOY)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_UNDEPLOY);
        }
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Undeploy " + id);
        }
        undeployImpl(id, session);
    }

    protected String getQueryFilename(NutsId id, NutsDescriptor descriptor) {
        String name = id.getName() + "-" + id.getVersion().getValue();
        Map<String, String> query = id.getQueryMap();
        String ext = "";
        String file = query.get(NutsConstants.QUERY_FILE);
        if (file == null) {
            if (!CoreStringUtils.isEmpty(descriptor.getExt())) {
                ext = "." + descriptor.getExt();
            }
        } else {
            ext = extensions.get(file);
        }
        return name + ext;
    }

    protected abstract void undeployImpl(NutsId id, NutsSession session);

    protected abstract Iterator<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsSession session);

    public abstract String copyDescriptorToImpl(NutsId id, String localPath, NutsSession session);

    protected abstract String copyToImpl(NutsId id, String localPath, NutsSession session);

    protected abstract NutsFile fetchImpl(NutsId id, NutsSession session);

    //    protected abstract NutsId resolveIdImpl(NutsId id, NutsSession session);
    protected abstract Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session);

    protected abstract void pushImpl(NutsId id, String repoId, boolean force, NutsSession session);

    protected abstract NutsId deployImpl(NutsId id, NutsDescriptor descriptor, String file, boolean force, NutsSession session);

    protected abstract String fetchDescriptorHashImpl(NutsId id, NutsSession session);

    protected abstract String fetchHashImpl(NutsId id, NutsSession session);

    protected abstract NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session);

    protected void checkSession(NutsSession session) {
        if (session == null) {
            throw new NutsIllegalArgumentException("Missing Session");
        }
    }

    protected void checkNutsId(NutsId id, String right) {
        if (id == null) {
            throw new NutsIllegalArgumentException("Missing id");
        }
        if (!getSecurityManager().isAllowed(right)) {
            throw new NutsSecurityException("Not Allowed " + right);
        }
        if (CoreStringUtils.isEmpty(id.getGroup())) {
            throw new NutsIllegalArgumentException("Missing group for " + id);
        }
        if (CoreStringUtils.isEmpty(id.getName())) {
            throw new NutsIllegalArgumentException("Missing name for " + id);
        }
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setEnabled(boolean enabled) {
        NutsRepository pr = getParentRepository();
        if (pr != null) {
            pr.getConfigManager().getMirror(getRepositoryId()).setEnabled(enabled);
        } else {
            getWorkspace().getConfigManager().setRepositoryEnabled(getRepositoryId(), enabled);
        }
    }

    @Override
    public boolean isEnabled() {
        NutsRepository pr = getParentRepository();
        if (pr != null) {
            return pr.getConfigManager().getMirror(getRepositoryId()).isEnabled();
        } else {
            return getWorkspace().getConfigManager().isRepositoryEnabled(getRepositoryId());
        }
    }

    private void checkNutsRepositoryConfig(NutsRepositoryConfig config) {
        if (CoreStringUtils.isEmpty(config.getType())) {
            throw new NutsIllegalArgumentException("Empty Repository Type");
        }
        if (CoreStringUtils.isEmpty(config.getId())) {
            throw new NutsIllegalArgumentException("Empty Repository Id");
        }
//        if (CoreStringUtils.isEmpty(config.getLocation())) {
//            throw new NutsIllegalArgumentException("Empty Repository Id");
//        }
    }

    protected void fireOnUndeploy(NutsFile file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onUndeploy(this, file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onUndeploy(this, file);
        }
    }

    protected void fireOnDeploy(NutsFile file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onDeploy(this, file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onDeploy(this, file);
        }
    }

    protected void fireOnInstall(NutsFile file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onInstall(this, file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onInstall(this, file);
        }
    }

    protected void fireOnPush(NutsFile file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onPush(this, file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onPush(this, file);
        }
    }

    protected void fireOnAddRepository(NutsRepository repository) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onAddRepository(getWorkspace(), this, repository);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onAddRepository(getWorkspace(), this, repository);
        }
    }

    protected void fireOnRemoveRepository(NutsRepository repository) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onRemoveRepository(getWorkspace(), this, repository);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onRemoveRepository(getWorkspace(), this, repository);
        }
    }


    @Override
    public int getSpeed() {
        int s = getConfigManager().getSpeed();
        if (isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors()) {
                s += mirror.getSpeed();
            }
        }
        return s;
    }

}
