/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts;

import java.nio.file.Path;
import java.util.Map;

/**
 * @category Base
 */
public interface NutsWorkspaceLocationManager {
    Path getHomeLocation(NutsStoreLocation folderType);

    Path getStoreLocation(NutsStoreLocation folderType);

    void setStoreLocation(NutsStoreLocation folderType, String location, NutsUpdateOptions options);

    void setStoreLocationStrategy(NutsStoreLocationStrategy strategy, NutsUpdateOptions options);

    void setStoreLocationLayout(NutsOsFamily layout, NutsUpdateOptions options);

    Path getStoreLocation(String id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsId id, NutsStoreLocation folderType);
    void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType, String location, NutsUpdateOptions options);
    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    NutsOsFamily getStoreLocationLayout();

    /**
     * all home locations key/value map where keys are in the form "location"
     * and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<String, String> getStoreLocations();

    /**
     * all home locations key/value map where keys are in the form
     * "osfamily:location" and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<String, String> getHomeLocations();

    Path getHomeLocation(NutsOsFamily layout, NutsStoreLocation location);

    Path getWorkspaceLocation();

    String getDefaultIdFilename(NutsId id);

    String getDefaultIdBasedir(NutsId id);

    String getDefaultIdContentExtension(String packaging);

    String getDefaultIdExtension(NutsId id);

}
