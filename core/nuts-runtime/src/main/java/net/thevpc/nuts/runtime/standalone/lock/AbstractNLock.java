package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public abstract class AbstractNLock implements NLock {
    @Override
    public void runWith(Runnable runnable) {
        try {
            lock();
            runnable.run();
        } finally {
            unlock();
        }
    }

    @Override
    public <T> T callWith(Callable<T> callable) {
        lock();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            unlock();
        }
    }

    @Override
    public boolean runWithImmediately(Runnable runnable) {
        if (isHeldByCurrentThread()) {
            relock();
            try {
                runnable.run();
            } finally {
                reunlock();
            }
            return true;
        } else {
            if (tryLock()) {
                try {
                    runnable.run();
                    return true;
                } finally {
                    unlock();
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean runWith(Runnable runnable, long time, TimeUnit unit)  {
        if (isHeldByCurrentThread()) {
            relock();
            try {
                runnable.run();
            } finally {
                reunlock();
            }
            return true;
        } else {
            try {
                if (tryLock(time, unit)) {
                    try {
                        runnable.run();
                        return true;
                    } finally {
                        unlock();
                    }
                } else {
                    return false;
                }
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    @Override
    public <T> NOptional<T> callWithImmediately(Callable<T> callable) {
        if (isHeldByCurrentThread()) {
            relock();
            try {
                try {
                    return NOptional.of(callable.call());
                } catch (Exception e) {
                    return NOptional.ofError(NMsg.ofC("error call %s", e), e);
                }
            } finally {
                reunlock();
            }
        } else {
            if (tryLock()) {
                try {
                    return NOptional.of(callable.call());
                } catch (Exception e) {
                    return NOptional.ofError(NMsg.ofC("error call %s", e), e);
                } finally {
                    unlock();
                }
            } else {
                return NOptional.ofEmpty();
            }
        }
    }

    @Override
    public <T> NOptional<T> callWith(Callable<T> callable, long time, TimeUnit unit) {
        if (isHeldByCurrentThread()) {
            relock();
            try {
                try {
                    return NOptional.of(callable.call());
                } catch (Exception e) {
                    return NOptional.ofError(NMsg.ofC("error call %s", e), e);
                }
            } finally {
                reunlock();
            }
        } else {
            try {
                if (tryLock(time, unit)) {
                    try {
                        return NOptional.of(callable.call());
                    } catch (Exception e) {
                        return NOptional.ofError(NMsg.ofC("error call %s", e), e);
                    } finally {
                        unlock();
                    }
                } else {
                    return NOptional.ofEmpty();
                }
            } catch (InterruptedException e) {
                return NOptional.ofError(NMsg.ofC("error call %s", e), e);
            }
        }
    }

    protected abstract void reunlock();

    protected abstract void relock();
}
