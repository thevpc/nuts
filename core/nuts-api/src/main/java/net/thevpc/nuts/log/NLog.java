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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Workspace aware Logger
 *
 * @app.category Logging
 */
public interface NLog {

    /**
     * create an instance of {@link NLog}
     *
     * @param clazz logger clazz
     * @param session session
     * @return new instance of {@link NLog}
     */
    static NLog of(Class clazz, NSession session) {
        return NLogs.of(session).createLogger(clazz, session);
    }

    /**
     * create an instance of {@link NLog}
     *
     * @param name logger name
     * @param session session
     * @return new instance of {@link NLog}
     */
    static NLog of(String name, NSession session) {
        return NLogs.of(session).createLogger(name, session);
    }

    /**
     * Log handler
     *
     * @param session session
     * @return Log handler
     */
    static List<Handler> getHandlers(NSession session) {
        return NLogs.of(session).getHandlers(session);
    }

    /**
     * remove the given handler
     *
     * @param handler handler to remove
     * @param session session
     */
    static void removeHandler(Handler handler, NSession session) {
        NLogs.of(session).removeHandler(handler, session);
    }

    /**
     * add the given handler
     *
     * @param handler handler to add
     * @param session session
     */
    static void addHandler(Handler handler, NSession session) {
        NLogs.of(session).addHandler(handler, session);
    }

    /**
     * terminal handler
     *
     * @param session session
     * @return terminal handler
     */
    static Handler getTermHandler(NSession session) {
        return NLogs.of(session).getTermHandler(session);
    }

    /**
     * file handler
     *
     * @param session session
     * @return file handler
     */
    static Handler getFileHandler(NSession session) {
        return NLogs.of(session).getFileHandler(session);
    }


    /**
     * return terminal logger level
     *
     * @param session session
     * @return terminal logger level
     */
    static Level getTermLevel(NSession session) {
        return NLogs.of(session).getTermLevel(session);
    }

    /**
     * set terminal logger level
     *
     * @param session session
     * @param level new level
     */
    static void setTermLevel(Level level, NSession session) {
        NLogs.of(session).setTermLevel(level, session);
    }

    /**
     * return file logger level
     *
     * @param session session
     * @return file logger level
     */
    static Level getFileLevel(NSession session) {
        return NLogs.of(session).getFileLevel(session);
    }

    /**
     * set file logger level
     *
     * @param level new level
     * @param session session
     */
    static void setFileLevel(Level level, NSession session) {
        NLogs.of(session).setFileLevel(level, session);
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
    void log(NSession session, Level level, NLogVerb verb, NMsg msg, Throwable thrown);

    /**
     * log message using the given verb and level
     *
     * @param session       session
     * @param level         message level
     * @param verb          message verb / category
     * @param msgSupplier   message supplier
     * @param errorSupplier message error
     */
    void log(NSession session, Level level, NLogVerb verb, Supplier<NMsg> msgSupplier, Supplier<Throwable> errorSupplier);

    /**
     * create a logger op.
     * A Logger Op handles all information to log in a custom manner.
     *
     * @return new instance of {@link NLogOp}
     */
    NLogOp with();


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
