package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NLockAcquireException;
import net.thevpc.nuts.concurrent.NLockBarrierException;
import net.thevpc.nuts.concurrent.NLockReleaseException;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NUpletElementBuilder;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.NWorkspaceProfilerImpl;
import net.thevpc.nuts.runtime.standalone.util.TimePeriod;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class DefaultFileNLock extends AbstractNLock {

    private static final TimePeriod FIVE_MINUTES = new TimePeriod(5, TimeUnit.MINUTES);
    private final Path path;
    private final Object lockedObject;
    private volatile Thread ownerThread;

    public static class LockInfo {
        private String hostname;
        private String pid;
        private Instant instant;
        private Instant maxValidInstant;

        public boolean isValid() {
            return instant != null && maxValidInstant != null;
        }

        public void deserialize(String value) {
            if (value == null || value.isEmpty()) return;
            String[] lines = value.split("\\r?\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("hostname=")) {
                    hostname = line.substring("hostname=".length()).trim();
                } else if (line.startsWith("pid=")) {
                    pid = line.substring("pid=".length()).trim();
                } else if (line.startsWith("instant=")) {
                    try {
                        instant = Instant.parse(line.substring("instant=".length()).trim());
                    } catch (Exception e) {
                        NLog.of(LockInfo.class).debug(NMsg.ofC("Failed to parse instant: %s", line));
                    }
                } else if (line.startsWith("maxValidInstant=")) {
                    try {
                        maxValidInstant = Instant.parse(line.substring("maxValidInstant=".length()).trim());
                    } catch (Exception e) {
                        NLog.of(LockInfo.class).debug(NMsg.ofC("Failed to parse maxValidInstant: %s", line));
                    }
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
        public String getHostname() { return hostname; }
        public String getPid() { return pid; }
        public Instant getInstant() { return instant; }
        public Instant getMaxValidInstant() { return maxValidInstant; }
    }

    public DefaultFileNLock(Path path, Object lockedObject) {
        this.path = path;
        this.lockedObject = lockedObject;
    }

    public TimePeriod getDefaultTimePeriod() {
        return NWorkspace.of()
                .getConfigProperty("nuts.file-lock.timeout")
                .flatMap(NLiteral::asString)
                .flatMap(s -> TimePeriod.parse(s, TimeUnit.SECONDS))
                .orElse(FIVE_MINUTES);
    }

    @Override
    public synchronized boolean isLocked() {
        return Files.exists(path);
    }

    @Override
    public synchronized boolean isHeldByCurrentThread() {
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
        NLog _log = NLog.of(DefaultFileNLock.class);
        NChronometer chrono = NChronometer.of();
        boolean lockAcquired = false;
        try {
            long now = System.currentTimeMillis();
            long maxWaitingTime = 30000;
            PollTime ptime = preferredPollTime(maxWaitingTime, TimeUnit.MILLISECONDS);
            long lastLog = now;
            do {
                long now2 = System.currentTimeMillis();
                if (now2 - lastLog > maxWaitingTime) {
                    _log.warn(NMsg.ofC("Lock file duration is excessive. waiting for %s for %s", NDuration.ofMillis(now2 - now), path));
                    lastLog = now2;
                }
                if (tryLockImmediately()) {
                    lockAcquired = true;
                    return;
                }
                NWorkspaceProfilerImpl.sleep(ptime.minTimeToSleep, "DefaultFileNLock::lock");
            } while (true);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new NLockAcquireException(NMsg.ofC("Interrupted while acquiring lock"),
//                    lockedObject, this, e);
        } finally {
            chrono.stop();
            if (!lockAcquired && chrono.durationMs() > 10) {
                _log.warn(NMsg.ofC("failed to acquire lock file %s after %s", path, chrono.duration()));
            }
        }
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
        if (ownerThread != null && Thread.currentThread() != ownerThread) {
            throw new NLockReleaseException(
                    NMsg.ofC("Lock not held by current thread. Owner: %s",
                            ownerThread != null ? ownerThread.getName() : "none"),
                    lockedObject, this, null);
        }
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
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

    protected static class PollTime {
        final long timeMs;
        final long minTimeToSleep;

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
                case NANOSECONDS: timeMs = Math.max(1, time / 1_000_000L); break;
                case MICROSECONDS: timeMs = Math.max(1, time / 1_000L); break;
                case MILLISECONDS: timeMs = time; break;
                case SECONDS: timeMs = time * 1000L; break;
                case MINUTES: timeMs = time * 60_000L; break;
                case HOURS: timeMs = time * 3_600_000L; break;
                case DAYS: timeMs = time * 86_400_000L; break;
            }
        }

        // FIX: Clamp the POLL INTERVAL, not the total timeout
        // Reasonable poll interval: 10ms (responsive) to 100ms (low CPU)
        long minTimeToSleep = Math.max(10L, Math.min(100L, timeMs / 10));

        // Optional: Cap total timeout to prevent accidental infinite waits
        // (Adjust 5 minutes to your needs, or remove entirely)
        if (timeMs > 300_000L) timeMs = 300_000L; // 5 minutes max

        return new PollTime(timeMs, minTimeToSleep);
    }

    @Override
    public synchronized boolean tryLock(long time, TimeUnit unit) {
        NAssert.requireNamedNonNull(unit, "unit");
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
        NAssert.requireNamedNonNull(unit, "unit");
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
        // Support reentrancy: if current thread already owns the lock, return true
        if (ownerThread == Thread.currentThread()) {
            return true;
        }
        synchronized (DefaultFileNLock.class) {
            try {
                LockInfo existingLock = readLockInfo();

                if (existingLock != null && existingLock.isValid()) {
                    if (Instant.now().isBefore(existingLock.maxValidInstant)) {
                        // Lock is still valid and held by someone else (or another process)
                        return false;
                    }
                    // Lock is expired - delete stale file before attempting acquisition
                    Files.deleteIfExists(path);
                }

                // Atomic acquisition attempt
                writeLockAtomic();
                return true;
            } catch (FileAlreadyExistsException e) {
                // Lost the race to another process/thread
                return false;
            } catch (IOException ex) {
                NLog.of(DefaultFileNLock.class).debug(
                        NMsg.ofC("IO error on %s: %s", path, ex.getMessage()));
                return false;
            }
        }
    }

    private LockInfo readLockInfo() {
        if (!Files.exists(path)) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            LockInfo li = new LockInfo();
            li.deserialize(new String(bytes));
            return li.isValid() ? li : null;
        } catch (Exception ex) {
            NLog.of(DefaultFileNLock.class).debug(
                    NMsg.ofC("Failed to read lock file %s: %s", path, ex.getMessage()));
            return null;
        }
    }

    // Atomic lock file creation with proper error handling
    private void writeLockAtomic() throws IOException {
        NPath p = NPath.of(path);
        p.mkParentDirs();

        // Atomic creation - throws FileAlreadyExistsException if file exists
        Files.createFile(path);

        try {
            LockInfo li = new LockInfo();
            li.hostname = NEnv.of().getHostName();
            li.instant = Instant.now();
            li.maxValidInstant = li.instant.plusSeconds(12 * 3600); // 12 hours TTL
            li.pid = NWorkspace.of().getPid();

            Files.write(path, li.serialize().getBytes());
            ownerThread = Thread.currentThread();
        } catch (IOException e) {
            // Cleanup on write failure to avoid stale lock file
            try {
                Files.deleteIfExists(path);
            } catch (IOException cleanupEx) {
                NLog.of(DefaultFileNLock.class).warn(
                        NMsg.ofC("Failed to cleanup lock file after write error: %s", cleanupEx.getMessage()));
            }
            throw e;
        }
    }

    @Override
    public Condition newCondition() {
        throw new NUnsupportedOperationException(NMsg.ofPlain("unsupported Lock.newCondition"));
    }

    @Override
    protected void reunlock() {
        // No-op for file-based lock
    }

    @Override
    protected void relock() {
        // No-op for file-based lock
    }

    @Override
    public NElement describe() {
        NUpletElementBuilder b = NElement.ofUpletBuilder("FileLock");
        if (path != null) {
            b.add("path", path.toString());
        }
        b.add("locked", isLocked());
        b.add("heldByCurrentThread", isHeldByCurrentThread());
        if (ownerThread != null) {
            b.add("ownerThread", ownerThread.getName());
        }
        return b.build();
    }
}