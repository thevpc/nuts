/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepoConfigManager;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.spi.NAuthenticationAgent;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 *
 * @author thevpc
 */
public class DefaultNRepositorySecurityModel {

//    private final NutsLogger LOG;

    private final NRepository repository;
    private final WrapperNAuthenticationAgent agent;
    private final Map<String, NAuthorizations> authorizations = new HashMap<>();

    public DefaultNRepositorySecurityModel(final NRepository repo) {
        this.repository = repo;
        this.agent = new WrapperNAuthenticationAgent(repo.getWorkspace(), (session) -> repo.config().setSession(session).getConfigMap(), (x, s) -> getAuthenticationAgent(x, s));
        this.repository.addRepositoryListener(new NRepositoryListener() {

            public void onConfigurationChanged(NRepositoryEvent event) {
                authorizations.clear();
            }
        });
//        LOG = repo.getWorkspace().log().of(DefaultNRepositorySecurityModel.class);
    }

    public void checkAllowed(String right, String operationName, NSession session) {
        NSessionUtils.checkSession(repository.getWorkspace(), session);
        if (!isAllowed(right, session)) {
            if (NBlankable.isBlank(operationName)) {
                throw new NSecurityException(session, NMsg.ofCstyle("%s not allowed!",right));
            } else {
                throw new NSecurityException(session, NMsg.ofCstyle("%s : %s not allowed!",operationName,right));
            }
        }
//        return this;
    }

    public NAddUserCommand addUser(String name, NSession session) {
        return NAddUserCommand.of(session).setRepository(repository).setUsername(name);
    }

    public NUpdateUserCommand updateUser(String name, NSession session) {
        return NUpdateUserCommand.of(session).setRepository(repository).setUsername(name);
    }

    public NRemoveUserCommand removeUser(String name, NSession session) {
        return NRemoveUserCommand.of(session).setRepository(repository).setUsername(name);
    }

    private NAuthorizations getAuthorizations(String n, NSession session) {
        NAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NUserConfig s = NRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .getUser(n, session);
        if (s != null) {
            List<String> rr = s.getPermissions();
            aa = new NAuthorizations(
                    CoreCollectionUtils.nonNullList(rr)
            );
            authorizations.put(n, aa);
        } else {
            aa = new NAuthorizations(Collections.emptyList());
        }
        return aa;
    }

    public boolean isAllowed(String right, NSession session) {
        NWorkspaceSecurityManager sec = NWorkspaceSecurityManager.of(session);
        if (!sec.isSecure()) {
            return true;
        }
        String name = sec.getCurrentUsername();
        if (NConstants.Users.ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NAuthorizations s = getAuthorizations(n, session);
            Boolean ea = s.explicitAccept(right);
            if (ea != null) {
                return ea;
            }
            NUserConfig uc = NRepositoryConfigManagerExt.of(repository.config())
                    .getModel()
                    .getUser(n, session);
            if (uc != null && uc.getGroups() != null) {
                for (String g : uc.getGroups()) {
                    if (!visitedGroups.contains(g)) {
                        visitedGroups.add(g);
                        items.push(g);
                    }
                }
            }
        }
        return sec
                .isAllowed(right);
    }

    public List<NUser> findUsers(NSession session) {
        List<NUser> all = new ArrayList<>();
        for (NUserConfig secu : NRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .getUsers(session)) {
            all.add(getEffectiveUser(secu.getUser(), session));
        }
        return all;
    }

    public NUser getEffectiveUser(String username, NSession session) {
        NUserConfig u = NRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .getUser(username, session);
        Stack<String> inherited = new Stack<>();
        if (u != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(u.getGroups());
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NUserConfig ss = NRepositoryConfigManagerExt.of(repository.config())
                        .getModel()
                        .getUser(s, session);
                if (ss != null) {
                    inherited.addAll(ss.getPermissions());
                    for (String group : ss.getGroups()) {
                        if (!visited.contains(group)) {
                            curr.push(group);
                        }
                    }
                }
            }
        }
        return u == null ? null : new DefaultNUser(u, inherited);
    }

    public NAuthenticationAgent getAuthenticationAgent(String id, NSession session) {
        id = NStringUtils.trim(id);
        if (id.isEmpty()) {
            id = ((DefaultNRepoConfigManager) repository.config())
                    .getModel()
                    .getStoredConfig(session).getAuthenticationAgent();
        }
        NAuthenticationAgent a = NConfigsExt.of(NConfigs.of(session))
                .getModel()
                .createAuthenticationAgent(id, session);
        return a;
    }

    public void setAuthenticationAgent(String authenticationAgent, NSession session) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        DefaultNRepoConfigManager cc = (DefaultNRepoConfigManager) repository.config().setSession(session);

        if (NConfigsExt.of(NConfigs.of(session))
                .getModel().createAuthenticationAgent(authenticationAgent, session) == null) {
            throw new NIllegalArgumentException(session,
                    NMsg.ofCstyle("unsupported Authentication Agent %s", authenticationAgent)
            );
        }

        NRepositoryConfig conf = cc.getModel().getStoredConfig(session);
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgent)) {
            conf.setAuthenticationAgent(authenticationAgent);
            cc.getModel().fireConfigurationChanged("authentication-agent", session);
        }
//        return this;
    }

    public void checkCredentials(char[] credentialsId, char[] password, NSession session) throws NSecurityException {
        agent.checkCredentials(credentialsId, password, session);
    }

    public char[] getCredentials(char[] credentialsId, NSession session) {
        return agent.getCredentials(credentialsId, session);
    }

    public boolean removeCredentials(char[] credentialsId, NSession session) {
        return agent.removeCredentials(credentialsId, session);
    }

    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NSession session) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId, session);
    }

    public NRepository getRepository() {
        return repository;
    }

    public NWorkspace getWorkspace() {
        return repository.getWorkspace();
    }

}
