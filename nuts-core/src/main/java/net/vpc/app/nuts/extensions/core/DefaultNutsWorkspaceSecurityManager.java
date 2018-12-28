/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
import net.vpc.app.nuts.extensions.util.CoreSecurityUtils;
import net.vpc.common.strings.StringUtils;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;

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
        NutsEffectiveUser adminSecurity = findUser(NutsConstants.USER_ADMIN);
        if (adminSecurity == null || !adminSecurity.hasCredentials()) {
            if (DefaultNutsWorkspace.log.isLoggable(Level.CONFIG)) {
                DefaultNutsWorkspace.log.log(Level.CONFIG, NutsConstants.USER_ADMIN + " user has no credentials. reset to default");
            }
            setUserCredentials(NutsConstants.USER_ADMIN, "admin");
        }
        String credentials = CoreSecurityUtils.evalSHA1(adminPassword);
        if (Objects.equals(credentials, adminPassword)) {
            throw new NutsSecurityException("Invalid credentials");
        }
        boolean activated = false;
        if (ws.getConfigManager().isSecure()) {
            ws.getConfigManager().setSecure(false);
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
        if (!ws.getConfigManager().isSecure()) {
            ws.getConfigManager().setSecure(true);
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
        ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_SET_PASSWORD,"set-user-credentials");
        if (StringUtils.isEmpty(login)) {
            if (!NutsConstants.USER_ANONYMOUS.equals(getCurrentLogin())) {
                login = getCurrentLogin();
            } else {
                throw new NutsIllegalArgumentException("Not logged in");
            }
        }
        NutsUserConfig u = ws.getConfigManager().getUser(login);
        if (u == null) {
            throw new NutsIllegalArgumentException("No such user " + login);
        }
        if (!getCurrentLogin().equals(login)) {
            ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_ADMIN,"set-user-credentials");
        }
        if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
            ws.getExtensionManager().createSupported(NutsAuthenticationAgent.class, u.getAuthenticationAgent())
                    .checkCredentials(
                            u.getCredentials(),
                            u.getAuthenticationAgent(),
                            password,
                            ws.getConfigManager()
                    );
