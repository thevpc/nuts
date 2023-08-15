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
package net.thevpc.nuts;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public interface NConfigs extends NComponent,NSessionProvider {
    static NConfigs of(NSession session) {
        return NExtensions.of(session).createComponent(NConfigs.class).get();
    }

    NWorkspaceStoredConfig stored();

    boolean isReadOnly();

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force when true, save will always be performed
     * @return true if the save action was applied
     */
    boolean save(boolean force);

    boolean save();

    NWorkspaceBootConfig loadBootConfig(String path, boolean global, boolean followLinks);

    boolean isSupportedRepositoryType(String repositoryType);

    List<NAddRepositoryOptions> getDefaultRepositories();

    Set<String> getAvailableArchetypes();

    NPath resolveRepositoryPath(String repositoryLocation);

    NIndexStoreFactory getIndexStoreClientFactory();

    String getJavaCommand();

    String getJavaOptions();

    boolean isSystemWorkspace();

    NConfigs setSession(NSession session);

    Map<String, String> getConfigMap();

    NOptional<NLiteral> getConfigProperty(String property);

    /**
     * @param property property
     * @param value    value
     * @return {@code this} instance
     */
    NConfigs setConfigProperty(String property, String value);
}
