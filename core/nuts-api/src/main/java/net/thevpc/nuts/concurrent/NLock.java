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
package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NIsolationLevel;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.platform.NStoreScope;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.*;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * NutsLock is simply an adapter to standard {@link Lock}.
 * It adds no extra functionality but rather is provided as
 * a base for future changes.
 *
 * @app.category Input Output
 * @since 0.5.8
 */
public interface NLock extends Lock, NDescribable {
    /**
     * Creates a new instance of of.
     *
     * @param source source
     * @return of result
     */
    static NLock of(Object source) {
        return NLockBuilder.of().source(source).build();
    }

    /**
     * Creates a new instance of of path.
     *
     * @param source source
     * @return of path result
     */
    static NLock ofPath(NPath source) {
        return NLockBuilder.of().source(source).build();
    }

    /**
     * Creates a new instance of of path companion.
     *
     * @param source source
     * @return of path companion result
     */
    static NLock ofPathCompanion(NPath source) {
        return NLockBuilder.of().source(source).resource(source.resolveSibling(source.name() + ".lock")).build();
    }

    /**
     * Creates a new instance of of resource.
     *
     * @param resource resource
     * @return of resource result
     */
    static NLock ofResource(Object resource) {
        return NLockBuilder.of().resource(resource).build();
    }

    /**
     * Creates a new instance of of id.
     *
     * @param id id
     * @return of id result
     */
    static NLock ofId(NId id) {
        if (NWorkspace.of().bootOptions().isolationLevel().orNull() == NIsolationLevel.MEMORY) {
            /**
             * Creates a new instance of of.
             *
             * @param id.longId() id.long id()
             * @return of result
             */
            return of(id.longId());
        } else {
            /**
             * Creates a new instance of of id path.
             *
             * @param id id
             * @param NStoreScope.WORKSPACE n store scope.workspace
             * @return of id path result
             */
            return ofIdPath(id, NStoreScope.WORKSPACE);
        }
    }

    /**
     * Creates a new instance of of id path.
     *
     * @param id id
     * @param storeScope store scope
     * @return of id path result
     */
    static NLock ofIdPath(NId id, NStoreScope storeScope) {
        /**
         * Creates a new instance of of id path.
         *
         * @param id id
         * @param storeScope store scope
         * @param null null
         * @return of id path result
         */
        return ofIdPath(id, storeScope, null);
    }

    /**
     * Creates a new instance of of id path.
     *
     * @param id id
     * @param storeScope store scope
     * @param path path
     * @return of id path result
     */
    static NLock ofIdPath(NId id, NStoreScope storeScope, String path) {
        if(NBlankable.isBlank(path)){
            path="nuts-" + NStringUtils.firstNonBlankStripped(id.face(), "content") + ".lock";
        }
        return NLockBuilder.of().source(id.longId()).resource(NPath.of(NStoreKey.ofRun(id).scope(storeScope)).resolve(path).toPath().get()).build();
    }

    /**
     * Checks if is locked.
     *
     * @return is locked result
     */
    @NGetter
    boolean isLocked();

    /**
     * Checks if is held by current thread.
     *
     * @return is held by current thread result
     */
    @NGetter
    boolean isHeldByCurrentThread();

    /**
     * Run with.
     *
     * @param runnable runnable
     */
    void runWith(Runnable runnable);

    /**
     * Call with.
     *
     * @param callable callable
     * @return call with result
     */
    <T> T callWith(Callable<T> callable);

    /**
     * Run with immediately.
     *
     * @param runnable runnable
     * @return run with immediately result
     */
    boolean runWithImmediately(Runnable runnable);

    /**
     * Run with.
     *
     * @param runnable runnable
     * @param time time
     * @param unit unit
     * @return run with result
     */
    boolean runWith(Runnable runnable, long time, TimeUnit unit);

    /**
     * Call with immediately.
     *
     * @param callable callable
     * @return call with immediately result
     */
    <T> NOptional<T> callWithImmediately(Callable<T> callable);

    /**
     * Call with.
     *
     * @param callable callable
     * @param time time
     * @param unit unit
     * @return call with result
     */
    <T> NOptional<T> callWith(Callable<T> callable, long time, TimeUnit unit);

}
