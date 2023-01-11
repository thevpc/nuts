package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.io.NOutputStreamAdapter;

import java.io.OutputStream;

public class OutputStreamFromNOutputStream extends OutputStream implements NOutputStreamAdapter {

    private NOutputStream base;

    public OutputStreamFromNOutputStream(NOutputStream base) {
        this.base = base;
    }

    public NOutputStream getBaseOutputStream() {
        return base;
    }

    @Override
    public void write(int b) {
        base.write(b);
    }

    @Override
    public void write(byte[] b) {
        base.print(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        base.print(b, off, len);
    }

    @Override
    public void flush() {
        base.flush();
    }

    @Override
    public void close() {
        base.close();
    }
}
