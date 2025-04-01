package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.concurrent.NLockException;


import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DefaultNLockBuilder extends AbstractNLockBuilder {
    Map<Object,DefaultMemLock> memLocks=new HashMap<>();
    public DefaultNLockBuilder(NWorkspace workspace) {
        super();
    }

    @Override
    public NLock build() {
        Object s = getSource();
        Object lr = getResource();
        Path lrPath = null;
        if (lr == null) {
            if (s == null) {
                throw new NLockException(NMsg.ofPlain("unsupported lock for null"), null, null);
            }
            Path p = toPath(s);
            if (p == null) {
                return memLocks.computeIfAbsent(s,e->new DefaultMemLock(e));
                //throw new NLockException(NMsg.ofC("unsupported lock for %s", s.getClass().getName()), null, s);
            }
            lrPath = p.resolveSibling(p.getFileName().toString() + ".lock");
            return new DefaultFileNLock(lrPath, s);
        } else {
            lrPath = toPath(lr);
            if (lrPath == null) {
                return memLocks.computeIfAbsent(lr,e->new DefaultMemLock(e));
                //throw new NLockException(NMsg.ofC("unsupported lock %s", lr.getClass().getName()), lr, s);
            }
            return new DefaultFileNLock(lrPath, s);
        }
    }
//
//    @Override
//    public <T> T call(Callable<T> runnable) {
//        NLock lock = create();
//        lock.lock();
////        if () {
////            throw new NLockAcquireException(null, NUtils.firstNonNull(getResource(),getSource()), lock);
////        }
//        T value = null;
//        try {
//            value = runnable.call();
//        } catch (Exception e) {
//            if (e instanceof NException) {
//                throw (NException) e;
//            }
//            throw new NException(NMsg.ofPlain("call failed"), e);
//        } finally {
//            lock.unlock();
//        }
//        return value;
//    }
//
//    @Override
//    public <T> T call(Callable<T> runnable, long time, TimeUnit unit) {
//        NLock lock = create();
//        boolean b = false;
//        try {
//            b = lock.tryLock(time, unit);
//        } catch (InterruptedException e) {
//            throw new NLockAcquireException(null, NUtils.firstNonNull(getResource(),getSource()), lock);
//        }
//        if (!b) {
//            throw new NLockAcquireException(null, NUtils.firstNonNull(getResource(),getSource()), lock);
//        }
//        T value = null;
//        try {
//            value = runnable.call();
//        } catch (Exception e) {
//            if (e instanceof NException) {
//                throw (NException) e;
//            }
//            throw new NException(NMsg.ofPlain("call failed"), e);
//        } finally {
//            lock.unlock();
//        }
//        return value;
//    }
//
//    @Override
//    public void run(Runnable runnable) {
//        NLock lock = create();
//        if (!lock.tryLock()) {
//            throw new NLockAcquireException(null, NUtils.firstNonNull(getResource(),getSource()), lock);
//        }
//        try {
//            runnable.run();
//        } catch (Exception e) {
//            if (e instanceof NException) {
//                throw (NException) e;
//            }
//            throw new NException(NMsg.ofPlain("call failed"), e);
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    @Override
//    public void run(Runnable runnable, long time, TimeUnit unit) {
//        NLock lock = create();
//        boolean b = false;
//        try {
//            b = lock.tryLock(time, unit);
//        } catch (InterruptedException e) {
//            throw new NLockAcquireException(null, NUtils.firstNonNull(getResource(),getSource()), lock);
//        }
//        if (!b) {
//            throw new NLockAcquireException(null, NUtils.firstNonNull(getResource(),getSource()), lock);
//        }
//        try {
//            runnable.run();
//        } catch (Exception e) {
//            if (e instanceof NException) {
//                throw (NException) e;
//            }
//            throw new NException(NMsg.ofPlain("lock action failed"), e);
//        } finally {
//            lock.unlock();
//        }
//    }

    private Path toPath(Object lockedObject) {
        if (lockedObject instanceof Path) {
            return (Path) lockedObject;
        } else if (lockedObject instanceof NPath) {
            return ((NPath) lockedObject).toPath().get();
        } else if (lockedObject instanceof File) {
            return ((File) lockedObject).toPath();
            //when source is string it will be handled as memory lock!!
//        } else if (lockedObject instanceof String) {
//            return Paths.get((String) lockedObject);
        }
        return null;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
