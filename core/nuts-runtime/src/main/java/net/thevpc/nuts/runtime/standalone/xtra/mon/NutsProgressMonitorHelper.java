package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.NutsProgressHandlerEvent;
import net.thevpc.nuts.util.NutsProgressMonitor;
import net.thevpc.nuts.util.NutsProgressMonitorModel;
import net.thevpc.nuts.util.NutsProgressEventType;

public class NutsProgressMonitorHelper {
    public static void processState(NutsProgressMonitor monitor, NutsProgressHandlerEvent event) {
        switch (event.getEventType()){
            case START:{
                monitor.start();
                break;
            }
        }
    }
}
