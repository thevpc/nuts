package net.thevpc.nuts.log;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NSessionProvider;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NLogRecord extends LogRecord implements NSessionProvider {
    private NSession session;
    private NMsg nmsg;
    private NMsgIntent verb;
    /**
     * duration
     */
    private long time;

    public NLogRecord(NSession session, Level level, NMsgIntent verb, NMsg msg, String filteredText, long time, Throwable thrown) {
        super(level, filteredText);
        this.nmsg = msg;
        this.verb = verb;
        this.session = session;
        this.time = time;
        setParameters(msg.getParams());
        setThrown(thrown);
    }

    @NGetter
    public NMsg formattedMessage() {
        return nmsg;
    }

    @NGetter
    public long time() {
        return time;
    }

    @NGetter
    public NMsgIntent getVerb() {
        return verb;
    }

    @NGetter
    public NSession session() {
        return session;
    }

    @NSetter
    public void session(NSession session) {
        this.session = session;
    }

}
