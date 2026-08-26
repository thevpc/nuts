/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.core;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

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
    @NGetter
    String globalName();

    /**
     * Repository ref.
     *
     * @return repository ref result
     */
    @NGetter
    NRepositoryRef repositoryRef();

    /**
     * Type.
     *
     * @return type result
     */
    @NGetter
    String type();

    /**
     * Groups.
     *
     * @return groups result
     */
    @NGetter
    String groups();

    /**
     * Speed.
     *
     * @return speed result
     */
    @NGetter
    NSpeedQualifier speed();

    /**
     * Checks if is temporary.
     *
     * @return is temporary result
     */
    boolean isTemporary();

    /**
     * Checks if is preview.
     *
     * @return is preview result
     */
    boolean isPreview();

    /**
     * Tags.
     *
     * @return tags result
     */
    @NGetter
    Set<String> tags();

    /**
     * Temporary.
     *
     * @param enabled enabled
     * @return temporary result
     */
    @NSetter
    NRepositoryConfigManager temporary(boolean enabled);

    /**
     * Checks if is index subscribed.
     *
     * @return is index subscribed result
     */
    boolean isIndexSubscribed();


    /**
     * Location.
     *
     * @return location result
     */
    @NGetter
    NRepositoryLocation location();

    /**
     * Location path.
     *
     * @return location path result
     */
    @NGetter
    NPath locationPath();

    /**
     * return repository configured location as string
     *
     * @return repository location path
     */
    @NGetter
    NPath storeLocation();

    /**
     * Returns the store location.
     *
     * @param folderType folder type
     * @return get store location result
     */
    NPath getStoreLocation(NStoreType folderType);

    /**
     * Checks if is index enabled.
     *
     * @return is index enabled result
     */
    @NGetter
    boolean isIndexEnabled();

    /**
     * Index enabled.
     *
     * @param enabled enabled
     * @return index enabled result
     */
    @NSetter
    NRepositoryConfigManager indexEnabled(boolean enabled);

    /**
     * Sets the mirror enabled.
     *
     * @param repoName repo name
     * @param enabled enabled
     * @return set mirror enabled result
     */
    NRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled);

    /**
     * Deploy weight.
     *
     * @return deploy weight result
     */
    @NGetter
    int deployWeight();

    /**
     * Checks if is enabled.
     *
     * @return is enabled result
     */
    @NGetter
    boolean isEnabled();

    /**
     * Enabled.
     *
     * @param enabled enabled
     * @return enabled result
     */
    @NSetter
    NRepositoryConfigManager enabled(boolean enabled);

    /**
     * Subscribe index.
     *
     * @return subscribe index result
     */
    NRepositoryConfigManager subscribeIndex();

    /**
     * Unsubscribe index.
     *
     * @return unsubscribe index result
     */
    NRepositoryConfigManager unsubscribeIndex();

    /**
     * Checks if is supported mirroring.
     *
     * @return is supported mirroring result
     */
    boolean isSupportedMirroring();

    /**
     * Finds the find mirror by id.
     *
     * @param repositoryNameOrId repository name or id
     * @return find mirror by id result
     */
    NRepository findMirrorById(String repositoryNameOrId);

    /**
     * Finds the find mirror by name.
     *
     * @param repositoryNameOrId repository name or id
     * @return find mirror by name result
     */
    NRepository findMirrorByName(String repositoryNameOrId);

    /**
     * Mirrors.
     *
     * @return mirrors result
     */
    @NGetter
    List<NRepository> mirrors();

    /**
     * search for (or throw error) a repository with the given repository name
     * or id.
     *
     * @param repositoryIdOrName repository name or id
     * @return found repository or throw an exception
     */
    NOptional<NRepository> getMirror(String repositoryIdOrName);

    /**
     * add new repository
     *
     * @param options repository definition
     * @return {@code this} instance
     */
    NRepository addMirror(NRepositorySpec options);

    /**
     * @param repositoryId repository id pr id
     * @return {@code this} instance
     */
    NRepositoryConfigManager removeMirror(String repositoryId);

    /**
     * Store strategy.
     *
     * @return store strategy result
     */
    @NGetter
    NStoreStrategy storeStrategy();

    /**
     * Returns the config map.
     *
     * @param inherit inherit
     * @return get config map result
     */
    Map<String, String> getConfigMap(boolean inherit);

    /**
     * Returns the config property.
     *
     * @param key key
     * @param inherit inherit
     * @return get config property result
     */
    NOptional<NLiteral> getConfigProperty(String key, boolean inherit);

    /**
     * Config map.
     *
     * @return config map result
     */
    @NGetter
    Map<String, String> configMap();

    /**
     * Returns the config property.
     *
     * @param property property
     * @return get config property result
     */
    NOptional<NLiteral> getConfigProperty(String property);

    /**
     * Sets the config property.
     *
     * @param property property
     * @param value value
     * @return set config property result
     */
    NRepositoryConfigManager setConfigProperty(String property, String value);
}
