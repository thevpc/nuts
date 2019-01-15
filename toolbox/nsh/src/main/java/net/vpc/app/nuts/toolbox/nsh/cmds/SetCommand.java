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

import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import net.vpc.common.javashell.JavaShell;
import net.vpc.common.javashell.cmds.CmdSyntaxError;
import net.vpc.common.javashell.cmds.CommandContext;

/**
 * Created by vpc on 1/7/17.
 */
public class SetCommand extends AbstractNutsCommand {


    public SetCommand() {
        super("set", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        JavaShell shell = context.getShell();
        int commandArgsCount = args.length;
        if (commandArgsCount == 0) {
            throw new CmdSyntaxError(1, args, getName(), getHelpHeader(), getHelp());
        }
        context.out().println("setting");
        for (int i = 0; i < commandArgsCount; i++) {
            String name = args[i];
            boolean isSetted = false;
            if (i < (commandArgsCount - 1)) {
                String eq = args[i + 1];
                if ("=".equals(eq)) {
                    if (i < (commandArgsCount - 2)) {
                        doSet(name, args[i + 2], context);
                        i += 2;
                        isSetted = true;
                    } else {
                        doSet(name, null, context);
                        i += 1;
                        isSetted = true;
                    }
                }
            }
            if (!isSetted) {
                throw new CmdSyntaxError(1, args, getName(), getHelpHeader(), getHelp());
            }
        }
        return 0;
    }
    
    private void doSet(String name, String value, CommandContext context) throws RemoteException {
        if (value == null) {
            context.env().setEnv(name, value);
        } else {
            String valsEnv = context.env().getEnv().getProperty(name + ".VALUES");
            if (valsEnv != null) {
                List<String> stringList = Arrays.asList(valsEnv.split(":"));
                if (!stringList.contains(value)) {
                    System.err.printf("Invalid value %s=%s\n", name,value);
                    System.err.printf("Valid values are \n");
                    for (String s : stringList) {
                        System.err.printf("\t%s\n" , s);
                    }
                    return;
                }
            }
            context.env().setEnv(name, value);
        }
    }
}
