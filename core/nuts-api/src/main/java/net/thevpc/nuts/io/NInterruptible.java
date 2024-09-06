package net.thevpc.nuts.io;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.reserved.rpi.NIORPI;

import java.io.InputStream;

public interface NInterruptible<T> {
    static NInterruptible<InputStream> ofInputStream(InputStream base, NSession session) {
        return NIORPI.of(session).ofInterruptible(base);
    }

    void interrupt() throws NInterruptException;
    T base();
}
