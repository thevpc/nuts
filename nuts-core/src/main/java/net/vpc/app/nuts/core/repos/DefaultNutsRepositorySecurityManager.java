/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.NutsEffectiveUserImpl;

import java.util.*;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsAddUserCommand;
import net.vpc.app.nuts.core.DefaultNutsRemoveUserCommand;
import net.vpc.app.nuts.core.DefaultNutsUpdateUserCommand;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
class DefaultNutsRepositorySecurityManager implements NutsRepositorySecurityManager {

    private static final Logger LOG = Logger.getLogger(DefaultNutsRepositorySecurityManager.class.getName());

    private final NutsRepository repo;

    DefaultNutsRepositorySecurityManager(final NutsRepository outer) {
        this.repo = outer;
    }

    @Override
    public void checkAllowed(String right, String operationName) {
        if (!isAllowed(right)) {
            if (CoreStringUtils.isBlank(operationName)) {
                throw new NutsSecurityException(repo.getWorkspace(),right + " not allowed!");
            } else {
                throw new NutsSecurityException(repo.getWorkspace(),operationName + ": " + right + " not allowed!");
            }
        }
    }

    @Override
    public NutsAddUserCommand addUser(String name) {
        return new DefaultNutsAddUserCommand(repo);
    }

    @Override
    public NutsUpdateUserCommand updateUser(String name) {
        return new DefaultNutsUpdateUserCommand(repo);
    }

    @Override
    public NutsRemoveUserCommand removeUser(String name) {
        return new DefaultNutsRemoveUserCommand(repo);
    }

    @Override
    public boolean isAllowed(String right) {
        String name = repo.getWorkspace().security().getCurrentLogin();
        if (NutsConstants.Users.ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsUserConfig s = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(n);
            if (s != null) {
                if (s.containsRight("!" + right)) {
                    return false;
                }
                if (s.containsRight(right)) {
                    return true;
                }
                for (String g : s.getGroups()) {
                    if (!visitedGroups.contains(g)) {
                        visitedGroups.add(g);
                        items.push(g);
                    }
                }
            }
        }
        return repo.getWorkspace().security().isAllowed(right);
    }

    @Override
    public NutsEffectiveUser[] findUsers() {
        List<NutsEffectiveUser> all = new ArrayList<>();
        for (NutsUserConfig secu : NutsRepositoryConfigManagerExt.of(repo.config()).getUsers()) {
            all.add(getEffectiveUser(secu.getUser()));
        }
        return all.toArray(new NutsEffectiveUser[0]);
    }

    @Override
    public NutsEffectiveUser getEffectiveUser(String username) {
        NutsUserConfig u = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(username);
        Stack<String> inherited = new Stack<>();
        if (u != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(u.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(s);
                if (ss != null) {
                    inherited.addAll(Arrays.asList(ss.getRights()));
                    for (String group : ss.getGroups()) {
                        if (!visited.contains(group)) {
                            curr.push(group);
                        }
                    }
                }
            }
        }
        return u == null ? null : new NutsEffectiveUserImpl(u, inherited.toArray(new String[0]));
    }

    @Override
    public NutsAuthenticationAgent getAuthenticationAgent() {
        return repo.getWorkspace().config().createAuthenticationAgent(
                ((DefaultNutsRepositoryConfigManager) repo.config())
                        .getStoredConfig().getAuthenticationAgent());
    }

    @Override
    public void setAuthenticationAgent(String authenticationAgent) {

        DefaultNutsRepositoryConfigManager cc = (DefaultNutsRepositoryConfigManager) repo.config();

        if (repo.getWorkspace().config().createAuthenticationAgent(authenticationAgent) == null) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(),"Unsupported Authentication Agent " + authenticationAgent);
        }

        NutsRepositoryConfig conf = cc.getStoredConfig();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgent)) {
            conf.setAuthenticationAgent(authenticationAgent);
            cc.fireConfigurationChanged();
        }
    }
}
