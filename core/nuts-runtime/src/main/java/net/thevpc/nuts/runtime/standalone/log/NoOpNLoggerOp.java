package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NLoggerOp;
import net.thevpc.nuts.util.NLoggerVerb;

import java.util.function.Supplier;
import java.util.logging.Level;

public class NoOpNLoggerOp implements NLoggerOp {
    public static final NoOpNLoggerOp INSTANCE=new NoOpNLoggerOp();
    private NoOpNLoggerOp() {
    }

    @Override
    public NLoggerOp verb(NLoggerVerb verb) {
        return this;
    }

    @Override
    public NLoggerOp error(Throwable error) {
        return null;
    }

    @Override
    public void log(NMsg msg) {

    }

    @Override
    public void log(Supplier<NMsg> msgSupplier) {

    }

    @Override
    public NLoggerOp time(long time) {
        return this;
    }

    @Override
    public NLoggerOp level(Level level) {
        return this;
    }

    @Override
    public NLoggerOp session(NSession session) {
        return this;
    }

    @Override
    public boolean isLoggable(Level level) {
        return false;
    }

}
