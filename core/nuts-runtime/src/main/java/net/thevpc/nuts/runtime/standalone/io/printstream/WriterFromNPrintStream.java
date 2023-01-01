package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NPrintStreamAdapter;

import java.io.IOException;
import java.io.Writer;

public class WriterFromNPrintStream extends Writer implements NPrintStreamAdapter {
    private NOutStream base;

    public WriterFromNPrintStream(NOutStream base) {
        this.base = base;
    }

    @Override
    public NOutStream getBasePrintStream() {
        return base;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        base.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        base.flush();
    }

    @Override
    public void close() throws IOException {
        base.close();
    }
}
