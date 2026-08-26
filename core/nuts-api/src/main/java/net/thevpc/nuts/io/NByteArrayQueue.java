package net.thevpc.nuts.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * NByteArrayQueue class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NByteArrayQueue {
    private byte[] content;
    private int increment;
    private int from;
    private int to;
    private Map<String, Pattern> cachedPatterns = new HashMap<>();

    /**
     * N byte array queue.
     *
     * @return n byte array queue result
     */
    public NByteArrayQueue() {
      /**
       * This.
       *
       * @param 256 256
       */
        this(256);
    }

    /**
     * N byte array queue.
     *
     * @param initial initial
     * @return n byte array queue result
     */
    public NByteArrayQueue(int initial) {
      /**
       * This.
       *
       * @param initial initial
       * @param 256) 256)
       */
        this(initial, Math.min(initial, 256));
    }

    /**
     * N byte array queue.
     *
     * @param initial initial
     * @param increment increment
     * @return n byte array queue result
     */
    public NByteArrayQueue(int initial, int increment) {
        content = new byte[initial];
        this.increment = increment;
    }

    /**
     * Write.
     *
     * @param inputStream input stream
     * @param max max
     * @return write result
     */
    public int write(InputStream inputStream, int max) {
        byte[] all = new byte[max];
        int count = 0;
        try {
            count = inputStream.read(all);
        } catch (IOException e) {
            /**
             * Unchecked io exception.
             *
             * @param e e
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(e);
        }
        if (count > 0) {
          /**
           * Write.
           *
           * @param all all
           * @param 0 0
           * @param count count
           */
            write(all, 0, count);
        }
        return count;
    }


    /**
     * Write.
     *
     * @param c c
     * @return write result
     */
    public synchronized void write(byte[] c) {
      /**
       * Write.
       *
       * @param c c
       * @param 0 0
       * @param c.length c.length
       */
        write(c, 0, c.length);
    }

    /**
     * Write.
     *
     * @param c c
     * @param offset offset
     * @param len len
     * @return write result
     */
    public synchronized void write(byte[] c, int offset, int len) {
      /**
       * Ensure available.
       *
       * @param len len
       */
        ensureAvailable(len);
        try {
            System.arraycopy(c, offset, content, to, len);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw e;
        }
        to += len;
    }

    /**
     * Write.
     *
     * @param c c
     * @return write result
     */
    public synchronized void write(byte c) {
      /**
       * Ensure available.
       *
       * @param 1 1
       */
        ensureAvailable(1);
        content[to++] = c;
    }

    /**
     * Length.
     *
     * @return length result
     */
    public int length() {
        return to - from;
    }

    /**
     * Peek.
     *
     * @return peek result
     */
    public byte peek() {
        if (to > from) {
            return content[from];
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
            return new String(content, from, count);
        } else {
            return new String(content, from, c);
        }
    }

    /**
     * Checks if can read.
     *
     * @return can read result
     */
    public boolean canRead() {
        return from < to;
    }

    /**
     * Checks if can read by count.
     *
     * @param count count
     * @return can read by count result
     */
    public boolean canReadByCount(int count) {
        return from + count - 1 < to;
    }

    /**
     * Read.
     *
     * @param buffer buffer
     * @return read result
     */
    public int read(byte[] buffer) {
        /**
         * Read.
         *
         * @param buffer buffer
         * @param 0 0
         * @param buffer.length buffer.length
         * @return read result
         */
        return read(buffer, 0, buffer.length);
    }

    /**
     * Read.
     *
     * @param buffer buffer
     * @param offset offset
     * @param count count
     * @return read result
     */
    public int read(byte[] buffer, int offset, int count) {
        if (from + count < to) {
            System.arraycopy(content, from, buffer, offset, count);
            from += count;
            return count;
        } else {
            count = to - from;
            System.arraycopy(content, from, buffer, offset, count);
            from = 0;
            to = 0;
            return count;
        }
    }

    /**
     * Read.
     *
     * @param count count
     * @return read result
     */
    public byte[] read(int count) {
        if (from + count < to) {
            byte[] buffer = new byte[count];
            System.arraycopy(content, from, buffer, 0, count);
            from += count;
            return buffer;
        } else {
            byte[] r = new byte[to - from];
            System.arraycopy(content, from, r, 0, to - from);
            from = 0;
            to = 0;
            return r;
        }
    }

    /**
     * Skip.
     *
     * @param count count
     */
    public void skip(int count) {
        if (from + count < to) {
            from += count;
        } else {
            from = 0;
            to = 0;
        }
    }

    /**
     * Clear.
     */
    public void clear() {
        from = 0;
        to = 0;
    }

    /**
     * Read.
     *
     * @return read result
     */
    public byte read() {
        if (canRead()) {
            return content[from++];
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
     * Ensure available.
     *
     * @param z z
     */
    public void ensureAvailable(int z) {
        int currentEffLen = length();
        int newEffLen = currentEffLen + z;
        if (newEffLen > content.length) {
            byte[] n = new byte[newEffLen + increment];
            System.arraycopy(content, from, n, 0, currentEffLen);
            content = n;
            from = 0;
            to = currentEffLen;
            return;
        }
        int rightAvailable = content.length - to;
        if (z <= rightAvailable) {
            return;
        }
        System.arraycopy(content, from, content, 0, currentEffLen);
        from = 0;
        to = currentEffLen;
    }

    /**
     * Returns string representation of queued bytes.
     *
     * @return string representation of queue content
     */
    @Override
    public String toString() {
        int c = length();
        return new String(content, from, c);
    }

    /**
     * Byte at.
     *
     * @param index index
     * @return byte at result
     */
    public byte byteAt(int index) {
        if (index >= 0 && index < length()) {
            return content[from + index];
        }
        /**
         * Index out of bounds exception.
         *
         * @param index index
         * @return index out of bounds exception result
         */
        throw new IndexOutOfBoundsException("invalid index " + index);
    }


    /**
     * Checks if has next.
     *
     * @return has next result
     */
    public boolean hasNext() {
        return to > from;
    }

    /**
     * Increment.
     *
     * @return increment result
     */
    public int increment() {
        return increment;
    }

    /**
     * From.
     *
     * @return from result
     */
    public int from() {
        return from;
    }

    /**
     * Converts to to.
     *
     * @return to result
     */
    public int to() {
        return to;
    }

    /**
     * Allocated size.
     *
     * @return allocated size result
     */
    public int allocatedSize() {
        return content.length;
    }
}
