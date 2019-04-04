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
package net.vpc.app.nuts.core;

import com.sun.security.auth.UserPrincipal;
import net.vpc.app.nuts.*;
import net.vpc.common.strings.StringUtils;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

public class NutsWorkspaceLoginModule implements LoginModule {

    private CallbackHandler handler;
    private Subject subject;
    private UserPrincipal userPrincipal;
    private String login;
    private static ThreadLocal<NutsWorkspace> workspace=new ThreadLocal<>();

    static {
        final Configuration configuration = Configuration.getConfiguration();
        if (configuration.getAppConfigurationEntry("nuts") == null) {
            Configuration.setConfiguration(new NutsWorkspaceSecurityConfiguration(configuration));
            Configuration.getConfiguration();
        }
    }

    public static void configure(NutsWorkspace workspace) {
        NutsWorkspaceLoginModule.workspace.set(workspace);
        //do nothing
    }

    public NutsWorkspaceLoginModule() {
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
            char[] pp = callback == null ? null : callback.getPassword();
            String password = pp == null ? null : String.valueOf(pp);

            NutsWorkspace workspace = NutsWorkspaceLoginModule.workspace.get();
            if (workspace == null) {
                throw new LoginException("Authentication failed : No Workspace");
            }

            if (NutsConstants.USER_ANONYMOUS.equals(name)) {
                this.login = name;
                return true;
            }

            NutsUserConfig registeredUser = workspace.config().getUser(name);
            if (registeredUser != null) {
                try {
                    workspace.config().createAuthenticationAgent(registeredUser.getAuthenticationAgent())
                            .checkCredentials(
                            registeredUser.getCredentials(),
                                    registeredUser.getAuthenticationAgent(),
                                    password,
                                    workspace.config()
                    );
                    this.login = name;
                    return true;
                }catch (Exception ex){
                    //
                }

//                if(!StringUtils.isEmpty(registeredUser.getCredentials())){
//                    if ((StringUtils.isEmpty(password) && StringUtils.isEmpty(registeredUser.getCredentials()))
//                            || (!StringUtils.isEmpty(password) && !StringUtils.isEmpty(registeredUser.getCredentials())
//                            && registeredUser.getCredentials().equals(CoreSecurityUtils.evalSHA1(password)))) {
//                        this.login = name;
//                        return true;
//                    }
//                }
            }

            // If credentials are NOT OK we throw a LoginException
            throw new LoginException("Authentication failed");

        } catch (IOException e) {
            throw new LoginException(StringUtils.exceptionToString(e));
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(StringUtils.exceptionToString(e));
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
