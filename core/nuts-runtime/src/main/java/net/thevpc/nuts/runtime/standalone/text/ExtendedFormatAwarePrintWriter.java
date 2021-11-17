package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.terminals.NutsTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSessionAware;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

public class ExtendedFormatAwarePrintWriter extends PrintWriter implements ExtendedFormatAware, NutsSessionAware {
    private NutsWorkspace ws;
    private NutsSession session;
    private Object base = null;

    public ExtendedFormatAwarePrintWriter(Writer out,NutsSession session) {
        super(out);
        base = out;
        this.session=session;
        this.ws=session.getWorkspace();
    }

    public ExtendedFormatAwarePrintWriter(Writer out, boolean autoFlush,NutsSession session) {
        super(out, autoFlush);
        base = out;
        this.session=session;
        this.ws=session.getWorkspace();
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out,NutsSession session) {
        super(out);
        base = out;
        this.session=session;
        this.ws=session.getWorkspace();
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out, boolean autoFlush,NutsSession session) {
        super(out, autoFlush);
        base = out;
        this.session=session;
        this.ws=session.getWorkspace();
    }

    @Override
    public void setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
//        this.session = session;
//        this.ws = session==null?null:session.getWorkspace();
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
        throw new NutsUnsupportedEnumException(session, other);
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
            print(NutsTexts.of(session).setSession(session).toText(
                    NutsMessage.cstyle(
                            format, args
                    )
            ));
        }else{
            NutsSession s2 = this.session.copy().setLocale(l.toString());
            print(
                    NutsTexts.of(s2).toText(
                            NutsMessage.cstyle(
                                    format, args
                            )
                    )
            );
        }
        return this;
    }
}
