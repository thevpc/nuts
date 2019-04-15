/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.options.CommandNonOption;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.strings.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class HelpCommand extends AbstractNutsCommand {

    public HelpCommand() {
        super("help", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        boolean showColors = false;
        List<String> commandNames = new ArrayList<>();
        Argument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAll("-c", "--colors")) {
                showColors = true;
            } else if (cmdLine.isOption()) {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException("Invalid option " + cmdLine.read().getStringExpression());
                }
            } else {
                commandNames.add(cmdLine.readNonOption(new CommandNonOption("command", context.consoleContext())).getStringOrError());
            }
        }
        if (cmdLine.isExecMode()) {
            if (showColors) {
                String colorsText = context.getWorkspace().io().getResourceString("/net/vpc/app/nuts/toolbox/nuts-help-colors.help", HelpCommand.class, "no help found");
                context.out().println(colorsText);
            } else {
                if (commandNames.isEmpty()) {
                    String helpText = context.getWorkspace().io().getResourceString("/net/vpc/app/nuts/toolbox/nsh.help", HelpCommand.class, "no help found");
                    context.out().println(helpText);
                    context.out().println("@@AVAILABLE COMMANDS ARE:@@");
                    NutsCommand[] commands = context.getShell().getCommands();
                    Arrays.sort(commands, new Comparator<NutsCommand>() {
                        @Override
                        public int compare(NutsCommand o1, NutsCommand o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    int max = 1;
                    for (NutsCommand cmd : commands) {
                        int x = cmd.getName().length();
                        if (x > max) {
                            max = x;
                        }
                    }
                    for (NutsCommand cmd : commands) {
                        context.out().printf("##%s## : ", StringUtils.alignLeft(cmd.getName(), max));
                        context.out().println(cmd.getHelpHeader()); //formatted
                    }
                } else {
                    for (String commandName : commandNames) {
                        NutsCommand command1 = context.getShell().findCommand(commandName);
                        if (command1 == null) {
                            context.err().printf("Command not found : %s\n", commandName);
                        } else {
                            String help = command1.getHelp();
                            context.out().printf("==Command== %s\f", commandName);
                            context.out().println(help);
                        }

                    }
                }
            }
        }
        return 0;
    }
}
