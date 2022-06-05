package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.*;

import java.util.logging.Level;

class SilentStartNutsProgressListenerAdapter implements NutsProgressListener {
    private NutsLogger LOG;
    private final NutsProgressListener finalMonitor;
    private final NutsMessage path;

    public SilentStartNutsProgressListenerAdapter(NutsProgressListener finalMonitor, NutsMessage path) {
        this.finalMonitor = finalMonitor;
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
    public void onStart(NutsProgressEvent event) {
    }

    @Override
    public void onComplete(NutsProgressEvent event) {
        finalMonitor.onComplete(event);
        if (event.getError() != null) {
            _LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.ofJstyle("download failed    : {0}", path));
        } else {
            _LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.SUCCESS)
                    .log(NutsMessage.ofJstyle( "download succeeded : {0}", path));
        }
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        return finalMonitor.onProgress(event);
    }
}
