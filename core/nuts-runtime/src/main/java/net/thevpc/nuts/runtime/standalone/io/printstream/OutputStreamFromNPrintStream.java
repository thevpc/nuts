package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NPrintStreamAdapter;

import java.io.OutputStream;

public class OutputStreamFromNPrintStream extends OutputStream implements NPrintStreamAdapter {

    private NStream base;

    public OutputStreamFromNPrintStream(NStream base) {
        this.base = base;
    }

    public NStream getBasePrintStream() {
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
