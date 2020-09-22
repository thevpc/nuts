/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util;

import java.util.Arrays;
import net.vpc.app.nuts.NutsConfigurable;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsCommandLine;

/**
 *
 * @author vpc
 */
public class NutsConfigurableHelper {

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    public static <T> T configure(NutsConfigurable c, NutsWorkspace ws, boolean skipUnsupported, String[] args, String commandName) {
        c.configure(skipUnsupported, ws.commandLine().create(args).setCommandName(commandName));
        return (T) c;
    }

    public static boolean configure(NutsConfigurable c, NutsWorkspace ws, boolean skipUnsupported, NutsCommandLine commandLine) {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        while (commandLine.hasNext()) {
            if (robustMode) {
                String[] before = commandLine.toArray();
                if (!c.configureFirst(commandLine)) {
                    if (skipUnsupported) {
                        commandLine.skip();
                    } else {
                        commandLine.unexpectedArgument();
                    }
                } else {
                    conf = true;
                }
                String[] after = commandLine.toArray();
                if (Arrays.equals(before, after)) {
                    throw new IllegalStateException("Bad implementation of configureFirst in class " + c.getClass().getName() + "."
                            + " Commandline is not consumed. Perhaps missing skip() class."
                            + " args = " + Arrays.toString(after));
                }
            } else {
                if (!c.configureFirst(commandLine)) {
                    if (skipUnsupported) {
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
