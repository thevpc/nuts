package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

public class NutsLogConsoleHandler extends ConsoleHandler {
    public NutsLogConsoleHandler() {
    }

    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        if (record instanceof NutsLogRecord) {
            NutsLogRecord rr = (NutsLogRecord) record;
            NutsSession session = rr.getSession();
            NutsWorkspace ws = rr.getWorkspace();
            PrintStream out = null;
            if (session != null) {
                out = session.out();
            } else {
                out = ws.io().terminal().out();
            }
            setOutputStream(out);
            if (ws.io().terminalFormat().isFormatted(out)) {
                setFormatter(NutsLogRichFormatter.RICH);
            } else {
                setFormatter(NutsLogPlainFormatter.PLAIN);
            }
        } else {
            setOutputStream(System.err);
        }
        super.publish(record);
    }
}
