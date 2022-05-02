package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NutsProgressFactory;
import net.thevpc.nuts.util.NutsProgressMonitor;
import net.thevpc.nuts.NutsSession;

public class DefaultNutsProgressFactory implements NutsProgressFactory {
    @Override
    public NutsProgressMonitor create(Object source, Object sourceOrigin, NutsSession session) {
        if (!acceptMonitoring(source, sourceOrigin, session)) {
            return null;
        }
        return new DefaultNutsCountProgressMonitor();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin, NutsSession session) {
        if (!NutsProgressUtils.acceptProgress(session)) {
            return false;
        }
        return true;
    }
}
