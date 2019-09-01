package net.vpc.app.nuts.core.util.io;

import net.vpc.app.nuts.NutsInputStreamProgressFactory;
import net.vpc.app.nuts.NutsInputStreamProgressMonitor;
import net.vpc.app.nuts.NutsSession;

public class SingletonNutsInputStreamProgressFactory implements NutsInputStreamProgressFactory {
    private final NutsInputStreamProgressMonitor value;

    public SingletonNutsInputStreamProgressFactory(NutsInputStreamProgressMonitor value) {
        this.value = value;
    }

    @Override
    public NutsInputStreamProgressMonitor create(Object source, Object sourceOrigin, String sourceName, NutsSession session) {
        return value;
    }
}
