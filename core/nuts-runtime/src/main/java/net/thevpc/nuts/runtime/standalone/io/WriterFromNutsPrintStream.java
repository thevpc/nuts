package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsPrintStreamAdapter;

import java.io.IOException;
import java.io.Writer;

public class WriterFromNutsPrintStream extends Writer implements NutsPrintStreamAdapter {
    private NutsPrintStream base;

    public WriterFromNutsPrintStream(NutsPrintStream base) {
        this.base = base;
    }

    @Override
    public NutsPrintStream getBaseNutsPrintStream() {
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
