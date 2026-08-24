package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;

import java.io.OutputStream;

/**
 * NOutputStreamBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NOutputStreamBuilder {
    /**
     * Creates a new instance of of.
     *
     * @param outputStream output stream
     * @return of result
     */
    static NOutputStreamBuilder of(OutputStream outputStream) {
        return NIORPI.of().createOutputStreamBuilder(outputStream);
    }

    /**
     * Base.
     *
     * @return base result
     */
    OutputStream base();

    /**
     * Base.
     *
     * @param base base
     * @return base result
     */
    NOutputStreamBuilder base(OutputStream base);

    /**
     * Metadata.
     *
     * @return metadata result
     */
    NContentMetadata metadata();

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NOutputStreamBuilder metadata(NContentMetadata metadata);

    /**
     * Checks if is close base.
     *
     * @return is close base result
     */
    boolean isCloseBase();

    /**
     * Close base.
     *
     * @param closeBase close base
     * @return close base result
     */
    NOutputStreamBuilder closeBase(boolean closeBase);

    /**
     * Close action.
     *
     * @return close action result
     */
    Runnable closeAction();

    /**
     * Close action.
     *
     * @param closeAction close action
     * @return close action result
     */
    NOutputStreamBuilder closeAction(Runnable closeAction);

    /**
     * Creates a new instance of create output stream.
     *
     * @return create output stream result
     */
    OutputStream createOutputStream();
}
