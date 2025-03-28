package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.concurrent.NLockAcquireException;
import net.thevpc.nuts.concurrent.NLockBarrierException;
import net.thevpc.nuts.concurrent.NLockReleaseException;
import net.thevpc.nuts.runtime.standalone.NWorkspaceProfilerImpl;
import net.thevpc.nuts.runtime.standalone.util.TimePeriod;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NLiteral;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultMemLock extends AbstractNLock {
    private static TimePeriod FIVE_MINUTES = new TimePeriod(5, TimeUnit.MINUTES);
    private Object lockedObject;
    private NWorkspace workspace;
    private ReentrantLock lock = new ReentrantLock();

    public DefaultMemLock(Object lockedObject, NWorkspace workspace) {
        this.lockedObject = lockedObject;
        this.workspace = workspace;
    }

    public TimePeriod getDefaultTimePeriod() {
        return TimePeriod.parse(
                NWorkspace.of().getConfigProperty("nuts.file-lock.timeout").flatMap(NLiteral::asString).get(),
                TimeUnit.SECONDS
        ).orElse(FIVE_MINUTES);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        TimePeriod tp = getDefaultTimePeriod();
        if (!tryLockInterruptibly(tp.getCount(), tp.getUnit())) {
            throw new NLockAcquireException(null, lockedObject, this, null);
        }
    }

    @Override
    public synchronized void lock() {
        lock.lock();
    }

    public void checkFree() {
        if (!isFree()) {
            throw new NLockBarrierException(null, lockedObject, this);
        }
    }

    public synchronized boolean isFree() {
        if (lock.tryLock()) {
            lock.unlock();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void unlock() {
        try {
            lock.unlock();
        } catch (Exception ex) {
            throw new NLockReleaseException(null, lockedObject, this, ex);
        }
    }

    @Override
    public boolean tryLock() {
        return tryLockImmediately();
    }

    public boolean tryLock(TimePeriod p) {
        return tryLock(p.getCount(), p.getUnit());
    }

    private class PollTime {

        long timeMs;
        long minTimeToSleep;

        public PollTime(long timeMs, long minTimeToSleep) {
            this.timeMs = timeMs;
            this.minTimeToSleep = minTimeToSleep;
        }
    }

    private PollTime preferredPollTime(long time, TimeUnit unit) {
        long timeMs = 0;
        if (time <= 0) {
            timeMs = Long.MAX_VALUE;
        } else {
            switch (unit) {
                case NANOSECONDS: {
                    timeMs = time / 1000000;
                    if (timeMs <= 0) {
                        timeMs = 1;
                    }
                    break;
                }
                case MICROSECONDS: {
                    timeMs = time / 1000;
                    if (timeMs <= 0) {
                        timeMs = 1;
                    }
                    break;
                }
                case MILLISECONDS: {
                    timeMs = time;
                    break;
                }
                case SECONDS: {
                    timeMs = time * 1000;
                    break;
                }
                case MINUTES: {
                    timeMs = time * 1000 * 60;
                    break;
                }
                case HOURS: {
                    timeMs = time * 1000 * 3600;
                    break;
                }
                case DAYS: {
                    timeMs = time * 1000 * 3600 * 24;
                    break;
                }
            }
        }
        long minTimeToSleep = timeMs / 10;
        if (timeMs < 200) {
            timeMs = 200;
        }
        return new PollTime(timeMs, minTimeToSleep);
    }

    @Override
    public synchronized boolean tryLock(long time, TimeUnit unit) {
        NAssert.requireNonNull(unit, "unit");
        try {
            lock.tryLock(time, unit);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public synchronized boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
        NAssert.requireNonNull(unit, "unit");
        long now = System.currentTimeMillis();
        PollTime ptime = preferredPollTime(time, unit);
        do {
            if (tryLockImmediatelyInterruptibly()) {
                return true;
            }
            if (System.currentTimeMillis() - now > ptime.timeMs) {
                break;
            }
            NWorkspaceProfilerImpl.sleep(ptime.minTimeToSleep,"DefaultMemLock::tryLockInterruptibly");
        } while (true);
        return false;
    }

    public boolean tryLockImmediatelyInterruptibly() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return tryLockImmediately();
    }

    @Override
    public boolean isLocked() {
        return lock.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    public boolean tryLockImmediately() {
        try {
            lock.tryLock();
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    @Override
    public Condition newCondition() {
        return lock.newCondition();
    }

    @Override
    protected void reunlock() {
        lock.unlock();
    }

    @Override
    protected void relock() {
        lock.lock();
    }
}
