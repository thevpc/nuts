package net.thevpc.nuts.reserved;

import java.io.EOFException;
import java.io.UncheckedIOException;

public final class ReservedSimpleCharQueue {

    private char[] content;
    private int index;

    public ReservedSimpleCharQueue(char[] value) {
        content = value;
    }

    public boolean hasNext() {
        return index < content.length;
    }

    public int length() {
        return content.length - index;
    }

    public char peek() {
        if (index < content.length) {
            return content[index];
        }
        throw new UncheckedIOException(new EOFException());
    }

    public String peek(int count) {
        int c = length();
        if (count < c) {
            return new String(content, index, count);
        } else {
            return new String(content, index, c);
        }
    }

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

    public void skip(int count) {
        if (index + count < content.length) {
            index += count;
        } else {
            index = content.length;
        }
    }

    public char read() {
        if (index < content.length) {
            return content[index++];
        }
        throw new UncheckedIOException(new EOFException());
    }

    public char readAt(int index) {
        if (index >= 0 && index < length()) {
            return content[this.index + index];
        }
        throw new IndexOutOfBoundsException("invalid index " + index);
    }

    public String toString() {
        int c = length();
        return new String(content, index, c);
    }


}
