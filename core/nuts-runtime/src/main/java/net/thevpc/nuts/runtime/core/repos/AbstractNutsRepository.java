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
package net.thevpc.nuts.runtime.core.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.common.DefaultObservableMap;
import net.thevpc.nuts.runtime.core.common.ObservableMap;
import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsRepositoryEnvManager;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.nio.file.Paths;
import java.util.*;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.util.CachedValue;
import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsRepoConfigManager;
import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsRepositoryEnvModel;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsRepositorySecurityManager;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsRepositorySecurityModel;

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
    protected boolean enabled = true;
    protected DefaultNutsRepositoryEnvModel envModel;
    protected NutsSession initSession;
    protected CachedValue<Boolean> available = new CachedValue<>(() -> isAvailableImpl(), 30);

    public AbstractNutsRepository() {
        userProperties = new DefaultObservableMap<>();
        envModel = new DefaultNutsRepositoryEnvModel(this);
        securityModel = new DefaultNutsRepositorySecurityModel(this);
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

    protected boolean isAvailableImpl() {
        return true;
    }

    @Override
    public NutsRepositoryEnvManager env() {
        return new DefaultNutsRepositoryEnvManager(envModel);
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
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
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
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return userProperties.getListeners();
    }

    public boolean isEnabled() {
        return enabled && config().isEnabled();
    }

    public NutsRepository setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public String toString() {
        NutsRepositoryConfigManagerExt cc = NutsRepositoryConfigManagerExt.of(config());
        NutsRepositoryConfigManager c = config();
        String name = getName();
        String storePath = null;
        String loc = cc.getModel().getLocation();
        String impl = getClass().getSimpleName();
        if (c != null) {
            String storeLocation = cc.getModel().getStoreLocation();
            storePath = storeLocation == null ? null : Paths.get(storeLocation).toAbsolutePath().toString();
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

    protected String getIdExtension(NutsId id, NutsSession session) {
        return getWorkspace().locations().setSession(session).getDefaultIdExtension(id);
    }

    public String getIdBasedir(NutsId id, NutsSession session) {
        return getWorkspace().locations().setSession(session).getDefaultIdBasedir(id);
    }

    public String getIdFilename(NutsId id, NutsSession session) {
        //return getWorkspace().locations().getDefaultIdFilename(id);
        String classifier = "";
        String ext = getIdExtension(id, session);
        if (!ext.equals(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!NutsBlankable.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }
}
