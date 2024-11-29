package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.env.NBootManager;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.NSession;

import java.io.PrintStream;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class NLogConsoleHandler extends StreamHandler {

    private NPrintStream out;
    private NSession session;

    public NLogConsoleHandler(NPrintStream out, boolean closeable, NSession session) {
        this.session = session;
        setFormatter(new NLogRichFormatter(session, false));
        setOutputStream(out, closeable);
    }

    public synchronized void setOutputStream(NPrintStream out, boolean closable) throws SecurityException {
        flush();
        this.out = out;
        if (closable) {
            super.setOutputStream(out.asPrintStream());
        } else {
            super.setOutputStream(new PrintStream(out.asPrintStream()) {
                @Override
                public void close() {
                    //
                }
            });
        }
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (!super.isLoggable(record)) {
            return false;
        }
        NSession session = NLogUtils.resolveSession(record,this.session);
        if (session.isBot()) {
            return false;
        }
        NLogConfig logConfig = NBootManager.of().getBootOptions().getLogConfig().orElseGet(NLogConfig::new);
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
        return true;
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        out.resetLine();
        super.publish(record);
    }
}
