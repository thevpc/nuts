package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NMatchType;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

/**
 * NByteQueue interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NByteQueue {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NByteQueue of() {
        return NUtilsRPI.of().createByteQueue();
    }

    /**
     * Creates a new instance of of.
     *
     * @param size size
     * @return of result
     */
    static NByteQueue of(int size) {
        return NUtilsRPI.of().createByteQueue(size);
    }

    /**
     * Creates a new instance of of.
     *
     * @param content content
     * @return of result
     */
    static NByteQueue of(byte[] content) {
        return NUtilsRPI.of().createByteQueue(content);
    }

    /**
     * Write.
     *
     * @param reader reader
     * @param max max
     * @return write result
     */
    int write(InputStream reader, int max);

    /**
     * Write.
     *
     * @param c c
     */
    void write(ByteBuffer c);

    /**
     * Write.
     *
     * @param c c
     */
    void write(byte[] c);

    /**
     * Write.
     *
     * @param c c
     * @param offset offset
     * @param len len
     */
    void write(byte[] c, int offset, int len);

    /**
     * Write.
     *
     * @param c c
     */
    void write(byte c);

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * Length.
     *
     * @return length result
     */
    int length();

    /**
     * Peek.
     *
     * @return peek result
     */
    byte peek();

    /**
     * Peek at.
     *
     * @param index index
     * @return peek at result
     */
    byte peekAt(int index);

    /**
     * Peek.
     *
     * @param count count
     * @return peek result
     */
    String peek(int count);

    /**
     * Checks if can read.
     *
     * @return can read result
     */
    boolean canRead();

    /**
     * Checks if can read by count.
     *
     * @param count count
     * @return can read by count result
     */
    boolean canReadByCount(int count);

    /**
     * Read.
     *
     * @param count count
     * @return read result
     */
    String read(int count);

    /**
     * Skip.
     *
     * @param count count
     */
    void skip(int count);

    /**
     * Skip value.
     *
     * @param value value
     * @return skip value result
     */
    NMatchType skipValue(String value);

    /**
     * Clear.
     */
    void clear();

    /**
     * Read.
     *
     * @return read result
     */
    byte read();

    /**
     * Ensure available.
     *
     * @param z z
     */
    void ensureAvailable(int z);

    /**
     * Byte at.
     *
     * @param index index
     * @return byte at result
     */
    byte byteAt(int index);

    /**
     * Bytes.
     *
     * @return bytes result
     */
    IntStream bytes();

    /**
     * Checks if has next.
     *
     * @return has next result
     */
    boolean hasNext();

    /**
     * Checks if is eof.
     *
     * @return is eof result
     */
    boolean isEOF();

    /**
     * Eof.
     *
     * @param eof eof
     */
    void eof(boolean eof);

    /**
     * Increment.
     *
     * @return increment result
     */
    int increment();

    /**
     * From.
     *
     * @return from result
     */
    int from();

    /**
     * Converts to to.
     *
     * @return to result
     */
    int to();

    /**
     * Allocated size.
     *
     * @return allocated size result
     */
    int allocatedSize();
}
