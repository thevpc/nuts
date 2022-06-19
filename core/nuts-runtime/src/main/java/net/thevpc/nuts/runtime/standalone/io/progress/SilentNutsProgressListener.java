package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NutsProgressEvent;
import net.thevpc.nuts.util.NutsProgressListener;

public class SilentNutsProgressListener implements NutsProgressListener {
    public SilentNutsProgressListener() {
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        return false;
    }
}
