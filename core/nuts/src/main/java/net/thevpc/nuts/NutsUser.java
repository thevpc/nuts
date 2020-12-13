/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

/**
 * Effective (including inherited) user information
 * @author thevpc
 * @since 0.5.4
 * %category Config
 */
public interface NutsUser {

    /**
     * return remote identity if applicable
     * @return remote identity if applicable
     */
    String getRemoteIdentity();

    /**
     * return user name
     * @return return user name
     */
    String getUser();

    /**
     *  true if the use has some credentials
     * @return true if the use has some credentials
     */
    boolean hasCredentials();

    /**
     * user allowed permissions
     * @return user allowed permissions
     */
    String[] getPermissions();

    /**
     * user inherited allowed permissions
     * @return user inherited allowed permissions
     */
    String[] getInheritedPermissions();

    /**
     * user groups
     * @return user groups
     */
    String[] getGroups();
}
