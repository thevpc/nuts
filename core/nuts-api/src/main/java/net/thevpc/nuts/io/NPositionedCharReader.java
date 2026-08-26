package net.thevpc.nuts.io;

import java.io.Reader;

/**
 * NPositionedCharReader class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPositionedCharReader extends NCharReader {
    private int line = 1;
    private int column = 1;
    private long pos = 1;
    private boolean lastWasCR = false;

    /**
     * N positioned char reader.
     *
     * @param reader reader
     * @return n positioned char reader result
     */
    public NPositionedCharReader(Reader reader) {
      /**
       * Super.
       *
       * @param reader reader
       */
        super(reader);
    }

    /**
     * Pos.
     *
     * @return pos result
     */
    public long pos() {
        return pos;
    }

    /**
     * Line.
     *
     * @return line result
     */
    public int line() {
        return line;
    }

    /**
     * Column.
     *
     * @return column result
     */
    public int column() {
        return column;
    }

    @Override
    public int read() {
        int x = super.read();
        if (x < 0) {
            return x;
        }
      /**
       * Advance.
       *
       * @param x x
       */
        advance((char) x);
        return x;
    }

    @Override
    public char readChar() {
        char c = super.readChar();
      /**
       * Advance.
       *
       * @param c c
       */
        advance(c);
        return c;
    }

    @Override
    public int read(char[] buffer, int offset, int count) {
        int n = super.read(buffer, offset, count);
        for (int i = 0; i < n; i++) {
          /**
           * Advance.
           *
           * @param i] i]
           */
            advance(buffer[offset + i]);
        }
        return n;
    }

    /**
     * Advance.
     *
     * @param c c
     * @return advance result
     */
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
