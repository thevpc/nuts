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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class SourceCommand extends SimpleNshBuiltin {

    public SourceCommand() {
        super("source", DEFAULT_SUPPORT);
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final NutsArgument a = commandLine.peek();
        if (!a.isOption()) {
            options.files.add(commandLine.next().getString());
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final String[] paths = context.getExecutionContext().getShellContext().vars().get("PATH", "").split(":|;");
        List<NutsPath> goodFiles = new ArrayList<>();
        for (String file : options.files) {
            NutsSession session = context.getSession();
            boolean found = false;
            if (!file.contains("/")) {
                for (String path : paths) {
                    NutsPath path0 = NutsPath.of(path, session);
                    if (path0.resolve(file).isFile()) {
                        file = path0.resolve(file).toString();
                        break;
                    }
                }
                if (!NutsPath.of(file, session).isFile()) {
                    if (NutsPath.of(context.getRootContext().getCwd(), session).resolve(file).isFile()) {
                        file = NutsPath.of(context.getRootContext().getCwd(), session).resolve(file).toString();
                    }
                }
                if (NutsPath.of(file, session).isFile()) {
                    found = true;
                    goodFiles.add(NutsPath.of(file, session));
                }
            }
            if (!found) {
                goodFiles.add(NutsPath.of(file, session));
            }
        }
//        JShellContext c2 = context.getShell().createContext(context.getExecutionContext().getGlobalContext());
//        c2.setArgs(context.getArgs());
        JShellContext c2 = context.getExecutionContext().getShellContext();
        for (NutsPath goodFile : goodFiles) {
            JShellContext c = context.getShell().createInlineContext(c2, goodFile.toString(), context.getArgs());
            context.getShell().executeServiceFile(c, false);
        }
    }

    private static class Options {

        List<String> files = new ArrayList<>();
    }

}
