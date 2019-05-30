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

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNshCommand;
import net.vpc.app.nuts.toolbox.nsh.NshCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsArgument;

/**
 * Created by vpc on 1/7/17.
 */
public class FindCommandCommand extends AbstractNshCommand {


    public FindCommandCommand() {
        super("find-command", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommand cmdLine = cmdLine(args, context);
        NutsArgument a;
        List<String> commands=new ArrayList<>();
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //
            }else if (cmdLine.peek().isOption()) {
                cmdLine.skip();
            }else{
                commands.add(cmdLine.next().getString());
            }
        }
        if(commands.isEmpty()){
            for (NshCommand command : context.getShell().getCommands()) {
                context.out().println(command.getName());
            }
            return 0;
        }else{
            for (String command : commands) {
                NshCommand c = context.getShell().findCommand(command);
                if(c!=null){
                    context.out().println(c.getName());
                }else{
                    String alias = context.getShell().getAlias(command);
                    if(alias!=null){
                        context.out().println("=="+command+"==");
                    }else{
                        throw new NutsExecutionException(context.getWorkspace(),command+": command not found",1);
                    }
                }
            }
            return 0;
        }
    }
}
