package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.util.NutsProgressFactory;
import net.thevpc.nuts.util.NutsProgressListener;
import net.thevpc.nuts.NutsSession;

public class SingletonNutsInputStreamProgressFactory implements NutsProgressFactory {
    private final NutsProgressListener value;

    public SingletonNutsInputStreamProgressFactory(NutsProgressListener value) {
        this.value = value;
    }

    @Override
    public NutsProgressListener createProgressListener(Object source, Object sourceOrigin, NutsSession session) {
        return value;
    }
}
