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
package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NSpeedQualifier;
import net.thevpc.nuts.NStoreStrategy;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.NUserConfig;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thevpc
 */
public interface NRepositoryConfigModel {

    boolean save(boolean force);

    NRepository getRepository();

    NWorkspace getWorkspace();

    void addMirror(NRepository repo);

    NRepository addMirror(NAddRepositoryOptions options);

    NRepository findMirror(String repositoryIdOrName);

    NRepository findMirrorById(String repositoryNameOrId);

    NRepository findMirrorByName(String repositoryNameOrId);

    int getDeployWeight();

    String getGlobalName();

    String getGroups();

    NPath getLocationPath();

    NRepositoryLocation getLocation();

    NRepository getMirror(String repositoryIdOrName);

    List<NRepository> getMirrors();

    String getName();

    NRepositoryRef getRepositoryRef();

    NSpeedQualifier getSpeed();

    NPath getStoreLocation();

    NPath getStoreLocation(NStoreType folderType);

    NStoreStrategy getStoreStrategy();

    String getType();

    String getUuid();

    boolean isEnabled();

    boolean isIndexEnabled();

    boolean isPreview();

    boolean isIndexSubscribed();

    boolean isSupportedMirroring();

    //        @Override
    //        public void setEnv(String property, String value, NutsUpdateOptions options) {
    //            //
    //        }
    boolean isTemporary();

    void removeMirror(String repositoryId);

    void setEnabled(boolean enabled);

    void setIndexEnabled(boolean enabled);

    void setMirrorEnabled(String repoName, boolean enabled);

    void setTemporary(boolean enabled);

    void subscribeIndex();

    void unsubscribeIndex();

    NPath getTempMirrorsRoot();

    NPath getMirrorsRoot();

    NUserConfig[] findUsers();

    NOptional<NUserConfig> findUser(String userId);

    NRepositoryConfig getStoredConfig();

    void fireConfigurationChanged(String configName);

    void setUser(NUserConfig user);

    void removeUser(String userId);

    NRepositoryConfig getConfig();

    Map<String, String> toMap(boolean inherit);

    Map<String, String> toMap();


    NOptional<NLiteral> get(String key, boolean inherit);

    void set(String property, String value);

    boolean containsTag(String tag);

    public Set<String> getTags() ;

    public void addTag(String tag) ;

    public void removeTag(String tag) ;

}
