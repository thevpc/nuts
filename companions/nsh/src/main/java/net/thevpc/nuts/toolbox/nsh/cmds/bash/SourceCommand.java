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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class SourceCommand extends NShellBuiltinDefault {

    public SourceCommand() {
        super("source", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        final NArg a = cmdLine.peek().get(session);
        if (!a.isOption()) {
            options.args.addAll(Arrays.asList(cmdLine.toStringArray()));
            cmdLine.skipAll();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.args.isEmpty()) {
            throwExecutionException("missing command", 1, session);
        }
        final String[] paths = context.vars().get("PATH", "").split(":|;");
        NPath file = NPath.of(options.args.remove(0), session);
        if (file.isName()) {
            for (String path : paths) {
                NPath basePathFolder = NPath.of(path, session);
                if (basePathFolder.resolve(file).isRegularFile()) {
                    file = basePathFolder.resolve(file);
                    break;
                }
            }
        }
        if(!file.isAbsolute()){
            file=file.toAbsolute(context.getDirectory());
        }
        if (!file.isRegularFile()) {
            throwExecutionException(NMsg.ofC("file not found : %s",file), 1, session);
        } else {
            NShellContext c2 = context.getShellContext();
            NShellContext c = context.getShell().createInlineContext(c2, file.toString(), options.args.toArray(new String[0]));
            context.getShell().executeServiceFile(c, false);
        }
    }

    private static class Options {

        List<String> args = new ArrayList<>();
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
