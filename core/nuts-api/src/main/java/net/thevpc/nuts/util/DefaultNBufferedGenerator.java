package net.thevpc.nuts.util;

/**
 * DefaultNBufferedGenerator class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class DefaultNBufferedGenerator<T> implements NBufferedGenerator<T> {
    private final NGenerator<T> in;
    private final Object[] buffer;
    private int pos = 0;    // current position in buffer
    private int limit = 0;  // number of elements available in buffer

    /**
     * Default n buffered generator.
     *
     * @param reader reader
     * @return default n buffered generator result
     */
    public DefaultNBufferedGenerator(NGenerator<T> reader) {
      /**
       * This.
       *
       * @param reader reader
       * @param 1024 1024
       */
        this(reader, 1024);
    }

    /**
     * Default n buffered generator.
     *
     * @param reader reader
     * @param bufferSize buffer size
     * @return default n buffered generator result
     */
    public DefaultNBufferedGenerator(NGenerator<T> reader, int bufferSize) {
        this.in = reader;
        this.buffer = new Object[bufferSize];
    }

    /**
     * Fill.
     *
     * @param min min
     * @return fill result
     */
    private void fill(int min) {
        while (limit - pos < min) {
            if (limit == buffer.length) {
                // shift remaining chars to start
                int remaining = limit - pos;
                System.arraycopy(buffer, pos, buffer, 0, remaining);
                pos = 0;
                limit = remaining;
            }
            int write = limit;
            int needed = min - (limit - pos);
            int capacity = Math.min(buffer.length - write, needed);

            for (int i = 0; i < capacity; i++) {
                T t = in.next();
                if (t == null) {
                    return;
                }
                buffer[write++] = t;
            }
            limit = write;
        }
    }

    /**
     * Buffered.
     *
     * @return buffered result
     */
    public int buffered() {
        return limit - pos;
    }

    /**
     * Peek at.
     *
     * @param offset offset
     * @return peek at result
     */
    public T peekAt(int offset) {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(offset + 1);
        int index = pos + offset;
        return (index < limit) ? (T) buffer[index] : null;
    }

    /**
     * Peek.
     *
     * @return peek result
     */
    public T peek() {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(1);
        return pos < limit ? (T) buffer[pos] : null;
    }


    /**
     * Checks if has next.
     *
     * @return has next result
     */
    public boolean hasNext() {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(1);
        return limit - pos > 0;
    }

    /**
     * Checks if has next.
     *
     * @param count count
     * @return has next result
     */
    public boolean hasNext(int count) {
      /**
       * Fill.
       *
       * @param count count
       */
        fill(count);
        return limit - pos >= count;
    }


    /**
     * Skip.
     *
     * @param count count
     * @return skip result
     */
    public boolean skip(int count) {
      /**
       * Fill.
       *
       * @param count count
       */
        fill(count);
        if (limit - pos < count) return false;
        pos += count;
        return true;
    }


    /**
     * Next.
     *
     * @return next result
     */
    public T next() {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(1);
        if (pos >= limit) {
            return null;
        }
        return (T) buffer[pos++];
    }
}
