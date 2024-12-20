package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NRunnable;

public class NRunnableWithDescription implements NRunnable {
    private final NRunnable base;
    private final NEDesc nfo;

    public NRunnableWithDescription(NRunnable base, NEDesc nfo) {
        this.base = base;
        this.nfo = nfo;
    }

    @Override
    public NElement describe() {
        return nfo.get();
    }

    @Override
    public void run() {
        base.run();
    }
}
