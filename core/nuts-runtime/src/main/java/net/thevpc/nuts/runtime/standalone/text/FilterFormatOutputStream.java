package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.OutputStream;

public class FilterFormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {
    public FilterFormatOutputStream(OutputStream out, NSystemTerminalBase term, NSession session) {
        super(out, term, true, session);
    }


    @Override
    public NTerminalModeOp getModeOp() {
        return NTerminalModeOp.FILTER;
    }

    @Override
    public ExtendedFormatAware convert(NTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                if (out instanceof ExtendedFormatAware) {
                    NTerminalModeOp m = ((ExtendedFormatAware) out).getModeOp();
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
        throw new NUnsupportedEnumException(session, other);
    }

}
