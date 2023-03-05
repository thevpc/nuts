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
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.JShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class WgetCommand extends JShellBuiltinDefault {

    public WgetCommand() {
        super("wget", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (commandLine.next("-O", "--output-document").isPresent()) {
            options.outputDocument = commandLine.nextNonOption().get(session).asString().orNull();
            return true;
        } else if (!commandLine.isNextOption()) {
            while (commandLine.hasNext()) {
                options.files.add(commandLine.next().flatMap(NLiteral::asString).get(session));
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onCmdExec(NCmdLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            throw new NExecutionException(context.getSession(), NMsg.ofPlain("wget: Missing Files"), 2);
        }
        for (String file : options.files) {
            download(file, options.outputDocument, context);
        }
    }

    protected void download(String path, String output, JShellExecutionContext context) {
        String output2 = output;
        NSession session = context.getSession();
        String urlName = NPath.of(path,session).getName();
        if (!NBlankable.isBlank(output2)) {
            output2 = output2.replace("{}", urlName);
        }
        NPath file = NPath.of(context.getAbsolutePath(NBlankable.isBlank(output2) ? urlName : output2),session);
        NCp.of(session)
                .from(NPath.of(path,session)).to(file).setSession(session)
                .addOptions(NPathOption.LOG, NPathOption.TRACE).run();
    }

    private static class Options {

        String outputDocument = null;
        List<String> files = new ArrayList<>();
    }
    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine commandLine, JShellExecutionContext context) {
        return onCmdNextOption(arg, commandLine, context);
    }
}
