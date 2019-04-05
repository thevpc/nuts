/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.core.filters.id.NutsSimpleIdFilter;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.common.util.IteratorBuilder;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepository implements NutsRepository {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(AbstractNutsRepository.class.getName());
    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<>();
    private String repositoryName;
    private NutsRepository parentRepository;
    private NutsWorkspace workspace;
    private final NutsRepositorySecurityManager securityManager = new DefaultNutsRepositorySecurityManager(this);
    private DefaultNutsRepositoryConfigManager configManager;
    protected NutsIndexStoreClient nutsIndexStoreClient;

    public AbstractNutsRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parent, int speed, boolean supportedMirroring) {
        init(options, workspace, parentRepository, speed, supportedMirroring);
    }

    protected void init(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parent, int speed, boolean supportedMirroring) {
        if (options.getConfig() == null) {
            throw new NutsIllegalArgumentException("Null Config");
        }
        checkNutsRepositoryConfig(options.getConfig());
        String folder = options.getLocation();
        if (StringUtils.isEmpty(folder)) {
            throw new IllegalArgumentException("Missing folder");
        }
        Path pfolder = workspace.io().path(folder);
        if ((Files.exists(pfolder) && !Files.isDirectory(pfolder))) {
            throw new NutsInvalidRepositoryException(folder, "Unable to resolve root as a valid folder " + options.getLocation());
        }
        configManager = new DefaultNutsRepositoryConfigManager(this, folder, options.getConfig(), Math.max(0, speed), options.getDeployOrder(), options.isTemporay(), options.isEnabled(), options.getConfig() == null ? null : options.getConfig().getName(), supportedMirroring);
        this.repositoryName = options.getName();
        this.workspace = workspace;
        this.parentRepository = parent;
        this.nutsIndexStoreClient = workspace.config().getIndexStoreClientFactory().createNutsIndexStoreClient(this);
        open(options.isCreate());
    }

    @Override
    public NutsRepository getParentRepository() {
        return parentRepository;
    }

    @Override
    public NutsRepositoryConfigManager config() {
        return configManager;
    }

    @Override
    public NutsRepositorySecurityManager security() {
        return securityManager;
    }

    protected void open(boolean autoCreate) {
        Path file = config().getStoreLocation().resolve(NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME);
        boolean found = false;
        if (Files.exists(file)) {
            NutsRepositoryConfig newConfig = null;
            try {
                newConfig = workspace.io().readJson(file, NutsRepositoryConfig.class);
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
                if (!getWorkspace().config().isReadOnly()) {
                    Path newfile = config().getStoreLocation().resolve("nuts-repository-"
                            + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())
                            + ".json");
                    log.log(Level.SEVERE, "Erroneous config file will replace by fresh one. Old config is copied to {0}", newfile);
                    try {
                        Files.move(file, newfile);
                        autoCreate = true;
                    } catch (IOException e) {
                        throw new UncheckedIOException("Unable to load and re-create config file " + file + " : " + e.toString(), e);
                    }
                } else {
                    throw new UncheckedIOException("Unable to load config file " + file, new IOException(ex));
                }
            }
            if (newConfig != null) {
                found = true;
                if (StringUtils.isEmpty(newConfig.getUuid())) {
                    newConfig.setUuid(UUID.randomUUID().toString());
                    if (!workspace.config().isReadOnly()) {
                        //save updates without processing mirrors
                        workspace.io().writeJson(newConfig, file, true);
                    }
                }
                newConfig.setType(getRepositoryType());
                checkNutsRepositoryConfig(newConfig);
                configManager.setConfig(newConfig);
            }
        }
        if (!found) {
            if (autoCreate) {
                NutsRepositoryConfig newConfig = new NutsRepositoryConfig(getName(), config().getLocation(true), getRepositoryType());
                newConfig.setUuid(UUID.randomUUID().toString());
                newConfig.setStoreLocationStrategy(getWorkspace().config().getRepositoryStoreLocationStrategy());
                checkNutsRepositoryConfig(newConfig);
                configManager.setConfig(newConfig);
            } else {
                throw new NutsRepositoryNotFoundException(getName());
            }
        }
    }

