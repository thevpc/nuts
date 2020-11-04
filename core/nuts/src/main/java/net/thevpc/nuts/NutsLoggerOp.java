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

import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Log operation
 * @category Logging
 */
public interface NutsLoggerOp {
    /**
     * set or unset formatted mode (Nuts Stream Format)
     * @param value formatted flag
     * @return {@code this} instance
     */
    NutsLoggerOp formatted(boolean value);

    /**
     * set formatted mode (Nuts Stream Format)
     * @return {@code this} instance
     */
    NutsLoggerOp formatted();

    /**
     * set log verb
     * @param verb verb or category
     * @return {@code this} instance
     */
    NutsLoggerOp verb(String verb);

    /**
     * set log error
     * @param error error thrown
     * @return {@code this} instance
     */
    NutsLoggerOp error(Throwable error);

    /**
     * set operation time
     * @param time operation time in ms
     * @return {@code this} instance
     */
    NutsLoggerOp time(long time);

    /**
     * set operation level
     * @param level message level
     * @return {@code this} instance
     */
    NutsLoggerOp level(Level level);

    /**
     * set message style (cstyle or positional)
     * @param style message format style
     * @return {@code this} instance
     */
    NutsLoggerOp style(NutsTextFormatStyle style);

    /**
     * log the given message
     * @param msg message
     * @param params message params
     */
    void log(String msg, Object... params);

    /**
     * log the given message
     * @param msgSupplier message supplier
     */
    void log(Supplier<String> msgSupplier);
}
