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
 * <br>
 * <p>
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

import java.util.Collection;
import java.util.List;

/**
 * Command class for adding users to workspaces and repositories. All Command
 * classes have a 'run' method to perform the operation.
 *
 * @author thevpc
 * @app.category Security
 * @see NutsWorkspaceSecurityManager#addUser(String)
 * @see NutsRepositorySecurityManager#addUser(String)
 * @since 0.5.5
 */
public interface NutsAddUserCommand extends NutsWorkspaceCommand {

    /**
     * add group named {@code group} to the specified user
     *
     * @param group group name
     * @return {@code this} instance
     */
    NutsAddUserCommand addGroup(String group);

    /**
     * add group list named {@code groups} to the specified user
     *
     * @param groups group list
     * @return {@code this} instance
     */
    NutsAddUserCommand addGroups(String... groups);

    /**
     * add group list named {@code groups} to the specified user
     *
     * @param groups group list
     * @return {@code this} instance
     */
    NutsAddUserCommand addGroups(Collection<String> groups);

    /**
     * add permission named {@code permission} to the specified user
     *
     * @param permission permission name from {@code NutsConstants.Permissions}
     * @return {@code this} instance
     */
    NutsAddUserCommand addPermission(String permission);

    /**
     * add permissions list named {@code permissions} to the specified user
     *
     * @param permissions group list
     * @return {@code this} instance
     */
    NutsAddUserCommand addPermissions(String... permissions);

    /**
     * add permissions list named {@code permissions} to the specified user
     *
     * @param permissions group list
     * @return {@code this} instance
     */
    NutsAddUserCommand addPermissions(Collection<String> permissions);

    /**
     * group list defined by {@link #addGroup}, @link {@link #addGroups(String...)}} and @link {@link #addGroups(Collection)}}
     *
     * @return group list
     */
    List<String> getGroups();

    /**
     * return username
     *
     * @return username
     */
    String getUsername();

    /**
     * set username
     *
     * @param username new value
     * @return {@code this} instance
     */
    NutsAddUserCommand setUsername(String username);

    /**
     * return credentials
     *
     * @return credentials
     */
    char[] getCredentials();

    /**
     * @param password new value
     * @return {@code this} instance
     */
    NutsAddUserCommand setCredentials(char[] password);

    /**
     * return remote identity
     *
     * @return remote identity
     */
    String getRemoteIdentity();

    /**
     * set remote identity
     *
     * @param remoteIdentity new value
     * @return {@code this} instance
     */
    NutsAddUserCommand setRemoteIdentity(String remoteIdentity);

    /**
     * return permissions
     *
     * @return permissions
     */
    List<String> getPermissions();

    /**
     * remove group
     *
     * @param groups new value
     * @return {@code this} instance
     */
    NutsAddUserCommand removeGroups(String... groups);

    /**
     * remove groups
     *
     * @param groups groups to remove
     * @return {@code this} instance
     */
    NutsAddUserCommand removeGroups(Collection<String> groups);

    /**
     * remove permissions
     *
     * @param permissions permission to remove
     * @return {@code this} instance
     */
    NutsAddUserCommand removePermissions(String... permissions);

    /**
     * remove permissions
     *
     * @param permissions permissions to remove
     * @return {@code this} instance
     */
    NutsAddUserCommand removePermissions(Collection<String> permissions);

    /**
     * remote credentials
     *
     * @return remote credentials
     */
    char[] getRemoteCredentials();

    /**
     * set remote credentials
     *
     * @param password new value
     * @return {@code this} instance
     */
    NutsAddUserCommand setRemoteCredentials(char[] password);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsAddUserCommand setSession(NutsSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NutsAddUserCommand copySession();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsAddUserCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsAddUserCommand run();

}
