package net.thevpc.nuts.io;

import java.io.OutputStream;

/**
 * OutputStreamDelegate interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface OutputStreamDelegate {
    /**
     * Delegate output stream.
     *
     * @return delegate output stream result
     */
    OutputStream delegateOutputStream();
}
