package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.*;

import java.util.logging.Level;

class SilentStartNProgressListenerAdapter implements NProgressListener {
    private NLogger LOG;
    private final NProgressListener delegate;
    private final NMsg path;

    public SilentStartNProgressListenerAdapter(NProgressListener delegate, NMsg path) {
        this.delegate = delegate;
        this.path = path;
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(SilentStartNProgressListenerAdapter.class,session);
        }
        return LOG;
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
                    _LOGOP(event.getSession()).level(Level.FINEST).verb(NLoggerVerb.FAIL)
                            .log(NMsg.ofJ("download failed    : {0}", path));
                } else {
                    _LOGOP(event.getSession()).level(Level.FINEST).verb(NLoggerVerb.SUCCESS)
                            .log(NMsg.ofJ( "download succeeded : {0}", path));
                }
                return b;
            }
            default:{
                return delegate.onProgress(event);
            }
        }
    }
}
