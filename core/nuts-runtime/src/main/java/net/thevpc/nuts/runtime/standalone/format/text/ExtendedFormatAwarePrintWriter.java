package net.thevpc.nuts.runtime.standalone.format.text;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSessionAware;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceAware;
import net.thevpc.nuts.runtime.standalone.format.text.util.FormattedPrintStreamUtils;
import net.thevpc.nuts.runtime.standalone.io.NutsTerminalModeOp;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

public class ExtendedFormatAwarePrintWriter extends PrintWriter implements ExtendedFormatAware, NutsSessionAware {
    private NutsWorkspace ws;
    private NutsSession session;
    private Object base = null;

    public ExtendedFormatAwarePrintWriter(Writer out) {
        super(out);
        base = out;
    }

    public ExtendedFormatAwarePrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
        base = out;
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out) {
        super(out);
        base = out;
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        base = out;
    }

    @Override
    public void setSession(NutsSession session) {
        this.session = session;
        this.ws = session==null?null:session.getWorkspace();
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        if (base instanceof ExtendedFormatAware) {
            return ((ExtendedFormatAware) base).getModeOp();
        }
        return NutsTerminalModeOp.NOP;
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
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
                return new FormatOutputStream(new SimpleWriterOutputStream(this, session), ws);
            }
            case FILTER: {
                return new FilterFormatOutputStream(new SimpleWriterOutputStream(this, session), ws);
            }
            case ESCAPE: {
                return new EscapeOutputStream(new SimpleWriterOutputStream(this, session), ws);
            }
            case UNESCAPE: {
                return new EscapeOutputStream(new SimpleWriterOutputStream(this, session), ws);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(session, Locale.getDefault(), format, args));
        return this;
    }

    @Override
    public ExtendedFormatAwarePrintWriter format(Locale l, String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(session, l, format, args));
        return this;
    }
}
