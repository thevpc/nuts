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
package net.thevpc.nuts;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.spi.NComponent;

import java.util.List;

/**
 * Workspace Security configuration manager
 *
 * @author thevpc
 * @app.category Security
 * @since 0.5.4
 */
public interface NWorkspaceSecurityManager extends NComponent {
    static NWorkspaceSecurityManager of() {
        return NExtensions.of(NWorkspaceSecurityManager.class);
    }

    /**
     * current user
     *
     * @return current user
     */
    String getCurrentUsername();

    /**
     * current user stack.
     * this is useful when login with multiple user identities.
     *
     * @return current user stack
     */
    String[] getCurrentLoginStack();

    /**
     * impersonate user and log as a distinct user with the given credentials.
     *
     * @param username user name
     * @param password user password
     * @return {@code this} instance
     */
    NWorkspaceSecurityManager login(String username, char[] password);

    /**
     * log out from last logged in user (if any) and pop out from user name stack.
     *
     * @return {@code this} instance
     */
    NWorkspaceSecurityManager logout();

    /**
     * create a User Create command.
     * No user will be added when simply calling this method.
     * You must fill in command parameters then call {@link NAddUserCmd#run()}.
     *
     * @param name user name
     * @return create add user command.
     */
    NAddUserCmd addUser(String name);

    /**
     * create a Update Create command.
     * No user will be updated when simply calling this method.
     * You must fill in command parameters then call {@link NUpdateUserCmd#run()}.
     *
     * @param name user name
     * @return create update user command.
     */
    NUpdateUserCmd updateUser(String name);

    /**
     * create a Remove Create command.
     * No user will be removed when simply calling this method.
     * You must fill in command parameters then call {@link NRemoveUserCmd#run()}.
     *
     * @param name user name
     * @return create remove user command.
     */
    NRemoveUserCmd removeUser(String name);

    /**
     * find all registered users
     *
     * @return all registered users
     */
    List<NUser> findUsers();

    /**
     * find user with the given name or null.
     *
     * @param username user name
     * @return user effective information
     */
    NUser findUser(String username);

    /**
     * return true if permission is valid and allowed for the current user.
     *
     * @param permission permission name. see {@code NutsConstants.Rights } class
     * @return true if permission is valid and allowed for the current user
     */
    boolean isAllowed(String permission);

    /**
     * check if allowed and throw a Security exception if not.
     *
     * @param permission    permission name. see {@code NutsConstants.Rights } class
     * @param operationName operation name
     * @return {@code this} instance
     */
    NWorkspaceSecurityManager checkAllowed(String permission, String operationName);

    /**
     * switch from/to secure mode.
     * when secure mode is disabled, no authorizations are checked against.
     *
     * @param secure        true if secure mode
     * @param adminPassword password for admin user
     * @return true if mode was switched correctly
     * @since 0.5.7
     */
    boolean setSecureMode(boolean secure, char[] adminPassword);

    /**
     * return true if current user has admin privileges
     *
     * @return true if current user has admin privileges
     */
    boolean isAdmin();

    boolean isAnonymous();

    /**
     * update default authentication agent.
     *
     * @param authenticationAgentId authentication agent id
     * @return {@code this} instance
     */
    NWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgentId);

    /**
     * get authentication agent with id {@code authenticationAgentId}.
     * if is blank, return default authentication agent
     *
     * @param authenticationAgentId agent id
     * @return authentication agent
     */
    NAuthenticationAgent getAuthenticationAgent(String authenticationAgentId);

    /**
     * return true if workspace is running secure mode
     *
     * @return true if workspace is running secure mode
     */
    boolean isSecureMode();

    /**
     * check if the given <code>password</code> is valid against the one stored
     * by the Authentication Agent for  <code>credentialsId</code>
     *
     * @param credentialsId credentialsId
     * @param password      password
     * @return this
     * @throws NSecurityException when check failed
     */
    NWorkspaceSecurityManager checkCredentials(char[] credentialsId, char[] password) throws NSecurityException;

    /**
     * get the credentials for the given id. The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @return credentials
     */
    char[] getCredentials(char[] credentialsId);

    /**
     * remove existing credentials with the given id The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @return credentials
     */
    boolean removeCredentials(char[] credentialsId);

    /**
     * store credentials in the agent's and return the credential id to store
     * into the config. if credentialId is not null, the given credentialId will
     * be updated and the credentialId is returned. The {@code credentialsId},if
     * present or returned, <strong>MUST</strong> be prefixed with
     * AuthenticationAgent's id and ':' character
     *
     * @param credentials   credential
     * @param allowRetrieve when true {@link #getCredentials(char[])} can be
     *                      invoked over {@code credentialId}
     * @param credentialId  preferred credentialId, if null, a new one is created
     * @return credentials-id
     */
    char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId);

}
