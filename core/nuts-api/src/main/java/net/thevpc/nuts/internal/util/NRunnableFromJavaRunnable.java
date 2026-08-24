package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.concurrent.NRunnable;

/**
 * NRunnableFromJavaRunnable class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NRunnableFromJavaRunnable implements NRunnable {
    private final Runnable base;

    /**
     * N runnable from java runnable.
     *
     * @param base base
     * @return n runnable from java runnable result
     */
    public NRunnableFromJavaRunnable(Runnable base) {
        this.base = base;
    }

    @Override
    public void run() {
        base.run();
    }
}
