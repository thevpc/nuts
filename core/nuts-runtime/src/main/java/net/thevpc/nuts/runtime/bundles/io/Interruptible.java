package net.thevpc.nuts.runtime.bundles.io;

public interface Interruptible {
    void interrupt() throws InterruptException;
}
