package net.thevpc.nuts.internal.util;

import java.io.EOFException;
import java.io.UncheckedIOException;

/**
 * NReservedSimpleCharQueue class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public final class NReservedSimpleCharQueue {

    private char[] content;
    private int index;

    /**
     * N reserved simple char queue.
     *
     * @param value value
     * @return n reserved simple char queue result
     */
    public NReservedSimpleCharQueue(char[] value) {
        content = value;
    }

    /**
     * Checks if has next.
     *
     * @return has next result
     */
    public boolean hasNext() {
        return index < content.length;
    }

    /**
     * Length.
     *
     * @return length result
     */
    public int length() {
        return content.length - index;
    }

    /**
     * Peek.
     *
     * @return peek result
     */
    public char peek() {
        if (index < content.length) {
            return content[index];
        }
        /**
         * Unchecked io exception.
         *
         * @param EOFException() eof exception()
         * @return unchecked io exception result
         */
        throw new UncheckedIOException(new EOFException());
    }

    /**
     * Peek.
     *
     * @param count count
     * @return peek result
     */
    public String peek(int count) {
        int c = length();
        if (count < c) {
            return new String(content, index, count);
        } else {
            return new String(content, index, c);
        }
    }

    /**
     * Read.
     *
     * @param count count
     * @return read result
     */
    public String read(int count) {
        if (index + count < content.length) {
            String s = new String(content, index, count);
            index += count;
            return s;
        } else {
            String s = new String(content, index, content.length - index);
            index = content.length;
            return s;
        }
    }

    /**
     * Skip.
     *
     * @param count count
     */
    public void skip(int count) {
        if (index + count < content.length) {
            index += count;
        } else {
            index = content.length;
        }
    }

    /**
     * Read.
     *
     * @return read result
     */
    public char read() {
        if (index < content.length) {
            return content[index++];
        }
        /**
         * Unchecked io exception.
         *
         * @param EOFException() eof exception()
         * @return unchecked io exception result
         */
        throw new UncheckedIOException(new EOFException());
    }

    /**
     * Peek at.
     *
     * @param index index
     * @return peek at result
     */
    public char peekAt(int index) {
        if (index >= 0 && index < length()) {
            return content[this.index + index];
        }
        /**
         * Index out of bounds exception.
         *
         * @param index index
         * @return index out of bounds exception result
         */
        throw new IndexOutOfBoundsException("invalid index " + index);
    }

    public String toString() {
        int c = length();
        return new String(content, index, c);
    }


}
