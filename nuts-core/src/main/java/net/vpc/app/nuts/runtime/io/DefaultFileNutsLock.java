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
        tryLock(0,TimeUnit.DAYS);
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
    public synchronized boolean tryLock(long time, TimeUnit unit) {
        if (unit == null) {
            throw new NutsIllegalArgumentException(ws, "Missing unit");
        }
        long now = System.currentTimeMillis();
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
        long minTimeToSleep=timeMs/10;
        if(timeMs<200){
            timeMs=200;
        }
        do{
            if(tryLockImmediately()){
                return true;
            }
            if(System.currentTimeMillis()-now>timeMs){
                break;
            }
            try {
                Thread.sleep(minTimeToSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }while(true);
        return false;
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
