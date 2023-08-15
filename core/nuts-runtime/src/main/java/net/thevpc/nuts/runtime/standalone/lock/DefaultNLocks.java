package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.concurrent.NLockAcquireException;
import net.thevpc.nuts.concurrent.NLockException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class DefaultNLocks extends AbstractNLocks {
    public DefaultNLocks(NSession session) {
        super(session);
    }

    @Override
    public NLock create() {
        checkSession();
        Object s = getSource();
        Object lr = getResource();
        Path lrPath = null;
        if (lr == null) {
            if (s == null) {
                throw new NLockException(getSession(), NMsg.ofPlain("unsupported lock for null"), null, null);
            }
            Path p = toPath(s);
            if (p == null) {
                throw new NLockException(getSession(), NMsg.ofC("unsupported lock for %s", s.getClass().getName()), null, s);
            }
            lrPath = p.resolveSibling(p.getFileName().toString() + ".lock");
        } else {
            lrPath = toPath(lr);
            if (lrPath == null) {
                throw new NLockException(getSession(), NMsg.ofC("unsupported lock %s", lr.getClass().getName()), lr, s);
            }
        }
        return new DefaultFileNLock(lrPath, s, getSession());
    }

    @Override
    public <T> T call(Callable<T> runnable) {
        checkSession();
        NLock lock = create();
        if (!lock.tryLock()) {
            throw new NLockAcquireException(getSession(), null, getResource(), lock);
        }
        T value = null;
        try {
            value = runnable.call();
        } catch (Exception e) {
            if (e instanceof NException) {
                throw (NException) e;
            }
            throw new NException(getSession(), NMsg.ofPlain("call failed"), e);
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public <T> T call(Callable<T> runnable, long time, TimeUnit unit) {
        checkSession();
        NLock lock = create();
        boolean b = false;
        try {
            b = lock.tryLock(time, unit);
        } catch (InterruptedException e) {
            throw new NLockAcquireException(getSession(), null, getResource(), lock);
        }
        if (!b) {
            throw new NLockAcquireException(getSession(), null, getResource(), lock);
        }
        T value = null;
        try {
            value = runnable.call();
        } catch (Exception e) {
            if (e instanceof NException) {
                throw (NException) e;
            }
            throw new NException(getSession(), NMsg.ofPlain("call failed"), e);
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public void run(Runnable runnable) {
        checkSession();
        NLock lock = create();
        if (!lock.tryLock()) {
            throw new NLockAcquireException(getSession(), null, getResource(), lock);
        }
        try {
            runnable.run();
        } catch (Exception e) {
            if (e instanceof NException) {
                throw (NException) e;
            }
            throw new NException(getSession(), NMsg.ofPlain("call failed"), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run(Runnable runnable, long time, TimeUnit unit) {
        checkSession();
        NLock lock = create();
        boolean b = false;
        try {
            b = lock.tryLock(time, unit);
        } catch (InterruptedException e) {
            throw new NLockAcquireException(getSession(), null, getResource(), lock);
        }
        if (!b) {
            throw new NLockAcquireException(getSession(), null, getResource(), lock);
        }
        try {
            runnable.run();
        } catch (Exception e) {
            if (e instanceof NException) {
                throw (NException) e;
            }
            throw new NException(getSession(), NMsg.ofPlain("lock action failed"), e);
        } finally {
            lock.unlock();
        }
    }

    private Path toPath(Object lockedObject) {
        if (lockedObject instanceof NId) {
            NId nid = (NId) lockedObject;
            String face = nid.getFace();
            if (NBlankable.isBlank(face)) {
                face = "content";
            }
            return NLocations.of(getSession()).getStoreLocation((NId) lockedObject, NStoreType.RUN)
                    .resolve("nuts-" + face)
                    .toPath().get()
                    ;
        } else if (lockedObject instanceof Path) {
            return (Path) lockedObject;
        } else if (lockedObject instanceof NPath) {
            return ((NPath) lockedObject).toPath().get();
        } else if (lockedObject instanceof File) {
            return ((File) lockedObject).toPath();
        } else if (lockedObject instanceof String) {
            return Paths.get((String) lockedObject);
        }
        return null;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
