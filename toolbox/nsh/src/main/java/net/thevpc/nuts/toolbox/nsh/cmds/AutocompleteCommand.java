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
import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellAutoCompleteCandidate;

import java.util.*;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class AutocompleteCommand extends SimpleNshBuiltin {

    public AutocompleteCommand() {
        super("autocomplete", DEFAULT_SUPPORT);
    }

    private static class Options {

        String cmd = null;
        List<String> items = new ArrayList<>();
        int index = -1;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (!cmdLine.peek().isOption()) {
            while (cmdLine.hasNext()) {
                String s = cmdLine.next().getString();
                if (options.cmd == null) {
                    options.cmd = s;
                } else {
                    if (s.startsWith("[]") && options.index < 0) {
                        options.index = options.items.size();
                        options.items.add(s.substring(2));
                    } else {
                        options.items.add(s);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.cmd == null) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("missing JShellCommandNode"), 1);
        }
        if (options.index < 0) {
            options.index = options.items.size();
            options.items.add("");
        }
        List<JShellAutoCompleteCandidate> aa = context.getRootContext().resolveAutoCompleteCandidates(
                options.cmd, options.items, options.index,
                context.getSession().commandLine().create(options.items).toString()
        );
        Properties p = new Properties();
        for (JShellAutoCompleteCandidate autoCompleteCandidate : aa) {
            String value = autoCompleteCandidate.getValue();
            String dvalue = autoCompleteCandidate.getDisplay();
            if (dvalue != null && dvalue.equals(value)) {
                dvalue = null;
            }
            p.setProperty(value == null ? "" : value, dvalue == null ? "" : dvalue);
        }
        switch (context.getSession().getOutputFormat()) {
            case PLAIN: {
                NutsTextManager text = context.getSession().text();
                for (String o : new TreeSet<String>((Set) p.keySet())) {
                    if (o.startsWith("-")) {
                        // option
                        context.getSession().out().printf("%s\n", text.ofStyled(o,NutsTextStyle.primary4()));
                    } else if (o.startsWith("<")) {
                        context.getSession().out().printf("%s\n", text.ofStyled(o,NutsTextStyle.primary1()));
                    } else {
                        context.getSession().out().printf("%s\n",
                                text.ofStyled(o,NutsTextStyle.pale())
                        );
                    }
                }
                break;
            }
            default: {
                context.getSession().out().printlnf(p);
            }
        }
    }

}
