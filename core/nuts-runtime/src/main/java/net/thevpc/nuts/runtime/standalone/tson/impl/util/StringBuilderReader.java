package net.thevpc.nuts.runtime.standalone.tson.impl.util;


import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

public class StringBuilderReader extends Reader {

    private StringBuilder str;
    private int length;
    private int next = 0;
    private int mark = 0;

    public StringBuilderReader(StringBuilder s) {
        this.str = s;
        this.length = s.length();
    }

    private void ensureOpen()  {
        if (str == null)
            throw new UncheckedIOException(new IOException("Stream closed"));
    }

    public int read()  {
        ensureOpen();
        if (next >= length)
            return -1;
        return str.charAt(next++);
    }

    public int read(char cbuf[], int off, int len)  {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if (next >= length){
                return -1;
            }
            int n = Math.min(length - next, len);
            str.getChars(next, next + n, cbuf, off);
            next += n;
            return n;
        }
    }

    public long skip(long ns)  {
        ensureOpen();
        if (next >= length)
            return 0;
        // Bound skip by beginning and end of the source
        long n = Math.min(length - next, ns);
        n = Math.max(-next, n);
        next += n;
        return n;
    }

    public boolean ready()  {
        ensureOpen();
        return true;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit)  {
        if (readAheadLimit < 0){
            throw new IllegalArgumentException();
        }
        ensureOpen();
        mark = next;
    }

    public void reset()  {
        ensureOpen();
        next = mark;
    }

    public void close() {
        str = null;
    }
}
