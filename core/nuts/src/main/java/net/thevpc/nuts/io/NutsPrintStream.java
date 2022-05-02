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
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.text.NutsTerminalCommand;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Locale;

public interface NutsPrintStream extends NutsStreamMetadataAware,NutsOutputTarget {

    static NutsPrintStream ofNull(NutsSession session) {
        return NutsPrintStreams.of(session).createNull();
    }

    /**
     * return new in-memory NutsPrintStream implementation.
     * this is equivalent to {@code NutsMemoryPrintStream.of(session)}
     *
     * @param session session
     * @return new in-memory NutsPrintStream implementation
     */
    static NutsMemoryPrintStream ofInMemory(NutsSession session) {
        return NutsPrintStreams.of(session).createInMemory();
    }

    static NutsPrintStream of(OutputStream out, NutsSession session) {
        return NutsPrintStreams.of(session).create(out);
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
    static NutsPrintStream of(OutputStream out, NutsTerminalMode mode, NutsSystemTerminalBase terminal, NutsSession session) {
        return NutsPrintStreams.of(session).create(out, mode, terminal);
    }

    static NutsPrintStream of(Writer out, NutsSession session) {
        return NutsPrintStreams.of(session).create(out);
    }

    NutsString getFormattedName();

    NutsPrintStream setFormattedName(NutsString name);

    NutsSession getSession();

    /**
     * update session and return a new instance
     *
     * @param session new session
     * @return a new instance of NutsPrintStream
     */
    NutsPrintStream setSession(NutsSession session);

    NutsPrintStream flush();

    NutsPrintStream close();

    NutsPrintStream write(byte[] b);

    NutsPrintStream write(int b);

    NutsPrintStream write(byte[] buf, int off, int len);

    NutsPrintStream write(char[] buf);

    NutsPrintStream write(char[] buf, int off, int len);

    NutsPrintStream print(NutsString b);

    NutsPrintStream print(boolean b);

    NutsPrintStream print(char c);

    NutsPrintStream print(int i);

    NutsPrintStream print(long l);

    NutsPrintStream print(float f);

    NutsPrintStream print(double d);

    NutsPrintStream print(char[] s);

    NutsPrintStream print(String s);

    NutsPrintStream print(Object obj);

    NutsPrintStream printf(Object obj);

    NutsPrintStream printlnf(Object obj);

    NutsPrintStream println();

    NutsPrintStream println(boolean x);

    NutsPrintStream println(char x);

    NutsPrintStream println(NutsString b);

    NutsPrintStream println(int x);

    NutsPrintStream println(long x);

    NutsPrintStream println(float x);

    NutsPrintStream println(double x);

    NutsPrintStream println(char[] x);

    NutsPrintStream println(String x);

    NutsPrintStream println(Object x);

    NutsPrintStream resetLine();

    NutsPrintStream printf(String format, Object... args);

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
    NutsPrintStream printj(String format, Object... args);

    NutsPrintStream printlnf(String format, Object... args);

    NutsPrintStream printf(Locale l, String format, Object... args);

    NutsPrintStream format(String format, Object... args);

    NutsPrintStream format(Locale l, String format, Object... args);

    NutsPrintStream append(CharSequence csq);

    NutsPrintStream append(CharSequence csq, int start, int end);

    NutsPrintStream append(char c);

    NutsTerminalMode mode();

    boolean isAutoFlash();

    /**
     * update mode and return a new instance
     *
     * @param other new mode
     * @return a new instance of NutsPrintStream (if the mode changes)
     */
    NutsPrintStream setMode(NutsTerminalMode other);

    NutsPrintStream run(NutsTerminalCommand command, NutsSession session);

    OutputStream asOutputStream();

    PrintStream asPrintStream();

    Writer asWriter();

    boolean isNtf();

    NutsSystemTerminalBase getTerminal();
}
