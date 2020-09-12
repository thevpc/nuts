/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Lock builder to create mainly File based Locks
 * @author vpc
 * @since 0.5.8
 * @category Input Output
 */
public interface NutsIOLockAction {

    /**
     * lock source represents a user defined
     * object for which the lock will be created.
     * @return lock source
     */
    Object getSource();

    /**
     * lock resource represents the lock it self.
     * In most cases this will be the lock file.
     * @return lock resource
     */
    Object getResource();

    /**
     * update source
     * @param source source
     * @return {@code this} instance
     */
    NutsIOLockAction source(Object source);

    /**
     * update source
     * @param source source
     * @return {@code this} instance
     */
    NutsIOLockAction setSource(Object source);

    /**
     * update resource
     * @param source resource
     * @return {@code this} instance
     */
    NutsIOLockAction setResource(File source);

    /**
     * update resource
     * @param source resource
     * @return {@code this} instance
     */
    NutsIOLockAction setResource(Path source);

    /**
     * update resource
     * @param source resource
     * @return {@code this} instance
     */
    NutsIOLockAction resource(Object source);

    /**
     * return session
     * @return session
     */
    NutsSession getSession();

    /**
     * update session
     * @param session session
     * @return {@code this} instance
     */
    NutsIOLockAction setSession(NutsSession session);

    /**
     * create lock object for the given source and resource
     * @return new {@link Lock} instance
     */
    NutsLock create();

    /**
     * create lock object for the given source and resource
     * @param runnable runnable
     * @param <T> result type
     * @return result
     */
    <T> T call(Callable<T> runnable);

    /**
     * create lock object for the given source and resource
     * @param runnable runnable
     * @param <T> result type
     * @param time time
     * @param unit unit
     * @return result
     */
    <T> T call(Callable<T> runnable, long time, TimeUnit unit);
    /**
     * create lock object for the given source and resource
     * @param runnable runnable
     */
    void run(Runnable runnable);

    /**
     * create lock object for the given source and resource
     * @param runnable runnable
     * @param time time
     * @param unit unit
     */
    void run(Runnable runnable, long time, TimeUnit unit);
}
