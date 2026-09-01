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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

/**
 * Lock builder to create local, file-based, companion, or distributed locks.
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.8
 */
public interface NLockBuilder extends NComponent {
    /**
     * Creates a new instance of {@link NLockBuilder}.
     *
     * @return of result
     */
    static NLockBuilder of() {
        return NExtensions.of(NLockBuilder.class);
    }

    /**
     * Lock unique identifier.
     *
     * @return lock id
     * @since 0.8.8
     */
    @NGetter
    String id();

    /**
     * Sets the lock unique identifier.
     *
     * @param id lock id
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder id(String id);

    /**
     * Target object for which the lock is created (e.g. entity, key, or protected path).
     *
     * @return lock target
     * @since 0.8.8
     */
    @NGetter
    Object target();

    /**
     * Sets the target object for which the lock is created.
     *
     * @param target lock target
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder target(Object target);

    /**
     * Sets the direct lock file path (where the file IS the lock file).
     *
     * @param lockFile lock file path
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder lockFile(NPath lockFile);

    /**
     * Sets the direct lock file path.
     *
     * @param lockFile lock file path
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    default NLockBuilder lockFile(Path lockFile) {
        return lockFile(NPath.of(lockFile));
    }

    /**
     * Sets the direct lock file path.
     *
     * @param lockFile lock file
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    default NLockBuilder lockFile(File lockFile) {
        return lockFile(NPath.of(lockFile));
    }

    /**
     * Gets the direct lock file if configured.
     *
     * @return lock file
     * @since 0.8.8
     */
    @NGetter
    NPath lockFile();

    /**
     * Sets the target path to be protected by a companion lock file.
     *
     * @param targetPath file or directory to protect
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder companion(NPath targetPath);

    /**
     * Sets the target path to be protected by a companion lock file.
     *
     * @param targetPath file or directory to protect
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    default NLockBuilder companion(Path targetPath) {
        return companion(NPath.of(targetPath));
    }

    /**
     * Sets the target path to be protected by a companion lock file.
     *
     * @param targetPath file or directory to protect
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    default NLockBuilder companion(File targetPath) {
        return companion(NPath.of(targetPath));
    }

    /**
     * Sets the target path to be protected by a companion lock file with custom suffix or filename.
     *
     * @param targetPath            file or directory to protect
     * @param companionNameOrSuffix custom lock suffix (e.g. {@code ".lock"}) or filename
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder companion(NPath targetPath, String companionNameOrSuffix);

    /**
     * Sets the target path to be protected by a companion lock file with custom suffix or filename.
     *
     * @param targetPath            file or directory to protect
     * @param companionNameOrSuffix custom lock suffix or filename
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    default NLockBuilder companion(Path targetPath, String companionNameOrSuffix) {
        return companion(NPath.of(targetPath), companionNameOrSuffix);
    }

    /**
     * Sets the target path to be protected by a companion lock file with custom suffix or filename.
     *
     * @param targetPath            file or directory to protect
     * @param companionNameOrSuffix custom lock suffix or filename
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    default NLockBuilder companion(File targetPath, String companionNameOrSuffix) {
        return companion(NPath.of(targetPath), companionNameOrSuffix);
    }

    /**
     * Checks if companion mode is enabled.
     *
     * @return true if companion mode
     * @since 0.8.8
     */
    @NGetter
    boolean isCompanion();

    /**
     * Gets custom companion name or suffix.
     *
     * @return companion name or suffix
     * @since 0.8.8
     */
    @NGetter
    String companionNameOrSuffix();

    /**
     * Configures the maximum lease duration for distributed locks.
     *
     * @param leaseDuration lease duration
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder leaseDuration(NDuration leaseDuration);

    /**
     * Gets the configured lease duration.
     *
     * @return lease duration
     * @since 0.8.8
     */
    @NGetter
    NDuration leaseDuration();

    /**
     * Configures the default acquisition timeout.
     *
     * @param timeout timeout duration
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder timeout(NDuration timeout);

    /**
     * Gets the configured acquisition timeout.
     *
     * @return timeout duration
     * @since 0.8.8
     */
    @NGetter
    NDuration timeout();

    /**
     * Configures the retry sleep interval between acquisition attempts.
     *
     * @param retryInterval retry interval
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder retryInterval(NDuration retryInterval);

    /**
     * Gets the retry sleep interval.
     *
     * @return retry interval
     * @since 0.8.8
     */
    @NGetter
    NDuration retryInterval();

    /**
     * Configures auto-renewal (heartbeat) of lease while holding the lock.
     *
     * @param autoRenew auto-renew enabled
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder autoRenew(boolean autoRenew);

    /**
     * Checks if auto-renew is enabled.
     *
     * @return auto-renew flag
     * @since 0.8.8
     */
    @NGetter
    boolean isAutoRenew();

    /**
     * Sets the custom backing {@link NLockStore} for this lock.
     *
     * @param store lock store
     * @return this instance
     * @since 0.8.8
     */
    @NSetter
    NLockBuilder store(NLockStore store);

    /**
     * Gets the configured lock store.
     *
     * @return lock store
     * @since 0.8.8
     */
    @NGetter
    NLockStore store();

    /**
     * lock source represents a user defined
     * object for which the lock will be created.
     *
     * @return lock source
     * @deprecated Use {@link #target()} instead
     */
    @Deprecated
    @NGetter
    default Object source() {
        return target();
    }

    /**
     * lock resource represents the lock it self.
     * In most cases this will be the lock file.
     *
     * @return lock resource
     * @deprecated Use {@link #lockFile()} or {@link #target()} instead
     */
    @Deprecated
    default Object resource() {
        NPath lf = lockFile();
        return lf != null ? lf : target();
    }

    /**
     * Resource.
     *
     * @param source source
     * @return resource result
     * @deprecated Use {@link #lockFile(NPath)} or {@link #target(Object)}
     */
    @Deprecated
    @NSetter
    default NLockBuilder resource(NPath source) {
        return lockFile(source);
    }

    /**
     * update source
     *
     * @param source source
     * @return {@code this} instance
     * @deprecated Use {@link #target(Object)} instead
     */
    @Deprecated
    @NSetter
    default NLockBuilder source(Object source) {
        return target(source);
    }

    /**
     * update resource
     *
     * @param source resource
     * @return {@code this} instance
     * @deprecated Use {@link #lockFile(File)} instead
     */
    @Deprecated
    @NSetter
    default NLockBuilder resource(File source) {
        return lockFile(source);
    }

    /**
     * update resource
     *
     * @param source resource
     * @return {@code this} instance
     * @deprecated Use {@link #lockFile(Path)} instead
     */
    @Deprecated
    @NSetter
    default NLockBuilder resource(Path source) {
        return lockFile(source);
    }

    /**
     * update resource
     *
     * @param source resource
     * @return {@code this} instance
     * @deprecated Use {@link #lockFile(NPath)} or {@link #target(Object)}
     */
    @Deprecated
    @NSetter
    default NLockBuilder resource(Object source) {
        if (source instanceof NPath) {
            return lockFile((NPath) source);
        } else if (source instanceof Path) {
            return lockFile((Path) source);
        } else if (source instanceof File) {
            return lockFile((File) source);
        }
        return target(source);
    }

    /**
     * create lock object for the configured builder parameters
     *
     * @return new {@link NLock} instance
     */
    NLock build();
}
