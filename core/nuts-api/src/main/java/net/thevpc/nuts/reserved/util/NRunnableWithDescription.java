package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NRunnable;

import java.util.function.Supplier;

public class NRunnableWithDescription implements NRunnable {
    private final NRunnable base;
    private final Supplier<NElement> nfo;

    public NRunnableWithDescription(NRunnable base, Supplier<NElement> nfo) {
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
