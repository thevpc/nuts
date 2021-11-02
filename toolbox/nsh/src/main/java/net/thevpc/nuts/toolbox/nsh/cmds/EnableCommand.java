/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellBuiltin;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class EnableCommand extends SimpleNshBuiltin {

    public EnableCommand() {
        super("enable", DEFAULT_SUPPORT);
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
            if (a.getKey().getString().equals("--sort")) {
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isOption()) {
            switch (a.getKey().getString()) {
                case "-a": {
                    options.a = commandLine.nextBoolean().getValue().getBoolean();
                    return true;
                }
                case "-d": {
                    options.d = commandLine.nextBoolean().getValue().getBoolean();
                    return true;
                }
                case "-n": {
                    options.n = commandLine.nextBoolean().getValue().getBoolean();
                    return true;
                }
                case "-p": {
                    options.p = commandLine.nextBoolean().getValue().getBoolean();
                    return true;
                }
                case "-s": {
                    options.s = commandLine.nextBoolean().getValue().getBoolean();
                    return true;
                }
                case "-f": {
                    options.file = commandLine.nextString().getValue().getString();
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
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.p || options.names.isEmpty()) {
            Map<String, String> result = new LinkedHashMap<>();
            for (JShellBuiltin command : context.getRootContext().builtins().getAll()) {
                result.put(command.getName(), command.isEnabled() ? "enabled" : "disabled");
            }
            switch (context.getSession().getOutputFormat()) {
                case PLAIN: {
                    for (Map.Entry<String, String> entry : result.entrySet()) {
                        context.getSession().out().println(entry.getValue() + " " + entry.getKey());
                    }
                    //if list
//                    for (String s : ((List<String>) context.getResult())) {
//                        context.out().printf("%s%n",
//                                text.builder().append("enable: ", NutsTextStyle.error())
//                                        .append(s, NutsTextStyle.primary5())
//                                        .append(" ")
//                                        .append("not a shell builtin", NutsTextStyle.error())
//                        );
//                    }
                    break;
                }
                default: {
                    context.getSession().out().printlnf(result);
                }
            }
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
                throwExecutionException(nobuiltin, 1, context.getSession());
            }
        }
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

}
