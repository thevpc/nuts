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
package net.vpc.app.nuts;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Format used to format command line by {@link NutsExecCommand}
 *
 * @author vpc
 * @see NutsExecCommand#format()
 * @since 0.5.4
 * @category Format
 */
public interface NutsExecCommandFormat extends NutsFormat {
    /**
     * true if input redirection is displayed
     * @return true if input redirection is displayed
     */
    boolean isRedirectInput();

    /**
     * if true input redirection is displayed
     * @param redirectInput new value
     * @return {@code this} instance
     */
    NutsExecCommandFormat setRedirectInput(boolean redirectInput);

    /**
     * true if output redirection is displayed
     * @return true if output redirection is displayed
     */
    boolean isRedirectOutput();

    /**
     * if true output redirection is displayed
     * @param redirectOutput new value
     * @return {@code this} instance
     */
    NutsExecCommandFormat setRedirectOutput(boolean redirectOutput);

    /**
     * true if error redirection is displayed
     * @return true if error redirection is displayed
     */
    boolean isRedirectError();

    /**
     * if true error redirection is displayed
     * @param redirectError new value
     * @return {@code this} instance
     */
    NutsExecCommandFormat setRedirectError(boolean redirectError);

    /**
     * return value to format
     * @return value to format
     */
    NutsExecCommand getValue();

    /**
     * set value to format
     * @param value value to format
     * @return {@code this} instance
     */
    NutsExecCommandFormat value(NutsExecCommand value);

    /**
     * set value to format
     * @param value value to format
     * @return {@code this} instance
     */
    NutsExecCommandFormat setValue(NutsExecCommand value);

    /**
     * return argument filter
     * @return argument filter
     */
    Predicate<ArgEntry> getArgumentFilter();

    /**
     * set arg filter
     * @param filter arg filter
     * @return {@code this} instance
     */
    NutsExecCommandFormat setArgumentFilter(Predicate<ArgEntry> filter);

    /**
     * return argument replacer
     * @return argument replacer
     */
    Function<ArgEntry, String> getArgumentReplacer();

    /**
     * set arg replacer
     * @param replacer arg replacer
     * @return {@code this} instance
     */
    NutsExecCommandFormat setArgumentReplacer(Function<ArgEntry, String> replacer);

    /**
     * return env filter
     * @return env filter
     */
    Predicate<EnvEntry> getEnvFilter();

    /**
     * set env filter
     * @param filter env filter
     * @return {@code this} instance
     */
    NutsExecCommandFormat setEnvFilter(Predicate<EnvEntry> filter);

    /**
     * return env replacer
     * @return env replacer
     */
    Function<EnvEntry, String> getEnvReplacer();

    /**
     * set env replacer
     * @param replacer env replacer
     * @return {@code this} instance
     */
    NutsExecCommandFormat setEnvReplacer(Function<EnvEntry, String> replacer);

    /**
     * env entry
     */
    interface EnvEntry {
        /**
         * env name
         * @return env name
         */
        String getName();

        /**
         * env value
         * @return env value
         */
        String getValue();
    }

    /**
     * argument entry
     */
    interface ArgEntry {
        /**
         * argument index
         * @return argument index
         */
        int getIndex();

        /**
         * argument value
         * @return argument value
         */
        String getValue();
    }
}
