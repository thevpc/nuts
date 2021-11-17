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
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsLogManager;

import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Workspace aware Logger
 *
 * @app.category Logging
 */
public interface NutsLogger {

    /**
     * create an instance of {@link NutsLogger}
     *
     * @param clazz logger clazz
     * @return new instance of {@link NutsLogger}
     */
    static NutsLogger of(Class clazz, NutsSession session) {
        return NutsLogManager.of(session).createLogger(clazz, session);
    }

    /**
     * create an instance of {@link NutsLogger}
     *
     * @param name logger name
     * @return new instance of {@link NutsLogger}
     */
    static NutsLogger of(String name, NutsSession session) {
        return NutsLogManager.of(session).createLogger(name, session);
    }

    /**
     * Log handler
     *
     * @return Log handler
     */
    static Handler[] getHandlers(NutsSession session) {
        return NutsLogManager.of(session).getHandlers(session);
    }

    /**
     * remove the given handler
     *
     * @param handler handler to remove
     */
    static void removeHandler(Handler handler, NutsSession session) {
        NutsLogManager.of(session).removeHandler(handler, session);
    }

    /**
     * add the given handler
     *
     * @param handler handler to add
     * @return this
     */
    static void addHandler(Handler handler, NutsSession session) {
        NutsLogManager.of(session).addHandler(handler, session);
    }

    /**
     * terminal handler
     *
     * @return terminal handler
     */
    static Handler getTermHandler(NutsSession session) {
        return NutsLogManager.of(session).getTermHandler(session);
    }

    /**
     * file handler
     *
     * @return file handler
     */
    static Handler getFileHandler(NutsSession session) {
        return NutsLogManager.of(session).getFileHandler(session);
    }


    /**
     * return terminal logger level
     *
     * @return terminal logger level
     */
    static Level getTermLevel(NutsSession session) {
        return NutsLogManager.of(session).getTermLevel(session);
    }

    /**
     * set terminal logger level
     *
     * @param level new level
     */
    static void setTermLevel(Level level, NutsSession session) {
        NutsLogManager.of(session).setTermLevel(level, session);
    }

    /**
     * return file logger level
     *
     * @return file logger level
     */
    static Level getFileLevel(NutsSession session) {
        return NutsLogManager.of(session).getFileLevel(session);
    }

    /**
     * set file logger level
     *
     * @param level new level
     * @return this
     */
    static void setFileLevel(Level level, NutsSession session) {
        NutsLogManager.of(session).setFileLevel(level, session);
    }


    /**
     * Check if a message of the given level would actually be logged
     * by this logger.  This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *
     * @param level a message logging level
     * @return true if the given message level is currently being logged.
     */
    boolean isLoggable(Level level);

    /**
     * log message using the given verb and level
     *
     * @param session session
     * @param level   message level
     * @param verb    message verb / category
     * @param msg     message
     * @param thrown  thrown exception
     */
    void log(NutsSession session, Level level, NutsLogVerb verb, NutsMessage msg, Throwable thrown);

    /**
     * log message using the given verb and level
     *
     * @param session       session
     * @param level         message level
     * @param verb          message verb / category
     * @param msgSupplier   message supplier
     * @param errorSupplier message error
     */
    void log(NutsSession session, Level level, NutsLogVerb verb, Supplier<NutsMessage> msgSupplier, Supplier<Throwable> errorSupplier);

    /**
     * create a logger op.
     * A Logger Op handles all information to log in a custom manner.
     *
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
