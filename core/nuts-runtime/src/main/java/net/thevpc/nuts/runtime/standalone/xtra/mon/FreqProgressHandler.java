package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.*;

import java.util.logging.Level;

public class FreqProgressHandler implements NutsProgressHandler {
    private long freq;
    private long lastMessageTime;
    private long lastProgressTime;
    private Level level = Level.INFO;
    private NutsProgressMonitor delegate;

    public FreqProgressHandler(NutsProgressMonitor delegate, long freq) {
        this.delegate = delegate;
        if (freq < 0) {
            freq = 0;
        }
        this.freq = freq;
    }

    public NutsProgressMonitor getDelegate() {
        return delegate;
    }

    @Override
    public void onEvent(NutsProgressHandlerEvent event) {

        switch (event.getEventType()) {
            case PROGRESS: {
                double progress = event.getModel().getProgress();
                long newd = System.currentTimeMillis();
                if (newd > lastProgressTime + freq
                        || progress == 0
                        || progress == 1
                        || Double.isNaN(progress)
                ) {
                    getDelegate().setProgress(progress);
                    lastProgressTime = newd;
                }
                break;
            }
            case MESSAGE: {
                long newd = System.currentTimeMillis();
                if (//message.getLevel().intValue() >= level.intValue() ||
                        newd > lastMessageTime + freq) {
                    getDelegate().setMessage(event.getModel().getMessage());
                    lastMessageTime = newd;
                }
                break;
            }
            default: {
                NutsProgressMonitorHelper.processState(getDelegate(), event);
            }
        }
    }
}
