package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;

import java.util.logging.Level;

class SilentStartNutsInputStreamProgressMonitorAdapter implements NutsProgressMonitor {
    private NutsLogger LOG;
    private final NutsProgressMonitor finalMonitor;
    private final String path;

    public SilentStartNutsInputStreamProgressMonitorAdapter(NutsProgressMonitor finalMonitor, String path) {
        this.finalMonitor = finalMonitor;
        this.path = path;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(SilentStartNutsInputStreamProgressMonitorAdapter.class,session);
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
            _LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("download failed    : {0}", path));
        } else {
            _LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.SUCCESS)
                    .log(NutsMessage.jstyle( "download succeeded : {0}", path));
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        return finalMonitor.onProgress(event);
    }
}
