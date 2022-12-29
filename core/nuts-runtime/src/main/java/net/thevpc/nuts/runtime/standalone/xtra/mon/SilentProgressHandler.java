package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.util.NProgressHandlerEvent;
import net.thevpc.nuts.util.NProgressHandler;

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
