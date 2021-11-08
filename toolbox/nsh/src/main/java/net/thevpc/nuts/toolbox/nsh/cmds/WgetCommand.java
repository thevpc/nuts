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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class WgetCommand extends SimpleJShellBuiltin {

    public WgetCommand() {
        super("wget", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (commandLine.next("-O", "--output-document") != null) {
            options.outputDocument = commandLine.requireNonOption().next().getString();
            return true;
        } else if (!commandLine.peek().isOption()) {
            while (commandLine.hasNext()) {
                options.files.add(commandLine.next().getString());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("wget: Missing Files"), 2);
        }
        for (String file : options.files) {
            download(file, options.outputDocument, context);
        }
    }

    protected void download(String path, String output, JShellExecutionContext context) {
        String output2 = output;
        URL url;
        NutsSession session = context.getSession();
        try {
            url = new URL(path);
        } catch (MalformedURLException ex) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("%s", ex), ex, 100);
        }
        String urlName = NutsPath.of(url,session).getName();
        if (!NutsBlankable.isBlank(output2)) {
            output2 = output2.replace("{}", urlName);
        }
        Path file = Paths.get(context.getShellContext().getAbsolutePath(NutsBlankable.isBlank(output2) ? urlName : output2));
        NutsCp.of(session)
                .from(path).to(file).setSession(session)
                .setLogProgress(true).run();
    }

    private static class Options {

        String outputDocument = null;
        List<String> files = new ArrayList<>();
    }
}
