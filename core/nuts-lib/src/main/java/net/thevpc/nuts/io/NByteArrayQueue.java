package net.thevpc.nuts.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NByteArrayQueue {
    private byte[] content;
    private int increment;
    private int from;
    private int to;
    private Map<String, Pattern> cachedPatterns = new HashMap<>();

    public NByteArrayQueue() {
        this(256);
    }

    public NByteArrayQueue(int initial) {
        this(initial, Math.min(initial, 256));
    }

    public NByteArrayQueue(int initial, int increment) {
        content = new byte[initial];
        this.increment = increment;
    }

    public int write(InputStream inputStream, int max) {
        byte[] all = new byte[max];
        int count = 0;
        try {
            count = inputStream.read(all);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (count > 0) {
            write(all, 0, count);
        }
        return count;
    }


    public synchronized void write(byte[] c) {
        write(c, 0, c.length);
    }

    public synchronized void write(byte[] c, int offset, int len) {
        ensureAvailable(len);
        try {
            System.arraycopy(c, offset, content, to, len);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw e;
        }
        to += len;
    }

    public synchronized void write(byte c) {
        ensureAvailable(1);
        content[to++] = c;
    }

    public int length() {
        return to - from;
    }

    public byte peek() {
        if (to > from) {
            return content[from];
        }
        throw new UncheckedIOException(new EOFException());
    }

    public String peek(int count) {
        int c = length();
        if (count < c) {
            return new String(content, from, count);
        } else {
            return new String(content, from, c);
        }
    }

    public boolean canRead() {
        return from < to;
    }

    public boolean canReadByCount(int count) {
        return from + count - 1 < to;
    }

    public int read(byte[] buffer) {
        return read(buffer, 0, buffer.length);
    }

    public int read(byte[] buffer, int offset, int count) {
        if (from + count < to) {
            System.arraycopy(content, from, buffer, offset, count);
            from += count;
            return count;
        } else {
            count = to - from;
            byte[] r = new byte[to - from];
            System.arraycopy(content, from, r, 0, count);
            from = 0;
            to = 0;
            return count;
        }
    }

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

    public void skip(int count) {
        if (from + count < to) {
            from += count;
        } else {
            from = 0;
            to = 0;
        }
    }

    public void clear() {
        from = 0;
        to = 0;
    }

    public byte read() {
        if (canRead()) {
            return content[from++];
        }
        throw new UncheckedIOException(new EOFException());
    }

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

    public String toString() {
        int c = length();
        return new String(content, from, c);
    }

    public byte byteAt(int index) {
        if (index >= 0 && index < length()) {
            return content[from + index];
        }
        throw new IndexOutOfBoundsException("invalid index " + index);
    }


    public boolean hasNext() {
        return to > from;
    }

    public int getIncrement() {
        return increment;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getAllocatedSize() {
        return content.length;
    }
}
