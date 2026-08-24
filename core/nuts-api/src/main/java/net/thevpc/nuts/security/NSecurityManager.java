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
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NSecurityManager of() {
        return NExtensions.of(NSecurityManager.class);
    }

    /**
     * current user
     *
     * @return current user
     */
    String currentUsername();

    /**
     * current user stack.
     * this is useful when login with multiple user identities.
     *
     * @return current user stack
     */
    List<String> currentLoginStack();

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
    List<NUser> users();


    /**
     * find user with the given name or null.
     *
     * @param username user name
     * @return user effective information
     */
    NOptional<NUser> getUser(String username);

    /**
     * Adds the specified user.
     *
     * @param query query
     * @return add user result
     */
    NSecurityManager addUser(NUserSpec query);

    /**
     * Update user.
     *
     * @param query query
     * @return update user result
     */
    NSecurityManager updateUser(NUserSpec query);

    /**
     * Returns the repository access.
     *
     * @param user user
     * @param repository repository
     * @return get repository access result
     */
    NOptional<NRepositoryAccess> getRepositoryAccess(String user, String repository);

    /**
     * Repository access list.
     *
     * @return repository access list result
     */
    List<NRepositoryAccess> repositoryAccessList();

    /**
     * Returns the repository access list by repository.
     *
     * @param repository repository
     * @return get repository access list by repository result
     */
    List<NRepositoryAccess> getRepositoryAccessListByRepository(String repository);

    /**
     * Returns the repository access list by user.
     *
     * @param user user
     * @return get repository access list by user result
     */
    List<NRepositoryAccess> getRepositoryAccessListByUser(String user);

    /**
     * Update repository access.
     *
     * @param repositoryAccess repository access
     * @return update repository access result
     */
    NSecurityManager updateRepositoryAccess(NRepositoryAccessSpec repositoryAccess);

    /**
     * return true if permission is valid and allowed for the current user.
     *
     * @param permission permission name. see {@code NutsConstants.Rights } class
     * @return true if permission is valid and allowed for the current user
     */
    boolean isAllowed(String permission);

    /**
     * Checks if is repository allowed.
     *
     * @param repository repository
     * @param permission permission
     * @return is repository allowed result
     */
    boolean isRepositoryAllowed(String repository, String permission);

    /**
     * check if allowed and throw a Security exception if not.
     *
     * @param permission    permission name. see {@code NutsConstants.Rights } class
     * @param operationName operation name
     * @return {@code this} instance
     */
    NSecurityManager checkAllowed(String permission, String operationName);

    /**
     * Check repository allowed.
     *
     * @param repository repository
     * @param permission permission
     * @param operationName operation name
     * @return check repository allowed result
     */
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

    /**
     * Checks if is anonymous.
     *
     * @return is anonymous result
     */
    boolean isAnonymous();

    /**
     * update default authentication agent.
     *
     * @param authenticationAgentId authentication agent id
     * @return {@code this} instance
     */
    NSecurityManager authenticationAgent(String authenticationAgentId);

    /**
     * return true if workspace is running secure mode
     *
     * @return true if workspace is running secure mode
     */
    boolean isSecureMode();

    /**
     * Call with secret.
     *
     * @param id id
     * @param caller caller
     * @return call with secret result
     */
    <T> T callWithSecret(NSecureToken id, NSecretCaller<T> caller);

    /**
     * Run with secret.
     *
     * @param id id
     * @param runner runner
     */
    void runWithSecret(NSecureToken id, NSecretRunner runner);

    /**
     * Verify.
     *
     * @param credentialsId credentials id
     * @param candidate candidate
     * @return verify result
     */
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

    /**
     * Adds the specified secret.
     *
     * @param credentials credentials
     * @return add secret result
     */
    NSecureToken addSecret(NSecureString credentials);

    /**
     * Adds the specified secret.
     *
     * @param credentials credentials
     * @param agent agent
     * @return add secret result
     */
    NSecureToken addSecret(NSecureString credentials, String agent);

    /**
     * Update secret.
     *
     * @param old old
     * @param credentials credentials
     * @param agent agent
     * @return update secret result
     */
    NSecureToken updateSecret(NSecureToken old, NSecureString credentials, String agent);

    /**
     * Adds the specified one way credential.
     *
     * @param password password
     * @return add one way credential result
     */
    NSecureToken addOneWayCredential(NSecureString password);

    /**
     * Adds the specified one way credential.
     *
     * @param password password
     * @param agent agent
     * @return add one way credential result
     */
    NSecureToken addOneWayCredential(NSecureString password, String agent);

    /**
     * Update one way credential.
     *
     * @param old old
     * @param credentials credentials
     * @param agent agent
     * @return update one way credential result
     */
    NSecureToken updateOneWayCredential(NSecureToken old, NSecureString credentials, String agent);

    /**
     * Adds the specified named credential.
     *
     * @param credential credential
     * @return add named credential result
     */
    NSecurityManager addNamedCredential(NNamedCredential credential);

    /**
     * Removes the specified named credential.
     *
     * @param name name
     * @param user user
     * @return remove named credential result
     */
    NSecurityManager removeNamedCredential(String name, String user);

    /**
     * Removes the specified named credential.
     *
     * @param name name
     * @return remove named credential result
     */
    NSecurityManager removeNamedCredential(String name);

    /**
     * Named credentials.
     *
     * @return named credentials result
     */
    List<NNamedCredential> namedCredentials();

    /**
     * Returns the named credentials.
     *
     * @param user user
     * @return get named credentials result
     */
    List<NNamedCredential> getNamedCredentials(String user);

    /**
     * Returns the named credential.
     *
     * @param name name
     * @param user user
     * @return get named credential result
     */
    NOptional<NNamedCredential> getNamedCredential(String name, String user);

    /**
     * Returns the named credential.
     *
     * @param name name
     * @return get named credential result
     */
    NOptional<NNamedCredential> getNamedCredential(String name);

    /**
     * Creates a new instance of create named credential builder.
     *
     * @return create named credential builder result
     */
    NNamedCredentialBuilder createNamedCredentialBuilder();

    /**
     * Adds the specified repository permissions.
     *
     * @param user user
     * @param repository repository
     * @param permissions permissions
     * @return add repository permissions result
     */
    NSecurityManager addRepositoryPermissions(String user, String repository, String... permissions);

    /**
     * Removes the specified repository permissions.
     *
     * @param user user
     * @param repository repository
     * @param permissions permissions
     * @return remove repository permissions result
     */
    NSecurityManager removeRepositoryPermissions(String user, String repository, String... permissions);

    /**
     * Creates a new instance of create user update query.
     *
     * @param username username
     * @return create user update query result
     */
    NUserSpec createUserUpdateQuery(String username);

    /**
     * Creates a new instance of create repository access spec.
     *
     * @param userName user name
     * @param repository repository
     * @return create repository access spec result
     */
    NRepositoryAccessSpec createRepositoryAccessSpec(String userName, String repository);

    /**
     * Creates a new instance of create empty secure string.
     *
     * @return create empty secure string result
     */
    NSecureString createEmptySecureString();

    /**
     * Creates a new instance of create secure string.
     *
     * @param content content
     * @return create secure string result
     */
    NSecureString createSecureString(char[] content);

    /**
     * Creates a new instance of create unsecure string.
     *
     * @param string string
     * @return create unsecure string result
     */
    NSecureString createUnsecureString(String string);
}
