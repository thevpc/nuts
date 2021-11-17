package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;

import java.util.function.Supplier;
import java.util.logging.Level;

public class NoOpNutsLoggerOp implements NutsLoggerOp {
    public static final NoOpNutsLoggerOp INSTANCE=new NoOpNutsLoggerOp();
    private NoOpNutsLoggerOp() {
    }

    @Override
    public NutsLoggerOp verb(NutsLogVerb verb) {
        return this;
    }

    @Override
    public NutsLoggerOp error(Throwable error) {
        return null;
    }

    @Override
    public void log(NutsMessage msg) {

    }

    @Override
    public void log(Supplier<NutsMessage> msgSupplier) {

    }

    @Override
    public NutsLoggerOp time(long time) {
        return this;
    }

    @Override
    public NutsLoggerOp level(Level level) {
        return this;
    }

    @Override
    public NutsLoggerOp session(NutsSession session) {
        return this;
    }
}
