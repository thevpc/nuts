///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
// *
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// *
// * Copyright (C) 2016-2020 thevpc
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License along
// * with this program; if not, write to the Free Software Foundation, Inc.,
// * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// * ====================================================================
// */
//package net.vpc.app.nuts.toolbox.nsh;
//
//import net.vpc.app.nuts.NutsSession;
//import net.vpc.app.nuts.NutsWorkspace;
//import net.vpc.common.javashell.ConsoleContext;
//import net.vpc.common.javashell.cmds.Command;
//
//class NutsShellInternalCmd implements Command {
//
//    private final NshCommand ncommand;
//    private final NutsJavaShell component;
//
//    public NutsShellInternalCmd(NshCommand ncommand, NutsJavaShell component) {
//        this.ncommand = ncommand;
//        this.component = component;
//    }
//
//    @Override
//    public int exec(String[] command, ConsoleContext shell) throws Exception {
//        NutsJavaShellEvalContext ncontext = (NutsJavaShellEvalContext) shell;
//        NutsConsoleContext commandContext = ncontext.getCommandContext();
//        NutsConsoleContext context = component.getContext();
//        NutsSession session = context.getSession().copy();
//        NutsWorkspace workspace = context.getWorkspace();
//
//        session.setTerminal(ncontext.getTerminal().copy());
//        commandContext.setSession(session);
//        commandContext.setEnv(shell.env().env());
//        return ncommand.exec(command, new DefaultNutsCommandContext(commandContext,ncommand));
//    }
//
//    @Override
//    public String getHelp() {
//        return ncommand.getHelp();
//    }
//
//    @Override
//    public String getName() {
//        return ncommand.getName();
//    }
//
//    @Override
//    public String getHelpHeader() {
//        return ncommand.getHelpHeader();
//    }
//}
