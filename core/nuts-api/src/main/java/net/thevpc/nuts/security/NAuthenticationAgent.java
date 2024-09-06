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

import net.thevpc.nuts.NSecurityException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;

import java.util.Map;

/**
 * an Authentication Agent is responsible of storing and retrieving credentials
 * in external repository (password manager, kwallet, keypads,
 * gnome-keyring...). And Id of the stored password is then saved as plain text
 * in nuts config file.
 * Criteria type is a string representing authentication agent id
 *
 * @author thevpc
 * @app.category Security
 * @since 0.5.4
 */
public interface NAuthenticationAgent extends NComponent/* as authentication agent*/ {

    /**
     * agent id;
     *
     * @return agent id
     */
    String getId();

    /**
     * check if the given <code>password</code> is valid against the one stored
     * by the Authentication Agent for  <code>credentialsId</code>
     *
     * @param credentialsId credentialsId
     * @param password      password
     * @param envProvider   environment provider, nullable
     * @param session       session
     * @throws NSecurityException when check failed
     */
    void checkCredentials(char[] credentialsId, char[] password, Map<String, String> envProvider, NSession session) throws NSecurityException;

    /**
     * get the credentials for the given id. The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param envProvider   environment provider, nullable
     * @param session       session
     * @return credentials
     */
    char[] getCredentials(char[] credentialsId, Map<String, String> envProvider, NSession session);

    /**
     * remove existing credentials with the given id The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param envProvider   environment provider, nullable
     * @param session       session
     * @return credentials
     */
    boolean removeCredentials(char[] credentialsId, Map<String, String> envProvider, NSession session);

    /**
     * store credentials in the agent's and return the credential id to store
     * into the config. if credentialId is not null, the given credentialId will
     * be updated and the credentialId is returned. The {@code credentialsId},if
     * present or returned, <strong>MUST</strong> be prefixed with
     * AuthenticationAgent'd id and ':' character
     *
     * @param credentials   credential
     * @param allowRetrieve when true {@link #getCredentials(char[], Map, NSession)}  }
     *                      can be invoked over {@code credentialId}
     * @param credentialId  preferred credentialId, if null, a new one is created
     * @param envProvider   environment provider, nullable
     * @param session       session
     * @return credentials-id
     */
    char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, Map<String, String> envProvider, NSession session);
}
