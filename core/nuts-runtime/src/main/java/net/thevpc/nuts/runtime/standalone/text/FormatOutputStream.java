package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsUnsupportedEnumException;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.terminal.NutsTerminalModeOp;

import java.io.OutputStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

public class FormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {

    public FormatOutputStream(OutputStream out, NutsSystemTerminalBase term,NutsSession session) {
        super(out, term, session);
        NutsTerminalModeOp op = CoreIOUtils.resolveNutsTerminalModeOp(out);
        if (op != NutsTerminalModeOp.NOP) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("expected Raw"));
        }
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
                return new RawOutputStream(out, getTerminal(),session);
            }
            case FORMAT: {
                return this;
            }
            case FILTER: {
                return new FilterFormatOutputStream(out, getTerminal(), session);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this,getTerminal(), session);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this, getTerminal(),session);
            }
        }
        throw new NutsUnsupportedEnumException(session, other);
    }

}
