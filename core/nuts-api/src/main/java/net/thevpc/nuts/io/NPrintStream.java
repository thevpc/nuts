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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.reserved.rpi.NIORPI;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;

public interface NPrintStream extends NOutputTarget, AutoCloseable {

    NPrintStream NULL = NullNPrintStream.NULL;

    /**
     * return new in-memory NutsPrintStream implementation.
     * this is equivalent to {@code NMemoryPrintStream.of()}
     *
     * @return new in-memory NutsPrintStream implementation
     */
    static NMemoryPrintStream ofMem() {
        return NIORPI.of().ofInMemoryPrintStream();
    }

    static NMemoryPrintStream ofMem(NTerminalMode mode) {
        return NIORPI.of().ofInMemoryPrintStream(mode);
    }

    static NPrintStream of(OutputStream out) {
        return NIORPI.of().ofPrintStream(out);
    }

    /**
     * create print stream that supports the given {@code mode}. If the given
     * {@code out} is a PrintStream that supports {@code mode}, it should be
     * returned without modification.
     *
     * @param out      stream to wrap
     * @param mode     mode to support
     * @param terminal terminal
     * @return {@code mode} supporting PrintStream
     */
    static NPrintStream of(OutputStream out, NTerminalMode mode, NSystemTerminalBase terminal) {
        return NIORPI.of().ofPrintStream(out, mode, terminal);
    }

    static NPrintStream of(OutputStream out, NTerminalMode mode) {
        return NIORPI.of().ofPrintStream(out, mode);
    }

    static NPrintStream of(Writer out) {
        return NIORPI.of().ofPrintStream(out);
    }

    static NPrintStream of(NPath path) {
        return NIORPI.of().ofPrintStream(path);
    }

    NPrintStream flush();

    void close();

    NPrintStream print(byte[] b);

    NPrintStream write(int b);

    NPrintStream writeRaw(byte[] buf, int off, int len);

    NPrintStream write(byte[] buf, int off, int len);

    NPrintStream write(char[] buf, int off, int len);

    NPrintStream print(byte[] buf, int off, int len);

    NPrintStream print(char[] buf, int off, int len);

    NPrintStream print(NMsg b);

    NPrintStream print(NText b);

    NPrintStream print(Boolean b);

    NPrintStream print(boolean b);

    NPrintStream print(char c);

    NPrintStream print(int i);

    NPrintStream print(long l);

    NPrintStream print(float f);

    NPrintStream print(double d);

    NPrintStream print(char[] s);

    NPrintStream print(Number d);

    NPrintStream print(Temporal d);

    NPrintStream print(Date d);

    NPrintStream print(String s);

    NPrintStream print(Object obj);

    NPrintStream println();

    NPrintStream println(Number d);

    NPrintStream println(Temporal d);

    NPrintStream println(Date d);

    NPrintStream println(boolean x);

    NPrintStream println(char x);

    NPrintStream println(NMsg b);

    NPrintStream println(NText b);

    NPrintStream println(int x);

    NPrintStream println(long x);

    NPrintStream println(float x);

    NPrintStream println(double x);

    NPrintStream println(char[] x);

    NPrintStream println(String x);

    NPrintStream println(Object x);

    NPrintStream print(Object text, NTextStyle style);

    NPrintStream print(Object text, NTextStyles styles);

    NPrintStream resetLine();

    NPrintStream print(CharSequence csq);

    NPrintStream print(CharSequence csq, int start, int end);

    NTerminalMode getTerminalMode();

    boolean isAutoFlash();

    /**
     * update mode and return a new instance
     *
     * @param other new mode
     * @return a new instance of NutsPrintStream (if the mode changes)
     */
    NPrintStream setTerminalMode(NTerminalMode other);

    NPrintStream run(NTerminalCmd command);

    OutputStream asOutputStream();

    PrintStream asPrintStream();

    Writer asWriter();

    boolean isNtf();

    NSystemTerminalBase getTerminal();
}
