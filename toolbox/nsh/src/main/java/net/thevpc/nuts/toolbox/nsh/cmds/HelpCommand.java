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

import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.toolbox.nsh.options.CommandNonOption;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.common.strings.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.jshell.JShellBuiltin;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
public class HelpCommand extends AbstractNshBuiltin {

    public HelpCommand() {
        super("help", DEFAULT_SUPPORT);
    }

    @Override
    public void exec(String[] args, NshExecutionContext context) {
        NutsCommandLine cmdLine = cmdLine(args, context);
        boolean showColors = false;
        List<String> commandNames = new ArrayList<>();
        NutsArgument a;
        boolean code = false;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //
            } else if (cmdLine.next("-c", "--colors") != null) {
                showColors = true;
            } else if (cmdLine.next("--ntf") != null) {
                code = true;
                context.getSession().getTerminal().setMode(NutsTerminalMode.FILTERED);
            } else if (cmdLine.peek().isOption()) {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "Invalid option " + cmdLine.next().getString());
                }
            } else {
                commandNames.add(cmdLine.nextNonOption(new CommandNonOption("command", context.getNutsShellContext())).required().getString());
            }
        }
        Function<String, String> ss = code ? new Function<String, String>() {
            @Override
            public String apply(String t) {
                return context.getWorkspace().formats().text().escapeText(t);
            }
        } : x -> x;
        if (cmdLine.isExecMode()) {
            if (showColors) {
                String colorsText = context.getWorkspace().formats().text().loadFormattedString("/net/thevpc/nuts/toolbox/nuts-help-colors.ntf", HelpCommand.class.getClassLoader(), "no help found");
                context.out().println(ss.apply(colorsText));
            } else {
                if (commandNames.isEmpty()) {
                    String helpText = context.getWorkspace().formats().text().loadFormattedString("/net/thevpc/nuts/toolbox/nsh.ntf", HelpCommand.class.getClassLoader(), "no help found");
                    context.out().println(ss.apply(helpText));
                    context.out().println(ss.apply("##AVAILABLE COMMANDS ARE:##"));
                    JShellBuiltin[] commands = context.getGlobalContext().builtins().getAll();
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
                        if (code) {
                            context.out().printf("```#####%s#####``` : ", ss.apply(StringUtils.alignLeft(cmd.getName(), max)));
                        } else {
                            context.out().printf("#####%s##### : ", StringUtils.alignLeft(cmd.getName(), max));
                        }
                        context.out().println(ss.apply(cmd.getHelpHeader())); //formatted
                    }
                } else {
                    for (String commandName : commandNames) {
                        JShellBuiltin command1 = context.getGlobalContext().builtins().find(commandName);
                        if (command1 == null) {
                            context.err().printf("JShellCommandNode not found : %s\n", ss.apply(commandName));
                        } else {
                            String help = command1.getHelp();
                            if (code) {
                                context.out().printf("```#####COMMAND#####``` : %s\f", ss.apply(commandName));
                            } else {
                                context.out().printf("#####COMMAND##### : %s\f", ss.apply(commandName));
                            }
                            context.out().println(ss.apply(help));
                        }

                    }
                }
            }
        }
    }
}
