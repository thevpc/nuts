package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsProgressEvent;
import net.thevpc.nuts.NutsProgressMonitor;
import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsLogVerb;

import java.util.logging.Level;
import net.thevpc.nuts.NutsLoggerOp;
import net.thevpc.nuts.NutsSession;

class SilentStartNutsInputStreamProgressMonitorAdapter implements NutsProgressMonitor {
    private NutsLogger LOG;
    private final NutsProgressMonitor finalMonitor;
    private final String path;

    public SilentStartNutsInputStreamProgressMonitorAdapter(NutsWorkspace ws, NutsProgressMonitor finalMonitor, String path) {
        this.finalMonitor = finalMonitor;
        this.path = path;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.log().of(SilentStartNutsInputStreamProgressMonitorAdapter.class);
        }
        return LOG;
    }
    
    @Override
    public void onStart(NutsProgressEvent event) {
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
        finalMonitor.onComplete(event);
        if (event.getError() != null) {
            _LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.FAIL).log("download failed    : {0}", path);
        } else {
            _LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log( "download succeeded : {0}", path);
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        return finalMonitor.onProgress(event);
    }
}
