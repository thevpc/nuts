package net.thevpc.nuts.runtime.core.log;

import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextFormatStyle;
import net.thevpc.nuts.NutsWorkspace;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NutsLogRecord extends LogRecord {
    private NutsWorkspace workspace;
    private NutsSession session;
    private NutsLogVerb verb;
    private boolean formatted;
    private long time;
    private NutsTextFormatStyle formatStyle = NutsTextFormatStyle.JSTYLE;

    public NutsLogRecord(NutsWorkspace ws, NutsSession session, Level level, NutsLogVerb verb, String msg, Object[] objects, boolean formatted, long time, NutsTextFormatStyle style) {
        super(level, msg);
        this.verb = verb;
        this.workspace = ws;
        this.session = session;
        this.formatted = formatted;
        this.time = time;
        this.formatStyle = style == null ? NutsTextFormatStyle.CSTYLE : style;
        setParameters(objects);
    }

    public NutsTextFormatStyle getFormatStyle() {
        return formatStyle;
    }

    public long getTime() {
        return time;
    }

    public boolean isFormatted() {
        return formatted;
    }

    public NutsLogVerb getVerb() {
        return verb;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    public NutsSession getSession() {
        return session;
    }

    public void setSession(NutsSession session) {
        this.session = session;
    }

    public NutsLogRecord filter(){
        if(isFormatted()) {
            NutsLogRecord r = new NutsLogRecord(workspace, session,getLevel(), verb, workspace.formats().text().filterText(getMessage()),getParameters(),false,time,formatStyle);
            r.setSequenceNumber(this.getSequenceNumber());
            r.setThreadID(this.getThreadID());
            r.setMillis(this.getMillis());
            r.setThrown(this.getThrown());
            return r;
        }else{
            return this;
        }
    }
    public NutsLogRecord escape(){
        if(isFormatted()) {
            return this;
        }else{
            NutsLogRecord r = new NutsLogRecord(workspace, session,getLevel(), verb, workspace.formats().text().escapeText(getMessage()),getParameters(),false,time,formatStyle);
            r.setSequenceNumber(this.getSequenceNumber());
            r.setThreadID(this.getThreadID());
            r.setMillis(this.getMillis());
            r.setThrown(this.getThrown());
            return r;
        }
    }
}
