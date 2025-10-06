package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogContext;
import net.thevpc.nuts.log.NLogs;
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
    public String getName() {
        return base.getName();
    }

    @Override
    public boolean isLoggable(Level level) {
        NLogContext c = NLogs.of().getContext();
        NLog s = c.getLog();
        if (s != null) {
            return s.isLoggable(level);
        }
        return base.isLoggable(level);
    }

    @Override
    public void log(Level level, Supplier<NMsg> msgSupplier) {
        NLogContext c = NLogs.of().getContext();
        NLog s = c.getLog();
        if (s != null) {
            s.log(level, () -> prepareMsg(msgSupplier.get(), c));
            return;
        }
        base.log(level, msgSupplier);
    }

    @Override
    public void log(NMsg msg) {
        NLogContext c = NLogs.of().getContext();
        NLog s = c.getLog();
        if (s != null) {
            s.log(msg);
//            s.log(prepareMsg(msg, c));
            return;
        }
        base.log(msg);
    }

    @Override
    public void log(NMsgBuilder msg) {
        log(msg.build());
    }

    private NMsg prepareMsg(NMsg other, NLogContext c) {
        return other.withPrefix(c.getMessagePrefix()).withSuffix(c.getMessageSuffix()).withPlaceholders(c::getPlaceholder);
    }

}
