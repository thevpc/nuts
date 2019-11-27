package net.vpc.app.nuts.runtime.log;

import net.vpc.app.nuts.NutsLoggerOp;
import net.vpc.app.nuts.NutsTextFormatStyle;

import java.util.function.Supplier;
import java.util.logging.Level;

public class NoOpNutsLoggerOp implements NutsLoggerOp {
    public static final NoOpNutsLoggerOp INSTANCE=new NoOpNutsLoggerOp();
    private NoOpNutsLoggerOp() {
    }

    @Override
    public NutsLoggerOp verb(String verb) {
        return this;
    }

    @Override
    public NutsLoggerOp error(Throwable error) {
        return null;
    }

    @Override
    public void log(String msg, Object... params) {
    }

    @Override
    public void log(Supplier<String> msgSupplier) {
    }

    @Override
    public NutsLoggerOp formatted(boolean value) {
        return this;
    }

    @Override
    public NutsLoggerOp formatted() {
        return this;
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
    public NutsLoggerOp style(NutsTextFormatStyle style) {
        return this;
    }
}
