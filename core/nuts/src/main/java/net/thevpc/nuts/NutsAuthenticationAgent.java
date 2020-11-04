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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

import java.util.Map;

/**
 * an Authentication Agent is responsible of storing and retrieving credentials
 * in external repository (password manager, kwallet, keypads,
 * gnome-keyring...). And Id of the stored password is then saved as plain text
 * in nuts config file.
 * Criteria type is a string representing authentication agent id
 * @author vpc
 * @since 0.5.4
 * @category Security
 */
public interface NutsAuthenticationAgent extends NutsComponent<String/* as authentication agent*/> {

    /**
     * agent id;
     * @return agent id
     */
    String getId();

    /**
     * check if the given <code>password</code> is valid against the one stored
     * by the Authentication Agent for  <code>credentialsId</code>
     *
     * @param credentialsId credentialsId
     * @param password password
     * @param envProvider environment provider, nullable
     * @param session
     * @throws NutsSecurityException when check failed
     */
    void checkCredentials(char[] credentialsId, char[] password, Map<String, String> envProvider, NutsSession session) throws NutsSecurityException;

    /**
     * get the credentials for the given id. The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param envProvider environment provider, nullable
     * @param session
     * @return credentials
     */
    char[] getCredentials(char[] credentialsId, Map<String, String> envProvider, NutsSession session);

    /**
     * remove existing credentials with the given id The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param envProvider environment provider, nullable
     * @param session
     * @return credentials
     */
    boolean removeCredentials(char[] credentialsId, Map<String, String> envProvider, NutsSession session);

    /**
     * store credentials in the agent's and return the credential id to store
     * into the config. if credentialId is not null, the given credentialId will
     * be updated and the credentialId is returned. The {@code credentialsId},if
     * present or returned, <strong>MUST</strong> be prefixed with
     * AuthenticationAgent'd id and ':' character
     *
     * @param credentials credential
     * @param allowRetrieve when true {@link #getCredentials(char[], Map, NutsSession)}  }
     * can be invoked over {@code credentialId}
     * @param credentialId preferred credentialId, if null, a new one is created
     * @param envProvider environment provider, nullable
     * @param session
     * @return credentials-id
     */
    char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, Map<String, String> envProvider, NutsSession session);
}
