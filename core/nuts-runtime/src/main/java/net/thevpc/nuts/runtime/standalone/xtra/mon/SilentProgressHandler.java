package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.time.NProgressHandlerEvent;
import net.thevpc.nuts.time.NProgressHandler;

/**
 * @author taha.bensalah@gmail.com on 7/17/16.
 */
public class SilentProgressHandler implements NProgressHandler {
    public SilentProgressHandler() {
        super();
    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {
        //do nothing
    }
}
