package net.thevpc.nuts.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * NonClosableInputStream class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NonClosableInputStream extends InputStream implements InputStreamDelegate {
    private InputStream base;

    /**
     * Non closable input stream.
     *
     * @param base base
     * @return non closable input stream result
     */
    public NonClosableInputStream(InputStream base) {
        this.base = base;
    }

    @Override
    public InputStream delegateInputStream() {
        return base;
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
