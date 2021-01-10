package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.io.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public class RawOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {
    private NutsWorkspace ws;
    public RawOutputStream(OutputStream out,NutsWorkspace ws) {
        super(out);
        this.ws=ws;
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
                return new FormatOutputStream(out,ws);
            }
            case FILTER: {
                return new FilterFormatOutputStream(out,ws);
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
