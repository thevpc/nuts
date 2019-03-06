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
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.core.filters.id.NutsSimpleIdFilter;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepository implements NutsRepository {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(AbstractNutsRepository.class.getName());
    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<String, String>();
    private String repositoryName;
    private NutsRepository parentRepository;
    private NutsWorkspace workspace;
    private Map<String, NutsRepository> mirrors = new HashMap<>();
    private NutsRepositorySecurityManager securityManager = new DefaultNutsRepositorySecurityManager(this);
    private DefaultNutsRepositoryConfigManager configManager;
    private boolean transientRepository;
    private NutsIndexStoreClient nutsIndexStoreClient;

    public AbstractNutsRepository(NutsRepositoryConfig config, NutsWorkspace workspace, NutsRepository parentRepository, String repositoryRoot, int speed) {
        init(config, workspace, parentRepository, repositoryRoot, speed);
    }

    protected void init(NutsRepositoryConfig config, NutsWorkspace workspace, NutsRepository parentRepository, String repositoryRoot, int speed) {
        if (config == null) {
            throw new NutsIllegalArgumentException("Null Config");
        }
        checkNutsRepositoryConfig(config);
        if (StringUtils.isEmpty(repositoryRoot)) {
            throw new IllegalArgumentException("Missing folder");
        }
        if ((new File(repositoryRoot).exists() && !new File(repositoryRoot).isDirectory())) {
            throw new NutsInvalidRepositoryException(repositoryRoot, "Unable to resolve root as a valid folder " + repositoryRoot);
        }
        configManager = new DefaultNutsRepositoryConfigManager(this, repositoryRoot, config, Math.max(0, speed));

        this.repositoryName = config.getName();
        this.workspace = workspace;
        this.parentRepository = parentRepository;
        this.nutsIndexStoreClient = workspace.getRepositoryManager().getIndexStoreClientFactory().createNutsIndexStoreClient(this);
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
        File file = new File(getConfigManager().getStoreLocation(), NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME);
        boolean found = false;
        if (file.exists()) {
            NutsRepositoryConfig newConfig = null;
            try {
                newConfig = workspace.getIOManager().readJson(file, NutsRepositoryConfig.class);
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Erroneous config file. Unable to load file " + file + " : " + ex.toString());
                if (!getWorkspace().getConfigManager().isReadOnly()) {
                    File newfile = CoreIOUtils.createFile(getConfigManager().getStoreLocation(), "nuts-repository-" +
                            new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())
                            + ".json");
                    log.log(Level.SEVERE, "Erroneous config file will replace by fresh one. Old config is copied to " + newfile.getPath());
                    try {
                        CoreIOUtils.move(file, newfile);
                        autoCreate = true;
                    } catch (IOException e) {
                        throw new NutsIOException("Unable to load and re-create config file " + file.getPath() + " : " + e.toString(), ex);
                    }
                } else {
                    throw new NutsIOException("Unable to load config file " + file.getPath(), ex);
                }
            }
            if (newConfig != null) {
                found = true;
                if (StringUtils.isEmpty(newConfig.getUuid())) {
                    newConfig.setUuid(UUID.randomUUID().toString());
                    if (!workspace.getConfigManager().isReadOnly()) {
                        //save updates without processing mirrors
                        workspace.getIOManager().writeJson(newConfig, file, true);
                    }
                }
                newConfig.setType(getRepositoryType());
                checkNutsRepositoryConfig(newConfig);
                configManager.setConfig(newConfig);
                repositoryName = getConfigManager().getName();
                for (NutsRepositoryLocation repositoryConfig : getConfigManager().getMirrors()) {
                    openRepository(repositoryConfig, new File(getMirrorsRoot(), repositoryConfig.getName()).getPath(), true);
                }

            }
        }
        if (!found) {
            if (autoCreate) {
                NutsRepositoryConfig newConfig = new NutsRepositoryConfig(getName(), getConfigManager().getLocation(true), getRepositoryType());
                newConfig.setUuid(UUID.randomUUID().toString());
                newConfig.setStoreLocationStrategy(getWorkspace().getConfigManager().getRepositoryStoreLocationStrategy());
                checkNutsRepositoryConfig(newConfig);
                configManager.setConfig(newConfig);
            } else {
                throw new NutsRepositoryNotFoundException(getName());
            }
        }
    }

    protected NutsRepository openRepository(NutsRepositoryLocation loc, String repositoriesRoot, boolean autoCreate) {
        loc = loc.copy();
        if (StringUtils.isEmpty(loc.getType())) {
            loc.setType(NutsConstants.REPOSITORY_TYPE_NUTS);
        }
        NutsRepositoryFactoryComponent factory_ = getWorkspace().getExtensionManager().createSupported(NutsRepositoryFactoryComponent.class, loc);
        if (factory_ != null) {
            String root = getWorkspace().getIOManager().expandPath(loc.getName(),
                    repositoriesRoot != null ? repositoriesRoot : CoreIOUtils.createFile(
                            getStoreLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES).getPath());

            NutsRepository r = factory_.create(loc, getWorkspace(), this, root);
            if (r != null) {
                r.open(autoCreate);
                wireRepository(r);
                return r;
            }
        }
        throw new NutsInvalidRepositoryException(loc.getName(), "Invalid type " + loc.getType());
    }

    private String getMirrorsRoot() {
        return new File(getConfigManager().getStoreLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES).getPath();
    }

    protected void wireRepository(NutsRepository repository) {
        CoreNutsUtils.validateRepositoryName(repository.getName(), mirrors.keySet());
        mirrors.put(repository.getName(), repository);
        fireOnAddRepository(repository);
    }

    @Override
    public int getSupportLevel(NutsId id, NutsSession session) {
        checkSession(session);
        int namespaceSupport = getSupportLevelCurrent(id, session);
        if (session.isTransitive()) {
            NutsSession transitiveSession = session.copy().setTransitive(true);
            for (NutsRepository remote : mirrors.values()) {
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
        if (StringUtils.isEmpty(groups)) {
            return 1;
        }
        return id.getGroup().matches(CoreStringUtils.simpexpToRegexp(groups)) ? groups.length() : 0;
    }

    @Override
    public String getName() {
        return repositoryName;
    }

    @Override
    public String getRepositoryType() {
        return getConfigManager().getType();
    }

    @Override
    public void save() {
        getConfigManager().save();
        NutsException error = null;
        for (NutsRepository repository : mirrors.values()) {
            try {
                repository.save();
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }
    }

    @Override
    public void save(boolean force) {
        getConfigManager().save(force);
        NutsException error = null;
        for (NutsRepository repository : mirrors.values()) {
            try {
                repository.save(force);
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }
    }

    @Override
    public void removeMirror(String repositoryId) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException();
        }
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
            log.log(Level.FINEST, StringUtils.alignLeft(getName(), 20) + " remove repo " + repositoryId);
        }
        getConfigManager().removeMirror(repositoryId);
        if (repo != null) {
            mirrors.remove(repositoryId);
            fireOnRemoveRepository(repo);
        }
    }

    @Override
    public boolean containsMirror(String repositoryIdPath) {
        return mirrors.containsKey(repositoryIdPath);
    }

    public NutsRepository getMirror(String repositoryIdPath) {
        NutsRepository r = mirrors.get(repositoryIdPath);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(repositoryIdPath);
    }

    @Override
    public NutsRepository[] getMirrors() {
        return mirrors.values().toArray(new NutsRepository[0]);
    }

    @Override
    public NutsRepository addMirror(NutsRepositoryLocation loc, boolean autoCreate) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException();
        }
        loc = loc.copy();
        if (StringUtils.isEmpty(loc.getType())) {
            loc.setType(NutsConstants.REPOSITORY_TYPE_NUTS);
        }
        boolean supported = false;
        try {
            supported = getWorkspace().getRepositoryManager().isSupportedRepositoryType(loc.getType());
        } catch (Exception e) {
            //
        }
        String mirrorName = loc.getName();
        if (!supported) {
            throw new NutsInvalidRepositoryException(mirrorName, "Invalid type " + loc.getType());
        }
        NutsRepositoryLocation repoConf = getConfigManager().getMirror(mirrorName);
        if (repoConf != null) {
            throw new NutsRepositoryAlreadyRegisteredException(mirrorName);
        }
        getConfigManager().addMirror(loc);
        return openRepository(loc, getMirrorsRoot(), autoCreate);
    }

    @Override
    public String toString() {
        return "id=" + getName()
                + " ; impl=" + getClass().getSimpleName() + " ; folder=" + getConfigManager().getLocation() + (StringUtils.isEmpty(getConfigManager().getLocation()) ? "" : (" ; location=" + getConfigManager().getLocation()));
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
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
    }

    @Override
    public NutsDescriptor fetchDescriptor(NutsId id, NutsSession session) {
        checkSession(session);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_FETCH_DESC, "fetch-descriptor");
        Map<String, String> queryMap = id.getQueryMap();
        queryMap.remove(NutsConstants.QUERY_OPTIONAL);
        queryMap.remove(NutsConstants.QUERY_SCOPE);
        queryMap.put(NutsConstants.QUERY_FACE, NutsConstants.FACE_DESCRIPTOR);
        id = id.setQuery(queryMap);
        checkAllowedFetch(id, session);
        long startTime = System.currentTimeMillis();
        try {
            String versionString = id.getVersion().getValue();
            NutsDescriptor d = null;
            if (CoreVersionUtils.isStaticVersionPattern(versionString) || StringUtils.isEmpty(versionString)) {
                if (StringUtils.isEmpty(versionString) || "LATEST".equals(versionString) || "RELEASE".equals(versionString)) {
                    NutsId a = findLatestVersion(id.setVersion(null), null, session);
                    if (a == null) {
                        throw new NutsNotFoundException(id);
                    }
                    a = a.setFaceDescriptor();
                    d = fetchDescriptorImpl(a, session);
                } else {
                    id = id.setFaceDescriptor();
                    d = fetchDescriptorImpl(id, session);
                }
            } else {
                DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(id.getQueryMap(), new NutsSimpleIdFilter(id), CoreVersionUtils.createNutsVersionFilter(versionString), null, this, session);
                NutsId a = findLatestVersion(id.setVersion(null), filter, session);
                if (a == null) {
                    throw new NutsNotFoundException(id);
                }
                a = a.setFaceDescriptor();
                d = fetchDescriptorImpl(a, session);
            }
            if (d == null) {
                throw new NutsNotFoundException(id);
            }
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.SUCCESS, "Fetch descriptor", startTime);
            }
            return d;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.ERROR, "Fetch descriptor", startTime);
            }
            throw ex;
        }
    }

    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsSession session) {
        List<NutsId> allVersions = findVersions(id, filter, session);
        NutsId a = null;
        for (NutsId next : allVersions) {
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    protected void traceMessage(NutsSession session, NutsId id, TraceResult tracePhase, String title, long startTime) {
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
        log.log(Level.FINEST, tracePhaseString + fetchString + StringUtils.alignLeft(title, 18) + " " + StringUtils.alignLeft(getName(), 20) + " " + (id == null ? "" : id.toString()) + timeMessage);
    }

    @Override
    public NutsId deploy(NutsId id, NutsDescriptor descriptor, String file, NutsConfirmAction foundAction, NutsSession session) {
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_DEPLOY, "deploy");
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsIllegalArgumentException("Empty group");
        }
        if (StringUtils.isEmpty(id.getName())) {
            throw new NutsIllegalArgumentException("Empty name");
        }
        if ((id.getVersion().isEmpty())) {
            throw new NutsIllegalArgumentException("Empty version");
        }
        if ("RELEASE".equals(id.getVersion().getValue())
                || "LATEST".equals(id.getVersion().getValue())
        ) {
            throw new NutsIllegalArgumentException("Invalid version " + id.getVersion());
        }
//        if (descriptor.getArch().length > 0 || descriptor.getOs().length > 0 || descriptor.getOsdist().length > 0 || descriptor.getPlatform().length > 0) {
//            if (StringUtils.isEmpty(descriptor.getFace())) {
//                throw new NutsIllegalArgumentException("face property '" + NutsConstants.QUERY_FACE + "' could not be null if env {arch,os,osdist,platform} is specified");
//            }
//        }
        try {
            id = id.unsetQuery();
            id = id.setAlternative(descriptor.getAlternative());
            NutsId d = deployImpl(id, descriptor, file, foundAction, session);
            if (session.isIndexEnabled() && nutsIndexStoreClient != null && nutsIndexStoreClient.isEnabled()) {
                try {
                    nutsIndexStoreClient.revalidate(id);
                } catch (NutsException ex) {
                    log.log(Level.FINEST, "[ERROR  ] Error revalidating Indexer for " + getName() + " : " + ex);
                }
            }
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[SUCCESS] " + StringUtils.alignLeft(getName(), 20) + " Deploy " + id);
            }
            return d;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] " + StringUtils.alignLeft(getName(), 20) + " Deploy " + id);
            }
            throw ex;
        }
    }

    @Override
    public void push(NutsId id, String repoId, NutsConfirmAction foundAction, NutsSession session) {
        checkSession(session);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_PUSH, "push");
        try {
            pushImpl(id, repoId, foundAction, session);
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[SUCCESS] " + StringUtils.alignLeft(getName(), 20) + " Push " + id);
            }
        } catch (RuntimeException ex) {

            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] " + StringUtils.alignLeft(getName(), 20) + " Push " + id);
            }
        }
    }

    public Iterator<NutsId> find(final NutsIdFilter filter, NutsSession session) {
        checkSession(session);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_FETCH_DESC, "find");
        checkAllowedFetch(null, session);
        try {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[SUCCESS] " + StringUtils.alignLeft(getName(), 20) + " Find components");
            }
            if (session.isIndexEnabled() && nutsIndexStoreClient != null && nutsIndexStoreClient.isEnabled()) {
                Iterator<NutsId> o = null;
                try {
                    o = nutsIndexStoreClient.find(filter, session);
                } catch (NutsException ex) {
                    log.log(Level.FINEST, "[ERROR  ] Error find operation using Indexer for " + getName() + " : " + ex);
                }

                if (o != null) {
                    return o;
                }
            }

            return findImpl(filter, session);
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] " + StringUtils.alignLeft(getName(), 20) + " Find components");
            }
            throw ex;
        }
    }

    @Override
    public NutsContent fetchContent(NutsId id, String localPath, NutsSession session) {
        checkSession(session);
        id = id.setFaceComponent();
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_FETCH_CONTENT, "fetch");
        checkAllowedFetch(id, session);
        long startTime = System.currentTimeMillis();
        try {
            NutsContent f = fetchContentImpl(id, localPath, session);
            if (f == null) {
                throw new NutsNotFoundException(id);
            }
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.SUCCESS, "Fetch component", startTime);
            }
            return f;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                traceMessage(session, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
    }

    public List<NutsId> findVersions(NutsId id, NutsIdFilter idFilter, NutsSession session) {
        id = id.setFaceComponent();
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_FETCH_DESC, "find-versions");
        checkSession(session);
        checkNutsId(id);
        checkAllowedFetch(id, session);
        try {
            if (session.isIndexEnabled() && nutsIndexStoreClient != null && nutsIndexStoreClient.isEnabled()) {
                List<NutsId> d = null;
                try {
                    d = nutsIndexStoreClient.findVersions(id, session);
                } catch (NutsException ex) {
                    log.log(Level.FINEST, "[ERROR  ] Error find version operation with Indexer for " + getName() + " : " + ex);
                }
                if (d != null && !d.isEmpty()) {
                    if (idFilter != null) {
                        for (Iterator<NutsId> iterator = d.iterator(); iterator.hasNext(); ) {
                            NutsId ii = iterator.next();
                            if (!idFilter.accept(ii)) {
                                iterator.remove();
                            }
                        }
                    }
                    return d;
                }
            }
            List<NutsId> d = findVersionsImpl(id, idFilter, session);
            if (d.isEmpty()) {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "[ERROR  ] [" + StringUtils.alignLeft(session.getFetchMode().toString(), 7) + "] " + StringUtils.alignLeft(getName(), 20) + " " + StringUtils.alignLeft("Fetch versions for", 24) + " " + id);
                }
            } else {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "[SUCCESS] [" + StringUtils.alignLeft(session.getFetchMode().toString(), 7) + "] " + StringUtils.alignLeft(getName(), 20) + " " + StringUtils.alignLeft("Fetch versions for", 24) + " " + id);
                }
            }
            return d;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] [" + StringUtils.alignLeft(session.getFetchMode().toString(), 7) + "] " + StringUtils.alignLeft(getName(), 20) + " " + StringUtils.alignLeft("Fetch versions for", 24) + " " + id);
            }
            throw ex;
        }
    }

    public void undeploy(NutsId id, NutsSession session) {
        checkSession(session);
        getSecurityManager().checkAllowed(NutsConstants.RIGHT_UNDEPLOY, "undeploy");
        try {
            undeployImpl(id, session);
            if (session.isIndexEnabled() && nutsIndexStoreClient != null && nutsIndexStoreClient.isEnabled()) {
                try {
                    nutsIndexStoreClient.invalidate(id);
                } catch (NutsException ex) {
                    log.log(Level.FINEST, "[ERROR  ] Error invalidating Indexer for " + getName() + " : " + ex);
                }
            }
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[SUCCESS] " + StringUtils.alignLeft(getName(), 20) + " Undeploy " + id);
            }
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] " + StringUtils.alignLeft(getName(), 20) + " Undeploy " + id);
            }
        }
    }

    protected String getIdComponentExtension(String packaging) {
        return getWorkspace().getConfigManager().getDefaultIdComponentExtension(packaging);
    }

    protected String getIdExtension(NutsId id) {
        return getWorkspace().getConfigManager().getDefaultIdExtension(id);
    }

    protected String getIdFilename(NutsId id) {
        String classifier = "";
        String ext = getIdExtension(id);
        if (!ext.equals(".nuts") && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!StringUtils.isEmpty(c)) {
                classifier = "-" + c;
            }
        }
        return id.getName() + "-" + id.getVersion().getValue() + classifier + ext;
    }

    protected void checkSession(NutsSession session) {
        if (session == null) {
            throw new NutsIllegalArgumentException("Missing Session");
        }
    }

    protected void checkNutsId(NutsId id) {
        if (id == null) {
            throw new NutsIllegalArgumentException("Missing id");
        }
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsIllegalArgumentException("Missing group for " + id);
        }
        if (StringUtils.isEmpty(id.getName())) {
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
            pr.getConfigManager().getMirror(getName()).setEnabled(enabled);
        } else {
            getWorkspace().getConfigManager().setRepositoryEnabled(getName(), enabled);
        }
    }

    @Override
    public boolean isEnabled() {
        NutsRepository pr = getParentRepository();
        if (pr != null) {
            return pr.getConfigManager().getMirror(getName()).isEnabled();
        } else {
            return getWorkspace().getConfigManager().isRepositoryEnabled(getName());
        }
    }

    private void checkNutsRepositoryConfig(NutsRepositoryConfig config) {
        if (StringUtils.isEmpty(config.getType())) {
            throw new NutsIllegalArgumentException("Empty Repository Type");
        }
        if (StringUtils.isEmpty(config.getName())) {
            throw new NutsIllegalArgumentException("Empty Repository Id");
        }
//        if (StringUtils.isEmpty(config.getLocation())) {
//            throw new NutsIllegalArgumentException("Empty Repository Id");
//        }
    }

    protected void fireOnUndeploy(NutsContentEvent evt) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onUndeploy(evt);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onUndeploy(evt);
        }
    }

    protected void fireOnDeploy(NutsContentEvent file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onDeploy(file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onDeploy(file);
        }
    }

    protected void fireOnInstall(NutsContentEvent evt) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onInstall(evt);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onInstall(evt);
        }
    }

    protected void fireOnPush(NutsContentEvent file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onPush(file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onPush(file);
        }
    }

    protected void fireOnAddRepository(NutsRepository repository) {
        NutsRepositoryEvent event = new NutsRepositoryEvent(getWorkspace(), this, repository);
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onAddRepository(event);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onAddRepository(event);
        }
    }

    protected void fireOnRemoveRepository(NutsRepository repository) {
        NutsRepositoryEvent event = new NutsRepositoryEvent(getWorkspace(), this, repository);
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onRemoveRepository(event);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryManager().getRepositoryListeners()) {
            listener.onRemoveRepository(event);
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

    public boolean isTransientRepository() {
        return transientRepository;
    }

    public AbstractNutsRepository setTransientRepository(boolean transientRepository) {
        this.transientRepository = transientRepository;
        return this;
    }

    protected abstract void undeployImpl(NutsId id, NutsSession session);

    protected abstract List<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsSession session);

    protected abstract NutsContent fetchContentImpl(NutsId id, String localPath, NutsSession session);

    protected abstract Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session);

    protected abstract void pushImpl(NutsId id, String repoId, NutsConfirmAction foundAction, NutsSession session);

    protected abstract NutsId deployImpl(NutsId id, NutsDescriptor descriptor, String file, NutsConfirmAction foundAction, NutsSession session);

    protected abstract NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session);

    protected void helperHttpDownloadToFile(String path, File file, boolean mkdirs) throws IOException {
        InputStream stream = CoreHttpUtils.getHttpClientFacade(getWorkspace(), path).open();
        if (stream != null) {
            if (!path.toLowerCase().startsWith("file://")) {
                log.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
            } else {
                log.log(Level.FINEST, "downloading url {0} to file {1}", new Object[]{path, file});
            }
        } else {
            log.log(Level.FINEST, "downloading url failed : {0} to file {1}", new Object[]{path, file});
        }
        CoreNutsUtils.copy(stream, file, mkdirs, true);
    }

    protected String getIdRemotePath(NutsId id) {
        return URLUtils.buildUrl(getConfigManager().getLocation(true), getIdRelativePath(id));
    }

    protected String getIdRelativePath(NutsId id) {
        String groupId = id.getGroup();
        String artifactId = id.getName();
        String version = id.getVersion().getValue();
        String idFilename = getIdFilename(id);
        String a = id.getAlternative();
        if (!StringUtils.isEmpty(a) && !NutsConstants.ALTERNATIVE_DEFAULT_VALUE.equals(a)) {
            idFilename = a + "/" + idFilename;
        }
        return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + idFilename;
    }

    @Override
    public String getUuid() {
        return getConfigManager().getUuid();
    }

    @Override
    public String getStoreLocation(NutsStoreFolder folderType) {
        return getConfigManager().getStoreLocation(folderType);
    }

    @Override
    public boolean isIndexSubscribed() {
        return this.nutsIndexStoreClient.isSubscribed(this);
    }

    @Override
    public boolean subscribeIndex() {
        return this.nutsIndexStoreClient.subscribe();
    }

    @Override
    public void unsubscribeIndex() {
        this.nutsIndexStoreClient.unsubscribe();
    }
}
