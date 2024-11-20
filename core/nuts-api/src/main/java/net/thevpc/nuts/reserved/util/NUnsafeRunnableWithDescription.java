package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NUnsafeRunnable;

public class NUnsafeRunnableWithDescription implements NUnsafeRunnable {
    private final NUnsafeRunnable base;
    private final NEDesc nfo;

    public NUnsafeRunnableWithDescription(NUnsafeRunnable base, NEDesc nfo) {
        this.base = base;
        this.nfo = nfo;
    }

    @Override
    public NElement describe() {
        return nfo.get();
    }

    @Override
    public void run() throws Exception {
        base.run();
    }
}
