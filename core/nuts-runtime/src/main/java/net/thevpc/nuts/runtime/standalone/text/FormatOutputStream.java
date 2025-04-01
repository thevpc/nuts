package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOpUtils;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.OutputStream;

public class FormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {

    public FormatOutputStream(OutputStream out, NSystemTerminalBase term) {
        super(out, term, false);
        NTerminalModeOp op = NTerminalModeOpUtils.resolveNutsTerminalModeOp(out);
        if (op != NTerminalModeOp.NOP) {
            throw new NIllegalArgumentException(NMsg.ofPlain("expected Raw"));
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
                return new RawOutputStream(out, getTerminal());
            }
            case FORMAT: {
                return this;
            }
            case FILTER: {
                return new FilterFormatOutputStream(out, getTerminal());
            }
            case ESCAPE: {
                return new EscapeOutputStream(this, getTerminal());
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this, getTerminal());
            }
        }
        throw new NUnsupportedEnumException(other);
    }

}
