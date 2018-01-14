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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.CommandNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by vpc on 1/7/17.
 */
public class HelpCommand extends AbstractNutsCommand {

    public HelpCommand() {
        super("help", CORE_SUPPORT);
    }

    public int run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        NutsPrintStream out = context.getTerminal().getOut();
        if (cmdLine.isEmpty()) {
            if (cmdLine.isExecMode()) {
                out.drawln(context.getValidWorkspace().getHelpString());
                out.drawln("===AVAILABLE COMMANDS ARE:===");
                NutsCommand[] commands = context.getCommandLine().getCommands();
                Arrays.sort(commands, new Comparator<NutsCommand>() {
                    @Override
                    public int compare(NutsCommand o1, NutsCommand o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (NutsCommand cmd : commands) {
                    out.drawln("[[" + CoreStringUtils.alignLeft(cmd.getName(),15) + "]] : " + cmd.getHelpHeader());
                }
            }
            return 0;
        }

        while (!cmdLine.isEmpty()) {
            String command = cmdLine.readNonOptionOrError(new CommandNonOption("Command", context)).getString();
            if (cmdLine.isExecMode()) {
                NutsCommand command1 = context.getCommandLine().findCommand(command);
                if (command1 == null) {
                    context.getTerminal().getErr().println("Command not found : " + command);
                } else {
                    String help = command1.getHelp();
                    out.drawln("==Command== " + command);
                    out.drawln(help);
                }
            }
        }
        return 0;
    }
}
