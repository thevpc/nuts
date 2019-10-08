package net.vpc.app.nuts.core.util.io;

import net.vpc.app.nuts.NutsInputStreamEvent;
import net.vpc.app.nuts.NutsInputStreamProgressMonitor;

public class NutsInputStreamProgressMonitorList implements NutsInputStreamProgressMonitor {
    private NutsInputStreamProgressMonitor[] all;

    public NutsInputStreamProgressMonitorList(NutsInputStreamProgressMonitor[] all) {
        this.all = all;
    }

    @Override
    public void onStart(NutsInputStreamEvent event) {
        for (NutsInputStreamProgressMonitor i : all) {
            i.onStart(event);
        }
    }

    @Override
    public void onComplete(NutsInputStreamEvent event) {
        for (NutsInputStreamProgressMonitor i : all) {
            i.onComplete(event);
        }
    }

    @Override
    public boolean onProgress(NutsInputStreamEvent event) {
        boolean b = false;
        for (NutsInputStreamProgressMonitor i : all) {
            b |= i.onProgress(event);
        }
        return b;
    }
}
