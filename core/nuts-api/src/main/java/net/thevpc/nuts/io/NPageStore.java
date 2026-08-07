package net.thevpc.nuts.io;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A low-level physical block storage manager that reads and writes pages of raw bytes.
 *
 * @since 0.8.4
 */
public interface NPageStore extends AutoCloseable {

    /**
     * Creates an in-memory page store.
     *
     * @param pageSize the size of each page in bytes
     * @return a new in-memory NPageStore
     */
    static NPageStore ofInMemory(int pageSize) {
        return net.thevpc.nuts.internal.rpi.NUtilsRPI.of().createInMemoryPageStore(pageSize);
    }

    /**
     * Creates a file-backed page store.
     *
     * @param path the path of the backing file
     * @param pageSize the size of each page in bytes
     * @return a new file-backed NPageStore
     */
    static NPageStore ofFile(NPath path, int pageSize) {
        return net.thevpc.nuts.internal.rpi.NUtilsRPI.of().createFilePageStore(path, pageSize);
    }

    /**
     * Returns the fixed size of each page in bytes (typically 4096).
     *
     * @return the page size
     */
    int pageSize();

    /**
     * Allocates a new page on disk/memory and returns its page ID.
     *
     * @return the allocated page ID
     * @throws IOException if an I/O error occurs
     */
    long allocatePage() throws IOException;

    /**
     * Reads the content of the given page ID into the specified ByteBuffer.
     *
     * @param pageId the page ID to read
     * @param buffer the destination byte buffer
     * @throws IOException if an I/O error occurs
     */
    void readPage(long pageId, ByteBuffer buffer) throws IOException;

    /**
     * Writes the content of the specified ByteBuffer to the given page ID.
     *
     * @param pageId the page ID to write to
     * @param buffer the source byte buffer containing the data
     * @throws IOException if an I/O error occurs
     */
    void writePage(long pageId, ByteBuffer buffer) throws IOException;

    /**
     * Frees the page with the given ID, making it available for future allocations.
     *
     * @param pageId the page ID to free
     * @throws IOException if an I/O error occurs
     */
    void freePage(long pageId) throws IOException;

    /**
     * Gets a metadata value stored in the header.
     *
     * @param index the metadata field index (e.g. 0 to 4)
     * @return the metadata value, or -1 if not set
     */
    long getUserData(int index);

    /**
     * Sets a metadata value stored in the header.
     *
     * @param index the metadata field index (e.g. 0 to 4)
     * @param value the metadata value to store
     */
    void setUserData(int index, long value);

    /**
     * Flushes any dirty or cached pages to disk.
     *
     * @throws IOException if an I/O error occurs
     */
    void flush() throws IOException;

    @Override
    void close() throws IOException;
}
