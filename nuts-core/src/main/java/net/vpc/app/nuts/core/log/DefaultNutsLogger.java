package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsLogOp;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsLogger;

import java.util.function.Supplier;
import java.util.logging.*;

public class DefaultNutsLogger implements NutsLogger {
    private NutsWorkspace workspace;
    private NutsSession session;
    private boolean formatted;
    private Logger log;
    private static final int offValue = Level.OFF.intValue();

    public DefaultNutsLogger(NutsWorkspace workspace, Class log) {
        this(workspace, log.getName());
    }

    public DefaultNutsLogger(NutsWorkspace workspace, String log) {
        this(workspace, Logger.getLogger(log));
    }

    public DefaultNutsLogger(NutsWorkspace workspace, Logger log) {
        this.workspace = workspace;
        this.log = log;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsSession getSession() {
        return session;
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
        if(isLoggable(level, workspace.log().getTermLevel())){
            return true;
        }
        if(isLoggable(level, workspace.log().getFileLevel())){
            return true;
        }
        for (Handler handler : workspace.log().getHandlers()) {
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
        LogRecord lr = new NutsLogRecord(workspace, session,level,NutsLogVerb.FAIL, msg,DefaultNutsLogOp.OBJECTS0,formatted);
        lr.setThrown(thrown);
        doLog(lr);
    }

    public void log(Level level, String verb, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new NutsLogRecord(workspace, session,level,verb, msg,DefaultNutsLogOp.OBJECTS0,formatted);
        doLog(lr);
    }

    public void log(Level level, String verb, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new NutsLogRecord(workspace, session,level, verb,msgSupplier.get(),DefaultNutsLogOp.OBJECTS0,formatted);
        doLog(lr);
    }

    public void log(Level level, String verb, String msg, Object params) {
        log(level, verb, msg, new Object[]{params});
    }

    public void log(Level level, String verb, String msg, Object[] params) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new NutsLogRecord(workspace, null,level, verb,msg,params,false);
        lr.setParameters(params);
        doLog(lr);
    }

    // private support method for logging.
    private void doLog(LogRecord lr) {
        lr.setLoggerName(log.getName());
        //ignore resource bundling...
        log(lr);
    }

    @Override
    public NutsLogOp withLevel(Level level) {
        if (!isLoggable(level)) {
            return NoOpNutsLogOp.INSTANCE;
        }
        return new DefaultNutsLogOp(this,level);
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
        DefaultNutsLogManager logManager = (DefaultNutsLogManager) workspace.log();
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
