package net.thevpc.nuts.runtime.core.log;

import net.thevpc.nuts.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class NutsLogConsoleHandler extends StreamHandler {

    private OutputStream out;

    public NutsLogConsoleHandler(PrintStream out, boolean closeable) {
        setOutputStream(out, closeable);
    }

    public synchronized void setOutputStream(OutputStream out, boolean closable) throws SecurityException {
        flush();
        this.out = out;
        if (closable) {
            super.setOutputStream(out);
        } else {
            super.setOutputStream(new PrintStream(out) {
                @Override
                public void close() {
                    //
                }
            });
        }
    }

    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        if (record instanceof NutsLogRecord) {
            NutsLogRecord rr = (NutsLogRecord) record;
            NutsWorkspace ws = rr.getWorkspace();
            NutsTerminalManager tf = ws.term().setSession(
                    rr.getSession() == null ? NutsWorkspaceUtils.defaultSession(ws) : rr.getSession()
            );
            
            if (tf != null && tf.isFormatted(out)) {
                if (!rr.isFormatted()) {
                    record = ((NutsLogRecord) record).escape();
                }
                setFormatter(NutsLogRichFormatter.RICH);
            } else {
                if (rr.isFormatted()) {
                    record = ((NutsLogRecord) record).filter();
                }
                setFormatter(NutsLogPlainFormatter.PLAIN);
            }
        } else {
            setFormatter(NutsLogPlainFormatter.PLAIN);
            setOutputStream(System.err);
        }
        super.publish(record);
//        flush();
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (!super.isLoggable(record)) {
            return false;
        }
        if (record instanceof NutsLogRecord) {
            NutsSession session = ((NutsLogRecord) record).getSession();
            if (session != null) {
                if (session.isBot()) {
                    return false;
                }
                NutsLogConfig logConfig = session.getWorkspace().config().options().getLogConfig();
                Level sessionLogLevel = session.getLogTermLevel();
                if (sessionLogLevel == null) {
                    if (logConfig != null) {
                        sessionLogLevel = logConfig.getLogTermLevel();
                    }
                    if (sessionLogLevel == null) {
                        sessionLogLevel = Level.OFF;
                    }
                }
                final int sessionLogLevelValue = sessionLogLevel.intValue();
                Level recLogLevel = record.getLevel();
                if (recLogLevel.intValue() < sessionLogLevelValue || sessionLogLevelValue == Level.OFF.intValue()) {
                    return false;
                }
                Filter sessionLogFilter = session.getLogTermFilter();
                if (sessionLogFilter == null && logConfig != null) {
                    sessionLogFilter = logConfig.getLogTermFilter();
                }
                if (sessionLogFilter != null) {
                    if (!sessionLogFilter.isLoggable(record)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
