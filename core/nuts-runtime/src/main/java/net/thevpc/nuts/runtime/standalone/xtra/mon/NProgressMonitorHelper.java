package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.util.NProgressHandlerEvent;
import net.thevpc.nuts.util.NProgressMonitor;

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
