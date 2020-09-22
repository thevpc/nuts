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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Nuts Log Manager
 * @category Logging
 */
public interface NutsLogManager {
    /**
     * Log handler
     * @return Log handler
     */
    Handler[] getHandlers();

    /**
     * remove the given handler
     * @param handler handler to remove
     */
    void removeHandler(Handler handler);

    /**
     * add the given handler
     * @param handler handler to add
     */
    void addHandler(Handler handler);

    /**
     * terminal handler
     * @return terminal handler
     */
    Handler getTermHandler();

    /**
     * file handler
     * @return file handler
     */
    Handler getFileHandler();

    /**
     * create an instance of {@link NutsLogger}
     * @param name logger name
     * @return new instance of {@link NutsLogger}
     */
    NutsLogger of(String name);

    /**
     * create an instance of {@link NutsLogger}
     * @param clazz logger clazz
     * @return new instance of {@link NutsLogger}
     */
    NutsLogger of(Class clazz);

    /**
     * return terminal logger level
     * @return terminal logger level
     */
    Level getTermLevel();

    /**
     * set terminal logger level
     * @param level new level
     * @param options update options
     */
    void setTermLevel(Level level, NutsUpdateOptions options);

    /**
     * return file logger level
     * @return file logger level
     */
    Level getFileLevel();

    /**
     * set file logger level
     * @param level new level
     * @param options update options
     */
    void setFileLevel(Level level, NutsUpdateOptions options);
}
