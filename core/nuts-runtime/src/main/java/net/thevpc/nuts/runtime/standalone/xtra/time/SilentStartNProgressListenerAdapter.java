package net.thevpc.nuts.runtime.standalone.xtra.time;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
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

    protected NLogOp _LOGOP() {
        return _LOG().with();
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
                    _LOGOP().level(Level.FINEST).verb(NLogVerb.FAIL)
                            .time(event.getDuration().getTimeAsMillis())
                            .log(NMsg.ofC(NI18n.of("download failed    : %s"), path));
                } else {
                    _LOGOP().level(Level.FINEST).verb(NLogVerb.SUCCESS)
                            .time(event.getDuration().getTimeAsMillis())
                            .log(NMsg.ofC( NI18n.of("download succeeded : %s"), path));
                }
                return b;
            }
            default:{
                return delegate.onProgress(event);
            }
        }
    }
}
