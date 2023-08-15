package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.time.NProgressHandlerEvent;
import net.thevpc.nuts.time.NProgressMonitor;

public class NProgressMonitorHelper {
    public static void processState(NProgressMonitor monitor, NProgressHandlerEvent event) {
        switch (event.getEventType()){
            case START:{
                monitor.start();
                break;
            }
        }
    }
}
