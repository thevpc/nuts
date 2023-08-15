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
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.log;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;

import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Log operation
 *
 * @app.category Logging
 */
public interface NLogOp {
    static NLogOp of(Class clazz, NSession session) {
        return NLog.of(clazz, session).with();
    }

    static NLogOp of(String name, NSession session) {
        return NLog.of(name, session).with();
    }

    NLogOp session(NSession session);

    /**
     * set log verb
     *
     * @param verb verb or category
     * @return {@code this} instance
     */
    NLogOp verb(NLogVerb verb);

    /**
     * set log error
     *
     * @param error error thrown
     * @return {@code this} instance
     */
    NLogOp error(Throwable error);

    /**
     * set operation time
     *
     * @param time operation time in ms
     * @return {@code this} instance
     */
    NLogOp time(long time);

    /**
     * set operation level
     *
     * @param level message level
     * @return {@code this} instance
     */
    NLogOp level(Level level);

    /**
     * log the given message
     *
     * @param msg message
     */
    void log(NMsg msg);

    /**
     * log the given message
     *
     * @param msgSupplier message supplier
     */
    void log(Supplier<NMsg> msgSupplier);

    /**
     * Check if a message of the given level would actually be logged
     * by this logger.  This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *
     * @param level a message logging level
     * @return true if the given message level is currently being logged.
     */
    boolean isLoggable(Level level);

}
