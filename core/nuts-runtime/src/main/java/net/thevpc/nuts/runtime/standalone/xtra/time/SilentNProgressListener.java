package net.thevpc.nuts.runtime.standalone.xtra.time;

import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;

public class SilentNProgressListener implements NProgressListener {
    public SilentNProgressListener() {
    }

    @Override
    public boolean onProgress(NProgressEvent event) {
        return false;
    }
}
