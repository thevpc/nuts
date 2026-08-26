package net.thevpc.nuts.io;

import net.thevpc.nuts.concurrent.NInterruptedException;
import net.thevpc.nuts.internal.rpi.NIORPI;

import java.io.InputStream;

/**
 * NInterruptible interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NInterruptible<T> {
    /**
     * Creates a new instance of of input stream.
     *
     * @param base base
     * @return of input stream result
     */
    static NInterruptible<InputStream> ofInputStream(InputStream base) {
        return NIORPI.of().createInterruptible(base);
    }

    /**
     * Interrupt.
     *
     * @throws NInterruptedException if execution fails
     */
    void interrupt() throws NInterruptedException;

    /**
     * Base.
     *
     * @return base result
     */
    T base();
}
