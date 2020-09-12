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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Classes implementing this interface are responsible of printing objects in multiple format
 * using {@link NutsIterableFormat}.
 * TODO : should merge with NutsIterableFormat
 * @author vpc
 * @since 0.5.5
 * @category Format
 */
public interface NutsIterableOutput extends NutsConfigurable {

    /**
     * configure out stream
     * @param out out stream
     * @return {@code this} instance
     */
    NutsIterableOutput out(PrintStream out);

    /**
     * configure out stream
     * @param out out stream
     * @return {@code this} instance
     */
    NutsIterableOutput setOut(PrintStream out);

    /**
     * configure out stream
     * @param out out stream
     * @return {@code this} instance
     */
    NutsIterableOutput out(PrintWriter out);

    /**
     * configure out c
     * @param out out writer
     * @return {@code this} instance
     */
    NutsIterableOutput setOut(PrintWriter out);

    /**
     * configure session
     * @param session session
     * @return {@code this} instance
     */
    NutsIterableOutput setSession(NutsSession session);

    /**
     * called at the iteration start
     */
    void start();

    /**
     * called at the each visited item
     * @param object visited item
     */
    void next(Object object);

    /**
     * called at the iteration completing
     */
    void complete();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsIterableOutput configure(boolean skipUnsupported, String... args);

}
