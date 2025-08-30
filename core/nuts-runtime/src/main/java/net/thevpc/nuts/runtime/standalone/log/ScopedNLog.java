package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgBuilder;

import java.util.function.Supplier;
import java.util.logging.Level;

class ScopedNLog implements NLog {
    private final DefaultNLogModel model;
    private final NLog base;

    public ScopedNLog(DefaultNLogModel model, NLog base) {
        this.model = model;
        this.base = base;
    }

    @Override
    public NLog scoped() {
        return this;
    }

    @Override
    public void runWith(Runnable r) {
        r.run();
    }

    @Override
    public <T> T callWith(NCallable<T> r) {
        return r.call();
    }

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public boolean isLoggable(Level level) {
        NLog s = model.scopedLoggerThreadLocal.get();
        if (s != null) {
            return s.isLoggable(level);
        }
        return base.isLoggable(level);
    }

    @Override
    public void log(Level level, Supplier<NMsg> msgSupplier) {
        NLog s = model.scopedLoggerThreadLocal.get();
        if (s != null) {
            s.log(level, msgSupplier);
            return;
        }
        base.log(level, msgSupplier);
    }

    @Override
    public void log(NMsg msg) {
        NLog s = model.scopedLoggerThreadLocal.get();
        if (s != null) {
            s.log(msg);
            return;
        }
        base.log(msg);
    }

    @Override
    public void log(NMsgBuilder msg) {
        log(msg.build());
    }
}
