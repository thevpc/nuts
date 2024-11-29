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
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.env.NLocations;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepoConfigManager;
import net.thevpc.nuts.runtime.standalone.repository.config.NRepositoryConfigModel;
import net.thevpc.nuts.runtime.standalone.util.NCachedValue;
import net.thevpc.nuts.util.NDefaultObservableMap;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.spi.NRepositorySPI;

import java.util.*;

import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.security.DefaultNRepositorySecurityManager;
import net.thevpc.nuts.runtime.standalone.security.DefaultNRepositorySecurityModel;

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
    protected NObservableMap<String, Object> userProperties;
    protected NCachedValue<Boolean> available;
    protected boolean supportsDeploy;
    protected boolean enabled = true;

    public AbstractNRepository(NWorkspace workspace) {
        this.workspace=workspace;
        this.userProperties = new NDefaultObservableMap<>();
        this.securityModel = new DefaultNRepositorySecurityModel(this);
        this.available = new NCachedValue<>(workspace, () -> isAvailableImpl(), 0);;
    }

    public boolean isPreview() {
        return configModel.containsTag(NConstants.RepoTags.PREVIEW);
    }

    @Override
    public boolean containsTags(String tag) {
        return configModel.containsTag(NStringUtils.trim(tag));
    }

    @Override
    public Set<String> getTags() {
        return configModel.getTags();
    }

    @Override
    public NRepository addTag(String tag) {
        configModel.addTag(NStringUtils.trim(tag));
        return this;
    }

    @Override
    public NRepository removeTag(String tag) {
        this.configModel.removeTag(tag);
        return this;
    }

    @Override
    public boolean isAvailable() {
        return isAvailable(false);
    }

    @Override
    public boolean isAvailable(boolean force) {
        if (force) {
            return available.update();
        }
        return available.getValue();
    }

    @Override
    public boolean isSupportedDeploy() {
        return isSupportedDeploy(false);
    }

    @Override
    public boolean isSupportedDeploy(boolean force) {
        return supportsDeploy;
    }

    protected boolean isAvailableImpl() {
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
    public NRepository addUserPropertyListener(NObservableMapListener<String, Object> listener) {
        userProperties.addMapListener(listener);
        return this;
    }

    @Override
    public NRepository removeUserPropertyListener(NObservableMapListener<String, Object> listener) {
        userProperties.removeMapListener(listener);
        return this;
    }

    @Override
    public List<NObservableMapListener<String, Object>> getUserPropertyListeners() {
        return userProperties.getMapListeners();
    }

    public boolean isEnabled() {
        return this.enabled && this.config().isEnabled();
    }

    public NRepository setEnabled(boolean enabled) {
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

    protected String getIdExtension(NId id) {
        return NLocations.of().getDefaultIdExtension(id);
    }

    public NPath getIdBasedir(NId id) {
        return NLocations.of().getDefaultIdBasedir(id);
    }

    public String getIdFilename(NId id) {
        return getIdFilename(id, getIdExtension(id));
    }

    public String getIdFilename(NId id, String ext) {
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
