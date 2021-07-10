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
 *
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

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Base Format Interface used to print "things".
 * @author thevpc
 * @since 0.5.5
 * @category Format
 */
public interface NutsFormat extends NutsCommandLineConfigurable {

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
    NutsString format();

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
    void print(NutsPrintStream out);

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
    void print(NutsSessionTerminal terminal);

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
    void println(NutsPrintStream out);

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient print stream
     */
    void println(OutputStream out);

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
    void println(NutsSessionTerminal terminal);

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
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    NutsFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsFormat configure(boolean skipUnsupported, String... args);

    /**
     * true when Nuts Text Format is used for formatting (default)
     * @return true when Nuts Text Format is used for formatting (default)
     */
    boolean isNtf() ;

    NutsFormat setNtf(boolean ntf);
}
