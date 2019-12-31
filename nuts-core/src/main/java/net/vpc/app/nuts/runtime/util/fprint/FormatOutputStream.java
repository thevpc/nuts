package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.runtime.io.NutsTerminalModeOp;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FormatOutputStream extends FilterOutputStream implements ExtendedFormatAware {
    FormatOutputStreamSupport h;

    public FormatOutputStream(OutputStream out) {
        super(out);
        NutsTerminalModeOp op = CoreIOUtils.resolveNutsTerminalModeOp(out);
        if (op != NutsTerminalModeOp.NOP) {
            throw new IllegalArgumentException("Expected Raw");
        }
        h = new FormatOutputStreamSupport(FPrint.RENDERER_ANSI);
        h.setRawOutput(new FormatOutputStreamSupport.RawOutputStream() {
            @Override
            public void writeRaw(byte[] buf, int off, int len) throws IOException {
                writeRaw0(buf, off, len);
            }

            @Override
            public void flushRaw() throws IOException {
                flushRaw0();
            }
        });
    }

    private void writeRaw0(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void flushRaw0() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        h.processBytes(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        h.processByte(b);
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        return NutsTerminalModeOp.FORMAT;
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                if (out instanceof ExtendedFormatAware) {
                    return (ExtendedFormatAware) out;
                }
                return new RawOutputStream(out);
            }
            case FORMAT: {
                return this;
            }
            case FILTER: {
                return new FilterFormatOutputStream(out);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public void flush() throws IOException {
        h.flush();
        super.flush();
    }

    @Override
    public String toString() {
        return "FormatOutputStream{" +
                "h=" + h +
                "o=" + out +
                '}';
    }
}
