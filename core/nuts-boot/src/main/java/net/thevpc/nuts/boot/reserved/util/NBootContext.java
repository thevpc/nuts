package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootException;

import java.util.concurrent.Callable;

public class NBootContext {
    public static InheritableThreadLocal<NBootContext> curr = new InheritableThreadLocal<>();
    public NBootLog log;
    public int bootConnectionTimout;
    private NBootCache cache = new NBootCache();


    public static NBootLog log() {
        return context().log;
    }
    public static NBootCache cache() {
        return context().cache;
    }

    public static NBootContext context() {
        return curr.get();
    }

    public void runWith(Runnable r) {
        NBootContext old = curr.get();
        try {
            curr.set(this);
            r.run();
        }finally {
            curr.set(old);
        }
    }

    public <T> T callWith(Callable<T> r) {
        NBootContext old = curr.get();
        curr.set(this);
        try {
            return r.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new NBootException(NBootMsg.ofC("unable to call %s : error =%s", r, e), e);
        }finally {
            curr.set(old);
        }
    }
}
