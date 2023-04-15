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
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NComponent;

import java.util.List;

/**
 * @app.category Base
 */
public interface NRepositories extends NComponent, NSessionProvider {
    static NRepositories of(NSession session) {
        return NExtensions.of(session).createComponent(NRepositories.class).get();
    }

    NRepositoryFilters filter();

    NRepository addRepository(NAddRepositoryOptions options);

    /**
     * creates a new repository from the given
     * {@code repositoryNamedUrl}.Accepted {@code repositoryNamedUrl} values are
     * :
     * <ul>
     * <li>'local' : corresponds to a local updatable repository.
     * <p>
     * will be named 'local'</li>
     * <li>'m2', '.m2', 'maven-local' : corresponds the local maven folder
     * repository. will be named 'local'</li>
     * <li>'maven-central': corresponds the remote maven central repository.
     * will be named 'local'</li>
     * <li>'maven-git', 'vpc-public-maven': corresponds the remote maven
     * vpc-public-maven git folder repository. will be named 'local'</li>
     * <li>'maven-git', 'nuts-public': corresponds the remote nuts
     * nuts-public git folder repository. will be named 'local'</li>
     * <li>name=uri-or-path : corresponds the given uri. will be named name.
     * Here are some examples:
     * <ul>
     * <li>myremote=http://192.168.6.3/folder</li>
     * <li>myremote=/folder/subfolder</li>
     * <li>myremote=c:/folder/subfolder</li>
     * </ul>
     * </li>
     * <li>uri-or-path : corresponds the given uri. will be named uri's last
     * path package name. Here are some examples:
     * <ul>
     * <li>http://192.168.6.3/folder : will be named 'folder'</li>
     * <li>myremote=/folder/subfolder : will be named 'folder'</li>
     * <li>myremote=c:/folder/subfolder : will be named 'folder'</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param repositoryNamedUrl repositoryNamedUrl
     * @return created repository
     */
    NRepository addRepository(String repositoryNamedUrl);

    NRepository findRepositoryById(String repositoryIdOrName);

    NRepository findRepositoryByName(String repositoryIdOrName);

    /**
     * @param repositoryIdOrName repository id or name
     * @return null if not found
     */
    NRepository findRepository(String repositoryIdOrName);

    NRepository getRepository(String repositoryIdOrName) throws NRepositoryNotFoundException;

    NRepositories removeRepository(String locationOrRepositoryId);

    List<NRepository> getRepositories();

    ///////////////
    NRepositories removeAllRepositories();

    NRepositories setSession(NSession session);

}
