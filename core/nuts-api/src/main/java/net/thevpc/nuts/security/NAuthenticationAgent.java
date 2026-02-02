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

import net.thevpc.nuts.spi.NComponent;

import java.util.function.Function;

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
     * agent id artifactId+version;
     *
     * @return agent id
     */
    String getId();


    <T> T withSecret(NCredentialId id, NSecretCaller<T> consumer, Function<String, String> env);

    boolean verify(NCredentialId id, char[] candidate, Function<String, String> env);

    /**
     * remove existing credentials with the given id The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param envProvider   environment provider, nullable
     * @return credentials
     */
    boolean removeCredentials(NCredentialId credentialsId, Function<String, String> envProvider);

    NCredentialId addSecret(char[] credentials, Function<String, String> envProvider);

    /**
     * Update credential. MAY return a new NCredentialId (immutable storage)
     * or the same instance (mutable storage). Callers MUST rebind named
     * credentials to the returned ID to ensure correctness.
     */
    NCredentialId updateSecret(NCredentialId od, char[] credentials, Function<String, String> envProvider);

    NCredentialId addOneWayCredential(char[] password, Function<String, String> envProvider);

    /**
     * Update credential. MAY return a new NCredentialId (immutable storage)
     * or the same instance (mutable storage). Callers MUST rebind named
     * credentials to the returned ID to ensure correctness.
     */
    NCredentialId updateOneWay(NCredentialId old, char[] credentials, Function<String, String> envProvider);

}
