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
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigSecurity;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.spi.NAuthenticationAgent;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

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
 * @author thevpc
 */
public class DefaultNWorkspaceSecurityModel {

    private final ThreadLocal<Stack<LoginContext>> loginContextStack = new ThreadLocal<>();
    private final DefaultNWorkspace ws;
    private final WrapperNAuthenticationAgent agent;
    private final Map<String, NAuthorizations> authorizations = new HashMap<>();
    private NLog LOG;

    public DefaultNWorkspaceSecurityModel(final DefaultNWorkspace ws) {
        this.ws = ws;
        this.agent = new WrapperNAuthenticationAgent(ws, (session) -> NConfigs.of(session).getConfigMap(), (x, s) -> getAuthenticationAgent(x, s));
        NEvents.of(NSessionUtils.defaultSession(ws)).addWorkspaceListener(new ClearAuthOnWorkspaceChange());
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNWorkspaceSecurityModel.class, session);
        }
        return LOG;
    }

    public void login(final String username, final char[] password, NSession session) {
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


    public boolean setSecureMode(boolean secure, char[] adminPassword, NSession session) {
        if (secure) {
            return switchSecureMode(adminPassword, session);
        } else {
            return switchUnsecureMode(adminPassword, session);
        }
    }

    public boolean switchUnsecureMode(char[] adminPassword, NSession session) {
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        NUser adminSecurity = findUser(NConstants.Users.ADMIN, session);
        if (adminSecurity == null || !adminSecurity.hasCredentials()) {
            if (_LOG(session).isLoggable(Level.CONFIG)) {
                _LOGOP(session).level(Level.CONFIG).verb(NLogVerb.WARNING)
                        .log(NMsg.ofJ("{0} user has no credentials. reset to default", NConstants.Users.ADMIN));
            }
            NUserConfig u = NConfigsExt.of(NConfigs.of(session)).getModel().getUser(NConstants.Users.ADMIN, session);
            u.setCredentials(CoreStringUtils.chrToStr(createCredentials("admin".toCharArray(), false, null, session)));
            NConfigsExt.of(NConfigs.of(session)).getModel().setUser(u, session);
        }

        char[] credentials = NDigestUtils.evalSHA1(adminPassword, session);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NSecurityException(session, NMsg.ofPlain("invalid credentials"));
        }
        Arrays.fill(credentials, '\0');
        boolean activated = false;
        if (isSecure(session)) {
            NConfigsExt.of(NConfigs.of(session)).getModel().setSecure(false, session);
            activated = true;
        }
        return activated;
    }

    public boolean switchSecureMode(char[] adminPassword, NSession session) {
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        boolean deactivated = false;
        char[] credentials = NDigestUtils.evalSHA1(adminPassword, session);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NSecurityException(session, NMsg.ofPlain("invalid credentials"));
        }
        Arrays.fill(credentials, '\0');
        if (!isSecure(session)) {
            NConfigsExt.of(NConfigs.of(session)).getModel().setSecure(true, session);
            deactivated = true;
        }
        return deactivated;
    }


    public boolean isAdmin(NSession session) {
        return NConstants.Users.ADMIN.equals(getCurrentUsername(session));
    }


    public void logout(NSession session) {
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null || r.isEmpty()) {
            throw new NLoginException(session, NMsg.ofPlain("not logged in"));
        }
        try {
            LoginContext loginContext = r.pop();
            loginContext.logout();
        } catch (LoginException ex) {
            throw new NLoginException(session, NMsg.ofPlain("login failed"), ex);
        }
    }


    public NUser findUser(String username, NSession session) {
        NUserConfig security = NConfigsExt.of(NConfigs.of(session)).getModel().getUser(username, session);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(security.getGroups());
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NUserConfig ss = NConfigsExt.of(NConfigs.of(session)).getModel().getUser(s, session);
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
        return security == null ? null : new DefaultNUser(security, inherited);
    }


    public List<NUser> findUsers(NSession session) {
        List<NUser> all = new ArrayList<>();
        for (NUserConfig secu : NConfigsExt.of(NConfigs.of(session)).getModel().getUsers(session)) {
            all.add(findUser(secu.getUser(), session));
        }
        return all;
    }


    public NAddUserCommand addUser(String name, NSession session) {
        return NAddUserCommand.of(session).setUsername(name);
    }


    public NUpdateUserCommand updateUser(String name, NSession session) {
        return NUpdateUserCommand.of(session).setUsername(name);
    }


    public NRemoveUserCommand removeUser(String name, NSession session) {
        return NRemoveUserCommand.of(session).setUsername(name);
    }


    public void checkAllowed(String permission, String operationName, NSession session) {
        if (!isAllowed(permission, session)) {
            if (NBlankable.isBlank(operationName)) {
                throw new NSecurityException(session, NMsg.ofC("%s not allowed!", permission));
            } else {
                throw new NSecurityException(session, NMsg.ofC("%s : %s not allowed!", operationName, permission));
            }
        }
    }

    private NAuthorizations getAuthorizations(String n, NSession session) {
        NAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NUserConfig s = NConfigsExt.of(NConfigs.of(session)).getModel().getUser(n, session);
        if (s != null) {
            List<String> rr = s.getPermissions();
            aa = new NAuthorizations(CoreCollectionUtils.nonNullList(rr));
            authorizations.put(n, aa);
        } else {
            aa = new NAuthorizations(Collections.emptyList());
        }
        return aa;
    }


    public boolean isAllowed(String permission, NSession session) {
        if (!isSecure(session)) {
            return true;
        }
        String name = getCurrentUsername(session);
        if (NBlankable.isBlank(name)) {
            return false;
        }
        if (NConstants.Users.ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NAuthorizations s = getAuthorizations(n, session);
            Boolean ea = s.explicitAccept(permission);
            if (ea != null) {
                return ea;
            }
            NUserConfig uc = NConfigsExt.of(NConfigs.of(session)).getModel().getUser(n, session);
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


    public String[] getCurrentLoginStack(NSession session) {
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
                logins.add(NConstants.Users.ADMIN);
            } else {
                logins.add(NConstants.Users.ANONYMOUS);
            }
        }
        return logins.toArray(new String[0]);
    }

    private boolean isInitializing() {
        return ((NWorkspaceExt) ws).getModel().bootModel.isInitializing();
    }


    public String getCurrentUsername(NSession session) {
        if (isInitializing()) {
            return NConstants.Users.ADMIN;
        }
        String name = null;
        Subject currentSubject = getLoginSubject();
        if (currentSubject != null) {
            for (Principal principal : currentSubject.getPrincipals()) {
                name = principal.getName();
                if (!NBlankable.isBlank(name)) {
                    if (!NBlankable.isBlank(name)) {
                        return name;
                    }
                }
            }
        }
        return NConstants.Users.ANONYMOUS;
    }

    private Subject getLoginSubject() {
        LoginContext c = getLoginContext();
        if (c == null) {
            return null;
        }
        return c.getSubject();
    }


    public void login(CallbackHandler handler, NSession session) {
        NWorkspaceLoginModule.configure(session); //initialize it
        //        if (!NutsConstants.Misc.USER_ANONYMOUS.equals(getCurrentLogin())) {
        //            throw new NutsLoginException("Already logged in");
        //        }
        LoginContext login;
        try {
            login = CorePlatformUtils.runWithinLoader(new Callable<LoginContext>() {

                public LoginContext call() throws Exception {
                    return new LoginContext("nuts", handler);
                }
            }, NWorkspaceLoginModule.class.getClassLoader(), session);
            login.login();
        } catch (LoginException ex) {
            throw new NLoginException(session, NMsg.ofPlain("login failed"), ex);
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


    public NAuthenticationAgent getAuthenticationAgent(String authenticationAgentId, NSession session) {
        authenticationAgentId = NStringUtils.trim(authenticationAgentId);
        if (NBlankable.isBlank(authenticationAgentId)) {
            authenticationAgentId = NConfigsExt.of(NConfigs.of(session))
                    .getModel().getStoredConfigSecurity().getAuthenticationAgent();
        }
        NAuthenticationAgent a = NConfigsExt.of(NConfigs.of(session))
                .getModel().createAuthenticationAgent(authenticationAgentId, session);
        return a;
    }


    public void setAuthenticationAgent(String authenticationAgentId, NSession session) {

        DefaultNWorkspaceConfigModel cc = NConfigsExt.of(NConfigs.of(session)).getModel();

        if (cc.createAuthenticationAgent(authenticationAgentId, session) == null) {
            throw new NIllegalArgumentException(session,
                    NMsg.ofC("unsupported Authentication Agent %s", authenticationAgentId)
            );
        }

        NWorkspaceConfigSecurity conf = cc.getStoredConfigSecurity();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgentId)) {
            conf.setAuthenticationAgent(authenticationAgentId);
            cc.fireConfigurationChanged("authentication-agent", session, ConfigEventType.SECURITY);
        }
    }


    public boolean isSecure(NSession session) {
        return NConfigsExt.of(NConfigs.of(session)).getModel().getStoredConfigSecurity().isSecure();
    }


    public void checkCredentials(char[] credentialsId, char[] password, NSession session) throws NSecurityException {
        agent.checkCredentials(credentialsId, password, session);
    }


    public char[] getCredentials(char[] credentialsId, NSession session) {
        return agent.getCredentials(credentialsId, session);
    }


    public boolean removeCredentials(char[] credentialsId, NSession session) {
        return agent.removeCredentials(credentialsId, session);
    }


    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NSession session) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId, session);
    }

    public NWorkspace getWorkspace() {
        return ws;
    }

    private class ClearAuthOnWorkspaceChange implements NWorkspaceListener {

        public ClearAuthOnWorkspaceChange() {
        }

        @Override
        public void onConfigurationChanged(NWorkspaceEvent event) {
            authorizations.clear();
        }
    }

}
