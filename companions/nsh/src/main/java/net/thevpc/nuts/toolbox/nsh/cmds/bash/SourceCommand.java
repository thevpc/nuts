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
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class SourceCommand extends SimpleJShellBuiltin {

    public SourceCommand() {
        super("source", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        final NutsArgument a = commandLine.peek().get(session);
        if (!a.isOption()) {
            options.args.addAll(Arrays.asList(commandLine.toStringArray()));
            commandLine.skipAll();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        if (options.args.isEmpty()) {
            throwExecutionException("missing command", 1, session);
        }
        final String[] paths = context.vars().get("PATH", "").split(":|;");
        NutsPath file = NutsPath.of(options.args.remove(0), session);
        if (file.isName()) {
            for (String path : paths) {
                NutsPath basePathFolder = NutsPath.of(path, session);
                if (basePathFolder.resolve(file).isRegularFile()) {
                    file = basePathFolder.resolve(file);
                    break;
                }
            }
        }
        if(!file.isAbsolute()){
            file=file.toAbsolute(context.getCwd());
        }
        if (!file.isRegularFile()) {
            throwExecutionException(NutsMessage.ofCstyle("file not found : %s",file), 1, session);
        } else {
            JShellContext c2 = context.getShellContext();
            JShellContext c = context.getShell().createInlineContext(c2, file.toString(), options.args.toArray(new String[0]));
            context.getShell().executeServiceFile(c, false);
        }
    }

    private static class Options {

        List<String> args = new ArrayList<>();
    }

}
