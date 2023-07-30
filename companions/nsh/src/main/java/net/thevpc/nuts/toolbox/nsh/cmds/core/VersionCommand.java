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
package net.thevpc.nuts.toolbox.nsh.cmds.core;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NVersionFormat;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinCore;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;

/**
 * Created by vpc on 1/7/17.
 */
public class VersionCommand extends NShellBuiltinCore {

    public VersionCommand() {
        super("version", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.version == null) {
            options.version = NVersionFormat.of(context.getSession());
        }
        return options.version.configureFirst(cmdLine);
    }

    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.version == null) {
            options.version = NVersionFormat.of(context.getSession());
        }
        return options.version.configureFirst(cmdLine);
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        if (options.version == null) {
            options.version = NVersionFormat.of(context.getSession());
        }
        if(context.getSession().isPlainOut()){
            context.out().println( context.getSession().getAppId().getVersion().getValue());
        }else {
            options.version
                    .setSession(session)
                    .addProperty("app-version", context.getSession().getAppId().getVersion().getValue())
                    .println(context.out());
        }
    }

    private static class Options {
        NVersionFormat version;
    }
}
