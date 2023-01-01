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

public interface NOutStream extends NOutputTarget {

    static NOutStream ofNull(NSession session) {
        return NIO.of(session).createNullPrintStream();
    }

    /**
     * return new in-memory NutsPrintStream implementation.
     * this is equivalent to {@code NutsMemoryPrintStream.of(session)}
     *
     * @param session session
     * @return new in-memory NutsPrintStream implementation
     */
    static NOutMemoryStream ofInMemory(NSession session) {
        return NIO.of(session).createInMemoryPrintStream();
    }

    static NOutStream of(OutputStream out, NSession session) {
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
    static NOutStream of(OutputStream out, NTerminalMode mode, NSystemTerminalBase terminal, NSession session) {
        return NIO.of(session).createPrintStream(out, mode, terminal);
    }

    static NOutStream of(Writer out, NSession session) {
        return NIO.of(session).createPrintStream(out);
    }

    NSession getSession();

    /**
     * update session and return a new instance
     *
     * @param session new session
     * @return a new instance of NutsPrintStream
     */
    NOutStream setSession(NSession session);

    NOutStream flush();

    NOutStream close();

    NOutStream write(byte[] b);

    NOutStream write(int b);

    NOutStream write(byte[] buf, int off, int len);

    NOutStream write(char[] buf);

    NOutStream write(char[] buf, int off, int len);

    NOutStream print(NMsg b);

    NOutStream print(NString b);

    NOutStream print(boolean b);

    NOutStream print(char c);

    NOutStream print(int i);

    NOutStream print(long l);

    NOutStream print(float f);

    NOutStream print(double d);

    NOutStream print(char[] s);

    NOutStream print(String s);

    NOutStream print(Object obj);

    NOutStream printf(Object obj);

    NOutStream printlnf(Object obj);

    NOutStream println();

    NOutStream println(boolean x);

    NOutStream println(char x);

    NOutStream println(NMsg b);

    NOutStream println(NString b);

    NOutStream println(int x);

    NOutStream println(long x);

    NOutStream println(float x);

    NOutStream println(double x);

    NOutStream println(char[] x);

    NOutStream println(String x);

    NOutStream println(Object x);

    NOutStream append(Object text, NTextStyle style);

    NOutStream append(Object text, NTextStyles styles);

    NOutStream resetLine();

    NOutStream printf(String format, Object... args);

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
    NOutStream printj(String format, Object... args);

    NOutStream printlnj(String format, Object... args);

    NOutStream printv(String format, Map<String,?> args);

    NOutStream printlnv(String format, Map<String,?> args);

    NOutStream printlnf(String format, Object... args);

    NOutStream printf(Locale l, String format, Object... args);

    NOutStream format(String format, Object... args);

    NOutStream format(Locale l, String format, Object... args);

    NOutStream append(CharSequence csq);

    NOutStream append(CharSequence csq, int start, int end);

    NOutStream append(char c);

    NTerminalMode getTerminalMode();

    boolean isAutoFlash();

    /**
     * update mode and return a new instance
     *
     * @param other new mode
     * @return a new instance of NutsPrintStream (if the mode changes)
     */
    NOutStream setTerminalMode(NTerminalMode other);

    NOutStream run(NTerminalCommand command, NSession session);

    OutputStream asOutputStream();

    PrintStream asPrintStream();

    Writer asWriter();

    boolean isNtf();

    NSystemTerminalBase getTerminal();
}
