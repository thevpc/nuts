package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogRecord;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultNLogOp implements NLogOp {
    private DefaultNLog logger;
    private Level level;
    private NLogVerb verb;
    private NMsg msg;
    private long time;
    private Supplier<NMsg> msgSupplier;
    private Throwable error;

    public DefaultNLogOp(DefaultNLog logger) {
        this.logger = logger;
    }

    @Override
    public NLogOp verb(NLogVerb verb) {
        this.verb = verb;
        return this;
    }

    @Override
    public NLogOp level(Level level) {
        this.level = level;
        return this;
    }

    @Override
    public NLogOp error(Throwable error) {
        this.error = error;
        return this;
    }

    @Override
    public void log(NMsg message) {
        this.msg = message;
        run();
    }

    @Override
    public void log(Supplier<NMsg> msgSupplier) {
        this.msgSupplier = msgSupplier;
        run();
    }

    @Override
    public NLogOp time(long time) {
        this.time = time;
        return this;
    }

    private void run() {
        Level level = this.level;
        if (level == null && msg != null) {
            level = msg.getLevel();
        }
        if (level == null) {
            level = Level.FINE;
        }
        if (logger.isLoggable(level)) {
            NMsg m = msg;
            if (msgSupplier != null) {
                m = msgSupplier.get();
            }
            NLogRecord record = new NLogRecord(
                    NSession.of(),
                    level,
                    verb,
                    m,
                    NLogUtils.filterLogText(m),
                    time,
                    error
            );
            logger.log(record);
        }
    }

    @Override
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }
}
