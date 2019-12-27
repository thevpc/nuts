package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.runtime.io.NutsTerminalModeOp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FilterFormatOutputStream extends FilterOutputStream implements ExtendedFormatAware {
    FormatNodeHelper h;

    public FilterFormatOutputStream(OutputStream out) {
        super(out);
        h = new FormatNodeHelper(FPrint.RENDERER_ANSI_STRIPPER);
        h.setRawer(new FormatNodeHelper.Rower() {
            @Override
            public void writeRaw(byte[] buf, int off, int len) throws IOException {
                write0(buf, off, len);
            }
        });
    }

    private void write0(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        h.processBytes(b,off,len);
    }

    @Override
    public void write(int b) throws IOException {
        h.processByte(b);
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        return NutsTerminalModeOp.FILTER;
    }


    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                if (out instanceof ExtendedFormatAware) {
                    NutsTerminalModeOp m = ((ExtendedFormatAware) out).getModeOp();
                    return (ExtendedFormatAware) out;
                }
                return new RawOutputStream(out);
            }
            case FORMAT: {
                return new FormatOutputStream(out);
            }
            case FILTER: {
                return this;//new FilterFormatOutputStream(out);
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
}
