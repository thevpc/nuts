package net.thevpc.nuts.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * NonClosableOutputStream class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NonClosableOutputStream extends OutputStream implements OutputStreamDelegate {
    private OutputStream out;

    /**
     * Non closable output stream.
     *
     * @param out out
     * @return non closable output stream result
     */
    public NonClosableOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public OutputStream delegateOutputStream() {
        return out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.flush();
    }

}
