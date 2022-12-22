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

import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class AliasCommand extends SimpleJShellBuiltin {

    public AliasCommand() {
        super("alias", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        final NutsArgument a = commandLine.peek().get(session);
        if (a.isOption()) {
            if (a.key().equals("--sort")) {
                commandLine.skip();
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isKeyValue()) {
            commandLine.skip();
            options.add.put(a.key(), a.getStringValue().get(session));
            return true;
        } else {
            commandLine.skip();
            options.show.add(a.asString().get(session));
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        JShell shell = context.getShell();
        if (options.add.isEmpty() && options.show.isEmpty()) {
            options.show.addAll(context.aliases().getAll());
        }
        for (Map.Entry<String, String> entry : options.add.entrySet()) {
            context.aliases().set(entry.getKey(), entry.getValue());
        }
        List<ResultItem> outRes = new ArrayList<>();
        List<ResultItem> errRes = new ArrayList<>();
        for (String a : options.show) {
            final String v = context.aliases().get(a);
            if (v == null) {
                errRes.add(new ResultItem(a, v));
            } else {
                outRes.add(new ResultItem(a, v));
            }
        }
        switch (context.getSession().getOutputFormat()) {
            case PLAIN: {
                for (ResultItem resultItem : outRes) {
                    if (resultItem.value == null) {
                        context.getSession().err().printf("alias : %s ```error not found```%n", resultItem.name);
                    } else {
                        context.getSession().out().printf("alias : %s ='%s'%n", resultItem.name, resultItem.value);
                    }
                }
                break;
            }
            default: {
                context.getSession().out().printlnf(outRes);
            }
        }
        if (!errRes.isEmpty()) {
            throwExecutionException(errRes, 1, context.getSession());
        }
    }

    private static class Options {

        LinkedHashMap<String, String> add = new LinkedHashMap<String, String>();
        Set<String> show = new LinkedHashSet<String>();
        List<String> displayOptions = new ArrayList<String>();
    }

    private static class ResultItem {

        String name;
        String value;

        public ResultItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

    }


}
