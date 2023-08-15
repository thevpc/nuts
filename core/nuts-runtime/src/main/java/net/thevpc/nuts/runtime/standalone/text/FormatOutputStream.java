package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOpUtils;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.OutputStream;

public class FormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {

    public FormatOutputStream(OutputStream out, NSystemTerminalBase term, NSession session) {
        super(out, term, false, session);
        NTerminalModeOp op = NTerminalModeOpUtils.resolveNutsTerminalModeOp(out);
        if (op != NTerminalModeOp.NOP) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("expected Raw"));
        }
    }

    @Override
    public NTerminalModeOp getModeOp() {
        return NTerminalModeOp.FORMAT;
    }

    @Override
    public ExtendedFormatAware convert(NTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                if (out instanceof ExtendedFormatAware) {
                    return (ExtendedFormatAware) out;
                }
                return new RawOutputStream(out, getTerminal(), session);
            }
            case FORMAT: {
                return this;
            }
            case FILTER: {
                return new FilterFormatOutputStream(out, getTerminal(), session);
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
