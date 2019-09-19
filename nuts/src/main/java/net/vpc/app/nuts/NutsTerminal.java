/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.IOError;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * A Terminal handles in put stream, an output stream and an error stream to communicate
 * with user.
 * @since 0.5.4
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
     * Reads a single line of text from the the terminal's input stream.
     *
     * @throws java.io.UncheckedIOException
     *         If an I/O error occurs.
     *
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
     * @return  A string containing the line read from the terminal's input stream, not
     *          including any line-termination characters, or <tt>null</tt>
     *          if an end of stream has been reached.
     */
    char[] readPassword(String prompt, Object... params);

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
}
