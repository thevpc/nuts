package net.thevpc.nuts.io;

import java.io.IOException;
import java.io.OutputStream;

public class NonClosableOutputStream extends OutputStream implements OutputStreamDelegate {
    private OutputStream out;

    public NonClosableOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public OutputStream getDelegateOutputStream() {
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
