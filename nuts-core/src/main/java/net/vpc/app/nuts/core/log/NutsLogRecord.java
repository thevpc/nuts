package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NutsLogRecord extends LogRecord {
    private NutsWorkspace workspace;
    private NutsSession session;

    public NutsLogRecord(NutsSession session,Level level, String msg) {
        super(level, msg);
        this.session=session;
        this.workspace=session.getWorkspace();
    }
    public NutsLogRecord(NutsWorkspace ws,Level level, String msg) {
        super(level, msg);
        this.workspace=ws;
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
