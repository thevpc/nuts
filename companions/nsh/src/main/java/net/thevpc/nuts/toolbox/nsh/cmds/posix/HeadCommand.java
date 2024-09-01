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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class HeadCommand extends NShellBuiltinDefault {
    public HeadCommand() {
        super("head", NConstants.Support.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get(session);
        if (a.isOption() && a.getKey().isInt()) {
            options.max = a.getKey().asInt().get(session);
            cmdLine.skip();
            return true;
        } else if (!a.isOption()) {
            String path = cmdLine.next().flatMap(NLiteral::asString).get(session);
            String file = NPath.of(path, session).toAbsolute(context.getDirectory()).toString();
            options.files.add(file);
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.files.isEmpty()) {
            cmdLine.throwMissingArgument();
        }
        for (String file : options.files) {
            head(file, options.max, context);
        }
    }

    private void head(String file, int max, NShellExecutionContext context) {
        BufferedReader r = null;
        NSession session = context.getSession();
        try {
            try {
                r = new BufferedReader(new InputStreamReader(NPath.of(file, session)
                        .getInputStream()));
                String line = null;
                int count = 0;
                while (count < max && (line = r.readLine()) != null) {
                    session.out().println(line);
                    count++;
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
        List<String> files = new ArrayList<>();
    }
    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
