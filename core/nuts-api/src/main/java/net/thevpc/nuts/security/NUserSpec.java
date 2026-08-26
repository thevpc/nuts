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
public interface NUserSpec {

    /**
     * Creates a new instance of of.
     *
     * @param userName user name
     * @return of result
     */
    static NUserSpec of(String userName) {
        return NSecurityManager.of().createUserUpdateQuery(userName);
    }

    /**
     * return user name
     *
     * @return return user name
     */
    String userName();

    /**
     * Credential.
     *
     * @return credential result
     */
    NSecureString credential();

    /**
     * Old credential.
     *
     * @return old credential result
     */
    NSecureString oldCredential();

    /**
     * Credential.
     *
     * @param value value
     * @return credential result
     */
    NUserSpec credential(NSecureString value);

    /**
     * Old credential.
     *
     * @param value value
     * @return old credential result
     */
    NUserSpec oldCredential(NSecureString value);

    /**
     * Adds the specified permissions.
     *
     * @param value value
     * @return add permissions result
     */
    NUserSpec addPermissions(String... value);

    /**
     * Removes the specified permissions.
     *
     * @param value value
     * @return remove permissions result
     */
    NUserSpec removePermissions(String... value);

    /**
     * Permissions.
     *
     * @param value value
     * @return permissions result
     */
    NUserSpec permissions(List<String> value);

    /**
     * Adds the specified groups.
     *
     * @param value value
     * @return add groups result
     */
    NUserSpec addGroups(String... value);

    /**
     * Removes the specified groups.
     *
     * @param value value
     * @return remove groups result
     */
    NUserSpec removeGroups(String... value);

    /**
     * Groups.
     *
     * @param value value
     * @return groups result
     */
    NUserSpec groups(List<String> value);


    /**
     * user allowed permissions
     *
     * @return user allowed permissions
     */
    List<String> permissions();

    /**
     * user groups
     *
     * @return user groups
     */
    List<String> groups();

}
