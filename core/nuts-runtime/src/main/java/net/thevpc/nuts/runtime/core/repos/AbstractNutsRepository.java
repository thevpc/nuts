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
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.core.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.common.DefaultObservableMap;
import net.thevpc.nuts.runtime.core.common.ObservableMap;
import net.thevpc.nuts.runtime.main.repos.DefaultNutsRepositoryEnvManager;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepository implements NutsRepository {

    private static final long serialVersionUID = 1L;

    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<>();
    protected NutsRepository parentRepository;
    protected NutsWorkspace workspace;
    protected NutsRepositorySecurityManager securityManager;
    protected NutsRepositoryConfigManager configManager;
    protected ObservableMap<String, Object> userProperties;
    protected boolean enabled = true;
    protected DefaultNutsRepositoryEnvManager env;

    public AbstractNutsRepository() {
        userProperties = new DefaultObservableMap<>();
        env=new DefaultNutsRepositoryEnvManager(this);
    }

    @Override
    public NutsRepositoryEnvManager env() {
        return env;
    }

    @Override
    public String getRepositoryType() {
        return config().getType();
    }

    @Override
    public String getUuid() {
        return config().getUuid();
    }

    @Override
    public String getName() {
        return config().getName();
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
        return configManager;
    }

    @Override
    public NutsRepositorySecurityManager security() {
        return securityManager;
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
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public void addUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.addListener(listener);
    }

    @Override
    public void removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.removeListener(listener);
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
        NutsRepositoryConfigManager c = config();
        String name = getName();
        String storePath = null;
        String loc = config().getLocation(false);
        String impl = getClass().getSimpleName();
        if (c != null) {
            Path storeLocation = c.getStoreLocation();
            storePath = storeLocation == null ? null : storeLocation.toAbsolutePath().toString();
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

    protected String getIdExtension(NutsId id) {
        return getWorkspace().locations().getDefaultIdExtension(id);
    }

    public String getIdBasedir(NutsId id) {
        return getWorkspace().locations().getDefaultIdBasedir(id);
    }

    public String getIdFilename(NutsId id) {
        //return getWorkspace().locations().getDefaultIdFilename(id);
        String classifier = "";
        String ext = getIdExtension(id);
        if (!ext.equals(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!CoreStringUtils.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }

    protected void checkSession(NutsSession session) {
        if (session == null) {
            throw new NutsIllegalArgumentException(workspace, "Missing Session");
        }
    }
}
