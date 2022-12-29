/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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


import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NTerminals;
import net.thevpc.nuts.util.NQuestion;

import java.io.InputStream;

/**
 * Created by vpc on 2/20/17.
 *
 * @app.category Base
 * @since 0.5.4
 */
public interface NSessionTerminal {
    static NSessionTerminal of(NSession session) {
        return NTerminals.of(session).createTerminal(session);
    }

    static NSessionTerminal of(NSessionTerminal parent, NSession session) {
        return NTerminals.of(session).createTerminal(parent, session);
    }

    static NSessionTerminal of(InputStream in, NStream out, NStream err, NSession session) {
        return NTerminals.of(session).createTerminal(in, out, err, session);
    }

    static NSessionTerminal ofMem(NSession session) {
        return NTerminals.of(session).createMemTerminal(session);
    }

    static NSessionTerminal ofMem(boolean mergeError, NSession session) {
        return NTerminals.of(session).createMemTerminal(mergeError, session);
    }

    String readLine(NStream out, String prompt, Object... params);

    char[] readPassword(NStream out, String prompt, Object... params);

    String readLine(NStream out, NMsg message);

    char[] readPassword(NStream out, NMsg message);

    String readLine(NStream out, NMsg message, NSession session);

    char[] readPassword(NStream out, NMsg message, NSession session);

    InputStream getIn();

    void setIn(InputStream in);

    NStream getOut();

    void setOut(NStream out);

    NStream getErr();

    void setErr(NStream out);

    //    NutsSystemTerminalBase geTerminalBase();
//
//    void seTerminalBase(NutsSystemTerminalBase terminalBase);
//
    NSessionTerminal copy();

//    void setParent(NutsSystemTerminalBase parent);

    /**
     * Reads a single line of text from the terminal's input stream.
     *
     * @param promptFormat prompt message format (cstyle)
     * @param params       prompt message parameters
     * @return A string containing the line read from the terminal's input
     * stream, not including any line-termination characters, or {@code null}
     * if an end of stream has been reached.
     * @throws java.io.UncheckedIOException If an I/O error occurs.
     */
    String readLine(String promptFormat, Object... params);

    /**
     * Reads password as a single line of text from the terminal's input stream.
     *
     * @param promptFormat prompt message format (cstyle)
     * @param params       prompt message parameters
     * @return A string containing the line read from the terminal's input
     * stream, not including any line-termination characters, or {@code null}
     * if an end of stream has been reached.
     * @throws java.io.UncheckedIOException If an I/O error occurs.
     */
    char[] readPassword(String promptFormat, Object... params);

    /**
     * create a {@link NQuestion} to write a question to the terminal's
     * output stream and read a typed value from the terminal's input stream.
     *
     * @param <T> type of the value to read
     * @return new instance of {@link NQuestion}
     */
    <T> NQuestion<T> ask();

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
    NStream out();

    /**
     * return terminal's error stream
     *
     * @return terminal's error stream
     */
    NStream err();

    /**
     * print progress with a message
     *
     * @param progress 0.0f-1.0f value
     * @param prompt   message
     * @param params   message prams
     * @return {@code this} instance
     */
    NSessionTerminal printProgress(float progress, String prompt, Object... params);

    /**
     * print indefinite progress with a message
     *
     * @param prompt message
     * @param params message prams
     * @return {@code this} instance
     */
    NSessionTerminal printProgress(String prompt, Object... params);

    /**
     * print progress with a message
     *
     * @param progress 0.0f-1.0f value
     * @param message  message
     * @return {@code this} instance
     */
    NSessionTerminal printProgress(float progress, NMsg message);

    /**
     * print indefinite progress with a message
     *
     * @param message message
     * @return {@code this} instance
     */
    NSessionTerminal printProgress(NMsg message);
}
