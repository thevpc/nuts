package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

import java.io.OutputStream;

public class FilterFormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {
    public FilterFormatOutputStream(OutputStream out,NutsWorkspace ws) {
        super(out,FPrint.RENDERER_ANSI_STRIPPER,ws);
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
                return new RawOutputStream(out,ws);
            }
            case FORMAT: {
                return new FormatOutputStream(out,ws);
            }
            case FILTER: {
                return this;//new FilterFormatOutputStream(out);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this,ws);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this,ws);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

}
