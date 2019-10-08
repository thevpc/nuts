/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.security;

import net.vpc.app.nuts.main.config.DefaultNutsWorkspaceConfigManager;
import net.vpc.app.nuts.main.config.NutsWorkspaceConfigSecurity;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.runtime.util.common.CorePlatformUtils;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import net.vpc.app.nuts.main.wscommands.DefaultNutsAddUserCommand;
import net.vpc.app.nuts.main.wscommands.DefaultNutsRemoveUserCommand;
import net.vpc.app.nuts.main.wscommands.DefaultNutsUpdateUserCommand;
import net.vpc.app.nuts.main.DefaultNutsWorkspace;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsWorkspaceSecurityManager implements NutsWorkspaceSecurityManager {

    public final NutsLogger LOG;
    private final ThreadLocal<Stack<LoginContext>> loginContextStack = new ThreadLocal<>();
    private final DefaultNutsWorkspace ws;
    private final WrapperNutsAuthenticationAgent agent;
    private final Map<String, NutsAuthorizations> authorizations = new HashMap<>();

    public DefaultNutsWorkspaceSecurityManager(final DefaultNutsWorkspace ws) {
        this.ws = ws;
        LOG=ws.log().of(DefaultNutsWorkspaceSecurityManager.class);
        this.agent = new WrapperNutsAuthenticationAgent(ws, ()->ws.config().getEnv(), x -> getAuthenticationAgent(x));
        ws.addWorkspaceListener(new NutsWorkspaceListener() {
            @Override
            public void onConfigurationChanged(NutsWorkspaceEvent event) {
                authorizations.clear();
            }
        });
    }

    @Override
    public NutsWorkspaceSecurityManager login(final String username, final char[] password) {
        login(new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        NameCallback nameCallback = (NameCallback) callback;
                        nameCallback.setName(username);
                    } else if (callback instanceof PasswordCallback) {
                        PasswordCallback passwordCallback = (PasswordCallback) callback;
                        passwordCallback.setPassword(password);
                    } else {
                        throw new UnsupportedCallbackException(callback, "The submitted Callback is unsupported");
                    }
                }
            }
        });
        return this;
    }

    @Override
    public boolean setSecureMode(boolean secure, char[] adminPassword, NutsUpdateOptions options) {
        if(secure){
            return switchSecureMode(adminPassword,options);
        }else{
            return switchUnsecureMode(adminPassword,options);
        }
    }

    public boolean switchUnsecureMode(char[] adminPassword, NutsUpdateOptions options) {
        options= CoreNutsUtils.validate(options,ws);
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        NutsUser adminSecurity = findUser(NutsConstants.Users.ADMIN);
        if (adminSecurity == null || !adminSecurity.hasCredentials()) {
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.log(Level.CONFIG, NutsLogVerb.WARNING, NutsConstants.Users.ADMIN + " user has no credentials. reset to default");
            }
            NutsUserConfig u = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(NutsConstants.Users.ADMIN);
            u.setCredentials(CoreStringUtils.chrToStr(createCredentials("admin".toCharArray(), false, null)));
            NutsWorkspaceConfigManagerExt.of(ws.config()).setUser(u, options);
        }

        char[] credentials = CoreIOUtils.evalSHA1(adminPassword);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NutsSecurityException(ws, "Invalid credentials");
        }
        Arrays.fill(credentials, '\0');
        boolean activated = false;
        if (isSecure()) {
            NutsWorkspaceConfigManagerExt.of(ws.config()).setSecure(false, options);
            activated = true;
        }
        return activated;
    }

    public boolean switchSecureMode(char[] adminPassword, NutsUpdateOptions options) {
        options= CoreNutsUtils.validate(options,ws);
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        boolean deactivated = false;
        char[] credentials = CoreIOUtils.evalSHA1(adminPassword);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NutsSecurityException(ws, "Invalid credentials");
        }
        Arrays.fill(credentials, '\0');
        if (!isSecure()) {
            NutsWorkspaceConfigManagerExt.of(ws.config()).setSecure(true, options);
            deactivated = true;
        }
        return deactivated;
    }

    @Override
    public boolean isAdmin() {
        return NutsConstants.Users.ADMIN.equals(getCurrentUsername());
    }

    @Override
    public NutsWorkspaceSecurityManager logout() {
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null || r.isEmpty()) {
            throw new NutsLoginException(ws, "Not logged in");
        }
        try {
            LoginContext loginContext = r.pop();
            loginContext.logout();
        } catch (LoginException ex) {
            throw new NutsLoginException(ws, ex);
        }
        return this;
    }

    @Override
    public NutsUser findUser(String username) {
        NutsUserConfig security = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(username);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(security.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(s);
                if (ss != null) {
                    inherited.addAll(Arrays.asList(ss.getPermissions()));
                    for (String group : ss.getGroups()) {
                        if (!visited.contains(group)) {
                            curr.push(group);
                        }
                    }
                }
            }
        }
        return security == null ? null : new DefaultNutsUser(security, inherited.toArray(new String[0]));
    }

    @Override
    public NutsUser[] findUsers() {
        List<NutsUser> all = new ArrayList<>();
        for (NutsUserConfig secu : NutsWorkspaceConfigManagerExt.of(ws.config()).getUsers()) {
            all.add(findUser(secu.getUser()));
        }
        return all.toArray(new NutsUser[0]);
    }

    @Override
    public NutsAddUserCommand addUser(String name) {
        return new DefaultNutsAddUserCommand(ws).username(name);
    }

    @Override
    public NutsUpdateUserCommand updateUser(String name) {
        return new DefaultNutsUpdateUserCommand(ws).login(name);
    }

    @Override
    public NutsRemoveUserCommand removeUser(String name) {
        return new DefaultNutsRemoveUserCommand(ws).login(name);
    }

    @Override
    public NutsWorkspaceSecurityManager checkAllowed(String permission, String operationName) {
        if (!isAllowed(permission)) {
            if (CoreStringUtils.isBlank(operationName)) {
                throw new NutsSecurityException(ws, permission + " not allowed!");
            } else {
                throw new NutsSecurityException(ws, operationName + ": " + permission + " not allowed!");
            }
        }
        return this;
    }

    private NutsAuthorizations getAuthorizations(String n) {
        NutsAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NutsUserConfig s = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(n);
        if (s != null) {
            String[] rr = s.getPermissions();
            aa = new NutsAuthorizations(Arrays.asList(rr == null ? new String[0] : rr));
            authorizations.put(n, aa);
        } else {
            aa = new NutsAuthorizations(Collections.emptyList());
        }
        return aa;
    }

    @Override
    public boolean isAllowed(String permission) {
        if (!isSecure()) {
            return true;
        }
        String name = getCurrentUsername();
        if (CoreStringUtils.isBlank(name)) {
            return false;
        }
        if (NutsConstants.Users.ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsAuthorizations s = getAuthorizations(n);
            Boolean ea = s.explicitAccept(permission);
            if (ea != null) {
                return ea;
            }
            NutsUserConfig uc = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(n);
            if (uc != null) {
                for (String g : uc.getGroups()) {
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
                logins.add(NutsConstants.Users.ADMIN);
            } else {
                logins.add(NutsConstants.Users.ANONYMOUS);
            }
        }
        return logins.toArray(new String[0]);
    }

    @Override
    public String getCurrentUsername() {
        if (ws.isInitializing()) {
            return NutsConstants.Users.ADMIN;
        }
        String name = null;
        Subject currentSubject = getLoginSubject();
        if (currentSubject != null) {
            for (Principal principal : currentSubject.getPrincipals()) {
                name = principal.getName();
                if (!CoreStringUtils.isBlank(name)) {
                    if (!CoreStringUtils.isBlank(name)) {
                        return name;
                    }
                }
            }
        }
        return NutsConstants.Users.ANONYMOUS;
    }

    private Subject getLoginSubject() {
        LoginContext c = getLoginContext();
        if (c == null) {
            return null;
        }
        return c.getSubject();
    }

    @Override
    public NutsWorkspaceSecurityManager login(CallbackHandler handler) {
        NutsWorkspaceLoginModule.configure(ws); //initialize it
        //        if (!NutsConstants.Misc.USER_ANONYMOUS.equals(getCurrentLogin())) {
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
            throw new NutsLoginException(ws, ex);
        }
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null) {
            r = new Stack<>();
            loginContextStack.set(r);
        }
        r.push(login);
        return this;
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

    @Override
    public NutsAuthenticationAgent getAuthenticationAgent(String authenticationAgentId) {
        authenticationAgentId = CoreStringUtils.trim(authenticationAgentId);
        if (CoreStringUtils.isBlank(authenticationAgentId)) {
            authenticationAgentId = NutsWorkspaceConfigManagerExt.of(ws.config())
                    .getStoredConfigSecurity().getAuthenticationAgent();
        }
        NutsAuthenticationAgent a = NutsWorkspaceConfigManagerExt.of(ws.config()).createAuthenticationAgent(authenticationAgentId);
        return a;
    }

    @Override
    public NutsWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgentId, NutsUpdateOptions options) {
        options= CoreNutsUtils.validate(options,ws);

        NutsWorkspaceConfigManagerExt cc = NutsWorkspaceConfigManagerExt.of(ws.config());

        if (cc.createAuthenticationAgent(authenticationAgentId) == null) {
            throw new NutsIllegalArgumentException(ws, "Unsupported Authentication Agent " + authenticationAgentId);
        }

        NutsWorkspaceConfigSecurity conf = cc.getStoredConfigSecurity();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgentId)) {
            conf.setAuthenticationAgent(authenticationAgentId);
            cc.fireConfigurationChanged("authentication-agent",options.getSession(), DefaultNutsWorkspaceConfigManager.ConfigEventType.SECURITY);
        }
        return this;
    }

    @Override
    public boolean isSecure() {
        return NutsWorkspaceConfigManagerExt.of(ws.config()).getStoredConfigSecurity().isSecure();
    }

    @Override
    public String currentUsername() {
        return getCurrentUsername();
    }

    @Override
    public String[] currentLoginStack() {
        return getCurrentLoginStack();
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password) throws NutsSecurityException {
        agent.checkCredentials(credentialsId, password);
    }

    @Override
    public char[] getCredentials(char[] credentialsId) {
        return agent.getCredentials(credentialsId);
    }

    @Override
    public boolean removeCredentials(char[] credentialsId) {
        return agent.removeCredentials(credentialsId);
    }

    @Override
    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId);
    }

}
