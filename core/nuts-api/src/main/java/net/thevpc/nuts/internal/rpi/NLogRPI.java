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
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogScope;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.spi.NLogSPI;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Nuts Log Manager
 *
 * @app.category Logging
 */
public interface NLogRPI extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NLogRPI of() {
        return NExtensions.of(NLogRPI.class);
    }

    /**
     * Creates a new instance of create scope.
     *
     * @return create scope result
     */
    NLogScope createScope();

    /**
     * Current scope.
     *
     * @return current scope result
     */
    NLogScope currentScope();

    /**
     * Run in scope.
     *
     * @param context context
     * @param runnable runnable
     */
    void runInScope(NLogScope context, Runnable runnable);

    /**
     * Call in scope.
     *
     * @param context context
     * @param callable callable
     * @return call in scope result
     */
    <T> T callInScope(NLogScope context, NCallable<T> callable);

    /**
     * create an instance of {@link NLog}
     *
     * @param name logger name
     * @return new instance of {@link NLog}
     */
    NLog getLogger(String name);

    /**
     * Returns the logger.
     *
     * @param logger logger
     * @return get logger result
     */
    NLog getLogger(Logger logger);

    /**
     * Null logger.
     *
     * @return null logger result
     */
    NLog nullLogger();

    /**
     * Creates a new instance of create custom logger.
     *
     * @param name name
     * @param spi spi
     * @return create custom logger result
     */
    NLog createCustomLogger(String name, NLogSPI spi);

    /**
     * return terminal logger level
     *
     * @return terminal logger level
     */
    Level termLevel();

    /**
     * set terminal logger level
     *
     * @param level new level
     * @return this
     */
    NLogRPI termLevel(Level level);

    /**
     * return file logger level
     *
     * @return file logger level
     */
    Level fileLevel();

    /**
     * set file logger level
     *
     * @param level new level
     * @return this
     */
    NLogRPI fileLevel(Level level);

}
