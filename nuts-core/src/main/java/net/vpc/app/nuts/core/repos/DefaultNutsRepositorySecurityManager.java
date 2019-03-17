/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.NutsEffectiveUserImpl;
import net.vpc.common.strings.StringUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        String name = repo.getWorkspace().getSecurityManager().getCurrentLogin();
        if (NutsConstants.USER_ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsUserConfig s = repo.getConfigManager().getUser(n);
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
        return repo.getWorkspace().getSecurityManager().isAllowed(right);
    }

    @Override
    public void addUser(String user, String credentials, String... rights) {
        if (StringUtils.isEmpty(user)) {
            throw new NutsIllegalArgumentException("Invalid user");
        }
        repo.getConfigManager().setUser(new NutsUserConfig(user, null, null, null, null));
        setUserCredentials(user, credentials, null);
        if (rights != null) {
            NutsUserConfig security = repo.getConfigManager().getUser(user);
            for (String right : rights) {
                if (!StringUtils.isEmpty(right)) {
                    security.addRight(right);
                }
            }
        }
    }

    @Override
    public void setUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = repo.getConfigManager().getUser(user);
            for (String right : security.getRights()) {
                security.removeRight(right);
            }
            for (String right : rights) {
                if (!StringUtils.isEmpty(right)) {
                    security.addRight(right);
                }
            }
        }
    }

    @Override
    public void addUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = repo.getConfigManager().getUser(user);
            for (String right : rights) {
                if (!StringUtils.isEmpty(right)) {
                    security.addRight(right);
                }
            }
        }
    }

    @Override
    public void removeUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = repo.getConfigManager().getUser(user);
            for (String right : rights) {
                security.removeRight(right);
            }
        }
    }

    @Override
    public void setUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig usr = repo.getConfigManager().getUser(user);
            for (String right : usr.getRights()) {
                usr.removeRight(right);
            }
            for (String grp : groups) {
                usr.addGroup(grp);
            }
            repo.getConfigManager().setUser(usr);
        }
    }

    @Override
    public void addUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig usr = repo.getConfigManager().getUser(user);
            for (String grp : groups) {
                if (!StringUtils.isEmpty(grp)) {
                    usr.addGroup(grp);
                }
            }
            repo.getConfigManager().setUser(usr);
        }
    }

    @Override
    public void removeUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig usr = repo.getConfigManager().getUser(user);
            for (String grp : groups) {
                usr.removeGroup(grp);
            }
            repo.getConfigManager().setUser(usr);
        }
    }

    @Override
    public void setUserRemoteIdentity(String user, String mappedIdentity) {
        NutsUserConfig u = repo.getConfigManager().getUser(user);
        u.setMappedUser(mappedIdentity);
        repo.getConfigManager().setUser(u);
    }

    @Override
    public void setUserCredentials(String username, String password, String oldPassword) {
        if (!isAllowed(NutsConstants.RIGHT_SET_PASSWORD)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_SET_PASSWORD);
        }
        if (StringUtils.isEmpty(username)) {
            username = repo.getWorkspace().getSecurityManager().getCurrentLogin();
        }
        NutsUserConfig u = repo.getConfigManager().getUser(username);
        if (u == null) {
            throw new NutsIllegalArgumentException("No such user " + username);
        }
        if (!repo.getWorkspace().getSecurityManager().getCurrentLogin().equals(username)) {
            if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
                throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_ADMIN);
            }
        }
        if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
            repo.getWorkspace().getConfigManager().createAuthenticationAgent(u.getAuthenticationAgent())
                    .checkCredentials(
                            u.getCredentials(), u.getAuthenticationAgent(), oldPassword,
                            repo.getConfigManager()
                    );
//            if (StringUtils.isEmpty(password)) {
//                throw new NutsSecurityException("Missing old password");
//            }
//            //check old password
//            if (!StringUtils.isEmpty(u.getCredentials()) && !u.getCredentials().equals(CoreSecurityUtils.evalSHA1(oldPassword))) {
//                throw new NutsSecurityException("Invalid password");
//            }
        }
        if (StringUtils.isEmpty(password)) {
            throw new NutsIllegalArgumentException("Missing password");
        }
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, StringUtils.alignLeft(repo.getName(), 20) + " Update user credentials " + username);
        }

        u.setCredentials(
                repo.getWorkspace().getConfigManager().createAuthenticationAgent(u.getAuthenticationAgent())
                        .setCredentials(password, u.getAuthenticationAgent(),
                                repo.getConfigManager())
        );
        repo.getConfigManager().setUser(u);
    }

    @Override
    public NutsEffectiveUser[] findUsers() {
        List<NutsEffectiveUser> all = new ArrayList<>();
        for (NutsUserConfig secu : repo.getConfigManager().getUsers()) {
            all.add(getEffectiveUser(secu.getUser()));
        }
        return all.toArray(new NutsEffectiveUser[0]);
    }

    @Override
    public NutsEffectiveUser getEffectiveUser(String username) {
        NutsUserConfig u = repo.getConfigManager().getUser(username);
        Stack<String> inherited = new Stack<>();
        if (u != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(u.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = repo.getConfigManager().getUser(s);
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

}
