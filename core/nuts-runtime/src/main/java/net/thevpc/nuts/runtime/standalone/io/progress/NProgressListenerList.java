package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NProgressEvent;
import net.thevpc.nuts.util.NProgressListener;

public class NProgressListenerList implements NProgressListener {
    private NProgressListener[] all;

    public NProgressListenerList(NProgressListener[] all) {
        this.all = all;
    }

    @Override
    public boolean onProgress(NProgressEvent event) {
        boolean b = false;
        for (NProgressListener i : all) {
            b |= i.onProgress(event);
        }
        return b;
    }
}