//    protected NutsRepository createRepository(NutsCreateRepositoryOptions options) {
//        NutsRepository r = ;
//        wireRepository(r);
//        return r;
//    }
//
//    protected NutsRepository openRepository(NutsRepositoryRef loc, String path) {
//        loc = loc.copy();
//        String root = getWorkspace().getIOManager().expandPath(loc.getLocation(),
//                path != null ? path : CoreIOUtils.createFile(
//                                getStoreLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES).getPath());
//
//        NutsRepository r = CoreNutsUtils.createRepository(root, getWorkspace());
//        r.open(false);
//        wireRepository(r);
//        return r;
//    }
    protected int getDeploymentSupportLevelCurrent(NutsId id, boolean offlineOnly) {
        if (offlineOnly && config().getSpeed() < SPEED_FAST) {
            return 0;
        }
        String groups = config().getGroups();
        if (StringUtils.isEmpty(groups)) {
            return 1 * config().getDeployOrder();
        }
        return id.getGroup().matches(CoreStringUtils.simpexpToRegexp(groups)) ? groups.length() : 0;
    }

    protected int getFindSupportLevelCurrent(NutsId id, NutsFetchMode mode) {
        switch (mode) {
            case INSTALLED:
            case LOCAL: {
                if (config().getSpeed() < SPEED_FAST) {
                    return 0;
                }
                break;
            }
            case REMOTE: {
                if (config().getSpeed() >= SPEED_FAST) {
                    return 0;
                }
                break;
            }
        }
        String groups = config().getGroups();
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
        return config().getType();
    }

    @Override
    public void save(boolean force) {
        config().save(force);
    }

    @Override
    public String toString() {
        NutsRepositoryConfigManager c = config();
        String name = getName();
        String storePath = null;
        String loc = config().getLocation(false);
        String impl = getClass().getSimpleName();
        if (c != null) {
            storePath = c.getStoreLocation().toAbsolutePath().toString();
        }
        LinkedHashMap<String, String> a = new LinkedHashMap<>();
        if (name != null) {
            a.put("name", name);
        }
        if (impl != null) {
            a.put("impl", impl);
        }
        if (storePath != null) {
            a.put("store", storePath);
        }
        if (loc != null) {
            a.put("location", loc);
        }
        return a.toString();
    }

    public void checkAllowedFetch(NutsId id, NutsRepositorySession session) {
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
    public NutsDescriptor fetchDescriptor(NutsId id, NutsRepositorySession session) {
        checkSession(session);
        security().checkAllowed(NutsConstants.RIGHT_FETCH_DESC);
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
            if (DefaultNutsVersion.isStaticVersionPattern(versionString) || StringUtils.isEmpty(versionString)) {
                if (StringUtils.isEmpty(versionString) || "LATEST".equals(versionString) || "RELEASE".equals(versionString)) {
                    NutsId a = findLatestVersion(id.setVersion(""), null, session);
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
                NutsIdFilter filter = new DefaultNutsIdMultiFilter(id.getQueryMap(), new NutsSimpleIdFilter(id), workspace.parser().parseVersionFilter(versionString), null, this, session).simplify();
                NutsId a = findLatestVersion(id.setVersion(""), filter, session);
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

    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
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

    protected void traceMessage(NutsRepositorySession session, NutsId id, TraceResult tracePhase, String title, long startTime) {
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
        String fetchString = fetchString = "[" + StringUtils.alignLeft(session.getFetchMode().name(), 7) + "] ";
        log.log(Level.FINEST, "{0}{1}{2} {3} {4}{5}", new Object[]{tracePhaseString, fetchString, StringUtils.alignLeft(title, 18), StringUtils.alignLeft(getName(), 20), id == null ? "" : id.toString(), timeMessage});
    }

    @Override
    public void deploy(NutsRepositoryDeploymentOptions deployment, NutsRepositorySession session) {
        security().checkAllowed(NutsConstants.RIGHT_DEPLOY);
        if (deployment == null) {
            throw new NutsIllegalArgumentException("Missing Deployment");
        }
        if (deployment.getId() == null) {
            throw new NutsIllegalArgumentException("Missing Id");
        }
        if (deployment.getContent() == null) {
            throw new NutsIllegalArgumentException("Missing Content");
        }
        if (deployment.getDescriptor() == null) {
            throw new NutsIllegalArgumentException("Missing Descriptor");
        }
        if (StringUtils.isEmpty(deployment.getId().getGroup())) {
            throw new NutsIllegalArgumentException("Empty group");
        }
        if (StringUtils.isEmpty(deployment.getId().getName())) {
            throw new NutsIllegalArgumentException("Empty name");
        }
        if ((deployment.getId().getVersion().isEmpty())) {
            throw new NutsIllegalArgumentException("Empty version");
        }
        if ("RELEASE".equals(deployment.getId().getVersion().getValue())
                || "LATEST".equals(deployment.getId().getVersion().getValue())) {
            throw new NutsIllegalArgumentException("Invalid version " + deployment.getId().getVersion());
        }
//        if (descriptor.getArch().length > 0 || descriptor.getOs().length > 0 || descriptor.getOsdist().length > 0 || descriptor.getPlatform().length > 0) {
//            if (StringUtils.isEmpty(descriptor.getFace())) {
//                throw new NutsIllegalArgumentException("face property '" + NutsConstants.QUERY_FACE + "' could not be null if env {arch,os,osdist,platform} is specified");
//            }
//        }
        try {
            deployImpl(deployment, session);
            if (session.isIndexed() && nutsIndexStoreClient != null && nutsIndexStoreClient.isEnabled()) {
                try {
                    nutsIndexStoreClient.revalidate(deployment.getId());
                } catch (NutsException ex) {
                    log.log(Level.FINEST, "[ERROR  ] Error revalidating Indexer for {0} : {1}", new Object[]{getName(), ex});
                }
            }
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[SUCCESS] {0} Deploy {1}", new Object[]{StringUtils.alignLeft(getName(), 20), deployment.getId()});
            }
            deployment.getId();
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] {0} Deploy {1}", new Object[]{StringUtils.alignLeft(getName(), 20), deployment.getId()});
            }
            throw ex;
        }
    }

    @Override
    public void push(NutsId id, NutsPushOptions options, NutsRepositorySession session) {
        checkSession(session);
        security().checkAllowed(NutsConstants.RIGHT_PUSH);
        try {
            pushImpl(id, options, session);
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[SUCCESS] {0} Push {1}", new Object[]{StringUtils.alignLeft(getName(), 20), id});
            }
        } catch (RuntimeException ex) {

            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] {0} Push {1}", new Object[]{StringUtils.alignLeft(getName(), 20), id});
            }
        }
    }

    @Override
    public Iterator<NutsId> find(final NutsIdFilter filter, NutsRepositorySession session) {
        checkSession(session);
        security().checkAllowed(NutsConstants.RIGHT_FETCH_DESC);
        checkAllowedFetch(null, session);
        try {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[SUCCESS] {0} Find components", StringUtils.alignLeft(getName(), 20));
            }
            if (session.isIndexed() && nutsIndexStoreClient != null && nutsIndexStoreClient.isEnabled()) {
                Iterator<NutsId> o = null;
                try {
                    o = nutsIndexStoreClient.find(filter, session);
                } catch (NutsException ex) {
                    log.log(Level.FINEST, "[ERROR  ] Error find operation using Indexer for {0} : {1}", new Object[]{getName(), ex});
                }

                if (o != null) {
                    return o;
                }
            }

            return findImpl(filter, session);
        } catch (NutsNotFoundException | SecurityException ex) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] {0} Find components", StringUtils.alignLeft(getName(), 20));
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "[ERROR  ] {0} Find components", StringUtils.alignLeft(getName(), 20));
            }
            throw ex;
        }
    }

    @Override
    public NutsContent fetchContent(NutsId id, Path localPath, NutsRepositorySession session) {
        checkSession(session);
        id = id.setFaceComponent();
        security().checkAllowed(NutsConstants.RIGHT_FETCH_CONTENT);
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

    @Override
    public Iterator<NutsId> findVersions(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        id = id.setFaceComponent();
        security().checkAllowed(NutsConstants.RIGHT_FETCH_DESC);
        checkSession(session);
        checkNutsId(id);
        checkAllowedFetch(id, session);
        try {
            if (session.isIndexed() && nutsIndexStoreClient != null && nutsIndexStoreClient.isEnabled()) {
                List<NutsId> d = null;
                try {
                    d = nutsIndexStoreClient.findVersions(id, session);
                } catch (NutsException ex) {
                    log.log(Level.FINEST, "[ERROR  ] Error find version operation with Indexer for {0} : {1}", new Object[]{getName(), ex});
                }
                if (d != null && !d.isEmpty() && idFilter != null) {
                    return IteratorBuilder.of(d.iterator()).filter(x -> idFilter.accept(x)).iterator();
                }
            }
            Iterator<NutsId> d
                    = findVersionsImpl(id, idFilter, session);
//            if (d.isEmpty()) {
//                if (log.isLoggable(Level.FINEST)) {
//                    log.log(Level.FINEST, "[ERROR  ] [{0}] {1} {2} {3}", new Object[]{StringUtils.alignLeft(session.getFetchMode().toString(), 7), StringUtils.alignLeft(getName(), 20), StringUtils.alignLeft("Fetch versions for", 24), id});
//                }
//            } else {
//                if (log.isLoggable(Level.FINEST)) {
//                    log.log(Level.FINEST, "[SUCCESS] [{0}] {1} {2} {3}", new Object[]{StringUtils.alignLeft(session.getFetchMode().toString(), 7), StringUtils.alignLeft(getName(), 20), StringUtils.alignLeft("Fetch versions for", 24), id});
//                }
//            }
            return d;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] [{0}] {1} {2} {3}", new Object[]{StringUtils.alignLeft(session.getFetchMode().toString(), 7), StringUtils.alignLeft(getName(), 20), StringUtils.alignLeft("Fetch versions for", 24), id});
            }
            throw ex;
        }
    }

    public void undeploy(NutsId id, NutsRepositorySession session) {
        checkSession(session);
        security().checkAllowed(NutsConstants.RIGHT_UNDEPLOY);
        try {
            undeployImpl(id, session);
            if (session.isIndexed() && nutsIndexStoreClient != null && nutsIndexStoreClient.isEnabled()) {
                try {
                    nutsIndexStoreClient.invalidate(id);
                } catch (NutsException ex) {
                    log.log(Level.FINEST, "[ERROR  ] Error invalidating Indexer for {0} : {1}", new Object[]{getName(), ex});
                }
            }
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[SUCCESS] {0} Undeploy {1}", new Object[]{StringUtils.alignLeft(getName(), 20), id});
            }
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "[ERROR  ] {0} Undeploy {1}", new Object[]{StringUtils.alignLeft(getName(), 20), id});
            }
        }
    }

    protected String getIdComponentExtension(String packaging) {
        return getWorkspace().config().getDefaultIdComponentExtension(packaging);
    }

    protected String getIdExtension(NutsId id) {
        return getWorkspace().config().getDefaultIdExtension(id);
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

    protected void checkSession(NutsRepositorySession session) {
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

    private void checkNutsRepositoryConfig(NutsRepositoryConfig config) {
        if (StringUtils.isEmpty(config.getType())) {
            throw new NutsIllegalArgumentException("Empty Repository Type");
        }
        if (StringUtils.isEmpty(config.getName())) {
            throw new NutsIllegalArgumentException("Empty Repository Name");
        }
//        if (StringUtils.isEmpty(config.getLocation())) {
//            throw new NutsIllegalArgumentException("Empty Repository Id");
//        }
    }

    protected void fireOnUndeploy(NutsContentEvent evt) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onUndeploy(evt);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onUndeploy(evt);
        }
    }

    protected void fireOnDeploy(NutsContentEvent file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onDeploy(file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onDeploy(file);
        }
    }

    protected void fireOnInstall(NutsContentEvent evt) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onInstall(evt);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onInstall(evt);
        }
    }

    protected void fireOnPush(NutsContentEvent file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onPush(file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onPush(file);
        }
    }

    protected void fireOnAddRepository(NutsRepository repository) {
        NutsRepositoryEvent event = new NutsRepositoryEvent(getWorkspace(), this, repository);
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onAddRepository(event);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onAddRepository(event);
        }
    }

    protected void fireOnRemoveRepository(NutsRepository repository) {
        NutsRepositoryEvent event = new NutsRepositoryEvent(getWorkspace(), this, repository);
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onRemoveRepository(event);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onRemoveRepository(event);
        }
    }

    protected abstract void undeployImpl(NutsId id, NutsRepositorySession session);

    protected abstract Iterator<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session);

    protected abstract NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session);

    protected abstract Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsRepositorySession session);

    protected abstract void pushImpl(NutsId id, NutsPushOptions options, NutsRepositorySession session);

    protected abstract void deployImpl(NutsRepositoryDeploymentOptions deployment, NutsRepositorySession session);

    protected abstract NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session);

    protected void helperHttpDownloadToFile(String path, Path file, boolean mkdirs) throws IOException {
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
        getWorkspace().io().copy().from(stream).to(file).safeCopy().run();
    }

    protected String getIdRemotePath(NutsId id) {
        return URLUtils.buildUrl(config().getLocation(true), getIdRelativePath(id));
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
        return config().getUuid();
    }
}
