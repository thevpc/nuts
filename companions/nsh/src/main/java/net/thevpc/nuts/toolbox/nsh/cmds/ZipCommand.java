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
import net.thevpc.nuts.cmdline.NutsArgumentName;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsCompress;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class ZipCommand extends SimpleJShellBuiltin {

    public ZipCommand() {
        super("zip", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        if (commandLine.next("-r").isPresent()) {
            options.r = true;
            return true;
        } else if (commandLine.isNextOption()) {
            return false;
        } else if (commandLine.peek().get(session).isNonOption()) {
            String path = commandLine.nextNonOption(NutsArgumentName.of("file", session))
                    .flatMap(NutsValue::asString).get(session);
            NutsPath file = NutsPath.of(path, session).toAbsolute(context.getCwd());
            if (options.outZip == null) {
                options.outZip = file;
            } else {
                options.files.add(file);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        if (options.files.isEmpty()) {
            commandLine.throwError(NutsMessage.ofPlain("missing input-files"),session);
        }
        if (options.outZip == null) {
            commandLine.throwError(NutsMessage.ofPlain("missing out-zip"),session);
        }
        NutsCompress aa = NutsCompress.of(session)
                .setTarget(options.outZip);
        for (NutsPath file : options.files) {
            aa.addSource(file);
        }
        aa.run();
    }


    private static class Options {
        List<NutsPath> files = new ArrayList<>();
        NutsPath outZip = null;

        boolean r = false;
    }
}
