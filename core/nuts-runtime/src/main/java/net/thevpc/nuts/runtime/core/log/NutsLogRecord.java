package net.thevpc.nuts.runtime.core.log;

import net.thevpc.nuts.*;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NutsLogRecord extends LogRecord {
    private NutsSession session;
    private NutsMessage nmsg;
    private NutsLogVerb verb;
    /**
     * duration
     */
    private long time;

    public NutsLogRecord(NutsSession session, Level level, NutsLogVerb verb, NutsMessage msg, long time,Throwable thrown) {
        super(level, msg.getMessage());
        this.nmsg = msg;
        this.verb = verb;
        this.session = session;
        this.time = time;
        setParameters(msg.getParams());
        setThrown(thrown);
    }

    public NutsMessage getNutsMessage() {
        return nmsg;
    }

    public long getTime() {
        return time;
    }

    public NutsLogVerb getVerb() {
        return verb;
    }

    public NutsWorkspace getWorkspace() {
        return session==null?null: session.getWorkspace();
    }

    public NutsSession getSession() {
        return session;
    }

    public void setSession(NutsSession session) {
        this.session = session;
    }

//    public NutsLogRecord filter(){
//        if(isFormatted()) {
//            NutsLogRecord r = new NutsLogRecord(session,getLevel(), verb,
//                    NutsTexts.of(session).builder().append(getMessage()).filteredText()
//                    ,getParameters(),false,time,formatStyle);
//            r.setSequenceNumber(this.getSequenceNumber());
//            r.setThreadID(this.getThreadID());
//            r.setMillis(this.getMillis());
//            r.setThrown(this.getThrown());
//            return r;
//        }else{
//            return this;
//        }
//    }
//    public NutsLogRecord escape(){
//        if(isFormatted()) {
//            return this;
//        }else{
//            NutsLogRecord r = new NutsLogRecord(session,getLevel(), verb,
//                    getWorkspace().text().builder().append(getMessage()).toString(),
//                    getParameters(),false,time,formatStyle);
//            r.setSequenceNumber(this.getSequenceNumber());
//            r.setThreadID(this.getThreadID());
//            r.setMillis(this.getMillis());
//            r.setThrown(this.getThrown());
//            return r;
//        }
//    }
}
