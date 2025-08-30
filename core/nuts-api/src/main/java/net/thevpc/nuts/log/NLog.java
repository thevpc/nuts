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

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgBuilder;

import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Workspace aware Logger
 *
 * @app.category Logging
 */
public interface NLog {
//    NLog NULL = NullNLog.NULL;

    String getName();

    /**
     * create an instance of {@link NLog}
     *
     * @param clazz logger clazz
     * @return new instance of {@link NLog}
     */
    static NLog of(Class<?> clazz) {
        return of(NAssert.requireNonBlank(clazz, "class").getName());
    }

    static NLog ofNull() {
        return NLogs.of().getNullLogger();
    }

    static NLog ofScoped(Class<?> clazz) {
        return ofScoped(NAssert.requireNonBlank(clazz, "class").getName());
    }

    /**
     * create an instance of {@link NLog}
     *
     * @param name logger name
     * @return new instance of {@link NLog}
     */
    static NLog of(String name) {
        return NLogs.of().getLogger(name);
    }

    static NLog of(String name, NLogSPI spi) {
        return NLogs.of().createCustomLogger(name, spi);
    }

    static NLog ofScoped(String name) {
        return of(name).scoped();
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

    NLog scoped();

    void runWith(Runnable r);

    <T> T callWith(NCallable<T> r);


    default void info(NMsg msg) {
        log(msg.asInfo());
    }

    default void debug(NMsg msg) {
        log(msg.asDebug());
    }

    default void warn(NMsg msg) {
        log(msg.asWarningAlert());
    }


    default void error(NMsg msg) {
        log(msg.asError());
    }

    void log(Level level, Supplier<NMsg> msgSupplier);

    void log(NMsg msg);

    void log(NMsgBuilder msg);

}
