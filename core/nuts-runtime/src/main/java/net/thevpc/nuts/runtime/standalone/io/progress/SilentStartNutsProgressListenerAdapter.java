package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.*;

import java.util.logging.Level;

class SilentStartNutsProgressListenerAdapter implements NutsProgressListener {
    private NutsLogger LOG;
    private final NutsProgressListener delegate;
    private final NutsMessage path;

    public SilentStartNutsProgressListenerAdapter(NutsProgressListener delegate, NutsMessage path) {
        this.delegate = delegate;
        this.path = path;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(SilentStartNutsProgressListenerAdapter.class,session);
        }
        return LOG;
    }
    
    @Override
    public boolean onProgress(NutsProgressEvent event) {
        switch (event.getState()){
            case START:{
                return false;
            }
            case COMPLETE:{
                boolean b=delegate.onProgress(event);
                if (event.getError() != null) {
                    _LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                            .log(NutsMessage.ofJstyle("download failed    : {0}", path));
                } else {
                    _LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.SUCCESS)
                            .log(NutsMessage.ofJstyle( "download succeeded : {0}", path));
                }
                return b;
            }
            default:{
                return delegate.onProgress(event);
            }
        }
    }
}
