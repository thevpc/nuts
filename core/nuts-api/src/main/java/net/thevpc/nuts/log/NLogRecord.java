package net.thevpc.nuts.log;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NSessionProvider;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * NLogRecord class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NLogRecord extends LogRecord implements NSessionProvider {
    private NSession session;
    private NMsg nmsg;
    private NMsgIntent verb;
    /**
     * duration
     */
    private long time;

    /**
     * N log record.
     *
     * @param session session
     * @param level level
     * @param verb verb
     * @param msg msg
     * @param filteredText filtered text
     * @param time time
     * @param thrown thrown
     * @return n log record result
     */
    public NLogRecord(NSession session, Level level, NMsgIntent verb, NMsg msg, String filteredText, long time, Throwable thrown) {
        super(level, filteredText);
        this.nmsg = msg;
        this.verb = verb;
        this.session = session;
        this.time = time;
        setParameters(msg.params());
        setThrown(thrown);
    }

    /**
     * Formatted message.
     *
     * @return formatted message result
     */
    @NGetter
    public NMsg formattedMessage() {
        return nmsg;
    }

    /**
     * Time.
     *
     * @return time result
     */
    @NGetter
    public long time() {
        return time;
    }

    /**
     * Verb.
     *
     * @return verb result
     */
    @NGetter
    public NMsgIntent verb() {
        return verb;
    }

    /**
     * Session.
     *
     * @return session result
     */
    @NGetter
    public NSession session() {
        return session;
    }

    /**
     * Session.
     *
     * @param session session
     */
    @NSetter
    public void session(NSession session) {
        this.session = session;
    }

}
