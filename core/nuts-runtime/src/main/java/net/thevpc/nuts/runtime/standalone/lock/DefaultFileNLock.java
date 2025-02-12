package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.concurrent.NLockAcquireException;
import net.thevpc.nuts.concurrent.NLockBarrierException;
import net.thevpc.nuts.concurrent.NLockReleaseException;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.util.TimePeriod;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class DefaultFileNLock extends AbstractNLock {

    private static TimePeriod FIVE_MINUTES = new TimePeriod(5, TimeUnit.MINUTES);
    private Path path;
    private Object lockedObject;
    private NWorkspace workspace;
    private Thread ownerThread;

    public DefaultFileNLock(Path path, Object lockedObject, NWorkspace workspace) {
        this.path = path;
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
    public boolean isLocked() {
        return Files.exists(path);
    }


    @Override
    public boolean isHeldByCurrentThread() {
        return isLocked() && Thread.currentThread() == ownerThread;
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
        long now = System.currentTimeMillis();
        PollTime ptime = preferredPollTime(now, TimeUnit.SECONDS);
        long lastLog=System.currentTimeMillis();
        do {
            long now2 = System.currentTimeMillis();
            if(now2-lastLog>30000){
                NLog.of(DefaultFileNLock.class).warn(NMsg.ofC("Lock file duration is excessive. waiting for %s for %s", NDuration.ofMillis(now2-now),path));
                lastLog=now2;
            }
            if (tryLockImmediately()) {
                return;
            }
            try {
                Thread.sleep(ptime.minTimeToSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (true);
    }

    public void checkFree() {
        if (!isFree()) {
            throw new NLockBarrierException(null, lockedObject, this);
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
            throw new NLockReleaseException(null, lockedObject, this, ex);
        } finally {
            ownerThread = null;
        }
    }

    @Override
    public boolean tryLock() {
        return tryLockImmediately();
    }

    public boolean tryLock(TimePeriod p) {
        return tryLock(p.getCount(), p.getUnit());
    }

    protected class PollTime {

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
                NPath p = NPath.of(path);
                p.mkParentDirs();
                Files.createFile(path);
                Date now = new Date();
                Files.write(path,
                        (
                                "hostname=" + workspace.getHostName() + "\n"
                                        + "pid=" + workspace.getPid() + "\n"
                                        + "time=" + now.getTime() + "\n"
                                        + "date=" + now + "\n"
                        ).getBytes());
                ownerThread = Thread.currentThread();
                return true;
            }
        } catch (IOException ex) {
            return false;
        }
        return false;
    }

    @Override
    public Condition newCondition() {
        throw new NUnsupportedOperationException(NMsg.ofPlain("unsupported Lock.newCondition"));
    }

    @Override
    protected void reunlock() {
    }

    @Override
    protected void relock() {
    }
}
