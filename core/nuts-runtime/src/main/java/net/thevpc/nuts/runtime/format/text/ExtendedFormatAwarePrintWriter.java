package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceAware;
import net.thevpc.nuts.runtime.format.text.util.FormattedPrintStreamUtils;
import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;

import java.io.*;
import java.util.Locale;

public class ExtendedFormatAwarePrintWriter extends PrintWriter implements ExtendedFormatAware, NutsWorkspaceAware {
    private NutsWorkspace ws;
    private Object base=null;

    public ExtendedFormatAwarePrintWriter(Writer out) {
        super(out);
        base=out;
    }

    public ExtendedFormatAwarePrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
        base=out;
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out) {
        super(out);
        base=out;
    }

    public ExtendedFormatAwarePrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        base=out;
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws = workspace;
    }

    @Override
    public ExtendedFormatAwarePrintWriter format(Locale l, String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws, l, format, args));
        return this;
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws, Locale.getDefault(), format, args));
        return this;
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        if(base instanceof ExtendedFormatAware){
            return ((ExtendedFormatAware) base).getModeOp();
        }
        return NutsTerminalModeOp.NOP;
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if(other==null || other==getModeOp()){
            return this;
        }
        if(base instanceof ExtendedFormatAware){
            return ((ExtendedFormatAware) base).convert(other);
        }
        switch (other){
            case NOP:{
                return this;
            }
            case FORMAT:{
                return new FormatOutputStream(new SimpleWriterOutputStream(this,ws),ws);
            }
            case FILTER:{
                return new FilterFormatOutputStream(new SimpleWriterOutputStream(this,ws),ws);
            }
            case ESCAPE:{
                return new EscapeOutputStream(new SimpleWriterOutputStream(this,ws),ws);
            }
            case UNESCAPE:{
                return new EscapeOutputStream(new SimpleWriterOutputStream(this,ws),ws);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }
}
