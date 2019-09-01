package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsLogConfig;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsLogger;

import java.util.function.Supplier;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DefaultNutsLogger implements NutsLogger {
    private NutsWorkspace ws;
    private Logger log;
    private static final int offValue = Level.OFF.intValue();

    public DefaultNutsLogger(NutsWorkspace ws, Class log) {
        this(ws, log.getName());
    }

    public DefaultNutsLogger(NutsWorkspace ws, String log) {
        this(ws, Logger.getLogger(log));
    }

    public DefaultNutsLogger(NutsWorkspace ws, Logger log) {
        this.ws = ws;
        this.log = log;
    }

    public int getLogLevelValue() {
        return getLogLevel().intValue();
    }

    public Filter getFilter() {
        //may have a filter ?
        return null;
    }

    public Level getLogLevel() {
        NutsLogConfig l = ws.config().options().getLogConfig();
        if (l != null) {
            Level lv = l.getLogLevel();
            if (lv != null) {
                return lv;
            }
        }
        return Level.OFF;
    }

    public boolean isLoggable(Level level) {
        int levelValue = getLogLevelValue();
        if (level.intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        return log.isLoggable(level);
    }

    public void log(Level level, String msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setThrown(thrown);
        doLog(lr);
    }

    public void log(Level level, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        doLog(lr);
    }

    public void log(Level level, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        doLog(lr);
    }

    public void log(Level level, String msg, Object params) {
        log(level,msg,new Object[]{params});
    }

    public void log(Level level, String msg, Object[] params) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setParameters(params);
        doLog(lr);
    }

    // private support method for logging.
    private void doLog(LogRecord lr) {
        lr.setLoggerName(log.getName());
        //ignore resource bundling...
        log(lr);
    }

    /**
     * Log a LogRecord.
     * <p>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
    public void log(LogRecord record) {
        if (!isLoggable(record.getLevel())) {
            return;
        }
        Filter theFilter = getFilter();
        if (theFilter != null && !theFilter.isLoggable(record)) {
            return;
        }
        log.log(record);
    }
}
