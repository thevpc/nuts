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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Nuts Log Manager
 *
 * @app.category Logging
 */
public interface NLogs extends NComponent {
    static NLogs of() {
       return NExtensions.of().createComponent(NLogs.class).get();
    }

    /**
     * Log handler
     *
     * @return Log handler
     */
    List<Handler> getHandlers();

    /**
     * remove the given handler
     *
     * @param handler handler to remove
     * @return this
     */
    NLogs removeHandler(Handler handler);

    /**
     * add the given handler
     *
     * @param handler handler to add
     * @return this
     */
    NLogs addHandler(Handler handler);

    /**
     * terminal handler
     *
     * @return terminal handler
     */
    Handler getTermHandler();

    /**
     * file handler
     *
     * @return file handler
     */
    Handler getFileHandler();

    /**
     * create an instance of {@link NLog}
     *
     * @param name logger name
     * @return new instance of {@link NLog}
     */
    NLog createLogger(String name);

    /**
     * create an instance of {@link NLog}
     *
     * @param clazz logger clazz
     * @return new instance of {@link NLog}
     */
    NLog createLogger(Class<?> clazz);

    /**
     * return terminal logger level
     *
     * @return terminal logger level
     */
    Level getTermLevel();

    /**
     * set terminal logger level
     *
     * @param level new level
     * @return this
     */
    NLogs setTermLevel(Level level);

    /**
     * return file logger level
     *
     * @return file logger level
     */
    Level getFileLevel();

    /**
     * set file logger level
     *
     * @param level new level
     * @return this
     */
    NLogs setFileLevel(Level level);

}
