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
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsTerminals;

import java.io.InputStream;

/**
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.4
 */
public interface NutsSystemTerminal extends NutsSystemTerminalBase {
    static void enableRichTerm(NutsSession session) {
        NutsTerminals.of(session).enableRichTerm(session);
    }

    /**
     * Reads a single line of text from the terminal's input stream.
     *
     * @param message message
     * @param session session
     * @return A string containing the line read from the terminal's input
     * stream, not including any line-termination characters, or {@code null}
     * if an end of stream has been reached.
     * @throws java.io.UncheckedIOException If an I/O error occurs.
     */
    String readLine(NutsMessage message, NutsSession session);

    /**
     * Reads password as a single line of text from the terminal's input stream.
     *
     * @param message message
     * @param session session
     * @return A string containing the line read from the terminal's input
     * stream, not including any line-termination characters, or {@code null}
     * if an end of stream has been reached.
     * @throws java.io.UncheckedIOException If an I/O error occurs.
     */
    char[] readPassword(NutsMessage message, NutsSession session);

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
    NutsPrintStream out();

    /**
     * return terminal's error stream
     *
     * @return terminal's error stream
     */
    NutsPrintStream err();

    /**
     * print progress with a message
     *
     * @param progress 0.0f-1.0f value
     * @param message  message
     * @param session  session
     * @return {@code this} instance
     */
    NutsSystemTerminal printProgress(float progress, NutsMessage message, NutsSession session);

    NutsSystemTerminalBase getBase();
}
