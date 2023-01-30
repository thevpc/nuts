package net.thevpc.nuts.util;

import net.thevpc.nuts.*;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NLogRecord extends LogRecord implements NSessionProvider{
    private NSession session;
    private NMsg nmsg;
    private NLoggerVerb verb;
    /**
     * duration
     */
    private long time;

    public NLogRecord(NSession session, Level level, NLoggerVerb verb, NMsg msg, long time, Throwable thrown) {
        super(level, String.valueOf(msg.getMessage()));
        this.nmsg = msg;
        this.verb = verb;
        this.session = session;
        this.time = time;
        setParameters(msg.getParams());
        setThrown(thrown);
    }

    public NMsg getFormattedMessage() {
        return nmsg;
    }

    public long getTime() {
        return time;
    }

    public NLoggerVerb getVerb() {
        return verb;
    }

    public NWorkspace getWorkspace() {
        return session == null ? null : session.getWorkspace();
    }

    public NSession getSession() {
        return session;
    }

    public void setSession(NSession session) {
        this.session = session;
    }

}
