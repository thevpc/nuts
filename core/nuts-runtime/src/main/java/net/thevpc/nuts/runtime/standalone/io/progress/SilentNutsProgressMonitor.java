package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;

import java.util.logging.Level;

public class SilentNutsProgressMonitor implements NutsProgressMonitor {
    public SilentNutsProgressMonitor() {
    }

    public void onStart(NutsProgressEvent event) {
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        return false;
    }
}
