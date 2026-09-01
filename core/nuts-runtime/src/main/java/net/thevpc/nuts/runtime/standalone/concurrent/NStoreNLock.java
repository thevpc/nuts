package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.concurrent.NLockAcquireException;
import net.thevpc.nuts.concurrent.NLockModel;
import net.thevpc.nuts.concurrent.NLockReleaseException;
import net.thevpc.nuts.concurrent.NLockStore;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NUnsupportedOperationException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

/**
 * An {@link NLock} implementation backed by an {@link NLockStore}.
 *
 * @since 0.8.8
 */
public class NStoreNLock implements NLock {
    private static final NDuration DEFAULT_LEASE_DURATION = NDuration.ofMinutes(5);
    private static final NDuration DEFAULT_RETRY_INTERVAL = NDuration.ofMillis(50);

    private final NLockStore store;
    private final String lockId;
    private final String ownerId;
    private final NDuration leaseDuration;
    private final NDuration timeout;
    private final NDuration retryInterval;
    private final boolean autoRenew;

    private final AtomicInteger holdCount = new AtomicInteger(0);
    private volatile Thread ownerThread;

    public NStoreNLock(NLockStore store, String lockId) {
        this(store, lockId, null, DEFAULT_LEASE_DURATION, null, DEFAULT_RETRY_INTERVAL, false);
    }

    public NStoreNLock(NLockStore store, String lockId, String ownerId, NDuration leaseDuration, NDuration timeout, NDuration retryInterval, boolean autoRenew) {
        this.store = NAssert.requireNamedNonNull(store, "store");
        this.lockId = NAssert.requireNamedNonBlank(lockId, "lockId");
        this.ownerId = ownerId != null && !ownerId.trim().isEmpty() ? ownerId : generateOwnerId();
        this.leaseDuration = leaseDuration != null && leaseDuration.toMillis() > 0 ? leaseDuration : DEFAULT_LEASE_DURATION;
        this.timeout = timeout;
        this.retryInterval = retryInterval != null && retryInterval.toMillis() > 0 ? retryInterval : DEFAULT_RETRY_INTERVAL;
        this.autoRenew = autoRenew;
    }

    private static String generateOwnerId() {
        String host = NEnv.of().hostName();
        String pid = NEnv.of().pid();
        return (host != null ? host : "localhost") + ":" + (pid != null ? pid : "0") + "/" + UUID.randomUUID();
    }

    @Override
    public String lockId() {
        return lockId;
    }

    @Override
    public NDuration leaseDuration() {
        return leaseDuration;
    }

    public String ownerId() {
        return ownerId;
    }

    public NLockStore store() {
        return store;
    }

    @Override
    public boolean renew(NDuration leaseDuration) {
        if (!isHeldByCurrentThread()) {
            return false;
        }
        return store.renew(lockId, ownerId, leaseDuration != null ? leaseDuration : this.leaseDuration);
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return ownerThread == Thread.currentThread() && holdCount.get() > 0;
    }

    @Override
    public boolean isLocked() {
        if (isHeldByCurrentThread()) {
            return true;
        }
        NLockModel model = store.load(lockId);
        return model != null && !model.isExpired();
    }

    @Override
    public void lock() {
        if (isHeldByCurrentThread()) {
            holdCount.incrementAndGet();
            return;
        }
        long sleepTime = retryInterval.toMillis();
        long start = System.currentTimeMillis();
        long maxWaitMs = timeout != null ? timeout.toMillis() : -1;

        while (!tryLock()) {
            if (maxWaitMs > 0 && (System.currentTimeMillis() - start) >= maxWaitMs) {
                throw new NLockAcquireException(NMsg.ofC("Timed out waiting for lock %s", lockId), lockId, this);
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NLockAcquireException(NMsg.ofC("Interrupted while acquiring lock %s", lockId), lockId, this, e);
            }
            if (sleepTime < 1000) {
                sleepTime = Math.min(1000, sleepTime * 2);
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        if (isHeldByCurrentThread()) {
            holdCount.incrementAndGet();
            return;
        }
        long sleepTime = retryInterval.toMillis();
        long start = System.currentTimeMillis();
        long maxWaitMs = timeout != null ? timeout.toMillis() : -1;

        while (!tryLock()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (maxWaitMs > 0 && (System.currentTimeMillis() - start) >= maxWaitMs) {
                throw new NLockAcquireException(NMsg.ofC("Timed out waiting for lock %s", lockId), lockId, this);
            }
            Thread.sleep(sleepTime);
            if (sleepTime < 1000) {
                sleepTime = Math.min(1000, sleepTime * 2);
            }
        }
    }

    @Override
    public boolean tryLock() {
        if (isHeldByCurrentThread()) {
            holdCount.incrementAndGet();
            return true;
        }
        if (store.tryAcquire(lockId, ownerId, leaseDuration)) {
            ownerThread = Thread.currentThread();
            holdCount.set(1);
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (isHeldByCurrentThread()) {
            holdCount.incrementAndGet();
            return true;
        }
        long start = System.currentTimeMillis();
        long maxDurationMs = unit.toMillis(time);
        long sleepTime = retryInterval.toMillis();

        while (true) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (tryLock()) {
                return true;
            }
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed >= maxDurationMs) {
                return false;
            }
            long remaining = maxDurationMs - elapsed;
            long actualSleep = Math.min(remaining, sleepTime);
            if (actualSleep > 0) {
                Thread.sleep(actualSleep);
            }
            if (sleepTime < 500) {
                sleepTime = Math.min(500, sleepTime * 2);
            }
        }
    }

    @Override
    public void unlock() {
        if (!isHeldByCurrentThread()) {
            throw new NLockReleaseException(NMsg.ofP("Lock is not held by current thread"), lockId, this);
        }
        int count = holdCount.decrementAndGet();
        if (count == 0) {
            ownerThread = null;
            store.release(lockId, ownerId);
        }
    }

    @Override
    public Condition newCondition() {
        throw new NUnsupportedOperationException(NMsg.ofP("unsupported Lock.newCondition on store locks"));
    }

    @Override
    public NElement describe() {
        return NElement.ofTupleBuilder("StoreLock")
                .add("id", lockId)
                .add("owner", ownerId)
                .add("holdCount", holdCount.get())
                .add("leaseDuration", leaseDuration.toString())
                .add("locked", isLocked())
                .add("heldByCurrentThread", isHeldByCurrentThread())
                .build();
    }
}
