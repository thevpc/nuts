package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.util.NRunnable;

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
