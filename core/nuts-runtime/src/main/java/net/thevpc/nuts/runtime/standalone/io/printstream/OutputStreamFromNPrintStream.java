package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NPrintStreamAdapter;

import java.io.OutputStream;

public class OutputStreamFromNPrintStream extends OutputStream implements NPrintStreamAdapter {

    private NPrintStream base;

    public OutputStreamFromNPrintStream(NPrintStream base) {
        this.base = base;
    }

    public NPrintStream getBasePrintStream() {
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
