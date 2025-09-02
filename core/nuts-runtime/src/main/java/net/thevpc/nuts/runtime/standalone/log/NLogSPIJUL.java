package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.log.NLogRecord;
import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NLogSPIJUL implements NLogSPI {
    private Logger log;

    public NLogSPIJUL(Logger log) {
        this.log = log;
    }

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
        NLogRecord rec=new NLogRecord(NSession.of(),message.getLevel(),message.getIntent(),message,message.toString(),System.currentTimeMillis(),message.getThrowable());
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
