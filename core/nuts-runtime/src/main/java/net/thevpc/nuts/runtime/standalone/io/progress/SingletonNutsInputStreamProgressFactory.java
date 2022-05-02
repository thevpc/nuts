package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NutsProgressFactory;
import net.thevpc.nuts.util.NutsProgressMonitor;
import net.thevpc.nuts.NutsSession;

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
