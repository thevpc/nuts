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
 *
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
package net.thevpc.nuts.toolbox.nutsserver;

import net.thevpc.common.strings.StringUtils;

import javax.security.auth.login.LoginException;
import java.io.IOException;

import net.thevpc.common.collections.ListValueMap;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspaceSecurityManager;

/**
 * Created by vpc on 1/24/17.
 */
public abstract class AbstractFacadeCommand implements FacadeCommand {

    private String name;

    public AbstractFacadeCommand(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(FacadeCommandContext context) throws IOException, LoginException {
        ListValueMap<String, String> parameters = context.getParameters();
        String userLogin = parameters.getFirst("ul");
        String userPasswordS = parameters.getFirst("up");
        char[] userPassword = userPasswordS == null ? null : userPasswordS.toCharArray();
        NutsWorkspaceSecurityManager secu = context.getWorkspace().security();
        NutsSession session = context.getSession();
        userLogin = userLogin == null ? null :new String(secu.getCredentials(userLogin.toCharArray(), session));
        userPassword = userPassword==null?null:secu.getCredentials(userPassword, session);
        if (!StringUtils.isBlank(userLogin)) {
            boolean loggedId = false;
            try {
                context.getWorkspace().security().login(userLogin, userPassword, session);
                loggedId = true;
                executeImpl(context);
            } finally {
                if (loggedId) {
                    context.getWorkspace().security().logout(session);
                }
            }
        } else {
            executeImpl(context);
        }
    }

    public abstract void executeImpl(FacadeCommandContext context) throws IOException, LoginException;
}
