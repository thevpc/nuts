package net.thevpc.nuts.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;

public class AutoCloseableReader extends Reader implements AutoCloseable {
    private Reader base;
    private boolean closed;

    public AutoCloseableReader(Reader base) {
        this.base = base;
    }

    @Override
    public int read(char[] chars, int i, int len) throws IOException {
        int r = base.read(chars, i, len);
        if (r == 0 && len > 0) {
            close();
        }
        return r;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        int r = base.read(target);
        if (r == 0 && target.length() > 0) {
            close();
        }
        return r;
    }

    @Override
    public int read() throws IOException {
        int r = base.read();
        if (r < 0) {
            close();
        }
        return r;
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        int r = base.read(cbuf);
        if (r == 0 && cbuf.length > 0) {
            close();
        }
        return r;
    }

    @Override
    public long skip(long n) throws IOException {
        long r = base.skip(n);
        if (r == 0 && n > 0) {
            close();
        }
        return r;
    }

    @Override
    public boolean ready() throws IOException {
        boolean r = base.ready();
        return r;
    }

    @Override
    public boolean markSupported() {
        boolean r = base.markSupported();
        return r;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        base.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        base.reset();
    }

    @Override
    public void close() throws IOException {
        if(!closed) {
            base.close();
            closed=true;
        }
    }
}
