package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsTextFormatStyle;
import net.vpc.app.nuts.NutsWorkspace;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NutsLogRecord extends LogRecord {
    private NutsWorkspace workspace;
    private NutsSession session;
    private String verb;
    private boolean formatted;
    private long time;
    private NutsTextFormatStyle formatStyle = NutsTextFormatStyle.POSITIONAL;

    public NutsLogRecord(NutsWorkspace ws, NutsSession session, Level level, String verb, String msg, Object[] objects, boolean formatted, long time, NutsTextFormatStyle style) {
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

    public String getVerb() {
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
}
