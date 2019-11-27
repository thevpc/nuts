package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.NutsProgressEvent;
import net.vpc.app.nuts.NutsProgressMonitor;

public class NutsProgressMonitorList implements NutsProgressMonitor {
    private NutsProgressMonitor[] all;

    public NutsProgressMonitorList(NutsProgressMonitor[] all) {
        this.all = all;
    }

    @Override
    public void onStart(NutsProgressEvent event) {
        for (NutsProgressMonitor i : all) {
            i.onStart(event);
        }
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
        for (NutsProgressMonitor i : all) {
            i.onComplete(event);
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        boolean b = false;
        for (NutsProgressMonitor i : all) {
            b |= i.onProgress(event);
        }
        return b;
    }
}
