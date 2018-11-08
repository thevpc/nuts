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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.toolbox.nsh.*;
import net.vpc.common.javashell.QuitShellException;

import net.vpc.common.javashell.InterrupShellException;

/**
 * Created by vpc on 1/7/17.
 */
public class ConsoleCommand extends AbstractNutsCommand {

    public ConsoleCommand() {
        super("nsh", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandAutoComplete autoComplete = context.getAutoComplete();
        if (autoComplete != null) {
            return -1;
        }

        if (args.length > 0) {
            NutsTerminal terminal = context.getTerminal();
            NutsConsole console = new DefaultNutsConsole();
            console.init(context.getValidWorkspace(),context.getSession());
            return console.runCommand(args);
        }

        NutsTerminal terminal = context.getTerminal();
        NutsPrintStream out = terminal.getFormattedOut();
        out.printf("**Nuts** console (**Network Updatable Things Services**) **v%s** (c) vpc 2017\n", context.getValidWorkspace().getConfigManager().getWorkspaceRuntimeId().getVersion().toString());

        NutsConsole console = new DefaultNutsConsole();
        console.init(context.getValidWorkspace(),context.getSession());

        while (true) {

            terminal = context.getTerminal();
            NutsWorkspace ws = context.getWorkspace();
            String wss = ws == null ? "" : context.resolvePath(ws.getConfigManager().getWorkspaceLocation());
            String login = null;
            if (ws != null) {
                login = ws.getSecurityManager().getCurrentLogin();
            }
            String prompt = login + "@" + wss;
            if (!CoreStringUtils.isEmpty(context.getServiceName())) {
                prompt = prompt + "@" + context.getServiceName();
            }
            prompt += "> ";

            String line = null;
            try {
                line = terminal.readLine(prompt);
            } catch (InterrupShellException ex) {
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
