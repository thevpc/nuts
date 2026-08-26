package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NByteQueue;
import net.thevpc.nuts.util.NMatchType;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class DefaultNByteQueue implements NByteQueue {

    private byte[] content;
    private final int increment;
    private int from;
    private int to;
    private boolean eof;
    private final Map<String, Pattern> cachedPatterns = new HashMap<>();

    public static NByteQueue of() {
        return new DefaultNByteQueue(256);
    }

    public static NByteQueue of(int size) {
        return new DefaultNByteQueue(size <= 0 ? 256 : size);
    }

    public static NByteQueue of(byte[] content) {
        return new DefaultNByteQueue(content, -1, -1);
    }

    public DefaultNByteQueue() {
        this(256);
    }

    public DefaultNByteQueue(int initial) {
        this((initial <= 0 ? 256 : initial), Math.min((initial <= 0 ? 256 : initial), 256));
    }

    public DefaultNByteQueue(int initial, int increment) {
        content = new byte[(initial <= 0 ? 256 : initial)];
        this.increment = increment<=0?Math.min(content.length, 256):increment;
    }

    public DefaultNByteQueue(byte[] data, int initial, int increment) {
        int len = data.length;
        if (initial <= 0) {
            if (len == 0) {
                initial = 256;
            } else {
                initial = len;
            }
        }
        if (increment <= 0) {
            increment = Math.min(initial, 256);
        }
        this.content = new byte[initial];
        this.increment = increment;
        this.to = len;
        if (len > 0) {
            System.arraycopy(data, 0, this.content, 0, len);
        }
    }

    @Override
    public int write(InputStream reader, int max) {
        byte[] all = new byte[max];
        int count = 0;
        try {
            count = reader.read(all);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (count > 0) {
            write(all, 0, count);
        }
        return count;
    }

    @Override
    public void write(ByteBuffer c) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        int n = c.remaining();
        if (n == 0) {
            return;
        }
        ensureAvailable(n);
        c.get(content, to, n);
        to += n;
    }


    @Override
    public synchronized void write(byte[] c) {
        write(c, 0, c.length);
    }

    @Override
    public synchronized void write(byte[] c, int offset, int len) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        ensureAvailable(len);
        try {
            System.arraycopy(c, offset, content, to, len);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw e;
        }
        to += len;
    }

    @Override
    public synchronized void write(byte c) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        ensureAvailable(1);
        content[to++] = c;
    }

    @Override
    public boolean isEmpty() {
        return to - from <= 0;
    }

    @Override
    public int length() {
        return to - from;
    }

    @Override
    public byte peek() {
        if (to > from) {
            return content[from];
        }
        throw new UncheckedIOException(new EOFException());
    }


    @Override
    public byte peekAt(int index) {
        if (index >= 0 && index < length()) {
            return content[from + index];
        }
        throw new UncheckedIOException(new EOFException());
    }

    @Override
    public String peek(int count) {
        int c = length();
        if (count < c) {
            return new String(content, from, count);
        } else {
            return new String(content, from, c);
        }
    }

    @Override
    public boolean canRead() {
        return from < to;
    }

    @Override
    public boolean canReadByCount(int count) {
        return from + count - 1 < to;
    }

    @Override
    public String read(int count) {
        if (from + count < to) {
            String s = new String(content, from, count);
            from += count;
            return s;
        } else {
            String s = new String(content, from, to - from);
            from = 0;
            to = 0;
            return s;
        }
    }

    @Override
    public void skip(int count) {
        if (from + count < to) {
            from += count;
        } else {
            from = 0;
            to = 0;
        }
    }

    @Override
    public NMatchType skipValue(String value) {
        int count = value.length();
        if (from + count < to) {
            for (int i = 0; i < count; i++) {
                if (value.charAt(i) != content[from + i]) {
                    return NMatchType.NO_MATCH;
                }
            }
            skip(count);
            return NMatchType.FULL_MATCH;
        }
        for (int i = 0; i < to; i++) {
            if (value.charAt(i) != content[from + i]) {
                return NMatchType.NO_MATCH;
            }
        }
        return NMatchType.PARTIAL_MATCH;
    }


    private Pattern pattern(String pattern) {
        return Pattern.compile("^" + pattern);
//        return cachedPatterns.computeIfAbsent("^" + pattern, Pattern::compile);
    }

    @Override
    public void clear() {
        from = 0;
        to = 0;
    }

    @Override
    public byte read() {
        if (canRead()) {
            return content[from++];
        }
        throw new UncheckedIOException(new EOFException());
    }

    @Override
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

    @Override
    public byte byteAt(int index) {
        if (index >= 0 && index < length()) {
            return content[from + index];
        }
        throw new IndexOutOfBoundsException("invalid index " + index);
    }

    @Override
    public IntStream bytes() {
        return toString().chars();
    }

    @Override
    public boolean hasNext() {
        return to > from;
    }

    @Override
    public boolean isEOF() {
        return eof;
    }

    @Override
    public void eof(boolean eof) {
        this.eof = eof;
    }

    @Override
    public int increment() {
        return increment;
    }

    @Override
    public int from() {
        return from;
    }

    @Override
    public int to() {
        return to;
    }

    @Override
    public int allocatedSize() {
        return content.length;
    }
}
