package net.thevpc.nuts.runtime.standalone.io.printstream;

import java.io.IOException;
import java.io.OutputStream;

public class NNonClosableOutputStream extends OutputStream {
    private OutputStream out;

    public NNonClosableOutputStream(OutputStream out) {
        this.out = out;
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
