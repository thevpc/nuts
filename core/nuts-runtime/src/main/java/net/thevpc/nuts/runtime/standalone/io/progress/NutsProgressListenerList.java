package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NutsProgressEvent;
import net.thevpc.nuts.util.NutsProgressListener;

public class NutsProgressListenerList implements NutsProgressListener {
    private NutsProgressListener[] all;

    public NutsProgressListenerList(NutsProgressListener[] all) {
        this.all = all;
    }

    @Override
    public void onStart(NutsProgressEvent event) {
        for (NutsProgressListener i : all) {
            i.onStart(event);
        }
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
        for (NutsProgressListener i : all) {
            i.onComplete(event);
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        boolean b = false;
        for (NutsProgressListener i : all) {
            b |= i.onProgress(event);
        }
        return b;
    }
}
