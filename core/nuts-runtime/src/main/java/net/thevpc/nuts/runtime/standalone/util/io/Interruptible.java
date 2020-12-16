package net.thevpc.nuts.runtime.standalone.util.io;

public interface Interruptible {
    void interrupt() throws InterruptException;
}
