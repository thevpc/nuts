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
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class ZipCommand extends AbstractNshBuiltin {

    public ZipCommand() {
        super("zip", DEFAULT_SUPPORT);
    }

    @Override
    public int execImpl(String[] args, JShellExecutionContext context) {
        NutsCommandLine commandLine = cmdLine(args, context);
        Options options = new Options();
        List<String> files = new ArrayList<>();
//        NutsPrintStream out = context.out();
        File outZip = null;
        NutsArgument a;
        NutsCommandLineManager nutsCommandLineFormat = context.getSession().commandLine();
        while (commandLine.hasNext()) {
            if (commandLine.next("-r") != null) {
                options.r = true;
            } else if (commandLine.peek().isOption()) {
                commandLine.unexpectedArgument();
            } else if (commandLine.peek().isNonOption()) {
                String path = commandLine.required().nextNonOption(nutsCommandLineFormat.createName("file")).getString();
                File file = new File(context.getGlobalContext().getAbsolutePath(path));
                if (outZip == null) {
                    outZip = file;
                } else {
                    files.add(file.getPath());
                }
            } else {
                context.configureLast(commandLine);
            }
        }
        if (files.isEmpty()) {
            commandLine.required(NutsMessage.cstyle("missing input-files"));
        }
        if (outZip == null) {
            commandLine.required(NutsMessage.cstyle("missing out-zip"));
        }
        NutsIOCompressAction aa = context.getSession().io().compress()
                .setTarget(outZip.getPath());
        for (String file : files) {
            aa.addSource(file);
        }
        aa.run();
        return 0;
    }

    private static class Options {

        boolean r = false;
    }
}
