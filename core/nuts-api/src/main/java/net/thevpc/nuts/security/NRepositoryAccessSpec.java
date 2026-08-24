/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.security;

import java.util.List;

/**
 * Effective (including inherited) user information
 *
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public interface NRepositoryAccessSpec {

    /**
     * Creates a new instance of of.
     *
     * @param userName user name
     * @param repository repository
     * @return of result
     */
    static NRepositoryAccessSpec of(String userName, String repository) {
        return NSecurityManager.of().createRepositoryAccessSpec(userName, repository);
    }

    /**
     * User name.
     *
     * @return user name result
     */
    String userName();

    /**
     * Repository.
     *
     * @return repository result
     */
    String repository();

    /**
     * Remote user name.
     *
     * @return remote user name result
     */
    String remoteUserName();

    /**
     * Remote credential.
     *
     * @return remote credential result
     */
    NSecureString remoteCredential();

    /**
     * Remote auth type.
     *
     * @return remote auth type result
     */
    String remoteAuthType();

    /**
     * Permissions.
     *
     * @return permissions result
     */
    List<String> permissions();

    /**
     * Remote user name.
     *
     * @param remoteUserName remote user name
     * @return remote user name result
     */
    NRepositoryAccessSpec remoteUserName(String remoteUserName);

    /**
     * Remote credential.
     *
     * @param remoteCredential remote credential
     * @return remote credential result
     */
    NRepositoryAccessSpec remoteCredential(NSecureString remoteCredential);

    /**
     * Remote auth type.
     *
     * @param remoteAuthType remote auth type
     * @return remote auth type result
     */
    NRepositoryAccessSpec remoteAuthType(String remoteAuthType);

    /**
     * Adds the specified permissions.
     *
     * @param permissions permissions
     * @return add permissions result
     */
    NRepositoryAccessSpec addPermissions(String... permissions);

    /**
     * Removes the specified permissions.
     *
     * @param permissions permissions
     * @return remove permissions result
     */
    NRepositoryAccessSpec removePermissions(String... permissions);

    /**
     * Permissions.
     *
     * @param permissions permissions
     * @return permissions result
     */
    NRepositoryAccessSpec permissions(List<String> permissions);


}
