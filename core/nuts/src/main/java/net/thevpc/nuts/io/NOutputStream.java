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
 *
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
package net.thevpc.nuts.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;

public interface NOutputStream extends NOutputTarget {

    static NOutputStream ofNull(NSession session) {
        return NIO.of(session).createNullOutputStream();
    }

    /**
     * return new in-memory NutsPrintStream implementation.
     * this is equivalent to {@code NutsMemoryPrintStream.of(session)}
     *
     * @param session session
     * @return new in-memory NutsPrintStream implementation
     */
    static NMemoryOutputStream ofInMemory(NSession session) {
        return NIO.of(session).createInMemoryOutputStream();
    }

    static NOutputStream of(OutputStream out, NSession session) {
        return NIO.of(session).createOutputStream(out);
    }

    /**
     * create print stream that supports the given {@code mode}. If the given
     * {@code out} is a PrintStream that supports {@code mode}, it should be
     * returned without modification.
     *
     * @param out      stream to wrap
     * @param mode     mode to support
     * @param terminal terminal
     * @param session  session
     * @return {@code mode} supporting PrintStream
     */
    static NOutputStream of(OutputStream out, NTerminalMode mode, NSystemTerminalBase terminal, NSession session) {
        return NIO.of(session).createOutputStream(out, mode, terminal);
    }

    static NOutputStream of(Writer out, NSession session) {
        return NIO.of(session).createOutputStream(out);
    }

    NSession getSession();

    /**
     * update session and return a new instance
     *
     * @param session new session
     * @return a new instance of NutsPrintStream
     */
    NOutputStream setSession(NSession session);

    NOutputStream flush();

    NOutputStream close();

    NOutputStream print(byte[] b);

    NOutputStream write(int b);


    NOutputStream write(byte[] buf, int off, int len);
    NOutputStream write(char[] buf, int off, int len);

    NOutputStream print(byte[] buf, int off, int len);
    NOutputStream print(char[] buf, int off, int len);

    NOutputStream print(NMsg b);

    NOutputStream print(NString b);

    NOutputStream print(Boolean b);
    NOutputStream print(boolean b);

    NOutputStream print(char c);

    NOutputStream print(int i);

    NOutputStream print(long l);

    NOutputStream print(float f);

    NOutputStream print(double d);

    NOutputStream print(char[] s);

    NOutputStream print(Number d);
    NOutputStream print(Temporal d);

    NOutputStream print(Date d) ;
    NOutputStream print(String s);

    NOutputStream print(Object obj);

    NOutputStream println();

    NOutputStream println(Number d);
    NOutputStream println(Temporal d);

    NOutputStream println(Date d) ;
    NOutputStream println(boolean x);

    NOutputStream println(char x);

    NOutputStream println(NMsg b);

    NOutputStream println(NString b);

    NOutputStream println(int x);

    NOutputStream println(long x);

    NOutputStream println(float x);

    NOutputStream println(double x);

    NOutputStream println(char[] x);

    NOutputStream println(String x);

    NOutputStream println(Object x);

    NOutputStream print(Object text, NTextStyle style);

    NOutputStream print(Object text, NTextStyles styles);

    NOutputStream resetLine();

    NOutputStream print(CharSequence csq);

    NOutputStream print(CharSequence csq, int start, int end);

    NTerminalMode getTerminalMode();

    boolean isAutoFlash();

    /**
     * update mode and return a new instance
     *
     * @param other new mode
     * @return a new instance of NutsPrintStream (if the mode changes)
     */
    NOutputStream setTerminalMode(NTerminalMode other);

    NOutputStream run(NTerminalCommand command, NSession session);

    OutputStream asOutputStream();

    PrintStream asPrintStream();

    Writer asWriter();

    boolean isNtf();

    NSystemTerminalBase getTerminal();
}
