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
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NLiteral;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class EnableCommand extends SimpleJShellBuiltin {

    public EnableCommand() {
        super("enable", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        final NArg a = commandLine.peek().get(session);
        if (a.isOption()) {
            if (a.key().equals("--sort")) {
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isOption()) {
            switch(a.key()) {
                case "-a": {
                    commandLine.withNextFlag((v, r, s) -> options.a = v);
                    return true;
                }
                case "-d": {
                    commandLine.withNextFlag((v, r, s) -> options.d = v);
                    return true;
                }
                case "-n": {
                    commandLine.withNextFlag((v, r, s) -> options.n = v);
                    return true;
                }
                case "-p": {
                    commandLine.withNextFlag((v, r, s) -> options.p = v);
                    return true;
                }
                case "-s": {
                    commandLine.withNextFlag((v, r, s) -> options.s = v);
                    return true;
                }
                case "-f": {
                    commandLine.withNextEntry((v, r, s) -> options.file = v);
                    return true;
                }
            }
        } else {
            options.names.add(commandLine.next().flatMap(NLiteral::asString).get(session));
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.p || options.names.isEmpty()) {
            Map<String, String> result = new LinkedHashMap<>();
            for (JShellBuiltin command : context.builtins().getAll()) {
                result.put(command.getName(), command.isEnabled() ? "enabled" : "disabled");
            }
            switch (context.getSession().getOutputFormat()) {
                case PLAIN: {
                    for (Map.Entry<String, String> entry : result.entrySet()) {
                        context.getSession().out().println(entry.getValue() + " " + entry.getKey());
                    }
                    //if list
//                    for (String s : ((List<String>) context.getResult())) {
//                        context.out().print(NMsg.ofC("%s%n",
//                                text.builder().append("enable: ", NutsTextStyle.error())
//                                        .append(s, NutsTextStyle.primary5())
//                                        .append(" ")
//                                        .append("not a shell builtin", NutsTextStyle.error())
//                        );
//                    }
                    break;
                }
                default: {
                    context.getSession().out().println(result);
                }
            }
        } else if (options.n) {
            List<String> nobuiltin = new ArrayList<>();
            for (String name : options.names) {
                JShellBuiltin c = context.builtins().find(name);
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
