package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {
    public FormatOutputStream(OutputStream out) {
        super(out,FPrint.RENDERER_ANSI);
        NutsTerminalModeOp op = CoreIOUtils.resolveNutsTerminalModeOp(out);
        if (op != NutsTerminalModeOp.NOP) {
            throw new IllegalArgumentException("Expected Raw");
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
                return new RawOutputStream(out);
            }
            case FORMAT: {
                return this;
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
