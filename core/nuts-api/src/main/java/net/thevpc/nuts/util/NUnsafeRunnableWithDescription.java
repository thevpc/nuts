package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NUnsafeRunnableWithDescription implements NUnsafeRunnable, NImmutable {
    private final NUnsafeRunnable base;
    private final Supplier<NElement> nfo;

    public NUnsafeRunnableWithDescription(NUnsafeRunnable base, Supplier<NElement> nfo) {
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
