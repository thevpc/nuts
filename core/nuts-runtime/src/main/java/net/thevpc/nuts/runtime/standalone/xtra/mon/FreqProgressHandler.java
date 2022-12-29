package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.util.*;

import java.util.logging.Level;

public class FreqProgressHandler implements NProgressHandler {
    private long freq;
    private long lastMessageTime;
    private long lastProgressTime;
    private Level level = Level.INFO;
    private NProgressMonitor delegate;

    public FreqProgressHandler(NProgressMonitor delegate, long freq) {
        this.delegate = delegate;
        if (freq < 0) {
            freq = 0;
        }
        this.freq = freq;
    }

    public NProgressMonitor getDelegate() {
        return delegate;
    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {

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
                NProgressMonitorHelper.processState(getDelegate(), event);
            }
        }
    }
}
