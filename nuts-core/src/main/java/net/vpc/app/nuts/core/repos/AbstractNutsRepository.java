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

import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsDeployRepositoryCommand;
import net.vpc.app.nuts.core.DefaultNutsFetchContentRepositoryCommand;
import net.vpc.app.nuts.core.DefaultNutsFetchDescriptorRepositoryCommand;
import net.vpc.app.nuts.core.DefaultNutsSearchRepositoryCommand;
import net.vpc.app.nuts.core.DefaultNutsSeachVersionsRepositoryCommand;
import net.vpc.app.nuts.core.DefaultNutsPushRepositoryCommand;
import net.vpc.app.nuts.core.DefaultNutsRepositoryEvent;
import net.vpc.app.nuts.core.DefaultNutsRepositoryUndeployCommand;
import net.vpc.app.nuts.core.DefaultNutsUpdateRepositoryStatisticsCommand;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepository implements NutsRepository, NutsRepositoryExt {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(AbstractNutsRepository.class.getName());
    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<>();
    private NutsRepository parentRepository;
    private NutsWorkspace workspace;
    private final NutsRepositorySecurityManager securityManager = new DefaultNutsRepositorySecurityManager(this);
    private DefaultNutsRepositoryConfigManager configManager;
    protected NutsIndexStoreClient nutsIndexStoreClient;

    public AbstractNutsRepository(NutsCreateRepositoryOptions options,
            NutsWorkspace workspace, NutsRepository parent,
            int speed, boolean supportedMirroring, String repositoryType) {
        init(options, workspace, parentRepository, speed, supportedMirroring, repositoryType);
    }

    @Override
    public NutsIndexStoreClient getIndexStoreClient() {
        return nutsIndexStoreClient;
    }

    protected void init(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parent, int speed, boolean supportedMirroring, String repositoryType) {
        NutsRepositoryConfig optionsConfig = options.getConfig();
        if (optionsConfig == null) {
            throw new NutsIllegalArgumentException(workspace, "Null Config");
        }
        this.workspace = workspace;
        this.parentRepository = parent;
        configManager = new DefaultNutsRepositoryConfigManager(
                this, options.getLocation(), optionsConfig,
                Math.max(0, speed), options.getDeployOrder(),
                options.isTemporay(), options.isEnabled(),
                optionsConfig.getName(), supportedMirroring,
                options.getName(), repositoryType
        );
        this.nutsIndexStoreClient = workspace.config().getIndexStoreClientFactory().createNutsIndexStoreClient(this);
//        Path file = config().getStoreLocation().resolve(NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME);
//        boolean found = false;
//        if (Files.exists(file)) {
//            //ok
//        } else {
//            if (options.isCreate()) {
//                NutsRepositoryConfig newConfig = new NutsRepositoryConfig(config().getName(), config().getLocation(true), getRepositoryType());
//                newConfig.setUuid(UUID.randomUUID().toString());
//                newConfig.setStoreLocationStrategy(getWorkspace().config().getRepositoryStoreLocationStrategy());
//                checkNutsRepositoryConfig(newConfig);
//                configManager.setConfig(newConfig);
//            } else {
//                throw new NutsRepositoryNotFoundException(config().getName());
//            }
//        }
//        open(options.isCreate());
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

    @Override
    public boolean acceptNutsId(NutsId id) {
        String groups = config().getGroups();
        if (CoreStringUtils.isBlank(groups)) {
            return true;
        }
        return id.getGroup().matches(CoreStringUtils.simpexpToRegexp(groups));
    }

    @Override
    public int getFindSupportLevelCurrent(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode) {
        switch (supportedAction) {
            case SEARCH: {
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
                if (CoreStringUtils.isBlank(groups)) {
                    return 1;
                }
                return id.getGroup().matches(CoreStringUtils.simpexpToRegexp(groups)) ? groups.length() : 0;
            }
            case DEPLOY: {
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
                if (CoreStringUtils.isBlank(groups)) {
                    return 1 * config().getDeployOrder();
                }
                return id.getGroup().matches(CoreStringUtils.simpexpToRegexp(groups)) ? groups.length() : 0;
            }
        }
        throw new NutsUnsupportedArgumentException(workspace, "Unsupported action " + supportedAction);
    }

    @Override
    public String getRepositoryType() {
        return config().getType();
    }

    @Override
    public String toString() {
        NutsRepositoryConfigManager c = config();
        String name = config().getName();
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

    @Override
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
    public NutsFetchDescriptorRepositoryCommand fetchDescriptor() {
        return new DefaultNutsFetchDescriptorRepositoryCommand(this);
    }

    @Override
    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        Iterator<NutsId> allVersions = searchVersions().id(id).filter(filter).session(session).run().getResult();
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
        CoreNutsUtils.traceMessage(LOG, config().name(), session, id, tracePhase, title, startTime);
    }

    @Override
    public NutsDeployRepositoryCommand deploy() {
        return new DefaultNutsDeployRepositoryCommand(this);
    }

    @Override
    public NutsPushRepositoryCommand push() {
        return new DefaultNutsPushRepositoryCommand(this);
    }

    @Override
    public NutsSearchRepositoryCommand search() {
        return new DefaultNutsSearchRepositoryCommand(this);
    }

    @Override
    public NutsFetchContentRepositoryCommand fetchContent() {
        return new DefaultNutsFetchContentRepositoryCommand(this);
    }

    @Override
    public NutsSearchVersionsRepositoryCommand searchVersions() {
        return new DefaultNutsSeachVersionsRepositoryCommand(this);
    }

    @Override
    public NutsRepositoryUndeployCommand undeploy() {
        return new DefaultNutsRepositoryUndeployCommand(this);
    }

    protected String getIdComponentExtension(String packaging) {
        return getWorkspace().config().getDefaultIdComponentExtension(packaging);
    }

    protected String getIdExtension(NutsId id) {
        return getWorkspace().config().getDefaultIdExtension(id);
    }

    @Override
    public String getIdBasedir(NutsId id) {
        return getWorkspace().config().getDefaultIdBasedir(id);
    }

    @Override
    public String getIdFilename(NutsId id) {
        //return getWorkspace().config().getDefaultIdFilename(id);
        String classifier = "";
        String ext = getIdExtension(id);
        if (!ext.equals(".nuts") && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!CoreStringUtils.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getName() + "-" + id.getVersion().getValue() + classifier + ext;
    }

    protected void checkSession(NutsSession session) {
        if (session == null) {
            throw new NutsIllegalArgumentException(workspace, "Missing Session");
        }
    }

    protected void checkSession(NutsRepositorySession session) {
        if (session == null) {
            throw new NutsIllegalArgumentException(workspace, "Missing Session");
        }
    }

    protected void checkNutsId(NutsId id) {
        if (id == null) {
            throw new NutsIllegalArgumentException(workspace, "Missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroup())) {
            throw new NutsIllegalArgumentException(workspace, "Missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            throw new NutsIllegalArgumentException(workspace, "Missing name for " + id);
        }
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public void fireOnUndeploy(NutsContentEvent evt) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onUndeploy(evt);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onUndeploy(evt);
        }
    }

    @Override
    public void fireOnDeploy(NutsContentEvent file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onDeploy(file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onDeploy(file);
        }
    }

    @Override
    public void fireOnInstall(NutsContentEvent evt) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onInstall(evt);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onInstall(evt);
        }
    }

    @Override
    public void fireOnPush(NutsContentEvent file) {
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onPush(file);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onPush(file);
        }
    }

    @Override
    public void fireOnAddRepository(NutsRepository repository) {
        NutsRepositoryEvent event = new DefaultNutsRepositoryEvent(getWorkspace(), this, repository);
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onAddRepository(event);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onAddRepository(event);
        }
    }

    @Override
    public void fireOnRemoveRepository(NutsRepository repository) {
        NutsRepositoryEvent event = new DefaultNutsRepositoryEvent(getWorkspace(), this, repository);
        for (NutsRepositoryListener listener : getRepositoryListeners()) {
            listener.onRemoveRepository(event);
        }
        for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
            listener.onRemoveRepository(event);
        }
    }

    protected void helperHttpDownloadToFile(String path, Path file, boolean mkdirs) throws IOException {
        InputStream stream = CoreIOUtils.getHttpClientFacade(getWorkspace(), path).open();
        if (stream != null) {
            if (!path.toLowerCase().startsWith("file://")) {
                LOG.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
            } else {
                LOG.log(Level.FINEST, "downloading url {0} to file {1}", new Object[]{path, file});
            }
        } else {
            LOG.log(Level.FINEST, "downloading url failed : {0} to file {1}", new Object[]{path, file});
        }
        getWorkspace().io().copy().from(stream).to(file).safeCopy().monitorable().run();
    }

    protected String getIdRemotePath(NutsId id) {
        return CoreIOUtils.buildUrl(config().getLocation(true), getIdRelativePath(id));
    }

    protected String getIdRelativePath(NutsId id) {
        return getIdBasedir(id) + "/" + getIdFilename(id);
    }

    @Override
    public String getUuid() {
        return config().getUuid();
    }

    @Override
    public String uuid() {
        return getUuid();
    }

    @Override
    public NutsUpdateRepositoryStatisticsCommand updateStatistics() {
        return new DefaultNutsUpdateRepositoryStatisticsCommand(this) {
            @Override
            public NutsUpdateRepositoryStatisticsCommand run() {
                return this;
            }
        };
    }
}
