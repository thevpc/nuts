package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPrintStream;

import java.io.IOException;
import java.io.Writer;

public class WriterFromNutsPrintStream extends Writer {
    private NutsPrintStream base;

    public WriterFromNutsPrintStream(NutsPrintStream base) {
        this.base = base;
    }

    public NutsPrintStream getBase() {
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
