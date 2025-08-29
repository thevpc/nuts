package net.thevpc.nuts.runtime.standalone.xtra.time;

import net.thevpc.nuts.core.NI18n;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;
import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;

class SilentStartNProgressListenerAdapter implements NProgressListener {
    private final NProgressListener delegate;
    private final NMsg path;

    public SilentStartNProgressListenerAdapter(NProgressListener delegate, NMsg path) {
        this.delegate = delegate;
        this.path = path;
    }

    protected NLog _LOG() {
            return NLog.of(SilentStartNProgressListenerAdapter.class);
    }
    
    @Override
    public boolean onProgress(NProgressEvent event) {
        switch (event.getState()){
            case START:{
                return false;
            }
            case COMPLETE:{
                boolean b=delegate.onProgress(event);
                if (event.getError() != null) {
                    _LOG()

                            .log(NMsg.ofC(NI18n.of("download failed    : %s"), path).withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL).withDurationMillis(event.getDuration().getTimeAsMillis()));
                } else {
                    _LOG()

                            .log(NMsg.ofC( NI18n.of("download succeeded : %s"), path).withLevel(Level.FINEST).withIntent(NMsgIntent.SUCCESS).withDurationMillis(event.getDuration().getTimeAsMillis()));
                }
                return b;
            }
            default:{
                return delegate.onProgress(event);
            }
        }
    }
}
