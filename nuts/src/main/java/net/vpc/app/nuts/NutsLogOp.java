package net.vpc.app.nuts;

import java.util.function.Supplier;
import java.util.logging.Level;

public interface NutsLogOp {
    NutsLogOp formatted(boolean value);
    NutsLogOp formatted();
    NutsLogOp withVerb(String verb);
    NutsLogOp withError(Throwable throwable);
    NutsLogOp withTime(long time);
    void log(String msg, Object... params);
    void log(Supplier<String> msgSupplier);
}
