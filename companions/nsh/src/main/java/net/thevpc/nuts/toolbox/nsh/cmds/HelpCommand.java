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
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles._StringUtils;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.options.CommandNonOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by vpc on 1/7/17.
 */
public class HelpCommand extends SimpleJShellBuiltin {

    public HelpCommand() {
        super("help", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        NutsSession session = context.getSession();
        Options options = context.getOptions();
        if (commandLine.next("--ntf") != null) {
            options.code = true;
            return true;
        } else if (commandLine.peek().get(session).isNonOption()) {
            options.commandNames.add(
                    commandLine.nextNonOption(new CommandNonOption("command", context.getShellContext()))
                    .get().asString().get(session));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.code) {
            context.getSession().getTerminal().setOut(
                    context.getSession().getTerminal().out().setMode(NutsTerminalMode.INHERITED)
            );
        }
        final NutsTexts text = NutsTexts.of(context.getSession());
        Function<String, String> ss = options.code ? new Function<String, String>() {
            @Override
            public String apply(String t) {
                return text.ofPlain(t).toString();
            }
        } : x -> x;
        if (commandLine.isExecMode()) {
            if (options.commandNames.isEmpty()) {
                NutsText n = text.parser().parseResource("/net/thevpc/nuts/toolbox/nsh.ntf",
                        text.parser().createLoader(HelpCommand.class.getClassLoader())
                );
                String helpText = (n == null ? "no help found" : n.toString());
                context.out().println(ss.apply(helpText));
                context.out().println(NutsTexts.of(context.getSession()).ofStyled("AVAILABLE COMMANDS ARE:", NutsTextStyle.primary1()));
                JShellBuiltin[] commands = context.getShellContext().builtins().getAll();
                Arrays.sort(commands, new Comparator<JShellBuiltin>() {
                    @Override
                    public int compare(JShellBuiltin o1, JShellBuiltin o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                int max = 1;
                for (JShellBuiltin cmd : commands) {
                    int x = cmd.getName().length();
                    if (x > max) {
                        max = x;
                    }
                }
                for (JShellBuiltin cmd : commands) {
                    context.out().printf("%s : ", text.ofStyled(_StringUtils.formatLeft(cmd.getName(), max), NutsTextStyle.primary4()));
                    context.out().println(ss.apply(cmd.getHelpHeader())); //formatted
                }
            } else {
                int x = 0;
                for (String commandName : options.commandNames) {
                    JShellBuiltin command1 = context.getShellContext().builtins().find(commandName);
                    if (command1 == null) {
                        context.err().printf("command not found : %s\n", text.ofStyled(commandName, NutsTextStyle.error()));
                        x = 1;
                    } else {
                        String help = command1.getHelp();
                        context.out().printf("%s : %s\f", text.ofStyled("COMMAND", NutsTextStyle.primary4()), "commandName");
                        context.out().println(ss.apply(help));
                    }
                }
                throwExecutionException("error", x, context.getSession());
            }
        }
    }

    private static class Options {
        boolean code = false;
        List<String> commandNames = new ArrayList<>();
    }

}
