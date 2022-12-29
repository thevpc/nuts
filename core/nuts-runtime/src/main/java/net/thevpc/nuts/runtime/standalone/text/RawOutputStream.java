package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.io.outputstream.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;

import java.io.OutputStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NSystemTerminalBase;

public class RawOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {
    private NSession session;
    private NWorkspace ws;
    private NSystemTerminalBase term;
    public RawOutputStream(OutputStream out, NSystemTerminalBase term, NSession session) {
        super(out);
        this.session=session;
        this.term=term;
        this.ws=session.getWorkspace();
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
        throw new NUnsupportedEnumException(session, other);
    }
}
