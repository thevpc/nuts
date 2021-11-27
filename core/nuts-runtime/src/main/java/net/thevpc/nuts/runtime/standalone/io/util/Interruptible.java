package net.thevpc.nuts.runtime.standalone.io.util;

public interface Interruptible {
    void interrupt() throws InterruptException;
}
