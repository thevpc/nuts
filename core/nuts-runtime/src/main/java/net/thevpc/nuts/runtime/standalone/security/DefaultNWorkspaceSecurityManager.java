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
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import javax.security.auth.callback.*;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NUser;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.List;

/**
 *
 * @author thevpc
 */
@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNWorkspaceSecurityManager implements NWorkspaceSecurityManager {

    public final DefaultNWorkspaceSecurityModel model;
    public NWorkspace workspace;

    public DefaultNWorkspaceSecurityManager(NWorkspace workspace) {
        this.workspace = workspace;
        NWorkspaceExt e = (NWorkspaceExt) workspace;
        this.model = e.getModel().securityModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NWorkspaceSecurityManager login(final String username, final char[] password) {
        model.login(username, password);
        return this;
    }

    @Override
    public boolean setSecureMode(boolean secure, char[] adminPassword) {
        return model.setSecureMode(secure, adminPassword);
    }

    public boolean switchUnsecureMode(char[] adminPassword) {
        return model.switchUnsecureMode(adminPassword);
    }

    public boolean switchSecureMode(char[] adminPassword) {
        return model.switchSecureMode(adminPassword);
    }

    @Override
    public boolean isAdmin() {
        return model.isAdmin();
    }

    @Override
    public NWorkspaceSecurityManager logout() {
        model.logout();
        return this;
    }

    @Override
    public NUser findUser(String username) {
        return model.findUser(username);
    }

    @Override
    public List<NUser> findUsers() {
        return model.findUsers();
    }

    @Override
    public NAddUserCmd addUser(String name) {
        return model.addUser(name);
    }

    @Override
    public NUpdateUserCmd updateUser(String name) {
        return model.updateUser(name);
    }

    @Override
    public NRemoveUserCmd removeUser(String name) {
        return model.removeUser(name);
    }

    @Override
    public NWorkspaceSecurityManager checkAllowed(String permission, String operationName) {
        model.checkAllowed(permission, operationName);
        return this;
    }

    @Override
    public boolean isAllowed(String permission) {
        return model.isAllowed(permission);
    }

    @Override
    public String[] getCurrentLoginStack() {
        return model.getCurrentLoginStack();
    }

    @Override
    public String getCurrentUsername() {
        return model.getCurrentUsername();
    }

    @Override
    public NWorkspaceSecurityManager login(CallbackHandler handler) {
        model.login(handler);
        return this;
    }

    @Override
    public NAuthenticationAgent getAuthenticationAgent(String authenticationAgentId) {
        return model.getAuthenticationAgent(authenticationAgentId);
    }

    @Override
    public NWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgentId) {
        model.setAuthenticationAgent(authenticationAgentId);
        return this;
    }

    @Override
    public boolean isSecure() {
        return model.isSecure();
    }

    @Override
    public NWorkspaceSecurityManager checkCredentials(char[] credentialsId, char[] password) throws NSecurityException {
        model.checkCredentials(credentialsId, password);
        return this;
    }

    @Override
    public char[] getCredentials(char[] credentialsId) {
        return model.getCredentials(credentialsId);
    }

    @Override
    public boolean removeCredentials(char[] credentialsId) {
        return model.removeCredentials(credentialsId);
    }

    @Override
    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId) {
        return model.createCredentials(credentials, allowRetrieve, credentialId);
    }

}
