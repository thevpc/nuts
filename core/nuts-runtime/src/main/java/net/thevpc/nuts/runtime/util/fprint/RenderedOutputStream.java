package net.thevpc.nuts.runtime.util.fprint;

import java.io.IOException;
import java.io.OutputStream;

public class RenderedOutputStream extends OutputStream {
    FormatOutputStreamSupport h;
    OutputStream out;

    public RenderedOutputStream(OutputStream out, FormattedPrintStreamRenderer renderer) {
        this.out=out;
        h = new FormatOutputStreamSupport(out,renderer);
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
