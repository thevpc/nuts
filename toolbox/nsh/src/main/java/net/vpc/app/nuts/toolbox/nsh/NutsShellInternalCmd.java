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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceExtensionManager;
import net.vpc.common.javashell.JavaShellEvalContext;
import net.vpc.common.javashell.cmds.JavaShellInternalCmd;

class NutsShellInternalCmd implements JavaShellInternalCmd {

    private final NutsCommand ncommand;
    private final DefaultNutsConsole component;

    public NutsShellInternalCmd(NutsCommand ncommand, DefaultNutsConsole component) {
        this.ncommand = ncommand;
        this.component = component;
    }

    @Override
    public int exec(String[] command, JavaShellEvalContext shell) throws Exception {
        NutsJavaShellEvalContext ncontext = (NutsJavaShellEvalContext) shell;
        NutsCommandContext commandContext = ncontext.getCommandContext();
        NutsCommandContext context = component.getContext();
        NutsSession session = context.getSession().copy();
        NutsWorkspace workspace = context.getWorkspace();
        NutsWorkspaceExtensionManager extensionManager = workspace.getExtensionManager();
        session.setTerminal(
                context.getTerminal(),
                shell.getIn(),
                extensionManager.createPrintStream(shell.getOut(),true),
                extensionManager.createPrintStream(shell.getErr(),true)
        );
        commandContext.setSession(session);
        commandContext.setEnv(shell.getEnv().getEnv());
        return ncommand.exec(command, commandContext);
    }

    @Override
    public String getHelp() {
        return ncommand.getHelp();
    }

    @Override
    public String getName() {
        return ncommand.getName();
    }

    @Override
    public String getHelpHeader() {
        return ncommand.getHelpHeader();
    }
}
