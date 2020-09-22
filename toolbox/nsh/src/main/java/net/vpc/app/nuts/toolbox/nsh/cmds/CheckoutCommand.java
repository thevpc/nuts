///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// * <br>
// * Copyright (C) 2016-2020 thevpc
// * <br>
// * This program is free software; you can redistribute it and/or modify it under
// * the terms of the GNU General Public License as published by the Free Software
// * Foundation; either version 3 of the License, or (at your option) any later
// * version.
// * <br>
// * This program is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// * details.
// * <br>
// * You should have received a copy of the GNU General Public License along with
// * this program; if not, write to the Free Software Foundation, Inc., 51
// * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// * ====================================================================
// */
//package net.vpc.app.nuts.toolbox.nsh.cmds;
//
//import java.nio.file.Paths;
//import net.vpc.app.nuts.NutsDefinition;
//import net.vpc.app.nuts.NutsWorkspace;
//import net.vpc.app.nuts.toolbox.nsh.AbstractNshCommand;
//import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
//import net.vpc.app.nuts.app.options.NutsIdNonOption;
//import net.vpc.common.commandline.Argument;
//import net.vpc.common.commandline.FolderNonOption;
//
///**
// * Created by vpc on 1/7/17.
// */
//public class CheckoutCommand extends AbstractNshCommand {
//
//    public CheckoutCommand() {
//        super("checkout", DEFAULT_SUPPORT);
//    }
//
//    @Override
//    public int exec(String[] args, NutsCommandContext context) throws Exception {
//        NutsCommand cmdLine = cmdLine(args, context);
//
//        NutsCommandArg a;
//        while (cmdLine.hasNext()) {
//            if (context.configure(cmdLine)) {
//                //
//            } else {
//                String id = cmdLine.required().nextNonOption(new NutsIdNonOption("Nuts", context.getWorkspace())).getString();
//                String contentFile = cmdLine.required().nextNonOption(new FolderNonOption("folder")).getString();
//                if (cmdLine.isExecMode()) {
//                    NutsWorkspace ws = context.getWorkspace();
//                    NutsDefinition nf = ws.checkout(
//                            id,
//                            ws.io().path(contentFile),
//                            context.getSession()
//                    );
//                    context.out().printf("Folder ==%s== initialized with ==%s==\n", contentFile, nf.getId());
//                }
//            }
//        }
//        return 0;
//    }
//}
