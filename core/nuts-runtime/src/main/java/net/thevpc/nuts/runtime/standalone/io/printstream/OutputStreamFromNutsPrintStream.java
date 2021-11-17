package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsPrintStreamAdapter;

import java.io.OutputStream;

public class OutputStreamFromNutsPrintStream extends OutputStream implements NutsPrintStreamAdapter {

    private NutsPrintStream base;

    public OutputStreamFromNutsPrintStream(NutsPrintStream base) {
        this.base = base;
    }

    public NutsPrintStream getBasePrintStream() {
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
