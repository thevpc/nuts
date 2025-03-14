package net.thevpc.nuts.runtime.standalone.time;

import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;

public class SingletonNInputStreamProgressFactory implements NProgressFactory {
    private final NProgressListener value;

    public SingletonNInputStreamProgressFactory(NProgressListener value) {
        this.value = value;
    }

    @Override
    public NProgressListener createProgressListener(Object source, Object sourceOrigin) {
        return value;
    }
}
