package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;

import java.io.OutputStream;

public class FormatOutputStream extends RenderedOutputStream implements ExtendedFormatAware {
    public FormatOutputStream(OutputStream out,NutsWorkspace ws) {
        super(out,FPrint.RENDERER_ANSI,ws);
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
                return new RawOutputStream(out,ws);
            }
            case FORMAT: {
                return this;
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
