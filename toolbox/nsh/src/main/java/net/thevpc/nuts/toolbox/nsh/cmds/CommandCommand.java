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
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.Arrays;
import java.util.List;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class CommandCommand extends SimpleNshBuiltin {

    public CommandCommand() {
        super("command", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean p;
        String commandName;
        List<String> args;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = null;
        //inverse configuration order
        if (context.getExecutionContext().configureFirst(commandLine)) {
            return true;
        } else if ((a = commandLine.nextBoolean("-p")) != null) {
            options.p = a.getValue().getBoolean();
        } else if (!commandLine.peek().isOption()) {
            if (options.commandName == null) {
                options.commandName = commandLine.next().getString();
            }
            options.args.addAll(Arrays.asList(commandLine.toStringArray()));
            commandLine.skipAll();
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.commandName != null) {
            context.getShell().executePreparedCommand(options.args.toArray(new String[0]), false, true, true, context.getGlobalContext());
        }
    }

}
