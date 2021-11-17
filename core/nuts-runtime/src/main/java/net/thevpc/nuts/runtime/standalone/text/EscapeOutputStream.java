package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.standalone.io.outputstream.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.terminals.NutsTerminalModeOp;

import java.io.IOException;
import java.io.OutputStream;

public class EscapeOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {

    NutsWorkspace ws;
    NutsSession session;

    public EscapeOutputStream(OutputStream out, NutsSession session) {
        super(out);
        this.session = session;
        this.ws = session.getWorkspace();
        NutsTerminalModeOp t = CoreIOUtils.resolveNutsTerminalModeOp(out);
        if (t.in() != NutsTerminalMode.FORMATTED && t.in() != NutsTerminalMode.FILTERED) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("illegal Formatted"));
        }
    }

    public OutputStream getOut() {
        return out;
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        return NutsTerminalModeOp.ESCAPE;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(
                DefaultNutsTextNodeParser.escapeText0(Character.toString((char) b)).getBytes()
        );
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] bytes = DefaultNutsTextNodeParser.escapeText0(new String(b, off, len)).getBytes();
        out.write(bytes, 0, bytes.length);
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.NOP);
                }
                return new RawOutputStream(out, session);
            }
            case FORMAT: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.FORMAT);
                }
                return new FormatOutputStream(out, session);
            }
            case FILTER: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.FILTER);
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
        throw new NutsUnsupportedEnumException(session, other);
    }
}