//
//            if (StringUtils.isEmpty(password)) {
//                throw new NutsSecurityException("Missing old password");
//            }
//            //check old password
//            if (StringUtils.isEmpty(u.getCredentials()) || u.getCredentials().equals(CoreSecurityUtils.evalSHA1(password))) {
//                throw new NutsSecurityException("Invalid password");
//            }
        }
        if (StringUtils.isEmpty(password)) {
            throw new NutsIllegalArgumentException("Missing password");
        }
        ws.getConfigManager().setUser(u);
        setUserCredentials(u.getUser(), password);
    }

    @Override
    public void setUserRemoteIdentity(String user, String mappedIdentity) {
        NutsUserConfig security = ws.getConfigManager().getUser(user);
        security.setMappedUser(mappedIdentity);
        ws.getConfigManager().setUser(security);
    }

    @Override
    public void setUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = ws.getConfigManager().getUser(user);
            for (String right : security.getRights()) {
                security.removeRight(right);
            }
            for (String right : rights) {
                if (!StringUtils.isEmpty(right)) {
                    security.addRight(right);
                }
            }
            ws.getConfigManager().setUser(security);
        }
    }

    @Override
    public void addUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = ws.getConfigManager().getUser(user);
            for (String right : rights) {
                if (!StringUtils.isEmpty(right)) {
                    security.addRight(right);
                }
            }
            ws.getConfigManager().setUser(security);
        }
    }

    @Override
    public void removeUserRights(String user, String... rights) {
        if (rights != null) {
            NutsUserConfig security = ws.getConfigManager().getUser(user);
            for (String right : rights) {
                security.removeRight(right);
            }
            ws.getConfigManager().setUser(security);
        }
    }

    @Override
    public void setUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig security = ws.getConfigManager().getUser(user);
            for (String right : security.getRights()) {
                security.removeRight(right);
            }
            for (String right : groups) {
                security.addGroup(right);
            }
            ws.getConfigManager().setUser(security);
        }
    }

    @Override
    public NutsEffectiveUser findUser(String username) {
        NutsUserConfig security = ws.getConfigManager().getUser(username);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(security.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = ws.getConfigManager().getUser(s);
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
        return security == null ? null : new NutsEffectiveUserImpl(security, inherited.toArray(new String[0]));
    }

    @Override
    public NutsEffectiveUser[] findUsers() {
        List<NutsEffectiveUser> all = new ArrayList<>();
        for (NutsUserConfig secu : ws.getConfigManager().getUsers()) {
            all.add(findUser(secu.getUser()));
        }
        return all.toArray(new NutsEffectiveUser[0]);
    }

    @Override
    public void addUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig usr = ws.getConfigManager().getUser(user);
            for (String grp : groups) {
                if (!StringUtils.isEmpty(grp)) {
                    usr.addGroup(grp);
                }
            }
            ws.getConfigManager().setUser(usr);
        }
    }

    @Override
    public void removeUserGroups(String user, String... groups) {
        if (groups != null) {
            NutsUserConfig usr = ws.getConfigManager().getUser(user);
            for (String grp : groups) {
                usr.removeGroup(grp);
            }
            ws.getConfigManager().setUser(usr);
        }
    }

    @Override
    public void addUser(String user, String credentials, String... rights) {
        if (StringUtils.isEmpty(user)) {
            throw new NutsIllegalArgumentException("Invalid user");
        }
        ws.getConfigManager().setUser(new NutsUserConfig(user, null, null, null, null));
        setUserCredentials(user, credentials);
        if (rights != null) {
            NutsUserConfig security = ws.getConfigManager().getUser(user);
            for (String right : rights) {
                if (!StringUtils.isEmpty(right)) {
                    security.addRight(right);
                }
            }
            ws.getConfigManager().setUser(security);
        }
    }

    @Override
    public void setUserAuthenticationAgent(String user, String authenticationAgent) {
        NutsUserConfig security = ws.getConfigManager().getUser(user);
        if (security == null) {
            throw new NutsIllegalArgumentException("User not found " + user);
        }
        if (StringUtils.isEmpty(authenticationAgent)) {
            authenticationAgent = null;
        }
        security.setAuthenticationAgent(authenticationAgent);
        ws.getConfigManager().setUser(security);
    }

    @Override
    public void setUserCredentials(String user, String credentials) {
        NutsUserConfig security = ws.getConfigManager().getUser(user);
        if (security == null) {
            throw new NutsIllegalArgumentException("User not found " + user);
        }
        security.setCredentials(ws.getExtensionManager().createSupported(NutsAuthenticationAgent.class, security.getAuthenticationAgent())
                .setCredentials(credentials, security.getAuthenticationAgent(),
                        ws.getConfigManager()));
        ws.getConfigManager().setUser(security);
    }

    @Override
    public void checkAllowed(String right,String operationName) {
        if(!isAllowed(right)){
            if(StringUtils.isEmpty(operationName)){
                throw new NutsSecurityException(right+" not allowed!");
            }else{
                throw new NutsSecurityException(operationName+": "+right+" not allowed!");
            }
        }
    }

    @Override
    public boolean isAllowed(String right) {
        if (!ws.getConfigManager().isSecure()) {
            return true;
        }
        String name = getCurrentLogin();
        if (StringUtils.isEmpty(name)) {
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
            NutsUserConfig s = ws.getConfigManager().getUser(n);
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
        return logins.toArray(new String[0]);
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
                if (!StringUtils.isEmpty(name)) {
                    if (!StringUtils.isEmpty(name)) {
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
        NutsWorkspaceLoginModule.configure(ws); //initialize it
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
