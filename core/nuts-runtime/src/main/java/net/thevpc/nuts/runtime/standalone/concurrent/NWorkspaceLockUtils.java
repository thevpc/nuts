package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.concurrent.NLockAcquireException;
import net.thevpc.nuts.concurrent.NLockBuilder;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.platform.NStoreScope;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Short lived, cross-process locks for the mutable state that is shared by all
 * processes using the same workspace.
 * <p>
 * Artifact content and descriptors already have their own per-id locks (see
 * {@code NRepositoryFolderHelper}). This helper is only meant for the small
 * critical sections around workspace level state: install information, the
 * default version pointer, the install log, and configuration files.
 * <p>
 * The locks are intentionally short leased. A process that dies while holding
 * one of these locks must not block the rest of the workspace for the default
 * twelve hours used by the artifact locks.
 */
public final class NWorkspaceLockUtils {

    /**
     * Lease for a short filesystem mutation (install info, config, log).
     */
    public static final NDuration SHORT_LEASE = NDuration.ofMinutes(2);
    /**
     * How long to wait for a short filesystem mutation.
     */
    public static final NDuration SHORT_TIMEOUT = NDuration.ofSeconds(30);
    /**
     * Workspace creation can involve installing launchers/companions, hence a
     * longer lease. It is still bounded so a crashed creator cannot block a
     * workspace forever.
     */
    public static final NDuration INIT_LEASE = NDuration.ofMinutes(30);
    public static final NDuration INIT_TIMEOUT = NDuration.ofMinutes(5);

    private NWorkspaceLockUtils() {
    }

    /**
     * Creates a lock where {@code lockFile} is the lock itself.
     */
    public static NLock fileLock(NPath lockFile, NDuration lease) {
        return NLockBuilder.of()
                .lockFile(lockFile)
                .leaseDuration(lease)
                .build();
    }

    /**
     * Creates a companion lock ({@code <target>.lock}) protecting {@code target}.
     */
    public static NLock companionLock(NPath target, NDuration lease) {
        return NLockBuilder.of()
                .companion(target)
                .leaseDuration(lease)
                .build();
    }

    /**
     * Lock for a named, workspace scoped resource. Lock files live in the
     * workspace RUN store so they never pollute the configuration folders.
     */
    public static NLock workspaceLock(String name, NDuration lease) {
        return fileLock(workspaceRunLockPath(name), lease);
    }

    public static NPath workspaceRunLockPath(String name) {
        return NPath.of(NStoreKey.ofRun())
                .resolve("locks")
                .resolve(safeName(name) + ".lock");
    }

    /**
     * Lock backing an install-information file. This intentionally resolves to
     * the same path as {@link NLock#ofIdPath(NId, NStoreScope, String)} so read
     * and write sections coordinate.
     */
    public static NLock idLock(NId id, String fileName, NDuration lease) {
        return fileLock(idLockPath(id, fileName), lease);
    }

    public static NPath idLockPath(NId id, String fileName) {
        return NPath.of(NStoreKey.ofRun(id).scope(NStoreScope.WORKSPACE))
                .resolve(fileName);
    }

    /**
     * Builds the locks for the given ids in a stable order so that two
     * processes touching the same pair of ids can never deadlock.
     */
    public static List<NLock> idLocks(Collection<NId> ids, String fileName, NDuration lease) {
        LinkedHashSet<NId> unique = new LinkedHashSet<>();
        if (ids != null) {
            for (NId id : ids) {
                if (id != null) {
                    unique.add(id);
                }
            }
        }
        List<NId> sorted = new ArrayList<>(unique);
        sorted.sort(Comparator.comparing(NId::longId));
        List<NLock> locks = new ArrayList<>(sorted.size());
        for (NId id : sorted) {
            locks.add(idLock(id, fileName, lease));
        }
        return locks;
    }

    /**
     * Acquires all locks (in the given order) and runs {@code callable} while
     * they are held.
     */
    public static <T> T withLocks(List<NLock> locks, NDuration timeout, Callable<T> callable) {
        List<NLock> acquired = new ArrayList<>(locks == null ? 0 : locks.size());
        try {
            if (locks != null) {
                for (NLock lock : locks) {
                    acquire(lock, timeout);
                    acquired.add(lock);
                }
            }
            return call(callable);
        } finally {
            releaseAll(acquired);
        }
    }

    public static <T> T withLock(NLock lock, NDuration timeout, Callable<T> callable) {
        return withLocks(Collections.singletonList(lock), timeout, callable);
    }

    public static void withLock(NLock lock, NDuration timeout, Runnable runnable) {
        withLock(lock, timeout, () -> {
            runnable.run();
            return null;
        });
    }

    public static void acquire(NLock lock, NDuration timeout) {
        if (lock == null) {
            return;
        }
        try {
            long millis = timeout == null ? 30_000L : Math.max(1L, timeout.toMillis());
            if (!lock.tryLock(millis, TimeUnit.MILLISECONDS)) {
                throw new NLockAcquireException(NMsg.ofC("timed out waiting for lock %s", lock), lock, lock);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NLockAcquireException(NMsg.ofC("interrupted while waiting for lock %s", lock), lock, lock, e);
        }
    }

    public static void releaseAll(List<NLock> locks) {
        if (locks == null) {
            return;
        }
        for (int i = locks.size() - 1; i >= 0; i--) {
            releaseQuietly(locks.get(i));
        }
    }

    public static void releaseQuietly(NLock lock) {
        if (lock == null) {
            return;
        }
        try {
            lock.unlock();
        } catch (RuntimeException ex) {
            NLog.of(NWorkspaceLockUtils.class)
                    .log(NMsg.ofC("unable to release lock %s", lock).asWarning(ex));
        }
    }

    private static <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw NException.ofUncheckedException(ex);
        }
    }

    private static String safeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "lock";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
