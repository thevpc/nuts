/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Base Format Interface used to print "things".
 * @author vpc
 * @since 0.5.5
 */
public interface NutsFormat extends NutsConfigurable {

    /**
     * equivalent to {@link #format() }
     *
     * @return formatted current value
     */
    @Override
    String toString();

    /**
     * format current value and return the string result
     *
     * @return formatted current value
     */
    String format();

    /**
     * format current value and write result to {@code getSession().out()}.
     *
     */
    void print();

    /**
     * format current value and write result to {@code getSession().out()} and
     * finally appends a new line.
     */
    void println();

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient print stream
     */
    void print(PrintStream out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient writer
     */
    void print(Writer out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient writer
     */
    void print(OutputStream out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient path
     */
    void print(Path out);

    /**
     * format current value and write result to {@code out}
     *
     * @param out recipient file
     */
    void print(File out);

    /**
     * format current value and write result to {@code terminal}
     *
     * @param terminal recipient terminal
     */
    void print(NutsTerminal terminal);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient
     */
    void println(Writer out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient print stream
     */
    void println(PrintStream out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient path
     */
    void println(Path out);

    /**
     * format current value and write result to {@code terminal} and finally appends
     * a new line.
     *
     * @param terminal recipient terminal
     */
    void println(NutsTerminal terminal);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param file recipient file
     */
    void println(File file);

    /**
     * session associated to this format instance
     *
     * @return session associated to this format instance
     */
    NutsSession getSession();

    /**
     * equivalent to {@link #setSession(net.vpc.app.nuts.NutsSession) }
     *
     * @param session session
     * @return {@code this instance}
     */
    NutsFormat session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    NutsFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsFormat configure(boolean skipUnsupported, String... args);
}
