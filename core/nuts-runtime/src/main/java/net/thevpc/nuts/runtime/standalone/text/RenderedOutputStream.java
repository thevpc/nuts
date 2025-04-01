package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.io.NOutputStreamTransparentAdapter;

import java.io.IOException;
import java.io.OutputStream;

import net.thevpc.nuts.runtime.standalone.io.printstream.NPrintStreamBase;
import net.thevpc.nuts.runtime.standalone.io.printstream.NPrintStreamRaw;
import net.thevpc.nuts.spi.NSystemTerminalBase;

public class RenderedOutputStream extends OutputStream implements NOutputStreamTransparentAdapter {

    FormatOutputStreamSupport h;
    OutputStream out;
    NSystemTerminalBase terminal;

    public RenderedOutputStream(OutputStream out, NSystemTerminalBase terminal, boolean filtered) {
        this.out = out;
        this.terminal=terminal;
        h = new FormatOutputStreamSupport(
                new NPrintStreamRaw(out,true,null,new NPrintStreamBase.Bindings(), terminal)
                , terminal,filtered);
    }

    public NSystemTerminalBase getTerminal() {
        return terminal;
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
