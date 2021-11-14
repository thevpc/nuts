package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class DefaultNutsLocks extends AbstractNutsLocks {
    public DefaultNutsLocks(NutsSession ws) {
        super(ws);
    }

    @Override
    public NutsLock create() {
        checkSession();
        Object s = getSource();
        Object lr = getResource();
        Path lrPath = null;
        if (lr == null) {
            if (s == null) {
                throw new NutsLockException(getSession(), NutsMessage.cstyle("unsupported lock for null"), null, null);
            }
            Path p = toPath(s);
            if (p == null) {
                throw new NutsLockException(getSession(), NutsMessage.cstyle("unsupported lock for %s", s.getClass().getName()), null, s);
            }
            lrPath = p.resolveSibling(p.getFileName().toString() + ".lock");
        } else {
            lrPath = toPath(lr);
            if (lrPath == null) {
                throw new NutsLockException(getSession(), NutsMessage.cstyle("unsupported lock %s", lr.getClass().getName()), lr, s);
            }
        }
        return new DefaultFileNutsLock(lrPath, s, getSession());
    }

    @Override
    public <T> T call(Callable<T> runnable) {
        checkSession();
        NutsLock lock = create();
        if (!lock.tryLock()) {
            throw new NutsLockAcquireException(getSession(), null, getResource(), lock);
        }
        T value = null;
        try {
            value = runnable.call();
        } catch (Exception e) {
            if (e instanceof NutsException) {
                throw (NutsException) e;
            }
            throw new NutsException(getSession(), NutsMessage.plain("call failed"), e);
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public <T> T call(Callable<T> runnable, long time, TimeUnit unit) {
        checkSession();
        NutsLock lock = create();
        boolean b = false;
        try {
            b = lock.tryLock(time, unit);
        } catch (InterruptedException e) {
            throw new NutsLockAcquireException(getSession(), null, getResource(), lock);
        }
        if (!b) {
            throw new NutsLockAcquireException(getSession(), null, getResource(), lock);
        }
        T value = null;
        try {
            value = runnable.call();
        } catch (Exception e) {
            if (e instanceof NutsException) {
                throw (NutsException) e;
            }
            throw new NutsException(getSession(), NutsMessage.plain("call failed"), e);
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public void run(Runnable runnable) {
        checkSession();
        NutsLock lock = create();
        if (!lock.tryLock()) {
            throw new NutsLockAcquireException(getSession(), null, getResource(), lock);
        }
        try {
            runnable.run();
        } catch (Exception e) {
            if (e instanceof NutsException) {
                throw (NutsException) e;
            }
            throw new NutsException(getSession(), NutsMessage.plain("call failed"), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run(Runnable runnable, long time, TimeUnit unit) {
        checkSession();
        NutsLock lock = create();
        boolean b = false;
        try {
            b = lock.tryLock(time, unit);
        } catch (InterruptedException e) {
            throw new NutsLockAcquireException(getSession(), null, getResource(), lock);
        }
        if (!b) {
            throw new NutsLockAcquireException(getSession(), null, getResource(), lock);
        }
        try {
            runnable.run();
        } catch (Exception e) {
            if (e instanceof NutsException) {
                throw (NutsException) e;
            }
            throw new NutsException(getSession(), NutsMessage.cstyle("lock action failed"), e);
        } finally {
            lock.unlock();
        }
    }

    private Path toPath(Object lockedObject) {
        if (lockedObject instanceof NutsId) {
            NutsId nid = (NutsId) lockedObject;
            String face = nid.getFace();
            if (NutsBlankable.isBlank(face)) {
                face = "content";
            }
            return getSession().locations().setSession(getSession()).getStoreLocation((NutsId) lockedObject, NutsStoreLocation.RUN)
                    .resolve("nuts-" + face)
                    .toFile()
                    ;
        } else if (lockedObject instanceof Path) {
            return (Path) lockedObject;
        } else if (lockedObject instanceof NutsPath) {
            return ((NutsPath) lockedObject).toFile();
        } else if (lockedObject instanceof File) {
            return ((File) lockedObject).toPath();
        } else if (lockedObject instanceof String) {
            return Paths.get((String) lockedObject);
        }
        return null;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

}
