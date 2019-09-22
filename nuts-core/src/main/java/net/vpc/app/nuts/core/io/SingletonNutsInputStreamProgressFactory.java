package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.NutsProgressFactory;
import net.vpc.app.nuts.NutsProgressMonitor;
import net.vpc.app.nuts.NutsSession;

public class SingletonNutsInputStreamProgressFactory implements NutsProgressFactory {
    private final NutsProgressMonitor value;

    public SingletonNutsInputStreamProgressFactory(NutsProgressMonitor value) {
        this.value = value;
    }

    @Override
    public NutsProgressMonitor create(Object source, Object sourceOrigin, NutsSession session) {
        return value;
    }
}
