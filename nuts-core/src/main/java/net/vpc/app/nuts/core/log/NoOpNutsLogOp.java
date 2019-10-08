package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsLogOp;

import java.util.function.Supplier;
import java.util.logging.Level;

public class NoOpNutsLogOp implements NutsLogOp {
    public static final NoOpNutsLogOp INSTANCE=new NoOpNutsLogOp();
    private NoOpNutsLogOp() {
    }

    @Override
    public NutsLogOp withVerb(String verb) {
        return this;
    }

    @Override
    public NutsLogOp withError(Throwable throwable) {
        return null;
    }

    @Override
    public void log(String msg, Object... params) {
    }

    @Override
    public void log(Supplier<String> msgSupplier) {
    }

    @Override
    public NutsLogOp formatted(boolean value) {
        return this;
    }

    @Override
    public NutsLogOp formatted() {
        return this;
    }
}
