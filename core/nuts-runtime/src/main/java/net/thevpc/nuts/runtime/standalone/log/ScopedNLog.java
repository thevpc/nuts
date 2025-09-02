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
    private NMsg prefix;

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
            s.log(level, ()->prepareMsg(msgSupplier.get()));
            return;
        }
        base.log(level, ()->prepareMsg(msgSupplier.get()));
    }

    @Override
    public void log(NMsg msg) {
        NLog s = model.scopedLoggerThreadLocal.get();
        if (s != null) {
            s.log(prepareMsg(msg));
            return;
        }
        base.log(prepareMsg(msg));
    }

    @Override
    public void log(NMsgBuilder msg) {
        log(msg.build());
    }

    @Override
    public void setPrefix(NMsg prefix) {
        this.prefix = prefix;
    }

    @Override
    public NMsg getPrefix() {
        return prefix;
    }

    private NMsg prepareMsg(NMsg other) {
        if (other != null) {
            if (prefix != null) {
                return NMsg.ofC("%s %s", prefix, other)
                        .withLevel(other.getLevel())
                        .withIntent(other.getIntent())
                        .withDurationNanos(other.getDurationNanos())
                        .withThrowable(other.getThrowable())
                        ;
            } else {
                return other;
            }
        } else {
            if (prefix != null) {
                return other;
            } else {
                return NMsg.ofBlank();
            }
        }
    }
}
