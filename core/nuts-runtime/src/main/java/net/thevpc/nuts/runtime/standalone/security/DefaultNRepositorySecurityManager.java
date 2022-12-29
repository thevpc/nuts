/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NAuthenticationAgent;

import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNRepositorySecurityManager implements NRepositorySecurityManager {

    private DefaultNRepositorySecurityModel model;
    private NSession session;

    public DefaultNRepositorySecurityManager(DefaultNRepositorySecurityModel model) {
        this.model = model;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NRepositorySecurityManager setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    public DefaultNRepositorySecurityModel getModel() {
        return model;
    }

    @Override
    public NRepositorySecurityManager checkAllowed(String right, String operationName) {
        checkSession(session);
        model.checkAllowed(right, operationName, session);
        return this;
    }

    private void checkSession(NSession session1) {
        NSessionUtils.checkSession(model.getWorkspace(), session1);
    }

    @Override
    public NAddUserCommand addUser(String name) {
        checkSession(session);
        return model.addUser(name, session);
    }

    @Override
    public NUpdateUserCommand updateUser(String name) {
        checkSession(session);
        return model.updateUser(name, session).setSession(session);
    }

    @Override
    public NRemoveUserCommand removeUser(String name) {
        checkSession(session);
        return model.removeUser(name, session).setSession(session);
    }

    @Override
    public boolean isAllowed(String right) {
        checkSession(session);
        return model.isAllowed(right, session);
    }

    @Override
    public List<NUser> findUsers() {
        checkSession(session);
        return model.findUsers(session);
    }

    @Override
    public NUser getEffectiveUser(String username) {
        checkSession(session);
        return model.getEffectiveUser(username, session);
    }

    @Override
    public NAuthenticationAgent getAuthenticationAgent(String id) {
        checkSession(session);
        return model.getAuthenticationAgent(id, session);
    }

    @Override
    public NRepositorySecurityManager setAuthenticationAgent(String authenticationAgent) {
        checkSession(session);
        model.setAuthenticationAgent(authenticationAgent, session);
        return this;
    }

    @Override
    public NRepositorySecurityManager checkCredentials(char[] credentialsId, char[] password) throws NSecurityException {
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
