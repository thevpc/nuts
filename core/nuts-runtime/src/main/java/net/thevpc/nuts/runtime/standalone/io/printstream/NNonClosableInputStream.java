package net.thevpc.nuts.runtime.standalone.io.printstream;

import java.io.IOException;
import java.io.InputStream;


public class NNonClosableInputStream extends InputStream {
    private InputStream base;

    public NNonClosableInputStream(InputStream base) {
        this.base = base;
    }

    @Override
    public int read() throws IOException {
        return base.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return base.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return base.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return base.skip(n);
    }

    @Override
    public int available() throws IOException {
        return base.available();
    }

    @Override
    public void close() throws IOException {
        //
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
