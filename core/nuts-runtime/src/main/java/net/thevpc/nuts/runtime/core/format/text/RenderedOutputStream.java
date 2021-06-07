package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsOutputStreamTransparentAdapter;
import net.thevpc.nuts.NutsWorkspace;

import java.io.IOException;
import java.io.OutputStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.io.OutputStreamHelper;

public class RenderedOutputStream extends OutputStream implements NutsOutputStreamTransparentAdapter {

    FormatOutputStreamSupport h;
    OutputStream out;
    NutsSession session;
    NutsWorkspace ws;

    public RenderedOutputStream(OutputStream out, FormattedPrintStreamRenderer renderer, NutsSession session) {
        this.out = out;
        this.session = session;
        this.ws = session.getWorkspace();
        h = new FormatOutputStreamSupport(
                new OutputStreamHelper(out,session)
                , renderer, session);
    }

    @Override
    public OutputStream baseOutputStream() {
        return out;
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
        return getClass().getSimpleName() + "{"
                + "h=" + h
                + "o=" + out
                + '}';
    }

    public void close() throws IOException {
        try (OutputStream ostream = out) {
            flush();
        }
    }
}
