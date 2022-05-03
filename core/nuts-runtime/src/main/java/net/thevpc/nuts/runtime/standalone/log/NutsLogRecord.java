package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NutsLogRecord extends LogRecord {
    private NutsSession session;
    private NutsMessage nmsg;
    private NutsLoggerVerb verb;
    /**
     * duration
     */
    private long time;

    public NutsLogRecord(NutsSession session, Level level, NutsLoggerVerb verb, NutsMessage msg, long time, Throwable thrown) {
        super(level, String.valueOf(msg.getMessage()));
        this.nmsg = msg;
        this.verb = verb;
        this.session = session;
        this.time = time;
        setParameters(msg.getParams());
        setThrown(thrown);
    }

    public NutsMessage getFormattedMessage() {
        return nmsg;
    }

    public long getTime() {
        return time;
    }

    public NutsLoggerVerb getVerb() {
        return verb;
    }

    public NutsWorkspace getWorkspace() {
        return session == null ? null : session.getWorkspace();
    }

    public NutsSession getSession() {
        return session;
    }

    public void setSession(NutsSession session) {
        this.session = session;
    }

}
