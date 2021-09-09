package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsUnsupportedEnumException;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

import java.io.OutputStream;
import net.thevpc.nuts.NutsSession;

public class FormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {

    public FormatOutputStream(OutputStream out, NutsSession session) {
        super(out, FPrint.RENDERER_ANSI, session);
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
                return new RawOutputStream(out, session);
            }
            case FORMAT: {
                return this;
            }
            case FILTER: {
                return new FilterFormatOutputStream(out, session);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this, session);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this, session);
            }
        }
        throw new NutsUnsupportedEnumException(session, other);
    }

}
