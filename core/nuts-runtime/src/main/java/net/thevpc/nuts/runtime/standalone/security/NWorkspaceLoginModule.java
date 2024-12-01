/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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
import net.thevpc.nuts.NUserConfig;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import com.sun.security.auth.UserPrincipal;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

public class NWorkspaceLoginModule implements LoginModule {

    private CallbackHandler handler;
    private Subject subject;
    private UserPrincipal userPrincipal;
    private String login;
    private static final ThreadLocal<NSession> SESSION = new ThreadLocal<>();

    static {
        final Configuration configuration = Configuration.getConfiguration();
        if (configuration.getAppConfigurationEntry("nuts") == null) {
            Configuration.setConfiguration(new NWorkspaceSecurityConfiguration(configuration));
            Configuration.getConfiguration();
        }
    }

    public static void configure(NSession session) {
        NWorkspaceLoginModule.SESSION.set(session);
        //do nothing
    }

    public NWorkspaceLoginModule() {
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {

        handler = callbackHandler;
        this.subject = subject;
    }

    @Override
    public boolean login() throws LoginException {

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("login");
        callbacks[1] = new PasswordCallback("password", true);

        try {
            if (handler == null) {
                return false;
            }
            handler.handle(callbacks);
            String name = ((NameCallback) callbacks[0]).getName();
            PasswordCallback callback = (PasswordCallback) callbacks[1];
            char[] password = callback == null ? null : callback.getPassword();

            NSession session = NWorkspaceLoginModule.SESSION.get();
            if (session == null) {
                throw new LoginException("Authentication failed : No Workspace");
            }

            if (NConstants.Users.ANONYMOUS.equals(name)) {
                this.login = name;
                return true;
            }
            NUserConfig registeredUser = NWorkspaceExt.of()
                    .getConfigModel()
                    .getUser(name);
            if (registeredUser != null) {
                try {
                    NWorkspaceSecurityManager.of()
                            .checkCredentials(registeredUser.getCredentials().toCharArray(),password);
                    this.login = name;
                    return true;
                } catch (Exception ex) {
                    //
                }

//                if(!CoreStringUtils.isEmpty(registeredUser.getCredentials())){
//                    if ((CoreStringUtils.isEmpty(password) && CoreStringUtils.isEmpty(registeredUser.getCredentials()))
//                            || (!CoreStringUtils.isEmpty(password) && !CoreStringUtils.isEmpty(registeredUser.getCredentials())
//                            && registeredUser.getCredentials().equals(CoreSecurityUtils.evalSHA1(password)))) {
//                        this.login = name;
//                        return true;
//                    }
//                }
            }

            // If credentials are NOT OK we throw a LoginException
            throw new LoginException("Authentication failed");

        } catch (IOException e) {
            throw new LoginException(CoreStringUtils.exceptionToString(e));
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(CoreStringUtils.exceptionToString(e));
        }

    }

    @Override
    public boolean commit() throws LoginException {

        userPrincipal = new UserPrincipal(login);
        subject.getPrincipals().add(userPrincipal);

        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        return true;
    }

}
