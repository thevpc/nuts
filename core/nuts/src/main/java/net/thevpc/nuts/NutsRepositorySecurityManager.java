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

/**
 *
 * @author thevpc
 * @since 0.5.4
 * @app.category Security
 */
public interface NutsRepositorySecurityManager {

    boolean isAllowed(String right);

    NutsRepositorySecurityManager checkAllowed(String right, String operationName) throws SecurityException;

    NutsAddUserCommand addUser(String name);

    NutsUpdateUserCommand updateUser(String name);

    NutsRemoveUserCommand removeUser(String name);

    NutsUser[] findUsers();

    NutsUser getEffectiveUser(String username);

    NutsRepositorySecurityManager setAuthenticationAgent(String authenticationAgent);

    NutsAuthenticationAgent getAuthenticationAgent(String id);

    /**
     * check if the given <code>password</code> is valid against the one stored
     * by the Authentication Agent for  <code>credentialsId</code>
     *
     * @param credentialsId credentialsId
     * @param password password
     * @throws NutsSecurityException when check failed
     * @return this
     */
    NutsRepositorySecurityManager checkCredentials(char[] credentialsId, char[] password) throws NutsSecurityException;

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
     * @param credentials credential
     * @param allowRetrieve when true {@link #getCredentials(char[])} can be
     * invoked over {@code credentialId}
     * @param credentialId preferred credentialId, if null, a new one is created
     * @return credentials-id
     */
    char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId);

    NutsSession getSession();

    NutsRepositorySecurityManager setSession(NutsSession session);
}
