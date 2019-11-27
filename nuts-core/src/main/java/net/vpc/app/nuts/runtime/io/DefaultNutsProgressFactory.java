package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

import java.util.logging.Logger;

public class DefaultNutsProgressFactory implements NutsProgressFactory {
    private static final Logger LOG = Logger.getLogger(DefaultNutsProgressFactory.class.getName());

    @Override
    public NutsProgressMonitor create(Object source, Object sourceOrigin, NutsSession session) {
        if (!acceptMonitoring(source, sourceOrigin, session)) {
            return null;
        }
        return new DefaultNutsCountProgressMonitor();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin, NutsSession session) {
        if (!CoreNutsUtils.acceptMonitoring(session)) {
            return false;
        }
        return true;
    }
}
