package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.io.outputstream.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;

import java.io.OutputStream;

import net.thevpc.nuts.spi.NSystemTerminalBase;

public class RawOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {
    private NWorkspace workspace;
    private NSystemTerminalBase term;
    public RawOutputStream(OutputStream out, NSystemTerminalBase term, NWorkspace workspace) {
        super(out);
        this.term=term;
        this.workspace =workspace;
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
                return new FormatOutputStream(out,term,workspace);
            }
            case FILTER: {
                return new FilterFormatOutputStream(out,term, workspace);
            }
            case ESCAPE: {
                return new EscapeOutputStream(this,term,workspace);
            }
            case UNESCAPE: {
                return new UnescapeOutputStream(this,term,workspace);
            }
        }
        throw new NUnsupportedEnumException(other);
    }
}
