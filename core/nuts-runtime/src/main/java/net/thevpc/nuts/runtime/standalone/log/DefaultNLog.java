package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLogs;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogRecord;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.logging.*;

public class DefaultNLog implements NLog {
    private NWorkspace workspace;
    private long defaultTime;
    private Logger log;
    private static final int offValue = Level.OFF.intValue();
    private LinkedList<LogRecord> suspendedTerminalRecords = new LinkedList<>();
    private int suspendedMax = 100;
    private boolean suspendTerminalMode = false;

    public DefaultNLog(NWorkspace workspace, Class<?> log, boolean suspended) {
        this(workspace, log.getName());
        if(suspended){
            suspendTerminal();
        }
    }

    public DefaultNLog(NWorkspace workspace, Class<?> log) {
        this(workspace, log.getName());
    }

    public DefaultNLog(NWorkspace workspace, String log) {
        this(workspace, Logger.getLogger(log));
    }

    public DefaultNLog(NWorkspace workspace, Logger log) {
        this.workspace = workspace;
        this.log = log;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    public Filter getFilter() {
        //may have a filter ?
        return null;
    }

    private boolean isLoggable(Level level, Level current) {
        int levelValue = current.intValue();
        if (!(level.intValue() < levelValue || levelValue == offValue)) {
            return true;
        }
        return false;
    }

    public boolean isLoggable(Level level) {
        if (isLoggable(level, NLog.getTermLevel())) {
            return true;
        }
        if (isLoggable(level, NLog.getFileLevel())) {
            return true;
        }
        for (Handler handler : NLog.getHandlers()) {
            if (isLoggable(level, handler.getLevel())) {
                return true;
            }
        }

        if (log.isLoggable(level)) {
            return true;
        }
        return false;
    }

    public void log(Level level, NLogVerb verb, NMsg msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        NSession session=workspace.currentSession();
        doLog(new NLogRecord(session, level, verb, msg, defaultTime,thrown));
    }

    public void log(Level level, NLogVerb verb, Supplier<NMsg> msgSupplier, Supplier<Throwable> errorSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        NSession session=workspace.currentSession();
        doLog(new NLogRecord(session, level, verb, msgSupplier==null?null:msgSupplier.get(), defaultTime,
                errorSupplier==null?null:errorSupplier.get()
        ));
    }

    // private support method for logging.
    private void doLog(LogRecord record) {
        record.setLoggerName(log.getName());
        //ignore resource bundling...
        if (!isLoggable(record)) {
            return;
        }
        log0(record);
    }

    @Override
    public NLogOp with() {
        return new DefaultNLogOp(workspace,this);
    }

    private boolean isLoggable(LogRecord record) {
        Filter theFilter = getFilter();
        if (theFilter != null && !theFilter.isLoggable(record)) {
            return false;
        }
        return true;
    }

    public void log(LogRecord record) {
        if (!isLoggable(record.getLevel())) {
            return;
        }
        if (!isLoggable(record)) {
            return;
        }
        //compile for once the parameter based text!
        //record=NutsLogRichFormatter.compile(record);
        log0(record);
    }

    public void suspendTerminal() {
        suspendTerminalMode = true;
    }


    /**
     * Log a LogRecord.
     * <br>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
    private void log0(LogRecord record) {
        DefaultNLogs logManager = (DefaultNLogs) NLogs.of();
        logManager.getModel().updateHandlers(record);
        Handler ch = logManager.getTermHandler();
        if (ch != null) {
            if (ch.isLoggable(record)) {
                if (suspendTerminalMode) {
                    suspendedTerminalRecords.add(record);
                    if (suspendedTerminalRecords.size() > suspendedMax) {
                        LogRecord r = suspendedTerminalRecords.removeFirst();
                        logManager.getModel().updateTermHandler(r);
                        ch.publish(r);
                        ch.flush();
                    }
                } else {
                    ch.publish(record);
                    ch.flush();
                }
            }
        }
        Handler fh = logManager.getModel().getFileHandler();
        if (fh != null) {
            if (fh.isLoggable(record)) {
                fh.publish(record);
            }
        }
        for (Handler handler : logManager.getModel().getHandlers()) {
            if (handler.isLoggable(record)) {
                handler.publish(record);
            }
        }
        this.log.log(record);
    }

    public void resumeTerminal() {
        suspendTerminalMode = false;
        Handler ch = NLog.getTermHandler();
        DefaultNLogs logManager = (DefaultNLogs) NLogs.of();
        for (Iterator<LogRecord> iterator = suspendedTerminalRecords.iterator(); iterator.hasNext(); ) {
            LogRecord r = iterator.next();
            iterator.remove();
            logManager.getModel().updateHandlers(r);
            if (ch != null) {
                ch.publish(r);
                ch.flush();
            }
        }
        if (ch != null) {
            ch.flush();
        }
    }
}
