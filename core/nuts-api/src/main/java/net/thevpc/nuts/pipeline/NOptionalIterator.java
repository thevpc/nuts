package net.thevpc.nuts.pipeline;

import net.thevpc.nuts.util.NOptional;

/**
 * NOptionalIterator interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NOptionalIterator<T> extends AutoCloseable {
    /**
     * never returns null, always return an optional
     * @return
     */
    NOptional<T> next();

    @Override
    default void close(){

    }
}
