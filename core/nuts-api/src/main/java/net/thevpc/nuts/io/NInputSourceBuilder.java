package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.mon.NProgressListener;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * NInputSourceBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NInputSourceBuilder {

    /**
     * Creates a new instance of of.
     *
     * @param is is
     * @return of result
     */
    static NInputSourceBuilder of(InputStream is) {
        return NIORPI.of().createInputSourceBuilder(is);
    }

    /**
     * Base.
     *
     * @param baseInputStream base input stream
     * @return base result
     */
    NInputSourceBuilder base(InputStream baseInputStream);

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
    NInputSourceBuilder closeBase(boolean closeBase);

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
    NInputSourceBuilder closeAction(Runnable closeAction);

    /**
     * Checks if is interruptible.
     *
     * @return is interruptible result
     */
    boolean isInterruptible();

    /**
     * Interruptible.
     *
     * @param interruptible interruptible
     * @return interruptible result
     */
    NInputSourceBuilder interruptible(boolean interruptible);

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
    NInputSourceBuilder metadata(NContentMetadata metadata);

    /**
     * Source.
     *
     * @return source result
     */
    @NGetter
    Object source();

    /**
     * Source.
     *
     * @param source source
     * @return source result
     */
    @NSetter
    NInputSourceBuilder source(Object source);

    /**
     * Source name.
     *
     * @return source name result
     */
    NMsg sourceName();

    /**
     * Source name.
     *
     * @param sourceName source name
     * @return source name result
     */
    NInputSourceBuilder sourceName(NMsg sourceName);

    /**
     * Expected length.
     *
     * @return expected length result
     */
    Long expectedLength();

    /**
     * Expected length.
     *
     * @param expectedLength expected length
     * @return expected length result
     */
    NInputSourceBuilder expectedLength(Long expectedLength);

    /**
     * Monitoring listener.
     *
     * @return monitoring listener result
     */
    NProgressListener monitoringListener();

    /**
     * Monitoring listener.
     *
     * @param monitoringListener monitoring listener
     * @return monitoring listener result
     */
    NInputSourceBuilder monitoringListener(NProgressListener monitoringListener);

    /**
     * Checks if is non blocking.
     *
     * @return is non blocking result
     */
    boolean isNonBlocking();

    /**
     * Non blocking.
     *
     * @param nonBlocking non blocking
     * @return non blocking result
     */
    NInputSourceBuilder nonBlocking(boolean nonBlocking);

    /**
     * Tee.
     *
     * @return tee result
     */
    OutputStream tee();

    /**
     * Tee.
     *
     * @param tee tee
     * @return tee result
     */
    NInputSourceBuilder tee(OutputStream tee);

    /**
     * Creates a new instance of create non blocking input stream.
     *
     * @return create non blocking input stream result
     */
    NNonBlockingInputStream createNonBlockingInputStream();

    /**
     * Creates a new instance of create interruptible input stream.
     *
     * @return create interruptible input stream result
     */
    NInterruptible<InputStream> createInterruptibleInputStream();

    /**
     * Creates a new instance of create input stream.
     *
     * @return create input stream result
     */
    InputStream createInputStream();

    /**
     * Creates a new instance of create input source.
     *
     * @return create input source result
     */
    NInputSource createInputSource();
}
