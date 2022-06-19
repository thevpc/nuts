package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.NutsProgressEventType;
import net.thevpc.nuts.util.NutsProgressHandlerEvent;
import net.thevpc.nuts.util.NutsProgressMonitorModel;
import net.thevpc.nuts.util.NutsProgressHandler;

/**
 * @author taha.bensalah@gmail.com on 7/17/16.
 */
public class SilentProgressHandler implements NutsProgressHandler {
    public SilentProgressHandler() {
        super();
    }

    @Override
    public void onEvent(NutsProgressHandlerEvent event) {
        //do nothing
    }
}
