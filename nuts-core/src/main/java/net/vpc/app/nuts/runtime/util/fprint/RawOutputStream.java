package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.runtime.io.NutsTerminalModeOp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RawOutputStream extends FilterOutputStream implements ExtendedFormatAware {
    public RawOutputStream(OutputStream out) {
        super(out);
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
                return new FormatOutputStream(out);
            }
            case FILTER: {
                return new FilterFormatOutputStream(out);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }
}
