package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NProgressEvent;
import net.thevpc.nuts.util.NProgressListener;

public class SilentNProgressListener implements NProgressListener {
    public SilentNProgressListener() {
    }

    @Override
    public boolean onProgress(NProgressEvent event) {
        return false;
    }
}
