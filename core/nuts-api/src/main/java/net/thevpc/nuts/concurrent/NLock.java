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
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * NutsLock extends {@link Lock} with describing capabilities, convenience execution methods,
 * and support for local, file-based, companion, and distributed locks.
 *
 * @app.category Input Output
 * @since 0.5.8
 */
public interface NLock extends Lock, NDescribable {

    /**
     * Creates a new lock for the given target object, ID, or file.
     *
     * @param target target object or identifier
     * @return lock instance
     */
    static NLock of(Object target) {
        return NLockBuilder.of().target(target).build();
    }

    /**
     * Creates a named lock for the given identifier using the default lock factory.
     *
     * @param lockId unique lock identifier
     * @return lock instance
     */
    static NLock of(String lockId) {
        return NLockBuilder.of().id(lockId).build();
    }

    /**
     * Creates a direct file lock where the specified file IS the lock file.
     *
     * @param lockFile lock file path
     * @return lock instance
     */
    static NLock ofFile(NPath lockFile) {
        return NLockBuilder.of().lockFile(lockFile).build();
    }

    /**
     * Creates a direct file lock where the specified file IS the lock file.
     *
     * @param lockFile lock file path
     * @return lock instance
     */
    static NLock ofFile(Path lockFile) {
        return ofFile(NPath.of(lockFile));
    }

    /**
     * Creates a direct file lock where the specified file IS the lock file.
     *
     * @param lockFile lock file
     * @return lock instance
     */
    static NLock ofFile(File lockFile) {
        return ofFile(NPath.of(lockFile));
    }

    /**
     * Creates a companion lock protecting the given file or directory.
     *
     * @param targetPath file or directory to protect
     * @return lock instance
     */
    static NLock ofCompanion(NPath targetPath) {
        return NLockBuilder.of().companion(targetPath).build();
    }

    /**
     * Creates a companion lock protecting the given file or directory.
     *
     * @param targetPath file or directory to protect
     * @return lock instance
     */
    static NLock ofCompanion(Path targetPath) {
        return ofCompanion(NPath.of(targetPath));
    }

    /**
     * Creates a companion lock protecting the given file or directory.
     *
     * @param targetPath file or directory to protect
     * @return lock instance
     */
    static NLock ofCompanion(File targetPath) {
        return ofCompanion(NPath.of(targetPath));
    }

    /**
     * Creates a companion lock protecting the given file or directory using a custom suffix or companion name.
     *
     * @param targetPath            file or directory to protect
     * @param companionNameOrSuffix custom lock suffix (e.g. {@code ".lock"}) or filename
     * @return lock instance
     */
    static NLock ofCompanion(NPath targetPath, String companionNameOrSuffix) {
        return NLockBuilder.of().companion(targetPath, companionNameOrSuffix).build();
    }

    /**
     * Creates a companion lock protecting the given file or directory using a custom suffix or companion name.
     *
     * @param targetPath            file or directory to protect
     * @param companionNameOrSuffix custom lock suffix or filename
     * @return lock instance
     */
    static NLock ofCompanion(Path targetPath, String companionNameOrSuffix) {
        return ofCompanion(NPath.of(targetPath), companionNameOrSuffix);
    }

    /**
     * Creates a companion lock protecting the given file or directory using a custom suffix or companion name.
     *
     * @param targetPath            file or directory to protect
     * @param companionNameOrSuffix custom lock suffix or filename
     * @return lock instance
     */
    static NLock ofCompanion(File targetPath, String companionNameOrSuffix) {
        return ofCompanion(NPath.of(targetPath), companionNameOrSuffix);
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
        return ofCompanion(source);
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
            return of(id.longId());
        } else {
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
        if (NBlankable.isBlank(path)) {
            path = "nuts-" + NStringUtils.firstNonBlankStripped(id.face(), "content") + ".lock";
        }
        return NLockBuilder.of().source(id.longId()).resource(NPath.of(NStoreKey.ofRun(id).scope(storeScope)).resolve(path).toPath().get()).build();
    }

    /**
     * Returns the lock identifier, if applicable.
     *
     * @return lock identifier or null
     * @since 0.8.8
     */
    @NGetter
    default String lockId() {
        return null;
    }

    /**
     * Returns the configured lease duration, if applicable.
     *
     * @return lease duration or null
     * @since 0.8.8
     */
    @NGetter
    default NDuration leaseDuration() {
        return null;
    }

    /**
     * Renews the lock lease duration if supported.
     *
     * @param leaseDuration new lease duration
     * @return true if renewed
     * @since 0.8.8
     */
    default boolean renew(NDuration leaseDuration) {
        return false;
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
     * Runs the given runnable while holding this lock.
     *
     * @param runnable runnable
     */
    default void runWith(Runnable runnable) {
        lock();
        try {
            runnable.run();
        } finally {
            unlock();
        }
    }

    /**
     * Calls the given callable while holding this lock.
     *
     * @param callable callable
     * @param <T>      return type
     * @return result of callable
     */
    default <T> T callWith(Callable<T> callable) {
        lock();
        try {
            return callable.call();
        } catch (Exception e) {
            throw NException.ofUncheckedException(e);
        } finally {
            unlock();
        }
    }

    /**
     * Runs the given runnable immediately if the lock can be acquired without waiting.
     *
     * @param runnable runnable
     * @return true if lock was acquired and runnable executed
     */
    default boolean runWithImmediately(Runnable runnable) {
        if (tryLock()) {
            try {
                runnable.run();
                return true;
            } finally {
                unlock();
            }
        }
        return false;
    }

    /**
     * Runs the given runnable if the lock can be acquired within the specified timeout.
     *
     * @param runnable runnable
     * @param time     time
     * @param unit     unit
     * @return true if lock was acquired and runnable executed
     */
    default boolean runWith(Runnable runnable, long time, TimeUnit unit) {
        try {
            if (tryLock(time, unit)) {
                try {
                    runnable.run();
                    return true;
                } finally {
                    unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /**
     * Calls the given callable immediately if the lock can be acquired without waiting.
     *
     * @param callable callable
     * @param <T>      return type
     * @return optional containing the result if acquired
     */
    default <T> NOptional<T> callWithImmediately(Callable<T> callable) {
        if (tryLock()) {
            try {
                return NOptional.of(callable.call());
            } catch (Exception e) {
                return NOptional.ofError(NMsg.ofC("error call %s", e), e);
            } finally {
                unlock();
            }
        }
        return NOptional.ofEmpty();
    }

    /**
     * Calls the given callable if the lock can be acquired within the specified timeout.
     *
     * @param callable callable
     * @param time     time
     * @param unit     unit
     * @param <T>      return type
     * @return optional containing the result if acquired
     */
    default <T> NOptional<T> callWith(Callable<T> callable, long time, TimeUnit unit) {
        try {
            if (tryLock(time, unit)) {
                try {
                    return NOptional.of(callable.call());
                } catch (Exception e) {
                    return NOptional.ofError(NMsg.ofC("error call %s", e), e);
                } finally {
                    unlock();
                }
            } else {
                return NOptional.ofEmpty();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return NOptional.ofError(NMsg.ofC("error call %s", e), e);
        }
    }
}
