package net.vpc.app.nuts;

import java.util.function.Supplier;
import java.util.logging.Level;

public interface NutsLoggerOp {
    NutsLoggerOp formatted(boolean value);

    NutsLoggerOp formatted();

    NutsLoggerOp verb(String verb);

    NutsLoggerOp error(Throwable throwable);

    NutsLoggerOp time(long time);

    NutsLoggerOp level(Level level);

    NutsLoggerOp style(NutsTextFormatStyle style);

    void log(String msg, Object... params);

    void log(Supplier<String> msgSupplier);
}
