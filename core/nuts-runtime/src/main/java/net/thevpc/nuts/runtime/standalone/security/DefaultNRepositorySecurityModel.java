/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepoConfigManager;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
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
        this.agent = new WrapperNAuthenticationAgent(repo.getWorkspace(), () -> repo.config().getConfigMap(), (x) -> getAuthenticationAgent(x));
        this.repository.addRepositoryListener(new NRepositoryListener() {

            public void onConfigurationChanged(NRepositoryEvent event) {
                authorizations.clear();
            }
        });
//        LOG = repo.getWorkspace().log().of(DefaultNRepositorySecurityModel.class);
    }

    public void checkAllowed(String right, String operationName) {
        NSession session=repository.getWorkspace().currentSession();
        NSessionUtils.checkSession(repository.getWorkspace(), session);
        if (!isAllowed(right)) {
            if (NBlankable.isBlank(operationName)) {
                throw new NSecurityException(NMsg.ofC("%s not allowed!",right));
            } else {
                throw new NSecurityException(NMsg.ofC("%s : %s not allowed!",operationName,right));
            }
        }
//        return this;
    }

    public NAddUserCmd addUser(String name) {
        return NAddUserCmd.of().setRepository(repository).setUsername(name);
    }

    public NUpdateUserCmd updateUser(String name) {
        return NUpdateUserCmd.of().setRepository(repository).setUsername(name);
    }

    public NRemoveUserCmd removeUser(String name) {
        return NRemoveUserCmd.of().setRepository(repository).setUsername(name);
    }

    private NAuthorizations getAuthorizations(String n) {
        NAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NUserConfig s = NRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .findUser(n).orNull();
        if (s != null) {
            List<String> rr = s.getPermissions();
            aa = new NAuthorizations(
                    NCoreCollectionUtils.nonNullList(rr)
            );
            authorizations.put(n, aa);
        } else {
            aa = new NAuthorizations(Collections.emptyList());
        }
        return aa;
    }

    public boolean isAllowed(String right) {
        NSession session=repository.getWorkspace().currentSession();
        NWorkspaceSecurityManager sec = NWorkspaceSecurityManager.of();
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
            NAuthorizations s = getAuthorizations(n);
            Boolean ea = s.explicitAccept(right);
            if (ea != null) {
                return ea;
            }
            NUserConfig uc = NRepositoryConfigManagerExt.of(repository.config())
                    .getModel()
                    .findUser(n).orNull();
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

    public List<NUser> findUsers() {
        List<NUser> all = new ArrayList<>();
        for (NUserConfig secu : NRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .findUsers()) {
            all.add(getEffectiveUser(secu.getUser()));
        }
        return all;
    }

    public NUser getEffectiveUser(String username) {
        NUserConfig u = NRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .findUser(username).orNull();
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
                        .findUser(s).orNull();
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

    public NAuthenticationAgent getAuthenticationAgent(String id) {
        id = NStringUtils.trim(id);
        if (id.isEmpty()) {
            id = ((DefaultNRepoConfigManager) repository.config())
                    .getModel()
                    .getStoredConfig().getAuthenticationAgent();
        }
        NSession session = getWorkspace().currentSession();
        NAuthenticationAgent a = NConfigsExt.of(NConfigs.of())
                .getModel()
                .createAuthenticationAgent(id);
        return a;
    }

    public void setAuthenticationAgent(String authenticationAgent) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        DefaultNRepoConfigManager cc = (DefaultNRepoConfigManager) repository.config();
        NSession session=repository.getWorkspace().currentSession();

        if (NConfigsExt.of(NConfigs.of())
                .getModel().createAuthenticationAgent(authenticationAgent) == null) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("unsupported Authentication Agent %s", authenticationAgent)
            );
        }

        NRepositoryConfig conf = cc.getModel().getStoredConfig();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgent)) {
            conf.setAuthenticationAgent(authenticationAgent);
            cc.getModel().fireConfigurationChanged("authentication-agent");
        }
//        return this;
    }

    public void checkCredentials(char[] credentialsId, char[] password) throws NSecurityException {
        agent.checkCredentials(credentialsId, password);
    }

    public char[] getCredentials(char[] credentialsId) {
        return agent.getCredentials(credentialsId);
    }

    public boolean removeCredentials(char[] credentialsId) {
        return agent.removeCredentials(credentialsId);
    }

    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId);
    }

    public NRepository getRepository() {
        return repository;
    }

    public NWorkspace getWorkspace() {
        return repository.getWorkspace();
    }

}
