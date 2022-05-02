package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.runtime.standalone.io.outputstream.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.NutsTerminalModeOpUtils;
import net.thevpc.nuts.runtime.standalone.io.terminal.NutsTerminalModeOp;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.text.NutsTexts;

import java.io.IOException;
import java.io.OutputStream;

public class UnescapeOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {

    private NutsSession session;
    private NutsSystemTerminalBase term;

    public UnescapeOutputStream(OutputStream out, NutsSystemTerminalBase term,NutsSession session) {
        super(out);
        this.session = session;
        this.term = term;
        NutsTerminalModeOp t = NutsTerminalModeOpUtils.resolveNutsTerminalModeOp(out);
        if (t.in() != NutsTerminalMode.FORMATTED && t.in() != NutsTerminalMode.FILTERED) {
            throw new IllegalArgumentException("Illegal Formatted");
        }
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        return NutsTerminalModeOp.UNESCAPE;
    }

    public OutputStream getOut() {
        return out;
    }

    private String filterThanEscape(String b) throws IOException {
        NutsTexts txt = NutsTexts.of(session);
        String filtered = txt.builder().append(b).filteredText();
        return txt.ofPlain(filtered).toString();
//        return ws.text().escapeText(
//                ws.text().filterText(b)
//        );
    }

    @Override
    public void write(int b) throws IOException {
        out.write(filterThanEscape(Character.toString((char) b)).getBytes());
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] bytes = filterThanEscape(new String(b, off, len)).getBytes();
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
                return new RawOutputStream(out, term,session);
            }
            case FORMAT: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.FORMAT);
                }
                return new FormatOutputStream(out, term,session);
            }
            case FILTER: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.FILTER);
                }
                return this;//new FilterFormatOutputStream(out);
            }
            case ESCAPE: {
                return ((ExtendedFormatAware) out);
            }
            case UNESCAPE: {
                return ((ExtendedFormatAware) out);
            }
        }
        throw new NutsUnsupportedEnumException(session, other);
    }
}
