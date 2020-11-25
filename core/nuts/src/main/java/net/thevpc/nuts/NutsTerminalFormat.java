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

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Formatter;
import java.util.Locale;

/**
 * Filtered Terminal Format Helper
 *
 * @see NutsTerminalManager#getTerminalFormat()
 * @see NutsWorkspace#io()
 * @author vpc
 * @since 0.5.5
 * %category Format
 */
public interface NutsTerminalFormat {

    /**
     * prepare PrintStream to handle NutsString aware format pattern. If the instance
     * already supports Nuts specific pattern it will be returned unmodified.
     *
     * @param out PrintStream to check
     * @return NutsString pattern format capable PrintStream
     */
    PrintStream prepare(PrintStream out);

    /**
     * prepare PrintWriter to handle %N (escape) format pattern. If the instance
     * already supports Nuts specific pattern it will be returned unmodified.
     *
     * @param out PrintWriter to check
     * @return %N pattern format capable PrintWriter
     */
    PrintWriter prepare(PrintWriter out);

    /**
     * true if the stream is not null and could be resolved as Formatted Output
     * Stream. If False is returned this does no mean necessarily that the
     * stream is not formatted.
     *
     * @param out stream to check
     * @return true if formatted
     */
    boolean isFormatted(OutputStream out);

    /**
     * true if the stream is not null and could be resolved as Formatted Output
     * Stream. If False is returned this does no mean necessarily that the
     * stream is not formatted.
     *
     * @param out stream to check
     * @return true if formatted
     */
    boolean isFormatted(Writer out);
}
