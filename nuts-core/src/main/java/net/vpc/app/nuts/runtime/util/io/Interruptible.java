package net.vpc.app.nuts.runtime.util.io;

public interface Interruptible {
    void interrupt() throws InterruptException;
}
