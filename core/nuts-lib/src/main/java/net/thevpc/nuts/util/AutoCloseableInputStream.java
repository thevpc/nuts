package net.thevpc.nuts.util;

import java.io.IOException;
import java.io.InputStream;

public class AutoCloseableInputStream extends java.io.InputStream implements AutoCloseable {
    private java.io.InputStream base;
    private boolean closed;

    public AutoCloseableInputStream(InputStream base) {
        this.base = base;
    }

    @Override
    public int read() throws IOException {
        int r = base.read();
        return r;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int r = super.read(b);
        if (r < 0) {
            close();
        }
        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = super.read(b, off, len);
        if (r == 0 && len > 0) {
            close();
        }
        return r;
    }

    @Override
    public long skip(long n) throws IOException {
        long r = super.skip(n);
        if (r == 0 && n > 0) {
            close();
        }
        return r;
    }

    @Override
    public int available() throws IOException {
        return super.available();
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            base.close();
            closed = true;
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        base.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        base.reset();
    }

    @Override
    public boolean markSupported() {
        return base.markSupported();
    }
}
