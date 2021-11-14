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

import java.nio.file.Path;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public interface NutsRepositoryConfigModel {

    boolean save(boolean force, NutsSession session);

    NutsRepository getRepository();

    NutsWorkspace getWorkspace();

    void addMirror(NutsRepository repo, NutsSession session);

    NutsRepository addMirror(NutsAddRepositoryOptions options, NutsSession session);

    NutsRepository findMirror(String repositoryIdOrName, NutsSession session);

    NutsRepository findMirrorById(String repositoryNameOrId, NutsSession session);

    NutsRepository findMirrorByName(String repositoryNameOrId, NutsSession session);

    int getDeployWeight(NutsSession session);

    String getGlobalName(NutsSession session);

    String getGroups(NutsSession session);

    NutsPath getLocation(boolean expand, NutsSession session);

    NutsRepository getMirror(String repositoryIdOrName, NutsSession session);

    NutsRepository[] getMirrors(NutsSession session);

    String getName();

    NutsRepositoryRef getRepositoryRef(NutsSession session);

    NutsSpeedQualifier getSpeed(NutsSession session);

    NutsPath getStoreLocation();

    NutsPath getStoreLocation(NutsStoreLocation folderType, NutsSession session);

    //        @Override
    //        public int getSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode fetchMode, boolean transitive) {
    //            return 0;
    //        }
    NutsStoreLocationStrategy getStoreLocationStrategy(NutsSession session);

    String getType(NutsSession session);

    String getUuid();

    String getLocation();

    boolean isEnabled(NutsSession session);

    boolean isIndexEnabled(NutsSession session);

    boolean isIndexSubscribed(NutsSession session);

    boolean isSupportedMirroring(NutsSession session);

    //        @Override
    //        public void setEnv(String property, String value, NutsUpdateOptions options) {
    //            //
    //        }
    boolean isTemporary(NutsSession session);

    void removeMirror(String repositoryId, NutsSession session);

    void setEnabled(boolean enabled, NutsSession session);

    void setIndexEnabled(boolean enabled, NutsSession session);

    void setMirrorEnabled(String repoName, boolean enabled, NutsSession session);

    void setTemporary(boolean enabled, NutsSession session);

    void subscribeIndex(NutsSession session);

    void unsubscribeIndex(NutsSession session);

    NutsPath getTempMirrorsRoot(NutsSession session);

    NutsPath getMirrorsRoot(NutsSession session);

    NutsUserConfig[] getUsers(NutsSession session);

    NutsUserConfig getUser(String userId, NutsSession session);

    NutsRepositoryConfig getStoredConfig(NutsSession session);

    void fireConfigurationChanged(String configName, NutsSession session);

    void setUser(NutsUserConfig user, NutsSession session);

    void removeUser(String userId, NutsSession session);

    NutsRepositoryConfig getConfig(NutsSession session);

    Map<String, String> toMap(boolean inherit, NutsSession session);

    Map<String, String> toMap(NutsSession session);


    String get(String key, String defaultValue, boolean inherit, NutsSession session);

    String get(String property, String defaultValue, NutsSession session);

    void set(String property, String value, NutsSession session);


}
