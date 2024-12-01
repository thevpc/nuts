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
    static NLog NULL =new NLog() {
        @Override
        public boolean isLoggable(Level level) {
            return false;
        }

        @Override
        public void log(Level level, NLogVerb verb, NMsg msg, Throwable thrown) {

        }

        @Override
        public void log(Level level, NLogVerb verb, Supplier<NMsg> msgSupplier, Supplier<Throwable> errorSupplier) {

        }

        @Override
        public NLogOp with() {
            return null;
        }

        @Override
        public void log(LogRecord record) {

        }
    };
    /**
     * create an instance of {@link NLog}
     *
     * @param clazz logger clazz
     * @return new instance of {@link NLog}
     */
    static NLog of(Class<?> clazz) {
        return NLogs.of().createLogger(clazz);
    }

    /**
     * create an instance of {@link NLog}
     *
     * @param name logger name
     * @return new instance of {@link NLog}
     */
    static NLog of(String name) {
        return NLogs.of().createLogger(name);
    }

    /**
     * Log handler
     *
     * @return Log handler
     */
    static List<Handler> getHandlers() {
        return NLogs.of().getHandlers();
    }

    /**
     * remove the given handler
     *
     * @param handler handler to remove
     */
    static void removeHandler(Handler handler) {
        NLogs.of().removeHandler(handler);
    }

    /**
     * add the given handler
     *
     * @param handler handler to add
     */
    static void addHandler(Handler handler) {
        NLogs.of().addHandler(handler);
    }

    /**
     * terminal handler
     *
     * @return terminal handler
     */
    static Handler getTermHandler() {
        return NLogs.of().getTermHandler();
    }

    /**
     * file handler
     *
     * @return file handler
     */
    static Handler getFileHandler() {
        return NLogs.of().getFileHandler();
    }


    /**
     * return terminal logger level
     *
     * @return terminal logger level
     */
    static Level getTermLevel() {
        return NLogs.of().getTermLevel();
    }

    /**
     * set terminal logger level
     *
     * @param level new level
     */
    static void setTermLevel(Level level) {
        NLogs.of().setTermLevel(level);
    }

    /**
     * return file logger level
     *
     * @return file logger level
     */
    static Level getFileLevel() {
        return NLogs.of().getFileLevel();
    }

    /**
     * set file logger level
     *
     * @param level new level
     */
    static void setFileLevel(Level level) {
        NLogs.of().setFileLevel(level);
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
     * @param level  message level
     * @param verb   message verb / category
     * @param msg    message
     * @param thrown thrown exception
     */
    void log(Level level, NLogVerb verb, NMsg msg, Throwable thrown);

    default void log(Level level, NLogVerb verb, NMsg msg){
        log(level,verb,msg,null);
    }

    default void warn(NMsg msg, Throwable thrown){
        log(Level.WARNING, NLogVerb.WARNING, msg, thrown);
    }

    default void warn(NMsg msg){
        log(Level.WARNING, NLogVerb.WARNING, msg,null);
    }

    default void error(NMsg msg, Throwable thrown){
        log(Level.SEVERE, NLogVerb.FAIL, msg, thrown);
    }

    default void error(NMsg msg){
        log(Level.SEVERE, NLogVerb.FAIL, msg,null);
    }

    /**
     * log message using the given verb and level
     *
     * @param level         message level
     * @param verb          message verb / category
     * @param msgSupplier   message supplier
     * @param errorSupplier message error
     */
    void log(Level level, NLogVerb verb, Supplier<NMsg> msgSupplier, Supplier<Throwable> errorSupplier);

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
