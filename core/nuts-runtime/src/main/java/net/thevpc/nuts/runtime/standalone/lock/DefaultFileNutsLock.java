package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.TimePeriod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;

public class DefaultFileNutsLock implements NutsLock {

    private static TimePeriod FIVE_MINUTES = new TimePeriod(5, TimeUnit.MINUTES);
    private Path path;
    private Object lockedObject;
    private NutsSession session;

    public DefaultFileNutsLock(Path path, Object lockedObject, NutsSession session) {
        this.path = path;
        this.lockedObject = lockedObject;
        this.session = session;
    }

    public TimePeriod getDefaultTimePeriod() {
        TimePeriod tp = TimePeriod.parseLenient(
                session.config().getConfigProperty("nuts.file-lock.timeout").getString(),
                TimeUnit.SECONDS
        );
        if (tp == null) {
            return FIVE_MINUTES;
        }
        return tp;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        TimePeriod tp = getDefaultTimePeriod();
        if (!tryLockInterruptibly(tp.getCount(), tp.getUnit())) {
            throw new NutsLockAcquireException(session, null, lockedObject, this, null);
        }
    }

    @Override
    public synchronized void lock() {
        TimePeriod tp = getDefaultTimePeriod();
        if (!tryLock(tp.getCount(), tp.getUnit())) {
            throw new NutsLockAcquireException(session, null, lockedObject, this, null);
        }
    }

    public void checkFree() {
        if (!isFree()) {
            throw new NutsLockBarrierException(session, null, lockedObject, this);
        }
    }

    public synchronized boolean isFree() {
        return !Files.exists(path);
    }

    @Override
    public synchronized void unlock() {
        try {
            Files.delete(path);
        } catch (IOException ex) {
            throw new NutsLockReleaseException(session, null, lockedObject, this, ex);
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
        if (unit == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing unit"));
        }
        long now = System.currentTimeMillis();
        PollTime ptime = preferredPollTime(time, unit);
        do {
            if (tryLockImmediately()) {
                return true;
            }
            if (System.currentTimeMillis() - now > ptime.timeMs) {
                break;
            }
            try {
                Thread.sleep(ptime.minTimeToSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (true);
        return false;
    }

    public synchronized boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
        if (unit == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing unit"));
        }
        long now = System.currentTimeMillis();
        PollTime ptime = preferredPollTime(time, unit);
        do {
            if (tryLockImmediatelyInterruptibly()) {
                return true;
            }
            if (System.currentTimeMillis() - now > ptime.timeMs) {
                break;
            }
            try {
                Thread.sleep(ptime.minTimeToSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (true);
        return false;
    }

    public boolean tryLockImmediatelyInterruptibly() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return tryLockImmediately();
    }

    public boolean tryLockImmediately() {
        try {
            if (!Files.exists(path)) {
                Path p = path.getParent();
                CoreIOUtils.mkdirs(p,session);
                Files.createFile(path);
                return true;
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    @Override
    public Condition newCondition() {
        throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("unsupported Lock.newCondition"));
    }
}
