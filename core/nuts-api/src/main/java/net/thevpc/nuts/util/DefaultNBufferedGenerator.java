package net.thevpc.nuts.util;

public class DefaultNBufferedGenerator<T> implements NBufferedGenerator<T> {
    private final NGenerator<T> in;
    private final Object[] buffer;
    private int pos = 0;    // current position in buffer
    private int limit = 0;  // number of elements available in buffer

    public DefaultNBufferedGenerator(NGenerator<T> reader) {
        this(reader, 1024);
    }

    public DefaultNBufferedGenerator(NGenerator<T> reader, int bufferSize) {
        this.in = reader;
        this.buffer = new Object[bufferSize];
    }

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

    public int buffered() {
        return limit - pos;
    }

    public T peekAt(int offset) {
        fill(offset + 1);
        int index = pos + offset;
        return (index < limit) ? (T) buffer[index] : null;
    }

    public T peek() {
        fill(1);
        return pos < limit ? (T) buffer[pos] : null;
    }


    public boolean hasNext() {
        fill(1);
        return limit - pos > 0;
    }

    public boolean hasNext(int count) {
        fill(count);
        return limit - pos >= count;
    }


    public boolean skip(int count) {
        fill(count);
        if (limit - pos < count) return false;
        pos += count;
        return true;
    }


    public T next() {
        fill(1);
        if (pos >= limit) {
            return null;
        }
        return (T) buffer[pos++];
    }
}
