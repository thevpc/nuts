package net.thevpc.nuts.io;

/**
 * NInputContentProvider interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NInputContentProvider extends NInputStreamProvider{
    /**
     * Name.
     *
     * @return name result
     */
    String name();
    /**
     * Content type.
     *
     * @return content type result
     */
    String contentType();
    /**
     * Charset.
     *
     * @return charset result
     */
    String charset();
}
