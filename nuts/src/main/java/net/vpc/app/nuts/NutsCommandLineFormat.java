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

import java.util.Collection;
import java.util.List;

/**
 * Simple Command line Format
 *
 * @author vpc
 * @since 0.5.7
 */
public interface NutsCommandLineFormat extends NutsFormat{

    /**
     * set command line
     * @param value value
     * @return {@code this} instance
     */
    NutsCommandLineFormat setValue(NutsCommandLine value);

    /**
     * set command line from string array
     * @param args args
     * @return {@code this} instance
     */
    NutsCommandLineFormat setValue(String[] args);

    /**
     * set command line from parsed string
     * @param args args
     * @return {@code this} instance
     */
    NutsCommandLineFormat setValue(String args);

    /**
     * return current command line
     *
     * @return current command line
     */
    NutsCommandLine getValue();

    /**
     * return new Command line instance
     * @param line command line to parse
     * @return new Command line instance
     */
    NutsCommandLine parse(String line);

    /**
     * return new Command line instance
     *
     * @param args command line args
     * @return new Command line instance
     */
    NutsCommandLine create(String ... args);

    /**
     * return new Command line instance
     *
     * @param args command line args
     * @return new Command line instance
     */
    NutsCommandLine create(List<String> args);


    /**
     * create new argument
     * @param argument new argument
     * @return new argument
     */
    NutsArgument createArgument(String argument);

    /**
     * create argument name
     * @param type create argument type
     * @return argument name
     */
    default NutsArgumentName createName(String type) {
        return createName(type, type);
    }

    /**
     * create argument name
     * @param type argument type
     * @param label argument label
     * @return argument name
     */
    NutsArgumentName createName(String type, String label);

    /**
     * create argument candidate
     * @param value candidate value
     * @return argument candidate
     */
    default NutsArgumentCandidate createCandidate(String value) {
        return createCandidate(value, value);
    }

    /**
     * create argument candidate
     * @param value candidate value
     * @param label candidate label
     * @return argument candidate
     */
    NutsArgumentCandidate createCandidate(String value, String label);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsCommandLineFormat setSession(NutsSession session);

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
    NutsCommandLineFormat configure(boolean skipUnsupported, String... args);

}
