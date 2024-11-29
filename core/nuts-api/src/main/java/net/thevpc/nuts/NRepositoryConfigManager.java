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
package net.thevpc.nuts;

import net.thevpc.nuts.env.NSpeedQualifier;
import net.thevpc.nuts.env.NStoreStrategy;
import net.thevpc.nuts.env.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NRepositoryLocation;
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
public interface NRepositoryConfigManager{

    /**
     * global name is independent from workspace
     *
     * @return repository global (workspace independent) name
     */
    String getGlobalName();

    NRepositoryRef getRepositoryRef();

    String getType();

    String getGroups();

    NSpeedQualifier getSpeed();

    boolean isTemporary();

    boolean isPreview();

    Set<String> getTags();

    NRepositoryConfigManager setTemporary(boolean enabled);

    boolean isIndexSubscribed();


    NRepositoryLocation getLocation();

    NPath getLocationPath();

    /**
     * return repository configured location as string
     *
     * @return repository location path
     */
    NPath getStoreLocation();

    NPath getStoreLocation(NStoreType folderType);

    boolean isIndexEnabled();

    NRepositoryConfigManager setIndexEnabled(boolean enabled);

    NRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled);

    int getDeployWeight();

    boolean isEnabled();

    NRepositoryConfigManager setEnabled(boolean enabled);

    NRepositoryConfigManager subscribeIndex();

    NRepositoryConfigManager unsubscribeIndex();

    boolean isSupportedMirroring();

    NRepository findMirrorById(String repositoryNameOrId);

    NRepository findMirrorByName(String repositoryNameOrId);

    List<NRepository> getMirrors();

    /**
     * search for (or throw error) a repository with the given repository name
     * or id.
     *
     * @param repositoryIdOrName repository name or id
     * @return found repository or throw an exception
     * @throws NRepositoryNotFoundException if not found
     */
    NRepository getMirror(String repositoryIdOrName);

    /**
     * search for (or return null) a repository with the given repository name
     * or id.
     *
     * @param repositoryIdOrName repository name or id
     * @return found repository or return null
     */
    NRepository findMirror(String repositoryIdOrName);

    /**
     * add new repository
     *
     * @param options repository definition
     * @return {@code this} instance
     */
    NRepository addMirror(NAddRepositoryOptions options);

    /**
     * @param repositoryId repository id pr id
     * @return {@code this} instance
     */
    NRepositoryConfigManager removeMirror(String repositoryId);

    NStoreStrategy getStoreStrategy();

    Map<String, String> getConfigMap(boolean inherit);

    NOptional<NLiteral> getConfigProperty(String key, boolean inherit);

    Map<String, String> getConfigMap();

    NOptional<NLiteral> getConfigProperty(String property);

    NRepositoryConfigManager setConfigProperty(String property, String value);
}
