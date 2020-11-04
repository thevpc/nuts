/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

/**
 * Configurable interface define a extensible way to configure nuts commands
 * and objects using simple argument line options.
 * @author vpc
 * @since 0.5.5
 * @category Command Line
 */
public interface NutsConfigurable {

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * silently
     * @param args arguments to configure with
     * @return {@code this} instance
     */
    Object configure(boolean skipUnsupported, String... args);

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * silently
     * @param commandLine arguments to configure with
     * @return true when the at least one argument was processed
     */
    boolean configure(boolean skipUnsupported, NutsCommandLine commandLine);

    /**
     * ask {@code this} instance to configure with the very first argument of
     * {@code commandLine}. If the first argument is not supported, return
     * {@code false} and consume (skip/read) the argument. If the argument
     * required one or more parameters, these arguments are also consumed and
     * finally return {@code true}
     *
     * @param commandLine arguments to configure with
     * @return true when the at least one argument was processed
     */
    boolean configureFirst(NutsCommandLine commandLine);
}
