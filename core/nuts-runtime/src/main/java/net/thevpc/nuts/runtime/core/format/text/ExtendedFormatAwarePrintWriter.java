package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

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
                return new FormatOutputStream(new SimpleWriterOutputStream(this, session), session);
            }
            case FILTER: {
                return new FilterFormatOutputStream(new SimpleWriterOutputStream(this, session), session);
            }
            case ESCAPE: {
                return new EscapeOutputStream(new SimpleWriterOutputStream(this, session), session);
            }
            case UNESCAPE: {
                return new EscapeOutputStream(new SimpleWriterOutputStream(this, session), session);
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
        return format(null,format,args);
    }

    @Override
    public ExtendedFormatAwarePrintWriter format(Locale l, String format, Object... args) {
        if(l==null){
            print(session.getWorkspace().formats().text().setSession(session).toText(
                    NutsMessage.cstyle(
                            format, args
                    )
            ));
        }else{
            NutsSession s2 = this.session.copy().setLocale(l.toString());
            print(
                    s2.getWorkspace().formats().text().setSession(s2).toText(
                            NutsMessage.cstyle(
                                    format, args
                            )
                    )
            );
        }
        return this;
    }
}
