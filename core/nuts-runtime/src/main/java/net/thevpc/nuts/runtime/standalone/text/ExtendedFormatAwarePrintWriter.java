package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSessionAware;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

public class ExtendedFormatAwarePrintWriter extends PrintWriter implements ExtendedFormatAware, NSessionAware {
    private final NWorkspace ws;
    private NSession session;
    private final NSystemTerminalBase term;
    private Object base = null;

    public ExtendedFormatAwarePrintWriter(Writer out, NSystemTerminalBase term, NSession session) {
        super(out);
        base = out;
        this.session = session;
        this.term = term;
        this.ws = session.getWorkspace();
    }

    public ExtendedFormatAwarePrintWriter(Writer out, boolean autoFlush, NSystemTerminalBase term, NSession session) {
        super(out, autoFlush);
        base = out;
        this.session = session;
        this.term = term;
        this.ws = session.getWorkspace();
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out, NSystemTerminalBase term, NSession session) {
        super(out);
        base = out;
        this.session = session;
        this.term = term;
        this.ws = session.getWorkspace();
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out, boolean autoFlush, NSystemTerminalBase term, NSession session) {
        super(out, autoFlush);
        base = out;
        this.session = session;
        this.term = term;
        this.ws = session.getWorkspace();
    }

    @Override
    public void setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
//        this.session = session;
//        this.ws = session==null?null:session.getWorkspace();
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
                return new FormatOutputStream(new SimpleWriterOutputStream(this, term, session), term, session);
            }
            case FILTER: {
                return new FilterFormatOutputStream(new SimpleWriterOutputStream(this, term, session), term, session);
            }
            case ESCAPE: {
                return new EscapeOutputStream(new SimpleWriterOutputStream(this, term, session), term, session);
            }
            case UNESCAPE: {
                return new EscapeOutputStream(new SimpleWriterOutputStream(this, term, session), term, session);
            }
        }
        throw new NUnsupportedEnumException(session, other);
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
            print(NTexts.of(session).ofText(
                    NMsg.ofC(
                            format, args
                    )
            ));
        } else {
            NSession s2 = this.session.copy().setLocale(l.toString());
            print(
                    NTexts.of(s2).ofText(
                            NMsg.ofC(
                                    format, args
                            )
                    )
            );
        }
        return this;
    }
}
