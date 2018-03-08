/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import java.util.ArrayList;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CommandNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class HelpCommand extends AbstractNutsCommand {

    public HelpCommand() {
        super("help", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        NutsPrintStream out = context.getTerminal().getOut();
        boolean showLicense = false;
        List<String> commandNames = new ArrayList<>();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.read("-l", "--license")) {
                showLicense = true;
            } else if (cmdLine.isOption()) {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException("Invalid option " + cmdLine.read().getString());
                }
            } else {
                commandNames.add(cmdLine.readNonOption(new CommandNonOption("command", context)).getStringOrError());
            }
        }
        if (cmdLine.isExecMode()) {
            if (showLicense) {
                String licenseText = CoreNutsUtils.getResourceString("/net/vpc/app/nuts/nuts.license", context.getValidWorkspace(), HelpCommand.class);
                out.println(licenseText);
            } else {
                if (commandNames.isEmpty()) {
                    String helpText = CoreNutsUtils.getResourceString("/net/vpc/app/nuts/nuts-help.help", context.getValidWorkspace(), HelpCommand.class);
                    out.println(helpText);
                    out.println("@@AVAILABLE COMMANDS ARE:@@");
                    NutsCommand[] commands = context.getCommandLine().getCommands();
                    Arrays.sort(commands, new Comparator<NutsCommand>() {
                        @Override
                        public int compare(NutsCommand o1, NutsCommand o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    for (NutsCommand cmd : commands) {
                        out.printf("##%s## : ", CoreStringUtils.alignLeft(cmd.getName(), 15));
                        out.println(cmd.getHelpHeader()); //formatted
                    }
                } else {
                    for (String commandName : commandNames) {
                        NutsCommand command1 = context.getCommandLine().findCommand(commandName);
                        if (command1 == null) {
                            context.getTerminal().getErr().printf("Command not found : %s\n", commandName);
                        } else {
                            String help = command1.getHelp();
                            out.printf("==Command== %s\f", commandName);
                            out.println(help);
                        }

                    }
                }
            }
        }
        return 0;
    }
}
