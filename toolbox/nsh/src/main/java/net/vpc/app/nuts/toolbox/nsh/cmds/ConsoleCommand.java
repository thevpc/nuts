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

import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.*;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandAutoComplete;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.javashell.InterruptShellException;
import net.vpc.common.javashell.QuitShellException;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class ConsoleCommand extends AbstractNutsCommand {

    public ConsoleCommand() {
        super("nsh", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandAutoComplete autoComplete = context.consoleContext().getAutoComplete();
        if (autoComplete != null) {
            return -1;
        }
        Argument a;
        boolean noColors = false;
        List<String> invokeArgs = new ArrayList<>();
        CommandLine cmdLine = new CommandLine(args);
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else  {
                while(cmdLine.hasNext()) {
                    invokeArgs.add(cmdLine.read().getExpression());
                }
            }
        }

        if (invokeArgs.size() > 0) {
            NutsJavaShell console = new NutsJavaShell(context.getWorkspace(), context.getSession());
            return console.runCommand(invokeArgs.toArray(new String[0]));
        }

        NutsTerminal terminal = context.getTerminal();
        PrintStream out = terminal.getFormattedOut();
        out.printf("**Nuts** console (**Network Updatable Things Services**) **v%s** (c) vpc 2017\n", context.getWorkspace().getConfigManager().getBootRuntime().getVersion().toString());

        NutsJavaShell console = new NutsJavaShell(context.getWorkspace(), context.getSession());

        while (true) {

            terminal = context.getTerminal();
            NutsWorkspace ws = context.getWorkspace();
            String wss = ws == null ? "" : context.getShell().getAbsolutePath(ws.getConfigManager().getWorkspaceLocation());
            String login = null;
            if (ws != null) {
                login = ws.getSecurityManager().getCurrentLogin();
            }
            String prompt = login + "@" + wss;
            if (!StringUtils.isEmpty(context.consoleContext().getServiceName())) {
                prompt = prompt + "@" + context.consoleContext().getServiceName();
            }
            prompt += "> ";

            String line = null;
            try {
                line = terminal.readLine(prompt);
            } catch (InterruptShellException ex) {
                terminal.getFormattedErr().printf("==%s==\n", ex.getMessage());
                continue;
            }
            if (line == null) {
                break;
            }
            if (line.trim().length() > 0) {
                try {
                    console.runLine(line);
                } catch (QuitShellException q) {
                    return 0;
                }
            }
        }
        return 1;
    }
}
