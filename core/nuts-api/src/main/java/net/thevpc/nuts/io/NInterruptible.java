package net.thevpc.nuts.io;

import net.thevpc.nuts.reserved.rpi.NIORPI;

import java.io.InputStream;

public interface NInterruptible<T> {
    static NInterruptible<InputStream> ofInputStream(InputStream base) {
        return NIORPI.of().ofInterruptible(base);
    }

    void interrupt() throws NInterruptException;
    T base();
}
