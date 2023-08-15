package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOpUtils;
import net.thevpc.nuts.runtime.standalone.io.outputstream.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.io.OutputStream;

public class EscapeOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {

    NWorkspace ws;
    NSession session;
    NSystemTerminalBase term;

    public EscapeOutputStream(OutputStream out, NSystemTerminalBase term, NSession session) {
        super(out);
        this.session = session;
        this.term = term;
        this.ws = session.getWorkspace();
        NTerminalModeOp t = NTerminalModeOpUtils.resolveNutsTerminalModeOp(out);
        if (t.in() != NTerminalMode.FORMATTED && t.in() != NTerminalMode.FILTERED) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("illegal Formatted"));
        }
    }

    public OutputStream getOut() {
        return out;
    }

    @Override
    public NTerminalModeOp getModeOp() {
        return NTerminalModeOp.ESCAPE;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(
                DefaultNTexts.escapeText0(Character.toString((char) b)).getBytes()
        );
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] bytes = DefaultNTexts.escapeText0(new String(b, off, len)).getBytes();
        out.write(bytes, 0, bytes.length);
    }

    @Override
    public ExtendedFormatAware convert(NTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NTerminalModeOp.NOP);
                }
                return new RawOutputStream(out, term,session);
            }
            case FORMAT: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NTerminalModeOp.FORMAT);
                }
                return new FormatOutputStream(out, term,session);
            }
            case FILTER: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NTerminalModeOp.FILTER);
                }
                return this;//new FilterFormatOutputStream(out);
            }
            case ESCAPE: {
                return this;//new EscapeOutputStream(this);
            }
            case UNESCAPE: {
                return ((ExtendedFormatAware) out);
            }
        }
        throw new NUnsupportedEnumException(session, other);
    }
}
