package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.NutsProgressEvent;
import net.vpc.app.nuts.NutsProgressMonitor;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.log.NutsLogVerb;

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
            LOG.log(Level.FINEST, NutsLogVerb.FAIL, "Download Failed    : {0}", path);
        } else {
            LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "Download Succeeded : {0}", path);
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        return finalMonitor.onProgress(event);
    }
}
