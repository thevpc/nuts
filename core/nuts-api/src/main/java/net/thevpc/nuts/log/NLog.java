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

import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.internal.rpi.NLogRPI;
import net.thevpc.nuts.spi.NLogSPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgBuilder;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Workspace aware Logger
 *
 * @app.category Logging
 */
public interface NLog extends NLogger{

    /**
     * Run in scope.
     *
     * @param context context
     * @param runnable runnable
     */
    static void runInScope(NLogScope context, Runnable runnable) {
        NLogRPI.of().runInScope(context, runnable);
    }

    /**
     * Call in scope.
     *
     * @param context context
     * @param callable callable
     * @return call in scope result
     */
    static <T> T callInScope(NLogScope context, NCallable<T> callable) {
        return NLogRPI.of().callInScope(context, callable);
    }

    /**
     * return terminal logger level
     *
     * @return terminal logger level
     */
    static Level termLevel(){
        return NLogRPI.of().termLevel();
    }

    /**
     * set terminal logger level
     *
     * @param level new level
     * @return this
     */
    static NLogRPI termLevel(Level level){
        return NLogRPI.of().termLevel(level);
    }

    /**
     * return file logger level
     *
     * @return file logger level
     */
    static Level fileLevel(){
        return NLogRPI.of().fileLevel();
    }

    /**
     * set file logger level
     *
     * @param level new level
     * @return this
     */
    static void fileLevel(Level level){
        NLogRPI.of().fileLevel(level);
    }

    /**
     * create an instance of {@link NLog}
     *
     * @param clazz logger clazz
     * @return new instance of {@link NLog}
     */
    static NLog of(Class<?> clazz) {
        /**
         * Creates a new instance of of.
         *
         * @param "class").getName() "class").get name()
         * @return of result
         */
        return of(NAssert.requireNamedNonBlank(clazz, "class").getName());
    }

    /**
     * Creates a new instance of of.
     *
     * @param logger logger
     * @return of result
     */
    static NLog of(Logger logger) {
        return NLogRPI.of().getLogger(logger);
    }

    /**
     * Creates a new instance of of null.
     *
     * @return of null result
     */
    static NLog ofNull() {
        return NLogRPI.of().nullLogger();
    }

    /**
     * Creates a new instance of of scoped.
     *
     * @param clazz clazz
     * @return of scoped result
     */
    static NLog ofScoped(Class<?> clazz) {
        /**
         * Creates a new instance of of scoped.
         *
         * @param "class").getName() "class").get name()
         * @return of scoped result
         */
        return ofScoped(NAssert.requireNamedNonBlank(clazz, "class").getName());
    }

    /**
     * create an instance of {@link NLog}
     *
     * @param name logger name
     * @return new instance of {@link NLog}
     */
    static NLog of(String name) {
        return NLogRPI.of().getLogger(name);
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param spi spi
     * @return of result
     */
    static NLog of(String name, NLogSPI spi) {
        return NLogRPI.of().createCustomLogger(name, spi);
    }

    /**
     * Creates a new instance of of.
     *
     * @param spi spi
     * @return of result
     */
    static NLog of(NLogSPI spi) {
        return NLogRPI.of().createCustomLogger(null, spi);
    }

    /**
     * Creates a new instance of of scoped.
     *
     * @param name name
     * @return of scoped result
     */
    static NLog ofScoped(String name) {
        /**
         * Creates a new instance of of.
         *
         * @param name).scoped( name).scoped(
         * @return of result
         */
        return of(name).scoped();
    }

    /**
     * Name.
     *
     * @return name result
     */
    String name();

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
     * Scoped.
     *
     * @return scoped result
     */
    NLog scoped();

    /**
     * Info.
     *
     * @param msg msg
     */
    default void info(NMsg msg) {
      /**
       * Log.
       *
       * @param msg.asInfo() msg.as info()
       */
        log(msg.asInfo());
    }

    /**
     * Debug.
     *
     * @param msg msg
     */
    default void debug(NMsg msg) {
      /**
       * Log.
       *
       * @param msg.asDebug() msg.as debug()
       */
        log(msg.asDebug());
    }

    /**
     * Warn.
     *
     * @param msg msg
     */
    default void warn(NMsg msg) {
      /**
       * Log.
       *
       * @param msg.asWarningAlert() msg.as warning alert()
       */
        log(msg.asWarningAlert());
    }

    /**
     * Error.
     *
     * @param msg msg
     */
    default void error(NMsg msg) {
      /**
       * Log.
       *
       * @param msg.asError() msg.as error()
       */
        log(msg.asError());
    }

    /**
     * Log.
     *
     * @param level level
     * @param msgSupplier msg supplier
     */
    void log(Level level, Supplier<NMsg> msgSupplier);

    /**
     * Log.
     *
     * @param msg msg
     */
    void log(NMsg msg);

    /**
     * Log.
     *
     * @param msg msg
     */
    void log(NMsgBuilder msg);

}
