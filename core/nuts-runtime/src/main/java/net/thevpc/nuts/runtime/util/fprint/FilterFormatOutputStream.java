package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FilterFormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {
    public FilterFormatOutputStream(OutputStream out) {
        super(out,FPrint.RENDERER_ANSI_STRIPPER);
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
