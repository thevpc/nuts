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
import net.vpc.app.nuts.extensions.core.NutsRepositoryConfigImpl;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.app.nuts.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepository implements NutsRepository {

    private static final Logger log = Logger.getLogger(AbstractNutsRepository.class.getName());
    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<String, String>();
    private NutsRepositoryConfig config;
    private String repositoryId;
    private NutsWorkspace workspace;
    private Map<String, NutsRepository> mirors = new HashMap<>();
    private File root;
    private int speed;

    public AbstractNutsRepository(NutsRepositoryConfig config, NutsWorkspace workspace, File root, int slowness) {
        this.speed = Math.max(0, slowness);
        if (config == null) {
            throw new IllegalArgumentException("Null Config");
        }
        checkNutsRepositoryConfig(config);
        if (root == null) {
            root = CoreIOUtils.resolvePath(config.getId(), CoreIOUtils.createFile(workspace.getWorkspaceLocation(), NutsConstants.DEFAULT_REPOSITORIES_ROOT), workspace.getWorkspaceRootLocation());
        } else {
            root = CoreIOUtils.resolvePath(root.getPath(), null, workspace.getWorkspaceRootLocation());
        }
        if (root == null || (root.exists() && !root.isDirectory())) {
            throw new NutsRepositoryInvalidException(String.valueOf(root));
        }
        this.root = root;
        this.config = config;
        this.repositoryId = config.getId();
        this.workspace = workspace;
    }

    @Override
    public String getEnv(String key, String defaultValue, boolean inherit) {
        String t = getConfig().getEnv(key, null);
        if (!CoreStringUtils.isEmpty(t)) {
            return t;
        }
        t = getWorkspace().getConfig().getEnv(key, null);
        if (!CoreStringUtils.isEmpty(t)) {
            return t;
        }
        return defaultValue;
    }

    @Override
    public int getSpeed() {
        int s = speed;
        if (isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors()) {
                s += mirror.getSpeed();
            }
        }
        return s;
    }

    @Override
    public NutsRepositoryConfig getConfig() {
        return config;
    }

    private void checkNutsRepositoryConfig(NutsRepositoryConfig config) {
        if (CoreStringUtils.isEmpty(config.getType())) {
            throw new IllegalArgumentException("Empty Repository Type");
        }
        if (CoreStringUtils.isEmpty(config.getId())) {
            throw new IllegalArgumentException("Empty Repository Id");
        }
//        if (CoreStringUtils.isEmpty(config.getLocation())) {
//            throw new IllegalArgumentException("Empty Repository Id");
//        }
    }

    public File getRoot() {
        return root;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public void open(boolean autoCreate) throws IOException {
        File file = new File(getRoot(), NutsConstants.NUTS_REPOSITORY_FILE);
        boolean found = false;
        if (file.exists()) {
            NutsRepositoryConfig newConfig = CoreJsonUtils.get(getWorkspace()).loadJson(file, NutsRepositoryConfigImpl.class);
            if (newConfig != null) {
                found = true;
                newConfig.setType(config.getType());
                checkNutsRepositoryConfig(newConfig);
                config = newConfig;
                repositoryId = config.getId();
                for (NutsRepositoryConfig repositoryConfig : config.getMirrors()) {
                    wireRepository(getWorkspace().openRepository(repositoryConfig.getId(), new File(getMirorsRoot(), repositoryConfig.getId()), repositoryConfig.getLocation(), repositoryConfig.getType(), true));
                }

            }
        }
        if (!found) {
            if (autoCreate) {
                NutsRepositoryConfig newConfig = new NutsRepositoryConfigImpl(getRepositoryId(), config.getLocation(), config.getType());
                checkNutsRepositoryConfig(newConfig);
                config = newConfig;
            } else {
                throw new NutsRepositoryInvalidException(getRepositoryId());
            }
        }
    }

    private File getMirorsRoot() {
        return new File(getRoot(), NutsConstants.DEFAULT_REPOSITORIES_ROOT);
    }

    protected void wireRepository(NutsRepository repository) {
        CoreNutsUtils.validateRepositoryId(repository.getRepositoryId());
        if (mirors.containsKey(repository.getRepositoryId())) {
            throw new NutsRepositoryAlreadyRegisteredException(repository.getRepositoryId());
        }
        mirors.put(repository.getRepositoryId(), repository);
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onAddRepository(getWorkspace(), this, repository);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onAddRepository(getWorkspace(), this, repository);
        }
    }

    @Override
    public int getSupportLevel(NutsId id, NutsSession session) {
        checkSession(session);
        int namespaceSupport = getSupportLevel(id);
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

    protected int getSupportLevel(NutsId id) {
        String groups = config.getGroups();
        if (CoreStringUtils.isEmpty(groups)) {
            return 1;
        }
        return id.getGroup().matches(CoreStringUtils.simpexpToRegexp(groups, false)) ? groups.length() : 0;
    }

    @Override
    public String getRepositoryId() {
        return repositoryId;
    }

    protected void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    @Override
    public void save() throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_SAVE_REPOSITORY)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_SAVE_REPOSITORY);
        }
        File file = CoreIOUtils.createFile(getRoot(), NutsConstants.NUTS_REPOSITORY_FILE);
        boolean created = false;
        if (!file.exists()) {
            created = true;
        }
        getRoot().mkdirs();
        CoreJsonUtils.get(getWorkspace()).storeJson(config, file, CoreJsonUtils.PRETTY_IGNORE_EMPTY_OPTIONS);
        if (created) {
            log.log(Level.INFO, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Created repository " + getRepositoryId() + " at " + getRoot().getPath());
        } else {
            log.log(Level.FINE, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Updated repository " + getRepositoryId() + " at " + getRoot().getPath());
        }
        for (NutsRepository repository : mirors.values()) {
            repository.save();
        }
    }

    @Override
    public void removeMirror(String repositoryId) {
        if (!isSupportedMirroring()) {
            throw new UnsupportedOperationException();
        }
//        NutsRepository repo = getMirror(repositoryId);
//        if (repo == null) {
//            throw new RepositoryNotFoundException(repositoryId);
//        }
//        mirors.remove(repo);

        boolean updated = false;
        NutsRepository repo = null;
        try {
            repo = getMirror(repositoryId);
        } catch (RepositoryNotFoundException ex) {
            //ignore
        }
        if (repo != null) {
            updated = true;
        }
        if (config.getMirror(repositoryId) != null) {
            updated = true;
        }
        if (!updated) {
            throw new RepositoryNotFoundException(repositoryId);
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " remove repo " + repositoryId);
        config.removeMirror(repositoryId);
        if (repo != null) {
            mirors.remove(repositoryId);
            for (NutsRepositoryListener listener : getRepositoryListeners()) {
                listener.onRemoveRepository(getWorkspace(), this, repo);
            }
            for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
                listener.onRemoveRepository(getWorkspace(), this, repo);
            }
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
            throw new RepositoryNotFoundException(repositoryIdPath);
        } else {
            NutsRepository r = mirors.get(repositoryIdPath);
            if (r != null) {
                return r;
            }
            throw new RepositoryNotFoundException(repositoryIdPath);
        }
    }

    @Override
    public NutsRepository[] getMirrors() {
        return mirors.values().toArray(new NutsRepository[mirors.size()]);
    }

    @Override
    public NutsRepository addMirror(String repositoryId, String location, String type, boolean autoCreate) throws IOException {
        if (!isSupportedMirroring()) {
            throw new UnsupportedOperationException();
        }
        if (CoreStringUtils.isEmpty(type)) {
            type = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }
        boolean supported = false;
        try {
            supported = getWorkspace().isSupportedRepositoryType(type);
        } catch (Exception e) {
            //
        }
        if (!supported) {
            throw new NutsRepositoryUnsupportedException(type);
        }

        NutsRepositoryConfig newConf = new NutsRepositoryConfigImpl(repositoryId, location, type);

        NutsRepositoryConfig repoConf = config.getMirror(repositoryId);
        if (repoConf != null) {
            throw new NutsRepositoryAlreadyRegisteredException(repositoryId);
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " add repo " + repositoryId);
        config.addMirror(newConf);

        NutsEnvironmentContext.REPOSITORY.set(this);
        NutsRepository repo = getWorkspace().openRepository(repositoryId, new File(getMirorsRoot(), repositoryId), location, type, autoCreate);
        wireRepository(repo);
        return repo;
    }

    @Override
    public String toString() {
        return "id=" + getRepositoryId() + " ; impl=" + getClass().getSimpleName() + " ; folder=" + getRoot() + (CoreStringUtils.isEmpty(config.getLocation()) ? "" : (" ; location=" + config.getLocation()));
    }

    public boolean isAllowedFetch(NutsSession session) {
        return true;
    }

    @Override
    public boolean isAllowed(String right) {
        String name = getWorkspace().getCurrentLogin();
        if (NutsConstants.USER_ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        NutsRepositoryConfig c = getConfig();
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsSecurityEntityConfig s = c.getSecurity(n);
            if (s != null) {
                if (s.containsRight("!" + right)) {
                    return false;
                }
                if (s.containsRight(right)) {
                    return true;
                }
                for (String g : s.getGroups()) {
                    if (!visitedGroups.contains(g)) {
                        visitedGroups.add(g);
                        items.push(g);
                    }
                }
            }
        }
        return getWorkspace().isAllowed(right);
    }

    public void setUserCredentials(String username, String credentials) throws IOException {
        if (CoreStringUtils.isEmpty(credentials)) {
            credentials = null;
        } else {
            credentials = CoreSecurityUtils.httpEncrypt(credentials.getBytes(), CoreNutsUtils.DEFAULT_PASSPHRASE);
        }

        NutsSecurityEntityConfig security = getConfig().getSecurity(username);
        if (security == null) {
            throw new IllegalArgumentException("User not found " + username);
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Update user credentials " + username);
        security.setCredentials(credentials);
    }

    @Override
    public void setUserCredentials(String username, String password, String oldPassword) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_SET_PASSWORD)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_SET_PASSWORD);
        }
        if (CoreStringUtils.isEmpty(username)) {
            username = getWorkspace().getCurrentLogin();
        }
        NutsSecurityEntityConfig u = getConfig().getSecurity(username);
        if (u == null) {
            throw new IllegalArgumentException("No such user " + username);
        }

        if (!getWorkspace().getCurrentLogin().equals(username)) {
            if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
                throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_ADMIN);
            }
        }
        if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
            if (CoreStringUtils.isEmpty(password)) {
                throw new SecurityException("Missing old password");
            }
            //check old password
            if (CoreStringUtils.isEmpty(u.getCredentials())
                    || u.getCredentials().equals(CoreSecurityUtils.evalSHA1(password))) {
                throw new SecurityException("Invalid password");
            }
        }
        if (CoreStringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Missing password");
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Update user credentials " + username);
        getConfig().setSecurity(u);
        setUserCredentials(u.getUser(), password);
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
    public NutsDescriptor fetchDescriptor(NutsId id, NutsSession session) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        if (!isAllowedFetch(session)) {
            throw new NutsNotFoundException(id.toString());
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Fetch desc " + id);
        NutsDescriptor d = fetchDescriptorImpl(id, session);
        if (d == null) {
            throw new NutsNotFoundException(id);
        }
        return d;
    }

    @Override
    public String fetchHash(NutsId id, NutsSession session) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_FETCH_CONTENT)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_CONTENT);
        }
        if (!isAllowedFetch(session)) {
            throw new NutsNotFoundException(id.toString());
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Fetch component hash " + id);
        String d = fetchHashImpl(id, session);
        if (d == null) {
            throw new NutsNotFoundException(id);
        }
        return d;
    }

    @Override
    public String fetchDescriptorHash(NutsId id, NutsSession session) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        if (!isAllowedFetch(session)) {
            throw new NutsNotFoundException(id.toString());
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Fetch desc hash " + id);
        String d = fetchDescriptorHashImpl(id, session);
        if (d == null) {
            throw new NutsNotFoundException(id);
        }
        return d;
    }


    public NutsId deploy(NutsId id, NutsDescriptor descriptor, File file, NutsSession session) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_DEPLOY)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_DEPLOY);
        }
        if (CoreStringUtils.isEmpty(id.getGroup())) {
            throw new IllegalArgumentException("Empty group");
        }
        if (CoreStringUtils.isEmpty(id.getName())) {
            throw new IllegalArgumentException("Empty name");
        }
        if ((id.getVersion().isEmpty())) {
            throw new IllegalArgumentException("Empty version");
        }
        if ("RELEASE".equals(id.getVersion().getValue())
                || "LATEST".equals(id.getVersion().getValue())
                || "RELEASE".equals(id.getVersion().getValue())) {
            throw new IllegalArgumentException("Invalid version " + id.getVersion());
        }
        if (descriptor.getArch().length > 0 || descriptor.getOs().length > 0 || descriptor.getOsdist().length > 0 || descriptor.getPlatform().length > 0) {
            if (CoreStringUtils.isEmpty(descriptor.getFace())) {
                throw new IllegalArgumentException("face property '" + NutsConstants.QUERY_FACE + "' could not be null if env {arch,os,osdist,platform} is specified");
            }
        }
        id = id.unsetQuery();
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Deploy " + id);
        id = id.setFace(descriptor.getFace());
        return deployImpl(id, descriptor, file, session);
    }

    public void push(NutsId id, String repoId, NutsSession session) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_PUSH)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_PUSH);
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Push " + id);
        pushImpl(id, repoId, session);
    }

    public Iterator<NutsId> find(final NutsDescriptorFilter filter, NutsSession session) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        if (!isAllowedFetch(session)) {
            return Collections.emptyIterator();
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Find components");
        return findImpl(filter, session);
    }

    @Override
    public NutsId resolveId(NutsId id, NutsSession session) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        if (!isAllowedFetch(session)) {
            throw new NutsNotFoundException(id.toString());
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Resolve " + id);
        NutsId id2 = resolveIdImpl(id, session);
        if (id2 == null) {
            throw new NutsNotFoundException(id);
        }
        return id2;
    }

    public NutsFile fetch(NutsId id, NutsSession session) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_FETCH_CONTENT)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_CONTENT);
        }
        if (!isAllowedFetch(session)) {
            throw new NutsNotFoundException(id.toString());
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Fetch component " + id);
        NutsFile f = fetchImpl(id, session);
        if (f == null) {
            throw new NutsNotFoundException(id);
        }
        return f;
    }

    public File fetch(NutsId id, NutsSession session, File localPath) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_FETCH_CONTENT)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_CONTENT);
        }
        if (!isAllowedFetch(session)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_CONTENT);
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Fetch component (to local) " + id);
        return fetchImpl(id, session, localPath);
    }

    @Override
    public boolean fetchDescriptor(NutsId id, NutsSession session, File localPath) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        if (!isAllowedFetch(session)) {
            throw new NutsNotFoundException(id.toString());
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Fetch desc (to local) " + id);
        return fetchDescriptorImpl(id, session, localPath);
    }

    public Iterator<NutsId> findVersions(NutsId id, NutsVersionFilter versionFilter, NutsSession session) throws IOException {
        checkSession(session);
        checkNutsId(id, NutsConstants.RIGHT_FETCH_DESC);
        if (!isAllowedFetch(session)) {
            return Collections.emptyIterator();
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Fetch versions for " + id);
        return findVersionsImpl(id, versionFilter, session);
    }

    public Iterator<NutsId> findVersions(NutsId id, NutsDescriptorFilter versionFilter, NutsSession session) throws IOException {
        checkSession(session);
        checkNutsId(id, NutsConstants.RIGHT_FETCH_DESC);
        if (!isAllowedFetch(session)) {
            return Collections.emptyIterator();
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Fetch versions for " + id);
        return findVersionsImpl(id, versionFilter, session);
    }

    public void undeploy(NutsId id, NutsSession session) throws IOException {
        checkSession(session);
        if (!isAllowed(NutsConstants.RIGHT_UNDEPLOY)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_UNDEPLOY);
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " Undeploy " + id);
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


    protected abstract void undeployImpl(NutsId id, NutsSession session) throws IOException;

    protected Iterator<NutsId> findVersionsImpl(NutsId id, NutsVersionFilter versionFilter, NutsSession session) throws IOException {
        return findVersionsImpl(id, new VersionAndEnvNutsDescriptorFilter(versionFilter, id.getQueryMap()), session);
    }

    protected abstract Iterator<NutsId> findVersionsImpl(NutsId id, NutsDescriptorFilter versionFilter, NutsSession session) throws IOException;

    public abstract boolean fetchDescriptorImpl(NutsId id, NutsSession session, File localPath) throws IOException;

    protected abstract File fetchImpl(NutsId id, NutsSession session, File localPath) throws IOException;

    protected abstract NutsFile fetchImpl(NutsId id, NutsSession session) throws IOException;

    protected abstract NutsId resolveIdImpl(NutsId id, NutsSession session) throws IOException;

    protected abstract Iterator<NutsId> findImpl(final NutsDescriptorFilter filter, NutsSession session) throws IOException;

    protected abstract void pushImpl(NutsId id, String repoId, NutsSession session) throws IOException;

    protected abstract NutsId deployImpl(NutsId id, NutsDescriptor descriptor, File file, NutsSession session) throws IOException;

    protected abstract String fetchDescriptorHashImpl(NutsId id, NutsSession session) throws IOException;

    protected abstract String fetchHashImpl(NutsId id, NutsSession session) throws IOException;

    protected abstract NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) throws IOException;


    protected void checkSession(NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
    }

    protected void checkNutsId(NutsId id, String right) {
        if (id == null) {
            throw new IllegalArgumentException("Missing id");
        }
        if (!isAllowed(right)) {
            throw new SecurityException("Not Allowed " + right);
        }
        if (CoreStringUtils.isEmpty(id.getGroup())) {
            throw new IllegalArgumentException("Missing group");
        }
        if (CoreStringUtils.isEmpty(id.getName())) {
            throw new IllegalArgumentException("Missing name");
        }
    }
}
