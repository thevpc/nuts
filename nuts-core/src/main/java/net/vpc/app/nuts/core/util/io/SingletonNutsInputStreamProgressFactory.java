package net.vpc.app.nuts.core.util.io;

import net.vpc.app.nuts.NutsInputStreamProgressFactory;
import net.vpc.app.nuts.NutsProgressMonitor;
import net.vpc.app.nuts.NutsSession;

public class SingletonNutsInputStreamProgressFactory implements NutsInputStreamProgressFactory {
    private final NutsProgressMonitor value;

    public SingletonNutsInputStreamProgressFactory(NutsProgressMonitor value) {
        this.value = value;
    }

    @Override
    public NutsProgressMonitor create(Object source, Object sourceOrigin, String sourceName, NutsSession session) {
        return value;
    }
}
