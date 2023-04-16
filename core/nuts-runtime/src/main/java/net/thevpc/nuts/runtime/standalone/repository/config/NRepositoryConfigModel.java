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
package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NRepositoryLocation;

import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public interface NRepositoryConfigModel {

    boolean save(boolean force, NSession session);

    NRepository getRepository();

    NWorkspace getWorkspace();

    void addMirror(NRepository repo, NSession session);

    NRepository addMirror(NAddRepositoryOptions options, NSession session);

    NRepository findMirror(String repositoryIdOrName, NSession session);

    NRepository findMirrorById(String repositoryNameOrId, NSession session);

    NRepository findMirrorByName(String repositoryNameOrId, NSession session);

    int getDeployWeight(NSession session);

    String getGlobalName(NSession session);

    String getGroups(NSession session);

    NPath getLocationPath(NSession session);

    NRepositoryLocation getLocation(NSession session);

    NRepository getMirror(String repositoryIdOrName, NSession session);

    List<NRepository> getMirrors(NSession session);

    String getName();

    NRepositoryRef getRepositoryRef(NSession session);

    NSpeedQualifier getSpeed(NSession session);

    NPath getStoreLocation();

    NPath getStoreLocation(NStoreType folderType, NSession session);

    //        @Override
    //        public int getSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode fetchMode, boolean transitive) {
    //            return 0;
    //        }
    NStoreStrategy getStoreStrategy(NSession session);

    String getType(NSession session);

    String getUuid();

    NRepositoryLocation getLocation();

    boolean isEnabled(NSession session);

    boolean isIndexEnabled(NSession session);

    boolean isIndexSubscribed(NSession session);

    boolean isSupportedMirroring(NSession session);

    //        @Override
    //        public void setEnv(String property, String value, NutsUpdateOptions options) {
    //            //
    //        }
    boolean isTemporary(NSession session);

    void removeMirror(String repositoryId, NSession session);

    void setEnabled(boolean enabled, NSession session);

    void setIndexEnabled(boolean enabled, NSession session);

    void setMirrorEnabled(String repoName, boolean enabled, NSession session);

    void setTemporary(boolean enabled, NSession session);

    void subscribeIndex(NSession session);

    void unsubscribeIndex(NSession session);

    NPath getTempMirrorsRoot(NSession session);

    NPath getMirrorsRoot(NSession session);

    NUserConfig[] getUsers(NSession session);

    NUserConfig getUser(String userId, NSession session);

    NRepositoryConfig getStoredConfig(NSession session);

    void fireConfigurationChanged(String configName, NSession session);

    void setUser(NUserConfig user, NSession session);

    void removeUser(String userId, NSession session);

    NRepositoryConfig getConfig(NSession session);

    Map<String, String> toMap(boolean inherit, NSession session);

    Map<String, String> toMap(NSession session);


    NOptional<NLiteral> get(String key, boolean inherit, NSession session);

    void set(String property, String value, NSession session);


}
