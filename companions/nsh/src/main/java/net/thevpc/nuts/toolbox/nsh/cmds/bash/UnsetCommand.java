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

import net.thevpc.nuts.NSupported;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class UnsetCommand extends NShellBuiltinDefault {

    public UnsetCommand() {
        super("unset", NSupported.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg aa = cmdLine.peek().get(session);
        if (aa.isOption()) {
            if (aa.key().equals("-v")) {
                cmdLine.withNextFlag((v, a, s) -> options.fct= !v);
                return true;
            } else if (aa.key().equals("-f")) {
                cmdLine.withNextFlag((v, a, s) -> options.fct= v);
                return true;
            }
        } else {
            options.list.addAll(Arrays.asList(cmdLine.toStringArray()));
            cmdLine.skipAll();
        }
        return false;
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.fct) {
            for (String k : options.list) {
                context.functions().unset(k);
            }
        } else {
            for (String k : options.list) {
                context.aliases().set(k, null);
            }
        }
    }

    private static class Options {

        boolean fct;
        Set<String> list = new HashSet<>();
    }
    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return onCmdNextOption(arg, cmdLine, context);
    }
}
