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
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepoConfigManager;
import net.thevpc.nuts.runtime.standalone.repository.config.NRepositoryConfigModel;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NCachedValue;
import net.thevpc.nuts.runtime.standalone.util.collections.DefaultObservableMap;
import net.thevpc.nuts.runtime.standalone.util.collections.ObservableMap;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.spi.NRepositorySPI;

import java.util.*;

import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.security.DefaultNRepositorySecurityManager;
import net.thevpc.nuts.runtime.standalone.security.DefaultNRepositorySecurityModel;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMapListener;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNRepository implements NRepository, NRepositorySPI {

    private static final long serialVersionUID = 1L;

    private final List<NRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<>();
    protected NRepository parentRepository;
    protected NWorkspace workspace;
    protected DefaultNRepositorySecurityModel securityModel;
    protected NRepositoryConfigModel configModel;
    protected ObservableMap<String, Object> userProperties;
    protected NSession initSession;
    protected NCachedValue<Boolean> available = new NCachedValue<>(this::isAvailableImpl, 0);
    protected boolean supportsDeploy;
    protected boolean enabled=true;

    public AbstractNRepository() {
        userProperties = new DefaultObservableMap<>();
        securityModel = new DefaultNRepositorySecurityModel(this);
    }

    protected void checkSession(NSession session) {
        NSessionUtils.checkSession(getWorkspace(), session);
    }

    @Override
    public boolean isAvailable(NSession session) {
        return isAvailable(false, session);
    }

    @Override
    public boolean isAvailable(boolean force, NSession session) {
        if (force) {
            return available.update(session);
        }
        return available.getValue(session);
    }

    @Override
    public boolean isSupportedDeploy(NSession session) {
        return isSupportedDeploy(false, session);
    }

    @Override
    public boolean isSupportedDeploy(boolean force, NSession session) {
        return supportsDeploy;
    }

    protected boolean isAvailableImpl(NSession session) {
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
    public NWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NRepository getParentRepository() {
        return parentRepository;
    }

    @Override
    public NRepositoryConfigManager config() {
        return new DefaultNRepoConfigManager(configModel);
    }

    @Override
    public NRepositorySecurityManager security() {
        return new DefaultNRepositorySecurityManager(securityModel);
    }

    @Override
    public NRepository removeRepositoryListener(NRepositoryListener listener) {
        repositoryListeners.add(listener);
        return this;
    }

    @Override
    public NRepository addRepositoryListener(NRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
        return this;
    }

    @Override
    public List<NRepositoryListener> getRepositoryListeners() {
        return repositoryListeners;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public NRepository addUserPropertyListener(NMapListener<String, Object> listener) {
        userProperties.addListener(listener);
        return this;
    }

    @Override
    public NRepository removeUserPropertyListener(NMapListener<String, Object> listener) {
        userProperties.removeListener(listener);
        return this;
    }

    @Override
    public List<NMapListener<String, Object>> getUserPropertyListeners() {
        return userProperties.getListeners();
    }

    public boolean isEnabled(NSession session) {
        return this.enabled && this.config().setSession(session).isEnabled();
    }

    public NRepository setEnabled(boolean enabled, NSession session) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public String toString() {
        NRepositoryConfigManagerExt cc = NRepositoryConfigManagerExt.of(config());
        NRepositoryConfigManager c = config();
        String name = getName();
        String storePath = null;
        NRepositoryLocation loc = cc.getModel().getLocation();
        String impl = getClass().getSimpleName();
        if (c != null) {
            NPath storeLocation = cc.getModel().getStoreLocation();
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

    protected String getIdExtension(NId id, NSession session) {
        return NLocations.of(session).getDefaultIdExtension(id);
    }

    public NPath getIdBasedir(NId id, NSession session) {
        return NLocations.of(session).getDefaultIdBasedir(id);
    }

    public String getIdFilename(NId id, NSession session) {
        return getIdFilename(id, getIdExtension(id, session), session);
    }

    public String getIdFilename(NId id, String ext, NSession session) {
        String classifier = "";
        if (!ext.equals(NConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!NBlankable.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }
}
