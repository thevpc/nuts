package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.io.NOutputStreamAdapter;

import java.io.IOException;
import java.io.Writer;

public class WriterFromNOutputStream extends Writer implements NOutputStreamAdapter {
    private NOutputStream base;

    public WriterFromNOutputStream(NOutputStream base) {
        this.base = base;
    }

    @Override
    public NOutputStream getBaseOutputStream() {
        return base;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        base.print(cbuf, off, len);
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
