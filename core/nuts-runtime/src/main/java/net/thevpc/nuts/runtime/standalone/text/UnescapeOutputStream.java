package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.io.outputstream.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOpUtils;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTexts;

import java.io.IOException;
import java.io.OutputStream;

public class UnescapeOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {

    private NWorkspace workspace;
    private NSystemTerminalBase term;

    public UnescapeOutputStream(OutputStream out, NSystemTerminalBase term, NWorkspace workspace) {
        super(out);
        this.workspace = workspace;
        this.term = term;
        NTerminalModeOp t = NTerminalModeOpUtils.resolveNutsTerminalModeOp(out);
        if (t.in() != NTerminalMode.FORMATTED && t.in() != NTerminalMode.FILTERED) {
            throw new IllegalArgumentException("Illegal Formatted");
        }
    }

    @Override
    public NTerminalModeOp getModeOp() {
        return NTerminalModeOp.UNESCAPE;
    }

    public OutputStream getOut() {
        return out;
    }

    private String filterThanEscape(String b) throws IOException {
        NTexts txt = NTexts.of();
        String filtered = txt.ofBuilder().append(b).filteredText();
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
    public ExtendedFormatAware convert(NTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NTerminalModeOp.NOP);
                }
                return new RawOutputStream(out, term, workspace);
            }
            case FORMAT: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NTerminalModeOp.FORMAT);
                }
                return new FormatOutputStream(out, term, workspace);
            }
            case FILTER: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NTerminalModeOp.FILTER);
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
        throw new NUnsupportedEnumException(other);
    }
}
