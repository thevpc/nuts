package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NSetter;

import java.io.InputStream;

/**
 * NInputStreamProvider interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NInputStreamProvider {
    /**
     * Input stream.
     *
     * @return input stream result
     */
    @NSetter
    InputStream inputStream();
}
