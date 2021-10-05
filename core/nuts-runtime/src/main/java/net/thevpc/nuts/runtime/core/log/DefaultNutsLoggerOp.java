package net.thevpc.nuts.runtime.core.log;

import net.thevpc.nuts.*;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultNutsLoggerOp implements NutsLoggerOp {
    private NutsSession session;
    private DefaultNutsLogger logger;
    private Level level = Level.FINE;
    private NutsLogVerb verb;
    private NutsMessage msg;
    private long time;
    private Supplier<NutsMessage> msgSupplier;
    private Throwable error;

    public DefaultNutsLoggerOp(DefaultNutsLogger logger) {
        this.logger = logger;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsLoggerOp session(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsLoggerOp verb(NutsLogVerb verb) {
        this.verb = verb;
        return this;
    }

    @Override
    public NutsLoggerOp level(Level level) {
        this.level = level == null ? Level.FINE : level;
        return this;
    }

    @Override
    public NutsLoggerOp error(Throwable error) {
        this.error = error;
        return this;
    }

    @Override
    public void log(NutsMessage message) {
        this.msg = message;
        run();
    }

    @Override
    public void log(Supplier<NutsMessage> msgSupplier) {
        this.msgSupplier = msgSupplier;
        run();
    }

    @Override
    public NutsLoggerOp time(long time) {
        this.time = time;
        return this;
    }

    private void run() {
        if (logger.isLoggable(level)) {
            NutsMessage m = msg;
            if (msgSupplier != null) {
                m = msgSupplier.get();
            }
            NutsSession s=session;
            if(s==null){
                s=logger.getSession();
            }
            NutsLogRecord record = new NutsLogRecord(
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
}
