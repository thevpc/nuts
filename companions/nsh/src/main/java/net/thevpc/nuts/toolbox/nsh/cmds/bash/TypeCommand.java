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

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.cmdresolver.NShellCommandResolution;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class TypeCommand extends NShellBuiltinDefault {

    public TypeCommand() {
        super("type", NConstants.Support.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options config = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get(session);
        if (a.isNonOption()) {
            config.commands.add(cmdLine.next().flatMap(NLiteral::asString).get(session));
            return true;
        }
        return false;
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options config = context.getOptions();
        NShell shell = context.getShell();
        List<ResultItem> result = new ArrayList<>();
        for (String cmd : config.commands) {
            NShellBuiltin ic = context.builtins().find(cmd);
            if (ic != null && ic.isEnabled()) {
                result.add(new ResultItem(
                        cmd,
                        "builtin",
                        cmd + " is a shell builtin"
                ));
            } else {
                String alias = context.aliases().get(cmd);
                if (alias != null) {
                    result.add(new ResultItem(
                            cmd,
                            "alias",
                            cmd + " is aliased to `" + alias + "`"
                    ));
                } else {
                    NShellCommandResolution pp = shell.getCommandTypeResolver().type(cmd, context.getShellContext());
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
        switch (context.getSession().getOutputFormat()) {
            case PLAIN: {
                for (ResultItem resultItem : result) {
                    context.getSession().out().println(resultItem.message);
                }
                break;
            }
            default: {
                context.getSession().out().println(result);
            }
        }
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
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return onCmdNextOption(arg, cmdLine, context);
    }
}
