package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPrintStream;

import java.io.OutputStream;

public class OutputStreamFromNutsPrintStream extends OutputStream {

    private NutsPrintStream base;

    public OutputStreamFromNutsPrintStream(NutsPrintStream base) {
        this.base = base;
    }

    public NutsPrintStream getBase() {
        return base;
    }

    @Override
    public void write(int b) {
        base.write(b);
    }

    @Override
    public void write(byte[] b) {
        base.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        base.write(b, off, len);
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
