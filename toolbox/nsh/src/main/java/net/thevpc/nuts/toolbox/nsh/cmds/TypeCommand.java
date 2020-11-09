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

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.jshell.JShell;
import net.thevpc.jshell.JShellCommandType;
import net.thevpc.jshell.JShellBuiltin;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class TypeCommand extends SimpleNshBuiltin {

    public TypeCommand() {
        super("type", DEFAULT_SUPPORT);
    }

    private static class Options {

        List<String> commands = new ArrayList<>();
    }

    private static class ResultItem {

        String command;
        String type;
        String message;

        public ResultItem(String command, String type, String message) {
            this.command = command;
            this.type = type;
            this.message = message;
        }

        public ResultItem() {
        }

    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (a.isNonOption()) {
            config.commands.add(commandLine.next().getString());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        JShell shell = context.getShell();
        List<ResultItem> result = new ArrayList<>();
        for (String cmd : config.commands) {
            JShellBuiltin ic = context.getRootContext().builtins().find(cmd);
            if (ic != null && ic.isEnabled()) {
                result.add(new ResultItem(
                        cmd,
                        "builtin",
                        cmd + " is a shell builtin"
                ));
            } else {
                String alias = context.getRootContext().aliases().get(cmd);
                if (alias != null) {
                    result.add(new ResultItem(
                            cmd,
                            "alias",
                            cmd + " is aliased to `" + alias + "`"
                    ));
                } else {
                    JShellCommandType pp = shell.getCommandTypeResolver().type(cmd, context.getRootContext());
                    if (pp != null) {
                        result.add(new ResultItem(
                                cmd,
                                pp.getType(),
                                pp.getDescription()
                        ));
                    } else {
                        if (ic != null) {
                            result.add(new ResultItem(
                                    cmd,
                                    "error",
                                    cmd + " is disabled"
                            ));
                        } else {
                            result.add(new ResultItem(
                                    cmd,
                                    "error",
                                    cmd + " not found"
                            ));
                        }
                    }
                }
            }
        }
        context.setPrintlnOutObject(result);
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context) {
        List<ResultItem> result = context.getResult();
        for (ResultItem resultItem : result) {
            context.out().println(resultItem.message);
        }
    }

}
