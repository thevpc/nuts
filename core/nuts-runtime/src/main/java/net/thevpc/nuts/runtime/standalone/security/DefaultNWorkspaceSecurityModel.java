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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.NConstants;

import net.thevpc.nuts.NUser;
import net.thevpc.nuts.NUserConfig;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.security.*;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNWorkspaceSecurityModel {

    private final ThreadLocal<Stack<DefaultNLoginContext>> loginContextStack = new ThreadLocal<>();
    private final DefaultNWorkspace workspace;
    private final WrapperNAuthenticationAgent agent;
    private final Map<String, NAuthorizations> authorizations = new HashMap<>();
    private NLog LOG;

    public DefaultNWorkspaceSecurityModel(final DefaultNWorkspace ws) {
        this.workspace = ws;
        this.agent = new WrapperNAuthenticationAgent(ws, () -> NWorkspace.of().getConfigMap(), (x) -> getAuthenticationAgent(x));
        workspace.addWorkspaceListener(new ClearAuthOnWorkspaceChange());
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNWorkspaceSecurityModel.class);
    }

    public void login(final String username, final char[] password) {
        NUserConfig registeredUser = NWorkspaceExt.of()
                .getConfigModel()
                .getUser(username);
        if (registeredUser != null) {
            try {
                checkCredentials(registeredUser.getCredentials().toCharArray(), password);
                Stack<DefaultNLoginContext> r = loginContextStack.get();
                if(r==null){
                    r=new Stack<>();
                }
                r.push(new DefaultNLoginContext(username));
                return;
            } catch (Exception ex) {
                //
            }
        }
        throw new NLoginException(NMsg.ofC("Authentication failed for %s",username));
    }


    public boolean setSecureMode(boolean secure, char[] adminPassword) {
        if (secure) {
            return switchSecureMode(adminPassword);
        } else {
            return switchUnsecureMode(adminPassword);
        }
    }

    public boolean switchUnsecureMode(char[] adminPassword) {
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        NUser adminSecurity = findUser(NConstants.Users.ADMIN);
        if (adminSecurity == null || !adminSecurity.hasCredentials()) {
            if (_LOG().isLoggable(Level.CONFIG)) {
                _LOGOP().level(Level.CONFIG).verb(NLogVerb.WARNING)
                        .log(NMsg.ofC("%s user has no credentials. reset to default", NConstants.Users.ADMIN));
            }
            NUserConfig u = NWorkspaceExt.of(workspace).getConfigModel().getUser(NConstants.Users.ADMIN);
            u.setCredentials(CoreStringUtils.chrToStr(createCredentials("admin".toCharArray(), false, null)));
            NWorkspaceExt.of(workspace).getConfigModel().setUser(u);
        }

        char[] credentials = NDigestUtils.evalSHA1(adminPassword);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NSecurityException(NMsg.ofPlain("invalid credentials"));
        }
        Arrays.fill(credentials, '\0');
        boolean activated = false;
        if (isSecure()) {
            NWorkspaceExt.of(workspace).getConfigModel().setSecure(false);
            activated = true;
        }
        return activated;
    }

    public boolean switchSecureMode(char[] adminPassword) {
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        boolean deactivated = false;
        char[] credentials = NDigestUtils.evalSHA1(adminPassword);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NSecurityException(NMsg.ofPlain("invalid credentials"));
        }
        Arrays.fill(credentials, '\0');
        if (!isSecure()) {
            NWorkspaceExt.of(workspace).getConfigModel().setSecure(true);
            deactivated = true;
        }
        return deactivated;
    }


    public boolean isAdmin() {
        return NConstants.Users.ADMIN.equals(getCurrentUsername());
    }


    public void logout() {
        Stack<DefaultNLoginContext> r = loginContextStack.get();
        if (r == null || r.isEmpty()) {
            throw new NLoginException(NMsg.ofPlain("not logged in"));
        }
        r.pop();
    }


    public NUser findUser(String username) {
        NUserConfig security = NWorkspaceExt.of(workspace).getConfigModel().getUser(username);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(security.getGroups());
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NUserConfig ss = NWorkspaceExt.of(workspace).getConfigModel().getUser(s);
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


    public List<NUser> findUsers() {
        List<NUser> all = new ArrayList<>();
        for (NUserConfig secu : NWorkspaceExt.of(workspace).getConfigModel().getUsers()) {
            all.add(findUser(secu.getUser()));
        }
        return all;
    }


    public NAddUserCmd addUser(String name) {
        return NAddUserCmd.of().setUsername(name);
    }


    public NUpdateUserCmd updateUser(String name) {
        return NUpdateUserCmd.of().setUsername(name);
    }


    public NRemoveUserCmd removeUser(String name) {
        return NRemoveUserCmd.of().setUsername(name);
    }


    public void checkAllowed(String permission, String operationName) {
        if (!isAllowed(permission)) {
            if (NBlankable.isBlank(operationName)) {
                throw new NSecurityException(NMsg.ofC("%s not allowed!", permission));
            } else {
                throw new NSecurityException(NMsg.ofC("%s : %s not allowed!", operationName, permission));
            }
        }
    }

    private NAuthorizations getAuthorizations(String n) {
        NAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NUserConfig s = NWorkspaceExt.of(workspace).getConfigModel().getUser(n);
        if (s != null) {
            List<String> rr = s.getPermissions();
            aa = new NAuthorizations(NCoreCollectionUtils.nonNullList(rr));
            authorizations.put(n, aa);
        } else {
            aa = new NAuthorizations(Collections.emptyList());
        }
        return aa;
    }


    public boolean isAllowed(String permission) {
        if (!isSecure()) {
            return true;
        }
        String name = getCurrentUsername();
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
            NAuthorizations s = getAuthorizations(n);
            Boolean ea = s.explicitAccept(permission);
            if (ea != null) {
                return ea;
            }
            NUserConfig uc = NWorkspaceExt.of(workspace).getConfigModel().getUser(n);
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


    public String[] getCurrentLoginStack() {
        List<String> logins = new ArrayList<String>();
        Stack<DefaultNLoginContext> c = loginContextStack.get();
        if (c != null) {
            for (DefaultNLoginContext loginContext : c) {
                logins.add(loginContext.getUserName());
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
        return ((NWorkspaceExt) workspace).getModel().bootModel.isInitializing();
    }


    public String getCurrentUsername() {
        if (isInitializing()) {
            return NConstants.Users.ADMIN;
        }
        String name = null;
        DefaultNLoginContext currentSubject = getLoginContext();
        if (currentSubject != null) {
            return currentSubject.getUserName();
        }
        return NConstants.Users.ANONYMOUS;
    }



    private DefaultNLoginContext getLoginContext() {
        Stack<DefaultNLoginContext> c = loginContextStack.get();
        if (c == null) {
            return null;
        }
        if (c.isEmpty()) {
            return null;
        }
        return c.peek();
    }


    public NAuthenticationAgent getAuthenticationAgent(String authenticationAgentId) {
        authenticationAgentId = NStringUtils.trim(authenticationAgentId);
        if (NBlankable.isBlank(authenticationAgentId)) {
            authenticationAgentId = workspace
                    .getConfigModel().getStoredConfigSecurity().getAuthenticationAgent();
        }
        NAuthenticationAgent a = workspace
                .getConfigModel().createAuthenticationAgent(authenticationAgentId);
        return a;
    }


    public void setAuthenticationAgent(String authenticationAgentId) {
        DefaultNWorkspaceConfigModel cc = NWorkspaceExt.of(workspace).getConfigModel();

        if (cc.createAuthenticationAgent(authenticationAgentId) == null) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("unsupported Authentication Agent %s", authenticationAgentId)
            );
        }

        NWorkspaceConfigSecurity conf = cc.getStoredConfigSecurity();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgentId)) {
            conf.setAuthenticationAgent(authenticationAgentId);
            cc.fireConfigurationChanged("authentication-agent", ConfigEventType.SECURITY);
        }
    }


    public boolean isSecure() {
        return NWorkspaceExt.of(workspace).getConfigModel().getStoredConfigSecurity().isSecure();
    }


    public void checkCredentials(char[] credentialsId, char[] password) throws NSecurityException {
        agent.checkCredentials(credentialsId, password);
    }


    public char[] getCredentials(char[] credentialsId) {
        return agent.getCredentials(credentialsId);
    }


    public boolean removeCredentials(char[] credentialsId) {
        return agent.removeCredentials(credentialsId);
    }


    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId);
    }

    public NWorkspace getWorkspace() {
        return workspace;
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
