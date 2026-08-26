package net.thevpc.nuts.runtime.standalone.xtra.time;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.mon.NProgressFactory;
import net.thevpc.nuts.mon.NProgressListener;

public class DefaultNProgressFactory implements NProgressFactory {

    public DefaultNProgressFactory() {
    }

    @Override
    public NProgressListener createProgressListener(Object source, Object sourceOrigin) {
        if (!acceptMonitoring(source, sourceOrigin)) {
            return null;
        }
        return new DefaultNCountProgressListener();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin) {
        if (!NSession.of().isProgress()) {
            return false;
        }
        return true;
    }
}
