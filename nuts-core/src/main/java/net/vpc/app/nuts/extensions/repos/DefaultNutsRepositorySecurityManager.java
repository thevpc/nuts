/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.repos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsIllegalArgumentsException;
import net.vpc.app.nuts.NutsRepositoryConfig;
import net.vpc.app.nuts.NutsRepositorySecurityManager;
import net.vpc.app.nuts.NutsSecurityEntityConfig;
import net.vpc.app.nuts.NutsSecurityException;
import net.vpc.app.nuts.NutsUserInfo;
import net.vpc.app.nuts.extensions.core.NutsSecurityEntityConfigImpl;
import net.vpc.app.nuts.extensions.core.NutsUserInfoImpl;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreSecurityUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

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
    public boolean isAllowed(String right) {
        String name = repo.getWorkspace().getSecurityManager().getCurrentLogin();
        if (NutsConstants.USER_ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        NutsRepositoryConfig c = repo.getConfigManager().getConfig();
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsSecurityEntityConfig s = c.getSecurity(n);
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
        if (CoreStringUtils.isEmpty(user)) {
            throw new NutsIllegalArgumentsException("Invalid user");
        }
        repo.getConfigManager().getConfig().setSecurity(new NutsSecurityEntityConfigImpl(user, null, null, null));
        setUserCredentials(user, credentials, null);
        if (rights != null) {
            NutsSecurityEntityConfig security = repo.getConfigManager().getConfig().getSecurity(user);
            for (String right : rights) {
                security.addRight(right);
            }
        }
    }

    @Override
    public void setUserRights(String user, String... rights) {
        if (rights != null) {
            NutsSecurityEntityConfig security = repo.getConfigManager().getConfig().getSecurity(user);
            for (String right : security.getRights()) {
                security.removeRight(right);
            }
            for (String right : rights) {
                security.addRight(right);
            }
        }
    }

    @Override
    public void addUserRights(String user, String... rights) {
        if (rights != null) {
            NutsSecurityEntityConfig security = repo.getConfigManager().getConfig().getSecurity(user);
            for (String right : rights) {
                security.addRight(right);
            }
        }
    }

    @Override
    public void removeUserRights(String user, String... rights) {
        if (rights != null) {
            NutsSecurityEntityConfig security = repo.getConfigManager().getConfig().getSecurity(user);
            for (String right : rights) {
                security.removeRight(right);
            }
        }
    }

    @Override
    public void setUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsSecurityEntityConfig security = repo.getConfigManager().getConfig().getSecurity(user);
            for (String right : security.getRights()) {
                security.removeRight(right);
            }
            for (String right : groups) {
                security.addGroup(right);
            }
        }
    }

    @Override
    public void addUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsSecurityEntityConfig security = repo.getConfigManager().getConfig().getSecurity(user);
            for (String right : groups) {
                security.addGroup(right);
            }
        }
    }

    @Override
    public void removeUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsSecurityEntityConfig security = repo.getConfigManager().getConfig().getSecurity(user);
            for (String right : groups) {
                security.removeGroup(right);
            }
        }
    }

    @Override
    public void setUserRemoteIdentity(String user, String mappedIdentity) {
        repo.getConfigManager().getConfig().getSecurity(user).setMappedUser(mappedIdentity);
    }

    @Override
    public void setUserCredentials(String username, String password, String oldPassword) {
        if (!isAllowed(NutsConstants.RIGHT_SET_PASSWORD)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_SET_PASSWORD);
        }
        if (CoreStringUtils.isEmpty(username)) {
            username = repo.getWorkspace().getSecurityManager().getCurrentLogin();
        }
        NutsSecurityEntityConfig u = repo.getConfigManager().getConfig().getSecurity(username);
        if (u == null) {
            throw new NutsIllegalArgumentsException("No such user " + username);
        }
        if (!repo.getWorkspace().getSecurityManager().getCurrentLogin().equals(username)) {
            if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
                throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_ADMIN);
            }
        }
        if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
            if (CoreStringUtils.isEmpty(password)) {
                throw new NutsSecurityException("Missing old password");
            }
            //check old password
            if (!CoreStringUtils.isEmpty(u.getCredentials()) && !u.getCredentials().equals(CoreSecurityUtils.evalSHA1(oldPassword))) {
                throw new NutsSecurityException("Invalid password");
            }
        }
        if (CoreStringUtils.isEmpty(password)) {
            throw new NutsIllegalArgumentsException("Missing password");
        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(repo.getRepositoryId(), 20) + " Update user credentials " + username);
        repo.getConfigManager().getConfig().setSecurity(u);
        if (CoreStringUtils.isEmpty(password)) {
            password = null;
        } else {
            password = CoreSecurityUtils.httpEncrypt(password.getBytes(), CoreNutsUtils.DEFAULT_PASSPHRASE);
        }
        u.setCredentials(password);
    }

    @Override
    public NutsUserInfo[] findUsers() {
        List<NutsUserInfo> all = new ArrayList<>();
        for (NutsSecurityEntityConfig secu : repo.getConfigManager().getConfig().getSecurity()) {
            all.add(findUser(secu.getUser()));
        }
        return all.toArray(new NutsUserInfo[all.size()]);
    }

    @Override
    public NutsUserInfo findUser(String username) {
        NutsSecurityEntityConfig security = repo.getConfigManager().getConfig().getSecurity(username);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(security.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsSecurityEntityConfig ss = repo.getConfigManager().getConfig().getSecurity(s);
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
        return security == null ? null : new NutsUserInfoImpl(security, inherited.toArray(new String[inherited.size()]));
    }

}
