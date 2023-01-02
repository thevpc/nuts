/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NValue;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class CommandCommand extends SimpleJShellBuiltin {

    public CommandCommand() {
        super("command", DEFAULT_SUPPORT,Options.class);
    }


    @Override
    protected boolean configureFirst(NCommandLine commandLine, JShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        NArg a = null;
        //inverse configuration order
        if (context.configureFirst(commandLine)) {
            return true;
        } else if ((a = commandLine.nextBoolean("-p").orNull()) != null) {
            options.p = a.getBooleanValue().get(session);
        } else if (!commandLine.isNextOption()) {
            if (options.commandName == null) {
                options.commandName = commandLine.next().flatMap(NValue::asString).get(session);
            }
            options.args.addAll(Arrays.asList(commandLine.toStringArray()));
            commandLine.skipAll();
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.commandName != null) {
            context.getShell().executePreparedCommand(options.args.toArray(new String[0]), false, true, true, context.getShellContext());
        }
    }

    private static class Options {

        boolean p;
        String commandName;
        List<String> args;
    }

}
