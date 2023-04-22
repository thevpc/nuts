package net.thevpc.nuts.io;

public interface NInterruptible {
    void interrupt() throws NInterruptException;
}
