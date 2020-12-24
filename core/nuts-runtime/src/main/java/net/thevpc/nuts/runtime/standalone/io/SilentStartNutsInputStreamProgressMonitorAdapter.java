package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsProgressEvent;
import net.thevpc.nuts.NutsProgressMonitor;
import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsLogVerb;

import java.util.logging.Level;

class SilentStartNutsInputStreamProgressMonitorAdapter implements NutsProgressMonitor {
    private final NutsLogger LOG;
    private final NutsProgressMonitor finalMonitor;
    private final String path;

    public SilentStartNutsInputStreamProgressMonitorAdapter(NutsWorkspace ws, NutsProgressMonitor finalMonitor, String path) {
        LOG=ws.log().of(SilentStartNutsInputStreamProgressMonitorAdapter.class);
        this.finalMonitor = finalMonitor;
        this.path = path;
    }

    @Override
    public void onStart(NutsProgressEvent event) {
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
        finalMonitor.onComplete(event);
        if (event.getError() != null) {
            LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.FAIL).log("download failed    : {0}", path);
        } else {
            LOG.with().session(event.getSession()).level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log( "download succeeded : {0}", path);
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        return finalMonitor.onProgress(event);
    }
}
