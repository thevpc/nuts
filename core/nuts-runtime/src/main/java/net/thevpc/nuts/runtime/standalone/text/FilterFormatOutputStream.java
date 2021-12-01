package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsUnsupportedEnumException;
import net.thevpc.nuts.runtime.standalone.io.terminals.NutsTerminalModeOp;

import java.io.OutputStream;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

public class FilterFormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {
    public FilterFormatOutputStream(OutputStream out, NutsSystemTerminalBase term, NutsSession session) {
        super(out, term, session);
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
                return new RawOutputStream(out, getTerminal(), session);
            }
            case FORMAT: {
                return new FormatOutputStream(out, getTerminal(), session);
            }
            case FILTER: {
                return this;//new FilterFormatOutputStream(out);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this, getTerminal(), session);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this, getTerminal(), session);
            }
        }
        throw new NutsUnsupportedEnumException(session, other);
    }

}
