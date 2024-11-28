package net.thevpc.nuts.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NLogRecord extends LogRecord implements NSessionProvider{
    private NSession session;
    private NMsg nmsg;
    private NLogVerb verb;
    /**
     * duration
     */
    private long time;

    public NLogRecord(NSession session, Level level, NLogVerb verb, NMsg msg, long time, Throwable thrown) {
        super(level, NText.of(msg).filteredText());
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

    public NLogVerb getVerb() {
        return verb;
    }

    public NSession getSession() {
        return session;
    }

    public void setSession(NSession session) {
        this.session = session;
    }

}
