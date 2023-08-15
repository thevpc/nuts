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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

public interface NIO extends NComponent {
    static NIO of(NSession session) {
        return NExtensions.of(session).createComponent(NIO.class).get();
    }

    InputStream ofNullRawInputStream();

    boolean isStdin(InputStream in);

    InputStream stdin();

    NPrintStream ofNullPrintStream();

    OutputStream ofNullRawOutputStream();

    NMemoryPrintStream ofInMemoryPrintStream();

    NMemoryPrintStream ofInMemoryPrintStream(NTerminalMode mode);

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
    NPrintStream ofPrintStream(OutputStream out, NTerminalMode mode, NSystemTerminalBase terminal);

    NPrintStream ofPrintStream(OutputStream out);

    NPrintStream ofPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal);

    NPrintStream ofPrintStream(NPath out);

    NPrintStream ofPrintStream(Writer out);

    boolean isStdout(NPrintStream out);

    boolean isStderr(NPrintStream out);

    NPrintStream stdout();

    NPrintStream stderr();

    NInputSource ofMultiRead(NInputSource source);

    NInputSource ofInputSource(InputStream inputStream);

    NInputSource ofInputSource(InputStream inputStream, NContentMetadata metadata);

    NInputSource ofInputSource(byte[] inputStream);

    NInputSource ofInputSource(byte[] inputStream, NContentMetadata metadata);

    NOutputTarget ofOutputTarget(OutputStream outputStream);

    NOutputTarget ofOutputTarget(OutputStream outputStream, NContentMetadata metadata);

    /**
     * Checks for the current system terminal and does best effort
     * to enable a rich terminal. Rich terminals add somme features
     * including 'auto-complete'. This Method may replace the system
     * terminal and may even load a nuts extension to enable such features.
     *
     * @return {@code this} instance
     */
    NIO enableRichTerm();

    /**
     * return new terminal bound to the given session
     *
     * @return new terminal
     */
    NSessionTerminal createTerminal();

    /**
     * return new terminal
     *
     * @param in  in
     * @param out out
     * @param err err
     * @return new terminal
     */
    NSessionTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err);

    /**
     * return new terminal bound to the given parent terminal and session.
     *
     * @param terminal parent terminal (or null)
     * @return new terminal bound to the given parent terminal and session.
     */
    NSessionTerminal createTerminal(NSessionTerminal terminal);

    /**
     * return a new terminal with empty input and byte-array output/error.
     * Using such terminals help capturing all output/error stream upon execution.
     * This method is equivalent to createMemTerminal(false,session)
     *
     * @return a new terminal with empty input and byte-array output/error.
     */
    NSessionTerminal createInMemoryTerminal();

    /**
     * return a new terminal with empty input and byte-array output/error.
     * Using such terminals help capturing all output/error stream upon execution.
     *
     * @param mergeErr when true out and err are merged into a single stream
     * @return a new terminal with empty input and byte-array output/error.
     */
    NSessionTerminal createInMemoryTerminal(boolean mergeErr);


    /**
     * return workspace system terminal.
     *
     * @return workspace system terminal
     */
    NSystemTerminal getSystemTerminal();

    /**
     * update workspace wide system terminal
     *
     * @param terminal system terminal
     * @return {@code this} instance
     */
    NIO setSystemTerminal(NSystemTerminalBase terminal);

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NSessionTerminal getDefaultTerminal();

    /**
     * update workspace wide terminal
     *
     * @param terminal terminal
     * @return {@code this} instance
     */
    NIO setDefaultTerminal(NSessionTerminal terminal);


    NInputStreamBuilder ofInputStreamBuilder(InputStream base);
    NOutputStreamBuilder ofOutputStreamBuilder(OutputStream base);

}
