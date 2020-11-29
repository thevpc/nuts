/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.jshell.JShellBuiltin;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class EnableCommand extends SimpleNshBuiltin {

    public EnableCommand() {
        super("enable", DEFAULT_SUPPORT);
    }

    private static class Options {

        String file;
        boolean a;
        boolean d;
        boolean n;
        boolean p;
        boolean s;
        Set<String> names = new LinkedHashSet<String>();
        List<String> displayOptions = new ArrayList<String>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final NutsArgument a = commandLine.peek();
        if (a.isOption()) {
            if (a.getStringKey().equals("--sort")) {
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isOption()) {
            switch (a.getStringKey()) {
                case "-a": {
                    options.a = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-d": {
                    options.d = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-n": {
                    options.n = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-p": {
                    options.p = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-s": {
                    options.s = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-f": {
                    options.file = commandLine.nextString().getStringValue();
                    return true;
                }
            }
        } else {
            options.names.add(commandLine.next().getString());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.p || options.names.isEmpty()) {
            Map<String, String> result = new LinkedHashMap<>();
            for (JShellBuiltin command : context.getRootContext().builtins().getAll()) {
                result.put(command.getName(), command.isEnabled() ? "enabled" : "disabled");
            }
            context.setPrintlnOutObject(context);
        } else if (options.n) {
            List<String> nobuiltin = new ArrayList<>();
            for (String name : options.names) {
                JShellBuiltin c = context.getRootContext().builtins().find(name);
                if (c == null) {
                    nobuiltin.add(name);
                } else {
                    c.setEnabled(false);
                }
            }
            if (!nobuiltin.isEmpty()) {
                context.setErrObject(nobuiltin);
            }
        }
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context) {
        if (context.getResult() instanceof Map) {
            for (Map.Entry<String, String> entry : ((Map<String, String>) context.getResult()).entrySet()) {
                context.out().println(entry.getValue() + " " + entry.getKey());
            }
        } else if (context.getResult() instanceof List) {
            for (String s : ((List<String>) context.getResult())) {
                context.out().printf("@@enable: ######{{%s}}###### : not a shell builti@@%n", s);
            }
        }
    }

}
