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

import java.util.Arrays;
import java.util.List;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.common.javashell.JShellBuiltin;
import net.vpc.app.nuts.toolbox.nsh.NshExecutionContext;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
public class CommandCommand extends SimpleNshBuiltin {

    public CommandCommand() {
        super("command", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean p;
        String commandName;
        List<String> args;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = null;
        //inverse configuration order
        if (context.getExecutionContext().configureFirst(commandLine)) {
            return true;
        } else if ((a = commandLine.nextBoolean("-p")) != null) {
            options.p = a.getBooleanValue();
        } else if (!commandLine.peek().isOption()) {
            if (options.commandName == null) {
                options.commandName = commandLine.next().getString();
            }
            options.args.addAll(Arrays.asList(commandLine.toArray()));
            commandLine.skipAll();
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.commandName!=null) {
            JShellBuiltin a = context.getGlobalContext().builtins().find(options.commandName);
            if (a != null) {
                a.exec(options.args.toArray(new String[0]), context.getExecutionContext());
            } else {
                context.getWorkspace()
                        .exec()
                        .command(options.commandName)
                        .command(options.args)
                        .setDirectory(context.getGlobalContext().getCwd())
                        .setEnv(context.getExecutionContext().vars().getExported())
                        .run()
                        .failFast();
            }
        }
    }

}
