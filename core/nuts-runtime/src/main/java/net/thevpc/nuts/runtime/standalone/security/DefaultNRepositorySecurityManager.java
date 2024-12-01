/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import net.thevpc.nuts.NUser;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.security.NAuthenticationAgent;

import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNRepositorySecurityManager implements NRepositorySecurityManager {

    private DefaultNRepositorySecurityModel model;

    public DefaultNRepositorySecurityManager(DefaultNRepositorySecurityModel model) {
        this.model = model;
    }

    public DefaultNRepositorySecurityModel getModel() {
        return model;
    }

    @Override
    public NRepositorySecurityManager checkAllowed(String right, String operationName) {
        model.checkAllowed(right, operationName);
        return this;
    }

    private void checkSession(NSession session1) {
        NSessionUtils.checkSession(model.getWorkspace(), session1);
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
    public boolean isAllowed(String right) {
        return model.isAllowed(right);
    }

    @Override
    public List<NUser> findUsers() {
        return model.findUsers();
    }

    @Override
    public NUser getEffectiveUser(String username) {
        return model.getEffectiveUser(username);
    }

    @Override
    public NAuthenticationAgent getAuthenticationAgent(String id) {
        return model.getAuthenticationAgent(id);
    }

    @Override
    public NRepositorySecurityManager setAuthenticationAgent(String authenticationAgent) {
        model.setAuthenticationAgent(authenticationAgent);
        return this;
    }

    @Override
    public NRepositorySecurityManager checkCredentials(char[] credentialsId, char[] password) throws NSecurityException {
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
