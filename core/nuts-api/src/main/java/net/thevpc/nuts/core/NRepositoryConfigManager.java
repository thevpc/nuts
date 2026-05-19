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

    @NGetter
    NRepositoryRef repositoryRef();

    @NGetter
    String type();

    @NGetter
    String groups();

    @NGetter
    NSpeedQualifier speed();

    boolean isTemporary();

    boolean isPreview();

    @NGetter
    Set<String> tags();

    @NSetter
    NRepositoryConfigManager temporary(boolean enabled);

    boolean isIndexSubscribed();


    @NGetter
    NRepositoryLocation location();

    @NGetter
    NPath locationPath();

    /**
     * return repository configured location as string
     *
     * @return repository location path
     */
    @NGetter
    NPath storeLocation();

    NPath getStoreLocation(NStoreType folderType);

    @NGetter
    boolean isIndexEnabled();

    @NSetter
    NRepositoryConfigManager indexEnabled(boolean enabled);

    NRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled);

    @NGetter
    int deployWeight();

    @NGetter
    boolean isEnabled();

    @NSetter
    NRepositoryConfigManager enabled(boolean enabled);

    NRepositoryConfigManager subscribeIndex();

    NRepositoryConfigManager unsubscribeIndex();

    boolean isSupportedMirroring();

    NRepository findMirrorById(String repositoryNameOrId);

    NRepository findMirrorByName(String repositoryNameOrId);

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

    @NGetter
    NStoreStrategy storeStrategy();

    Map<String, String> getConfigMap(boolean inherit);

    NOptional<NLiteral> getConfigProperty(String key, boolean inherit);

    @NGetter
    Map<String, String> configMap();

    NOptional<NLiteral> getConfigProperty(String property);

    NRepositoryConfigManager setConfigProperty(String property, String value);
}
