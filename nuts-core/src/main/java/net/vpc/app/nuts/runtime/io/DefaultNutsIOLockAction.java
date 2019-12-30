package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class DefaultNutsIOLockAction extends AbstractNutsIOLockAction {
    public DefaultNutsIOLockAction(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public void run(Runnable runnable, long time, TimeUnit unit) {
        NutsLock lock = create();
        boolean b = false;
        try {
            b = lock.tryLock(time, unit);
        } catch (InterruptedException e) {
            throw new NutsLockAcquireException(getWs(), null, getResource(), lock);
        }
        if (!b) {
            throw new NutsLockAcquireException(getWs(), null, getResource(), lock);
        }
        try {
            runnable.run();
        } catch (Exception e) {
            if (e instanceof NutsException) {
                throw (NutsException) e;
            }
            throw new NutsException(getWs(), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T call(Callable<T> runnable, long time, TimeUnit unit) {
        NutsLock lock = create();
        boolean b = false;
        try {
            b = lock.tryLock(time, unit);
        } catch (InterruptedException e) {
            throw new NutsLockAcquireException(getWs(), null, getResource(), lock);
        }
        if (!b) {
            throw new NutsLockAcquireException(getWs(), null, getResource(), lock);
        }
        T value = null;
        try {
            value = runnable.call();
        } catch (Exception e) {
            if (e instanceof NutsException) {
                throw (NutsException) e;
            }
            throw new NutsException(getWs(), e);
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public <T> T call(Callable<T> runnable) {
        NutsLock lock = create();
        if (!lock.tryLock()) {
            throw new NutsLockAcquireException(getWs(), null, getResource(), lock);
        }
        T value = null;
        try {
            value = runnable.call();
        } catch (Exception e) {
            if (e instanceof NutsException) {
                throw (NutsException) e;
            }
            throw new NutsException(getWs(), e);
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public void run(Runnable runnable) {
        NutsLock lock = create();
        if (!lock.tryLock()) {
            throw new NutsLockAcquireException(getWs(), null, getResource(), lock);
        }
        try {
            runnable.run();
        } catch (Exception e) {
            if (e instanceof NutsException) {
                throw (NutsException) e;
            }
            throw new NutsException(getWs(), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NutsLock create() {
        Object s = getSource();
        Object lr = getResource();
        Path lrPath = null;
        if (lr == null) {
            if (s == null) {
                throw new NutsLockException(getWs(), "Unsupported lock for null", null, null);
            }
            Path p = toPath(s);
            if (p == null) {
                throw new NutsLockException(getWs(), "Unsupported lock for " + s.getClass().getName(), null, s);
            }
            lrPath = p.resolveSibling(p.getFileName().toString() + ".lock");
        } else {
            lrPath = toPath(lr);
            if (lrPath == null) {
                throw new NutsLockException(getWs(), "Unsupported lock " + lr.getClass().getName(), lr, s);
            }
        }
        return new DefaultFileNutsLock(lrPath, s, getWs());
    }

    private Path toPath(Object lockedObject) {
        if (lockedObject instanceof NutsId) {
            NutsId nid = (NutsId) lockedObject;
            String face = nid.getFace();
            if (CoreStringUtils.isBlank(face)) {
                face = "content";
            }
            return getWs().config().getStoreLocation((NutsId) lockedObject, NutsStoreLocation.RUN).resolve("lock-" + face + ".lock");
        } else if (lockedObject instanceof Path) {
            return (Path) lockedObject;
        } else if (lockedObject instanceof File) {
            return ((File) lockedObject).toPath();
        } else if (lockedObject instanceof String) {
            return Paths.get((String) lockedObject);
        }
        return null;
    }
}
