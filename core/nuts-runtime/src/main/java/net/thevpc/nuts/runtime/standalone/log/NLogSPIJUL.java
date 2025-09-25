package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NMsg;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class NLogSPIJUL implements NLogSPI {
    private final Logger log;

    public NLogSPIJUL(String name) {
        this.log = Logger.getLogger(name);
    }

    @Override
    public boolean isLoggable(Level level) {
        return log.isLoggable(level);
    }

    @Override
    public void log(NMsg message) {
        String[] caller = findCaller();
        Instant now = Instant.now();
        NMsg msg2=NMsg.ofC("%s [%-6s] [%-7s] %s%s", now, message.getLevel(), message.getIntent(), message,
                message.getDurationNanos() <= 0 ? ""
                        : NMsg.ofC(" (duration: %s)", NDuration.ofNanos(message.getDurationNanos()))
        );
        LogRecord rec = new LogRecord(message.getLevel(),"{0}");
        rec.setMillis(now.toEpochMilli());
        rec.setThrown(message.getThrowable());
        rec.setParameters(new Object[]{msg2.toString()});
        rec.setSourceClassName(caller[0]);
        rec.setSourceMethodName(caller[1]);
        log.log(rec);
    }

    private String[] findCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // skip the first few frames: getStackTrace(), findCaller(), log(), etc.
        for (int i = 3; i < stack.length; i++) {
            StackTraceElement e = stack[i];
            String cname = e.getClassName();
            // skip internal logging classes
            if (!cname.startsWith("net.thevpc.nuts.runtime.standalone.log") && !cname.startsWith("java.util.logging")) {
                return new String[]{cname, e.getMethodName()};
            }
        }
        return new String[]{"unknown", "unknown"};
    }
}
