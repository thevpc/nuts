/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;

import net.vpc.app.nuts.extensions.cmd.cmdline.CommandNonOption;
import net.vpc.app.nuts.util.IOUtils;
import net.vpc.app.nuts.util.MapStringMapper;
import net.vpc.app.nuts.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class HelpCommand extends AbstractNutsCommand {

    public HelpCommand() {
        super("help", CORE_SUPPORT);
    }

    public void run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        NutsPrintStream out = context.getTerminal().getOut();
        if (cmdLine.isEmpty()) {
            if (cmdLine.isExecMode()) {
                out.drawln(getHelpContent());
                out.drawln("===AVAILABLE COMMANDS ARE:===");
                for (NutsCommand cmd : context.getCommandLine().getCommands()) {
                    out.drawln(cmd.getHelpHeader());
                }
            }
            return;
        }

        while (!cmdLine.isEmpty()) {
            String command = cmdLine.removeNonOptionOrError(new CommandNonOption("Command", context)).getString();
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
    }

    public String getHelpContent() {
        String help = null;
        try {
            InputStream s = null;
            try {
                s = Main.class.getResourceAsStream("/net/vpc/app/nuts/help.help");
                if (s != null) {
                    help = IOUtils.readStreamAsString(s, true);
                }
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Unable to load main help", e);
        }
        if (help == null) {
            help = "no help found";
        }

        HashMap<String, String> props = new HashMap<>();
        props.putAll((Map) System.getProperties());
        props.put("nuts.boot-version", Main.getBootVersion());
        help = StringUtils.replaceVars(help, new MapStringMapper(props));
        return help;
    }
}
