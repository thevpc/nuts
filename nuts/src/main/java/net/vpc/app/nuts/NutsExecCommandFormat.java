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

/**
 * Format used to format command line by {@link NutsExecCommand}
 *
 * @see NutsExecCommand#setCommandLineFormat(NutsExecCommandFormat)
 * @see NutsExecCommand#getCommandString()
 * @author vpc
 * @since 0.5.4
 */
public interface NutsExecCommandFormat {

    /**
     * true if argument is accepted (to be displayed)
     * @param argIndex arg index
     * @param arg arg value
     * @return true if argument is accepted (to be displayed)
     */
    default boolean acceptArgument(int argIndex, String arg) {
        return true;
    }

    /**
     * true if env is accepted (to be displayed)
     * @param envName env name
     * @param envValue env value
     * @return true if env is accepted (to be displayed)
     */
    default boolean acceptEnvName(String envName, String envValue) {
        return true;
    }

    /**
     * true if redirect input is accepted (to be displayed)
     * @return true if redirect input is accepted (to be displayed)
     */
    default boolean acceptRedirectInput() {
        return true;
    }

    /**
     * true if redirect output is accepted (to be displayed)
     * @return true if redirect output is accepted (to be displayed)
     */
    default boolean acceptRedirectOutput() {
        return true;
    }

    /**
     * true if redirect error is accepted (to be displayed)
     * @return true if redirect error is accepted (to be displayed)
     */
    default boolean acceptRedirectError() {
        return true;
    }

    /**
     * replace the given argument or return null
     * @param argIndex arg index
     * @param arg arg value
     * @return new value or null
     */
    default String replaceArgument(int argIndex, String arg) {
        return null;
    }

    /**
     * replace the given env name or return null
     * @param envName env name
     * @param envValue env value
     * @return new value or null
     */
    default String replaceEnvName(String envName, String envValue) {
        return null;
    }

    /**
     * replace the given env value or return null
     * @param envName env name
     * @param envValue env value
     * @return new value or null
     */
    default String replaceEnvValue(String envName, String envValue) {
        return null;
    }
}
