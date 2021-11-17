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
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import javax.security.auth.callback.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsAuthenticationAgent;

/**
 *
 * @author thevpc
 */
public class DefaultNutsWorkspaceSecurityManager implements NutsWorkspaceSecurityManager {

    public final DefaultNutsWorkspaceSecurityModel model;
    public NutsSession session;

    public DefaultNutsWorkspaceSecurityManager(DefaultNutsWorkspaceSecurityModel model) {
        this.model = model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspaceSecurityManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public NutsWorkspaceSecurityManager login(final String username, final char[] password) {
        checkSession();
        model.login(username, password, session);
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean setSecureMode(boolean secure, char[] adminPassword) {
        checkSession();
        return model.setSecureMode(secure, adminPassword, session);
    }

    public boolean switchUnsecureMode(char[] adminPassword) {
        checkSession();
        return model.switchUnsecureMode(adminPassword, session);
    }

    public boolean switchSecureMode(char[] adminPassword) {
        checkSession();
        return model.switchSecureMode(adminPassword, session);
    }

    @Override
    public boolean isAdmin() {
        checkSession();
        return model.isAdmin(session);
    }

    @Override
    public NutsWorkspaceSecurityManager logout() {
        checkSession();
        model.logout(session);
        return this;
    }

    @Override
    public NutsUser findUser(String username) {
        checkSession();
        return model.findUser(username, session);
    }

    @Override
    public NutsUser[] findUsers() {
        checkSession();
        return model.findUsers(session);
    }

    @Override
    public NutsAddUserCommand addUser(String name) {
        checkSession();
        return model.addUser(name, session);
    }

    @Override
    public NutsUpdateUserCommand updateUser(String name) {
        checkSession();
        return model.updateUser(name, session);
    }

    @Override
    public NutsRemoveUserCommand removeUser(String name) {
        checkSession();
        return model.removeUser(name, session);
    }

    @Override
    public NutsWorkspaceSecurityManager checkAllowed(String permission, String operationName) {
        checkSession();
        model.checkAllowed(permission, operationName, session);
        return this;
    }

    @Override
    public boolean isAllowed(String permission) {
        checkSession();
        return model.isAllowed(permission, session);
    }

    @Override
    public String[] getCurrentLoginStack() {
        checkSession();
        return model.getCurrentLoginStack(session);
    }

    @Override
    public String getCurrentUsername() {
        checkSession();
        return model.getCurrentUsername(session);
    }

    @Override
    public NutsWorkspaceSecurityManager login(CallbackHandler handler) {
        checkSession();
        model.login(handler, session);
        return this;
    }

    @Override
    public NutsAuthenticationAgent getAuthenticationAgent(String authenticationAgentId) {
        checkSession();
        return model.getAuthenticationAgent(authenticationAgentId, session);
    }

    @Override
    public NutsWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgentId) {
        checkSession();
        model.setAuthenticationAgent(authenticationAgentId, session);
        return this;
    }

    @Override
    public boolean isSecure() {
        checkSession();
        return model.isSecure(session);
    }

    @Override
    public NutsWorkspaceSecurityManager checkCredentials(char[] credentialsId, char[] password) throws NutsSecurityException {
        checkSession();
        model.checkCredentials(credentialsId, password, session);
        return this;
    }

    @Override
    public char[] getCredentials(char[] credentialsId) {
        checkSession();
        return model.getCredentials(credentialsId, session);
    }

    @Override
    public boolean removeCredentials(char[] credentialsId) {
        checkSession();
        return model.removeCredentials(credentialsId, session);
    }

    @Override
    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId) {
        checkSession();
        return model.createCredentials(credentials, allowRetrieve, credentialId, session);
    }

}
