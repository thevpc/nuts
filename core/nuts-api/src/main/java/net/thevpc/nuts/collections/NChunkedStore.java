package net.thevpc.nuts.collections;

import net.thevpc.nuts.pipeline.NIterator;
import net.thevpc.nuts.pipeline.NStream;

/**
 * A generic, chunk-based persistence store for elements of type {@code T}.
 *
 * <p>The {@code NChunkedStore} partitions and stores objects in multiple consecutive chunk files
 * within a designated folder on the filesystem. This is useful for dealing with datasets
 * or queues that exceed the available system memory by flushing/paging elements to disk.</p>
 *
 * <p>Implementations typically buffer incoming records in memory and flush them to the current chunk
 * file on disk once the buffer size limit is exceeded, or when a new chunk is started.</p>
 *
 * @param <T> the type of elements stored in this chunked store
 * @since 0.8.4
 */
public interface NChunkedStore<T> extends AutoCloseable {

    /**
     * Flushes any currently buffered, in-memory elements to the active chunk file on disk.
     */
    void flush();

    /**
     * Adds the specified element to the store.
     *
     * <p>The element is first added to an in-memory buffer, which is automatically flushed
     * to disk once its capacity limit is reached, or when the current chunk reaches its size limit.</p>
     *
     * @param content the element to add
     * @throws net.thevpc.nuts.util.NIllegalArgumentException if the element is rejected by the underlying factory validator
     */
    void add(T content);

    /**
     * Checks whether the store is empty.
     *
     * @return {@code true} if the store contains no elements, {@code false} otherwise
     */
    boolean isEmpty();

    /**
     * Returns the total number of elements currently stored in this chunked store.
     *
     * @return the total count of elements
     */
    long size();

    /**
     * Returns an iterator over all elements in the store.
     *
     * <p>The iterator reads elements sequentially from the chunk files on disk.</p>
     *
     * @return a {@link NIterator} over the store elements
     */
    NIterator<T> iterator();

    /**
     * Returns a sequential stream of elements from the store.
     *
     * @return a {@link NStream} over the store elements
     */
    NStream<T> stream();

    /**
     * Closes this store, flushing any remaining buffered elements to disk.
     */
    void close();
}

