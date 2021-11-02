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
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class HeadCommand extends SimpleNshBuiltin {
    public HeadCommand() {
        super("head", DEFAULT_SUPPORT);
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (a.isOption() && a.getKey().isInt()) {
            options.max = a.getKey().getInt();
            commandLine.skip();
            return true;
        } else if (!a.isOption()) {
            String path = commandLine.next().getString();
            String file = NutsPath.of(path, context.getSession()).toAbsolute(context.getRootContext().getCwd()).toString();
            options.files.add(file);
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            commandLine.required();
        }
        for (String file : options.files) {
            head(file, options.max, context);
        }
    }

    private void head(String file, int max, SimpleNshCommandContext context) {
        BufferedReader r = null;
        NutsSession session = context.getSession();
        try {
            try {
                r = new BufferedReader(new InputStreamReader(NutsPath.of(file, session)
                        .getInputStream()));
                String line = null;
                int count = 0;
                while (count < max && (line = r.readLine()) != null) {
                    session.out().println(line);
                    count++;
                }
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("%s", ex), ex, 100);
        }
    }

    private static class Options {

        int max = 0;
        List<String> files = new ArrayList<>();
    }
}
