package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NLockAcquireException;
import net.thevpc.nuts.concurrent.NLockBarrierException;
import net.thevpc.nuts.concurrent.NLockReleaseException;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.NWorkspaceProfilerImpl;
import net.thevpc.nuts.runtime.standalone.util.TimePeriod;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class DefaultFileNLock extends AbstractNLock {

    private static TimePeriod FIVE_MINUTES = new TimePeriod(5, TimeUnit.MINUTES);
    private Path path;
    private Object lockedObject;
    private NWorkspace workspace;
    private Thread ownerThread;

    public static class LockInfo {
        String hostname;
        String pid;
        Instant instant;
        Instant maxValidInstant;

        public boolean isValid() {
            return instant != null && maxValidInstant != null;
        }

        public void deserialize(String value) {
            NStringBuilder sb = new NStringBuilder(value);
            for (String line : sb.lines()) {
                line = line.trim();
                if (line.startsWith("hostname=")) {
                    hostname = line.substring("hostname=".length()).trim();
                } else if (line.startsWith("pid=")) {
                    pid = line.substring("pid=".length()).trim();
                } else if (line.startsWith("instant=")) {
                    instant = Instant.parse(line.substring("instant=".length()).trim());
                } else if (line.startsWith("maxValidInstant=")) {
                    maxValidInstant = Instant.parse(line.substring("maxValidInstant=".length()).trim());
                }
            }
        }

        public String serialize() {
            NStringBuilder sb = new NStringBuilder();
            sb.println("hostname=" + NStringUtils.trim(hostname));
            sb.println("pid=" + NStringUtils.trim(pid));
            sb.println("instant=" + instant);
            sb.println("maxValidInstant=" + maxValidInstant);
            return sb.toString();
        }

        @Override
        public String toString() {
            return "LockInfo{" +
                    "hostname='" + hostname + '\'' +
                    ", pid='" + pid + '\'' +
                    ", instant='" + instant + "'" +
                    ", maxValidInstant='" + maxValidInstant + "'" +
                    '}';
        }
    }

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
        long maxWaitingTime=30000;
        PollTime ptime = preferredPollTime(maxWaitingTime, TimeUnit.MILLISECONDS);
        long lastLog = System.currentTimeMillis();
        do {
            long now2 = System.currentTimeMillis();
            if (now2 - lastLog > maxWaitingTime) {
                NLog.of(DefaultFileNLock.class).warn(NMsg.ofC("Lock file duration is excessive. waiting for %s for %s", NDuration.ofMillis(now2 - now), path));
                lastLog = now2;
            }
            if (tryLockImmediately()) {
                return;
            }
            NWorkspaceProfilerImpl.sleep(ptime.minTimeToSleep, "DefaultFileNLock::lock");
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
        if (timeMs > 500) {
            timeMs = 500;
        }else if (timeMs < 100) {
            timeMs = 100;
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
            NWorkspaceProfilerImpl.sleep(ptime.minTimeToSleep, "DefaultFileNLock::tryLock");
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
            NWorkspaceProfilerImpl.sleep(ptime.minTimeToSleep, "DefaultFileNLock::tryLockInterruptibly");
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
            synchronized (DefaultFileNLock.class) {
                if (!Files.exists(path)) {
                    writeLock();
                    return true;
                }else{
                    LockInfo li = new LockInfo();
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        li.deserialize(new String(bytes));
                    }catch (Exception ex) {
                        //
                    }
                    if(li.isValid() && Instant.now().compareTo(li.maxValidInstant)<=0){
                        return false;
                    }
                    writeLock();
                    return true;
                }
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private void writeLock() {
        try {
            NPath p = NPath.of(path);
            p.mkParentDirs();
            Files.createFile(path);
            LockInfo li = new LockInfo();
            li.hostname=workspace.getHostName();
            li.instant=Instant.now();
            li.maxValidInstant=li.instant.plusSeconds(12*3600);
            li.pid=workspace.getPid();

            Files.write(path,li.serialize().getBytes());
            ownerThread = Thread.currentThread();
        } catch (IOException ex) {
            throw new NLockAcquireException(NMsg.ofC("unable to acquire lock"), lockedObject, this, ex);
        }
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
