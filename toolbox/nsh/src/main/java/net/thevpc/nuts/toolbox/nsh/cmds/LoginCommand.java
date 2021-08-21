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
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class LoginCommand extends SimpleNshBuiltin {

    public LoginCommand() {
        super("login", DEFAULT_SUPPORT);
    }

    private static class Options {

        String login;
        char[] password;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (!a.isOption()) {
            if (options.login == null) {
                options.login = commandLine.next(context.getWorkspace().commandLine().createName("username")).getString();
                return true;
            } else if (options.password == null) {
                options.password = commandLine.next(context.getWorkspace().commandLine().createName("password")).getString().toCharArray();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void prepareOptions(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (!NutsConstants.Users.ANONYMOUS.equals(options.login)
                && (options.password == null
                || NutsUtilStrings.isBlank(new String(options.password)))) {
            NutsSession session = context.getSession();
            options.password = session.getTerminal().ask()
                    .resetLine()
                    .setSession(session)
                    .forPassword("Password:").getValue();
        }
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        context.getWorkspace().security().login(options.login, options.password);
    }

}
