package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;
import net.thevpc.nuts.NSession;

public class SingletonNInputStreamProgressFactory implements NProgressFactory {
    private final NProgressListener value;

    public SingletonNInputStreamProgressFactory(NProgressListener value) {
        this.value = value;
    }

    @Override
    public NProgressListener createProgressListener(Object source, Object sourceOrigin, NSession session) {
        return value;
    }
}
