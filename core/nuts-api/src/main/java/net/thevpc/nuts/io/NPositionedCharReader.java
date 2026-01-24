package net.thevpc.nuts.io;

import java.io.Reader;

public class NPositionedCharReader extends NCharReader {
    private int line = 1;
    private int column = 1;
    private long pos = 1;
    private boolean lastWasCR = false;

    public NPositionedCharReader(Reader reader) {
        super(reader);
    }

    public long pos() {
        return pos;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    @Override
    public int read() {
        int x = super.read();
        if (x < 0) {
            return x;
        }
        advance((char) x);
        return x;
    }

    @Override
    public char readChar() {
        char c = super.readChar();
        advance(c);
        return c;
    }

    @Override
    public int read(char[] buffer, int offset, int count) {
        int n = super.read(buffer, offset, count);
        for (int i = 0; i < n; i++) {
            advance(buffer[offset + i]);
        }
        return n;
    }

    private void advance(char c) {
        if (c == '\n') {
            if (!lastWasCR) line++;
            column = 1;
            lastWasCR = false;
        } else if (c == '\r') {
            line++;
            column = 1;
            lastWasCR = true;
        } else {
            column++;
            lastWasCR = false;
        }
        pos++;
    }
}
