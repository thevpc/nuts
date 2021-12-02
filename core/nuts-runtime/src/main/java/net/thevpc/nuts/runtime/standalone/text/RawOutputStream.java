package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsUnsupportedEnumException;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.io.outputstream.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.NutsTerminalModeOp;

import java.io.OutputStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

public class RawOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {
    private NutsSession session;
    private NutsWorkspace ws;
    private NutsSystemTerminalBase term;
    public RawOutputStream(OutputStream out, NutsSystemTerminalBase term, NutsSession session) {
        super(out);
        this.session=session;
        this.term=term;
        this.ws=session.getWorkspace();
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        return NutsTerminalModeOp.NOP;
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                return this;
            }
            case FORMAT: {
                return new FormatOutputStream(out,term,session);
            }
            case FILTER: {
                return new FilterFormatOutputStream(out,term, session);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this,term,session);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this,term,session);
            }
        }
        throw new NutsUnsupportedEnumException(session, other);
    }
}
