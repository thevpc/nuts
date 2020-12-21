/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.main.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.main.config.NutsWorkspaceConfigSecurity;
import net.thevpc.nuts.runtime.standalone.log.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CorePlatformUtils;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.standalone.main.wscommands.DefaultNutsAddUserCommand;
import net.thevpc.nuts.runtime.standalone.main.wscommands.DefaultNutsRemoveUserCommand;
import net.thevpc.nuts.runtime.standalone.main.wscommands.DefaultNutsUpdateUserCommand;
import net.thevpc.nuts.runtime.standalone.main.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;

/**
 *
 * @author thevpc
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
        this.agent = new WrapperNutsAuthenticationAgent(ws, ()->ws.env().toMap(), (x,s) -> getAuthenticationAgent(x, s));
        ws.events().addWorkspaceListener(new NutsWorkspaceListener() {
            @Override
            public void onConfigurationChanged(NutsWorkspaceEvent event) {
                authorizations.clear();
            }
        });
    }

    @Override
    public NutsWorkspaceSecurityManager login(final String username, final char[] password, NutsSession session) {
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
                        throw new UnsupportedCallbackException(callback, "the submitted Callback is unsupported");
                    }
                }
            }
        }, session);
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
        NutsUser adminSecurity = findUser(NutsConstants.Users.ADMIN, options.getSession());
        if (adminSecurity == null || !adminSecurity.hasCredentials()) {
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().level(Level.CONFIG).verb(NutsLogVerb.WARNING).log( NutsConstants.Users.ADMIN + " user has no credentials. reset to default");
            }
            NutsUserConfig u = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(NutsConstants.Users.ADMIN, options.getSession());
            u.setCredentials(CoreStringUtils.chrToStr(createCredentials("admin".toCharArray(), false, null, options.getSession())));
            NutsWorkspaceConfigManagerExt.of(ws.config()).setUser(u, options);
        }

        char[] credentials = CoreIOUtils.evalSHA1(adminPassword);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NutsSecurityException(ws, "Invalid credentials");
        }
        Arrays.fill(credentials, '\0');
        boolean activated = false;
        if (isSecure(options.getSession())) {
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
            throw new NutsSecurityException(ws, "invalid credentials");
        }
        Arrays.fill(credentials, '\0');
        if (!isSecure(options.getSession())) {
            NutsWorkspaceConfigManagerExt.of(ws.config()).setSecure(true, options);
            deactivated = true;
        }
        return deactivated;
    }

    @Override
    public boolean isAdmin(NutsSession session) {
        return NutsConstants.Users.ADMIN.equals(getCurrentUsername(session));
    }

    @Override
    public NutsWorkspaceSecurityManager logout(NutsSession session) {
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null || r.isEmpty()) {
            throw new NutsLoginException(ws, "not logged in");
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
    public NutsUser findUser(String username, NutsSession session) {
        NutsUserConfig security = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(username, session);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(security.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(s, session);
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
    public NutsUser[] findUsers(NutsSession session) {
        List<NutsUser> all = new ArrayList<>();
        for (NutsUserConfig secu : NutsWorkspaceConfigManagerExt.of(ws.config()).getUsers(session)) {
            all.add(findUser(secu.getUser(), session));
        }
        return all.toArray(new NutsUser[0]);
    }

    @Override
    public NutsAddUserCommand addUser(String name, NutsSession session) {
        return new DefaultNutsAddUserCommand(ws).setUsername(name).setSession(session);
    }

    @Override
    public NutsUpdateUserCommand updateUser(String name, NutsSession session) {
        return new DefaultNutsUpdateUserCommand(ws).setUsername(name).setSession(session);
    }

    @Override
    public NutsRemoveUserCommand removeUser(String name, NutsSession session) {
        return new DefaultNutsRemoveUserCommand(ws).setUsername(name).setSession(session);
    }

    @Override
    public NutsWorkspaceSecurityManager checkAllowed(String permission, String operationName, NutsSession session) {
        if (!isAllowed(permission, session)) {
            if (CoreStringUtils.isBlank(operationName)) {
                throw new NutsSecurityException(ws, permission + " not allowed!");
            } else {
                throw new NutsSecurityException(ws, operationName + ": " + permission + " not allowed!");
            }
        }
        return this;
    }

    private NutsAuthorizations getAuthorizations(String n, NutsSession session) {
        NutsAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NutsUserConfig s = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(n, session);
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
    public boolean isAllowed(String permission, NutsSession session) {
        if (!isSecure(session)) {
            return true;
        }
        String name = getCurrentUsername(session);
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
            NutsAuthorizations s = getAuthorizations(n, session);
            Boolean ea = s.explicitAccept(permission);
            if (ea != null) {
                return ea;
            }
            NutsUserConfig uc = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(n, session);
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
    public String[] getCurrentLoginStack(NutsSession session) {
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
    public String getCurrentUsername(NutsSession session) {
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
    public NutsWorkspaceSecurityManager login(CallbackHandler handler, NutsSession session) {
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
    public NutsAuthenticationAgent getAuthenticationAgent(String authenticationAgentId, NutsSession session) {
        authenticationAgentId = CoreStringUtils.trim(authenticationAgentId);
        if (CoreStringUtils.isBlank(authenticationAgentId)) {
            authenticationAgentId = NutsWorkspaceConfigManagerExt.of(ws.config())
                    .getStoredConfigSecurity().getAuthenticationAgent();
        }
        NutsAuthenticationAgent a = NutsWorkspaceConfigManagerExt.of(ws.config()).createAuthenticationAgent(authenticationAgentId, session);
        return a;
    }

    @Override
    public NutsWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgentId, NutsUpdateOptions options) {
        options= CoreNutsUtils.validate(options,ws);

        NutsWorkspaceConfigManagerExt cc = NutsWorkspaceConfigManagerExt.of(ws.config());

        if (cc.createAuthenticationAgent(authenticationAgentId,options.getSession()) == null) {
            throw new NutsIllegalArgumentException(ws, "unsupported Authentication Agent " + authenticationAgentId);
        }

        NutsWorkspaceConfigSecurity conf = cc.getStoredConfigSecurity();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgentId)) {
            conf.setAuthenticationAgent(authenticationAgentId);
            cc.fireConfigurationChanged("authentication-agent",options.getSession(), ConfigEventType.SECURITY);
        }
        return this;
    }

    @Override
    public boolean isSecure(NutsSession session) {
        return NutsWorkspaceConfigManagerExt.of(ws.config()).getStoredConfigSecurity().isSecure();
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password, NutsSession session) throws NutsSecurityException {
        agent.checkCredentials(credentialsId, password, session);
    }

    @Override
    public char[] getCredentials(char[] credentialsId, NutsSession session) {
        return agent.getCredentials(credentialsId, session);
    }

    @Override
    public boolean removeCredentials(char[] credentialsId, NutsSession session) {
        return agent.removeCredentials(credentialsId, session);
    }

    @Override
    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NutsSession session) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId, session);
    }

}
