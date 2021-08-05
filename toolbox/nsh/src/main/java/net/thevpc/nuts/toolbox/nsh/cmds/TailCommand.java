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

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


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

    public int execImpl(String[] args, JShellExecutionContext context) {
        NutsCommandLine commandLine = cmdLine(args, context);
        Options options = new Options();
        List<String> files = new ArrayList<>();
        NutsPrintStream out = context.out();
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
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("not yet supported"), 2);
        }
        for (String file : files) {
            tail(file, options.max, context);
        }
        return 0;
    }

    private void tail(String file, int max, JShellExecutionContext context) {
        BufferedReader r = null;
        try {
            try {
                r = new BufferedReader(new InputStreamReader(context.getSession().getWorkspace().io().path(file)
                        .input().open()));
                String line = null;
                int count = 0;
                LinkedList<String> lines=new LinkedList<>();
                while ((line = r.readLine()) != null) {
                    lines.add(line);
                    count++;
                    if(count> max) {
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
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("%s",ex), ex, 100);
        }
    }
}
