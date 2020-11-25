package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.runtime.util.fprint.parser.DefaultTextNodeParser;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UnescapeOutputStream extends FilterOutputStream implements ExtendedFormatAware {

    public UnescapeOutputStream(OutputStream out) {
        super(out);
        NutsTerminalModeOp t = CoreIOUtils.resolveNutsTerminalModeOp(out);
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

    @Override
    public void write(int b) throws IOException {
        out.write(
                DefaultTextNodeParser.escapeText0(
                        DefaultTextNodeParser.filterText0(Character.toString((char) b))
                ).getBytes()
        );
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] bytes = DefaultTextNodeParser.escapeText0(DefaultTextNodeParser.filterText0(new String(b, off, len)))
                .getBytes();
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
                return new RawOutputStream(out);
            }
            case FORMAT: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.FORMAT);
                }
                return new FormatOutputStream(out);
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
        throw new IllegalArgumentException("Unsupported");
    }
}
