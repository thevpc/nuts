package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsWorkspace;

import java.io.IOException;
import java.io.OutputStream;

public class RenderedOutputStream extends OutputStream {
    FormatOutputStreamSupport h;
    OutputStream out;
    NutsWorkspace ws;

    public RenderedOutputStream(OutputStream out, FormattedPrintStreamRenderer renderer,NutsWorkspace ws) {
        this.out=out;
        this.ws=ws;
        h = new FormatOutputStreamSupport(out,renderer,ws);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        h.processBytes(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        h.processByte(b);
    }

    @Override
    public void flush() throws IOException {
        h.flush();
        super.flush();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{" +
                "h=" + h +
                "o=" + out +
                '}';
    }

    public void close() throws IOException {
        try (OutputStream ostream = out) {
            flush();
        }
    }
}
