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
import java.util.logging.*;

/**
 * Workspace aware Logger
 * @category Logging
 */
public interface NutsLogger {

    /**
     * Check if a message of the given level would actually be logged
     * by this logger.  This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *
     * @param   level   a message logging level
     * @return  true if the given message level is currently being logged.
     */
    boolean isLoggable(Level level);

    /**
     * log message using 'FAIL' verb
     * @param level message level
     * @param msg message
     * @param thrown error thrown
     */
    void log(Level level, String msg, Throwable thrown);

    /**
     * log message using the given verb and level
     * @param level message level
     * @param verb message verb / category
     * @param msg message
     */
    void log(Level level, String verb, String msg);

    /**
     * log message using the given verb and level
     * @param level message level
     * @param verb message verb / category
     * @param msg message
     * @param params message parameters
     */
    void log(Level level, String verb, String msg, Object... params);

    /**
     * log message using the given verb and level
     * @param level message level
     * @param verb message verb / category
     * @param msgSupplier message supplier
     */
    void log(Level level, String verb, Supplier<String> msgSupplier);

    /**
     * create a logger op.
     * A Logger Op handles all information to log in a custom manner.
     * @return new instance of {@link NutsLoggerOp}
     */
    NutsLoggerOp with();


    /**
     * Log a LogRecord.
     * <br>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
    void log(LogRecord record);
}
