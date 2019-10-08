package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class DefaultFileNutsLock implements NutsLock {
    private Path path;
    private Object lockedObject;
    private NutsWorkspace ws;

    public DefaultFileNutsLock(Path path, Object lockedObject, NutsWorkspace ws) {
        this.path = path;
        this.lockedObject = lockedObject;
        this.ws = ws;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock();
    }

    public void checkFree() {
        if (!isFree()) {
            throw new NutsLockBarrierException(ws, null, lockedObject, this);
        }
    }

    public synchronized boolean isFree() {
        return !Files.exists(path);
    }

    @Override
    public synchronized void lock() {
        try {
            Path p = path.getParent();
            if (p != null) {
                Files.createDirectories(p);
            }
            Files.createFile(path);
        } catch (IOException ex) {
            throw new NutsLockAcquireException(ws, null, lockedObject, this, ex);
        }
    }

    @Override
    public synchronized void unlock() {
        try {
            Files.delete(path);
        } catch (IOException ex) {
            throw new NutsLockReleaseException(ws, null, lockedObject, this, ex);
        }
    }

    @Override
    public boolean tryLock() {
        return tryLockImmediately();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        return tryLockImmediately();
    }

    public boolean tryLockImmediately() {
        try {
            if (!Files.exists(path)) {
                Path p = path.getParent();
                if (p != null) {
                    Files.createDirectories(p);
                }
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
        throw new NutsUnsupportedOperationException(ws, "Unsupported Lock.newCondition");
    }
}
