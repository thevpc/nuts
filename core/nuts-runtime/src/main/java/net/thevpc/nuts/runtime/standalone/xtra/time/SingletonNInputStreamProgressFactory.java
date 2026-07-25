package net.thevpc.nuts.runtime.standalone.xtra.time;

import net.thevpc.nuts.mon.NProgressFactory;
import net.thevpc.nuts.mon.NProgressListener;

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
