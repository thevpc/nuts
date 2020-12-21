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

import javax.security.auth.callback.CallbackHandler;

/**
 * Workspace Security configuration manager
 * @author thevpc
 * @since 0.5.4
 * %category Security
 */
public interface NutsWorkspaceSecurityManager {

    /**
     * current user
     * @return current user
     * @param session session
     */
    String getCurrentUsername(NutsSession session);

    /**
     * current user stack.
     * this is useful when login with multiple user identities.
     * @return current user stack
     * @param session session
     */
    String[] getCurrentLoginStack(NutsSession session);

    /**
     * impersonate user and log as a distinct user with the given credentials.
     * @param username user name
     * @param password user password
     * @param session session
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager login(String username, char[] password, NutsSession session);

    /**
     * impersonate user and log as a distinct user with the given credentials and stack
     * user name so that it can be retrieved using @{code getCurrentLoginStack()}.
     * @param handler security handler
     * @param session session
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager login(CallbackHandler handler, NutsSession session);

    /**
     * log out from last logged in user (if any) and pop out from user name stack.
     * @return {@code this} instance
     * @param session session
     */
    NutsWorkspaceSecurityManager logout(NutsSession session);

    /**
     * create a User Create command.
     * No user will be added when simply calling this method.
     * You must fill in command parameters then call {@link NutsAddUserCommand#run()}.
     * @param name user name
     * @param session session
     * @return create add user command.
     */
    NutsAddUserCommand addUser(String name, NutsSession session);

    /**
     * create a Update Create command.
     * No user will be updated when simply calling this method.
     * You must fill in command parameters then call {@link NutsUpdateUserCommand#run()}.
     * @param name user name
     * @param session session
     * @return create update user command.
     */
    NutsUpdateUserCommand updateUser(String name, NutsSession session);

    /**
     * create a Remove Create command.
     * No user will be removed when simply calling this method.
     * You must fill in command parameters then call {@link NutsRemoveUserCommand#run()}.
     * @param name user name
     * @param session session
     * @return create remove user command.
     */
    NutsRemoveUserCommand removeUser(String name, NutsSession session);

    /**
     * find all registered users
     * @return all registered users
     * @param session session
     */
    NutsUser[] findUsers(NutsSession session);

    /**
     * find user with the given name or null.
     * @param username user name
     * @param session session
     * @return user effective information
     */
    NutsUser findUser(String username, NutsSession session);

    /**
     * return true if permission is valid and allowed for the current user.
     * @param permission permission name. see {@code NutsConstants.Rights } class
     * @param session session
     * @return true if permission is valid and allowed for the current user
     */
    boolean isAllowed(String permission, NutsSession session);

    /**
     * check if allowed and throw a Security exception if not.
     * @param permission permission name. see {@code NutsConstants.Rights } class
     * @param operationName operation name
     * @param session session
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager checkAllowed(String permission, String operationName, NutsSession session);

    /**
     * switch from/to secure mode.
     * when secure mode is disabled, no authorizations are checked against.
     * @param secure true if secure mode
     * @param adminPassword password for admin user
     * @param options update options
     * @return true if mode was switched correctly
     * @since 0.5.7
     */
    boolean setSecureMode(boolean secure, char[] adminPassword, NutsUpdateOptions options);

    /**
     * return true if current user has admin privileges
     *
     * @return true if current user has admin privileges
     * @param session session
     */
    boolean isAdmin(NutsSession session);

    /**
     * update default authentication agent.
     * @param authenticationAgentId  authentication agent id
     * @param options update options
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgentId, NutsUpdateOptions options);

    /**
     * get authentication agent with id {@code authenticationAgentId}.
     * if is blank, return default authentication agent
     * @param authenticationAgentId agent id
     * @param session session
     * @return authentication agent
     */
    NutsAuthenticationAgent getAuthenticationAgent(String authenticationAgentId, NutsSession session);

    /**
     * return true if workspace is running secure mode
     *
     * @return true if workspace is running secure mode
     * @param session session
     */
    boolean isSecure(NutsSession session);

    /**
     * check if the given <code>password</code> is valid against the one stored
     * by the Authentication Agent for  <code>credentialsId</code>
     *
     * @param credentialsId credentialsId
     * @param password password
     * @param session session
     * @throws NutsSecurityException when check failed
     */
    void checkCredentials(char[] credentialsId, char[] password, NutsSession session) throws NutsSecurityException;

    /**
     * get the credentials for the given id. The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param session session
     * @return credentials
     */
    char[] getCredentials(char[] credentialsId, NutsSession session);

    /**
     * remove existing credentials with the given id The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param session session
     * @return credentials
     */
    boolean removeCredentials(char[] credentialsId, NutsSession session);

    /**
     * store credentials in the agent's and return the credential id to store
     * into the config. if credentialId is not null, the given credentialId will
     * be updated and the credentialId is returned. The {@code credentialsId},if
     * present or returned, <strong>MUST</strong> be prefixed with
     * AuthenticationAgent's id and ':' character
     *
     * @param credentials credential
     * @param allowRetrieve when true {@link #getCredentials(char[], NutsSession)} can be
     * invoked over {@code credentialId}
     * @param credentialId preferred credentialId, if null, a new one is created
     * @param session session
     * @return credentials-id
     */
    char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NutsSession session);

}
