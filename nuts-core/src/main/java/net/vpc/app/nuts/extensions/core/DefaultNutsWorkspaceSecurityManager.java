/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsLoginException;
import net.vpc.app.nuts.NutsSecurityEntityConfig;
import net.vpc.app.nuts.NutsSecurityException;
import net.vpc.app.nuts.NutsUserInfo;
import net.vpc.app.nuts.NutsWorkspaceConfig;
import net.vpc.app.nuts.NutsWorkspaceSecurityManager;
import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
import net.vpc.app.nuts.extensions.util.CoreSecurityUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 *
 * @author vpc
 */
class DefaultNutsWorkspaceSecurityManager implements NutsWorkspaceSecurityManager {

    private ThreadLocal<Stack<LoginContext>> loginContextStack = new ThreadLocal<>();
    private final DefaultNutsWorkspace ws;

    protected DefaultNutsWorkspaceSecurityManager(final DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public void login(final String login, final String password) {
        login(new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        NameCallback nameCallback = (NameCallback) callback;
                        nameCallback.setName(login);
                    } else if (callback instanceof PasswordCallback) {
                        PasswordCallback passwordCallback = (PasswordCallback) callback;
                        passwordCallback.setPassword(password == null ? null : password.toCharArray());
                    } else {
                        throw new UnsupportedCallbackException(callback, "The submitted Callback is unsupported");
                    }
                }
            }
        });
    }

    @Override
    public boolean switchUnsecureMode(String adminPassword) {
        if (adminPassword == null) {
            adminPassword = "";
        }
        NutsUserInfo adminSecurity = findUser(NutsConstants.USER_ADMIN);
        if (adminSecurity == null || !adminSecurity.hasCredentials()) {
            DefaultNutsWorkspace.log.log(Level.CONFIG, NutsConstants.USER_ADMIN + " user has no credentials. reset to default");
            setUserCredentials(NutsConstants.USER_ADMIN, "admin");
        }
        String credentials = CoreSecurityUtils.evalSHA1(adminPassword);
        if (Objects.equals(credentials, adminPassword)) {
            throw new NutsSecurityException("Invalid credentials");
        }
        boolean activated = false;
        if (ws.getConfigManager().getConfig().isSecure()) {
            ws.getConfigManager().getConfig().setSecure(false);
            activated = true;
        }
        return activated;
    }

    @Override
    public boolean isAdmin() {
        return NutsConstants.USER_ADMIN.equals(getCurrentLogin());
    }

    @Override
    public boolean switchSecureMode(String adminPassword) {
        if (adminPassword == null) {
            adminPassword = "";
        }
        boolean deactivated = false;
        String credentials = CoreSecurityUtils.evalSHA1(adminPassword);
        if (Objects.equals(credentials, adminPassword)) {
            throw new NutsSecurityException("Invalid credentials");
        }
        if (!ws.getConfigManager().getConfig().isSecure()) {
            ws.getConfigManager().getConfig().setSecure(true);
            deactivated = true;
        }
        return deactivated;
    }

    @Override
    public void logout() {
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null || r.isEmpty()) {
            throw new NutsLoginException("Not logged in");
        }
        try {
            LoginContext loginContext = r.pop();
            loginContext.logout();
        } catch (LoginException ex) {
            throw new NutsLoginException(ex);
        }
    }

    @Override
    public void setUserCredentials(String login, String password, String oldPassword) {
        if (!isAllowed(NutsConstants.RIGHT_SET_PASSWORD)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_SET_PASSWORD);
        }
        if (CoreStringUtils.isEmpty(login)) {
            if (!NutsConstants.USER_ANONYMOUS.equals(getCurrentLogin())) {
                login = getCurrentLogin();
            } else {
                throw new NutsIllegalArgumentException("Not logged in");
            }
        }
        NutsSecurityEntityConfig u = ws.getConfigManager().getConfig().getSecurity(login);
        if (u == null) {
            throw new NutsIllegalArgumentException("No such user " + login);
        }
        if (!getCurrentLogin().equals(login)) {
            if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
                throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_ADMIN);
            }
        }
        if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
            if (CoreStringUtils.isEmpty(password)) {
                throw new NutsSecurityException("Missing old password");
            }
            //check old password
            if (CoreStringUtils.isEmpty(u.getCredentials()) || u.getCredentials().equals(CoreSecurityUtils.evalSHA1(password))) {
                throw new NutsSecurityException("Invalid password");
            }
        }
        if (CoreStringUtils.isEmpty(password)) {
            throw new NutsIllegalArgumentException("Missing password");
        }
        ws.getConfigManager().getConfig().setSecurity(u);
        setUserCredentials(u.getUser(), password);
    }

    @Override
    public void setUserRemoteIdentity(String user, String mappedIdentity) {
        ws.getConfigManager().getConfig().getSecurity(user).setMappedUser(mappedIdentity);
    }

    @Override
    public void setUserRights(String user, String... rights) {
        if (rights != null) {
            NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(user);
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
            NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(user);
            for (String right : rights) {
                security.addRight(right);
            }
        }
    }

    @Override
    public void removeUserRights(String user, String... rights) {
        if (rights != null) {
            NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(user);
            for (String right : rights) {
                security.removeRight(right);
            }
        }
    }

    @Override
    public void setUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(user);
            for (String right : security.getRights()) {
                security.removeRight(right);
            }
            for (String right : groups) {
                security.addGroup(right);
            }
        }
    }

    @Override
    public NutsUserInfo findUser(String username) {
        NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(username);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(security.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsSecurityEntityConfig ss = ws.getConfigManager().getConfig().getSecurity(s);
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

    @Override
    public NutsUserInfo[] findUsers() {
        List<NutsUserInfo> all = new ArrayList<>();
        for (NutsSecurityEntityConfig secu : ws.getConfigManager().getConfig().getSecurity()) {
            all.add(findUser(secu.getUser()));
        }
        return all.toArray(new NutsUserInfo[all.size()]);
    }

    @Override
    public void addUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(user);
            for (String right : groups) {
                security.addGroup(right);
            }
        }
    }

    @Override
    public void removeUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(user);
            for (String right : groups) {
                security.removeGroup(right);
            }
        }
    }

    @Override
    public void addUser(String user, String credentials, String... rights) {
        if (CoreStringUtils.isEmpty(user)) {
            throw new NutsIllegalArgumentException("Invalid user");
        }
        ws.getConfigManager().getConfig().setSecurity(new NutsSecurityEntityConfigImpl(user, null, null, null));
        setUserCredentials(user, credentials);
        if (rights != null) {
            NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(user);
            for (String right : rights) {
                security.addRight(right);
            }
        }
    }

    @Override
    public void setUserCredentials(String user, String credentials) {
        NutsSecurityEntityConfig security = ws.getConfigManager().getConfig().getSecurity(user);
        if (security == null) {
            throw new NutsIllegalArgumentException("User not found " + user);
        }
        if (CoreStringUtils.isEmpty(credentials)) {
            credentials = null;
        } else {
            credentials = CoreSecurityUtils.evalSHA1(credentials);
        }
        security.setCredentials(credentials);
    }

    @Override
    public boolean isAllowed(String right) {
        NutsWorkspaceConfig c = ws.getConfigManager().getConfig();
        if (!c.isSecure()) {
            return true;
        }
        String name = getCurrentLogin();
        if (CoreStringUtils.isEmpty(name)) {
            return false;
        }
        if (NutsConstants.USER_ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsSecurityEntityConfig s = c.getSecurity(n);
            if (s != null) {
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
        return false;
    }

    @Override
    public String[] getCurrentLoginStack() {
        List<String> logins = new ArrayList<String>();
        Stack<LoginContext> c = loginContextStack.get();
        if (c != null) {
            for (LoginContext loginContext : c) {
                Subject subject = loginContext.getSubject();
                if (subject != null) {
                    for (Principal principal : subject.getPrincipals()) {
                        logins.add(principal.getName());
                        break;
                    }
                }
            }
        }
        if (logins.isEmpty()) {
            if (ws.isInitializing()) {
                logins.add(NutsConstants.USER_ADMIN);
            } else {
                logins.add(NutsConstants.USER_ANONYMOUS);
            }
        }
        return logins.toArray(new String[logins.size()]);
    }

    @Override
    public String getCurrentLogin() {
        if (ws.isInitializing()) {
            return NutsConstants.USER_ADMIN;
        }
        String name = null;
        Subject currentSubject = getLoginSubject();
        if (currentSubject != null) {
            for (Principal principal : currentSubject.getPrincipals()) {
                name = principal.getName();
                if (!CoreStringUtils.isEmpty(name)) {
                    if (!CoreStringUtils.isEmpty(name)) {
                        return name;
                    }
                }
            }
        }
        return NutsConstants.USER_ANONYMOUS;
    }

    private Subject getLoginSubject() {
        LoginContext c = getLoginContext();
        if (c == null) {
            return null;
        }
        return c.getSubject();
    }

    @Override
    public String login(CallbackHandler handler) {
        NutsWorkspaceLoginModule.install(); //initialize it
        //        if (!NutsConstants.USER_ANONYMOUS.equals(getCurrentLogin())) {
        //            throw new NutsLoginException("Already logged in");
        //        }
        LoginContext login;
        try {
            login = CorePlatformUtils.runWithinLoader(new Callable<LoginContext>() {
                @Override
                public LoginContext call() throws Exception {
                    return new LoginContext("nuts", handler);
                }
            }, NutsWorkspaceLoginModule.class.getClassLoader());
            login.login();
        } catch (LoginException ex) {
            throw new NutsLoginException(ex);
        }
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null) {
            r = new Stack<>();
            loginContextStack.set(r);
        }
        r.push(login);
        return getCurrentLogin();
    }

    private LoginContext getLoginContext() {
        Stack<LoginContext> c = loginContextStack.get();
        if (c == null) {
            return null;
        }
        if (c.isEmpty()) {
            return null;
        }
        return c.peek();
    }

}
