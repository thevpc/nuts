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
 *
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
package net.thevpc.nuts;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.env.NOsFamily;

import java.util.Map;

/**
 * @author thevpc
 * @app.category Base
 */
public interface NLocations extends NComponent, NSessionProvider {
    static NLocations of(NSession session) {
        return NExtensions.of(session).createComponent(NLocations.class).get();
    }

    NPath getHomeLocation(NStoreType folderType);

    NPath getStoreLocation(NStoreType folderType);

    NPath getStoreLocation(NId id, NStoreType folderType);

    NPath getStoreLocation(NStoreType folderType, String repositoryIdOrName);

    NPath getStoreLocation(NId id, NStoreType folderType, String repositoryIdOrName);

    NStoreStrategy getStoreStrategy();

    NLocations setStoreStrategy(NStoreStrategy strategy);

    NStoreStrategy getRepositoryStoreStrategy();

    NOsFamily getStoreLayout();

    NLocations setStoreLayout(NOsFamily storeLayout);

    /**
     * all home locations key/value map where keys are in the form "location"
     * and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<NStoreType, String> getStoreLocations();

    String getDefaultIdFilename(NId id);

    NPath getDefaultIdBasedir(NId id);

    String getDefaultIdContentExtension(String packaging);

    String getDefaultIdExtension(NId id);

    /**
     * all home locations key/value map where keys are in the form
     * "osfamily:location" and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<NHomeLocation, String> getHomeLocations();

    NPath getHomeLocation(NHomeLocation location);

    NPath getWorkspaceLocation();

    NLocations setStoreLocation(NStoreType folderType, String location);

    NLocations setHomeLocation(NHomeLocation homeType, String location);

    NLocations setSession(NSession session);

}
