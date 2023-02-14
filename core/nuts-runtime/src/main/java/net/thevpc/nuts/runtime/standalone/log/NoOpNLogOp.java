package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NLogOp;
import net.thevpc.nuts.util.NLogVerb;

import java.util.function.Supplier;
import java.util.logging.Level;

public class NoOpNLogOp implements NLogOp {
    public static final NoOpNLogOp INSTANCE=new NoOpNLogOp();
    private NoOpNLogOp() {
    }

    @Override
    public NLogOp verb(NLogVerb verb) {
        return this;
    }

    @Override
    public NLogOp error(Throwable error) {
        return null;
    }

    @Override
    public void log(NMsg msg) {

    }

    @Override
    public void log(Supplier<NMsg> msgSupplier) {

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
    public NLogOp session(NSession session) {
        return this;
    }

    @Override
    public boolean isLoggable(Level level) {
        return false;
    }

}
