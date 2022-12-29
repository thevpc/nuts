package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NProgressFactory;
import net.thevpc.nuts.util.NProgressListener;
import net.thevpc.nuts.NSession;

public class DefaultNProgressFactory implements NProgressFactory {
    @Override
    public NProgressListener createProgressListener(Object source, Object sourceOrigin, NSession session) {
        if (!acceptMonitoring(source, sourceOrigin, session)) {
            return null;
        }
        return new DefaultNCountProgressListener();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin, NSession session) {
        if (!session.isProgress()) {
            return false;
        }
        return true;
    }
}
