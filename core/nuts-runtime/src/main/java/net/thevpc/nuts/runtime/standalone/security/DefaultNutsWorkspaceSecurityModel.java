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
 * <p>
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
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootManager;
import net.thevpc.nuts.runtime.standalone.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.config.NutsWorkspaceConfigSecurity;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.DefaultNutsAddUserCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.DefaultNutsRemoveUserCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.DefaultNutsUpdateUserCommand;
import net.thevpc.nuts.spi.NutsAuthenticationAgent;

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
 * @author thevpc
 */
public class DefaultNutsWorkspaceSecurityModel {

    private final ThreadLocal<Stack<LoginContext>> loginContextStack = new ThreadLocal<>();
    private final DefaultNutsWorkspace ws;
    private final WrapperNutsAuthenticationAgent agent;
    private final Map<String, NutsAuthorizations> authorizations = new HashMap<>();
    private NutsLogger LOG;

    public DefaultNutsWorkspaceSecurityModel(final DefaultNutsWorkspace ws) {
        this.ws = ws;
        this.agent = new WrapperNutsAuthenticationAgent(ws, (session) -> session.env().getEnvMap(), (x, s) -> getAuthenticationAgent(x, s));
        NutsWorkspaceUtils.defaultSession(ws).events().addWorkspaceListener(new ClearAuthOnWorkspaceChange());
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsWorkspaceSecurityModel.class,session);
        }
        return LOG;
    }

    public void login(final String username, final char[] password, NutsSession session) {
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
    }


    public boolean setSecureMode(boolean secure, char[] adminPassword, NutsSession session) {
        if (secure) {
            return switchSecureMode(adminPassword, session);
        } else {
            return switchUnsecureMode(adminPassword, session);
        }
    }

    public boolean switchUnsecureMode(char[] adminPassword, NutsSession session) {
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        NutsUser adminSecurity = findUser(NutsConstants.Users.ADMIN, session);
        if (adminSecurity == null || !adminSecurity.hasCredentials()) {
            if (_LOG(session).isLoggable(Level.CONFIG)) {
                _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.WARNING)
                        .log(NutsMessage.jstyle("{0} user has no credentials. reset to default",NutsConstants.Users.ADMIN));
            }
            NutsUserConfig u = NutsWorkspaceConfigManagerExt.of(session.config()).getModel().getUser(NutsConstants.Users.ADMIN, session);
            u.setCredentials(CoreStringUtils.chrToStr(createCredentials("admin".toCharArray(), false, null, session)));
            NutsWorkspaceConfigManagerExt.of(session.config()).getModel().setUser(u, session);
        }

        char[] credentials = CoreIOUtils.evalSHA1(adminPassword,session);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NutsSecurityException(session, NutsMessage.plain("invalid credentials"));
        }
        Arrays.fill(credentials, '\0');
        boolean activated = false;
        if (isSecure(session)) {
            NutsWorkspaceConfigManagerExt.of(session.config()).getModel().setSecure(false, session);
            activated = true;
        }
        return activated;
    }

    public boolean switchSecureMode(char[] adminPassword, NutsSession session) {
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        boolean deactivated = false;
        char[] credentials = CoreIOUtils.evalSHA1(adminPassword,session);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NutsSecurityException(session, NutsMessage.plain("invalid credentials"));
        }
        Arrays.fill(credentials, '\0');
        if (!isSecure(session)) {
            NutsWorkspaceConfigManagerExt.of(session.config()).getModel().setSecure(true, session);
            deactivated = true;
        }
        return deactivated;
    }


    public boolean isAdmin(NutsSession session) {
        return NutsConstants.Users.ADMIN.equals(getCurrentUsername(session));
    }


    public void logout(NutsSession session) {
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null || r.isEmpty()) {
            throw new NutsLoginException(session, NutsMessage.cstyle("not logged in"));
        }
        try {
            LoginContext loginContext = r.pop();
            loginContext.logout();
        } catch (LoginException ex) {
            throw new NutsLoginException(session, NutsMessage.plain("login failed"), ex);
        }
    }


    public NutsUser findUser(String username, NutsSession session) {
        NutsUserConfig security = NutsWorkspaceConfigManagerExt.of(session.config()).getModel().getUser(username, session);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(security.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = NutsWorkspaceConfigManagerExt.of(session.config()).getModel().getUser(s, session);
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


    public NutsUser[] findUsers(NutsSession session) {
        List<NutsUser> all = new ArrayList<>();
        for (NutsUserConfig secu : NutsWorkspaceConfigManagerExt.of(session.config()).getModel().getUsers(session)) {
            all.add(findUser(secu.getUser(), session));
        }
        return all.toArray(new NutsUser[0]);
    }


    public NutsAddUserCommand addUser(String name, NutsSession session) {
        return new DefaultNutsAddUserCommand(ws).setUsername(name).setSession(session);
    }


    public NutsUpdateUserCommand updateUser(String name, NutsSession session) {
        return new DefaultNutsUpdateUserCommand(ws).setUsername(name).setSession(session);
    }


    public NutsRemoveUserCommand removeUser(String name, NutsSession session) {
        return new DefaultNutsRemoveUserCommand(ws).setUsername(name).setSession(session);
    }


    public void checkAllowed(String permission, String operationName, NutsSession session) {
        if (!isAllowed(permission, session)) {
            if (NutsBlankable.isBlank(operationName)) {
                throw new NutsSecurityException(session, NutsMessage.cstyle("%s not allowed!", permission));
            } else {
                throw new NutsSecurityException(session, NutsMessage.cstyle("%s : %s not allowed!", operationName, permission));
            }
        }
    }

    private NutsAuthorizations getAuthorizations(String n, NutsSession session) {
        NutsAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NutsUserConfig s = NutsWorkspaceConfigManagerExt.of(session.config()).getModel().getUser(n, session);
        if (s != null) {
            String[] rr = s.getPermissions();
            aa = new NutsAuthorizations(Arrays.asList(rr == null ? new String[0] : rr));
            authorizations.put(n, aa);
        } else {
            aa = new NutsAuthorizations(Collections.emptyList());
        }
        return aa;
    }


    public boolean isAllowed(String permission, NutsSession session) {
        if (!isSecure(session)) {
            return true;
        }
        String name = getCurrentUsername(session);
        if (NutsBlankable.isBlank(name)) {
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
            NutsUserConfig uc = NutsWorkspaceConfigManagerExt.of(session.config()).getModel().getUser(n, session);
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
            if (isInitializing()) {
                logins.add(NutsConstants.Users.ADMIN);
            } else {
                logins.add(NutsConstants.Users.ANONYMOUS);
            }
        }
        return logins.toArray(new String[0]);
    }

    private boolean isInitializing() {
        return ((DefaultNutsBootManager) ws.boot()).getModel().isInitializing();
    }


    public String getCurrentUsername(NutsSession session) {
        if (isInitializing()) {
            return NutsConstants.Users.ADMIN;
        }
        String name = null;
        Subject currentSubject = getLoginSubject();
        if (currentSubject != null) {
            for (Principal principal : currentSubject.getPrincipals()) {
                name = principal.getName();
                if (!NutsBlankable.isBlank(name)) {
                    if (!NutsBlankable.isBlank(name)) {
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


    public void login(CallbackHandler handler, NutsSession session) {
        NutsWorkspaceLoginModule.configure(session); //initialize it
        //        if (!NutsConstants.Misc.USER_ANONYMOUS.equals(getCurrentLogin())) {
        //            throw new NutsLoginException("Already logged in");
        //        }
        LoginContext login;
        try {
            login = CorePlatformUtils.runWithinLoader(new Callable<LoginContext>() {

                public LoginContext call() throws Exception {
                    return new LoginContext("nuts", handler);
                }
            }, NutsWorkspaceLoginModule.class.getClassLoader(), session);
            login.login();
        } catch (LoginException ex) {
            throw new NutsLoginException(session, NutsMessage.plain("login failed"), ex);
        }
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null) {
            r = new Stack<>();
            loginContextStack.set(r);
        }
        r.push(login);
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


    public NutsAuthenticationAgent getAuthenticationAgent(String authenticationAgentId, NutsSession session) {
        authenticationAgentId = NutsUtilStrings.trim(authenticationAgentId);
        if (NutsBlankable.isBlank(authenticationAgentId)) {
            authenticationAgentId = NutsWorkspaceConfigManagerExt.of(session.config())
                    .getModel().getStoredConfigSecurity().getAuthenticationAgent();
        }
        NutsAuthenticationAgent a = NutsWorkspaceConfigManagerExt.of(session.config())
                .getModel().createAuthenticationAgent(authenticationAgentId, session);
        return a;
    }


    public void setAuthenticationAgent(String authenticationAgentId, NutsSession session) {

        DefaultNutsWorkspaceConfigModel cc = NutsWorkspaceConfigManagerExt.of(session.config()).getModel();

        if (cc.createAuthenticationAgent(authenticationAgentId, session) == null) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("unsupported Authentication Agent %s", authenticationAgentId)
            );
        }

        NutsWorkspaceConfigSecurity conf = cc.getStoredConfigSecurity();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgentId)) {
            conf.setAuthenticationAgent(authenticationAgentId);
            cc.fireConfigurationChanged("authentication-agent", session, ConfigEventType.SECURITY);
        }
    }


    public boolean isSecure(NutsSession session) {
        return NutsWorkspaceConfigManagerExt.of(session.config()).getModel().getStoredConfigSecurity().isSecure();
    }


    public void checkCredentials(char[] credentialsId, char[] password, NutsSession session) throws NutsSecurityException {
        agent.checkCredentials(credentialsId, password, session);
    }


    public char[] getCredentials(char[] credentialsId, NutsSession session) {
        return agent.getCredentials(credentialsId, session);
    }


    public boolean removeCredentials(char[] credentialsId, NutsSession session) {
        return agent.removeCredentials(credentialsId, session);
    }


    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NutsSession session) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId, session);
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    private class ClearAuthOnWorkspaceChange implements NutsWorkspaceListener {

        public ClearAuthOnWorkspaceChange() {
        }

        @Override
        public void onConfigurationChanged(NutsWorkspaceEvent event) {
            authorizations.clear();
        }
    }

}
