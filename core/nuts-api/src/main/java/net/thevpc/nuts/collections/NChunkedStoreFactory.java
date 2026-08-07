package net.thevpc.nuts.collections;

import net.thevpc.nuts.pipeline.NOptionalIterator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * A factory interface used to validate, serialize, and deserialize elements
 * within a {@link NChunkedStore}.
 *
 * @param <T> the type of elements managed by this factory
 * @since 0.8.4
 */
public interface NChunkedStoreFactory<T> {

    /**
     * Determines whether the given element is valid and can be stored in the chunked store.
     *
     * @param any the element to validate
     * @return {@code true} if the element is acceptable, {@code false} otherwise
     */
    boolean accept(T any);

    /**
     * Creates and returns a scanner iterator to read elements sequentially from the provided input stream.
     *
     * @param inputStream the source input stream to scan
     * @return a {@link NOptionalIterator} returning the scanned elements of type {@code T}
     */
    NOptionalIterator<T> scanner(InputStream inputStream);

    /**
     * Creates and returns a consumer (appender) to write elements to the provided output stream.
     *
     * @param outputStream the destination output stream to append elements to
     * @return a {@link Consumer} used for writing elements of type {@code T}
     */
    Consumer<T> appender(OutputStream outputStream);
}
