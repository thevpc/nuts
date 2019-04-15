/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.NutsEffectiveUserImpl;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsWorkspaceConfigManager;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
class DefaultNutsRepositorySecurityManager implements NutsRepositorySecurityManager {

    private static final Logger log = Logger.getLogger(DefaultNutsRepositorySecurityManager.class.getName());

    private final AbstractNutsRepository repo;

    DefaultNutsRepositorySecurityManager(final AbstractNutsRepository outer) {
        this.repo = outer;
    }

    @Override
    public void checkAllowed(String right) {
        if (!isAllowed(right)) {
            throw new NutsSecurityException(right + " not allowed!");
        }
    }

    @Override
    public boolean isAllowed(String right) {
        String name = repo.getWorkspace().security().getCurrentLogin();
        if (NutsConstants.Names.USER_ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsUserConfig s = repo.config().getUser(n);
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
    public void addUser(String user, String credentials, String... rights) {
        if (CoreStringUtils.isBlank(user)) {
            throw new NutsIllegalArgumentException("Invalid user");
        }
        repo.config().setUser(new NutsUserConfig(user, null, null, null));
        setUserCredentials(user, credentials, null);
        if (rights != null) {
            NutsUserConfig security = repo.config().getUser(user);
            for (String right : rights) {
                if (!CoreStringUtils.isBlank(right)) {
                    security.addRight(right);
                }
            }
        }
    }

    @Override
    public void setUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = repo.config().getUser(user);
            for (String right : security.getRights()) {
                security.removeRight(right);
            }
            for (String right : rights) {
                if (!CoreStringUtils.isBlank(right)) {
                    security.addRight(right);
                }
            }
        }
    }

    @Override
    public void addUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = repo.config().getUser(user);
            for (String right : rights) {
                if (!CoreStringUtils.isBlank(right)) {
                    security.addRight(right);
                }
            }
        }
    }

    @Override
    public void removeUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = repo.config().getUser(user);
            for (String right : rights) {
                security.removeRight(right);
            }
        }
    }

    @Override
    public void setUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig usr = repo.config().getUser(user);
            for (String right : usr.getRights()) {
                usr.removeRight(right);
            }
            for (String grp : groups) {
                usr.addGroup(grp);
            }
            repo.config().setUser(usr);
        }
    }

    @Override
    public void addUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig usr = repo.config().getUser(user);
            for (String grp : groups) {
                if (!CoreStringUtils.isBlank(grp)) {
                    usr.addGroup(grp);
                }
            }
            repo.config().setUser(usr);
        }
    }

    @Override
    public void removeUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig usr = repo.config().getUser(user);
            for (String grp : groups) {
                usr.removeGroup(grp);
            }
            repo.config().setUser(usr);
        }
    }

    @Override
    public void setUserRemoteIdentity(String user, String mappedIdentity) {
        NutsUserConfig u = repo.config().getUser(user);
        u.setMappedUser(mappedIdentity);
        repo.config().setUser(u);
    }

    @Override
    public void setUserCredentials(String username, String password, String oldPassword) {
        if (!isAllowed(NutsConstants.Rights.SET_PASSWORD)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.Rights.SET_PASSWORD);
        }
        if (CoreStringUtils.isBlank(username)) {
            username = repo.getWorkspace().security().getCurrentLogin();
        }
        NutsUserConfig u = repo.config().getUser(username);
        if (u == null) {
            throw new NutsIllegalArgumentException("No such user " + username);
        }
        if (!repo.getWorkspace().security().getCurrentLogin().equals(username)) {
            if (!isAllowed(NutsConstants.Rights.ADMIN)) {
                throw new NutsSecurityException("Not Allowed " + NutsConstants.Rights.ADMIN);
            }
        }
        if (!isAllowed(NutsConstants.Rights.ADMIN)) {
            getAuthenticationAgent()
                    .checkCredentials(
                            u.getCredentials(), oldPassword,
                            repo.config()
                    );
//            if (CoreStringUtils.isEmpty(password)) {
//                throw new NutsSecurityException("Missing old password");
//            }
//            //check old password
//            if (!CoreStringUtils.isEmpty(u.getCredentials()) && !u.getCredentials().equals(CoreSecurityUtils.evalSHA1(oldPassword))) {
//                throw new NutsSecurityException("Invalid password");
//            }
        }
        if (CoreStringUtils.isBlank(password)) {
            throw new NutsIllegalArgumentException("Missing password");
        }
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, CoreStringUtils.alignLeft(repo.config().getName(), 20) + " Update user credentials " + username);
        }

        u.setCredentials(
                getAuthenticationAgent()
                        .setCredentials(password, repo.config())
        );
        repo.config().setUser(u);
    }

    @Override
    public NutsEffectiveUser[] findUsers() {
        List<NutsEffectiveUser> all = new ArrayList<>();
        for (NutsUserConfig secu : repo.config().getUsers()) {
            all.add(getEffectiveUser(secu.getUser()));
        }
        return all.toArray(new NutsEffectiveUser[0]);
    }

    @Override
    public NutsEffectiveUser getEffectiveUser(String username) {
        NutsUserConfig u = repo.config().getUser(username);
        Stack<String> inherited = new Stack<>();
        if (u != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(u.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = repo.config().getUser(s);
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
            throw new NutsIllegalArgumentException("Unsupported Authentication Agent " + authenticationAgent);
        }

        NutsRepositoryConfig conf = cc.getStoredConfig();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgent)) {
            conf.setAuthenticationAgent(authenticationAgent);
            cc.fireConfigurationChanged();
        }
    }
}
