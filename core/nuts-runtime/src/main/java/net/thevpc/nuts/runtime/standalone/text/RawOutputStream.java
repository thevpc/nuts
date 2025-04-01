package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.runtime.standalone.io.outputstream.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;

import java.io.OutputStream;

import net.thevpc.nuts.spi.NSystemTerminalBase;

public class RawOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {
    private NSystemTerminalBase term;
    public RawOutputStream(OutputStream out, NSystemTerminalBase term) {
        super(out);
        this.term=term;
    }

    @Override
    public NTerminalModeOp getModeOp() {
        return NTerminalModeOp.NOP;
    }

    @Override
    public ExtendedFormatAware convert(NTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                return this;
            }
            case FORMAT: {
                return new FormatOutputStream(out,term);
            }
            case FILTER: {
                return new FilterFormatOutputStream(out,term);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this,term);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this,term);
            }
        }
        throw new NUnsupportedEnumException(other);
    }
}
