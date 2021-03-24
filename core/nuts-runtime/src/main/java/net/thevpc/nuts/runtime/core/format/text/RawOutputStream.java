package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.io.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

import java.io.OutputStream;
import net.thevpc.nuts.NutsSession;

public class RawOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {
    private NutsSession session;
    private NutsWorkspace ws;
    public RawOutputStream(OutputStream out,NutsSession session) {
        super(out);
        this.session=session;
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
                return new FormatOutputStream(out,session);
            }
            case FILTER: {
                return new FilterFormatOutputStream(out,session);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this,session);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this,session);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }
}
