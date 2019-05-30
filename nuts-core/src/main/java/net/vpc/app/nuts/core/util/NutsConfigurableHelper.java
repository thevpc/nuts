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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.core.util;

import java.util.Arrays;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsConfigurable;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class NutsConfigurableHelper {

    public static <T> T configure(NutsConfigurable c, NutsWorkspace ws, String[] cmdLine,String commandName) {
        c.configure(ws.parser().parseCommand(cmdLine).setCommandName(commandName), false);
        return (T) c;
    }

    public static boolean configure(NutsConfigurable c, NutsWorkspace ws, NutsCommand commandLine, boolean skipIgnored) {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        while (commandLine.hasNext()) {
            if (robustMode) {
                String[] before = commandLine.toArray();
                if (!c.configureFirst(commandLine)) {
                    if (skipIgnored) {
                        commandLine.skip();
                    } else {
                        commandLine.unexpectedArgument();
                    }
                } else {
                    conf = true;
                }
                String[] after = commandLine.toArray();
                if (Arrays.equals(before, after)) {
                    throw new IllegalStateException("Bad implementation of configureFirst in class " + c.getClass().getName()+"."
                            + " Commandline is not consumed. Perhaps missing skip() class."
                            + " args = " + Arrays.toString(after));
                }
            } else {
                if (!c.configureFirst(commandLine)) {
                    if (skipIgnored) {
                        commandLine.skip();
                    } else {
                        commandLine.unexpectedArgument();
                    }
                } else {
                    conf = true;
                }
            }
            maxLoops--;
            if (maxLoops < 0) {
                robustMode = true;
            }
        }
        return conf;
    }

}
