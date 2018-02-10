/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by vpc on 1/7/17.
 */
public class ErrCommand extends AbstractNutsCommand {

    public ErrCommand() {
        super("err", CORE_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandAutoComplete autoComplete = context.getAutoComplete();
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        boolean verbose = false;
        boolean visitedArgVerbose = false;
        while (!cmdLine.isEmpty()) {
            if (!visitedArgVerbose && cmdLine.read("--trace", "--stacktrace", "-v")) {
                visitedArgVerbose = true;
                verbose = true;
            } else {
                cmdLine.requireEmpty();
            }
        }
        if (cmdLine.isExecMode()) {
            String lastErrorMessage = context.getCommandLine().getLastErrorMessage();
            Throwable lastError = context.getCommandLine().getLastThrowable();
            if (lastError == null) {
                return 0;
            }
            if (verbose) {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                PrintStream po = new PrintStream(o);
                lastError.printStackTrace(po);
                po.flush();
                String s = new String(o.toByteArray());
                context.getTerminal().getErr().println(s);
            } else {
                context.getTerminal().getErr().println(lastErrorMessage);
            }
        }
        return 0;
    }
}
