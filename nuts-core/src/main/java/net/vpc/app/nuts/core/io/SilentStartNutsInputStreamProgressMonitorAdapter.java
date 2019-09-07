package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.NutsInputStreamEvent;
import net.vpc.app.nuts.NutsInputStreamProgressMonitor;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.log.NutsLogVerb;

import java.util.logging.Level;

class SilentStartNutsInputStreamProgressMonitorAdapter implements NutsInputStreamProgressMonitor {
    private final NutsLogger LOG;
    private final NutsInputStreamProgressMonitor finalMonitor;
    private final String path;

    public SilentStartNutsInputStreamProgressMonitorAdapter(NutsWorkspace ws,NutsInputStreamProgressMonitor finalMonitor, String path) {
        LOG=ws.log().of(SilentStartNutsInputStreamProgressMonitorAdapter.class);
        this.finalMonitor = finalMonitor;
        this.path = path;
    }

    @Override
    public void onStart(NutsInputStreamEvent event) {
    }

    @Override
    public void onComplete(NutsInputStreamEvent event) {
        finalMonitor.onComplete(event);
        if (event.getException() != null) {
            LOG.log(Level.FINEST, NutsLogVerb.ERROR, "Download Failed    : {0}", new Object[]{path});
        } else {
            LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "Download Succeeded : {0}", new Object[]{path});
        }
    }

    @Override
    public boolean onProgress(NutsInputStreamEvent event) {
        return finalMonitor.onProgress(event);
    }
}
