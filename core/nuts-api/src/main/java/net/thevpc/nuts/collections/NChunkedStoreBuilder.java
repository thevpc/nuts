package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.io.NPath;

/**
 * A builder interface for creating and configuring instances of {@link NChunkedStore}.
 *
 * <p>Provides options to configure chunk sizes, buffer sizes, folder location,
 * append behavior, zero-padding index layout, and custom serializer factories.</p>
 *
 * @param <T> the type of elements to be stored
 * @since 0.8.4
 */
public interface NChunkedStoreBuilder<T> {

    /**
     * Creates a new builder configured for storing {@code String} elements line-by-line.
     *
     * @param folder the folder where chunk files are saved
     * @return a new {@code NChunkedStoreBuilder} for {@code String} elements
     */
    static NChunkedStoreBuilder<String> ofLines(NPath folder) {
        NUtilsRPI r = NUtilsRPI.of();
        return r.createChunkedStoreBuilder(folder, r.createLineChunkedStoreFactory());
    }

    /**
     * Creates a new builder for storing elements of type {@code T} using a custom factory.
     *
     * @param <T> the type of elements to be stored
     * @param folder the folder where chunk files are saved
     * @param storeFactory the factory to serialize and deserialize elements
     * @return a new {@code NChunkedStoreBuilder} for type {@code T}
     */
    static <T> NChunkedStoreBuilder<T> of(NPath folder, NChunkedStoreFactory<T> storeFactory) {
        return NUtilsRPI.of().createChunkedStoreBuilder(folder, storeFactory);
    }

    /**
     * Returns the buffer size used for reading and writing metadata files (e.g. pointers).
     *
     * @return the metadata buffer size
     */
    int metadataBufferSize();

    /**
     * Configures the buffer size used for reading and writing metadata files.
     *
     * @param metadataBufferSize the new metadata buffer size
     * @return this builder instance
     */
    NChunkedStoreBuilder<T> metadataBufferSize(int metadataBufferSize);

    /**
     * Returns the folder directory path where the chunk and pointer files are stored.
     *
     * @return the folder path
     */
    NPath folder();

    /**
     * Configures the folder directory path where chunk and pointer files will be stored.
     *
     * @param folder the new folder path
     * @return this builder instance
     */
    NChunkedStoreBuilder<T> folder(NPath folder);

    /**
     * Returns the factory used to validate, serialize and deserialize elements.
     *
     * @return the factory instance
     */
    NChunkedStoreFactory<T> factory();

    /**
     * Configures the factory used to validate, serialize and deserialize elements.
     *
     * @param factory the new factory instance
     * @return this builder instance
     */
    NChunkedStoreBuilder<T> factory(NChunkedStoreFactory<T> factory);

    /**
     * Returns the maximum number of elements stored per chunk file.
     *
     * @return the maximum chunk size
     */
    int chunkSize();

    /**
     * Configures the maximum number of elements allowed per chunk file.
     * When a chunk reaches this size, the store starts writing to a new chunk file.
     *
     * @param chunkSize the maximum chunk size
     * @return this builder instance
     */
    NChunkedStoreBuilder<T> chunkSize(int chunkSize);

    /**
     * Returns whether the store will append elements to an existing store in the folder,
     * or clear existing files on startup.
     *
     * @return {@code true} if configured to append, {@code false} otherwise
     */
    boolean isAppend();

    /**
     * Configures whether the store should append elements to an existing store in the directory.
     *
     * <p>If set to {@code false}, any existing chunk and pointer files in the directory
     * will be deleted when writing starts.</p>
     *
     * @param append {@code true} to append, {@code false} to overwrite/clear the store
     * @return this builder instance
     */
    NChunkedStoreBuilder<T> append(boolean append);

    /**
     * Returns the buffer size (number of elements) maintained in memory before flushing.
     *
     * @return the data buffer size
     */
    int dataBufferSize();

    /**
     * Configures the memory buffer size (number of elements) before automatic flushing to disk occurs.
     *
     * @param bufferSize the new data buffer size
     * @return this builder instance
     */
    NChunkedStoreBuilder<T> bufferSize(int bufferSize);

    /**
     * Returns the minimum digit layout length for formatting chunk filenames (e.g. zero-padded width).
     *
     * @return the number layout value
     */
    int numberLayout();

    /**
     * Configures the minimum digit layout length used to format the numeric indices in chunk filenames.
     * For example, a value of 8 formats chunk 1 as {@code 00000001.chunk}.
     *
     * @param numberLayout the minimum digit layout length
     * @return this builder instance
     */
    NChunkedStoreBuilder<T> numberLayout(int numberLayout);

    /**
     * Builds and returns a new {@link NChunkedStore} instance configured by this builder.
     *
     * @return a configured {@code NChunkedStore} instance
     */
    NChunkedStore<T> build();
}
