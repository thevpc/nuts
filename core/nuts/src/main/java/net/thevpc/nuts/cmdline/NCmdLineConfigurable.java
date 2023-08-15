/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;

import java.util.Arrays;

/**
 * Configurable interface define a extensible way to configure nuts commands
 * and objects using simple argument line options.
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.5
 */
public interface NCmdLineConfigurable {

    /**
     * configure the current command with the given arguments.
     * <p>
     * Sample implementation would be
     * <pre>
     *         return NCmdLineConfigurable.configure(this, getSession(), skipUnsupported, args, getCommandName(),getSession());
     * </pre>
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     *                        silently
     * @param args            arguments to configure with
     * @return {@code this} instance
     */
    Object configure(boolean skipUnsupported, String... args);

    /**
     * configure the current command with the given arguments.
     * @param skipUnsupported when true, all unsupported options are skipped
     *                        silently
     * @param cmdLine         arguments to configure with
     * @return true when the at least one argument was processed
     */
    default boolean configure(boolean skipUnsupported, NCmdLine cmdLine) {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        NSession session = cmdLine.getSession();
        while (cmdLine.hasNext()) {
            if (robustMode) {
                String[] before = cmdLine.toStringArray();
                if (!configureFirst(cmdLine)) {
                    if (skipUnsupported) {
                        cmdLine.skip();
                    } else {
                        cmdLine.throwUnexpectedArgument();
                    }
                } else {
                    conf = true;
                }
                String[] after = cmdLine.toStringArray();
                if (Arrays.equals(before, after)) {
                    throw new NIllegalArgumentException(session,
                            NMsg.ofC(
                                    "bad implementation of configureFirst in class %s."
                                            + " cmdLine is not consumed; perhaps missing skip() class."
                                            + " args = %s", getClass().getName(), Arrays.toString(after)
                            )
                    );
                }
            } else {
                if (!configureFirst(cmdLine)) {
                    if (skipUnsupported) {
                        cmdLine.skip();
                    } else {
                        cmdLine.throwUnexpectedArgument();
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

    /**
     * ask {@code this} instance to configure with the very first argument of
     * {@code cmdLine}. If the first argument is not supported, return
     * {@code false} and consume (skip/read) the argument. If the argument
     * required one or more parameters, these arguments are also consumed and
     * finally return {@code true}
     *
     * @param cmdLine arguments to configure with
     * @return true when the at least one argument was processed
     */
    boolean configureFirst(NCmdLine cmdLine);

    default void configureLast(NCmdLine cmdLine) {
        if (!configureFirst(cmdLine)) {
            cmdLine.throwUnexpectedArgument();
        }
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param <T>             {@code this} Type
     * @param c               argument configurable
     * @param skipUnsupported skipUnsupported
     * @param args            argument to configure with
     * @param commandName     commandName
     * @return {@code this} instance
     */
    static <T> T configure(NCmdLineConfigurable c, boolean skipUnsupported, String[] args, String commandName, NSession session) {
        c.configure(skipUnsupported, NCmdLine.of(args).setSession(session).setCommandName(commandName));
        return (T) c;
    }
}
