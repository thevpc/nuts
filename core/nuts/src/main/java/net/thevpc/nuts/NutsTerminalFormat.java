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
 * Copyright (C) 2016-2020 thevpc
 *
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
 * @category Format
 */
public interface NutsTerminalFormat {

    int textLength(String value);

    /**
     * this method removes all special "nuts print format" sequences support
     * and returns the raw string to be printed on an
     * ordinary {@link PrintStream}
     *
     * @param value input string
     * @return string without any escape sequences so that the text printed
     * correctly on any non formatted {@link PrintStream}
     */
    String filterText(String value);

    /**
     * This method escapes all special characters that are interpreted by
     * "nuts print format" o that this exact string is printed on
     * such print streams When str is null, an empty string is return
     *
     * @param value input string
     * @return string with escaped characters so that the text printed correctly
     * on a "nuts print format" aware print stream
     */
    String escapeText(String value);

    /**
     * format string. supports {@link Formatter#format(java.util.Locale, java.lang.String, java.lang.Object...)
     * }
     * pattern format and adds NutsString special format to print unfiltered strings.
     *
     *
     * @param style style
     * @param locale locale
     * @param format format
     * @param args arguments
     * @return formatted string
     */
    String formatText(NutsTextFormatStyle style, Locale locale, String format, Object... args);

    /**
     * format string. supports {@link Formatter#format(java.lang.String, java.lang.Object...)
     * }
     * pattern format and adds NutsString special format to print unfiltered strings.
     *
     *
     * @param style format style
     * @param format format
     * @param args arguments
     * @return formatted string
     */
    String formatText(NutsTextFormatStyle style, String format, Object... args);

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
