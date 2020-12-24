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
 *
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

import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.common.io.TextFiles;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
public class TailCommand extends AbstractNshBuiltin {

    public TailCommand() {
        super("tail", DEFAULT_SUPPORT);
    }

    private static class Options {

        int max = 0;
    }

    public void exec(String[] args, NshExecutionContext context) {
        NutsCommandLine commandLine = cmdLine(args, context);
        Options options = new Options();
        List<String> files = new ArrayList<>();
        PrintStream out = context.out();
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek();
            if (a.isOption()) {
                if (ShellHelper.isInt(a.getString().substring(1))) {
                    options.max = Integer.parseInt(commandLine.next().getString().substring(1));
                } else {
                    context.configureLast(commandLine);
                }
            } else {
                String path = a.getString();
                File file = new File(context.getGlobalContext().getAbsolutePath(path));
                files.add(file.getPath());
            }
        }
        if (files.isEmpty()) {
            throw new NutsExecutionException(context.getWorkspace(), "Not yet supported", 2);
        }
        for (String file : files) {
            try {
                TextFiles.tail(TextFiles.create(file), options.max, out);
            } catch (IOException ex) {
                throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
            }
        }
    }
}
