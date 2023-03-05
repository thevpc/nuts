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
package net.thevpc.nuts.toolbox.nsh.cmds.posix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nsh.cmds.JShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class ZipCommand extends JShellBuiltinDefault {

    public ZipCommand() {
        super("zip", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (commandLine.next("-r").isPresent()) {
            options.r = true;
            return true;
        } else if (commandLine.isNextOption()) {
            return false;
        } else if (commandLine.peek().get(session).isNonOption()) {
            String path = commandLine.nextNonOption(NArgName.of("file", session))
                    .flatMap(NLiteral::asString).get(session);
            NPath file = NPath.of(path, session).toAbsolute(context.getCwd());
            if (options.outZip == null) {
                options.outZip = file;
            } else {
                options.files.add(file);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onCmdExec(NCmdLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.files.isEmpty()) {
            commandLine.throwError(NMsg.ofPlain("missing input-files"));
        }
        if (options.outZip == null) {
            commandLine.throwError(NMsg.ofPlain("missing out-zip"));
        }
        NCompress aa = NCompress.of(session)
                .setTarget(options.outZip);
        for (NPath file : options.files) {
            aa.addSource(file);
        }
        aa.run();
    }


    private static class Options {
        List<NPath> files = new ArrayList<>();
        NPath outZip = null;

        boolean r = false;
    }

    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine commandLine, JShellExecutionContext context) {
        return onCmdNextOption(arg, commandLine, context);
    }
}
