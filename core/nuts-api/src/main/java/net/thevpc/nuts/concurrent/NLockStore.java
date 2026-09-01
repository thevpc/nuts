package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;

/**
 * Storage and distributed backend interface for {@link NLock} instances.
 * <p>
 * An {@code NLockStore} abstracts atomic lock acquisition, lease extension,
 * release, and state persistence across memory, file systems, databases,
 * or distributed coordination systems.
 *
 * @see NLock
 * @see NLockModel
 * @see NLockFactory
 * @since 0.8.8
 */
public interface NLockStore {

    /**
     * Loads the current lock model for the given identifier.
     *
     * @param id the unique identifier of the lock
     * @return the corresponding {@link NLockModel}, or {@code null} if not found
     */
    NLockModel load(String id);

    /**
     * Attempts to atomically acquire the lock for the given identifier and owner.
     *
     * @param id            the unique lock identifier
     * @param owner         the owner identifier (e.g. process/thread token)
     * @param leaseDuration the maximum lease duration before lock auto-expires
     * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
     */
    boolean tryAcquire(String id, String owner, NDuration leaseDuration);

    /**
     * Renews the lease duration for an already held lock.
     *
     * @param id            the unique lock identifier
     * @param owner         the owner identifier that currently holds the lock
     * @param leaseDuration the new lease duration to extend from now
     * @return {@code true} if the lock lease was extended, {@code false} otherwise
     */
    boolean renew(String id, String owner, NDuration leaseDuration);

    /**
     * Releases a previously acquired lock if held by the specified owner.
     *
     * @param id    the unique lock identifier
     * @param owner the owner identifier
     * @return {@code true} if the lock was released, {@code false} otherwise
     */
    boolean release(String id, String owner);

    /**
     * Deletes the lock entry identified by {@code id}.
     *
     * @param id the unique lock identifier
     * @return {@code true} if deleted
     */
    boolean delete(String id);
}
