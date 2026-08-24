package net.thevpc.nuts.io;

import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NException;

import java.io.*;

/**
 * NCharReader class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCharReader extends Reader {
    private final Reader in;
    private final char[] buffer;
    private final boolean autoClose;
    private int pos = 0;    // current position in buffer
    private int limit = 0;  // number of chars available in buffer

    /**
     * N char reader.
     *
     * @param reader reader
     * @return n char reader result
     */
    public NCharReader(Reader reader) {
      /**
       * This.
       *
       * @param reader reader
       * @param 1024 1024
       * @param true true
       */
        this(reader, 1024, true);
    }

    /**
     * N char reader.
     *
     * @param reader reader
     * @param bufferSize buffer size
     * @param autoClose auto close
     * @return n char reader result
     */
    public NCharReader(Reader reader, int bufferSize, boolean autoClose) {
        this.in = reader;
        this.buffer = new char[bufferSize];
        this.autoClose = autoClose;
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
            int n = 0;
            try {
                n = in.read(buffer, limit, buffer.length - limit);
            } catch (IOException e) {
                throw NException.ofSafeIOException(e);
            }
            if (n < 0) break;
            limit += n;
        }
    }

    /**
     * Peek at.
     *
     * @param offset offset
     * @return peek at result
     */
    public int peekAt(int offset) {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(offset + 1);
        int index = pos + offset;
        return (index < limit) ? buffer[index] : -1;
    }

    /**
     * Read.
     *
     * @param count count
     * @return read result
     */
    public String read(int count) {
        char[] c = new char[count];
        int v = read(c);
        if(v<0){
            return "";
        }
        return new String(c, 0, v);
    }

    /**
     * Checks if can read.
     *
     * @return can read result
     */
    public boolean canRead() {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(1);
        return limit - pos > 0;
    }

    /**
     * Checks if can read.
     *
     * @param count count
     * @return can read result
     */
    public boolean canRead(int count) {
      /**
       * Fill.
       *
       * @param count count
       */
        fill(count);
        return limit - pos >= count;
    }

    /**
     * Read.
     *
     * @param buffer buffer
     * @return read result
     */
    public int read(char[] buffer) {
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
    public int read(char[] buffer, int offset, int count) {
      /**
       * Fill.
       *
       * @param count count
       */
        fill(count);
        int available = Math.min(count, limit - pos);
        if (available <= 0) return -1;

        System.arraycopy(this.buffer, pos, buffer, offset, available);
        pos += available;
        return available;
    }

    /**
     * Read.
     *
     * @param text text
     * @return read result
     */
    public boolean read(String text) {
        if (text == null || text.isEmpty()) return true;
      /**
       * Fill.
       *
       * @param text.length() text.length()
       */
        fill(text.length());
        if (limit - pos < text.length()) return false;
        for (int i = 0; i < text.length(); i++) {
            if (buffer[pos + i] != text.charAt(i)) return false;
        }
        pos += text.length();
        return true;
    }

    /**
     * Peek.
     *
     * @param text text
     * @return peek result
     */
    public boolean peek(String text) {
        if (text == null || text.isEmpty()) return true;
      /**
       * Fill.
       *
       * @param text.length() text.length()
       */
        fill(text.length());
        if (limit - pos < text.length()) return false;
        for (int i = 0; i < text.length(); i++) {
            if (buffer[pos + i] != text.charAt(i)) return false;
        }
        return true;
    }

    /**
     * Read char.
     *
     * @return read char result
     */
    public char readChar() {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(1);
        if (pos >= limit) throw NException.ofSafeIOException(new EOFException());
        return buffer[pos++];
    }

    @Override
    public int read() {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(1);
        if (pos >= limit) {
            return -1;
        }
        return buffer[pos++];
    }

    /**
     * Peek.
     *
     * @param offset offset
     * @param count count
     * @return peek result
     */
    public String peek(int offset, int count) {
      /**
       * Fill.
       *
       * @param count count
       */
        fill(offset + count);
        int available = Math.min(count, limit - pos - offset);
        return new String(buffer, pos + offset, available);
    }

    /**
     * Read line.
     *
     * @return read line result
     */
    public String readLine() {
        StringBuilder sb=new StringBuilder();
        while (true) {
            int c = peek();
            if (c == -1 || c == '\n' || c == '\r') {
                break;
            }
            char ch = (char) read();
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Read new line.
     *
     * @return read new line result
     */
    public NNewLineMode readNewLine() {
        int c = this.peek();
        if (c == '\r') {
            this.read();
            if (this.peek() == '\n') {
                this.read();
                return NNewLineMode.CRLF;
            } else {
                return NNewLineMode.CR;
            }
        } else if (c == '\n') {
            this.read();
            return NNewLineMode.LF;
        } else {
            // EOF
            return null;
        }
    }

    /**
     * Peek.
     *
     * @return peek result
     */
    public int peek() {
      /**
       * Fill.
       *
       * @param 1 1
       */
        fill(1);
        return pos < limit ? buffer[pos] : -1;
    }

    /**
     * Peek.
     *
     * @param count count
     * @return peek result
     */
    public String peek(int count) {
      /**
       * Fill.
       *
       * @param count count
       */
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
                throw NException.ofSafeIOException(e);
            }
        }
    }
}
