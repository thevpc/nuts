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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class RmCommand extends SimpleJShellBuiltin {

    public RmCommand() {
        super("rm", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("-R")) != null) {
            options.R = a.getValue().getBoolean();
            return true;
        } else if (commandLine.peek().isNonOption()) {
            options.files.add(ShellHelper.xfileOf(commandLine.next().getString(), context.getShellContext().getCwd(), context.getSession()));
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.files.size() < 1) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("missing parameters"), 2);
        }
//        ShellHelper.WsSshListener listener = options.verbose ? new ShellHelper.WsSshListener(context.getSession()) : null;
        for (NutsPath p : options.files) {
//            if (p instanceof SshXFile) {
//                ((SshXFile) p).setListener(listener);
//            }
            if (options.R) {
                p.deleteTree();
            } else {
                p.delete();
            }
        }
    }

    public static class Options {

        boolean R = false;
        boolean verbose = false;
        List<NutsPath> files = new ArrayList<>();
    }

}
