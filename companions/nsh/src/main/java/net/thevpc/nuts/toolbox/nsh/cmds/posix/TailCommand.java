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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by vpc on 1/7/17.
 */
public class TailCommand extends NShellBuiltinDefault {

    public TailCommand() {
        super("tail", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get(session);
        if (a.isOption()) {
            if (ShellHelper.isInt(a.asString()
                    .get(session).substring(1))) {
                options.max = Integer.parseInt(cmdLine.next()
                        .get(session).asString()
                        .get(session).substring(1));
                return true;
            } else {
                return false;
            }
        } else {
            String path = a.asString().get(session);
            NPath file = NPath.of(path, session).toAbsolute(context.getDirectory());
            options.files.add(file);
            return true;
        }
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();

        if (options.files.isEmpty()) {
            throw new NExecutionException(session, NMsg.ofPlain("not yet supported"), NExecutionException.ERROR_2);
        }
        for (NPath file : options.files) {
            tail(file, options.max, context);
        }
    }

    private void tail(NPath file, int max, NShellExecutionContext context) {
        BufferedReader r = null;
        NSession session = context.getSession();
        try {
            try {
                r = new BufferedReader(new InputStreamReader(file.getInputStream()));
                String line = null;
                int count = 0;
                LinkedList<String> lines = new LinkedList<>();
                while ((line = r.readLine()) != null) {
                    lines.add(line);
                    count++;
                    if (count > max) {
                        lines.remove();
                    }
                }
                for (String s : lines) {
                    context.out().println(s);
                }
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } catch (IOException ex) {
            throw new NExecutionException(session, NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_3);
        }
    }

    private static class Options {
        int max = 0;
        List<NPath> files = new ArrayList<>();
    }
    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
