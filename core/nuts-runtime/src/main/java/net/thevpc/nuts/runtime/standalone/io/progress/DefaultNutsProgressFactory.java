package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NutsProgressFactory;
import net.thevpc.nuts.util.NutsProgressListener;
import net.thevpc.nuts.NutsSession;

public class DefaultNutsProgressFactory implements NutsProgressFactory {
    @Override
    public NutsProgressListener createProgressListener(Object source, Object sourceOrigin, NutsSession session) {
        if (!acceptMonitoring(source, sourceOrigin, session)) {
            return null;
        }
        return new DefaultNutsCountProgressListener();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin, NutsSession session) {
        if (!session.isProgress()) {
            return false;
        }
        return true;
    }
}
