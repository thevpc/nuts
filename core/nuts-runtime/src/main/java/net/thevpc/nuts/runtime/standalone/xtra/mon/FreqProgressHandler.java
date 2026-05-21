package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.time.NProgressHandler;
import net.thevpc.nuts.time.NProgressHandlerEvent;
import net.thevpc.nuts.time.NProgressMonitor;

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

        switch (event.eventType()) {
            case PROGRESS: {
                double progress = event.model().progress();
                long newd = System.currentTimeMillis();
                if (newd > lastProgressTime + freq
                        || progress == 0
                        || progress == 1
                        || Double.isNaN(progress)
                ) {
                    getDelegate().progress(progress);
                    lastProgressTime = newd;
                }
                break;
            }
            case MESSAGE: {
                long newd = System.currentTimeMillis();
                if (//message.getLevel().intValue() >= level.intValue() ||
                        newd > lastMessageTime + freq) {
                    getDelegate().message(event.model().message());
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
