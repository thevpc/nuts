package net.thevpc.nuts.runtime.standalone.time;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;

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
