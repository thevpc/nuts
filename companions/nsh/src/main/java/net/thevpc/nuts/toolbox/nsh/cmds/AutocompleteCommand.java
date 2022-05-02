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
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellAutoCompleteCandidate;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class AutocompleteCommand extends SimpleJShellBuiltin {

    public AutocompleteCommand() {
        super("autocomplete", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        if (!cmdLine.isNextOption()) {
            while (cmdLine.hasNext()) {
                String s = cmdLine.next().flatMap(NutsValue::asString).get(session);
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
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        if (options.cmd == null) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("missing JShellCommandNode"), 1);
        }
        if (options.index < 0) {
            options.index = options.items.size();
            options.items.add("");
        }
        List<JShellAutoCompleteCandidate> aa = context.getShellContext().resolveAutoCompleteCandidates(
                options.cmd, options.items, options.index,
                NutsCommandLine.of(options.items).toString()
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
        switch (session.getOutputFormat()) {
            case PLAIN: {
                NutsTexts text = NutsTexts.of(session);
                for (String o : new TreeSet<String>((Set) p.keySet())) {
                    if (o.startsWith("-")) {
                        // option
                        session.out().printf("%s\n", text.ofStyled(o, NutsTextStyle.primary4()));
                    } else if (o.startsWith("<")) {
                        session.out().printf("%s\n", text.ofStyled(o, NutsTextStyle.primary1()));
                    } else {
                        session.out().printf("%s\n",
                                text.ofStyled(o, NutsTextStyle.pale())
                        );
                    }
                }
                break;
            }
            default: {
                session.out().printlnf(p);
            }
        }
    }

    private static class Options {

        String cmd = null;
        List<String> items = new ArrayList<>();
        int index = -1;
    }

}
