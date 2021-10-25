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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellFunction;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class SetCommand extends SimpleNshBuiltin {

    public SetCommand() {
        super("set", DEFAULT_SUPPORT);
    }

    private static class Options {

        LinkedHashMap<String, String> vars = new LinkedHashMap<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (a.isNonOption()) {
            if (a.isKeyValue()) {
                options.vars.put(a.getKey().getString(), a.getValue().getString());
                return true;
            }
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.vars.isEmpty()) {
            List<String> results = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : context.getExecutionContext().vars().getAll().entrySet()) {
                results.add(entry.getKey() + "=" + entry.getValue());
            }
            for (JShellFunction function : context.getRootContext().functions().getAll()) {
                results.add(function.getDefinition());
            }
            context.getSession().out().printlnf(results);
        } else {
            for (Map.Entry<String, String> entry : options.vars.entrySet()) {
                context.getExecutionContext().vars().set(entry.getKey(), entry.getValue());
            }
        }
    }
}
