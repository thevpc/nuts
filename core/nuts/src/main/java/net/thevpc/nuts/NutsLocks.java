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

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Lock builder to create mainly File based Locks
 * @author thevpc
 * @since 0.5.8
 * @app.category Input Output
 */
public interface NutsLocks extends NutsComponent {
    static NutsLocks of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsLocks.class, true, null);
    }

    /**
     * lock source represents a user defined
     * object for which the lock will be created.
     * @return lock source
     */
    Object getSource();

    /**
     * update source
     * @param source source
     * @return {@code this} instance
     */
    NutsLocks setSource(Object source);

    /**
     * lock resource represents the lock it self.
     * In most cases this will be the lock file.
     * @return lock resource
     */
    Object getResource();

    /**
     * update resource
     * @param source resource
     * @return {@code this} instance
     */
    NutsLocks setResource(File source);

    /**
     * update resource
     * @param source resource
     * @return {@code this} instance
     */
    NutsLocks setResource(Path source);

    /**
     * update resource
     * @param source resource
     * @return {@code this} instance
     */
    NutsLocks setResource(Object source);

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
    NutsLocks setSession(NutsSession session);

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
