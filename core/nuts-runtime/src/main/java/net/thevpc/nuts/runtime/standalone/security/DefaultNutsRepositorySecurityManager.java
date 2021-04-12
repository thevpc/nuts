/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNutsRepositorySecurityManager implements NutsRepositorySecurityManager {

    private DefaultNutsRepositorySecurityModel model;
    private NutsSession session;

    public DefaultNutsRepositorySecurityManager(DefaultNutsRepositorySecurityModel model) {
        this.model = model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsRepositorySecurityManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public DefaultNutsRepositorySecurityModel getModel() {
        return model;
    }

    @Override
    public NutsRepositorySecurityManager checkAllowed(String right, String operationName) {
        checkSession(session);
        model.checkAllowed(right, operationName, session);
        return this;
    }

    private void checkSession(NutsSession session1) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session1);
    }

    @Override
    public NutsAddUserCommand addUser(String name) {
        checkSession(session);
        return model.addUser(name, session);
    }

    @Override
    public NutsUpdateUserCommand updateUser(String name) {
        checkSession(session);
        return model.updateUser(name, session).setSession(session);
    }

    @Override
    public NutsRemoveUserCommand removeUser(String name) {
        checkSession(session);
        return model.removeUser(name, session).setSession(session);
    }

    @Override
    public boolean isAllowed(String right) {
        checkSession(session);
        return model.isAllowed(right, session);
    }

    @Override
    public NutsUser[] findUsers() {
        checkSession(session);
        return model.findUsers(session);
    }

    @Override
    public NutsUser getEffectiveUser(String username) {
        checkSession(session);
        return model.getEffectiveUser(username, session);
    }

    @Override
    public NutsAuthenticationAgent getAuthenticationAgent(String id) {
        checkSession(session);
        return model.getAuthenticationAgent(id, session);
    }

    @Override
    public NutsRepositorySecurityManager setAuthenticationAgent(String authenticationAgent) {
        checkSession(session);
        model.setAuthenticationAgent(authenticationAgent, session);
        return this;
    }

    @Override
    public NutsRepositorySecurityManager checkCredentials(char[] credentialsId, char[] password) throws NutsSecurityException {
        checkSession(session);
        model.checkCredentials(credentialsId, password, session);
        return this;
    }

    @Override
    public char[] getCredentials(char[] credentialsId) {
        checkSession(session);
        return model.getCredentials(credentialsId, session);
    }

    @Override
    public boolean removeCredentials(char[] credentialsId) {
        checkSession(session);
        return model.removeCredentials(credentialsId, session);
    }

    @Override
    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId) {
        checkSession(session);
        return model.createCredentials(credentials, allowRetrieve, credentialId, session);
    }
}
