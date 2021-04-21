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
 *
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

import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A Terminal handles in put stream, an output stream and an error stream to communicate
 * with user.
 * @since 0.5.4
 * @category Input Output
 */
public interface NutsTerminal extends NutsTerminalBase {

    /**
     * change terminal mode for both out and err
     *
     * @param mode mode
     * @return {@code this} instance
     */
    NutsTerminal setMode(NutsTerminalMode mode);

    /**
     * change terminal mode for both out and err
     *
     * @param mode mode
     * @return {@code this} instance
     */
    NutsTerminal mode(NutsTerminalMode mode);

    /**
     * change terminal mode for out
     *
     * @param mode mode
     * @return {@code this} instance
     */
    @Override
    NutsTerminal setOutMode(NutsTerminalMode mode);

    /**
     * change terminal mode for out
     *
     * @param mode mode
     * @return {@code this} instance
     */
    NutsTerminal outMode(NutsTerminalMode mode);

    /**
     * change terminal mode for err
     *
     * @param mode mode
     * @return {@code this} instance
     */
    @Override
    NutsTerminal setErrMode(NutsTerminalMode mode);

    /**
     * change terminal mode for out
     *
     * @param mode mode
     * @return {@code this} instance
     */
    NutsTerminal errMode(NutsTerminalMode mode);

    /**
     * return err mode
     *
     * @return err mode
     */
    @Override
    NutsTerminalMode getErrMode();

    /**
     * return out mode
     *
     * @return out mode
     */
    @Override
    NutsTerminalMode getOutMode();

    /**
     * Reads a single line of text from the terminal's input stream.
     *
     * @throws java.io.UncheckedIOException
     *         If an I/O error occurs.
     *
     * @param promptFormat prompt message format (cstyle)
     * @param params prompt message parameters
     * @return  A string containing the line read from the terminal's input stream, not
     *          including any line-termination characters, or <tt>null</tt>
     *          if an end of stream has been reached.
     */
    String readLine(String promptFormat, Object... params);

    /**
     * Reads password as a single line of text from the terminal's input stream.
     *
     * @throws java.io.UncheckedIOException
     *         If an I/O error occurs.
     *
     * @param promptFormat prompt message format (cstyle)
     * @param params prompt message parameters
     * @return  A string containing the line read from the terminal's input stream, not
     *          including any line-termination characters, or <tt>null</tt>
     *          if an end of stream has been reached.
     */
    char[] readPassword(String promptFormat, Object... params);

    /**
     * create a {@link NutsQuestion} to write a question to the terminal's output stream
     * and read a typed value from the terminal's input stream.
     * @param <T> type of teh value to read
     * @return new instance of {@link NutsQuestion}
     */
    <T> NutsQuestion<T> ask();

    /**
     * return terminal's input stream
     * @return terminal's input stream
     */
    InputStream in();

    /**
     * return terminal's output stream
     * @return terminal's output stream
     */
    PrintStream out();

    /**
     * return terminal's error stream
     * @return terminal's error stream
     */
    PrintStream err();

    NutsTerminal sendOutCommand(NutsTerminalCommand command);
    NutsTerminal sendErrCommand(NutsTerminalCommand command);

}
