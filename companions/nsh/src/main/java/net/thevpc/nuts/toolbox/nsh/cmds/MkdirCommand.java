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

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class MkdirCommand extends SimpleJShellBuiltin {

    public MkdirCommand() {
        super("mkdir", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        NutsSession session = context.getSession();
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("--parent", "-p").orNull()) != null) {
            options.p = a.getBooleanValue().get(session);
            return true;
        } else if (commandLine.peek().get(session).isNonOption()) {
            options.files.addAll(Arrays.asList(commandLine.toStringArray()));
            commandLine.skipAll();
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        options.xfiles = ShellHelper.xfilesOf(options.files, context.getShellContext().getCwd(), session);
        if (options.xfiles.size() < 1) {
            commandLine.throwMissingArgument(session);
        }
//        ShellHelper.WsSshListener listener = new ShellHelper.WsSshListener(context.getSession());
        for (NutsPath v : options.xfiles) {
//            if (v instanceof SshXFile) {
//                ((SshXFile) v).setListener(listener);
//            }
            if(options.p) {
                v.mkdirs();
            }else{
                v.mkdir();
            }
        }
    }

    public static class Options {

        List<String> files = new ArrayList<>();
        List<NutsPath> xfiles = new ArrayList<>();

        boolean p;
    }
}
