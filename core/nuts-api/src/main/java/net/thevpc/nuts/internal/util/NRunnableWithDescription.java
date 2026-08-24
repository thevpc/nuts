package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.concurrent.NRunnable;

import java.util.function.Supplier;

/**
 * NRunnableWithDescription class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NRunnableWithDescription implements NRunnable {
    private final NRunnable base;
    private final Supplier<NElement> nfo;

    /**
     * N runnable with description.
     *
     * @param base base
     * @param nfo nfo
     * @return n runnable with description result
     */
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
