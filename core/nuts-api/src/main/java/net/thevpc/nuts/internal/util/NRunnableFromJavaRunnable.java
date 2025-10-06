package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.concurrent.NRunnable;

public class NRunnableFromJavaRunnable implements NRunnable {
    private final Runnable base;

    public NRunnableFromJavaRunnable(Runnable base) {
        this.base = base;
    }

    @Override
    public void run() {
        base.run();
    }
}
