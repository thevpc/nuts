package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsLogger;

import java.util.function.Supplier;
import java.util.logging.*;

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

    public Filter getFilter() {
        //may have a filter ?
        return null;
    }

    private boolean isLoggable(Level level,Level current) {
        int levelValue = current.intValue();
        if (!(level.intValue() < levelValue || levelValue == offValue)) {
            return true;
        }
        return false;
    }
    public boolean isLoggable(Level level) {
        if(isLoggable(level,ws.log().getTermLevel())){
            return true;
        }
        if(isLoggable(level,ws.log().getFileLevel())){
            return true;
        }
        for (Handler handler : ws.log().getHandlers()) {
            if(isLoggable(level,handler.getLevel())){
                return true;
            }
        }

        if(log.isLoggable(level)){
            return true;
        }
        return false;
    }

    public void log(Level level, String msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new NutsLogRecord(ws, level, msg);
        lr.setThrown(thrown);
        doLog(lr);
    }

    public void log(Level level, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new NutsLogRecord(ws, level, msg);
        doLog(lr);
    }

    public void log(Level level, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new NutsLogRecord(ws, level, msgSupplier.get());
        doLog(lr);
    }

    public void log(Level level, String msg, Object params) {
        log(level, msg, new Object[]{params});
    }

    public void log(Level level, String msg, Object[] params) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new NutsLogRecord(ws, level, msg);
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
        DefaultNutsLogManager logManager = (DefaultNutsLogManager)ws.log();
        logManager.updateHandlers(record);
        Handler ch = logManager.getTermHandler();
        if (ch != null) {
            if(ch.isLoggable(record)) {
                ch.publish(record);
                ch.flush();
            }
        }
        Handler fh = logManager.getFileHandler();
        if (fh != null) {
            if(fh.isLoggable(record)) {
                fh.publish(record);
            }
        }
        for (Handler handler : logManager.getHandlers()) {
            if(handler.isLoggable(record)) {
                handler.publish(record);
            }
        }
        this.log.log(record);
    }
}
