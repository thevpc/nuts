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

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NIsolationLevel;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

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
public interface NLock extends Lock {
    static NLock of(Object source) {
        return NLockBuilder.of().setSource(source).build();
    }

    static NLock ofPath(NPath source) {
        return NLockBuilder.of().setSource(source).build();
    }

    static NLock ofPathCompanion(NPath source) {
        return NLockBuilder.of().setSource(source).setResource(source.resolveSibling(source.getName() + ".lock")).build();
    }

    static NLock ofResource(Object resource) {
        return NLockBuilder.of().setResource(resource).build();
    }

    static NLock ofId(NId id) {
        if (NWorkspace.of().getBootOptions().getIsolationLevel().orNull() == NIsolationLevel.MEMORY) {
            return of(id.getLongId());
        } else {
            return ofIdPath(id);
        }
    }

    static NLock ofIdPath(NId id) {
        return NLockBuilder.of().setSource(id.getLongId()).setResource(NWorkspace.of().getStoreLocation(id, NStoreType.RUN)
                .resolve("nuts-" + NStringUtils.firstNonBlank(id.getFace(), "content"))
                .toPath().get()
        ).build();
    }

    boolean isLocked();

    boolean isHeldByCurrentThread();

    void runWith(Runnable runnable);

    <T> T callWith(Callable<T> callable);

    boolean runWithImmediately(Runnable runnable);

    boolean runWith(Runnable runnable, long time, TimeUnit unit);

    <T> NOptional<T> callWithImmediately(Callable<T> callable);

    <T> NOptional<T> callWith(Callable<T> callable, long time, TimeUnit unit);

}
