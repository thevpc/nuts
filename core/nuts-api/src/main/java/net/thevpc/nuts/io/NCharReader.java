package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NExceptions;

import java.io.*;

public class NCharReader extends Reader {
    private final Reader in;
    private final char[] buffer;
    private final boolean autoClose;
    private int pos = 0;    // current position in buffer
    private int limit = 0;  // number of chars available in buffer

    public NCharReader(Reader reader) {
        this(reader, 1024, true);
    }

    public NCharReader(Reader reader, int bufferSize, boolean autoClose) {
        this.in = reader;
        this.buffer = new char[bufferSize];
        this.autoClose = autoClose;
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
            int n = 0;
            try {
                n = in.read(buffer, limit, buffer.length - limit);
            } catch (IOException e) {
                throw NExceptions.ofSafeIOException(e);
            }
            if (n < 0) break;
            limit += n;
        }
    }

    public int peekAt(int offset) {
        fill(offset + 1);
        int index = pos + offset;
        return (index < limit) ? buffer[index] : -1;
    }

    public String read(int count) {
        char[] c = new char[count];
        int v = read(c);
        return new String(c, 0, v);
    }

    public boolean canRead() {
        fill(1);
        return limit - pos > 0;
    }

    public boolean canRead(int count) {
        fill(count);
        return limit - pos >= count;
    }

    public int read(char[] buffer) {
        return read(buffer, 0, buffer.length);
    }

    public int read(char[] buffer, int offset, int count) {
        fill(count);
        int available = Math.min(count, limit - pos);
        if (available <= 0) return -1;

        System.arraycopy(this.buffer, pos, buffer, offset, available);
        pos += available;
        return available;
    }

    public boolean read(String text) {
        if (text == null || text.isEmpty()) return true;
        fill(text.length());
        if (limit - pos < text.length()) return false;
        for (int i = 0; i < text.length(); i++) {
            if (buffer[pos + i] != text.charAt(i)) return false;
        }
        pos += text.length();
        return true;
    }

    public boolean peek(String text) {
        if (text == null || text.isEmpty()) return true;
        fill(text.length());
        if (limit - pos < text.length()) return false;
        for (int i = 0; i < text.length(); i++) {
            if (buffer[pos + i] != text.charAt(i)) return false;
        }
        return true;
    }

    public char readChar() {
        fill(1);
        if (pos >= limit) throw NExceptions.ofSafeIOException(new EOFException());
        return buffer[pos++];
    }

    @Override
    public int read() {
        fill(1);
        if (pos >= limit) {
            return -1;
        }
        return buffer[pos++];
    }

    public String peek(int offset, int count) {
        fill(offset + count);
        int available = Math.min(count, limit - pos - offset);
        return new String(buffer, pos + offset, available);
    }

    public int peek() {
        fill(1);
        return pos < limit ? buffer[pos] : -1;
    }

    public String peek(int count) {
        fill(count);
        int available = Math.min(count, limit - pos);
        return new String(buffer, pos, available);
    }

    @Override
    public void close() {
        if (autoClose) {
            try {
                in.close();
            } catch (IOException e) {
                throw NExceptions.ofSafeIOException(e);
            }
        }
    }
}
