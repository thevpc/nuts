package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NMatchType;
import net.thevpc.nuts.util.NMultiPattern;
import net.thevpc.nuts.util.NStringMatchResult;

import java.io.Reader;
import java.nio.CharBuffer;

/**
 * NCharQueue interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NCharQueue extends CharSequence {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NCharQueue of() {
        return NUtilsRPI.of().createCharQueue();
    }

    /**
     * Creates a new instance of of.
     *
     * @param size size
     * @return of result
     */
    static NCharQueue of(int size) {
        return NUtilsRPI.of().createCharQueue(size);
    }

    /**
     * Creates a new instance of of.
     *
     * @param size size
     * @param increment increment
     * @return of result
     */
    static NCharQueue of(int size,int increment) {
        return NUtilsRPI.of().createCharQueue(size,increment);
    }

    /**
     * Creates a new instance of of.
     *
     * @param content content
     * @return of result
     */
    static NCharQueue of(char[] content) {
        return NUtilsRPI.of().createCharQueue(content);
    }

    /**
     * Write.
     *
     * @param reader reader
     * @param max max
     * @return write result
     */
    int write(Reader reader, int max);

    /**
     * Write.
     *
     * @param c c
     */
    void write(String c);

    /**
     * Write.
     *
     * @param c c
     */
    void write(CharSequence c);

    /**
     * Write.
     *
     * @param c c
     */
    void write(CharBuffer c);

    /**
     * Write.
     *
     * @param c c
     */
    void write(char[] c);

    /**
     * Write.
     *
     * @param c c
     * @param offset offset
     * @param len len
     */
    void write(char[] c, int offset, int len);

    /**
     * Write.
     *
     * @param c c
     */
    void write(char c);

    /**
     * Peek.
     *
     * @return peek result
     */
    char peek();

    /**
     * Peek at.
     *
     * @param index index
     * @return peek at result
     */
    char peekAt(int index);

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
     * Peek pattern.
     *
     * @param pattern pattern
     * @return peek pattern result
     */
    NStringMatchResult peekPattern(String pattern);

    /**
     * Clear.
     */
    void clear();

    /**
     * Do with pattern.
     *
     * @param pattern pattern
     * @return do with pattern result
     */
    NStringMatchResult doWithPattern(NMultiPattern pattern);

    /**
     * Peek pattern.
     *
     * @param pattern pattern
     * @param fully fully
     * @return peek pattern result
     */
    NStringMatchResult peekPattern(String pattern, boolean fully);

    /**
     * Peek string.
     *
     * @param value value
     * @return peek string result
     */
    NStringMatchResult peekString(String value);

    /**
     * Peek string.
     *
     * @param value value
     * @param fully fully
     * @return peek string result
     */
    NStringMatchResult peekString(String value, boolean fully);

    /**
     * Read blank.
     *
     * @return read blank result
     */
    String readBlank();

    /**
     * Read new line.
     *
     * @param fully fully
     * @return read new line result
     */
    String readNewLine(boolean fully);

    /**
     * Read.
     *
     * @return read result
     */
    char read();

    /**
     * Ensure available.
     *
     * @param z z
     */
    void ensureAvailable(int z);

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
