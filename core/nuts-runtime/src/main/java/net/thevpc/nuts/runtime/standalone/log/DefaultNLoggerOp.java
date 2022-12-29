package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NLogRecord;
import net.thevpc.nuts.util.NLoggerOp;
import net.thevpc.nuts.util.NLoggerVerb;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultNLoggerOp implements NLoggerOp {
    private NSession session;
    private DefaultNLogger logger;
    private Level level = Level.FINE;
    private NLoggerVerb verb;
    private NMsg msg;
    private long time;
    private Supplier<NMsg> msgSupplier;
    private Throwable error;

    public DefaultNLoggerOp(DefaultNLogger logger) {
        this.logger = logger;
    }

    public NSession getSession() {
        return session;
    }

    @Override
    public NLoggerOp session(NSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NLoggerOp verb(NLoggerVerb verb) {
        this.verb = verb;
        return this;
    }

    @Override
    public NLoggerOp level(Level level) {
        this.level = level == null ? Level.FINE : level;
        return this;
    }

    @Override
    public NLoggerOp error(Throwable error) {
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
    public NLoggerOp time(long time) {
        this.time = time;
        return this;
    }

    private void run() {
        if (logger.isLoggable(level)) {
            NMsg m = msg;
            if (msgSupplier != null) {
                m = msgSupplier.get();
            }
            NSession s=session;
            if(s==null){
                s=logger.getSession();
            }
            NLogRecord record = new NLogRecord(
                    s,
                    level,
                    verb,
                    m,
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
