package net.thevpc.nuts.runtime.standalone.log;


import net.thevpc.nuts.NErr;
import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.PrintStream;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class NLogConsoleHandler implements NLogSPI {
    private DefaultNLogModel model;
    private boolean suspendTerminalMode = false;
    private final LinkedList<Rec> suspendedTerminalRecords = new LinkedList<>();
    private int suspendedMax = 100;
    private static class Rec{
        Instant instant;
        NMsg msg;

        public Rec(Instant instant, NMsg msg) {
            this.instant = instant;
            this.msg = msg;
        }
    }

    public NLogConsoleHandler(DefaultNLogModel model) {
        this.model = model;
    }

    @Override
    public boolean isLoggable(Level level) {
        int levelValue = this.model.getTermLevel() == null ? Level.INFO.intValue() : this.model.getTermLevel().intValue();
        if (!(level.intValue() >= levelValue && levelValue != Level.OFF.intValue())) {
            return false;
        }
        NSession session = NSession.of();
        NLogConfig logConfig = NWorkspace.of().getBootOptions().getLogConfig().orElseGet(NLogConfig::new);
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
        if (!(level.intValue() >= sessionLogLevelValue && sessionLogLevelValue != Level.OFF.intValue())) {
            return false;
        }
        return true;
    }

    @Override
    public synchronized void log(NMsg message) {
        if (!isLoggable(message.getLevel())) {
            return;
        }
        if (suspendTerminalMode) {
            synchronized (suspendedTerminalRecords) {
                suspendedTerminalRecords.add(new Rec(Instant.now(), message));
                if (suspendedTerminalRecords.size() > suspendedMax) {
                    Rec r = suspendedTerminalRecords.removeFirst();
                    log0(r);
                }
            }
        } else {
            log0(new Rec(Instant.now(), message));
        }

    }

    public void suspendTerminal() {
        suspendTerminalMode = true;
    }

    public void resumeTerminal() {
        suspendTerminalMode = false;
        DefaultNLogModel logModel = NWorkspaceExt.of().getModel().logModel;
        synchronized (suspendedTerminalRecords) {
            for (Iterator<Rec> iterator = suspendedTerminalRecords.iterator(); iterator.hasNext(); ) {
                Rec r = iterator.next();
                iterator.remove();
                log0(r);
            }
        }
    }

    private void log0(Rec rec) {
        NErr.resetLine();
        NErr.println(NMsg.ofC("%s %s %s %s", rec.instant, rec.msg.getLevel(), rec.msg.getIntent(), rec.msg));
        if (rec.msg.getThrowable() != null) {
            NErr.println(NStringUtils.stacktrace(rec.msg.getThrowable()));
        }
    }
}
