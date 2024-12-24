package net.thevpc.nuts.log;

import net.thevpc.nuts.util.NMsg;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NullNLog implements NLog{
    public static final NLog NULL=new NullNLog();
    private static final NullNLogOp NULLOP=new NullNLogOp();

    @Override
    public boolean isLoggable(Level level) {
        return false;
    }

    @Override
    public void log(Level level, NLogVerb verb, NMsg msg, Throwable thrown) {

    }

    @Override
    public void log(Level level, NLogVerb verb, Supplier<NMsg> msgSupplier, Supplier<Throwable> errorSupplier) {

    }

    @Override
    public NLogOp with() {
        return NULLOP;
    }

    @Override
    public void log(LogRecord record) {

    }

    @Override
    public void error(NMsg msg) {
        NLog.super.error(msg);
    }

    @Override
    public void error(NMsg msg, Throwable thrown) {
        NLog.super.error(msg, thrown);
    }

    @Override
    public void warn(NMsg msg) {
        NLog.super.warn(msg);
    }

    @Override
    public void debug(NMsg msg) {
        NLog.super.debug(msg);
    }

    @Override
    public void info(NMsg msg) {
        NLog.super.info(msg);
    }

    @Override
    public void warn(NMsg msg, Throwable thrown) {
        NLog.super.warn(msg, thrown);
    }

    @Override
    public void log(Level level, NLogVerb verb, NMsg msg) {
        NLog.super.log(level, verb, msg);
    }

    private static class NullNLogOp implements NLogOp {
        @Override
        public NLogOp verb(NLogVerb verb) {
            return this;
        }

        @Override
        public NLogOp error(Throwable error) {
            return this;
        }

        @Override
        public NLogOp time(long time) {
            return this;
        }

        @Override
        public NLogOp level(Level level) {
            return this;
        }

        @Override
        public void log(NMsg msg) {

        }

        @Override
        public void log(Supplier<NMsg> msgSupplier) {

        }

        @Override
        public boolean isLoggable(Level level) {
            return false;
        }
    }
}
