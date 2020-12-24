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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.util;

import java.util.Arrays;

import net.thevpc.nuts.NutsCommandLineConfigurable;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsCommandLine;

/**
 *
 * @author thevpc
 */
public class NutsConfigurableHelper {

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param c argument configurable
     * @param ws workspace
     * @param skipUnsupported skipUnsupported
     * @param args argument to configure with
     * @param commandName commandName
     * @param <T> {@code this} Type
     * @return {@code this} instance
     */
    public static <T> T configure(NutsCommandLineConfigurable c, NutsWorkspace ws, boolean skipUnsupported, String[] args, String commandName) {
        c.configure(skipUnsupported, ws.commandLine().create(args).setCommandName(commandName));
        return (T) c;
    }

    public static boolean configure(NutsCommandLineConfigurable c, NutsWorkspace ws, boolean skipUnsupported, NutsCommandLine commandLine) {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        while (commandLine.hasNext()) {
            if (robustMode) {
                String[] before = commandLine.toStringArray();
                if (!c.configureFirst(commandLine)) {
                    if (skipUnsupported) {
                        commandLine.skip();
                    } else {
                        commandLine.unexpectedArgument();
                    }
                } else {
                    conf = true;
                }
                String[] after = commandLine.toStringArray();
                if (Arrays.equals(before, after)) {
                    throw new IllegalStateException("bad implementation of configureFirst in class " + c.getClass().getName() + "."
                            + " commandline is not consumed; perhaps missing skip() class."
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
