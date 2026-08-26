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

import net.thevpc.nuts.spi.base.NSystemTerminalBase;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.text.NMsg;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * NPrintStream interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NPrintStream extends NOutputTarget, AutoCloseable {

    NPrintStream NULL = NullNPrintStream.INSTANCE;

    /**
     * return new in-memory NutsPrintStream implementation.
     * this is equivalent to {@code NMemoryPrintStream.of()}
     *
     * @return new in-memory NutsPrintStream implementation
     */
    static NMemoryPrintStream ofMem() {
        return NIORPI.of().createInMemoryPrintStream();
    }

    /**
     * Creates a new instance of of mem.
     *
     * @param mode mode
     * @return of mem result
     */
    static NMemoryPrintStream ofMem(NTerminalMode mode) {
        return NIORPI.of().createInMemoryPrintStream(mode);
    }

    /**
     * Creates a new instance of of.
     *
     * @param out out
     * @return of result
     */
    static NPrintStream of(OutputStream out) {
        return NIORPI.of().createPrintStream(out);
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
        return NIORPI.of().createPrintStream(out, mode, terminal);
    }

    /**
     * create print stream that supports the given {@code mode}. If the given
     * @param out base output stream
     * @param mode expected mode of the new NPrintStream
     * @param baseMode of the base output stream
     * @return new NPrintStream
     */
    static NPrintStream of(OutputStream out, NTerminalMode mode, NTerminalMode baseMode) {
        return NIORPI.of().createPrintStream(out, mode, baseMode);
    }

    /**
     * Creates a new instance of of.
     *
     * @param out out
     * @param mode mode
     * @return of result
     */
    static NPrintStream of(OutputStream out, NTerminalMode mode) {
        return NIORPI.of().createPrintStream(out, mode);
    }

    /**
     * Creates a new instance of of.
     *
     * @param out out
     * @return of result
     */
    static NPrintStream of(Writer out) {
        return NIORPI.of().createPrintStream(out);
    }

    /**
     * Creates a new instance of of.
     *
     * @param path path
     * @return of result
     */
    static NPrintStream of(NPath path) {
        return NIORPI.of().createPrintStream(path);
    }

    /**
     * Flush.
     *
     * @return flush result
     */
    NPrintStream flush();

    /**
     * Close.
     */
    void close();

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    NPrintStream print(byte[] b);

    /**
     * Write.
     *
     * @param b b
     * @return write result
     */
    NPrintStream write(int b);

    /**
     * Write raw.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write raw result
     */
    NPrintStream writeRaw(byte[] buf, int off, int len);

    /**
     * Write.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write result
     */
    NPrintStream write(byte[] buf, int off, int len);

    /**
     * Write.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return write result
     */
    NPrintStream write(char[] buf, int off, int len);

    /**
     * Print.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return print result
     */
    NPrintStream print(byte[] buf, int off, int len);

    /**
     * Print.
     *
     * @param buf buf
     * @param off off
     * @param len len
     * @return print result
     */
    NPrintStream print(char[] buf, int off, int len);

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    NPrintStream print(NMsg b);

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    NPrintStream print(NText b);

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    NPrintStream print(Boolean b);

    /**
     * Print.
     *
     * @param b b
     * @return print result
     */
    NPrintStream print(boolean b);

    /**
     * Print.
     *
     * @param c c
     * @return print result
     */
    NPrintStream print(char c);

    /**
     * Print.
     *
     * @param i i
     * @return print result
     */
    NPrintStream print(int i);

    /**
     * Print.
     *
     * @param l l
     * @return print result
     */
    NPrintStream print(long l);

    /**
     * Print.
     *
     * @param f f
     * @return print result
     */
    NPrintStream print(float f);

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    NPrintStream print(double d);

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    NPrintStream print(char[] s);

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    NPrintStream print(Number d);

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    NPrintStream print(Temporal d);

    /**
     * Print.
     *
     * @param d d
     * @return print result
     */
    NPrintStream print(Date d);

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    NPrintStream print(String s);

    /**
     * Print.
     *
     * @param obj obj
     * @return print result
     */
    NPrintStream print(Object obj);

    /**
     * Println.
     *
     * @return println result
     */
    NPrintStream println();

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    NPrintStream println(Number d);

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    NPrintStream println(Temporal d);

    /**
     * Println.
     *
     * @param d d
     * @return println result
     */
    NPrintStream println(Date d);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(boolean x);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(char x);

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    NPrintStream println(NMsg b);

    /**
     * Println.
     *
     * @param b b
     * @return println result
     */
    NPrintStream println(NText b);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(int x);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(long x);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(float x);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(double x);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(char[] x);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(String x);

    /**
     * Println.
     *
     * @param x x
     * @return println result
     */
    NPrintStream println(Object x);

    /**
     * Print.
     *
     * @param text text
     * @param style style
     * @return print result
     */
    NPrintStream print(Object text, NTextStyle style);

    /**
     * Print.
     *
     * @param text text
     * @param styles styles
     * @return print result
     */
    NPrintStream print(Object text, NTextStyles styles);

    /**
     * Reset line.
     *
     * @return reset line result
     */
    NPrintStream resetLine();

    /**
     * Print.
     *
     * @param csq csq
     * @return print result
     */
    NPrintStream print(CharSequence csq);

    /**
     * Print.
     *
     * @param csq csq
     * @param start start
     * @param end end
     * @return print result
     */
    NPrintStream print(CharSequence csq, int start, int end);

    /**
     * Terminal mode.
     *
     * @return terminal mode result
     */
    NTerminalMode terminalMode();

    /**
     * Checks if is auto flash.
     *
     * @return is auto flash result
     */
    boolean isAutoFlash();

    /**
     * update mode and return a new instance
     *
     * @param other new mode
     * @return a new instance of NutsPrintStream (if the mode changes)
     */
    NPrintStream terminalMode(NTerminalMode other);

    /**
     * Run.
     *
     * @param command command
     * @return run result
     */
    NPrintStream run(NTerminalCmd command);

    /**
     * As output stream.
     *
     * @return as output stream result
     */
    OutputStream asOutputStream();

    /**
     * As print stream.
     *
     * @return as print stream result
     */
    PrintStream asPrintStream();

    /**
     * As writer.
     *
     * @return as writer result
     */
    Writer asWriter();

    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    boolean isNtf();

    /**
     * Terminal.
     *
     * @return terminal result
     */
    NSystemTerminalBase terminal();

    /**
     * Print progress line.
     *
     * @param b b
     * @return print progress line result
     */
    NPrintStream printProgressLine(NText b);

    /**
     * As string writer.
     *
     * @return as string writer result
     */
    NStringWriter asStringWriter();
}
