package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsUnsupportedEnumException;
import net.thevpc.nuts.runtime.standalone.io.terminals.NutsTerminalModeOp;

import java.io.OutputStream;
import net.thevpc.nuts.NutsSession;

public class FilterFormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {
    public FilterFormatOutputStream(OutputStream out,NutsSession session) {
        super(out,FPrint.RENDERER_ANSI_STRIPPER,session);
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
                return new RawOutputStream(out,session);
            }
            case FORMAT: {
                return new FormatOutputStream(out,session);
            }
            case FILTER: {
                return this;//new FilterFormatOutputStream(out);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this,session);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this,session);
            }
        }
        throw new NutsUnsupportedEnumException(session, other);
    }

}
