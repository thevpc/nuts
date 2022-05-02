package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsPrintStreamAdapter;

import java.io.IOException;
import java.io.Writer;

public class WriterFromNutsPrintStream extends Writer implements NutsPrintStreamAdapter {
    private NutsPrintStream base;

    public WriterFromNutsPrintStream(NutsPrintStream base) {
        this.base = base;
    }

    @Override
    public NutsPrintStream getBasePrintStream() {
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
