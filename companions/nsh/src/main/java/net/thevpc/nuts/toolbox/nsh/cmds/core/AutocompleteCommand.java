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
package net.thevpc.nuts.toolbox.nsh.cmds.core;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.autocomplete.NShellAutoCompleteCandidate;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class AutocompleteCommand extends NShellBuiltinDefault {

    public AutocompleteCommand() {
        super("autocomplete", NSupported.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (!cmdLine.isNextOption()) {
            while (cmdLine.hasNext()) {
                String s = cmdLine.next().flatMap(NLiteral::asString).get(session);
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
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return onCmdNextOption(arg, cmdLine, context);
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.cmd == null) {
            throw new NExecutionException(session, NMsg.ofPlain("missing NShellCommandNode"), NExecutionException.ERROR_1);
        }
        if (options.index < 0) {
            options.index = options.items.size();
            options.items.add("");
        }
        List<NShellAutoCompleteCandidate> aa = context.getShellContext().resolveAutoCompleteCandidates(
                options.cmd, options.items, options.index,
                NCmdLine.of(options.items).toString()
        );
        Properties p = new Properties();
        for (NShellAutoCompleteCandidate autoCompleteCandidate : aa) {
            String value = autoCompleteCandidate.getValue();
            String dvalue = autoCompleteCandidate.getDisplay();
            if (dvalue != null && dvalue.equals(value)) {
                dvalue = null;
            }
            p.setProperty(value == null ? "" : value, dvalue == null ? "" : dvalue);
        }
        switch (session.getOutputFormat()) {
            case PLAIN: {
                NTexts text = NTexts.of(session);
                for (String o : new TreeSet<String>((Set) p.keySet())) {
                    if (o.startsWith("-")) {
                        // option
                        session.out().println(NMsg.ofC("%s", text.ofStyled(o, NTextStyle.primary4())));
                    } else if (o.startsWith("<")) {
                        session.out().println(NMsg.ofC("%s", text.ofStyled(o, NTextStyle.primary1())));
                    } else {
                        session.out().println(NMsg.ofC("%s",
                                text.ofStyled(o, NTextStyle.pale())
                        ));
                    }
                }
                break;
            }
            default: {
                session.out().println(p);
            }
        }
    }

    private static class Options {

        String cmd = null;
        List<String> items = new ArrayList<>();
        int index = -1;
    }

}
