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
import net.thevpc.nuts.NSession;
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
    static NLogs of(NSession session) {
       return NExtensions.of(session).createComponent(NLogs.class).get();
    }

    /**
     * Log handler
     *
     * @param session session
     * @return Log handler
     */
    List<Handler> getHandlers(NSession session);

    /**
     * remove the given handler
     *
     * @param handler handler to remove
     * @param session session
     * @return this
     */
    NLogs removeHandler(Handler handler, NSession session);

    /**
     * add the given handler
     *
     * @param handler handler to add
     * @param session session
     * @return this
     */
    NLogs addHandler(Handler handler, NSession session);

    /**
     * terminal handler
     *
     * @param session session
     * @return terminal handler
     */
    Handler getTermHandler(NSession session);

    /**
     * file handler
     *
     * @param session session
     * @return file handler
     */
    Handler getFileHandler(NSession session);

    /**
     * create an instance of {@link NLog}
     *
     * @param name logger name
     * @param session session
     * @return new instance of {@link NLog}
     */
    NLog createLogger(String name, NSession session);

    /**
     * create an instance of {@link NLog}
     *
     * @param clazz logger clazz
     * @param session session
     * @return new instance of {@link NLog}
     */
    NLog createLogger(Class clazz, NSession session);

    /**
     * return terminal logger level
     *
     * @param session session
     * @return terminal logger level
     */
    Level getTermLevel(NSession session);

    /**
     * set terminal logger level
     *
     * @param level new level
     * @param session session
     * @return this
     */
    NLogs setTermLevel(Level level, NSession session);

    /**
     * return file logger level
     *
     * @param session session
     * @return file logger level
     */
    Level getFileLevel(NSession session);

    /**
     * set file logger level
     *
     * @param level new level
     * @param session session
     * @return this
     */
    NLogs setFileLevel(Level level, NSession session);

}
