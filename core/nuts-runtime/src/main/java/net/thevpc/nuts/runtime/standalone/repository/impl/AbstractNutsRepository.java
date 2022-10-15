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
 *
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
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNutsRepoConfigManager;
import net.thevpc.nuts.runtime.standalone.repository.config.NutsRepositoryConfigModel;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsCachedValue;
import net.thevpc.nuts.runtime.standalone.util.collections.DefaultObservableMap;
import net.thevpc.nuts.runtime.standalone.util.collections.ObservableMap;
import net.thevpc.nuts.spi.NutsRepositoryLocation;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.util.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsRepositorySecurityManager;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsRepositorySecurityModel;
import net.thevpc.nuts.util.NutsMapListener;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepository implements NutsRepository, NutsRepositorySPI {

    private static final long serialVersionUID = 1L;

    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<>();
    protected NutsRepository parentRepository;
    protected NutsWorkspace workspace;
    protected DefaultNutsRepositorySecurityModel securityModel;
    protected NutsRepositoryConfigModel configModel;
    protected ObservableMap<String, Object> userProperties;
    protected NutsSession initSession;
    protected NutsCachedValue<Boolean> available = new NutsCachedValue<>(this::isAvailableImpl, 0);
    protected boolean supportsDeploy;

    public AbstractNutsRepository() {
        userProperties = new DefaultObservableMap<>();
        securityModel = new DefaultNutsRepositorySecurityModel(this);
    }

    protected void checkSession(NutsSession session) {
        NutsSessionUtils.checkSession(getWorkspace(), session);
    }

    @Override
    public boolean isAvailable(NutsSession session) {
        return isAvailable(false, session);
    }

    @Override
    public boolean isAvailable(boolean force, NutsSession session) {
        if (force) {
            return available.update(session);
        }
        return available.getValue(session);
    }

    @Override
    public boolean isSupportedDeploy(NutsSession session) {
        return isSupportedDeploy(false, session);
    }

    @Override
    public boolean isSupportedDeploy(boolean force, NutsSession session) {
        return supportsDeploy;
    }

    protected boolean isAvailableImpl(NutsSession session) {
        return true;
    }

    @Override
    public String getRepositoryType() {
        return config().getType();
    }

    @Override
    public String getUuid() {
        return configModel.getUuid();
    }

    @Override
    public String getName() {
        return configModel.getName();
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsRepository getParentRepository() {
        return parentRepository;
    }

    @Override
    public NutsRepositoryConfigManager config() {
        return new DefaultNutsRepoConfigManager(configModel);
    }

    @Override
    public NutsRepositorySecurityManager security() {
        return new DefaultNutsRepositorySecurityManager(securityModel);
    }

    @Override
    public NutsRepository removeRepositoryListener(NutsRepositoryListener listener) {
        repositoryListeners.add(listener);
        return this;
    }

    @Override
    public NutsRepository addRepositoryListener(NutsRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
        return this;
    }

    @Override
    public List<NutsRepositoryListener> getRepositoryListeners() {
        return repositoryListeners;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public NutsRepository addUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.addListener(listener);
        return this;
    }

    @Override
    public NutsRepository removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.removeListener(listener);
        return this;
    }

    @Override
    public List<NutsMapListener<String, Object>> getUserPropertyListeners() {
        return userProperties.getListeners();
    }

    public boolean isEnabled(NutsSession session) {
        return this.config().setSession(session).isEnabled();
    }

    public NutsRepository setEnabled(boolean enabled, NutsSession session) {
        this.config().setSession(session).setEnabled(enabled);
        return this;
    }

    @Override
    public String toString() {
        NutsRepositoryConfigManagerExt cc = NutsRepositoryConfigManagerExt.of(config());
        NutsRepositoryConfigManager c = config();
        String name = getName();
        String storePath = null;
        NutsRepositoryLocation loc = cc.getModel().getLocation();
        String impl = getClass().getSimpleName();
        if (c != null) {
            NutsPath storeLocation = cc.getModel().getStoreLocation();
            storePath = storeLocation == null ? null : storeLocation.toAbsolute().toString();
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
            a.put("location", loc.toString());
        }
        return a.toString();
    }

    protected String getIdExtension(NutsId id, NutsSession session) {
        return session.locations().getDefaultIdExtension(id);
    }

    public NutsPath getIdBasedir(NutsId id, NutsSession session) {
        return session.locations().getDefaultIdBasedir(id);
    }

    public String getIdFilename(NutsId id, NutsSession session) {
        return getIdFilename(id,getIdExtension(id, session),session);
    }

    public String getIdFilename(NutsId id, String ext,NutsSession session) {
        String classifier = "";
        if (!ext.equals(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!NutsBlankable.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }
}
