package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

public class ExtendedFormatAwarePrintWriter extends PrintWriter implements ExtendedFormatAware {
    private final NWorkspace workspace;
    private final NSystemTerminalBase term;
    private Object base = null;

    public ExtendedFormatAwarePrintWriter(Writer out, NSystemTerminalBase term, NWorkspace workspace) {
        super(out);
        base = out;
        this.workspace = workspace;
        this.term = term;
    }

    public ExtendedFormatAwarePrintWriter(Writer out, boolean autoFlush, NSystemTerminalBase term, NWorkspace workspace) {
        super(out, autoFlush);
        base = out;
        this.workspace = workspace;
        this.term = term;
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out, NSystemTerminalBase term, NWorkspace workspace) {
        super(out);
        base = out;
        this.term = term;
        this.workspace = workspace;
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out, boolean autoFlush, NSystemTerminalBase term, NWorkspace workspace) {
        super(out, autoFlush);
        base = out;
        this.workspace = workspace;
        this.term = term;
    }



    @Override
    public NTerminalModeOp getModeOp() {
        if (base instanceof ExtendedFormatAware) {
            return ((ExtendedFormatAware) base).getModeOp();
        }
        return NTerminalModeOp.NOP;
    }

    @Override
    public ExtendedFormatAware convert(NTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        if (base instanceof ExtendedFormatAware) {
            return ((ExtendedFormatAware) base).convert(other);
        }
        switch (other) {
            case NOP: {
                return this;
            }
            case FORMAT: {
                return new FormatOutputStream(new SimpleWriterOutputStream(this, term, workspace), term);
            }
            case FILTER: {
                return new FilterFormatOutputStream(new SimpleWriterOutputStream(this, term, workspace), term);
            }
            case ESCAPE: {
                return new EscapeOutputStream(new SimpleWriterOutputStream(this, term, workspace), term);
            }
            case UNESCAPE: {
                return new EscapeOutputStream(new SimpleWriterOutputStream(this, term, workspace), term);
            }
        }
        throw new NUnsupportedEnumException(other);
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        return format(null, format, args);
    }

    @Override
    public ExtendedFormatAwarePrintWriter format(Locale l, String format, Object... args) {
        if (l == null) {
            print(NText.of(
                    NMsg.ofC(
                            format, args
                    )
            ));
        } else {
            print(
                    NText.of(
                            NMsg.ofC(
                                    format, args
                            )
                    )
            );
        }
        return this;
    }
}
