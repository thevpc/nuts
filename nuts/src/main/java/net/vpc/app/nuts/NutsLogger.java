package net.vpc.app.nuts;

import java.util.function.Supplier;
import java.util.logging.*;

public interface NutsLogger {

    boolean isLoggable(Level level);

    void log(Level level, String msg, Throwable thrown);

    void log(Level level, String msg);

    void log(Level level, Supplier<String> msgSupplier);

    void log(Level level, String msg, Object params);

    void log(Level level, String msg, Object[] params);

    /**
     * Log a LogRecord.
     * <p>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
    void log(LogRecord record);
}
