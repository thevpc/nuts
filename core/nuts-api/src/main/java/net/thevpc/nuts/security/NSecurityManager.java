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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * Workspace Security configuration manager
 *
 * @author thevpc
 * @app.category Security
 * @since 0.5.4
 */
public interface NSecurityManager extends NComponent {
    static NSecurityManager of() {
        return NExtensions.of(NSecurityManager.class);
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
    NSecurityManager login(String username, NSecureString password);

    /**
     * log out from last logged in user (if any) and pop out from user name stack.
     *
     * @return {@code this} instance
     */
    NSecurityManager logout();

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
    NOptional<NUser> findUser(String username);

    NSecurityManager addUser(NUserSpec query);

    NSecurityManager updateUser(NUserSpec query);

    NOptional<NRepositoryAccess> findRepositoryAccess(String user, String repository);

    List<NRepositoryAccess> findRepositoryAccess();

    List<NRepositoryAccess> findRepositoryAccessByRepository(String repository);

    List<NRepositoryAccess> findRepositoryAccessByUser(String user);

    NSecurityManager updateRepositoryAccess(NRepositoryAccessSpec repositoryAccess);

    /**
     * return true if permission is valid and allowed for the current user.
     *
     * @param permission permission name. see {@code NutsConstants.Rights } class
     * @return true if permission is valid and allowed for the current user
     */
    boolean isAllowed(String permission);

    boolean isRepositoryAllowed(String repository, String permission);

    /**
     * check if allowed and throw a Security exception if not.
     *
     * @param permission    permission name. see {@code NutsConstants.Rights } class
     * @param operationName operation name
     * @return {@code this} instance
     */
    NSecurityManager checkAllowed(String permission, String operationName);

    NSecurityManager checkRepositoryAllowed(String repository, String permission, String operationName);

    /**
     * switch from/to secure mode.
     * when secure mode is disabled, no authorizations are checked against.
     *
     * @param secure        true if secure mode
     * @param adminPassword password for admin user
     * @return true if mode was switched correctly
     * @since 0.5.7
     */
    boolean setSecureMode(boolean secure, NSecureString adminPassword);

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
    NSecurityManager setAuthenticationAgent(String authenticationAgentId);

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

    <T> T callWithSecret(NSecureToken id, NSecretCaller<T> caller);

    void runWithSecret(NSecureToken id, NSecretRunner runner);

    boolean verify(NSecureToken credentialsId, NSecureString candidate);

    /**
     * remove existing credentials with the given id The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @return credentials
     */
    boolean removeCredentials(NSecureToken credentialsId);

    NSecureToken addSecret(NSecureString credentials);

    NSecureToken addSecret(NSecureString credentials, String agent);

    NSecureToken updateSecret(NSecureToken old, NSecureString credentials, String agent);

    NSecureToken addOneWayCredential(NSecureString password);

    NSecureToken addOneWayCredential(NSecureString password, String agent);

    NSecureToken updateOneWayCredential(NSecureToken old, NSecureString credentials, String agent);

    NSecurityManager addNamedCredential(NNamedCredential credential);

    NSecurityManager removeNamedCredential(String name, String user);

    NSecurityManager removeNamedCredential(String name);

    List<NNamedCredential> findNamedCredentials();

    List<NNamedCredential> findNamedCredentials(String user);

    NOptional<NNamedCredential> findNamedCredential(String name, String user);

    NOptional<NNamedCredential> findNamedCredential(String name);

    NNamedCredentialBuilder createNamedCredentialBuilder();

    NSecurityManager addRepositoryPermissions(String user, String repository, String... permissions);

    NSecurityManager removeRepositoryPermissions(String user, String repository, String... permissions);

    NUserSpec createUserUpdateQuery(String username);

    NRepositoryAccessSpec createRepositoryAccessSpec(String userName, String repository);

    NSecureString createEmptySecureString();

    NSecureString createSecureString(char[] content);

    NSecureString createUnsecureString(String string);
}
