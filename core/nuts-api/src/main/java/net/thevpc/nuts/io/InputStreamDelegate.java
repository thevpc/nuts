package net.thevpc.nuts.io;

import java.io.InputStream;

/**
 * InputStreamDelegate interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface InputStreamDelegate {
    /**
     * Delegate input stream.
     *
     * @return delegate input stream result
     */
    InputStream delegateInputStream();
}
