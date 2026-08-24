/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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


import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.text.NMsg;

import java.io.InputStream;

/**
 * Created by vpc on 2/20/17.
 *
 * @app.category Base
 * @since 0.5.4
 */
public interface NTerminal {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTerminal of() {
        return NSession.of().terminal();
    }

    /**
     * Creates a new instance of of system.
     *
     * @return of system result
     */
    static NTerminal ofSystem() {
        return NIORPI.of().createTerminal();
    }

    /**
     * Creates a new instance of of.
     *
     * @param parent parent
     * @return of result
     */
    static NTerminal of(NTerminal parent) {
        return NIORPI.of().createTerminal(parent);
    }

    /**
     * Creates a new instance of of.
     *
     * @param in in
     * @param out out
     * @param err err
     * @return of result
     */
    static NTerminal of(InputStream in, NPrintStream out, NPrintStream err) {
        return NIORPI.of().createTerminal(in, out, err);
    }

    /**
     * Creates a new instance of of mem.
     *
     * @return of mem result
     */
    static NTerminal ofMem() {
        return NIORPI.of().createInMemoryTerminal();
    }

    /**
     * Creates a new instance of of mem.
     *
     * @param mergeError merge error
     * @return of mem result
     */
    static NTerminal ofMem(boolean mergeError) {
        return NIORPI.of().createInMemoryTerminal(mergeError);
    }

    /**
     * Read line.
     *
     * @param out out
     * @param message message
     * @return read line result
     */
    String readLine(NPrintStream out, NMsg message);


    /**
     * Reads password as a single line of text from the terminal's input stream.
     *
     * @param prompt prompt message
     * @return A string containing the line read from the terminal's input
     * stream, not including any line-termination characters, or {@code null}
     * if an end of stream has been reached.
     * @throws java.io.UncheckedIOException If an I/O error occurs.
     */
    char[] readPassword(NMsg prompt);

    /**
     * Reads password as a single line of text from the terminal's input stream.
     *
     * @param prompt prompt message
     * @return A string containing the line read from the terminal's input
     * stream, not including any line-termination characters, or {@code null}
     * if an end of stream has been reached.
     * @throws java.io.UncheckedIOException If an I/O error occurs.
     */
    char[] readPassword(NPrintStream out, NMsg prompt);

    /**
     * In.
     *
     * @param in in
     */
    void in(InputStream in);

    /**
     * Out.
     *
     * @param out out
     */
    void out(NPrintStream out);

    /**
     * Err.
     *
     * @param out out
     */
    void err(NPrintStream out);

    //    NutsSystemTerminalBase geTerminalBase();
//
//    void seTerminalBase(NutsSystemTerminalBase terminalBase);
//
    /**
     * Copy.
     *
     * @return copy result
     */
    NTerminal copy();


    /**
     * Reads a single line of text from the terminal's input stream.
     *
     * @param prompt prompt message
     * @return A string containing the line read from the terminal's input
     * stream, not including any line-termination characters, or {@code null}
     * if an end of stream has been reached.
     * @throws java.io.UncheckedIOException If an I/O error occurs.
     */
    String readLine(NMsg prompt);


    /**
     * create a {@link NAsk} to write a question to the terminal's
     * output stream and read a typed value from the terminal's input stream.
     *
     * @param <T> type of the value to read
     * @return new instance of {@link NAsk}
     */
    <T> NAsk<T> ask();

    /**
     * return terminal's input stream
     *
     * @return terminal's input stream
     */
    InputStream in();

    /**
     * return terminal's output stream
     *
     * @return terminal's output stream
     */
    NPrintStream out();

    /**
     * return terminal's error stream
     *
     * @return terminal's error stream
     */
    NPrintStream err();


    /**
     * print progress with a message
     *
     * @param progress 0.0f-1.0f value
     * @param message  message
     * @return {@code this} instance
     */
    NTerminal printProgress(float progress, NMsg message);

    /**
     * print indefinite progress with a message
     *
     * @param message message
     * @return {@code this} instance
     */
    NTerminal printProgress(NMsg message);

}
