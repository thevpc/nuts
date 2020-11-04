package net.thevpc.nuts.runtime.util.io;

public interface Interruptible {
    void interrupt() throws InterruptException;
}
