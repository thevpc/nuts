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
    public String getName() {
        return log.getName();
    }

    @Override
    public boolean isLoggable(Level level) {
        return log.isLoggable(level);
    }

    @Override
    public void log(NMsg message) {
        NLogRecord rec=new NLogRecord(NSession.of(),message.getLevel(),message.getIntent(),message,message.toString(),System.currentTimeMillis(),message.getThrowable());
        log.log(rec);
    }
}
