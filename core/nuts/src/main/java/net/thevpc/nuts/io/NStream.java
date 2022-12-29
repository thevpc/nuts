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
import java.util.Locale;
import java.util.Map;

public interface NStream extends NOutputTarget {

    static NStream ofNull(NSession session) {
        return NIO.of(session).createNullPrintStream();
    }

    /**
     * return new in-memory NutsPrintStream implementation.
     * this is equivalent to {@code NutsMemoryPrintStream.of(session)}
     *
     * @param session session
     * @return new in-memory NutsPrintStream implementation
     */
    static NMemoryStream ofInMemory(NSession session) {
        return NIO.of(session).createInMemoryPrintStream();
    }

    static NStream of(OutputStream out, NSession session) {
        return NIO.of(session).createPrintStream(out);
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
    static NStream of(OutputStream out, NTerminalMode mode, NSystemTerminalBase terminal, NSession session) {
        return NIO.of(session).createPrintStream(out, mode, terminal);
    }

    static NStream of(Writer out, NSession session) {
        return NIO.of(session).createPrintStream(out);
    }

    NSession getSession();

    /**
     * update session and return a new instance
     *
     * @param session new session
     * @return a new instance of NutsPrintStream
     */
    NStream setSession(NSession session);

    NStream flush();

    NStream close();

    NStream write(byte[] b);

    NStream write(int b);

    NStream write(byte[] buf, int off, int len);

    NStream write(char[] buf);

    NStream write(char[] buf, int off, int len);

    NStream print(NMsg b);

    NStream print(NString b);

    NStream print(boolean b);

    NStream print(char c);

    NStream print(int i);

    NStream print(long l);

    NStream print(float f);

    NStream print(double d);

    NStream print(char[] s);

    NStream print(String s);

    NStream print(Object obj);

    NStream printf(Object obj);

    NStream printlnf(Object obj);

    NStream println();

    NStream println(boolean x);

    NStream println(char x);

    NStream println(NMsg b);

    NStream println(NString b);

    NStream println(int x);

    NStream println(long x);

    NStream println(float x);

    NStream println(double x);

    NStream println(char[] x);

    NStream println(String x);

    NStream println(Object x);

    NStream append(Object text, NTextStyle style);

    NStream append(Object text, NTextStyles styles);

    NStream resetLine();

    NStream printf(String format, Object... args);

    /**
     * print java formatted string (with {})
     * {@code
     * printj("{1,choice,0#|1# 1 file|1< {1} files}",nbr);
     * }
     *
     * @param format java style format (with {})
     * @param args   format args
     * @return {@code this} instance
     */
    NStream printj(String format, Object... args);

    NStream printlnj(String format, Object... args);

    NStream printv(String format, Map<String,?> args);

    NStream printlnv(String format, Map<String,?> args);

    NStream printlnf(String format, Object... args);

    NStream printf(Locale l, String format, Object... args);

    NStream format(String format, Object... args);

    NStream format(Locale l, String format, Object... args);

    NStream append(CharSequence csq);

    NStream append(CharSequence csq, int start, int end);

    NStream append(char c);

    NTerminalMode getTerminalMode();

    boolean isAutoFlash();

    /**
     * update mode and return a new instance
     *
     * @param other new mode
     * @return a new instance of NutsPrintStream (if the mode changes)
     */
    NStream setTerminalMode(NTerminalMode other);

    NStream run(NTerminalCommand command, NSession session);

    OutputStream asOutputStream();

    PrintStream asPrintStream();

    Writer asWriter();

    boolean isNtf();

    NSystemTerminalBase getTerminal();
}
