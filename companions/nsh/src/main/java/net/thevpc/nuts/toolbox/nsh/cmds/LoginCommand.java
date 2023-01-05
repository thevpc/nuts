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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class LoginCommand extends SimpleJShellBuiltin {

    public LoginCommand() {
        super("login", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = commandLine.peek().get(session);
        if (!a.isOption()) {
            if (options.login == null) {
                options.login = commandLine.next(NArgName.of("username", session))
                        .flatMap(NLiteral::asString).get(session);
                return true;
            } else if (options.password == null) {
                options.password = commandLine.next(NArgName.of("password", session))
                        .flatMap(NLiteral::asString).get(session).toCharArray();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void execBuiltin(NCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (!NConstants.Users.ANONYMOUS.equals(options.login)
                && (options.password == null
                || NBlankable.isBlank(new String(options.password)))) {
            NSession session = context.getSession();
            options.password = session.getTerminal().ask()
                    .resetLine()
                    .forPassword(NMsg.ofPlain("Password:")).getValue();
        }
        NWorkspaceSecurityManager.of(context.getSession()).login(options.login, options.password);
    }

    private static class Options {

        String login;
        char[] password;
    }

}
