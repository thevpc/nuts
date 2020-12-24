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
    void log(NutsSession session, Level level, String msg, Throwable thrown);

    /**
     * log message using the given verb and level
     * @param level message level
     * @param verb message verb / category
     * @param msg message
     */
    void log(NutsSession session, Level level, String verb, String msg);

    /**
     * log message using the given verb and level
     * @param level message level
     * @param verb message verb / category
     * @param msg message
     * @param params message parameters
     */
    void log(NutsSession session, Level level, String verb, String msg, Object... params);

    /**
     * log message using the given verb and level
     * @param level message level
     * @param verb message verb / category
     * @param msgSupplier message supplier
     */
    void log(NutsSession session, Level level, String verb, Supplier<String> msgSupplier);

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
