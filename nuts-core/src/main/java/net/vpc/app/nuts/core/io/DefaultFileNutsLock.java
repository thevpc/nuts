package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public void checkFree() {
        if(!isFree()){
            throw new NutsLockBarrierException(ws, null, lockedObject, this);
        }
    }

    @Override
    public synchronized boolean isFree() {
        return !Files.exists(path);
    }

    @Override
    public synchronized void acquire() {
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
    public synchronized void release() {
        try {
            Files.delete(path);
        } catch (IOException ex) {
            throw new NutsLockReleaseException(ws, null, lockedObject, this, ex);
        }
    }
}
