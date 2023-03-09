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

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.text.NTextCode;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.NStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class EchoCommand extends NShellBuiltinDefault {

    public EchoCommand() {
        super("echo", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine commandLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        switch (commandLine.peek().get(session).key()) {
            case "-n": {
                commandLine.withNextFlag((v, a, s) -> options.newLine=v);
                return true;
            }
            case "-p":
            case "--plain": {
                commandLine.withNextTrueFlag((v, a, s) -> options.highlighter=null);
                return true;
            }
            case "-H":
            case "--highlight":
            case "--highlighter":
            {
                commandLine.withNextEntry((v, a, s) -> options.highlighter= NStringUtils.trim(v));
                return true;
            }
            default: {
                if (commandLine.peek().get(session).isNonOption()) {
                    while (commandLine.hasNext()) {
                        if (options.tokensCount > 0) {
                            options.message.append(" ");
                        }
                        options.message.append(commandLine.next().get(session).toString());
                        options.tokensCount++;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onCmdExec(NCmdLine commandLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        Object ns = null;
        if (options.highlighter == null) {
            ns = options.message.toString();
        } else {
            NTextCode c = NTexts.of(context.getSession()).ofCode(
                    options.highlighter.isEmpty()?"ntf":options.highlighter
                    , options.message.toString());
            ns = c.highlight(context.getSession());
        }
        if (options.newLine) {
            context.getSession().out().println(ns);
        } else {
            context.getSession().out().print(ns);
        }
    }

    private static class Options {

        boolean newLine = true;
        String highlighter = null;
        boolean first = true;
        StringBuilder message = new StringBuilder();
        int tokensCount = 0;
    }
    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine commandLine, NShellExecutionContext context) {
        return onCmdNextOption(arg, commandLine, context);
    }

}
